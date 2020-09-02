package com.oppo.internal.telephony.explock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.util.SparseArray;
import com.android.internal.telephony.OemConstant;
import com.oppo.internal.telephony.OppoCallStateMonitor;
import com.oppo.internal.telephony.OppoTelephonyController;
import com.oppo.internal.telephony.utils.OppoManagerHelper;

public class OemRegionLockMonitorManager {
    private static final int CALL_DURATION_INDEX = 1;
    private static final String CALL_DURATION_TIME = "oem_r_l_call_duration_time";
    private static final int CALL_DURATION_TIME_MIN = 180;
    private static final String CALL_TIMES = "oem_r_l_call_times";
    private static final int CALL_TIMES_INDEX = 0;
    private static final int CALL_TIMES_MIN = 3;
    private static final String CHARGE_ALL_TIMES = "oem_r_l_charge_all_times";
    private static final int CHARGE_FULL_INDEX = 7;
    private static final String CHARGE_FULL_TIMES = "oem_r_l_charge_full_times";
    private static final int CHARGE_FULL_TIMES_MIN = 1;
    private static final int CHARGE_TIMES_INDEX = 6;
    private static final int CHARGE_TIMES_MIN = 3;
    private static final String DISABLE_RLOCK = "persist.sys.rlock.disable";
    private static final int DISABLE_V_1 = 1;
    private static final int DISABLE_V_2 = 2;
    private static final int ENABLE_ALL = 0;
    private static final boolean ENALBE = SystemProperties.get(DISABLE_RLOCK, "false").equals("false");
    protected static final int EVENT_CALL_END = 104;
    protected static final int EVENT_CALL_START = 103;
    protected static final int EVENT_OEM_SCREEN_CHANGED = 101;
    protected static final int EVENT_SHUTDOWN_CHANGED = 102;
    private static final String IN_CALL_PROXIMITY = "oem_r_l_in_call_proximity_change_times";
    private static boolean NEED_CALL_STATE = false;
    private static boolean NEED_NETWORK_INFO = false;
    private static boolean NEED_SCREEN_CHANGE = false;
    private static boolean NEED_STEP_COUNTER = false;
    private static boolean NEED_SUPPORT_CHARGE = false;
    private static long ONE_MINUTE = 60000;
    private static final String OPPO_ACTION_BATTERY_CHANGED = "oppo.intent.action.BATTERY_PLUGGED_CHANGED";
    private static final int PROXIMITY_CHANGE_INDEX = 2;
    private static final int PROXIMITY_CHANGE_TIMES_INCALL = 3;
    private static final String PRO_KEY_LOG_STATE = "persist.sys.rlock.para";
    private static final String REGISTER_DURATION_STATE = "oem_r_l_register_duration_state";
    private static final String RLOCK_ACTIVE_STATUS = "oem_r_l_active_state";
    private static final int SCREEN_CHANGE_INDEX = 5;
    private static final String SCREEN_CHANGE_TIMES = "oem_r_l_screen_change_times";
    private static final int SCREEN_CHANGE_TIMES_MIN = 10;
    protected static final int SCREEN_WRITE_INTERVAL = 3;
    private static final int SERVICE_CELL_COUNT_MIN = 3;
    private static final int SERVICE_CELL_INDEX = 4;
    private static final String SERVICE_CELL_NUM = "oem_r_l_service_cell_num";
    private static final String STEP_COUNTER = "oem_r_l_step_counter";
    private static final int STEP_COUNTER_MIN = 500;
    private static final int STEP_COUNT_INDEX = 3;
    private static final String TAG = "OemRegionLockMonitorManager";
    private static final boolean VDBG = "true".equals(SystemProperties.get("persist.sys.regionlock.debug", "false"));
    private static Context mContext;
    private static RegionLockDesUtils mDesUtils;
    private static Handler mEventHandler;
    private static OemRegionLockMonitorManager mInstance;
    private static volatile boolean mIsCallStateListen = false;
    private static volatile boolean mIsProximListen = false;
    private static volatile boolean mIsScreenListen = false;
    private static volatile boolean mIsStepCounterListen = false;
    private static final Object mLock = new Object();
    private static volatile boolean mRegistered = false;
    private static long sCallBeginTime = 0;
    private static int sCallCount;
    private static long sCallDurationMinute;
    private static HandlerThread sHandlerThread = new HandlerThread("oemRLMM");
    private boolean callSuccess = false;
    private boolean chargeSuccess = false;
    private final SparseArray<DeviceUsageState> mCurrentDeviceUsageState = new SparseArray<>();
    protected int mCurrentScreenChangeCount = 0;
    protected boolean mLastBatteryPresent;
    protected boolean mLastCallOn;
    private final SparseArray<DeviceUsageState> mLastDeviceUsageState = new SparseArray<>();
    protected int mLastScreenChangeCount = 0;
    protected boolean mLastScreenOn;
    private int mProximityEventCount = 0;
    private OemRegonLockReceiver mReceiver;
    private boolean networkSuccess = false;
    private boolean screenSuccess = false;
    private boolean stepSuccess = false;

