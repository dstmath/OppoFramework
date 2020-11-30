package com.mediatek.internal.telephony;

import android.content.Context;
import com.mediatek.common.util.OperatorCustomizationFactoryLoader;
import java.util.ArrayList;
import java.util.List;

public class MtkOpTelephonyCustomizationUtils {
    static MtkOpTelephonyCustomizationFactoryBase sFactory = null;
    private static final List<OperatorCustomizationFactoryLoader.OperatorFactoryInfo> sOperatorFactoryInfoList = new ArrayList();

    static {
        sOperatorFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP09CTelephony.jar", "com.mediatek.op09c.telephony.MtkOp09CTelephonyCustomizationFactory", (String) null, "OP09", "SEGC"));
        sOperatorFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP01Telephony.jar", "com.mediatek.op01.telephony.MtkOp01TelephonyCustomizationFactory", (String) null, "OP01"));
        sOperatorFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP02Telephony.jar", "com.mediatek.op02.telephony.MtkOp02TelephonyCustomizationFactory", (String) null, "OP02"));
    }

    public static synchronized MtkOpTelephonyCustomizationFactoryBase getOpFactory(Context context) {
        MtkOpTelephonyCustomizationFactoryBase mtkOpTelephonyCustomizationFactoryBase;
        synchronized (MtkOpTelephonyCustomizationUtils.class) {
            sFactory = (MtkOpTelephonyCustomizationFactoryBase) OperatorCustomizationFactoryLoader.loadFactory(context, sOperatorFactoryInfoList);
            if (sFactory == null) {
                sFactory = new MtkOpTelephonyCustomizationFactoryBase();
            }
            mtkOpTelephonyCustomizationFactoryBase = sFactory;
        }
        return mtkOpTelephonyCustomizationFactoryBase;
    }
}
