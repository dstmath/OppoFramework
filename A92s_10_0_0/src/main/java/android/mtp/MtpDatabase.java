package android.mtp;

import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.mtp.MtpStorageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.WindowManager;
import com.android.internal.annotations.VisibleForNative;
import com.google.android.collect.Sets;
import dalvik.system.CloseGuard;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class MtpDatabase implements AutoCloseable {
    private static final int[] AUDIO_PROPERTIES = {MtpConstants.PROPERTY_ARTIST, MtpConstants.PROPERTY_ALBUM_NAME, MtpConstants.PROPERTY_ALBUM_ARTIST, MtpConstants.PROPERTY_TRACK, MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE, MtpConstants.PROPERTY_DURATION, MtpConstants.PROPERTY_COMPOSER, MtpConstants.PROPERTY_AUDIO_WAVE_CODEC, MtpConstants.PROPERTY_BITRATE_TYPE, MtpConstants.PROPERTY_AUDIO_BITRATE, MtpConstants.PROPERTY_NUMBER_OF_CHANNELS, MtpConstants.PROPERTY_SAMPLE_RATE};
    /* access modifiers changed from: private */
    public static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int[] DEVICE_PROPERTIES = {MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER, MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME, MtpConstants.DEVICE_PROPERTY_IMAGE_SIZE, MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL, MtpConstants.DEVICE_PROPERTY_PERCEIVED_DEVICE_TYPE};
    private static final int EXIT_MESSAGE = 2;
    private static final int[] FILE_PROPERTIES = {MtpConstants.PROPERTY_STORAGE_ID, MtpConstants.PROPERTY_OBJECT_FORMAT, MtpConstants.PROPERTY_PROTECTION_STATUS, MtpConstants.PROPERTY_OBJECT_SIZE, MtpConstants.PROPERTY_OBJECT_FILE_NAME, MtpConstants.PROPERTY_DATE_MODIFIED, MtpConstants.PROPERTY_PERSISTENT_UID, MtpConstants.PROPERTY_PARENT_OBJECT, MtpConstants.PROPERTY_NAME, MtpConstants.PROPERTY_DISPLAY_NAME, MtpConstants.PROPERTY_DATE_ADDED};
    private static final int HANDLE_MESSAGE = 1;
    private static final String[] ID_PROJECTION = {"_id"};
    private static final int[] IMAGE_PROPERTIES = {56392};
    private static final int INIT_MESSAGE = 0;
    private static final String NO_MEDIA = ".nomedia";
    private static final String[] PATH_PROJECTION = {"_data"};
    private static final String PATH_WHERE = "_data=?";
    private static final int[] PLAYBACK_FORMATS = {12288, 12289, 12292, 12293, 12296, 12297, 12299, MtpConstants.FORMAT_EXIF_JPEG, MtpConstants.FORMAT_TIFF_EP, MtpConstants.FORMAT_BMP, MtpConstants.FORMAT_GIF, MtpConstants.FORMAT_JFIF, MtpConstants.FORMAT_PNG, MtpConstants.FORMAT_TIFF, MtpConstants.FORMAT_WMA, MtpConstants.FORMAT_OGG, MtpConstants.FORMAT_AAC, MtpConstants.FORMAT_MP4_CONTAINER, MtpConstants.FORMAT_MP2, MtpConstants.FORMAT_3GP_CONTAINER, MtpConstants.FORMAT_ABSTRACT_AV_PLAYLIST, MtpConstants.FORMAT_WPL_PLAYLIST, MtpConstants.FORMAT_M3U_PLAYLIST, MtpConstants.FORMAT_PLS_PLAYLIST, MtpConstants.FORMAT_XML_DOCUMENT, MtpConstants.FORMAT_FLAC, MtpConstants.FORMAT_DNG, MtpConstants.FORMAT_HEIF};
    /* access modifiers changed from: private */
    public static final String TAG = MtpDatabase.class.getSimpleName();
    private static final int[] VIDEO_PROPERTIES = {MtpConstants.PROPERTY_ARTIST, MtpConstants.PROPERTY_ALBUM_NAME, MtpConstants.PROPERTY_DURATION, MtpConstants.PROPERTY_DESCRIPTION};
    /* access modifiers changed from: private */
    public int mBatteryLevel;
    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        /* class android.mtp.MtpDatabase.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                int unused = MtpDatabase.this.mBatteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                int newLevel = intent.getIntExtra("level", 0);
                if (newLevel != MtpDatabase.this.mBatteryLevel) {
                    int unused2 = MtpDatabase.this.mBatteryLevel = newLevel;
                    if (MtpDatabase.this.mServer != null) {
                        MtpDatabase.this.mServer.sendDevicePropertyChanged(MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL);
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mBatteryScale;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    private final Context mContext;
    private SharedPreferences mDeviceProperties;
    private int mDeviceType;
    /* access modifiers changed from: private */
    public final ScanHandler mHandler;
    /* access modifiers changed from: private */
    public final HandlerThread mHandlerThread;
    private MtpStorageManager mManager;
    private final ContentProviderClient mMediaProvider;
    @VisibleForNative
    private long mNativeContext;
    private final SparseArray<MtpPropertyGroup> mPropertyGroupsByFormat = new SparseArray<>();
    private final SparseArray<MtpPropertyGroup> mPropertyGroupsByProperty = new SparseArray<>();
    /* access modifiers changed from: private */
    public MtpServer mServer;
    private final HashMap<String, MtpStorage> mStorageMap = new HashMap<>();

    private final native void native_finalize();

    private final native void native_setup();

    static {
        System.loadLibrary("media_jni");
    }

    @VisibleForNative
    private int[] getSupportedObjectProperties(int format) {
        if (!(format == 12296 || format == 12297)) {
            if (format != 12299) {
                if (!(format == 14337 || format == 14340 || format == 14343 || format == 14347)) {
                    if (!(format == 47489 || format == 47492)) {
                        if (!(format == 14353 || format == 14354)) {
                            switch (format) {
                                case MtpConstants.FORMAT_WMA:
                                case MtpConstants.FORMAT_OGG:
                                case MtpConstants.FORMAT_AAC:
                                    break;
                                default:
                                    return FILE_PROPERTIES;
                            }
                        }
                    }
                }
                return IntStream.concat(Arrays.stream(FILE_PROPERTIES), Arrays.stream(IMAGE_PROPERTIES)).toArray();
            }
            return IntStream.concat(Arrays.stream(FILE_PROPERTIES), Arrays.stream(VIDEO_PROPERTIES)).toArray();
        }
        return IntStream.concat(Arrays.stream(FILE_PROPERTIES), Arrays.stream(AUDIO_PROPERTIES)).toArray();
    }

    public static Uri getObjectPropertiesUri(int format, String volumeName) {
        if (!(format == 12296 || format == 12297)) {
            if (format != 12299) {
                if (!(format == 14337 || format == 14340 || format == 14343 || format == 14347)) {
                    if (!(format == 47489 || format == 47492)) {
                        if (!(format == 14353 || format == 14354)) {
                            switch (format) {
                                case MtpConstants.FORMAT_WMA:
                                case MtpConstants.FORMAT_OGG:
                                case MtpConstants.FORMAT_AAC:
                                    break;
                                default:
                                    return MediaStore.Files.getContentUri(volumeName);
                            }
                        }
                    }
                }
                return MediaStore.Images.Media.getContentUri(volumeName);
            }
            return MediaStore.Video.Media.getContentUri(volumeName);
        }
        return MediaStore.Audio.Media.getContentUri(volumeName);
    }

    @VisibleForNative
    private int[] getSupportedDeviceProperties() {
        return DEVICE_PROPERTIES;
    }

    @VisibleForNative
    private int[] getSupportedPlaybackFormats() {
        return PLAYBACK_FORMATS;
    }

    @VisibleForNative
    private int[] getSupportedCaptureFormats() {
        return null;
    }

    public MtpDatabase(Context context, String[] subDirectories) {
        native_setup();
        this.mContext = (Context) Objects.requireNonNull(context);
        this.mMediaProvider = context.getContentResolver().acquireContentProviderClient(MediaStore.AUTHORITY);
        this.mHandlerThread = new HandlerThread(TAG, 10);
        this.mHandlerThread.start();
        this.mHandler = new ScanHandler(this.mHandlerThread.getLooper(), this.mContext);
        this.mManager = new MtpStorageManager(new MtpStorageManager.MtpNotifier() {
            /* class android.mtp.MtpDatabase.AnonymousClass2 */

            @Override // android.mtp.MtpStorageManager.MtpNotifier
            public void sendObjectAdded(int id) {
                if (MtpDatabase.this.mServer != null) {
                    MtpDatabase.this.mServer.sendObjectAdded(id);
                }
            }

            @Override // android.mtp.MtpStorageManager.MtpNotifier
            public void sendObjectRemoved(int id) {
                if (MtpDatabase.this.mServer != null) {
                    MtpDatabase.this.mServer.sendObjectRemoved(id);
                }
            }

            @Override // android.mtp.MtpStorageManager.MtpNotifier
            public void sendObjectInfoChanged(int id) {
                if (MtpDatabase.this.mServer != null) {
                    MtpDatabase.this.mServer.sendObjectInfoChanged(id);
                }
            }
        }, subDirectories == null ? null : Sets.newHashSet(subDirectories));
        initDeviceProperties(context);
        this.mDeviceType = SystemProperties.getInt("sys.usb.mtp.device_type", 0);
        this.mCloseGuard.open("close");
    }

    public void setServer(MtpServer server) {
        this.mServer = server;
        try {
            this.mContext.unregisterReceiver(this.mBatteryReceiver);
        } catch (IllegalArgumentException e) {
        }
        if (server != null) {
            this.mContext.registerReceiver(this.mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        this.mManager.close();
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            ContentProviderClient contentProviderClient = this.mMediaProvider;
            if (contentProviderClient != null) {
                contentProviderClient.close();
            }
            native_finalize();
        }
    }

    public void releaseScanThread() {
        this.mHandler.sendEmptyMessage(2);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            close();
            super.finalize();
            HandlerThread handlerThread = this.mHandlerThread;
            if (handlerThread != null) {
                handlerThread.quitSafely();
            }
        } catch (Throwable th) {
            super.finalize();
            throw th;
        }
    }

    public void addStorage(StorageVolume storage) {
        MtpStorage mtpStorage = this.mManager.addMtpStorage(storage);
        this.mStorageMap.put(storage.getPath(), mtpStorage);
        MtpServer mtpServer = this.mServer;
        if (mtpServer != null) {
            mtpServer.addStorage(mtpStorage);
        }
    }

    public void removeStorage(StorageVolume storage) {
        MtpStorage mtpStorage = this.mStorageMap.get(storage.getPath());
        if (mtpStorage != null) {
            MtpServer mtpServer = this.mServer;
            if (mtpServer != null) {
                mtpServer.removeStorage(mtpStorage);
            }
            this.mManager.removeMtpStorage(mtpStorage);
            this.mStorageMap.remove(storage.getPath());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0061, code lost:
        if (r6 != null) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0063, code lost:
        r6.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0077, code lost:
        if (r6 != null) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007a, code lost:
        r17.deleteDatabase("device-properties");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    private void initDeviceProperties(Context context) {
        this.mDeviceProperties = context.getSharedPreferences("device-properties", 0);
        if (context.getDatabasePath("device-properties").exists()) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = context.openOrCreateDatabase("device-properties", 0, null);
                if (!(db == null || (c = db.query("properties", new String[]{"_id", "code", "value"}, null, null, null, null, null)) == null)) {
                    SharedPreferences.Editor e = this.mDeviceProperties.edit();
                    while (c.moveToNext()) {
                        e.putString(c.getString(1), c.getString(2));
                    }
                    e.commit();
                }
                if (c != null) {
                    c.close();
                }
            } catch (Exception e2) {
                Log.e(TAG, "failed to migrate device properties", e2);
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
                if (db != null) {
                    db.close();
                }
                throw th;
            }
        }
    }

    @VisibleForNative
    private int beginSendObject(String path, int format, int parent, int storageId) {
        MtpStorageManager mtpStorageManager = this.mManager;
        MtpStorageManager.MtpObject parentObj = parent == 0 ? mtpStorageManager.getStorageRoot(storageId) : mtpStorageManager.getObject(parent);
        if (parentObj == null) {
            return -1;
        }
        return this.mManager.beginSendObject(parentObj, Paths.get(path, new String[0]).getFileName().toString(), format);
    }

    @VisibleForNative
    private void endSendObject(int handle, boolean succeeded) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null || !this.mManager.endSendObject(obj, succeeded)) {
            Log.e(TAG, "Failed to successfully end send object");
        } else if (succeeded) {
            MediaStore.scanFile(this.mContext, obj.getPath().toFile());
        }
    }

    class HandlerParams {
        String path;

        HandlerParams(String path2) {
            this.path = path2;
        }
    }

    /* access modifiers changed from: package-private */
    public class ScanHandler extends Handler {
        private final Context mContext;
        private ArrayList<HandlerParams> mPendingInstalls = new ArrayList<>();

        ScanHandler(Looper looper, Context context) {
            super(looper);
            this.mContext = context;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                doHandleMessage(msg);
            } finally {
                Process.setThreadPriority(10);
            }
        }

        private void doHandleMessage(Message msg) {
            HandlerParams params;
            int i = msg.what;
            if (i == 0) {
                int idx = this.mPendingInstalls.size();
                this.mPendingInstalls.add(idx, (HandlerParams) msg.obj);
                if (idx == 0) {
                    MtpDatabase.this.mHandler.sendEmptyMessage(1);
                }
            } else if (i != 1) {
                if (i == 2) {
                    if (this.mPendingInstalls.size() != 0) {
                        if (MtpDatabase.DEBUG) {
                            Log.d(MtpDatabase.TAG, "sendEmptyMessageDelayed EXIT_MESSAGE");
                        }
                        MtpDatabase.this.mHandler.sendEmptyMessageDelayed(2, 500);
                    } else if (MtpDatabase.this.mHandlerThread != null) {
                        boolean quitsafe = MtpDatabase.this.mHandlerThread.quitSafely();
                        if (MtpDatabase.DEBUG) {
                            String access$600 = MtpDatabase.TAG;
                            Log.d(access$600, "mHandlerThread.quitSafely ?" + quitsafe);
                        }
                    }
                }
            } else if (this.mPendingInstalls.size() > 0 && (params = this.mPendingInstalls.get(0)) != null) {
                MediaStore.scanFile(this.mContext, new File(params.path));
                if (this.mPendingInstalls.size() > 0) {
                    this.mPendingInstalls.remove(0);
                }
                if (this.mPendingInstalls.size() != 0) {
                    MtpDatabase.this.mHandler.sendEmptyMessage(1);
                }
            }
        }
    }

    @VisibleForNative
    private void rescanFile(String path, int handle, int format) {
        Message msg = this.mHandler.obtainMessage(0);
        msg.obj = new HandlerParams(path);
        this.mHandler.sendMessage(msg);
        String str = TAG;
        Log.e(str, "rescanFile sendMessage path" + path);
    }

    @VisibleForNative
    private int[] getObjectList(int storageID, int format, int parent) {
        List<MtpStorageManager.MtpObject> objs = this.mManager.getObjects(parent, format, storageID);
        if (objs == null) {
            return null;
        }
        int[] ret = new int[objs.size()];
        for (int i = 0; i < objs.size(); i++) {
            ret[i] = objs.get(i).getId();
        }
        return ret;
    }

    @VisibleForNative
    private int getNumObjects(int storageID, int format, int parent) {
        List<MtpStorageManager.MtpObject> objs = this.mManager.getObjects(parent, format, storageID);
        if (objs == null) {
            return -1;
        }
        return objs.size();
    }

    @VisibleForNative
    private MtpPropertyList getObjectPropertyList(int handle, int format, int property, int groupCode, int depth) {
        MtpPropertyGroup propertyGroup;
        int handle2 = handle;
        int format2 = format;
        if (property != 0) {
            int err = -1;
            int depth2 = depth;
            if (depth2 == -1 && (handle2 == 0 || handle2 == -1)) {
                handle2 = -1;
                depth2 = 0;
            }
            if (depth2 != 0 && depth2 != 1) {
                return new MtpPropertyList(MtpConstants.RESPONSE_SPECIFICATION_BY_DEPTH_UNSUPPORTED);
            }
            List<MtpStorageManager.MtpObject> objs = null;
            MtpStorageManager.MtpObject thisObj = null;
            if (handle2 == -1) {
                objs = this.mManager.getObjects(0, format2, -1);
                if (objs == null) {
                    return new MtpPropertyList(8201);
                }
            } else if (handle2 != 0) {
                MtpStorageManager.MtpObject obj = this.mManager.getObject(handle2);
                if (obj == null) {
                    return new MtpPropertyList(8201);
                }
                if (obj.getFormat() == format2 || format2 == 0) {
                    thisObj = obj;
                }
            }
            if (handle2 == 0 || depth2 == 1) {
                if (handle2 == 0) {
                    handle2 = -1;
                }
                objs = this.mManager.getObjects(handle2, format2, -1);
                if (objs == null) {
                    return new MtpPropertyList(8201);
                }
            }
            if (objs == null) {
                objs = new ArrayList<>();
            }
            if (thisObj != null) {
                objs.add(thisObj);
            }
            MtpPropertyList ret = new MtpPropertyList(8193);
            for (MtpStorageManager.MtpObject obj2 : objs) {
                if (property == err) {
                    if (!(format2 != 0 || handle2 == 0 || handle2 == err)) {
                        format2 = obj2.getFormat();
                    }
                    propertyGroup = this.mPropertyGroupsByFormat.get(format2);
                    if (propertyGroup == null) {
                        propertyGroup = new MtpPropertyGroup(getSupportedObjectProperties(format2));
                        this.mPropertyGroupsByFormat.put(format2, propertyGroup);
                    }
                } else {
                    propertyGroup = this.mPropertyGroupsByProperty.get(property);
                    if (propertyGroup == null) {
                        propertyGroup = new MtpPropertyGroup(new int[]{property});
                        this.mPropertyGroupsByProperty.put(property, propertyGroup);
                    }
                }
                int err2 = propertyGroup.getPropertyList(this.mMediaProvider, obj2.getVolumeName(), obj2, ret);
                if (err2 != 8193) {
                    return new MtpPropertyList(err2);
                }
                err = -1;
            }
            return ret;
        } else if (groupCode == 0) {
            return new MtpPropertyList(8198);
        } else {
            return new MtpPropertyList(MtpConstants.RESPONSE_SPECIFICATION_BY_GROUP_UNSUPPORTED);
        }
    }

    private int renameFile(int handle, String newName) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return 8201;
        }
        Path oldPath = obj.getPath();
        if (!this.mManager.beginRenameObject(obj, newName)) {
            return 8194;
        }
        Path newPath = obj.getPath();
        boolean success = oldPath.toFile().renameTo(newPath.toFile());
        try {
            Os.access(oldPath.toString(), OsConstants.F_OK);
            Os.access(newPath.toString(), OsConstants.F_OK);
        } catch (ErrnoException e) {
        }
        if (!this.mManager.endRenameObject(obj, oldPath.getFileName().toString(), success)) {
            Log.e(TAG, "Failed to end rename object");
        }
        if (!success) {
            return 8194;
        }
        ContentValues values = new ContentValues();
        values.put("_data", newPath.toString());
        String[] whereArgs = {oldPath.toString()};
        try {
            this.mMediaProvider.update(MediaStore.Files.getMtpObjectsUri(obj.getVolumeName()), values, PATH_WHERE, whereArgs);
        } catch (RemoteException e2) {
            Log.e(TAG, "RemoteException in mMediaProvider.update", e2);
        }
        if (obj.isDir()) {
            if (!oldPath.getFileName().startsWith(".") || newPath.startsWith(".")) {
                return 8193;
            }
            MediaStore.scanFile(this.mContext, newPath.toFile());
            return 8193;
        } else if (!oldPath.getFileName().toString().toLowerCase(Locale.US).equals(".nomedia") || newPath.getFileName().toString().toLowerCase(Locale.US).equals(".nomedia")) {
            return 8193;
        } else {
            MediaStore.scanFile(this.mContext, newPath.getParent().toFile());
            return 8193;
        }
    }

    @VisibleForNative
    private int beginMoveObject(int handle, int newParent, int newStorage) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        MtpStorageManager.MtpObject parent = newParent == 0 ? this.mManager.getStorageRoot(newStorage) : this.mManager.getObject(newParent);
        if (obj == null || parent == null) {
            return 8201;
        }
        return this.mManager.beginMoveObject(obj, parent) ? 8193 : 8194;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.content.ContentValues.put(java.lang.String, java.lang.Integer):void
     arg types: [java.lang.String, int]
     candidates:
      android.content.ContentValues.put(java.lang.String, java.lang.Boolean):void
      android.content.ContentValues.put(java.lang.String, java.lang.Byte):void
      android.content.ContentValues.put(java.lang.String, java.lang.Double):void
      android.content.ContentValues.put(java.lang.String, java.lang.Float):void
      android.content.ContentValues.put(java.lang.String, java.lang.Long):void
      android.content.ContentValues.put(java.lang.String, java.lang.Short):void
      android.content.ContentValues.put(java.lang.String, java.lang.String):void
      android.content.ContentValues.put(java.lang.String, byte[]):void
      android.content.ContentValues.put(java.lang.String, java.lang.Integer):void */
    @VisibleForNative
    private void endMoveObject(int oldParent, int newParent, int oldStorage, int newStorage, int objId, boolean success) {
        MtpStorageManager.MtpObject oldParentObj = oldParent == 0 ? this.mManager.getStorageRoot(oldStorage) : this.mManager.getObject(oldParent);
        MtpStorageManager.MtpObject newParentObj = newParent == 0 ? this.mManager.getStorageRoot(newStorage) : this.mManager.getObject(newParent);
        String name = this.mManager.getObject(objId).getName();
        if (newParentObj == null || oldParentObj == null || !this.mManager.endMoveObject(oldParentObj, newParentObj, name, success)) {
            Log.e(TAG, "Failed to end move object");
            return;
        }
        MtpStorageManager.MtpObject obj = this.mManager.getObject(objId);
        if (success && obj != null) {
            ContentValues values = new ContentValues();
            Path path = newParentObj.getPath().resolve(name);
            Path oldPath = oldParentObj.getPath().resolve(name);
            values.put("_data", path.toString());
            if (obj.getParent().isRoot()) {
                values.put("parent", (Integer) 0);
            } else {
                int parentId = findInMedia(newParentObj, path.getParent());
                if (parentId != -1) {
                    values.put("parent", Integer.valueOf(parentId));
                } else {
                    deleteFromMedia(obj, oldPath, obj.isDir());
                    return;
                }
            }
            String[] whereArgs = {oldPath.toString()};
            int parentId2 = -1;
            try {
                if (!oldParentObj.isRoot()) {
                    try {
                        parentId2 = findInMedia(oldParentObj, oldPath.getParent());
                    } catch (RemoteException e) {
                        e = e;
                    }
                }
                if (!oldParentObj.isRoot()) {
                    if (parentId2 == -1) {
                        try {
                            MediaStore.scanFile(this.mContext, path.toFile());
                            return;
                        } catch (RemoteException e2) {
                            e = e2;
                            Log.e(TAG, "RemoteException in mMediaProvider.update", e);
                        }
                    }
                }
                this.mMediaProvider.update(MediaStore.Files.getMtpObjectsUri(obj.getVolumeName()), values, PATH_WHERE, whereArgs);
            } catch (RemoteException e3) {
                e = e3;
                Log.e(TAG, "RemoteException in mMediaProvider.update", e);
            }
        }
    }

    @VisibleForNative
    private int beginCopyObject(int handle, int newParent, int newStorage) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        MtpStorageManager.MtpObject parent = newParent == 0 ? this.mManager.getStorageRoot(newStorage) : this.mManager.getObject(newParent);
        if (obj == null || parent == null) {
            return 8201;
        }
        return this.mManager.beginCopyObject(obj, parent);
    }

    @VisibleForNative
    private void endCopyObject(int handle, boolean success) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null || !this.mManager.endCopyObject(obj, success)) {
            Log.e(TAG, "Failed to end copy object");
        } else if (success) {
            MediaStore.scanFile(this.mContext, obj.getPath().toFile());
        }
    }

    @VisibleForNative
    private int setObjectProperty(int handle, int property, long intValue, String stringValue) {
        if (property != 56327) {
            return MtpConstants.RESPONSE_OBJECT_PROP_NOT_SUPPORTED;
        }
        return renameFile(handle, stringValue);
    }

    @VisibleForNative
    private int getDeviceProperty(int property, long[] outIntValue, char[] outStringValue) {
        switch (property) {
            case MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL:
                outIntValue[0] = (long) this.mBatteryLevel;
                outIntValue[1] = (long) this.mBatteryScale;
                return 8193;
            case MtpConstants.DEVICE_PROPERTY_IMAGE_SIZE:
                Display display = ((WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                int width = display.getMaximumSizeDimension();
                int height = display.getMaximumSizeDimension();
                String imageSize = Integer.toString(width) + "x" + Integer.toString(height);
                imageSize.getChars(0, imageSize.length(), outStringValue, 0);
                outStringValue[imageSize.length()] = 0;
                return 8193;
            case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER:
            case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME:
                String value = this.mDeviceProperties.getString(Integer.toString(property), "");
                int length = value.length();
                if (length > 255) {
                    length = 255;
                }
                value.getChars(0, length, outStringValue, 0);
                outStringValue[length] = 0;
                if (length > 0) {
                    Log.i(TAG, "getDeviceProperty  property = " + Integer.toHexString(property));
                    Log.i(TAG, "getDeviceProperty  value = " + value + ", length = " + length);
                    return 8193;
                }
                Log.i(TAG, "getDeviceProperty  length = " + length);
                if (property == 54274) {
                    String deviceName = SystemProperties.get("ro.oppo.market.name", "OPPO MTP Device");
                    int lengthDeviceName = deviceName.length();
                    if (lengthDeviceName > 255) {
                        lengthDeviceName = 255;
                    }
                    if (lengthDeviceName > 0) {
                        deviceName.getChars(0, lengthDeviceName, outStringValue, 0);
                        outStringValue[lengthDeviceName] = 0;
                        Log.d(TAG, "getDeviceProperty  deviceName = " + deviceName + ", lengthDeviceName = " + lengthDeviceName);
                    } else {
                        Log.d(TAG, "getDeviceProperty  lengthDeviceName = " + lengthDeviceName);
                    }
                }
                return 8193;
            case MtpConstants.DEVICE_PROPERTY_PERCEIVED_DEVICE_TYPE:
                outIntValue[0] = (long) this.mDeviceType;
                return 8193;
            default:
                return 8202;
        }
    }

    @VisibleForNative
    private int setDeviceProperty(int property, long intValue, String stringValue) {
        switch (property) {
            case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER:
            case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME:
                SharedPreferences.Editor e = this.mDeviceProperties.edit();
                e.putString(Integer.toString(property), stringValue);
                if (e.commit()) {
                    return 8193;
                }
                return 8194;
            default:
                return 8202;
        }
    }

    @VisibleForNative
    private boolean getObjectInfo(int handle, int[] outStorageFormatParent, char[] outName, long[] outCreatedModified) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return false;
        }
        outStorageFormatParent[0] = obj.getStorageId();
        outStorageFormatParent[1] = obj.getFormat();
        outStorageFormatParent[2] = obj.getParent().isRoot() ? 0 : obj.getParent().getId();
        int nameLen = Integer.min(obj.getName().length(), 255);
        obj.getName().getChars(0, nameLen, outName, 0);
        outName[nameLen] = 0;
        outCreatedModified[0] = obj.getModifiedTime();
        outCreatedModified[1] = obj.getModifiedTime();
        return true;
    }

    @VisibleForNative
    private int getObjectFilePath(int handle, char[] outFilePath, long[] outFileLengthFormat) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return 8201;
        }
        String path = obj.getPath().toString();
        int pathLen = Integer.min(path.length(), 4096);
        path.getChars(0, pathLen, outFilePath, 0);
        outFilePath[pathLen] = 0;
        outFileLengthFormat[0] = obj.getSize();
        outFileLengthFormat[1] = (long) obj.getFormat();
        return 8193;
    }

    private int getObjectFormat(int handle) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return -1;
        }
        return obj.getFormat();
    }

    @VisibleForNative
    private int beginDeleteObject(int handle) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return 8201;
        }
        if (!this.mManager.beginRemoveObject(obj)) {
            return 8194;
        }
        return 8193;
    }

    @VisibleForNative
    private void endDeleteObject(int handle, boolean success) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj != null) {
            if (!this.mManager.endRemoveObject(obj, success)) {
                Log.e(TAG, "Failed to end remove object");
            }
            if (success) {
                deleteFromMedia(obj, obj.getPath(), obj.isDir());
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0054, code lost:
        if (r9 == null) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0057, code lost:
        return r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x002f, code lost:
        if (r9 != null) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0031, code lost:
        r9.close();
     */
    private int findInMedia(MtpStorageManager.MtpObject obj, Path path) {
        Uri objectsUri = MediaStore.Files.getMtpObjectsUri(obj.getVolumeName());
        int ret = -1;
        Cursor c = null;
        try {
            c = this.mMediaProvider.query(objectsUri, ID_PROJECTION, PATH_WHERE, new String[]{path.toString()}, null, null);
            if (c != null && c.moveToNext()) {
                ret = c.getInt(0);
            }
        } catch (RemoteException e) {
            String str = TAG;
            Log.e(str, "Error finding " + path + " in MediaProvider");
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    private void deleteFromMedia(MtpStorageManager.MtpObject obj, Path path, boolean isDir) {
        Uri objectsUri = MediaStore.Files.getMtpObjectsUri(obj.getVolumeName());
        if (isDir) {
            try {
                ContentProviderClient contentProviderClient = this.mMediaProvider;
                contentProviderClient.delete(objectsUri, "_data LIKE ?1 AND lower(substr(_data,1,?2))=lower(?3)", new String[]{path + "/%", Integer.toString(path.toString().length() + 1), path.toString() + "/"});
            } catch (Exception e) {
                String str = TAG;
                Log.d(str, "Failed to delete " + path + " from MediaProvider");
                return;
            }
        }
        if (this.mMediaProvider.delete(objectsUri, PATH_WHERE, new String[]{path.toString()}) <= 0) {
            String str2 = TAG;
            Log.i(str2, "Mediaprovider didn't delete " + path);
        } else if (!isDir && path.toString().toLowerCase(Locale.US).endsWith(".nomedia")) {
            MediaStore.scanFile(this.mContext, path.getParent().toFile());
        }
    }

    @VisibleForNative
    private int[] getObjectReferences(int handle) {
        int handle2;
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null || (handle2 = findInMedia(obj, obj.getPath())) == -1) {
            return null;
        }
        Cursor c = null;
        try {
            Cursor c2 = this.mMediaProvider.query(MediaStore.Files.getMtpReferencesUri(obj.getVolumeName(), (long) handle2), PATH_PROJECTION, null, null, null, null);
            if (c2 == null) {
                if (c2 != null) {
                    c2.close();
                }
                return null;
            }
            ArrayList<Integer> result = new ArrayList<>();
            while (c2.moveToNext()) {
                MtpStorageManager.MtpObject refObj = this.mManager.getByPath(c2.getString(0));
                if (refObj != null) {
                    result.add(Integer.valueOf(refObj.getId()));
                }
            }
            int[] array = result.stream().mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
            c2.close();
            return array;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectList", e);
            if (c != null) {
                c.close();
            }
            return null;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    @VisibleForNative
    private int setObjectReferences(int handle, int[] references) {
        int refHandle;
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return 8201;
        }
        int handle2 = findInMedia(obj, obj.getPath());
        int i = -1;
        if (handle2 == -1) {
            return 8194;
        }
        Uri uri = MediaStore.Files.getMtpReferencesUri(obj.getVolumeName(), (long) handle2);
        ArrayList<ContentValues> valuesList = new ArrayList<>();
        int length = references.length;
        int i2 = 0;
        while (i2 < length) {
            MtpStorageManager.MtpObject refObj = this.mManager.getObject(references[i2]);
            if (!(refObj == null || (refHandle = findInMedia(refObj, refObj.getPath())) == i)) {
                ContentValues values = new ContentValues();
                values.put("_id", Integer.valueOf(refHandle));
                valuesList.add(values);
            }
            i2++;
            i = -1;
        }
        try {
            if (this.mMediaProvider.bulkInsert(uri, (ContentValues[]) valuesList.toArray(new ContentValues[0])) > 0) {
                return 8193;
            }
            return 8194;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in setObjectReferences", e);
        }
    }
}
