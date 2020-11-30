package com.mediatek.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.internal.telephony.MtkSuppServContants;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MtkSuppServConf {
    private static final int CARRIER_CONFIG_LOADING_TIME = 5000;
    private static final int EVENT_CARRIER_CONFIG_LOADED = 1;
    private static final int EVENT_CARRIER_CONFIG_LOADED_TIMEOUT = 2;
    private static final int EVENT_ICC_CHANGED = 4;
    private static final int EVENT_INIT = 0;
    private static final int EVENT_RECORDS_LOADED = 3;
    private static final String LOG_TAG = "SuppServConf";
    private int OPERATORUTILS_BOOL_FALSE = 2;
    private int OPERATORUTILS_BOOL_TRUE = 1;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.MtkSuppServConf.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            MtkSuppServConf mtkSuppServConf = MtkSuppServConf.this;
            mtkSuppServConf.logd("mBroadcastReceiver: action " + intent.getAction());
            if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                int subId = intent.getIntExtra("subscription", -1);
                MtkSuppServConf mtkSuppServConf2 = MtkSuppServConf.this;
                mtkSuppServConf2.logi("Receive ACTION_CARRIER_CONFIG_CHANGED: subId=" + subId + ", mPhone.getSubId()=" + MtkSuppServConf.this.mPhone.getSubId());
                if (subId == MtkSuppServConf.this.mPhone.getSubId()) {
                    MtkSuppServConf.this.logi("CarrierConfigLoader is loading complete!");
                    MtkSuppServConf.this.mSSConfigHandler.removeMessages(2);
                    MtkSuppServConf.this.mSSConfigHandler.obtainMessage(1).sendToTarget();
                }
            }
        }
    };
    private Context mContext = null;
    private HashMap<MtkSuppServContants.CUSTOMIZATION_ITEM, SSConfig> mCustomizationMap = new HashMap<>();
    private final AtomicReference<IccRecords> mIccRecords = new AtomicReference<>();
    private MtkGsmCdmaPhone mPhone = null;
    private SSConfigHandler mSSConfigHandler = null;
    private UiccController mUiccController = null;

    /* access modifiers changed from: package-private */
    /* renamed from: com.mediatek.internal.telephony.MtkSuppServConf$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$mediatek$internal$telephony$MtkSuppServContants$CUSTOMIZATION_ITEM = new int[MtkSuppServContants.CUSTOMIZATION_ITEM.values().length];
    }

    /* access modifiers changed from: private */
    public class SSConfig {
        public static final int DONE = 1;
        public static final int UNSET = 0;
        public boolean bDefault = false;
        public boolean bValue = false;
        public int iDefault = -1;
        public int iValue = -1;
        public String mCarrierConfigKey = "";
        private int mStatus = 0;
        public int mSystemPropIdx = -1;
        public String sDefault = "";
        public String sValue = "";

        public SSConfig(String carrierConfigKey, int systemPropIdx) {
            this.mCarrierConfigKey = carrierConfigKey;
            this.mSystemPropIdx = systemPropIdx;
        }

        public SSConfig(String carrierConfigKey, int systemPropIdx, boolean bDefault2) {
            this.mCarrierConfigKey = carrierConfigKey;
            this.mSystemPropIdx = systemPropIdx;
            this.bDefault = bDefault2;
            this.bValue = this.bDefault;
        }

        public SSConfig(String carrierConfigKey, int systemPropIdx, String sDefault2) {
            this.mCarrierConfigKey = carrierConfigKey;
            this.mSystemPropIdx = systemPropIdx;
            this.sDefault = sDefault2;
            this.sValue = this.sDefault;
        }

        public SSConfig(String carrierConfigKey, int systemPropIdx, int iDefault2) {
            this.mCarrierConfigKey = carrierConfigKey;
            this.mSystemPropIdx = systemPropIdx;
            this.iDefault = iDefault2;
            this.iValue = this.iDefault;
        }

        public void setValue(boolean bValue2) {
            this.bValue = bValue2;
            this.mStatus = 1;
        }

        public void setValue(String sValue2) {
            this.sValue = sValue2;
            this.mStatus = 1;
        }

        public void setValue(int iValue2) {
            this.iValue = iValue2;
            this.mStatus = 1;
        }

        public void reset() {
            this.bValue = this.bDefault;
            this.sValue = this.sDefault;
            this.iValue = this.iDefault;
            this.mStatus = 0;
        }

        public String toString() {
            return "bValue: " + this.bValue + ", sValue: " + this.sValue + ", iValue: " + this.iValue;
        }
    }

    public MtkSuppServConf(Context context, Phone phone) {
        this.mContext = context;
        this.mPhone = (MtkGsmCdmaPhone) phone;
        logi("MtkSuppServConf constructor.");
    }

    public void init(Looper looper) {
        logi("MtkSuppServConf init.");
        this.mSSConfigHandler = new SSConfigHandler(looper);
        initConfig();
        registerCarrierConfigIntent();
        registerEvent();
        this.mSSConfigHandler.obtainMessage(0).sendToTarget();
    }

    private void registerCarrierConfigIntent() {
        this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
    }

    private void unregisterCarrierConfigIntent() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
    }

    private void registerEvent() {
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this.mSSConfigHandler, 4, (Object) null);
    }

    private void unregisterEvent() {
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.unregisterForIccChanged(this.mSSConfigHandler);
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            r.unregisterForRecordsLoaded(this.mSSConfigHandler);
            this.mIccRecords.set(null);
        }
    }

    public void dispose() {
        unregisterCarrierConfigIntent();
        unregisterEvent();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUpdateIcc() {
        if (this.mUiccController != null) {
            IccRecords newIccRecords = getUiccRecords(1);
            if (newIccRecords == null && this.mPhone.getPhoneType() == 2) {
                newIccRecords = getUiccRecords(2);
            }
            IccRecords r = this.mIccRecords.get();
            if (!(newIccRecords == null || r == null)) {
                logd("onUpdateIcc: newIccRecords=" + newIccRecords + ", r=" + r);
            }
            if (r != newIccRecords) {
                if (r != null) {
                    logi("Removing stale icc objects.");
                    r.unregisterForRecordsLoaded(this.mSSConfigHandler);
                    this.mIccRecords.set(null);
                }
                if (newIccRecords == null) {
                    onSimNotReady();
                } else if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
                    logi("New records found.");
                    this.mIccRecords.set(newIccRecords);
                    newIccRecords.registerForRecordsLoaded(this.mSSConfigHandler, 3, (Object) null);
                }
            }
        }
    }

    private void onSimNotReady() {
        logd("onSimNotReady");
        resetConfig();
    }

    private IccRecords getUiccRecords(int appFamily) {
        return this.mUiccController.getIccRecords(this.mPhone.getPhoneId(), appFamily);
    }

    /* access modifiers changed from: private */
    public class SSConfigHandler extends Handler {
        public SSConfigHandler() {
        }

        public SSConfigHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            MtkSuppServConf mtkSuppServConf = MtkSuppServConf.this;
            mtkSuppServConf.logd("handleMessage msg: " + MtkSuppServConf.this.eventToString(msg.what));
            int i = msg.what;
            if (i == 0 || i == 1 || i == 2) {
                MtkSuppServConf.this.resetConfig();
                MtkSuppServConf.this.loadCarrierConfig();
                MtkSuppServConf.this.printConfig();
            } else if (i == 3) {
                sendMessageDelayed(obtainMessage(2), 5000);
            } else if (i == 4) {
                MtkSuppServConf.this.onUpdateIcc();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String eventToString(int eventId) {
        if (eventId == 0) {
            return "EVENT_INIT";
        }
        if (eventId == 1) {
            return "EVENT_CARRIER_CONFIG_LOADED";
        }
        if (eventId == 2) {
            return "EVENT_CARRIER_CONFIG_LOADED_TIMEOUT";
        }
        if (eventId == 3) {
            return "EVENT_RECORDS_LOADED";
        }
        if (eventId != 4) {
            return "UNKNOWN_EVENT";
        }
        return "EVENT_ICC_CHANGED";
    }

    private void initConfig() {
        logi("initConfig start.");
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.GSM_UT_SUPPORT, new SSConfig("mtk_carrier_ss_gsm_ut_support", 0));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.NOT_SUPPORT_XCAP, new SSConfig("mtk_carrier_ss_not_support_xcap", 1));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.TBCLIR, new SSConfig("mtk_carrier_ss_tb_clir", 2));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.IMS_NW_CW, new SSConfig("mtk_carrier_ss_ims_nw_cw", 3));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.ENABLE_XCAP_HTTP_RESPONSE_409, new SSConfig("mtk_carrier_ss_enable_xcap_http_response_409", 4));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.TRANSFER_XCAP_404, new SSConfig("mtk_carrier_ss_transfer_xcap_404", 5));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.NOT_SUPPORT_CALL_IDENTITY, new SSConfig("mtk_carrier_ss_not_support_call_identity", 6));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.RE_REGISTER_FOR_CF, new SSConfig("mtk_carrier_ss_re_register_for_cf", 7));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.SUPPORT_SAVE_CF_NUMBER, new SSConfig("mtk_carrier_ss_support_save_cf_number", 8));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.QUERY_CFU_AGAIN_AFTER_SET, new SSConfig("mtk_carrier_ss_query_cfu_again_after_set", 9));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.NOT_SUPPORT_OCB, new SSConfig("mtk_carrier_ss_not_support_ocb", 10));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.NOT_SUPPORT_WFC_UT, new SSConfig("mtk_carrier_ss_not_support_wfc_ut", 11));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.NEED_CHECK_DATA_ENABLE, new SSConfig("mtk_carrier_ss_need_check_data_enable", 12));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.NEED_CHECK_DATA_ROAMING, new SSConfig("mtk_carrier_ss_need_check_data_roaming", 13));
        this.mCustomizationMap.put(MtkSuppServContants.CUSTOMIZATION_ITEM.NEED_CHECK_IMS_WHEN_ROAMING, new SSConfig("mtk_carrier_ss_need_check_ims_when_roaming", 14));
        logi("initConfig end.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetConfig() {
        logi("resetConfig start.");
        for (Map.Entry<MtkSuppServContants.CUSTOMIZATION_ITEM, SSConfig> entry : this.mCustomizationMap.entrySet()) {
            entry.getValue().reset();
        }
        logi("resetConfig end.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadCarrierConfig() {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        int subId = this.mPhone.getSubId();
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        } else {
            logd("CarrierConfigManager is null.");
        }
        if (b != null) {
            loadFromCarrierConfig(b);
        } else {
            logd("Config is null.");
        }
    }

    private void loadFromCarrierConfig(PersistableBundle b) {
        logi("loadFromCarrierConfig start.");
        SSConfig config = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.GSM_UT_SUPPORT);
        if (config != null && b.containsKey(config.mCarrierConfigKey)) {
            config.setValue(b.getBoolean(config.mCarrierConfigKey, config.bDefault));
        }
        SSConfig config2 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.NOT_SUPPORT_XCAP);
        if (config2 != null && b.containsKey(config2.mCarrierConfigKey)) {
            config2.setValue(b.getBoolean(config2.mCarrierConfigKey, config2.bDefault));
        }
        SSConfig config3 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.TBCLIR);
        if (config3 != null && b.containsKey(config3.mCarrierConfigKey)) {
            config3.setValue(b.getBoolean(config3.mCarrierConfigKey, config3.bDefault));
        }
        SSConfig config4 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.IMS_NW_CW);
        if (config4 != null && b.containsKey(config4.mCarrierConfigKey)) {
            config4.setValue(b.getBoolean(config4.mCarrierConfigKey, config4.bDefault));
        }
        SSConfig config5 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.ENABLE_XCAP_HTTP_RESPONSE_409);
        if (config5 != null && b.containsKey(config5.mCarrierConfigKey)) {
            config5.setValue(b.getBoolean(config5.mCarrierConfigKey, config5.bDefault));
        }
        SSConfig config6 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.TRANSFER_XCAP_404);
        if (config6 != null && b.containsKey(config6.mCarrierConfigKey)) {
            config6.setValue(b.getBoolean(config6.mCarrierConfigKey, config6.bDefault));
        }
        SSConfig config7 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.NOT_SUPPORT_CALL_IDENTITY);
        if (config7 != null && b.containsKey(config7.mCarrierConfigKey)) {
            config7.setValue(b.getBoolean(config7.mCarrierConfigKey, config7.bDefault));
        }
        SSConfig config8 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.RE_REGISTER_FOR_CF);
        if (config8 != null && b.containsKey(config8.mCarrierConfigKey)) {
            config8.setValue(b.getBoolean(config8.mCarrierConfigKey, config8.bDefault));
        }
        SSConfig config9 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.SUPPORT_SAVE_CF_NUMBER);
        if (config9 != null && b.containsKey(config9.mCarrierConfigKey)) {
            config9.setValue(b.getBoolean(config9.mCarrierConfigKey, config9.bDefault));
        }
        SSConfig config10 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.QUERY_CFU_AGAIN_AFTER_SET);
        if (config10 != null && b.containsKey(config10.mCarrierConfigKey)) {
            config10.setValue(b.getBoolean(config10.mCarrierConfigKey, config10.bDefault));
        }
        SSConfig config11 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.NOT_SUPPORT_OCB);
        if (config11 != null && b.containsKey(config11.mCarrierConfigKey)) {
            config11.setValue(b.getBoolean(config11.mCarrierConfigKey, config11.bDefault));
        }
        SSConfig config12 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.NOT_SUPPORT_WFC_UT);
        if (config12 != null && b.containsKey(config12.mCarrierConfigKey)) {
            config12.setValue(b.getBoolean(config12.mCarrierConfigKey, config12.bDefault));
        }
        SSConfig config13 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.NEED_CHECK_DATA_ENABLE);
        if (config13 != null && b.containsKey(config13.mCarrierConfigKey)) {
            config13.setValue(b.getBoolean(config13.mCarrierConfigKey, config13.bDefault));
        }
        SSConfig config14 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.NEED_CHECK_DATA_ROAMING);
        if (config14 != null && b.containsKey(config14.mCarrierConfigKey)) {
            config14.setValue(b.getBoolean(config14.mCarrierConfigKey, config14.bDefault));
        }
        SSConfig config15 = this.mCustomizationMap.get(MtkSuppServContants.CUSTOMIZATION_ITEM.NEED_CHECK_IMS_WHEN_ROAMING);
        if (config15 != null && b.containsKey(config15.mCarrierConfigKey)) {
            config15.setValue(b.getBoolean(config15.mCarrierConfigKey, config15.bDefault));
        }
        MtkSuppServHelper ssHelper = MtkSuppServManager.getSuppServHelper(this.mPhone.getPhoneId());
        if (ssHelper != null) {
            ssHelper.notifyCarrierConfigLoaded();
        }
        logi("loadFromCarrierConfig end.");
    }

    private int getSysPropForBool(int idx) {
        int r = -1;
        if (idx > -1) {
            int i = 0;
            int which = SystemProperties.getInt(MtkSuppServContants.SYS_PROP_BOOL_CONFIG, 0);
            if (which > 0 && ((1 << idx) & which) != 0) {
                if (((1 << idx) & SystemProperties.getInt(MtkSuppServContants.SYS_PROP_BOOL_VALUE, 0)) != 0) {
                    i = 1;
                }
                r = i;
                StringBuilder sb = new StringBuilder();
                sb.append("getSysPropForBool idx: ");
                sb.append(idx);
                sb.append("=");
                sb.append(r == 1 ? "TRUE" : "FALSE");
                logi(sb.toString());
            }
        }
        return r;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void printConfig() {
        for (Map.Entry<MtkSuppServContants.CUSTOMIZATION_ITEM, SSConfig> entry : this.mCustomizationMap.entrySet()) {
            logi("" + MtkSuppServContants.toString(entry.getKey()) + " -> " + entry.getValue().toString());
        }
    }

    public boolean isGsmUtSupport(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.GSM_UT_SUPPORT, mccMnc);
    }

    public boolean isNotSupportXcap(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.NOT_SUPPORT_XCAP, mccMnc);
    }

    public boolean isTbClir(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.TBCLIR, mccMnc);
    }

    public boolean isImsNwCW(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.IMS_NW_CW, mccMnc);
    }

    public boolean isNeedCheckImsWhenRoaming(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.NEED_CHECK_IMS_WHEN_ROAMING, mccMnc);
    }

    public boolean isEnableXcapHttpResponse409(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.ENABLE_XCAP_HTTP_RESPONSE_409, mccMnc);
    }

    public boolean isTransferXcap404(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.TRANSFER_XCAP_404, mccMnc);
    }

    public boolean isNotSupportCallIdentity(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.NOT_SUPPORT_CALL_IDENTITY, mccMnc);
    }

    public boolean isReregisterForCF(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.RE_REGISTER_FOR_CF, mccMnc);
    }

    public boolean isSupportSaveCFNumber(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.SUPPORT_SAVE_CF_NUMBER, mccMnc);
    }

    public boolean isQueryCFUAgainAfterSet(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.QUERY_CFU_AGAIN_AFTER_SET, mccMnc);
    }

    public boolean isNotSupportOCB(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.NOT_SUPPORT_OCB, mccMnc);
    }

    public boolean isNotSupportWFCUt(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.NOT_SUPPORT_WFC_UT, mccMnc);
    }

    public boolean isNeedCheckDataEnabled(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.NEED_CHECK_DATA_ENABLE, mccMnc);
    }

    public boolean isNeedCheckDataRoaming(String mccMnc) {
        return getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM.NEED_CHECK_DATA_ROAMING, mccMnc);
    }

    private boolean getBooleanValue(MtkSuppServContants.CUSTOMIZATION_ITEM item, String mccMnc) {
        HashMap<MtkSuppServContants.CUSTOMIZATION_ITEM, SSConfig> hashMap = this.mCustomizationMap;
        if (hashMap == null || !hashMap.containsKey(item)) {
            logi("Null or Without config: " + MtkSuppServContants.toString(item));
            return false;
        }
        SSConfig config = this.mCustomizationMap.get(item);
        int value = getSysPropForBool(config.mSystemPropIdx);
        if (value != -1) {
            StringBuilder sb = new StringBuilder();
            sb.append("");
            sb.append(MtkSuppServContants.toString(item));
            sb.append(": ");
            sb.append(value == 1);
            logi(sb.toString());
            if (value == 1) {
                return true;
            }
            return false;
        }
        logi("" + MtkSuppServContants.toString(item) + ": " + config.bValue);
        return config.bValue;
    }

    private String getStringValue(MtkSuppServContants.CUSTOMIZATION_ITEM item, String mccMnc) {
        String sConfigFromOperatorUtils = getFromOperatorUtilsString(item, mccMnc);
        if (!sConfigFromOperatorUtils.equals("")) {
            return sConfigFromOperatorUtils;
        }
        HashMap<MtkSuppServContants.CUSTOMIZATION_ITEM, SSConfig> hashMap = this.mCustomizationMap;
        if (hashMap == null || !hashMap.containsKey(item)) {
            logi("Null or Without config: " + MtkSuppServContants.toString(item));
            return "";
        }
        SSConfig config = this.mCustomizationMap.get(item);
        logi("" + MtkSuppServContants.toString(item) + ": " + config.sValue);
        return config.sValue;
    }

    private int getIntValue(MtkSuppServContants.CUSTOMIZATION_ITEM item, String mccMnc) {
        HashMap<MtkSuppServContants.CUSTOMIZATION_ITEM, SSConfig> hashMap = this.mCustomizationMap;
        if (hashMap == null || !hashMap.containsKey(item)) {
            logi("Null or Without config: " + MtkSuppServContants.toString(item));
            return -1;
        }
        SSConfig config = this.mCustomizationMap.get(item);
        logi("" + MtkSuppServContants.toString(item) + ": " + config.iValue);
        return config.iValue;
    }

    private String getFromOperatorUtilsString(MtkSuppServContants.CUSTOMIZATION_ITEM item, String mccMnc) {
        int i = AnonymousClass2.$SwitchMap$com$mediatek$internal$telephony$MtkSuppServContants$CUSTOMIZATION_ITEM[item.ordinal()];
        return "";
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void logw(String s) {
        Rlog.w(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logi(String s) {
        Rlog.i(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String s) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void logv(String s) {
        Rlog.v(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }
}
