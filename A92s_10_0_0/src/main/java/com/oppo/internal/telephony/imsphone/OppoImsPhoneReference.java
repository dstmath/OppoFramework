package com.oppo.internal.telephony.imsphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telecom.ConferenceParticipant;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.ims.ImsManager;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SettingsObserver;
import com.android.internal.telephony.imsphone.IOppoImsPhone;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCall;
import com.android.internal.telephony.imsphone.ImsPhoneConnection;
import com.android.internal.telephony.imsphone.ImsPhoneMmiCode;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.telephony.util.ReflectionHelper;
import com.oppo.internal.telephony.OppoCallStateMonitor;
import com.oppo.internal.telephony.explock.RegionLockConstant;
import com.oppo.internal.telephony.utils.OppoPhoneUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OppoImsPhoneReference implements IOppoImsPhone {
    private static final int EVENT_CARRIER_CONFIG_CHANGED = 1052;
    private static final int EVENT_DEFAULT_DATA_SUBSCRIPTION_CHANGED = 1050;
    private static final int EVENT_DELAY_TO_UNREGISTER = 1055;
    private static final int EVENT_NETWORK_MODE_CHANGED = 1051;
    private static final int EVENT_RADIO_STATE_CHANGED = 1054;
    private static final int EVENT_ROM_UPDATE_START = 1053;
    protected static final int IMSPHONE_EVENT_BASE = 50;
    private static final String KEY_ERLVT_OFF = "config_oppo_erlvt_off_bool";
    private static final String KEY_EVS = "config_oppo_evs_support_bool";
    private static final String KEY_HAS_SET = "rus_mtk_ims_preconfig_has_set_";
    private static final String KEY_HVOLTE = "config_oppo_hvolte_support_bool";
    private static final String KEY_IS_CONFIG_READY = "rus_mtk_ims_is_config_ready";
    private static final String KEY_LAST_OP = "rus_mtk_ims_last_op";
    private static final String KEY_VERSION_FOR_PRESET = "mtk_ims_preset_version";
    private static HandlerThread mConfigThread = new HandlerThread("rus", 10);
    protected String LOG_TAG = "OppoImsPhone";
    private boolean isResetSuccess = false;
    private boolean isSupport1XInCallMMI = false;
    private Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler(mConfigThread.getLooper()) {
        /* class com.oppo.internal.telephony.imsphone.OppoImsPhoneReference.AnonymousClass2 */

        public void handleMessage(Message msg) {
            OppoImsPhoneReference oppoImsPhoneReference = OppoImsPhoneReference.this;
            oppoImsPhoneReference.logd("handleMessage what=" + msg.what);
            boolean z = true;
            switch (msg.what) {
                case OppoImsPhoneReference.EVENT_DEFAULT_DATA_SUBSCRIPTION_CHANGED /*{ENCODED_INT: 1050}*/:
                case OppoImsPhoneReference.EVENT_NETWORK_MODE_CHANGED /*{ENCODED_INT: 1051}*/:
                    OppoImsPhoneReference.this.evalUpdateImsConfig(true);
                    return;
                case OppoImsPhoneReference.EVENT_CARRIER_CONFIG_CHANGED /*{ENCODED_INT: 1052}*/:
                    OppoImsPhoneReference.this.startObserverNetworkMode();
                    OppoImsPhoneReference.this.setUserAgentToMd();
                    OppoImsPhoneReference.this.loadPresetRusConfig();
                    OppoImsPhoneReference.this.setRusConfigToMd(true);
                    OppoImsPhoneReference oppoImsPhoneReference2 = OppoImsPhoneReference.this;
                    oppoImsPhoneReference2.updateCarrierConfig(oppoImsPhoneReference2.mPhone.getSubId());
                    return;
                case OppoImsPhoneReference.EVENT_ROM_UPDATE_START /*{ENCODED_INT: 1053}*/:
                    OppoImsPhoneReference oppoImsPhoneReference3 = OppoImsPhoneReference.this;
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    oppoImsPhoneReference3.setRusConfigToMd(z);
                    return;
                case OppoImsPhoneReference.EVENT_RADIO_STATE_CHANGED /*{ENCODED_INT: 1054}*/:
                    OppoImsPhoneReference.this.resetRadioAndIms(true);
                    return;
                case OppoImsPhoneReference.EVENT_DELAY_TO_UNREGISTER /*{ENCODED_INT: 1055}*/:
                    OppoImsPhoneReference.this.handleDelayMessage();
                    return;
                default:
                    OppoImsPhoneReference.this.logd("Wrong event do nothing!");
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public ImsPhone mPhone;
    private int mPhoneId = 0;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.oppo.internal.telephony.imsphone.OppoImsPhoneReference.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            OppoImsPhoneReference oppoImsPhoneReference = OppoImsPhoneReference.this;
            oppoImsPhoneReference.logd("Receive intent: " + intent);
            if (intent != null && intent.getAction().equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                OppoImsPhoneReference.this.mHandler.sendEmptyMessage(OppoImsPhoneReference.EVENT_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
            }
        }
    };
    SettingsObserver mSettingObserver = null;
    private TelephonyManager mTelephonyManager;

    private void registerSettingObserver(Context context) {
        this.mSettingObserver = new SettingsObserver(context, this.mHandler);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        context.registerReceiver(this.mReceiver, intentFilter);
    }

    private void handleDdsChange() {
        logd("handleDdsChange");
        ImsManager imsmanager = ImsManager.getInstance(this.mContext, this.mPhone.getPhoneId());
        if (imsmanager != null && imsmanager.isWfcEnabledByPlatform()) {
            imsmanager.updateImsServiceConfig(true);
        }
    }

    private void handleNetworkModeChange() {
        ImsManager imsmanager = ImsManager.getInstance(this.mContext, this.mPhone.getPhoneId());
        ImsPhone imsPhone = this.mPhone;
        if (imsPhone != null && imsmanager != null && imsPhone.getSubId() != SubscriptionManager.getDefaultDataSubscriptionId() && imsmanager.isWfcEnabledByPlatform()) {
            imsmanager.updateImsServiceConfig(true);
        }
    }

    static {
        mConfigThread.start();
    }

    public boolean handle1xInCallMmiCode(String dialString, ImsPhoneCall call) {
        List<ConferenceParticipant> paticipants;
        logd("dialString = " + dialString + " , isSupport1XInCallMMI = " + this.isSupport1XInCallMMI);
        int i = 0;
        if (dialString.length() == 2 && dialString.charAt(0) == '1' && this.isSupport1XInCallMMI) {
            try {
                if (!(!call.isMultiparty() || call.getImsCall() == null || (paticipants = call.getConferenceParticipants()) == null)) {
                    ArrayList<ConferenceParticipant> reParticipants = new ArrayList<>(paticipants);
                    Uri[] hostHandles = this.mPhone.getCurrentSubscriberUris();
                    while (true) {
                        if (i >= paticipants.size()) {
                            break;
                        } else if (OppoPhoneUtil.isParticipantHost(hostHandles, paticipants.get(i).getHandle())) {
                            reParticipants.remove(i);
                            break;
                        } else {
                            i++;
                        }
                    }
                    int index = Integer.parseInt(String.valueOf(dialString.charAt(1))) - 1;
                    logd("index = " + index + " , " + reParticipants.get(index).getHandle());
                    if (index <= reParticipants.size()) {
                        ConferenceParticipant removeParticipant = reParticipants.get(index);
                        ImsPhoneConnection conn = call.getFirstConnection();
                        if (conn != null) {
                            conn.onDisconnectConferenceParticipant(removeParticipant.getHandle());
                        }
                    }
                }
            } catch (Exception e) {
            }
            return true;
        }
        logd("not 1X code or isSupport1XInCallMMI was not configured");
        return false;
    }

    public int setCallForwardingTimer(Phone phone, int commandInterfaceCFReason, int timerSeconds) {
        String region = SystemProperties.get("persist.sys.oppo.region", "US");
        if (commandInterfaceCFReason != 2) {
            return timerSeconds;
        }
        if ((!region.equals("FR") || !OppoPhoneUtil.isOrangeCard(phone)) && (!region.equals("AE") || !OppoPhoneUtil.isEtisalatCard(phone))) {
            return timerSeconds;
        }
        logd("FR ORANGE & ETISALAT CFNRy not need No reply timer element set it to 0!");
        return 0;
    }

    public int setCallForwardingServiceClass(int commandInterfaceServiceClass) {
        String operator = SystemProperties.get("ro.oppo.operator", "US");
        String region = SystemProperties.get("persist.sys.oppo.region", "US");
        logd("operator and region : " + operator + " " + region);
        if ((!operator.equals("TELSTRA") && !operator.equals("VODAFONE")) || !region.equals("AU") || commandInterfaceServiceClass != 0) {
            return commandInterfaceServiceClass;
        }
        logd("telstra and vodafone default serviceClass set to VOICE!");
        return 1;
    }

    /* access modifiers changed from: private */
    public void updateCarrierConfig(int subid) {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configManager == null) {
            loge("CarrierConfiguration: No carrier config service found or not active subId = " + subid);
            return;
        }
        PersistableBundle carrierConfig = configManager.getConfigForSubId(subid);
        if (carrierConfig == null) {
            loge("cacheCarrierConfiguration: Empty carrier config.");
            return;
        }
        this.isSupport1XInCallMMI = carrierConfig.getBoolean("support_1x_incall_mmi", false);
        logd("updateCarrierConfig isSupport1XInCallMMI = " + this.isSupport1XInCallMMI + " , subId = " + subid);
    }

    /* access modifiers changed from: private */
    public void startObserverNetworkMode() {
        ImsPhone imsPhone;
        if (this.mSettingObserver != null && (imsPhone = this.mPhone) != null) {
            int subId = imsPhone.getSubId();
            this.mSettingObserver.unobserve();
            SettingsObserver settingsObserver = this.mSettingObserver;
            settingsObserver.observe(Settings.Global.getUriFor("preferred_network_mode" + subId), (int) EVENT_NETWORK_MODE_CHANGED);
        }
    }

    /* access modifiers changed from: private */
    public void evalUpdateImsConfig(boolean withDualLTE) {
        ImsManager imsmanager = ImsManager.getInstance(this.mContext, this.mPhoneId);
        if (this.mPhone.getDefaultPhone() != null) {
            int subId = this.mPhone.getDefaultPhone().getSubId();
            if (imsmanager != null) {
                boolean isDualLTE = OemTelephonyUtils.getBooleanCarrierConfig(this.mContext, "config_oppo_dual_lte_available_bool", subId);
                logd("evalUpdateImsConfig isDualLTE " + isDualLTE + ", withDualLTE " + withDualLTE);
                TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
                if (isDualLTE == withDualLTE && tm.getSimState(this.mPhoneId) == 10) {
                    imsmanager.updateImsServiceConfig(true);
                }
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void setUserAgentToMd() {
        char c;
        String value;
        if (this.mPhone.getDefaultPhone() == null) {
            logd("mDefaultPhone is null or not dds, return");
            return;
        }
        String op = OppoPhoneUtil.getNameByPhoneId(this.mPhoneId);
        String model = SystemProperties.get("ro.product.model", " ");
        String androidVersion = SystemProperties.get("ro.build.version.release", " ");
        String displayVersion = SystemProperties.get("ro.build.display.id_ims", " ");
        String softVersion = "A.01";
        Matcher matcher = Pattern.compile("_(A\\.\\d\\d)_").matcher(displayVersion);
        if (matcher.find()) {
            softVersion = matcher.group(1);
        }
        String user_agent_name = "Realme";
        if (!SystemProperties.get("ro.product.brand.sub", "").equalsIgnoreCase(user_agent_name)) {
            user_agent_name = "OPPO";
        }
        switch (op.hashCode()) {
            case -710947125:
                if (op.equals("TELSTRA")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 64811:
                if (op.equals("AIS")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 73456:
                if (op.equals("JIO")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 84824:
                if (op.equals("VDF")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 2015970:
                if (op.equals("APTG")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 75424881:
                if (op.equals("OPTUS")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1930837649:
                if (op.equals("AIRTEL")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                value = user_agent_name + "_" + model + "_" + softVersion;
                break;
            case 1:
            case 2:
                value = user_agent_name + "_" + model + "_" + displayVersion;
                break;
            case 3:
                value = user_agent_name + "_" + displayVersion;
                break;
            case 4:
                value = "Telstra " + user_agent_name + " " + model + " Android_" + androidVersion + " " + displayVersion;
                break;
            case 5:
                value = "Optus " + user_agent_name + " " + model + " Android_" + androidVersion + " " + displayVersion;
                break;
            case 6:
                value = "Vodafone " + user_agent_name + " " + model + " Android_" + androidVersion + " " + displayVersion;
                break;
            default:
                value = user_agent_name + "_" + displayVersion;
                break;
        }
        logd("setUserAgentToMd value : " + value + " for phoneId : " + this.mPhoneId);
        this.mPhone.getDefaultPhone().invokeOemRilRequestStrings(new String[]{"AT+ECFGSET=\"" + "user_agent" + "\",\"" + value + "\"", ""}, (Message) null);
    }

    public void setRusConfig(boolean reset) {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(EVENT_ROM_UPDATE_START, reset ? 1 : 0, -1));
        }
    }

    public void setRusConfigToMd(boolean reset) {
        String str;
        try {
            if (this.mPhone.getDefaultPhone() != null) {
                if (SubscriptionManager.isValidPhoneId(this.mPhoneId)) {
                    int subId = this.mPhone.getDefaultPhone().getSubId();
                    logd("setConfig for subid " + subId + ", mPhoneId " + this.mPhoneId);
                    String op = OppoPhoneUtil.getNameByPhoneId(this.mPhoneId);
                    if (OppoPhoneUtil.OP_DEFAULT.equals(op) && isTestSim(this.mContext, this.mPhoneId)) {
                        op = "TEST";
                    }
                    String erlvtOffFlag = SystemProperties.get("persist.radio.erlvt.off", "-1");
                    String evsOnFlag = SystemProperties.get("persist.radio.ims_evs_enable", "-1");
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
                    if (!"-1".equals(erlvtOffFlag)) {
                        OppoImsDatabaseHelper dbHelper = OppoImsDatabaseHelper.getDbHelper(this.mContext);
                        if ("0".equals(erlvtOffFlag)) {
                            str = "1";
                        } else {
                            str = "0";
                        }
                        dbHelper.updateConfig(op, "support_video_early_media", str, "0");
                        sharedPrefs.edit().putBoolean(KEY_IS_CONFIG_READY, true).apply();
                        SharedPreferences.Editor edit = sharedPrefs.edit();
                        edit.putString(KEY_LAST_OP + this.mPhoneId, OppoPhoneUtil.OP_DEFAULT).apply();
                    }
                    if (!"-1".equals(evsOnFlag)) {
                        OppoImsDatabaseHelper.getDbHelper(this.mContext).updateConfig(op, "evs_support", evsOnFlag, "0");
                        sharedPrefs.edit().putBoolean(KEY_IS_CONFIG_READY, true).apply();
                        SharedPreferences.Editor edit2 = sharedPrefs.edit();
                        edit2.putString(KEY_LAST_OP + this.mPhoneId, OppoPhoneUtil.OP_DEFAULT).apply();
                    }
                    String lastOp = sharedPrefs.getString(KEY_LAST_OP + this.mPhoneId, OppoPhoneUtil.OP_DEFAULT);
                    boolean isConfigReady = sharedPrefs.getBoolean(KEY_IS_CONFIG_READY, false);
                    if (!TextUtils.isEmpty(op) && !op.equals(lastOp)) {
                        if (isConfigReady) {
                            logd("op : " + op + ", lastOp " + lastOp);
                            Object imsConfig = ReflectionHelper.callMethod(ImsManager.getInstance(this.mContext, this.mPhoneId), "com.mediatek.ims.internal.MtkImsManager", "oppoGetConfigInterface", new Class[]{Integer.TYPE, Context.class}, new Object[]{Integer.valueOf(this.mPhoneId), this.mContext});
                            if (imsConfig != null) {
                                SharedPreferences.Editor edit3 = sharedPrefs.edit();
                                edit3.putString(KEY_LAST_OP + this.mPhoneId, op).apply();
                                ReflectionHelper.callMethod(imsConfig, "com.mediatek.ims.internal.MtkImsConfig", "setModemImsCfg", new Class[]{String[].class, String[].class, Integer.TYPE}, new Object[]{new String[]{"reset_ims_to_default", "reset_ltecsr_to_default", "reset_vdm_to_default", "reset_sdm_to_default", "reset_erac_to_default"}, new String[]{"0", "0", "0", "0", "0"}, Integer.valueOf(this.mPhoneId)});
                                ReflectionHelper.callMethod(imsConfig, "com.mediatek.ims.internal.MtkImsConfig", "setModemImsWoCfg", new Class[]{String[].class, String[].class, Integer.TYPE}, new Object[]{"reset", 0, Integer.valueOf(this.mPhoneId)});
                                ReflectionHelper.callMethod(imsConfig, "com.mediatek.ims.internal.MtkImsConfig", "setModemImsIwlanCfg", new Class[]{String[].class, String[].class, Integer.TYPE}, new Object[]{"wans_reset", 0, Integer.valueOf(this.mPhoneId)});
                                setUserAgentToMd();
                                logd("setImsCfg op : " + op);
                                Map map = OppoImsDatabaseHelper.getDbHelper(this.mContext).getConfig(op, 0);
                                if (!map.isEmpty()) {
                                    String[] keys = (String[]) map.keySet().toArray(new String[0]);
                                    String[] values = (String[]) map.values().toArray(new String[0]);
                                    Object obj = ReflectionHelper.callMethod(imsConfig, "com.mediatek.ims.internal.MtkImsConfig", "setModemImsCfg", new Class[]{String[].class, String[].class, Integer.TYPE}, new Object[]{keys, values, Integer.valueOf(this.mPhoneId)});
                                    int[] retIms = obj == null ? new int[]{-1} : (int[]) obj;
                                    logd("setImsCfg keys : " + Arrays.toString(keys) + "values : " + Arrays.toString(values) + ", ret = " + Arrays.toString(retIms));
                                    if (keys != null) {
                                        int idx = Arrays.binarySearch(keys, "mtk_ct_volte_support");
                                        if (idx != -1) {
                                            logd("set persist.radio_oppo_ct_volte_support to " + values[idx]);
                                            SystemProperties.set("persist.radio_oppo_ct_volte_support", values[idx]);
                                        }
                                    }
                                }
                                Map map2 = OppoImsDatabaseHelper.getDbHelper(this.mContext).getConfig(op, 1);
                                if (!map2.isEmpty()) {
                                    String[] keysWO = (String[]) map2.keySet().toArray(new String[0]);
                                    String[] valuesWO = (String[]) map2.values().toArray(new String[0]);
                                    Object obj2 = ReflectionHelper.callMethod(imsConfig, "com.mediatek.ims.internal.MtkImsConfig", "setModemImsWoCfg", new Class[]{String[].class, String[].class, Integer.TYPE}, new Object[]{keysWO, valuesWO, Integer.valueOf(this.mPhoneId)});
                                    int[] retWO = obj2 == null ? new int[]{-1} : (int[]) obj2;
                                    logd("setImsCfg keys : " + Arrays.toString(keysWO) + "values : " + Arrays.toString(valuesWO) + ", ret = " + Arrays.toString(retWO));
                                }
                                Map map3 = OppoImsDatabaseHelper.getDbHelper(this.mContext).getConfig(op, 2);
                                if (!map3.isEmpty()) {
                                    String[] keysIWLAN = (String[]) map3.keySet().toArray(new String[0]);
                                    String[] valuesIWLAN = (String[]) map3.values().toArray(new String[0]);
                                    Object obj3 = ReflectionHelper.callMethod(imsConfig, "com.mediatek.ims.internal.MtkImsConfig", "setModemImsIwlanCfg", new Class[]{String[].class, String[].class, Integer.TYPE}, new Object[]{keysIWLAN, valuesIWLAN, Integer.valueOf(this.mPhoneId)});
                                    int[] retIWLAN = obj3 == null ? new int[]{-1} : (int[]) obj3;
                                    logd("setImsCfg keys : " + Arrays.toString(keysIWLAN) + "values : " + Arrays.toString(valuesIWLAN) + ", ret = " + Arrays.toString(retIWLAN));
                                }
                            }
                            if (reset) {
                                resetRadioAndIms(false);
                                return;
                            }
                            return;
                        }
                    }
                    logd("not ready to set config , return, op : " + op + ", lastOp : " + lastOp + ", isConfigReady : " + isConfigReady);
                    return;
                }
            }
            logd("phone is null or not valid, return");
        } catch (Exception e) {
            logd(e.toString());
        }
    }

    private static boolean isTestSim(Context context, int phoneId) {
        if (phoneId == 0) {
            return "1".equals(SystemProperties.get("gsm.sim.ril.testsim", "0"));
        }
        if (phoneId == 1) {
            return "1".equals(SystemProperties.get("gsm.sim.ril.testsim.2", "0"));
        }
        if (phoneId == 2) {
            return "1".equals(SystemProperties.get("gsm.sim.ril.testsim.3", "0"));
        }
        if (phoneId != 3) {
            return false;
        }
        return "1".equals(SystemProperties.get("gsm.sim.ril.testsim.4", "0"));
    }

    /* access modifiers changed from: private */
    public void loadPresetRusConfig() {
        ImsManager imsManager = ImsManager.getInstance(this.mContext, this.mPhoneId);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        if (imsManager == null) {
            return;
        }
        if (OppoImsDatabaseHelper.getDbHelper(this.mContext) != null && sharedPrefs != null) {
            boolean evsByCarrier = OemTelephonyUtils.getBooleanCarrierConfig(this.mContext, KEY_EVS, this.mPhone.getSubId());
            boolean erlvtOffByCarrier = OemTelephonyUtils.getBooleanCarrierConfig(this.mContext, KEY_ERLVT_OFF, this.mPhone.getSubId());
            boolean hVolteByCarrier = OemTelephonyUtils.getBooleanCarrierConfig(this.mContext, KEY_HVOLTE, this.mPhone.getSubId());
            boolean hasPresetConfig = evsByCarrier || erlvtOffByCarrier || hVolteByCarrier;
            String op = OppoPhoneUtil.getNameByPhoneId(this.mPhoneId);
            String lastVersion = sharedPrefs.getString(KEY_VERSION_FOR_PRESET + this.mPhoneId, "");
            String version = SystemProperties.get("ro.build.version.ota", "N/A");
            if (!version.equals(lastVersion)) {
                SharedPreferences.Editor edit = sharedPrefs.edit();
                edit.putBoolean(KEY_HAS_SET + op, false).apply();
                SharedPreferences.Editor edit2 = sharedPrefs.edit();
                edit2.putString(KEY_VERSION_FOR_PRESET + this.mPhoneId, version).apply();
            }
            boolean hasSet = sharedPrefs.getBoolean(KEY_HAS_SET + op, false);
            logd("loadPresetRusConfig hasPresetConfig " + hasPresetConfig + ", hasSet " + hasSet + ", mPhoneId " + this.mPhoneId);
            if (!hasSet) {
                if (evsByCarrier) {
                    OppoImsDatabaseHelper.getDbHelper(this.mContext).updateConfig(op, "evs_support", "1", "0");
                } else {
                    OppoImsDatabaseHelper.getDbHelper(this.mContext).updateConfig(op, "evs_support", "0", "0");
                }
                if (erlvtOffByCarrier) {
                    OppoImsDatabaseHelper.getDbHelper(this.mContext).updateConfig(op, "support_video_early_media", "0", "0");
                } else {
                    OppoImsDatabaseHelper.getDbHelper(this.mContext).updateConfig(op, "support_video_early_media", "1", "0");
                }
                if (hVolteByCarrier) {
                    OppoImsDatabaseHelper.getDbHelper(this.mContext).updateConfig(op, "mtk_ct_volte_support", RegionLockConstant.TEST_OP_CUANDCMCC, "0");
                    SystemProperties.set("persist.radio_oppo_ct_volte_support", RegionLockConstant.TEST_OP_CUANDCMCC);
                    logd("loadPresetRusConfig set hvolte prop for " + this.mPhoneId);
                } else {
                    OppoImsDatabaseHelper.getDbHelper(this.mContext).updateConfig(op, "mtk_ct_volte_support", "1", "0");
                    SystemProperties.set("persist.radio_oppo_ct_volte_support", "1");
                    logd("loadPresetRusConfig set hvolte prop for " + this.mPhoneId);
                }
                SharedPreferences.Editor edit3 = sharedPrefs.edit();
                edit3.putString(KEY_LAST_OP + this.mPhoneId, OppoPhoneUtil.OP_DEFAULT).apply();
                sharedPrefs.edit().putBoolean(KEY_IS_CONFIG_READY, true).apply();
                SharedPreferences.Editor edit4 = sharedPrefs.edit();
                edit4.putBoolean(KEY_HAS_SET + op, true).apply();
            }
        }
    }

    /* access modifiers changed from: private */
    public void resetRadioAndIms(boolean power) {
        logd("resetRadioAndIms for phoneId " + this.mPhoneId + ", power " + power);
        ImsManager imsManager = ImsManager.getInstance(this.mContext, this.mPhoneId);
        if (this.mPhone.getDefaultPhone() != null && this.mPhone.getDefaultPhone().mCi != null && imsManager != null) {
            if (power && this.mPhone.getDefaultPhone().mCi.getRadioState() != 1) {
                boolean isAirplanMode = SystemProperties.getBoolean("persist.radio.airplane.mode.on", false);
                if (!this.mPhone.getDefaultPhone().isRadioOn() && !isAirplanMode) {
                    logd("resetRadioAndIms turn radio on ");
                    this.mPhone.getDefaultPhone().mCi.unregisterForRadioStateChanged(this.mHandler);
                    this.mPhone.getDefaultPhone().setRadioPower(power);
                    ImsManager.updateImsServiceConfig(this.mContext, this.mPhoneId, true);
                    this.isResetSuccess = true;
                }
            } else if (!power) {
                logd("resetRadioAndIms turn radio off");
                this.mPhone.getDefaultPhone().setRadioPower(power);
                this.mPhone.getDefaultPhone().mCi.registerForRadioStateChanged(this.mHandler, (int) EVENT_RADIO_STATE_CHANGED, (Object) null);
                this.mHandler.sendEmptyMessageDelayed(EVENT_DELAY_TO_UNREGISTER, 30000);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleDelayMessage() {
        if (!(this.mPhone.getDefaultPhone() == null || this.mPhone.getDefaultPhone().mCi == null)) {
            this.mPhone.getDefaultPhone().mCi.unregisterForRadioStateChanged(this.mHandler);
        }
        if (!this.isResetSuccess) {
            SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
            edit.putString(KEY_LAST_OP + this.mPhoneId, OppoPhoneUtil.OP_DEFAULT).apply();
        }
    }

    public void handleCarrerConfigChanged(int subId, Intent intent) {
        if (intent != null && subId == this.mPhone.getSubId() && this.mPhone.getDefaultPhone() != null) {
            boolean isForce = intent.getBooleanExtra("IS_FORCE", true);
            logd("send EVENT_CARRIER_CONFIG_CHANGED " + this.mPhoneId + ", isForce " + isForce + ", isRadioOn() " + this.mPhone.getDefaultPhone().isRadioOn());
            TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
            if (isForce && SubscriptionManager.isValidPhoneId(this.mPhoneId) && this.mPhone.getDefaultPhone().isRadioOn() && tm.getSimState(this.mPhoneId) == 5) {
                this.mHandler.sendEmptyMessage(EVENT_CARRIER_CONFIG_CHANGED);
            }
        }
    }

    public void handleInCallMmiForSpecificOp(ImsPhone mPhone2, ImsPhoneMmiCode imsPhoneMmiCode, MmiCode.State mState, CharSequence mMessage, String mccMnc, String mDialingNumber, boolean isUssiEnabled, Context mContext2) throws CallStateException {
        logd("handleInCallMmiForSpecificOp");
        if (isUssiEnabled) {
            if (!"45606".equals(mccMnc) || !mPhone2.isInCall() || !mPhone2.isVolteEnabled()) {
                logd("processCode: Sending short code '" + mDialingNumber + "' over IMS");
                imsPhoneMmiCode.sendUssd(mDialingNumber);
                return;
            }
            ReflectionHelper.setDeclaredField(imsPhoneMmiCode, "com.android.internal.telephony.imsphone.ImsPhoneMmiCode", "mState", MmiCode.State.FAILED);
            ReflectionHelper.setDeclaredField(imsPhoneMmiCode, "com.android.internal.telephony.imsphone.ImsPhoneMmiCode", "mMessage", mContext2.getText(17040445));
            mPhone2.onMMIDone(imsPhoneMmiCode);
            ReflectionHelper.callMethod(mPhone2, "com.mediatek.internal.telephony.imsphone.MtkImsPhone", "removeMmi", new Class[]{ImsPhoneMmiCode.class}, new Object[]{imsPhoneMmiCode});
        } else if (OppoPhoneUtil.isUssdEnabledInVolteCall(mccMnc) || !mPhone2.isInCall() || !mPhone2.isVolteEnabled()) {
            logd("Sending short code '" + mDialingNumber + "' over CS pipe.");
            ReflectionHelper.callMethod(mPhone2, "com.mediatek.internal.telephony.imsphone.MtkImsPhone", "removeMmi", new Class[]{ImsPhoneMmiCode.class}, new Object[]{imsPhoneMmiCode});
            throw new CallStateException("cs_fallback");
        } else {
            logd("Ignore the operation of sending short Code: " + mDialingNumber + " over CS pipe.");
            ReflectionHelper.setDeclaredField(imsPhoneMmiCode, "com.android.internal.telephony.imsphone.ImsPhoneMmiCode", "mState", MmiCode.State.FAILED);
            ReflectionHelper.setDeclaredField(imsPhoneMmiCode, "com.android.internal.telephony.imsphone.ImsPhoneMmiCode", "mMessage", mContext2.getText(17040445));
            mPhone2.onMMIDone(imsPhoneMmiCode);
            ReflectionHelper.callMethod(mPhone2, "com.mediatek.internal.telephony.imsphone.MtkImsPhone", "removeMmi", new Class[]{ImsPhoneMmiCode.class}, new Object[]{imsPhoneMmiCode});
        }
    }

    public void dispose() {
        logd("unobserve mSettingObserver");
        SettingsObserver settingsObserver = this.mSettingObserver;
        if (settingsObserver != null) {
            settingsObserver.unobserve();
        }
    }

    public OppoImsPhoneReference(ImsPhone phone) {
        this.mPhone = phone;
        this.mPhoneId = this.mPhone.getPhoneId();
        this.LOG_TAG += "/" + this.mPhoneId;
        this.mTelephonyManager = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        OppoCallStateMonitor.getInstance(phone.getContext()).initIms(phone);
        this.mContext = this.mPhone.getContext();
        registerSettingObserver(this.mContext);
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(this.LOG_TAG, s);
        }
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e(this.LOG_TAG, s);
    }
}
