package com.mediatek.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

public class MtkLteDataOnlyController {
    private static final String ACTION_CHECK_PERMISSISON_SERVICE = "com.mediatek.intent.action.LTE_DATA_ONLY_MANAGER";
    public static final int CDMA3G_SIM = 3;
    public static final int CDMA4G_SIM = 4;
    public static final int CDMA_SIM = 5;
    private static final String CHECK_PERMISSION_SERVICE_PACKAGE = "com.android.phone";
    private static final String CSIM = "CSIM";
    public static final int ERROR_SIM = -1;
    public static final int GSM_SIM = 2;
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"vendor.gsm.ril.fulluicctype", "vendor.gsm.ril.fulluicctype.2", "vendor.gsm.ril.fulluicctype.3", "vendor.gsm.ril.fulluicctype.4"};
    private static final String RUIM = "RUIM";
    private static final String SIM = "SIM";
    public static final int SVLTE_RAT_MODE_3G = 1;
    public static final int SVLTE_RAT_MODE_4G = 0;
    public static final int SVLTE_RAT_MODE_4G_DATA_ONLY = 2;
    private static final String TAG = "MtkLteDataOnlyController";
    private static final String USIM = "USIM";
    private Context mContext;

    public MtkLteDataOnlyController(Context context) {
        this.mContext = context;
    }

    public boolean checkPermission() {
        if (!isSupportTddDataOnlyCheck() || !is4GDataOnly()) {
            return true;
        }
        startService();
        return false;
    }

    public boolean checkPermission(int subId) {
        int slotId = SubscriptionManager.getSlotIndex(subId);
        int cdmaSlotId = SystemProperties.getInt("persist.vendor.radio.cdma_slot", -1) - 1;
        Rlog.d(TAG, "checkPermission subId=" + subId + ", slotId=" + slotId + " cdmaSlotId=" + cdmaSlotId);
        if (cdmaSlotId == slotId) {
            return checkPermission();
        }
        return true;
    }

    private void startService() {
        int[] subId = SubscriptionManager.getSubId(getCdmaSlot());
        Intent serviceIntent = new Intent(ACTION_CHECK_PERMISSISON_SERVICE);
        serviceIntent.setPackage(CHECK_PERMISSION_SERVICE_PACKAGE);
        if (subId != null) {
            serviceIntent.putExtra("subscription", subId[0]);
        }
        Context context = this.mContext;
        if (context != null) {
            context.startService(serviceIntent);
        }
    }

    private boolean is4GDataOnly() {
        int[] subId;
        if (this.mContext == null || (subId = SubscriptionManager.getSubId(SystemProperties.getInt("persist.vendor.radio.cdma_slot", -1) - 1)) == null) {
            return false;
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        if (Settings.Global.getInt(contentResolver, "preferred_network_mode" + subId[0], MtkRILConstants.PREFERRED_NETWORK_MODE) == 102) {
            return true;
        }
        return false;
    }

    public static String getFullIccCardTypeExt() {
        int slotId = getCdmaSlot();
        if (slotId < 0 || slotId >= PROPERTY_RIL_FULL_UICC_TYPE.length) {
            slotId = 0;
        }
        String cardType = SystemProperties.get(PROPERTY_RIL_FULL_UICC_TYPE[slotId]);
        Rlog.d(TAG, "getFullIccCardTypeExt slotId = " + slotId + ",cardType = " + cardType);
        return cardType;
    }

    public static int getSimType() {
        String fullUiccType = getFullIccCardTypeExt();
        if (fullUiccType == null) {
            return -1;
        }
        if (fullUiccType.contains(CSIM) || fullUiccType.contains(RUIM)) {
            if (fullUiccType.contains(CSIM) || fullUiccType.contains(USIM)) {
                return 4;
            }
            if (fullUiccType.contains(SIM)) {
                return 3;
            }
            return 5;
        } else if (fullUiccType.contains(SIM) || fullUiccType.contains(USIM)) {
            return 2;
        } else {
            return -1;
        }
    }

    public static boolean isCdmaCardType() {
        return getSimType() == 4 || getSimType() == 3 || getSimType() == 5;
    }

    public static boolean isCdmaLteCardType() {
        return getSimType() == 4;
    }

    public static boolean isCdma3GCardType() {
        return getSimType() == 3;
    }

    private static int getCdmaSlot() {
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            if (TelephonyManager.getDefault().getCurrentPhoneTypeForSlot(i) == 2) {
                return i;
            }
        }
        return -1;
    }

    private boolean isSupportTddDataOnlyCheck() {
        boolean isCdma4gCard = isCdmaLteCardType();
        boolean isCdmaLteDcSupport = SystemProperties.get("ro.vendor.mtk_c2k_lte_mode").equals("1");
        boolean isSupport4gDataOnly = "1".equals(SystemProperties.get("ro.vendor.mtk_tdd_data_only_support"));
        boolean checkResult = false;
        if (isCdma4gCard && isCdmaLteDcSupport && isSupport4gDataOnly) {
            checkResult = true;
        }
        Rlog.d(TAG, "isCdma4gCard : " + isCdma4gCard + ", isCdmaLteDcSupport : " + isCdmaLteDcSupport + ", isSupport4gDataOnly : " + isSupport4gDataOnly + ", isSupportTddDataOnlyCheck return " + checkResult);
        return checkResult;
    }
}
