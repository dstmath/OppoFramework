package com.oppo.internal.telephony.rus;

import java.util.HashMap;

public final class RusUpdateRegionNetlock extends RusBase {
    private static final String TAG = "RusUpdateRegionNetlock";
    private static final String mRegionNetlockName = "region_netcode_config.xml";

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
            return;
        }
        String strRus = this.mRusServerHelp.getSubConfig(this.mRusServerHelp.readRusDataFromXml("RegionNetlock.xml"), "<netcode", "/netcode>");
        printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",strRus:" + strRus);
        this.mRusServerHelp.saveXmlToFile(strRus, mRegionNetlockName);
    }
}
