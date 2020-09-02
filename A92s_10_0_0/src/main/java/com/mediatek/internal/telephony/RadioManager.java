package com.mediatek.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.ims.ImsManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.selfactivation.SaPersistDataHelper;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RadioManager extends Handler {
    protected static final String ACTION_AIRPLANE_CHANGE_DONE = "com.mediatek.intent.action.AIRPLANE_CHANGE_DONE";
    public static final String ACTION_FORCE_SET_RADIO_POWER = "com.mediatek.internal.telephony.RadioManager.intent.action.FORCE_SET_RADIO_POWER";
    public static final String ACTION_MODEM_POWER_NO_CHANGE = "com.mediatek.intent.action.MODEM_POWER_CHANGE";
    private static final String ACTION_WIFI_OFFLOAD_SERVICE_ON = "mediatek.intent.action.WFC_POWER_ON_MODEM";
    private static final String ACTION_WIFI_ONLY_MODE_CHANGED = "android.intent.action.ACTION_WIFI_ONLY_MODE";
    protected static final boolean AIRPLANE_MODE_OFF = false;
    protected static final boolean AIRPLANE_MODE_ON = true;
    public static final int ERROR_AIRPLANE_MODE = 2;
    public static final int ERROR_ICCID_NOT_READY = 5;
    public static final int ERROR_MODEM_OFF = 4;
    public static final int ERROR_NO_PHONE_INSTANCE = 1;
    public static final int ERROR_PCO = 6;
    public static final int ERROR_PCO_ALREADY_OFF = 7;
    public static final int ERROR_SIM_SWITCH_EXECUTING = 8;
    public static final int ERROR_TURN_OFF_RADIO_DURING_ECC = 9;
    public static final int ERROR_WIFI_ONLY = 3;
    private static final int[] EVENT_DSBP_STATE_CHANGED = {10, 11, 12, 13};
    private static final int EVENT_DSBP_STATE_CHANGED_SLOT_1 = 10;
    private static final int EVENT_DSBP_STATE_CHANGED_SLOT_2 = 11;
    private static final int EVENT_DSBP_STATE_CHANGED_SLOT_3 = 12;
    private static final int EVENT_DSBP_STATE_CHANGED_SLOT_4 = 13;
    private static final int[] EVENT_RADIO_AVAILABLE = {1, 2, 3, 4};
    private static final int EVENT_RADIO_AVAILABLE_SLOT_1 = 1;
    private static final int EVENT_RADIO_AVAILABLE_SLOT_2 = 2;
    private static final int EVENT_RADIO_AVAILABLE_SLOT_3 = 3;
    private static final int EVENT_RADIO_AVAILABLE_SLOT_4 = 4;
    private static final int EVENT_REPORT_AIRPLANE_DONE = 8;
    private static final int EVENT_REPORT_SIM_MODE_DONE = 9;
    private static final int EVENT_SET_MODEM_POWER_OFF_DONE = 6;
    private static final int EVENT_SET_SILENT_REBOOT_DONE = 7;
    private static final int EVENT_VIRTUAL_SIM_ON = 5;
    protected static final String EXTRA_AIRPLANE_MODE = "airplaneMode";
    public static final String EXTRA_MODEM_POWER = "modemPower";
    private static final String EXTRA_WIFI_OFFLOAD_SERVICE_ON = "mediatek:POWER_ON_MODEM";
    private static final boolean ICC_READ_NOT_READY = false;
    private static final boolean ICC_READ_READY = true;
    protected static final int INITIAL_RETRY_INTERVAL_MSEC = 200;
    protected static final int INVALID_PHONE_ID = -1;
    private static final String IS_NOT_SILENT_REBOOT = "0";
    protected static final String IS_SILENT_REBOOT = "1";
    static final String LOG_TAG = "RadioManager";
    protected static final boolean MODEM_POWER_OFF = false;
    protected static final boolean MODEM_POWER_ON = true;
    protected static final int MODE_PHONE1_ONLY = 1;
    private static final int MODE_PHONE2_ONLY = 2;
    private static final int MODE_PHONE3_ONLY = 4;
    private static final int MODE_PHONE4_ONLY = 8;
    protected static final int NO_SIM_INSERTED = 0;
    private static final String PREF_CATEGORY_RADIO_STATUS = "RADIO_STATUS";
    private static final String PROPERTY_AIRPLANE_MODE = "persist.vendor.radio.airplane.mode.on";
    protected static String[] PROPERTY_ICCID_SIM = {"vendor.ril.iccid.sim1", "vendor.ril.iccid.sim2", "vendor.ril.iccid.sim3", "vendor.ril.iccid.sim4"};
    protected static String[] PROPERTY_RADIO_OFF = {"vendor.ril.ipo.radiooff", "vendor.ril.ipo.radiooff.2"};
    protected static final String PROPERTY_SILENT_REBOOT_MD1 = "vendor.gsm.ril.eboot";
    private static final String PROPERTY_SIM_MODE = "persist.vendor.radio.sim.mode";
    protected static final boolean RADIO_POWER_OFF = false;
    protected static final boolean RADIO_POWER_ON = true;
    public static final int REASON_NONE = -1;
    public static final int REASON_PCO_OFF = 1;
    public static final int REASON_PCO_ON = 0;
    private static final String REGISTRANTS_WITH_NO_NAME = "NO_NAME";
    protected static final int SIM_INSERTED = 1;
    private static final int SIM_NOT_INITIALIZED = -1;
    protected static final String STRING_NO_SIM_INSERTED = "N/A";
    public static final int SUCCESS = 0;
    protected static final int TO_SET_MODEM_POWER = 2;
    protected static final int TO_SET_RADIO_POWER = 1;
    private static final int WIFI_ONLY_INIT = -1;
    private static final boolean WIFI_ONLY_MODE_OFF = false;
    private static final boolean WIFI_ONLY_MODE_ON = true;
    private static final boolean isOP01 = DataSubConstants.OPERATOR_OP01.equalsIgnoreCase(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, ""));
    private static final boolean isOP09 = DataSubConstants.OPERATOR_OP09.equalsIgnoreCase(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, ""));
    private static final boolean mFlightModePowerOffModem = SystemProperties.get("ro.vendor.mtk_flight_mode_power_off_md").equals("1");
    protected static ConcurrentHashMap<IRadioPower, String> mNotifyRadioPowerChange = new ConcurrentHashMap<>();
    protected static SharedPreferences sIccidPreference;
    private static RadioManager sRadioManager;
    private boolean mAirDnMsgSent;
    protected boolean mAirplaneMode = false;
    protected int mBitmapForPhoneCount;
    private CommandsInterface[] mCi;
    /* access modifiers changed from: private */
    public Context mContext;
    private Runnable[] mForceSetRadioPowerRunnable;
    private ImsSwitchController mImsSwitchController = null;
    private int[] mInitializeWaitCounter;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.RadioManager.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            RadioManager.log("BroadcastReceiver: " + intent.getAction());
            if (intent.getAction().equals("android.telephony.action.SIM_CARD_STATE_CHANGED")) {
                RadioManager.this.onReceiveSimStateChangedIntent(intent);
            } else if (intent.getAction().equals(RadioManager.ACTION_FORCE_SET_RADIO_POWER)) {
                RadioManager.this.onReceiveForceSetRadioPowerIntent(intent);
            } else if (intent.getAction().equals(RadioManager.ACTION_WIFI_ONLY_MODE_CHANGED)) {
                RadioManager.this.onReceiveWifiOnlyModeStateChangedIntent(intent);
            } else if (intent.getAction().equals(RadioManager.ACTION_WIFI_OFFLOAD_SERVICE_ON)) {
                RadioManager.this.onReceiveWifiStateChangedIntent(intent);
            } else if (intent.getAction().equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE") || intent.getAction().equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED")) {
                if (RadioManager.isFlightModePowerOffModemConfigEnabled()) {
                    RadioManager.this.mPowerSM.updateModemPowerState(RadioManager.this.mAirplaneMode, RadioManager.this.mBitmapForPhoneCount, 128);
                }
                if (RadioManager.this.mIsDsbpChanging[RadioManager.this.findMainCapabilityPhoneId()]) {
                    boolean unused = RadioManager.this.mIsPendingRadioByDsbpChanging = true;
                } else {
                    RadioManager.this.setRadioPowerAfterCapabilitySwitch();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean[] mIsDsbpChanging;
    /* access modifiers changed from: private */
    public boolean mIsPendingRadioByDsbpChanging = false;
    private boolean mIsRadioUnavailable = false;
    /* access modifiers changed from: private */
    public boolean mIsWifiOn = false;
    private boolean mIsWifiOnlyDevice;
    private boolean mModemPower = true;
    private ModemPowerMessage[] mModemPowerMessages;
    private boolean mNeedIgnoreMessageForChangeDone;
    private boolean mNeedIgnoreMessageForWait;
    private Runnable mNotifyMSimModeChangeRunnable;
    private Runnable[] mNotifySimModeChangeRunnable;
    protected int mPhoneCount;
    /* access modifiers changed from: private */
    public PowerSM mPowerSM;
    private Runnable[] mRadioPowerRunnable;
    public int[] mReason;
    protected int[] mSimInsertedStatus;
    private int mSimModeSetting;
    private boolean mWifiOnlyMode = false;

    public static RadioManager init(Context context, int phoneCount, CommandsInterface[] ci) {
        RadioManager radioManager;
        synchronized (RadioManager.class) {
            if (sRadioManager == null) {
                sRadioManager = new RadioManager(context, phoneCount, ci);
            }
            radioManager = sRadioManager;
        }
        return radioManager;
    }

    public static RadioManager getInstance() {
        RadioManager radioManager;
        synchronized (RadioManager.class) {
            radioManager = sRadioManager;
        }
        return radioManager;
    }

    protected RadioManager(Context context, int phoneCount, CommandsInterface[] ci) {
        char c;
        boolean z;
        int i = 0;
        int airplaneMode = Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on", 0);
        int wifionlyMode = ImsManager.getWfcMode(context);
        this.mAirDnMsgSent = false;
        if (ImsManager.getInstance(context, 0).isServiceReady() && ImsManager.isWfcEnabledByPlatform(context) && !StorageManager.inCryptKeeperBounce()) {
            log("initial actual wifi state when wifi calling is on");
            WifiManager wiFiManager = (WifiManager) context.getSystemService("wifi");
            if (wiFiManager != null) {
                this.mIsWifiOn = wiFiManager.isWifiEnabled();
            }
        }
        log("Initialize RadioManager under airplane mode:" + airplaneMode + " wifi only mode:" + wifionlyMode + " wifi mode: " + this.mIsWifiOn);
        this.mSimInsertedStatus = new int[phoneCount];
        for (int i2 = 0; i2 < phoneCount; i2++) {
            this.mSimInsertedStatus[i2] = -1;
        }
        this.mInitializeWaitCounter = new int[phoneCount];
        for (int i3 = 0; i3 < phoneCount; i3++) {
            this.mInitializeWaitCounter[i3] = 0;
        }
        this.mRadioPowerRunnable = new RadioPowerRunnable[phoneCount];
        for (int i4 = 0; i4 < phoneCount; i4++) {
            this.mRadioPowerRunnable[i4] = new RadioPowerRunnable(true, i4);
        }
        this.mNotifySimModeChangeRunnable = new SimModeChangeRunnable[phoneCount];
        for (int i5 = 0; i5 < phoneCount; i5++) {
            this.mNotifySimModeChangeRunnable[i5] = new SimModeChangeRunnable(true, i5);
        }
        this.mNotifyMSimModeChangeRunnable = new MSimModeChangeRunnable(3);
        this.mForceSetRadioPowerRunnable = new ForceSetRadioPowerRunnable[phoneCount];
        this.mContext = context;
        this.mAirplaneMode = airplaneMode != 0;
        this.mWifiOnlyMode = wifionlyMode == 0;
        this.mCi = ci;
        this.mPhoneCount = phoneCount;
        this.mBitmapForPhoneCount = convertPhoneCountIntoBitmap(phoneCount);
        sIccidPreference = this.mContext.getSharedPreferences("RADIO_STATUS", 4);
        this.mSimModeSetting = Settings.Global.getInt(context.getContentResolver(), "msim_mode_setting", this.mBitmapForPhoneCount);
        this.mImsSwitchController = new ImsSwitchController(this.mContext, this.mPhoneCount, this.mCi);
        this.mCi[RadioCapabilitySwitchUtil.getMainCapabilityPhoneId()].setVendorSetting(8, Integer.toString(airplaneMode), obtainMessage(8));
        log("Not BSP Package, register intent!!!");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telephony.action.SIM_CARD_STATE_CHANGED");
        filter.addAction(ACTION_FORCE_SET_RADIO_POWER);
        filter.addAction(ACTION_WIFI_ONLY_MODE_CHANGED);
        filter.addAction(ACTION_WIFI_OFFLOAD_SERVICE_ON);
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        registerListener();
        this.mIsWifiOnlyDevice = !((ConnectivityManager) this.mContext.getSystemService("connectivity")).isNetworkSupported(0);
        this.mPowerSM = new PowerSM("PowerSM");
        this.mPowerSM.start();
        this.mReason = new int[phoneCount];
        int i6 = 0;
        while (i6 < phoneCount) {
            MtkSubscriptionManager.getSubIdUsingPhoneId(i6);
            int selfActivationState = SaPersistDataHelper.getIntData(this.mContext, i6, SaPersistDataHelper.DATA_KEY_SA_STATE, i);
            String pcoEnable = SystemProperties.get("persist.vendor.pco5.radio.ctrl", "0");
            if (2 != selfActivationState) {
                z = true;
            } else if (!pcoEnable.equals("0")) {
                z = true;
                this.mReason[i6] = 1;
                c = 65535;
                i6++;
                i = 0;
            } else {
                z = true;
            }
            c = 65535;
            this.mReason[i6] = -1;
            i6++;
            i = 0;
        }
        this.mIsDsbpChanging = new boolean[phoneCount];
        for (int i7 = 0; i7 < phoneCount; i7++) {
            this.mIsDsbpChanging[i7] = false;
        }
    }

    private int convertPhoneCountIntoBitmap(int phoneCount) {
        int ret = 0;
        for (int i = 0; i < phoneCount; i++) {
            ret += 1 << i;
        }
        log("Convert phoneCount " + phoneCount + " into bitmap " + ret);
        return ret;
    }

    /* access modifiers changed from: private */
    public void setRadioPowerAfterCapabilitySwitch() {
        log("Update radio power after capability switch or dsbp changing");
        int targetPhoneId = SubscriptionManager.from(this.mContext).getDefaultDataPhoneId();
        if (!SubscriptionManager.isValidPhoneId(targetPhoneId)) {
            targetPhoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        }
        setRadioPower(!this.mAirplaneMode, targetPhoneId);
        for (int i = 0; i < this.mPhoneCount; i++) {
            if (targetPhoneId != i) {
                setRadioPower(!this.mAirplaneMode, i);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onReceiveWifiStateChangedIntent(Intent intent) {
        if (intent.getAction().equals(ACTION_WIFI_OFFLOAD_SERVICE_ON)) {
            int extraWifiState = intent.getBooleanExtra(EXTRA_WIFI_OFFLOAD_SERVICE_ON, false) ? 3 : 1;
            log("Receiving ACTION_WIFI_OFFLOAD_SERVICE_ON, airplaneMode: " + this.mAirplaneMode + " isFlightModePowerOffModemConfigEnabled:" + isFlightModePowerOffModemConfigEnabled() + ", mIsWifiOn: " + this.mIsWifiOn);
            if (extraWifiState == 1) {
                log("WIFI_STATE_CHANGED disabled");
                this.mIsWifiOn = false;
                if (this.mAirplaneMode && isFlightModePowerOffModemConfigEnabled()) {
                    log("WIFI_STATE_CHANGED disabled, set modem off");
                    setSilentRebootPropertyForAllModem("1");
                    this.mPowerSM.updateModemPowerState(false, this.mBitmapForPhoneCount, 4);
                }
            } else if (extraWifiState != 3) {
                log("default: WIFI_STATE_CHANGED extra" + extraWifiState);
            } else {
                log("WIFI_STATE_CHANGED enabled");
                this.mIsWifiOn = true;
                if (this.mAirplaneMode && isFlightModePowerOffModemConfigEnabled()) {
                    if (isModemPowerOff(0)) {
                    }
                    log("WIFI_STATE_CHANGED enabled, set modem on");
                    setSilentRebootPropertyForAllModem("1");
                    this.mPowerSM.updateModemPowerState(true, this.mBitmapForPhoneCount, 4);
                }
            }
        } else {
            log("Wrong intent");
        }
    }

    /* access modifiers changed from: protected */
    public void onReceiveSimStateChangedIntent(Intent intent) {
        int simStatus = intent.getIntExtra("android.telephony.extra.SIM_STATE", 0);
        int phoneId = intent.getIntExtra("phone", -1);
        if (!isValidPhoneId(phoneId)) {
            log("INTENT:Invalid phone id:" + phoneId + ", do nothing!");
            return;
        }
        log("INTENT:SIM_STATE_CHANGED: " + intent.getAction() + ", sim status: " + simStatus + ", phoneId: " + phoneId);
        if (11 == simStatus) {
            this.mSimInsertedStatus[phoneId] = 1;
            log("Phone[" + phoneId + "]: " + simStatusToString(1));
            if ("N/A".equals(readIccIdUsingPhoneId(phoneId))) {
                log("Phone " + phoneId + ":SIM ready but ICCID not ready, do nothing");
            } else if (!this.mAirplaneMode) {
                log("Set Radio Power due to SIM_STATE_CHANGED, power: " + true + ", phoneId: " + phoneId);
                setRadioPower(true, phoneId);
            }
        } else if (1 == simStatus) {
            this.mSimInsertedStatus[phoneId] = 0;
            log("Phone[" + phoneId + "]: " + simStatusToString(0));
            if (!this.mAirplaneMode) {
                log("Set Radio Power due to SIM_STATE_CHANGED, power: " + false + ", phoneId: " + phoneId);
                setRadioPower(false, phoneId);
            }
        }
    }

    public void onReceiveWifiOnlyModeStateChangedIntent(Intent intent) {
        boolean enabled = intent.getBooleanExtra("state", false);
        log("Received ACTION_WIFI_ONLY_MODE_CHANGED, enabled = " + enabled);
        if (enabled == this.mWifiOnlyMode) {
            log("enabled = " + enabled + ", mWifiOnlyMode = " + this.mWifiOnlyMode + " is not expected (the same)");
            return;
        }
        this.mWifiOnlyMode = enabled;
        if (!this.mAirplaneMode) {
            boolean radioPower = !enabled;
            for (int i = 0; i < this.mPhoneCount; i++) {
                setRadioPower(radioPower, i);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onReceiveForceSetRadioPowerIntent(Intent intent) {
        int mode = intent.getIntExtra("mode", -1);
        log("force set radio power, mode: " + mode);
        if (mode == -1) {
            log("Invalid mode, MSIM_MODE intent has no extra value");
            return;
        }
        for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
            if (true == (((1 << phoneId) & mode) != 0)) {
                forceSetRadioPower(true, phoneId);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isValidPhoneId(int phoneId) {
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public String simStatusToString(int simStatus) {
        if (simStatus == -1) {
            return "SIM HAVE NOT INITIALIZED";
        }
        if (simStatus == 0) {
            return "NO SIM DETECTED";
        }
        if (simStatus != 1) {
            return null;
        }
        return "SIM DETECTED";
    }

    public void notifyAirplaneModeChange(boolean enabled) {
        if (enabled == this.mAirplaneMode) {
            log("enabled = " + enabled + ", mAirplaneMode = " + this.mAirplaneMode + " is not expected (the same)");
            return;
        }
        int phoneId = findMainCapabilityPhoneId();
        this.mAirplaneMode = enabled;
        log("Airplane mode changed: " + enabled + " mDesiredPower: " + this.mPowerSM.mDesiredModemPower + " mCurrentModemPower: " + this.mPowerSM.mCurrentModemPower);
        this.mCi[phoneId].setVendorSetting(8, Integer.toString(enabled ? 1 : 0), obtainMessage(8));
        if (enabled) {
            this.mIsWifiOn = false;
        }
        int radioAction = -1;
        if (!isFlightModePowerOffModemConfigEnabled() || isUnderCryptKeeper()) {
            if (isMSimModeSupport()) {
                log("Airplane mode changed: turn on/off all radio");
                radioAction = 1;
            }
        } else if (this.mPowerSM.mDesiredModemPower && !this.mAirplaneMode) {
            log("Airplane mode changed: turn on all radio due to mode conflict");
            radioAction = 1;
        } else if (this.mAirplaneMode || !this.mIsWifiOn) {
            log("Airplane mode changed: turn on/off all modem");
            radioAction = 2;
        } else {
            log("airplane mode changed: airplane mode on and wifi-calling on. Then,leave airplane mode: turn on/off all radio");
            radioAction = 1;
        }
        if (radioAction == 1) {
            boolean radioPower = !enabled;
            int targetPhoneId = SubscriptionManager.from(this.mContext).getDefaultDataPhoneId();
            if (!SubscriptionManager.isValidPhoneId(targetPhoneId)) {
                targetPhoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
            }
            setRadioPower(radioPower, targetPhoneId);
            for (int i = 0; i < this.mPhoneCount; i++) {
                if (targetPhoneId != i) {
                    setRadioPower(radioPower, i);
                }
            }
            Intent intent = new Intent(ACTION_AIRPLANE_CHANGE_DONE);
            intent.putExtra(EXTRA_AIRPLANE_MODE, !enabled);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } else if (radioAction == 2) {
            setSilentRebootPropertyForAllModem("1");
            this.mPowerSM.updateModemPowerState(!enabled, this.mBitmapForPhoneCount, 2);
        }
    }

    public static boolean isUnderCryptKeeper() {
        if (!SystemProperties.get("ro.crypto.type").equals("block") || !SystemProperties.get("ro.crypto.state").equals("encrypted") || !SystemProperties.get("vold.decrypt").equals("trigger_restart_min_framework")) {
            log("[Special Case] Not Under CryptKeeper");
            return false;
        }
        log("[Special Case] Under CryptKeeper, Not to turn on/off modem");
        return true;
    }

    public void setSilentRebootPropertyForAllModem(String isSilentReboot) {
        TelephonyManager.getDefault().getMultiSimConfiguration();
        int phoneId = findMainCapabilityPhoneId();
        int on = 0;
        if (isSilentReboot.equals("1")) {
            on = 1;
        }
        log("enable silent reboot");
        this.mCi[phoneId].setVendorSetting(10, Integer.toString(on), obtainMessage(7));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
    public void notifyRadioAvailable(int phoneId) {
        log("Phone " + phoneId + " notifies radio available airplane mode: " + this.mAirplaneMode + " cryptkeeper: " + isUnderCryptKeeper() + " mIsWifiOn:" + this.mIsWifiOn);
        if (isRadioAvaliable()) {
            this.mPowerSM.sendEvent(3);
        }
        if (RadioCapabilitySwitchUtil.getMainCapabilityPhoneId() == phoneId) {
            cleanModemPowerMessage();
            if (this.mAirplaneMode && isFlightModePowerOffModemConfigEnabled() && !isUnderCryptKeeper() && !this.mIsWifiOn) {
                log("Power off modem because boot up under airplane mode");
                this.mPowerSM.updateModemPowerState(false, 1 << phoneId, 64);
            }
        }
        if (!this.mAirDnMsgSent && this.mAirplaneMode) {
            if (!isFlightModePowerOffModemConfigEnabled() || isUnderCryptKeeper()) {
                Intent intent = new Intent(ACTION_AIRPLANE_CHANGE_DONE);
                intent.putExtra(EXTRA_AIRPLANE_MODE, true);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                this.mAirDnMsgSent = true;
            }
        }
    }

    /* access modifiers changed from: private */
    public void setModemPower(boolean power, int phoneBitMap) {
        log("Set Modem Power according to bitmap, Power:" + power + ", PhoneBitMap:" + phoneBitMap);
        if (PhoneFactory.getDefaultPhone().getServiceStateTracker().isDeviceShuttingDown()) {
            Rlog.d(LOG_TAG, "[RadioManager] skip the request because device is shutdown");
            return;
        }
        TelephonyManager.MultiSimVariants config = TelephonyManager.getDefault().getMultiSimConfiguration();
        Message[] responses = monitorModemPowerChangeDone(power, phoneBitMap, findMainCapabilityPhoneId());
        int i = AnonymousClass2.$SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[config.ordinal()];
        if (i == 1 || i == 2 || i == 3) {
            int phoneId = findMainCapabilityPhoneId();
            log("Set Modem Power, Power:" + power + ", phoneId:" + phoneId);
            this.mCi[phoneId].setModemPower(power, responses[phoneId]);
            if (!power) {
                for (int i2 = 0; i2 < this.mPhoneCount; i2++) {
                    resetSimInsertedStatus(i2);
                }
            }
        } else {
            int phoneId2 = PhoneFactory.getDefaultPhone().getPhoneId();
            log("Set Modem Power under SS mode:" + power + ", phoneId:" + phoneId2);
            this.mCi[phoneId2].setModemPower(power, responses[phoneId2]);
        }
        if (!power) {
            return;
        }
        if ((isOP01 || isOP09) && SystemProperties.get("vendor.ril.atci.flightmode").equals("1")) {
            log("Power on Modem, Set vendor.ril.atci.flightmode to 0");
            SystemProperties.set("vendor.ril.atci.flightmode", "0");
        }
    }

    /* renamed from: com.mediatek.internal.telephony.RadioManager$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$android$telephony$TelephonyManager$MultiSimVariants = new int[TelephonyManager.MultiSimVariants.values().length];

        static {
            try {
                $SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[TelephonyManager.MultiSimVariants.DSDS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[TelephonyManager.MultiSimVariants.DSDA.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[TelephonyManager.MultiSimVariants.TSTS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public int findMainCapabilityPhoneId() {
        int result = Integer.valueOf(SystemProperties.get("persist.vendor.radio.simswitch", "1")).intValue() - 1;
        if (result < 0 || result >= this.mPhoneCount) {
            return 0;
        }
        return result;
    }

    protected class RadioPowerRunnable implements Runnable {
        int retryPhoneId;
        boolean retryPower;

        public RadioPowerRunnable(boolean power, int phoneId) {
            this.retryPower = power;
            this.retryPhoneId = phoneId;
        }

        public void run() {
            RadioManager.this.setRadioPower(this.retryPower, this.retryPhoneId);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
    public int setRadioPower(boolean power, int phoneId) {
        String printableIccid;
        log("setRadioPower, power=" + power + "  phoneId=" + phoneId);
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone == null) {
            return 1;
        }
        if ((isFlightModePowerOffModemEnabled() || power) && this.mAirplaneMode) {
            log("Set Radio Power on under airplane mode, ignore");
            return 2;
        }
        log("setRadioPowerByPhone: isUiccSlotForbid :" + OemConstant.isUiccSlotForbid(phoneId));
        if (OemConstant.isUiccSlotForbid(phoneId)) {
            log("setRadioPowerByPhone: isUiccSlotForbid return");
            return 4;
        } else if (!((ConnectivityManager) this.mContext.getSystemService("connectivity")).isNetworkSupported(0)) {
            log("wifi-only device, so return");
            return 3;
        } else if (MtkProxyController.getInstance().isCapabilitySwitching()) {
            log("SIM switch executing, return and wait SIM switch done");
            return 8;
        } else if (isModemPowerOff(phoneId)) {
            log("modem for phone " + phoneId + " off, do not set radio again");
            return 4;
        } else {
            String pcoEnable = SystemProperties.get("persist.vendor.pco5.radio.ctrl", "0");
            if (1 == this.mReason[phoneId] && power && !pcoEnable.equals("0")) {
                log("Not allow to turn on radio under PCO=5");
                return 6;
            } else if (1 == this.mReason[phoneId] && phone.mCi.getRadioState() == 0) {
                log("PCO5 and already off");
                return 7;
            } else {
                boolean isInEcc = false;
                TelecomManager tm = (TelecomManager) this.mContext.getSystemService("telecom");
                if (tm != null && tm.isInEmergencyCall()) {
                    isInEcc = true;
                }
                if (power || !isInEcc) {
                    removeCallbacks(this.mRadioPowerRunnable[phoneId]);
                    if (isIccIdReady(phoneId)) {
                        setSimInsertedStatus(phoneId);
                        boolean radioPower = power;
                        String iccId = readIccIdUsingPhoneId(phoneId);
                        if (isRequiredRadioOff(iccId)) {
                            if (isInEcc) {
                                log("Adjust radio to off because once manually turned off during ECC, return");
                                return 9;
                            }
                            if ("N/A".equals(iccId)) {
                                printableIccid = "N/A";
                            } else {
                                printableIccid = binaryToHex(getHashCode(SubscriptionInfo.givePrintableIccid(iccId)));
                            }
                            log("Adjust radio to off because once manually turned off, hash(iccid): " + printableIccid + " , phone: " + phoneId);
                            radioPower = false;
                        } else if (!this.mAirplaneMode && !phone.isShuttingDown() && 1 != this.mReason[phoneId]) {
                            radioPower = true;
                        }
                        if (this.mWifiOnlyMode && !isInEcc) {
                            log("setradiopower but wifi only, turn off");
                            radioPower = false;
                        }
                        boolean isCTACase = checkForCTACase();
                        if (getSimInsertedStatus(phoneId) != 0) {
                            log("Trigger set Radio Power, power: " + radioPower + ", phoneId: " + phoneId);
                            PhoneFactory.getPhone(phoneId).setRadioPower(radioPower);
                        } else if (isCTACase) {
                            int capabilityPhoneId = findMainCapabilityPhoneId();
                            log("No SIM inserted, force to turn on 3G/4G phone " + capabilityPhoneId + " radio if no any sim radio is enabled!");
                            Phone phone2 = PhoneFactory.getPhone(capabilityPhoneId);
                            if ((phone2 instanceof GsmCdmaPhone) && ((GsmCdmaPhone) phone2).isPhoneTypeGsm()) {
                                log("NO SIM GSM CASE, To check if there would be anyother CDMA Phone");
                                int i = 0;
                                while (true) {
                                    if (i >= this.mPhoneCount) {
                                        break;
                                    }
                                    Phone phone3 = PhoneFactory.getPhone(i);
                                    if ((phone3 instanceof GsmCdmaPhone) && ((GsmCdmaPhone) phone3).isPhoneTypeCdma()) {
                                        capabilityPhoneId = phone3.getPhoneId();
                                        log("It found that CDMA Phone in capabilityPhoneId = " + phone3.getPhoneId());
                                        break;
                                    }
                                    i++;
                                }
                            }
                            PhoneFactory.getPhone(capabilityPhoneId).setRadioPower(true);
                            for (int i2 = 0; i2 < this.mPhoneCount; i2++) {
                                Phone phone4 = PhoneFactory.getPhone(i2);
                                if (!(phone4 == null || i2 == capabilityPhoneId || isInEcc)) {
                                    phone4.setRadioPower(false);
                                }
                            }
                        } else if (true != isInEcc || this.mAirplaneMode) {
                            log("No SIM inserted, turn Radio off!");
                            radioPower = false;
                            PhoneFactory.getPhone(phoneId).setRadioPower(false);
                        } else {
                            log("No SIM inserted, turn/keep Radio On for ECC! target power: " + radioPower + ", phoneId: " + phoneId);
                            if (radioPower) {
                                PhoneFactory.getPhone(phoneId).setRadioPower(radioPower);
                            }
                        }
                        refreshSimSetting(radioPower, phoneId);
                        return 0;
                    } else if (hasCallbacks(this.mForceSetRadioPowerRunnable[phoneId])) {
                        log("ForceSetRadioPowerRunnable exists queue, do not execute RadioPowerRunnablefor phone " + phoneId);
                        return 5;
                    } else {
                        log("RILD initialize not completed, wait for 200ms");
                        this.mRadioPowerRunnable[phoneId] = new RadioPowerRunnable(power, phoneId);
                        postDelayed(this.mRadioPowerRunnable[phoneId], 200);
                        return 5;
                    }
                } else {
                    if (this.mAirplaneMode) {
                        ConnectivityManager.from(this.mContext).setAirplaneMode(false);
                        Intent intent = new Intent(ACTION_AIRPLANE_CHANGE_DONE);
                        intent.putExtra(EXTRA_AIRPLANE_MODE, false);
                        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    }
                    log("Not allow to operate radio power during emergency call");
                    return 2;
                }
            }
        }
    }

    public int setRadioPower(boolean power, int phoneId, int reason) {
        this.mReason[phoneId] = reason;
        return setRadioPower(power, phoneId);
    }

    /* access modifiers changed from: protected */
    public int getSimInsertedStatus(int phoneId) {
        return this.mSimInsertedStatus[phoneId];
    }

    /* access modifiers changed from: protected */
    public void setSimInsertedStatus(int phoneId) {
        if ("N/A".equals(readIccIdUsingPhoneId(phoneId))) {
            this.mSimInsertedStatus[phoneId] = 0;
        } else {
            this.mSimInsertedStatus[phoneId] = 1;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isIccIdReady(int phoneId) {
        String iccId = readIccIdUsingPhoneId(phoneId);
        if (iccId == null || "".equals(iccId)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public String readIccIdUsingPhoneId(int phoneId) {
        String printableIccid;
        String ret = SystemProperties.get(PROPERTY_ICCID_SIM[phoneId]);
        if ("N/A".equals(ret)) {
            printableIccid = "N/A";
        } else {
            printableIccid = binaryToHex(getHashCode(SubscriptionInfo.givePrintableIccid(ret)));
        }
        log("Hash(ICCID) for phone " + phoneId + " is " + printableIccid);
        return ret;
    }

    /* access modifiers changed from: protected */
    public boolean checkForCTACase() {
        boolean isCTACase = true;
        if (this.mAirplaneMode || this.mWifiOnlyMode) {
            isCTACase = false;
        } else {
            for (int i = 0; i < this.mPhoneCount; i++) {
                log("Check For CTA case: mSimInsertedStatus[" + i + "]:" + this.mSimInsertedStatus[i]);
                int[] iArr = this.mSimInsertedStatus;
                if (iArr[i] == 1 || iArr[i] == -1) {
                    isCTACase = false;
                }
            }
        }
        boolean isInEcc = false;
        TelecomManager tm = (TelecomManager) this.mContext.getSystemService("telecom");
        if (tm != null && tm.isInEmergencyCall()) {
            isInEcc = true;
        }
        if (!isCTACase && !isInEcc) {
            turnOffCTARadioIfNecessary();
        }
        log("CTA case: " + isCTACase);
        return isCTACase;
    }

    private void turnOffCTARadioIfNecessary() {
        for (int i = 0; i < this.mPhoneCount; i++) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone != null && this.mSimInsertedStatus[i] == 0) {
                if (isModemPowerOff(i)) {
                    log("modem off, not to handle CTA");
                    return;
                }
                log("turn off phone " + i + " radio because we are no longer in CTA mode");
                phone.setRadioPower(false);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void refreshSimSetting(boolean radioPower, int phoneId) {
        if (PhoneFactory.getDefaultPhone().getServiceStateTracker().isDeviceShuttingDown()) {
            Rlog.i(LOG_TAG, "[RadioManager] skip the refreshSimSetting because device is shutdown");
            return;
        }
        int oldMode = this.mSimModeSetting;
        if (!radioPower) {
            this.mSimModeSetting = (~(1 << phoneId)) & this.mSimModeSetting;
        } else {
            this.mSimModeSetting = (1 << phoneId) | this.mSimModeSetting;
        }
        log("Refresh MSIM mode setting to " + this.mSimModeSetting + " from " + oldMode);
        this.mCi[findMainCapabilityPhoneId()].setVendorSetting(9, Integer.toString(this.mSimModeSetting), obtainMessage(9));
        Settings.Global.putInt(this.mContext.getContentResolver(), "msim_mode_setting", this.mSimModeSetting);
    }

    protected class ForceSetRadioPowerRunnable implements Runnable {
        int mRetryPhoneId;
        boolean mRetryPower;

        public ForceSetRadioPowerRunnable(boolean power, int phoneId) {
            this.mRetryPower = power;
            this.mRetryPhoneId = phoneId;
        }

        public void run() {
            RadioManager.this.forceSetRadioPower(this.mRetryPower, this.mRetryPhoneId);
        }
    }

    public void forceSetRadioPower(boolean power, int phoneId) {
        log("force set radio power for phone" + phoneId + " ,power: " + power);
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone != null) {
            if (!isFlightModePowerOffModemConfigEnabled() || !this.mAirplaneMode) {
                if (isModemPowerOff(phoneId) && this.mAirplaneMode) {
                    log("Modem Power Off for phone " + phoneId + ", Power on modem first");
                    this.mPowerSM.updateModemPowerState(true, 1 << phoneId, 16);
                }
                removeCallbacks(this.mForceSetRadioPowerRunnable[phoneId]);
                if (!isIccIdReady(phoneId) || (isFlightModePowerOffModemConfigEnabled() && !this.mAirplaneMode && power && isModemOff(phoneId))) {
                    log("force set radio power, read iccid not ready, wait for200ms");
                    this.mForceSetRadioPowerRunnable[phoneId] = new ForceSetRadioPowerRunnable(power, phoneId);
                    postDelayed(this.mForceSetRadioPowerRunnable[phoneId], 200);
                    return;
                }
                refreshIccIdPreference(power, readIccIdUsingPhoneId(phoneId));
                phone.setRadioPower(power);
                refreshSimSetting(power, phoneId);
                return;
            }
            log("Force Set Radio Power under airplane mode, ignore");
        }
    }

    private class SimModeChangeRunnable implements Runnable {
        int mPhoneId;
        boolean mPower;

        public SimModeChangeRunnable(boolean power, int phoneId) {
            this.mPower = power;
            this.mPhoneId = phoneId;
        }

        public void run() {
            RadioManager.this.notifySimModeChange(this.mPower, this.mPhoneId);
        }
    }

    public void notifySimModeChange(boolean power, int phoneId) {
        log("SIM mode changed, power: " + power + ", phoneId" + phoneId);
        if (!isMSimModeSupport() || this.mAirplaneMode) {
            log("Airplane mode on or MSIM Mode option is closed, do nothing!");
            return;
        }
        removeCallbacks(this.mNotifySimModeChangeRunnable[phoneId]);
        if (!isIccIdReady(phoneId)) {
            log("sim mode read iccid not ready, wait for 200ms");
            this.mNotifySimModeChangeRunnable[phoneId] = new SimModeChangeRunnable(power, phoneId);
            postDelayed(this.mNotifySimModeChangeRunnable[phoneId], 200);
            return;
        }
        if ("N/A".equals(readIccIdUsingPhoneId(phoneId))) {
            power = false;
            log("phoneId " + phoneId + " sim not insert, set  power  to " + false);
        }
        if (!OemConstant.isUiccSlotForbid(phoneId) || power) {
            refreshIccIdPreference(power, readIccIdUsingPhoneId(phoneId));
            log("after refreshIccIdPreference, updateImsServiceConfigForSlot ");
            ImsManager imsManager = ImsManager.getInstance(this.mContext, phoneId);
            if (imsManager != null) {
                imsManager.updateImsServiceConfig(true);
            }
        } else {
            log("phoneId " + phoneId + "notifySimModeChange: isUiccSlotForbid not refreshIccId");
        }
        log("Set Radio Power due to SIM mode change, power: " + power + ", phoneId: " + phoneId);
        setRadioPower(power, phoneId);
    }

    protected class MSimModeChangeRunnable implements Runnable {
        int mRetryMode;

        public MSimModeChangeRunnable(int mode) {
            this.mRetryMode = mode;
        }

        public void run() {
            RadioManager.this.notifyMSimModeChange(this.mRetryMode);
        }
    }

    public void notifyMSimModeChange(int mode) {
        log("MSIM mode changed, mode: " + mode);
        if (mode == -1) {
            log("Invalid mode, MSIM_MODE intent has no extra value");
        } else if (!isMSimModeSupport() || this.mAirplaneMode) {
            log("Airplane mode on or MSIM Mode option is closed, do nothing!");
        } else {
            boolean iccIdReady = true;
            int phoneId = 0;
            while (true) {
                if (phoneId >= this.mPhoneCount) {
                    break;
                } else if (!isIccIdReady(phoneId)) {
                    iccIdReady = false;
                    break;
                } else {
                    phoneId++;
                }
            }
            removeCallbacks(this.mNotifyMSimModeChangeRunnable);
            if (!iccIdReady) {
                this.mNotifyMSimModeChangeRunnable = new MSimModeChangeRunnable(mode);
                postDelayed(this.mNotifyMSimModeChangeRunnable, 200);
                return;
            }
            for (int phoneId2 = 0; phoneId2 < this.mPhoneCount; phoneId2++) {
                boolean singlePhonePower = ((1 << phoneId2) & mode) != 0;
                if ("N/A".equals(readIccIdUsingPhoneId(phoneId2))) {
                    singlePhonePower = false;
                    log("phoneId " + phoneId2 + " sim not insert, set  power  to " + false);
                }
                if (!OemConstant.isUiccSlotForbid(phoneId2) || singlePhonePower) {
                    refreshIccIdPreference(singlePhonePower, readIccIdUsingPhoneId(phoneId2));
                    log("after refreshIccIdPreference, updateImsServiceConfig ");
                    ImsManager imsManager = ImsManager.getInstance(this.mContext, phoneId2);
                    if (imsManager != null) {
                        imsManager.updateImsServiceConfig(true);
                    }
                } else {
                    log("phoneId " + phoneId2 + "notifySimModeChange: isUiccSlotForbid not refreshIccId");
                }
                log("Set Radio Power due to MSIM mode change, power: " + singlePhonePower + ", phoneId: " + phoneId2);
                setRadioPower(singlePhonePower, phoneId2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void refreshIccIdPreference(boolean power, String iccid) {
        log("refresh iccid preference");
        SharedPreferences.Editor editor = sIccidPreference.edit();
        if (power || "N/A".equals(iccid)) {
            removeIccIdFromPreference(editor, iccid);
        } else {
            putIccIdToPreference(editor, iccid);
        }
        editor.commit();
    }

    private void putIccIdToPreference(SharedPreferences.Editor editor, String iccid) {
        String printableIccid;
        if (iccid != null) {
            if ("N/A".equals(iccid)) {
                printableIccid = "N/A";
            } else {
                printableIccid = binaryToHex(getHashCode(SubscriptionInfo.givePrintableIccid(iccid)));
            }
            log("Add radio off SIM: " + printableIccid);
            editor.putInt(getHashCode(iccid), 0);
        }
    }

    private void removeIccIdFromPreference(SharedPreferences.Editor editor, String iccid) {
        String printableIccid;
        if (iccid != null) {
            if ("N/A".equals(iccid)) {
                printableIccid = "N/A";
            } else {
                printableIccid = binaryToHex(getHashCode(SubscriptionInfo.givePrintableIccid(iccid)));
            }
            log("Remove radio off SIM: " + printableIccid);
            editor.remove(getHashCode(iccid));
        }
    }

    public static void sendRequestBeforeSetRadioPower(boolean power, int phoneId) {
        log("Send request before EFUN, power:" + power + " phoneId:" + phoneId);
        notifyRadioPowerChange(power, phoneId);
    }

    public static boolean isPowerOnFeatureAllClosed() {
        if (!isFlightModePowerOffModemConfigEnabled() && !isMSimModeSupport()) {
            return true;
        }
        return false;
    }

    public static boolean isFlightModePowerOffModemConfigEnabled() {
        if (SystemProperties.get("vendor.ril.testmode").equals("1")) {
            return SystemProperties.get("vendor.ril.test.poweroffmd").equals("1");
        }
        if (isOP01 || isOP09) {
            if (SystemProperties.get("vendor.ril.atci.flightmode").equals("1")) {
                return true;
            }
            if (SystemProperties.get("vendor.gsm.sim.ril.testsim").equals("1") || SystemProperties.get("vendor.gsm.sim.ril.testsim.2").equals("1") || SystemProperties.get("vendor.gsm.sim.ril.testsim.3").equals("1") || SystemProperties.get("vendor.gsm.sim.ril.testsim.4").equals("1")) {
                return true;
            }
        }
        return mFlightModePowerOffModem;
    }

    public static boolean isFlightModePowerOffModemEnabled() {
        if (getInstance() == null) {
            log("Instance not exists, return config only");
            return isFlightModePowerOffModemConfigEnabled();
        } else if (isFlightModePowerOffModemConfigEnabled()) {
            return !getInstance().mIsWifiOn;
        } else {
            return false;
        }
    }

    public static boolean isModemPowerOff(int phoneId) {
        return getInstance().isModemOff(phoneId);
    }

    public static boolean isMSimModeSupport() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void resetSimInsertedStatus(int phoneId) {
        log("reset Sim InsertedStatus for Phone:" + phoneId);
        this.mSimInsertedStatus[phoneId] = -1;
    }

    public void handleMessage(Message msg) {
        int phoneIdForMsg = getCiIndex(msg);
        log("handleMessage msg.what: " + eventIdtoString(msg.what));
        switch (msg.what) {
            case 1:
            case 2:
            case 3:
            case 4:
                notifyRadioAvailable(msg.what - 1);
                return;
            case 5:
                forceSetRadioPower(true, phoneIdForMsg);
                return;
            case 6:
                StringBuilder sb = new StringBuilder();
                sb.append("handle EVENT_SET_MODEM_POWER_OFF_DONE -> ");
                sb.append(this.mModemPower ? "ON" : "OFF");
                log(sb.toString());
                if (!this.mModemPower) {
                    AsyncResult ar = (AsyncResult) msg.obj;
                    ModemPowerMessage powerMessage = (ModemPowerMessage) ar.userObj;
                    log("handleModemPowerMessage, message:" + powerMessage.toString());
                    if (ar.exception != null) {
                        log("handleModemPowerMessage, Unhandle ar.exception:" + ar.exception);
                    } else if (ar.result != null) {
                        log("handleModemPowerMessage, result:" + ar.result);
                    }
                    powerMessage.isFinish = true;
                    if (isSetModemPowerFinish()) {
                        cleanModemPowerMessage();
                        unMonitorModemPowerChangeDone();
                        this.mPowerSM.sendEvent(5);
                        return;
                    }
                    return;
                }
                log("EVENT_SET_MODEM_POWER_OFF_DONE: wrong state");
                return;
            case 7:
            case 8:
            case 9:
            default:
                super.handleMessage(msg);
                return;
            case 10:
            case 11:
            case 12:
            case 13:
                notifyDsbpStateChanged(msg.what, (AsyncResult) msg.obj);
                return;
        }
    }

    private void notifyDsbpStateChanged(int what, AsyncResult ar) {
        if (ar.exception == null && ar.result != null) {
            int state = ((Integer) ar.result).intValue();
            int phoneId = 0;
            switch (what) {
                case 10:
                    phoneId = 0;
                    break;
                case 11:
                    phoneId = 1;
                    break;
                case 12:
                    phoneId = 2;
                    break;
                case 13:
                    phoneId = 3;
                    break;
            }
            log("notifyDsbpStateChanged state:" + state + "phoneId:" + phoneId);
            if (state == 1) {
                this.mIsDsbpChanging[phoneId] = true;
                return;
            }
            this.mIsDsbpChanging[phoneId] = false;
            if (findMainCapabilityPhoneId() == phoneId && this.mIsPendingRadioByDsbpChanging) {
                this.mIsPendingRadioByDsbpChanging = false;
                setRadioPowerAfterCapabilitySwitch();
            }
        }
    }

    private String eventIdtoString(int what) {
        switch (what) {
            case 1:
            case 2:
            case 3:
            case 4:
                return "EVENT_RADIO_AVAILABLE";
            case 5:
                return "EVENT_VIRTUAL_SIM_ON";
            case 6:
            default:
                return null;
            case 7:
                return "EVENT_SET_SILENT_REBOOT_DONE";
            case 8:
                return "EVENT_REPORT_AIRPLANE_DONE";
            case 9:
                return "EVENT_REPORT_SIM_MODE_DONE";
            case 10:
            case 11:
            case 12:
            case 13:
                return "EVENT_DSBP_STATE_CHANGED";
        }
    }

    private int getCiIndex(Message msg) {
        Integer index = new Integer(0);
        if (msg != null) {
            if (msg.obj != null && (msg.obj instanceof Integer)) {
                index = (Integer) msg.obj;
            } else if (msg.obj != null && (msg.obj instanceof AsyncResult)) {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.userObj != null && (ar.userObj instanceof Integer)) {
                    index = (Integer) ar.userObj;
                }
            }
        }
        return index.intValue();
    }

    /* access modifiers changed from: protected */
    public boolean isModemOff(int phoneId) {
        int i = AnonymousClass2.$SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[TelephonyManager.getDefault().getMultiSimConfiguration().ordinal()];
        if (i == 1) {
            return SystemProperties.get("vendor.ril.ipo.radiooff").equals("1");
        }
        if (i != 2) {
            if (i != 3) {
                return SystemProperties.get("vendor.ril.ipo.radiooff").equals("1");
            }
            return SystemProperties.get("vendor.ril.ipo.radiooff").equals("1");
        } else if (phoneId == 0) {
            return SystemProperties.get("vendor.ril.ipo.radiooff").equals("1");
        } else {
            if (phoneId != 1) {
                return true;
            }
            return SystemProperties.get("vendor.ril.ipo.radiooff.2").equals("1");
        }
    }

    public static synchronized void registerForRadioPowerChange(String name, IRadioPower iRadioPower) {
        synchronized (RadioManager.class) {
            if (name == null) {
                name = REGISTRANTS_WITH_NO_NAME;
            }
            log(name + " registerForRadioPowerChange");
            mNotifyRadioPowerChange.put(iRadioPower, name);
        }
    }

    public static synchronized void unregisterForRadioPowerChange(IRadioPower iRadioPower) {
        synchronized (RadioManager.class) {
            log(mNotifyRadioPowerChange.get(iRadioPower) + " unregisterForRadioPowerChange");
            mNotifyRadioPowerChange.remove(iRadioPower);
        }
    }

    private static synchronized void notifyRadioPowerChange(boolean power, int phoneId) {
        synchronized (RadioManager.class) {
            for (Map.Entry<IRadioPower, String> e : mNotifyRadioPowerChange.entrySet()) {
                log("notifyRadioPowerChange: user:" + e.getValue());
                e.getKey().notifyRadioPowerChange(power, phoneId);
            }
        }
    }

    /* access modifiers changed from: private */
    public static void log(String s) {
        Rlog.d(LOG_TAG, "[RadioManager] " + s);
    }

    public boolean isAllowAirplaneModeChange() {
        log("always allow airplane mode");
        return true;
    }

    public void forceAllowAirplaneModeChange(boolean forceSwitch) {
    }

    /* access modifiers changed from: protected */
    public final Message[] monitorModemPowerChangeDone(boolean power, int phoneBitMap, int mainCapabilityPhoneId) {
        this.mModemPower = power;
        log("monitorModemPowerChangeDone, Power:" + power + ", PhoneBitMap:" + phoneBitMap + ", mainCapabilityPhoneId:" + mainCapabilityPhoneId + ", mPhoneCount:" + this.mPhoneCount);
        this.mNeedIgnoreMessageForChangeDone = false;
        this.mIsRadioUnavailable = false;
        int i = this.mPhoneCount;
        Message[] msgs = new Message[i];
        if (!this.mModemPower) {
            ModemPowerMessage[] messages = createMessage(power, phoneBitMap, mainCapabilityPhoneId, i);
            this.mModemPowerMessages = messages;
            for (int i2 = 0; i2 < messages.length; i2++) {
                if (messages[i2] != null) {
                    msgs[i2] = obtainMessage(6, messages[i2]);
                }
            }
        }
        return msgs;
    }

    /* access modifiers changed from: protected */
    public void unMonitorModemPowerChangeDone() {
        this.mNeedIgnoreMessageForChangeDone = true;
        Intent intent = new Intent(ACTION_AIRPLANE_CHANGE_DONE);
        intent.putExtra(EXTRA_AIRPLANE_MODE, true ^ this.mModemPower);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        for (int i = 0; i < this.mPhoneCount; i++) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone != null) {
                phone.mCi.unregisterForRadioStateChanged(this);
                log("unMonitorModemPowerChangeDone, phoneId = " + i);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean waitForReady(boolean state) {
        if (!waitRadioAvaliable(state)) {
            return false;
        }
        log("waitForReady, wait radio avaliable");
        this.mPowerSM.updateModemPowerState(state, this.mBitmapForPhoneCount, 2);
        return true;
    }

    private boolean waitRadioAvaliable(boolean state) {
        boolean wait = !this.mIsWifiOnlyDevice && !isRadioAvaliable();
        log("waitRadioAvaliable, state=" + state + ", wait=" + wait);
        return wait;
    }

    private boolean isRadioAvaliable() {
        for (int i = 0; i < this.mPhoneCount; i++) {
            if (!isRadioAvaliable(i)) {
                log("isRadioAvaliable=false, phoneId = " + i);
                return false;
            }
        }
        return true;
    }

    private boolean isRadioAvaliable(int phoneId) {
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone == null) {
            return false;
        }
        log("phoneId = " + phoneId + ", RadioState=" + phone.mCi.getRadioState());
        if (phone.mCi.getRadioState() != 2) {
            return true;
        }
        return false;
    }

    private boolean isRadioOn() {
        for (int i = 0; i < this.mPhoneCount; i++) {
            if (!isRadioOn(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean isRadioOn(int phoneId) {
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone != null && phone.mCi.getRadioState() == 1) {
            return true;
        }
        return false;
    }

    private boolean isRadioUnavailable() {
        for (int i = 0; i < this.mPhoneCount; i++) {
            if (isRadioAvaliable(i)) {
                log("isRadioUnavailable=false, phoneId = " + i);
                return false;
            }
        }
        return true;
    }

    private final boolean isSetModemPowerFinish() {
        if (this.mModemPowerMessages == null) {
            return true;
        }
        int i = 0;
        while (true) {
            ModemPowerMessage[] modemPowerMessageArr = this.mModemPowerMessages;
            if (i >= modemPowerMessageArr.length) {
                return true;
            }
            if (modemPowerMessageArr[i] != null) {
                log("isSetModemPowerFinish [" + i + "]: " + this.mModemPowerMessages[i]);
                if (!this.mModemPowerMessages[i].isFinish) {
                    return false;
                }
            } else {
                log("isSetModemPowerFinish [" + i + "]: MPMsg is null");
            }
            i++;
        }
    }

    private final void cleanModemPowerMessage() {
        log("cleanModemPowerMessage");
        if (this.mModemPowerMessages != null) {
            int i = 0;
            while (true) {
                ModemPowerMessage[] modemPowerMessageArr = this.mModemPowerMessages;
                if (i < modemPowerMessageArr.length) {
                    modemPowerMessageArr[i] = null;
                    i++;
                } else {
                    this.mModemPowerMessages = null;
                    return;
                }
            }
        }
    }

    private static final class ModemPowerMessage {
        public boolean isFinish = false;
        private final int mPhoneId;

        public ModemPowerMessage(int phoneId) {
            this.mPhoneId = phoneId;
        }

        public String toString() {
            return "MPMsg [mPhoneId=" + this.mPhoneId + ", isFinish=" + this.isFinish + "]";
        }
    }

    private static final ModemPowerMessage[] createMessage(boolean power, int phoneBitMap, int mainCapabilityPhoneId, int phoneCount) {
        TelephonyManager.MultiSimVariants config = TelephonyManager.getDefault().getMultiSimConfiguration();
        log("createMessage, config:" + config);
        ModemPowerMessage[] msgs = new ModemPowerMessage[phoneCount];
        int i = AnonymousClass2.$SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[config.ordinal()];
        if (i == 1 || i == 2 || i == 3) {
            msgs[mainCapabilityPhoneId] = new ModemPowerMessage(mainCapabilityPhoneId);
        } else {
            int phoneId = PhoneFactory.getDefaultPhone().getPhoneId();
            msgs[phoneId] = new ModemPowerMessage(phoneId);
        }
        for (int i2 = 0; i2 < phoneCount; i2++) {
            if (msgs[i2] != null) {
                log("createMessage, [" + i2 + "]: " + msgs[i2].toString());
            }
        }
        return msgs;
    }

    private void registerListener() {
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mCi[i].registerForVirtualSimOn(this, 5, null);
            this.mCi[i].registerForAvailable(this, EVENT_RADIO_AVAILABLE[i], (Object) null);
            this.mCi[i].registerForDsbpStateChanged(this, EVENT_DSBP_STATE_CHANGED[i], null);
        }
    }

    private boolean isRequiredRadioOff(String iccid) {
        if (sIccidPreference.contains(getHashCode(iccid))) {
            return true;
        }
        return false;
    }

    public String getHashCode(String iccid) {
        try {
            MessageDigest alga = MessageDigest.getInstance("SHA-256");
            alga.update(iccid.getBytes());
            return new String(alga.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("isRequiredRadioOff SHA-256 must exist");
        }
    }

    /* access modifiers changed from: private */
    public class PowerSM extends StateMachine {
        /* access modifiers changed from: private */
        public int mCurrentModemCause = 0;
        public boolean mCurrentModemPower = true;
        /* access modifiers changed from: private */
        public int mDesiredModemCause = 0;
        public boolean mDesiredModemPower = true;
        protected PowerIdleState mIdleState = new PowerIdleState();
        protected int mPhoneBitMap;
        protected PowerTurnOffState mTurnOffState = new PowerTurnOffState();
        protected PowerTurnOnState mTurnOnState = new PowerTurnOnState();
        private PowerSM self = null;

        PowerSM(String name) {
            super(name);
            addState(this.mIdleState);
            addState(this.mTurnOnState);
            addState(this.mTurnOffState);
            setInitialState(this.mIdleState);
        }

        /* access modifiers changed from: private */
        public void updateModemPowerState(boolean power, int phoneBitMap, int cause) {
            if ((!power) && RadioManager.isUnderCryptKeeper()) {
                log("Skip MODEM_POWER_OFF due to CryptKeeper mode");
                return;
            }
            this.mPhoneBitMap = phoneBitMap;
            int i = 2;
            if (4 == cause) {
                if (RadioManager.this.mAirplaneMode && RadioManager.isFlightModePowerOffModemConfigEnabled()) {
                    this.mDesiredModemCause = 4 | this.mDesiredModemCause;
                    if (RadioManager.this.mIsWifiOn) {
                        this.mDesiredModemPower = true;
                    } else {
                        this.mDesiredModemPower = false;
                    }
                }
                if (power) {
                    i = 1;
                }
                sendEvent(i);
            } else if (2 == cause) {
                this.mDesiredModemCause |= 2;
                this.mDesiredModemPower = power;
                if (power) {
                    i = 1;
                }
                sendEvent(i);
            } else if (16 == cause) {
                this.mDesiredModemCause |= 16;
                this.mDesiredModemPower = power;
                sendEvent(1);
            } else if (8 == cause) {
                this.mDesiredModemCause |= 8;
                this.mDesiredModemPower = power;
                if (power) {
                    i = 1;
                }
                sendEvent(i);
            } else if (64 == cause) {
                this.mCurrentModemPower = true;
                this.mDesiredModemPower = false;
                sendEvent(2);
            } else if (128 == cause) {
                sendEvent(6);
            }
        }

        private void sendEvent(int event, int arg1) {
            Rlog.i(RadioManager.LOG_TAG, "sendEvent: " + PowerEvent.print(event));
            Message msg = Message.obtain(getHandler(), event);
            msg.arg1 = arg1;
            getHandler().sendMessage(msg);
        }

        /* access modifiers changed from: private */
        public void sendEvent(int event) {
            Rlog.i(RadioManager.LOG_TAG, "sendEvent: " + PowerEvent.print(event));
            getHandler().sendMessage(Message.obtain(getHandler(), event));
        }

        private class PowerIdleState extends State {
            private PowerIdleState() {
            }

            public void enter() {
                Rlog.i(RadioManager.LOG_TAG, "PowerIdleState: enter");
                PowerSM powerSM = PowerSM.this;
                powerSM.log("mDesiredModemPower: " + PowerSM.this.mDesiredModemPower + " mCurrentModemPower: " + PowerSM.this.mCurrentModemPower);
                if (RadioManager.this.mPowerSM.mDesiredModemPower == RadioManager.this.mPowerSM.mCurrentModemPower) {
                    return;
                }
                if (RadioManager.this.mPowerSM.mDesiredModemPower) {
                    RadioManager.this.mPowerSM.transitionTo(RadioManager.this.mPowerSM.mTurnOnState);
                } else {
                    RadioManager.this.mPowerSM.transitionTo(RadioManager.this.mPowerSM.mTurnOffState);
                }
            }

            public void exit() {
                Rlog.i(RadioManager.LOG_TAG, "PowerIdleState: exit");
            }

            /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
             method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
             arg types: [java.lang.String, int]
             candidates:
              ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
            public boolean processMessage(Message msg) {
                Rlog.i(RadioManager.LOG_TAG, "processMessage: " + PowerEvent.print(msg.what));
                int i = msg.what;
                if (i != 1) {
                    if (i != 2) {
                        if (i == 3) {
                            RadioManager.this.mPowerSM.mCurrentModemPower = true;
                            if (RadioManager.this.mPowerSM.mDesiredModemPower != RadioManager.this.mPowerSM.mCurrentModemPower) {
                                if (RadioManager.this.mPowerSM.mDesiredModemPower) {
                                    RadioManager.this.mPowerSM.transitionTo(RadioManager.this.mPowerSM.mTurnOnState);
                                } else {
                                    RadioManager.this.mPowerSM.transitionTo(RadioManager.this.mPowerSM.mTurnOffState);
                                }
                            }
                        } else if (i != 5) {
                            Rlog.i(RadioManager.LOG_TAG, "un-expected event, stay at idle");
                        } else {
                            RadioManager.this.mPowerSM.mCurrentModemPower = false;
                            if (RadioManager.this.mPowerSM.mDesiredModemPower != RadioManager.this.mPowerSM.mCurrentModemPower) {
                                RadioManager.this.mPowerSM.transitionTo(RadioManager.this.mPowerSM.mDesiredModemPower ? RadioManager.this.mPowerSM.mTurnOnState : RadioManager.this.mPowerSM.mTurnOffState);
                            } else {
                                Rlog.i(RadioManager.LOG_TAG, "the same power state: " + PowerEvent.print(msg.what));
                            }
                        }
                    } else if (RadioManager.this.mPowerSM.mDesiredModemPower != RadioManager.this.mPowerSM.mCurrentModemPower) {
                        RadioManager.this.mPowerSM.transitionTo(RadioManager.this.mPowerSM.mTurnOffState);
                    } else {
                        Rlog.i(RadioManager.LOG_TAG, "the same power state: " + PowerEvent.print(msg.what));
                    }
                } else if (RadioManager.this.mPowerSM.mDesiredModemPower != RadioManager.this.mPowerSM.mCurrentModemPower) {
                    RadioManager.this.mPowerSM.transitionTo(RadioManager.this.mPowerSM.mTurnOnState);
                } else {
                    Rlog.i(RadioManager.LOG_TAG, "the same power state: " + PowerEvent.print(msg.what));
                    Intent intent = new Intent(RadioManager.ACTION_MODEM_POWER_NO_CHANGE);
                    intent.putExtra(RadioManager.EXTRA_MODEM_POWER, true);
                    RadioManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                }
                return true;
            }
        }

        private class PowerTurnOnState extends State {
            private PowerTurnOnState() {
            }

            public void enter() {
                Rlog.i(RadioManager.LOG_TAG, "PowerTurnOnState: enter");
                if (!RadioManager.this.waitForReady(true) && !MtkProxyController.getInstance().isCapabilitySwitching()) {
                    RadioManager.this.mPowerSM.mCurrentModemPower = true;
                    int unused = RadioManager.this.mPowerSM.mCurrentModemCause = RadioManager.this.mPowerSM.mDesiredModemCause;
                    RadioManager.this.setModemPower(true, RadioManager.this.mPowerSM.mPhoneBitMap);
                }
            }

            public void exit() {
                Rlog.i(RadioManager.LOG_TAG, "PowerTurnOnState: exit");
            }

            public boolean processMessage(Message msg) {
                Rlog.i(RadioManager.LOG_TAG, "processMessage: " + PowerEvent.print(msg.what));
                int i = msg.what;
                if (i == 3) {
                    PowerSM powerSM = PowerSM.this;
                    powerSM.mCurrentModemPower = true;
                    RadioManager.this.mPowerSM.transitionTo(RadioManager.this.mPowerSM.mIdleState);
                } else if (i == 4 || i == 6) {
                    RadioManager.this.mPowerSM.transitionTo(RadioManager.this.mPowerSM.mIdleState);
                } else {
                    Rlog.i(RadioManager.LOG_TAG, "un-expected event, stay at PowerTurnOnState");
                }
                return true;
            }
        }

        private class PowerTurnOffState extends State {
            private PowerTurnOffState() {
            }

            public void enter() {
                Rlog.i(RadioManager.LOG_TAG, "PowerTurnOffState: enter");
                if (!RadioManager.this.waitForReady(false) && !MtkProxyController.getInstance().isCapabilitySwitching()) {
                    RadioManager.this.mPowerSM.mCurrentModemPower = false;
                    int unused = RadioManager.this.mPowerSM.mCurrentModemCause = RadioManager.this.mPowerSM.mDesiredModemCause;
                    RadioManager.this.setModemPower(false, RadioManager.this.mPowerSM.mPhoneBitMap);
                }
            }

            public void exit() {
                Rlog.i(RadioManager.LOG_TAG, "PowerTurnOffState: exit");
            }

            public boolean processMessage(Message msg) {
                Rlog.i(RadioManager.LOG_TAG, "processMessage: " + PowerEvent.print(msg.what));
                int i = msg.what;
                if (i == 3) {
                    PowerSM powerSM = PowerSM.this;
                    powerSM.mCurrentModemPower = true;
                    RadioManager.this.mPowerSM.transitionTo(RadioManager.this.mPowerSM.mIdleState);
                } else if (i == 5 || i == 6) {
                    RadioManager.this.mPowerSM.transitionTo(RadioManager.this.mPowerSM.mIdleState);
                } else {
                    Rlog.i(RadioManager.LOG_TAG, "un-expected event, stay at PowerTurnOffState");
                }
                return true;
            }
        }
    }

    static class PowerEvent {
        static final int EVENT_MODEM_POWER_OFF = 2;
        static final int EVENT_MODEM_POWER_OFF_DONE = 5;
        static final int EVENT_MODEM_POWER_ON = 1;
        static final int EVENT_MODEM_POWER_ON_DONE = 4;
        static final int EVENT_RADIO_AVAILABLE = 3;
        static final int EVENT_SIM_SWITCH_DONE = 6;
        static final int EVENT_START = 0;

        PowerEvent() {
        }

        public static String print(int eventCode) {
            switch (eventCode) {
                case 1:
                    return "EVENT_MODEM_POWER_ON";
                case 2:
                    return "EVENT_MODEM_POWER_OFF";
                case 3:
                    return "EVENT_RADIO_AVAILABLE";
                case 4:
                    return "EVENT_MODEM_POWER_ON_DONE";
                case 5:
                    return "EVENT_MODEM_POWER_OFF_DONE";
                case 6:
                    return "EVENT_SIM_SWITCH_DONE";
                default:
                    throw new IllegalArgumentException("Invalid eventCode: " + eventCode);
            }
        }
    }

    static class ModemPowerCasue {
        static final int CAUSE_AIRPLANE_MODE = 2;
        static final int CAUSE_ECC = 16;
        static final int CAUSE_FORCE = 32;
        static final int CAUSE_IPO = 8;
        static final int CAUSE_RADIO_AVAILABLE = 64;
        static final int CAUSE_SIM_SWITCH = 128;
        static final int CAUSE_START = 0;
        static final int CAUSE_WIFI_CALLING = 4;

        ModemPowerCasue() {
        }

        public static String print(int eventCode) {
            if (eventCode == 2) {
                return "CAUSE_AIRPLANE_MODE";
            }
            if (eventCode == 4) {
                return "CAUSE_WIFI_CALLING";
            }
            if (eventCode == 8) {
                return "CAUSE_IPO";
            }
            if (eventCode == 16) {
                return "CAUSE_ECC";
            }
            if (eventCode == 32) {
                return "CAUSE_FORCE";
            }
            if (eventCode == 64) {
                return "CAUSE_RADIO_AVAILABLE";
            }
            throw new IllegalArgumentException("Invalid eventCode: " + eventCode);
        }
    }

    private String binaryToHex(String binaryStr) {
        return String.format("%040x", new BigInteger(1, binaryStr.getBytes()));
    }
}
