package com.mediatek.internal.telephony;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.ims.ImsManager;
import com.android.internal.telephony.CommandsInterface;
import com.mediatek.ims.internal.IMtkImsService;
import com.mediatek.ims.internal.MtkImsManager;

public class ImsSwitchController extends Handler {
    private static final int BIND_IMS_SERVICE_DELAY_IN_MILLIS = 2000;
    static final String LOG_TAG = "ImsSwitchController";
    /* access modifiers changed from: private */
    public static IMtkImsService mMtkImsService = null;
    /* access modifiers changed from: private */
    public Runnable mBindImsServiceRunnable = new Runnable() {
        /* class com.mediatek.internal.telephony.ImsSwitchController.AnonymousClass1 */

        public void run() {
            Rlog.w(ImsSwitchController.LOG_TAG, "try to bind ImsService again");
            if (!ImsSwitchController.this.checkAndBindImsService(0)) {
                ImsSwitchController.this.postDelayed(this, 2000);
                return;
            }
            ImsSwitchController.log("manually updateImsServiceConfig");
            if (!MtkImsManager.isSupportMims()) {
                ImsManager.updateImsServiceConfig(ImsSwitchController.this.mContext, RadioCapabilitySwitchUtil.getMainCapabilityPhoneId(), true);
                return;
            }
            for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                ImsManager.updateImsServiceConfig(ImsSwitchController.this.mContext, i, true);
            }
        }
    };
    private CommandsInterface[] mCi;
    /* access modifiers changed from: private */
    public Context mContext;
    private ImsServiceDeathRecipient mDeathRecipient = new ImsServiceDeathRecipient();
    private int mPhoneCount;
    private RadioPowerInterface mRadioPowerIf;

    ImsSwitchController(Context context, int phoneCount, CommandsInterface[] ci) {
        log("Initialize ImsSwitchController");
        this.mContext = context;
        this.mCi = ci;
        this.mPhoneCount = phoneCount;
        if (SystemProperties.get("persist.vendor.ims_support").equals("1") && !SystemProperties.get("ro.vendor.mtk_tc1_feature").equals("1")) {
            this.mRadioPowerIf = new RadioPowerInterface();
            RadioManager.registerForRadioPowerChange(LOG_TAG, this.mRadioPowerIf);
            if (mMtkImsService == null) {
                checkAndBindImsService(0);
            }
        }
    }

    class RadioPowerInterface implements IRadioPower {
        RadioPowerInterface() {
        }

        @Override // com.mediatek.internal.telephony.IRadioPower
        public void notifyRadioPowerChange(boolean power, int phoneId) {
            ImsSwitchController.log("notifyRadioPowerChange, power:" + power + " phoneId:" + phoneId);
            if (!MtkImsManager.isSupportMims() && RadioCapabilitySwitchUtil.getMainCapabilityPhoneId() != phoneId) {
                ImsSwitchController.log("radio power change ignore due to phone id isn't LTE phone");
            } else if (SystemProperties.get("ro.vendor.md_auto_setup_ims").equals("1")) {
                ImsSwitchController.log("[" + phoneId + "] Modem auto registration so that we don't triggerImsService updateRadioState");
            } else {
                if (ImsSwitchController.mMtkImsService == null) {
                    boolean unused = ImsSwitchController.this.checkAndBindImsService(phoneId);
                }
                if (ImsSwitchController.mMtkImsService != null) {
                    try {
                        ImsSwitchController.mMtkImsService.updateRadioState(power ? 1 : 0, phoneId);
                    } catch (RemoteException e) {
                        Rlog.e(ImsSwitchController.LOG_TAG, "RemoteException can't notify power state change");
                    }
                } else {
                    Rlog.w(ImsSwitchController.LOG_TAG, "notifyRadioPowerChange: ImsService not ready !!!");
                }
                ImsSwitchController.log("radio power change processed");
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean checkAndBindImsService(int phoneId) {
        IBinder b = ServiceManager.getService("mtkIms");
        if (b == null) {
            return false;
        }
        try {
            b.linkToDeath(this.mDeathRecipient, 0);
            mMtkImsService = IMtkImsService.Stub.asInterface(b);
            log("checkAndBindImsService: mMtkImsService = " + mMtkImsService);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    private class ImsServiceDeathRecipient implements IBinder.DeathRecipient {
        private ImsServiceDeathRecipient() {
        }

        public void binderDied() {
            Rlog.w(ImsSwitchController.LOG_TAG, "ImsService died detected");
            IMtkImsService unused = ImsSwitchController.mMtkImsService = null;
            ImsSwitchController imsSwitchController = ImsSwitchController.this;
            imsSwitchController.postDelayed(imsSwitchController.mBindImsServiceRunnable, 2000);
        }
    }

    /* access modifiers changed from: private */
    public static void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
