package com.mediatek.internal.telephony.dataconnection;

import android.net.NetworkCapabilities;
import android.os.Looper;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionMonitor;
import com.android.internal.telephony.dataconnection.TelephonyNetworkFactory;

public class MtkTelephonyNetworkFactory extends TelephonyNetworkFactory {
    public MtkTelephonyNetworkFactory(SubscriptionMonitor subscriptionMonitor, Looper looper, Phone phone) {
        super(subscriptionMonitor, looper, phone);
    }

    /* access modifiers changed from: protected */
    public NetworkCapabilities makeNetworkFilter(int subscriptionId) {
        NetworkCapabilities nc = MtkTelephonyNetworkFactory.super.makeNetworkFilter(subscriptionId);
        nc.addCapability(27);
        nc.addCapability(26);
        return nc;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIgnoreCapabilityCheck(NetworkCapabilities capabilities, int action) {
        if (capabilities.hasCapability(4) || capabilities.hasCapability(10)) {
            if (action != 1) {
                log("ignoreCapabilityCheck() ignore IMS/EIMS PDN");
                return true;
            } else if (MtkDcHelper.getInstance().isMultiPsAttachSupport()) {
                log("ignoreCapabilityCheck() allow IMS/EIMS pdn activation");
                return true;
            } else {
                log("ignoreCapabilityCheck() reject IMS/EIMS pdn activation");
                return false;
            }
        } else if (!capabilities.hasCapability(26)) {
            return false;
        } else {
            log("ignoreCapabilityCheck() ignore VSIM PDN");
            return true;
        }
    }
}
