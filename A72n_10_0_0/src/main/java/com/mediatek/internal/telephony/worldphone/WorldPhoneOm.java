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
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.internal.telephony.ppl.PplSmsFilterExtension;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.util.ArrayList;
import java.util.Iterator;
import mediatek.telephony.MtkServiceState;

public class WorldPhoneOm extends Handler implements IWorldPhone {
    private static final int EMSR_STANDBY_TIMER = 8;
    private static final int[] FDD_STANDBY_TIMER = {60};
    private static final String[] MCC_TABLE_DOMESTIC = {"460"};
    private static final String[] PLMN_TABLE_TYPE1 = {"46000", "46002", "46004", "46007", "46008"};
    private static final String[] PLMN_TABLE_TYPE1_EXT = {"45412"};
    private static final String[] PLMN_TABLE_TYPE3 = {"46001", "46006", "46009", "45407", "46003", "46005", "45502", "46011"};
    private static final int PROJECT_SIM_NUM = WorldPhoneUtil.getProjectSimNum();
    private static final int[] TDD_STANDBY_TIMER = {40};
    private static Phone[] sActivePhones;
    private static int sBtSapState;
    private static MtkRIL[] sCi;
    private static Context sContext = null;
    private static int sDataRegState;
    private static int sDefaultBootuUpModem = 0;
    private static Phone sDefultPhone = null;
    private static int sDenyReason;
    private static int sFddStandByCounter;
    private static boolean[] sFirstSelect;
    private static IccRecords[] sIccRecordsInstance;
    private static String[] sImsi;
    private static boolean sIsAutoSelectEnable;
    private static boolean[] sIsInvalidSim;
    private static boolean sIsResumeCampingFail1;
    private static boolean sIsResumeCampingFail2;
    private static boolean sIsResumeCampingFail3;
    private static boolean sIsResumeCampingFail4;
    private static int sIsWaintInFddTimeOut;
    private static int sIsWaintInTddTimeOut;
    private static String sLastPlmn;
    private static Object sLock = new Object();
    private static int sMajorSim;
    private static ArrayList<String> sMccDomestic;
    private static ModemSwitchHandler sModemSwitchHandler = null;
    private static String[] sNwPlmnStrings;
    private static String sPlmnSs;
    private static ArrayList<String> sPlmnType1;
    private static ArrayList<String> sPlmnType1Ext;
    private static ArrayList<String> sPlmnType3;
    private static Phone[] sProxyPhones = null;
    private static int sRegion;
    private static int sRilDataRadioTechnology;
    private static int sRilDataRegState;
    private static int sRilVoiceRadioTechnology;
    private static int sRilVoiceRegState;
    private static MtkServiceState sServiceState;
    private static int sSimLocked;
    private static int sSimLockedSlotId = -1;
    private static int[] sSuspendId;
    private static boolean[] sSuspendWaitImsi;
    private static int sTddStandByCounter;
    private static UiccController sUiccController = null;
    private static int sUserType;
    private static boolean sVoiceCapable;
    private static int sVoiceRegState;
    private static boolean sWaitInEmsrResume;
    private static boolean sWaitInFdd;
    private static boolean sWaitInTdd;
    private static CommandsInterface[] smCi;
    private Runnable mEmsrResumeByTimerRunnable = new Runnable() {
        /* class com.mediatek.internal.telephony.worldphone.WorldPhoneOm.AnonymousClass4 */

        public void run() {
            boolean unused = WorldPhoneOm.sWaitInEmsrResume = false;
            int unused2 = WorldPhoneOm.sMajorSim = WorldPhoneUtil.getMajorSim();
            if (WorldPhoneOm.sMajorSim != -99 && WorldPhoneOm.sMajorSim != -1 && WorldPhoneOm.sSuspendWaitImsi[WorldPhoneOm.sMajorSim]) {
                WorldPhoneOm.sCi[WorldPhoneOm.sMajorSim].setResumeRegistration(WorldPhoneOm.sSuspendId[WorldPhoneOm.sMajorSim], WorldPhoneOm.this.obtainMessage(WorldPhoneOm.sMajorSim + 70));
            }
        }
    };
    private Runnable mFddStandByTimerRunnable = new Runnable() {
        /* class com.mediatek.internal.telephony.worldphone.WorldPhoneOm.AnonymousClass3 */

        public void run() {
            WorldPhoneOm.access$1808();
            if (WorldPhoneOm.sFddStandByCounter >= WorldPhoneOm.FDD_STANDBY_TIMER.length) {
                int unused = WorldPhoneOm.sFddStandByCounter = WorldPhoneOm.FDD_STANDBY_TIMER.length - 1;
            }
            if (WorldPhoneOm.sBtSapState == 0) {
                WorldPhoneOm.logd("FDD time out!");
                int unused2 = WorldPhoneOm.sIsWaintInFddTimeOut = 1;
                WorldPhoneOm.this.handleSwitchModem(101);
                return;
            }
            WorldPhoneOm.logd("FDD time out but BT SAP is connected, switch not executed!");
        }
    };
    private boolean mIsRegisterEccStateReceiver = false;
    private Runnable mTddStandByTimerRunnable = new Runnable() {
        /* class com.mediatek.internal.telephony.worldphone.WorldPhoneOm.AnonymousClass2 */

        public void run() {
            WorldPhoneOm.access$1408();
            if (WorldPhoneOm.sTddStandByCounter >= WorldPhoneOm.TDD_STANDBY_TIMER.length) {
                int unused = WorldPhoneOm.sTddStandByCounter = WorldPhoneOm.TDD_STANDBY_TIMER.length - 1;
            }
            if (WorldPhoneOm.sBtSapState == 0) {
                WorldPhoneOm.logd("TDD time out!");
                int unused2 = WorldPhoneOm.sIsWaintInTddTimeOut = 1;
                WorldPhoneOm.this.handleSwitchModem(100);
                return;
            }
            WorldPhoneOm.logd("TDD time out but BT SAP is connected, switch not executed!");
        }
    };
    private BroadcastReceiver mWorldPhoneEccStateReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.worldphone.WorldPhoneOm.AnonymousClass5 */

