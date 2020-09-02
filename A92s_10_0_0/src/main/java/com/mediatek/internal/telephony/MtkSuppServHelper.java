package com.mediatek.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsMmTelManager;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.imsphone.MtkImsPhone;
import com.mediatek.internal.telephony.uicc.MtkSIMRecords;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MtkSuppServHelper extends Handler {
    private static final String ACTION_SYSTEM_UPDATE_SUCCESSFUL = "com.mediatek.systemupdate.UPDATE_SUCCESSFUL";
    private static final boolean CFU_QUERY_WHEN_IMS_REGISTERED_DEFAULT = false;
    private static final String CFU_SETTING_ALWAYS_NOT_QUERY = "1";
    private static final String CFU_SETTING_ALWAYS_QUERY = "2";
    private static final String CFU_SETTING_DEFAULT = "0";
    private static final String CFU_SETTING_QUERY_IF_EFCFIS_INVALID = "3";
    private static final boolean DBG = true;
    private static final int EFCFIS_STATUS_INVALID = 3;
    private static final int EFCFIS_STATUS_NOT_READY = 0;
    private static final int EFCFIS_STATUS_VALID = 2;
    private static final int EVENT_CALL_FORWARDING_STATUS_FROM_MD = 6;
    private static final int EVENT_CARRIER_CONFIG_LOADED = 15;
    private static final int EVENT_CFU_STATUS_FROM_MD = 8;
    public static final int EVENT_CLEAN_CFU_STATUS = 16;
    private static final int EVENT_DATA_CONNECTION_ATTACHED = 2;
    private static final int EVENT_DATA_CONNECTION_DETACHED = 3;
    private static final int EVENT_GET_CALL_FORWARD_BY_GSM_DONE = 4;
    private static final int EVENT_GET_CALL_FORWARD_BY_IMS_DONE = 5;
    private static final int EVENT_GET_CALL_FORWARD_TIME_SLOT_BY_GSM_DONE = 10;
    private static final int EVENT_GET_CALL_FORWARD_TIME_SLOT_BY_IMS_DONE = 11;
    private static final int EVENT_ICCRECORDS_READY = 1;
    private static final int EVENT_ICC_CHANGED = 13;
    private static final int EVENT_QUERY_CFU_OVER_CS = 7;
    private static final int EVENT_QUERY_CFU_OVER_CS_AFTER_DATA_NOT_ATTACHED = 14;
    private static final int EVENT_REGISTERED_TO_NETWORK = 0;
    private static final int EVENT_SIM_RECORDS_LOADED = 12;
    private static final int EVENT_SS_RESET = 9;
    private static final String IMS_NOT_QUERY_YET = "1";
    private static final String IMS_NO_NEED_QUERY = "0";
    private static final String IMS_QUERY_DONE = "2";
    private static final String LOG_TAG = "SuppServHelper";
    private static final int QUERY_OVER_GSM = 0;
    private static final int QUERY_OVER_GSM_OVER_UT = 1;
    private static final int QUERY_OVER_IMS = 2;
    private static final boolean SDBG = (!SystemProperties.get("ro.build.type").equals(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER));
    private static final String SIM_CHANGED = "1";
    private static final String SIM_NO_CHANGED = "0";
    private static final int TASK_CLEAN_CFU_STATUS = 4;
    private static final int TASK_QUERY_CFU = 0;
    private static final int TASK_QUERY_CFU_OVER_GSM = 1;
    private static final int TASK_QUERY_CFU_OVER_IMS = 2;
    private static final int TASK_SET_CW_STATUS = 5;
    private static final int TASK_TIME_SLOT_FAILED = 3;
    private static final int TIMER_FOR_RETRY_QUERY_CFU = 20000;
    private static final int TIMER_FOR_SKIP_WAITING_CFU_STATUS_FROM_MD = 20000;
    private static final int TIMER_FOR_WAIT_DATA_ATTACHED = 20000;
    private static final boolean VDBG = SystemProperties.get("ro.build.type").equals("eng");
    private static final boolean WITHOUT_TIME_SLOT = false;
    private static final boolean WITH_TIME_SLOT = true;
    private boolean mAlwaysQueryDone = false;
    private AtomicBoolean mAttached = new AtomicBoolean(false);
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.MtkSuppServHelper.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED")) {
                MtkSuppServHelper.this.handleSubinfoUpdate();
            } else if (action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE")) {
                MtkSuppServHelper.this.setNeedGetCFU(true);
                MtkSuppServHelper.this.mSuppServTaskDriven.appendTask(new Task(0, false, "Radio capability done"));
            } else if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                boolean bAirplaneModeOn = intent.getBooleanExtra("state", false);
                MtkSuppServHelper mtkSuppServHelper = MtkSuppServHelper.this;
                mtkSuppServHelper.logd("ACTION_AIRPLANE_MODE_CHANGED, bAirplaneModeOn = " + bAirplaneModeOn);
                if (bAirplaneModeOn) {
                    MtkSuppServHelper.this.setNeedGetCFU(true);
                    if (MtkSuppServHelper.this.isResetCSFBStatusAfterFlightMode()) {
                        MtkSuppServHelper.this.mPhone.setCsFallbackStatus(0);
                    }
                }
            } else if (action.equals("android.intent.action.ACTION_SUPPLEMENTARY_SERVICE_UT_TEST")) {
                MtkSuppServHelper.this.logd("ACTION_SUPPLEMENTARY_SERVICE_UT_TEST");
                if (MtkSuppServHelper.this.isSupportSuppServUTTest()) {
                    MtkSuppServHelper.this.makeMtkSuppServUtTest(intent).run();
                }
            } else if (action.equals("android.telephony.action.SIM_APPLICATION_STATE_CHANGED")) {
                int simStatus = intent.getIntExtra("android.telephony.extra.SIM_STATE", 0);
                int subId = intent.getIntExtra("subscription", -1);
                MtkSuppServHelper mtkSuppServHelper2 = MtkSuppServHelper.this;
                mtkSuppServHelper2.logd("ACTION_SIM_APPLICATION_STATE_CHANGED: " + simStatus + ", subId: " + subId + ", CallForwardingFromSimRecords: " + MtkSuppServHelper.this.getCallForwardingFromSimRecords());
                if (10 == simStatus && subId == MtkSuppServHelper.this.mPhone.getSubId() && MtkSuppServHelper.this.getCallForwardingFromSimRecords() == 1) {
                    MtkSuppServHelper.this.logd("ACTION_SIM_APPLICATION_STATE_CHANGED, refresh CFU info.");
                    MtkSuppServHelper.this.mPhone.notifyCallForwardingIndicator();
                }
            } else if (action.equals(MtkSuppServHelper.ACTION_SYSTEM_UPDATE_SUCCESSFUL)) {
                MtkSuppServHelper.this.logd("ACTION_SYSTEM_UPDATE_SUCCESSFUL, sync CFU info.");
                MtkSuppServHelper.this.setNeedSyncSysPropToSIMforOTA(true);
                if (MtkSuppServHelper.this.syncSysPropToSIMforOTA()) {
                    MtkSuppServHelper.this.setNeedSyncSysPropToSIMforOTA(false);
                    int unused = MtkSuppServHelper.this.mNeeedSyncForOTA = 0;
                }
            }
        }
    };
    private int mCFUStatusFromMD = -1;
    private boolean mCarrierConfigLoaded = false;
    private Context mContext;
    private final AtomicReference<IccRecords> mIccRecords = new AtomicReference<>();
    /* access modifiers changed from: private */
    public ImsManager mImsManager = null;
    private final ImsManager.Connector mImsManagerConnector;
    private final ImsMmTelManager.RegistrationCallback mImsRegistrationCallback = new ImsMmTelManager.RegistrationCallback() {
        /* class com.mediatek.internal.telephony.MtkSuppServHelper.AnonymousClass3 */

        public void onRegistered(int imsRadioTech) {
            MtkSuppServHelper mtkSuppServHelper = MtkSuppServHelper.this;
            mtkSuppServHelper.logd("onImsRegistered imsRadioTech=" + imsRadioTech);
            if (!MtkSuppServHelper.this.isMDSupportIMSSuppServ()) {
                if (MtkSuppServHelper.this.mPhone.isOpTbcwWithCS()) {
                    MtkSuppServHelper.this.mPhone.setTbcwMode(3);
                    MtkSuppServHelper.this.mPhone.setTbcwToEnabledOnIfDisabled();
                } else {
                    MtkSuppServHelper.this.mPhone.setTbcwMode(1);
                    MtkSuppServHelper.this.mPhone.setTbcwToEnabledOnIfDisabled();
                }
            }
            if (MtkSuppServHelper.this.getCfuSetting().equals("1")) {
                MtkSuppServHelper.this.logd("onImsRegistered, no need to query CFU over IMS due to ALWAYS_NOT_QUERY");
            } else if (!MtkSuppServHelper.this.isNotMachineTest()) {
                MtkSuppServHelper.this.logd("onImsRegistered, no need to query CFU over IMS due to machine test");
            } else {
                MtkSuppServHelper.this.setNeedGetCFU(true);
                if (MtkSuppServHelper.this.getIMSQueryStatus()) {
                    MtkSuppServHelper.this.mSuppServTaskDriven.appendTask(new Task(2, "IMS state in service"));
                }
            }
        }
    };
    private MtkSuppServHelper mMtkSuppServHelper = null;
    private boolean mNeedGetCFU = true;
    private boolean mNeedGetCFUOverIms = false;
    /* access modifiers changed from: private */
    public int mNeeedSyncForOTA = -1;
    /* access modifiers changed from: private */
    public MtkGsmCdmaPhone mPhone = null;
    private boolean mSimRecordsLoaded = false;
    private boolean mSkipCFUStatusFromMD = false;
    /* access modifiers changed from: private */
    public SuppServTaskDriven mSuppServTaskDriven = null;
    private UiccController mUiccController = null;

    private class Task {
        private boolean mExtraBool = false;
        private int mExtraInt = -1;
        private String mExtraMsg = "";
        private int mTaskId = -1;

        public Task(int taskId, boolean b, String extraMsg) {
            this.mTaskId = taskId;
            this.mExtraBool = b;
            this.mExtraMsg = extraMsg;
        }

        public Task(int taskId, String extraMsg) {
            this.mTaskId = taskId;
            this.mExtraMsg = extraMsg;
        }

        public int getTaskId() {
            return this.mTaskId;
        }

        public int getExtraInt() {
            return this.mExtraInt;
        }

        public boolean getExtraBoolean() {
            return this.mExtraBool;
        }

        public String getExtraMsg() {
            return this.mExtraMsg;
        }

        public String toString() {
            return "Task ID: " + this.mTaskId + ", ExtraBool: " + this.mExtraBool + ", ExtraInt: " + this.mExtraInt + ", ExtraMsg: " + this.mExtraMsg;
        }
    }

    /* access modifiers changed from: private */
    public class SuppServTaskDriven extends Handler {
        private static final int EVENT_DONE = 0;
        private static final int EVENT_EXEC_NEXT = 1;
        private static final int STATE_DOING = 1;
        private static final int STATE_DONE = 2;
        private static final int STATE_NO_PENDING = 0;
        private ArrayList<Task> mPendingTask = new ArrayList<>();
        private int mState = 0;
        private Object mStateLock = new Object();
        private Object mTaskLock = new Object();

        public SuppServTaskDriven() {
        }

        public SuppServTaskDriven(Looper looper) {
            super(looper);
        }

        public void appendTask(Task task) {
            synchronized (this.mTaskLock) {
                this.mPendingTask.add(task);
            }
            obtainMessage(1).sendToTarget();
        }

        private int getState() {
            int i;
            synchronized (this.mStateLock) {
                i = this.mState;
            }
            return i;
        }

        private void setState(int state) {
            synchronized (this.mStateLock) {
                this.mState = state;
            }
        }

        private Task getCurrentPendingTask() {
            synchronized (this.mTaskLock) {
                if (this.mPendingTask.size() == 0) {
                    return null;
                }
                Task task = this.mPendingTask.get(0);
                return task;
            }
        }

        private void removePendingTask(int index) {
            synchronized (this.mTaskLock) {
                if (this.mPendingTask.size() > 0) {
                    this.mPendingTask.remove(index);
                    MtkSuppServHelper mtkSuppServHelper = MtkSuppServHelper.this;
                    mtkSuppServHelper.logd("removePendingTask remain mPendingTask: " + this.mPendingTask.size());
                }
            }
        }

        public void clearPendingTask() {
            synchronized (this.mTaskLock) {
                this.mPendingTask.clear();
            }
        }

        public void exec() {
            Task task = getCurrentPendingTask();
            if (task == null) {
                setState(0);
            } else if (getState() != 1) {
                setState(1);
                int taskId = task.getTaskId();
                MtkSuppServHelper.this.logd(task.toString());
                if (taskId == 0) {
                    MtkSuppServHelper.this.startHandleCFUQueryProcess(task.getExtraBoolean(), task.getExtraMsg());
                } else if (taskId == 1) {
                    MtkSuppServHelper.this.queryCallForwardStatusOverGSM();
                } else if (taskId == 2) {
                    MtkSuppServHelper.this.queryCallForwardStatusOverIMS();
                } else if (taskId == 3) {
                    MtkSuppServHelper.this.startCFUQuery(true);
                } else if (taskId == 4) {
                    MtkSuppServHelper.this.cleanCFUStatus();
                } else if (taskId != 5) {
                    MtkSuppServHelper.this.taskDone();
                } else {
                    if (!MtkSuppServHelper.this.isMDSupportIMSSuppServ()) {
                        MtkSuppServHelper.this.mPhone.setTbcwMode(0);
                        MtkSuppServHelper.this.mPhone.setSSPropertyThroughHidl(MtkSuppServHelper.this.mPhone.getPhoneId(), "persist.vendor.radio.terminal-based.cw", "disabled_tbcw");
                    }
                    MtkSuppServHelper.this.taskDone();
                }
            }
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                removePendingTask(0);
                setState(2);
            } else if (i != 1) {
                return;
            }
            exec();
        }

        private String stateToString(int state) {
            if (state == 0) {
                return "STATE_NO_PENDING";
            }
            if (state == 1) {
                return "STATE_DOING";
            }
            if (state != 2) {
                return "UNKNOWN_STATE";
            }
            return "STATE_DONE";
        }

        private String eventToString(int event) {
            if (event == 0) {
                return "EVENT_DONE";
            }
            if (event != 1) {
                return "UNKNOWN_EVENT";
            }
            return "EVENT_EXEC_NEXT";
        }
    }

    public MtkSuppServHelper(Context context, Phone phone) {
        this.mContext = context;
        this.mPhone = (MtkGsmCdmaPhone) phone;
        this.mImsManagerConnector = new ImsManager.Connector(this.mPhone.getContext(), this.mPhone.getPhoneId(), new ImsManager.Connector.Listener() {
            /* class com.mediatek.internal.telephony.MtkSuppServHelper.AnonymousClass1 */

            public void connectionReady(ImsManager manager) throws ImsException {
                ImsManager unused = MtkSuppServHelper.this.mImsManager = manager;
                MtkSuppServHelper.this.setImsCallback();
            }

            public void connectionUnavailable() {
                MtkSuppServHelper.this.unSetImsCallback();
            }
        });
        this.mImsManagerConnector.connect();
        registerEvent();
        registerBroadcastReceiver();
        logd("MtkSuppServHelper init done.");
    }

    public void init(Looper looper) {
        this.mSuppServTaskDriven = new SuppServTaskDriven(looper);
    }

    private boolean checkInitCriteria(StringBuilder criteriaFailReason) {
        String cfuSetting = getCfuSetting();
        if (!getNeedGetCFU()) {
            criteriaFailReason.append("No need to get CFU. (flag is false), ");
            return false;
        } else if (!isSubInfoReady()) {
            criteriaFailReason.append("SubInfo not ready, ");
            return false;
        } else if (!isIccCardMncMccAvailable(this.mPhone.getPhoneId())) {
            criteriaFailReason.append("MCC MNC not ready, ");
            return false;
        } else if (!isIccRecordsAvailable()) {
            criteriaFailReason.append("Icc record available, ");
            return false;
        } else if (!isVoiceInService()) {
            criteriaFailReason.append("Network is not registered, ");
            return false;
        } else if (!getSimRecordsLoaded()) {
            criteriaFailReason.append("Sim not loaded, ");
            return false;
        } else {
            if (cfuSetting.equals(CFU_SETTING_QUERY_IF_EFCFIS_INVALID)) {
                int efcfisStatus = checkEfCfis();
                logd("efcfisStatus: " + efcfisStatus);
                if (efcfisStatus == 2 || efcfisStatus == 0) {
                    criteriaFailReason.append("EfCfis in SIM is valid, no need to check or SIMRecords not ready.");
                    return false;
                }
            }
            criteriaFailReason.append("All Criteria ready.");
            return true;
        }
    }

    private void onUpdateIcc() {
        if (this.mUiccController != null) {
            IccRecords newIccRecords = getUiccRecords(1);
            if (newIccRecords == null && this.mPhone.getPhoneType() == 2) {
                newIccRecords = getUiccRecords(2);
            }
            IccRecords r = this.mIccRecords.get();
            if (newIccRecords == null) {
                logd("onUpdateIcc: newIccRecords is null");
            } else {
                logd("onUpdateIcc: newIccRecords is not null");
            }
            if (r != newIccRecords) {
                setSimRecordsLoaded(false);
                if (r != null) {
                    logi("Removing stale icc objects.");
                    r.unregisterForRecordsLoaded(this);
                    this.mIccRecords.set(null);
                } else {
                    logd("onUpdateIcc: mIccRecords is not null");
                }
                if (newIccRecords == null) {
                    logd("onUpdateIcc: Sim not ready.");
                } else if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
                    logi("New records found.");
                    this.mIccRecords.set(newIccRecords);
                    newIccRecords.registerForRecordsLoaded(this, 12, (Object) null);
                }
            }
        }
    }

    private IccRecords getUiccRecords(int appFamily) {
        return this.mUiccController.getIccRecords(this.mPhone.getPhoneId(), appFamily);
    }

    private boolean getSimRecordsLoaded() {
        logi("mSimRecordsLoaded: " + this.mSimRecordsLoaded);
        return this.mSimRecordsLoaded;
    }

    private void setSimRecordsLoaded(boolean value) {
        logi("Set mSimRecordsLoaded: " + value);
        this.mSimRecordsLoaded = value;
    }

    private boolean getCarrierConfigLoaded() {
        logi("mCarrierConfigLoaded: " + this.mCarrierConfigLoaded);
        return this.mCarrierConfigLoaded;
    }

    private void setCarrierConfigLoaded(boolean value) {
        logi("Set mCarrierConfigLoaded: " + value);
        this.mCarrierConfigLoaded = value;
    }

    /* access modifiers changed from: private */
    public void handleSubinfoUpdate() {
        if (isSubInfoReady()) {
            handleSuppServInit();
            if (isIccRecordsAvailable()) {
            }
        }
    }

    /* access modifiers changed from: private */
    public String getCfuSetting() {
        String defaultQueryCfuMode = getCFUQueryDefault();
        if (!TelephonyManager.from(this.mContext).isVoiceCapable()) {
            return SystemProperties.get("persist.vendor.radio.cfu.querytype", "1");
        }
        return SystemProperties.get("persist.vendor.radio.cfu.querytype", defaultQueryCfuMode);
    }

    private String getCFUQueryDefault() {
        return "0";
    }

    /* access modifiers changed from: private */
    public boolean isNotMachineTest() {
        String isTestSim = "0";
        boolean isRRMEnv = false;
        if (this.mPhone.getPhoneId() == 0) {
            isTestSim = SystemProperties.get("vendor.gsm.sim.ril.testsim", "0");
        } else if (this.mPhone.getPhoneId() == 1) {
            isTestSim = SystemProperties.get("vendor.gsm.sim.ril.testsim.2", "0");
        }
        String operatorNumeric = this.mPhone.getServiceState().getOperatorNumeric();
        if (operatorNumeric != null && operatorNumeric.equals("46602")) {
            isRRMEnv = true;
        }
        logd("isTestSIM : " + isTestSim + " isRRMEnv : " + isRRMEnv);
        if (!isTestSim.equals("0") || isRRMEnv) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void startHandleCFUQueryProcess(boolean forceQuery, String reason) {
        StringBuilder criteriaFailReason = new StringBuilder();
        boolean checkCriteria = checkInitCriteria(criteriaFailReason);
        String cfuSetting = getCfuSetting();
        logd("startHandleCFUQueryProcess(), forceQuery: " + forceQuery + ", CFU_KEY = " + cfuSetting + ", reason: " + reason + ", checkCriteria: " + checkCriteria + ", criteriaFailReason: " + criteriaFailReason.toString());
        if (!checkCriteria) {
            taskDone();
            return;
        }
        if (!isNotMachineTest()) {
            taskDone();
        } else if (cfuSetting.equals(CFU_SETTING_QUERY_IF_EFCFIS_INVALID) && getSIMChangedRecordFromSystemProp()) {
            SystemProperties.set("persist.vendor.radio.cfu.change." + this.mPhone.getPhoneId(), "0");
            startCFUQuery();
        } else if (!cfuSetting.equals(MtkGsmCdmaPhone.ACT_TYPE_UTRAN)) {
            taskDone();
        } else if (!this.mAlwaysQueryDone) {
            logd("Always query done: " + this.mAlwaysQueryDone);
            startCFUQuery();
            this.mAlwaysQueryDone = true;
        } else {
            taskDone();
        }
        setNeedGetCFU(false);
    }

    public void setAlwaysQueryDoneFlag(boolean flag) {
        logd("setAlwaysQueryDoneFlag: flag = " + flag);
        this.mAlwaysQueryDone = flag;
    }

    /* access modifiers changed from: private */
    public void setNeedGetCFU(boolean bNeed) {
        logd("setNeedGetCFU: " + bNeed);
        this.mNeedGetCFU = bNeed;
    }

    private boolean getNeedGetCFU() {
        return this.mNeedGetCFU;
    }

    /* access modifiers changed from: private */
    public void taskDone() {
        this.mSuppServTaskDriven.obtainMessage(0).sendToTarget();
    }

    private boolean isIccCardMncMccAvailable(int phoneId) {
        IccRecords iccRecords = UiccController.getInstance().getIccRecords(phoneId, 1);
        if (iccRecords == null) {
            return false;
        }
        if (iccRecords.getOperatorNumeric() != null) {
            return true;
        }
        return false;
    }

    private boolean isReceiveCFUStatusFromMD() {
        return (!this.mSkipCFUStatusFromMD && this.mCFUStatusFromMD != -1) ? true : true;
    }

    private boolean isIccRecordsAvailable() {
        if (this.mPhone.getIccRecords() != null) {
            return true;
        }
        return false;
    }

    private boolean isVoiceInService() {
        if (this.mPhone.mSST == null || this.mPhone.mSST.mSS == null || this.mPhone.mSST.mSS.getState() != 0) {
            return false;
        }
        return true;
    }

    private boolean isSubInfoReady() {
        SubscriptionManager subMgr = SubscriptionManager.from(this.mContext);
        SubscriptionInfo mySubInfo = null;
        if (subMgr != null) {
            mySubInfo = subMgr.getActiveSubscriptionInfo(this.mPhone.getSubId());
        }
        if (mySubInfo == null || mySubInfo.getIccId() == null) {
            return false;
        }
        return true;
    }

    private void handleSuppServInit() {
        String mySettingName = "persist.vendor.radio.cfu.iccid." + this.mPhone.getPhoneId();
        String oldIccId = SystemProperties.get(mySettingName, "");
        String cfuSetting = getCfuSetting();
        SubscriptionManager subMgr = SubscriptionManager.from(this.mContext);
        SubscriptionInfo mySubInfo = null;
        if (subMgr != null) {
            mySubInfo = subMgr.getActiveSubscriptionInfo(this.mPhone.getSubId());
        }
        if (mySubInfo == null) {
            this.mNeeedSyncForOTA = -1;
        }
        if (mySubInfo != null && !mySubInfo.getIccId().equals(oldIccId)) {
            logw("mySubId " + this.mPhone.getSubId() + " mySettingName " + Rlog.pii(SDBG, mySettingName) + " old iccid : " + Rlog.pii(SDBG, oldIccId) + " new iccid : " + Rlog.pii(SDBG, mySubInfo.getIccId()));
            SystemProperties.set(mySettingName, mySubInfo.getIccId());
            StringBuilder sb = new StringBuilder();
            sb.append("persist.vendor.radio.cfu.change.");
            sb.append(this.mPhone.getPhoneId());
            SystemProperties.set(sb.toString(), "1");
            if (isNeedSyncSysPropToSIMforOTA()) {
                setNeedSyncSysPropToSIMforOTA(false);
                this.mNeeedSyncForOTA = 0;
            }
            handleSuppServIfSimChanged();
        } else if (mySubInfo != null && mySubInfo.getIccId().equals(oldIccId)) {
            this.mNeeedSyncForOTA = 1;
            if (isNeedSyncSysPropToSIMforOTA()) {
                logd("ICC are the sames and trigger CFU status sync for OTA.");
                if (syncSysPropToSIMforOTA()) {
                    setNeedSyncSysPropToSIMforOTA(false);
                    this.mNeeedSyncForOTA = 0;
                }
            }
        } else if (cfuSetting.equals(MtkGsmCdmaPhone.ACT_TYPE_UTRAN)) {
            this.mSuppServTaskDriven.appendTask(new Task(0, false, "Always query CFU"));
        }
    }

    private void handleSuppServIfSimChanged() {
        if (getSIMChangedRecordFromSystemProp()) {
            reset();
            this.mPhone.setCsFallbackStatus(0);
            this.mSuppServTaskDriven.appendTask(new Task(5, false, "Sim Changed"));
            this.mAlwaysQueryDone = false;
            this.mPhone.saveTimeSlot(null);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            if (sp.getInt("clir_key" + this.mPhone.getPhoneId(), -1) != -1) {
                SharedPreferences.Editor editor = sp.edit();
                editor.remove("clir_key" + this.mPhone.getPhoneId());
                if (!editor.commit()) {
                    loge("failed to commit the removal of CLIR preference");
                }
            }
            if (needQueryCFUOverIms()) {
                TelephonyManager.setTelephonyProperty(this.mPhone.getPhoneId(), "persist.vendor.radio.cfu_over_ims", "1");
            } else {
                TelephonyManager.setTelephonyProperty(this.mPhone.getPhoneId(), "persist.vendor.radio.cfu_over_ims", "0");
            }
            this.mSuppServTaskDriven.appendTask(new Task(0, false, "Sim Changed"));
        }
    }

    /* access modifiers changed from: private */
    public boolean getIMSQueryStatus() {
        String status = "0";
        if (needQueryCFUOverIms()) {
            status = this.mPhone.getSystemProperty("persist.vendor.radio.cfu_over_ims", "0");
        }
        if (MtkGsmCdmaPhone.ACT_TYPE_UTRAN.equals(status) || "0".equals(status) || !"1".equals(status)) {
            return false;
        }
        return true;
    }

    private boolean getSIMChangedRecordFromSystemProp() {
        String isChanged = SystemProperties.get("persist.vendor.radio.cfu.change." + this.mPhone.getPhoneId(), "0");
        logd("getSIMChangedRecordFromSystemProp: " + isChanged);
        if (isChanged.equals("1")) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public int getCallForwardingFromSimRecords() {
        IccRecords r = this.mPhone.getIccRecords();
        if (r != null) {
            return r.getVoiceCallForwardingFlag();
        }
        return -1;
    }

    private void startCFUQuery() {
        startCFUQuery(false);
    }

    /* access modifiers changed from: private */
    public void startCFUQuery(boolean bForceNoTimeSlot) {
        if (isIMSRegistered()) {
            this.mPhone.getImsPhone();
            if (!isSupportCFUTimeSlot() || bForceNoTimeSlot) {
                getCallForwardingOption(2, false);
            } else {
                getCallForwardingOption(2, true);
            }
        } else {
            boolean bDataEnable = this.mPhone.getDataEnabledSettings().isDataEnabled();
            if (this.mPhone.getCsFallbackStatus() == 0 && this.mPhone.isGsmUtSupport() && bDataEnable) {
                logd("startCFUQuery, get data attached state : " + this.mAttached.get());
                if (!this.mAttached.get()) {
                    sendMessageDelayed(obtainMessage(14), 20000);
                    taskDone();
                } else if (!isSupportCFUTimeSlot() || bForceNoTimeSlot) {
                    getCallForwardingOption(1, false);
                } else {
                    getCallForwardingOption(1, true);
                }
            } else if (this.mPhone.isDuringVoLteCall() || this.mPhone.isDuringImsEccCall()) {
                logi("No need query CFU in CS domain due to during volte call and ims ecc call!");
                taskDone();
            } else if (isIMSRegistered() && isNoNeedToCSFBWhenIMSRegistered()) {
                taskDone();
            } else if (isNotSupportUtToCS()) {
                taskDone();
            } else {
                getCallForwardingOption(0, false);
            }
        }
    }

    private boolean isIMSRegistered() {
        Phone imsPhone = this.mPhone.getImsPhone();
        if (this.mPhone.getCsFallbackStatus() == 0 && imsPhone != null && imsPhone.getServiceState().getState() == 0) {
            return true;
        }
        return false;
    }

    public boolean getIMSRegistered() {
        return isIMSRegistered();
    }

    /* access modifiers changed from: private */
    public void setImsCallback() throws ImsException {
        try {
            this.mImsManager.addRegistrationCallback(this.mImsRegistrationCallback);
        } catch (ImsException ie) {
            logd("ImsManager addRegistrationCallback failed, " + ie.toString());
        }
    }

    /* access modifiers changed from: private */
    public void unSetImsCallback() {
        ImsManager imsManager = this.mImsManager;
        if (imsManager != null) {
            imsManager.removeRegistrationListener(this.mImsRegistrationCallback);
        }
    }

    private void registerEvent() {
        this.mPhone.getServiceStateTracker().registerForDataConnectionAttached(1, this, 2, (Object) null);
        this.mPhone.getServiceStateTracker().registerForDataConnectionDetached(1, this, 3, (Object) null);
        this.mPhone.getServiceStateTracker().registerForNetworkAttached(this, 0, (Object) null);
        this.mPhone.registerForSimRecordsLoaded(this, 12, null);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 13, (Object) null);
        this.mPhone.mCi.registerForCallForwardingInfo(this, 8, null);
    }

    private void unRegisterEvent() {
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionAttached(1, this);
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionDetached(1, this);
        this.mPhone.getServiceStateTracker().unregisterForNetworkAttached(this);
        this.mPhone.unregisterForSimRecordsLoaded(this);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.unregisterForIccChanged(this);
        this.mPhone.mCi.unregisterForCallForwardingInfo(this);
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        filter.addAction("android.telephony.action.SIM_APPLICATION_STATE_CHANGED");
        filter.addAction(ACTION_SYSTEM_UPDATE_SUCCESSFUL);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        IntentFilter utTestFilter = new IntentFilter();
        utTestFilter.addAction("android.intent.action.ACTION_SUPPLEMENTARY_SERVICE_UT_TEST");
        this.mContext.registerReceiver(this.mBroadcastReceiver, utTestFilter, "com.mediatek.permission.SUPPLEMENTARY_SERVICE_UT_TEST", null);
    }

    private void unRegisterBroadReceiver() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
    }

    public void dispose() {
        unRegisterEvent();
        unRegisterBroadReceiver();
        this.mImsManagerConnector.disconnect();
    }

    /* JADX INFO: Multiple debug info for r1v24 java.lang.Object: [D('cmdException' com.android.internal.telephony.CommandException), D('ar' android.os.AsyncResult)] */
    public void handleMessage(Message msg) {
        logd("handleMessage: " + toEventString(msg.what) + "(" + msg.what + ")");
        AsyncResult ar = (AsyncResult) msg.obj;
        switch (msg.what) {
            case 0:
            case 1:
                this.mSuppServTaskDriven.appendTask(new Task(0, false, toReasonString(msg.what)));
                return;
            case 2:
                this.mAttached.set(true);
                if (hasMessages(14)) {
                    logd("remove EVENT_QUERY_CFU_OVER_CS_AFTER_DATA_NOT_ATTACHED, and then start CFU query again");
                    removeMessages(14);
                    startCFUQuery();
                    return;
                }
                this.mSuppServTaskDriven.appendTask(new Task(0, false, toReasonString(msg.what)));
                return;
            case 3:
                this.mAttached.set(false);
                return;
            case 4:
                if (ar.exception == null) {
                    Message ret = this.mPhone.getCFCallbackMessage();
                    AsyncResult.forMessage(ret, ar.result, ar.exception);
                    ret.sendToTarget();
                }
                taskDone();
                return;
            case 5:
                if (isMDSupportIMSSuppServ()) {
                    Message ret2 = this.mPhone.getCFCallbackMessage();
                    AsyncResult.forMessage(ret2, ar.result, ar.exception);
                    ret2.sendToTarget();
                    this.mPhone.setSystemProperty("persist.vendor.radio.cfu_over_ims", MtkGsmCdmaPhone.ACT_TYPE_UTRAN);
                    taskDone();
                    return;
                }
                CommandException cmdException = null;
                if (ar.exception != null && (ar.exception instanceof CommandException)) {
                    cmdException = ar.exception;
                    logd("cmdException error:" + cmdException.getCommandError());
                }
                if (cmdException == null || !(cmdException.getCommandError() == CommandException.Error.OPERATION_NOT_ALLOWED || cmdException.getCommandError() == CommandException.Error.OEM_ERROR_3)) {
                    if (cmdException == null) {
                        Message ret3 = this.mPhone.getCFCallbackMessage();
                        AsyncResult.forMessage(ret3, ar.result, ar.exception);
                        ret3.sendToTarget();
                    }
                } else if (!isNotSupportUtToCS() && !isNoNeedToCSFBWhenIMSRegistered()) {
                    this.mSuppServTaskDriven.appendTask(new Task(1, false, toReasonString(msg.what)));
                }
                this.mPhone.setSystemProperty("persist.vendor.radio.cfu_over_ims", MtkGsmCdmaPhone.ACT_TYPE_UTRAN);
                taskDone();
                return;
            case 6:
            case 9:
            default:
                logd("Unhandled msg: " + msg.what);
                return;
            case 7:
                if (!isNotSupportUtToCS()) {
                    this.mSuppServTaskDriven.appendTask(new Task(1, "Query Cfu over CS"));
                    return;
                }
                return;
            case 8:
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2 != null && ar2.exception == null && ar2.result != null) {
                    int[] cfuResult = (int[]) ar2.result;
                    logd("handle EVENT_CFU_STATUS_FROM_MD:" + cfuResult[0]);
                    this.mCFUStatusFromMD = cfuResult[0];
                    return;
                }
                return;
            case 10:
                CommandException cmdException2 = null;
                if (ar.exception != null && (ar.exception instanceof CommandException)) {
                    cmdException2 = ar.exception;
                    logd("cmdException error:" + cmdException2.getCommandError());
                }
                if (msg.arg1 == 1 && cmdException2 != null && cmdException2.getCommandError() == CommandException.Error.REQUEST_NOT_SUPPORTED) {
                    this.mSuppServTaskDriven.appendTask(new Task(3, false, toReasonString(msg.what)));
                } else if (msg.arg1 != 1 || cmdException2 == null) {
                    Message ret4 = this.mPhone.getCFTimeSlotCallbackMessage();
                    AsyncResult.forMessage(ret4, ar.result, ar.exception);
                    ret4.sendToTarget();
                }
                taskDone();
                return;
            case 11:
                CommandException cmdException3 = null;
                if (ar.exception != null && (ar.exception instanceof CommandException)) {
                    cmdException3 = ar.exception;
                    logd("cmdException error:" + cmdException3.getCommandError());
                }
                if (msg.arg1 == 1 && cmdException3 != null && cmdException3.getCommandError() == CommandException.Error.REQUEST_NOT_SUPPORTED) {
                    this.mSuppServTaskDriven.appendTask(new Task(3, false, toReasonString(msg.what)));
                } else if (msg.arg1 != 1 || cmdException3 == null) {
                    Message ret5 = this.mPhone.getCFTimeSlotCallbackMessage();
                    AsyncResult.forMessage(ret5, ar.result, ar.exception);
                    ret5.sendToTarget();
                }
                this.mPhone.setSystemProperty("persist.vendor.radio.cfu_over_ims", MtkGsmCdmaPhone.ACT_TYPE_UTRAN);
                taskDone();
                return;
            case 12:
                setSimRecordsLoaded(true);
                if (isNeedSyncSysPropToSIMforOTA() && syncSysPropToSIMforOTA()) {
                    setNeedSyncSysPropToSIMforOTA(false);
                    this.mNeeedSyncForOTA = 0;
                }
                notifyCdmaCallForwardingIndicator();
                this.mSuppServTaskDriven.appendTask(new Task(0, false, toReasonString(msg.what)));
                return;
            case 13:
                onUpdateIcc();
                return;
            case 14:
                logd("Receive the event for query CFU over CS after data not attached");
                if (!isNotSupportUtToCS()) {
                    this.mSuppServTaskDriven.appendTask(new Task(1, "Query Cfu over CS"));
                    return;
                }
                return;
            case 15:
                setCarrierConfigLoaded(true);
                this.mSuppServTaskDriven.appendTask(new Task(0, false, toReasonString(msg.what)));
                return;
            case 16:
                logd("Receive EVENT_CLEAN_CFU_STATUS, SIM has disposed");
                this.mSuppServTaskDriven.appendTask(new Task(4, toReasonString(msg.what)));
                return;
        }
    }

    /* access modifiers changed from: private */
    public void queryCallForwardStatusOverGSM() {
        getCallForwardingOption(0, false);
    }

    /* access modifiers changed from: private */
    public void queryCallForwardStatusOverIMS() {
        if (!isIMSRegistered()) {
            taskDone();
        } else if (isSupportCFUTimeSlot()) {
            getCallForwardingOption(2, true);
        } else {
            getCallForwardingOption(2, false);
        }
    }

    private int checkEfCfis() {
        IccRecords r = this.mPhone.getIccRecords();
        if (r == null || !(r instanceof MtkSIMRecords)) {
            return 0;
        }
        if (((MtkSIMRecords) r).checkEfCfis()) {
            return 2;
        }
        return 3;
    }

    /* access modifiers changed from: private */
    public void setNeedSyncSysPropToSIMforOTA(boolean value) {
        TelephonyManager.setTelephonyProperty(this.mPhone.getPhoneId(), "persist.vendor.radio.cfu.sync_for_ota", value ? "1" : "0");
    }

    private boolean isNeedSyncSysPropToSIMforOTA() {
        if (TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "persist.vendor.radio.cfu.sync_for_ota", "0").equals("1")) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean syncSysPropToSIMforOTA() {
        int i = this.mNeeedSyncForOTA;
        if (i == 0) {
            logd("syncSysPropToSIMforOTA: No need to sync (sim change): " + this.mNeeedSyncForOTA);
            return true;
        } else if (i == -1) {
            logd("syncSysPropToSIMforOTA: No need to sync (unknown): " + this.mNeeedSyncForOTA);
            return false;
        } else if (!getSimRecordsLoaded()) {
            logd("syncSysPropToSIMforOTA: SIM not loaded.");
            return false;
        } else if (this.mCFUStatusFromMD == -1) {
            logd("syncSysPropToSIMforOTA: ECFU not receive yet.");
            return false;
        } else {
            int checkEfCfis = checkEfCfis();
            logd("syncSysPropToSIMforOTA: checkEfCfis = " + checkEfCfis);
            if (checkEfCfis == 0) {
                return false;
            }
            if (checkEfCfis != 2) {
                return checkEfCfis == 3 ? true : true;
            }
            boolean cfuStatus = this.mPhone.getCallForwardingIndicator();
            if (cfuStatus) {
                logd("syncSysPropToSIMforOTA: true from system preference.");
                this.mPhone.setVoiceCallForwardingFlag(1, cfuStatus, "");
            } else if (this.mCFUStatusFromMD == 1) {
                logd("syncSysPropToSIMforOTA: from MD.");
                this.mPhone.setVoiceCallForwardingFlag(1, true, "");
            }
            return true;
        }
    }

    private boolean needQueryCFUOverIms() {
        return false;
    }

    public void setIccRecordsReady() {
        obtainMessage(1).sendToTarget();
    }

    private void reset() {
        this.mNeedGetCFU = true;
        this.mSuppServTaskDriven.clearPendingTask();
    }

    private void getCallForwardingOption(int reason, boolean withTimeSlot) {
        MtkSuppServQueueHelper suppServQueueHelper = MtkSuppServManager.getSuppServQueueHelper();
        if (suppServQueueHelper == null) {
            if (reason == 0) {
                queryCallForwardingOption(reason, withTimeSlot, obtainMessage(4));
            } else if (reason != 1) {
                if (reason == 2) {
                    if (withTimeSlot != 0) {
                        queryCallForwardingOption(reason, withTimeSlot, obtainMessage(11, 1, 0, null));
                    } else {
                        queryCallForwardingOption(reason, withTimeSlot, obtainMessage(5, null));
                    }
                }
            } else if (withTimeSlot != 0) {
                if (isMDSupportIMSSuppServ()) {
                    queryCallForwardingOption(reason, withTimeSlot, obtainMessage(10));
                } else {
                    queryCallForwardingOption(reason, withTimeSlot, obtainMessage(10, 1, 0, null));
                }
            } else if (isMDSupportIMSSuppServ()) {
                queryCallForwardingOption(reason, withTimeSlot, obtainMessage(4));
            } else {
                queryCallForwardingOption(reason, withTimeSlot, obtainMessage(4, null));
            }
        } else if (reason == 0) {
            suppServQueueHelper.getCallForwardingOption(reason, withTimeSlot ? 1 : 0, obtainMessage(4), this.mPhone.getPhoneId());
        } else if (reason != 1) {
            if (reason == 2) {
                if (withTimeSlot != 0) {
                    suppServQueueHelper.getCallForwardingOption(reason, withTimeSlot, obtainMessage(11, 1, 0, null), this.mPhone.getPhoneId());
                } else {
                    suppServQueueHelper.getCallForwardingOption(reason, withTimeSlot, obtainMessage(5, null), this.mPhone.getPhoneId());
                }
            }
        } else if (withTimeSlot != 0) {
            if (isMDSupportIMSSuppServ()) {
                suppServQueueHelper.getCallForwardingOption(reason, withTimeSlot, obtainMessage(10), this.mPhone.getPhoneId());
            } else {
                suppServQueueHelper.getCallForwardingOption(reason, withTimeSlot, obtainMessage(10, 1, 0, null), this.mPhone.getPhoneId());
            }
        } else if (isMDSupportIMSSuppServ()) {
            suppServQueueHelper.getCallForwardingOption(reason, withTimeSlot, obtainMessage(4), this.mPhone.getPhoneId());
        } else {
            suppServQueueHelper.getCallForwardingOption(reason, withTimeSlot, obtainMessage(4, null), this.mPhone.getPhoneId());
        }
    }

    public void queryCallForwardingOption(int reason, boolean withTimeSlot, Message respCallback) {
        logd("queryCallForwardingOption, reason: " + reason + ", withTimeSlot: " + withTimeSlot);
        if (reason != 0) {
            if (reason != 1) {
                if (reason == 2) {
                    Phone imsPhone = this.mPhone.getImsPhone();
                    if (withTimeSlot) {
                        ((MtkImsPhone) imsPhone).getCallForwardInTimeSlot(0, respCallback);
                    } else {
                        ((MtkImsPhone) imsPhone).getCallForwardingOption(0, respCallback);
                    }
                }
            } else if (withTimeSlot) {
                if (isMDSupportIMSSuppServ()) {
                    this.mPhone.mMtkCi.queryCallForwardInTimeSlotStatus(0, 1, respCallback);
                } else {
                    this.mPhone.getMtkSSRequestDecisionMaker().queryCallForwardInTimeSlotStatus(0, 1, respCallback);
                }
            } else if (isMDSupportIMSSuppServ()) {
                this.mPhone.mCi.queryCallForwardStatus(0, 1, (String) null, respCallback);
            } else {
                this.mPhone.getMtkSSRequestDecisionMaker().queryCallForwardStatus(0, 1, null, respCallback);
            }
        } else if (isVoiceInService()) {
            this.mPhone.mCi.queryCallForwardStatus(0, 1, (String) null, respCallback);
        } else {
            AsyncResult.forMessage(respCallback, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
            respCallback.sendToTarget();
            taskDone();
        }
    }

    public void notifyCarrierConfigLoaded() {
        obtainMessage(15).sendToTarget();
    }

    /* access modifiers changed from: private */
    public boolean isSupportSuppServUTTest() {
        return SystemProperties.get("persist.vendor.ims_support").equals("1") && SystemProperties.get("persist.vendor.volte_support").equals("1") && this.mPhone.getPhoneId() == 0;
    }

    /* access modifiers changed from: private */
    public boolean isMDSupportIMSSuppServ() {
        if (SystemProperties.get("ro.vendor.md_auto_setup_ims").equals("1")) {
            return true;
        }
        return false;
    }

    private boolean isNotSupportUtToCS() {
        return this.mPhone.isNotSupportUtToCSforCFUQuery();
    }

    private boolean isNoNeedToCSFBWhenIMSRegistered() {
        return this.mPhone.isNoNeedToCSFBWhenIMSRegistered();
    }

    private boolean isSupportCFUTimeSlot() {
        return this.mPhone.isSupportCFUTimeSlot();
    }

    /* access modifiers changed from: private */
    public boolean isResetCSFBStatusAfterFlightMode() {
        return this.mPhone.isResetCSFBStatusAfterFlightMode();
    }

    /* access modifiers changed from: private */
    public MtkSuppServUtTest makeMtkSuppServUtTest(Intent intent) {
        return new MtkSuppServUtTest(this.mContext, intent, this.mPhone);
    }

    private void notifyCdmaCallForwardingIndicator() {
        if (this.mPhone.isGsmSsPrefer() && this.mPhone.getPhoneType() == 2) {
            this.mPhone.notifyCallForwardingIndicator();
        }
    }

    public String getXCAPErrorMessageFromSysProp(CommandException.Error error) {
        String propNamePrefix = "vendor.gsm.radio.ss.errormsg." + this.mPhone.getPhoneId();
        String fullErrorMsg = "";
        int idx = 0;
        String propValue = SystemProperties.get(propNamePrefix + "." + 0, "");
        while (!propValue.equals("")) {
            fullErrorMsg = fullErrorMsg + propValue;
            idx++;
            propValue = SystemProperties.get(propNamePrefix + "." + idx, "");
        }
        logd("fullErrorMsg: " + fullErrorMsg);
        if (AnonymousClass4.$SwitchMap$com$android$internal$telephony$CommandException$Error[error.ordinal()] != 1 || !fullErrorMsg.startsWith("409")) {
            return null;
        }
        String errorMsg = fullErrorMsg.substring("409".length() + 1);
        logd("errorMsg: " + errorMsg);
        return errorMsg;
    }

    /* renamed from: com.mediatek.internal.telephony.MtkSuppServHelper$4  reason: invalid class name */
    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$CommandException$Error = new int[CommandException.Error.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.OEM_ERROR_25.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void cleanCFUStatus() {
        if (this.mPhone != null) {
            setAlwaysQueryDoneFlag(false);
            this.mPhone.cleanCallForwardingIndicatorFromSharedPref();
            this.mPhone.notifyCallForwardingIndicatorWithoutCheckSimState();
        }
        taskDone();
    }

    private String toReasonString(int event) {
        if (event == 0) {
            return "CS in service";
        }
        if (event == 1) {
            return "ICCRecords ready";
        }
        if (event == 2) {
            return "Data Attached";
        }
        if (event == 12) {
            return "SIM records loaded";
        }
        if (event == 15) {
            return "Carrier config loaded";
        }
        if (event != 16) {
            return "Unknown reason, should not be here.";
        }
        return "Clean CFU status";
    }

    private String toEventString(int event) {
        switch (event) {
            case 0:
                return "EVENT_REGISTERED_TO_NETWORK";
            case 1:
                return "EVENT_ICCRECORDS_READY";
            case 2:
                return "EVENT_DATA_CONNECTION_ATTACHED";
            case 3:
                return "EVENT_DATA_CONNECTION_DETACHED";
            case 4:
                return "EVENT_GET_CALL_FORWARD_BY_GSM_DONE";
            case 5:
                return "EVENT_GET_CALL_FORWARD_BY_IMS_DONE";
            case 6:
                return "EVENT_CALL_FORWARDING_STATUS_FROM_MD";
            case 7:
                return "EVENT_QUERY_CFU_OVER_CS";
            case 8:
                return "EVENT_CFU_STATUS_FROM_MD";
            case 9:
                return "EVENT_SS_RESET";
            case 10:
                return "EVENT_GET_CALL_FORWARD_TIME_SLOT_BY_GSM_DONE";
            case 11:
                return "EVENT_GET_CALL_FORWARD_TIME_SLOT_BY_IMS_DONE";
            case 12:
                return "EVENT_SIM_RECORDS_LOADED";
            case 13:
                return "EVENT_ICC_CHANGED";
            case 14:
                return "EVENT_QUERY_CFU_OVER_CS_AFTER_DATA_NOT_ATTACHED";
            case 15:
                return "EVENT_CARRIER_CONFIG_LOADED";
            case 16:
                return "EVENT_CLEAN_CFU_STATUS";
            default:
                return "UNKNOWN_EVENT_ID";
        }
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void logw(String s) {
        Rlog.w(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void logi(String s) {
        Rlog.i(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    /* access modifiers changed from: private */
    public void logd(String s) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void logv(String s) {
        Rlog.v(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    public static String encryptString(String message) {
        byte[] textByte;
        Base64.Encoder encoder = Base64.getEncoder();
        try {
            textByte = message.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            textByte = null;
        }
        if (textByte == null) {
            return "";
        }
        return encoder.encodeToString(textByte);
    }
}
