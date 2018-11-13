package com.mediatek.common.telephony;

import android.content.ContentValues;

public interface ITelephonyProviderExt {
    int onLoadApns(ContentValues contentValues);
}
