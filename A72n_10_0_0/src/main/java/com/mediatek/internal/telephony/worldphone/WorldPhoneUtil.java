package com.mediatek.internal.telephony.worldphone;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ProxyController;
import com.mediatek.internal.telephony.ModemSwitchHandler;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import com.mediatek.internal.telephony.ratconfiguration.RatConfiguration;

public class WorldPhoneUtil implements IWorldPhone {
    private static final int ACTIVE_MD_TYPE_LTG = 4;
    private static final int ACTIVE_MD_TYPE_LWCG = 5;
    private static final int ACTIVE_MD_TYPE_LWG = 3;
    private static final int ACTIVE_MD_TYPE_LfWG = 7;
    private static final int ACTIVE_MD_TYPE_LtTG = 6;
    private static final int ACTIVE_MD_TYPE_TG = 2;
    private static final int ACTIVE_MD_TYPE_UNKNOWN = 0;
    private static final int ACTIVE_MD_TYPE_WG = 1;
    public static final int CARD_TYPE_CSIM = 8;
    public static final int CARD_TYPE_NONE = 0;
    public static final int CARD_TYPE_RUIM = 4;
    public static final int CARD_TYPE_SIM = 1;
    public static final int CARD_TYPE_USIM = 2;
    public static final int CSFB_ON_SLOT = -1;
    private static final boolean IS_WORLD_MODE_SUPPORT;
    private static final int PROJECT_SIM_NUM = TelephonyManager.getDefault().getSimCount();
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"vendor.gsm.ril.fulluicctype", "vendor.gsm.ril.fulluicctype.2", "vendor.gsm.ril.fulluicctype.3", "vendor.gsm.ril.fulluicctype.4"};
    public static final int RADIO_TECH_MODE_CSFB = 2;
    public static final int RADIO_TECH_MODE_SVLTE = 3;
    public static final int RADIO_TECH_MODE_UNKNOWN = 1;
    public static final int SVLTE_ON_SLOT_0 = 0;
    public static final int SVLTE_ON_SLOT_1 = 1;
    public static final String SVLTE_PROP = "persist.vendor.radio.svlte_slot";
    public static final int UTRAN_DIVISION_DUPLEX_MODE_FDD = 1;
    public static final int UTRAN_DIVISION_DUPLEX_MODE_TDD = 2;
    public static final int UTRAN_DIVISION_DUPLEX_MODE_UNKNOWN = 0;
    private static int[] mC2KWPCardtype = new int[TelephonyManager.getDefault().getPhoneCount()];
    private static Phone[] sActivePhones = new Phone[PROJECT_SIM_NUM];
    private static int[] sCardModes = initCardModes();
    private static Context sContext = null;
    private static Phone sDefultPhone = null;
    private static Phone[] sProxyPhones = null;
    public static boolean sSimSwitching = false;
    public static int sToModem = 0;
    private static IWorldPhone sWorldPhone = null;

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.vendor.mtk_md_world_mode_support", 0) != 1) {
            z = false;
        }
        IS_WORLD_MODE_SUPPORT = z;
    }

    public WorldPhoneUtil() {
        logd("Constructor invoked");
        sDefultPhone = PhoneFactory.getDefaultPhone();
        sProxyPhones = PhoneFactory.getPhones();
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            sActivePhones[i] = sProxyPhones[i];
        }
        Phone phone = sDefultPhone;
        if (phone != null) {
            sContext = phone.getContext();
        } else {
            logd("DefaultPhone = null");
        }
    }

    public static void makeWorldPhoneManager() {
        if (isWorldModeSupport() && isWorldPhoneSupport()) {
            logd("Factory World mode support");
            WorldMode.init();
        } else if (isWorldPhoneSupport()) {
            logd("Factory World phone support");
            sWorldPhone = WorldPhoneWrapper.getWorldPhoneInstance();
        } else {
            logd("Factory World phone not support");
        }
    }

    public static IWorldPhone getWorldPhone() {
        if (sWorldPhone == null) {
            logd("sWorldPhone is null");
        }
        return sWorldPhone;
    }

    public static int getProjectSimNum() {
        return PROJECT_SIM_NUM;
    }

    public static int getMajorSim() {
        if (!ProxyController.getInstance().isCapabilitySwitching()) {
            String currMajorSim = SystemProperties.get("persist.vendor.radio.simswitch", "");
            if (currMajorSim == null || currMajorSim.equals("")) {
                logd("[getMajorSim]: fail to get major SIM");
                return -99;
            }
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("[getMajorSim]: ");
                sb.append(Integer.parseInt(currMajorSim) - 1);
                logd(sb.toString());
                return Integer.parseInt(currMajorSim) - 1;
            } catch (NumberFormatException e) {
                Rlog.d(IWorldPhone.LOG_TAG, e.toString());
                return -99;
            } catch (Exception e2) {
                Rlog.d(IWorldPhone.LOG_TAG, e2.toString());
                return -99;
            }
        } else {
            logd("[getMajorSim]: radio capability is switching");
            return -99;
        }
    }

    public static int getModemSelectionMode() {
        if (sContext != null) {
            return SystemProperties.getInt(IWorldPhone.WORLD_PHONE_AUTO_SELECT_MODE, 1);
        }
        logd("sContext = null");
        return 1;
    }

    public static boolean isWorldPhoneSupport() {
        return RatConfiguration.isWcdmaSupported() && RatConfiguration.isTdscdmaSupported();
    }

    public static boolean isLteSupport() {
        return RatConfiguration.isLteFddSupported() || RatConfiguration.isLteTddSupported();
    }

    public static String regionToString(int region) {
        if (region == 0) {
            return "REGION_UNKNOWN";
        }
        if (region == 1) {
            return "REGION_DOMESTIC";
        }
        if (region != 2) {
            return "Invalid Region";
        }
        return "REGION_FOREIGN";
    }

    @Override // com.mediatek.internal.telephony.worldphone.IWorldPhone
    public void setModemSelectionMode(int mode, int modemType) {
    }

    @Override // com.mediatek.internal.telephony.worldphone.IWorldPhone
    public void notifyRadioCapabilityChange(int capailitySimId) {
    }

    public static boolean isWorldModeSupport() {
        return IS_WORLD_MODE_SUPPORT;
    }

    public static int get3GDivisionDuplexMode() {
        int duplexMode;
        switch (getActiveModemType()) {
            case 1:
            case 3:
            case 5:
            case 7:
                duplexMode = 1;
                break;
            case 2:
            case 4:
            case 6:
                duplexMode = 2;
                break;
            default:
                duplexMode = 0;
                break;
        }
        logd("get3GDivisionDuplexMode=" + duplexMode);
        return duplexMode;
    }

    private static int getActiveModemType() {
        int activeMdType = 0;
        int activeMode = -1;
        if (isWorldModeSupport()) {
            int modemType = WorldMode.getWorldMode();
            activeMode = Integer.valueOf(SystemProperties.get("vendor.ril.nw.worldmode.activemode", Integer.toString(0))).intValue();
            switch (modemType) {
                case 8:
                case 16:
                case 20:
                case WorldMode.MD_WORLD_MODE_LFCTG /* 21 */:
                    activeMdType = 4;
                    break;
                case 9:
                case 18:
                    activeMdType = 3;
                    break;
                case 10:
                case 12:
                    if (activeMode > 0) {
                        if (activeMode != 1) {
                            if (activeMode == 2) {
                                activeMdType = 4;
                                break;
                            }
                        } else {
                            activeMdType = 3;
                            break;
                        }
                    }
                    break;
                case 11:
                case 15:
                case WorldMode.MD_WORLD_MODE_LTWCG /* 19 */:
                    activeMdType = 5;
                    break;
                case 13:
                case 17:
                    activeMdType = 6;
                    break;
                case 14:
                    activeMdType = 7;
                    break;
                default:
                    activeMdType = 0;
                    break;
            }
        } else {
            int modemType2 = ModemSwitchHandler.getActiveModemType();
            activeMdType = modemType2 != 3 ? modemType2 != 4 ? modemType2 != 5 ? modemType2 != 6 ? 0 : 4 : 3 : 2 : 1;
        }
        logd("getActiveModemType=" + activeMdType + " activeMode=" + activeMode);
        return activeMdType;
    }

    public static boolean isWorldPhoneSwitching() {
        if (isWorldModeSupport()) {
            return WorldMode.isWorldModeSwitching();
        }
        return false;
    }

    private static int[] initCardModes() {
        try {
            int[] cardModes = new int[TelephonyManager.getDefault().getPhoneCount()];
            String[] svlteType = SystemProperties.get(SVLTE_PROP, "3,2,2,2").split(",");
            for (int i = 0; i < cardModes.length; i++) {
                if (i < svlteType.length) {
                    cardModes[i] = Integer.parseInt(svlteType[i]);
                } else {
                    cardModes[i] = 1;
                }
            }
            return cardModes;
        } catch (NumberFormatException e) {
            Rlog.d(IWorldPhone.LOG_TAG, e.toString());
            return null;
        } catch (Exception e2) {
            Rlog.d(IWorldPhone.LOG_TAG, e2.toString());
            return null;
        }
    }

    private static int getFullCardType(int slotId) {
        if (slotId < 0 || slotId >= TelephonyManager.getDefault().getPhoneCount()) {
            logd("getFullCardType invalid slotId:" + slotId);
            return 0;
        }
        String cardType = SystemProperties.get(PROPERTY_RIL_FULL_UICC_TYPE[slotId]);
        String[] appType = cardType.split(",");
        int fullType = 0;
        for (int i = 0; i < appType.length; i++) {
            if ("USIM".equals(appType[i])) {
                fullType |= 2;
            } else if ("SIM".equals(appType[i])) {
                fullType |= 1;
            } else if ("CSIM".equals(appType[i])) {
                fullType |= 8;
            } else if ("RUIM".equals(appType[i])) {
                fullType |= 4;
            }
        }
        logd("getFullCardType fullType=" + fullType + " cardType =" + cardType);
        return fullType;
    }

    public static int[] getC2KWPCardType() {
        int i = 0;
        while (true) {
            int[] iArr = mC2KWPCardtype;
            if (i >= iArr.length) {
                return iArr;
            }
            iArr[i] = getFullCardType(i);
            logd("getC2KWPCardType mC2KWPCardtype[" + i + "]=" + mC2KWPCardtype[i]);
            i++;
        }
    }

    public static int getActiveSvlteModeSlotId() {
        int svlteSlotId = -1;
        if (!isCdmaLteDcSupport()) {
            logd("[getActiveSvlteModeSlotId] SVLTE not support, return -1.");
            return -1;
        }
        int i = 0;
        while (true) {
            int[] iArr = sCardModes;
            if (i < iArr.length) {
                if (iArr[i] == 3) {
                    svlteSlotId = i;
                }
                i++;
            } else {
                logd("[getActiveSvlteModeSlotId] slotId: " + svlteSlotId);
                return svlteSlotId;
            }
        }
    }

    public static boolean isCdmaLteDcSupport() {
        if (SystemProperties.get("ro.vendor.mtk_c2k_lte_mode").equals("1") || SystemProperties.get("ro.vendor.mtk_c2k_lte_mode").equals(MtkGsmCdmaPhone.ACT_TYPE_UTRAN)) {
            return true;
        }
        return false;
    }

    public static boolean isC2kSupport() {
        return RatConfiguration.isC2kSupported();
    }

    public static boolean getSimLockedState(int simApplicateionState) {
        if (simApplicateionState == 2 || simApplicateionState == 3 || simApplicateionState == 4 || simApplicateionState == 7 || simApplicateionState == 0) {
            return true;
        }
        return false;
    }

    public static void saveToModemType(int modemType) {
        sToModem = modemType;
    }

    public static int getToModemType() {
        return sToModem;
    }

    public static boolean isSimSwitching() {
        return sSimSwitching;
    }

    public static void setSimSwitchingFlag(boolean flag) {
        sSimSwitching = flag;
    }

    private static void logd(String msg) {
        Rlog.d(IWorldPhone.LOG_TAG, "[WPP_UTIL]" + msg);
    }
}
