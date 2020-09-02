package com.mediatek.internal.telephony.worldphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.mediatek.internal.telephony.MtkRIL;

public class WorldMode extends Handler {
    static final String ACTION_ADB_SWITCH_WORLD_MODE = "android.intent.action.ACTION_ADB_SWITCH_WORLD_MODE";
    public static final String ACTION_WORLD_MODE_CHANGED = "mediatek.intent.action.ACTION_WORLD_MODE_CHANGED";
    static final int EVENT_RADIO_ON_1 = 1;
    static final int EVENT_RADIO_ON_2 = 2;
    static final int EVENT_RADIO_ON_3 = 3;
    static final int EVENT_RADIO_ON_4 = 4;
    static final String EXTRA_WORLDMODE = "worldMode";
    public static final String EXTRA_WORLD_MODE_CHANGE_STATE = "worldModeState";
    private static final String LOG_TAG = "WORLDMODE";
    public static final int MASK_CDMA = 32;
    public static final int MASK_GSM = 1;
    public static final int MASK_LTEFDD = 16;
    public static final int MASK_LTETDD = 8;
    public static final int MASK_TDSCDMA = 2;
    public static final int MASK_WCDMA = 4;
    public static final int MD_WM_CHANGED_END = 1;
    public static final int MD_WM_CHANGED_START = 0;
    public static final int MD_WM_CHANGED_UNKNOWN = -1;
    public static final int MD_WORLD_MODE_LCTG = 16;
    public static final int MD_WORLD_MODE_LFCTG = 21;
    public static final int MD_WORLD_MODE_LFTG = 20;
    public static final int MD_WORLD_MODE_LFWCG = 15;
    public static final int MD_WORLD_MODE_LFWG = 14;
    public static final int MD_WORLD_MODE_LTCTG = 17;
    public static final int MD_WORLD_MODE_LTG = 8;
    public static final int MD_WORLD_MODE_LTTG = 13;
    public static final int MD_WORLD_MODE_LTWCG = 19;
    public static final int MD_WORLD_MODE_LTWG = 18;
    public static final int MD_WORLD_MODE_LWCG = 11;
    public static final int MD_WORLD_MODE_LWCTG = 12;
    public static final int MD_WORLD_MODE_LWG = 9;
    public static final int MD_WORLD_MODE_LWTG = 10;
    public static final int MD_WORLD_MODE_UNKNOWN = 0;
    private static final int PROJECT_SIM_NUM = WorldPhoneUtil.getProjectSimNum();
    static final int WORLD_MODE_RESULT_ERROR = 101;
    static final int WORLD_MODE_RESULT_SUCCESS = 100;
    static final int WORLD_MODE_RESULT_WM_ID_NOT_SUPPORT = 102;
    private static Phone[] sActivePhones;
    private static int sActiveWorldMode = 0;
    private static MtkRIL[] sCi;
    private static Context sContext = null;
    /* access modifiers changed from: private */
    public static int sCurrentWorldMode = updateCurrentWorldMode();
    private static WorldMode sInstance;
    private static Phone[] sProxyPhones = null;
    private static boolean sSwitchingState = false;
    private static int sUpdateSwitchingFlag = 0;
    private static CommandsInterface[] smCi;
    private final BroadcastReceiver mWorldModeReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.worldphone.WorldMode.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            WorldMode.logd("[Receiver]+");
            String action = intent.getAction();
            WorldMode.logd("Action: " + action);
            if (WorldMode.ACTION_WORLD_MODE_CHANGED.equals(action)) {
                int wmState = intent.getIntExtra(WorldMode.EXTRA_WORLD_MODE_CHANGE_STATE, -1);
                WorldMode.logd("wmState: " + wmState);
                if (wmState == 1) {
                    int unused = WorldMode.sCurrentWorldMode = WorldMode.updateCurrentWorldMode();
                }
            } else if (WorldMode.ACTION_ADB_SWITCH_WORLD_MODE.equals(action)) {
                int toMode = intent.getIntExtra(WorldMode.EXTRA_WORLDMODE, 0);
                WorldMode.logd("toMode: " + toMode);
                if (toMode >= 8 && toMode <= 21) {
                    WorldMode.setWorldMode(toMode);
                }
            }
            WorldMode.logd("[Receiver]-");
        }
    };

    static {
        int i = PROJECT_SIM_NUM;
        sActivePhones = new Phone[i];
        smCi = new CommandsInterface[i];
        sCi = new MtkRIL[i];
    }

    public WorldMode() {
        logd("Constructor Init world mode: " + sCurrentWorldMode + "sSwitchingState: " + sSwitchingState);
        sProxyPhones = PhoneFactory.getPhones();
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            Phone[] phoneArr = sActivePhones;
            phoneArr[i] = sProxyPhones[i];
            smCi[i] = phoneArr[i].mCi;
            MtkRIL[] mtkRILArr = sCi;
            mtkRILArr[i] = (MtkRIL) smCi[i];
            mtkRILArr[i].registerForOn(this, i + 1, null);
        }
        IntentFilter intentFilter = new IntentFilter(ACTION_WORLD_MODE_CHANGED);
        intentFilter.addAction(ACTION_ADB_SWITCH_WORLD_MODE);
        if (PhoneFactory.getDefaultPhone() != null) {
            sContext = PhoneFactory.getDefaultPhone().getContext();
        } else {
            logd("DefaultPhone = null");
        }
        sContext.registerReceiver(this.mWorldModeReceiver, intentFilter);
    }

    public static void init() {
        synchronized (WorldMode.class) {
            if (sInstance == null) {
                sInstance = new WorldMode();
            } else {
                logd("init() called multiple times!  sInstance = " + sInstance);
            }
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult asyncResult = (AsyncResult) msg.obj;
        WorldPhoneUtil.getMajorSim();
        int i = msg.what;
        if (i == 1) {
            logd("handleMessage : <EVENT_RADIO_ON_1>");
            if (WorldPhoneUtil.getMajorSim() == 0) {
                sCurrentWorldMode = updateCurrentWorldMode();
            }
        } else if (i == 2) {
            logd("handleMessage : <EVENT_RADIO_ON_2>");
            if (WorldPhoneUtil.getMajorSim() == 1) {
                sCurrentWorldMode = updateCurrentWorldMode();
            }
        } else if (i == 3) {
            logd("handleMessage : <EVENT_RADIO_ON_3>");
            if (WorldPhoneUtil.getMajorSim() == 2) {
                sCurrentWorldMode = updateCurrentWorldMode();
            }
        } else if (i != 4) {
            logd("Unknown msg:" + msg.what);
        } else {
            logd("handleMessage : <EVENT_RADIO_ON_4>");
            if (WorldPhoneUtil.getMajorSim() == 3) {
                sCurrentWorldMode = updateCurrentWorldMode();
            }
        }
    }

    private static boolean checkWmCapability(int worldMode, int bnadMode) {
        int iRat = 0;
        if (worldMode == 8) {
            iRat = 27;
        } else if (worldMode == 13) {
            iRat = 11;
        } else if (worldMode == 10) {
            iRat = 31;
        } else if (worldMode == 14) {
            iRat = 21;
        } else if (worldMode == 9) {
            iRat = 29;
        } else if (worldMode == 12) {
            iRat = 63;
        } else if (worldMode == 16) {
            iRat = 59;
        } else if (worldMode == 17) {
            iRat = 43;
        } else if (worldMode == 15) {
            iRat = 53;
        } else if (worldMode == 11) {
            iRat = 61;
        } else if (worldMode == 18) {
            iRat = 13;
        } else if (worldMode == 19) {
            iRat = 45;
        } else if (worldMode == 20) {
            iRat = 19;
        } else if (worldMode == 21) {
            iRat = 51;
        }
        if (true == WorldPhoneUtil.isC2kSupport()) {
            bnadMode |= 32;
        }
        if (true == WorldPhoneUtil.isWorldPhoneSupport() && (4 == (iRat & 4) || 2 == (iRat & 2))) {
            bnadMode = bnadMode | 4 | 2;
        }
        logd("checkWmCapability: modem=" + worldMode + " rat=" + iRat + " bnadMode=" + bnadMode);
        if (iRat == (iRat & bnadMode) && (iRat & 32) == (bnadMode & 32)) {
            return true;
        }
        return false;
    }

    public static int setWorldModeWithBand(int worldMode, int bandMode) {
        if (!checkWmCapability(worldMode, bandMode)) {
            logd("setWorldModeWithBand: not match, modem=" + worldMode + " bandMode=" + bandMode);
            return 102;
        }
        setWorldMode(worldMode);
        return 100;
    }

    public static void setWorldMode(int worldMode) {
        int protocolSim = WorldPhoneUtil.getMajorSim();
        logd("[setWorldMode]protocolSim: " + protocolSim);
        if (protocolSim < 0 || protocolSim > 3) {
            setWorldMode(sCi[0], worldMode);
        } else {
            setWorldMode(sCi[protocolSim], worldMode);
        }
    }

    private static void setWorldMode(MtkRIL ci, int worldMode) {
        logd("[setWorldMode] worldMode=" + worldMode);
        if (worldMode == sCurrentWorldMode) {
            if (worldMode == 8) {
                logd("Already in uTLG mode");
            } else if (worldMode == 9) {
                logd("Already in uLWG mode");
            } else if (worldMode == 10) {
                logd("Already in uLWTG mode");
            } else if (worldMode == 11) {
                logd("Already in uLWCG mode");
            } else if (worldMode == 12) {
                logd("Already in uLWTCG mode");
            } else if (worldMode == 13) {
                logd("Already in LtTG mode");
            } else if (worldMode == 14) {
                logd("Already in LfWG mode");
            } else if (worldMode == 15) {
                logd("Already in uLfWCG mode");
            } else if (worldMode == 16) {
                logd("Already in uLCTG mode");
            } else if (worldMode == 17) {
                logd("Already in uLtCTG mode");
            } else if (worldMode == 18) {
                logd("Already in uLtWG mode");
            } else if (worldMode == 19) {
                logd("Already in uLtWCG mode");
            } else if (worldMode == 20) {
                logd("Already in uLfTG mode");
            } else if (worldMode == 21) {
                logd("Already in uLfCTG mode");
            }
        } else if (ci.getRadioState() == 2) {
            logd("Radio unavailable, can not switch world mode");
        } else if (worldMode < 8 || worldMode > 21) {
            logd("Invalid world mode:" + worldMode);
        } else {
            ci.reloadModemType(worldMode, null);
            ci.storeModemType(worldMode, null);
            ci.restartRILD(null);
        }
    }

    public static int getWorldMode() {
        sCurrentWorldMode = Integer.valueOf(SystemProperties.get("vendor.ril.active.md", Integer.toString(0))).intValue();
        logd("getWorldMode=" + WorldModeToString(sCurrentWorldMode));
        return sCurrentWorldMode;
    }

    /* access modifiers changed from: private */
    public static int updateCurrentWorldMode() {
        sCurrentWorldMode = Integer.valueOf(SystemProperties.get("vendor.ril.active.md", Integer.toString(0))).intValue();
        logd("updateCurrentWorldMode=" + WorldModeToString(sCurrentWorldMode));
        return sCurrentWorldMode;
    }

    public static boolean updateSwitchingState(boolean isSwitching) {
        if (isSwitching || isWorldModeSwitching()) {
            int i = sUpdateSwitchingFlag;
            if (i <= 0 || true != isSwitching) {
                sSwitchingState = isSwitching;
                logd("updateSwitchingState=" + sSwitchingState);
                return true;
            }
            sUpdateSwitchingFlag = i - 1;
            logd("sUpdateSwitchingFlag- =" + sUpdateSwitchingFlag);
            return false;
        }
        sUpdateSwitchingFlag++;
        logd("sUpdateSwitchingFlag+ =" + sUpdateSwitchingFlag);
        return false;
    }

    public static boolean resetSwitchingState(int state) {
        logd("reset sUpdateSwitchingFlag = " + sUpdateSwitchingFlag);
        sUpdateSwitchingFlag = 0;
        logd("reset sSwitchingState = " + sSwitchingState);
        sSwitchingState = false;
        return true;
    }

    public static boolean isWorldModeSwitching() {
        if (sSwitchingState) {
            return true;
        }
        return false;
    }

    public static String WorldModeToString(int worldMode) {
        if (worldMode == 8) {
            return "uTLG";
        }
        if (worldMode == 9) {
            return "uLWG";
        }
        if (worldMode == 10) {
            return "uLWTG";
        }
        if (worldMode == 11) {
            return "uLWCG";
        }
        if (worldMode == 12) {
            return "uLWTCG";
        }
        if (worldMode == 13) {
            return "LtTG";
        }
        if (worldMode == 14) {
            return "LfWG";
        }
        if (worldMode == 15) {
            return "uLfWCG";
        }
        if (worldMode == 16) {
            return "uLCTG";
        }
        if (worldMode == 17) {
            return "uLtCTG";
        }
        if (worldMode == 18) {
            return "uLtWG";
        }
        if (worldMode == 19) {
            return "uLtWCG";
        }
        if (worldMode == 20) {
            return "uLfTG";
        }
        if (worldMode == 21) {
            return "uLfCTG";
        }
        return "Invalid world mode";
    }

    /* access modifiers changed from: private */
    public static void logd(String msg) {
        Rlog.d("WORLDMODE", "[WorldMode]" + msg);
    }
}
