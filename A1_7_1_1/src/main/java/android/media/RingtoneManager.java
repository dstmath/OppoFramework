package android.media;

import android.Manifest.permission;
import android.app.Activity;
import android.app.backup.FullBackup;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
import android.os.SystemProperties;
import android.provider.DrmStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.Media;
import android.provider.Settings.System;
import android.util.Log;
import com.android.internal.database.SortCursor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import libcore.io.Streams;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
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
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public class RingtoneManager {
    public static final String ACTION_RINGTONE_PICKER = "android.intent.action.RINGTONE_PICKER";
    private static final String[] DRM_COLUMNS = null;
    private static final int DRM_LEVEL_ALL = 4;
    private static final int DRM_LEVEL_FL = 1;
    private static final int DRM_LEVEL_SD = 2;
    private static final String EXTRA_DRM_LEVEL = "android.intent.extra.drm_level";
    public static final String EXTRA_RINGTONE_AUDIO_ATTRIBUTES_FLAGS = "android.intent.extra.ringtone.AUDIO_ATTRIBUTES_FLAGS";
    public static final String EXTRA_RINGTONE_DEFAULT_URI = "android.intent.extra.ringtone.DEFAULT_URI";
    public static final String EXTRA_RINGTONE_EXISTING_URI = "android.intent.extra.ringtone.EXISTING_URI";
    @Deprecated
    public static final String EXTRA_RINGTONE_INCLUDE_DRM = "android.intent.extra.ringtone.INCLUDE_DRM";
    public static final String EXTRA_RINGTONE_PICKED_POSITION = "android.intent.extra.ringtone.PICKED_POSITION";
    public static final String EXTRA_RINGTONE_PICKED_URI = "android.intent.extra.ringtone.PICKED_URI";
    public static final String EXTRA_RINGTONE_SHOW_DEFAULT = "android.intent.extra.ringtone.SHOW_DEFAULT";
    public static final String EXTRA_RINGTONE_SHOW_MORE_RINGTONES = "android.intent.extra.ringtone.SHOW_MORE_RINGTONES";
    public static final String EXTRA_RINGTONE_SHOW_SILENT = "android.intent.extra.ringtone.SHOW_SILENT";
    public static final String EXTRA_RINGTONE_TITLE = "android.intent.extra.ringtone.TITLE";
    public static final String EXTRA_RINGTONE_TYPE = "android.intent.extra.ringtone.TYPE";
    public static final int ID_COLUMN_INDEX = 0;
    private static final String[] INTERNAL_COLUMNS = null;
    private static final String[] MEDIA_COLUMNS = null;
    private static final String TAG = "RingtoneManager";
    public static final int TITLE_COLUMN_INDEX = 1;
    public static final int TYPE_ALARM = 4;
    public static final int TYPE_ALL = 7;
    public static final int TYPE_NOTIFICATION = 2;
    public static final int TYPE_RINGTONE = 1;
    public static final int TYPE_SIP_CALL = 16;
    public static final int TYPE_VIDEO_CALL = 8;
    public static final int URI_COLUMN_INDEX = 2;
    private static final boolean mIsCnVersion = false;
    private final Activity mActivity;
    private final Context mContext;
    private Cursor mCursor;
    private final List<String> mFilterColumns;
    private boolean mIncludeDrm;
    private Ringtone mPreviousRingtone;
    private boolean mStopPreviousRingtone;
    private int mType;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.RingtoneManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.RingtoneManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.RingtoneManager.<clinit>():void");
    }

    public RingtoneManager(Activity activity) {
        this.mType = 1;
        this.mFilterColumns = new ArrayList();
        this.mStopPreviousRingtone = true;
        this.mActivity = activity;
        this.mContext = activity;
        setType(this.mType);
    }

    public RingtoneManager(Context context) {
        this.mType = 1;
        this.mFilterColumns = new ArrayList();
        this.mStopPreviousRingtone = true;
        this.mActivity = null;
        this.mContext = context;
        setType(this.mType);
    }

    public void setType(int type) {
        if (this.mCursor != null) {
            throw new IllegalStateException("Setting filter columns should be done before querying for ringtones.");
        }
        this.mType = type;
        setFilterColumnsList(type);
    }

    public int inferStreamType() {
        switch (this.mType) {
            case 2:
                return 5;
            case 4:
                return 4;
            default:
                return 2;
        }
    }

    public void setStopPreviousRingtone(boolean stopPreviousRingtone) {
        this.mStopPreviousRingtone = stopPreviousRingtone;
    }

    public boolean getStopPreviousRingtone() {
        return this.mStopPreviousRingtone;
    }

    public void stopPreviousRingtone() {
        if (this.mPreviousRingtone != null) {
            this.mPreviousRingtone.stop();
        }
    }

    @Deprecated
    public boolean getIncludeDrm() {
        return false;
    }

    @Deprecated
    public void setIncludeDrm(boolean includeDrm) {
        if (includeDrm) {
            Log.w(TAG, "setIncludeDrm no longer supported");
        }
    }

    public Cursor getCursor() {
        if (this.mCursor == null || !this.mCursor.requery()) {
            Cursor internalCursor = getInternalRingtones();
            Cursor drmCursor = this.mIncludeDrm ? getDrmRingtones() : null;
            Cursor mediaCursor = getMediaRingtones();
            Cursor[] cursorArr = new Cursor[3];
            cursorArr[0] = internalCursor;
            cursorArr[1] = drmCursor;
            cursorArr[2] = mediaCursor;
            this.mCursor = new SortCursor(cursorArr, "title_key");
            Log.v(TAG, "mCursor.hashCode " + this.mCursor.hashCode());
            Log.v(TAG, "getCursor with new cursor = " + this.mCursor);
            return this.mCursor;
        }
        Log.v(TAG, "getCursor with old cursor = " + this.mCursor);
        return this.mCursor;
    }

    public Ringtone getRingtone(int position) {
        if (this.mStopPreviousRingtone && this.mPreviousRingtone != null) {
            this.mPreviousRingtone.stop();
        }
        this.mPreviousRingtone = getRingtone(this.mContext, getRingtoneUri(position), inferStreamType());
        return this.mPreviousRingtone;
    }

    public Uri getRingtoneUri(int position) {
        if (this.mCursor == null) {
            Log.v(TAG, "mCursor is null");
            return null;
        }
        try {
            if (!this.mCursor.isClosed() && this.mCursor.moveToPosition(position)) {
                return getUriFromCursor(this.mCursor);
            }
            Log.v(TAG, "mCursor position is wrong");
            return null;
        } catch (IllegalStateException e) {
            Log.v(TAG, "mCursor exception");
            return null;
        }
    }

    private static Uri getUriFromCursor(Cursor cursor) {
        return ContentUris.withAppendedId(Uri.parse(cursor.getString(2)), cursor.getLong(0));
    }

    public int getRingtonePosition(Uri ringtoneUri) {
        if (ringtoneUri == null) {
            return -1;
        }
        Cursor cursor = getCursor();
        int cursorCount = cursor.getCount();
        return queryPosition(cursor, ringtoneUri);
    }

    private int validRingtoneForMTCall(Uri ringtoneUri) {
        if (ringtoneUri == null) {
            return -1;
        }
        Cursor cursor = getCursor();
        try {
            int queryPosition = queryPosition(cursor, ringtoneUri);
            return queryPosition;
        } finally {
            if (!(cursor == null || cursor.isClosed())) {
                cursor.close();
                Log.d(TAG, "Cursor already closed.");
            }
        }
    }

    private int queryPosition(Cursor cursor, Uri ringtoneUri) {
        int cursorCount = cursor.getCount();
        if (!cursor.moveToFirst()) {
            return -1;
        }
        Uri currentUri = null;
        Object previousUriString = null;
        for (int i = 0; i < cursorCount; i++) {
            String uriString = cursor.getString(2);
            if (currentUri == null || !uriString.equals(previousUriString)) {
                currentUri = Uri.parse(uriString);
            }
            if (ringtoneUri.equals(ContentUris.withAppendedId(currentUri, cursor.getLong(0)))) {
                return i;
            }
            cursor.move(1);
            String previousUriString2 = uriString;
        }
        return -1;
    }

    public static Uri getValidRingtoneUri(Context context) {
        RingtoneManager rm = new RingtoneManager(context);
        Uri uri = getValidRingtoneUriFromCursorAndClose(context, rm.getInternalRingtones());
        if (uri == null) {
            uri = getValidRingtoneUriFromCursorAndClose(context, rm.getMediaRingtones());
        }
        if (uri == null) {
            return getValidRingtoneUriFromCursorAndClose(context, rm.getDrmRingtones());
        }
        return uri;
    }

    private static Uri getValidRingtoneUriFromCursorAndClose(Context context, Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        Uri uri = null;
        if (cursor.moveToFirst()) {
            uri = getUriFromCursor(cursor);
        }
        cursor.close();
        return uri;
    }

    private Cursor getInternalRingtones() {
        if (mIsCnVersion) {
            return query(Media.INTERNAL_CONTENT_URI, INTERNAL_COLUMNS, constructBooleanTrueWhereClause(this.mFilterColumns, this.mIncludeDrm) + appendDrmToWhereClause(), null, "title_key");
        }
        return query(Media.INTERNAL_CONTENT_URI, INTERNAL_COLUMNS, constructBooleanTrueWhereClause(this.mFilterColumns, this.mIncludeDrm) + appendOperatorToWhereClause(), null, "title_key");
    }

    private String appendOperatorToWhereClause() {
        StringBuilder sb = new StringBuilder();
        if (SystemProperties.get("ro.oppo.operator", "").equals("TELSTRA")) {
            sb.append("");
        } else {
            sb.append(" and (");
            sb.append("_data not like '%telstra%'");
            sb.append(")");
        }
        return sb.toString();
    }

    private Cursor getDrmRingtones() {
        return query(Audio.CONTENT_URI, DRM_COLUMNS, null, null, "title");
    }

    private Cursor getMediaRingtones() {
        Cursor cursor = null;
        if (this.mContext.checkPermission(permission.READ_EXTERNAL_STORAGE, Process.myPid(), Process.myUid()) != 0) {
            Log.w(TAG, "No READ_EXTERNAL_STORAGE permission, ignoring ringtones on ext storage");
            return null;
        }
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED) || status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            cursor = query(Media.EXTERNAL_CONTENT_URI, MEDIA_COLUMNS, constructBooleanTrueWhereClause(this.mFilterColumns, this.mIncludeDrm) + appendDrmToWhereClause(), null, "title_key");
        }
        return cursor;
    }

    private void setFilterColumnsList(int type) {
        List<String> columns = this.mFilterColumns;
        columns.clear();
        if ((type & 1) != 0) {
            columns.add(AudioColumns.IS_RINGTONE);
        }
        if ((type & 2) != 0) {
            columns.add(AudioColumns.IS_NOTIFICATION);
        }
        if ((type & 4) != 0) {
            columns.add(AudioColumns.IS_ALARM);
        }
    }

    private static String constructBooleanTrueWhereClause(List<String> columns, boolean includeDrm) {
        if (columns == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = columns.size() - 1; i >= 0; i--) {
            sb.append((String) columns.get(i)).append("=1 or ");
        }
        if (columns.size() > 0) {
            sb.setLength(sb.length() - 4);
        }
        sb.append(")");
        return sb.toString();
    }

    private String appendDrmToWhereClause() {
        return "";
    }

    private Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return this.mContext.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public static Ringtone getRingtone(Context context, Uri ringtoneUri) {
        return getRingtone(context, ringtoneUri, -1);
    }

    private static Ringtone getRingtone(Context context, Uri ringtoneUri, int streamType) {
        Log.d(TAG, "getRingtone() ringtoneUri = " + ringtoneUri + ", streamType = " + streamType);
        try {
            Ringtone r = new Ringtone(context, true);
            if (streamType >= 0) {
                r.setStreamType(streamType);
            }
            r.setUri(ringtoneUri);
            return r;
        } catch (Exception ex) {
            Log.e(TAG, "Failed to open ringtone " + ringtoneUri + ": " + ex);
            return null;
        }
    }

    private static int validRingtoneUri(Context context, Uri ringtoneUri, int type) {
        RingtoneManager rm = new RingtoneManager(context);
        rm.setType(type);
        return rm.validRingtoneForMTCall(ringtoneUri);
    }

    public static Uri getActualDefaultRingtoneUri(Context context, int type) {
        Uri uri = null;
        String setting = getSettingForType(type);
        if (setting == null) {
            return null;
        }
        String uriString = System.getStringForUser(context.getContentResolver(), setting, context.getUserId());
        Log.i(TAG, "Get actual default ringtone uri= " + uriString);
        if (uriString != null) {
            uri = Uri.parse(uriString);
        }
        return uri;
    }

    public static void setActualDefaultRingtoneUri(Context context, int type, Uri ringtoneUri) {
        InputStream inputStream;
        OutputStream outputStream;
        Throwable th;
        Throwable th2;
        Throwable th3 = null;
        ContentResolver resolver = context.getContentResolver();
        String setting = getSettingForType(type);
        if (setting != null) {
            String uri;
            if (ringtoneUri != null) {
                uri = ringtoneUri.toString();
            } else {
                uri = null;
            }
            System.putStringForUser(resolver, setting, uri, context.getUserId());
            if (ringtoneUri != null) {
                Uri cacheUri = getCacheForType(type);
                inputStream = null;
                outputStream = null;
                try {
                    inputStream = openRingtone(context, ringtoneUri);
                    outputStream = resolver.openOutputStream(cacheUri);
                    Streams.copy(inputStream, outputStream);
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (Throwable th4) {
                            th3 = th4;
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable th5) {
                            th = th5;
                            if (th3 != null) {
                                if (th3 != th) {
                                    th3.addSuppressed(th);
                                    th = th3;
                                }
                            }
                        }
                    }
                    th = th3;
                    if (th != null) {
                        try {
                            throw th;
                        } catch (IOException e) {
                            Log.w(TAG, "Failed to cache ringtone: " + e);
                        }
                    }
                } catch (Throwable th32) {
                    Throwable th6 = th32;
                    th32 = th;
                    th = th6;
                }
            }
            Log.i(TAG, "Set actual default ringtone uri= " + ringtoneUri);
            return;
        }
        return;
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Throwable th7) {
                th2 = th7;
                if (th32 != null) {
                    if (th32 != th2) {
                        th32.addSuppressed(th2);
                        th2 = th32;
                    }
                }
            }
        }
        th2 = th32;
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Throwable th8) {
                th32 = th8;
                if (th2 != null) {
                    if (th2 != th32) {
                        th2.addSuppressed(th32);
                        th32 = th2;
                    }
                }
            }
        }
        th32 = th2;
        if (th32 != null) {
            throw th32;
        }
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0009 A:{ExcHandler: java.lang.SecurityException (r0_0 'e' java.lang.Exception), Splitter: B:1:0x0004} */
    /* JADX WARNING: Missing block: B:4:0x0009, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x000a, code:
            android.util.Log.w(TAG, "Failed to open directly; attempting failover: " + r0);
     */
    /* JADX WARNING: Missing block: B:8:0x0039, code:
            return new android.os.ParcelFileDescriptor.AutoCloseInputStream(((android.media.AudioManager) r7.getSystemService(android.media.AudioManager.class)).getRingtonePlayer().openRingtone(r8));
     */
    /* JADX WARNING: Missing block: B:9:0x003a, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x0040, code:
            throw new java.io.IOException(r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static InputStream openRingtone(Context context, Uri uri) throws IOException {
        try {
            return context.getContentResolver().openInputStream(uri);
        } catch (Exception e) {
        }
    }

    private static String getSettingForType(int type) {
        if ((type & 1) != 0) {
            return System.RINGTONE;
        }
        if ((type & 2) != 0) {
            return System.NOTIFICATION_SOUND;
        }
        if ((type & 4) != 0) {
            return System.ALARM_ALERT;
        }
        return null;
    }

    public static Uri getCacheForType(int type) {
        if ((type & 1) != 0) {
            return System.RINGTONE_CACHE_URI;
        }
        if ((type & 2) != 0) {
            return System.NOTIFICATION_SOUND_CACHE_URI;
        }
        if ((type & 4) != 0) {
            return System.ALARM_ALERT_CACHE_URI;
        }
        return null;
    }

    public static boolean isDefault(Uri ringtoneUri) {
        return getDefaultType(ringtoneUri) != -1;
    }

    public static int getDefaultType(Uri defaultRingtoneUri) {
        if (defaultRingtoneUri == null) {
            return -1;
        }
        if (defaultRingtoneUri.equals(System.DEFAULT_RINGTONE_URI)) {
            return 1;
        }
        if (defaultRingtoneUri.equals(System.DEFAULT_NOTIFICATION_URI)) {
            return 2;
        }
        if (defaultRingtoneUri.equals(System.DEFAULT_ALARM_ALERT_URI)) {
            return 4;
        }
        return -1;
    }

    public static Uri getDefaultUri(int type) {
        if ((type & 1) != 0) {
            return System.DEFAULT_RINGTONE_URI;
        }
        if ((type & 2) != 0) {
            return System.DEFAULT_NOTIFICATION_URI;
        }
        if ((type & 4) != 0) {
            return System.DEFAULT_ALARM_ALERT_URI;
        }
        return null;
    }

    public Cursor getNewCursor() {
        Cursor internalCursor = getInternalRingtones();
        Cursor drmCursor = this.mIncludeDrm ? getDrmRingtones() : null;
        Cursor mediaCursor = getMediaRingtones();
        Cursor[] cursorArr = new Cursor[3];
        cursorArr[0] = internalCursor;
        cursorArr[1] = drmCursor;
        cursorArr[2] = mediaCursor;
        this.mCursor = new SortCursor(cursorArr, "title_key");
        Log.v(TAG, "getNewCursor mCursor.hashCode " + this.mCursor.hashCode());
        Log.v(TAG, "getNewCursor with cursor = " + this.mCursor);
        return this.mCursor;
    }

    public static boolean isRingtoneExist(Context context, Uri uri) {
        if (uri == null) {
            Log.e(TAG, "Check ringtone exist with null uri!");
            return false;
        }
        boolean exist;
        try {
            AssetFileDescriptor fd = context.getContentResolver().openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
            if (fd == null) {
                exist = false;
            } else {
                fd.close();
                exist = true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            exist = false;
        } catch (IOException e2) {
            e2.printStackTrace();
            exist = true;
        }
        Log.d(TAG, uri + " is exist " + exist);
        return exist;
    }
}
