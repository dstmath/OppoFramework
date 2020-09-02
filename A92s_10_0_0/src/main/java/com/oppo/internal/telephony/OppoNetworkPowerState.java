package com.oppo.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.utils.ConnectivityManagerHelper;
import com.oppo.internal.telephony.utils.OppoManagerHelper;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class OppoNetworkPowerState {
    private static final boolean DBG = false;
    private static final int EVENT_OEM_DEEP_SLEEP_MODE_CHANGED = 102;
    private static final int EVENT_OEM_DEVICE_IDLE_MODE_CHANGED = 100;
    private static final int EVENT_OEM_GET_SEARCH_COUNT_DONE = 1001;
    private static final int EVENT_OEM_LIGHT_DEVICE_IDLE_MODE_CHANGED = 101;
    private static final int EVENT_OEM_SET_DEVICE_IDLE_MODE = 30;
    private static final int IGNORE_FREQUENT_SCREENONOFF_TIME_S = 5;
    private static final String INTENT_NW_POWER_BOOT_MONITOR = "oppo.intent.action.INTENT_NW_POWER_BOOT_MONITOR";
    private static final String INTENT_NW_POWER_STOP_MONITOR = "oppo.intent.action.INTENT_NW_POWER_STOP_MONITOR";
    private static final String INTENT_NW_POWER_STOP_MONITOR_UNSL = "oppo.intent.action.INTENT_NW_POWER_STOP_MONITOR_UNSL";
    private static final String INTENT_NW_POWER_UNSL_MONITOR = "oppo.intent.action.INTENT_NW_POWER_UNSL_MONITOR";
    private static final String LOG_TAG = "OppoNetworkPowerState";
    private static final int MSG_PERFORM_SCREENOFF = 2;
    private static final int MSG_PERFORM_SCREENON = 1;
    protected static final int OOS_DOZE_CFG_PARA_LENGTH = 2;
    private static final long SAMPLE_TIME = 3540;
    private static final long SAMPLE_TIME_UPLOAD = 14400;
    private static final Object lockCellCount = new Object();
    private static final Object lockDataCallCount = new Object();
    /* access modifiers changed from: private */
    public static final Object lockDcsMsg = new Object();
    private static final Object lockNitzCount = new Object();
    private static final Object lockNoService = new Object();
    private static final Object lockRilCount = new Object();
    private static final Object lockSmsSendCount = new Object();
    private static final int[] mIgnoreKey = {1009, 19, 135, 1028};
    private static final double mPerDataCallPowerLost = 0.6d;
    private static final double mPerNITZLost = 0.05d;
    private static final double mPerNoServicePowerLost = 0.017d;
    private static final double mPerSMSSendLost = 0.3d;
    private static boolean[] sAlreadyPerformScreenState = {false, false};
    private static boolean[] sAlreadyUpdated = {false, false};
    /* access modifiers changed from: private */
    public static boolean[] sAlreadyUpdatedDsc = {false, false};
    private static long[] sBatteryPercent = {0, 0};
    private static long[] sBeginNoServiceTime = {0, 0};
    private static int[] sCdmaRegistedFailedAndTotalCount = {0, 0};
    private static String sCellInfoTopMsp = "Null";
    private static int sDataCallCount = 0;
    private static String sDcsMsg = "";
    private static HashMap<Long, Integer> sGetCellCount = new HashMap<>();
    private static HashMap<Integer, String> sGetCellUidPackage = new HashMap<>();
    private static AtomicBoolean sIsScreenOn = new AtomicBoolean(false);
    private static int sNitzCount = 0;
    private static long sNoServiceTime = 0;
    private static long[] sNoServiceTimeSubs = {0, 0};
    private static int[] sNwSearchCdmaCount = {0, 0};
    private static int[] sNwSearchCount = {0, 0};
    /* access modifiers changed from: private */
    public static long[] sQmiIpaData = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    /* access modifiers changed from: private */
    public static AtomicBoolean sQmiIpaReady = new AtomicBoolean(false);
    private static HashMap<Integer, Integer> sRilCount = new HashMap<>();
    private static String sRiljTopMsp = "Null";
    private static int[] sRrcCount = {0, 0};
    private static long[] sRrcDuration = {0, 0};
    /* access modifiers changed from: private */
    public static long sScreenOffDuration = 0;
    private static long sScreenOffTime = 0;
    private static long sScreenOnTime = 0;
    private static int sSmsSendCount = 0;
    private final String DEEP_SLEEP_URI = "oppoguaedelf_deep_sleep_status";
    private final Object lockVoiceRegState = new Object();
    private BroadcastReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public boolean mDeepSleepStatus = false;
    private final Uri mDeepSleepUri = Settings.System.getUriFor("oppoguaedelf_deep_sleep_status");
    /* access modifiers changed from: private */
    public boolean mDeviceIdle = false;
    private boolean mDeviceInactiveState = false;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private boolean mIsIgnore = false;
    /* access modifiers changed from: private */
    public boolean mLightDeviceIdle = false;
    private ContentObserver mOosDozeParaObserver = new ContentObserver(new Handler()) {
        /* class com.oppo.internal.telephony.OppoNetworkPowerState.AnonymousClass4 */

        public void onChange(boolean selfChange) {
            OppoNetworkPowerState.this.updateOosDozeParaFromSettings(false);
        }
    };
    private boolean mOosLpmEnableInDeviceIdle = true;
    private boolean mOosLpmEnableInLightDeviceIdle = false;
    private OppoRIL mOppoRIL;
    /* access modifiers changed from: private */
    public GsmCdmaPhone mPhone;
    private ContentResolver mResolver;
    private ServiceStateTracker mSST;
    /* access modifiers changed from: private */
    public AtomicBoolean mSearchCountReady = new AtomicBoolean(true);
    private SettingObserver mSettingObserver;
    private int mVoiceRegState = 0;

    public OppoNetworkPowerState(Context context, GsmCdmaPhone phone, ServiceStateTracker sst, OppoRIL oppoRIL) {
        this.mContext = context;
        this.mPhone = phone;
        this.mSST = sst;
        this.mOppoRIL = oppoRIL;
        this.mHandler = new Handler() {
            /* class com.oppo.internal.telephony.OppoNetworkPowerState.AnonymousClass1 */

            public void handleMessage(Message msg) {
                int i = msg.what;
                boolean z = true;
                if (i == 1) {
                    OppoNetworkPowerState.this.performScreenOn();
                } else if (i == 2) {
                    OppoNetworkPowerState.this.performScreenOff();
                } else if (i != OppoNetworkPowerState.EVENT_OEM_GET_SEARCH_COUNT_DONE) {
                    switch (i) {
                        case OppoNetworkPowerState.EVENT_OEM_DEVICE_IDLE_MODE_CHANGED /*{ENCODED_INT: 100}*/:
                            OppoNetworkPowerState oppoNetworkPowerState = OppoNetworkPowerState.this;
                            if (msg.arg1 == 0) {
                                z = false;
                            }
                            boolean unused = oppoNetworkPowerState.mDeviceIdle = z;
                            OppoNetworkPowerState.this.updateDeviceInactiveStateForOosLpm();
                            return;
                        case 101:
                            OppoNetworkPowerState oppoNetworkPowerState2 = OppoNetworkPowerState.this;
                            if (msg.arg1 == 0) {
                                z = false;
                            }
                            boolean unused2 = oppoNetworkPowerState2.mLightDeviceIdle = z;
                            OppoNetworkPowerState.this.updateDeviceInactiveStateForOosLpm();
                            return;
                        case 102:
                            OppoNetworkPowerState oppoNetworkPowerState3 = OppoNetworkPowerState.this;
                            if (msg.arg1 == 0) {
                                z = false;
                            }
                            boolean unused3 = oppoNetworkPowerState3.mDeepSleepStatus = z;
                            OppoNetworkPowerState.this.updateDeviceInactiveStateForOosLpm();
                            return;
                        default:
                            OppoNetworkPowerState.this.loge("HandleMessage Unknow message: " + msg.what);
                            return;
                    }
                } else {
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        OppoNetworkPowerState.this.searchCountDone((int[]) ar.result);
                    }
                    OppoNetworkPowerState.this.mSearchCountReady.set(true);
                }
            }
        };
        initBroadcastRecriver();
        registerDeepsleepObserver();
        registerOosDozeParaSettings();
    }

    private void initBroadcastRecriver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_NW_POWER_UNSL_MONITOR);
        filter.addAction("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED");
        filter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.oppo.internal.telephony.OppoNetworkPowerState.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                long[] data;
                String str;
                String action = intent.getAction();
                OppoNetworkPowerState oppoNetworkPowerState = OppoNetworkPowerState.this;
                oppoNetworkPowerState.log("Received broadcast:" + action);
                String str2 = "on";
                if (action.equals("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED")) {
                    Message msg = OppoNetworkPowerState.this.mHandler.obtainMessage(101);
                    msg.arg1 = OppoNetworkPowerState.this.isLightDeviceIdleModeOn() ? 1 : 0;
                    OppoNetworkPowerState oppoNetworkPowerState2 = OppoNetworkPowerState.this;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Light Device Idle mode ");
                    if (msg.arg1 == 1) {
                        str = str2;
                    } else {
                        str = "off";
                    }
                    sb.append(str);
                    oppoNetworkPowerState2.log(sb.toString());
                    OppoNetworkPowerState.this.mHandler.sendMessage(msg);
                }
                if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                    Message msg2 = OppoNetworkPowerState.this.mHandler.obtainMessage(OppoNetworkPowerState.EVENT_OEM_DEVICE_IDLE_MODE_CHANGED);
                    msg2.arg1 = OppoNetworkPowerState.this.isDeviceIdleModeOn() ? 1 : 0;
                    OppoNetworkPowerState oppoNetworkPowerState3 = OppoNetworkPowerState.this;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Device Idle mode ");
                    if (msg2.arg1 != 1) {
                        str2 = "off";
                    }
                    sb2.append(str2);
                    oppoNetworkPowerState3.log(sb2.toString());
                    OppoNetworkPowerState.this.mHandler.sendMessage(msg2);
                }
                if (action.equals(OppoNetworkPowerState.INTENT_NW_POWER_UNSL_MONITOR)) {
                    try {
                        if (!OppoNetworkPowerState.sQmiIpaReady.get() && (data = intent.getLongArrayExtra("unsl")) != null && data.length == 11) {
                            for (int i = 0; i < 11; i++) {
                                OppoNetworkPowerState.sQmiIpaData[i] = data[i];
                            }
                            OppoNetworkPowerState.sQmiIpaReady.set(true);
                            OppoNetworkPowerState.this.log("QmiIpaReady.");
                        }
                    } catch (Exception e) {
                        OppoNetworkPowerState oppoNetworkPowerState4 = OppoNetworkPowerState.this;
                        oppoNetworkPowerState4.loge("Receive INTENT_NW_POWER_UNSL_MONITOR error:" + e.toString());
                    }
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0025, code lost:
        if (r0[1] != false) goto L_0x0027;
     */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x005d  */
    public void performScreenOn() {
        sScreenOffDuration = sScreenOnTime - sScreenOffTime;
        sAlreadyPerformScreenState[this.mPhone.getPhoneId()] = true;
        boolean[] zArr = sAlreadyPerformScreenState;
        if (!zArr[0] || zArr[1]) {
            boolean[] zArr2 = sAlreadyPerformScreenState;
            if (!zArr2[0]) {
            }
            if (sScreenOffDuration > SAMPLE_TIME) {
                settleScreenOn();
            }
            new Thread(new Runnable() {
                /* class com.oppo.internal.telephony.OppoNetworkPowerState.AnonymousClass3 */

                public void run() {
                    boolean isActivePhone = OppoUiccManagerImpl.getInstance().getSubState(OppoNetworkPowerState.this.mPhone.getSubId()) == 1;
                    int phoneId = OppoNetworkPowerState.this.mPhone.getPhoneId();
                    int i = 0;
                    while (true) {
                        if (i >= 4) {
                            break;
                        } else if ((!OppoNetworkPowerState.this.mSearchCountReady.get() || !OppoNetworkPowerState.sQmiIpaReady.get()) && i != 3) {
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                                OppoNetworkPowerState oppoNetworkPowerState = OppoNetworkPowerState.this;
                                oppoNetworkPowerState.loge("Thread error:" + e.toString());
                            }
                            i++;
                        }
                    }
                    if (i == 3 && !OppoNetworkPowerState.sQmiIpaReady.get()) {
                        OppoNetworkPowerState.this.log("PerformScreenOn timeout and force start.");
                    }
                    OppoNetworkPowerState.sAlreadyUpdatedDsc[phoneId] = true;
                    synchronized (OppoNetworkPowerState.lockDcsMsg) {
                        if (isActivePhone) {
                            OppoNetworkPowerState.this.prepareDcsNormalMsg();
                        }
                        if (OppoNetworkPowerState.sAlreadyUpdatedDsc[0] && OppoNetworkPowerState.sAlreadyUpdatedDsc[1]) {
                            OppoNetworkPowerState.this.prepareDcsRilMsg();
                            OppoNetworkPowerState.this.prepareDcsQmiMsg();
                            OppoNetworkPowerState.this.prepareDcsIpaMsg();
                            OppoNetworkPowerState.this.prepareDcsBatteryMsg();
                            if (OppoNetworkPowerState.sScreenOffDuration > OppoNetworkPowerState.SAMPLE_TIME_UPLOAD) {
                                OppoNetworkPowerState.this.log("Upload to DCS.");
                                OppoNetworkPowerState.this.notifyOppoManager();
                            }
                        }
                    }
                    OppoNetworkPowerState.this.printScreenOnLog(isActivePhone, phoneId);
                    OppoNetworkPowerState.this.mSearchCountReady.set(true);
                }
            }).start();
        }
        if (sScreenOffDuration > SAMPLE_TIME_UPLOAD) {
            this.mContext.sendBroadcastAsUser(new Intent(INTENT_NW_POWER_STOP_MONITOR_UNSL), UserHandle.ALL);
        } else {
            this.mContext.sendBroadcastAsUser(new Intent(INTENT_NW_POWER_STOP_MONITOR), UserHandle.ALL);
        }
        sBatteryPercent[1] = (long) getBattery();
        if (sScreenOffDuration > SAMPLE_TIME) {
        }
        new Thread(new Runnable() {
            /* class com.oppo.internal.telephony.OppoNetworkPowerState.AnonymousClass3 */

            public void run() {
                boolean isActivePhone = OppoUiccManagerImpl.getInstance().getSubState(OppoNetworkPowerState.this.mPhone.getSubId()) == 1;
                int phoneId = OppoNetworkPowerState.this.mPhone.getPhoneId();
                int i = 0;
                while (true) {
                    if (i >= 4) {
                        break;
                    } else if ((!OppoNetworkPowerState.this.mSearchCountReady.get() || !OppoNetworkPowerState.sQmiIpaReady.get()) && i != 3) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            OppoNetworkPowerState oppoNetworkPowerState = OppoNetworkPowerState.this;
                            oppoNetworkPowerState.loge("Thread error:" + e.toString());
                        }
                        i++;
                    }
                }
                if (i == 3 && !OppoNetworkPowerState.sQmiIpaReady.get()) {
                    OppoNetworkPowerState.this.log("PerformScreenOn timeout and force start.");
                }
                OppoNetworkPowerState.sAlreadyUpdatedDsc[phoneId] = true;
                synchronized (OppoNetworkPowerState.lockDcsMsg) {
                    if (isActivePhone) {
                        OppoNetworkPowerState.this.prepareDcsNormalMsg();
                    }
                    if (OppoNetworkPowerState.sAlreadyUpdatedDsc[0] && OppoNetworkPowerState.sAlreadyUpdatedDsc[1]) {
                        OppoNetworkPowerState.this.prepareDcsRilMsg();
                        OppoNetworkPowerState.this.prepareDcsQmiMsg();
                        OppoNetworkPowerState.this.prepareDcsIpaMsg();
                        OppoNetworkPowerState.this.prepareDcsBatteryMsg();
                        if (OppoNetworkPowerState.sScreenOffDuration > OppoNetworkPowerState.SAMPLE_TIME_UPLOAD) {
                            OppoNetworkPowerState.this.log("Upload to DCS.");
                            OppoNetworkPowerState.this.notifyOppoManager();
                        }
                    }
                }
                OppoNetworkPowerState.this.printScreenOnLog(isActivePhone, phoneId);
                OppoNetworkPowerState.this.mSearchCountReady.set(true);
            }
        }).start();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0027, code lost:
        if (r0[1] == false) goto L_0x0029;
     */
    public void performScreenOff() {
        if (!this.mIsIgnore) {
            log("-------------------------SCRENN OFF----------------------------------");
            sAlreadyPerformScreenState[this.mPhone.getPhoneId()] = false;
            boolean[] zArr = sAlreadyPerformScreenState;
            if (zArr[0] || !zArr[1]) {
                boolean[] zArr2 = sAlreadyPerformScreenState;
                if (zArr2[0]) {
                }
                resetStatisticalVariable();
                notifyPowerMonitorScreenOff();
            }
            this.mContext.sendBroadcastAsUser(new Intent(INTENT_NW_POWER_BOOT_MONITOR), UserHandle.ALL);
            sBatteryPercent[0] = (long) getBattery();
            resetStatisticalVariable();
            notifyPowerMonitorScreenOff();
        }
    }

    /* access modifiers changed from: private */
    public void printScreenOnLog(boolean isActivePhone, int phoneId) {
        long tempNoServiceTime;
        long tempNoServiceTimeSubs;
        synchronized (lockNoService) {
            tempNoServiceTime = sNoServiceTime;
            tempNoServiceTimeSubs = sNoServiceTimeSubs[phoneId];
        }
        log("-------------------------SCRENN ON----------------------------------");
        log("IS_ACTIVE_PHONE = " + isActivePhone + ", DATA_CALL_COUNT = " + sDataCallCount + ", NO_SERVICE_TIME = " + tempNoServiceTime + ", SUB_NO_SERVICE_TIME = " + tempNoServiceTimeSubs + ", SMS_SEND_COUNT = " + sSmsSendCount + ", NITZ_COUNT = " + sNitzCount);
        StringBuilder sb = new StringBuilder();
        sb.append("RILJ_TOP = ");
        sb.append(sRiljTopMsp);
        log(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("CallCell_TOP = ");
        sb2.append(sCellInfoTopMsp);
        log(sb2.toString());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0014, code lost:
        if (r0[0] == false) goto L_0x0016;
     */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0034  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0038 A[SYNTHETIC] */
    private void settleScreenOn() {
        boolean[] zArr = sAlreadyUpdated;
        if (!zArr[0] || zArr[1]) {
            boolean[] zArr2 = sAlreadyUpdated;
            if (zArr2[1]) {
            }
            boolean isActivePhone = OppoUiccManagerImpl.getInstance().getSubState(this.mPhone.getSubId()) != 1;
            synchronized (lockNoService) {
                if (this.mPhone.getPhoneId() < 2 && isActivePhone && sBeginNoServiceTime[this.mPhone.getPhoneId()] != 0) {
                    if (sBeginNoServiceTime[this.mPhone.getPhoneId()] < sScreenOffTime) {
                        sBeginNoServiceTime[this.mPhone.getPhoneId()] = sScreenOffTime;
                    }
                    sNoServiceTime += sScreenOnTime - sBeginNoServiceTime[this.mPhone.getPhoneId()];
                    long[] jArr = sNoServiceTimeSubs;
                    int phoneId = this.mPhone.getPhoneId();
                    jArr[phoneId] = jArr[phoneId] + (sScreenOnTime - sBeginNoServiceTime[this.mPhone.getPhoneId()]);
                }
            }
            sAlreadyUpdated[this.mPhone.getPhoneId()] = true;
            boolean[] zArr3 = sAlreadyUpdated;
            if (zArr3[0] && zArr3[1]) {
                notifyPowerMonitorScreenOn();
                return;
            }
            return;
        }
        sRiljTopMsp = getGetRilTopMsg();
        sCellInfoTopMsp = getTopCellInfo();
        if (OppoUiccManagerImpl.getInstance().getSubState(this.mPhone.getSubId()) != 1) {
        }
        synchronized (lockNoService) {
        }
    }

    private void resetStatisticalVariable() {
        sAlreadyUpdated[this.mPhone.getPhoneId()] = false;
        sAlreadyUpdatedDsc[this.mPhone.getPhoneId()] = false;
        sQmiIpaReady.set(false);
        int[] iArr = sCdmaRegistedFailedAndTotalCount;
        iArr[0] = 0;
        iArr[1] = 0;
        synchronized (lockNoService) {
            sNoServiceTimeSubs[this.mPhone.getPhoneId()] = 0;
            sNoServiceTime = 0;
        }
        synchronized (lockDataCallCount) {
            sDataCallCount = 0;
        }
        synchronized (lockSmsSendCount) {
            sSmsSendCount = 0;
        }
        synchronized (lockNitzCount) {
            sNitzCount = 0;
        }
        synchronized (lockRilCount) {
            sRilCount.clear();
        }
        synchronized (lockCellCount) {
            sGetCellCount.clear();
            sGetCellUidPackage.clear();
        }
    }

    /* access modifiers changed from: private */
    public void searchCountDone(int[] result) {
        int phoneId = this.mPhone.getPhoneId();
        try {
            if (result[0] < sNwSearchCount[phoneId]) {
                sNwSearchCount[phoneId] = (result[0] - sNwSearchCount[phoneId]) + 65535;
            } else {
                sNwSearchCount[phoneId] = result[0] - sNwSearchCount[phoneId];
            }
            if (result[1] < sNwSearchCdmaCount[phoneId]) {
                sNwSearchCdmaCount[phoneId] = (result[1] - sNwSearchCdmaCount[phoneId]) + 65535;
            } else {
                sNwSearchCdmaCount[phoneId] = result[1] - sNwSearchCdmaCount[phoneId];
            }
            if (result[2] < sRrcCount[phoneId]) {
                sRrcCount[phoneId] = (result[2] - sRrcCount[phoneId]) + 65535;
            } else {
                sRrcCount[phoneId] = result[2] - sRrcCount[phoneId];
            }
            if (((long) result[3]) + (((long) result[4]) << 16) < sRrcDuration[phoneId]) {
                sRrcDuration[phoneId] = ((((long) result[3]) + (((long) result[4]) << 16)) - sRrcDuration[phoneId]) - 1;
            } else {
                sRrcDuration[phoneId] = (((long) result[3]) + (((long) result[4]) << 16)) - sRrcDuration[phoneId];
            }
        } catch (Exception e) {
            loge("SearchCountDone error:" + e.toString());
        }
    }

    private int getCombRegState(ServiceState ss) {
        int regState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        return ((regState == 1 || regState == 3) && dataRegState == 0) ? dataRegState : regState;
    }

    private String getCardType() {
        int cardType = OppoUiccManagerImpl.getInstance().getCardType(this.mSST.getImsi(), this.mPhone.getIccSerialNumber());
        if (cardType == -1) {
            return "UNKNOWN";
        }
        if (cardType == 9) {
            return "TEST";
        }
        if (cardType == 1) {
            return "CT";
        }
        if (cardType == 2) {
            return "CM";
        }
        if (cardType == 3) {
            return "CU";
        }
        if (cardType != 4) {
            return "UNKNOWN";
        }
        return "OTHER";
    }

    private String getIsRoaming() {
        boolean isRoaming = this.mSST.mSS == null ? false : this.mSST.mSS.getRoaming();
        if (1 == 0 || isRoaming) {
            return "0";
        }
        return "1";
    }

    private String getGetRilTopMsg() {
        HashMap<Integer, Integer> rilCount;
        int[] top3Value = {0, 0, 0};
        int[] top3Key = {0, 0, 0};
        synchronized (lockRilCount) {
            rilCount = (HashMap) sRilCount.clone();
        }
        for (Map.Entry<Integer, Integer> entry : rilCount.entrySet()) {
            int key = entry.getKey().intValue();
            int value = entry.getValue().intValue();
            int tempKey = 0;
            if (value > 0) {
                int i = 0;
                while (true) {
                    int[] iArr = mIgnoreKey;
                    if (i >= iArr.length) {
                        break;
                    } else if (key == iArr[i]) {
                        tempKey = -1;
                        break;
                    } else {
                        i++;
                    }
                }
                if (tempKey >= 0) {
                    for (int i2 = 0; i2 < top3Value.length; i2++) {
                        if (value > top3Value[i2]) {
                            int tempValue = top3Value[i2];
                            int tempKey2 = top3Key[i2];
                            top3Value[i2] = value;
                            top3Key[i2] = key;
                            value = tempValue;
                            key = tempKey2;
                        }
                    }
                }
            }
        }
        for (int i3 = 0; i3 < top3Value.length; i3++) {
            if (top3Value[i3] != 0) {
                log("RILJ_TOP[" + i3 + "]" + invokeResponseToString(top3Key[i3]) + ":" + top3Value[i3]);
            }
        }
        if (top3Value[0] <= 0) {
            return "Null";
        }
        return invokeResponseToString(top3Key[0]) + "=" + top3Value[0];
    }

    private String getTopCellInfo() {
        HashMap<Long, Integer> cellCount;
        String packageName;
        long[] top3Key = {0, 0, 0};
        int[] top3Value = {0, 0, 0};
        String top_package = "Null";
        synchronized (lockCellCount) {
            cellCount = (HashMap) sGetCellCount.clone();
        }
        Iterator<Map.Entry<Long, Integer>> it = cellCount.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Integer> entry = it.next();
            long key = entry.getKey().longValue();
            int value = entry.getValue().intValue();
            if (value > 0) {
                int i = 0;
                while (i < top3Value.length) {
                    if (value > top3Value[i]) {
                        int tempValue = top3Value[i];
                        long tempKey = top3Key[i];
                        top3Value[i] = value;
                        top3Key[i] = key;
                        value = tempValue;
                        key = tempKey;
                    }
                    i++;
                    it = it;
                }
                it = it;
            }
        }
        int i2 = 0;
        while (i2 < top3Value.length) {
            try {
                if (top3Value[i2] > 0) {
                    long uid = (top3Key[i2] & -4294967296L) >> 32;
                    long pid = top3Key[i2] & 4294967295L;
                    synchronized (lockCellCount) {
                        packageName = sGetCellUidPackage.get(Integer.valueOf((int) uid));
                        if (packageName == null) {
                            packageName = "Null";
                        }
                    }
                    if (i2 == 0) {
                        top_package = packageName;
                        int top_uid = (int) uid;
                        int top_pid = (int) pid;
                    }
                    log("CallCell_TOP[" + i2 + "] Uid:" + uid + ", Pid:" + pid + ", Package:" + packageName + ", Count:" + top3Value[i2]);
                }
                i2++;
            } catch (Exception e) {
                loge("getTopCellInfo error :" + e.toString());
            }
        }
        if (top3Value[0] <= 0) {
            return "Null";
        }
        return top_package + "=" + top3Value[0];
    }

    private String invokeResponseToString(int request) {
        try {
            Method method = Class.forName("com.android.internal.telephony.RIL").getDeclaredMethod("responseToString", Integer.TYPE);
            method.setAccessible(true);
            Object obj = method.invoke(null, Integer.valueOf(request));
            if (obj == null || obj.getClass() != String.class) {
                return "Null";
            }
            return (String) obj;
        } catch (Exception e) {
            loge("InvokeResponseToString error :" + e.toString());
            return "Null";
        }
    }

    private int getBattery() {
        return ((BatteryManager) this.mPhone.getContext().getSystemService("batterymanager")).getIntProperty(4);
    }

    public void screenOn() {
        sIsScreenOn.set(true);
        sScreenOnTime = System.currentTimeMillis() / 1000;
        if (sScreenOnTime - sScreenOffTime <= 5) {
            this.mIsIgnore = true;
            return;
        }
        this.mIsIgnore = false;
        this.mHandler.sendEmptyMessage(1);
    }

    public void screenOff() {
        sIsScreenOn.set(false);
        sScreenOffTime = System.currentTimeMillis() / 1000;
        this.mHandler.sendEmptyMessageDelayed(2, 6000);
    }

    public void updateNoServiceTime() {
        boolean isVolteEnabled;
        boolean isActivePhone = false;
        boolean isInService = this.mVoiceRegState == 0;
        boolean isPowerOff = getCombRegState(this.mSST.mSS) == 3;
        if (OppoUiccManagerImpl.getInstance().getSubState(this.mPhone.getSubId()) == 1) {
            isActivePhone = true;
        }
        long now = System.currentTimeMillis() / 1000;
        this.mPhone.isVolteEnabled();
        int curRAT = this.mSST.mSS.getRilDataRadioTechnology();
        if (curRAT == 19 || curRAT == 14) {
            isVolteEnabled = true;
        } else {
            isVolteEnabled = false;
        }
        if (isActivePhone) {
            synchronized (lockNoService) {
                if (this.mPhone.getPhoneId() < 2 && ((isInService || isPowerOff || isVolteEnabled) && sBeginNoServiceTime[this.mPhone.getPhoneId()] != 0)) {
                    if (sBeginNoServiceTime[this.mPhone.getPhoneId()] < sScreenOffTime) {
                        sBeginNoServiceTime[this.mPhone.getPhoneId()] = sScreenOffTime;
                    }
                    sNoServiceTime += now - sBeginNoServiceTime[this.mPhone.getPhoneId()];
                    long[] jArr = sNoServiceTimeSubs;
                    int phoneId = this.mPhone.getPhoneId();
                    jArr[phoneId] = jArr[phoneId] + (now - sBeginNoServiceTime[this.mPhone.getPhoneId()]);
                    sBeginNoServiceTime[this.mPhone.getPhoneId()] = 0;
                } else if (this.mPhone.getPhoneId() < 2 && !isInService && !isPowerOff && !isVolteEnabled && sBeginNoServiceTime[this.mPhone.getPhoneId()] == 0) {
                    sBeginNoServiceTime[this.mPhone.getPhoneId()] = now;
                }
            }
        }
    }

    public static void addDataCallCount() {
        if (!sIsScreenOn.get()) {
            synchronized (lockDataCallCount) {
                sDataCallCount++;
            }
        }
    }

    public static void addSmsSendCount() {
        if (!sIsScreenOn.get()) {
            synchronized (lockSmsSendCount) {
                sSmsSendCount++;
            }
        }
    }

    public static void addNitzCount() {
        if (!sIsScreenOn.get()) {
            synchronized (lockNitzCount) {
                sNitzCount++;
            }
        }
    }

    public void updateVoiceRegState(int state) {
        synchronized (this.lockVoiceRegState) {
            this.mVoiceRegState = state;
        }
    }

    public static void countUnsolMsg(int response) {
        if (!sIsScreenOn.get() && response != 0) {
            int count = 0;
            synchronized (lockRilCount) {
                if (sRilCount.get(Integer.valueOf(response)) != null) {
                    count = sRilCount.get(Integer.valueOf(response)).intValue();
                }
                sRilCount.put(Integer.valueOf(response), new Integer(count + 1));
            }
        }
    }

    public static void countGetCellInfo(int getCellUid, int getCellPid, String getCellPackage) {
        if (!sIsScreenOn.get()) {
            synchronized (lockCellCount) {
                int count = 0;
                Long id = Long.valueOf((((long) getCellUid) << 32) | ((long) getCellPid));
                if (sGetCellCount.get(id) != null) {
                    count = sGetCellCount.get(id).intValue();
                }
                sGetCellCount.put(id, Integer.valueOf(count + 1));
                sGetCellUidPackage.put(Integer.valueOf(getCellUid), getCellPackage);
            }
        }
    }

    private void notifyPowerMonitorScreenOn() {
        ConnectivityManager cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        ConnectivityManagerHelper.setTelephonyPowerState(cm, getTelephonyPowerState());
        ConnectivityManagerHelper.setTelephonyPowerLost(cm, getTelephonyPowerLost());
        ConnectivityManagerHelper.setAlreadyUpdated(cm, isAlreadyUpdated());
        Rlog.d(LOG_TAG, "NotifyPowerMonitorScreenOn done.");
    }

    private void notifyPowerMonitorScreenOff() {
        ConnectivityManagerHelper.setAlreadyUpdated((ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity"), isAlreadyUpdated());
        Rlog.d(LOG_TAG, "NotifyPowerMonitorScreenOff done.");
    }

    private void registerOosDozeParaSettings() {
        Rlog.d(LOG_TAG, "registerOosDozeParaSettings");
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("OosDozeCfg"), false, this.mOosDozeParaObserver);
    }

    private static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int i = str.length();
        do {
            i--;
            if (i < 0) {
                if (str.isEmpty()) {
                    return false;
                }
                return true;
            }
        } while (Character.isDigit(str.charAt(i)));
        return false;
    }

    /* access modifiers changed from: private */
    public void updateOosDozeParaFromSettings(boolean isInitial) {
        String mtkOosDozeCfg = Settings.Global.getString(this.mContext.getContentResolver(), "OosDozeCfg");
        if (mtkOosDozeCfg != null) {
            Rlog.d(LOG_TAG, "updateOosDozeParaFromSettings mtkOosDozeCfg is " + mtkOosDozeCfg);
            String[] paraArray = mtkOosDozeCfg.split(";");
            if (2 != paraArray.length) {
                log("updateOosDozeParaFromSettings length is  " + paraArray.length);
                return;
            }
            if (paraArray[0].contains("enable_in_device_idle")) {
                String[] deviceIdleArray = paraArray[0].split("=");
                if (2 == deviceIdleArray.length && isNumeric(deviceIdleArray[1])) {
                    if (1 == Integer.parseInt(deviceIdleArray[1])) {
                        this.mOosLpmEnableInDeviceIdle = true;
                    } else {
                        this.mOosLpmEnableInDeviceIdle = false;
                    }
                }
            }
            if (paraArray[1].contains("enable_in_light_device_idle")) {
                String[] lightDeviceIdleArray = paraArray[1].split("=");
                if (2 == lightDeviceIdleArray.length && isNumeric(lightDeviceIdleArray[1])) {
                    if (1 == Integer.parseInt(lightDeviceIdleArray[1])) {
                        this.mOosLpmEnableInLightDeviceIdle = true;
                    } else {
                        this.mOosLpmEnableInLightDeviceIdle = false;
                    }
                }
            }
            Rlog.d(LOG_TAG, "updateOosDozeParaFromSettings enable_in_device_idle: " + this.mOosLpmEnableInDeviceIdle + ", enable_in_light_device_idle: " + this.mOosLpmEnableInLightDeviceIdle);
            updateDeviceInactiveStateForOosLpm();
            return;
        }
        Rlog.d(LOG_TAG, "updateOosDozeParaFromSettings no mtkOosDozeCfg setting");
    }

    private void registerDeepsleepObserver() {
        this.mSettingObserver = new SettingObserver();
        this.mContext.getContentResolver().registerContentObserver(this.mDeepSleepUri, true, this.mSettingObserver);
    }

    private class SettingObserver extends ContentObserver {
        public SettingObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            Message msg = OppoNetworkPowerState.this.mHandler.obtainMessage(102);
            msg.arg1 = Settings.System.getInt(OppoNetworkPowerState.this.mContext.getContentResolver(), "oppoguaedelf_deep_sleep_status", 0);
            StringBuilder sb = new StringBuilder();
            sb.append("Deep Sleep mode ");
            sb.append(msg.arg1 == 1 ? "on" : "off");
            Rlog.d(OppoNetworkPowerState.LOG_TAG, sb.toString());
            OppoNetworkPowerState.this.mHandler.sendMessage(msg);
        }
    }

    public void updateDeviceInactiveStateForOosLpm() {
        boolean mOldDeviceInactiveState = this.mDeviceInactiveState;
        this.mDeviceInactiveState = this.mDeepSleepStatus || (this.mOosLpmEnableInDeviceIdle && this.mDeviceIdle) || (this.mOosLpmEnableInLightDeviceIdle && this.mLightDeviceIdle);
        boolean z = this.mDeviceInactiveState;
        if (z == mOldDeviceInactiveState) {
            return;
        }
        if (z) {
            this.mPhone.mCi.invokeOemRilRequestStrings(new String[]{"AT+ESRVREC=,,,1", ""}, (Message) null);
        } else {
            this.mPhone.mCi.invokeOemRilRequestStrings(new String[]{"AT+ESRVREC=,,,0", ""}, (Message) null);
        }
    }

    /* access modifiers changed from: private */
    public boolean isDeviceIdleModeOn() {
        return ((PowerManager) this.mPhone.getContext().getSystemService("power")).isDeviceIdleMode();
    }

    /* access modifiers changed from: private */
    public boolean isLightDeviceIdleModeOn() {
        return ((PowerManager) this.mPhone.getContext().getSystemService("power")).isLightDeviceIdleMode();
    }

    public void setOosLpmSwitchForDeviceIdle(boolean enableInDeviceIdle, boolean enableInLightDeviceIdle) {
        this.mOosLpmEnableInDeviceIdle = enableInDeviceIdle;
        this.mOosLpmEnableInLightDeviceIdle = enableInLightDeviceIdle;
        Rlog.d(LOG_TAG, "setOosLpmSwitchForDeviceIdle  enableInDeviceIdle=" + enableInDeviceIdle + " enableInLightDeviceIdle=" + enableInLightDeviceIdle);
    }

    /* access modifiers changed from: private */
    public void prepareDcsNormalMsg() {
        try {
            boolean isActivePhone = OppoUiccManagerImpl.getInstance().getSubState(this.mPhone.getSubId()) == 1;
            int phoneId = this.mPhone.getPhoneId();
            if (!sDcsMsg.equals("")) {
                sDcsMsg += ";";
            }
            sDcsMsg += "SIM" + phoneId + "#" + getCardType();
            sDcsMsg += ",HOME" + phoneId + "#" + getIsRoaming();
            StringBuilder sb = new StringBuilder();
            sb.append(sDcsMsg);
            sb.append(",ACTIVE");
            sb.append(phoneId);
            sb.append("#");
            sb.append(isActivePhone ? "1" : "0");
            sDcsMsg = sb.toString();
        } catch (Exception e) {
            loge("prepareDcsNormalMsg error :" + e.toString());
        }
    }

    /* access modifiers changed from: private */
    public void prepareDcsRilMsg() {
        try {
            sDcsMsg += ";";
            synchronized (lockNoService) {
                sDcsMsg += "NOSERVICETIME#" + sNoServiceTime;
            }
            sDcsMsg += ",DATACALLCOUNT#" + sDataCallCount;
            sDcsMsg += ",SMSSENDCOUNT#" + sSmsSendCount;
            sDcsMsg += ",NITZCOUNT#" + sNitzCount;
            sDcsMsg += ",RILJTOP#" + sRiljTopMsp;
            sDcsMsg += ",CALLCELLTOP#" + sCellInfoTopMsp;
        } catch (Exception e) {
            loge("prepareDcsRilMsg error :" + e.toString());
        }
    }

    /* access modifiers changed from: private */
    public void prepareDcsQmiMsg() {
        try {
            sDcsMsg += ";";
            for (int i = 0; i < 5; i++) {
                long count = sQmiIpaData[i] & 65535;
                long passagewayId = (sQmiIpaData[i] & 4294901760L) >> 16;
                long channelId = (sQmiIpaData[i] & -4294967296L) >> 32;
                if (i == 0) {
                    sDcsMsg += "QMI" + i + "#" + passagewayId + "_" + channelId + "=" + count;
                } else {
                    sDcsMsg += ",QMI" + i + "#" + passagewayId + "_" + channelId + "=" + count;
                }
            }
        } catch (Exception e) {
            loge("prepareDcsQmiMsg error :" + e.toString());
        }
    }

    /* access modifiers changed from: private */
    public void prepareDcsIpaMsg() {
        String str = "Null";
        String str2 = ".";
        try {
            sDcsMsg += ";";
            int i = 5;
            while (i < 10) {
                long count = (sQmiIpaData[i] & 1152851135862669312L) >> 50;
                long ip = sQmiIpaData[i] & 4294967295L;
                int[] ipRes = {0, 0, 0, 0};
                ipRes[0] = (int) (ip & 255);
                ipRes[1] = (int) ((65280 & ip) >> 8);
                ipRes[2] = (int) ((16711680 & ip) >> 16);
                ipRes[3] = (int) ((4278190080L & ip) >> 24);
                String hostName = InetAddress.getByName(ipRes[0] + str2 + ipRes[1] + str2 + ipRes[2] + str2 + ipRes[3]).getHostName();
                String uidName = this.mContext.getPackageManager().getNameForUid((int) ((sQmiIpaData[i] & 1125895611875328L) >> 32));
                if (uidName == null) {
                    uidName = str;
                }
                if (i == 5) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(sDcsMsg);
                    sb.append("IPA");
                    sb.append(i - 5);
                    sb.append("#");
                    sb.append(hostName);
                    sb.append("_");
                    sb.append(ip);
                    sb.append("_");
                    sb.append(uidName);
                    sb.append("=");
                    sb.append(count);
                    sDcsMsg = sb.toString();
                } else {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(sDcsMsg);
                    sb2.append(",IPA");
                    sb2.append(i - 5);
                    sb2.append("#");
                    sb2.append(hostName);
                    sb2.append("_");
                    sb2.append(ip);
                    sb2.append("_");
                    sb2.append(uidName);
                    sb2.append("=");
                    sb2.append(count);
                    sDcsMsg = sb2.toString();
                }
                i++;
                str = str;
                str2 = str2;
            }
        } catch (Exception e) {
            loge("prepareDcsIpaMsg error :" + e.toString());
        }
    }

    /* access modifiers changed from: private */
    public void prepareDcsBatteryMsg() {
        try {
            sDcsMsg += ";";
            sDcsMsg += "BATTERY#" + sBatteryPercent[0] + "_" + sBatteryPercent[1] + "_" + String.format("%.2f", Double.valueOf(((double) sScreenOffDuration) / 3600.0d)) + "h";
        } catch (Exception e) {
            loge("prepareDcsBatteryMsg error :" + e.toString());
        }
    }

    /* access modifiers changed from: private */
    public void notifyOppoManager() {
        try {
            String[] log_array = OemTelephonyUtils.getOemRes(this.mContext, "zz_oppo_critical_log_79", "").split(",");
            int log_type = Integer.valueOf(log_array[0]).intValue();
            String log_desc = log_array[1];
            log(sDcsMsg);
            OppoManagerHelper.writeLogToPartition(log_type, sDcsMsg, OppoRIL.ISSUE_SYS_OEM_NW_NO_SERVICE_AND_NW_SEARCH, log_desc);
            synchronized (lockDcsMsg) {
                sDcsMsg = "";
            }
        } catch (Exception e) {
            loge("notifyOppoManager error :" + e.toString());
        }
    }

    private static String getTelephonyPowerState() {
        long escapeTime = sScreenOnTime - sScreenOffTime;
        String[] wakeupReson = {"DATA_CALL_COUNT", "NO_SERVICE_TIME", "SMS_SEND_COUNT", "NITZ_COUNT"};
        double[] wakeupSrcPowerLost = {0.0d, 0.0d, 0.0d, 0.0d, 0.0d};
        wakeupSrcPowerLost[0] = ((double) sDataCallCount) * mPerDataCallPowerLost;
        if (escapeTime <= 0 || sNoServiceTime <= escapeTime) {
            wakeupSrcPowerLost[1] = ((double) sNoServiceTime) * mPerNoServicePowerLost;
        } else {
            wakeupSrcPowerLost[1] = ((double) escapeTime) * mPerNoServicePowerLost;
        }
        wakeupSrcPowerLost[2] = ((double) sSmsSendCount) * mPerSMSSendLost;
        wakeupSrcPowerLost[3] = ((double) sNitzCount) * mPerNITZLost;
        int topIndex = 0;
        double max = 0.0d;
        for (int i = 0; i < wakeupSrcPowerLost.length; i++) {
            if (wakeupSrcPowerLost[i] > max) {
                max = wakeupSrcPowerLost[i];
                topIndex = i;
            }
        }
        return wakeupReson[topIndex] + ":" + max + " RILJ_TOP:" + sRiljTopMsp;
    }

    private static double getTelephonyPowerLost() {
        long escapeTime = sScreenOnTime - sScreenOffTime;
        double ret = (((double) sDataCallCount) * mPerDataCallPowerLost) + (((double) sSmsSendCount) * mPerSMSSendLost) + (((double) sNitzCount) * mPerNITZLost);
        if (escapeTime <= 0 || sNoServiceTime <= escapeTime) {
            return (((double) sNoServiceTime) * mPerNoServicePowerLost) + ret;
        }
        return (((double) escapeTime) * mPerNoServicePowerLost) + ret;
    }

    private static boolean isAlreadyUpdated() {
        boolean[] zArr = sAlreadyUpdated;
        return zArr[0] && zArr[1];
    }

    /* access modifiers changed from: private */
    public synchronized void log(String s) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "] " + s);
    }

    /* access modifiers changed from: private */
    public synchronized void loge(String s) {
        Rlog.e(LOG_TAG, "[" + this.mPhone.getPhoneId() + "] " + s);
    }
}