    static {
        boolean z = ENALBE;
        NEED_SUPPORT_CHARGE = z;
        NEED_STEP_COUNTER = z;
        NEED_CALL_STATE = z;
        NEED_SCREEN_CHANGE = z;
        NEED_NETWORK_INFO = z;
    }

    public static OemRegionLockMonitorManager getInstance() {
        return mInstance;
    }

    public static OemRegionLockMonitorManager make(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new OemRegionLockMonitorManager(context);
            }
        }
        return mInstance;
    }

    private OemRegionLockMonitorManager(Context context) {
        mContext = context;
        OemCellInfoMonitor.getDefault(context);
        OemSensorStateMonitor.getDefault(context);
        try {
            mDesUtils = new RegionLockDesUtils();
        } catch (Exception e) {
        }
        sHandlerThread.start();
        mEventHandler = new EventHandler(sHandlerThread.getLooper());
        this.mReceiver = new OemRegonLockReceiver();
        registerLockMonitor(context);
    }

    public void registerLockMonitor(Context context) {
        registerReceiver(context);
        registerSceeen();
        registerCallState(context);
    }

    public void registerReceiver(Context context) {
        if (!mRegistered) {
            logd("registerStateReceiver..");
            IntentFilter filter = new IntentFilter();
            filter.addAction(OPPO_ACTION_BATTERY_CHANGED);
            context.registerReceiver(this.mReceiver, filter);
            mRegistered = true;
        }
    }

    public void unregisterReceiver() {
        if (mRegistered) {
            logd("unregisterReceiver");
            mContext.unregisterReceiver(this.mReceiver);
            mRegistered = false;
        }
    }

    public void registerSceeen() {
        if (!mIsScreenListen) {
            logd("registerSceeen");
            OppoTelephonyController.getInstance(mContext).registerForOemScreenChanged(mEventHandler, 101, null);
            mIsScreenListen = true;
        }
    }

    public void unregisterScreen() {
        if (mIsScreenListen) {
            logd("unregisterScreen");
            OppoTelephonyController.getInstance(mContext).unregisterOemScreenChanged(mEventHandler);
            mIsScreenListen = false;
        }
    }

    public void registerCallState(Context context) {
        if (!mIsCallStateListen) {
            logd("registerCallState");
            OppoCallStateMonitor.getInstance(context).registerForCallActive(mEventHandler, 103, null);
            OppoCallStateMonitor.getInstance(context).registerForCallEnd(mEventHandler, 104, null);
            mIsCallStateListen = true;
        }
    }

    public void unregisterCallState(Context context) {
        if (mIsCallStateListen) {
            logd("unregisterCallState");
            OppoCallStateMonitor.getInstance(context).unregisterForCallActive(mEventHandler);
            OppoCallStateMonitor.getInstance(context).unregisterForCallEnd(mEventHandler);
            mIsCallStateListen = false;
        }
    }

    private void registerProximi() {
        if (!mIsProximListen) {
            logd("registerProximi");
            OemSensorStateMonitor.getDefault(mContext).notifyRegisterSensor(8);
            mIsProximListen = true;
        }
    }

    private void unregisterProximi() {
        if (mIsProximListen) {
            logd("unregisterProximi");
            OemSensorStateMonitor.getDefault(mContext).notifyUnRegisterSensor(8);
            mIsProximListen = false;
        }
    }

    private void registerStepCounter() {
        if (!mIsStepCounterListen) {
            logd("registerStepCounter");
            OemSensorStateMonitor.getDefault(mContext).notifyRegisterSensor(19);
            mIsStepCounterListen = true;
        }
    }

    private void unregisterStepCounter() {
        if (mIsStepCounterListen) {
            logd("unregisterStepCounter");
            OemSensorStateMonitor.getDefault(mContext).notifyUnRegisterSensor(19);
            mIsStepCounterListen = false;
        }
    }

    public void updateScreenChangeInfo(int screenChangeCount, boolean isOn) {
        DeviceUsageState state = getCurrentDeviceUsageState();
        int unused = state.mScreenChangeCount = screenChangeCount;
        boolean unused2 = state.mScreenOn = isOn;
        notifyUpdateLockStatus();
    }

    public void updateCallServiceInfo(long callduration, int callCount, boolean isCalling) {
        DeviceUsageState state = getCurrentDeviceUsageState();
        long unused = state.mCallDurationMinute = callduration;
        int unused2 = state.mCallCount = callCount;
        boolean unused3 = state.mIsCalling = isCalling;
        notifyUpdateLockStatus();
    }

    public void updateNetworkInfo(int cellcount) {
        logd("updateNetworkInfo:,cellcount = " + cellcount);
        if (setServiceCellCount(cellcount)) {
            int unused = getCurrentDeviceUsageState().mRegisterCellCount = cellcount;
            notifyUpdateLockStatus();
        }
    }

    public void updateMatchRegisterDurationState(boolean hasMatch) {
        if (setRegisterDurationState(hasMatch)) {
            int unused = getCurrentDeviceUsageState().mRegisterDurationState = hasMatch ? 1 : 0;
            notifyUpdateLockStatus();
        }
    }

    public void updateSensorChangedInfo(int type, int changeCount, int status, boolean isworking) {
        logDebug("updateSensorChangedInfo:,type = " + type + ",changeCount = " + changeCount + ",status = isworking = " + isworking);
        DeviceUsageState state = getCurrentDeviceUsageState();
        if (8 == type) {
            if (setProximityChangeCountIncall(changeCount)) {
                int unused = state.mPSersorChangeCount = changeCount;
                int unused2 = state.mPSersorState = status;
                boolean unused3 = state.mIsProWorking = isworking;
            } else {
                return;
            }
        } else if (setStepCounterCount(changeCount)) {
            int unused4 = state.mStepCounterCount = changeCount;
            int unused5 = state.mStepsorState = status;
            boolean unused6 = state.mIsSteping = isworking;
        } else {
            return;
        }
        notifyUpdateLockStatus();
    }

    public void updateChargeChangedInfo(int chargeAllCount, int chargeFullCount, boolean isworking) {
        DeviceUsageState state = getCurrentDeviceUsageState();
        int unused = state.mChargeAllCount = chargeAllCount;
        int unused2 = state.mStepCounterCount = chargeFullCount;
        boolean unused3 = state.mIsCharging = isworking;
        notifyUpdateLockStatus();
    }

    private void notifyUpdateLockStatus() {
        DeviceUsageState currentState = getCurrentDeviceUsageState();
        logDebug("getCurrentUsageState = " + currentState.toString());
        DeviceUsageState lastState = getLastDeviceUsageState();
        logDebug("getLastUsageState = " + currentState.toString());
        if (!currentState.equals(lastState)) {
            lastState.copyFrom(currentState);
            matchUnlock();
        }
    }

    private void matchUnlock() {
        if (matchCallState() && matchCharge() && matchScreenChange() && matchNetworkInfo()) {
            unlockRegionLock();
        }
    }

    private void unlockRegionLock() {
        Rlog.e(TAG, "unlockRegionLock:");
        Intent intent = new Intent(RegionLockConstant.ACTION_NETWORK_LOCK);
        intent.putExtra(RegionLockConstant.NETLOCK_STATUS, "0");
        intent.putExtra(RegionLockConstant.UNLOCK_TYPE, "1");
        sendBroadCastChangedNetlockStatus(intent);
        OemLockUtils.setRegionLockedStatus("0");
        setRlockActiveState();
    }

    private void sendBroadCastChangedNetlockStatus(Intent intent) {
        mContext.sendBroadcast(intent, "oppo.permission.OPPO_COMPONENT_SAFE");
    }

    private boolean matchCharge() {
        boolean match = true;
        if (NEED_SUPPORT_CHARGE && !this.chargeSuccess) {
            boolean success = getChargeAllCount() >= 3;
            this.chargeSuccess = success;
            if (this.chargeSuccess && !getKeyLogSaveState(1)) {
                recordRegionLockKeyLog(1, "charge_info");
            }
            match = success;
        }
        if (match) {
            unregisterReceiver();
        }
        return match;
    }

    private boolean matchStepCounter() {
        boolean match = true;
        if (NEED_STEP_COUNTER && !this.stepSuccess) {
            boolean success = getStepCounterCount() >= STEP_COUNTER_MIN;
            this.stepSuccess = success;
            if (this.stepSuccess && !getKeyLogSaveState(2)) {
                recordRegionLockKeyLog(2, "step_info");
            }
            match = success;
        }
        if (match) {
            unregisterStepCounter();
        }
        return match;
    }

    private boolean matchCallState() {
        boolean match = true;
        if (NEED_CALL_STATE && !this.callSuccess) {
            boolean success = getCallCount() >= 3 && getCallDurationTime() >= 180 && getProximityChangeCountIncall() >= 3;
            this.callSuccess = success;
            if (this.callSuccess && !getKeyLogSaveState(0)) {
                recordRegionLockKeyLog(0, "call_info");
            }
            match = success;
        }
        if (match) {
            unregisterCallState(mContext);
        }
        return match;
    }

    private boolean matchScreenChange() {
        boolean match = true;
        if (NEED_SCREEN_CHANGE && !this.screenSuccess) {
            boolean success = getScreenChangeCount() >= 10;
            this.screenSuccess = success;
            if (this.screenSuccess && !getKeyLogSaveState(3)) {
                recordRegionLockKeyLog(3, "screen_info");
            }
            match = success;
        }
        if (match) {
            unregisterScreen();
        }
        return match;
    }

    private boolean matchNetworkInfo() {
        boolean match = true;
        if (NEED_NETWORK_INFO && !this.networkSuccess) {
            boolean success = getServiceCellCount() >= 3;
            this.networkSuccess = success;
            if (this.networkSuccess && !getKeyLogSaveState(4)) {
                recordRegionLockKeyLog(4, "network_info");
            }
            match = success;
        }
        return match && matchNetworkRegisterTime();
    }

    private boolean matchNetworkRegisterTime() {
        return getRegisterDurationState() >= 1;
    }

    protected class OemRegonLockReceiver extends BroadcastReceiver {
        private int mBatteryLevel = 100;
        private int mBatteryStatus = 1;
        private boolean mIsBattery = false;
        private int mcheckBatteryLavelMax = 100;

        public OemRegonLockReceiver() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:24:0x006f, code lost:
            return;
         */
        public void onReceive(Context context, Intent intent) {
            String action;
            if (intent != null && (action = intent.getAction()) != null) {
                OemRegionLockMonitorManager.logd("Action:" + action);
                synchronized (this) {
                    if (action.equals(OemRegionLockMonitorManager.OPPO_ACTION_BATTERY_CHANGED)) {
                        boolean batteryPresent = false;
                        int powerSorce = intent.getIntExtra("plugged", 0);
                        if (1 == powerSorce || 2 == powerSorce || 4 == powerSorce) {
                            batteryPresent = true;
                        }
                        OemRegionLockMonitorManager.logd("ACTION_BATTERY_CHANGED batteryPresent:" + batteryPresent + "mIsBattery " + this.mIsBattery + "batteryPresent = " + batteryPresent);
                        if (this.mIsBattery != batteryPresent) {
                            this.mIsBattery = batteryPresent;
                            OemRegionLockMonitorManager.this.onBatteryStateChanged(this.mIsBattery, true, false);
                        }
                    }
                }
            }
        }
    }

    public void onSreenChanged(boolean isOn) {
        logDebug("onSreenChange : " + isOn);
        synchronized (this) {
            if (this.mLastScreenOn != isOn) {
                this.mCurrentScreenChangeCount++;
                if (this.mLastScreenChangeCount + 3 <= this.mCurrentScreenChangeCount) {
                    if (setScreenChangeCount(this.mCurrentScreenChangeCount - this.mLastScreenChangeCount)) {
                        updateScreenChangeInfo(getScreenChangeCount(), isOn);
                    }
                    this.mLastScreenChangeCount = this.mCurrentScreenChangeCount;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0036, code lost:
        updateChargeChangedInfo(getChargeAllCount(), getChargeFullCount(), r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0034, code lost:
        if (setChargeInfo(r3, r4, r5) == false) goto L_?;
     */
    public void onBatteryStateChanged(boolean batteryPresent, boolean updateChargeCount, boolean isChargeFull) {
        synchronized (this) {
            if (this.mLastBatteryPresent != batteryPresent) {
                this.mLastBatteryPresent = batteryPresent;
                logd("onBatteryChanged mLastBatteryPresent:" + this.mLastBatteryPresent + ",updateChargeCount = " + updateChargeCount + ",isChargeFull = " + isChargeFull);
            }
        }
    }

    public void RecordCallStateInfo(boolean onCall) {
        logd("RecordCallStateInfo onCall = " + onCall + ",mLastCallOn = " + this.mLastCallOn);
        if (this.mLastCallOn != onCall) {
            if (onCall) {
                sCallBeginTime = SystemClock.elapsedRealtime();
                if (!mIsProximListen) {
                    registerProximi();
                    loge("Register sensor state!");
                }
            } else if (sCallBeginTime != 0) {
                sCallDurationMinute = (SystemClock.elapsedRealtime() - sCallBeginTime) / 1000;
                sCallBeginTime = 0;
                setCallDurationTime(sCallDurationMinute);
                setCallCount();
                unregisterProximi();
            }
            this.mLastCallOn = onCall;
            updateCallServiceInfo(getCallDurationTime(), getCallCount(), onCall);
        }
    }

    public void disableRlockFeature(int feature) {
        if (2 == feature) {
            disableFeatureV2();
        } else if (1 == feature) {
            unlockRegionLock();
            SystemProperties.set(DISABLE_RLOCK, "true");
        } else if (feature == 0) {
            enableFeature();
        }
    }

    public void disableFeatureV2() {
        NEED_CALL_STATE = false;
        NEED_STEP_COUNTER = false;
        NEED_NETWORK_INFO = false;
        NEED_SCREEN_CHANGE = false;
        NEED_SUPPORT_CHARGE = false;
        matchUnlock();
        SystemProperties.set(DISABLE_RLOCK, "true");
    }

    public void enableFeature() {
        NEED_CALL_STATE = true;
        NEED_STEP_COUNTER = true;
        NEED_NETWORK_INFO = true;
        NEED_SCREEN_CHANGE = true;
        NEED_SUPPORT_CHARGE = true;
        SystemProperties.set(DISABLE_RLOCK, "false");
    }

    private void recordRegionLockKeyLog(int saveIndex, String keyInfo) {
        int log_type = -1;
        String log_desc = "";
        String issueLog = "";
        try {
            StringBuilder logInfo = new StringBuilder();
            logInfo.append("call_times : " + getCallCount());
            logInfo.append(",call_duration : " + getCallDurationTime());
            logInfo.append(",call_pro_change_times : " + getProximityChangeCountIncall());
            logInfo.append(",step_counter : " + getStepCounterCount());
            logInfo.append(",cell_num : " + getServiceCellCount());
            logInfo.append(",screen_change_times : " + getScreenChangeCount());
            logInfo.append(",charge_all_times : " + getChargeAllCount());
            logInfo.append(",register_duration_state : " + getRegisterDurationState());
            logInfo.append(",rlock_active_state : " + getRlockActiveState());
            issueLog = logInfo.toString();
            logd("recordRegionLockKeyLog : " + issueLog + ",keyInfo : " + keyInfo);
            String[] log_array = mContext.getString(mContext.getResources().getIdentifier("zz_oppo_critical_log_99", "string", "android")).split(",");
            log_type = Integer.valueOf(log_array[0]).intValue();
            log_desc = log_array[1];
        } catch (Exception e) {
            logd("recordRegionLockKeyLog:" + e);
        }
        OppoManagerHelper.writeLogToPartition(log_type, issueLog, keyInfo, log_desc);
        setKeyLogSaveState(saveIndex, true);
    }

    private static boolean getKeyLogSaveState(int index) {
        boolean saved = SystemProperties.getBoolean(PRO_KEY_LOG_STATE + index, false);
        logd("getKeyLogSaveState saved:" + saved);
        return saved;
    }

    private static void setKeyLogSaveState(int index, boolean save) {
        SystemProperties.set(PRO_KEY_LOG_STATE + index, Boolean.toString(save));
    }

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message msg) {
            OemRegionLockMonitorManager.logd("EventHandler:" + msg.what);
            int i = msg.what;
            if (i == 101) {
                AsyncResult arscreen = (AsyncResult) msg.obj;
                if (arscreen != null) {
                    OemRegionLockMonitorManager.this.onSreenChanged(((Boolean) arscreen.result).booleanValue());
                    return;
                }
                OemRegionLockMonitorManager.loge("EVENT_OEM_SCREEN_CHANGED error");
            } else if (i == 103) {
                OemRegionLockMonitorManager.this.RecordCallStateInfo(true);
            } else if (i == 104) {
                OemRegionLockMonitorManager.this.RecordCallStateInfo(false);
            }
        }
    }

    public static class DeviceUsageState {
        /* access modifiers changed from: private */
        public int mCallCount = 0;
        /* access modifiers changed from: private */
        public long mCallDurationMinute = 0;
        /* access modifiers changed from: private */
        public int mChargeAllCount = 0;
        private int mChargeFullCount = 0;
        /* access modifiers changed from: private */
        public boolean mIsCalling = false;
        /* access modifiers changed from: private */
        public boolean mIsCharging = false;
        /* access modifiers changed from: private */
        public boolean mIsProWorking = false;
        /* access modifiers changed from: private */
        public boolean mIsSteping = false;
        /* access modifiers changed from: private */
        public int mPSersorChangeCount = 0;
        /* access modifiers changed from: private */
        public int mPSersorState = 0;
        /* access modifiers changed from: private */
        public int mRegisterCellCount = 0;
        /* access modifiers changed from: private */
        public int mRegisterDurationState = 0;
        /* access modifiers changed from: private */
        public int mScreenChangeCount = 0;
        /* access modifiers changed from: private */
        public boolean mScreenOn = false;
        /* access modifiers changed from: private */
        public int mStepCounterCount = 0;
        /* access modifiers changed from: private */
        public int mStepsorState = 0;

        public DeviceUsageState() {
            initDeviceUsageState();
        }

        public void initDeviceUsageState() {
            this.mCallDurationMinute = OemRegionLockMonitorManager.getCallDurationTime();
            this.mCallCount = getCallCount();
            this.mPSersorChangeCount = getPSersorChangeCount();
            this.mStepCounterCount = getStepCounterCount();
            this.mRegisterCellCount = OemRegionLockMonitorManager.getServiceCellCount();
            this.mRegisterDurationState = OemRegionLockMonitorManager.getRegisterDurationState();
            this.mScreenChangeCount = getScreenChangeCount();
            this.mChargeAllCount = getChargeAllCount();
            this.mChargeFullCount = getChargeFullCount();
        }

        public void copyFrom(DeviceUsageState state) {
            this.mIsCalling = state.mIsCalling;
            this.mCallDurationMinute = state.mCallDurationMinute;
            this.mCallCount = state.mCallCount;
            this.mIsProWorking = state.mIsProWorking;
            this.mPSersorChangeCount = state.mPSersorChangeCount;
            this.mPSersorState = state.mPSersorState;
            this.mIsSteping = state.mIsSteping;
            this.mStepCounterCount = state.mStepCounterCount;
            this.mStepsorState = state.mStepsorState;
            this.mRegisterCellCount = state.mRegisterCellCount;
            this.mRegisterDurationState = state.mRegisterDurationState;
            this.mScreenOn = state.mScreenOn;
            this.mScreenChangeCount = state.mScreenChangeCount;
            this.mIsCharging = state.mIsCharging;
            this.mChargeAllCount = state.mChargeAllCount;
            this.mChargeFullCount = state.mChargeFullCount;
        }

        public boolean equals(DeviceUsageState state) {
            return this.mIsCalling == state.mIsCalling && this.mCallDurationMinute == state.mCallDurationMinute && this.mCallCount == state.mCallCount && this.mIsProWorking == state.mIsProWorking && this.mPSersorChangeCount == state.mPSersorChangeCount && this.mPSersorState == state.mPSersorState && this.mIsSteping == state.mIsSteping && this.mStepCounterCount == state.mStepCounterCount && this.mStepsorState == state.mStepsorState && this.mRegisterCellCount == state.mRegisterCellCount && this.mRegisterDurationState == state.mRegisterDurationState && this.mScreenOn == state.mScreenOn && this.mScreenChangeCount == state.mScreenChangeCount && this.mIsCharging == state.mIsCharging && this.mChargeAllCount == state.mChargeAllCount && this.mChargeFullCount == state.mChargeFullCount;
        }

        public boolean isCalling() {
            return this.mIsCalling;
        }

        public long getCallDurationMinute() {
            return this.mCallDurationMinute;
        }

        public int getCallCount() {
            return this.mCallCount;
        }

        public int getPSersorChangeCount() {
            return this.mPSersorChangeCount;
        }

        public boolean getIsProximity() {
            return this.mIsProWorking;
        }

        public int getStepCounterCount() {
            return this.mStepCounterCount;
        }

        public boolean IsSteping() {
            return this.mIsSteping;
        }

        public int getStepsorState() {
            return this.mStepsorState;
        }

        public int getRegisterCellCount() {
            return this.mRegisterCellCount;
        }

        public boolean getScreenOn() {
            return this.mScreenOn;
        }

        public int getScreenChangeCount() {
            return this.mScreenChangeCount;
        }

        public boolean isCharging() {
            return this.mIsCharging;
        }

        public int getChargeAllCount() {
            return this.mChargeAllCount;
        }

        public int getChargeFullCount() {
            return this.mChargeFullCount;
        }

        public String toString() {
            return "isCalling=" + String.valueOf(this.mIsCalling) + ", " + "mCallDurationMinute=" + String.valueOf(this.mCallDurationMinute) + ", " + "mCallCount=" + String.valueOf(this.mCallCount) + ", " + "mPSersorChangeCount=" + String.valueOf(this.mPSersorChangeCount) + ", " + "mIsProWorking=" + String.valueOf(this.mIsProWorking) + ", " + "mPSersorState=" + String.valueOf(this.mPSersorState) + ", " + "mIsSteping=" + String.valueOf(this.mIsSteping) + ", " + "mStepCounterCount=" + String.valueOf(this.mStepCounterCount) + ", " + "mStepsorState=" + String.valueOf(this.mStepsorState) + ", " + "mRegisterCellCount=" + String.valueOf(this.mRegisterCellCount) + ", " + "mRegisterDurationState=" + String.valueOf(this.mRegisterDurationState) + ", " + "mScreenOn=" + String.valueOf(this.mScreenOn) + ", " + "mScreenChangeCount=" + String.valueOf(this.mScreenChangeCount) + ", " + "mIsCharging=" + String.valueOf(this.mIsCharging) + ", " + "mChargeAllCount=" + String.valueOf(this.mChargeAllCount) + ", " + "mChargeFullCount=" + String.valueOf(this.mChargeFullCount) + ", ";
        }
    }

    private DeviceUsageState getCurrentDeviceUsageState() {
        return getDeviceUsageState(0, this.mCurrentDeviceUsageState);
    }

    private DeviceUsageState getLastDeviceUsageState() {
        return getDeviceUsageState(0, this.mLastDeviceUsageState);
    }

    private static DeviceUsageState getDeviceUsageState(int key, SparseArray<DeviceUsageState> array) {
        DeviceUsageState state = array.get(key);
        if (state != null) {
            return state;
        }
        DeviceUsageState state2 = new DeviceUsageState();
        array.put(key, state2);
        return state2;
    }

    public static int getScreenChangeCount() {
        return getDataFromSettings(SCREEN_CHANGE_TIMES);
    }

    public static boolean setScreenChangeCount(int value) {
        int count = getDataFromSettings(SCREEN_CHANGE_TIMES);
        if (count > 10) {
            return true;
        }
        logd("setScreenChangeCount:" + count);
        return setDataToSettings(SCREEN_CHANGE_TIMES, count + value);
    }

    public static int getChargeAllCount() {
        return getDataFromSettings(CHARGE_ALL_TIMES);
    }

    public static boolean setChargeAllCount() {
        int count = getDataFromSettings(CHARGE_ALL_TIMES);
        if (count > 3) {
            return true;
        }
        return setDataToSettings(CHARGE_ALL_TIMES, count + 1);
    }

    public static boolean setChargeInfo(boolean isCharging, boolean updateChargeCount, boolean isFullCharge) {
        boolean result1 = false;
        boolean result2 = false;
        if (isCharging && updateChargeCount && !isFullCharge) {
            result1 = setChargeAllCount();
        } else if (!updateChargeCount && isFullCharge) {
            result2 = setChargeFullCount();
        }
        return result1 || result2;
    }

    public static int getChargeFullCount() {
        return getDataFromSettings(CHARGE_FULL_TIMES);
    }

    public static boolean setChargeFullCount() {
        int count = getDataFromSettings(CHARGE_FULL_TIMES);
        if (count > 1) {
            return true;
        }
        return setDataToSettings(CHARGE_FULL_TIMES, count + 1);
    }

    public static int getStepCounterCount() {
        return getDataFromSettings(STEP_COUNTER);
    }

    public static boolean setStepCounterCount(int value) {
        int count = getDataFromSettings(STEP_COUNTER);
        if (count > STEP_COUNTER_MIN) {
            return true;
        }
        return setDataToSettings(STEP_COUNTER, count + value);
    }

    public static int getProximityChangeCountIncall() {
        return getDataFromSettings(IN_CALL_PROXIMITY);
    }

    public static boolean setProximityChangeCountIncall(int value) {
        int count = getDataFromSettings(IN_CALL_PROXIMITY);
        if (count > 3) {
            return true;
        }
        return setDataToSettings(IN_CALL_PROXIMITY, count + value);
    }

    public static int getCallCount() {
        return getDataFromSettings(CALL_TIMES);
    }

    public static boolean setCallCount() {
        int count = getDataFromSettings(CALL_TIMES);
        if (count > 3) {
            return true;
        }
        return setDataToSettings(CALL_TIMES, count + 1);
    }

    public static long getCallDurationTime() {
        return getLongDataFromSettings(CALL_DURATION_TIME);
    }

    public static boolean setCallDurationTime(long second) {
        long time = (long) getDataFromSettings(CALL_DURATION_TIME);
        if (time > 180) {
            return true;
        }
        return setLongDataFromSettings(CALL_DURATION_TIME, time + second);
    }

    public static int getServiceCellCount() {
        return getDataFromSettings(SERVICE_CELL_NUM);
    }

    public static boolean setServiceCellCount(int cellCount) {
        if (cellCount > 3) {
            return true;
        }
        return setDataToSettings(SERVICE_CELL_NUM, cellCount);
    }

    public static boolean setRegisterDurationState(boolean hasMatch) {
        getDataFromSettings(REGISTER_DURATION_STATE);
        return setDataToSettings(REGISTER_DURATION_STATE, 1);
    }

    public static int getRegisterDurationState() {
        return getDataFromSettings(REGISTER_DURATION_STATE);
    }

    public static int getRlockActiveState() {
        return getDataFromSettings(RLOCK_ACTIVE_STATUS);
    }

    public static boolean setRlockActiveState() {
        return setDataToSettings(RLOCK_ACTIVE_STATUS, 1);
    }

    public static int getDataFromSettings(String type) {
        int value = 0;
        try {
            value = Integer.parseInt(mDesUtils.decrypt(Settings.Secure.getString(mContext.getContentResolver(), type)));
        } catch (Exception e) {
        }
        logDebug("get type = " + type + ",value = " + value);
        return value;
    }

    public static boolean setDataToSettings(String type, int value) {
        logDebug(" set type = " + type + ",value = " + value);
        try {
            Settings.Secure.putString(mContext.getContentResolver(), type, mDesUtils.encrypt(String.valueOf(value)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static long getLongDataFromSettings(String type) {
        long value = 0;
        try {
            value = Long.parseLong(mDesUtils.decrypt(Settings.Secure.getString(mContext.getContentResolver(), type)));
        } catch (Exception e) {
        }
        logDebug("get type = " + type + ",value = " + value);
        return value;
    }

    public static boolean setLongDataFromSettings(String type, long value) {
        logDebug("set type = " + type + ",value = " + value);
        try {
            Settings.Secure.putString(mContext.getContentResolver(), type, mDesUtils.encrypt(String.valueOf(value)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(TAG, s);
        }
    }

    static void logDebug(String s) {
        if (OemConstant.SWITCH_LOG && VDBG) {
            Rlog.d(TAG, s);
        }
    }

    static void loge(String s) {
        Rlog.e(TAG, s);
    }
}