        public void onReceive(Context context, Intent intent) {
            WorldPhoneOm.logd("mWorldPhoneEccStateReceiver, received " + intent.getAction());
            if (!WorldPhoneOm.this.isEccInProgress()) {
                WorldPhoneOm.this.unRegisterEccStateReceiver();
                WorldPhoneOm.this.handleSimSwitched();
            }
        }
    };
    private final BroadcastReceiver mWorldPhoneReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.worldphone.WorldPhoneOm.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            WorldPhoneOm.logd("Action: " + action);
            if (action.equals("android.telephony.action.SIM_APPLICATION_STATE_CHANGED")) {
                int state = intent.getIntExtra("android.telephony.extra.SIM_STATE", 0);
                int slotId = intent.getIntExtra("slot", 0);
                int unused = WorldPhoneOm.sMajorSim = WorldPhoneUtil.getMajorSim();
                WorldPhoneOm.logd("slotId:" + slotId + " state:" + state + " sMajorSim:" + WorldPhoneOm.sMajorSim + "sSimLockedSlotId:" + WorldPhoneOm.sSimLockedSlotId);
                WorldPhoneOm.this.handleSimApplicationStateChanged(slotId, state);
            } else if (action.equals("android.telephony.action.SIM_CARD_STATE_CHANGED")) {
                int state2 = intent.getIntExtra("android.telephony.extra.SIM_STATE", 0);
                int slotId2 = intent.getIntExtra("slot", 0);
                int unused2 = WorldPhoneOm.sMajorSim = WorldPhoneUtil.getMajorSim();
                WorldPhoneOm.logd("slotId: " + slotId2 + " state: " + state2 + " sMajorSim:" + WorldPhoneOm.sMajorSim);
                WorldPhoneOm.this.handleSimCardStateChanged(slotId2, state2);
            } else if (action.equals(IWorldPhone.ACTION_SHUTDOWN_IPO)) {
                if (WorldPhoneOm.sDefaultBootuUpModem == 100) {
                    if (WorldPhoneUtil.isLteSupport()) {
                        ModemSwitchHandler.reloadModem(WorldPhoneOm.sCi[0], 5);
                        WorldPhoneOm.logd("Reload to FDD CSFB modem");
                        return;
                    }
                    ModemSwitchHandler.reloadModem(WorldPhoneOm.sCi[0], 3);
                    WorldPhoneOm.logd("Reload to WG modem");
                } else if (WorldPhoneOm.sDefaultBootuUpModem == 101) {
                    if (WorldPhoneUtil.isLteSupport()) {
                        ModemSwitchHandler.reloadModem(WorldPhoneOm.sCi[0], 6);
                        WorldPhoneOm.logd("Reload to TDD CSFB modem");
                        return;
                    }
                    ModemSwitchHandler.reloadModem(WorldPhoneOm.sCi[0], 4);
                    WorldPhoneOm.logd("Reload to TG modem");
                }
            } else if (action.equals(IWorldPhone.ACTION_ADB_SWITCH_MODEM)) {
                int toModem = intent.getIntExtra(ModemSwitchHandler.EXTRA_MD_TYPE, 0);
                WorldPhoneOm.logd("toModem: " + toModem);
                if (toModem == 3 || toModem == 4 || toModem == 5 || toModem == 6) {
                    WorldPhoneOm.this.setModemSelectionMode(0, toModem);
                } else {
                    WorldPhoneOm.this.setModemSelectionMode(1, toModem);
                }
            } else if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                if (!intent.getBooleanExtra("state", false)) {
                    WorldPhoneOm.logd("Leave flight mode");
                    String unused3 = WorldPhoneOm.sLastPlmn = null;
                    for (int i = 0; i < WorldPhoneOm.PROJECT_SIM_NUM; i++) {
                        WorldPhoneOm.sIsInvalidSim[i] = false;
                    }
                    return;
                }
                WorldPhoneOm.logd("Enter flight mode");
                for (int i2 = 0; i2 < WorldPhoneOm.PROJECT_SIM_NUM; i2++) {
                    WorldPhoneOm.sFirstSelect[i2] = true;
                }
            } else if (action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE")) {
                int unused4 = WorldPhoneOm.sMajorSim = WorldPhoneUtil.getMajorSim();
                if (WorldPhoneUtil.isSimSwitching()) {
                    WorldPhoneUtil.setSimSwitchingFlag(false);
                    ModemSwitchHandler.setActiveModemType(WorldPhoneUtil.getToModemType());
                }
                WorldPhoneOm.this.handleSimSwitched();
            } else if (action.equals(IWorldPhone.ACTION_TEST_WORLDPHONE)) {
                WorldPhoneOm.this.handleWorldPhoneTest(intent.getIntExtra(IWorldPhone.EXTRA_FAKE_USER_TYPE, 0), intent.getIntExtra(IWorldPhone.EXTRA_FAKE_REGION, 0));
            } else if (action.equals(IWorldPhone.ACTION_SAP_CONNECTION_STATE_CHANGED)) {
                int sapState = intent.getIntExtra("android.bluetooth.profile.extra.STATE", 0);
                if (sapState == 2) {
                    WorldPhoneOm.logd("BT_SAP connection state is CONNECTED");
                    int unused5 = WorldPhoneOm.sBtSapState = 1;
                } else if (sapState == 0) {
                    WorldPhoneOm.logd("BT_SAP connection state is DISCONNECTED");
                    int unused6 = WorldPhoneOm.sBtSapState = 0;
                } else {
                    WorldPhoneOm.logd("BT_SAP connection state is " + sapState);
                }
            }
        }
    };

    static /* synthetic */ int access$1408() {
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

    public WorldPhoneOm() {
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
        ModemSwitchHandler.getActiveModemType();
        IntentFilter intentFilter = new IntentFilter("android.telephony.action.SIM_CARD_STATE_CHANGED");
        intentFilter.addAction("android.telephony.action.SIM_APPLICATION_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction(IWorldPhone.ACTION_SHUTDOWN_IPO);
        intentFilter.addAction(IWorldPhone.ACTION_ADB_SWITCH_MODEM);
        intentFilter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        intentFilter.addAction(IWorldPhone.ACTION_SAP_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(IWorldPhone.ACTION_TEST_WORLDPHONE);
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
        sPlmnType1 = new ArrayList<>();
        sPlmnType1Ext = new ArrayList<>();
        sPlmnType3 = new ArrayList<>();
        sMccDomestic = new ArrayList<>();
        loadDefaultData();
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
    /* access modifiers changed from: public */
    private void handleSimCardStateChanged(int slotId, int state) {
        if (state == 1) {
            sLastPlmn = null;
            sImsi[slotId] = "";
            sFirstSelect[slotId] = true;
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

    private boolean getSimLockState() {
        int policy = MtkTelephonyManagerEx.getDefault().getSimLockPolicy();
        if (policy < 1 || policy > 7) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSimApplicationStateChanged(int slotId, int state) {
        if (true == WorldPhoneUtil.getSimLockedState(state) && sIsAutoSelectEnable && (slotId != sMajorSim || slotId == sSimLockedSlotId)) {
            boolean lockPolicy = getSimLockState();
            logd("handle sim applicatin state changed," + lockPolicy + "," + state);
            if (lockPolicy && state == 4) {
                sSimLocked = 0;
                sSimLockedSlotId = -1;
            } else {
                sSimLocked = 1;
                sSimLockedSlotId = slotId;
            }
        }
        if (!WorldPhoneUtil.getSimLockedState(state) && sIsAutoSelectEnable && ((slotId != sMajorSim || slotId == sSimLockedSlotId) && sSimLocked == 1)) {
            logd("retry to world mode change after not major sim pin unlock");
            sSimLocked = 0;
            sSimLockedSlotId = -1;
            handleSimSwitched();
        }
        if (state == 10) {
            logd("reset sIsInvalidSim by solt:" + slotId);
            sIsInvalidSim[slotId] = false;
            sUiccController = UiccController.getInstance();
            if (sUiccController != null) {
                sIccRecordsInstance[slotId] = sProxyPhones[slotId].getIccCard().getIccRecords();
                IccRecords[] iccRecordsArr = sIccRecordsInstance;
                if (iccRecordsArr[slotId] != null) {
                    sImsi[slotId] = iccRecordsArr[slotId].getIMSI();
                    if (!sIsAutoSelectEnable || slotId != sMajorSim) {
                        logd("Not major SIM or in maual selection mode");
                        getUserType(sImsi[slotId]);
                        boolean[] zArr = sSuspendWaitImsi;
                        if (zArr[slotId]) {
                            zArr[slotId] = false;
                            logd("IMSI for slot" + slotId + ", resuming with ID:" + sSuspendId[slotId]);
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
                        if (i == 1) {
                            int i2 = sRegion;
                            if (i2 == 1) {
                                handleSwitchModem(101);
                            } else if (i2 == 2) {
                                handleSwitchModem(100);
                            } else {
                                logd("Region unknown");
                            }
                        } else if (i == 2 || i == 3) {
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
                        case IWorldPhone.EVENT_REG_SUSPENDED_1 /* 30 */:
                            logd("handleMessage : <EVENT_REG_SUSPENDED_1>");
                            handleRegistrationSuspend(ar, 0);
                            return;
                        case IWorldPhone.EVENT_REG_SUSPENDED_2 /* 31 */:
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
                                case IWorldPhone.EVENT_INVALID_SIM_NOTIFY_2 /* 61 */:
                                    logd("handleMessage : <EVENT_INVALID_SIM_NOTIFY_2>");
                                    handleInvalidSimNotify(1, ar);
                                    return;
                                case IWorldPhone.EVENT_INVALID_SIM_NOTIFY_3 /* 62 */:
                                    logd("handleMessage : <EVENT_INVALID_SIM_NOTIFY_3>");
                                    handleInvalidSimNotify(2, ar);
                                    return;
                                case IWorldPhone.EVENT_INVALID_SIM_NOTIFY_4 /* 63 */:
                                    logd("handleMessage : <EVENT_INVALID_SIM_NOTIFY_4>");
                                    handleInvalidSimNotify(3, ar);
                                    return;
                                default:
                                    switch (i) {
                                        case IWorldPhone.EVENT_RESUME_CAMPING_1 /* 70 */:
                                            if (ar.exception != null) {
                                                logd("handleMessage : <EVENT_RESUME_CAMPING_1> with exception");
                                                sIsResumeCampingFail1 = true;
                                                return;
                                            }
                                            return;
                                        case IWorldPhone.EVENT_RESUME_CAMPING_2 /* 71 */:
                                            if (ar.exception != null) {
                                                logd("handleMessage : <EVENT_RESUME_CAMPING_2> with exception");
                                                sIsResumeCampingFail2 = true;
                                                return;
                                            }
                                            return;
                                        case IWorldPhone.EVENT_RESUME_CAMPING_3 /* 72 */:
                                            if (ar.exception != null) {
                                                logd("handleMessage : <EVENT_RESUME_CAMPING_3> with exception");
                                                sIsResumeCampingFail3 = true;
                                                return;
                                            }
                                            return;
                                        case IWorldPhone.EVENT_RESUME_CAMPING_4 /* 73 */:
                                            if (ar.exception != null) {
                                                logd("handleMessage : <EVENT_RESUME_CAMPING_4> with exception");
                                                sIsResumeCampingFail4 = true;
                                                return;
                                            }
                                            return;
                                        default:
                                            switch (i) {
                                                case IWorldPhone.EVENT_SERVICE_STATE_CHANGED_1 /* 80 */:
                                                    logd("handleMessage : <EVENT_SERVICE_STATE_CHANGED_1>");
                                                    handleServiceStateChange(ar, 0);
                                                    return;
                                                case IWorldPhone.EVENT_SERVICE_STATE_CHANGED_2 /* 81 */:
                                                    logd("handleMessage : <EVENT_SERVICE_STATE_CHANGED_2>");
                                                    handleServiceStateChange(ar, 1);
                                                    return;
                                                case IWorldPhone.EVENT_SERVICE_STATE_CHANGED_3 /* 82 */:
                                                    logd("handleMessage : <EVENT_SERVICE_STATE_CHANGED_3>");
                                                    handleServiceStateChange(ar, 2);
                                                    return;
                                                case IWorldPhone.EVENT_SERVICE_STATE_CHANGED_4 /* 83 */:
                                                    logd("handleMessage : <EVENT_SERVICE_STATE_CHANGED_4>");
                                                    handleServiceStateChange(ar, 3);
                                                    return;
                                                default:
                                                    switch (i) {
                                                        case IWorldPhone.EVENT_WP_GMSS_RAT_CHANGED_1 /* 1060 */:
                                                            logd("handleMessage : <EVENT_WP_GMSS_RAT_CHANGED_1>");
                                                            handleGmssRatChange(ar, 0);
                                                            return;
                                                        case IWorldPhone.EVENT_WP_GMSS_RAT_CHANGED_2 /* 1061 */:
                                                            logd("handleMessage : <EVENT_WP_GMSS_RAT_CHANGED_2>");
                                                            handleGmssRatChange(ar, 1);
                                                            return;
                                                        case IWorldPhone.EVENT_WP_GMSS_RAT_CHANGED_3 /* 1062 */:
                                                            logd("handleMessage : <EVENT_WP_GMSS_RAT_CHANGED_3>");
                                                            handleGmssRatChange(ar, 2);
                                                            return;
                                                        case IWorldPhone.EVENT_WP_GMSS_RAT_CHANGED_4 /* 1063 */:
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
        String currMajorSim = SystemProperties.get("persist.vendor.radio.simswitch", "");
        if (currMajorSim == null || currMajorSim.equals("")) {
            sMajorSim = slotId;
            logd("[getMajorSim]: fail to get major SIM");
        } else {
            try {
                sMajorSim = Integer.parseInt(currMajorSim) - 1;
                logd("[getMajorSim]: " + sMajorSim);
            } catch (NumberFormatException e) {
                Rlog.d(IWorldPhone.LOG_TAG, e.toString());
            } catch (Exception e2) {
                Rlog.d(IWorldPhone.LOG_TAG, e2.toString());
            }
        }
        logd("Slot:" + slotId + " sMajorSim:" + sMajorSim);
        if (ar.exception != null || ar.result == null) {
            logd("AsyncResult is wrong " + ar.exception);
            return;
        }
        String[] plmnString = (String[]) ar.result;
        if (slotId == sMajorSim) {
            sNwPlmnStrings = plmnString;
        }
        for (int i = 0; i < plmnString.length; i++) {
            logd("plmnString[" + i + "]=" + plmnString[i]);
        }
        if (sIsAutoSelectEnable) {
            sRegion = getRegion(plmnString[0]);
            if (sMajorSim == slotId && sUserType == 1 && sDenyReason != 2) {
                searchForDesignateService(plmnString[0]);
            }
            if (!(sUserType == 3 || sRegion != 2 || sMajorSim == -1)) {
                handleSwitchModem(100);
            }
        }
    }

    private static synchronized void initNWPlmnString() {
        synchronized (WorldPhoneOm.class) {
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
            ModemSwitchHandler.getActiveModemType();
            if (slotId != sMajorSim) {
                return;
            }
            if (sIsAutoSelectEnable) {
                if (isNoService() && sCi[slotId].getRadioState() != 2) {
                    handleNoService();
                } else if (isInService()) {
                    sLastPlmn = sPlmnSs;
                    removeModemStandByTimer();
                    logd("reset sIsInvalidSim");
                    sIsInvalidSim[slotId] = false;
                }
            } else if (isInService()) {
                logd("reset sIsInvalidSim in manual mode");
                sLastPlmn = sPlmnSs;
                sIsInvalidSim[slotId] = false;
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
                logd("Not major slot or in maual selection mode, camp on OK");
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
    /* access modifiers changed from: public */
    private boolean handleSwitchModem(int toModem) {
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
        if (sIsAutoSelectEnable && WorldPhoneUtil.isCdmaLteDcSupport() && !isNeedSwitchModem(mMajorSim)) {
            logd("[handleSwitchModem]No need to handle, switch not executed!");
            return false;
        } else if (sSimLocked == 1) {
            logd("sim has been locked!");
            return false;
        } else if (mMajorSim < 0 || !sIsInvalidSim[mMajorSim] || WorldPhoneUtil.getModemSelectionMode() != 1) {
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
                logd("[handleSwitchModem] Auto select disable, storing modem type: " + toModem);
                sCi[0].storeModemType(toModem, null);
            } else {
                int i = sDefaultBootuUpModem;
                if (i == 0) {
                    logd("[handleSwitchModem] Storing modem type: " + toModem);
                    sCi[0].storeModemType(toModem, null);
                } else if (i == 100) {
                    if (WorldPhoneUtil.isLteSupport()) {
                        logd("[handleSwitchModem] Storing modem type: 5");
                        sCi[0].storeModemType(5, null);
                    } else {
                        logd("[handleSwitchModem] Storing modem type: 3");
                        sCi[0].storeModemType(3, null);
                    }
                } else if (i == 101) {
                    if (WorldPhoneUtil.isLteSupport()) {
                        logd("[handleSwitchModem] Storing modem type: 6");
                        sCi[0].storeModemType(6, null);
                    } else {
                        logd("[handleSwitchModem] Storing modem type: 4");
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
            ModemSwitchHandler.switchModem(0, toModem);
            resetNetworkProperties();
            return true;
        } else {
            logd("[handleSwitchModem] Invalid SIM, switch not executed!");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSimSwitched() {
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
            logd("Major capability in slot" + sMajorSim);
            String[] strArr = sImsi;
            int i2 = sMajorSim;
            if (strArr[i2] == null || strArr[i2].equals("")) {
                logd("Major slot IMSI not ready");
                sUserType = 0;
                return;
            }
            sUserType = getUserType(sImsi[sMajorSim]);
            int i3 = sUserType;
            if (i3 == 1) {
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
            } else if (i3 == 2 || i3 == 3) {
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
        if (sProxyPhones[sMajorSim].getIccCard().getState() == IccCardConstants.State.READY) {
            int i = sUserType;
            if (i == 1) {
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
                    } else if (!sWaitInFdd) {
                        sWaitInFdd = true;
                        logd("Wait " + FDD_STANDBY_TIMER[sFddStandByCounter] + "s. Timer index = " + sFddStandByCounter);
                        postDelayed(this.mFddStandByTimerRunnable, (long) (FDD_STANDBY_TIMER[sFddStandByCounter] * 1000));
                    } else {
                        logd("Timer already set:" + FDD_STANDBY_TIMER[sFddStandByCounter] + "s");
                    }
                }
            } else if (i != 2 && i != 3) {
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
        logd("[isAllowCampOn] " + plmnString + "User type: " + sUserType);
        sRegion = getRegion(plmnString);
        if (WorldPhoneUtil.isSimSwitching()) {
            mdType = WorldPhoneUtil.getToModemType();
            logd("SimSwitching mdType:" + ModemSwitchHandler.modemToString(mdType));
        } else {
            mdType = ModemSwitchHandler.getActiveModemType();
        }
        int i = sUserType;
        if (i == 1) {
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
        } else if (i != 2 && i != 3) {
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
        String currentMcc = plmn.substring(0, 3);
        Iterator<String> it = sMccDomestic.iterator();
        while (it.hasNext()) {
            if (currentMcc.equals(it.next())) {
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
        Iterator<String> it = sPlmnType1.iterator();
        while (it.hasNext()) {
            if (imsi2.equals(it.next())) {
                logd("[getUserType] Type1 user");
                return 1;
            }
        }
        Iterator<String> it2 = sPlmnType1Ext.iterator();
        while (it2.hasNext()) {
            if (imsi2.equals(it2.next())) {
                logd("[getUserType] Extended Type1 user");
                return 1;
            }
        }
        Iterator<String> it3 = sPlmnType3.iterator();
        while (it3.hasNext()) {
            if (imsi2.equals(it3.next())) {
                logd("[getUserType] Type3 user");
                return 3;
            }
        }
        logd("[getUserType] Type2 user");
        return 2;
    }

    private void resumeCampingProcedure(int slotId, boolean isResumeModem) {
        logd("Resume camping slot:" + slotId + " isResumeModem:" + isResumeModem + ", sSimLocked:" + sSimLocked);
        boolean switchModem = false;
        String[] strArr = sNwPlmnStrings;
        if (strArr == null || strArr[0] == null) {
            logd("sNwPlmnStrings[0] is null");
            return;
        }
        String plmnString = strArr[0];
        if (sSimLocked == 1 || isAllowCampOn(plmnString, slotId)) {
            switchModem = false;
        } else {
            logd("Because: " + sDenyReason);
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
        sIsResumeCampingFail1 = false;
        sIsResumeCampingFail2 = false;
        sIsResumeCampingFail3 = false;
        sIsResumeCampingFail4 = false;
        sBtSapState = 0;
        resetSimProperties();
        resetNetworkProperties();
        sSimLocked = 0;
        sSimLockedSlotId = -1;
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
        if (strPlmn == null || strPlmn.length() < 5) {
            logd("[searchForDesignateService]- null source");
            return;
        }
        String strPlmn2 = strPlmn.substring(0, 5);
        Iterator<String> it = sPlmnType1.iterator();
        while (it.hasNext()) {
            if (strPlmn2.equals(it.next())) {
                logd("Find TD service");
                logd("sUserType: " + sUserType + " sRegion: " + sRegion);
                if (sRegion == 1) {
                    ModemSwitchHandler.getActiveModemType();
                    handleSwitchModem(101);
                    return;
                }
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
        handleSwitchModem(modemType);
        if (modemType == ModemSwitchHandler.getActiveModemType()) {
            removeModemStandByTimer();
        }
    }

    @Override // com.mediatek.internal.telephony.worldphone.IWorldPhone
    public void notifyRadioCapabilityChange(int capailitySimId) {
        int toModem;
        int majorSimId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        int activeSvlteModeSlotId = WorldPhoneUtil.getActiveSvlteModeSlotId();
        logd("[setRadioCapabilityChange] majorSimId:" + majorSimId + " capailitySimId=" + capailitySimId);
        removeEmsrResumeByTimer();
        if (!sIsAutoSelectEnable) {
            logd("[setRadioCapabilityChange] Auto modem selection disabled");
            removeModemStandByTimer();
            return;
        }
        String[] strArr = sImsi;
        if (strArr[capailitySimId] == null || strArr[capailitySimId].equals("")) {
            logd("Capaility slot IMSI not ready");
            sUserType = 0;
            return;
        }
        sUserType = getUserType(sImsi[capailitySimId]);
        int i = sUserType;
        if (i == 1) {
            String[] strArr2 = sNwPlmnStrings;
            if (strArr2 != null) {
                sRegion = getRegion(strArr2[0]);
            }
            int i2 = sRegion;
            if (i2 == 1) {
                sFirstSelect[capailitySimId] = false;
                toModem = 101;
            } else if (i2 == 2) {
                sFirstSelect[capailitySimId] = false;
                toModem = 100;
            } else {
                logd("Unknown region");
                return;
            }
        } else if (i == 2 || i == 3) {
            sFirstSelect[capailitySimId] = false;
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
        MtkRIL ci = null;
        if (majorSimId != -99) {
            if (!WorldPhoneUtil.isCdmaLteDcSupport()) {
                ci = sCi[0];
            } else if (isSpecialCardMode()) {
                logd("isSpecialCardMode=true, ignore this change!");
            } else if (capailitySimId != activeSvlteModeSlotId) {
                logd("new RT mode is CSFB");
                ci = sCi[majorSimId];
            } else if (toModem == 5) {
                logd("new RT mode is SVLTE and new type is LWG");
                ci = sCi[majorSimId];
            }
            if (ci != null) {
                ci.reloadModemType(toModem, null);
                resetNetworkProperties();
                WorldPhoneUtil.setSimSwitchingFlag(true);
                WorldPhoneUtil.saveToModemType(toModem);
                ci.storeModemType(toModem, null);
                return;
            }
            return;
        }
        logd("notifyRadioCapabilityChange: major sim is unknown");
    }

    public void handleRadioTechModeSwitch() {
        int toModem;
        logd("[handleRadioTechModeSwitch]");
        if (!sIsAutoSelectEnable) {
            logd("Auto modem selection disabled");
            removeModemStandByTimer();
            return;
        }
        logd("Auto modem selection enabled");
        String[] strArr = sImsi;
        int i = sMajorSim;
        if (strArr[i] == null || strArr[i].equals("")) {
            logd("Capaility slot IMSI not ready");
            sUserType = 0;
            return;
        }
        sUserType = getUserType(sImsi[sMajorSim]);
        int i2 = sUserType;
        if (i2 == 1) {
            String[] strArr2 = sNwPlmnStrings;
            if (strArr2 != null) {
                sRegion = getRegion(strArr2[0]);
            }
            int i3 = sRegion;
            if (i3 == 1) {
                sFirstSelect[sMajorSim] = false;
                toModem = 101;
            } else if (i3 == 2) {
                sFirstSelect[sMajorSim] = false;
                toModem = 100;
            } else {
                logd("Unknown region");
                return;
            }
        } else if (i2 == 2 || i2 == 3) {
            sFirstSelect[sMajorSim] = false;
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
        logd("[handleRadioTechModeSwitch]: switch type: " + toModem);
        handleSwitchModem(toModem);
        resetNetworkProperties();
    }

    private boolean isNeedSwitchModem(int majorSimId) {
        boolean isNeed = true;
        if (WorldPhoneUtil.isC2kSupport()) {
            int activeSvlteModeSlotId = WorldPhoneUtil.getActiveSvlteModeSlotId();
            if (sUserType == 2 && majorSimId >= 0 && majorSimId == activeSvlteModeSlotId && ModemSwitchHandler.getActiveModemType() == 5) {
                isNeed = false;
            }
        }
        logd("[isNeedSwitchModem] isNeed = " + isNeed);
        return isNeed;
    }

    private boolean isSpecialCardMode() {
        boolean specialCardMode = false;
        int[] cardType = WorldPhoneUtil.getC2KWPCardType();
        if ((is4GCdmaCard(cardType[0]) && is4GCdmaCard(cardType[1])) || (is3GCdmaCard(cardType[0]) && is3GCdmaCard(cardType[1]))) {
            logd("isSpecialCardMode cardType1=" + cardType[0] + ", cardType2=" + cardType[1]);
            specialCardMode = true;
        }
        logd("isSpecialCardMode:" + specialCardMode);
        return specialCardMode;
    }

    private boolean is4GCdmaCard(int cardType) {
        if ((cardType & 2) <= 0 || !containsCdma(cardType)) {
            return false;
        }
        return true;
    }

    private boolean is3GCdmaCard(int cardType) {
        if ((cardType & 2) == 0 && (cardType & 1) == 0 && containsCdma(cardType)) {
            return true;
        }
        return false;
    }

    private boolean containsCdma(int cardType) {
        if ((cardType & 4) > 0 || (cardType & 8) > 0) {
            return true;
        }
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
    /* access modifiers changed from: public */
    private void unRegisterEccStateReceiver() {
        Context context = sContext;
        if (context == null) {
            logd("unRegisterEccStateReceiver, context is null => return");
            return;
        }
        context.unregisterReceiver(this.mWorldPhoneEccStateReceiver);
        this.mIsRegisterEccStateReceiver = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isEccInProgress() {
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

    private static void loadDefaultData() {
        for (String plmn : PLMN_TABLE_TYPE1) {
            sPlmnType1.add(plmn);
        }
        for (String plmn2 : PLMN_TABLE_TYPE1_EXT) {
            sPlmnType1Ext.add(plmn2);
        }
        for (String plmn3 : PLMN_TABLE_TYPE3) {
            sPlmnType3.add(plmn3);
        }
        for (String mcc : MCC_TABLE_DOMESTIC) {
            sMccDomestic.add(mcc);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWorldPhoneTest(int fakeUserType, int fakeRegion) {
        boolean hasChanged = false;
        if (fakeUserType == 0 && fakeRegion == 0) {
            logd("Leave ADB Test mode");
            sPlmnType1.clear();
            sPlmnType1Ext.clear();
            sPlmnType3.clear();
            sMccDomestic.clear();
            loadDefaultData();
            return;
        }
        sMajorSim = WorldPhoneUtil.getMajorSim();
        int i = sMajorSim;
        if (i == -99 || i == -1) {
            logd("sMajorSim is Unknown or Capability OFF");
        } else {
            String imsi = sImsi[i];
            if (imsi == null || imsi.equals("")) {
                logd("Imsi of sMajorSim is unknown");
            } else {
                String imsi2 = imsi.substring(0, 5);
                if (fakeUserType == 1) {
                    sPlmnType1.add(imsi2);
                    hasChanged = true;
                } else if (fakeUserType != 3) {
                    logd("Unknown fakeUserType:" + fakeUserType);
                } else {
                    sPlmnType3.add(imsi2);
                    hasChanged = true;
                }
            }
            String currentMcc = sNwPlmnStrings[0];
            if (currentMcc == null || currentMcc.equals("") || currentMcc.length() < 5) {
                logd("Invalid sNwPlmnStrings");
            } else {
                String currentMcc2 = currentMcc.substring(0, 3);
                if (fakeRegion == 1) {
                    sMccDomestic.add(currentMcc2);
                    hasChanged = true;
                } else if (fakeRegion == 2) {
                    sMccDomestic.remove(currentMcc2);
                    hasChanged = true;
                } else {
                    logd("Unknown fakeRegion:" + fakeRegion);
                }
            }
        }
        if (hasChanged) {
            logd("sPlmnType1:" + sPlmnType1);
            logd("sPlmnType1Ext:" + sPlmnType1Ext);
            logd("sPlmnType3:" + sPlmnType3);
            logd("sMccDomestic:" + sMccDomestic);
            handleRadioTechModeSwitch();
        }
    }

    /* access modifiers changed from: private */
    public static void logd(String msg) {
        Rlog.d(IWorldPhone.LOG_TAG, "[WPOM]" + msg);
    }
}
