package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.uicc.IccCardStatus;

public class OppoSimHotswapManager {
    public static final String ACTION_HOTSWAP_STATE_CHANGE = "oppo.intent.action.SUBINFO_STATE_CHANGE";
    private static final String ACTION_SIM_STATE_CHANGED = "com.dmyk.android.telephony.action.SIM_STATE_CHANGED";
    private static final boolean CMCC_DM_SWITCH = SystemProperties.get("ro.product.oem_dm", OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK).equals("1");
    private static final boolean DBG = true;
    private static final String EXTRA_SIM_PHONEID = "com.dmyk.android.telephony.extra.SIM_PHONEID";
    private static final String EXTRA_SIM_STATE = "com.dmyk.android.telephony.extra.SIM_STATE";
    private static final String FEATURE_ENABLE_HOTSWAP = "gsm.enable_hotswap";
    public static final String HOTSWAP_PROPERTY_SLOT0 = "persist.radio.hotplug_slot0";
    public static final String HOTSWAP_PROPERTY_SLOT1 = "persist.radio.hotplug_slot1";
    public static final char HOTSWAP_PROPERTY_VALUE_FLAG_FW_READ = '1';
    public static final char HOTSWAP_PROPERTY_VALUE_FLAG_INVALID = '0';
    public static final char HOTSWAP_PROPERTY_VALUE_FLAG_QCRIL_UPDATE = '2';
    public static final int HOTSWAP_PROPERTY_VALUE_LENGTH = 3;
    public static final char HOTSWAP_PROPERTY_VALUE_SIM_PLUG_STATE_NO_PLUG = '0';
    public static final char HOTSWAP_PROPERTY_VALUE_SIM_PLUG_STATE_PLUG_IN = '1';
    public static final char HOTSWAP_PROPERTY_VALUE_SIM_PLUG_STATE_PLUG_OUT = '2';
    public static final int HOTSWAP_STATE_INVALID = 0;
    public static final int HOTSWAP_STATE_SIM_PLUG_IN = 1;
    public static final int HOTSWAP_STATE_SIM_PLUG_OUT = 2;
    public static final String INTENT_KEY_SIM_STATE = "simstate";
    public static final String INTENT_KEY_SLOT_ID = "slotid";
    public static final String INTENT_KEY_SUB_ID = "subid";
    public static final String INTENT_VALUE_SIM_PLUG_IN = "PLUGIN";
    public static final String INTENT_VALUE_SIM_PLUG_OUT = "PLUGOUT";
    private static final String LOG_TAG = "OppoSimHotswapManager";
    private static final int PROJECT_SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static OppoSimHotswapManager mInstance = null;
    private static final Object mLock = new Object();
    private static volatile int[] mSimHotswapState = new int[PROJECT_SIM_NUM];
    private Context mContext;
    private IccCardStatus.CardState[] mOldSimState = new IccCardStatus.CardState[PROJECT_SIM_NUM];

