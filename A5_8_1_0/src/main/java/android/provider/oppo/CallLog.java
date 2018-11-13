package android.provider.oppo;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.location.Country;
import android.location.CountryDetector;
import android.net.Uri;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.ContactsContract.CommonDataKinds.Callable;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.DataUsageFeedback;
import android.provider.Settings.System;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.InboundSmsHandler;
import com.android.internal.telephony.uicc.SpnOverride;
import java.util.List;

public class CallLog {
    public static final String AUTHORITY = "call_log";
    public static final Uri CONTENT_URI = Uri.parse("content://call_log");
    private static boolean isMtkGeminiSupport;
    private static boolean isMtkSupport;

    public static class Calls implements BaseColumns {
        public static final String ALLOW_VOICEMAILS_PARAM_KEY = "allow_voicemails";
        public static final int AUTOREJECTED_TYPE = 5;
        public static final String CACHED_FORMATTED_NUMBER = "formatted_number";
        public static final String CACHED_LOOKUP_URI = "lookup_uri";
        public static final String CACHED_MATCHED_NUMBER = "matched_number";
        public static final String CACHED_NAME = "name";
        public static final String CACHED_NORMALIZED_NUMBER = "normalized_number";
        public static final String CACHED_NUMBER_LABEL = "numberlabel";
        public static final String CACHED_NUMBER_TYPE = "numbertype";
        public static final String CACHED_PHOTO_ID = "photo_id";
        public static final Uri CONTENT_FILTER_URI = Uri.parse("content://call_log/calls/filter");
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/calls";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/calls";
        public static final Uri CONTENT_URI = Uri.parse("content://call_log/calls");
        public static final Uri CONTENT_URI_WITH_VOICEMAIL = CONTENT_URI.buildUpon().appendQueryParameter(ALLOW_VOICEMAILS_PARAM_KEY, "true").build();
        public static final String COUNTRY_ISO = "countryiso";
        public static final String DATA_ID = "data_id";
        public static final String DATA_USAGE = "data_usage";
        public static final String DATE = "date";
        public static final String DEFAULT_SORT_ORDER = "date DESC";
        public static final String DURATION = "duration";
        public static final String DURATION_TYPE = "duration_type";
        public static final int DURATION_TYPE_ACTIVE = 0;
        public static final int DURATION_TYPE_CALLOUT = 1;
        public static final String EXTRA_CALL_TYPE_FILTER = "android.provider.extra.CALL_TYPE_FILTER";
        public static final String FEATURES = "features";
        public static final int FEATURES_VIDEO = 1;
        public static final String GEOCODED_LOCATION = "geocoded_location";
        public static final int INCOMING_TYPE = 1;
        public static final String IP_PREFIX = "ip_prefix";
        public static final String IS_READ = "is_read";
        public static final String LIMIT_PARAM_KEY = "limit";
        private static final int MIN_DURATION_FOR_NORMALIZED_NUMBER_UPDATE_MS = 10000;
        public static final int MISSED_TYPE = 3;
        public static final String NEW = "new";
        public static final String NUMBER = "number";
        public static final String NUMBER_PRESENTATION = "presentation";
        public static final String OFFSET_PARAM_KEY = "offset";
        public static final int OUTGOING_TYPE = 2;
        public static final String PHONE_ACCOUNT_COMPONENT_NAME = "subscription_component_name";
        public static final String PHONE_ACCOUNT_ID = "subscription_id";
        public static final int PRESENTATION_ALLOWED = 1;
        public static final int PRESENTATION_PAYPHONE = 4;
        public static final int PRESENTATION_RESTRICTED = 2;
        public static final int PRESENTATION_UNKNOWN = 3;
        public static final String PRIVATE_NAME = "private_name";
        public static final String PRIVATE_TYPE = "private_type";
        public static final String RAW_CONTACT_ID = "raw_contact_id";
        public static final String RING_TIME = "ring_time";
        public static final String SIM_ID = "simid";
        public static final String SUB_ID = "sub_id";
        public static final String TRANSCRIPTION = "transcription";
        public static final String TYPE = "type";
        public static final int VOICEMAIL_TYPE = 4;
        public static final String VOICEMAIL_URI = "voicemail_uri";
        public static final String VTCALL = "vtcall";

