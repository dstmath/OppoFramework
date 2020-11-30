package com.mediatek.internal.telephony.dataconnection;

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
import android.util.SparseArray;
import android.view.Display;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionController;
import java.util.ArrayList;

public class FdManager extends Handler {
    private static final boolean DBG = true;
    private static final int DISABLE_FASTDORMANCY = 0;
    private static final int ENABLE_FASTDORMANCY = 1;
    private static final int EVENT_BASE = 0;
    private static final int EVENT_FD_MODE_SET = 0;
    private static final int EVENT_RADIO_ON = 1;
    private static final boolean IN_CHARGING = true;
    private static final String LOG_TAG = "FdManager";
    private static final boolean MTK_FD_SUPPORT;
    private static final boolean NOT_IN_CHARGING = false;
    private static final String PROPERTY_FD_ON_CHARGE = "persist.vendor.fd.on.charge";
    private static final String PROPERTY_FD_SCREEN_OFF_ONLY = "vendor.fd.screen.off.only";
    private static final String PROPERTY_RIL_FD_MODE = "vendor.ril.fd.mode";
    private static final SparseArray<FdManager> sFdManagers = new SparseArray<>();
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.mediatek.internal.telephony.dataconnection.FdManager.AnonymousClass2 */

        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            FdManager fdManager = FdManager.this;
            fdManager.onScreenSwitched(fdManager.isScreenOn());
        }
    };
    private DisplayManager mDisplayManager;
    private int mEnableFdOnCharing = Integer.parseInt(SystemProperties.get(PROPERTY_FD_ON_CHARGE, "0"));
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.dataconnection.FdManager.AnonymousClass1 */

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
                FdManager.this.logd("mIntentReceiver: Received ACTION_CHARGING");
                FdManager.this.onChargingModeSwitched(true);
            } else if (c == 1) {
                FdManager.this.logd("mIntentReceiver: Received ACTION_DISCHARGING");
                FdManager.this.onChargingModeSwitched(false);
            } else if (c == 2) {
                ArrayList<String> active = intent.getStringArrayListExtra("tetherArray");
                FdManager fdManager = FdManager.this;
                if (active != null && active.size() > 0) {
                    z = true;
                }
                fdManager.mIsTetheredMode = z;
                FdManager.this.logd("mIntentReceiver: Received ACTION_TETHER_STATE_CHANGED mIsTetheredMode = " + FdManager.this.mIsTetheredMode);
                FdManager.this.onTetheringSwitched();
            } else if (c != 3) {
                FdManager.this.logw("mIntentReceiver: weird, should never be here!");
            } else {
                FdManager.this.logd("mIntentReceiver: Received ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
                FdManager.this.onDefaultDataSwitched();
            }
        }
    };
    private boolean mIsCharging = false;
    private boolean mIsScreenOn = true;
    private boolean mIsTetheredMode = false;
    private Phone mPhone;

    static {
        boolean z = true;
        if (Integer.parseInt(SystemProperties.get("ro.vendor.mtk_fd_support", "0")) != 1) {
            z = false;
        }
        MTK_FD_SUPPORT = z;
    }

    public static FdManager getInstance(Phone phone) {
        if (!MTK_FD_SUPPORT || phone == null) {
            Rlog.e(LOG_TAG, "Fast dormancy feature is not enabled or FdManager initialize fail");
            return null;
        }
        int phoneId = getPhoneId(phone);
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            Rlog.e(LOG_TAG, "phoneId " + phoneId + " is invalid!");
            return null;
        }
        FdManager fdMgr = sFdManagers.get(phoneId);
        if (fdMgr != null) {
            return fdMgr;
        }
        Rlog.d(LOG_TAG, "FdManager " + phoneId + " doesn't exist, create one");
        FdManager fdMgr2 = new FdManager(phone);
        sFdManagers.put(phoneId, fdMgr2);
        return fdMgr2;
    }

    private FdManager(Phone p) {
        this.mPhone = p;
        this.mIsCharging = isDeviceCharging();
        logd("Initial FdManager: mIsCharging = " + this.mIsCharging);
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
        logd("Dispose FdManager");
        if (MTK_FD_SUPPORT) {
            this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
            this.mPhone.mCi.unregisterForOn(this);
            this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
            sFdManagers.remove(getPhoneId(this.mPhone));
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
    /* access modifiers changed from: public */
    private boolean isScreenOn() {
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
            if (!isFdAllowed()) {
                return;
            }
            if (shouldEnableFd()) {
                this.mPhone.mCi.setFdMode(1, -1, -1, obtainMessage(0));
            } else {
                this.mPhone.mCi.setFdMode(0, -1, -1, obtainMessage(0));
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
    /* access modifiers changed from: public */
    private void onScreenSwitched(boolean isScreenOn) {
        logd("onScreenSwitched: screenOn = " + isScreenOn);
        if (this.mIsScreenOn != isScreenOn) {
            this.mIsScreenOn = isScreenOn;
            if (isFdEnabledOnlyWhenScreenOff()) {
                updateFdModeIfNeeded();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onChargingModeSwitched(boolean isCharging) {
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
    /* access modifiers changed from: public */
    private void onTetheringSwitched() {
        updateFdModeIfNeeded();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDefaultDataSwitched() {
        updateFdModeIfNeeded();
    }

    private static boolean isFdEnabledOnlyWhenScreenOff() {
        return SystemProperties.getInt(PROPERTY_FD_SCREEN_OFF_ONLY, 0) == 1;
    }

    private static int getPhoneId(Phone phone) {
        return phone.getPhoneId();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String s) {
        Rlog.d(LOG_TAG, "[phoneId" + getPhoneId(this.mPhone) + "]" + s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logw(String s) {
        Rlog.w(LOG_TAG, "[phoneId" + getPhoneId(this.mPhone) + "]" + s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, "[phoneId" + getPhoneId(this.mPhone) + "]" + s);
    }
}
