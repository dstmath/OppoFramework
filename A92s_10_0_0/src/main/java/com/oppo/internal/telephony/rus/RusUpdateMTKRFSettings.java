package com.oppo.internal.telephony.rus;

import com.android.internal.telephony.DataEntity;
import com.oppo.internal.telephony.rf.OemMTKRFSettings;
import java.util.HashMap;

public final class RusUpdateMTKRFSettings extends RusBase {
    private static final String TAG = "RusUpdateMTKRFSettings";

    public RusUpdateMTKRFSettings() {
        this.mRebootExecute = true;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
            return;
        }
        String[] rfdata = new String[4];
        if (rusData.containsKey("switch") && rusData.containsKey("gsm") && rusData.containsKey("wcdma") && rusData.containsKey("lte")) {
            rfdata[0] = rusData.get("switch");
            rfdata[1] = rusData.get("gsm");
            rfdata[2] = rusData.get("wcdma");
            rfdata[3] = rusData.get("lte");
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",switch:" + rfdata[0] + ",gsm:" + rfdata[1] + ",wcdma:" + rfdata[2] + ",lte:" + rfdata[3]);
            if (!isReboot || !"0".equals(rfdata[0]) || OemMTKRFSettings.getLastRFState(this.mPhone.getContext())) {
                setcmdRFSetting(rfdata);
            }
        }
    }

    private void setcmdRFSetting(String[] rfdata) {
        if (rfdata.length >= 4) {
            DataEntity data = new DataEntity();
            boolean z = false;
            try {
                if (!"0".equals(rfdata[0])) {
                    z = true;
                }
                data.isSwitch = z;
                data.gsm = Integer.valueOf(rfdata[1]).intValue();
                data.wcdma = Integer.valueOf(rfdata[2]).intValue();
                data.lte = Integer.valueOf(rfdata[3]).intValue();
            } catch (Exception e) {
                printLog(TAG, "Exception executeMTKRFSettings " + e.getMessage());
            }
            OemMTKRFSettings.isSwitch = data.isSwitch;
            OemMTKRFSettings rfSettings = OemMTKRFSettings.getDefault(this.mPhone.getContext());
            rfSettings.setInitValue(data);
            rfSettings.restore(this.mPhone.getContext());
            if (data.isSwitch) {
                rfSettings.register();
            } else {
                rfSettings.unregister();
            }
        }
    }
}
