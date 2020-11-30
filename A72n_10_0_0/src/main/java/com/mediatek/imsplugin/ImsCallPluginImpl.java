package com.mediatek.imsplugin;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.mediatek.ims.internal.MtkImsManager;
import com.mediatek.ims.plugin.impl.ImsCallPluginBase;
import com.mediatek.internal.telephony.IMtkTelephonyEx;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.telephony.MtkTelephonyManagerEx;

public class ImsCallPluginImpl extends ImsCallPluginBase {
    private static final String MULTI_IMS_SUPPORT = "persist.vendor.mims_support";
    private static final String TAG = "ImsCallPluginImpl";
    private Context mContext;

    public ImsCallPluginImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public boolean isSupportMims() {
        return MtkImsManager.isSupportMims();
    }

    public int setImsFwkRequest(int request) {
        return 32768 | request;
    }

    public int getRealRequest(int request) {
        return -32769 & request;
    }

    public boolean isImsFwkRequest(int request) {
        if ((request & 32768) == 32768) {
            return true;
        }
        return false;
    }

    public int getUpgradeCancelFlag() {
        return 65536;
    }

    public int getUpgradeCancelTimeoutFlag() {
        return 131072;
    }

    public int getMainCapabilityPhoneId() {
        return RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
    }

    public boolean isCapabilitySwitching() {
        IMtkTelephonyEx iTelEx = IMtkTelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
        if (iTelEx == null) {
            return false;
        }
        try {
            return iTelEx.isCapabilitySwitching();
        } catch (RemoteException e) {
            Log.d(TAG, "Exception:" + e);
            return false;
        }
    }

    public int getSimCardState(int slotId) {
        return MtkTelephonyManagerEx.getDefault().getSimCardState(slotId);
    }

    public int getSimApplicationState(int slotId) {
        return MtkTelephonyManagerEx.getDefault().getSimApplicationState(slotId);
    }
}