        public static Uri addCall(CallerInfo ci, Context context, String number, int presentation, int callType, int features, PhoneAccountHandle accountHandle, long start, int duration, Long dataUsage) {
            return addCall(ci, context, number, presentation, callType, features, accountHandle, start, duration, dataUsage, false);
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, int presentation, int callType, int features, PhoneAccountHandle accountHandle, long start, int duration, Long dataUsage, boolean addForAllUsers) {
            return addCall(ci, context, number, presentation, callType, features, accountHandle, start, duration, dataUsage, addForAllUsers, 1, 0);
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, int presentation, int callType, int features, PhoneAccountHandle accountHandle, long start, int duration, Long dataUsage, boolean addForAllUsers, int newCall, int ringTime) {
            String ipPrefix;
            CallLog.isMtkSupport = context.getPackageManager().hasSystemFeature("mtk.gemini.support");
            CallLog.isMtkGeminiSupport = context.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.mtk");
            ContentResolver resolver = context.getContentResolver();
            int numberPresentation = 1;
            if (presentation == 2) {
                numberPresentation = 2;
            } else if (presentation == 4) {
                numberPresentation = 4;
            } else if (TextUtils.isEmpty(number) || presentation == 3) {
                numberPresentation = 3;
            }
            if (!(numberPresentation == 1 || ci == null)) {
                ci.name = SpnOverride.MVNO_TYPE_NONE;
            }
            String accountComponentString = null;
            String accountId = null;
            if (accountHandle != null) {
                accountComponentString = accountHandle.getComponentName().flattenToString();
                accountId = accountHandle.getId();
            }
            ContentValues contentValues = new ContentValues(6);
            contentValues.put("number", number);
            contentValues.put(NUMBER_PRESENTATION, Integer.valueOf(numberPresentation));
            contentValues.put(TYPE, Integer.valueOf(callType));
            contentValues.put(FEATURES, Integer.valueOf(features));
            contentValues.put(DATE, Long.valueOf(start));
            contentValues.put(DURATION, Long.valueOf((long) duration));
            if (dataUsage != null) {
                contentValues.put(DATA_USAGE, dataUsage);
            }
            contentValues.put(PHONE_ACCOUNT_COMPONENT_NAME, accountComponentString);
            contentValues.put(PHONE_ACCOUNT_ID, accountId);
            contentValues.put(NEW, Integer.valueOf(newCall));
            if (callType == 3) {
                contentValues.put(IS_READ, Integer.valueOf(0));
            }
            if (ci != null) {
                contentValues.put(CACHED_NAME, ci.name);
                contentValues.put(CACHED_NUMBER_TYPE, Integer.valueOf(ci.numberType));
                contentValues.put(CACHED_NUMBER_LABEL, ci.numberLabel);
            }
            int simId = 0;
            int cardId = 0;
            if (accountHandle != null) {
                cardId = Integer.parseInt(accountHandle.getId());
            }
            SubscriptionInfo subInfo = SubscriptionManager.from(context).getActiveSubscriptionInfo(cardId);
            if (subInfo != null) {
                simId = subInfo.getSubscriptionId();
            }
            contentValues.put(SIM_ID, Integer.valueOf(simId));
            if (features > 0) {
                contentValues.put(VTCALL, Integer.valueOf(features));
            }
            contentValues.put(RING_TIME, Integer.valueOf(ringTime));
            if (CallLog.isMtkSupport && CallLog.isMtkGeminiSupport) {
                ipPrefix = System.getString(resolver, "ipprefix" + simId);
            } else {
                ipPrefix = System.getString(resolver, "ipprefix");
            }
            if (!(ipPrefix == null || number == null || !number.startsWith(ipPrefix) || (number.equals(ipPrefix) ^ 1) == 0 || callType != 2)) {
                contentValues.put(IP_PREFIX, ipPrefix);
                contentValues.put("number", number.substring(ipPrefix.length(), number.length()));
            }
            Uri result = null;
            if (addForAllUsers) {
                UserManager userManager = (UserManager) context.getSystemService("user");
                List<UserInfo> users = userManager.getUsers(true);
                int currentUserId = userManager.getUserHandle();
                int count = users.size();
                for (int i = 0; i < count; i++) {
                    UserInfo user = (UserInfo) users.get(i);
                    UserHandle userHandle = user.getUserHandle();
                    if (userManager.isUserRunning(userHandle)) {
                        if (!((userManager.hasUserRestriction("no_outgoing_calls", userHandle) ^ 1) == 0 || (user.isManagedProfile() ^ 1) == 0)) {
                            Uri uri = addEntryAndRemoveExpiredEntries(context, ContentProvider.maybeAddUserId(CONTENT_URI, user.id), contentValues);
                            if (user.id == currentUserId) {
                                result = uri;
                            }
                        }
                    }
                }
            } else {
                result = addEntryAndRemoveExpiredEntries(context, CONTENT_URI, contentValues);
            }
            if (ci != null && ci.contactIdOrZero > 0) {
                Cursor cursor;
                if (ci.normalizedNumber != null) {
                    String normalizedPhoneNumber = ci.normalizedNumber;
                    cursor = resolver.query(Phone.CONTENT_URI, new String[]{"_id"}, "contact_id =? AND data4 =?", new String[]{String.valueOf(ci.contactIdOrZero), normalizedPhoneNumber}, null);
                } else {
                    cursor = resolver.query(Uri.withAppendedPath(Callable.CONTENT_FILTER_URI, Uri.encode(ci.phoneNumber != null ? ci.phoneNumber : number)), new String[]{"_id"}, "contact_id =?", new String[]{String.valueOf(ci.contactIdOrZero)}, null);
                }
                if (cursor != null) {
                    try {
                        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                            String dataId = cursor.getString(0);
                            updateDataUsageStatForData(resolver, dataId);
                            if (duration >= MIN_DURATION_FOR_NORMALIZED_NUMBER_UPDATE_MS && callType == 2 && TextUtils.isEmpty(ci.normalizedNumber)) {
                                updateNormalizedNumber(context, resolver, dataId, number);
                            }
                        }
                        cursor.close();
                    } catch (Throwable th) {
                        cursor.close();
                    }
                }
            }
            return result;
        }

