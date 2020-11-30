package com.mediatek.internal.telephony;

import android.content.Context;

public interface IDefaultTelephonyExt {
    String getOperatorNumericFromImpi(String str, int i);

    void init(Context context);

    boolean isRatMenuControlledBySIM();

    boolean isSetLanguageBySIM();
}
