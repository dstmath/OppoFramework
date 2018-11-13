package android.app;

import android.app.backup.FullBackup;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.provider.Downloads.Impl;
import android.provider.Downloads.Impl.RequestHeaders;
import android.provider.MediaStore.Files;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class DownloadManager {
    public static final String ACTION_DOWNLOAD_COMPLETE = "android.intent.action.DOWNLOAD_COMPLETE";
    public static final String ACTION_NOTIFICATION_CLICKED = "android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED";
    public static final String ACTION_VIEW_DOWNLOADS = "android.intent.action.VIEW_DOWNLOADS";
    public static final String COLUMN_ALLOW_WRITE = "allow_write";
    public static final String COLUMN_BYTES_DOWNLOADED_SO_FAR = "bytes_so_far";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LAST_MODIFIED_TIMESTAMP = "last_modified_timestamp";
    @Deprecated
    public static final String COLUMN_LOCAL_FILENAME = "local_filename";
    public static final String COLUMN_LOCAL_URI = "local_uri";
    public static final String COLUMN_MEDIAPROVIDER_URI = "mediaprovider_uri";
    public static final String COLUMN_MEDIA_SCANNED = "scanned";
    public static final String COLUMN_MEDIA_TYPE = "media_type";
    public static final String COLUMN_REASON = "reason";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TOTAL_SIZE_BYTES = "total_size";
    public static final String COLUMN_URI = "uri";
    public static final int ERROR_BLOCKED = 1010;
    public static final int ERROR_CANNOT_RESUME = 1008;
    public static final int ERROR_DEVICE_NOT_FOUND = 1007;
    public static final int ERROR_FILE_ALREADY_EXISTS = 1009;
    public static final int ERROR_FILE_ERROR = 1001;
    public static final int ERROR_HTTP_DATA_ERROR = 1004;
    public static final int ERROR_INSUFFICIENT_SPACE = 1006;
    public static final int ERROR_INVALID_DESCRIPTOR = 1011;
    public static final int ERROR_TOO_MANY_REDIRECTS = 1005;
    public static final int ERROR_UNHANDLED_HTTP_CODE = 1002;
    public static final int ERROR_UNKNOWN = 1000;
    public static final String EXTRA_DOWNLOAD_ID = "extra_download_id";
    public static final String EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS = "extra_click_download_ids";
    public static final String INTENT_EXTRAS_SORT_BY_SIZE = "android.app.DownloadManager.extra_sortBySize";
    private static final String NON_DOWNLOADMANAGER_DOWNLOAD = "non-dwnldmngr-download-dont-retry2download";
    public static final int PAUSED_BY_APP = 5;
    public static final int PAUSED_QUEUED_FOR_WIFI = 3;
    public static final int PAUSED_UNKNOWN = 4;
    public static final int PAUSED_WAITING_FOR_NETWORK = 2;
    public static final int PAUSED_WAITING_TO_RETRY = 1;
    public static final int STATUS_FAILED = 16;
    public static final int STATUS_FAILED_INSUFFICIENT_MEMORY = 32;
    public static final int STATUS_FAILED_INVALID_DESCRIPTOR = 64;
    public static final int STATUS_PAUSED = 4;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_RUNNING = 2;
    public static final int STATUS_SUCCESSFUL = 8;
    public static final String[] UNDERLYING_COLUMNS = null;
    private static final String XLOGTAG = "DownloadManager/Framework";
    private boolean mAccessFilename;
    private Uri mBaseUri;
    private final String mPackageName;
    private final ContentResolver mResolver;

    private static class CursorTranslator extends CursorWrapper {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f4-assertionsDisabled = false;
        private final boolean mAccessFilename;
        private final Uri mBaseUri;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.app.DownloadManager.CursorTranslator.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.app.DownloadManager.CursorTranslator.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.DownloadManager.CursorTranslator.<clinit>():void");
        }

        public CursorTranslator(Cursor cursor, Uri baseUri, boolean accessFilename) {
            super(cursor);
            this.mBaseUri = baseUri;
            this.mAccessFilename = accessFilename;
        }

        public int getInt(int columnIndex) {
            return (int) getLong(columnIndex);
        }

        public long getLong(int columnIndex) {
            if (getColumnName(columnIndex).equals("reason")) {
                return getReason(super.getInt(getColumnIndex("status")));
            }
            if (getColumnName(columnIndex).equals("status")) {
                return (long) translateStatus(super.getInt(getColumnIndex("status")));
            }
            return super.getLong(columnIndex);
        }

        public String getString(int columnIndex) {
            String columnName = getColumnName(columnIndex);
            if (columnName.equals(DownloadManager.COLUMN_LOCAL_URI)) {
                return getLocalUri();
            }
            if (!columnName.equals(DownloadManager.COLUMN_LOCAL_FILENAME) || this.mAccessFilename) {
                return super.getString(columnIndex);
            }
            throw new SecurityException("COLUMN_LOCAL_FILENAME is deprecated; use ContentResolver.openFileDescriptor() instead");
        }

        private String getLocalUri() {
            long destinationType = getLong(getColumnIndex(Impl.COLUMN_DESTINATION));
            if (destinationType == 4 || destinationType == 0 || destinationType == 6) {
                String localPath = super.getString(getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                if (localPath == null) {
                    return null;
                }
                return Uri.fromFile(new File(localPath)).toString();
            }
            return ContentUris.withAppendedId(Impl.ALL_DOWNLOADS_CONTENT_URI, getLong(getColumnIndex("_id"))).toString();
        }

        private long getReason(int status) {
            switch (translateStatus(status)) {
                case 4:
                    return getPausedReason(status);
                case 16:
                    return getErrorCode(status);
                default:
                    return 0;
            }
        }

        private long getPausedReason(int status) {
            switch (status) {
                case 193:
                    return 5;
                case 194:
                    return 1;
                case 195:
                    return 2;
                case 196:
                    return 3;
                default:
                    return 4;
            }
        }

        private long getErrorCode(int status) {
            if ((400 <= status && status < 488) || (500 <= status && status < CalendarColumns.CAL_ACCESS_EDITOR)) {
                return (long) status;
            }
            switch (status) {
                case 198:
                    return 1006;
                case 199:
                    return 1007;
                case 488:
                    return 1009;
                case Impl.STATUS_CANNOT_RESUME /*489*/:
                    return 1008;
                case 492:
                    return 1001;
                case Impl.STATUS_UNHANDLED_REDIRECT /*493*/:
                case Impl.STATUS_UNHANDLED_HTTP_CODE /*494*/:
                    return 1002;
                case Impl.STATUS_HTTP_DATA_ERROR /*495*/:
                    return 1004;
                case Impl.STATUS_TOO_MANY_REDIRECTS /*497*/:
                    return 1005;
                case Impl.STATUS_BLOCKED /*498*/:
                    return 1010;
                default:
                    return 1000;
            }
        }

        private int translateStatus(int status) {
            switch (status) {
                case 190:
                    return 1;
                case 192:
                    return 2;
                case 193:
                case 194:
                case 195:
                case 196:
                    return 4;
                case 200:
                    return 8;
                default:
                    if (f4-assertionsDisabled || Impl.isStatusError(status)) {
                        return 16;
                    }
                    throw new AssertionError();
            }
        }
    }

    public static class Query {
        public static final int ORDER_ASCENDING = 1;
        public static final int ORDER_DESCENDING = 2;
        private long[] mIds;
        private boolean mOnlyIncludeVisibleInDownloadsUi;
        private String mOrderByColumn;
        private int mOrderDirection;
        private Integer mStatusFlags;

        public Query() {
            this.mIds = null;
            this.mStatusFlags = null;
            this.mOrderByColumn = Impl.COLUMN_LAST_MODIFICATION;
            this.mOrderDirection = 2;
            this.mOnlyIncludeVisibleInDownloadsUi = false;
        }

        public Query setFilterById(long... ids) {
            this.mIds = ids;
            return this;
        }

        public Query setFilterByStatus(int flags) {
            this.mStatusFlags = Integer.valueOf(flags);
            return this;
        }

        public Query setOnlyIncludeVisibleInDownloadsUi(boolean value) {
            this.mOnlyIncludeVisibleInDownloadsUi = value;
            return this;
        }

        public Query orderBy(String column, int direction) {
            if (direction == 1 || direction == 2) {
                if (column.equals(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)) {
                    this.mOrderByColumn = Impl.COLUMN_LAST_MODIFICATION;
                } else if (column.equals(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)) {
                    this.mOrderByColumn = Impl.COLUMN_TOTAL_BYTES;
                } else {
                    throw new IllegalArgumentException("Cannot order by " + column);
                }
                this.mOrderDirection = direction;
                return this;
            }
            throw new IllegalArgumentException("Invalid direction: " + direction);
        }

        Cursor runQuery(ContentResolver resolver, String[] projection, Uri baseUri) {
            Uri uri = baseUri;
            List<String> selectionParts = new ArrayList();
            String[] selectionArgs = null;
            if (this.mIds != null) {
                selectionParts.add(DownloadManager.getWhereClauseForIds(this.mIds));
                selectionArgs = DownloadManager.getWhereArgsForIds(this.mIds);
            }
            if (this.mStatusFlags != null) {
                List<String> parts = new ArrayList();
                if ((this.mStatusFlags.intValue() & 1) != 0) {
                    parts.add(statusClause("=", 190));
                }
                if ((this.mStatusFlags.intValue() & 2) != 0) {
                    parts.add(statusClause("=", 192));
                }
                if ((this.mStatusFlags.intValue() & 4) != 0) {
                    parts.add(statusClause("=", 193));
                    parts.add(statusClause("=", 194));
                    parts.add(statusClause("=", 195));
                    parts.add(statusClause("=", 196));
                }
                if ((this.mStatusFlags.intValue() & 8) != 0) {
                    parts.add(statusClause("=", 200));
                }
                if ((this.mStatusFlags.intValue() & 16) != 0) {
                    parts.add("(" + statusClause(">=", 400) + " AND " + statusClause("<", CalendarColumns.CAL_ACCESS_EDITOR) + ")");
                }
                selectionParts.add(joinStrings(" OR ", parts));
            }
            if (this.mOnlyIncludeVisibleInDownloadsUi) {
                selectionParts.add("is_visible_in_downloads_ui != '0'");
            }
            selectionParts.add("deleted != '1'");
            return resolver.query(baseUri, projection, joinStrings(" AND ", selectionParts), selectionArgs, this.mOrderByColumn + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + (this.mOrderDirection == 1 ? "ASC" : "DESC"));
        }

        private String joinStrings(String joiner, Iterable<String> parts) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (String part : parts) {
                if (!first) {
                    builder.append(joiner);
                }
                builder.append(part);
                first = false;
            }
            return builder.toString();
        }

        private String statusClause(String operator, int value) {
            return "status" + operator + "'" + value + "'";
        }
    }

    public static class Request {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f5-assertionsDisabled = false;
        @Deprecated
        public static final int NETWORK_BLUETOOTH = 4;
        public static final int NETWORK_MOBILE = 1;
        public static final int NETWORK_WIFI = 2;
        private static final int SCANNABLE_VALUE_NO = 2;
        private static final int SCANNABLE_VALUE_YES = 0;
        public static final int VISIBILITY_HIDDEN = 2;
        public static final int VISIBILITY_VISIBLE = 0;
        public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;
        public static final int VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION = 3;
        private int mAllowedNetworkTypes;
        private CharSequence mDescription;
        private Uri mDestinationUri;
        private int mFlags;
        private boolean mIsVisibleInDownloadsUi;
        private boolean mMeteredAllowed;
        private String mMimeType;
        private int mNotificationVisibility;
        private List<Pair<String, String>> mRequestHeaders;
        private boolean mRoamingAllowed;
        private boolean mScannable;
        private CharSequence mTitle;
        private Uri mUri;
        private boolean mUseSystemCache;
        private String mUserAgent;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.app.DownloadManager.Request.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.app.DownloadManager.Request.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.DownloadManager.Request.<clinit>():void");
        }

        public Request(Uri uri) {
            this.mRequestHeaders = new ArrayList();
            this.mAllowedNetworkTypes = -1;
            this.mRoamingAllowed = true;
            this.mMeteredAllowed = true;
            this.mFlags = 0;
            this.mIsVisibleInDownloadsUi = true;
            this.mScannable = false;
            this.mUseSystemCache = false;
            this.mNotificationVisibility = 0;
            if (uri == null) {
                throw new NullPointerException();
            }
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equals(IntentFilter.SCHEME_HTTP) || scheme.equals(IntentFilter.SCHEME_HTTPS))) {
                throw new IllegalArgumentException("Can only download HTTP/HTTPS URIs: " + uri);
            }
            this.mUri = uri;
        }

        Request(String uriString) {
            this.mRequestHeaders = new ArrayList();
            this.mAllowedNetworkTypes = -1;
            this.mRoamingAllowed = true;
            this.mMeteredAllowed = true;
            this.mFlags = 0;
            this.mIsVisibleInDownloadsUi = true;
            this.mScannable = false;
            this.mUseSystemCache = false;
            this.mNotificationVisibility = 0;
            this.mUri = Uri.parse(uriString);
        }

        public Request setDestinationUri(Uri uri) {
            this.mDestinationUri = uri;
            Log.v(DownloadManager.XLOGTAG, "setDestinationUri: mDestinationUri " + this.mDestinationUri);
            return this;
        }

        public Request setDestinationToSystemCache() {
            this.mUseSystemCache = true;
            return this;
        }

        public Request setDestinationInExternalFilesDir(Context context, String dirType, String subPath) {
            File file = context.getExternalFilesDir(dirType);
            if (file == null) {
                throw new IllegalStateException("Failed to get external storage files directory");
            }
            if (file.exists()) {
                if (!file.isDirectory()) {
                    throw new IllegalStateException(file.getAbsolutePath() + " already exists and is not a directory");
                }
            } else if (!file.mkdirs()) {
                throw new IllegalStateException("Unable to create directory: " + file.getAbsolutePath());
            }
            setDestinationFromBase(file, subPath);
            return this;
        }

        public Request setDestinationInExternalPublicDir(String dirType, String subPath) {
            File file = Environment.getExternalStoragePublicDirectory(dirType);
            Log.v(DownloadManager.XLOGTAG, "setExternalPublicDir: dirType " + dirType + " subPath " + subPath + "file" + file);
            if (file == null) {
                throw new IllegalStateException("Failed to get external storage public directory");
            }
            if (file.exists()) {
                if (!file.isDirectory()) {
                    throw new IllegalStateException(file.getAbsolutePath() + " already exists and is not a directory");
                }
            } else if (!file.mkdirs()) {
                throw new IllegalStateException("Unable to create directory: " + file.getAbsolutePath());
            }
            setDestinationFromBase(file, subPath);
            return this;
        }

        private void setDestinationFromBase(File base, String subPath) {
            if (subPath == null) {
                throw new NullPointerException("subPath cannot be null");
            }
            this.mDestinationUri = Uri.withAppendedPath(Uri.fromFile(base), subPath);
            Log.v(DownloadManager.XLOGTAG, "setDestinationFromBase: mDestinationUri " + this.mDestinationUri);
        }

        public void allowScanningByMediaScanner() {
            this.mScannable = true;
        }

        public Request addRequestHeader(String header, String value) {
            if (header == null) {
                throw new NullPointerException("header cannot be null");
            } else if (header.contains(":")) {
                throw new IllegalArgumentException("header may not contain ':'");
            } else {
                Object value2;
                if (value2 == null) {
                    value2 = "";
                }
                this.mRequestHeaders.add(Pair.create(header, value2));
                return this;
            }
        }

        public Request setTitle(CharSequence title) {
            this.mTitle = title;
            return this;
        }

        public Request setDescription(CharSequence description) {
            this.mDescription = description;
            return this;
        }

        public Request setMimeType(String mimeType) {
            this.mMimeType = mimeType;
            return this;
        }

        @Deprecated
        public Request setShowRunningNotification(boolean show) {
            if (show) {
                return setNotificationVisibility(0);
            }
            return setNotificationVisibility(2);
        }

        public Request setNotificationVisibility(int visibility) {
            this.mNotificationVisibility = visibility;
            return this;
        }

        public Request setAllowedNetworkTypes(int flags) {
            this.mAllowedNetworkTypes = flags;
            return this;
        }

        public Request setAllowedOverRoaming(boolean allowed) {
            this.mRoamingAllowed = allowed;
            return this;
        }

        public Request setAllowedOverMetered(boolean allow) {
            this.mMeteredAllowed = allow;
            return this;
        }

        public Request setRequiresCharging(boolean requiresCharging) {
            if (requiresCharging) {
                this.mFlags |= 1;
            } else {
                this.mFlags &= -2;
            }
            return this;
        }

        public Request setRequiresDeviceIdle(boolean requiresDeviceIdle) {
            if (requiresDeviceIdle) {
                this.mFlags |= 2;
            } else {
                this.mFlags &= -3;
            }
            return this;
        }

        public Request setVisibleInDownloadsUi(boolean isVisible) {
            this.mIsVisibleInDownloadsUi = isVisible;
            return this;
        }

        public Request setUserAgent(String userAgent) {
            this.mUserAgent = userAgent;
            Log.v(DownloadManager.XLOGTAG, "setUserAgent: userAgent is: " + userAgent);
            return this;
        }

        ContentValues toContentValues(String packageName) {
            int i = 2;
            ContentValues values = new ContentValues();
            if (!f5-assertionsDisabled) {
                if (!(this.mUri != null)) {
                    throw new AssertionError();
                }
            }
            values.put("uri", this.mUri.toString());
            values.put(Impl.COLUMN_IS_PUBLIC_API, Boolean.valueOf(true));
            values.put(Impl.COLUMN_NOTIFICATION_PACKAGE, packageName);
            if (this.mDestinationUri != null) {
                values.put(Impl.COLUMN_DESTINATION, Integer.valueOf(4));
                values.put(Impl.COLUMN_FILE_NAME_HINT, this.mDestinationUri.toString());
            } else {
                int i2;
                String str = Impl.COLUMN_DESTINATION;
                if (this.mUseSystemCache) {
                    i2 = 5;
                } else {
                    i2 = 2;
                }
                values.put(str, Integer.valueOf(i2));
            }
            String str2 = "scanned";
            if (this.mScannable) {
                i = 0;
            }
            values.put(str2, Integer.valueOf(i));
            if (!this.mRequestHeaders.isEmpty()) {
                encodeHttpHeaders(values);
            }
            putIfNonNull(values, "title", this.mTitle);
            putIfNonNull(values, "description", this.mDescription);
            putIfNonNull(values, "mimetype", this.mMimeType);
            values.put(Impl.COLUMN_VISIBILITY, Integer.valueOf(this.mNotificationVisibility));
            values.put(Impl.COLUMN_ALLOWED_NETWORK_TYPES, Integer.valueOf(this.mAllowedNetworkTypes));
            values.put(Impl.COLUMN_ALLOW_ROAMING, Boolean.valueOf(this.mRoamingAllowed));
            values.put("allow_metered", Boolean.valueOf(this.mMeteredAllowed));
            values.put("flags", Integer.valueOf(this.mFlags));
            values.put(Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, Boolean.valueOf(this.mIsVisibleInDownloadsUi));
            if (this.mUserAgent != null) {
                values.put(Impl.COLUMN_USER_AGENT, this.mUserAgent);
            }
            return values;
        }

        private void encodeHttpHeaders(ContentValues values) {
            int index = 0;
            for (Pair<String, String> header : this.mRequestHeaders) {
                values.put(RequestHeaders.INSERT_KEY_PREFIX + index, ((String) header.first) + ": " + ((String) header.second));
                index++;
            }
        }

        private void putIfNonNull(ContentValues contentValues, String key, Object value) {
            if (value != null) {
                contentValues.put(key, value.toString());
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.DownloadManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.DownloadManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.DownloadManager.<clinit>():void");
    }

    public DownloadManager(Context context) {
        boolean z;
        this.mBaseUri = Impl.CONTENT_URI;
        this.mResolver = context.getContentResolver();
        this.mPackageName = context.getPackageName();
        if (context.getApplicationInfo().targetSdkVersion < 24) {
            z = true;
        } else {
            z = false;
        }
        this.mAccessFilename = z;
    }

    public void setAccessAllDownloads(boolean accessAllDownloads) {
        if (accessAllDownloads) {
            this.mBaseUri = Impl.ALL_DOWNLOADS_CONTENT_URI;
        } else {
            this.mBaseUri = Impl.CONTENT_URI;
        }
    }

    public void setAccessFilename(boolean accessFilename) {
        this.mAccessFilename = accessFilename;
    }

    public long enqueue(Request request) {
        Uri downloadUri = this.mResolver.insert(Impl.CONTENT_URI, request.toContentValues(this.mPackageName));
        if (downloadUri != null) {
            return Long.parseLong(downloadUri.getLastPathSegment());
        }
        return -1;
    }

    public int markRowDeleted(long... ids) {
        if (ids != null && ids.length != 0) {
            return this.mResolver.delete(this.mBaseUri, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
        }
        throw new IllegalArgumentException("input param 'ids' can't be null");
    }

    public int remove(long... ids) {
        return markRowDeleted(ids);
    }

    public Cursor query(Query query) {
        Cursor underlyingCursor = query.runQuery(this.mResolver, UNDERLYING_COLUMNS, this.mBaseUri);
        if (underlyingCursor == null) {
            return null;
        }
        return new CursorTranslator(underlyingCursor, this.mBaseUri, this.mAccessFilename);
    }

    public ParcelFileDescriptor openDownloadedFile(long id) throws FileNotFoundException {
        return this.mResolver.openFileDescriptor(getDownloadUri(id), FullBackup.ROOT_TREE_TOKEN);
    }

    public Uri getUriForDownloadedFile(long id) {
        Query query = new Query();
        long[] jArr = new long[1];
        jArr[0] = id;
        Cursor cursor = null;
        try {
            cursor = query(query.setFilterById(jArr));
            if (cursor == null) {
                return null;
            }
            Uri withAppendedId;
            if (!cursor.moveToFirst() || 8 != cursor.getInt(cursor.getColumnIndexOrThrow("status"))) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            } else if (this.mAccessFilename) {
                int destination = cursor.getInt(cursor.getColumnIndexOrThrow(Impl.COLUMN_DESTINATION));
                if (destination == 1 || destination == 5 || destination == 3 || destination == 2) {
                    withAppendedId = ContentUris.withAppendedId(Impl.ALL_DOWNLOADS_CONTENT_URI, id);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return withAppendedId;
                }
                withAppendedId = Uri.fromFile(new File(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCAL_FILENAME))));
                if (cursor != null) {
                    cursor.close();
                }
                return withAppendedId;
            } else {
                withAppendedId = ContentUris.withAppendedId(Impl.ALL_DOWNLOADS_CONTENT_URI, id);
                if (cursor != null) {
                    cursor.close();
                }
                return withAppendedId;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String getMimeTypeForDownloadedFile(long id) {
        Query query = new Query();
        long[] jArr = new long[1];
        jArr[0] = id;
        Cursor cursor = null;
        try {
            cursor = query(query.setFilterById(jArr));
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                String string = cursor.getString(cursor.getColumnIndexOrThrow("media_type"));
                if (cursor != null) {
                    cursor.close();
                }
                return string;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void restartDownload(long... ids) {
        Cursor cursor = query(new Query().setFilterById(ids));
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int status = cursor.getInt(cursor.getColumnIndex("status"));
                if (status == 8 || status == 16) {
                    cursor.moveToNext();
                } else {
                    throw new IllegalArgumentException("Cannot restart incomplete download: " + cursor.getLong(cursor.getColumnIndex("_id")));
                }
            }
            ContentValues values = new ContentValues();
            values.put(Impl.COLUMN_CURRENT_BYTES, Integer.valueOf(0));
            values.put(Impl.COLUMN_TOTAL_BYTES, Integer.valueOf(-1));
            values.putNull("_data");
            values.put("status", Integer.valueOf(190));
            values.put(Impl.COLUMN_FAILED_CONNECTIONS, Integer.valueOf(0));
            values.put("scanned", Integer.valueOf(0));
            this.mResolver.update(this.mBaseUri, values, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
        } finally {
            cursor.close();
        }
    }

    public void forceDownload(long... ids) {
        ContentValues values = new ContentValues();
        values.put("status", Integer.valueOf(190));
        values.put(Impl.COLUMN_CONTROL, Integer.valueOf(0));
        values.put(Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT, Integer.valueOf(1));
        this.mResolver.update(this.mBaseUri, values, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
    }

    public static Long getMaxBytesOverMobile(Context context) {
        try {
            return Long.valueOf(Global.getLong(context.getContentResolver(), Global.DOWNLOAD_MAX_BYTES_OVER_MOBILE));
        } catch (SettingNotFoundException e) {
            return null;
        }
    }

    public boolean rename(Context context, long id, String displayName) {
        if (FileUtils.isValidFatFilename(displayName)) {
            Query query = new Query();
            long[] jArr = new long[1];
            jArr[0] = id;
            Cursor cursor = null;
            String oldDisplayName = null;
            String mimeType = null;
            String filePath = null;
            String oldMediaProviderUri = null;
            try {
                cursor = query(query.setFilterById(jArr));
                if (cursor == null) {
                    return false;
                }
                if (cursor.moveToFirst()) {
                    if (8 != cursor.getInt(cursor.getColumnIndexOrThrow("status"))) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return false;
                    }
                    oldDisplayName = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    oldMediaProviderUri = cursor.getString(cursor.getColumnIndexOrThrow("mediaprovider_uri"));
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow("media_type"));
                    filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCAL_FILENAME));
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (oldDisplayName == null || mimeType == null || filePath == null) {
                    throw new IllegalStateException("Document with id " + id + " does not exist");
                }
                int index = filePath.lastIndexOf(File.separator);
                if (index > 0) {
                    filePath = filePath.substring(0, index);
                }
                File file = new File(filePath);
                File before = new File(file, oldDisplayName);
                File after = new File(file, displayName);
                if (after.exists()) {
                    throw new IllegalStateException("Already exists " + after);
                }
                ContentValues values = new ContentValues();
                values.put("title", displayName);
                values.put("_data", after.toString());
                values.putNull("mediaprovider_uri");
                long[] ids = new long[1];
                ids[0] = id;
                int updateRet = this.mResolver.update(this.mBaseUri, values, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
                Log.v(XLOGTAG, "rename update DownloadDB ret = " + updateRet);
                if (before.renameTo(after)) {
                    String[] strArr = new String[1];
                    strArr[0] = before.getAbsolutePath();
                    context.getContentResolver().delete(Files.getContentUri("external"), "_data=?", strArr);
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(after));
                    context.sendBroadcast(intent);
                    return updateRet == 1;
                }
                ContentValues newValues = new ContentValues();
                newValues.put("title", oldDisplayName);
                newValues.put("_data", before.toString());
                newValues.put("mediaprovider_uri", oldMediaProviderUri);
                Log.v(XLOGTAG, "rename revert update DownloadDB ret = " + this.mResolver.update(this.mBaseUri, newValues, getWhereClauseForIds(ids), getWhereArgsForIds(ids)));
                throw new IllegalStateException("Failed to rename to " + after);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            throw new SecurityException(displayName + " is not a valid filename");
        }
    }

    public static Long getRecommendedMaxBytesOverMobile(Context context) {
        try {
            return Long.valueOf(Global.getLong(context.getContentResolver(), Global.DOWNLOAD_RECOMMENDED_MAX_BYTES_OVER_MOBILE));
        } catch (SettingNotFoundException e) {
            return null;
        }
    }

    public static boolean isActiveNetworkExpensive(Context context) {
        return false;
    }

    public static long getActiveNetworkWarningBytes(Context context) {
        return -1;
    }

    public long addCompletedDownload(String title, String description, boolean isMediaScannerScannable, String mimeType, String path, long length, boolean showNotification) {
        return addCompletedDownload(title, description, isMediaScannerScannable, mimeType, path, length, showNotification, false, null, null);
    }

    public long addCompletedDownload(String title, String description, boolean isMediaScannerScannable, String mimeType, String path, long length, boolean showNotification, Uri uri, Uri referer) {
        return addCompletedDownload(title, description, isMediaScannerScannable, mimeType, path, length, showNotification, false, uri, referer);
    }

    public long addCompletedDownload(String title, String description, boolean isMediaScannerScannable, String mimeType, String path, long length, boolean showNotification, boolean allowWrite) {
        return addCompletedDownload(title, description, isMediaScannerScannable, mimeType, path, length, showNotification, allowWrite, null, null);
    }

    public long addCompletedDownload(String title, String description, boolean isMediaScannerScannable, String mimeType, String path, long length, boolean showNotification, boolean allowWrite, Uri uri, Uri referer) {
        validateArgumentIsNonEmpty("title", title);
        validateArgumentIsNonEmpty("description", description);
        validateArgumentIsNonEmpty("path", path);
        validateArgumentIsNonEmpty("mimeType", mimeType);
        if (length < 0) {
            throw new IllegalArgumentException(" invalid value for param: totalBytes");
        }
        Request request;
        int i;
        if (uri != null) {
            request = new Request(uri);
        } else {
            request = new Request(NON_DOWNLOADMANAGER_DOWNLOAD);
        }
        request.setTitle(title).setDescription(description).setMimeType(mimeType);
        if (referer != null) {
            request.addRequestHeader("Referer", referer.toString());
        }
        ContentValues values = request.toContentValues(null);
        values.put(Impl.COLUMN_DESTINATION, Integer.valueOf(6));
        values.put("_data", path);
        values.put("status", Integer.valueOf(200));
        values.put(Impl.COLUMN_TOTAL_BYTES, Long.valueOf(length));
        String str = "scanned";
        if (isMediaScannerScannable) {
            i = 0;
        } else {
            i = 2;
        }
        values.put(str, Integer.valueOf(i));
        values.put(Impl.COLUMN_VISIBILITY, Integer.valueOf(showNotification ? 3 : 2));
        values.put("allow_write", Integer.valueOf(allowWrite ? 1 : 0));
        Uri downloadUri = this.mResolver.insert(Impl.CONTENT_URI, values);
        if (downloadUri == null) {
            return -1;
        }
        return Long.parseLong(downloadUri.getLastPathSegment());
    }

    private static void validateArgumentIsNonEmpty(String paramName, String val) {
        if (TextUtils.isEmpty(val)) {
            throw new IllegalArgumentException(paramName + " can't be null");
        }
    }

    public Uri getDownloadUri(long id) {
        return ContentUris.withAppendedId(Impl.ALL_DOWNLOADS_CONTENT_URI, id);
    }

    static String getWhereClauseForIds(long[] ids) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                whereClause.append("OR ");
            }
            whereClause.append("_id");
            whereClause.append(" = ? ");
        }
        whereClause.append(")");
        return whereClause.toString();
    }

    static String[] getWhereArgsForIds(long[] ids) {
        String[] whereArgs = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            whereArgs[i] = Long.toString(ids[i]);
        }
        return whereArgs;
    }
}
