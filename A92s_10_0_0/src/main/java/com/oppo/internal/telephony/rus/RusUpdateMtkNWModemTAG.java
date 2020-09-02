package com.oppo.internal.telephony.rus;

import android.os.IBinder;
import android.os.Parcel;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import java.util.HashMap;

public final class RusUpdateMtkNWModemTAG extends RusBase {
    private static int PROJECT_SIM_NUM = TelephonyManager.getDefault().getSimCount();
    private static final String TAG = "RusUpdateMtkNWModemTAG";

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("nw_modem_tag")) {
            String value = rusData.get("nw_modem_tag");
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",nw_modem_tag:" + value);
            if ("1".equals(value) || "0".equals(value)) {
                enableModemTAG(6, value);
            }
        }
    }

    private void enableModemTAG(int type, String data) {
        Parcel p = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder ib = ServiceManager.checkService("power_hal_mgr_service");
        try {
            p.writeInterfaceToken(ib.getInterfaceDescriptor());
            p.writeInt(type);
            p.writeString(data);
            ib.transact(14, p, reply, 0);
            reply.readException();
            printLog(TAG, "power app enableModemTAG data: " + data);
            reply.recycle();
            p.recycle();
        } catch (Exception e) {
            printLog(TAG, "enableModemTAG Exception:" + e.toString());
        }
    }
}
