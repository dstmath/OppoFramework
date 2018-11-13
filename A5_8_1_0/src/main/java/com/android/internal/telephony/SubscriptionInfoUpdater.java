package com.android.internal.telephony;

import android.app.ActivityManager;
import android.app.UserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.service.euicc.EuiccProfileInfo;
import android.service.euicc.GetEuiccProfileInfoListResult;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAccessRule;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import com.android.internal.telephony.euicc.EuiccController;
import com.android.internal.telephony.uicc.IccCardProxy;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.oppo.hypnus.HypnusManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionInfoUpdater extends Handler {
    private static final String ACTION_SUBINFO_STATE_CHANGE = "oppo.intent.action.SUBINFO_STATE_CHANGE";
    public static final String CURR_SUBID = "curr_subid";
    private static final int EVENT_GET_NETWORK_SELECTION_MODE_DONE = 2;
    private static final int EVENT_REFRESH_EMBEDDED_SUBSCRIPTIONS = 9;
    private static final int EVENT_SIM_ABSENT = 4;
    private static final int EVENT_SIM_IO_ERROR = 6;
    private static final int EVENT_SIM_LOADED = 3;
    private static final int EVENT_SIM_LOCKED = 5;
    protected static final int EVENT_SIM_LOCKED_QUERY_ICCID_DONE = 1;
    private static final int EVENT_SIM_RESTRICTED = 8;
    private static final int EVENT_SIM_UNKNOWN = 7;
    private static final String ICCID_STRING_FOR_NO_SIM = "";
    private static final String INTENT_KEY_SIM_STATE = "simstate";
    private static final String INTENT_KEY_SLOT_ID = "slotid";
    private static final String INTENT_KEY_SUB_ID = "subid";
    private static final String INTENT_VALUE_SIM_CARD_TYPE = "CARDTYPE";
    private static final String INTENT_VALUE_SIM_PLUG_OUT = "PLUGOUT";
    private static final String LOG_TAG = "SubscriptionInfoUpdater";
    private static final int PROJECT_SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    public static final int SIM_CHANGED = -1;
    public static final int SIM_NEW = -2;
    public static final int SIM_NOT_CHANGE = 0;
    public static final int SIM_NOT_INSERT = -99;
    public static final int SIM_REPOSITION = -3;
    public static final int STATUS_NO_SIM_INSERTED = 0;
    public static final int STATUS_SIM1_INSERTED = 1;
    public static final int STATUS_SIM2_INSERTED = 2;
    public static final int STATUS_SIM3_INSERTED = 4;
    public static final int STATUS_SIM4_INSERTED = 8;
    private static Context mContext = null;
    private static HypnusManager mHM = null;
    private static boolean mHasRequestLogBack = false;
    protected static String[] mIccId = new String[PROJECT_SIM_NUM];
    private static int[] mInsertSimState = new int[PROJECT_SIM_NUM];
    private static OemDeviceLock mOemLock = null;
    private static Phone[] mPhone;
    private static boolean sHasInSertLockSim = false;
    private CarrierServiceBindHelper mCarrierServiceBindHelper;
    private int mCurrentlyActiveUserId;
    private EuiccManager mEuiccManager;
    private boolean mHotPlugOut = false;
    private IPackageManager mPackageManager;
    private boolean mShutdown = false;
    private SubscriptionManager mSubscriptionManager = null;
    private final BroadcastReceiver sReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            SubscriptionInfoUpdater.this.logd("[Receiver]+");
            String action = intent.getAction();
            SubscriptionInfoUpdater.this.logd("Action: " + action);
            if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                SubscriptionInfoUpdater.this.logd("ACTION_SHUTDOWN come in");
                SubscriptionInfoUpdater.this.mShutdown = true;
            } else if (action.equals(SubscriptionInfoUpdater.ACTION_SUBINFO_STATE_CHANGE)) {
                if (SubscriptionInfoUpdater.INTENT_VALUE_SIM_PLUG_OUT.equalsIgnoreCase(intent.getStringExtra(SubscriptionInfoUpdater.INTENT_KEY_SIM_STATE))) {
                    SubscriptionInfoUpdater.this.logd("plug out,do not switch dds");
                    SubscriptionInfoUpdater.this.mHotPlugOut = true;
                }
            }
            if (action.equals("android.intent.action.SIM_STATE_CHANGED") || (action.equals(IccCardProxy.ACTION_INTERNAL_SIM_STATE_CHANGED) ^ 1) == 0) {
                int slotIndex = intent.getIntExtra("phone", -1);
                SubscriptionInfoUpdater.this.logd("slotIndex: " + slotIndex);
                if (SubscriptionManager.isValidSlotIndex(slotIndex)) {
                    String simStatus = intent.getStringExtra("ss");
                    SubscriptionInfoUpdater.this.logd("simStatus: " + simStatus);
                    if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                        if ("ABSENT".equals(simStatus)) {
                            SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(4, slotIndex, -1));
                            if (!(SubscriptionInfoUpdater.mPhone[slotIndex].getModemUuId() == null || ("".equals(SubscriptionInfoUpdater.mPhone[slotIndex].getModemUuId()) ^ 1) == 0)) {
                                SubscriptionInfoUpdater.this.cleanMccProperties(Integer.valueOf(SubscriptionInfoUpdater.mPhone[slotIndex].getModemUuId()).intValue());
                            }
                            SubscriptionInfoUpdater.mPhone[slotIndex].setOppoNeedNotifyStatus(true);
                        } else if ("UNKNOWN".equals(simStatus)) {
                            SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(7, slotIndex, -1));
                        } else if ("CARD_IO_ERROR".equals(simStatus)) {
                            SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(6, slotIndex, -1));
                        } else if ("CARD_RESTRICTED".equals(simStatus)) {
                            SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(8, slotIndex, -1));
                        } else if ("NOT_READY".equals(simStatus)) {
                            SubscriptionInfoUpdater.this.sendEmptyMessage(9);
                            if (!(SubscriptionInfoUpdater.mPhone[slotIndex] == null || SubscriptionInfoUpdater.mPhone[slotIndex].getServiceStateTracker() == null || SubscriptionInfoUpdater.mPhone[slotIndex].getServiceStateTracker().getCombinedRegState() != 0)) {
                                SubscriptionInfoUpdater.this.logd("Sim not ready, do pollstate");
                                SubscriptionInfoUpdater.mPhone[slotIndex].getServiceStateTracker().pollState();
                            }
                        } else {
                            SubscriptionInfoUpdater.this.logd("Ignoring simStatus: " + simStatus);
                        }
                    } else if (action.equals(IccCardProxy.ACTION_INTERNAL_SIM_STATE_CHANGED)) {
                        if ("LOCKED".equals(simStatus)) {
                            SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(5, slotIndex, -1, intent.getStringExtra("reason")));
                        } else if ("LOADED".equals(simStatus)) {
                            SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(3, slotIndex, -1));
                        } else {
                            SubscriptionInfoUpdater.this.logd("Ignoring simStatus: " + simStatus);
                        }
                    }
                    SubscriptionInfoUpdater.this.logd("[Receiver]-");
                    return;
                }
                SubscriptionInfoUpdater.this.logd("ACTION_SIM_STATE_CHANGED contains invalid slotIndex: " + slotIndex);
            }
        }
    };

    protected static class QueryIccIdUserObj {
        public String reason;
        public int slotId;

        QueryIccIdUserObj(String reason, int slotId) {
            this.reason = reason;
            this.slotId = slotId;
        }
    }

    public SubscriptionInfoUpdater(Looper looper, Context context, Phone[] phone, CommandsInterface[] ci) {
        super(looper);
        logd("Constructor invoked");
        mContext = context;
        mPhone = phone;
        this.mSubscriptionManager = SubscriptionManager.from(mContext);
        this.mEuiccManager = (EuiccManager) mContext.getSystemService("euicc_service");
        this.mPackageManager = Stub.asInterface(ServiceManager.getService("package"));
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction(IccCardProxy.ACTION_INTERNAL_SIM_STATE_CHANGED);
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        intentFilter.addAction(ACTION_SUBINFO_STATE_CHANGE);
        mContext.registerReceiver(this.sReceiver, intentFilter);
        this.mCarrierServiceBindHelper = new CarrierServiceBindHelper(mContext);
        initializeCarrierApps();
        if (OemConstant.EXP_VERSION) {
            mOemLock = OemDeviceLock.getInstance(mContext);
        }
    }

    private void initializeCarrierApps() {
        this.mCurrentlyActiveUserId = 0;
        try {
            ActivityManager.getService().registerUserSwitchObserver(new UserSwitchObserver() {
                public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
                    SubscriptionInfoUpdater.this.mCurrentlyActiveUserId = newUserId;
                    CarrierAppUtils.disableCarrierAppsUntilPrivileged(SubscriptionInfoUpdater.mContext.getOpPackageName(), SubscriptionInfoUpdater.this.mPackageManager, TelephonyManager.getDefault(), SubscriptionInfoUpdater.mContext.getContentResolver(), SubscriptionInfoUpdater.this.mCurrentlyActiveUserId);
                    if (reply != null) {
                        try {
                            reply.sendResult(null);
                        } catch (RemoteException e) {
                        }
                    }
                }
            }, LOG_TAG);
            this.mCurrentlyActiveUserId = ActivityManager.getService().getCurrentUser().id;
        } catch (RemoteException e) {
            logd("Couldn't get current user ID; guessing it's 0: " + e.getMessage());
        }
        CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), mContext.getContentResolver(), this.mCurrentlyActiveUserId);
    }

    protected boolean isAllIccIdQueryDone() {
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (mIccId[i] == null) {
                logd("Wait for SIM" + (i + 1) + " IccId");
                return false;
            }
        }
        logd("All IccIds query complete");
        return true;
    }

    public void setDisplayNameForNewSub(String newSubName, int subId, int newNameSource) {
        SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
        if (subInfo != null) {
            int oldNameSource = subInfo.getNameSource();
            CharSequence oldSubName = subInfo.getDisplayName();
            logd("[setDisplayNameForNewSub] subId = " + subInfo.getSubscriptionId() + ", oldSimName = " + oldSubName + ", oldNameSource = " + oldNameSource + ", newSubName = " + newSubName + ", newNameSource = " + newNameSource);
            if (oldSubName == null || ((oldNameSource == 0 && newSubName != null) || !(oldNameSource != 1 || newSubName == null || (newSubName.equals(oldSubName) ^ 1) == 0))) {
                this.mSubscriptionManager.setDisplayName(newSubName, subInfo.getSubscriptionId(), (long) newNameSource);
                return;
            }
            return;
        }
        logd("SUB" + (subId + 1) + " SubInfo not created yet");
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case 1:
                ar = msg.obj;
                QueryIccIdUserObj uObj = ar.userObj;
                int slotId = uObj.slotId;
                logd("handleMessage : <EVENT_SIM_LOCKED_QUERY_ICCID_DONE> SIM" + (slotId + 1));
                if (ar.exception != null) {
                    mIccId[slotId] = "";
                    logd("Query IccId fail: " + ar.exception);
                } else if (ar.result != null) {
                    byte[] data = ar.result;
                    mIccId[slotId] = IccUtils.bcdToString(data, 0, data.length);
                } else {
                    logd("Null ar");
                    mIccId[slotId] = "";
                }
                logd("sIccId[" + slotId + "] = " + mIccId[slotId]);
                TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService("phone");
                mTelephonyManager.setSimOperatorNameForPhone(slotId, "");
                if (isAllIccIdQueryDone()) {
                    updateSubscriptionInfoByIccId();
                }
                if (mIccId[slotId] == "" && mTelephonyManager.getSimState(slotId) == 1) {
                    logd("Card is ABSENT");
                } else {
                    broadcastSimStateChanged(slotId, "LOCKED", uObj.reason);
                }
                if (!"".equals(mIccId[slotId])) {
                    updateCarrierServices(slotId, "LOCKED");
                    return;
                }
                return;
            case 2:
                ar = (AsyncResult) msg.obj;
                Integer slotId2 = ar.userObj;
                if (ar.exception != null || ar.result == null) {
                    logd("EVENT_GET_NETWORK_SELECTION_MODE_DONE: error getting network mode.");
                    return;
                } else if (ar.result[0] == 1) {
                    mPhone[slotId2.intValue()].setNetworkSelectionModeAutomatic(null);
                    return;
                } else {
                    return;
                }
            case 3:
                handleSimLoaded(msg.arg1);
                return;
            case 4:
                handleSimAbsentOrError(msg.arg1, "ABSENT");
                IccPhoneBookInterfaceManager iccPbkIntMgr = mPhone[msg.arg1].getIccPhoneBookInterfaceManager();
                if (iccPbkIntMgr != null) {
                    iccPbkIntMgr.resetSimNameLength();
                    return;
                }
                return;
            case 5:
                handleSimLocked(msg.arg1, (String) msg.obj);
                return;
            case 6:
                handleSimAbsentOrError(msg.arg1, "CARD_IO_ERROR");
                return;
            case 7:
                updateCarrierServices(msg.arg1, "UNKNOWN");
                return;
            case 8:
                updateCarrierServices(msg.arg1, "CARD_RESTRICTED");
                return;
            case 9:
                if (updateEmbeddedSubscriptions()) {
                    SubscriptionController.getInstance().notifySubscriptionInfoChanged();
                }
                if (msg.obj != null) {
                    ((Runnable) msg.obj).run();
                    return;
                }
                return;
            default:
                logd("Unknown msg:" + msg.what);
                return;
        }
    }

    void requestEmbeddedSubscriptionInfoListRefresh(Runnable callback) {
        sendMessage(obtainMessage(9, callback));
    }

    protected void handleSimLocked(int slotId, String reason) {
        IccFileHandler fileHandler;
        if (mIccId[slotId] != null && mIccId[slotId].equals("")) {
            logd("SIM" + (slotId + 1) + " hot plug in");
            mIccId[slotId] = null;
        }
        if (mPhone[slotId].getIccCard() == null) {
            fileHandler = null;
        } else {
            fileHandler = mPhone[slotId].getIccCard().getIccFileHandler();
        }
        if (fileHandler != null) {
            String iccId = mIccId[slotId];
            SubscriptionController mSubscriptionController = SubscriptionController.getInstance();
            boolean isSoftSimEnable = false;
            if (mSubscriptionController != null) {
                isSoftSimEnable = mSubscriptionController.isHasSoftSimCard();
            } else {
                logd("[handleSimLocked],mSubscriptionController is null");
            }
            logd("[handleSimLocked],isSoftSimEnable:" + isSoftSimEnable + " ,SlotID: " + slotId);
            if (iccId == null || isSoftSimEnable) {
                logd("Querying IccId");
                fileHandler.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(1, new QueryIccIdUserObj(reason, slotId)));
                return;
            }
            logd("NOT Querying IccId its already set sIccid[" + slotId + "]=" + iccId);
            updateCarrierServices(slotId, "LOCKED");
            broadcastSimStateChanged(slotId, "LOCKED", reason);
            return;
        }
        logd("sFh[" + slotId + "] is null, ignore");
    }

    protected void handleModemLogPostback(int cardType) {
        if (!(mHasRequestLogBack || cardType == 9 || cardType == 4 || cardType == -1)) {
            OppoModemLogManager.openModemLogPostBack(mContext);
        }
    }

    protected void handleSimLoaded(int slotId) {
        logd("handleSimLoaded: slotId: " + slotId);
        int loadedSlotId = slotId;
        IccRecords records = mPhone[slotId].getIccCard().getIccRecords();
        if (records == null) {
            logd("handleSimLoaded: IccRecords null");
        } else if (records.getIccId() == null) {
            logd("onRecieve: IccID null");
        } else {
            mIccId[slotId] = records.getIccId();
            if (isAllIccIdQueryDone()) {
                updateSubscriptionInfoByIccId();
                for (int subId : this.mSubscriptionManager.getActiveSubscriptionIdList()) {
                    slotId = SubscriptionController.getInstance().getPhoneId(subId);
                    String operator = mPhone[slotId].getOperatorNumeric();
                    if (operator == null || (TextUtils.isEmpty(operator) ^ 1) == 0) {
                        logd("EVENT_RECORDS_LOADED Operator name is null");
                    } else {
                        if (subId == SubscriptionController.getInstance().getDefaultSubId()) {
                            MccTable.updateMccMncConfiguration(mContext, operator, false);
                        }
                        SubscriptionController.getInstance().setMccMnc(operator, subId);
                    }
                    if (!(mPhone == null || mPhone[slotId] == null || !mPhone[slotId].getIccRecordsLoaded())) {
                        String[] imsi = mPhone[slotId].getLteCdmaImsi(slotId);
                        String imsiData = "" != imsi[0] ? imsi[0] : imsi[1];
                        updateAllSimInfoForLoad(subId, imsiData, mIccId[slotId]);
                        handleModemLogPostback(OemConstant.getCardType(imsiData, mIccId[slotId]));
                    }
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                    if (sp.getInt(CURR_SUBID + slotId, -1) != subId) {
                        int networkType = RILConstants.PREFERRED_NETWORK_MODE;
                        try {
                            networkType = Global.getInt(mContext.getContentResolver(), "preferred_network_mode" + subId);
                        } catch (SettingNotFoundException e) {
                            logd("Settings Exception reading value at subid for  Settings.Global.PREFERRED_NETWORK_MODE");
                            try {
                                networkType = TelephonyManager.getIntAtIndex(mContext.getContentResolver(), "preferred_network_mode", slotId);
                            } catch (SettingNotFoundException e2) {
                                Rlog.e(LOG_TAG, "Settings Exception Reading Value At Index for Settings.Global.PREFERRED_NETWORK_MODE");
                            }
                        }
                        mPhone[slotId].setPreferredNetworkType(networkType, null);
                        Global.putInt(mPhone[slotId].getContext().getContentResolver(), "preferred_network_mode" + subId, networkType);
                        mPhone[slotId].getNetworkSelectionMode(obtainMessage(2, new Integer(slotId)));
                        Editor editor = sp.edit();
                        editor.putInt(CURR_SUBID + slotId, subId);
                        editor.apply();
                    }
                }
            }
            if (OemConstant.EXP_VERSION) {
                OemDeviceLock oemDeviceLock = mOemLock;
                if (OemDeviceLock.IS_OP_LOCK && OemDeviceLock.getDeviceLockStatus()) {
                    if (allSimActived()) {
                        updateOperatorDeviceLock(true, true);
                    } else {
                        updateOperatorDeviceLock(false, true);
                    }
                }
            }
            CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), mContext.getContentResolver(), this.mCurrentlyActiveUserId);
            broadcastSimStateChanged(loadedSlotId, "LOADED", null);
            updateCarrierServices(loadedSlotId, "LOADED");
        }
    }

    private void updateCarrierServices(int slotId, String simState) {
        ((CarrierConfigManager) mContext.getSystemService("carrier_config")).updateConfigForPhoneId(slotId, simState);
        this.mCarrierServiceBindHelper.updateForPhoneId(slotId, simState);
    }

    protected void handleSimAbsentOrError(int slotId, String simState) {
        if (!(mIccId[slotId] == null || (mIccId[slotId].equals("") ^ 1) == 0)) {
            logd("SIM" + (slotId + 1) + " hot plug out or error.");
        }
        mIccId[slotId] = "";
        if (isAllIccIdQueryDone()) {
            updateSubscriptionInfoByIccId();
        }
        updateCarrierServices(slotId, simState);
        if (simState == "ABSENT") {
            TelephonyManager tm = TelephonyManager.from(mContext);
            if (tm != null) {
                tm.setSimOperatorNameForPhone(slotId, "");
            }
        }
    }

    private void handleSimAbsent(int slotId) {
        if (!(mIccId[slotId] == null || (mIccId[slotId].equals("") ^ 1) == 0)) {
            logd("SIM" + (slotId + 1) + " hot plug out or error.");
        }
        mIccId[slotId] = "";
        if (isAllIccIdQueryDone()) {
            updateSubscriptionInfoByIccId();
        }
        updateCarrierServices(slotId, "ABSENT");
    }

    private void handleSimError(int slotId) {
        if (!(mIccId[slotId] == null || (mIccId[slotId].equals("") ^ 1) == 0)) {
            logd("SIM" + (slotId + 1) + " Error ");
        }
        mIccId[slotId] = "";
        if (isAllIccIdQueryDone()) {
            updateSubscriptionInfoByIccId();
        }
        updateCarrierServices(slotId, "CARD_IO_ERROR");
    }

    protected synchronized void updateSubscriptionInfoByIccId() {
        int i;
        ContentValues value;
        logd("updateSubscriptionInfoByIccId:+ Start");
        if (mHM == null) {
            mHM = HypnusManager.getHypnusManager();
        }
        if (mHM != null) {
            mHM.hypnusSetAction(12, 1000);
            logd("hypnusSetAction(),start");
        }
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            mInsertSimState[i] = 0;
        }
        int insertedSimCount = PROJECT_SIM_NUM;
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            if ("".equals(mIccId[i])) {
                insertedSimCount--;
                mInsertSimState[i] = -99;
            }
        }
        logd("insertedSimCount = " + insertedSimCount);
        if (insertedSimCount == 0) {
            this.mHotPlugOut = false;
        }
        if (SubscriptionController.getInstance().getActiveSubIdList().length > insertedSimCount) {
            SubscriptionController.getInstance().clearSubInfo();
        }
        i = 0;
        while (i < PROJECT_SIM_NUM) {
            if (mInsertSimState[i] != -99) {
                int index = 2;
                int j = i + 1;
                while (j < PROJECT_SIM_NUM) {
                    if (mInsertSimState[j] == 0 && mIccId[i].equals(mIccId[j])) {
                        mInsertSimState[i] = 1;
                        mInsertSimState[j] = index;
                        index++;
                    }
                    j++;
                }
            }
            i++;
        }
        ContentResolver contentResolver = mContext.getContentResolver();
        String[] oldIccId = new String[PROJECT_SIM_NUM];
        i = 0;
        while (i < PROJECT_SIM_NUM) {
            oldIccId[i] = null;
            List<SubscriptionInfo> oldSubInfo = SubscriptionController.getInstance().getSubInfoUsingSlotIndexWithCheck(i, false, mContext.getOpPackageName());
            if (oldSubInfo == null || oldSubInfo.size() <= 0) {
                if (mInsertSimState[i] == 0) {
                    mInsertSimState[i] = -1;
                }
                oldIccId[i] = "";
                logd("updateSubscriptionInfoByIccId: No SIM in slot " + i + " last time");
            } else {
                oldIccId[i] = ((SubscriptionInfo) oldSubInfo.get(0)).getIccId();
                logd("updateSubscriptionInfoByIccId: oldSubId = " + ((SubscriptionInfo) oldSubInfo.get(0)).getSubscriptionId());
                if (mInsertSimState[i] == 0 && (mIccId[i].equals(oldIccId[i]) ^ 1) != 0) {
                    mInsertSimState[i] = -1;
                }
                if (mInsertSimState[i] != 0) {
                    value = new ContentValues(1);
                    value.put("sim_id", Integer.valueOf(-1));
                    contentResolver.update(SubscriptionManager.CONTENT_URI, value, "_id=" + Integer.toString(((SubscriptionInfo) oldSubInfo.get(0)).getSubscriptionId()), null);
                    SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
                }
            }
            i++;
        }
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            logd("updateSubscriptionInfoByIccId: oldIccId[" + i + "] = " + oldIccId[i] + ", sIccId[" + i + "] = " + mIccId[i]);
        }
        int nNewCardCount = 0;
        int nNewSimStatus = 0;
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            if (mInsertSimState[i] == -99) {
                logd("updateSubscriptionInfoByIccId: No SIM inserted in slot " + i + " this time");
            } else {
                if (mInsertSimState[i] > 0) {
                    this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[i] + Integer.toString(mInsertSimState[i]), i);
                    logd("SUB" + (i + 1) + " has invalid IccId");
                } else {
                    this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[i], i);
                }
                if (isNewSim(mIccId[i], oldIccId)) {
                    nNewCardCount++;
                    switch (i) {
                        case 0:
                            nNewSimStatus |= 1;
                            break;
                        case 1:
                            nNewSimStatus |= 2;
                            break;
                        case 2:
                            nNewSimStatus |= 4;
                            break;
                    }
                    mInsertSimState[i] = -2;
                }
            }
        }
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            if (mInsertSimState[i] == -1) {
                mInsertSimState[i] = -3;
            }
            logd("updateSubscriptionInfoByIccId: sInsertSimState[" + i + "] = " + mInsertSimState[i]);
        }
        List<SubscriptionInfo> subInfos = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        int nSubCount = subInfos == null ? 0 : subInfos.size();
        logd("updateSubscriptionInfoByIccId: nSubCount = " + nSubCount);
        for (i = 0; i < nSubCount; i++) {
            SubscriptionInfo temp = (SubscriptionInfo) subInfos.get(i);
            String msisdn = TelephonyManager.from(mContext).getLine1Number(temp.getSubscriptionId());
            if (msisdn != null) {
                value = new ContentValues(1);
                value.put("number", msisdn);
                contentResolver.update(SubscriptionManager.CONTENT_URI, value, "_id=" + Integer.toString(temp.getSubscriptionId()), null);
                SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
            }
        }
        if (!(this.mShutdown || (this.mHotPlugOut ^ 1) == 0 || insertedSimCount == 0)) {
            this.mSubscriptionManager.setDefaultDataSubId(calculateDataSubId());
        }
        if (OemConstant.EXP_VERSION) {
            OemDeviceLock oemDeviceLock = mOemLock;
            if (OemDeviceLock.IS_OP_LOCK && OemDeviceLock.getDeviceLockStatus()) {
                oemDeviceLock = mOemLock;
                if (OemDeviceLock.isSimBindingCompleted()) {
                    updateDeviceLockUI();
                    sHasInSertLockSim = hasInsertBindingSimCard();
                    updateOperatorDeviceLockStatus(true, false);
                }
            }
        }
        updateEmbeddedSubscriptions();
        SubscriptionController.getInstance().notifySubscriptionInfoChanged();
        logd("updateSubscriptionInfoByIccId:- SubscriptionInfo update complete");
    }

    public boolean updateEmbeddedSubscriptions() {
        if (!this.mEuiccManager.isEnabled()) {
            return false;
        }
        GetEuiccProfileInfoListResult result = EuiccController.get().blockingGetEuiccProfileInfoList();
        if (result == null) {
            return false;
        }
        EuiccProfileInfo[] embeddedProfiles;
        int i;
        ContentValues values;
        if (result.result == 0) {
            embeddedProfiles = result.profiles;
        } else {
            logd("updatedEmbeddedSubscriptions: error " + result.result + " listing profiles");
            embeddedProfiles = new EuiccProfileInfo[0];
        }
        boolean isRemovable = result.isRemovable;
        String[] embeddedIccids = new String[embeddedProfiles.length];
        for (i = 0; i < embeddedProfiles.length; i++) {
            embeddedIccids[i] = embeddedProfiles[i].iccid;
        }
        boolean hasChanges = false;
        List<SubscriptionInfo> existingSubscriptions = SubscriptionController.getInstance().getSubscriptionInfoListForEmbeddedSubscriptionUpdate(embeddedIccids, isRemovable);
        ContentResolver contentResolver = mContext.getContentResolver();
        int i2 = 0;
        int length = embeddedProfiles.length;
        while (true) {
            int i3 = i2;
            if (i3 >= length) {
                break;
            }
            byte[] bArr;
            EuiccProfileInfo embeddedProfile = embeddedProfiles[i3];
            int index = findSubscriptionInfoForIccid(existingSubscriptions, embeddedProfile.iccid);
            if (index < 0) {
                SubscriptionController.getInstance().insertEmptySubInfoRecord(embeddedProfile.iccid, -1);
            } else {
                existingSubscriptions.remove(index);
            }
            values = new ContentValues();
            values.put("is_embedded", Integer.valueOf(1));
            String str = "access_rules";
            if (embeddedProfile.accessRules == null) {
                bArr = null;
            } else {
                bArr = UiccAccessRule.encodeRules(embeddedProfile.accessRules);
            }
            values.put(str, bArr);
            values.put("is_removable", Boolean.valueOf(isRemovable));
            values.put("display_name", embeddedProfile.nickname);
            values.put("name_source", Integer.valueOf(2));
            hasChanges = true;
            contentResolver.update(SubscriptionManager.CONTENT_URI, values, "icc_id=\"" + embeddedProfile.iccid + "\"", null);
            SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
            i2 = i3 + 1;
        }
        if (!existingSubscriptions.isEmpty()) {
            List<String> iccidsToRemove = new ArrayList();
            for (i = 0; i < existingSubscriptions.size(); i++) {
                SubscriptionInfo info = (SubscriptionInfo) existingSubscriptions.get(i);
                if (info.isEmbedded()) {
                    iccidsToRemove.add("\"" + info.getIccId() + "\"");
                }
            }
            String whereClause = "icc_id IN (" + TextUtils.join(",", iccidsToRemove) + ")";
            values = new ContentValues();
            values.put("is_embedded", Integer.valueOf(0));
            hasChanges = true;
            contentResolver.update(SubscriptionManager.CONTENT_URI, values, whereClause, null);
            SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
        }
        return hasChanges;
    }

    private static int findSubscriptionInfoForIccid(List<SubscriptionInfo> list, String iccid) {
        for (int i = 0; i < list.size(); i++) {
            if (TextUtils.equals(iccid, ((SubscriptionInfo) list.get(i)).getIccId())) {
                return i;
            }
        }
        return -1;
    }

    private boolean isNewSim(String iccId, String[] oldIccId) {
        boolean newSim = true;
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (iccId.equals(oldIccId[i])) {
                newSim = false;
                break;
            }
        }
        logd("newSim = " + newSim);
        return newSim;
    }

    private void broadcastSimStateChanged(int slotId, String state, String reason) {
        Intent i = new Intent("android.intent.action.SIM_STATE_CHANGED");
        i.addFlags(67108864);
        i.putExtra("phoneName", "Phone");
        i.putExtra("ss", state);
        i.putExtra("reason", reason);
        SubscriptionManager.putPhoneIdAndSubIdExtra(i, slotId);
        logd("Broadcasting intent ACTION_SIM_STATE_CHANGED  state " + state + " reason " + null + " for mCardIndex : " + slotId);
        IntentBroadcaster.getInstance().broadcastStickyIntent(i, slotId);
    }

    public void dispose() {
        logd("[dispose]");
        mContext.unregisterReceiver(this.sReceiver);
    }

    private void logd(String message) {
        Rlog.d(LOG_TAG, message);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("SubscriptionInfoUpdater:");
        this.mCarrierServiceBindHelper.dump(fd, pw, args);
    }

    public void broadcastSubInfoUpdateIntent(String slotid, String subid, String simstate) {
        Intent intent = new Intent(ACTION_SUBINFO_STATE_CHANGE);
        intent.putExtra(INTENT_KEY_SLOT_ID, slotid);
        intent.putExtra(INTENT_KEY_SUB_ID, subid);
        intent.putExtra(INTENT_KEY_SIM_STATE, simstate);
        logd("Broadcasting intent ACTION_SUBINFO_STATE_CHANGE slotid:" + slotid + " simstate:" + simstate + " subid:" + subid);
        mContext.sendBroadcast(intent);
    }

    private void updateAllSimInfoForLoad(int subId, String imsiData, String iccidData) {
        if (mContext == null) {
            logd("updateAllSimInfoForLoad error: -1");
            return;
        }
        ContentResolver contentResolver = mContext.getContentResolver();
        TelephonyManager tm = TelephonyManager.from(mContext);
        if (contentResolver != null) {
            ContentValues value = new ContentValues();
            if (tm != null) {
                String msisdn = tm.getLine1NumberForSubscriber(subId);
                if (msisdn != null) {
                    value.put("number", msisdn);
                    logd("msisdn = " + msisdn);
                } else {
                    logd("msisdn = null");
                }
            }
            SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
            if (!(subInfo == null || subInfo.getNameSource() == 2)) {
                String nameToSet = "";
                String simCarrierName = "";
                String imsi = "";
                if (tm != null) {
                    simCarrierName = tm.getSimOperatorName(subId);
                    imsi = tm.getSubscriberId(subId);
                }
                SubscriptionManager subscriptionManager = this.mSubscriptionManager;
                int slotId = SubscriptionManager.getSlotIndex(subId);
                nameToSet = SubscriptionController.getCarrierName(mContext, simCarrierName, imsi, iccidData, slotId);
                if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US")) {
                    if (!TextUtils.isEmpty(subInfo.getDisplayName())) {
                        nameToSet = subInfo.getDisplayName().toString();
                    } else if (SystemProperties.getInt("gsm.vsim.slotid", -1) == slotId) {
                        logd("It's red tea,sim name = " + nameToSet);
                    } else {
                        nameToSet = SubscriptionController.getInstance().getExportSimDefaultName(slotId);
                    }
                }
                logd("sim name = " + nameToSet);
                value.put("display_name", nameToSet);
                value.put("name_source", Integer.valueOf(-1));
            }
            value.put("color", Integer.valueOf(OemConstant.getCardType(imsiData, iccidData)));
            int result = contentResolver.update(SubscriptionManager.CONTENT_URI, value, "_id=" + Integer.toString(subId), null);
            SubscriptionController.getInstance().refreshCachedActiveSubscriptionInfoList();
            logd("update result = " + result);
            broadcastSubInfoUpdateIntent("-1", Integer.toString(subId), INTENT_VALUE_SIM_CARD_TYPE);
            SubscriptionController.getInstance().notifySubscriptionInfoChanged();
        } else {
            logd("update fial,contentResolver = null");
        }
    }

    public void cleanMccProperties(int stackId) {
        String sysMcc = SystemProperties.get("android.telephony.mcc_change", "");
        String sysMcc2 = SystemProperties.get("android.telephony.mcc_change2", "");
        if (!TextUtils.isEmpty(sysMcc) && !TextUtils.isEmpty(sysMcc2)) {
            logd("cleanMccProperties stackId:" + stackId + " sysMcc:" + sysMcc + " sysMcc2:" + sysMcc2);
            if (stackId == 0) {
                SystemProperties.set("android.telephony.mcc_change", "");
            } else {
                SystemProperties.set("android.telephony.mcc_change2", "");
            }
        }
    }

    public int calculateDataSubId() {
        SubscriptionManager subscriptionManager = this.mSubscriptionManager;
        int oldDatasubId = SubscriptionManager.getDefaultDataSubscriptionId();
        int calDataSubId = -1;
        int primarySlot = Global.getInt(mContext.getContentResolver(), SubscriptionController.OPPO_MULTI_SIM_NETWORK_PRIMARY_SLOT, 0);
        logd("calculateDataSubId oldDatasubId:" + oldDatasubId + ", primarySlot:" + primarySlot);
        SubscriptionController subController = SubscriptionController.getInstance();
        if (subController == null) {
            return oldDatasubId;
        }
        List<SubscriptionInfo> subList = subController.getActiveSubscriptionInfoList(mContext.getOpPackageName());
        if (subList != null) {
            int activeSimRef = 0;
            for (SubscriptionInfo si : subList) {
                if (subController.isCurrentSubActive(si.getSimSlotIndex())) {
                    activeSimRef++;
                    if (si.getSimSlotIndex() == primarySlot) {
                        calDataSubId = si.getSubscriptionId();
                        logd("calculateDataSubId get subId:" + calDataSubId + " from primarySlot:" + primarySlot);
                        break;
                    } else if (-1 == calDataSubId) {
                        calDataSubId = si.getSubscriptionId();
                    }
                }
            }
            if (activeSimRef == 0 && 1 == subList.size()) {
                for (SubscriptionInfo si2 : subList) {
                    if (si2.getSimSlotIndex() == primarySlot) {
                        calDataSubId = si2.getSubscriptionId();
                        logd("calculateDataSubId activeSimRef == 0, calDataSubId:" + calDataSubId);
                        break;
                    }
                }
            }
            if (calDataSubId == -1) {
                for (SubscriptionInfo si22 : subList) {
                    if (si22.getSimSlotIndex() == primarySlot) {
                        calDataSubId = si22.getSubscriptionId();
                        break;
                    }
                    calDataSubId = si22.getSubscriptionId();
                }
            }
        }
        if (calDataSubId == -1) {
            calDataSubId = oldDatasubId;
        }
        logd("calculateDataSubId return calDataSubId:" + calDataSubId);
        return calDataSubId;
    }

    private void updateDeviceLockUI() {
        boolean[] isSimInsert = new boolean[2];
        int slot = 0;
        while (slot < TelephonyManager.getDefault().getPhoneCount()) {
            if (mIccId[slot] == null || (mIccId[slot].equals("") ^ 1) == 0) {
                isSimInsert[slot] = false;
            } else {
                isSimInsert[slot] = true;
            }
            slot++;
        }
        if (!isSimInsert[0] && (isSimInsert[1] ^ 1) != 0) {
            OemDeviceLock oemDeviceLock = mOemLock;
            OemDeviceLock.setDeviceLockedForPhone(0, true);
            oemDeviceLock = mOemLock;
            OemDeviceLock.setDeviceLockedForPhone(1, true);
            oemDeviceLock = mOemLock;
            OemDeviceLock.setSimLoadedForPhone(true);
            oemDeviceLock = mOemLock;
            OemDeviceLock.setSimInsertForPhone(isSimInsert);
            oemDeviceLock = mOemLock;
            OemDeviceLock.notifyDeviceLocked(false);
        }
    }

    private boolean hasInsertBindingSimCard() {
        boolean[] lock = new boolean[4];
        int slot = 0;
        while (slot < TelephonyManager.getDefault().getPhoneCount()) {
            if (!(mIccId[slot] == null || (mIccId[slot].equals("") ^ 1) == 0)) {
                OemDeviceLock oemDeviceLock;
                if (mIccId[slot].length() >= 15) {
                    String simOperator = mIccId[slot].substring(0, 6);
                    oemDeviceLock = mOemLock;
                    lock = OemDeviceLock.isNeedAllowedOperator(simOperator, mIccId[slot], slot, false);
                }
                if (lock[0] && lock[1] && lock[2]) {
                    Rlog.d(LOG_TAG, "has insert locked sim card");
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(slot, false);
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedSlot(slot);
                    return true;
                }
            }
            slot++;
        }
        return false;
    }

    private void updateOperatorDeviceLock(boolean simAllActived, boolean checkGid1OrSpn) {
        boolean[] isSimInsert = new boolean[2];
        OemDeviceLock oemDeviceLock = mOemLock;
        if (!OemDeviceLock.isSimBindingCompleted()) {
            firstBindingDeviceLock(checkGid1OrSpn);
        }
        updateOperatorDeviceLockStatus(simAllActived, checkGid1OrSpn);
    }

    private void firstBindingDeviceLock(boolean checkGid1OrSpn) {
        boolean[] lock = new boolean[4];
        String simOperator = null;
        int slot = 0;
        while (slot < TelephonyManager.getDefault().getPhoneCount()) {
            if (!(mIccId[slot] == null || (mIccId[slot].equals("") ^ 1) == 0)) {
                OemDeviceLock oemDeviceLock;
                if (mIccId[slot].length() >= 15) {
                    simOperator = mIccId[slot].substring(0, 6);
                    oemDeviceLock = mOemLock;
                    lock = OemDeviceLock.initOperatorDeviceLock(simOperator, mIccId[slot], slot, true);
                }
                boolean z = (lock[0] && lock[1]) ? lock[2] : false;
                Rlog.d(LOG_TAG, "firstBindingDeviceLock,success" + z + ",slotId = " + slot + ",simOperator = " + simOperator + ",mIccId[" + slot + "] = " + mIccId[slot]);
                if (z) {
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(slot, false);
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedSlot(slot);
                    if (lock[3]) {
                        Rlog.d(LOG_TAG, "has binding operator success");
                        return;
                    }
                } else if (!(lock[0] || !lock[1] || lock[3])) {
                    Rlog.d(LOG_TAG, "firstBindingDeviceLock,first init locked fail");
                }
            }
            slot++;
        }
    }

    private void updateOperatorDeviceLockStatus(boolean allSimActived, boolean checkGid1OrSpn) {
        OemDeviceLock oemDeviceLock;
        boolean[] lock = new boolean[4];
        boolean[] isSimInsert = new boolean[2];
        int insertLockedSim = 0;
        int currentSlot = -1;
        int slot = 0;
        while (slot < TelephonyManager.getDefault().getPhoneCount()) {
            if (!(mIccId[slot] == null || (mIccId[slot].equals("") ^ 1) == 0)) {
                String simOperator = null;
                if (mIccId[slot].length() >= 15) {
                    simOperator = mIccId[slot].substring(0, 6);
                    oemDeviceLock = mOemLock;
                    lock = OemDeviceLock.isNeedAllowedOperator(simOperator, mIccId[slot], slot, checkGid1OrSpn);
                }
                isSimInsert[slot] = true;
                Rlog.d(LOG_TAG, "updateOperatorDeviceLock,slot = " + slot + ",lock[0] = " + lock[0] + ",lock[1]  = " + lock[1] + ",lock[2] = " + lock[2] + ",simOperator = " + simOperator);
                if (lock[0] && lock[1] && lock[2]) {
                    Rlog.d(LOG_TAG, "has insert locked sim card");
                    insertLockedSim = 1;
                    currentSlot = slot;
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(slot, false);
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedSlot(slot);
                }
            }
            slot++;
        }
        if (!(allSimActived || (insertLockedSim ^ 1) == 0)) {
            insertLockedSim = sHasInSertLockSim;
            oemDeviceLock = mOemLock;
            currentSlot = OemDeviceLock.getDeviceLockedSlot();
        }
        int anotherSlot = currentSlot == 0 ? 1 : 0;
        if (insertLockedSim == 0 || currentSlot == -1) {
            oemDeviceLock = mOemLock;
            if (OemDeviceLock.isNeedAllowedOperator("", "", 0, false)[0]) {
                Rlog.d(LOG_TAG, "updateOperatorDeviceLock, has init locked,but not insert simcard");
            } else {
                Rlog.d(LOG_TAG, "updateOperatorDeviceLock has not init locked");
            }
            oemDeviceLock = mOemLock;
            OemDeviceLock.setDeviceLockedForPhone(0, true);
            oemDeviceLock = mOemLock;
            OemDeviceLock.setDeviceLockedForPhone(1, true);
        } else if (mIccId[anotherSlot] == null || (mIccId[anotherSlot].equals("") ^ 1) == 0) {
            isSimInsert[anotherSlot] = false;
            oemDeviceLock = mOemLock;
            OemDeviceLock.setDeviceLockedForPhone(anotherSlot, true);
        } else {
            isSimInsert[anotherSlot] = true;
            if (mIccId[anotherSlot].length() >= 15) {
                oemDeviceLock = mOemLock;
                if (OemDeviceLock.isAllowSimCheck(anotherSlot, mIccId[anotherSlot].substring(0, 6), true)) {
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(anotherSlot, false);
                } else {
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(anotherSlot, true);
                }
            } else {
                oemDeviceLock = mOemLock;
                OemDeviceLock.setDeviceLockedForPhone(anotherSlot, true);
            }
        }
        oemDeviceLock = mOemLock;
        OemDeviceLock.setSimLoadedForPhone(true);
        oemDeviceLock = mOemLock;
        OemDeviceLock.setSimInsertForPhone(isSimInsert);
        oemDeviceLock = mOemLock;
        OemDeviceLock.notifyDeviceLocked(false);
        oemDeviceLock = mOemLock;
        OemDeviceLock.notifyUpdateDataCapacity(mPhone, isSimInsert);
        oemDeviceLock = mOemLock;
        OemDeviceLock.updateServiceState(isSimInsert);
    }

    private boolean allSimActived() {
        if (currentInsertSimCount() > SubscriptionController.getInstance().getActiveSubInfoCount(getClass().getPackage().getName())) {
            return false;
        }
        return true;
    }

    private int currentInsertSimCount() {
        for (int slot = 0; slot < TelephonyManager.getDefault().getPhoneCount(); slot++) {
            if (mPhone[slot].getIccCard().hasIccCard()) {
                int i = 0 + 1;
            }
        }
        return 0;
    }
}
