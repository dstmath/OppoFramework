package com.mediatek.internal.telephony;

import android.os.ServiceManager;
import android.telephony.Rlog;
import com.mediatek.internal.telephony.IMtkTelephonyRegistryEx;

public class MtkTelephonyRegistryEx extends IMtkTelephonyRegistryEx.Stub {
    private static final boolean DBG = false;
    private static final boolean DBG_LOC = false;
    private static final String LOG_TAG = "MtkTelephonyRegistryEx";
    private static final boolean VDBG = false;
    private static MtkTelephonyRegistryEx sInstance;

    static MtkTelephonyRegistryEx init() {
        MtkTelephonyRegistryEx mtkTelephonyRegistryEx;
        synchronized (MtkTelephonyRegistryEx.class) {
            if (sInstance == null) {
                sInstance = new MtkTelephonyRegistryEx();
            } else {
                Rlog.e(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            mtkTelephonyRegistryEx = sInstance;
        }
        return mtkTelephonyRegistryEx;
    }

    protected MtkTelephonyRegistryEx() {
        publish();
    }

    private void publish() {
        Rlog.d(LOG_TAG, "publish: " + this);
        ServiceManager.addService("telephony.mtkregistry", this);
    }

    private static boolean idMatchEx(int rSubId, int subId, int dSubId, int rPhoneId, int phoneId) {
        Rlog.d(LOG_TAG, "idMatchEx: rSubId=" + rSubId + ", subId=" + subId + ", dSubId=" + dSubId + ", rPhoneId=" + rPhoneId + ", phoneId=" + phoneId);
        return subId < 0 ? rPhoneId == phoneId : rSubId == Integer.MAX_VALUE ? subId == dSubId : rSubId == subId;
    }
}
