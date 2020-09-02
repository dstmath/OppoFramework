package android.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.text.TextUtils;

public class ColorOSPhoneNumberUtils {
    private static final String IP_CALL = "ip_call";
    private static final String IP_CALL_PREFIX = "ip_call_prefix_sub";
    static final String LOG_TAG = "ColorOSPhoneNumberUtils";
    public static final char PAUSE = ',';
    public static final char WAIT = ';';
    public static final char WILD = 'N';

    public static String checkAndAppendPrefix(Intent intent, int subscription, String number, Context context) {
        if (intent.getBooleanExtra(IP_CALL, false) && number != null && subscription < TelephonyManager.getDefault().getPhoneCount()) {
            ContentResolver contentResolver = context.getContentResolver();
            String IPPrefix = Settings.System.getString(contentResolver, IP_CALL_PREFIX + (subscription + 1));
            if (!TextUtils.isEmpty(IPPrefix)) {
                return IPPrefix + number;
            }
        }
        return number;
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x00da  */
    public static String colorGetNumberFromIntent(Intent intent, Context context) {
        int subscription;
        String phoneColumn;
        int[] subId;
        String number = null;
        Uri uri = intent.getData();
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            subscription = intent.getIntExtra("subscription", SubscriptionManager.getDefaultVoicePhoneId());
        } else {
            subscription = 0;
        }
        if (scheme.equals(PhoneAccount.SCHEME_TEL) || scheme.equals("sip")) {
            return checkAndAppendPrefix(intent, subscription, uri.getSchemeSpecificPart(), context);
        }
        if (scheme.equals(PhoneAccount.SCHEME_VOICEMAIL)) {
            if (!TelephonyManager.getDefault().isMultiSimEnabled() || (subId = SubscriptionManager.getSubId(subscription)) == null || subId[0] <= 0) {
                return TelephonyManager.getDefault().getVoiceMailNumber();
            }
            return TelephonyManager.getDefault().getVoiceMailNumber(subId[0]);
        } else if (context == null) {
            return null;
        } else {
            intent.resolveType(context);
            String authority = uri.getAuthority();
            if (Contacts.AUTHORITY.equals(authority)) {
                phoneColumn = "number";
            } else if (ContactsContract.AUTHORITY.equals(authority)) {
                phoneColumn = "data1";
            } else {
                phoneColumn = null;
            }
            Cursor c = null;
            try {
                Cursor c2 = context.getContentResolver().query(uri, new String[]{phoneColumn}, null, null, null);
                if (c2 != null) {
                    try {
                        if (c2.moveToFirst()) {
                            number = c2.getString(c2.getColumnIndex(phoneColumn));
                        }
                    } catch (RuntimeException e) {
                        e = e;
                        c = c2;
                        try {
                            Rlog.e(LOG_TAG, "Error getting phone number.", e);
                            if (c != null) {
                            }
                            return checkAndAppendPrefix(intent, subscription, number, context);
                        } catch (Throwable th) {
                            th = th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        c = c2;
                        if (c != null) {
                            c.close();
                        }
                        throw th;
                    }
                }
                if (c2 != null) {
                    c2.close();
                }
            } catch (RuntimeException e2) {
                e = e2;
                Rlog.e(LOG_TAG, "Error getting phone number.", e);
                if (c != null) {
                    c.close();
                }
                return checkAndAppendPrefix(intent, subscription, number, context);
            }
            return checkAndAppendPrefix(intent, subscription, number, context);
        }
    }
}
