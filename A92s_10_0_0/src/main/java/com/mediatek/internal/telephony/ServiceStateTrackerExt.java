package com.mediatek.internal.telephony;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.Rlog;
import mediatek.telephony.MtkServiceState;

public class ServiceStateTrackerExt implements IServiceStateTrackerExt {
    private static final int CARD_TYPE_CSIM = 2;
    private static final int CARD_TYPE_NONE = 0;
    private static final int CARD_TYPE_RUIM = 4;
    private static final int CARD_TYPE_USIM = 1;
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"vendor.gsm.ril.fulluicctype", "vendor.gsm.ril.fulluicctype.2", "vendor.gsm.ril.fulluicctype.3", "vendor.gsm.ril.fulluicctype.4"};
    static final String TAG = "SSTExt";
    protected Context mContext;

    public ServiceStateTrackerExt() {
    }

    public ServiceStateTrackerExt(Context context) {
        this.mContext = context;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public String onUpdateSpnDisplay(String plmn, MtkServiceState ss, int phoneId) {
        return plmn;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean isImeiLocked() {
        return false;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean isBroadcastEmmrrsPsResume(int value) {
        return false;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean needEMMRRS() {
        return false;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean needSpnRuleShowPlmnOnly() {
        if (SystemProperties.get("ro.vendor.mtk_cta_support").equals("1")) {
            return true;
        }
        return false;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean needBrodcastAcmt(int errorType, int errorCause) {
        return false;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean needRejectCauseNotification(int cause) {
        return false;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean needIgnoreFemtocellUpdate(int state, int cause) {
        return false;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean needToShowCsgId() {
        return true;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean needBlankDisplay(int cause) {
        return false;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean needIgnoredState(int state, int newState, int cause) {
        if (state == 0 && newState == 2) {
            Rlog.i(TAG, "set dontUpdateNetworkStateFlag for searching state");
            return true;
        }
        if (cause != -1) {
            if (state == 0 && newState == 3 && cause != 0) {
                Rlog.i(TAG, "set dontUpdateNetworkStateFlag for REG_DENIED with cause");
                return true;
            } else if (state == 0 && newState == 0 && cause != 0) {
                Rlog.i(TAG, "set dontUpdateNetworkStateFlag for NOT_REG_AND_NOT_SEARCH with cause");
                return true;
            }
        }
        Rlog.i(TAG, "clear dontUpdateNetworkStateFlag");
        return false;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean operatorDefinedInternationalRoaming(String operatorNumeric) {
        return false;
    }

    public void log(String text) {
        Rlog.d(TAG, text);
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean allowSpnDisplayed() {
        return true;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public int needAutoSwitchRatMode(int phoneId, String nwPlmn) {
        return -1;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean isSupportRatBalancing() {
        return false;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean isNeedDisableIVSR() {
        return false;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public String onUpdateSpnDisplayForIms(String plmn, MtkServiceState ss, int lac, int phoneId, Object simRecords) {
        return plmn;
    }

    private boolean isCdmaLteDcSupport() {
        if (SystemProperties.get("ro.vendor.mtk_c2k_lte_mode").equals("1") || SystemProperties.get("ro.vendor.mtk_c2k_lte_mode").equals(MtkGsmCdmaPhone.ACT_TYPE_UTRAN)) {
            return true;
        }
        return false;
    }

    private String[] getSupportCardType(int slotId) {
        String[] values = null;
        if (slotId >= 0) {
            String[] strArr = PROPERTY_RIL_FULL_UICC_TYPE;
            if (slotId < strArr.length) {
                String prop = SystemProperties.get(strArr[slotId], "");
                if (!prop.equals("") && prop.length() > 0) {
                    values = prop.split(",");
                }
                StringBuilder sb = new StringBuilder();
                sb.append("getSupportCardType slotId ");
                sb.append(slotId);
                sb.append(", prop value= ");
                sb.append(prop);
                sb.append(", size= ");
                sb.append(values != null ? values.length : 0);
                log(sb.toString());
                return values;
            }
        }
        log("getSupportCardType: invalid slotId " + slotId);
        return null;
    }

    private boolean isCdma4GCard(int slotId) {
        int cardType = 0;
        String[] values = getSupportCardType(slotId);
        if (values == null) {
            log("isCdma4GCard, get non support card type");
            return false;
        }
        for (int i = 0; i < values.length; i++) {
            if ("USIM".equals(values[i])) {
                cardType |= 1;
            } else if ("RUIM".equals(values[i])) {
                cardType |= 4;
            } else if ("CSIM".equals(values[i])) {
                cardType |= 2;
            }
        }
        log("isCdma4GCard, cardType=" + cardType);
        if (((cardType & 4) > 0 || (cardType & 2) > 0) && (cardType & 1) > 0) {
            return true;
        }
        return false;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean isRoamingForSpecialSIM(String strServingPlmn, String strHomePlmn) {
        boolean cdmaLteSupport = isCdmaLteDcSupport();
        log("isRoamingForSpecialSIM, strServingPlmn: " + strServingPlmn + ", strHomePlmn: " + strHomePlmn + ", cdmaLteSupport = " + cdmaLteSupport);
        if (!cdmaLteSupport || strServingPlmn == null || strServingPlmn.startsWith("460")) {
            return false;
        }
        if (!"45403".equals(strHomePlmn) && !"45404".equals(strHomePlmn)) {
            return false;
        }
        Rlog.d(TAG, "special SIM, force roaming. IMSI:" + strHomePlmn);
        return true;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean showEccForIms() {
        return false;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public boolean getMtkRsrpOnly() {
        return true;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public int[] getMtkLteRsrpThreshold() {
        return null;
    }

    @Override // com.mediatek.internal.telephony.IServiceStateTrackerExt
    public int[] getMtkLteRssnrThreshold() {
        return null;
    }
}
