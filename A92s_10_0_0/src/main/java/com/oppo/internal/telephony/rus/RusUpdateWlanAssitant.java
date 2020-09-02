package com.oppo.internal.telephony.rus;

import android.net.ConnectivityManager;
import com.android.internal.telephony.util.ReflectionHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class RusUpdateWlanAssitant extends RusBase {
    private static final String TAG = "RusUpdateWlanAssitant";
    private final ConnectivityManager mCm = ((ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity"));

    public RusUpdateWlanAssitant() {
        this.mRebootExecute = true;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
            return;
        }
        new ArrayList();
        for (Map.Entry<String, String> entry : rusData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + "," + key + ":" + value);
            ReflectionHelper.callMethod(this.mCm, "android.net.ConnectivityManager", "updateDataNetworkConfig", new Class[]{String.class, String.class}, new Object[]{key, value});
        }
    }
}
