package com.mediatek.internal.telephony;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.DefaultPhoneNotifier;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.internal.telephony.IMtkTelephonyRegistryEx;
import java.util.Arrays;
import mediatek.telephony.MtkServiceState;

public class MtkPhoneNotifier extends DefaultPhoneNotifier {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "MtkPhoneNotifr";
    private final int mFakeSub = 2147483646;
    protected IMtkTelephonyRegistryEx mMtkRegistry;

    public MtkPhoneNotifier() {
        Rlog.d(LOG_TAG, "constructor");
        MtkTelephonyRegistryEx.init();
        this.mMtkRegistry = IMtkTelephonyRegistryEx.Stub.asInterface(ServiceManager.getService("telephony.mtkregistry"));
    }

    private boolean checkSubIdPhoneId(Phone s) {
        if (s.getSubId() >= 0) {
            return true;
        }
        int defaultFallbackSubId = MtkSubscriptionController.getMtkInstance().getDefaultFallbackSubId();
        int defaultFallbackPhoneId = MtkSubscriptionController.getMtkInstance().getPhoneId(defaultFallbackSubId);
        int[] subIds = MtkSubscriptionController.getMtkInstance().getActiveSubIdList(true);
        Rlog.d(LOG_TAG, "checkSubIdPhoneId defaultFallbackSubId:" + defaultFallbackSubId + " defaultFallbackPhoneId:" + defaultFallbackPhoneId + " sender's SubId:" + s.getSubId() + " activeSubIds: " + Arrays.toString(subIds));
        if (subIds.length == 0) {
            return true;
        }
        return false;
    }

    public void notifyServiceState(Phone sender) {
        if (checkSubIdPhoneId(sender)) {
            MtkPhoneNotifier.super.notifyServiceState(sender);
            return;
        }
        ServiceState ss = sender.getServiceState();
        int phoneId = sender.getPhoneId();
        if (ss == null) {
            ss = new MtkServiceState();
            ss.setStateOutOfService();
        }
        Rlog.d(LOG_TAG, "MtkPhoneNotifier notifyServiceState phoneId:" + phoneId + " fake subId: " + 2147483646 + " ServiceState: " + ss);
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyServiceStateForPhoneId(phoneId, 2147483646, ss);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifySignalStrength(Phone sender) {
        if (checkSubIdPhoneId(sender)) {
            MtkPhoneNotifier.super.notifySignalStrength(sender);
            return;
        }
        int phoneId = sender.getPhoneId();
        Rlog.d(LOG_TAG, "MtkPhoneNotifier notifySignalStrength phoneId:" + phoneId + " fake subId: " + 2147483646 + " signal: " + sender.getSignalStrength());
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifySignalStrengthForPhoneId(phoneId, 2147483646, sender.getSignalStrength());
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyDataConnection(Phone sender, String apnType, PhoneConstants.DataState state) {
        if (sender.getActiveApnHost(apnType) != null || "default".equals(apnType) || "emergency".equals(apnType)) {
            MtkPhoneNotifier.super.notifyDataConnection(sender, apnType, state);
        }
    }

    public void notifyMtkServiceState(Phone sender, MtkServiceState mss) {
        MtkServiceState mtkServiceState = mss;
        int phoneId = sender.getPhoneId();
        if (mtkServiceState == null) {
            mtkServiceState = new MtkServiceState();
            mtkServiceState.setStateOutOfService();
        }
        Rlog.d(LOG_TAG, "MtkPhoneNotifier notifyMtkServiceState phoneId:" + phoneId + " fake subId: " + 2147483646 + " ServiceState: " + mtkServiceState);
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyServiceStateForPhoneId(phoneId, 2147483646, mtkServiceState);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyMtkSignalStrength(Phone sender, SignalStrength ss) {
        int phoneId = sender.getPhoneId();
        Rlog.d(LOG_TAG, "MtkPhoneNotifier notifyMtkSignalStrength phoneId:" + phoneId + " fake subId: " + 2147483646 + " signal: " + ss);
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifySignalStrengthForPhoneId(phoneId, 2147483646, ss);
            }
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: protected */
    public int mtkGetDataNetworkType(TelephonyManager telephony, Phone sender, String apnType, PhoneConstants.DataState state, int subId) {
        MtkServiceState turboSS;
        if (!apnType.equals("default") || state != PhoneConstants.DataState.CONNECTED || (turboSS = sender.getDcTracker(1).getTurboSS()) == null) {
            return MtkPhoneNotifier.super.mtkGetDataNetworkType(telephony, sender, apnType, state, subId);
        }
        Rlog.d(LOG_TAG, "mtkGetDataNetworkType: get turbo SS");
        return turboSS.getDataNetworkType();
    }
}
