package com.mediatek.internal.telephony.worldphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telecom.ITelecomService;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.internal.telephony.ModemSwitchHandler;
import com.mediatek.internal.telephony.MtkRIL;
import com.mediatek.internal.telephony.ppl.PplSmsFilterExtension;
import mediatek.telephony.MtkServiceState;

public class WorldPhoneOp01 extends Handler implements IWorldPhone {
    private static final int EMSR_STANDBY_TIMER = 8;
    /* access modifiers changed from: private */
    public static final int[] FDD_STANDBY_TIMER = {60};
    private static final String[] MCC_TABLE_DOMESTIC = {"460", "001", "002", "003", "004", "005", "006", "007", "008", "009", "010", "011", "012"};
    private static final String[] PLMN_TABLE_TYPE1 = {"46000", "46002", "46004", "46007", "46008", "00101", "00211", "00321", "00431", "00541", "00651", "00761", "00871", "00902", "01012", "01122", "01232", "46602", "50270"};
    private static final String[] PLMN_TABLE_TYPE3 = {"46001", "46006", "46009", "45407", "46003", "46005", "45502", "46011"};
    /* access modifiers changed from: private */
    public static final int PROJECT_SIM_NUM = WorldPhoneUtil.getProjectSimNum();
    private static final String[] PROPERTY_RIL_CT3G = {"vendor.gsm.ril.ct3g", "vendor.gsm.ril.ct3g.2", "vendor.gsm.ril.ct3g.3", "vendor.gsm.ril.ct3g.4"};
    /* access modifiers changed from: private */
    public static final int[] TDD_STANDBY_TIMER = {40};
    private static Phone[] sActivePhones;
    /* access modifiers changed from: private */
    public static int sBtSapState;
    /* access modifiers changed from: private */
    public static MtkRIL[] sCi;
    private static Context sContext = null;
    private static int sDataRegState;
    /* access modifiers changed from: private */
    public static int sDefaultBootuUpModem = 0;
    private static Phone sDefultPhone = null;
    private static int sDenyReason;
    /* access modifiers changed from: private */
    public static int sFddStandByCounter;
    /* access modifiers changed from: private */
    public static boolean[] sFirstSelect;
    private static IccRecords[] sIccRecordsInstance;
    private static String[] sImsi;
    private static boolean sIsAutoSelectEnable;
    /* access modifiers changed from: private */
    public static boolean[] sIsInvalidSim;
    private static boolean sIsResumeCampingFail1;
    private static boolean sIsResumeCampingFail2;
    private static boolean sIsResumeCampingFail3;
    private static boolean sIsResumeCampingFail4;
    /* access modifiers changed from: private */
    public static int sIsWaintInFddTimeOut;
    /* access modifiers changed from: private */
    public static int sIsWaintInTddTimeOut;
    /* access modifiers changed from: private */
    public static String sLastPlmn;
    private static Object sLock = new Object();
    /* access modifiers changed from: private */
    public static int sMajorSim;
    private static ModemSwitchHandler sModemSwitchHandler = null;
    private static String[] sNwPlmnStrings;
    private static String sPlmnSs;
    private static Phone[] sProxyPhones = null;
    /* access modifiers changed from: private */
    public static int sRegion;
    private static int sRilDataRadioTechnology;
    private static int sRilDataRegState;
    private static int sRilVoiceRadioTechnology;
    private static int sRilVoiceRegState;
    private static MtkServiceState sServiceState;
    private static int sSimLocked;
    /* access modifiers changed from: private */
    public static int[] sSuspendId;
    /* access modifiers changed from: private */
    public static boolean[] sSuspendWaitImsi;
    /* access modifiers changed from: private */
    public static int sSwitchModemCauseType;
    /* access modifiers changed from: private */
    public static int sTddStandByCounter;
    private static UiccController sUiccController = null;
    private static int sUserType;
    private static boolean sVoiceCapable;
    private static int sVoiceRegState;
    /* access modifiers changed from: private */
    public static boolean sWaitInEmsrResume;
    private static boolean sWaitInFdd;
    private static boolean sWaitInTdd;
    private static CommandsInterface[] smCi;
    private Runnable mEmsrResumeByTimerRunnable = new Runnable() {
        /* class com.mediatek.internal.telephony.worldphone.WorldPhoneOp01.AnonymousClass4 */

        public void run() {
            boolean unused = WorldPhoneOp01.sWaitInEmsrResume = false;
            int unused2 = WorldPhoneOp01.sMajorSim = WorldPhoneUtil.getMajorSim();
            if (WorldPhoneOp01.sMajorSim != -99 && WorldPhoneOp01.sMajorSim != -1 && WorldPhoneOp01.sSuspendWaitImsi[WorldPhoneOp01.sMajorSim]) {
                WorldPhoneOp01.sCi[WorldPhoneOp01.sMajorSim].setResumeRegistration(WorldPhoneOp01.sSuspendId[WorldPhoneOp01.sMajorSim], WorldPhoneOp01.this.obtainMessage(WorldPhoneOp01.sMajorSim + 70));
            }
        }
    };
    private Runnable mFddStandByTimerRunnable = new Runnable() {
        /* class com.mediatek.internal.telephony.worldphone.WorldPhoneOp01.AnonymousClass3 */

        public void run() {
            WorldPhoneOp01.access$1808();
            if (WorldPhoneOp01.sFddStandByCounter >= WorldPhoneOp01.FDD_STANDBY_TIMER.length) {
                int unused = WorldPhoneOp01.sFddStandByCounter = WorldPhoneOp01.FDD_STANDBY_TIMER.length - 1;
            }
            if (WorldPhoneOp01.sBtSapState == 0) {
                WorldPhoneOp01.logd("FDD time out!");
                int unused2 = WorldPhoneOp01.sSwitchModemCauseType = 1;
                int unused3 = WorldPhoneOp01.sIsWaintInFddTimeOut = 1;
                WorldPhoneOp01.logd("sSwitchModemCauseType = " + WorldPhoneOp01.sSwitchModemCauseType);
                boolean unused4 = WorldPhoneOp01.this.handleSwitchModem(101);
                return;
            }
            WorldPhoneOp01.logd("FDD time out but BT SAP is connected, switch not executed!");
        }
    };
    private boolean mIsRegisterEccStateReceiver = false;
    private Runnable mTddStandByTimerRunnable = new Runnable() {
        /* class com.mediatek.internal.telephony.worldphone.WorldPhoneOp01.AnonymousClass2 */

        public void run() {
            WorldPhoneOp01.access$1308();
            if (WorldPhoneOp01.sTddStandByCounter >= WorldPhoneOp01.TDD_STANDBY_TIMER.length) {
                int unused = WorldPhoneOp01.sTddStandByCounter = WorldPhoneOp01.TDD_STANDBY_TIMER.length - 1;
            }
            if (WorldPhoneOp01.sBtSapState == 0) {
                WorldPhoneOp01.logd("TDD time out!");
                int unused2 = WorldPhoneOp01.sSwitchModemCauseType = 1;
                int unused3 = WorldPhoneOp01.sIsWaintInTddTimeOut = 1;
                WorldPhoneOp01.logd("sSwitchModemCauseType = " + WorldPhoneOp01.sSwitchModemCauseType);
                boolean unused4 = WorldPhoneOp01.this.handleSwitchModem(100);
                return;
            }
            WorldPhoneOp01.logd("TDD time out but BT SAP is connected, switch not executed!");
        }
    };
    private BroadcastReceiver mWorldPhoneEccStateReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.worldphone.WorldPhoneOp01.AnonymousClass5 */

