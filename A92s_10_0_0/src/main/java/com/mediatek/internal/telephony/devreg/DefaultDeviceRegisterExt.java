package com.mediatek.internal.telephony.devreg;

import android.content.Context;
import android.util.Log;

public class DefaultDeviceRegisterExt implements IDeviceRegisterExt {
    private static final String TAG = "DefaultDeviceRegisterExt";
    protected Context mContext;
    protected DeviceRegisterController mDeviceRegisterController;

    public DefaultDeviceRegisterExt(Context context, DeviceRegisterController controller) {
        this.mContext = context;
        this.mDeviceRegisterController = controller;
    }

    @Override // com.mediatek.internal.telephony.devreg.IDeviceRegisterExt
    public void setCdmaCardEsnOrMeid(String rawValue) {
        Log.i(TAG, "setCdmaCardEsnOrMeid " + rawValue);
    }

    @Override // com.mediatek.internal.telephony.devreg.IDeviceRegisterExt
    public void handleAutoRegMessage(byte[] pdu) {
        Log.i(TAG, "handleAutoRegMessage");
    }

    @Override // com.mediatek.internal.telephony.devreg.IDeviceRegisterExt
    public void handleAutoRegMessage(int subId, String format, byte[] pdu) {
        Log.i(TAG, "handleAutoRegMessage sub " + subId + ", format " + format);
    }
}
