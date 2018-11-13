package com.mediatek.common.telephony;

import android.content.Context;
import android.database.Cursor;

public interface ICallerInfoExt {
    CharSequence getTypeLabel(Context context, int i, CharSequence charSequence, Cursor cursor);

    CharSequence getTypeLabel(Context context, int i, CharSequence charSequence, Cursor cursor, int i2);
}
