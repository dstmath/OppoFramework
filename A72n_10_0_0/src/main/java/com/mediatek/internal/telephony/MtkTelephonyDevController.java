package com.mediatek.internal.telephony;

import android.content.res.Resources;
import android.os.AsyncResult;
import com.android.internal.telephony.HardwareConfig;
import com.android.internal.telephony.TelephonyDevController;
import java.util.List;

public class MtkTelephonyDevController extends TelephonyDevController {
    protected static final String LOG_TAG = "MtkTDC";

    public MtkTelephonyDevController() {
        logd("MtkTelephonyDevController constructor");
    }

    /* access modifiers changed from: protected */
    public void initFromResource() {
        String[] hwStrings = Resources.getSystem().getStringArray(17236074);
        if (hwStrings != null) {
            for (String hwString : hwStrings) {
                HardwareConfig hw = new MtkHardwareConfig(hwString);
                if (hw.type == 0) {
                    updateOrInsert(hw, mModems);
                } else if (hw.type == 1) {
                    updateOrInsert(hw, mSims);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleGetHardwareConfigChanged(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            loge("handleGetHardwareConfigChanged - returned an error.");
            return;
        }
        List hwcfg = (List) ar.result;
        for (int i = 0; i < hwcfg.size(); i++) {
            HardwareConfig hw = (HardwareConfig) hwcfg.get(i);
            if (hw != null) {
                String str = hw.type + "," + hw.uuid + "," + hw.state;
                MtkHardwareConfig mtkHwCfg = new MtkHardwareConfig(hw.type == 0 ? str + "," + hw.rilModel + ",0," + hw.maxActiveVoiceCall + "," + hw.maxActiveDataCall + "," + hw.maxStandby : str + "," + hw.modemUuid);
                if (mtkHwCfg.type == 0) {
                    updateOrInsert(mtkHwCfg, mModems);
                } else if (hw.type == 1) {
                    updateOrInsert(mtkHwCfg, mSims);
                }
            }
        }
    }
}
