package com.mediatek.common.regionalphone;

import android.net.Uri;
import android.provider.BaseColumns;

public class RegionalPhone {
    public static final Uri APN_URI = Uri.parse("content://com.mediatek.provider.regionalphone/apn");
    public static final String AUTHORITY = "com.mediatek.provider.regionalphone";
    public static final Uri BROWSER_URI = Uri.parse("content://com.mediatek.provider.regionalphone/browser");
    public static final String DATABASE_NAME = "regionalphone.db";
    public static final Uri MMS_SMS_URI = Uri.parse("content://com.mediatek.provider.regionalphone/mms_sms");
    public static final Uri SEARCHENGINE_URI = Uri.parse("content://com.mediatek.provider.regionalphone/searchengine");
    public static final Uri SETTINGS_URI = Uri.parse("content://com.mediatek.provider.regionalphone/settings");
    public static final String TABLE_APN = "apn";
    public static final String TABLE_BROWSER = "browser";
    public static final String TABLE_MMS_SMS = "mms_sms";
    public static final String TABLE_SEARCHENGINE = "searchengine";
    public static final String TABLE_SETTINGS = "settings";
    public static final String TABLE_WALLPAPER = "wallpaper";
    public static final Uri WALLPAPER_URI = Uri.parse("content://com.mediatek.provider.regionalphone/wallpaper");

    public static final class APN implements BaseColumns {
        public static final String MCC_MNC_TIMESTAMP = "mcc_mnc_timestamp";
        public static final String MMS_GPRS_APN = "mms_GPRS_APN";
        public static final String MMS_NAME = "mms_name";
        public static final String MMS_PORT = "mms_port";
        public static final String MMS_PROXY = "mms_proxy";
        public static final String MMS_SERVER = "mms_server";
        public static final String SMS_PREFERRED_BEARER = "sms_preferredBearer";
    }

    public static final class BROWSER implements BaseColumns {
        public static final String BOOKMARK_TITLE = "bookmarkTitle";
        public static final String BOOKMARK_URL = "bookmarkURL";
        public static final String IS_FOLDER = "folder";
        public static final String MCC_MNC_TIMESTAMP = "mcc_mnc_timestamp";
        public static final String PARENT = "parent";
        public static final String THUMBNAIL = "thumbnail";
    }

    public static final class MMS_SMS implements BaseColumns {
        public static final String MCC_MNC_TIMESTAMP = "mcc_mnc_timestamp";
        public static final String MMS_CREATION_MODE = "creationMode";
        public static final String SMS_C_NUMBER = "CNumber";
    }

    public static final class SEARCHENGINE implements BaseColumns {
        public static final String ENCODING = "encoding";
        public static final String FAVICON = "favicon";
        public static final String KEYWORD = "keyword";
        public static final String MCC_MNC_TIMESTAMP = "mcc_mnc_timestamp";
        public static final String SEARCH_ENGINE_LABEL = "searchEngineLabel";
        public static final String SEARCH_ENGINE_NAME = "searchEngineName";
        public static final String SEARCH_URL = "searchURL";
        public static final String SUGGESTION_URL = "suggestionURL";
    }

    public static final class SETTINGS implements BaseColumns {
        public static final String MCC_MNC_TIMESTAMP = "mcc_mnc_timestamp";
        public static final String NITZ_AUTOUPDATE = "NITZAutoUpdate";
        public static final String WIFI_DEFAULT = "wifi";
    }

    public static final class WALLPAPER implements BaseColumns {
        public static final String IMAGE_FILE_NAME = "fileName";
        public static final String MCC_MNC_TIMESTAMP = "mcc_mnc_timestamp";
    }
}
