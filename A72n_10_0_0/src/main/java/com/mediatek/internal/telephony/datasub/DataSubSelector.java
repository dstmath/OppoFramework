package com.mediatek.internal.telephony.datasub;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.mediatek.internal.telephony.MtkSubscriptionController;
import com.mediatek.internal.telephony.MtkSubscriptionManager;
import com.mediatek.internal.telephony.OpTelephonyCustomizationFactoryBase;
import com.mediatek.internal.telephony.OpTelephonyCustomizationUtils;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.util.List;

public class DataSubSelector {
    private static String ACTION_BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";
    private static String ACTION_SUBSIDY_LOCK_STATE_CHANGE = "com.mediatek.subsidy_lock.state_change";
    private static final boolean DBG = true;
    private static final String LOG_TAG = "DSSelector";
    private static final boolean USER_BUILD = TextUtils.equals(Build.TYPE, DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER);
    private static IDataSubSelectorOPExt mDataSubSelectorOPExt = null;
    private static String mOperatorSpec;
    private static DataSubSelector sDataSubSelector = null;
    private boolean mAirplaneModeOn = false;
    protected final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.datasub.DataSubSelector.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                DataSubSelector dataSubSelector = DataSubSelector.this;
                dataSubSelector.log("onReceive: action=" + action);
                if (action.equals("android.telephony.action.SIM_APPLICATION_STATE_CHANGED")) {
                    DataSubSelector.this.handleSimStateChanged(intent);
                } else if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                    int nDefaultDataSubId = intent.getIntExtra("subscription", -1);
                    DataSubSelector dataSubSelector2 = DataSubSelector.this;
                    dataSubSelector2.log("nDefaultDataSubId: " + nDefaultDataSubId);
                    DataSubSelector.this.handleDefaultDataChanged(intent);
                } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                    DataSubSelector.this.mAirplaneModeOn = intent.getBooleanExtra("state", false);
                    DataSubSelector dataSubSelector3 = DataSubSelector.this;
                    dataSubSelector3.log("ACTION_AIRPLANE_MODE_CHANGED, enabled = " + DataSubSelector.this.mAirplaneModeOn);
                    if (!DataSubSelector.this.mAirplaneModeOn) {
                        if (DataSubSelector.this.mIsNeedWaitAirplaneModeOff) {
                            DataSubSelector.this.mIsNeedWaitAirplaneModeOff = false;
                            DataSubSelector.this.handleAirPlaneModeOff(intent);
                        }
                        if (DataSubSelector.this.mIsNeedWaitAirplaneModeOffRoaming) {
                            DataSubSelector.this.mIsNeedWaitAirplaneModeOffRoaming = false;
                        }
                    }
                } else if (action.equals("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED")) {
                    DataSubSelector.this.handleSubinfoRecordUpdated(intent);
                } else if ("com.mediatek.intent.action.LOCATED_PLMN_CHANGED".equals(action)) {
                    DataSubSelector.this.handlePlmnChanged(intent);
                } else if (action.equals("com.mediatek.phone.ACTION_SIM_SLOT_LOCK_POLICY_INFORMATION")) {
                    DataSubSelector.this.handleSimMeLock(intent);
                } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE") && RadioCapabilitySwitchUtil.isSubsidyLockForOmSupported()) {
                    DataSubSelector.this.log("DataSubSelector receive CONNECTIVITY_ACTION");
                    DataSubSelector.this.handleConnectivityAction();
                } else if (action.equals(DataSubSelector.ACTION_SUBSIDY_LOCK_STATE_CHANGE) && RadioCapabilitySwitchUtil.isSubsidyLockForOmSupported()) {
                    DataSubSelector.this.log("DataSubSelector receive ACTION_SUBSIDY_LOCK_STATE_CHANGE");
                    DataSubSelector.this.handleSubsidyLockStateAction(intent);
                } else if (action.equals(DataSubSelector.ACTION_BOOT_COMPLETE) && RadioCapabilitySwitchUtil.isSubsidyLockForOmSupported()) {
                    DataSubSelector.this.log("DataSubSelector receive ACTION_BOOT_COMPLETE");
                    DataSubSelector.this.handleBootCompleteAction();
                }
            }
        }
    };
    private CapabilitySwitch mCapabilitySwitch = null;
    private Context mContext = null;
    private Intent mIntent = null;
    private boolean mIsInRoaming = false;
    private boolean mIsNeedPreCheck = true;
    private boolean mIsNeedWaitAirplaneModeOff = false;
    private boolean mIsNeedWaitAirplaneModeOffRoaming = false;
    private boolean mIsWaitIccid = false;
    private int mPhoneNum;
    private ContentObserver mPrefNetworkModeObserver = new ContentObserver(new Handler()) {
        /* class com.mediatek.internal.telephony.datasub.DataSubSelector.AnonymousClass2 */

        public void onChange(boolean selfChange) {
            DataSubSelector.this.log("mPrefNetworkModeObserver, changed");
            DataSubSelector.this.handlePrefNetworkModeChanged();
        }
    };
    private Handler mProtocolHandler;
    private ISimSwitchForDSSExt mSimSwitchForDSSExt = null;
    private OpTelephonyCustomizationFactoryBase mTelephonyCustomizationFactory = null;
    private UpdateNWTypeHandler mUpdateNWTypeHandler = new UpdateNWTypeHandler();

    public static DataSubSelector makeDataSubSelector(Context context, int phoneNum) {
        if (sDataSubSelector == null) {
            sDataSubSelector = new DataSubSelector(context, phoneNum);
        }
        return sDataSubSelector;
    }

    public static IDataSubSelectorOPExt getDataSubSelectorOpExt() {
        if (sDataSubSelector != null) {
            return mDataSubSelectorOPExt;
        }
        loge("DataSubSelector not init yet!");
        return null;
    }

    private DataSubSelector(Context context, int phoneNum) {
        log("DataSubSelector is created");
        this.mPhoneNum = phoneNum;
        mOperatorSpec = SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, "OM");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telephony.action.SIM_APPLICATION_STATE_CHANGED");
        filter.addAction("com.mediatek.intent.action.LOCATED_PLMN_CHANGED");
        filter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        filter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction(ACTION_SUBSIDY_LOCK_STATE_CHANGE);
        filter.addAction(ACTION_BOOT_COMPLETE);
        if (MtkTelephonyManagerEx.getDefault().getSimLockPolicy() != 0) {
            filter.addAction("com.mediatek.phone.ACTION_SIM_SLOT_LOCK_POLICY_INFORMATION");
        }
        context.registerReceiver(this.mBroadcastReceiver, filter);
        this.mContext = context;
        initOpDataSubSelector(context);
        if (mDataSubSelectorOPExt == null) {
            mDataSubSelectorOPExt = new DataSubSelectorOpExt(context);
        }
        initSimSwitchForDSS(context);
        if (this.mSimSwitchForDSSExt == null) {
            this.mSimSwitchForDSSExt = new SimSwitchForDSSExt(context);
        }
        this.mCapabilitySwitch = CapabilitySwitch.getInstance(context, this);
        this.mSimSwitchForDSSExt.init(this);
        mDataSubSelectorOPExt.init(this, this.mSimSwitchForDSSExt);
        registerPrefNetworkModeObserver();
    }

    private void initOpDataSubSelector(Context context) {
        try {
            this.mTelephonyCustomizationFactory = OpTelephonyCustomizationUtils.getOpFactory(context);
            mDataSubSelectorOPExt = this.mTelephonyCustomizationFactory.makeDataSubSelectorOPExt(context);
        } catch (Exception e) {
            log("mDataSubSelectorOPExt init fail");
            e.printStackTrace();
        }
    }

    private void initSimSwitchForDSS(Context context) {
        try {
            this.mTelephonyCustomizationFactory = OpTelephonyCustomizationUtils.getOpFactory(context);
            this.mSimSwitchForDSSExt = this.mTelephonyCustomizationFactory.makeSimSwitchForDSSOPExt(context);
        } catch (Exception e) {
            log("mSimSwitchForDSSExt init fail");
            e.printStackTrace();
        }
    }

    private void registerPrefNetworkModeObserver() {
        for (int i = 0; i < this.mPhoneNum; i++) {
            Phone curPhone = PhoneFactory.getPhone(i);
            ContentResolver contentResolver = curPhone.getContext().getContentResolver();
            contentResolver.registerContentObserver(Settings.Global.getUriFor("preferred_network_mode" + curPhone.getSubId()), true, this.mPrefNetworkModeObserver);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePrefNetworkModeChanged() {
        int defDataPhoneId;
        int defDataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
        if (defDataSubId != -1 && (defDataPhoneId = SubscriptionManager.getPhoneId(defDataSubId)) >= 0) {
            this.mCapabilitySwitch.setCapability(defDataPhoneId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDefaultDataChanged(Intent intent) {
        mDataSubSelectorOPExt.handleDefaultDataChanged(intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSubinfoRecordUpdated(Intent intent) {
        mDataSubSelectorOPExt.handleSubinfoRecordUpdated(intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSimStateChanged(Intent intent) {
        mDataSubSelectorOPExt.handleSimStateChanged(intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAirPlaneModeOff(Intent intent) {
        mDataSubSelectorOPExt.handleAirPlaneModeOff(intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePlmnChanged(Intent intent) {
        mDataSubSelectorOPExt.handlePlmnChanged(intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConnectivityAction() {
        mDataSubSelectorOPExt.handleConnectivityAction();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSubsidyLockStateAction(Intent intent) {
        mDataSubSelectorOPExt.handleSubsidyLockStateAction(intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBootCompleteAction() {
        mDataSubSelectorOPExt.handleBootCompleteAction();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSimMeLock(Intent intent) {
        mDataSubSelectorOPExt.handleSimMeLock(intent);
    }

    public boolean getAirPlaneModeOn() {
        return this.mAirplaneModeOn;
    }

    public boolean getIsWaitIccid() {
        return this.mIsWaitIccid;
    }

    public void setIsWaitIccid(boolean isWaitIccid) {
        this.mIsWaitIccid = isWaitIccid;
    }

    public boolean getIsNeedPreCheck() {
        return this.mIsNeedPreCheck;
    }

    public void setIsNeedPreCheck(boolean isNeedPreCheck) {
        this.mIsNeedPreCheck = isNeedPreCheck;
    }

    public void setDataEnabled(int phoneId, boolean enable) {
        log("oppo modify: do not set data enabled as false, return.");
    }

    public void setDefaultData(int phoneId) {
        SubscriptionController.getInstance();
        int sub = MtkSubscriptionManager.getSubIdUsingPhoneId(phoneId);
        int currSub = SubscriptionManager.getDefaultDataSubscriptionId();
        log("setDefaultDataSubId: " + sub + ", current default sub:" + currSub);
        if (sub == currSub || sub < -1) {
            log("setDefaultDataSubId: default data unchanged");
        } else {
            MtkSubscriptionController.getMtkInstance().setDefaultDataSubIdWithoutCapabilitySwitch(sub);
        }
    }

    public int getPhoneNum() {
        return this.mPhoneNum;
    }

    public void updateNetworkMode(Context context, final int subId) {
        this.mContext = context.getApplicationContext();
        final List<SubscriptionInfo> subInfoList = SubscriptionManager.from(this.mContext).getActiveSubscriptionInfoList();
        if (subInfoList == null) {
            log("subInfoList null");
            return;
        }
        this.mProtocolHandler = new Handler();
        this.mProtocolHandler.postDelayed(new Runnable() {
            /* class com.mediatek.internal.telephony.datasub.DataSubSelector.AnonymousClass3 */

            public void run() {
                if (subInfoList.size() == 1) {
                    DataSubSelector.this.updateNetworkModeUtil(subId, 9);
                } else if (subInfoList.size() > 1) {
                    for (int index = 0; index < subInfoList.size(); index++) {
                        int tempSubId = ((SubscriptionInfo) subInfoList.get(index)).getSubscriptionId();
                        if (tempSubId == subId) {
                            DataSubSelector.this.updateNetworkModeUtil(tempSubId, 9);
                        } else {
                            DataSubSelector.this.updateNetworkModeUtil(tempSubId, 0);
                        }
                    }
                }
            }
        }, 5000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNetworkModeUtil(int subId, int mode) {
        log("Updating network mode for subId " + subId + "mode " + mode);
        PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId)).setPreferredNetworkType(mode, this.mUpdateNWTypeHandler.obtainMessage(0, subId, mode));
    }

    /* access modifiers changed from: private */
    public class UpdateNWTypeHandler extends Handler {
        static final int MESSAGE_SET_PREFERRED_NETWORK_TYPE = 0;

        private UpdateNWTypeHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                handleSetPreferredNetworkTypeResponse(msg, msg.arg1, msg.arg2);
            }
        }

        private void handleSetPreferredNetworkTypeResponse(Message msg, int subId, int mode) {
            Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId));
            if (((AsyncResult) msg.obj).exception == null) {
                DataSubSelector dataSubSelector = DataSubSelector.this;
                dataSubSelector.log("handleSetPreferredNetwrokTypeResponse2: networkMode:" + mode);
                ContentResolver contentResolver = phone.getContext().getContentResolver();
                Settings.Global.putInt(contentResolver, "preferred_network_mode" + subId, mode);
                return;
            }
            DataSubSelector.this.log("handleSetPreferredNetworkTypeResponse:exception in setting network.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String txt) {
        Rlog.d(LOG_TAG, txt);
    }

    private static void loge(String txt) {
        Rlog.e(LOG_TAG, txt);
    }
}
