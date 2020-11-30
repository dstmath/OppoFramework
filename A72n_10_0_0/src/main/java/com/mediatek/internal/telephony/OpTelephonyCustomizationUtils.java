package com.mediatek.internal.telephony;

import android.content.Context;
import com.mediatek.common.util.OperatorCustomizationFactoryLoader;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import java.util.ArrayList;
import java.util.List;

public class OpTelephonyCustomizationUtils {
    static volatile OpTelephonyCustomizationFactoryBase sFactory = null;
    private static final List<OperatorCustomizationFactoryLoader.OperatorFactoryInfo> sOperatorFactoryInfoList = new ArrayList();

    static {
        sOperatorFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP01Telephony.jar", "com.mediatek.op01.telephony.Op01TelephonyCustomizationFactory", (String) null, DataSubConstants.OPERATOR_OP01));
        sOperatorFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP02Telephony.jar", "com.mediatek.op02.telephony.Op02TelephonyCustomizationFactory", (String) null, DataSubConstants.OPERATOR_OP02));
        sOperatorFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP08Telephony.jar", "com.mediatek.op08.telephony.Op08TelephonyCustomizationFactory", (String) null, "OP08"));
        sOperatorFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP09CTelephony.jar", "com.mediatek.op09c.telephony.Op09CTelephonyCustomizationFactory", (String) null, DataSubConstants.OPERATOR_OP09, DataSubConstants.SEGC));
        sOperatorFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP12Telephony.jar", "com.mediatek.op12.telephony.Op12TelephonyCustomizationFactory", (String) null, "OP12"));
        sOperatorFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP07Telephony.jar", "com.mediatek.op07.telephony.Op07TelephonyCustomizationFactory", (String) null, "OP07"));
        sOperatorFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("OP18Telephony.jar", "com.mediatek.op18.telephony.Op18TelephonyCustomizationFactory", (String) null, DataSubConstants.OPERATOR_OP18));
    }

    public static OpTelephonyCustomizationFactoryBase getOpFactory(Context context) {
        synchronized (OpTelephonyCustomizationFactoryBase.class) {
            sFactory = (OpTelephonyCustomizationFactoryBase) OperatorCustomizationFactoryLoader.loadFactory(context, sOperatorFactoryInfoList);
            if (sFactory == null) {
                sFactory = new OpTelephonyCustomizationFactoryBase();
            }
        }
        return sFactory;
    }
}
