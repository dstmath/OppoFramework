package android.media;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.drm.DrmManagerClient;
import android.graphics.BitmapFactory;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.provider.Settings;
import android.sax.ElementListener;
import android.sax.RootElement;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.telecom.Logging.Session;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.util.Xml;
import dalvik.system.CloseGuard;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

@Deprecated
public class MediaScanner implements AutoCloseable {
    private static final String ALARMS_DIR = "/alarms/";
    private static final String AUDIOBOOKS_DIR = "/audiobooks/";
    private static final int DATE_MODIFIED_PLAYLISTS_COLUMN_INDEX = 2;
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private static final boolean ENABLE_BULK_INSERTS = true;
    private static final int FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX = 3;
    private static final int FILES_PRESCAN_FORMAT_COLUMN_INDEX = 2;
    private static final int FILES_PRESCAN_ID_COLUMN_INDEX = 0;
    private static final int FILES_PRESCAN_MEDIA_TYPE_COLUMN_INDEX = 4;
    private static final int FILES_PRESCAN_PATH_COLUMN_INDEX = 1;
    @UnsupportedAppUsage
    private static final String[] FILES_PRESCAN_PROJECTION = {"_id", "_data", "format", "date_modified", "media_type"};
    /* access modifiers changed from: private */
    public static final String[] ID3_GENRES = {"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie", "Britpop", null, "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian", "Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "Synthpop"};
    private static final int ID_PLAYLISTS_COLUMN_INDEX = 0;
    private static final String[] ID_PROJECTION = {"_id"};
    public static final String LAST_INTERNAL_SCAN_FINGERPRINT = "lastScanFingerprint";
    private static final String MUSIC_DIR = "/music/";
    private static final String NOTIFICATIONS_DIR = "/notifications/";
    private static final String OEM_SOUNDS_DIR = (Environment.getOemDirectory() + "/media/audio");
    private static final int PATH_PLAYLISTS_COLUMN_INDEX = 1;
    private static final String[] PLAYLIST_MEMBERS_PROJECTION = {MediaStore.Audio.Playlists.Members.PLAYLIST_ID};
    private static final String PODCASTS_DIR = "/podcasts/";
    private static final String PRODUCT_SOUNDS_DIR = (Environment.getProductDirectory() + "/media/audio");
    private static final String RINGTONES_DIR = "/ringtones/";
    public static final String SCANNED_BUILD_PREFS_NAME = "MediaScanBuild";
    private static final String SYSTEM_SOUNDS_DIR = (Environment.getRootDirectory() + "/media/audio");
    private static final String TAG = "MediaScanner";
    private static HashMap<String, String> mMediaPaths = new HashMap<>();
    private static HashMap<String, String> mNoMediaPaths = new HashMap<>();
    /* access modifiers changed from: private */
    public static String sLastInternalScanFingerprint;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public final Uri mAudioUri;
    /* access modifiers changed from: private */
    public final BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();
    @UnsupportedAppUsage
    private final MyMediaScannerClient mClient = new MyMediaScannerClient();
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public final Context mContext;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public String mDefaultAlarmAlertFilename;
    /* access modifiers changed from: private */
    public boolean mDefaultAlarmSet;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public String mDefaultNotificationFilename;
    /* access modifiers changed from: private */
    public boolean mDefaultNotificationSet;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public String mDefaultRingtoneFilename;
    /* access modifiers changed from: private */
    public boolean mDefaultRingtoneSet;
    /* access modifiers changed from: private */
    public DrmManagerClient mDrmManagerClient = null;
    private final Uri mFilesFullUri;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public final Uri mFilesUri;
    /* access modifiers changed from: private */
    public final Uri mImagesUri;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public MediaInserter mMediaInserter;
    /* access modifiers changed from: private */
    public final ContentProviderClient mMediaProvider;
    /* access modifiers changed from: private */
    public int mMtpObjectHandle;
    private long mNativeContext;
    private int mOriginalCount;
    @UnsupportedAppUsage
    private final String mPackageName;
    /* access modifiers changed from: private */
    public final ArrayList<FileEntry> mPlayLists = new ArrayList<>();
    private final ArrayList<PlaylistEntry> mPlaylistEntries = new ArrayList<>();
    /* access modifiers changed from: private */
    public final Uri mPlaylistsUri;
    /* access modifiers changed from: private */
    public final boolean mProcessGenres;
    /* access modifiers changed from: private */
    public final boolean mProcessPlaylists;
    /* access modifiers changed from: private */
    public final Uri mVideoUri;
    private final String mVolumeName;

    private final native void native_finalize();

    private static final native void native_init();

    private final native void native_setup();

    private native void processDirectory(String str, MediaScannerClient mediaScannerClient);

    /* access modifiers changed from: private */
    public native boolean processFile(String str, String str2, MediaScannerClient mediaScannerClient);

    @UnsupportedAppUsage
    private native void setLocale(String str);

    public native byte[] extractAlbumArt(FileDescriptor fileDescriptor);

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    /* access modifiers changed from: private */
    public static class FileEntry {
        int mFormat;
        long mLastModified;
        @UnsupportedAppUsage
        boolean mLastModifiedChanged;
        int mMediaType;
        String mPath;
        @UnsupportedAppUsage
        long mRowId;

        @UnsupportedAppUsage
        @Deprecated
        FileEntry(long rowId, String path, long lastModified, int format) {
            this(rowId, path, lastModified, format, 0);
        }

        FileEntry(long rowId, String path, long lastModified, int format, int mediaType) {
            this.mRowId = rowId;
            this.mPath = path;
            this.mLastModified = lastModified;
            this.mFormat = format;
            this.mMediaType = mediaType;
            this.mLastModifiedChanged = false;
        }

        public String toString() {
            return this.mPath + " mRowId: " + this.mRowId;
        }
    }

    private static class PlaylistEntry {
        long bestmatchid;
        int bestmatchlevel;
        String path;

        private PlaylistEntry() {
        }
    }

    @UnsupportedAppUsage
    public MediaScanner(Context c, String volumeName) {
        native_setup();
        this.mContext = c;
        this.mPackageName = c.getPackageName();
        this.mVolumeName = volumeName;
        BitmapFactory.Options options = this.mBitmapOptions;
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        setDefaultRingtoneFileNames();
        this.mMediaProvider = this.mContext.getContentResolver().acquireContentProviderClient(MediaStore.AUTHORITY);
        if (sLastInternalScanFingerprint == null) {
            sLastInternalScanFingerprint = this.mContext.getSharedPreferences(SCANNED_BUILD_PREFS_NAME, 0).getString(LAST_INTERNAL_SCAN_FINGERPRINT, new String());
        }
        this.mAudioUri = MediaStore.Audio.Media.getContentUri(volumeName);
        this.mVideoUri = MediaStore.Video.Media.getContentUri(volumeName);
        this.mImagesUri = MediaStore.Images.Media.getContentUri(volumeName);
        this.mFilesUri = MediaStore.Files.getContentUri(volumeName);
        this.mFilesFullUri = MediaStore.setIncludeTrashed(MediaStore.setIncludePending(this.mFilesUri.buildUpon().appendQueryParameter("nonotify", WifiEnterpriseConfig.ENGINE_ENABLE).build()));
        if (!volumeName.equals(MediaStore.VOLUME_INTERNAL)) {
            this.mProcessPlaylists = true;
            this.mProcessGenres = true;
            this.mPlaylistsUri = MediaStore.Audio.Playlists.getContentUri(volumeName);
        } else {
            this.mProcessPlaylists = false;
            this.mProcessGenres = false;
            this.mPlaylistsUri = null;
        }
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        if (locale != null) {
            String language = locale.getLanguage();
            String country = locale.getCountry();
            if (language != null) {
                if (country != null) {
                    setLocale(language + Session.SESSION_SEPARATION_CHAR_CHILD + country);
                } else {
                    setLocale(language);
                }
            }
        }
        this.mCloseGuard.open("close");
    }

    private void setDefaultRingtoneFileNames() {
        this.mDefaultRingtoneFilename = SystemProperties.get("ro.config.ringtone");
        this.mDefaultNotificationFilename = SystemProperties.get("ro.config.notification_sound");
        this.mDefaultAlarmAlertFilename = SystemProperties.get("ro.config.alarm_alert");
    }

    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public boolean isDrmEnabled() {
        String prop = SystemProperties.get("drm.service.enabled");
        return prop != null && prop.equals("true");
    }

    private class MyMediaScannerClient implements MediaScannerClient {
        private String mAlbum;
        private String mAlbumArtist;
        private String mArtist;
        private int mColorRange;
        private int mColorStandard;
        private int mColorTransfer;
        private int mCompilation;
        private String mComposer;
        private long mDate;
        private final SimpleDateFormat mDateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        private int mDuration;
        private long mFileSize;
        @UnsupportedAppUsage
        @Deprecated
        private int mFileType;
        private String mGenre;
        private int mHeight;
        @UnsupportedAppUsage
        private boolean mIsDrm;
        private long mLastModified;
        @UnsupportedAppUsage
        private String mMimeType;
        @UnsupportedAppUsage
        private boolean mNoMedia;
        @UnsupportedAppUsage
        private String mPath;
        private boolean mScanSuccess;
        private String mTitle;
        private int mTrack;
        private int mWidth;
        private String mWriter;
        private int mYear;

        public MyMediaScannerClient() {
            this.mDateFormatter.setTimeZone(TimeZone.getTimeZone(Time.TIMEZONE_UTC));
        }

        @UnsupportedAppUsage
        public FileEntry beginFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            boolean z;
            boolean noMedia2;
            this.mMimeType = mimeType;
            this.mFileSize = fileSize;
            this.mIsDrm = false;
            this.mScanSuccess = true;
            if (!isDirectory) {
                if (noMedia || !MediaScanner.isNoMediaFile(path)) {
                    noMedia2 = noMedia;
                } else {
                    noMedia2 = true;
                }
                this.mNoMedia = noMedia2;
                if (this.mMimeType == null) {
                    this.mMimeType = MediaFile.getMimeTypeForFile(path);
                }
                if (MediaScanner.this.isDrmEnabled() && MediaFile.isDrmMimeType(this.mMimeType)) {
                    getMimeTypeFromDrm(path);
                }
            }
            FileEntry entry = MediaScanner.this.makeEntryFor(path);
            long delta = entry != null ? lastModified - entry.mLastModified : 0;
            boolean wasModified = delta > 1 || delta < -1;
            if (entry == null || wasModified) {
                if (wasModified) {
                    entry.mLastModified = lastModified;
                    z = true;
                } else {
                    z = true;
                    entry = new FileEntry(0, path, lastModified, isDirectory ? 12289 : 0, 0);
                }
                entry.mLastModifiedChanged = z;
            }
            if (!MediaScanner.this.mProcessPlaylists || !MediaFile.isPlayListMimeType(this.mMimeType)) {
                this.mArtist = null;
                this.mAlbumArtist = null;
                this.mAlbum = null;
                this.mTitle = null;
                this.mComposer = null;
                this.mGenre = null;
                this.mTrack = 0;
                this.mYear = 0;
                this.mDuration = 0;
                this.mPath = path;
                this.mDate = 0;
                this.mLastModified = lastModified;
                this.mWriter = null;
                this.mCompilation = 0;
                this.mWidth = 0;
                this.mHeight = 0;
                this.mColorStandard = -1;
                this.mColorTransfer = -1;
                this.mColorRange = -1;
                return entry;
            }
            MediaScanner.this.mPlayLists.add(entry);
            return null;
        }

        @Override // android.media.MediaScannerClient
        @UnsupportedAppUsage
        public void scanFile(String path, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            doScanFile(path, null, lastModified, fileSize, isDirectory, false, noMedia);
        }

        /* JADX WARNING: Removed duplicated region for block: B:36:0x00ca A[ADDED_TO_REGION] */
        /* JADX WARNING: Removed duplicated region for block: B:39:0x00d3  */
        /* JADX WARNING: Removed duplicated region for block: B:45:0x00ea  */
        @UnsupportedAppUsage
        public Uri doScanFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean scanAlways, boolean noMedia) {
            boolean scanAlways2;
            String path2;
            try {
                FileEntry entry = beginFile(path, mimeType, lastModified, fileSize, isDirectory, noMedia);
                if (entry == null) {
                    return null;
                }
                if (MediaScanner.this.mMtpObjectHandle != 0) {
                    try {
                        entry.mRowId = 0;
                    } catch (RemoteException e) {
                        e = e;
                    }
                }
                if (entry.mPath != null) {
                    if ((!MediaScanner.this.mDefaultNotificationSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultNotificationFilename)) || ((!MediaScanner.this.mDefaultRingtoneSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultRingtoneFilename)) || (!MediaScanner.this.mDefaultAlarmSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultAlarmAlertFilename)))) {
                        Log.w(MediaScanner.TAG, "forcing rescan of " + entry.mPath + "since ringtone setting didn't finish");
                        scanAlways2 = true;
                        if (entry.mLastModifiedChanged) {
                        }
                        if (noMedia) {
                        }
                    } else if (MediaScanner.isSystemSoundWithMetadata(entry.mPath) && !Build.FINGERPRINT.equals(MediaScanner.sLastInternalScanFingerprint)) {
                        Log.i(MediaScanner.TAG, "forcing rescan of " + entry.mPath + " since build fingerprint changed");
                        scanAlways2 = true;
                        if (entry.mLastModifiedChanged && !scanAlways2) {
                            return null;
                        }
                        if (noMedia) {
                            try {
                                return endFile(entry, false, false, false, false, false, false);
                            } catch (RemoteException e2) {
                                e = e2;
                                Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
                                return null;
                            }
                        } else {
                            boolean isaudio = MediaFile.isAudioMimeType(this.mMimeType);
                            boolean isvideo = MediaFile.isVideoMimeType(this.mMimeType);
                            boolean isimage = MediaFile.isImageMimeType(this.mMimeType);
                            if (isaudio || isvideo || isimage) {
                                try {
                                    path2 = Environment.maybeTranslateEmulatedPathToInternal(new File(path)).getAbsolutePath();
                                } catch (RemoteException e3) {
                                    e = e3;
                                    Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
                                    return null;
                                }
                            } else {
                                path2 = path;
                            }
                            if (isaudio || isvideo) {
                                try {
                                    this.mScanSuccess = MediaScanner.this.processFile(path2, mimeType, this);
                                } catch (RemoteException e4) {
                                    e = e4;
                                    Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
                                    return null;
                                }
                            }
                            if (isimage) {
                                try {
                                    this.mScanSuccess = processImageFile(path2);
                                } catch (RemoteException e5) {
                                    e = e5;
                                }
                            }
                            String lowpath = path2.toLowerCase(Locale.ROOT);
                            boolean ringtones = this.mScanSuccess && lowpath.indexOf(MediaScanner.RINGTONES_DIR) > 0;
                            boolean notifications = this.mScanSuccess && lowpath.indexOf(MediaScanner.NOTIFICATIONS_DIR) > 0;
                            boolean alarms = this.mScanSuccess && lowpath.indexOf(MediaScanner.ALARMS_DIR) > 0;
                            boolean podcasts = this.mScanSuccess && lowpath.indexOf(MediaScanner.PODCASTS_DIR) > 0;
                            boolean audiobooks = this.mScanSuccess && lowpath.indexOf(MediaScanner.AUDIOBOOKS_DIR) > 0;
                            try {
                                return endFile(entry, ringtones, notifications, alarms, podcasts, audiobooks, this.mScanSuccess && (lowpath.indexOf(MediaScanner.MUSIC_DIR) > 0 || (!ringtones && !notifications && !alarms && !podcasts && !audiobooks)));
                            } catch (RemoteException e6) {
                                e = e6;
                                Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
                                return null;
                            }
                        }
                    }
                }
                scanAlways2 = scanAlways;
                try {
                    if (entry.mLastModifiedChanged) {
                    }
                    if (noMedia) {
                    }
                } catch (RemoteException e7) {
                    e = e7;
                    Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
                    return null;
                }
            } catch (RemoteException e8) {
                e = e8;
                Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
                return null;
            }
        }

        private long parseDate(String date) {
            try {
                return this.mDateFormatter.parse(date).getTime();
            } catch (ParseException e) {
                return 0;
            }
        }

        private int parseSubstring(String s, int start, int defaultValue) {
            int length = s.length();
            if (start == length) {
                return defaultValue;
            }
            int start2 = start + 1;
            char ch = s.charAt(start);
            if (ch < '0' || ch > '9') {
                return defaultValue;
            }
            int result = ch - '0';
            while (start2 < length) {
                int start3 = start2 + 1;
                char ch2 = s.charAt(start2);
                if (ch2 < '0' || ch2 > '9') {
                    return result;
                }
                result = (result * 10) + (ch2 - '0');
                start2 = start3;
            }
            return result;
        }

        @Override // android.media.MediaScannerClient
        @UnsupportedAppUsage
        public void handleStringTag(String name, String value) {
            if (name.equalsIgnoreCase("title") || name.startsWith("title;")) {
                this.mTitle = value;
            } else if (name.equalsIgnoreCase("artist") || name.startsWith("artist;")) {
                this.mArtist = value.trim();
            } else if (name.equalsIgnoreCase("albumartist") || name.startsWith("albumartist;") || name.equalsIgnoreCase("band") || name.startsWith("band;")) {
                this.mAlbumArtist = value.trim();
            } else if (name.equalsIgnoreCase("album") || name.startsWith("album;")) {
                this.mAlbum = value.trim();
            } else if (name.equalsIgnoreCase(MediaStore.Audio.AudioColumns.COMPOSER) || name.startsWith("composer;")) {
                this.mComposer = value.trim();
            } else if (!MediaScanner.this.mProcessGenres || (!name.equalsIgnoreCase(MediaStore.Audio.AudioColumns.GENRE) && !name.startsWith("genre;"))) {
                boolean z = false;
                if (name.equalsIgnoreCase(MediaStore.Audio.AudioColumns.YEAR) || name.startsWith("year;")) {
                    this.mYear = parseSubstring(value, 0, 0);
                } else if (name.equalsIgnoreCase("tracknumber") || name.startsWith("tracknumber;")) {
                    this.mTrack = ((this.mTrack / 1000) * 1000) + parseSubstring(value, 0, 0);
                } else if (name.equalsIgnoreCase("discnumber") || name.equals("set") || name.startsWith("set;")) {
                    this.mTrack = (parseSubstring(value, 0, 0) * 1000) + (this.mTrack % 1000);
                } else if (name.equalsIgnoreCase("duration")) {
                    this.mDuration = parseSubstring(value, 0, 0);
                } else if (name.equalsIgnoreCase("writer") || name.startsWith("writer;")) {
                    this.mWriter = value.trim();
                } else if (name.equalsIgnoreCase(MediaStore.Audio.AudioColumns.COMPILATION)) {
                    this.mCompilation = parseSubstring(value, 0, 0);
                } else if (name.equalsIgnoreCase("isdrm")) {
                    if (parseSubstring(value, 0, 0) == 1) {
                        z = true;
                    }
                    this.mIsDrm = z;
                } else if (name.equalsIgnoreCase("date")) {
                    this.mDate = parseDate(value);
                } else if (name.equalsIgnoreCase("width")) {
                    this.mWidth = parseSubstring(value, 0, 0);
                } else if (name.equalsIgnoreCase("height")) {
                    this.mHeight = parseSubstring(value, 0, 0);
                } else if (name.equalsIgnoreCase("colorstandard")) {
                    this.mColorStandard = parseSubstring(value, 0, -1);
                } else if (name.equalsIgnoreCase("colortransfer")) {
                    this.mColorTransfer = parseSubstring(value, 0, -1);
                } else if (name.equalsIgnoreCase("colorrange")) {
                    this.mColorRange = parseSubstring(value, 0, -1);
                }
            } else {
                this.mGenre = getGenreName(value);
            }
        }

        private boolean convertGenreCode(String input, String expected) {
            String output = getGenreName(input);
            if (output.equals(expected)) {
                return true;
            }
            Log.d(MediaScanner.TAG, "'" + input + "' -> '" + output + "', expected '" + expected + "'");
            return false;
        }

        private void testGenreNameConverter() {
            convertGenreCode("2", "Country");
            convertGenreCode("(2)", "Country");
            convertGenreCode("(2", "(2");
            convertGenreCode("2 Foo", "Country");
            convertGenreCode("(2) Foo", "Country");
            convertGenreCode("(2 Foo", "(2 Foo");
            convertGenreCode("2Foo", "2Foo");
            convertGenreCode("(2)Foo", "Country");
            convertGenreCode("200 Foo", "Foo");
            convertGenreCode("(200) Foo", "Foo");
            convertGenreCode("200Foo", "200Foo");
            convertGenreCode("(200)Foo", "Foo");
            convertGenreCode("200)Foo", "200)Foo");
            convertGenreCode("200) Foo", "200) Foo");
        }

        public String getGenreName(String genreTagValue) {
            if (genreTagValue == null) {
                return null;
            }
            int length = genreTagValue.length();
            if (length > 0) {
                boolean parenthesized = false;
                StringBuffer number = new StringBuffer();
                int i = 0;
                while (i < length) {
                    char c = genreTagValue.charAt(i);
                    if (i != 0 || c != '(') {
                        if (!Character.isDigit(c)) {
                            break;
                        }
                        number.append(c);
                    } else {
                        parenthesized = true;
                    }
                    i++;
                }
                char charAfterNumber = i < length ? genreTagValue.charAt(i) : ' ';
                if ((parenthesized && charAfterNumber == ')') || (!parenthesized && Character.isWhitespace(charAfterNumber))) {
                    try {
                        short genreIndex = Short.parseShort(number.toString());
                        if (genreIndex >= 0) {
                            if (genreIndex < MediaScanner.ID3_GENRES.length && MediaScanner.ID3_GENRES[genreIndex] != null) {
                                return MediaScanner.ID3_GENRES[genreIndex];
                            }
                            if (genreIndex == 255) {
                                return null;
                            }
                            if (genreIndex >= 255 || i + 1 >= length) {
                                return number.toString();
                            }
                            if (parenthesized && charAfterNumber == ')') {
                                i++;
                            }
                            String ret = genreTagValue.substring(i).trim();
                            if (ret.length() != 0) {
                                return ret;
                            }
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
            return genreTagValue;
        }

        private boolean processImageFile(String path) {
            try {
                MediaScanner.this.mBitmapOptions.outWidth = 0;
                MediaScanner.this.mBitmapOptions.outHeight = 0;
                BitmapFactory.decodeFile(path, MediaScanner.this.mBitmapOptions);
                this.mWidth = MediaScanner.this.mBitmapOptions.outWidth;
                this.mHeight = MediaScanner.this.mBitmapOptions.outHeight;
                if (this.mWidth <= 0 || this.mHeight <= 0) {
                    return false;
                }
                return true;
            } catch (Throwable th) {
                return false;
            }
        }

        @Override // android.media.MediaScannerClient
        @UnsupportedAppUsage
        public void setMimeType(String mimeType) {
            if (!"audio/mp4".equals(this.mMimeType) || !mimeType.startsWith("video")) {
                this.mMimeType = mimeType;
            }
        }

        @UnsupportedAppUsage
        private ContentValues toValues() {
            ContentValues map = new ContentValues();
            map.put("_data", this.mPath);
            map.put("title", this.mTitle);
            map.put("date_modified", Long.valueOf(this.mLastModified));
            map.put("_size", Long.valueOf(this.mFileSize));
            map.put("mime_type", this.mMimeType);
            map.put(MediaStore.MediaColumns.IS_DRM, Boolean.valueOf(this.mIsDrm));
            map.putNull(MediaStore.MediaColumns.HASH);
            String resolution = null;
            int i = this.mWidth;
            if (i > 0 && this.mHeight > 0) {
                map.put("width", Integer.valueOf(i));
                map.put("height", Integer.valueOf(this.mHeight));
                resolution = this.mWidth + "x" + this.mHeight;
            }
            if (!this.mNoMedia) {
                boolean isVideoMimeType = MediaFile.isVideoMimeType(this.mMimeType);
                String str = MediaStore.UNKNOWN_STRING;
                if (isVideoMimeType) {
                    String str2 = this.mArtist;
                    map.put("artist", (str2 == null || str2.length() <= 0) ? str : this.mArtist);
                    String str3 = this.mAlbum;
                    if (str3 != null && str3.length() > 0) {
                        str = this.mAlbum;
                    }
                    map.put("album", str);
                    map.put("duration", Integer.valueOf(this.mDuration));
                    if (resolution != null) {
                        map.put(MediaStore.Video.VideoColumns.RESOLUTION, resolution);
                    }
                    int i2 = this.mColorStandard;
                    if (i2 >= 0) {
                        map.put(MediaStore.Video.VideoColumns.COLOR_STANDARD, Integer.valueOf(i2));
                    }
                    int i3 = this.mColorTransfer;
                    if (i3 >= 0) {
                        map.put(MediaStore.Video.VideoColumns.COLOR_TRANSFER, Integer.valueOf(i3));
                    }
                    int i4 = this.mColorRange;
                    if (i4 >= 0) {
                        map.put(MediaStore.Video.VideoColumns.COLOR_RANGE, Integer.valueOf(i4));
                    }
                    long j = this.mDate;
                    if (j > 0) {
                        map.put("datetaken", Long.valueOf(j));
                    }
                } else if (!MediaFile.isImageMimeType(this.mMimeType) && MediaFile.isAudioMimeType(this.mMimeType)) {
                    String str4 = this.mArtist;
                    map.put("artist", (str4 == null || str4.length() <= 0) ? str : this.mArtist);
                    String str5 = this.mAlbumArtist;
                    map.put(MediaStore.Audio.AudioColumns.ALBUM_ARTIST, (str5 == null || str5.length() <= 0) ? null : this.mAlbumArtist);
                    String str6 = this.mAlbum;
                    if (str6 != null && str6.length() > 0) {
                        str = this.mAlbum;
                    }
                    map.put("album", str);
                    map.put(MediaStore.Audio.AudioColumns.COMPOSER, this.mComposer);
                    map.put(MediaStore.Audio.AudioColumns.GENRE, this.mGenre);
                    int i5 = this.mYear;
                    if (i5 != 0) {
                        map.put(MediaStore.Audio.AudioColumns.YEAR, Integer.valueOf(i5));
                    }
                    map.put(MediaStore.Audio.AudioColumns.TRACK, Integer.valueOf(this.mTrack));
                    map.put("duration", Integer.valueOf(this.mDuration));
                    map.put(MediaStore.Audio.AudioColumns.COMPILATION, Integer.valueOf(this.mCompilation));
                }
            }
            return map;
        }

        /* JADX WARNING: Removed duplicated region for block: B:20:0x0060  */
        @UnsupportedAppUsage
        private Uri endFile(FileEntry entry, boolean ringtones, boolean notifications, boolean alarms, boolean podcasts, boolean audiobooks, boolean music) throws RemoteException {
            int degree;
            String album;
            int lastSlash;
            String str = this.mArtist;
            if (str == null || str.length() == 0) {
                this.mArtist = this.mAlbumArtist;
            }
            ContentValues values = toValues();
            String title = values.getAsString("title");
            if (title == null || TextUtils.isEmpty(title.trim())) {
                values.put("title", MediaFile.getFileTitle(values.getAsString("_data")));
            }
            if (MediaStore.UNKNOWN_STRING.equals(values.getAsString("album")) && (lastSlash = (album = values.getAsString("_data")).lastIndexOf(47)) >= 0) {
                int previousSlash = 0;
                while (true) {
                    int idx = album.indexOf(47, previousSlash + 1);
                    if (idx >= 0 && idx < lastSlash) {
                        previousSlash = idx;
                    } else if (previousSlash != 0) {
                        values.put("album", album.substring(previousSlash + 1, lastSlash));
                    }
                }
                if (previousSlash != 0) {
                }
            }
            long rowId = entry.mRowId;
            if (MediaFile.isAudioMimeType(this.mMimeType) && (rowId == 0 || MediaScanner.this.mMtpObjectHandle != 0)) {
                values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, Boolean.valueOf(ringtones));
                values.put(MediaStore.Audio.AudioColumns.IS_NOTIFICATION, Boolean.valueOf(notifications));
                values.put(MediaStore.Audio.AudioColumns.IS_ALARM, Boolean.valueOf(alarms));
                values.put(MediaStore.Audio.AudioColumns.IS_MUSIC, Boolean.valueOf(music));
                values.put(MediaStore.Audio.AudioColumns.IS_PODCAST, Boolean.valueOf(podcasts));
                values.put(MediaStore.Audio.AudioColumns.IS_AUDIOBOOK, Boolean.valueOf(audiobooks));
            } else if (MediaFile.isExifMimeType(this.mMimeType) && !this.mNoMedia) {
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(entry.mPath);
                } catch (Exception e) {
                }
                if (exif != null) {
                    long time = exif.getGpsDateTime();
                    if (time != -1) {
                        values.put("datetaken", Long.valueOf(time));
                    } else {
                        long time2 = exif.getDateTime();
                        if (time2 != -1 && Math.abs((this.mLastModified * 1000) - time2) >= 86400000) {
                            values.put("datetaken", Long.valueOf(time2));
                        }
                    }
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
                    if (orientation != -1) {
                        if (orientation == 3) {
                            degree = 180;
                        } else if (orientation == 6) {
                            degree = 90;
                        } else if (orientation != 8) {
                            degree = 0;
                        } else {
                            degree = 270;
                        }
                        values.put("orientation", Integer.valueOf(degree));
                    }
                }
            }
            Uri tableUri = MediaScanner.this.mFilesUri;
            int mediaType = 0;
            MediaInserter inserter = MediaScanner.this.mMediaInserter;
            if (!this.mNoMedia) {
                if (MediaFile.isVideoMimeType(this.mMimeType)) {
                    tableUri = MediaScanner.this.mVideoUri;
                    mediaType = 3;
                } else if (MediaFile.isImageMimeType(this.mMimeType)) {
                    tableUri = MediaScanner.this.mImagesUri;
                    mediaType = 1;
                } else if (MediaFile.isAudioMimeType(this.mMimeType)) {
                    tableUri = MediaScanner.this.mAudioUri;
                    mediaType = 2;
                } else if (MediaFile.isPlayListMimeType(this.mMimeType)) {
                    tableUri = MediaScanner.this.mPlaylistsUri;
                    mediaType = 4;
                }
            }
            Uri result = null;
            boolean needToSetSettings = false;
            if (!notifications || MediaScanner.this.mDefaultNotificationSet) {
                if (!ringtones || MediaScanner.this.mDefaultRingtoneSet) {
                    if (alarms && !MediaScanner.this.mDefaultAlarmSet && (TextUtils.isEmpty(MediaScanner.this.mDefaultAlarmAlertFilename) || doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultAlarmAlertFilename))) {
                        needToSetSettings = true;
                    }
                } else if (TextUtils.isEmpty(MediaScanner.this.mDefaultRingtoneFilename) || doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultRingtoneFilename)) {
                    needToSetSettings = true;
                }
            } else if (TextUtils.isEmpty(MediaScanner.this.mDefaultNotificationFilename) || doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultNotificationFilename)) {
                needToSetSettings = true;
            }
            if (rowId == 0) {
                if (MediaScanner.this.mMtpObjectHandle != 0) {
                    values.put(MediaStore.MediaColumns.MEDIA_SCANNER_NEW_OBJECT_ID, Integer.valueOf(MediaScanner.this.mMtpObjectHandle));
                }
                if (tableUri == MediaScanner.this.mFilesUri) {
                    int format = entry.mFormat;
                    if (format == 0) {
                        format = MediaFile.getFormatCode(entry.mPath, this.mMimeType);
                    }
                    values.put("format", Integer.valueOf(format));
                }
                if (inserter == null || needToSetSettings) {
                    if (inserter != null) {
                        inserter.flushAll();
                    }
                    result = MediaScanner.this.mMediaProvider.insert(tableUri, values);
                } else if (entry.mFormat == 12289) {
                    inserter.insertwithPriority(tableUri, values);
                } else {
                    inserter.insert(tableUri, values);
                }
                if (result != null) {
                    rowId = ContentUris.parseId(result);
                    entry.mRowId = rowId;
                }
            } else {
                result = ContentUris.withAppendedId(tableUri, rowId);
                values.remove("_data");
                if (!this.mNoMedia && mediaType != entry.mMediaType) {
                    ContentValues mediaTypeValues = new ContentValues();
                    mediaTypeValues.put("media_type", Integer.valueOf(mediaType));
                    MediaScanner.this.mMediaProvider.update(ContentUris.withAppendedId(MediaScanner.this.mFilesUri, rowId), mediaTypeValues, null, null);
                }
                MediaScanner.this.mMediaProvider.update(result, values, null, null);
            }
            if (needToSetSettings) {
                if (notifications) {
                    setRingtoneIfNotSet(Settings.System.NOTIFICATION_SOUND, tableUri, rowId);
                    boolean unused = MediaScanner.this.mDefaultNotificationSet = true;
                } else if (ringtones) {
                    setRingtoneIfNotSet(Settings.System.RINGTONE, tableUri, rowId);
                    boolean unused2 = MediaScanner.this.mDefaultRingtoneSet = true;
                } else if (alarms) {
                    setRingtoneIfNotSet(Settings.System.ALARM_ALERT, tableUri, rowId);
                    boolean unused3 = MediaScanner.this.mDefaultAlarmSet = true;
                }
            }
            return result;
        }

        private boolean doesPathHaveFilename(String path, String filename) {
            int pathFilenameStart = path.lastIndexOf(File.separatorChar) + 1;
            int filenameLength = filename.length();
            if (!path.regionMatches(pathFilenameStart, filename, 0, filenameLength) || pathFilenameStart + filenameLength != path.length()) {
                return false;
            }
            return true;
        }

        private void setRingtoneIfNotSet(String settingName, Uri uri, long rowId) {
            if (!MediaScanner.this.wasRingtoneAlreadySet(settingName)) {
                ContentResolver cr = MediaScanner.this.mContext.getContentResolver();
                if (TextUtils.isEmpty(Settings.System.getString(cr, settingName))) {
                    Uri settingUri = Settings.System.getUriFor(settingName);
                    RingtoneManager.setActualDefaultRingtoneUri(MediaScanner.this.mContext, RingtoneManager.getDefaultType(settingUri), ContentUris.withAppendedId(uri, rowId));
                }
                Settings.System.putInt(cr, MediaScanner.this.settingSetIndicatorName(settingName), 1);
            }
        }

        @UnsupportedAppUsage
        @Deprecated
        private int getFileTypeFromDrm(String path) {
            return 0;
        }

        private void getMimeTypeFromDrm(String path) {
            this.mMimeType = null;
            if (MediaScanner.this.mDrmManagerClient == null) {
                MediaScanner mediaScanner = MediaScanner.this;
                DrmManagerClient unused = mediaScanner.mDrmManagerClient = new DrmManagerClient(mediaScanner.mContext);
            }
            if (MediaScanner.this.mDrmManagerClient.canHandle(path, (String) null)) {
                this.mIsDrm = true;
                this.mMimeType = MediaScanner.this.mDrmManagerClient.getOriginalMimeType(path);
            }
            if (this.mMimeType == null) {
                this.mMimeType = ContentResolver.MIME_TYPE_DEFAULT;
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean isSystemSoundWithMetadata(String path) {
        if (path.startsWith(SYSTEM_SOUNDS_DIR + ALARMS_DIR)) {
            return true;
        }
        if (path.startsWith(SYSTEM_SOUNDS_DIR + RINGTONES_DIR)) {
            return true;
        }
        if (path.startsWith(SYSTEM_SOUNDS_DIR + NOTIFICATIONS_DIR)) {
            return true;
        }
        if (path.startsWith(OEM_SOUNDS_DIR + ALARMS_DIR)) {
            return true;
        }
        if (path.startsWith(OEM_SOUNDS_DIR + RINGTONES_DIR)) {
            return true;
        }
        if (path.startsWith(OEM_SOUNDS_DIR + NOTIFICATIONS_DIR)) {
            return true;
        }
        if (path.startsWith(PRODUCT_SOUNDS_DIR + ALARMS_DIR)) {
            return true;
        }
        if (path.startsWith(PRODUCT_SOUNDS_DIR + RINGTONES_DIR)) {
            return true;
        }
        if (path.startsWith(PRODUCT_SOUNDS_DIR + NOTIFICATIONS_DIR)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public String settingSetIndicatorName(String base) {
        return base + "_set";
    }

    /* access modifiers changed from: private */
    public boolean wasRingtoneAlreadySet(String name) {
        try {
            return Settings.System.getInt(this.mContext.getContentResolver(), settingSetIndicatorName(name)) != 0;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:68:0x0176  */
    @UnsupportedAppUsage
    private void prescan(String filePath, boolean prescanFiles) throws RemoteException {
        String[] selectionArgs;
        String where;
        MediaBulkDeleter deleter;
        Cursor c;
        MediaBulkDeleter deleter2;
        MediaBulkDeleter deleter3;
        Cursor c2 = null;
        this.mPlayLists.clear();
        int i = 1;
        int i2 = 2;
        int i3 = 0;
        if (filePath != null) {
            selectionArgs = new String[]{"", filePath};
            where = "_id>? AND _data=?";
        } else {
            selectionArgs = new String[]{""};
            where = "_id>?";
        }
        this.mDefaultRingtoneSet = wasRingtoneAlreadySet(Settings.System.RINGTONE);
        this.mDefaultNotificationSet = wasRingtoneAlreadySet(Settings.System.NOTIFICATION_SOUND);
        this.mDefaultAlarmSet = wasRingtoneAlreadySet(Settings.System.ALARM_ALERT);
        Uri.Builder builder = this.mFilesUri.buildUpon();
        builder.appendQueryParameter(MediaStore.PARAM_DELETE_DATA, "false");
        MediaBulkDeleter deleter4 = new MediaBulkDeleter(this.mMediaProvider, builder.build());
        if (prescanFiles) {
            try {
                Uri limitUri = this.mFilesUri.buildUpon().appendQueryParameter("limit", "1000").build();
                long lastId = Long.MIN_VALUE;
                while (true) {
                    selectionArgs[i3] = "" + lastId;
                    if (c2 != null) {
                        try {
                            c2.close();
                            c2 = null;
                        } catch (Throwable th) {
                            th = th;
                            c = c2;
                            deleter2 = deleter4;
                            if (c != null) {
                            }
                            deleter2.flush();
                            throw th;
                        }
                    }
                    deleter3 = deleter4;
                    try {
                        c2 = this.mMediaProvider.query(limitUri, FILES_PRESCAN_PROJECTION, where, selectionArgs, "_id", null);
                        if (c2 == null) {
                            break;
                        }
                        try {
                            if (c2.getCount() == 0) {
                                break;
                            }
                            lastId = lastId;
                            while (c2.moveToNext()) {
                                long rowId = c2.getLong(i3);
                                String path = c2.getString(i);
                                int format = c2.getInt(i2);
                                c2.getLong(3);
                                lastId = rowId;
                                if (path == null || !path.startsWith("/")) {
                                    c = c2;
                                    deleter2 = deleter3;
                                } else {
                                    boolean exists = false;
                                    try {
                                        exists = Os.access(path, OsConstants.F_OK);
                                    } catch (ErrnoException e) {
                                    } catch (Throwable th2) {
                                        th = th2;
                                        c = c2;
                                        deleter2 = deleter3;
                                        if (c != null) {
                                        }
                                        deleter2.flush();
                                        throw th;
                                    }
                                    if (exists || MtpConstants.isAbstractObject(format)) {
                                        c = c2;
                                        deleter2 = deleter3;
                                    } else if (!MediaFile.isPlayListMimeType(MediaFile.getMimeTypeForFile(path))) {
                                        deleter2 = deleter3;
                                        try {
                                            deleter2.delete(rowId);
                                            if (path.toLowerCase(Locale.US).endsWith("/.nomedia")) {
                                                deleter2.flush();
                                                c = c2;
                                                try {
                                                    this.mMediaProvider.call(MediaStore.UNHIDE_CALL, new File(path).getParent(), null);
                                                } catch (Throwable th3) {
                                                    th = th3;
                                                }
                                            } else {
                                                c = c2;
                                            }
                                        } catch (Throwable th4) {
                                            th = th4;
                                            c = c2;
                                            if (c != null) {
                                            }
                                            deleter2.flush();
                                            throw th;
                                        }
                                    } else {
                                        c = c2;
                                        deleter2 = deleter3;
                                    }
                                }
                                deleter3 = deleter2;
                                c2 = c;
                                i = 1;
                                i2 = 2;
                                i3 = 0;
                            }
                            deleter4 = deleter3;
                            builder = builder;
                            i = 1;
                            i2 = 2;
                            i3 = 0;
                        } catch (Throwable th5) {
                            th = th5;
                            c = c2;
                            deleter2 = deleter3;
                            if (c != null) {
                                c.close();
                            }
                            deleter2.flush();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        deleter2 = deleter3;
                        c = c2;
                        if (c != null) {
                        }
                        deleter2.flush();
                        throw th;
                    }
                }
                deleter = deleter3;
            } catch (Throwable th7) {
                th = th7;
                deleter2 = deleter4;
                c = null;
                if (c != null) {
                }
                deleter2.flush();
                throw th;
            }
        } else {
            deleter = deleter4;
        }
        if (c2 != null) {
            c2.close();
        }
        deleter.flush();
        this.mOriginalCount = 0;
        Cursor c3 = this.mMediaProvider.query(this.mImagesUri, ID_PROJECTION, null, null, null, null);
        if (c3 != null) {
            this.mOriginalCount = c3.getCount();
            c3.close();
        }
    }

    static class MediaBulkDeleter {
        final Uri mBaseUri;
        final ContentProviderClient mProvider;
        ArrayList<String> whereArgs = new ArrayList<>(100);
        StringBuilder whereClause = new StringBuilder();

        public MediaBulkDeleter(ContentProviderClient provider, Uri baseUri) {
            this.mProvider = provider;
            this.mBaseUri = baseUri;
        }

        public void delete(long id) throws RemoteException {
            if (this.whereClause.length() != 0) {
                this.whereClause.append(SmsManager.REGEX_PREFIX_DELIMITER);
            }
            this.whereClause.append("?");
            ArrayList<String> arrayList = this.whereArgs;
            arrayList.add("" + id);
            if (this.whereArgs.size() > 100) {
                flush();
            }
        }

        public void flush() throws RemoteException {
            int size = this.whereArgs.size();
            if (size > 0) {
                String[] foo = (String[]) this.whereArgs.toArray(new String[size]);
                ContentProviderClient contentProviderClient = this.mProvider;
                Uri uri = this.mBaseUri;
                contentProviderClient.delete(uri, "_id IN (" + this.whereClause.toString() + ")", foo);
                this.whereClause.setLength(0);
                this.whereArgs.clear();
            }
        }
    }

    @UnsupportedAppUsage
    private void postscan(String[] directories) throws RemoteException {
        if (this.mProcessPlaylists) {
            processPlayLists();
        }
        this.mPlayLists.clear();
    }

    private void releaseResources() {
        DrmManagerClient drmManagerClient = this.mDrmManagerClient;
        if (drmManagerClient != null) {
            drmManagerClient.close();
            this.mDrmManagerClient = null;
        }
    }

    public void scanDirectories(String[] directories) {
        try {
            System.currentTimeMillis();
            prescan(null, true);
            System.currentTimeMillis();
            this.mMediaInserter = new MediaInserter(this.mMediaProvider, 500);
            for (String str : directories) {
                processDirectory(str, this.mClient);
            }
            this.mMediaInserter.flushAll();
            this.mMediaInserter = null;
            System.currentTimeMillis();
            postscan(directories);
            System.currentTimeMillis();
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
        } catch (Throwable th) {
            releaseResources();
            throw th;
        }
        releaseResources();
    }

    @UnsupportedAppUsage
    public Uri scanSingleFile(String path, String mimeType) {
        try {
            prescan(path, true);
            File file = new File(path);
            if (file.exists()) {
                if (file.canRead()) {
                    Uri doScanFile = this.mClient.doScanFile(path, mimeType, file.lastModified() / 1000, file.length(), false, true, isNoMediaPath(path));
                    releaseResources();
                    return doScanFile;
                }
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
            return null;
        } finally {
            releaseResources();
        }
    }

    /* access modifiers changed from: private */
    public static boolean isNoMediaFile(String path) {
        int lastSlash;
        if (!new File(path).isDirectory() && (lastSlash = path.lastIndexOf(47)) >= 0 && lastSlash + 2 < path.length()) {
            if (path.regionMatches(lastSlash + 1, "._", 0, 2)) {
                return true;
            }
            if (path.regionMatches(true, path.length() - 4, ".jpg", 0, 4)) {
                if (path.regionMatches(true, lastSlash + 1, "AlbumArt_{", 0, 10) || path.regionMatches(true, lastSlash + 1, "AlbumArt.", 0, 9)) {
                    return true;
                }
                int length = (path.length() - lastSlash) - 1;
                if ((length != 17 || !path.regionMatches(true, lastSlash + 1, "AlbumArtSmall", 0, 13)) && (length != 10 || !path.regionMatches(true, lastSlash + 1, "Folder", 0, 6))) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public static void clearMediaPathCache(boolean clearMediaPaths, boolean clearNoMediaPaths) {
        synchronized (MediaScanner.class) {
            if (clearMediaPaths) {
                try {
                    mMediaPaths.clear();
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (clearNoMediaPaths) {
                mNoMediaPaths.clear();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0072, code lost:
        return isNoMediaFile(r11);
     */
    @UnsupportedAppUsage
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
            }
            if (!mMediaPaths.containsKey(parent)) {
                int offset = 1;
                while (offset >= 0) {
                    int slashIndex = path.indexOf(47, offset);
                    if (slashIndex > offset) {
                        slashIndex++;
                        if (new File(path.substring(0, slashIndex) + MediaStore.MEDIA_IGNORE_FILENAME).exists()) {
                            mNoMediaPaths.put(parent, "");
                            return true;
                        }
                    }
                    offset = slashIndex;
                }
                mMediaPaths.put(parent, "");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00d9, code lost:
        if (r18 != null) goto L_0x00f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00ef, code lost:
        if (r18 == null) goto L_0x00f4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00f1, code lost:
        r18.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00f4, code lost:
        releaseResources();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00f8, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00fe  */
    public void scanMtpFile(String path, int objectHandle, int format) {
        int i;
        String str;
        String mimeType = MediaFile.getMimeType(path, format);
        File file = new File(path);
        long lastModifiedSeconds = file.lastModified() / 1000;
        if (MediaFile.isAudioMimeType(mimeType) || MediaFile.isVideoMimeType(mimeType) || MediaFile.isImageMimeType(mimeType) || MediaFile.isPlayListMimeType(mimeType) || MediaFile.isDrmMimeType(mimeType)) {
            this.mMtpObjectHandle = objectHandle;
            Cursor fileList = null;
            try {
                if (MediaFile.isPlayListMimeType(mimeType)) {
                    prescan(null, true);
                    FileEntry entry = makeEntryFor(path);
                    if (entry != null) {
                        Cursor fileList2 = this.mMediaProvider.query(this.mFilesUri, FILES_PRESCAN_PROJECTION, null, null, null, null);
                        try {
                            processPlayList(entry, fileList2);
                            fileList = fileList2;
                        } catch (RemoteException e) {
                            e = e;
                            fileList = fileList2;
                            i = 0;
                            str = TAG;
                            try {
                                Log.e(str, "RemoteException in MediaScanner.scanFile()", e);
                                this.mMtpObjectHandle = i;
                            } catch (Throwable th) {
                                th = th;
                                this.mMtpObjectHandle = i;
                                if (fileList != null) {
                                    fileList.close();
                                }
                                releaseResources();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            fileList = fileList2;
                            i = 0;
                            this.mMtpObjectHandle = i;
                            if (fileList != null) {
                            }
                            releaseResources();
                            throw th;
                        }
                    }
                    i = 0;
                } else {
                    prescan(path, false);
                    MyMediaScannerClient myMediaScannerClient = this.mClient;
                    long length = file.length();
                    boolean z = format == 12289;
                    boolean isNoMediaPath = isNoMediaPath(path);
                    i = 0;
                    str = TAG;
                    try {
                        myMediaScannerClient.doScanFile(path, mimeType, lastModifiedSeconds, length, z, true, isNoMediaPath);
                    } catch (RemoteException e2) {
                        e = e2;
                        Log.e(str, "RemoteException in MediaScanner.scanFile()", e);
                        this.mMtpObjectHandle = i;
                    }
                }
                this.mMtpObjectHandle = i;
            } catch (RemoteException e3) {
                e = e3;
                i = 0;
                str = TAG;
                Log.e(str, "RemoteException in MediaScanner.scanFile()", e);
                this.mMtpObjectHandle = i;
            } catch (Throwable th3) {
                th = th3;
                i = 0;
                this.mMtpObjectHandle = i;
                if (fileList != null) {
                }
                releaseResources();
                throw th;
            }
        } else {
            ContentValues values = new ContentValues();
            values.put("_size", Long.valueOf(file.length()));
            values.put("date_modified", Long.valueOf(lastModifiedSeconds));
            try {
                this.mMediaProvider.update(MediaStore.Files.getMtpObjectsUri(this.mVolumeName), values, "_id=?", new String[]{Integer.toString(objectHandle)});
            } catch (RemoteException e4) {
                Log.e(TAG, "RemoteException in scanMtpFile", e4);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public FileEntry makeEntryFor(String path) {
        Cursor c = null;
        try {
            Cursor c2 = this.mMediaProvider.query(this.mFilesFullUri, FILES_PRESCAN_PROJECTION, "_data=?", new String[]{path}, null, null);
            if (c2 != null && c2.moveToFirst()) {
                FileEntry fileEntry = new FileEntry(c2.getLong(0), path, c2.getLong(3), c2.getInt(2), c2.getInt(4));
                c2.close();
                return fileEntry;
            } else if (c2 == null) {
                return null;
            } else {
                c2.close();
                return null;
            }
        } catch (RemoteException e) {
            if (c == null) {
                return null;
            }
            c.close();
            return null;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
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
            int start12 = start1 < 0 ? 0 : start1 + 1;
            int start22 = start2 < 0 ? 0 : start2 + 1;
            int length = end1 - start12;
            if (end2 - start22 != length || !path1.regionMatches(true, start12, path2, start22, length)) {
                break;
            }
            result++;
            end1 = start12 - 1;
            end2 = start22 - 1;
        }
        return result;
    }

    private boolean matchEntries(long rowId, String data) {
        int len = this.mPlaylistEntries.size();
        boolean done = true;
        for (int i = 0; i < len; i++) {
            PlaylistEntry entry = this.mPlaylistEntries.get(i);
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

    /* access modifiers changed from: private */
    public void cachePlaylistEntry(String line, String playListDirectory) {
        PlaylistEntry entry = new PlaylistEntry();
        int entryLength = line.length();
        while (entryLength > 0 && Character.isWhitespace(line.charAt(entryLength - 1))) {
            entryLength--;
        }
        if (entryLength >= 3) {
            boolean fullPath = false;
            if (entryLength < line.length()) {
                line = line.substring(0, entryLength);
            }
            char ch1 = line.charAt(0);
            if (ch1 == '/' || (Character.isLetter(ch1) && line.charAt(1) == ':' && line.charAt(2) == '\\')) {
                fullPath = true;
            }
            if (!fullPath) {
                line = playListDirectory + line;
            }
            entry.path = line;
            this.mPlaylistEntries.add(entry);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a  */
    private void processCachedPlaylist(Cursor fileList, ContentValues values, Uri playlistUri) {
        fileList.moveToPosition(-1);
        while (fileList.moveToNext() && !matchEntries(fileList.getLong(0), fileList.getString(1))) {
            while (fileList.moveToNext()) {
                while (fileList.moveToNext()) {
                }
            }
        }
        int len = this.mPlaylistEntries.size();
        int index = 0;
        for (int i = 0; i < len; i++) {
            PlaylistEntry entry = this.mPlaylistEntries.get(i);
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

    private void processM3uPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        BufferedReader reader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                this.mPlaylistEntries.clear();
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    if (line.length() > 0 && line.charAt(0) != '#') {
                        cachePlaylistEntry(line, playListDirectory);
                    }
                }
                processCachedPlaylist(fileList, values, uri);
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e);
                }
            }
        } catch (IOException e2) {
            Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e2);
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e3);
                }
            }
            throw th;
        }
    }

    private void processPlsPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        int equals;
        BufferedReader reader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                this.mPlaylistEntries.clear();
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    if (line.startsWith("File") && (equals = line.indexOf(61)) > 0) {
                        cachePlaylistEntry(line.substring(equals + 1), playListDirectory);
                    }
                }
                processCachedPlaylist(fileList, values, uri);
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e);
                }
            }
        } catch (IOException e2) {
            Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e2);
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e3);
                }
            }
            throw th;
        }
    }

    class WplHandler implements ElementListener {
        final ContentHandler handler;
        String playListDirectory;

        public WplHandler(String playListDirectory2, Uri uri, Cursor fileList) {
            this.playListDirectory = playListDirectory2;
            RootElement root = new RootElement("smil");
            root.getChild("body").getChild("seq").getChild(MediaStore.AUTHORITY).setElementListener(this);
            this.handler = root.getContentHandler();
        }

        @Override // android.sax.StartElementListener
        public void start(Attributes attributes) {
            String path = attributes.getValue("", "src");
            if (path != null) {
                MediaScanner.this.cachePlaylistEntry(path, this.playListDirectory);
            }
        }

        @Override // android.sax.EndElementListener
        public void end() {
        }

        /* access modifiers changed from: package-private */
        public ContentHandler getContentHandler() {
            return this.handler;
        }
    }

    private void processWplPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        FileInputStream fis = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                fis = new FileInputStream(f);
                this.mPlaylistEntries.clear();
                Xml.parse(fis, Xml.findEncodingByName("UTF-8"), new WplHandler(playListDirectory, uri, fileList).getContentHandler());
                processCachedPlaylist(fileList, values, uri);
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e);
                }
            }
        } catch (SAXException e2) {
            e2.printStackTrace();
            if (fis != null) {
                fis.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (fis != null) {
                fis.close();
            }
        } catch (Throwable th) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e4) {
                    Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e4);
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00b2, code lost:
        if (r5.equals("application/vnd.ms-wpl") == false) goto L_0x00c9;
     */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00cc  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00f1  */
    private void processPlayList(FileEntry entry, Cursor fileList) throws RemoteException {
        String name;
        Uri membersUri;
        String name2;
        String path = entry.mPath;
        ContentValues values = new ContentValues();
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0) {
            long rowId = entry.mRowId;
            String name3 = values.getAsString("name");
            if (name3 == null) {
                String name4 = values.getAsString("title");
                if (name4 == null) {
                    int lastDot = path.lastIndexOf(46);
                    if (lastDot < 0) {
                        name2 = path.substring(lastSlash + 1);
                    } else {
                        name2 = path.substring(lastSlash + 1, lastDot);
                    }
                    name = name2;
                } else {
                    name = name4;
                }
            } else {
                name = name3;
            }
            values.put("name", name);
            values.put("date_modified", Long.valueOf(entry.mLastModified));
            if (rowId == 0) {
                values.put("_data", path);
                Uri uri = this.mMediaProvider.insert(this.mPlaylistsUri, values);
                ContentUris.parseId(uri);
                membersUri = Uri.withAppendedPath(uri, "members");
            } else {
                Uri uri2 = ContentUris.withAppendedId(this.mPlaylistsUri, rowId);
                this.mMediaProvider.update(uri2, values, null, null);
                Uri membersUri2 = Uri.withAppendedPath(uri2, "members");
                this.mMediaProvider.delete(membersUri2, null, null);
                membersUri = membersUri2;
            }
            char c = 0;
            String playListDirectory = path.substring(0, lastSlash + 1);
            String mimeType = MediaFile.getMimeTypeForFile(path);
            int hashCode = mimeType.hashCode();
            if (hashCode != -1165508903) {
                if (hashCode != 264230524) {
                    if (hashCode == 1872259501) {
                    }
                } else if (mimeType.equals("audio/x-mpegurl")) {
                    c = 1;
                    if (c == 0) {
                        processWplPlayList(path, playListDirectory, membersUri, values, fileList);
                        return;
                    } else if (c == 1) {
                        processM3uPlayList(path, playListDirectory, membersUri, values, fileList);
                        return;
                    } else if (c == 2) {
                        processPlsPlayList(path, playListDirectory, membersUri, values, fileList);
                        return;
                    } else {
                        return;
                    }
                }
            } else if (mimeType.equals("audio/x-scpls")) {
                c = 2;
                if (c == 0) {
                }
            }
            c = 65535;
            if (c == 0) {
            }
        } else {
            throw new IllegalArgumentException("bad path " + path);
        }
    }

    private void processPlayLists() throws RemoteException {
        Iterator<FileEntry> iterator = this.mPlayLists.iterator();
        Cursor fileList = null;
        try {
            fileList = this.mMediaProvider.query(this.mFilesUri, FILES_PRESCAN_PROJECTION, "media_type=2", null, null, null);
            while (iterator.hasNext()) {
                FileEntry entry = iterator.next();
                if (entry.mLastModifiedChanged) {
                    processPlayList(entry, fileList);
                }
            }
            if (fileList == null) {
                return;
            }
        } catch (RemoteException e) {
            if (fileList == null) {
                return;
            }
        } catch (Throwable th) {
            if (fileList != null) {
                fileList.close();
            }
            throw th;
        }
        fileList.close();
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            this.mMediaProvider.close();
            native_finalize();
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            close();
        } finally {
            super.finalize();
        }
    }
}
