package com.oppo.internal.telephony.rus;

import android.os.SystemProperties;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.util.OemTelephonyUtils;
import java.util.HashMap;

public final class RusUpdateMtkLteWifiCoexist extends RusBase {
    private static final String TAG = "RusUpdateMtkLteWifiCoexist";

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
            return;
        }
        try {
            if (rusData.containsKey("enabled")) {
                int value = Integer.valueOf(rusData.get("enabled")).intValue();
                printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",enabled:" + value);
                boolean z = true;
                if (SystemProperties.getInt("persist.sys.ltewificoexist", 1) != value) {
                    SystemProperties.set("persist.sys.ltewificoexist", "" + value);
                    AbstractPhone tmpPhone = (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, this.mPhone);
                    if (value != 1) {
                        z = false;
                    }
                    tmpPhone.updateLteWifiCoexist(z);
                }
            }
        } catch (Exception e) {
            printLog(TAG, "Exception executeRusCommand " + e.getMessage());
        }
    }
}
