package android.provider;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiManager;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class Downloads {
    private static final String QUERY_WHERE_CLAUSE = "notificationpackage=? AND notificationclass=?";

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static final class Impl implements BaseColumns {
        public static final String ACTION_DOWNLOAD_COMPLETED = "android.intent.action.DOWNLOAD_COMPLETED";
        public static final String ACTION_NOTIFICATION_CLICKED = "android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED";
        public static final Uri ALL_DOWNLOADS_CONTENT_URI = null;
        public static final String AUTHORITY = "downloads";
        public static final String COLUMN_ALLOWED_NETWORK_TYPES = "allowed_network_types";
        public static final String COLUMN_ALLOW_METERED = "allow_metered";
        public static final String COLUMN_ALLOW_ROAMING = "allow_roaming";
        public static final String COLUMN_ALLOW_WRITE = "allow_write";
        public static final String COLUMN_APP_DATA = "entity";
        public static final String COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT = "bypass_recommended_size_limit";
        public static final String COLUMN_CONTINUE_DOWNLOAD_WITH_SAME_FILENAME = "continue_download_with_same_filename";
        public static final String COLUMN_CONTROL = "control";
        public static final String COLUMN_COOKIE_DATA = "cookiedata";
        public static final String COLUMN_CURRENT_BYTES = "current_bytes";
        public static final String COLUMN_DELETED = "deleted";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DESTINATION = "destination";
        public static final String COLUMN_DOWNLOAD_PATH_SELECTED = "download_path_selected_from_filemanager";
        public static final String COLUMN_ERROR_MSG = "errorMsg";
        public static final String COLUMN_FAILED_CONNECTIONS = "numfailed";
        public static final String COLUMN_FILE_NAME_HINT = "hint";
        public static final String COLUMN_FLAGS = "flags";
        public static final String COLUMN_IS_PUBLIC_API = "is_public_api";
        public static final String COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI = "is_visible_in_downloads_ui";
        public static final String COLUMN_LAST_MODIFICATION = "lastmod";
        public static final String COLUMN_LAST_UPDATESRC = "lastUpdateSrc";
        public static final String COLUMN_MEDIAPROVIDER_URI = "mediaprovider_uri";
        public static final String COLUMN_MEDIA_SCANNED = "scanned";
        public static final String COLUMN_MIME_TYPE = "mimetype";
        public static final String COLUMN_NOTIFICATION_CLASS = "notificationclass";
        public static final String COLUMN_NOTIFICATION_EXTRAS = "notificationextras";
        public static final String COLUMN_NOTIFICATION_PACKAGE = "notificationpackage";
        public static final String COLUMN_NO_INTEGRITY = "no_integrity";
        public static final String COLUMN_OMA_DOWNLOAD_DD_FILE_INFO_DESCRIPTION = "OMA_Download_DDFileInfo_Description";
        public static final String COLUMN_OMA_DOWNLOAD_DD_FILE_INFO_NAME = "OMA_Download_DDFileInfo_Name";
        public static final String COLUMN_OMA_DOWNLOAD_DD_FILE_INFO_SIZE = "OMA_Download_DDFileInfo_Size";
        public static final String COLUMN_OMA_DOWNLOAD_DD_FILE_INFO_TYPE = "OMA_Download_DDFileInfo_Type";
        public static final String COLUMN_OMA_DOWNLOAD_DD_FILE_INFO_VENDOR = "OMA_Download_DDFileInfo_Vendor";
        public static final String COLUMN_OMA_DOWNLOAD_FLAG = "OMA_Download";
        public static final String COLUMN_OMA_DOWNLOAD_INSTALL_NOTIFY_URL = "OMA_Download_Install_Notify_Url";
        public static final String COLUMN_OMA_DOWNLOAD_NEXT_URL = "OMA_Download_Next_Url";
        public static final String COLUMN_OMA_DOWNLOAD_OBJECT_URL = "OMA_Download_Object_Url";
        public static final String COLUMN_OMA_DOWNLOAD_STATUS = "OMA_Download_Status";
        public static final String COLUMN_OTHER_UID = "otheruid";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_REFERER = "referer";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_TOTAL_BYTES = "total_bytes";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_USER_AGENT = "useragent";
        public static final String COLUMN_VISIBILITY = "visibility";
        public static final Uri CONTENT_URI = null;
        public static final int CONTROL_PAUSED = 1;
        public static final int CONTROL_RUN = 0;
        public static final int DESTINATION_CACHE_PARTITION = 1;
        public static final int DESTINATION_CACHE_PARTITION_NOROAMING = 3;
        public static final int DESTINATION_CACHE_PARTITION_PURGEABLE = 2;
        public static final int DESTINATION_EXTERNAL = 0;
        public static final int DESTINATION_FILE_URI = 4;
        public static final int DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD = 6;
        public static final int DESTINATION_SYSTEMCACHE_PARTITION = 5;
        public static final String DRM_RIGHT_VALID = "drm_right_valid";
        public static final int FLAG_REQUIRES_CHARGING = 1;
        public static final int FLAG_REQUIRES_DEVICE_IDLE = 2;
        public static final int LAST_UPDATESRC_DONT_NOTIFY_DOWNLOADSVC = 1;
        public static final int LAST_UPDATESRC_NOT_RELEVANT = 0;
        public static final int MIN_ARTIFICIAL_ERROR_STATUS = 488;
        public static final String OMADL_ERROR_NEED_NOTIFY = "OMADL_ERROR_NEED_NOTIFY";
        public static final int OMADL_STATUS_DOWNLOAD_COMPLETELY = 200;
        public static final int OMADL_STATUS_ERROR_ALERTDIALOG_SHOWED = 599;
        public static final int OMADL_STATUS_ERROR_ATTRIBUTE_MISMATCH = 512;
        public static final int OMADL_STATUS_ERROR_INSTALL_FAILED = 400;
        public static final int OMADL_STATUS_ERROR_INSUFFICIENT_MEMORY = 403;
        public static final int OMADL_STATUS_ERROR_INVALID_DDVERSION = 515;
        public static final int OMADL_STATUS_ERROR_INVALID_DESCRIPTOR = 404;
        public static final int OMADL_STATUS_ERROR_NON_ACCEPTABLE_CONTENT = 492;
        public static final int OMADL_STATUS_ERROR_USER_CANCELLED = 490;
        public static final int OMADL_STATUS_ERROR_USER_DOWNLOAD_MEDIA_OBJECT = 491;
        public static final int OMADL_STATUS_HAS_NEXT_URL = 203;
        public static final int OMADL_STATUS_PARSE_DDFILE_SUCCESS = 201;
        public static final boolean OMA_DOWNLOAD_SUPPORT = true;
        public static final String PERMISSION_ACCESS = "android.permission.ACCESS_DOWNLOAD_MANAGER";
        public static final String PERMISSION_ACCESS_ADVANCED = "android.permission.ACCESS_DOWNLOAD_MANAGER_ADVANCED";
        public static final String PERMISSION_ACCESS_ALL = "android.permission.ACCESS_ALL_DOWNLOADS";
        public static final String PERMISSION_CACHE = "android.permission.ACCESS_CACHE_FILESYSTEM";
        public static final String PERMISSION_CACHE_NON_PURGEABLE = "android.permission.DOWNLOAD_CACHE_NON_PURGEABLE";
        public static final String PERMISSION_NO_NOTIFICATION = "android.permission.DOWNLOAD_WITHOUT_NOTIFICATION";
        public static final String PERMISSION_SEND_INTENTS = "android.permission.SEND_DOWNLOAD_COMPLETED_INTENTS";
        public static final Uri PUBLICLY_ACCESSIBLE_DOWNLOADS_URI = null;
        public static final String PUBLICLY_ACCESSIBLE_DOWNLOADS_URI_SEGMENT = "public_downloads";
        public static final int STATUS_BAD_REQUEST = 400;
        @Deprecated
        public static final int STATUS_BLOCKED = 498;
        public static final int STATUS_CANCELED = 490;
        public static final int STATUS_CANNOT_RESUME = 489;
        public static final int STATUS_DEVICE_NOT_FOUND_ERROR = 199;
        public static final int STATUS_FILE_ALREADY_EXISTS_ERROR = 488;
        public static final int STATUS_FILE_ERROR = 492;
        public static final int STATUS_HTTP_DATA_ERROR = 495;
        public static final int STATUS_HTTP_EXCEPTION = 496;
        public static final int STATUS_INSUFFICIENT_SPACE_ERROR = 198;
        public static final int STATUS_LENGTH_REQUIRED = 411;
        public static final int STATUS_NEED_HTTP_AUTH = 401;
        public static final int STATUS_NOT_ACCEPTABLE = 406;
        public static final int STATUS_PAUSED_BY_APP = 193;
        public static final int STATUS_PENDING = 190;
        public static final int STATUS_PRECONDITION_FAILED = 412;
        public static final int STATUS_QUEUED_FOR_WIFI = 196;
        public static final int STATUS_RUNNING = 192;
        public static final int STATUS_SUCCESS = 200;
        public static final int STATUS_TOO_MANY_REDIRECTS = 497;
        public static final int STATUS_UNHANDLED_HTTP_CODE = 494;
        public static final int STATUS_UNHANDLED_REDIRECT = 493;
        public static final int STATUS_UNKNOWN_ERROR = 491;
        public static final int STATUS_WAITING_FOR_NETWORK = 195;
        public static final int STATUS_WAITING_TO_RETRY = 194;
        public static final int VISIBILITY_HIDDEN = 2;
        public static final int VISIBILITY_VISIBLE = 0;
        public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;
        public static final String _DATA = "_data";

        public static class RequestHeaders {
            public static final String COLUMN_DOWNLOAD_ID = "download_id";
            public static final String COLUMN_HEADER = "header";
            public static final String COLUMN_VALUE = "value";
            public static final String HEADERS_DB_TABLE = "request_headers";
            public static final String INSERT_KEY_PREFIX = "http_header_";
            public static final String URI_SEGMENT = "headers";

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Downloads.Impl.RequestHeaders.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public RequestHeaders() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Downloads.Impl.RequestHeaders.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.provider.Downloads.Impl.RequestHeaders.<init>():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Downloads.Impl.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Downloads.Impl.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Downloads.Impl.<clinit>():void");
        }

        private Impl() {
        }

        public static boolean isStatusInformational(int status) {
            return status >= 100 && status < 200;
        }

        public static boolean isStatusSuccess(int status) {
            return status >= 200 && status < 300;
        }

        public static boolean isStatusError(int status) {
            return status >= 400 && status < CalendarColumns.CAL_ACCESS_EDITOR;
        }

        public static boolean isStatusClientError(int status) {
            return status >= 400 && status < 500;
        }

        public static boolean isStatusServerError(int status) {
            return status >= 500 && status < CalendarColumns.CAL_ACCESS_EDITOR;
        }

        public static boolean isNotificationToBeDisplayed(int visibility) {
            if (visibility == 1 || visibility == 3) {
                return true;
            }
            return false;
        }

        public static boolean isStatusCompleted(int status) {
            if (status < 200 || status >= 300) {
                return status >= 400 && status < CalendarColumns.CAL_ACCESS_EDITOR;
            } else {
                return true;
            }
        }

        public static String statusToString(int status) {
            switch (status) {
                case 190:
                    return "PENDING";
                case 192:
                    return "RUNNING";
                case 193:
                    return "PAUSED_BY_APP";
                case 194:
                    return "WAITING_TO_RETRY";
                case 195:
                    return "WAITING_FOR_NETWORK";
                case 196:
                    return "QUEUED_FOR_WIFI";
                case 198:
                    return "INSUFFICIENT_SPACE_ERROR";
                case 199:
                    return "DEVICE_NOT_FOUND_ERROR";
                case 200:
                    return WifiManager.PPPOE_STATUS_SUCCESS;
                case 400:
                    return "BAD_REQUEST";
                case 406:
                    return "NOT_ACCEPTABLE";
                case STATUS_LENGTH_REQUIRED /*411*/:
                    return "LENGTH_REQUIRED";
                case STATUS_PRECONDITION_FAILED /*412*/:
                    return "PRECONDITION_FAILED";
                case 488:
                    return "FILE_ALREADY_EXISTS_ERROR";
                case STATUS_CANNOT_RESUME /*489*/:
                    return "CANNOT_RESUME";
                case 490:
                    return "CANCELED";
                case 491:
                    return "UNKNOWN_ERROR";
                case 492:
                    return "FILE_ERROR";
                case STATUS_UNHANDLED_REDIRECT /*493*/:
                    return "UNHANDLED_REDIRECT";
                case STATUS_UNHANDLED_HTTP_CODE /*494*/:
                    return "UNHANDLED_HTTP_CODE";
                case STATUS_HTTP_DATA_ERROR /*495*/:
                    return "HTTP_DATA_ERROR";
                case STATUS_HTTP_EXCEPTION /*496*/:
                    return "HTTP_EXCEPTION";
                case STATUS_TOO_MANY_REDIRECTS /*497*/:
                    return "TOO_MANY_REDIRECTS";
                case STATUS_BLOCKED /*498*/:
                    return "BLOCKED";
                default:
                    return Integer.toString(status);
            }
        }
    }

    private Downloads() {
    }

    public static final void removeAllDownloadsByPackage(Context context, String notification_package, String notification_class) {
        context.getContentResolver().delete(Impl.CONTENT_URI, QUERY_WHERE_CLAUSE, new String[]{notification_package, notification_class});
    }
}
