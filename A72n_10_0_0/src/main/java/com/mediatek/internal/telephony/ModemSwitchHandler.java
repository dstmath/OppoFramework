package com.mediatek.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.mediatek.internal.telephony.worldphone.WorldPhoneUtil;

public class ModemSwitchHandler {
    public static final String ACTION_MD_TYPE_CHANGE = "mediatek.intent.action.ACTION_MD_TYPE_CHANGE";
    public static final String ACTION_MODEM_SWITCH_DONE = "mediatek.intent.action.ACTION_MODEM_SWITCH_DONE";
    private static final int EVENT_RADIO_NOT_AVAILABLE = 2;
    private static final int EVENT_RIL_CONNECTED = 1;
    public static final String EXTRA_MD_TYPE = "mdType";
    private static final String LOG_TAG = "WORLDMODE";
    private static final int MD_SWITCH_DEFAULT = 0;
    private static final int MD_SWITCH_RADIO_UNAVAILABLE = 2;
    private static final int MD_SWITCH_RESET_START = 1;
    public static final int MD_TYPE_FDD = 100;
    public static final int MD_TYPE_LTG = 6;
    public static final int MD_TYPE_LWG = 5;
    public static final int MD_TYPE_TDD = 101;
    public static final int MD_TYPE_TG = 4;
    public static final int MD_TYPE_UNKNOWN = 0;
    public static final int MD_TYPE_WG = 3;
    private static final int PROJECT_SIM_NUM = WorldPhoneUtil.getProjectSimNum();
    private static Phone[] sActivePhones;
    private static MtkRIL[] sCi;
    private static Context sContext = null;
    private static int sCurrentModemType = initActiveModemType();
    private static int sModemSwitchingFlag = 0;
    private static Phone[] sProxyPhones = null;
    private static Handler sWorldPhoneHandler = new Handler() {
        /* class com.mediatek.internal.telephony.ModemSwitchHandler.AnonymousClass1 */

        public void handleMessage(Message msg) {
            int protocolSim = WorldPhoneUtil.getMajorSim();
            ModemSwitchHandler.logd("handleMessage msg.what=" + msg.what + " sModemSwitchingFlag=" + ModemSwitchHandler.sModemSwitchingFlag + " protocolSim: " + protocolSim);
            int i = msg.what;
            if (i == 1) {
                ModemSwitchHandler.logd("[EVENT_RIL_CONNECTED] mRilVersion=" + ((AsyncResult) msg.obj).result);
                if (ModemSwitchHandler.sModemSwitchingFlag == 2) {
                    int unused = ModemSwitchHandler.sModemSwitchingFlag = 0;
                    ModemSwitchHandler.sContext.sendBroadcast(new Intent(ModemSwitchHandler.ACTION_MODEM_SWITCH_DONE));
                    if (protocolSim >= 0 && protocolSim <= 3) {
                        ModemSwitchHandler.sCi[protocolSim].unregisterForNotAvailable(ModemSwitchHandler.sWorldPhoneHandler);
                        ModemSwitchHandler.sCi[protocolSim].unregisterForRilConnected(ModemSwitchHandler.sWorldPhoneHandler);
                    }
                }
            } else if (i == 2) {
                int unused2 = ModemSwitchHandler.sModemSwitchingFlag = 2;
            }
        }
    };
    private static CommandsInterface[] smCi;

    static {
        int i = PROJECT_SIM_NUM;
        sActivePhones = new Phone[i];
        smCi = new CommandsInterface[i];
        sCi = new MtkRIL[i];
    }