        public void onReceive(Context context, Intent intent) {
            WorldPhoneOp01.logd("mWorldPhoneEccStateReceiver, received " + intent.getAction());
            if (!WorldPhoneOp01.this.isEccInProgress()) {
                WorldPhoneOp01.this.unRegisterEccStateReceiver();
                WorldPhoneOp01.this.handleSimSwitched();
            }
        }
    };
    private final BroadcastReceiver mWorldPhoneReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.worldphone.WorldPhoneOp01.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            WorldPhoneOp01.logd("Action: " + action);
            if (action.equals("android.telephony.action.SIM_APPLICATION_STATE_CHANGED")) {
                int state = intent.getIntExtra("android.telephony.extra.SIM_STATE", 0);
                int slotId = intent.getIntExtra("slot", 0);
                int unused = WorldPhoneOp01.sMajorSim = WorldPhoneUtil.getMajorSim();
                WorldPhoneOp01.logd("slotId: " + slotId + " state: " + state + " sMajorSim:" + WorldPhoneOp01.sMajorSim);
                WorldPhoneOp01.this.handleSimApplicationStateChanged(slotId, state);
            } else if (action.equals("android.telephony.action.SIM_CARD_STATE_CHANGED")) {
                int state2 = intent.getIntExtra("android.telephony.extra.SIM_STATE", 0);
                int slotId2 = intent.getIntExtra("slot", 0);
                int unused2 = WorldPhoneOp01.sMajorSim = WorldPhoneUtil.getMajorSim();
                WorldPhoneOp01.logd("slotId: " + slotId2 + " state: " + state2 + " sMajorSim:" + WorldPhoneOp01.sMajorSim);
                WorldPhoneOp01.this.handleSimCardStateChanged(slotId2, state2);
            } else if (action.equals(IWorldPhone.ACTION_SHUTDOWN_IPO)) {
                if (WorldPhoneOp01.sDefaultBootuUpModem == 100) {
                    if (WorldPhoneUtil.isLteSupport()) {
                        ModemSwitchHandler.reloadModem(WorldPhoneOp01.sCi[0], 5);
                        WorldPhoneOp01.logd("Reload to FDD CSFB modem");
                    } else {
                        ModemSwitchHandler.reloadModem(WorldPhoneOp01.sCi[0], 3);
                        WorldPhoneOp01.logd("Reload to WG modem");
                    }
                } else if (WorldPhoneOp01.sDefaultBootuUpModem == 101) {
                    if (WorldPhoneUtil.isLteSupport()) {
                        ModemSwitchHandler.reloadModem(WorldPhoneOp01.sCi[0], 6);
                        WorldPhoneOp01.logd("Reload to TDD CSFB modem");
                    } else {
                        ModemSwitchHandler.reloadModem(WorldPhoneOp01.sCi[0], 4);
                        WorldPhoneOp01.logd("Reload to TG modem");
                    }
                }
            } else if (action.equals(IWorldPhone.ACTION_ADB_SWITCH_MODEM)) {
                int toModem = intent.getIntExtra(ModemSwitchHandler.EXTRA_MD_TYPE, 0);
                WorldPhoneOp01.logd("toModem: " + toModem);
                if (toModem == 3 || toModem == 4 || toModem == 5 || toModem == 6) {
                    WorldPhoneOp01.this.setModemSelectionMode(0, toModem);
                } else {
                    WorldPhoneOp01.this.setModemSelectionMode(1, toModem);
                }
            } else if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                if (!intent.getBooleanExtra("state", false)) {
                    WorldPhoneOp01.logd("Leave flight mode");
                    String unused3 = WorldPhoneOp01.sLastPlmn = null;
                    for (int i = 0; i < WorldPhoneOp01.PROJECT_SIM_NUM; i++) {
                        WorldPhoneOp01.sIsInvalidSim[i] = false;
                    }
                } else {
                    WorldPhoneOp01.logd("Enter flight mode");
                    for (int i2 = 0; i2 < WorldPhoneOp01.PROJECT_SIM_NUM; i2++) {
                        WorldPhoneOp01.sFirstSelect[i2] = true;
                    }
                    int unused4 = WorldPhoneOp01.sRegion = 0;
                }
            } else if (action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE")) {
                int unused5 = WorldPhoneOp01.sMajorSim = WorldPhoneUtil.getMajorSim();
                if (WorldPhoneUtil.isSimSwitching()) {
                    WorldPhoneUtil.setSimSwitchingFlag(false);
                    ModemSwitchHandler.setActiveModemType(WorldPhoneUtil.getToModemType());
                }
                WorldPhoneOp01.this.handleSimSwitched();
            } else if (action.equals(IWorldPhone.ACTION_SAP_CONNECTION_STATE_CHANGED)) {
                int sapState = intent.getIntExtra("android.bluetooth.profile.extra.STATE", 0);
                if (sapState == 2) {
                    WorldPhoneOp01.logd("BT_SAP connection state is CONNECTED");
                    int unused6 = WorldPhoneOp01.sBtSapState = 1;
                } else if (sapState == 0) {
                    WorldPhoneOp01.logd("BT_SAP connection state is DISCONNECTED");
                    int unused7 = WorldPhoneOp01.sBtSapState = 0;
                } else {
                    WorldPhoneOp01.logd("BT_SAP connection state is " + sapState);
                }
            }
            WorldPhoneOp01.logd("Action: " + action + " handle end");
        }
    };

    static /* synthetic */ int access$1308() {
        int i = sTddStandByCounter;
        sTddStandByCounter = i + 1;
        return i;
    }

    static /* synthetic */ int access$1808() {
        int i = sFddStandByCounter;
        sFddStandByCounter = i + 1;
        return i;
    }

    static {
        int i = PROJECT_SIM_NUM;
        sActivePhones = new Phone[i];
        smCi = new CommandsInterface[i];
        sCi = new MtkRIL[i];
        sImsi = new String[i];
        sSuspendId = new int[i];
        sIsInvalidSim = new boolean[i];
        sSuspendWaitImsi = new boolean[i];
        sFirstSelect = new boolean[i];
        sIccRecordsInstance = new IccRecords[i];
    }

    public WorldPhoneOp01() {
        logd("Constructor invoked");
        sDefultPhone = PhoneFactory.getDefaultPhone();
        sProxyPhones = PhoneFactory.getPhones();
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            Phone[] phoneArr = sActivePhones;
            phoneArr[i] = sProxyPhones[i];
            smCi[i] = phoneArr[i].mCi;
            sCi[i] = (MtkRIL) smCi[i];
            sActivePhones[i].registerForServiceStateChanged(this, i + 80, (Object) null);
        }
        for (int i2 = 0; i2 < PROJECT_SIM_NUM; i2++) {
            sCi[i2].setOnPlmnChangeNotification(this, i2 + 10, null);
            sCi[i2].setOnRegistrationSuspended(this, i2 + 30, null);
            sCi[i2].registerForOn(this, i2 + 0, null);
            sCi[i2].setInvalidSimInfo(this, i2 + 60, null);
            if (WorldPhoneUtil.isC2kSupport()) {
                sCi[i2].registerForGmssRatChanged(this, i2 + IWorldPhone.EVENT_WP_GMSS_RAT_CHANGED_1, null);
            }
        }
        sModemSwitchHandler = new ModemSwitchHandler();
        logd(ModemSwitchHandler.modemToString(ModemSwitchHandler.getActiveModemType()));
        IntentFilter intentFilter = new IntentFilter("android.telephony.action.SIM_CARD_STATE_CHANGED");
        intentFilter.addAction("android.telephony.action.SIM_APPLICATION_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction(IWorldPhone.ACTION_SHUTDOWN_IPO);
        intentFilter.addAction(IWorldPhone.ACTION_ADB_SWITCH_MODEM);
        intentFilter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        intentFilter.addAction(IWorldPhone.ACTION_SAP_CONNECTION_STATE_CHANGED);
        Phone phone = sDefultPhone;
        if (phone != null) {
            sContext = phone.getContext();
        } else {
            logd("DefaultPhone = null");
        }
        sVoiceCapable = sContext.getResources().getBoolean(17891571);
        sContext.registerReceiver(this.mWorldPhoneReceiver, intentFilter);
        sTddStandByCounter = 0;
        sFddStandByCounter = 0;
        sWaitInTdd = false;
        sWaitInFdd = false;
        sWaitInEmsrResume = false;
        sRegion = 0;
        sLastPlmn = null;
        sBtSapState = 0;
        sIsWaintInTddTimeOut = 0;
        sIsWaintInFddTimeOut = 0;
        resetAllProperties();
        if (WorldPhoneUtil.getModemSelectionMode() == 0) {
            logd("Auto select disable");
            sIsAutoSelectEnable = false;
            SystemProperties.set(IWorldPhone.WORLD_PHONE_AUTO_SELECT_MODE, Integer.toString(0));
        } else {
            logd("Auto select enable");
            sIsAutoSelectEnable = true;
            SystemProperties.set(IWorldPhone.WORLD_PHONE_AUTO_SELECT_MODE, Integer.toString(1));
        }
        int[] iArr = FDD_STANDBY_TIMER;
        int i3 = sFddStandByCounter;
        iArr[i3] = SystemProperties.getInt(IWorldPhone.WORLD_PHONE_FDD_MODEM_TIMER, iArr[i3]);
        SystemProperties.set(IWorldPhone.WORLD_PHONE_FDD_MODEM_TIMER, Integer.toString(FDD_STANDBY_TIMER[sFddStandByCounter]));
        logd("FDD_STANDBY_TIMER = " + FDD_STANDBY_TIMER[sFddStandByCounter] + "s");
        StringBuilder sb = new StringBuilder();
        sb.append("sDefaultBootuUpModem = ");
        sb.append(sDefaultBootuUpModem);
        logd(sb.toString());
    }

    /* access modifiers changed from: private */
    public void handleSimCardStateChanged(int slotId, int state) {
        if (state == 1) {
            sLastPlmn = null;
            sImsi[slotId] = "";
            sFirstSelect[slotId] = true;
            sIsInvalidSim[slotId] = false;
            sSuspendWaitImsi[slotId] = false;
            if (slotId == sMajorSim) {
                logd("Major SIM removed, no world phone service");
                removeModemStandByTimer();
                sUserType = 0;
                sDenyReason = 1;
                sMajorSim = -99;
                sRegion = 0;
                return;
            }
            logd("SIM" + slotId + " is not major SIM");
        }
    }

    /* access modifiers changed from: private */
    public void handleSimApplicationStateChanged(int slotId, int state) {
        if (true == WorldPhoneUtil.getSimLockedState(state) && sIsAutoSelectEnable && slotId != sMajorSim) {
            sSimLocked = 1;
        }
        if (!WorldPhoneUtil.getSimLockedState(state) && sIsAutoSelectEnable && slotId != sMajorSim && sSimLocked == 1) {
            logd("retry to world mode change after not major sim pin unlock");
            sSimLocked = 0;
            handleSimSwitched();
        }
        if (state == 10) {
            if (sMajorSim == -99) {
                sMajorSim = WorldPhoneUtil.getMajorSim();
            }
            sUiccController = UiccController.getInstance();
            if (sUiccController != null) {
                sIccRecordsInstance[slotId] = sProxyPhones[slotId].getIccCard().getIccRecords();
                IccRecords[] iccRecordsArr = sIccRecordsInstance;
                if (iccRecordsArr[slotId] != null) {
                    sImsi[slotId] = iccRecordsArr[slotId].getIMSI();
                    if (!sIsAutoSelectEnable || slotId != sMajorSim) {
                        logd("Not major SIM");
                        getUserType(sImsi[slotId]);
                        boolean[] zArr = sSuspendWaitImsi;
                        if (zArr[slotId]) {
                            zArr[slotId] = false;
                            logd("IMSI fot slot" + slotId + ", resuming with ID:" + sSuspendId[slotId]);
                            sCi[slotId].setResumeRegistration(sSuspendId[slotId], null);
                            return;
                        }
                        return;
                    }
                    logd("Major SIM");
                    sUserType = getUserType(sImsi[slotId]);
                    boolean[] zArr2 = sFirstSelect;
                    if (zArr2[slotId]) {
                        zArr2[slotId] = false;
                        int i = sUserType;
                        if (i == 1 || i == 2) {
                            sSwitchModemCauseType = 0;
                            logd("sSwitchModemCauseType = " + sSwitchModemCauseType);
                            int i2 = sRegion;
                            if (i2 == 1) {
                                handleSwitchModem(101);
                            } else if (i2 == 2) {
                                handleSwitchModem(100);
                            } else {
                                logd("Region unknown");
                            }
                        } else if (i == 3) {
                            sSwitchModemCauseType = 255;
                            logd("sSwitchModemCauseType = " + sSwitchModemCauseType);
                            handleSwitchModem(100);
                        }
                    }
                    boolean[] zArr3 = sSuspendWaitImsi;
                    if (zArr3[slotId]) {
                        zArr3[slotId] = false;
                        if (sNwPlmnStrings != null) {
                            logd("IMSI fot slot" + slotId + " now ready, resuming PLMN:" + sNwPlmnStrings[0] + " with ID:" + sSuspendId[slotId] + " sWaitInEmsrResume:" + sWaitInEmsrResume);
                            if (!sWaitInEmsrResume) {
                                resumeCampingProcedure(slotId, false);
                            } else {
                                resumeCampingProcedure(slotId, true);
                            }
                        } else {
                            logd("sNwPlmnStrings is Null");
                        }
                    }
                } else {
                    logd("Null sIccRecordsInstance");
                }
            } else {
                logd("Null sUiccController");
            }
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        int i = msg.what;
        if (i == 0) {
            logd("handleMessage : <EVENT_RADIO_ON_1>");
            handleRadioOn(0);
        } else if (i == 1) {
            logd("handleMessage : <EVENT_RADIO_ON_2>");
            handleRadioOn(1);
        } else if (i == 2) {
            logd("handleMessage : <EVENT_RADIO_ON_3>");
            handleRadioOn(2);
        } else if (i != 3) {
            switch (i) {
                case 10:
                    logd("handleMessage : <EVENT_REG_PLMN_CHANGED_1>");
                    handlePlmnChange(ar, 0);
                    return;
                case 11:
                    logd("handleMessage : <EVENT_REG_PLMN_CHANGED_2>");
                    handlePlmnChange(ar, 1);
                    return;
                case 12:
                    logd("handleMessage : <EVENT_REG_PLMN_CHANGED_3>");
                    handlePlmnChange(ar, 2);
                    return;
                case 13:
                    logd("handleMessage : <EVENT_REG_PLMN_CHANGED_4>");
                    handlePlmnChange(ar, 3);
                    return;
                default:
                    switch (i) {
                        case IWorldPhone.EVENT_REG_SUSPENDED_1:
                            logd("handleMessage : <EVENT_REG_SUSPENDED_1>");
                            handleRegistrationSuspend(ar, 0);
                            return;
                        case IWorldPhone.EVENT_REG_SUSPENDED_2:
                            logd("handleMessage : <EVENT_REG_SUSPENDED_2>");
                            handleRegistrationSuspend(ar, 1);
                            return;
                        case 32:
                            logd("handleMessage : <EVENT_REG_SUSPENDED_3>");
                            handleRegistrationSuspend(ar, 2);
                            return;
                        case 33:
                            logd("handleMessage : <EVENT_REG_SUSPENDED_4>");
                            handleRegistrationSuspend(ar, 3);
                            return;
                        default:
                            switch (i) {
                                case 60:
                                    logd("handleMessage : <EVENT_INVALID_SIM_NOTIFY_1>");
                                    handleInvalidSimNotify(0, ar);
                                    return;
                                case IWorldPhone.EVENT_INVALID_SIM_NOTIFY_2:
                                    logd("handleMessage : <EVENT_INVALID_SIM_NOTIFY_2>");
                                    handleInvalidSimNotify(1, ar);
                                    return;
                                case IWorldPhone.EVENT_INVALID_SIM_NOTIFY_3:
                                    logd("handleMessage : <EVENT_INVALID_SIM_NOTIFY_3>");
                                    handleInvalidSimNotify(2, ar);
                                    return;
                                case IWorldPhone.EVENT_INVALID_SIM_NOTIFY_4:
                                    logd("handleMessage : <EVENT_INVALID_SIM_NOTIFY_4>");
                                    handleInvalidSimNotify(3, ar);
                                    return;
                                default:
                                    switch (i) {
                                        case IWorldPhone.EVENT_RESUME_CAMPING_1:
                                            if (ar.exception != null) {
                                                logd("handleMessage : <EVENT_RESUME_CAMPING_1> with exception");
                                                sIsResumeCampingFail1 = true;
                                                return;
                                            }
                                            return;
                                        case IWorldPhone.EVENT_RESUME_CAMPING_2:
                                            if (ar.exception != null) {
                                                logd("handleMessage : <EVENT_RESUME_CAMPING_2> with exception");
                                                sIsResumeCampingFail2 = true;
                                                return;
                                            }
                                            return;
                                        case IWorldPhone.EVENT_RESUME_CAMPING_3:
                                            if (ar.exception != null) {
                                                logd("handleMessage : <EVENT_RESUME_CAMPING_3> with exception");
                                                sIsResumeCampingFail3 = true;
                                                return;
                                            }
                                            return;
                                        case IWorldPhone.EVENT_RESUME_CAMPING_4:
                                            if (ar.exception != null) {
                                                logd("handleMessage : <EVENT_RESUME_CAMPING_4> with exception");
                                                sIsResumeCampingFail4 = true;
                                                return;
                                            }
                                            return;
                                        default:
                                            switch (i) {
                                                case IWorldPhone.EVENT_SERVICE_STATE_CHANGED_1:
                                                    logd("handleMessage : <EVENT_SERVICE_STATE_CHANGED_1>");
                                                    handleServiceStateChange(ar, 0);
                                                    return;
                                                case IWorldPhone.EVENT_SERVICE_STATE_CHANGED_2:
                                                    logd("handleMessage : <EVENT_SERVICE_STATE_CHANGED_2>");
                                                    handleServiceStateChange(ar, 1);
                                                    return;
                                                case IWorldPhone.EVENT_SERVICE_STATE_CHANGED_3:
                                                    logd("handleMessage : <EVENT_SERVICE_STATE_CHANGED_3>");
                                                    handleServiceStateChange(ar, 2);
                                                    return;
                                                case IWorldPhone.EVENT_SERVICE_STATE_CHANGED_4:
                                                    logd("handleMessage : <EVENT_SERVICE_STATE_CHANGED_4>");
                                                    handleServiceStateChange(ar, 3);
                                                    return;
                                                default:
                                                    switch (i) {
                                                        case IWorldPhone.EVENT_WP_GMSS_RAT_CHANGED_1:
                                                            logd("handleMessage : <EVENT_WP_GMSS_RAT_CHANGED_1>");
                                                            handleGmssRatChange(ar, 0);
                                                            return;
                                                        case IWorldPhone.EVENT_WP_GMSS_RAT_CHANGED_2:
                                                            logd("handleMessage : <EVENT_WP_GMSS_RAT_CHANGED_2>");
                                                            handleGmssRatChange(ar, 1);
                                                            return;
                                                        case IWorldPhone.EVENT_WP_GMSS_RAT_CHANGED_3:
                                                            logd("handleMessage : <EVENT_WP_GMSS_RAT_CHANGED_3>");
                                                            handleGmssRatChange(ar, 2);
                                                            return;
                                                        case IWorldPhone.EVENT_WP_GMSS_RAT_CHANGED_4:
                                                            logd("handleMessage : <EVENT_WP_GMSS_RAT_CHANGED_4>");
                                                            handleGmssRatChange(ar, 3);
                                                            return;
                                                        default:
                                                            logd("Unknown msg:" + msg.what);
                                                            return;
                                                    }
                                            }
                                    }
                            }
                    }
            }
        } else {
            logd("handleMessage : <EVENT_RADIO_ON_4>");
            handleRadioOn(3);
        }
    }

    private void handleRadioOn(int slotId) {
        sMajorSim = WorldPhoneUtil.getMajorSim();
        logd("handleRadioOn Slot:" + slotId + " sMajorSim:" + sMajorSim);
        sIsInvalidSim[slotId] = false;
        if (slotId != 0) {
            if (slotId != 1) {
                if (slotId != 2) {
                    if (slotId != 3) {
                        logd("unknow slotid");
                    } else if (sIsResumeCampingFail4) {
                        logd("try to resume camping again");
                        sCi[slotId].setResumeRegistration(sSuspendId[slotId], null);
                        sIsResumeCampingFail4 = false;
                    }
                } else if (sIsResumeCampingFail3) {
                    logd("try to resume camping again");
                    sCi[slotId].setResumeRegistration(sSuspendId[slotId], null);
                    sIsResumeCampingFail3 = false;
                }
            } else if (sIsResumeCampingFail2) {
                logd("try to resume camping again");
                sCi[slotId].setResumeRegistration(sSuspendId[slotId], null);
                sIsResumeCampingFail2 = false;
            }
        } else if (sIsResumeCampingFail1) {
            logd("try to resume camping again");
            sCi[slotId].setResumeRegistration(sSuspendId[slotId], null);
            sIsResumeCampingFail1 = false;
        }
    }

    private void handlePlmnChange(AsyncResult ar, int slotId) {
        int i;
        sMajorSim = WorldPhoneUtil.getMajorSim();
        logd("Slot:" + slotId + " sMajorSim:" + sMajorSim);
        if (ar.exception != null || ar.result == null) {
            logd("AsyncResult is wrong " + ar.exception);
            return;
        }
        String[] plmnString = (String[]) ar.result;
        if (slotId == sMajorSim) {
            sNwPlmnStrings = plmnString;
        }
        for (int i2 = 0; i2 < plmnString.length; i2++) {
            logd("plmnString[" + i2 + "]=" + plmnString[i2]);
        }
        if (sIsAutoSelectEnable) {
            if (sMajorSim == slotId && (((i = sUserType) == 1 || i == 2) && sDenyReason != 2)) {
                searchForDesignateService(plmnString[0]);
            }
            sRegion = getRegion(plmnString[0]);
            if (sUserType != 3 && sRegion == 2 && sMajorSim != -1) {
                sSwitchModemCauseType = 0;
                logd("sSwitchModemCauseType = " + sSwitchModemCauseType);
                handleSwitchModem(100);
            }
        }
    }

    private static synchronized void initNWPlmnString() {
        synchronized (WorldPhoneOp01.class) {
            if (sNwPlmnStrings == null) {
                sNwPlmnStrings = new String[1];
            }
        }
    }

    private void handleGmssRatChange(AsyncResult ar, int slotId) {
        sMajorSim = WorldPhoneUtil.getMajorSim();
        logd("Slot:" + slotId + " sMajorSim:" + sMajorSim);
        if (ar.exception != null || ar.result == null) {
            logd("AsyncResult is wrong " + ar.exception);
            return;
        }
        String mccString = Integer.toString(((int[]) ar.result)[1]);
        logd("[handleGmssRatChange] mccString=" + mccString);
        if (slotId == sMajorSim && mccString.length() >= 3) {
            initNWPlmnString();
            sNwPlmnStrings[0] = mccString;
        }
        if (sIsAutoSelectEnable) {
            sRegion = getRegion(mccString);
            if (sUserType != 3 && sRegion == 2 && sMajorSim != -1) {
                handleSwitchModem(100);
            }
        }
    }

    private void handleServiceStateChange(AsyncResult ar, int slotId) {
        sMajorSim = WorldPhoneUtil.getMajorSim();
        logd("Slot:" + slotId + " sMajorSim:" + sMajorSim + "RadioState:" + sCi[slotId].getRadioState());
        if (ar.exception != null || ar.result == null) {
            logd("AsyncResult is wrong " + ar.exception);
            return;
        }
        sServiceState = (MtkServiceState) ar.result;
        MtkServiceState mtkServiceState = sServiceState;
        if (mtkServiceState != null) {
            sPlmnSs = mtkServiceState.getOperatorNumeric();
            sVoiceRegState = sServiceState.getVoiceRegState();
            sRilVoiceRegState = sServiceState.getRilVoiceRegState();
            sRilVoiceRadioTechnology = sServiceState.getRilVoiceRadioTechnology();
            sDataRegState = sServiceState.getDataRegState();
            sRilDataRegState = sServiceState.getRilDataRegState();
            sRilDataRadioTechnology = sServiceState.getRilDataRadioTechnology();
            logd("slotId: " + slotId + ", sMajorSim: " + sMajorSim + ", sPlmnSs: " + sPlmnSs + ", sVoiceRegState: " + sVoiceRegState);
            StringBuilder sb = new StringBuilder();
            sb.append("sRilVoiceRegState: ");
            sb.append(sRilVoiceRegState);
            sb.append(", sRilVoiceRadioTech: ");
            MtkServiceState mtkServiceState2 = sServiceState;
            sb.append(MtkServiceState.rilRadioTechnologyToString(sRilVoiceRadioTechnology));
            sb.append(", sDataRegState: ");
            sb.append(sDataRegState);
            logd(sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("sRilDataRegState: ");
            sb2.append(sRilDataRegState);
            sb2.append(", sRilDataRadioTech: , ");
            MtkServiceState mtkServiceState3 = sServiceState;
            sb2.append(MtkServiceState.rilRadioTechnologyToString(sRilDataRadioTechnology));
            sb2.append(", sIsAutoSelectEnable: ");
            sb2.append(sIsAutoSelectEnable);
            logd(sb2.toString());
            logd(ModemSwitchHandler.modemToString(ModemSwitchHandler.getActiveModemType()));
            if (sIsAutoSelectEnable && slotId == sMajorSim) {
                if (isNoService() && sCi[slotId].getRadioState() != 2) {
                    handleNoService();
                } else if (isInService()) {
                    sLastPlmn = sPlmnSs;
                    removeModemStandByTimer();
                    sIsInvalidSim[slotId] = false;
                }
            }
        } else {
            logd("Null sServiceState");
        }
    }

    private void handleRegistrationSuspend(AsyncResult ar, int slotId) {
        logd("Registration Suspend Slot" + slotId);
        if (!ModemSwitchHandler.isModemTypeSwitching()) {
            if (ar.exception != null || ar.result == null) {
                logd("AsyncResult is wrong " + ar.exception);
                return;
            }
            sSuspendId[slotId] = ((int[]) ar.result)[0];
            logd("Suspending with Id=" + sSuspendId[slotId]);
            if (!sIsAutoSelectEnable || sMajorSim != slotId) {
                logd("Not major slot, camp on OK");
                sCi[slotId].setResumeRegistration(sSuspendId[slotId], null);
            } else if (sUserType != 0) {
                resumeCampingProcedure(slotId, true);
            } else {
                sSuspendWaitImsi[slotId] = true;
                if (!sWaitInEmsrResume) {
                    sWaitInEmsrResume = true;
                    logd("Wait EMSR:8s");
                    postDelayed(this.mEmsrResumeByTimerRunnable, 8000);
                } else {
                    logd("Emsr Resume Timer already set:8s");
                }
                logd("User type unknown, wait for IMSI");
            }
        }
    }

    private void handleInvalidSimNotify(int slotId, AsyncResult ar) {
        try {
            logd("Slot" + slotId);
            if (ar.exception != null || ar.result == null) {
                logd("AsyncResult is wrong " + ar.exception);
                return;
            }
            String[] invalidSimInfo = (String[]) ar.result;
            String plmn = invalidSimInfo[0];
            int cs_invalid = Integer.parseInt(invalidSimInfo[1]);
            int ps_invalid = Integer.parseInt(invalidSimInfo[2]);
            int cause = Integer.parseInt(invalidSimInfo[3]);
            int testMode = SystemProperties.getInt("vendor.gsm.gcf.testmode", 0);
            if (testMode != 0) {
                logd("Invalid SIM notified during test mode: " + testMode);
                return;
            }
            logd("testMode:" + testMode + ", cause: " + cause + ", cs_invalid: " + cs_invalid + ", ps_invalid: " + ps_invalid + ", plmn: " + plmn);
            if (sVoiceCapable && cs_invalid == 1 && sLastPlmn == null) {
                logd("CS reject, invalid SIM");
                sIsInvalidSim[slotId] = true;
            } else if (ps_invalid == 1 && sLastPlmn == null) {
                logd("PS reject, invalid SIM");
                sIsInvalidSim[slotId] = true;
            }
        } catch (NumberFormatException e) {
            Rlog.d(IWorldPhone.LOG_TAG, e.toString());
        } catch (Exception e2) {
            Rlog.d(IWorldPhone.LOG_TAG, e2.toString());
        }
    }

    /* access modifiers changed from: private */
    public boolean handleSwitchModem(int toModem) {
        int mMajorSim = WorldPhoneUtil.getMajorSim();
        if (sIsWaintInFddTimeOut != 0 || sIsWaintInTddTimeOut != 0) {
            sIsWaintInFddTimeOut = 0;
            sIsWaintInTddTimeOut = 0;
        } else if (isEccInProgress()) {
            logd("[handleSwitchModem]In ECC:" + this.mIsRegisterEccStateReceiver);
            if (!this.mIsRegisterEccStateReceiver) {
                registerEccStateReceiver();
            }
            return false;
        }
        if (sSimLocked == 1) {
            logd("sim has been locked!");
            return false;
        } else if (mMajorSim >= 0 && sIsInvalidSim[mMajorSim] && WorldPhoneUtil.getModemSelectionMode() == 1) {
            logd("Invalid SIM, switch not executed!");
            return false;
        } else if (!sIsAutoSelectEnable || isNeedSwitchModem()) {
            if (toModem == 101) {
                if (WorldPhoneUtil.isLteSupport()) {
                    toModem = 6;
                } else {
                    toModem = 4;
                }
            } else if (toModem == 100) {
                if (WorldPhoneUtil.isLteSupport()) {
                    toModem = 5;
                } else {
                    toModem = 3;
                }
            }
            if (toModem == ModemSwitchHandler.getActiveModemType()) {
                if (toModem == 3) {
                    logd("Already in WG modem");
                } else if (toModem == 4) {
                    logd("Already in TG modem");
                } else if (toModem == 5) {
                    logd("Already in FDD CSFB modem");
                } else if (toModem == 6) {
                    logd("Already in TDD CSFB modem");
                }
                return false;
            }
            if (!sIsAutoSelectEnable) {
                logd("Storing modem type: " + toModem);
                sCi[0].storeModemType(toModem, null);
            } else {
                int i = sDefaultBootuUpModem;
                if (i == 0) {
                    logd("Storing modem type: " + toModem);
                    sCi[0].storeModemType(toModem, null);
                } else if (i == 100) {
                    if (WorldPhoneUtil.isLteSupport()) {
                        logd("Storing modem type: 5");
                        sCi[0].storeModemType(5, null);
                    } else {
                        logd("Storing modem type: 3");
                        sCi[0].storeModemType(3, null);
                    }
                } else if (i == 101) {
                    if (WorldPhoneUtil.isLteSupport()) {
                        logd("Storing modem type: 6");
                        sCi[0].storeModemType(6, null);
                    } else {
                        logd("Storing modem type: 4");
                        sCi[0].storeModemType(4, null);
                    }
                }
            }
            for (int i2 = 0; i2 < PROJECT_SIM_NUM; i2++) {
                if (sActivePhones[i2].getState() != PhoneConstants.State.IDLE) {
                    logd("Phone" + i2 + " is not idle, modem switch not allowed");
                    return false;
                }
            }
            removeModemStandByTimer();
            if (toModem == 3) {
                logd("Switching to WG modem");
            } else if (toModem == 4) {
                logd("Switching to TG modem");
            } else if (toModem == 5) {
                logd("Switching to FDD CSFB modem");
            } else if (toModem == 6) {
                logd("Switching to TDD CSFB modem");
            }
            if (WorldPhoneUtil.isSimSwitching() && toModem == WorldPhoneUtil.getToModemType()) {
                logd("sim switching, already will to set modem:" + toModem);
                return false;
            }
            ModemSwitchHandler.reloadModemCauseType(sCi[0], sSwitchModemCauseType);
            ModemSwitchHandler.switchModem(0, toModem);
            resetNetworkProperties();
            return true;
        } else {
            logd("[handleSwitchModem]No need to handle, switch not executed!");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void handleSimSwitched() {
        int i = sMajorSim;
        if (i == -1) {
            logd("Major capability turned off");
            removeModemStandByTimer();
            sUserType = 0;
        } else if (!sIsAutoSelectEnable) {
            logd("Auto modem selection disabled");
            removeModemStandByTimer();
        } else if (i == -99) {
            logd("Major SIM unknown");
        } else {
            logd("Auto modem selection enabled");
            logd("Major capability in slot" + sMajorSim);
            String[] strArr = sImsi;
            int i2 = sMajorSim;
            if (strArr[i2] == null || strArr[i2].equals("")) {
                logd("Major slot IMSI not ready");
                sUserType = 0;
                return;
            }
            sSwitchModemCauseType = 255;
            logd("sSwitchModemCauseType = " + sSwitchModemCauseType);
            sUserType = getUserType(sImsi[sMajorSim]);
            int i3 = sUserType;
            if (i3 == 1 || i3 == 2) {
                String[] strArr2 = sNwPlmnStrings;
                if (strArr2 != null) {
                    sRegion = getRegion(strArr2[0]);
                }
                int i4 = sRegion;
                if (i4 == 1) {
                    sFirstSelect[sMajorSim] = false;
                    handleSwitchModem(101);
                } else if (i4 == 2) {
                    sFirstSelect[sMajorSim] = false;
                    handleSwitchModem(100);
                } else {
                    logd("Unknown region");
                }
            } else if (i3 == 3) {
                sFirstSelect[sMajorSim] = false;
                handleSwitchModem(100);
            } else {
                logd("Unknown user type");
            }
        }
    }

    private void handleNoService() {
        logd("[handleNoService]+ Can not find service");
        logd(PplSmsFilterExtension.INSTRUCTION_KEY_TYPE + sUserType + " user");
        logd(WorldPhoneUtil.regionToString(sRegion));
        int mdType = ModemSwitchHandler.getActiveModemType();
        logd(ModemSwitchHandler.modemToString(mdType));
        if (sProxyPhones[sMajorSim].getIccCard().getState() == IccCardConstants.State.READY) {
            int i = sUserType;
            if (i == 1 || i == 2) {
                if (mdType == 6 || mdType == 4) {
                    if (TDD_STANDBY_TIMER[sTddStandByCounter] < 0) {
                        logd("Standby in TDD modem");
                    } else if (!sWaitInTdd) {
                        sWaitInTdd = true;
                        logd("Wait " + TDD_STANDBY_TIMER[sTddStandByCounter] + "s. Timer index = " + sTddStandByCounter);
                        postDelayed(this.mTddStandByTimerRunnable, (long) (TDD_STANDBY_TIMER[sTddStandByCounter] * 1000));
                    } else {
                        logd("Timer already set:" + TDD_STANDBY_TIMER[sTddStandByCounter] + "s");
                    }
                } else if (mdType == 5 || mdType == 3) {
                    if (FDD_STANDBY_TIMER[sFddStandByCounter] < 0) {
                        logd("Standby in FDD modem");
                    } else if (sRegion != 2) {
                        sSwitchModemCauseType = 1;
                        logd("sSwitchModemCauseType = " + sSwitchModemCauseType);
                        handleSwitchModem(101);
                    } else if (!sWaitInFdd) {
                        sWaitInFdd = true;
                        logd("Wait " + FDD_STANDBY_TIMER[sFddStandByCounter] + "s. Timer index = " + sFddStandByCounter);
                        postDelayed(this.mFddStandByTimerRunnable, (long) (FDD_STANDBY_TIMER[sFddStandByCounter] * 1000));
                    } else {
                        logd("Timer already set:" + FDD_STANDBY_TIMER[sFddStandByCounter] + "s");
                    }
                }
            } else if (i != 3) {
                logd("Unknow user type");
            } else if (mdType == 5 || mdType == 3) {
                logd("Standby in FDD modem");
            } else {
                logd("Should not enter this state");
            }
        } else {
            logd("IccState not ready");
        }
        logd("[handleNoService]-");
    }

    private boolean isAllowCampOn(String plmnString, int slotId) {
        int mdType;
        logd("[isAllowCampOn]" + plmnString + "User type: " + sUserType);
        sRegion = getRegion(plmnString);
        if (WorldPhoneUtil.isSimSwitching()) {
            mdType = WorldPhoneUtil.getToModemType();
            logd("SimSwitching mdType:" + ModemSwitchHandler.modemToString(mdType));
        } else {
            mdType = ModemSwitchHandler.getActiveModemType();
            logd("mdType:" + ModemSwitchHandler.modemToString(mdType));
        }
        int i = sUserType;
        if (i == 1 || i == 2) {
            int i2 = sRegion;
            if (i2 == 1) {
                if (mdType == 6 || mdType == 4) {
                    sDenyReason = 0;
                    logd("Camp on OK");
                    return true;
                } else if (mdType == 5 || mdType == 3) {
                    sDenyReason = 3;
                    logd("Camp on REJECT");
                    return false;
                }
            } else if (i2 != 2) {
                logd("Unknow region");
            } else if (mdType == 6 || mdType == 4) {
                sDenyReason = 2;
                logd("Camp on REJECT");
                return false;
            } else if (mdType == 5 || mdType == 3) {
                sDenyReason = 0;
                logd("Camp on OK");
                return true;
            }
        } else if (i != 3) {
            logd("Unknown user type");
        } else if (mdType == 6 || mdType == 4) {
            sDenyReason = 2;
            logd("Camp on REJECT");
            return false;
        } else if (mdType == 5 || mdType == 3) {
            sDenyReason = 0;
            logd("Camp on OK");
            return true;
        }
        sDenyReason = 1;
        logd("Camp on REJECT");
        return false;
    }

    private boolean isInService() {
        boolean inService = false;
        if (sVoiceRegState == 0 || sDataRegState == 0) {
            inService = true;
        }
        logd("inService: " + inService);
        return inService;
    }

    private boolean isNoService() {
        boolean noService;
        int i;
        if (sVoiceRegState == 1 && (((i = sRilVoiceRegState) == 0 || i == 10) && sDataRegState == 1 && sRilDataRegState == 0)) {
            noService = true;
        } else {
            noService = false;
        }
        logd("noService: " + noService);
        return noService;
    }

    private int getRegion(String plmn) {
        if (plmn == null || plmn.equals("") || plmn.length() < 3) {
            logd("[getRegion] Invalid PLMN");
            return 0;
        }
        String currentMcc = plmn.length() >= 5 ? plmn.substring(0, 5) : null;
        if (currentMcc != null && (currentMcc.equals("46602") || currentMcc.equals("50270"))) {
            return 1;
        }
        String currentMcc2 = plmn.substring(0, 3);
        for (String mcc : MCC_TABLE_DOMESTIC) {
            if (currentMcc2.equals(mcc)) {
                logd("[getRegion] REGION_DOMESTIC");
                return 1;
            }
        }
        logd("[getRegion] REGION_FOREIGN");
        return 2;
    }

    private int getUserType(String imsi) {
        if (imsi == null || imsi.equals("")) {
            logd("[getUserType] null IMSI");
            return 0;
        }
        String imsi2 = imsi.substring(0, 5);
        for (String mccmnc : PLMN_TABLE_TYPE1) {
            if (imsi2.equals(mccmnc)) {
                logd("[getUserType] Type1 user");
                return 1;
            }
        }
        for (String mccmnc2 : PLMN_TABLE_TYPE3) {
            if (imsi2.equals(mccmnc2)) {
                logd("[getUserType] Type3 user");
                return 3;
            }
        }
        logd("[getUserType] Type2 user");
        return 2;
    }

    private void resumeCampingProcedure(int slotId, boolean isResumeModem) {
        logd("Resume camping slot:" + slotId + " isResumeModem:" + isResumeModem + ", sSimLocked:" + sSimLocked);
        String plmnString = sNwPlmnStrings[0];
        boolean switchModem = false;
        if (sSimLocked == 1 || isAllowCampOn(plmnString, slotId) || !isNeedSwitchModem()) {
            switchModem = false;
        } else {
            sSwitchModemCauseType = 0;
            logd("sSwitchModemCauseType=" + sSwitchModemCauseType + ",DenyReason=" + sDenyReason);
            int i = sDenyReason;
            if (i == 2) {
                switchModem = handleSwitchModem(100);
            } else if (i == 3) {
                switchModem = handleSwitchModem(101);
            }
        }
        if (!switchModem) {
            removeModemStandByTimer();
            removeEmsrResumeByTimer();
            if (isResumeModem) {
                sCi[slotId].setResumeRegistration(sSuspendId[slotId], obtainMessage(slotId + 70));
            }
        }
    }

    private void removeModemStandByTimer() {
        if (sWaitInTdd) {
            logd("Remove TDD wait timer. Set sWaitInTdd = false");
            sWaitInTdd = false;
            removeCallbacks(this.mTddStandByTimerRunnable);
        }
        if (sWaitInFdd) {
            logd("Remove FDD wait timer. Set sWaitInFdd = false");
            sWaitInFdd = false;
            removeCallbacks(this.mFddStandByTimerRunnable);
        }
    }

    private void removeEmsrResumeByTimer() {
        if (sWaitInEmsrResume) {
            logd("Remove EMSR wait timer. Set sWaitInEmsrResume = false");
            sWaitInEmsrResume = false;
            removeCallbacks(this.mEmsrResumeByTimerRunnable);
        }
    }

    private void resetAllProperties() {
        logd("[resetAllProperties]");
        sNwPlmnStrings = null;
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            sFirstSelect[i] = true;
        }
        sDenyReason = 1;
        resetSimProperties();
        resetNetworkProperties();
        sSimLocked = 0;
    }

    private void resetNetworkProperties() {
        logd("[resetNetworkProperties]");
        synchronized (sLock) {
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                sSuspendWaitImsi[i] = false;
            }
            if (sNwPlmnStrings != null) {
                for (int i2 = 0; i2 < sNwPlmnStrings.length; i2++) {
                    sNwPlmnStrings[i2] = "";
                }
            }
            removeEmsrResumeByTimer();
            sSwitchModemCauseType = 255;
            logd("sSwitchModemCauseType = " + sSwitchModemCauseType);
        }
    }

    private void resetSimProperties() {
        logd("[resetSimProperties]");
        synchronized (sLock) {
            for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                sImsi[i] = "";
            }
            sUserType = 0;
            sMajorSim = WorldPhoneUtil.getMajorSim();
        }
    }

    private void searchForDesignateService(String strPlmn) {
        if (strPlmn == null) {
            logd("[searchForDesignateService]- null source");
            return;
        }
        String strPlmn2 = strPlmn.substring(0, 5);
        for (String mccmnc : PLMN_TABLE_TYPE1) {
            if (strPlmn2.equals(mccmnc)) {
                logd("Find TD service");
                logd("sUserType: " + sUserType + " sRegion: " + sRegion);
                logd(ModemSwitchHandler.modemToString(ModemSwitchHandler.getActiveModemType()));
                sSwitchModemCauseType = 0;
                logd("sSwitchModemCauseType = " + sSwitchModemCauseType);
                handleSwitchModem(101);
                return;
            }
        }
    }

    @Override // com.mediatek.internal.telephony.worldphone.IWorldPhone
    public void setModemSelectionMode(int mode, int modemType) {
        SystemProperties.set(IWorldPhone.WORLD_PHONE_AUTO_SELECT_MODE, Integer.toString(mode));
        if (mode == 1) {
            logd("Modem Selection <AUTO>");
            sIsAutoSelectEnable = true;
            sMajorSim = WorldPhoneUtil.getMajorSim();
            handleSimSwitched();
            return;
        }
        logd("Modem Selection <MANUAL>");
        sIsAutoSelectEnable = false;
        sSwitchModemCauseType = 255;
        logd("sSwitchModemCauseType = " + sSwitchModemCauseType);
        handleSwitchModem(modemType);
        if (modemType == ModemSwitchHandler.getActiveModemType()) {
            removeModemStandByTimer();
        }
    }

    @Override // com.mediatek.internal.telephony.worldphone.IWorldPhone
    public void notifyRadioCapabilityChange(int capabilitySimId) {
        int toModem;
        logd("[setRadioCapabilityChange]");
        logd("Major capability will be set to slot:" + capabilitySimId);
        removeEmsrResumeByTimer();
        if (!sIsAutoSelectEnable) {
            logd("Auto modem selection disabled");
            removeModemStandByTimer();
            return;
        }
        logd("Auto modem selection enabled");
        String[] strArr = sImsi;
        if (strArr[capabilitySimId] == null || strArr[capabilitySimId].equals("")) {
            logd("Capaility slot IMSI not ready");
            sUserType = 0;
            return;
        }
        sUserType = getUserType(sImsi[capabilitySimId]);
        int i = sUserType;
        if (i == 1 || i == 2) {
            String[] strArr2 = sNwPlmnStrings;
            if (strArr2 != null) {
                sRegion = getRegion(strArr2[0]);
            }
            int i2 = sRegion;
            if (i2 == 1) {
                sFirstSelect[capabilitySimId] = false;
                toModem = 101;
            } else if (i2 == 2) {
                sFirstSelect[capabilitySimId] = false;
                toModem = 100;
            } else {
                logd("Unknown region");
                return;
            }
        } else if (i == 3) {
            sFirstSelect[capabilitySimId] = false;
            toModem = 100;
        } else {
            logd("Unknown user type");
            return;
        }
        if (toModem == 101) {
            if (WorldPhoneUtil.isLteSupport()) {
                toModem = 6;
            } else {
                toModem = 4;
            }
        } else if (toModem == 100) {
            if (WorldPhoneUtil.isLteSupport()) {
                toModem = 5;
            } else {
                toModem = 3;
            }
        }
        logd("notifyRadioCapabilityChange: Storing modem type: " + toModem);
        if (isNeedReloadModem(capabilitySimId)) {
            sCi[0].reloadModemType(toModem, null);
            resetNetworkProperties();
            WorldPhoneUtil.setSimSwitchingFlag(true);
            WorldPhoneUtil.saveToModemType(toModem);
        }
    }

    private boolean isNeedSwitchModem() {
        boolean isNeed = true;
        int majorSimId = WorldPhoneUtil.getMajorSim();
        if (WorldPhoneUtil.isC2kSupport()) {
            int activeSvlteModeSlotId = WorldPhoneUtil.getActiveSvlteModeSlotId();
            if (sUserType == 2 && (((majorSimId >= 0 && majorSimId == activeSvlteModeSlotId) || isCdmaCard(majorSimId)) && ModemSwitchHandler.getActiveModemType() == 5)) {
                isNeed = false;
            }
        }
        logd("[isNeedSwitchModem] isNeed = " + isNeed);
        return isNeed;
    }

    private boolean isNeedReloadModem(int capabilitySimId) {
        boolean isNeed = true;
        if (WorldPhoneUtil.isC2kSupport()) {
            int activeSvlteModeSlotId = WorldPhoneUtil.getActiveSvlteModeSlotId();
            logd("[isNeedReloadModem] activeSvlteModeSlotId = " + activeSvlteModeSlotId + ", sUserType = " + sUserType + ", capabilitySimId = " + capabilitySimId);
            if (sUserType == 2 && (((capabilitySimId >= 0 && capabilitySimId == activeSvlteModeSlotId) || isCdmaCard(capabilitySimId)) && ModemSwitchHandler.getActiveModemType() == 5)) {
                isNeed = false;
            }
        }
        logd("[isNeedReloadModem] isNeed = " + isNeed);
        return isNeed;
    }

    private boolean isCdmaCard(int slotId) {
        boolean retCdmaCard = false;
        if (!SubscriptionManager.isValidPhoneId(slotId)) {
            return false;
        }
        int[] cardType = WorldPhoneUtil.getC2KWPCardType();
        logd("isCdmaCard(), cardType=" + cardType[slotId]);
        if ((cardType[slotId] & 4) > 0 || (cardType[slotId] & 8) > 0 || isCt3gDualMode(slotId)) {
            retCdmaCard = true;
        }
        logd("isCdmaCard(), slotId=" + slotId + " retCdmaCard=" + retCdmaCard);
        return retCdmaCard;
    }

    private boolean isCt3gDualMode(int slotId) {
        if (slotId >= 0) {
            String[] strArr = PROPERTY_RIL_CT3G;
            if (slotId < strArr.length) {
                String result = SystemProperties.get(strArr[slotId], "");
                logd("isCt3gDualMode: " + result);
                return "1".equals(result);
            }
        }
        logd("isCt3gDualMode: invalid slotId " + slotId);
        return false;
    }

    private void registerEccStateReceiver() {
        if (sContext == null) {
            logd("registerEccStateReceiver, context is null => return");
            return;
        }
        IntentFilter filter = new IntentFilter("android.intent.action.PHONE_STATE");
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        sContext.registerReceiver(this.mWorldPhoneEccStateReceiver, filter);
        this.mIsRegisterEccStateReceiver = true;
    }

    /* access modifiers changed from: private */
    public void unRegisterEccStateReceiver() {
        Context context = sContext;
        if (context == null) {
            logd("unRegisterEccStateReceiver, context is null => return");
            return;
        }
        context.unregisterReceiver(this.mWorldPhoneEccStateReceiver);
        this.mIsRegisterEccStateReceiver = false;
    }

    /* access modifiers changed from: private */
    public boolean isEccInProgress() {
        String value = SystemProperties.get("ril.cdma.inecmmode", "");
        boolean inEcm = value.contains("true");
        boolean isInEcc = false;
        ITelecomService tm = ITelecomService.Stub.asInterface(ServiceManager.getService("telecom"));
        if (tm != null) {
            try {
                isInEcc = tm.isInEmergencyCall();
            } catch (RemoteException e) {
                logd("Exception of isEccInProgress");
            }
        }
        logd("isEccInProgress, value:" + value + ", inEcm:" + inEcm + ", isInEcc:" + isInEcc);
        return inEcm || isInEcc;
    }

    /* access modifiers changed from: private */
    public static void logd(String msg) {
        Rlog.d(IWorldPhone.LOG_TAG, "[WPOP01]" + msg);
    }
}