    private OppoSimHotswapManager(Context c) {
        log("Creating OppoSimHotswapManager");
        this.mContext = c;
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            mSimHotswapState[i] = 0;
            this.mOldSimState[i] = IccCardStatus.CardState.CARDSTATE_ABSENT;
        }
    }

    public static OppoSimHotswapManager getInstance(Context c) {
        OppoSimHotswapManager oppoSimHotswapManager;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new OppoSimHotswapManager(c);
            }
            oppoSimHotswapManager = mInstance;
        }
        return oppoSimHotswapManager;
    }

    public int getSimHotswapState(int index) {
        int i;
        logd("getSimHotswapState : " + mSimHotswapState[index]);
        synchronized (mLock) {
            i = mSimHotswapState[index];
        }
        return i;
    }

    public void setSimHotswapState(int index, int SimHotswapState) {
        logd("setSimHotswapState,new value: " + SimHotswapState + ",for index : " + index);
        synchronized (mLock) {
            mSimHotswapState[index] = SimHotswapState;
        }
    }

    public boolean isSimPlugIn(int slotId) {
        boolean isSimPlugIn = true;
        if (getSimHotswapState(slotId) != 1) {
            isSimPlugIn = false;
        }
        logd("[isSimPlugIn],isSimPlugIn: " + isSimPlugIn + ",for slotId : " + slotId);
        return isSimPlugIn;
    }

    public boolean isSimPlugOut(int slotId) {
        boolean isSimPlugOut = getSimHotswapState(slotId) == 2;
        logd("[isSimPlugOut],isSimPlugOut: " + isSimPlugOut + ",for slotId : " + slotId);
        return isSimPlugOut;
    }

    public boolean isNotSimPlugAction(int slotId) {
        boolean isNotPlugAction = getSimHotswapState(slotId) == 0;
        logd("[isNotSimPlugAction],isNotPlugAction: " + isNotPlugAction + ",for slotId : " + slotId);
        return isNotPlugAction;
    }

    public void checkAdnUpdateSimHotSwapState(int slotId, int simHotswapState) {
        logd("[checkAdnUpdateSimHotSwapState] old mSimHotswapState[" + slotId + "] = " + mSimHotswapState[slotId] + ",new simHotswapState = " + simHotswapState);
        if (simHotswapState < 0 || 2 < simHotswapState) {
            log("[checkAdnUpdateSimHotSwapState], invalid value: " + simHotswapState + " for slot:" + slotId);
        } else if (simHotswapState != getSimHotswapState(slotId)) {
            setSimHotswapState(slotId, simHotswapState);
            logd("[checkAdnUpdateSimHotSwapState],set slot: " + slotId + ",to : " + simHotswapState);
        }
    }

    public int getSimHotSwapStateFromProperty(int slotId) {
        String simHotswapState;
        int state;
        if (slotId == 0) {
            simHotswapState = SystemProperties.get(HOTSWAP_PROPERTY_SLOT0, PhoneConfigurationManager.SSSS);
        } else if (slotId == 1) {
            simHotswapState = SystemProperties.get(HOTSWAP_PROPERTY_SLOT1, PhoneConfigurationManager.SSSS);
        } else {
            log("[getSimHotSwapStateFromProperty] slotId:" + slotId + " is wrong ");
            return 0;
        }
        logd("[getSimHotSwapStateFromProperty] slotId:" + slotId + " simHotswapState: " + simHotswapState);
        if (simHotswapState.length() == 3) {
            char[] result = simHotswapState.toCharArray();
            log("[getSimHotSwapStateFromProperty] result:" + result[0] + "-" + result[1] + "-" + result[2]);
            char c = result[0];
            if (c == '1') {
                logd("[getSimHotSwapStateFromProperty] slotId:" + slotId + " property have read ");
                return 0;
            } else if (c != '2') {
                log("[getSimHotSwapStateFromProperty] isFwReady_isRilUpdate: " + result[0] + ", is wrong");
                return 0;
            } else {
                logd("[getSimHotSwapStateFromProperty] slotId:" + slotId + " property qcril update");
                switch (result[2]) {
                    case '0':
                        logd("[getSimHotSwapStateFromProperty] slotId:" + slotId + " no plug ");
                        state = 0;
                        break;
                    case '1':
                        logd("[getSimHotSwapStateFromProperty] slotId:" + slotId + " plug in ");
                        state = 1;
                        break;
                    case '2':
                        logd("[getSimHotSwapStateFromProperty] slotId:" + slotId + " plug out ");
                        state = 2;
                        break;
                    default:
                        log("[getSimHotSwapStateFromProperty] sim_plug_state: " + result[2] + ", is wrong");
                        state = 0;
                        break;
                }
                logd("[getSimHotSwapStateFromProperty] slotId = " + slotId + ",simHotswapState = " + simHotswapState + ",state = " + state);
                return state;
            }
        } else {
            log("[getSimHotSwapStateFromProperty] simHotswapState length is wrong ");
            return 0;
        }
    }

    public void updateSimHotSwapStatePropertyReadFlag(int slotId, String value) {
        logd("[updateSimHotSwapStatePropertyReadFlag] slotId:" + slotId + " property rest to:" + value);
        if (slotId == 0) {
            try {
                SystemProperties.set(HOTSWAP_PROPERTY_SLOT0, value);
            } catch (Exception e) {
                log("[updateSimHotSwapStatePropertyReadFlag] exception happen:" + e);
            }
        } else if (slotId == 1) {
            SystemProperties.get(HOTSWAP_PROPERTY_SLOT1, value);
        } else {
            log("[updateSimHotSwapStatePropertyReadFlag] slotId:" + slotId + " is wrong ");
        }
    }

    public void updateSimHotSwapState(int slotId) {
        int simHotswapState = getSimHotSwapStateFromProperty(slotId);
        setSimHotswapState(slotId, simHotswapState);
        logd("[updateSimHotSwapState] slotId = " + slotId + ",simHotswapState = " + simHotswapState);
    }

    public void processSimPlugState(int slotId, IccCardStatus.CardState newState) {
        if (isNotSimPlugAction(slotId)) {
            log("card[" + slotId + "] plug status not changed，newState:" + newState);
            this.mOldSimState[slotId] = newState;
            return;
        }
        if (isSimPlugIn(slotId) && newState == IccCardStatus.CardState.CARDSTATE_PRESENT && this.mOldSimState[slotId] == IccCardStatus.CardState.CARDSTATE_ABSENT) {
            log("card[" + slotId + "] inserted");
            broadcastSimHotswapState(Integer.toString(slotId), "-1", INTENT_VALUE_SIM_PLUG_IN);
            cmccAutoRegbroadcastSimHotSwapState(slotId);
        } else if (isSimPlugOut(slotId) && newState == IccCardStatus.CardState.CARDSTATE_ABSENT && this.mOldSimState[slotId] != IccCardStatus.CardState.CARDSTATE_ABSENT) {
            log("card[" + slotId + "] plugout");
            broadcastSimHotswapState(Integer.toString(slotId), "-1", INTENT_VALUE_SIM_PLUG_OUT);
        }
        setSimHotswapState(slotId, 0);
        this.mOldSimState[slotId] = newState;
        logd("[processSimPlugState] slotId = " + slotId + ",newState = " + newState);
    }

    public boolean isHotSwapSimReboot() {
        return SystemProperties.get(FEATURE_ENABLE_HOTSWAP, this.mContext.getPackageManager().hasSystemFeature("oppo.commcenter.reboot.dialog") ? "false" : "true").equals("false");
    }

    public void broadcastSimHotswapState(String slotid, String subid, String simstate) {
        Intent intent = new Intent("oppo.intent.action.SUBINFO_STATE_CHANGE");
        intent.putExtra("slotid", slotid);
        intent.putExtra("subid", subid);
        intent.putExtra("simstate", simstate);
        intent.addFlags(16777216);
        log("broadcastSimHotswapState slotid:" + slotid + " simstate:" + simstate + " subid:" + subid);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "oppo.permission.OPPO_COMPONENT_SAFE");
    }

    public void cmccAutoRegbroadcastSimHotSwapState(int slotId) {
        if (CMCC_DM_SWITCH) {
            TelephonyManager tm = TelephonyManager.from(this.mContext);
            int simState = 0;
            if (tm != null) {
                simState = tm.getSimState(slotId);
            } else {
                log("cmccAutoRegbroadcastSimHotSwapState, tm is null for slotid:" + slotId);
            }
            Intent intent = new Intent(ACTION_SIM_STATE_CHANGED);
            intent.putExtra(EXTRA_SIM_PHONEID, slotId);
            intent.putExtra(EXTRA_SIM_STATE, simState);
            log("Broadcasting intent ACTION_SIM_STATE_CHANGED slotid:" + slotId + " simState:" + simState + " for CmccAutoReg");
            this.mContext.sendBroadcast(intent);
        }
    }

    public static void log(String string) {
        Rlog.d(LOG_TAG, string);
    }

    public static void logd(String string) {
        log(string);
    }
}
