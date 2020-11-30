package com.mediatek.internal.telephony;

import android.content.Context;
import android.os.Build;
import android.os.ServiceManager;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.mediatek.internal.telephony.IMtkSub;

public class MtkSubscriptionControllerEx extends IMtkSub.Stub {
    private static final boolean DBG = true;
    private static final boolean ENGDEBUG = TextUtils.equals(Build.TYPE, "eng");
    private static final String LOG_TAG = "MtkSubscriptionControllerEx";
    private static MtkSubscriptionControllerEx sInstance = null;
    private Context mContext;

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.mediatek.internal.telephony.MtkSubscriptionControllerEx */
    /* JADX WARN: Multi-variable type inference failed */
    protected MtkSubscriptionControllerEx(Context c) {
        this.mContext = c;
        if (ServiceManager.getService("isubstub") == null) {
            ServiceManager.addService("isubstub", this);
            Rlog.d(LOG_TAG, "[MtkSubscriptionControllerEx] init by Context, this = " + this);
        }
        Rlog.d(LOG_TAG, "[MtkSubscriptionControllerEx] init by Context");
    }

    protected static void MtkInitStub(Context c) {
        synchronized (MtkSubscriptionControllerEx.class) {
            if (sInstance == null) {
                sInstance = new MtkSubscriptionControllerEx(c);
                Rlog.d(LOG_TAG, "[MtkSubscriptionControllerEx] sInstance = " + sInstance);
            } else {
                Rlog.w(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
        }
    }

    public MtkSubscriptionInfo getSubInfo(String callingPackage, int subId) {
        return MtkSubscriptionController.getMtkInstance().getSubscriptionInfo(callingPackage, subId);
    }

    public MtkSubscriptionInfo getSubInfoForIccId(String callingPackage, String iccId) {
        return MtkSubscriptionController.getMtkInstance().getSubscriptionInfoForIccId(callingPackage, iccId);
    }

    public int getSubIdUsingPhoneId(int phoneId) {
        return MtkSubscriptionController.getMtkInstance().getSubIdUsingPhoneId(phoneId);
    }

    public void setDefaultFallbackSubId(int subId, int subscriptionType) {
        MtkSubscriptionController.getMtkInstance().setDefaultFallbackSubId(subId, subscriptionType);
    }

    public void setDefaultDataSubIdWithoutCapabilitySwitch(int subId) {
        MtkSubscriptionController.getMtkInstance().setDefaultDataSubIdWithoutCapabilitySwitch(subId);
    }
}
