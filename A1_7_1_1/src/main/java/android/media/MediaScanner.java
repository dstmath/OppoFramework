package android.media;

import android.app.ActivityManager;
import android.app.backup.FullBackup;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.drm.DrmManagerClient;
import android.graphics.BitmapFactory.Options;
import android.media.MediaFile.MediaFileType;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.net.Uri.Builder;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Thumbnails;
import android.provider.MediaStore.Video;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.sax.ElementListener;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.util.Xml;
import dalvik.system.CloseGuard;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class MediaScanner implements AutoCloseable {
    private static final String ALARMS_DIR = "/alarms/";
    private static final String ALARM_SET = "alarm_set";
    private static final int APP1 = 65505;
    private static final int APPXTAG_PLUS_LENGTHTAG_BYTE_COUNT = 4;
    private static final int DATE_MODIFIED_PLAYLISTS_COLUMN_INDEX = 2;
    private static final boolean DEBUG = false;
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private static final boolean ENABLE_BULK_INSERTS = true;
    private static final String EXTERNAL_PRIMARY_STORAGE_PATH_L = "/storage/sdcard0/";
    private static final String EXTERNAL_SECONDARY_STORAGE_PATH_L = "/storage/sdcard1/";
    private static final int FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX = 3;
    private static final int FILES_PRESCAN_FORMAT_COLUMN_INDEX = 2;
    private static final int FILES_PRESCAN_ID_COLUMN_INDEX = 0;
    private static final int FILES_PRESCAN_PATH_COLUMN_INDEX = 1;
    private static final String[] FILES_PRESCAN_PROJECTION = null;
    private static final String[] ID3_GENRES = null;
    private static final int ID_PLAYLISTS_COLUMN_INDEX = 0;
    private static final String[] ID_PROJECTION = null;
    public static final String LAST_INTERNAL_SCAN_FINGERPRINT = "lastScanFingerprint";
    private static final boolean LOGD = false;
    private static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final String MTK_REFOCUS_PREFIX = "MRefocus";
    private static final String MUSIC_DIR = "/music/";
    private static final String NOTIFICATIONS_DIR = "/notifications/";
    private static final String NOTIFICATION_SET = "notification_set";
    private static final String NS_GDEPTH = "http://ns.google.com/photos/1.0/depthmap/";
    private static final int PATH_PLAYLISTS_COLUMN_INDEX = 1;
    private static final String[] PLAYLIST_MEMBERS_PROJECTION = null;
    private static final String PODCAST_DIR = "/podcasts/";
    private static final String RINGTONES_DIR = "/ringtones/";
    private static final String RINGTONE_SET = "ringtone_set";
    public static final String SCANNED_BUILD_PREFS_NAME = "MediaScanBuild";
    private static final int SOI = 65496;
    private static final int SOS = 65498;
    private static final String SYSTEM_SOUNDS_DIR = "/system/media/audio";
    private static final String TAG = "MediaScanner";
    private static final String XMP_EXT_MAIN_HEADER1 = "http://ns.adobe.com/xmp/extension/";
    private static final String XMP_HEADER_START = "http://ns.adobe.com/xap/1.0/\u0000";
    private static HashMap<String, String> mMediaPaths;
    private static HashMap<String, String> mNoMediaPaths;
    private static String sLastInternalScanFingerprint;
    private final Uri mAudioUri;
    private final Options mBitmapOptions;
    private final MyMediaScannerClient mClient;
    private final CloseGuard mCloseGuard;
    private final AtomicBoolean mClosed;
    private final Context mContext;
    private String mDefaultAlarmAlertFilename;
    private boolean mDefaultAlarmSet;
    private String mDefaultNotificationFilename;
    private boolean mDefaultNotificationSet;
    private String mDefaultRingtoneFilename;
    private boolean mDefaultRingtoneSet;
    private DrmManagerClient mDrmManagerClient;
    private final boolean mExternalIsEmulated;
    private final String mExternalStoragePath;
    private final Uri mFilesUri;
    private final Uri mFilesUriNoNotify;
    private final Uri mImagesUri;
    private long mLimitBmpFileSize;
    private long mLimitGifFileSize;
    private MediaInserter mMediaInserter;
    private final ContentProviderClient mMediaProvider;
    private int mMtpObjectHandle;
    private long mNativeContext;
    private final String mPackageName;
    private final ArrayList<FileEntry> mPlayLists;
    private final ArrayList<PlaylistEntry> mPlaylistEntries;
    private ArrayList<String> mPlaylistFilePathList;
    private final Uri mPlaylistsUri;
    private final boolean mProcessGenres;
    private final boolean mProcessPlaylists;
    private final Uri mThumbsUri;
    private final Uri mVideoThumbsUri;
    private final Uri mVideoUri;
    private final String mVolumeName;
    private boolean mWasEmptyPriorToScan;

    private static class FileEntry {
        int mFormat;
        long mLastModified;
        boolean mLastModifiedChanged;
        String mPath;
        long mRowId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e7 in method: android.media.MediaScanner.FileEntry.<init>(long, java.lang.String, long, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e7
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        FileEntry(long r1, java.lang.String r3, long r4, int r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e7 in method: android.media.MediaScanner.FileEntry.<init>(long, java.lang.String, long, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.FileEntry.<init>(long, java.lang.String, long, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.FileEntry.toString():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.FileEntry.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.FileEntry.toString():java.lang.String");
        }
    }

    static class MediaBulkDeleter {
        final Uri mBaseUri;
        final ContentProviderClient mProvider;
        ArrayList<String> whereArgs;
        StringBuilder whereClause;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaScanner.MediaBulkDeleter.<init>(android.content.ContentProviderClient, android.net.Uri):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public MediaBulkDeleter(android.content.ContentProviderClient r1, android.net.Uri r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaScanner.MediaBulkDeleter.<init>(android.content.ContentProviderClient, android.net.Uri):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MediaBulkDeleter.<init>(android.content.ContentProviderClient, android.net.Uri):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.media.MediaScanner.MediaBulkDeleter.delete(long):void, dex:  in method: android.media.MediaScanner.MediaBulkDeleter.delete(long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.media.MediaScanner.MediaBulkDeleter.delete(long):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void delete(long r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: null in method: android.media.MediaScanner.MediaBulkDeleter.delete(long):void, dex:  in method: android.media.MediaScanner.MediaBulkDeleter.delete(long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MediaBulkDeleter.delete(long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.media.MediaScanner.MediaBulkDeleter.flush():void, dex:  in method: android.media.MediaScanner.MediaBulkDeleter.flush():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.media.MediaScanner.MediaBulkDeleter.flush():void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void flush() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: null in method: android.media.MediaScanner.MediaBulkDeleter.flush():void, dex:  in method: android.media.MediaScanner.MediaBulkDeleter.flush():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MediaBulkDeleter.flush():void");
        }
    }

    private class MyMediaScannerClient implements MediaScannerClient {
        private String mAlbum;
        private String mAlbumArtist;
        private String mArtist;
        private int mCompilation;
        private String mComposer;
        private long mDate;
        private final SimpleDateFormat mDateFormatter;
        private String mDrmContentDescriptioin;
        private String mDrmContentName;
        private String mDrmContentUr;
        private String mDrmContentVendor;
        private long mDrmDataLen;
        private String mDrmIconUri;
        private long mDrmMethod;
        private long mDrmOffset;
        private String mDrmRightsIssuer;
        private int mDuration;
        private long mFileSize;
        private int mFileType;
        private String mGenre;
        private int mHeight;
        private boolean mIsDrm;
        private long mLastModified;
        private String mMimeType;
        private boolean mNoMedia;
        private int mOrientation;
        private String mPath;
        private String mSlowMotionSpeed;
        private String mTitle;
        private int mTrack;
        private int mWidth;
        private String mWriter;
        private int mYear;
        final /* synthetic */ MediaScanner this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaScanner.MyMediaScannerClient.<init>(android.media.MediaScanner):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public MyMediaScannerClient(android.media.MediaScanner r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaScanner.MyMediaScannerClient.<init>(android.media.MediaScanner):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.<init>(android.media.MediaScanner):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.MyMediaScannerClient.convertGenreCode(java.lang.String, java.lang.String):boolean, dex: 
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
        private boolean convertGenreCode(java.lang.String r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.MyMediaScannerClient.convertGenreCode(java.lang.String, java.lang.String):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.convertGenreCode(java.lang.String, java.lang.String):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.MyMediaScannerClient.doesPathHaveFilename(java.lang.String, java.lang.String):boolean, dex: 
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
        private boolean doesPathHaveFilename(java.lang.String r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.MyMediaScannerClient.doesPathHaveFilename(java.lang.String, java.lang.String):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.doesPathHaveFilename(java.lang.String, java.lang.String):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.MyMediaScannerClient.doesSettingEmpty(java.lang.String):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private boolean doesSettingEmpty(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.MyMediaScannerClient.doesSettingEmpty(java.lang.String):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.doesSettingEmpty(java.lang.String):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.MyMediaScannerClient.endFile(android.media.MediaScanner$FileEntry, boolean, boolean, boolean, boolean, boolean):android.net.Uri, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private android.net.Uri endFile(android.media.MediaScanner.FileEntry r1, boolean r2, boolean r3, boolean r4, boolean r5, boolean r6) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.MyMediaScannerClient.endFile(android.media.MediaScanner$FileEntry, boolean, boolean, boolean, boolean, boolean):android.net.Uri, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.endFile(android.media.MediaScanner$FileEntry, boolean, boolean, boolean, boolean, boolean):android.net.Uri");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.MyMediaScannerClient.getFileTypeFromDrm(java.lang.String):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private int getFileTypeFromDrm(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.MyMediaScannerClient.getFileTypeFromDrm(java.lang.String):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.getFileTypeFromDrm(java.lang.String):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.MyMediaScannerClient.parseDate(java.lang.String):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private long parseDate(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.MyMediaScannerClient.parseDate(java.lang.String):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.parseDate(java.lang.String):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.MyMediaScannerClient.parseSubstring(java.lang.String, int, int):int, dex: 
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
        private int parseSubstring(java.lang.String r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.MyMediaScannerClient.parseSubstring(java.lang.String, int, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.parseSubstring(java.lang.String, int, int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.media.MediaScanner.MyMediaScannerClient.processImageFile(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void processImageFile(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.media.MediaScanner.MyMediaScannerClient.processImageFile(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.processImageFile(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.MyMediaScannerClient.setSettingFlag(java.lang.String):void, dex: 
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
        private void setSettingFlag(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.MyMediaScannerClient.setSettingFlag(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.setSettingFlag(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.MyMediaScannerClient.setSettingIfNotSet(java.lang.String, android.net.Uri, long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void setSettingIfNotSet(java.lang.String r1, android.net.Uri r2, long r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.MyMediaScannerClient.setSettingIfNotSet(java.lang.String, android.net.Uri, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.setSettingIfNotSet(java.lang.String, android.net.Uri, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.MediaScanner.MyMediaScannerClient.testGenreNameConverter():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void testGenreNameConverter() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.MediaScanner.MyMediaScannerClient.testGenreNameConverter():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.testGenreNameConverter():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: f in method: android.media.MediaScanner.MyMediaScannerClient.toValues():android.content.ContentValues, dex:  in method: android.media.MediaScanner.MyMediaScannerClient.toValues():android.content.ContentValues, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: f in method: android.media.MediaScanner.MyMediaScannerClient.toValues():android.content.ContentValues, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: f
            	at com.android.dx.io.instructions.InstructionCodec$32.decode(InstructionCodec.java:693)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private android.content.ContentValues toValues() {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: f in method: android.media.MediaScanner.MyMediaScannerClient.toValues():android.content.ContentValues, dex:  in method: android.media.MediaScanner.MyMediaScannerClient.toValues():android.content.ContentValues, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.toValues():android.content.ContentValues");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaScanner.MyMediaScannerClient.beginFile(java.lang.String, java.lang.String, long, long, boolean, boolean):android.media.MediaScanner$FileEntry, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public android.media.MediaScanner.FileEntry beginFile(java.lang.String r1, java.lang.String r2, long r3, long r5, boolean r7, boolean r8) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaScanner.MyMediaScannerClient.beginFile(java.lang.String, java.lang.String, long, long, boolean, boolean):android.media.MediaScanner$FileEntry, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.beginFile(java.lang.String, java.lang.String, long, long, boolean, boolean):android.media.MediaScanner$FileEntry");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.media.MediaScanner.MyMediaScannerClient.doScanFile(java.lang.String, java.lang.String, long, long, boolean, boolean, boolean):android.net.Uri, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public android.net.Uri doScanFile(java.lang.String r1, java.lang.String r2, long r3, long r5, boolean r7, boolean r8, boolean r9) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.media.MediaScanner.MyMediaScannerClient.doScanFile(java.lang.String, java.lang.String, long, long, boolean, boolean, boolean):android.net.Uri, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.doScanFile(java.lang.String, java.lang.String, long, long, boolean, boolean, boolean):android.net.Uri");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.MyMediaScannerClient.getGenreName(java.lang.String):java.lang.String, dex: 
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
        public java.lang.String getGenreName(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.MyMediaScannerClient.getGenreName(java.lang.String):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.getGenreName(java.lang.String):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.MyMediaScannerClient.handleStringTag(java.lang.String, java.lang.String):void, dex: 
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
        public void handleStringTag(java.lang.String r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.MyMediaScannerClient.handleStringTag(java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.handleStringTag(java.lang.String, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.media.MediaScanner.MyMediaScannerClient.scanFile(java.lang.String, long, long, boolean, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void scanFile(java.lang.String r1, long r2, long r4, boolean r6, boolean r7) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.media.MediaScanner.MyMediaScannerClient.scanFile(java.lang.String, long, long, boolean, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.scanFile(java.lang.String, long, long, boolean, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.MyMediaScannerClient.setMimeType(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void setMimeType(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.MyMediaScannerClient.setMimeType(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.setMimeType(java.lang.String):void");
        }
    }

    private static class PlaylistEntry {
        long bestmatchid;
        int bestmatchlevel;
        String path;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.MediaScanner.PlaylistEntry.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private PlaylistEntry() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.MediaScanner.PlaylistEntry.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.PlaylistEntry.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.MediaScanner.PlaylistEntry.<init>(android.media.MediaScanner$PlaylistEntry):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        /* synthetic */ PlaylistEntry(android.media.MediaScanner.PlaylistEntry r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.MediaScanner.PlaylistEntry.<init>(android.media.MediaScanner$PlaylistEntry):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.PlaylistEntry.<init>(android.media.MediaScanner$PlaylistEntry):void");
        }
    }

    private static class Section {
        public boolean mIsXmpMain;
        public int mLength;
        public int mMarker;
        public long mOffset;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.media.MediaScanner.Section.<init>(int, long, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public Section(int r1, long r2, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.media.MediaScanner.Section.<init>(int, long, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.Section.<init>(int, long, int):void");
        }
    }

    class WplHandler implements ElementListener {
        final ContentHandler handler;
        String playListDirectory;
        final /* synthetic */ MediaScanner this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaScanner.WplHandler.<init>(android.media.MediaScanner, java.lang.String, android.net.Uri, android.database.Cursor):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public WplHandler(android.media.MediaScanner r1, java.lang.String r2, android.net.Uri r3, android.database.Cursor r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaScanner.WplHandler.<init>(android.media.MediaScanner, java.lang.String, android.net.Uri, android.database.Cursor):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.WplHandler.<init>(android.media.MediaScanner, java.lang.String, android.net.Uri, android.database.Cursor):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.MediaScanner.WplHandler.end():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void end() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.MediaScanner.WplHandler.end():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.WplHandler.end():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.WplHandler.getContentHandler():org.xml.sax.ContentHandler, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        org.xml.sax.ContentHandler getContentHandler() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.WplHandler.getContentHandler():org.xml.sax.ContentHandler, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.WplHandler.getContentHandler():org.xml.sax.ContentHandler");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.WplHandler.start(org.xml.sax.Attributes):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void start(org.xml.sax.Attributes r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaScanner.WplHandler.start(org.xml.sax.Attributes):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.WplHandler.start(org.xml.sax.Attributes):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.MediaScanner.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.<clinit>():void");
    }

    private final native void native_finalize();

    private static final native void native_init();

    private final native void native_setup();

    private native void processDirectory(String str, MediaScannerClient mediaScannerClient);

    private native void processFile(String str, String str2, MediaScannerClient mediaScannerClient);

    private native void setLocale(String str);

    public native byte[] extractAlbumArt(FileDescriptor fileDescriptor);

    public MediaScanner(Context c, String volumeName) {
        this.mClosed = new AtomicBoolean();
        this.mCloseGuard = CloseGuard.get();
        this.mWasEmptyPriorToScan = false;
        this.mBitmapOptions = new Options();
        this.mPlaylistEntries = new ArrayList();
        this.mPlayLists = new ArrayList();
        this.mDrmManagerClient = null;
        this.mLimitBmpFileSize = Long.MAX_VALUE;
        this.mLimitGifFileSize = Long.MAX_VALUE;
        this.mClient = new MyMediaScannerClient(this);
        this.mPlaylistFilePathList = new ArrayList();
        native_setup();
        this.mContext = c;
        this.mPackageName = c.getPackageName();
        this.mVolumeName = volumeName;
        this.mBitmapOptions.inSampleSize = 1;
        this.mBitmapOptions.inJustDecodeBounds = true;
        setDefaultRingtoneFileNames();
        this.mMediaProvider = this.mContext.getContentResolver().acquireContentProviderClient(MediaStore.AUTHORITY);
        if (sLastInternalScanFingerprint == null) {
            sLastInternalScanFingerprint = this.mContext.getSharedPreferences(SCANNED_BUILD_PREFS_NAME, 0).getString(LAST_INTERNAL_SCAN_FINGERPRINT, new String());
        }
        this.mAudioUri = Media.getContentUri(volumeName);
        this.mVideoUri = Video.Media.getContentUri(volumeName);
        this.mImagesUri = Images.Media.getContentUri(volumeName);
        this.mThumbsUri = Thumbnails.getContentUri(volumeName);
        this.mVideoThumbsUri = Video.Thumbnails.getContentUri(volumeName);
        this.mFilesUri = Files.getContentUri(volumeName);
        this.mFilesUriNoNotify = this.mFilesUri.buildUpon().appendQueryParameter("nonotify", WifiEnterpriseConfig.ENGINE_ENABLE).build();
        if (volumeName.equals("internal")) {
            this.mProcessPlaylists = false;
            this.mProcessGenres = false;
            this.mPlaylistsUri = null;
        } else {
            this.mProcessPlaylists = true;
            this.mProcessGenres = true;
            this.mPlaylistsUri = Playlists.getContentUri(volumeName);
        }
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        if (locale != null) {
            String language = locale.getLanguage();
            String country = locale.getCountry();
            if (language != null) {
                if (country != null) {
                    setLocale(language + "_" + country);
                } else {
                    setLocale(language);
                }
            }
        }
        this.mCloseGuard.open("close");
        this.mExternalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        this.mExternalIsEmulated = Environment.isExternalStorageEmulated();
        if (((ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE)).isLowRamDevice()) {
            this.mLimitBmpFileSize = 6291456;
            this.mLimitGifFileSize = 10485760;
            return;
        }
        this.mLimitBmpFileSize = 54525952;
        this.mLimitGifFileSize = 20971520;
    }

    private void setDefaultRingtoneFileNames() {
        this.mDefaultRingtoneFilename = SystemProperties.get("ro.config.ringtone");
        this.mDefaultNotificationFilename = SystemProperties.get("ro.config.notification_sound");
        this.mDefaultAlarmAlertFilename = SystemProperties.get("ro.config.alarm_alert");
        if (DEBUG) {
            Log.v(TAG, "setDefaultRingtoneFileNames: ringtone=" + this.mDefaultRingtoneFilename + ",notification=" + this.mDefaultNotificationFilename + ",alarm=" + this.mDefaultAlarmAlertFilename);
        }
    }

    private boolean isDrmEnabled() {
        String prop = SystemProperties.get("drm.service.enabled");
        return prop != null ? prop.equals("true") : false;
    }

    private static boolean isSystemSoundWithMetadata(String path) {
        if (path.startsWith("/system/media/audio/alarms/") || path.startsWith("/system/media/audio/ringtones/") || path.startsWith("/system/media/audio/notifications/")) {
            return true;
        }
        return false;
    }

    private String settingSetIndicatorName(String base) {
        return base + "_set";
    }

    private boolean wasRingtoneAlreadySet(String name) {
        boolean z = false;
        try {
            if (System.getInt(this.mContext.getContentResolver(), settingSetIndicatorName(name)) != 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    private void prescan(String filePath, boolean prescanFiles) throws RemoteException {
        String where;
        String[] selectionArgs;
        if (DEBUG) {
            Log.v(TAG, "prescan>>> filePath=" + filePath + ",prescanFiles=" + prescanFiles);
        }
        Cursor c = null;
        this.mPlayLists.clear();
        if (filePath != null) {
            where = "_id>? AND _data=?";
            selectionArgs = new String[2];
            selectionArgs[0] = "";
            selectionArgs[1] = filePath;
        } else {
            where = "_id>?";
            selectionArgs = new String[1];
            selectionArgs[0] = "";
        }
        this.mDefaultRingtoneSet = wasRingtoneAlreadySet(System.RINGTONE);
        this.mDefaultNotificationSet = wasRingtoneAlreadySet(System.NOTIFICATION_SOUND);
        this.mDefaultAlarmSet = wasRingtoneAlreadySet(System.ALARM_ALERT);
        Builder builder = this.mFilesUri.buildUpon();
        builder.appendQueryParameter(MediaStore.PARAM_DELETE_DATA, "false");
        MediaBulkDeleter mediaBulkDeleter = new MediaBulkDeleter(this.mMediaProvider, builder.build());
        long lastId = Long.MIN_VALUE;
        if (prescanFiles) {
            String path;
            try {
                Uri limitUri = this.mFilesUri.buildUpon().appendQueryParameter("limit", "1000").appendQueryParameter("force", WifiEnterpriseConfig.ENGINE_ENABLE).build();
                this.mWasEmptyPriorToScan = true;
                while (true) {
                    selectionArgs[0] = "" + lastId;
                    if (c != null) {
                        c.close();
                    }
                    c = this.mMediaProvider.query(limitUri, FILES_PRESCAN_PROJECTION, where, selectionArgs, "_id", null);
                    if (c != null) {
                        if (c.getCount() == 0) {
                            break;
                        }
                        this.mWasEmptyPriorToScan = false;
                        String externalPrimaryStoragePathOnM = null;
                        String externalSecondaryStoragePathOnM = null;
                        boolean isSharedSdCardEanbled = SystemProperties.getBoolean("ro.mtk_shared_sdcard", false);
                        for (VolumeInfo vol : ((StorageManager) this.mContext.getSystemService(Context.STORAGE_SERVICE)).getVolumes()) {
                            if (!VolumeInfo.ID_PRIVATE_INTERNAL.equals(vol.id)) {
                                if (isSharedSdCardEanbled) {
                                    if (vol.isPrimary()) {
                                        externalPrimaryStoragePathOnM = vol.getPath().getPath() + "/";
                                    } else if (vol.getDisk() != null && vol.getDisk().isSd()) {
                                        externalSecondaryStoragePathOnM = vol.getPath().getPath() + "/";
                                    }
                                } else if (vol.isPhoneStorage()) {
                                    externalPrimaryStoragePathOnM = vol.getPath().getPath() + "/";
                                } else if (vol.getDisk() != null && vol.getDisk().isSd()) {
                                    externalSecondaryStoragePathOnM = vol.getPath().getPath() + "/";
                                }
                            }
                        }
                        if (externalPrimaryStoragePathOnM != null) {
                            if (externalPrimaryStoragePathOnM.startsWith("/storage/emulated/")) {
                                externalPrimaryStoragePathOnM = externalPrimaryStoragePathOnM + UserHandle.myUserId() + "/";
                            }
                        } else if (externalSecondaryStoragePathOnM != null) {
                            externalPrimaryStoragePathOnM = externalSecondaryStoragePathOnM;
                            externalSecondaryStoragePathOnM = null;
                        }
                        if (DEBUG) {
                            Log.v(TAG, "prescan>>> externalPrimaryStoragePathOnM=" + externalPrimaryStoragePathOnM + ", externalSecondaryStoragePathOnM=" + externalSecondaryStoragePathOnM + ", uid = " + UserHandle.myUserId());
                        }
                        while (c.moveToNext()) {
                            long rowId = c.getLong(0);
                            path = c.getString(1);
                            int format = c.getInt(2);
                            long lastModified = c.getLong(3);
                            lastId = rowId;
                            if (path != null) {
                                if (path.startsWith("/")) {
                                    boolean exists = false;
                                    String newPath = null;
                                    if (path.startsWith("/storage/sdcard")) {
                                        if (!path.startsWith(EXTERNAL_PRIMARY_STORAGE_PATH_L) || externalPrimaryStoragePathOnM == null) {
                                            if (!path.startsWith(EXTERNAL_SECONDARY_STORAGE_PATH_L) || externalSecondaryStoragePathOnM == null) {
                                                newPath = null;
                                            } else {
                                                newPath = path.replace(EXTERNAL_SECONDARY_STORAGE_PATH_L, externalSecondaryStoragePathOnM);
                                            }
                                        } else {
                                            newPath = path.replace(EXTERNAL_PRIMARY_STORAGE_PATH_L, externalPrimaryStoragePathOnM);
                                        }
                                        if (newPath != null) {
                                            if (DEBUG) {
                                                Log.v(TAG, "try to check if newPath exists, " + newPath);
                                            }
                                            if (Os.access(newPath, OsConstants.F_OK)) {
                                                if (DEBUG) {
                                                    Log.v(TAG, "update>>> path=" + path + ", newPath=" + newPath);
                                                }
                                                exists = true;
                                                Uri realUri = ContentUris.withAppendedId(this.mFilesUri, rowId);
                                                ContentValues values = new ContentValues();
                                                values.put("_data", newPath);
                                                this.mMediaProvider.update(realUri, values, null, null);
                                            }
                                        }
                                    }
                                    if (newPath == null) {
                                        exists = Os.access(path, OsConstants.F_OK);
                                    }
                                    if (!exists) {
                                        if (!MtpConstants.isAbstractObject(format)) {
                                            MediaFileType mediaFileType = MediaFile.getFileType(path);
                                            if (!MediaFile.isPlayListFileType(mediaFileType == null ? 0 : mediaFileType.fileType)) {
                                                mediaBulkDeleter.delete(rowId);
                                                if (path.toLowerCase(Locale.US).endsWith("/.nomedia")) {
                                                    mediaBulkDeleter.flush();
                                                    this.mMediaProvider.call(MediaStore.UNHIDE_CALL, new File(path).getParent(), null);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        break;
                    }
                }
            } catch (ErrnoException e) {
                if (DEBUG) {
                    Log.e(TAG, "prescan: ErrnoException! path=" + path);
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
                mediaBulkDeleter.flush();
            }
        }
        if (c != null) {
            c.close();
        }
        mediaBulkDeleter.flush();
        int originalImageCount = 0;
        int originalVideoCount = 0;
        int originalAudioCount = 0;
        try {
            c = this.mMediaProvider.query(this.mImagesUri.buildUpon().appendQueryParameter("force", WifiEnterpriseConfig.ENGINE_ENABLE).build(), ID_PROJECTION, null, null, null, null);
            if (c != null) {
                originalImageCount = c.getCount();
                c.close();
            }
            c = this.mMediaProvider.query(this.mVideoUri.buildUpon().appendQueryParameter("force", WifiEnterpriseConfig.ENGINE_ENABLE).build(), ID_PROJECTION, null, null, null, null);
            if (c != null) {
                originalVideoCount = c.getCount();
                c.close();
            }
            c = this.mMediaProvider.query(this.mAudioUri.buildUpon().appendQueryParameter("force", WifiEnterpriseConfig.ENGINE_ENABLE).build(), ID_PROJECTION, null, null, null, null);
            if (c != null) {
                originalAudioCount = c.getCount();
                c.close();
                c = null;
            }
            if (c != null) {
                c.close();
            }
            if (DEBUG) {
                Log.v(TAG, "prescan<<< imageCount=" + originalImageCount + ",videoCount=" + originalVideoCount + ", audioCount=" + originalAudioCount + ", lastId=" + lastId + ",isEmpty=" + this.mWasEmptyPriorToScan);
            }
        } catch (Throwable th2) {
            if (c != null) {
                c.close();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00e8 A:{Catch:{ RemoteException -> 0x012a, all -> 0x01ab }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0116 A:{Catch:{ RemoteException -> 0x012a, all -> 0x01ab }} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0162 A:{Catch:{ RemoteException -> 0x012a, all -> 0x01ab }} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01b4  */
    /* JADX WARNING: Removed duplicated region for block: B:79:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x013d  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x00ad A:{Catch:{ RemoteException -> 0x012a, all -> 0x01ab }} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00e8 A:{Catch:{ RemoteException -> 0x012a, all -> 0x01ab }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0116 A:{Catch:{ RemoteException -> 0x012a, all -> 0x01ab }} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0162 A:{Catch:{ RemoteException -> 0x012a, all -> 0x01ab }} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01b4  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x013d  */
    /* JADX WARNING: Removed duplicated region for block: B:79:? A:{SYNTHETIC, RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void pruneDeadThumbnailFiles() {
        if (DEBUG) {
            Log.v(TAG, "pruneDeadThumbnailFiles>>>");
        }
        HashSet<String> existingFiles = new HashSet();
        String directory = Environment.getExternalStorageDirectory().getPath() + "/" + MiniThumbFile.getMiniThumbFileDirectoryPath();
        String[] files = new File(directory).list();
        if (files == null) {
            files = new String[0];
        }
        for (String str : files) {
            existingFiles.add(directory + "/" + str);
        }
        int imageThumbCount = 0;
        int videoThumbCount = 0;
        Cursor c = null;
        try {
            ContentProviderClient contentProviderClient = this.mMediaProvider;
            Uri uri = this.mThumbsUri;
            String[] strArr = new String[1];
            strArr[0] = "_data";
            c = contentProviderClient.query(uri, strArr, null, null, null, null);
            String miniThumbFilePath;
            Iterator<String> iterator;
            if (c == null || !c.moveToFirst()) {
                if (c != null) {
                    c.close();
                }
                contentProviderClient = this.mMediaProvider;
                uri = this.mVideoThumbsUri;
                strArr = new String[1];
                strArr[0] = "_data";
                c = contentProviderClient.query(uri, strArr, null, null, null, null);
                if (c == null && c.moveToFirst()) {
                    videoThumbCount = c.getCount();
                    do {
                        existingFiles.remove(c.getString(0));
                    } while (c.moveToNext());
                    if (c != null) {
                    }
                    miniThumbFilePath = directory + "/" + MiniThumbFile.getMiniThumbFilePrefix();
                    iterator = existingFiles.iterator();
                    while (iterator.hasNext()) {
                    }
                    for (String fileToDelete : existingFiles) {
                    }
                    if (c != null) {
                    }
                    if (DEBUG) {
                    }
                } else {
                    if (c != null) {
                        c.close();
                        c = null;
                    }
                    if (imageThumbCount > 0 || videoThumbCount > 0) {
                        miniThumbFilePath = directory + "/" + MiniThumbFile.getMiniThumbFilePrefix();
                        iterator = existingFiles.iterator();
                        while (iterator.hasNext()) {
                            if (((String) iterator.next()).startsWith(miniThumbFilePath)) {
                                iterator.remove();
                            }
                        }
                    }
                    for (String fileToDelete2 : existingFiles) {
                        if (DEBUG) {
                            Log.v(TAG, "delete dead thumbnail file " + fileToDelete2);
                        }
                        try {
                            new File(fileToDelete2).delete();
                        } catch (SecurityException ex) {
                            Log.e(TAG, "pruneDeadThumbnailFiles: path=" + fileToDelete2, ex);
                        }
                    }
                    if (c != null) {
                        c.close();
                    }
                    if (DEBUG) {
                        Log.v(TAG, "pruneDeadThumbnailFiles<<< for " + directory);
                        return;
                    }
                    return;
                }
            }
            imageThumbCount = c.getCount();
            do {
                existingFiles.remove(c.getString(0));
            } while (c.moveToNext());
            if (c != null) {
            }
            contentProviderClient = this.mMediaProvider;
            uri = this.mVideoThumbsUri;
            strArr = new String[1];
            strArr[0] = "_data";
            c = contentProviderClient.query(uri, strArr, null, null, null, null);
            if (c == null) {
            }
            if (c != null) {
            }
            miniThumbFilePath = directory + "/" + MiniThumbFile.getMiniThumbFilePrefix();
            iterator = existingFiles.iterator();
            while (iterator.hasNext()) {
            }
            for (String fileToDelete22 : existingFiles) {
            }
            if (c != null) {
            }
            if (DEBUG) {
            }
        } catch (RemoteException e) {
            Log.e(TAG, "pruneDeadThumbnailFiles: RemoteException!", e);
            if (c != null) {
                c.close();
            }
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    private void postscan(String[] directories) throws RemoteException {
        if (this.mProcessPlaylists) {
            processPlayLists();
        }
        int originalImageCount = 0;
        int originalVideoCount = 0;
        Cursor cursor = null;
        try {
            cursor = this.mMediaProvider.query(this.mImagesUri.buildUpon().appendQueryParameter("force", WifiEnterpriseConfig.ENGINE_ENABLE).build(), ID_PROJECTION, null, null, null, null);
            if (cursor != null) {
                originalImageCount = cursor.getCount();
                cursor.close();
            }
            cursor = this.mMediaProvider.query(this.mVideoUri.buildUpon().appendQueryParameter("force", WifiEnterpriseConfig.ENGINE_ENABLE).build(), ID_PROJECTION, null, null, null, null);
            if (cursor != null) {
                originalVideoCount = cursor.getCount();
                cursor.close();
                cursor = null;
            }
            if (cursor != null) {
                cursor.close();
            }
            if ((originalImageCount == 0 || originalVideoCount == 0) && this.mImagesUri.equals(Images.Media.getContentUri("external"))) {
                pruneDeadThumbnailFiles();
            }
            this.mPlayLists.clear();
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            if ((null == null || null == null) && this.mImagesUri.equals(Images.Media.getContentUri("external"))) {
                pruneDeadThumbnailFiles();
            }
            this.mPlayLists.clear();
        }
    }

    private void releaseResources() {
        if (this.mDrmManagerClient != null) {
            this.mDrmManagerClient.close();
            this.mDrmManagerClient = null;
        }
    }

    public void scanDirectories(String[] directories) {
        try {
            long start = System.currentTimeMillis();
            prescan(null, true);
            long prescan = System.currentTimeMillis();
            this.mMediaInserter = new MediaInserter(this.mMediaProvider, 500);
            for (String processDirectory : directories) {
                processDirectory(processDirectory, this.mClient);
            }
            this.mMediaInserter.flushAll();
            this.mMediaInserter = null;
            long scan = System.currentTimeMillis();
            postscan(directories);
            long end = System.currentTimeMillis();
            if (DEBUG) {
                Log.d(TAG, " prescan time: " + (prescan - start) + "ms\n");
                Log.d(TAG, "    scan time: " + (scan - prescan) + "ms\n");
                Log.d(TAG, "postscan time: " + (end - scan) + "ms\n");
                Log.d(TAG, "   total time: " + (end - start) + "ms\n");
            }
            releaseResources();
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
            releaseResources();
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
            releaseResources();
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
            releaseResources();
        } catch (Throwable th) {
            releaseResources();
            throw th;
        }
    }

    public Uri scanSingleFile(String path, String mimeType) {
        try {
            prescan(path, true);
            File file = new File(path);
            if (file.exists()) {
                String str = path;
                String str2 = mimeType;
                Uri doScanFile = this.mClient.doScanFile(str, str2, file.lastModified() / 1000, file.length(), file.isDirectory(), true, isNoMediaPath(path));
                releaseResources();
                return doScanFile;
            }
            Log.e(TAG, "scanSingleFile: Not exist path=" + path);
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
            return null;
        } finally {
            releaseResources();
        }
    }

    /* JADX WARNING: Missing block: B:20:0x0071, code:
            if (r10.regionMatches(true, r7 + 1, "AlbumArtSmall", 0, 13) == false) goto L_0x0073;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isNoMediaFile(String path) {
        if (new File(path).isDirectory()) {
            return false;
        }
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0 && lastSlash + 2 < path.length()) {
            if (path.regionMatches(lastSlash + 1, "._", 0, 2)) {
                return true;
            }
            if (path.regionMatches(true, path.length() - 4, ".jpg", 0, 4)) {
                if (!path.regionMatches(true, lastSlash + 1, "AlbumArt_{", 0, 10)) {
                    if (!path.regionMatches(true, lastSlash + 1, "AlbumArt.", 0, 9)) {
                        int length = (path.length() - lastSlash) - 1;
                        if (length == 17) {
                        }
                        if (length == 10) {
                            if (path.regionMatches(true, lastSlash + 1, "Folder", 0, 6)) {
                                return true;
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static void clearMediaPathCache(boolean clearMediaPaths, boolean clearNoMediaPaths) {
        synchronized (MediaScanner.class) {
            if (clearMediaPaths) {
                mMediaPaths.clear();
            }
            if (clearNoMediaPaths) {
                mNoMediaPaths.clear();
            }
        }
    }

    /* JADX WARNING: Missing block: B:35:0x007f, code:
            return isNoMediaFile(r9);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isNoMediaPath(String path) {
        if (path == null) {
            return false;
        }
        if (path.indexOf("/.") >= 0) {
            return true;
        }
        int firstSlash = path.lastIndexOf(47);
        if (firstSlash <= 0) {
            return false;
        }
        String parent = path.substring(0, firstSlash);
        synchronized (MediaScanner.class) {
            if (mNoMediaPaths.containsKey(parent)) {
                return true;
            } else if (!mMediaPaths.containsKey(parent)) {
                int offset = 1;
                while (offset >= 0) {
                    int slashIndex = path.indexOf(47, offset);
                    if (slashIndex > offset) {
                        slashIndex++;
                        if (new File(path.substring(0, slashIndex) + MediaStore.MEDIA_IGNORE_FILENAME).exists()) {
                            mNoMediaPaths.put(parent, "");
                            return true;
                        }
                    } else if (slashIndex == offset) {
                        slashIndex++;
                    }
                    offset = slashIndex;
                }
                mMediaPaths.put(parent, "");
            }
        }
    }

    public void scanMtpFile(String path, int objectHandle, int format) {
        MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = mediaFileType == null ? 0 : mediaFileType.fileType;
        File file = new File(path);
        long lastModifiedSeconds = file.lastModified() / 1000;
        if (MediaFile.isAudioFileType(fileType) || MediaFile.isVideoFileType(fileType) || MediaFile.isImageFileType(fileType) || MediaFile.isPlayListFileType(fileType) || MediaFile.isDrmFileType(fileType)) {
            this.mMtpObjectHandle = objectHandle;
            Cursor fileList = null;
            try {
                if (MediaFile.isPlayListFileType(fileType)) {
                    prescan(null, true);
                    FileEntry entry = makeEntryFor(path);
                    if (entry != null) {
                        fileList = this.mMediaProvider.query(this.mFilesUri, FILES_PRESCAN_PROJECTION, null, 0, null, null);
                        processPlayList(entry, fileList);
                    }
                } else {
                    prescan(path, false);
                    this.mClient.doScanFile(path, mediaFileType.mimeType, lastModifiedSeconds, file.length(), format == 12289, true, isNoMediaPath(path));
                }
                this.mMtpObjectHandle = 0;
                if (fileList != null) {
                    fileList.close();
                }
                releaseResources();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
                this.mMtpObjectHandle = 0;
                if (fileList != null) {
                    fileList.close();
                }
                releaseResources();
            } catch (Throwable th) {
                this.mMtpObjectHandle = 0;
                if (fileList != null) {
                    fileList.close();
                }
                releaseResources();
                throw th;
            }
            return;
        }
        ContentValues values = new ContentValues();
        values.put("_size", Long.valueOf(format == 12289 ? 0 : file.length()));
        values.put("date_modified", Long.valueOf(lastModifiedSeconds));
        try {
            String[] whereArgs = new String[1];
            whereArgs[0] = Integer.toString(objectHandle);
            this.mMediaProvider.update(Files.getMtpObjectsUri(this.mVolumeName), values, "_id=?", whereArgs);
        } catch (RemoteException e2) {
            Log.e(TAG, "RemoteException in scanMtpFile", e2);
        }
    }

    FileEntry makeEntryFor(String path) {
        Cursor cursor = null;
        try {
            String[] selectionArgs = new String[1];
            selectionArgs[0] = path;
            cursor = this.mMediaProvider.query(this.mFilesUriNoNotify, FILES_PRESCAN_PROJECTION, "_data=?", selectionArgs, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String str = path;
            FileEntry fileEntry = new FileEntry(cursor.getLong(0), str, cursor.getLong(3), cursor.getInt(2));
            if (cursor != null) {
                cursor.close();
            }
            return fileEntry;
        } catch (RemoteException e) {
            Log.e(TAG, "makeEntryFor: RemoteException! path=" + path, e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int matchPaths(String path1, String path2) {
        int result = 0;
        int end1 = path1.length();
        int end2 = path2.length();
        while (end1 > 0 && end2 > 0) {
            int slash1 = path1.lastIndexOf(47, end1 - 1);
            int slash2 = path2.lastIndexOf(47, end2 - 1);
            int backSlash1 = path1.lastIndexOf(92, end1 - 1);
            int backSlash2 = path2.lastIndexOf(92, end2 - 1);
            int start1 = slash1 > backSlash1 ? slash1 : backSlash1;
            int start2 = slash2 > backSlash2 ? slash2 : backSlash2;
            start1 = start1 < 0 ? 0 : start1 + 1;
            start2 = start2 < 0 ? 0 : start2 + 1;
            int length = end1 - start1;
            if (end2 - start2 != length || !path1.regionMatches(true, start1, path2, start2, length)) {
                break;
            }
            result++;
            end1 = start1 - 1;
            end2 = start2 - 1;
        }
        return result;
    }

    private boolean matchEntries(long rowId, String data) {
        int len = this.mPlaylistEntries.size();
        boolean done = true;
        for (int i = 0; i < len; i++) {
            PlaylistEntry entry = (PlaylistEntry) this.mPlaylistEntries.get(i);
            if (entry.bestmatchlevel != Integer.MAX_VALUE) {
                done = false;
                if (data.equalsIgnoreCase(entry.path)) {
                    entry.bestmatchid = rowId;
                    entry.bestmatchlevel = Integer.MAX_VALUE;
                } else {
                    int matchLength = matchPaths(data, entry.path);
                    if (matchLength > entry.bestmatchlevel) {
                        entry.bestmatchid = rowId;
                        entry.bestmatchlevel = matchLength;
                    }
                }
            }
        }
        return done;
    }

    private void cachePlaylistEntry(String line, String playListDirectory) {
        boolean z = true;
        PlaylistEntry entry = new PlaylistEntry();
        int entryLength = line.length();
        while (entryLength > 0 && Character.isWhitespace(line.charAt(entryLength - 1))) {
            entryLength--;
        }
        if (entryLength >= 3) {
            boolean fullPath;
            if (entryLength < line.length()) {
                line = line.substring(0, entryLength);
            }
            char ch1 = line.charAt(0);
            if (ch1 == '/') {
                fullPath = true;
            } else if (Character.isLetter(ch1) && line.charAt(1) == ':') {
                if (line.charAt(2) != '\\') {
                    z = false;
                }
                fullPath = z;
            } else {
                fullPath = false;
            }
            if (!fullPath) {
                line = playListDirectory + line;
            }
            entry.path = line;
            this.mPlaylistEntries.add(entry);
        }
    }

    private void processCachedPlaylist(Cursor fileList, ContentValues values, Uri playlistUri) {
        fileList.moveToPosition(-1);
        while (fileList.moveToNext()) {
            if (matchEntries(fileList.getLong(0), fileList.getString(1))) {
                break;
            }
        }
        int len = this.mPlaylistEntries.size();
        int index = 0;
        for (int i = 0; i < len; i++) {
            PlaylistEntry entry = (PlaylistEntry) this.mPlaylistEntries.get(i);
            if (entry.bestmatchlevel > 0) {
                try {
                    values.clear();
                    values.put("play_order", Integer.valueOf(index));
                    values.put("audio_id", Long.valueOf(entry.bestmatchid));
                    this.mMediaProvider.insert(playlistUri, values);
                    index++;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in MediaScanner.processCachedPlaylist()", e);
                    return;
                }
            }
        }
        this.mPlaylistEntries.clear();
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0060 A:{SYNTHETIC, Splitter: B:25:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0072 A:{SYNTHETIC, Splitter: B:31:0x0072} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processM3uPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                try {
                    String line = reader2.readLine();
                    this.mPlaylistEntries.clear();
                    while (line != null) {
                        if (line.length() > 0 && line.charAt(0) != '#') {
                            cachePlaylistEntry(line, playListDirectory);
                        }
                        line = reader2.readLine();
                    }
                    processCachedPlaylist(fileList, values, uri);
                    reader = reader2;
                } catch (IOException e2) {
                    e = e2;
                    reader = reader2;
                    try {
                        Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e);
                        if (reader == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e3);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e32) {
                    Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e32);
                }
            }
        } catch (IOException e4) {
            e32 = e4;
            Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e32);
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e322) {
                    Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e322);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0068 A:{SYNTHETIC, Splitter: B:25:0x0068} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x007a A:{SYNTHETIC, Splitter: B:31:0x007a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processPlsPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                try {
                    this.mPlaylistEntries.clear();
                    for (String line = reader2.readLine(); line != null; line = reader2.readLine()) {
                        if (line.startsWith("File")) {
                            int equals = line.indexOf(61);
                            if (equals > 0) {
                                cachePlaylistEntry(line.substring(equals + 1), playListDirectory);
                            }
                        }
                    }
                    processCachedPlaylist(fileList, values, uri);
                    reader = reader2;
                } catch (IOException e2) {
                    e = e2;
                    reader = reader2;
                    try {
                        Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e);
                        if (reader == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e3);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e32) {
                    Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e32);
                }
            }
        } catch (IOException e4) {
            e32 = e4;
            Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e32);
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e322) {
                    Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e322);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x006b A:{SYNTHETIC, Splitter: B:31:0x006b} */
    /* JADX WARNING: Removed duplicated region for block: B:47:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0059 A:{SYNTHETIC, Splitter: B:25:0x0059} */
    /* JADX WARNING: Removed duplicated region for block: B:45:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0044 A:{SYNTHETIC, Splitter: B:17:0x0044} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processWplPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        SAXException e;
        IOException e2;
        Throwable th;
        FileInputStream fis = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                FileInputStream fis2 = new FileInputStream(f);
                try {
                    this.mPlaylistEntries.clear();
                    Xml.parse(fis2, Xml.findEncodingByName("UTF-8"), new WplHandler(this, playListDirectory, uri, fileList).getContentHandler());
                    processCachedPlaylist(fileList, values, uri);
                    fis = fis2;
                } catch (SAXException e3) {
                    e = e3;
                    fis = fis2;
                    e.printStackTrace();
                    if (fis == null) {
                        try {
                            fis.close();
                            return;
                        } catch (IOException e22) {
                            Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e22);
                            return;
                        }
                    }
                    return;
                } catch (IOException e4) {
                    e22 = e4;
                    fis = fis2;
                    try {
                        e22.printStackTrace();
                        if (fis == null) {
                            try {
                                fis.close();
                                return;
                            } catch (IOException e222) {
                                Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e222);
                                return;
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fis != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fis = fis2;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e2222) {
                            Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e2222);
                        }
                    }
                    throw th;
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e22222) {
                    Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e22222);
                }
            }
        } catch (SAXException e5) {
            e = e5;
            e.printStackTrace();
            if (fis == null) {
            }
        } catch (IOException e6) {
            e22222 = e6;
            e22222.printStackTrace();
            if (fis == null) {
            }
        }
    }

    private void processPlayList(FileEntry entry, Cursor fileList) throws RemoteException {
        String path = entry.mPath;
        ContentValues values = new ContentValues();
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash < 0) {
            throw new IllegalArgumentException("bad path " + path);
        }
        Uri membersUri;
        long rowId = entry.mRowId;
        String name = values.getAsString("name");
        if (name == null) {
            name = values.getAsString("title");
            if (name == null) {
                int lastDot = path.lastIndexOf(46);
                if (lastDot < 0) {
                    name = path.substring(lastSlash + 1);
                } else {
                    name = path.substring(lastSlash + 1, lastDot);
                }
            }
        }
        values.put("name", name);
        values.put("date_modified", Long.valueOf(entry.mLastModified));
        Uri uri;
        if (rowId == 0) {
            values.put("_data", path);
            uri = this.mMediaProvider.insert(this.mPlaylistsUri, values);
            rowId = ContentUris.parseId(uri);
            membersUri = Uri.withAppendedPath(uri, "members");
        } else {
            uri = ContentUris.withAppendedId(this.mPlaylistsUri, rowId);
            this.mMediaProvider.update(uri, values, null, null);
            membersUri = Uri.withAppendedPath(uri, "members");
            this.mMediaProvider.delete(membersUri, null, null);
        }
        String playListDirectory = path.substring(0, lastSlash + 1);
        MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = mediaFileType == null ? 0 : mediaFileType.fileType;
        if (fileType == 501) {
            processM3uPlayList(path, playListDirectory, membersUri, values, fileList);
        } else if (fileType == 502) {
            processPlsPlayList(path, playListDirectory, membersUri, values, fileList);
        } else if (fileType == 503) {
            processWplPlayList(path, playListDirectory, membersUri, values, fileList);
        }
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processPlayLists() throws RemoteException {
        Iterator<FileEntry> iterator = this.mPlayLists.iterator();
        Cursor cursor = null;
        try {
            cursor = this.mMediaProvider.query(this.mFilesUri, FILES_PRESCAN_PROJECTION, "media_type=2", null, null, null);
            while (iterator.hasNext()) {
                FileEntry entry = (FileEntry) iterator.next();
                if (entry.mLastModifiedChanged) {
                    processPlayList(entry, cursor);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RemoteException e1) {
            Log.e(TAG, "processPlayLists: RemoteException!", e1);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public void close() {
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            this.mMediaProvider.close();
            native_finalize();
        }
    }

    protected void finalize() throws Throwable {
        try {
            this.mCloseGuard.warnIfOpen();
            close();
        } finally {
            super.finalize();
        }
    }

    private boolean isValueslessMimeType(String mimetype) {
        boolean valueless = false;
        if (MIME_APPLICATION_OCTET_STREAM.equalsIgnoreCase(mimetype)) {
            valueless = true;
            if (DEBUG) {
                Log.v(TAG, "isValueslessMimeType: mimetype=" + mimetype);
            }
        }
        return valueless;
    }

    public void preScanAll(String volume) {
        try {
            prescan(null, true);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e);
        }
    }

    public void postScanAll(ArrayList<String> playlistFilePathList) {
        try {
            if (this.mProcessPlaylists) {
                for (String path : playlistFilePathList) {
                    FileEntry entry = makeEntryFor(path);
                    long lastModified = new File(path).lastModified();
                    long delta = entry != null ? lastModified - entry.mLastModified : 0;
                    boolean wasModified = delta > 1 || delta < -1;
                    if (entry == null || wasModified) {
                        if (wasModified) {
                            entry.mLastModified = lastModified;
                        } else {
                            entry = new FileEntry(0, path, lastModified, 0);
                        }
                        entry.mLastModifiedChanged = true;
                    }
                    this.mPlayLists.add(entry);
                }
                processPlayLists();
            }
        } catch (Throwable e) {
            Log.e(TAG, "RemoteException in MediaScanner.postScanAll()", e);
        }
        int originalImageCount = 0;
        int originalVideoCount = 0;
        Cursor cursor = null;
        try {
            cursor = this.mMediaProvider.query(this.mImagesUri.buildUpon().appendQueryParameter("force", WifiEnterpriseConfig.ENGINE_ENABLE).build(), ID_PROJECTION, null, null, null, null);
            if (cursor != null) {
                originalImageCount = cursor.getCount();
                cursor.close();
            }
            cursor = this.mMediaProvider.query(this.mVideoUri.buildUpon().appendQueryParameter("force", WifiEnterpriseConfig.ENGINE_ENABLE).build(), ID_PROJECTION, null, null, null, null);
            if (cursor != null) {
                originalVideoCount = cursor.getCount();
                cursor.close();
                cursor = null;
            }
            if (cursor != null) {
                cursor.close();
            }
            if ((originalImageCount == 0 || originalVideoCount == 0) && this.mImagesUri.equals(Images.Media.getContentUri("external"))) {
                pruneDeadThumbnailFiles();
            }
        } catch (Throwable e2) {
            Log.e(TAG, "RemoteException in MediaScanner.postScanAll()", e2);
            if (cursor != null) {
                cursor.close();
            }
            if ((null == null || null == null) && this.mImagesUri.equals(Images.Media.getContentUri("external"))) {
                pruneDeadThumbnailFiles();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            if ((null == null || null == null) && this.mImagesUri.equals(Images.Media.getContentUri("external"))) {
                pruneDeadThumbnailFiles();
            }
        }
        if (DEBUG) {
            Log.v(TAG, "postScanAll");
        }
    }

    public ArrayList<String> scanFolders(Handler insertHanlder, String[] folders, String volume, boolean isSingelFile) {
        try {
            this.mPlayLists.clear();
            this.mMediaInserter = new MediaInserter(insertHanlder, 100);
            int i = 0;
            int length = folders.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    break;
                }
                String path = folders[i2];
                if (isSingelFile) {
                    File file = new File(path);
                    this.mClient.doScanFile(path, null, file.lastModified() / 1000, file.length(), file.isDirectory(), false, isNoMediaPath(path));
                } else {
                    processDirectory(path, this.mClient);
                }
                i = i2 + 1;
            }
            this.mMediaInserter.flushAll();
            this.mMediaInserter = null;
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
        }
        return this.mPlaylistFilePathList;
    }

    public ArrayList<String> scanFolders(String[] folders, String volume, boolean isSingelFileOrEmptyFolder) {
        try {
            this.mPlayLists.clear();
            this.mMediaInserter = new MediaInserter(this.mMediaProvider, 500);
            int i = 0;
            int length = folders.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    break;
                }
                String folder = folders[i2];
                File file = new File(folder);
                if (file.exists()) {
                    this.mClient.doScanFile(folder, null, file.lastModified() / 1000, file.length(), file.isDirectory(), false, isNoMediaPath(folder));
                }
                if (!isSingelFileOrEmptyFolder) {
                    processDirectory(folder, this.mClient);
                }
                i = i2 + 1;
            }
            this.mMediaInserter.flushAll();
            this.mMediaInserter = null;
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
        }
        return this.mPlaylistFilePathList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:91:0x01e4 A:{SYNTHETIC, Splitter: B:91:0x01e4} */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x01ec  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01ad A:{SYNTHETIC, Splitter: B:80:0x01ad} */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01b5  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x016c A:{SYNTHETIC, Splitter: B:66:0x016c} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0174  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isStereoPhoto(String filePath) {
        FileNotFoundException e;
        IllegalArgumentException e2;
        Throwable th;
        if (filePath == null) {
            if (DEBUG) {
                Log.d(TAG, "<isStereoPhoto> filePath is null!!");
            }
            return false;
        } else if (new File(filePath).exists()) {
            long start = System.currentTimeMillis();
            ArrayList<Section> sections = parseApp1Info(filePath);
            if (sections == null || sections.size() < 0) {
                if (DEBUG) {
                    Log.d(TAG, "<isStereoPhoto> " + filePath + ", no app1 sections");
                }
                return false;
            }
            RandomAccessFile rafIn = null;
            try {
                RandomAccessFile rafIn2 = new RandomAccessFile(filePath, FullBackup.ROOT_TREE_TOKEN);
                int i = 0;
                while (i < sections.size()) {
                    try {
                        if (isStereo((Section) sections.get(i), rafIn2)) {
                            if (DEBUG) {
                                Log.d(TAG, "<isStereoPhoto> " + filePath + " is stereo photo");
                            }
                            if (rafIn2 != null) {
                                try {
                                    rafIn2.close();
                                } catch (IOException e3) {
                                    Log.e(TAG, "<isStereoPhoto> IOException:", e3);
                                }
                            }
                            if (DEBUG) {
                                Log.d(TAG, "<isStereoPhoto> <performance> costs(ms): " + (System.currentTimeMillis() - start));
                            }
                            return true;
                        }
                        i++;
                    } catch (FileNotFoundException e4) {
                        e = e4;
                        rafIn = rafIn2;
                        Log.e(TAG, "<isStereoPhoto> FileNotFoundException:", e);
                        if (rafIn != null) {
                            try {
                                rafIn.close();
                            } catch (IOException e32) {
                                Log.e(TAG, "<isStereoPhoto> IOException:", e32);
                            }
                        }
                        if (DEBUG) {
                            Log.d(TAG, "<isStereoPhoto> <performance> costs(ms): " + (System.currentTimeMillis() - start));
                        }
                        return false;
                    } catch (IllegalArgumentException e5) {
                        e2 = e5;
                        rafIn = rafIn2;
                        try {
                            Log.e(TAG, "<isStereoPhoto> IllegalArgumentException:", e2);
                            if (rafIn != null) {
                                try {
                                    rafIn.close();
                                } catch (IOException e322) {
                                    Log.e(TAG, "<isStereoPhoto> IOException:", e322);
                                }
                            }
                            if (DEBUG) {
                                Log.d(TAG, "<isStereoPhoto> <performance> costs(ms): " + (System.currentTimeMillis() - start));
                            }
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            if (rafIn != null) {
                                try {
                                    rafIn.close();
                                } catch (IOException e3222) {
                                    Log.e(TAG, "<isStereoPhoto> IOException:", e3222);
                                }
                            }
                            if (DEBUG) {
                                Log.d(TAG, "<isStereoPhoto> <performance> costs(ms): " + (System.currentTimeMillis() - start));
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        rafIn = rafIn2;
                        if (rafIn != null) {
                        }
                        if (DEBUG) {
                        }
                        throw th;
                    }
                }
                if (DEBUG) {
                    Log.d(TAG, "<isStereoPhoto> " + filePath + " is not stereo photo");
                }
                if (rafIn2 != null) {
                    try {
                        rafIn2.close();
                    } catch (IOException e32222) {
                        Log.e(TAG, "<isStereoPhoto> IOException:", e32222);
                    }
                }
                if (DEBUG) {
                    Log.d(TAG, "<isStereoPhoto> <performance> costs(ms): " + (System.currentTimeMillis() - start));
                }
                return false;
            } catch (FileNotFoundException e6) {
                e = e6;
                Log.e(TAG, "<isStereoPhoto> FileNotFoundException:", e);
                if (rafIn != null) {
                }
                if (DEBUG) {
                }
                return false;
            } catch (IllegalArgumentException e7) {
                e2 = e7;
                Log.e(TAG, "<isStereoPhoto> IllegalArgumentException:", e2);
                if (rafIn != null) {
                }
                if (DEBUG) {
                }
                return false;
            }
        } else {
            if (DEBUG) {
                Log.d(TAG, "<isStereoPhoto> " + filePath + " not exists!!!");
            }
            return false;
        }
    }

    private static boolean isStereo(Section section, RandomAccessFile rafIn) {
        try {
            if (section.mIsXmpMain) {
                rafIn.seek(section.mOffset + 2);
                int len = rafIn.readUnsignedShort() - 2;
                rafIn.skipBytes(XMP_HEADER_START.length());
                byte[] xmpBuffer = new byte[(len - XMP_HEADER_START.length())];
                rafIn.read(xmpBuffer, 0, xmpBuffer.length);
                String xmpContent = new String(xmpBuffer);
                if (xmpContent == null) {
                    if (DEBUG) {
                        Log.d(TAG, "<isStereo> xmpContent is null");
                    }
                    return false;
                } else if (xmpContent.contains(MTK_REFOCUS_PREFIX)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            Log.e(TAG, "<isStereo> IOException:", e);
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:56:0x0107 A:{SYNTHETIC, Splitter: B:56:0x0107} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00be A:{SYNTHETIC, Splitter: B:46:0x00be} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static ArrayList<Section> parseApp1Info(String filePath) {
        IOException e;
        Throwable th;
        RandomAccessFile raf = null;
        try {
            RandomAccessFile raf2 = new RandomAccessFile(filePath, FullBackup.ROOT_TREE_TOKEN);
            try {
                if (raf2.readUnsignedShort() != SOI) {
                    if (DEBUG) {
                        Log.d(TAG, "<parseApp1Info> error, find no SOI");
                    }
                    ArrayList<Section> arrayList = new ArrayList();
                    if (raf2 != null) {
                        try {
                            raf2.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "<parseApp1Info> IOException, path " + filePath, e2);
                        }
                    }
                    return arrayList;
                }
                ArrayList<Section> sections = new ArrayList();
                while (true) {
                    int value = raf2.readUnsignedShort();
                    if (value == -1 || value == SOS) {
                        break;
                    }
                    int marker = value;
                    long offset = raf2.getFilePointer() - 2;
                    int length = raf2.readUnsignedShort();
                    if (value == APP1) {
                        Section section = new Section(value, offset, length);
                        long currentPos = raf2.getFilePointer();
                        section = checkIfMainXmpInApp1(raf2, section);
                        if (section != null && section.mIsXmpMain) {
                            sections.add(section);
                            break;
                        }
                        raf2.seek(currentPos);
                    }
                    raf2.skipBytes(length - 2);
                }
                if (raf2 != null) {
                    try {
                        raf2.close();
                    } catch (IOException e22) {
                        Log.e(TAG, "<parseApp1Info> IOException, path " + filePath, e22);
                        raf = raf2;
                    }
                }
                return sections;
            } catch (IOException e3) {
                e22 = e3;
                raf = raf2;
                try {
                    Log.e(TAG, "<parseApp1Info> IOException, path " + filePath, e22);
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e222) {
                            Log.e(TAG, "<parseApp1Info> IOException, path " + filePath, e222);
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e2222) {
                            Log.e(TAG, "<parseApp1Info> IOException, path " + filePath, e2222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                raf = raf2;
                if (raf != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e2222 = e4;
            Log.e(TAG, "<parseApp1Info> IOException, path " + filePath, e2222);
            if (raf != null) {
            }
            return null;
        }
    }

    private static Section checkIfMainXmpInApp1(RandomAccessFile raf, Section section) {
        UnsupportedEncodingException e;
        IOException e2;
        if (section == null) {
            if (DEBUG) {
                Log.d(TAG, "<checkIfMainXmpInApp1> section is null!!!");
            }
            return null;
        }
        try {
            if (section.mMarker == APP1) {
                raf.seek(section.mOffset + 4);
                byte[] buffer = new byte[XMP_EXT_MAIN_HEADER1.length()];
                raf.read(buffer, 0, buffer.length);
                String str = new String(buffer, 0, XMP_HEADER_START.length());
                String str2;
                try {
                    if (XMP_HEADER_START.equals(str)) {
                        section.mIsXmpMain = true;
                        str2 = str;
                    }
                } catch (UnsupportedEncodingException e3) {
                    e = e3;
                    Log.e(TAG, "<checkIfMainXmpInApp1> UnsupportedEncodingException" + e);
                    return null;
                } catch (IOException e4) {
                    e2 = e4;
                    str2 = str;
                    Log.e(TAG, "<checkIfMainXmpInApp1> IOException" + e2);
                    return null;
                }
            }
            return section;
        } catch (UnsupportedEncodingException e5) {
            e = e5;
            Log.e(TAG, "<checkIfMainXmpInApp1> UnsupportedEncodingException" + e);
            return null;
        } catch (IOException e6) {
            e2 = e6;
            Log.e(TAG, "<checkIfMainXmpInApp1> IOException" + e2);
            return null;
        }
    }
}