    public ModemSwitchHandler() {
        logd("Constructor invoked");
        logd("Init modem type: " + sCurrentModemType);
        sProxyPhones = PhoneFactory.getPhones();
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            Phone[] phoneArr = sActivePhones;
            phoneArr[i] = sProxyPhones[i];
            smCi[i] = phoneArr[i].mCi;
            sCi[i] = (MtkRIL) smCi[i];
        }
        if (PhoneFactory.getDefaultPhone() != null) {
            sContext = PhoneFactory.getDefaultPhone().getContext();
        } else {
            logd("DefaultPhone = null");
        }
    }

    public static void switchModem(int modemType) {
        int protocolSim = WorldPhoneUtil.getMajorSim();
        logd("protocolSim: " + protocolSim);
        if (protocolSim < 0 || protocolSim > 3) {
            logd("switchModem protocolSim is invalid");
        } else {
            switchModem(sCi[protocolSim], modemType);
        }
    }

    public static void switchModem(int isStoreModemType, int modemType) {
        int protocolSim = WorldPhoneUtil.getMajorSim();
        logd("protocolSim: " + protocolSim);
        if (protocolSim < 0 || protocolSim > 3) {
            logd("switchModem protocolSim is invalid");
        } else {
            switchModem(isStoreModemType, sCi[protocolSim], modemType);
        }
    }

    public static void switchModem(MtkRIL ci, int modemType) {
        logd("[switchModem] need store modem type");
        switchModem(1, ci, modemType);
    }

    public static void switchModem(int isStoreModemType, MtkRIL ci, int modemType) {
        logd("[switchModem]");
        if (ci.getRadioState() == 2) {
            logd("Radio unavailable, can not switch modem");
            return;
        }
        sCurrentModemType = getActiveModemType();
        if (modemType != sCurrentModemType) {
            sModemSwitchingFlag = 1;
            ci.registerForNotAvailable(sWorldPhoneHandler, 2, null);
            ci.registerForRilConnected(sWorldPhoneHandler, 1, null);
            setModemType(isStoreModemType, ci, modemType);
            setActiveModemType(modemType);
            logd("Broadcast intent ACTION_MD_TYPE_CHANGE");
            Intent intent = new Intent(ACTION_MD_TYPE_CHANGE);
            intent.putExtra(EXTRA_MD_TYPE, modemType);
            sContext.sendBroadcast(intent);
        } else if (modemType == 3) {
            logd("Already in WG modem");
        } else if (modemType == 4) {
            logd("Already in TG modem");
        } else if (modemType == 5) {
            logd("Already in FDD CSFB modem");
        } else if (modemType == 6) {
            logd("Already in TDD CSFB modem");
        }
    }

    private static boolean setModemType(int isStoreModemType, MtkRIL ci, int modemType) {
        if (ci.getRadioState() == 2) {
            logd("Radio unavailable, can not switch world mode");
            return false;
        } else if (modemType < 3 || modemType > 6) {
            logd("Invalid modemType:" + modemType);
            return false;
        } else {
            logd("silent reboot isStroeModemType=" + isStoreModemType);
            ci.reloadModemType(modemType, null);
            if (1 == isStoreModemType) {
                ci.storeModemType(modemType, null);
            }
            ci.setVendorSetting(10, Integer.toString(1), null);
            ci.restartRILD(null);
            return true;
        }
    }

    public static void reloadModem(int modemType) {
        int majorSim = WorldPhoneUtil.getMajorSim();
        if (majorSim < 0 || majorSim > 3) {
            logd("Invalid MajorSIM id" + majorSim);
            return;
        }
        reloadModem(sCi[majorSim], modemType);
    }

    public static void reloadModem(MtkRIL ci, int modemType) {
        logd("[reloadModem]");
        if (ci.getRadioState() == 2) {
            logd("Radio unavailable, can not reload modem");
        } else {
            ci.reloadModemType(modemType, null);
        }
    }

    public static void reloadModemCauseType(MtkRIL ci, int causeType) {
        logd("[reloadModemCauseType] " + causeType);
        ci.reloadModemType(causeType, null);
    }

    public static int getActiveModemType() {
        if (!WorldPhoneUtil.isWorldPhoneSupport() || WorldPhoneUtil.isWorldModeSupport()) {
            sCurrentModemType = Integer.valueOf(SystemProperties.get("vendor.ril.active.md", Integer.toString(0))).intValue();
        }
        logd("[getActiveModemType] " + sCurrentModemType);
        return sCurrentModemType;
    }

    public static int initActiveModemType() {
        sCurrentModemType = Integer.valueOf(SystemProperties.get("vendor.ril.active.md", Integer.toString(0))).intValue();
        logd("[initActiveModemType] " + sCurrentModemType);
        return sCurrentModemType;
    }

    public static void setActiveModemType(int modemType) {
        sCurrentModemType = modemType;
        logd("[setActiveModemType] " + modemToString(sCurrentModemType));
    }

    public static boolean isModemTypeSwitching() {
        logd("[isModemTypeSwitching]: sModemSwitchingFlag = " + sModemSwitchingFlag);
        if (sModemSwitchingFlag != 0) {
            return true;
        }
        return false;
    }

    public static String modemToString(int modemType) {
        if (modemType == 3) {
            return "WG";
        }
        if (modemType == 4) {
            return "TG";
        }
        if (modemType == 5) {
            return "FDD CSFB";
        }
        if (modemType == 6) {
            return "TDD CSFB";
        }
        if (modemType == 0) {
            return "UNKNOWN";
        }
        return "Invalid modem type";
    }

    /* access modifiers changed from: private */
    public static void logd(String msg) {
        Rlog.d("WORLDMODE", "[MSH]" + msg);
    }
}
