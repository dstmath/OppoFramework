package android.provider;

import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech.Engine;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.util.SeempLog;
import com.android.ims.ImsConferenceState;
import com.android.internal.telephony.SmsApplication;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Telephony {
    public static final int[] SIMBackgroundRes = new int[]{0, 0, 0, 0};
    private static final String TAG = "Telephony";

    public interface BaseMmsColumns extends BaseColumns {
        @Deprecated
        public static final String ADAPTATION_ALLOWED = "adp_a";
        @Deprecated
        public static final String APPLIC_ID = "apl_id";
        @Deprecated
        public static final String AUX_APPLIC_ID = "aux_apl_id";
        @Deprecated
        public static final String CANCEL_ID = "cl_id";
        @Deprecated
        public static final String CANCEL_STATUS = "cl_st";
        public static final String CONTENT_CLASS = "ct_cls";
        public static final String CONTENT_LOCATION = "ct_l";
        public static final String CONTENT_TYPE = "ct_t";
        public static final String CREATOR = "creator";
        public static final String DATE = "date";
        public static final String DATE_SENT = "date_sent";
        public static final String DELIVERY_REPORT = "d_rpt";
        public static final String DELIVERY_TIME = "d_tm";
        @Deprecated
        public static final String DELIVERY_TIME_TOKEN = "d_tm_tok";
        @Deprecated
        public static final String DISTRIBUTION_INDICATOR = "d_ind";
        @Deprecated
        public static final String DRM_CONTENT = "drm_c";
        @Deprecated
        public static final String ELEMENT_DESCRIPTOR = "e_des";
        public static final String EXPIRY = "exp";
        @Deprecated
        public static final String LIMIT = "limit";
        public static final String LOCKED = "locked";
        @Deprecated
        public static final String MBOX_QUOTAS = "mb_qt";
        @Deprecated
        public static final String MBOX_QUOTAS_TOKEN = "mb_qt_tok";
        @Deprecated
        public static final String MBOX_TOTALS = "mb_t";
        @Deprecated
        public static final String MBOX_TOTALS_TOKEN = "mb_t_tok";
        public static final String MESSAGE_BOX = "msg_box";
        public static final int MESSAGE_BOX_ALL = 0;
        public static final int MESSAGE_BOX_DRAFTS = 3;
        public static final int MESSAGE_BOX_FAILED = 5;
        public static final int MESSAGE_BOX_INBOX = 1;
        public static final int MESSAGE_BOX_OUTBOX = 4;
        public static final int MESSAGE_BOX_SENT = 2;
        public static final String MESSAGE_CLASS = "m_cls";
        @Deprecated
        public static final String MESSAGE_COUNT = "m_cnt";
        public static final String MESSAGE_ID = "m_id";
        public static final String MESSAGE_SIZE = "m_size";
        public static final String MESSAGE_TYPE = "m_type";
        public static final String MMS_VERSION = "v";
        @Deprecated
        public static final String MM_FLAGS = "mm_flg";
        @Deprecated
        public static final String MM_FLAGS_TOKEN = "mm_flg_tok";
        @Deprecated
        public static final String MM_STATE = "mm_st";
        @Deprecated
        public static final String PREVIOUSLY_SENT_BY = "p_s_by";
        @Deprecated
        public static final String PREVIOUSLY_SENT_DATE = "p_s_d";
        public static final String PRIORITY = "pri";
        @Deprecated
        public static final String QUOTAS = "qt";
        public static final String READ = "read";
        public static final String READ_REPORT = "rr";
        public static final String READ_STATUS = "read_status";
        @Deprecated
        public static final String RECOMMENDED_RETRIEVAL_MODE = "r_r_mod";
        @Deprecated
        public static final String RECOMMENDED_RETRIEVAL_MODE_TEXT = "r_r_mod_txt";
        @Deprecated
        public static final String REPLACE_ID = "repl_id";
        @Deprecated
        public static final String REPLY_APPLIC_ID = "r_apl_id";
        @Deprecated
        public static final String REPLY_CHARGING = "r_chg";
        @Deprecated
        public static final String REPLY_CHARGING_DEADLINE = "r_chg_dl";
        @Deprecated
        public static final String REPLY_CHARGING_DEADLINE_TOKEN = "r_chg_dl_tok";
        @Deprecated
        public static final String REPLY_CHARGING_ID = "r_chg_id";
        @Deprecated
        public static final String REPLY_CHARGING_SIZE = "r_chg_sz";
        public static final String REPORT_ALLOWED = "rpt_a";
        public static final String RESPONSE_STATUS = "resp_st";
        public static final String RESPONSE_TEXT = "resp_txt";
        public static final String RETRIEVE_STATUS = "retr_st";
        public static final String RETRIEVE_TEXT = "retr_txt";
        public static final String RETRIEVE_TEXT_CHARSET = "retr_txt_cs";
        public static final String SEEN = "seen";
        @Deprecated
        public static final String SENDER_VISIBILITY = "s_vis";
        @Deprecated
        public static final String START = "start";
        public static final String STATUS = "st";
        @Deprecated
        public static final String STATUS_TEXT = "st_txt";
        @Deprecated
        public static final String STORE = "store";
        @Deprecated
        public static final String STORED = "stored";
        @Deprecated
        public static final String STORE_STATUS = "store_st";
        @Deprecated
        public static final String STORE_STATUS_TEXT = "store_st_txt";
        public static final String SUBJECT = "sub";
        public static final String SUBJECT_CHARSET = "sub_cs";
        public static final String SUBSCRIPTION_ID = "sub_id";
        public static final String TEXT_ONLY = "text_only";
        public static final String THREAD_ID = "thread_id";
        @Deprecated
        public static final String TOTALS = "totals";
        public static final String TRANSACTION_ID = "tr_id";
    }

    public interface CanonicalAddressesColumns extends BaseColumns {
        public static final String ADDRESS = "address";
    }

    public interface CarrierColumns extends BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://carrier_information/carrier");
        public static final String EXPIRATION_TIME = "expiration_time";
        public static final String KEY_IDENTIFIER = "key_identifier";
        public static final String KEY_TYPE = "key_type";
        public static final String LAST_MODIFIED = "last_modified";
        public static final String MCC = "mcc";
        public static final String MNC = "mnc";
        public static final String MVNO_MATCH_DATA = "mvno_match_data";
        public static final String MVNO_TYPE = "mvno_type";
        public static final String PUBLIC_KEY = "public_key";
    }

    public static final class Carriers implements BaseColumns {
        public static final String APN = "apn";
        public static final String AUTH_TYPE = "authtype";
        public static final String BEARER = "bearer";
        public static final String BEARER_BITMASK = "bearer_bitmask";
        public static final int CARRIER_DELETED = 5;
        public static final int CARRIER_DELETED_BUT_PRESENT_IN_XML = 6;
        public static final int CARRIER_EDITED = 4;
        public static final String CARRIER_ENABLED = "carrier_enabled";
        public static final Uri CONTENT_URI = Uri.parse("content://telephony/carriers");
        public static final String CURRENT = "current";
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        public static final String EDITED = "edited";
        public static final String IMSI = "imsi";
        public static final String MAX_CONNS = "max_conns";
        public static final String MAX_CONNS_TIME = "max_conns_time";
        public static final String MCC = "mcc";
        public static final String MMSC = "mmsc";
        public static final String MMSPORT = "mmsport";
        public static final String MMSPROXY = "mmsproxy";
        public static final String MNC = "mnc";
        public static final String MODEM_COGNITIVE = "modem_cognitive";
        public static final String MTU = "mtu";
        public static final String MVNO_MATCH_DATA = "mvno_match_data";
        public static final String MVNO_TYPE = "mvno_type";
        public static final String NAME = "name";
        public static final String NUMERIC = "numeric";
        public static final String OPPOSPN = "oppoSpn";
        public static final String PASSWORD = "password";
        public static final String PNN = "pnn";
        public static final String PORT = "port";
        public static final String PROFILE_ID = "profile_id";
        public static final String PROTOCOL = "protocol";
        public static final String PROXY = "proxy";
        public static final String ROAMING_PROTOCOL = "roaming_protocol";
        public static final String SERVER = "server";
        public static final String SOURCE_TYPE = "sourcetype";
        public static final String SPN = "spn";
        public static final String SUBSCRIPTION_ID = "sub_id";
        public static final String TYPE = "type";
        public static final int UNEDITED = 0;
        public static final String USER = "user";
        public static final int USER_DELETED = 2;
        public static final int USER_DELETED_BUT_PRESENT_IN_XML = 3;
        public static final String USER_EDITABLE = "user_editable";
        public static final int USER_EDITED = 1;
        public static final String USER_VISIBLE = "user_visible";
        public static final String WAIT_TIME = "wait_time";

        public static final class GeminiCarriers {
            public static final Uri CONTENT_URI = Uri.parse("content://telephony/carriers_gemini");
            public static final Uri CONTENT_URI_DM = Uri.parse("content://telephony/carriers_dm_gemini");
        }

        public static final class SIM1Carriers {
            public static final Uri CONTENT_URI = Uri.parse("content://telephony/carriers_sim1");
        }

        public static final class SIM2Carriers {
            public static final Uri CONTENT_URI = Uri.parse("content://telephony/carriers_sim2");
        }

        private Carriers() {
        }
    }

    public static final class CellBroadcasts implements BaseColumns {
        public static final String CID = "cid";
        public static final String CMAS_CATEGORY = "cmas_category";
        public static final String CMAS_CERTAINTY = "cmas_certainty";
        public static final String CMAS_MESSAGE_CLASS = "cmas_message_class";
        public static final String CMAS_RESPONSE_TYPE = "cmas_response_type";
        public static final String CMAS_SEVERITY = "cmas_severity";
        public static final String CMAS_URGENCY = "cmas_urgency";
        public static final Uri CONTENT_URI = Uri.parse("content://cellbroadcasts");
        public static final String DEFAULT_SORT_ORDER = "date DESC";
        public static final String DELIVERY_TIME = "date";
        public static final String ETWS_WARNING_TYPE = "etws_warning_type";
        public static final String GEOGRAPHICAL_SCOPE = "geo_scope";
        public static final String LAC = "lac";
        public static final String LANGUAGE_CODE = "language";
        public static final String MESSAGE_BODY = "body";
        public static final String MESSAGE_FORMAT = "format";
        public static final String MESSAGE_PRIORITY = "priority";
        public static final String MESSAGE_READ = "read";
        public static final String PLMN = "plmn";
        public static final String[] QUERY_COLUMNS = new String[]{"_id", "geo_scope", "plmn", "lac", "cid", "serial_number", "service_category", "language", "body", "date", "read", "format", "priority", "etws_warning_type", "cmas_message_class", "cmas_category", "cmas_response_type", "cmas_severity", "cmas_urgency", "cmas_certainty"};
        public static final String SERIAL_NUMBER = "serial_number";
        public static final String SERVICE_CATEGORY = "service_category";
        public static final String V1_MESSAGE_CODE = "message_code";
        public static final String V1_MESSAGE_IDENTIFIER = "message_id";

        private CellBroadcasts() {
        }
    }

    public static final class Mms implements BaseMmsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://mms");
        public static final String DEFAULT_SORT_ORDER = "date DESC";
        public static final Pattern NAME_ADDR_EMAIL_PATTERN = Pattern.compile("\\s*(\"[^\"]*\"|[^<>\"]+)\\s*<([^<>]+)>\\s*");
        public static final Uri REPORT_REQUEST_URI = Uri.withAppendedPath(CONTENT_URI, "report-request");
        public static final Uri REPORT_STATUS_URI = Uri.withAppendedPath(CONTENT_URI, "report-status");

        public static final class Addr implements BaseColumns {
            public static final String ADDRESS = "address";
            public static final String CHARSET = "charset";
            public static final String CONTACT_ID = "contact_id";
            public static final String MSG_ID = "msg_id";
            public static final String TYPE = "type";

            private Addr() {
            }
        }

        public static final class Draft implements BaseMmsColumns {
            public static final Uri CONTENT_URI = Uri.parse("content://mms/drafts");
            public static final String DEFAULT_SORT_ORDER = "date DESC";

            private Draft() {
            }
        }

        public static final class Inbox implements BaseMmsColumns {
            public static final Uri CONTENT_URI = Uri.parse("content://mms/inbox");
            public static final String DEFAULT_SORT_ORDER = "date DESC";

            private Inbox() {
            }
        }

        public static final class Intents {
            public static final String CONTENT_CHANGED_ACTION = "android.intent.action.CONTENT_CHANGED";
            public static final String DELETED_CONTENTS = "deleted_contents";

            private Intents() {
            }
        }

        public static final class Outbox implements BaseMmsColumns {
            public static final Uri CONTENT_URI = Uri.parse("content://mms/outbox");
            public static final String DEFAULT_SORT_ORDER = "date DESC";

            private Outbox() {
            }
        }

        public static final class Part implements BaseColumns {
            public static final String CHARSET = "chset";
            public static final String CONTENT_DISPOSITION = "cd";
            public static final String CONTENT_ID = "cid";
            public static final String CONTENT_LOCATION = "cl";
            public static final String CONTENT_TYPE = "ct";
            public static final String CT_START = "ctt_s";
            public static final String CT_TYPE = "ctt_t";
            public static final String FILENAME = "fn";
            public static final String MSG_ID = "mid";
            public static final String NAME = "name";
            public static final String SEQ = "seq";
            public static final String TEXT = "text";
            public static final String _DATA = "_data";

            private Part() {
            }
        }

        public static final class Rate {
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Mms.CONTENT_URI, Engine.KEY_PARAM_RATE);
            public static final String SENT_TIME = "sent_time";

            private Rate() {
            }
        }

        public static final class Sent implements BaseMmsColumns {
            public static final Uri CONTENT_URI = Uri.parse("content://mms/sent");
            public static final String DEFAULT_SORT_ORDER = "date DESC";

            private Sent() {
            }
        }

        private Mms() {
        }

        public static Cursor query(ContentResolver cr, String[] projection) {
            SeempLog.record(10);
            return cr.query(CONTENT_URI, projection, null, null, "date DESC");
        }

        public static Cursor query(ContentResolver cr, String[] projection, String where, String orderBy) {
            String str;
            SeempLog.record(10);
            Uri uri = CONTENT_URI;
            if (orderBy == null) {
                str = "date DESC";
            } else {
                str = orderBy;
            }
            return cr.query(uri, projection, where, null, str);
        }

        public static String extractAddrSpec(String address) {
            Matcher match = NAME_ADDR_EMAIL_PATTERN.matcher(address);
            if (match.matches()) {
                return match.group(2);
            }
            return address;
        }

        public static boolean isEmailAddress(String address) {
            if (TextUtils.isEmpty(address)) {
                return false;
            }
            return Patterns.EMAIL_ADDRESS.matcher(extractAddrSpec(address)).matches();
        }

        public static boolean isPhoneNumber(String number) {
            if (TextUtils.isEmpty(number)) {
                return false;
            }
            return Patterns.PHONE.matcher(number).matches();
        }
    }

    public static final class MmsSms implements BaseColumns {
        public static final Uri CONTENT_CONVERSATIONS_URI = Uri.parse("content://mms-sms/conversations");
        public static final Uri CONTENT_DRAFT_URI = Uri.parse("content://mms-sms/draft");
        public static final Uri CONTENT_FILTER_BYPHONE_URI = Uri.parse("content://mms-sms/messages/byphone");
        public static final Uri CONTENT_LOCKED_URI = Uri.parse("content://mms-sms/locked");
        public static final Uri CONTENT_UNDELIVERED_URI = Uri.parse("content://mms-sms/undelivered");
        public static final Uri CONTENT_URI = Uri.parse("content://mms-sms/");
        public static final int ERR_TYPE_GENERIC = 1;
        public static final int ERR_TYPE_GENERIC_PERMANENT = 10;
        public static final int ERR_TYPE_MMS_PROTO_PERMANENT = 12;
        public static final int ERR_TYPE_MMS_PROTO_TRANSIENT = 3;
        public static final int ERR_TYPE_SMS_PROTO_PERMANENT = 11;
        public static final int ERR_TYPE_SMS_PROTO_TRANSIENT = 2;
        public static final int ERR_TYPE_TRANSPORT_FAILURE = 4;
        public static final int MMS_PROTO = 1;
        public static final int NO_ERROR = 0;
        public static final Uri SEARCH_URI = Uri.parse("content://mms-sms/search");
        public static final int SMS_PROTO = 0;
        public static final String TYPE_DISCRIMINATOR_COLUMN = "transport_type";

        public static final class PendingMessages implements BaseColumns {
            public static final Uri CONTENT_URI = Uri.withAppendedPath(MmsSms.CONTENT_URI, ImsConferenceState.STATUS_PENDING);
            public static final String DUE_TIME = "due_time";
            public static final String ERROR_CODE = "err_code";
            public static final String ERROR_TYPE = "err_type";
            public static final String LAST_TRY = "last_try";
            public static final String MSG_ID = "msg_id";
            public static final String MSG_TYPE = "msg_type";
            public static final String PROTO_TYPE = "proto_type";
            public static final String RETRY_INDEX = "retry_index";
            public static final String SUBSCRIPTION_ID = "pending_sub_id";

            private PendingMessages() {
            }
        }

        public static final class WordsTable {
            public static final String ID = "_id";
            public static final String INDEXED_TEXT = "index_text";
            public static final String SOURCE_ROW_ID = "source_id";
            public static final String TABLE_ID = "table_to_use";

            private WordsTable() {
            }
        }

        private MmsSms() {
        }
    }

    public static final class Mwi implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://mwimsg");
        public static final String FROM = "from_account";
        public static final String GOT_CONTENT = "got_content";
        public static final String MSG_ACCOUNT = "msg_account";
        public static final String MSG_CONTEXT = "msg_context";
        public static final String MSG_DATE = "msg_date";
        public static final String MSG_ID = "msg_id";
        public static final String PRIORITY = "priority";
        public static final String READ = "read";
        public static final String SEEN = "seen";
        public static final String SUBJECT = "subject";
        public static final String TO = "to_account";
    }

    public static class SIMInfo {
        public static final String ACTION = "action";
        public static final String CREATE = "created";
        public static final String DATE = "date";
        public static final String ERROR = "error";
        public static final String EXPIRATION = "expiration";
        public static final String LOCKED = "locked";
        public static final String SIID = "siid";
        public static final int STATUS_LOCKED = 1;
        public static final int STATUS_READ = 1;
        public static final int STATUS_SEEN = 1;
        public static final int STATUS_UNLOCKED = 0;
        public static final int STATUS_UNREAD = 0;
        public static final int STATUS_UNSEEN = 0;
        public static final String SUBSCRIPTION_ID = "sub_id";
        public static final String TEXT = "text";
        public static final String TYPE = "type";
        public static final int TYPE_SL = 1;
        public static final String URL = "url";
        public int mColor;
        public int mDataRoaming = 0;
        public int mDispalyNumberFormat = 1;
        public String mDisplayName = "";
        public String mICCId;
        public int mNameSource;
        public String mNumber = "";
        public int mSimBackgroundRes = Telephony.SIMBackgroundRes[0];
        public long mSimId;
        public int mSlot = -1;
        public int mWapPush = -1;

        public static class ErrorCode {
            public static final int ERROR_GENERAL = -1;
            public static final int ERROR_NAME_EXIST = -2;
        }

        private SIMInfo() {
        }

        private static SIMInfo fromCursor(Cursor cursor, Context ctx) {
            SIMInfo info = new SIMInfo();
            info.mSimId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            info.mICCId = cursor.getString(cursor.getColumnIndexOrThrow("icc_id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("display_name"));
            Log.d(Telephony.TAG, "name:" + name + ",info.mSlot:" + info.mSlot + "info.mSimId:" + info.mSimId);
            if (name != null && name.equals("SIM1") && info.mSlot == 1) {
                info.mDisplayName = "SIM2";
                setDisplayNameEx(ctx, "SIM2", info.mSimId, 0);
            } else if (name != null && name.equals("SIM2") && info.mSlot == 0) {
                info.mDisplayName = "SIM1";
                setDisplayNameEx(ctx, "SIM1", info.mSimId, 0);
            } else {
                info.mDisplayName = name;
            }
            info.mNameSource = cursor.getInt(cursor.getColumnIndexOrThrow("name_source"));
            info.mNumber = cursor.getString(cursor.getColumnIndexOrThrow("number"));
            info.mDispalyNumberFormat = cursor.getInt(cursor.getColumnIndexOrThrow("display_number_format"));
            info.mColor = cursor.getInt(cursor.getColumnIndexOrThrow("color"));
            info.mDataRoaming = cursor.getInt(cursor.getColumnIndexOrThrow("data_roaming"));
            info.mSlot = cursor.getInt(cursor.getColumnIndexOrThrow("sim_id"));
            int size = Telephony.SIMBackgroundRes.length;
            if (info.mColor >= 0 && info.mColor < size) {
                info.mSimBackgroundRes = Telephony.SIMBackgroundRes[info.mColor];
            }
            info.mWapPush = cursor.getInt(cursor.getColumnIndexOrThrow("wap_push"));
            return info;
        }

        private static SIMInfo fromCursor(Cursor cursor) {
            SIMInfo info = new SIMInfo();
            info.mSimId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            info.mICCId = cursor.getString(cursor.getColumnIndexOrThrow("icc_id"));
            info.mDisplayName = cursor.getString(cursor.getColumnIndexOrThrow("display_name"));
            info.mNameSource = cursor.getInt(cursor.getColumnIndexOrThrow("name_source"));
            info.mNumber = cursor.getString(cursor.getColumnIndexOrThrow("number"));
            info.mDispalyNumberFormat = cursor.getInt(cursor.getColumnIndexOrThrow("display_number_format"));
            info.mColor = cursor.getInt(cursor.getColumnIndexOrThrow("color"));
            info.mDataRoaming = cursor.getInt(cursor.getColumnIndexOrThrow("data_roaming"));
            info.mSlot = cursor.getInt(cursor.getColumnIndexOrThrow("sim_id"));
            int size = Telephony.SIMBackgroundRes.length;
            if (info.mColor >= 0 && info.mColor < size) {
                info.mSimBackgroundRes = Telephony.SIMBackgroundRes[info.mColor];
            }
            info.mWapPush = cursor.getInt(cursor.getColumnIndexOrThrow("wap_push"));
            return info;
        }

        public static int setDisplayNameEx(Context ctx, String displayName, long SIMId, long Source) {
            Log.i(Telephony.TAG, "setDisplayNameEx SIMId " + SIMId + " source = " + Source + "displayName = " + displayName);
            if (displayName == null || SIMId <= 0) {
                return -1;
            }
            ContentValues value = new ContentValues(1);
            value.put("display_name", displayName);
            value.put("name_source", Long.valueOf(Source));
            return ctx.getContentResolver().update(ContentUris.withAppendedId(SimInfo.CONTENT_URI, SIMId), value, null, null);
        }
    }

    public static final class ScrapSpace {
        public static final Uri CONTENT_URI = Uri.parse("content://mms/scrapSpace");
        public static final String SCRAP_FILE_PATH = "/sdcard/mms/scrapSpace/.temp.jpg";
    }

    public static final class ServiceStateTable {
        public static final String AUTHORITY = "service-state";
        public static final String CDMA_DEFAULT_ROAMING_INDICATOR = "cdma_default_roaming_indicator";
        public static final String CDMA_ERI_ICON_INDEX = "cdma_eri_icon_index";
        public static final String CDMA_ERI_ICON_MODE = "cdma_eri_icon_mode";
        public static final String CDMA_ROAMING_INDICATOR = "cdma_roaming_indicator";
        public static final Uri CONTENT_URI = Uri.parse("content://service-state/");
        public static final String CSS_INDICATOR = "css_indicator";
        public static final String DATA_OPERATOR_ALPHA_LONG = "data_operator_alpha_long";
        public static final String DATA_OPERATOR_ALPHA_SHORT = "data_operator_alpha_short";
        public static final String DATA_OPERATOR_NUMERIC = "data_operator_numeric";
        public static final String DATA_REG_STATE = "data_reg_state";
        public static final String DATA_ROAMING_TYPE = "data_roaming_type";
        public static final String IS_DATA_ROAMING_FROM_REGISTRATION = "is_data_roaming_from_registration";
        public static final String IS_EMERGENCY_ONLY = "is_emergency_only";
        public static final String IS_MANUAL_NETWORK_SELECTION = "is_manual_network_selection";
        public static final String IS_USING_CARRIER_AGGREGATION = "is_using_carrier_aggregation";
        public static final String NETWORK_ID = "network_id";
        public static final String RIL_DATA_RADIO_TECHNOLOGY = "ril_data_radio_technology";
        public static final String RIL_VOICE_RADIO_TECHNOLOGY = "ril_voice_radio_technology";
        public static final String SYSTEM_ID = "system_id";
        public static final String VOICE_OPERATOR_ALPHA_LONG = "voice_operator_alpha_long";
        public static final String VOICE_OPERATOR_ALPHA_SHORT = "voice_operator_alpha_short";
        public static final String VOICE_OPERATOR_NUMERIC = "voice_operator_numeric";
        public static final String VOICE_REG_STATE = "voice_reg_state";
        public static final String VOICE_ROAMING_TYPE = "voice_roaming_type";

        private ServiceStateTable() {
        }

        public static Uri getUriForSubscriptionIdAndField(int subscriptionId, String field) {
            return CONTENT_URI.buildUpon().appendEncodedPath(String.valueOf(subscriptionId)).appendEncodedPath(field).build();
        }

        public static Uri getUriForSubscriptionId(int subscriptionId) {
            return CONTENT_URI.buildUpon().appendEncodedPath(String.valueOf(subscriptionId)).build();
        }

        public static ContentValues getContentValuesForServiceState(ServiceState state) {
            ContentValues values = new ContentValues();
            values.put("voice_reg_state", Integer.valueOf(state.getVoiceRegState()));
            values.put("data_reg_state", Integer.valueOf(state.getDataRegState()));
            values.put("voice_roaming_type", Integer.valueOf(state.getVoiceRoamingType()));
            values.put("data_roaming_type", Integer.valueOf(state.getDataRoamingType()));
            values.put("voice_operator_alpha_long", state.getVoiceOperatorAlphaLong());
            values.put("voice_operator_alpha_short", state.getVoiceOperatorAlphaShort());
            values.put("voice_operator_numeric", state.getVoiceOperatorNumeric());
            values.put("data_operator_alpha_long", state.getDataOperatorAlphaLong());
            values.put("data_operator_alpha_short", state.getDataOperatorAlphaShort());
            values.put("data_operator_numeric", state.getDataOperatorNumeric());
            values.put("is_manual_network_selection", Boolean.valueOf(state.getIsManualSelection()));
            values.put("ril_voice_radio_technology", Integer.valueOf(state.getRilVoiceRadioTechnology()));
            values.put("ril_data_radio_technology", Integer.valueOf(state.getRilDataRadioTechnology()));
            values.put("css_indicator", Integer.valueOf(state.getCssIndicator()));
            values.put("network_id", Integer.valueOf(state.getNetworkId()));
            values.put("system_id", Integer.valueOf(state.getSystemId()));
            values.put("cdma_roaming_indicator", Integer.valueOf(state.getCdmaRoamingIndicator()));
            values.put("cdma_default_roaming_indicator", Integer.valueOf(state.getCdmaDefaultRoamingIndicator()));
            values.put("cdma_eri_icon_index", Integer.valueOf(state.getCdmaEriIconIndex()));
            values.put("cdma_eri_icon_mode", Integer.valueOf(state.getCdmaEriIconMode()));
            values.put("is_emergency_only", Boolean.valueOf(state.isEmergencyOnly()));
            values.put("is_data_roaming_from_registration", Boolean.valueOf(state.getDataRoamingFromRegistration()));
            values.put("is_using_carrier_aggregation", Boolean.valueOf(state.isUsingCarrierAggregation()));
            return values;
        }
    }

    public static final class SimInfo implements BaseColumns {
        public static final String COLOR = "color";
        public static final int COLOR_1 = 0;
        public static final int COLOR_2 = 1;
        public static final int COLOR_3 = 2;
        public static final int COLOR_4 = 3;
        public static final int COLOR_DEFAULT = 0;
        public static final Uri CONTENT_URI = Uri.parse("content://telephony/siminfo");
        public static final String DATA_ROAMING = "data_roaming";
        public static final int DATA_ROAMING_DEFAULT = 0;
        public static final int DATA_ROAMING_DISABLE = 0;
        public static final int DATA_ROAMING_ENABLE = 1;
        public static final int DEFAULT_NAME_MAX_INDEX = 99;
        public static final int DEFAULT_NAME_MIN_INDEX = 1;
        public static final int DEFAULT_NAME_RES = 0;
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        public static final int DEFAULT_SOURCE = 0;
        public static final int DISLPAY_NUMBER_DEFAULT = 1;
        public static final int DISPALY_NUMBER_NONE = 0;
        public static final String DISPLAY_NAME = "display_name";
        public static final int DISPLAY_NUMBER_FIRST = 1;
        public static final String DISPLAY_NUMBER_FORMAT = "display_number_format";
        public static final int DISPLAY_NUMBER_LAST = 2;
        public static final int ERROR_GENERAL = -1;
        public static final int ERROR_NAME_EXIST = -2;
        public static final String ICC_ID = "icc_id";
        public static final String NAME_SOURCE = "name_source";
        public static final String NUMBER = "number";
        public static final int SIM_SOURCE = 1;
        public static final String SLOT = "sim_id";
        public static final int SLOT_NONE = -1;
        public static final int USER_INPUT = 2;
        public static final String WAP_PUSH = "wap_push";
        public static final int WAP_PUSH_DEFAULT = -1;
        public static final int WAP_PUSH_DISABLE = 0;
        public static final int WAP_PUSH_ENABLE = 1;
    }

    public interface TextBasedSmsColumns {
        public static final String ADDRESS = "address";
        public static final String BODY = "body";
        public static final String CREATOR = "creator";
        public static final String DATE = "date";
        public static final String DATE_SENT = "date_sent";
        public static final String ERROR_CODE = "error_code";
        public static final String GROUPADDRESS = "oppo_groupaddress";
        public static final String LOCKED = "locked";
        public static final String MASS = "oppo_mass";
        public static final int MESSAGE_TYPE_ALL = 0;
        public static final int MESSAGE_TYPE_DRAFT = 3;
        public static final int MESSAGE_TYPE_FAILED = 5;
        public static final int MESSAGE_TYPE_INBOX = 1;
        public static final int MESSAGE_TYPE_OUTBOX = 4;
        public static final int MESSAGE_TYPE_QUEUED = 6;
        public static final int MESSAGE_TYPE_SENT = 2;
        public static final String MSG_ID = "msgid";
        public static final String MTU = "mtu";
        public static final String PERSON = "person";
        public static final String PHONE_ID = "phone_id";
        public static final String PRIORITY = "priority";
        public static final String PROTOCOL = "protocol";
        public static final String READ = "read";
        public static final String REPLY_PATH_PRESENT = "reply_path_present";
        public static final String SEEN = "seen";
        public static final String SERVICE_CENTER = "service_center";
        public static final String STATUS = "status";
        public static final int STATUS_COMPLETE = 0;
        public static final int STATUS_FAILED = 64;
        public static final int STATUS_NONE = -1;
        public static final int STATUS_PENDING = 32;
        public static final String SUBJECT = "subject";
        public static final String SUBSCRIPTION_ID = "sub_id";
        public static final String THREAD_ID = "thread_id";
        public static final String TYPE = "type";
    }

    public static final class Sms implements BaseColumns, TextBasedSmsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://sms");
        public static final String DEFAULT_SORT_ORDER = "date DESC";

        public static final class Conversations implements BaseColumns, TextBasedSmsColumns {
            public static final Uri CONTENT_URI = Uri.parse("content://sms/conversations");
            public static final String DEFAULT_SORT_ORDER = "date DESC";
            public static final String MESSAGE_COUNT = "msg_count";
            public static final String SNIPPET = "snippet";

            private Conversations() {
            }
        }

        public static final class Draft implements BaseColumns, TextBasedSmsColumns {
            public static final Uri CONTENT_URI = Uri.parse("content://sms/draft");
            public static final String DEFAULT_SORT_ORDER = "date DESC";

            private Draft() {
            }

            public static Uri addMessage(ContentResolver resolver, String address, String body, String subject, Long date) {
                return Sms.addMessageToUri(SubscriptionManager.getDefaultSmsSubscriptionId(), resolver, CONTENT_URI, address, body, subject, date, true, false);
            }

            public static Uri addMessage(int subId, ContentResolver resolver, String address, String body, String subject, Long date) {
                return Sms.addMessageToUri(subId, resolver, CONTENT_URI, address, body, subject, date, true, false);
            }
        }

        public static final class GroupMsg implements BaseColumns, TextBasedSmsColumns {
            public static final Uri CONTENT_URI = Uri.parse("content://sms/groupmsg");
            public static final String DEFAULT_SORT_ORDER = "date DESC";

            public static Uri addMessage(ContentResolver resolver, String[] address, int vMsgId, int vType, long threadId) {
                ArrayList<ContentProviderOperation> operationList = new ArrayList();
                for (Object withValue : address) {
                    Builder builder = ContentProviderOperation.newInsert(CONTENT_URI);
                    builder.withValue("msgid", Integer.valueOf(vMsgId));
                    builder.withValue("address", withValue);
                    builder.withValue("type", Integer.valueOf(vType));
                    builder.withValue("thread_id", Long.valueOf(threadId));
                    operationList.add(builder.build());
                }
                try {
                    ContentProviderResult[] applyBatch = resolver.applyBatch("sms", operationList);
                } catch (RemoteException e) {
                    Log.d(Telephony.TAG, "oppoa==" + e);
                } catch (OperationApplicationException e2) {
                    Log.d(Telephony.TAG, "oppob==" + e2);
                }
                return null;
            }

            public static Uri addMessageGemini(ContentResolver resolver, String[] address, int vMsgId, int vType, long threadId, int subId) {
                ArrayList<ContentProviderOperation> operationList = new ArrayList();
                for (Object withValue : address) {
                    Builder builder = ContentProviderOperation.newInsert(CONTENT_URI);
                    builder.withValue("msgid", Integer.valueOf(vMsgId));
                    builder.withValue("address", withValue);
                    builder.withValue("type", Integer.valueOf(vType));
                    builder.withValue("thread_id", Long.valueOf(threadId));
                    builder.withValue(TextBasedSmsColumns.PHONE_ID, Integer.valueOf(SubscriptionManager.getPhoneId(subId)));
                    operationList.add(builder.build());
                }
                try {
                    ContentProviderResult[] applyBatch = resolver.applyBatch("sms", operationList);
                } catch (RemoteException e) {
                    Log.d(Telephony.TAG, "oppoa==" + e);
                } catch (OperationApplicationException e2) {
                    Log.d(Telephony.TAG, "oppob==" + e2);
                }
                return null;
            }
        }

        public static final class Inbox implements BaseColumns, TextBasedSmsColumns {
            public static final Uri CONTENT_URI = Uri.parse("content://sms/inbox");
            public static final String DEFAULT_SORT_ORDER = "date DESC";

            private Inbox() {
            }

            public static Uri addMessage(ContentResolver resolver, String address, String body, String subject, Long date, boolean read) {
                return Sms.addMessageToUri(SubscriptionManager.getDefaultSmsSubscriptionId(), resolver, CONTENT_URI, address, body, subject, date, read, false);
            }

            public static Uri addMessage(int subId, ContentResolver resolver, String address, String body, String subject, Long date, boolean read) {
                return Sms.addMessageToUri(subId, resolver, CONTENT_URI, address, body, subject, date, read, false);
            }
        }

        public static final class Intents {
            public static final String ACTION_CHANGE_DEFAULT = "android.provider.Telephony.ACTION_CHANGE_DEFAULT";
            public static final String ACTION_DEFAULT_SMS_PACKAGE_CHANGED = "android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED";
            public static final String ACTION_EXTERNAL_PROVIDER_CHANGE = "android.provider.action.EXTERNAL_PROVIDER_CHANGE";
            public static final String DATA_SMS_RECEIVED_ACTION = "android.intent.action.DATA_SMS_RECEIVED";
            public static final String EXTRA_IS_DEFAULT_SMS_APP = "android.provider.extra.IS_DEFAULT_SMS_APP";
            public static final String EXTRA_PACKAGE_NAME = "package";
            public static final String MMS_DOWNLOADED_ACTION = "android.provider.Telephony.MMS_DOWNLOADED";
            public static final int RESULT_SMS_DUPLICATED = 5;
            public static final int RESULT_SMS_GENERIC_ERROR = 2;
            public static final int RESULT_SMS_HANDLED = 1;
            public static final int RESULT_SMS_OUT_OF_MEMORY = 3;
            public static final int RESULT_SMS_UNSUPPORTED = 4;
            public static final String SIM_FULL_ACTION = "android.provider.Telephony.SIM_FULL";
            public static final String SMS_CARRIER_PROVISION_ACTION = "android.provider.Telephony.SMS_CARRIER_PROVISION";
            public static final String SMS_CB_RECEIVED_ACTION = "android.provider.Telephony.SMS_CB_RECEIVED";
            public static final String SMS_DELIVER_ACTION = "android.provider.Telephony.SMS_DELIVER";
            public static final String SMS_EMERGENCY_CB_RECEIVED_ACTION = "android.provider.Telephony.SMS_EMERGENCY_CB_RECEIVED";
            public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
            public static final String SMS_REJECTED_ACTION = "android.provider.Telephony.SMS_REJECTED";
            public static final String SMS_SERVICE_CATEGORY_PROGRAM_DATA_RECEIVED_ACTION = "android.provider.Telephony.SMS_SERVICE_CATEGORY_PROGRAM_DATA_RECEIVED";
            public static final String WAP_PUSH_DELIVER_ACTION = "android.provider.Telephony.WAP_PUSH_DELIVER";
            public static final String WAP_PUSH_RECEIVED_ACTION = "android.provider.Telephony.WAP_PUSH_RECEIVED";

            private Intents() {
            }

            public static SmsMessage[] getMessagesFromIntent(Intent intent) {
                try {
                    Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
                    if (messages == null) {
                        Rlog.e(Telephony.TAG, "pdus does not exist in the intent");
                        return null;
                    }
                    String format = intent.getStringExtra("format");
                    int subId = intent.getIntExtra("subscription", SubscriptionManager.getDefaultSmsSubscriptionId());
                    Rlog.v(Telephony.TAG, " getMessagesFromIntent sub_id : " + subId);
                    int pduCount = messages.length;
                    SmsMessage[] msgs = new SmsMessage[pduCount];
                    for (int i = 0; i < pduCount; i++) {
                        msgs[i] = SmsMessage.createFromPdu(messages[i], format);
                        if (msgs[i] != null) {
                            msgs[i].setSubId(subId);
                        }
                    }
                    return msgs;
                } catch (ClassCastException e) {
                    Rlog.e(Telephony.TAG, "getMessagesFromIntent: " + e);
                    return null;
                }
            }
        }

        public static final class Outbox implements BaseColumns, TextBasedSmsColumns {
            public static final Uri CONTENT_URI = Uri.parse("content://sms/outbox");
            public static final String DEFAULT_SORT_ORDER = "date DESC";

            private Outbox() {
            }

            public static Uri addMessage(ContentResolver resolver, String address, String body, String subject, Long date, boolean deliveryReport, long threadId) {
                return Sms.addMessageToUri(SubscriptionManager.getDefaultSmsSubscriptionId(), resolver, CONTENT_URI, address, body, subject, date, true, deliveryReport, threadId);
            }

            public static Uri addGroupMessage(ContentResolver resolver, String address, String body, String subject, Long date, boolean deliveryReport, long threadId, String dest) {
                return Sms.addGroupMessageToUri(resolver, CONTENT_URI, address, body, subject, date, true, deliveryReport, threadId, dest);
            }

            public static Uri addMessage(int subId, ContentResolver resolver, String address, String body, String subject, Long date, boolean deliveryReport, long threadId) {
                return Sms.addMessageToUri(subId, resolver, CONTENT_URI, address, body, subject, date, true, deliveryReport, threadId);
            }
        }

        public static final class Sent implements BaseColumns, TextBasedSmsColumns {
            public static final Uri CONTENT_URI = Uri.parse("content://sms/sent");
            public static final String DEFAULT_SORT_ORDER = "date DESC";

            private Sent() {
            }

            public static Uri addMessage(ContentResolver resolver, String address, String body, String subject, Long date) {
                return Sms.addMessageToUri(SubscriptionManager.getDefaultSmsSubscriptionId(), resolver, CONTENT_URI, address, body, subject, date, true, false);
            }

            public static Uri addMessage(int subId, ContentResolver resolver, String address, String body, String subject, Long date) {
                return Sms.addMessageToUri(subId, resolver, CONTENT_URI, address, body, subject, date, true, false);
            }
        }

        private Sms() {
        }

        public static String getDefaultSmsPackage(Context context) {
            ComponentName component = SmsApplication.getDefaultSmsApplication(context, false);
            if (component != null) {
                return component.getPackageName();
            }
            return null;
        }

        public static Cursor query(ContentResolver cr, String[] projection) {
            SeempLog.record(10);
            return cr.query(CONTENT_URI, projection, null, null, "date DESC");
        }

        public static Cursor query(ContentResolver cr, String[] projection, String where, String orderBy) {
            String str;
            SeempLog.record(10);
            Uri uri = CONTENT_URI;
            if (orderBy == null) {
                str = "date DESC";
            } else {
                str = orderBy;
            }
            return cr.query(uri, projection, where, null, str);
        }

        public static Uri addMessageToUri(ContentResolver resolver, Uri uri, String address, String body, String subject, Long date, boolean read, boolean deliveryReport) {
            return addMessageToUri(SubscriptionManager.getDefaultSmsSubscriptionId(), resolver, uri, address, body, subject, date, read, deliveryReport, -1);
        }

        public static Uri addMessageToUri(int subId, ContentResolver resolver, Uri uri, String address, String body, String subject, Long date, boolean read, boolean deliveryReport) {
            return addMessageToUri(subId, resolver, uri, address, body, subject, date, read, deliveryReport, -1);
        }

        public static Uri addMessageToUri(ContentResolver resolver, Uri uri, String address, String body, String subject, Long date, boolean read, boolean deliveryReport, long threadId) {
            return addMessageToUri(SubscriptionManager.getDefaultSmsSubscriptionId(), resolver, uri, address, body, subject, date, read, deliveryReport, threadId);
        }

        public static Uri addGroupMessageToUri(ContentResolver resolver, Uri uri, String address, String body, String subject, Long date, boolean read, boolean deliveryReport, long threadId, String dest) {
            ContentValues values = new ContentValues(8);
            values.put("oppo_groupaddress", dest);
            values.put("address", address);
            if (date != null) {
                values.put("date", date);
            }
            values.put("read", read ? Integer.valueOf(1) : Integer.valueOf(0));
            values.put("subject", subject);
            values.put("body", body);
            if (deliveryReport) {
                values.put("status", Integer.valueOf(32));
            }
            if (threadId != -1) {
                values.put("thread_id", Long.valueOf(threadId));
            }
            values.put("oppo_mass", Integer.valueOf(1));
            return resolver.insert(uri, values);
        }

        public static Uri addGroupMessageToUriGemini(ContentResolver resolver, Uri uri, String address, String body, String subject, Long date, boolean read, boolean deliveryReport, long threadId, String dest, int subId) {
            ContentValues values = new ContentValues(10);
            values.put("oppo_groupaddress", dest);
            values.put("address", address);
            if (date != null) {
                values.put("date", date);
            }
            values.put("read", read ? Integer.valueOf(1) : Integer.valueOf(0));
            values.put("subject", subject);
            values.put("body", body);
            if (deliveryReport) {
                values.put("status", Integer.valueOf(32));
            }
            if (threadId != -1) {
                values.put("thread_id", Long.valueOf(threadId));
            }
            values.put("oppo_mass", Integer.valueOf(1));
            values.put(TextBasedSmsColumns.PHONE_ID, Integer.valueOf(SubscriptionManager.getPhoneId(subId)));
            return resolver.insert(uri, values);
        }

        public static Uri addMessageToUri(int subId, ContentResolver resolver, Uri uri, String address, String body, String subject, Long date, boolean read, boolean deliveryReport, long threadId) {
            return addMessageToUri(subId, resolver, uri, address, body, subject, date, read, deliveryReport, threadId, -1);
        }

        public static Uri addMessageToUri(int subId, ContentResolver resolver, Uri uri, String address, String body, String subject, Long date, boolean read, boolean deliveryReport, long threadId, int priority) {
            ContentValues values = new ContentValues(8);
            Rlog.v(Telephony.TAG, "Telephony addMessageToUri sub id: " + subId);
            values.put("sub_id", Integer.valueOf(subId));
            values.put("address", address);
            if (date != null) {
                values.put("date", date);
            }
            values.put("read", read ? Integer.valueOf(1) : Integer.valueOf(0));
            values.put("subject", subject);
            values.put("body", body);
            values.put("priority", Integer.valueOf(priority));
            if (deliveryReport) {
                values.put("status", Integer.valueOf(32));
            }
            if (threadId != -1) {
                values.put("thread_id", Long.valueOf(threadId));
            }
            return resolver.insert(uri, values);
        }

        public static boolean moveMessageToFolder(Context context, Uri uri, int folder, int error) {
            if (uri == null) {
                return false;
            }
            boolean z;
            boolean markAsUnread = false;
            boolean markAsRead = false;
            switch (folder) {
                case 1:
                case 3:
                    break;
                case 2:
                case 4:
                    markAsRead = true;
                    break;
                case 5:
                case 6:
                    markAsUnread = true;
                    break;
                default:
                    return false;
            }
            ContentValues values = new ContentValues(3);
            values.put("type", Integer.valueOf(folder));
            if (markAsUnread) {
                values.put("read", Integer.valueOf(0));
            } else if (markAsRead) {
                values.put("read", Integer.valueOf(1));
            }
            values.put("error_code", Integer.valueOf(error));
            if (1 == SqliteWrapper.update(context, context.getContentResolver(), uri, values, null, null)) {
                z = true;
            } else {
                z = false;
            }
            return z;
        }

        public static boolean isOutgoingFolder(int messageType) {
            if (messageType == 5 || messageType == 4 || messageType == 2 || messageType == 6) {
                return true;
            }
            return false;
        }
    }

    public interface ThreadsColumns extends BaseColumns {
        public static final String ARCHIVED = "archived";
        public static final String ATTACHMENT_INFO = "attachment_info";
        public static final String DATE = "date";
        public static final String ERROR = "error";
        public static final String HAS_ATTACHMENT = "has_attachment";
        public static final String MESSAGE_COUNT = "message_count";
        public static final String NOTIFICATION = "notification";
        public static final String READ = "read";
        public static final String RECIPIENT_IDS = "recipient_ids";
        public static final String SNIPPET = "snippet";
        public static final String SNIPPET_CHARSET = "snippet_cs";
        public static final String TYPE = "type";
    }

    public static final class Threads implements ThreadsColumns {
        public static final int BROADCAST_THREAD = 1;
        public static final int COMMON_THREAD = 0;
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MmsSms.CONTENT_URI, "conversations");
        private static final String[] ID_PROJECTION = new String[]{"_id"};
        public static final Uri OBSOLETE_THREADS_URI = Uri.withAppendedPath(CONTENT_URI, "obsolete");
        public static final String STATUS = "status";
        private static final Uri THREAD_ID_CONTENT_URI = Uri.parse("content://mms-sms/threadID");

        private Threads() {
        }

        public static long getOrCreateThreadId(Context context, String recipient) {
            Set recipients = new HashSet();
            recipients.add(recipient);
            return getOrCreateThreadId(context, recipients);
        }

        public static long getOrCreateThreadId(Context context, Set<String> recipients) {
            Uri.Builder uriBuilder = THREAD_ID_CONTENT_URI.buildUpon();
            for (String recipient : recipients) {
                String recipient2;
                if (Mms.isEmailAddress(recipient2)) {
                    recipient2 = Mms.extractAddrSpec(recipient2);
                }
                uriBuilder.appendQueryParameter("recipient", recipient2);
            }
            Context context2 = context;
            Cursor cursor = SqliteWrapper.query(context2, context.getContentResolver(), uriBuilder.build(), ID_PROJECTION, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        long j = cursor.getLong(0);
                        return j;
                    }
                    Rlog.e(Telephony.TAG, "getOrCreateThreadId returned no rows!");
                    cursor.close();
                } finally {
                    cursor.close();
                }
            }
            Rlog.e(Telephony.TAG, "getOrCreateThreadId failed with " + recipients.size() + " recipients");
            throw new IllegalArgumentException("Unable to find or allocate a thread ID.");
        }
    }

    public static final class WapPush implements BaseColumns {
        public static final String ACTION = "action";
        public static final String ADDR = "address";
        public static final Uri CONTENT_URI = Uri.parse("content://wappush");
        public static final Uri CONTENT_URI_SI = Uri.parse("content://wappush/si");
        public static final Uri CONTENT_URI_SL = Uri.parse("content://wappush/sl");
        public static final Uri CONTENT_URI_THREAD = Uri.parse("content://wappush/thread_id");
        public static final String CREATE = "created";
        public static final String DATE = "date";
        public static final String DEFAULT_SORT_ORDER = "date ASC";
        public static final String ERROR = "error";
        public static final String EXPIRATION = "expiration";
        public static final String LOCKED = "locked";
        public static final String READ = "read";
        public static final String SEEN = "seen";
        public static final String SERVICE_ADDR = "service_center";
        public static final String SIID = "siid";
        public static final int STATUS_LOCKED = 1;
        public static final int STATUS_READ = 1;
        public static final int STATUS_SEEN = 1;
        public static final int STATUS_UNLOCKED = 0;
        public static final int STATUS_UNREAD = 0;
        public static final int STATUS_UNSEEN = 0;
        public static final String SUBSCRIPTION_ID = "sub_id";
        public static final String TEXT = "text";
        public static final String THREAD_ID = "thread_id";
        public static final String TYPE = "type";
        public static final int TYPE_SI = 0;
        public static final int TYPE_SL = 1;
        public static final String URL = "url";
    }

    private Telephony() {
    }
}
