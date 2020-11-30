package com.mediatek.internal.telephony.devreg;

import android.app.PendingIntent;
import android.content.Context;
import com.android.internal.telephony.Phone;
import com.mediatek.internal.telephony.MtkUiccSmsController;
import com.mediatek.internal.telephony.OpTelephonyCustomizationUtils;

public class DeviceRegisterController {
    private static IDeviceRegisterExt sDeviceRegisterExt = null;
    private DeviceRegisterHandler[] mHandler = null;
    private MtkUiccSmsController mSmsController = null;

    public DeviceRegisterController(Context context, Phone[] phone, MtkUiccSmsController controller) {
        this.mSmsController = controller;
        try {
            sDeviceRegisterExt = OpTelephonyCustomizationUtils.getOpFactory(context).makeDeviceRegisterExt(context, this);
        } catch (Exception e) {
            e.printStackTrace();
            sDeviceRegisterExt = new DefaultDeviceRegisterExt(context, this);
        }
        this.mHandler = new DeviceRegisterHandler[phone.length];
        for (int i = 0; i < phone.length; i++) {
            this.mHandler[i] = new DeviceRegisterHandler(phone[i], this);
        }
    }

    public void sendDataSms(int subId, String destAddr, String scAddr, int destPort, int originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        this.mSmsController.sendData(subId, destAddr, scAddr, destPort, originalPort, data, sentIntent, deliveryIntent);
    }

    private static IDeviceRegisterExt getDeviceRegisterExt() {
        return sDeviceRegisterExt;
    }

    public void setCdmaCardEsnOrMeid(String esnMeid) {
        IDeviceRegisterExt iDeviceRegisterExt = sDeviceRegisterExt;
        if (iDeviceRegisterExt != null) {
            iDeviceRegisterExt.setCdmaCardEsnOrMeid(esnMeid);
        }
    }

    public void handleAutoRegMessage(byte[] pdu) {
        IDeviceRegisterExt iDeviceRegisterExt = sDeviceRegisterExt;
        if (iDeviceRegisterExt != null) {
            iDeviceRegisterExt.handleAutoRegMessage(pdu);
        }
    }

    public void handleAutoRegMessage(int subId, String format, byte[] pdu) {
        IDeviceRegisterExt iDeviceRegisterExt = sDeviceRegisterExt;
        if (iDeviceRegisterExt != null) {
            iDeviceRegisterExt.handleAutoRegMessage(subId, format, pdu);
        }
    }
}
