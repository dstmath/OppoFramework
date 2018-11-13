package com.mediatek.common.telephony;

import android.content.Context;

public interface IUiccControllerExt {
    CharSequence getMissingDetail(Context context);

    String getMissingTitle(Context context, int i);
}
