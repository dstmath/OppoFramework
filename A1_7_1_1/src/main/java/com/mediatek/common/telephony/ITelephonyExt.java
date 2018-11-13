package com.mediatek.common.telephony;

import android.content.Context;

public interface ITelephonyExt {
    String getOperatorNumericFromImpi(String str, int i);

    boolean ignoreDataRoaming(String str);

    boolean ignoreDefaultDataUnselected(String str);

    void init(Context context);

    boolean isOnlySingleDcAllowed();

    boolean isRatMenuControlledBySIM();

    boolean isSetLanguageBySIM();

    void resetImsPdnOverSSComplete(Context context);

    void startDataRoamingStrategy(Object obj);

    void stopDataRoamingStrategy();
}
