package com.mediatek.internal.telephony.datasub;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SettingsObserver;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import com.mediatek.internal.telephony.OpTelephonyCustomizationFactoryBase;
import com.mediatek.internal.telephony.OpTelephonyCustomizationUtils;
import com.mediatek.wfo.IMwiService;
import com.mediatek.wfo.IWifiOffloadService;
import com.mediatek.wfo.MwisConstants;
import com.mediatek.wfo.WifiOffloadManager;

public class SmartDataSwitchAssistant extends Handler {
    private static final boolean DBG = true;
    private static final int EVENT_CALL_ENDED = 20;
    private static final int EVENT_CALL_STARTED = 10;
    private static final int EVENT_ID_INTVL = 10;
    private static final int EVENT_SERVICE_STATE_CHANGED = 50;
    private static final int EVENT_SRVCC_STATE_CHANGED = 30;
    private static final int EVENT_TEMPORARY_DATA_SERVICE_SETTINGS = 40;
    private static final String LOG_TAG = "SmartDataSwitch";
    private static final String PROPERTY_DEFAULT_DATA_SELECTED = "persist.vendor.radio.default.data.selected";
    private static final String TEMP_DATA_SERVICE = "data_service_enabled";
    private static String mOperatorSpec;
    private static SmartDataSwitchAssistant sSmartDataSwitchAssistant = null;
    protected boolean isResetTdsSettingsByFwk = true;
    protected boolean isTemporaryDataServiceSettingOn = false;
    protected final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.datasub.SmartDataSwitchAssistant.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                    int defaultDataSubId = intent.getIntExtra("subscription", -1);
                    SmartDataSwitchAssistant.logd("onReceive: DEFAULT_DATA_SUBSCRIPTION_CHANGED defaultDataSubId=" + defaultDataSubId);
                    SmartDataSwitchAssistant.this.updateDefaultDataPhoneId(defaultDataSubId, "DataSubChanged");
                } else if (action.equals("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED")) {
                    int status = intent.getIntExtra("simDetectStatus", 4);
                    if (status == 2 || status == 1) {
                        SmartDataSwitchAssistant.logd("onSubInfoRecordUpdated: Detecct Status:" + status);
                        SmartDataSwitchAssistant.this.resetTdsSettingsByFwk();
                    }
                }
            }
        }
    };
    private ConnectivityManager mConnectivityManager;
    private Context mContext = null;
    protected int mDefaultDataPhoneId = -1;
    private HandoverStateListener mHandoverStateListener;
    protected int mInCallPhoneId = -1;
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    protected int mPhoneNum;
    protected Phone[] mPhones;
    private final RegistrantList mReEvalueRegistrants;
    protected ContentResolver mResolver;
    protected final SettingsObserver mSettingsObserver;
    private ISmartDataSwitchAssistantOpExt mSmartDataOpExt = null;
    private final SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionsChangedListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.mediatek.internal.telephony.datasub.SmartDataSwitchAssistant.AnonymousClass2 */

        public void onSubscriptionsChanged() {
            SmartDataSwitchAssistant.this.updateDefaultDataPhoneId(SubscriptionManager.getDefaultDataSubscriptionId(), "SubscriptionsChanged");
        }
    };
    private OpTelephonyCustomizationFactoryBase mTelephonyCustomizationFactory = null;
    private TelephonyManager mTelephonyManager = null;
    private IWifiOffloadService mWfoService;

    /* access modifiers changed from: private */
    public class HandoverStateListener extends WifiOffloadManager.Listener {
        private HandoverStateListener() {
        }

        @Override // com.mediatek.wfo.IWifiOffloadListener, com.mediatek.wfo.WifiOffloadManager.Listener
        public void onHandover(int simIdx, int stage, int ratType) {
            SmartDataSwitchAssistant.logd("onHandover() simIdx:" + simIdx + " stage:" + stage + " ratType:" + ratType);
            if (stage != 1) {
                return;
            }
            if (ratType == 2) {
                SmartDataSwitchAssistant.this.onHandoverToWifi();
                SmartDataSwitchAssistant.this.mReEvalueRegistrants.notifyRegistrants();
            } else if (ratType == 1 || ratType == 3) {
                SmartDataSwitchAssistant.this.onHandoverToCellular();
                SmartDataSwitchAssistant.this.mReEvalueRegistrants.notifyRegistrants();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void regSettingsObserver() {
        this.mSettingsObserver.unobserve();
        this.mSettingsObserver.observe(Settings.Global.getUriFor(TEMP_DATA_SERVICE), 40);
    }

    public static SmartDataSwitchAssistant makeSmartDataSwitchAssistant(Context context, Phone[] phones) {
        if (context == null || phones == null) {
            throw new RuntimeException("param is null");
        }
        if (sSmartDataSwitchAssistant == null) {
            sSmartDataSwitchAssistant = new SmartDataSwitchAssistant(context, phones);
        }
        logd("makeSDSA: X sSDSA =" + sSmartDataSwitchAssistant);
        return sSmartDataSwitchAssistant;
    }

    public static SmartDataSwitchAssistant getInstance() {
        SmartDataSwitchAssistant smartDataSwitchAssistant = sSmartDataSwitchAssistant;
        if (smartDataSwitchAssistant != null) {
            return smartDataSwitchAssistant;
        }
        throw new RuntimeException("Should not be called before sSmartDataSwitchAssistant");
    }

    private SmartDataSwitchAssistant(Context context, Phone[] phones) {
        logd(" is created");
        this.mPhones = phones;
        this.mPhoneNum = phones.length;
        this.mContext = context;
        this.mResolver = this.mContext.getContentResolver();
        mOperatorSpec = SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, "OM");
        this.mSettingsObserver = new SettingsObserver(this.mContext, this);
        if (isSmartDataSwitchSupport()) {
            registerEvents();
        }
        initOpSmartDataSwitchAssistant(context);
        if (this.mSmartDataOpExt == null) {
            this.mSmartDataOpExt = new SmartDataSwitchAssistantOpExt(context);
        }
        this.mSmartDataOpExt.init(this);
        this.mReEvalueRegistrants = new RegistrantList();
        if (Settings.Global.getInt(this.mResolver, TEMP_DATA_SERVICE, 0) != 0) {
            this.isTemporaryDataServiceSettingOn = true;
        } else {
            this.isTemporaryDataServiceSettingOn = false;
        }
        logd("init isTemporaryDataServiceSettingOn=" + this.isTemporaryDataServiceSettingOn);
    }

    private void initOpSmartDataSwitchAssistant(Context context) {
        try {
            this.mTelephonyCustomizationFactory = OpTelephonyCustomizationUtils.getOpFactory(context);
            this.mSmartDataOpExt = this.mTelephonyCustomizationFactory.makeSmartDataSwitchAssistantOpExt(context);
        } catch (Exception e) {
            loge("mSmartDataOpExt init fail");
            e.printStackTrace();
        }
    }

    public void dispose() {
        logd("SmartDataSwitchAssistant.dispose");
        if (isSmartDataSwitchSupport()) {
            unregisterEvents();
        }
    }

    private void registerEvents() {
        logd("registerEvents");
        regSettingsObserver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        if (this.isResetTdsSettingsByFwk) {
            filter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        }
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        SubscriptionManager.from(this.mContext).addOnSubscriptionsChangedListener(this.mSubscriptionsChangedListener);
    }

    private void unregisterEvents() {
        logd("unregisterEvents");
        getTelephonyManager();
        SubscriptionManager.from(this.mContext).removeOnSubscriptionsChangedListener(this.mSubscriptionsChangedListener);
    }

    public void handleMessage(Message msg) {
        boolean newSettings;
        int phoneId = msg.what % 10;
        int eventId = msg.what - phoneId;
        if (eventId == 30) {
            logd("SRVCC, phoneId=" + phoneId);
            onSrvccStateChanged();
            this.mReEvalueRegistrants.notifyRegistrants();
        } else if (eventId == 40) {
            boolean oldSettings = this.isTemporaryDataServiceSettingOn;
            if (Settings.Global.getInt(this.mResolver, TEMP_DATA_SERVICE, 0) != 0) {
                newSettings = true;
            } else {
                newSettings = false;
            }
            if (oldSettings != newSettings) {
                this.isTemporaryDataServiceSettingOn = newSettings;
                logd("TemporaryDataSetting changed newSettings=" + newSettings);
                onTemporaryDataSettingsChanged();
            }
        } else if (eventId != 50) {
            logd("Unhandled message with number: " + msg.what);
        } else if (onServiceStateChanged(phoneId)) {
            logd("EVENT_SERVICE_STATE_CHANGED: notify");
            this.mReEvalueRegistrants.notifyRegistrants();
        }
    }

    public void regSrvccEvent() {
        for (int i = 0; i < this.mPhoneNum; i++) {
            this.mPhones[i].registerForHandoverStateChanged(this, i + 30, (Object) null);
        }
    }

    public void unregSrvccEvent() {
        for (int i = 0; i < this.mPhoneNum; i++) {
            this.mPhones[i].unregisterForHandoverStateChanged(this);
        }
    }

    public void regServiceStateChangedEvent() {
        for (int i = 0; i < this.mPhoneNum; i++) {
            this.mPhones[i].registerForServiceStateChanged(this, i + 50, (Object) null);
        }
    }

    public void unregServiceStateChangedEvent() {
        for (int i = 0; i < this.mPhoneNum; i++) {
            this.mPhones[i].unregisterForServiceStateChanged(this);
        }
    }

    public void regImsHandoverEvent() {
        if (this.mWfoService == null) {
            this.mWfoService = getWifiOffLoadService();
        }
        if (this.mWfoService != null) {
            try {
                if (this.mHandoverStateListener == null) {
                    this.mHandoverStateListener = new HandoverStateListener();
                }
                this.mWfoService.registerForHandoverEvent(this.mHandoverStateListener);
            } catch (Exception e) {
                loge("regImsHandoverEvent(): RemoteException mWfoService()");
            }
        }
    }

    public void unregImsHandoverEvent() {
        try {
            this.mWfoService.unregisterForHandoverEvent(this.mHandoverStateListener);
        } catch (Exception e) {
            loge("unregImsHandoverEvent: RemoteException mWfoService()");
        }
    }

    public boolean isWifcCalling(int phoneId) {
        return this.mPhones[phoneId].isWifiCallingEnabled();
    }

    public boolean isVoLteCalling(int phoneId) {
        MtkGsmCdmaPhone[] mtkGsmCdmaPhoneArr = this.mPhones;
        if (mtkGsmCdmaPhoneArr[phoneId] != null) {
            return mtkGsmCdmaPhoneArr[phoneId].isDuringVoLteCall();
        }
        loge("isVoLteCalling: mPhones[" + phoneId + "] is null");
        return false;
    }

    public int getVoiceNetworkType(int phoneId) {
        if (phoneId != -1) {
            return this.mPhones[phoneId].getServiceStateTracker().mSS.getRilVoiceRadioTechnology();
        }
        loge("updateCallType() invalid Phone Id!");
        return 0;
    }

    public void updateDefaultDataPhoneId(int currDataSubId, String reason) {
        if (SubscriptionManager.isValidSubscriptionId(currDataSubId)) {
            int newDefaultDataPhoneId = SubscriptionManager.getPhoneId(currDataSubId);
            int i = this.mDefaultDataPhoneId;
            if (i == -1) {
                this.mDefaultDataPhoneId = newDefaultDataPhoneId;
                logd("first time to update mDefaultDataPhoneId=" + this.mDefaultDataPhoneId + " reason:" + reason);
                if (!isDefaultDataSelectedBeforeReboot()) {
                    resetTdsSettingsByFwk();
                }
            } else if (newDefaultDataPhoneId != i) {
                this.mDefaultDataPhoneId = newDefaultDataPhoneId;
                logd("updateDefaultDataPhoneId() mDefaultDataPhoneId=" + this.mDefaultDataPhoneId + " reason:" + reason);
                resetTdsSettingsByFwk();
            }
            setDefaultDataSelectedProperty(1);
            return;
        }
        setDefaultDataSelectedProperty(0);
    }

    public void onSrvccStateChanged() {
        logd("onSrvccStateChanged()");
        this.mSmartDataOpExt.onSrvccStateChanged();
    }

    public boolean onServiceStateChanged(int phoneId) {
        return this.mSmartDataOpExt.onServiceStateChanged(phoneId);
    }

    public void onHandoverToWifi() {
        logd("onHandoverToWifi()");
    }

    public void onHandoverToCellular() {
        logd("onHandoverToCellular()");
    }

    public void onTemporaryDataSettingsChanged() {
        logd("onTemporaryDataSettingsChanged() newSettings=" + this.isTemporaryDataServiceSettingOn);
        this.mReEvalueRegistrants.notifyRegistrants();
    }

    public void resetTdsSettingsByFwk() {
        logd("add for OPPO, donot reset TEMP_DATA_SERVICE");
    }

    private boolean isDefaultDataPhoneIdValid() {
        int i = this.mDefaultDataPhoneId;
        if (i == -1 || i == Integer.MAX_VALUE) {
            return false;
        }
        return true;
    }

    private ConnectivityManager getConnectivityManager() {
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        return this.mConnectivityManager;
    }

    private TelephonyManager getTelephonyManager() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        return this.mTelephonyManager;
    }

    private IWifiOffloadService getWifiOffLoadService() {
        if (this.mWfoService == null) {
            IBinder binder = ServiceManager.getService(WifiOffloadManager.WFO_SERVICE);
            if (binder != null) {
                this.mWfoService = IWifiOffloadService.Stub.asInterface(binder);
            } else {
                IBinder binder2 = ServiceManager.getService(MwisConstants.MWI_SERVICE);
                if (binder2 != null) {
                    try {
                        this.mWfoService = IMwiService.Stub.asInterface(binder2).getWfcHandlerInterface();
                    } catch (Exception e) {
                        loge("getWifiOffLoadService: can't get MwiService error:" + e);
                    }
                } else {
                    loge("getWifiOffLoadService: No MwiService exist");
                }
            }
        }
        return this.mWfoService;
    }

    private void setDefaultDataSelectedProperty(int selected) {
        if (!SystemProperties.get(PROPERTY_DEFAULT_DATA_SELECTED).equals(String.valueOf(selected))) {
            SystemProperties.set(PROPERTY_DEFAULT_DATA_SELECTED, String.valueOf(selected));
            logd("setDefaultDataSelectedProperty() selected=" + String.valueOf(selected));
        }
    }

    private boolean isDefaultDataSelectedBeforeReboot() {
        String defaultDataSelected = SystemProperties.get(PROPERTY_DEFAULT_DATA_SELECTED);
        logd("isDefaultDataSelectedBeforeReboot() property=" + defaultDataSelected);
        return defaultDataSelected.equals("1");
    }

    private boolean isSmartDataSwitchSupport() {
        return SystemProperties.get("persist.vendor.radio.smart.data.switch").equals("1");
    }

    public void registerReEvaluateEvent(Handler h, int what, Object obj, int phoneId) {
        if (!isSmartDataSwitchSupport()) {
            logd("registerReEvaluateEvent: not have TempDataSwitchCapability");
            return;
        }
        Registrant r = new Registrant(h, what, obj);
        logd("registerReEvaluateEvent()");
        setInCallPhoneId(phoneId);
        this.mReEvalueRegistrants.add(r);
        regImsHandoverEvent();
        this.mSmartDataOpExt.onCallStarted();
    }

    public void unregisterReEvaluateEvent(Handler h) {
        if (!isSmartDataSwitchSupport()) {
            logd("unregisterReEvaluateEvent: not have TempDataSwitchCapability");
            return;
        }
        logd("unregisterReEvaluateEvent()");
        setInCallPhoneId(-1);
        this.mReEvalueRegistrants.remove(h);
        unregImsHandoverEvent();
        this.mSmartDataOpExt.onCallEnded();
    }

    public boolean checkIsSwitchAvailable(int phoneId) {
        if (isSmartDataSwitchSupport()) {
            return this.mSmartDataOpExt.checkIsSwitchAvailable(phoneId);
        }
        logd("checkIsSwitchAvailable: not have TempDataSwitchCapability");
        return false;
    }

    public void onDsdaStateChanged() {
        logd("onDsdaStateChanged: notify");
        this.mReEvalueRegistrants.notifyRegistrants();
    }

    public boolean getTemporaryDataSettings() {
        return this.isTemporaryDataServiceSettingOn;
    }

    private void setInCallPhoneId(int phoneId) {
        this.mInCallPhoneId = phoneId;
    }

    public int getInCallPhoneId() {
        return this.mInCallPhoneId;
    }

    protected static void logv(String s) {
        Rlog.v(LOG_TAG, s);
    }

    protected static void logd(String s) {
        Rlog.d(LOG_TAG, s);
    }

    protected static void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }

    protected static void logi(String s) {
        Rlog.i(LOG_TAG, s);
    }
}
