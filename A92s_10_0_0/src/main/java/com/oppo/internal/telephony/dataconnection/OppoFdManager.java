package com.oppo.internal.telephony.dataconnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.AsyncResult;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Display;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.util.ReflectionHelper;
import java.util.ArrayList;

public class OppoFdManager extends Handler {
    private static final int COUNT_R8 = 4;
    private static final boolean DBG = true;
    private static final int DIALOG_SEND_FD = 202;
    private static final int DIALOG_SET_FAILED = 201;
    private static final int DISABLE_FASTDORMANCY = 0;
    private static final int ENABLE_FASTDORMANCY = 1;
    private static final int EVENT_BASE = 0;
    private static final int EVENT_FD_MODE_SET = 0;
    private static final int EVENT_RADIO_ON = 1;
    private static final String FK_SIM_SWITCH = "persist.vendor.radio.simswitch";
    private static final boolean IN_CHARGING = true;
    private static final String LOG_TAG = "OppoFdManager";
    private static final int MSG_SEND_FD = 102;
    private static final int MSG_SET_TIME = 101;
    private static final boolean MTK_FD_SUPPORT;
    private static final boolean NOT_IN_CHARGING = false;
    private static final String PROPERTY_FD_ON_CHARGE = "persist.vendor.fd.on.charge";
    private static final String PROPERTY_FD_SCREEN_OFF_ONLY = "vendor.fd.screen.off.only";
    private static final String PROPERTY_RIL_FD_MODE = "vendor.ril.fd.mode";
    private static final SparseArray<OppoFdManager> sOppoFdManagers = new SparseArray<>();
    private static String[] sTimerValueForBouygues = {"30", "30", "30", "30"};
    private static String[] sTimerValueForOrange = {"30", "50", "30", "50"};
    private static String[] sTimerValueForSwisscom = {"50", "300", "50", "300"};
    private static String[] sTimerValueForTmobile = {"30", "50", "30", "50"};
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.oppo.internal.telephony.dataconnection.OppoFdManager.AnonymousClass2 */

        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            OppoFdManager oppoFdManager = OppoFdManager.this;
            oppoFdManager.onScreenSwitched(oppoFdManager.isScreenOn());
        }
    };
    private DisplayManager mDisplayManager;
    private int mEnableFdOnCharing = Integer.parseInt(SystemProperties.get(PROPERTY_FD_ON_CHARGE, "0"));
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.oppo.internal.telephony.dataconnection.OppoFdManager.AnonymousClass1 */

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            boolean z = false;
            switch (action.hashCode()) {
                case -1754841973:
                    if (action.equals("android.net.conn.TETHER_STATE_CHANGED")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -54942926:
                    if (action.equals("android.os.action.DISCHARGING")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -25388475:
                    if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 948344062:
                    if (action.equals("android.os.action.CHARGING")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                OppoFdManager.this.logd("mIntentReceiver: Received ACTION_CHARGING");
                OppoFdManager.this.onChargingModeSwitched(true);
            } else if (c == 1) {
                OppoFdManager.this.logd("mIntentReceiver: Received ACTION_DISCHARGING");
                OppoFdManager.this.onChargingModeSwitched(false);
            } else if (c == 2) {
                ArrayList<String> active = intent.getStringArrayListExtra("tetherArray");
                OppoFdManager oppoFdManager = OppoFdManager.this;
                if (active != null && active.size() > 0) {
                    z = true;
                }
                boolean unused = oppoFdManager.mIsTetheredMode = z;
                OppoFdManager.this.logd("mIntentReceiver: Received ACTION_TETHER_STATE_CHANGED mIsTetheredMode = " + OppoFdManager.this.mIsTetheredMode);
                OppoFdManager.this.onTetheringSwitched();
            } else if (c != 3) {
                OppoFdManager.this.logw("mIntentReceiver: weird, should never be here!");
            } else {
                OppoFdManager.this.logd("mIntentReceiver: Received ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
                OppoFdManager.this.onDefaultDataSwitched();
            }
        }
    };
    private boolean mIsCharging = false;
    private boolean mIsScreenOn = true;
    /* access modifiers changed from: private */
    public boolean mIsTetheredMode = false;
    private Phone mPhone;
    private Handler mResponseHander = new Handler() {
        /* class com.oppo.internal.telephony.dataconnection.OppoFdManager.AnonymousClass3 */

        public void handleMessage(Message msg) {
            Rlog.i(OppoFdManager.LOG_TAG, "Receive msg from modem");
            if (msg.what == 101) {
                Rlog.i(OppoFdManager.LOG_TAG, "Receive MSG_SET_TIME");
                Throwable th = ((AsyncResult) msg.obj).exception;
            } else if (msg.what == 102) {
                Rlog.i(OppoFdManager.LOG_TAG, "Receive MSG_SEND_FD");
                Throwable th2 = ((AsyncResult) msg.obj).exception;
            }
        }
    };

    static {
        boolean z = true;
        if (Integer.parseInt(SystemProperties.get("ro.vendor.mtk_fd_support", "0")) != 1) {
            z = false;
        }
        MTK_FD_SUPPORT = z;
    }

    public static OppoFdManager getInstance(Phone phone) {
        if (!MTK_FD_SUPPORT || phone == null) {
            Rlog.e(LOG_TAG, "Fast dormancy feature is not enabled or FdManager initialize fail");
            return null;
        }
        int phoneId = getPhoneId(phone);
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            Rlog.e(LOG_TAG, "phoneId " + phoneId + " is invalid!");
            return null;
        }
        OppoFdManager fdMgr = sOppoFdManagers.get(phoneId);
        if (fdMgr != null) {
            return fdMgr;
        }
        Rlog.d(LOG_TAG, "OppoFdManager " + phoneId + " doesn't exist, create one");
        OppoFdManager fdMgr2 = new OppoFdManager(phone);
        sOppoFdManagers.put(phoneId, fdMgr2);
        return fdMgr2;
    }

    private OppoFdManager(Phone p) {
        this.mPhone = p;
        this.mIsCharging = isDeviceCharging();
        logd("Initial OppoFdManager: mIsCharging = " + this.mIsCharging);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.os.action.CHARGING");
        filter.addAction("android.os.action.DISCHARGING");
        filter.addAction("android.net.conn.TETHER_STATE_CHANGED");
        filter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone);
        this.mPhone.mCi.registerForOn(this, 1, (Object) null);
        this.mDisplayManager = (DisplayManager) this.mPhone.getContext().getSystemService("display");
        this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
    }

    public void dispose() {
        logd("Dispose sOppoFdManagers");
        if (MTK_FD_SUPPORT) {
            this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
            this.mPhone.mCi.unregisterForOn(this);
            this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
            sOppoFdManagers.remove(getPhoneId(this.mPhone));
        }
    }

    public void handleMessage(Message msg) {
        logd("handleMessage: msg.what = " + msg.what);
        int i = msg.what;
        if (i != 0) {
            if (i != 1) {
                logw("handleMessage: weird, should never be here!");
            } else {
                onRadioOn();
            }
        } else if (((AsyncResult) msg.obj).exception != null) {
            loge("handleMessage: RIL_REQUEST_SET_FD_MODE error!");
        }
    }

    /* access modifiers changed from: private */
    public boolean isScreenOn() {
        Display[] displays = ((DisplayManager) this.mPhone.getContext().getSystemService("display")).getDisplays();
        if (displays != null) {
            for (Display display : displays) {
                if (display.getState() == 2) {
                    logd("isScreenOn: Screen " + Display.typeToString(display.getType()) + " on");
                    return true;
                }
            }
            logd("isScreenOn: Screens all off");
            return false;
        }
        logd("isScreenOn: No displays found");
        return false;
    }

    private boolean isDeviceCharging() {
        return ((BatteryManager) this.mPhone.getContext().getSystemService("batterymanager")).isCharging();
    }

    private boolean isFdAllowed() {
        if (Integer.parseInt(SystemProperties.get(PROPERTY_RIL_FD_MODE, "0")) != 1) {
            return false;
        }
        int subId = this.mPhone.getSubId();
        int dataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
        logd("isFdAllowed: subId = " + subId + " dataSubId = " + dataSubId);
        return SubscriptionManager.isUsableSubIdValue(subId) && subId == dataSubId;
    }

    private boolean shouldEnableFd() {
        if (isFdEnabledOnlyWhenScreenOff() && this.mIsScreenOn) {
            return false;
        }
        if ((!this.mIsCharging || this.mEnableFdOnCharing != 0) && !this.mIsTetheredMode) {
            return true;
        }
        return false;
    }

    private void updateFdModeIfNeeded() {
        try {
            if (isFdAllowed() && isOperatorSupport()) {
                if (shouldEnableFd()) {
                    ReflectionHelper.callMethod(this.mPhone.mCi, "com.mediatek.internal.telephony.MtkRIL", "setFdMode", new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, Message.class}, new Object[]{1, -1, -1, obtainMessage(0)});
                    setFdConf();
                    return;
                }
                ReflectionHelper.callMethod(this.mPhone.mCi, "com.mediatek.internal.telephony.MtkRIL", "setFdMode", new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, Message.class}, new Object[]{0, -1, -1, obtainMessage(0)});
            }
        } catch (NumberFormatException e) {
            Rlog.d(LOG_TAG, e.toString());
        } catch (Exception e2) {
            Rlog.d(LOG_TAG, e2.toString());
        }
    }

    private void onRadioOn() {
        logd("onRadioOn: update fd status when radio on");
        updateFdModeIfNeeded();
    }

    /* access modifiers changed from: private */
    public void onScreenSwitched(boolean isScreenOn) {
        logd("onScreenSwitched: screenOn = " + isScreenOn);
        if (this.mIsScreenOn != isScreenOn) {
            this.mIsScreenOn = isScreenOn;
            if (isFdEnabledOnlyWhenScreenOff()) {
                updateFdModeIfNeeded();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onChargingModeSwitched(boolean isCharging) {
        boolean preCharging = this.mIsCharging;
        this.mIsCharging = isCharging;
        int preEnableFdonCharging = this.mEnableFdOnCharing;
        try {
            this.mEnableFdOnCharing = Integer.parseInt(SystemProperties.get(PROPERTY_FD_ON_CHARGE, "0"));
        } catch (NumberFormatException e) {
            Rlog.d(LOG_TAG, e.toString());
        } catch (Exception e2) {
            Rlog.d(LOG_TAG, e2.toString());
        }
        logd("onChargingModeSwitched: preCharging = " + preCharging + " mIsCharging = " + this.mIsCharging + " preEnableFdonCharging = " + preEnableFdonCharging + " mEnableFdOnCharing = " + this.mEnableFdOnCharing);
        if (preCharging != this.mIsCharging || preEnableFdonCharging != this.mEnableFdOnCharing) {
            updateFdModeIfNeeded();
        }
    }

    /* access modifiers changed from: private */
    public void onTetheringSwitched() {
        updateFdModeIfNeeded();
    }

    /* access modifiers changed from: private */
    public void onDefaultDataSwitched() {
        updateFdModeIfNeeded();
    }

    private static boolean isFdEnabledOnlyWhenScreenOff() {
        return SystemProperties.getInt(PROPERTY_FD_SCREEN_OFF_ONLY, 0) == 1;
    }

    private static int getPhoneId(Phone phone) {
        return phone.getPhoneId();
    }

    /* access modifiers changed from: private */
    public void logd(String s) {
        Rlog.d(LOG_TAG, "[phoneId" + getPhoneId(this.mPhone) + "]" + s);
    }

    /* access modifiers changed from: private */
    public void logw(String s) {
        Rlog.w(LOG_TAG, "[phoneId" + getPhoneId(this.mPhone) + "]" + s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, "[phoneId" + getPhoneId(this.mPhone) + "]" + s);
    }

    private void setFdConf() {
        try {
            if (isEuOrange()) {
                setFdTimerValue(sTimerValueForOrange);
            } else if (isNLTmobile()) {
                setFdTimerValue(sTimerValueForTmobile);
            } else if (isFRBouygues()) {
                setFdTimerValue(sTimerValueForBouygues);
            } else if (isCHSwisscom()) {
                setFdTimerValue(sTimerValueForSwisscom);
            }
            logd("Update fast dormancy idle timer executed.");
        } catch (NumberFormatException e) {
            Rlog.d(LOG_TAG, e.toString());
        }
        logd("setFdConf setFdTimerValue ");
    }

    private boolean isEuOrange() {
        try {
            String regionmark = SystemProperties.get("ro.oppo.regionmark", "");
            String operator = SystemProperties.get("ro.oppo.operator", "");
            String country = SystemProperties.get("ro.oppo.euex.country", "");
            logd("regionmark=" + regionmark + ", operator=" + operator + ", country=" + country);
            if (TextUtils.isEmpty(regionmark) || !"EUEX".equals(regionmark) || TextUtils.isEmpty(operator) || !"ORANGE".equals(operator)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isFRBouygues() {
        try {
            String regionmark = SystemProperties.get("ro.oppo.regionmark", "");
            String operator = SystemProperties.get("ro.oppo.operator", "");
            String country = SystemProperties.get("ro.oppo.euex.country", "");
            logd("regionmark=" + regionmark + ", operator=" + operator + ", country=" + country);
            if (TextUtils.isEmpty(regionmark) || !"EUEX".equals(regionmark) || TextUtils.isEmpty(operator) || !"BOUYGUE".equals(operator) || TextUtils.isEmpty(country) || !"FR".equals(country)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNLTmobile() {
        try {
            String regionmark = SystemProperties.get("ro.oppo.regionmark", "");
            String operator = SystemProperties.get("ro.oppo.operator", "");
            String country = SystemProperties.get("ro.oppo.euex.country", "");
            logd("regionmark=" + regionmark + ", operator=" + operator + ", country=" + country);
            if (TextUtils.isEmpty(regionmark) || !"EUEX".equals(regionmark) || TextUtils.isEmpty(operator) || !"TMOBILE".equals(operator) || TextUtils.isEmpty(country) || !"NL".equals(country)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isCHSwisscom() {
        try {
            String regionmark = SystemProperties.get("ro.oppo.regionmark", "");
            String operator = SystemProperties.get("ro.oppo.operator", "");
            String country = SystemProperties.get("ro.oppo.euex.country", "");
            logd("regionmark=" + regionmark + ", operator=" + operator + ", country=" + country);
            if (TextUtils.isEmpty(regionmark) || !"EUEX".equals(regionmark) || TextUtils.isEmpty(operator) || !"SWISSCOM".equals(operator) || TextUtils.isEmpty(country) || !"CH".equals(country)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int setFdTimerValue(String[] sOperationTimerValue) {
        int i = 0;
        while (i < 4) {
            try {
                Integer.valueOf(sOperationTimerValue[i]);
                Rlog.i(LOG_TAG, "sOperationTimerValue[" + i + "] = " + sOperationTimerValue[i]);
                sendAtCommand(new String[]{"STATUS_SYNC", "EM_FASTDORMANCY_SYNC:2," + i + "," + sOperationTimerValue[i]}, 102);
                i++;
            } catch (NumberFormatException e) {
                Rlog.e(LOG_TAG, "NumberFormatException");
                return 1;
            }
        }
        logd("setFdTimerValue: sTimerValue = " + sOperationTimerValue[0] + ", " + sOperationTimerValue[1] + ", " + sOperationTimerValue[2] + ", " + sOperationTimerValue[3]);
        return 0;
    }

    private void sendAtCommand(String[] command, int msg) {
        Rlog.i(LOG_TAG, "sendAtCommand() " + command[0]);
        this.mPhone.invokeOemRilRequestStrings(command, this.mResponseHander.obtainMessage(msg));
    }

    public boolean isOperatorSupport() {
        if (!this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.version.exp")) {
            return true;
        }
        String region = SystemProperties.get("persist.sys.oppo.region", "CN");
        String operator = SystemProperties.get("ro.oppo.operator", "");
        logd("isOperatorSupport region:" + region + " operator:" + operator);
        if ("MX".equals(region)) {
            return false;
        }
        if (!"AU".equals(region) || !"OPTUS".equals(operator)) {
            return true;
        }
        return false;
    }
}
