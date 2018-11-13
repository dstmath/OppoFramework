package com.android.internal.telephony.oem.rus;

import android.content.Context;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OemFeature;
import com.android.internal.telephony.OemProximitySensorManager;
import com.android.internal.telephony.OemSimProtect;

public class RusInitProcess {
    public static void execute(Context context) {
        RusFactory.getInstance(context);
        if (OemConstant.getWlanAssistantEnable(context)) {
            RusUpdateWlanAssitant mRusUpdateWlanAssitant = new RusUpdateWlanAssitant();
            mRusUpdateWlanAssitant.parseContentFromXML(mRusUpdateWlanAssitant.readFromFile());
        }
        OemConstant.checkVoocState("false");
        if (OemFeature.FEATURE_OPPO_COMM_PROXIMITY) {
            OemProximitySensorManager.getDefault(context);
        }
        if (OemFeature.FEATURE_OPPO_COMM_ORIENTATION) {
            new RusUpdateRFSettings().process(true);
        }
        OemSimProtect.getInstance().registerSimProtectObserver(context);
        RusUpdateUseNtp mRusUpdateUseNtp = new RusUpdateUseNtp();
        mRusUpdateUseNtp.parseContentFromXML(mRusUpdateUseNtp.readFromFile());
    }
}
