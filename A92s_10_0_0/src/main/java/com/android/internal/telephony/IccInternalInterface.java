package com.android.internal.telephony;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public interface IccInternalInterface {
    public static final int ERROR_ICC_PROVIDER_ADN_LIST_NOT_EXIST = -11;
    public static final int ERROR_ICC_PROVIDER_ANR_SAVE_FAILURE = -14;
    public static final int ERROR_ICC_PROVIDER_ANR_TOO_LONG = -6;
    public static final int ERROR_ICC_PROVIDER_EMAIL_FULL = -12;
    public static final int ERROR_ICC_PROVIDER_EMAIL_TOO_LONG = -13;
    public static final int ERROR_ICC_PROVIDER_GENERIC_FAILURE = -10;
    public static final int ERROR_ICC_PROVIDER_NOT_READY = -4;
    public static final int ERROR_ICC_PROVIDER_NUMBER_TOO_LONG = -1;
    public static final int ERROR_ICC_PROVIDER_PASSWORD_ERROR = -5;
    public static final int ERROR_ICC_PROVIDER_SNE_FULL = -16;
    public static final int ERROR_ICC_PROVIDER_SNE_TOO_LONG = -17;
    public static final int ERROR_ICC_PROVIDER_STORAGE_FULL = -3;
    public static final int ERROR_ICC_PROVIDER_SUCCESS = 1;
    public static final int ERROR_ICC_PROVIDER_TEXT_TOO_LONG = -2;
    public static final int ERROR_ICC_PROVIDER_UNKNOWN = 0;
    public static final int ERROR_ICC_PROVIDER_WRONG_ADN_FORMAT = -15;

    int delete(Uri uri, String str, String[] strArr);

    Uri insert(Uri uri, ContentValues contentValues);

    Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2);

    int update(Uri uri, ContentValues contentValues, String str, String[] strArr);
}