        public static String getLastOutgoingCall(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = resolver.query(CONTENT_URI, new String[]{"number"}, "type = 2", null, "date DESC LIMIT 1");
                String str;
                if (cursor == null || (cursor.moveToFirst() ^ 1) != 0) {
                    str = SpnOverride.MVNO_TYPE_NONE;
                    return str;
                }
                str = cursor.getString(0);
                if (cursor != null) {
                    cursor.close();
                }
                return str;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        private static Uri addEntryAndRemoveExpiredEntries(Context context, Uri uri, ContentValues values) {
            ContentResolver resolver = context.getContentResolver();
            Uri result = resolver.insert(uri, values);
            resolver.delete(uri, "_id IN (SELECT _id FROM calls ORDER BY date DESC LIMIT -1 OFFSET 500)", null);
            return result;
        }

        private static void updateDataUsageStatForData(ContentResolver resolver, String dataId) {
            resolver.update(DataUsageFeedback.FEEDBACK_URI.buildUpon().appendPath(dataId).appendQueryParameter(TYPE, "call").build(), new ContentValues(), null, null);
        }

        /* JADX WARNING: Missing block: B:4:0x000c, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static void updateNormalizedNumber(Context context, ContentResolver resolver, String dataId, String number) {
            if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(dataId) && !TextUtils.isEmpty(getCurrentCountryIso(context))) {
                String normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, getCurrentCountryIso(context));
                if (!TextUtils.isEmpty(normalizedNumber)) {
                    ContentValues values = new ContentValues();
                    values.put("data4", normalizedNumber);
                    resolver.update(Data.CONTENT_URI, values, InboundSmsHandler.SELECT_BY_ID, new String[]{dataId});
                }
            }
        }

        private static String getCurrentCountryIso(Context context) {
            CountryDetector detector = (CountryDetector) context.getSystemService("country_detector");
            if (detector == null) {
                return null;
            }
            Country country = detector.detectCountry();
            if (country != null) {
                return country.getCountryIso();
            }
            return null;
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, int presentation, int callType, long start, int duration) {
            return addCall(ci, context, number, presentation, callType, start, duration, -1, -1);
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, int presentation, int callType, long start, int duration, int simId) {
            return addCall(ci, context, number, presentation, callType, start, duration, simId, -1);
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, int presentation, int callType, long start, int duration, int simId, int vtCall) {
            String ipPrefix;
            CallLog.isMtkSupport = context.getPackageManager().hasSystemFeature("mtk.gemini.support");
            CallLog.isMtkGeminiSupport = context.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.mtk");
            ContentResolver resolver = context.getContentResolver();
            if (presentation == 2) {
                number = "-2";
                if (ci != null) {
                    ci.name = SpnOverride.MVNO_TYPE_NONE;
                }
            } else if (presentation == 4) {
                number = "-3";
                if (ci != null) {
                    ci.name = SpnOverride.MVNO_TYPE_NONE;
                }
            } else if (TextUtils.isEmpty(number) || presentation == 3) {
                number = "-1";
                if (ci != null) {
                    ci.name = SpnOverride.MVNO_TYPE_NONE;
                }
            }
            ContentValues values = new ContentValues(5);
            values.put("number", number);
            values.put(TYPE, Integer.valueOf(callType));
            values.put(DATE, Long.valueOf(start));
            values.put(DURATION, Long.valueOf((long) duration));
            values.put(NEW, Integer.valueOf(1));
            if (callType == 3) {
                values.put(IS_READ, Integer.valueOf(0));
            }
            if (ci != null) {
                if (!ci.isEmergencyNumber()) {
                    values.put(CACHED_NAME, ci.name);
                }
                values.put(CACHED_NUMBER_TYPE, Integer.valueOf(ci.numberType));
                values.put(CACHED_NUMBER_LABEL, ci.numberLabel);
            }
            values.put(SIM_ID, Integer.valueOf(simId));
            if (vtCall >= 0) {
                values.put(VTCALL, Integer.valueOf(vtCall));
            }
            if (ci != null && ci.contactIdOrZero > 0) {
                Cursor cursor;
                if (ci.normalizedNumber != null) {
                    String normalizedPhoneNumber = ci.normalizedNumber;
                    cursor = resolver.query(Phone.CONTENT_URI, new String[]{"_id"}, "contact_id =? AND data4 =?", new String[]{String.valueOf(ci.contactIdOrZero), normalizedPhoneNumber}, null);
                } else {
                    cursor = resolver.query(Uri.withAppendedPath(Callable.CONTENT_FILTER_URI, Uri.encode(ci.phoneNumber != null ? ci.phoneNumber : number)), new String[]{"_id"}, "contact_id =?", new String[]{String.valueOf(ci.contactIdOrZero)}, null);
                }
                if (cursor != null) {
                    try {
                        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                            resolver.update(DataUsageFeedback.FEEDBACK_URI.buildUpon().appendPath(cursor.getString(0)).appendQueryParameter(TYPE, "call").build(), new ContentValues(), null, null);
                        }
                        cursor.close();
                    } catch (Throwable th) {
                        cursor.close();
                    }
                }
            }
            if (CallLog.isMtkSupport && CallLog.isMtkGeminiSupport) {
                ipPrefix = System.getString(resolver, "ipprefix" + simId);
            } else {
                ipPrefix = System.getString(resolver, "ipprefix");
            }
            if (!(ipPrefix == null || number == null || !number.startsWith(ipPrefix) || (number.equals(ipPrefix) ^ 1) == 0 || callType != 2)) {
                values.put(IP_PREFIX, ipPrefix);
                values.put("number", number.substring(ipPrefix.length(), number.length()));
            }
            Uri result = resolver.insert(CONTENT_URI, values);
            removeExpiredEntries(context);
            return result;
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, int presentation, int callType, long start, int duration, int simId, int vtCall, int ringTime) {
            String ipPrefix;
            CallLog.isMtkSupport = context.getPackageManager().hasSystemFeature("mtk.gemini.support");
            CallLog.isMtkGeminiSupport = context.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.mtk");
            ContentResolver resolver = context.getContentResolver();
            if (presentation == 2) {
                number = "-2";
                if (ci != null) {
                    ci.name = SpnOverride.MVNO_TYPE_NONE;
                }
            } else if (presentation == 4) {
                number = "-3";
                if (ci != null) {
                    ci.name = SpnOverride.MVNO_TYPE_NONE;
                }
            } else if (TextUtils.isEmpty(number) || presentation == 3) {
                number = "-1";
                if (ci != null) {
                    ci.name = SpnOverride.MVNO_TYPE_NONE;
                }
            }
            ContentValues values = new ContentValues(5);
            values.put("number", number);
            values.put(TYPE, Integer.valueOf(callType));
            values.put(DATE, Long.valueOf(start));
            values.put(DURATION, Long.valueOf((long) duration));
            values.put(NEW, Integer.valueOf(1));
            if (callType == 3) {
                values.put(IS_READ, Integer.valueOf(0));
            }
            if (ci != null) {
                if (!ci.isEmergencyNumber()) {
                    values.put(CACHED_NAME, ci.name);
                }
                values.put(CACHED_NUMBER_TYPE, Integer.valueOf(ci.numberType));
                values.put(CACHED_NUMBER_LABEL, ci.numberLabel);
            }
            values.put(SIM_ID, Integer.valueOf(simId));
            if (vtCall >= 0) {
                values.put(VTCALL, Integer.valueOf(vtCall));
            }
            values.put(RING_TIME, Integer.valueOf(ringTime));
            if (ci != null && ci.contactIdOrZero > 0) {
                Cursor cursor;
                if (ci.normalizedNumber != null) {
                    String normalizedPhoneNumber = ci.normalizedNumber;
                    cursor = resolver.query(Phone.CONTENT_URI, new String[]{"_id"}, "contact_id =? AND data4 =?", new String[]{String.valueOf(ci.contactIdOrZero), normalizedPhoneNumber}, null);
                } else {
                    cursor = resolver.query(Uri.withAppendedPath(Callable.CONTENT_FILTER_URI, Uri.encode(ci.phoneNumber != null ? ci.phoneNumber : number)), new String[]{"_id"}, "contact_id =?", new String[]{String.valueOf(ci.contactIdOrZero)}, null);
                }
                if (cursor != null) {
                    try {
                        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                            resolver.update(DataUsageFeedback.FEEDBACK_URI.buildUpon().appendPath(cursor.getString(0)).appendQueryParameter(TYPE, "call").build(), new ContentValues(), null, null);
                        }
                        cursor.close();
                    } catch (Throwable th) {
                        cursor.close();
                    }
                }
            }
            if (CallLog.isMtkSupport && CallLog.isMtkGeminiSupport) {
                ipPrefix = System.getString(resolver, "ipprefix" + simId);
            } else {
                ipPrefix = System.getString(resolver, "ipprefix");
            }
            if (!(ipPrefix == null || number == null || !number.startsWith(ipPrefix) || (number.equals(ipPrefix) ^ 1) == 0 || callType != 2)) {
                values.put(IP_PREFIX, ipPrefix);
                values.put("number", number.substring(ipPrefix.length(), number.length()));
            }
            Uri result = resolver.insert(CONTENT_URI, values);
            removeExpiredEntries(context);
            return result;
        }

        public static Uri addCall(CallerInfo ci, Context context, String number, String private_name, int presentation, int callType, long start, int duration, int simId, int vtCall, int private_type, int ringTime) {
            String ipPrefix;
            CallLog.isMtkSupport = context.getPackageManager().hasSystemFeature("mtk.gemini.support");
            CallLog.isMtkGeminiSupport = context.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.mtk");
            ContentResolver resolver = context.getContentResolver();
            if (presentation == 2) {
                number = "-2";
                if (ci != null) {
                    ci.name = SpnOverride.MVNO_TYPE_NONE;
                }
            } else if (presentation == 4) {
                number = "-3";
                if (ci != null) {
                    ci.name = SpnOverride.MVNO_TYPE_NONE;
                }
            } else if (TextUtils.isEmpty(number) || presentation == 3) {
                number = "-1";
                if (ci != null) {
                    ci.name = SpnOverride.MVNO_TYPE_NONE;
                }
            }
            ContentValues values = new ContentValues(8);
            values.put("number", number);
            values.put(TYPE, Integer.valueOf(callType));
            values.put(DATE, Long.valueOf(start));
            values.put(DURATION, Long.valueOf((long) duration));
            values.put(NEW, Integer.valueOf(1));
            values.put(PRIVATE_NAME, private_name);
            values.put(PRIVATE_TYPE, Integer.valueOf(private_type));
            values.put(SIM_ID, Integer.valueOf(simId));
            if (callType == 3) {
                values.put(IS_READ, Integer.valueOf(0));
            }
            if (ci != null) {
                values.put(CACHED_NAME, ci.name);
                values.put(CACHED_NUMBER_TYPE, Integer.valueOf(ci.numberType));
                values.put(CACHED_NUMBER_LABEL, ci.numberLabel);
            }
            if (vtCall >= 0) {
                values.put(VTCALL, Integer.valueOf(vtCall));
            }
            values.put(RING_TIME, Integer.valueOf(ringTime));
            if (ci != null && ci.contactIdOrZero > 0) {
                Cursor cursor;
                if (ci.normalizedNumber != null) {
                    String normalizedPhoneNumber = ci.normalizedNumber;
                    cursor = resolver.query(Phone.CONTENT_URI, new String[]{"_id"}, "contact_id =? AND data4 =?", new String[]{String.valueOf(ci.contactIdOrZero), normalizedPhoneNumber}, null);
                } else {
                    String phoneNumber = ci.phoneNumber != null ? ci.phoneNumber : number;
                    cursor = resolver.query(Phone.CONTENT_URI, new String[]{"_id"}, "contact_id =? AND data1 =?", new String[]{String.valueOf(ci.contactIdOrZero), phoneNumber}, null);
                }
                if (cursor != null) {
                    try {
                        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                            resolver.update(DataUsageFeedback.FEEDBACK_URI.buildUpon().appendPath(cursor.getString(0)).appendQueryParameter(TYPE, "call").build(), new ContentValues(), null, null);
                        }
                        cursor.close();
                    } catch (Throwable th) {
                        cursor.close();
                    }
                }
            }
            if (CallLog.isMtkSupport && CallLog.isMtkGeminiSupport) {
                ipPrefix = System.getString(resolver, "ipprefix" + simId);
            } else {
                ipPrefix = System.getString(resolver, "ipprefix");
            }
            if (!(ipPrefix == null || number == null || !number.startsWith(ipPrefix) || (number.equals(ipPrefix) ^ 1) == 0 || callType != 2)) {
                values.put(IP_PREFIX, ipPrefix);
                values.put("number", number.substring(ipPrefix.length(), number.length()));
            }
            Uri result = resolver.insert(CONTENT_URI, values);
            removeExpiredEntries(context);
            return result;
        }

        private static void removeExpiredEntries(Context context) {
            context.getContentResolver().delete(CONTENT_URI, "_id IN (SELECT _id FROM calls ORDER BY date DESC LIMIT -1 OFFSET 500)", null);
        }
    }
}
