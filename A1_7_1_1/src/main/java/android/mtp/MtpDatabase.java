package android.mtp;

import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScanner;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.oppo.media.MediaScannerCallback;
import com.oppo.os.OppoUsbEnvironment;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

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
public class MtpDatabase implements AutoCloseable {
    static final int[] AUDIO_PROPERTIES = null;
    private static final boolean DEBUG = false;
    private static final int DEVICE_PROPERTIES_DATABASE_VERSION = 1;
    private static final int EXIT_MESSAGE = 2;
    static final int[] FILE_PROPERTIES = null;
    private static final String FORMAT_PARENT_WHERE = "format=? AND parent=?";
    private static final String[] FORMAT_PROJECTION = null;
    private static final String FORMAT_WHERE = "format=?";
    private static final int HANDLE_MESSAGE = 1;
    private static final String[] ID_PROJECTION = null;
    private static final String ID_WHERE = "_id=?";
    static final int[] IMAGE_PROPERTIES = null;
    private static final int INIT_MESSAGE = 0;
    private static final String[] OBJECT_INFO_PROJECTION = null;
    private static final String PARENT_WHERE = "parent=?";
    private static final String[] PATH_FORMAT_PROJECTION = null;
    private static final String[] PATH_PROJECTION = null;
    private static final String PATH_WHERE = "_data=?";
    private static final boolean PTP_NO_DELETE_DIRS = true;
    private static final String STORAGE_FORMAT_PARENT_WHERE = "storage_id=? AND format=? AND parent=?";
    private static final String STORAGE_FORMAT_WHERE = "storage_id=? AND format=?";
    private static final boolean STORAGE_MERGE = false;
    private static final String STORAGE_PARENT_WHERE = "storage_id=? AND parent=?";
    private static final String STORAGE_WHERE = "storage_id=?";
    private static final String TAG = "MtpDatabase";
    static final int[] VIDEO_PROPERTIES = null;
    private int mBatteryLevel;
    private BroadcastReceiver mBatteryReceiver;
    private int mBatteryScale;
    private final CloseGuard mCloseGuard;
    private final AtomicBoolean mClosed;
    private final Context mContext;
    private boolean mDatabaseModified;
    private SharedPreferences mDeviceProperties;
    private String[] mExternalSubDirectories;
    private final ScanHandler mHandler;
    private final HandlerThread mHandlerThread;
    private final ContentProviderClient mMediaProvider;
    private final MediaScanner mMediaScanner;
    private MediaScannerCallback mMediaScannerCallback;
    private final String mMediaStoragePath;
    private long mNativeContext;
    private final Uri mObjectsUri;
    private final String mPackageName;
    private final HashMap<Integer, MtpPropertyGroup> mPropertyGroupsByFormat;
    private final HashMap<Integer, MtpPropertyGroup> mPropertyGroupsByProperty;
    private HashMap<String, String> mRenameMap;
    private String mRootDirectoriesWhere;
    private MtpServer mServer;
    private final HashMap<String, MtpStorage> mStorageMap;
    private final String[] mSubDirectories;
    private String[] mSubDirectoriesName;
    private String mSubDirectoriesWhere;
    private String[] mSubDirectoriesWhereArgs;
    private String[] mSubRelativePath;
    private final String mVolumeName;

    /* renamed from: android.mtp.MtpDatabase$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ MtpDatabase this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.mtp.MtpDatabase.1.<init>(android.mtp.MtpDatabase):void, dex: 
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
        AnonymousClass1(android.mtp.MtpDatabase r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.mtp.MtpDatabase.1.<init>(android.mtp.MtpDatabase):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDatabase.1.<init>(android.mtp.MtpDatabase):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.mtp.MtpDatabase.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.mtp.MtpDatabase.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDatabase.1.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    class HandlerParams {
        int format;
        int handle;
        String path;
        final /* synthetic */ MtpDatabase this$0;
        String volumeName;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.mtp.MtpDatabase.HandlerParams.<init>(android.mtp.MtpDatabase, java.lang.String, java.lang.String, int, int):void, dex: 
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
        HandlerParams(android.mtp.MtpDatabase r1, java.lang.String r2, java.lang.String r3, int r4, int r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.mtp.MtpDatabase.HandlerParams.<init>(android.mtp.MtpDatabase, java.lang.String, java.lang.String, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDatabase.HandlerParams.<init>(android.mtp.MtpDatabase, java.lang.String, java.lang.String, int, int):void");
        }
    }

    class ScanHandler extends Handler {
        private MediaScanner mMediaScanner;
        private ArrayList<HandlerParams> mPendingInstalls;
        final /* synthetic */ MtpDatabase this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.mtp.MtpDatabase.ScanHandler.<init>(android.mtp.MtpDatabase, android.os.Looper, android.media.MediaScanner):void, dex: 
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
        ScanHandler(android.mtp.MtpDatabase r1, android.os.Looper r2, android.media.MediaScanner r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.mtp.MtpDatabase.ScanHandler.<init>(android.mtp.MtpDatabase, android.os.Looper, android.media.MediaScanner):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDatabase.ScanHandler.<init>(android.mtp.MtpDatabase, android.os.Looper, android.media.MediaScanner):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.mtp.MtpDatabase.ScanHandler.doHandleMessage(android.os.Message):void, dex: 
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
        private void doHandleMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.mtp.MtpDatabase.ScanHandler.doHandleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDatabase.ScanHandler.doHandleMessage(android.os.Message):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.mtp.MtpDatabase.ScanHandler.handleMessage(android.os.Message):void, dex: 
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
        public void handleMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.mtp.MtpDatabase.ScanHandler.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDatabase.ScanHandler.handleMessage(android.os.Message):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.mtp.MtpDatabase.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.mtp.MtpDatabase.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDatabase.<clinit>():void");
    }

    private final native void native_finalize();

    private final native void native_setup();

    public MtpDatabase(Context context, String volumeName, String storagePath, String[] subDirectories) {
        this(context, volumeName, storagePath, subDirectories, null);
    }

    public MtpDatabase(Context context, String volumeName, String storagePath, String[] subDirectories, MediaScannerCallback callback) {
        this(context, volumeName, storagePath, subDirectories, null, callback);
    }

    public MtpDatabase(Context context, String volumeName, String storagePath, String[] subDirectories, String[] subDirectoriesName, MediaScannerCallback callback) {
        this.mClosed = new AtomicBoolean();
        this.mCloseGuard = CloseGuard.get();
        this.mSubRelativePath = null;
        this.mStorageMap = new HashMap();
        this.mPropertyGroupsByProperty = new HashMap();
        this.mPropertyGroupsByFormat = new HashMap();
        this.mBatteryReceiver = new AnonymousClass1(this);
        native_setup();
        this.mContext = context;
        this.mPackageName = context.getPackageName();
        this.mMediaProvider = context.getContentResolver().acquireContentProviderClient(MediaStore.AUTHORITY);
        this.mVolumeName = volumeName;
        this.mMediaStoragePath = storagePath;
        this.mObjectsUri = Files.getMtpObjectsUri(volumeName);
        this.mMediaScannerCallback = callback;
        if (this.mMediaScannerCallback == null) {
            this.mMediaScanner = new MediaScanner(context, this.mVolumeName);
        } else {
            this.mMediaScanner = null;
        }
        this.mHandlerThread = new HandlerThread(TAG, 10);
        this.mHandlerThread.start();
        this.mHandler = new ScanHandler(this, this.mHandlerThread.getLooper(), this.mMediaScanner);
        this.mSubDirectories = subDirectories;
        if (subDirectories != null) {
            int i;
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            this.mSubRelativePath = new String[subDirectories.length];
            this.mExternalSubDirectories = new String[subDirectories.length];
            this.mSubDirectoriesName = subDirectoriesName;
            String internalPath = OppoUsbEnvironment.getInternalPath(this.mContext);
            String externalPath = OppoUsbEnvironment.getExternalPath(this.mContext);
            for (i = 0; i < count; i++) {
                builder.append("_data=? OR _data LIKE ?");
                if (i != count - 1) {
                    builder.append(" OR ");
                }
                this.mSubRelativePath[i] = this.mSubDirectories[i].substring(internalPath.length() + 1);
                this.mExternalSubDirectories[i] = externalPath + "/" + this.mSubRelativePath[i];
            }
            builder.append(")");
            this.mSubDirectoriesWhere = builder.toString();
            StringBuilder builder1 = new StringBuilder();
            builder1.append("(");
            for (i = 0; i < count; i++) {
                builder1.append(PATH_WHERE);
                if (i != count - 1) {
                    builder1.append(" OR ");
                }
            }
            builder1.append(")");
            this.mRootDirectoriesWhere = builder1.toString();
            this.mSubDirectoriesWhereArgs = new String[(count * 2)];
            int j = 0;
            for (String path : subDirectories) {
                int i2 = j + 1;
                this.mSubDirectoriesWhereArgs[j] = path;
                j = i2 + 1;
                this.mSubDirectoriesWhereArgs[i2] = path + "/%";
            }
        }
        initDeviceProperties(context);
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

    public void close() {
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            if (this.mMediaScanner != null) {
                this.mMediaScanner.close();
            }
            this.mMediaProvider.close();
            native_finalize();
        }
    }

    public void releaseScanThread() {
        this.mHandler.sendEmptyMessage(2);
    }

    protected void finalize() throws Throwable {
        try {
            this.mCloseGuard.warnIfOpen();
            close();
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quitSafely();
            }
            if (this.mMediaScannerCallback != null) {
                this.mMediaScannerCallback = null;
            }
        } finally {
            super.finalize();
        }
    }

    public void addStorage(MtpStorage storage) {
        this.mStorageMap.put(storage.getPath(), storage);
    }

    public void removeStorage(MtpStorage storage) {
        this.mStorageMap.remove(storage.getPath());
    }

    private void initDeviceProperties(Context context) {
        String devicePropertiesName = "device-properties";
        this.mDeviceProperties = context.getSharedPreferences("device-properties", 0);
        if (context.getDatabasePath("device-properties").exists()) {
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                db = context.openOrCreateDatabase("device-properties", 0, null);
                if (db != null) {
                    String[] strArr = new String[3];
                    strArr[0] = "_id";
                    strArr[1] = "code";
                    strArr[2] = "value";
                    cursor = db.query("properties", strArr, null, null, null, null, null);
                    if (cursor != null) {
                        Editor e = this.mDeviceProperties.edit();
                        while (cursor.moveToNext()) {
                            e.putString(cursor.getString(1), cursor.getString(2));
                        }
                        e.commit();
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception e2) {
                Log.e(TAG, "failed to migrate device properties", e2);
                context.deleteDatabase("device-properties");
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            }
            context.deleteDatabase("device-properties");
        }
    }

    private boolean inStorageSubDirectory(String path) {
        if (this.mSubDirectories == null) {
            return true;
        }
        if (path == null) {
            return false;
        }
        boolean allowed = false;
        int pathLength = path.length();
        for (int i = 0; i < this.mSubDirectories.length && !allowed; i++) {
            String subdir = this.mSubDirectories[i];
            int subdirLength = subdir.length();
            if (subdirLength < pathLength && path.charAt(subdirLength) == '/' && path.startsWith(subdir)) {
                allowed = true;
            }
        }
        if (allowed) {
            allowed = true;
        } else {
            allowed = inSubDir(path);
        }
        return allowed;
    }

    private boolean isStorageSubDirectory(String path) {
        if (this.mSubDirectories == null) {
            return false;
        }
        int i = 0;
        while (i < this.mSubDirectories.length) {
            if (path != null && path.toLowerCase(Locale.US).equals(this.mSubDirectories[i].toLowerCase(Locale.US))) {
                return true;
            }
            i++;
        }
        if (this.mExternalSubDirectories == null) {
            return false;
        }
        i = 0;
        while (i < this.mExternalSubDirectories.length) {
            if (path != null && path.toLowerCase(Locale.US).equals(this.mExternalSubDirectories[i].toLowerCase(Locale.US))) {
                return true;
            }
            i++;
        }
        return false;
    }

    private boolean inStorageRoot(String path) {
        try {
            String canonical = new File(path).getCanonicalPath();
            for (String root : this.mStorageMap.keySet()) {
                if (canonical.startsWith(root)) {
                    return true;
                }
            }
        } catch (IOException e) {
        }
        return false;
    }

    private int beginSendObject(String path, int format, int parent, int storageId, long size, long modified) {
        if (DEBUG) {
            Log.d(TAG, "beginSendObject: path = " + path + ", parent = " + parent + ", storageId = " + storageId);
        }
        if (inStorageRoot(path)) {
            String parentDir = null;
            if (path != null) {
                parentDir = path.substring(0, path.lastIndexOf(File.separator));
                if (DEBUG) {
                    Log.d(TAG, "beginSendObject: parentDir = " + parentDir);
                }
            }
            if (!isStorageSubDirectory(parentDir) || new File(parentDir).exists()) {
                if (path != null) {
                    Cursor cursor = null;
                    try {
                        ContentProviderClient contentProviderClient = this.mMediaProvider;
                        Uri uri = this.mObjectsUri;
                        String[] strArr = ID_PROJECTION;
                        String str = PATH_WHERE;
                        String[] strArr2 = new String[1];
                        strArr2[0] = path;
                        cursor = contentProviderClient.query(uri, strArr, str, strArr2, null, null);
                        if (cursor != null && cursor.getCount() > 0) {
                            Log.w(TAG, "file already exists in beginSendObject: " + path);
                            if (cursor != null) {
                                cursor.close();
                            }
                            return -1;
                        } else if (cursor != null) {
                            cursor.close();
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "RemoteException in beginSendObject", e);
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                this.mDatabaseModified = true;
                ContentValues values = new ContentValues();
                values.put("_data", path);
                values.put(FileColumns.FORMAT, Integer.valueOf(format));
                values.put("parent", Integer.valueOf(parent));
                values.put(FileColumns.STORAGE_ID, Integer.valueOf(storageId));
                values.put("_size", Long.valueOf(size));
                values.put("date_modified", Long.valueOf(modified));
                try {
                    Uri uri2 = this.mMediaProvider.insert(this.mObjectsUri, values);
                    if (uri2 != null) {
                        return Integer.parseInt((String) uri2.getPathSegments().get(2));
                    }
                    return -1;
                } catch (RemoteException e2) {
                    Log.e(TAG, "RemoteException in beginSendObject", e2);
                    return -1;
                }
            }
            Log.e(TAG, "attempt to put file in the sub directory : " + path);
            return -1;
        }
        Log.e(TAG, "attempt to put file outside of storage area: " + path);
        return -1;
    }

    private void endSendObject(String path, int handle, int format, boolean succeeded) {
        if (DEBUG) {
            Log.d(TAG, "endSendObject: path = " + path + ", succeeded = " + succeeded);
        }
        if (!succeeded) {
            deleteFile(handle);
        } else if (format == 47621) {
            String name = path;
            int lastSlash = path.lastIndexOf(47);
            if (lastSlash >= 0) {
                name = path.substring(lastSlash + 1);
            }
            if (name.endsWith(".pla")) {
                name = name.substring(0, name.length() - 4);
            }
            ContentValues values = new ContentValues(1);
            values.put("_data", path);
            values.put("name", name);
            values.put(FileColumns.FORMAT, Integer.valueOf(format));
            values.put("date_modified", Long.valueOf(System.currentTimeMillis() / 1000));
            values.put(MediaColumns.MEDIA_SCANNER_NEW_OBJECT_ID, Integer.valueOf(handle));
            try {
                Uri insert = this.mMediaProvider.insert(Playlists.EXTERNAL_CONTENT_URI, values);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in endSendObject", e);
            }
        } else {
            Message msg = this.mHandler.obtainMessage(0);
            msg.obj = new HandlerParams(this, path, this.mVolumeName, handle, format);
            this.mHandler.sendMessage(msg);
            if (DEBUG) {
                Log.e(TAG, "endSendObject sendMessage path" + path);
            }
        }
    }

    private Cursor createObjectQuery(int storageID, int format, int parent) throws RemoteException {
        String where;
        String[] whereArgs;
        int i;
        int count;
        if (DEBUG) {
            Log.d(TAG, "createObjectQuery: storageID = " + storageID + ", format = " + format + ", parent = " + parent);
        }
        String storagePath = OppoUsbEnvironment.getExternalPath(this.mContext);
        MtpStorage externalMtpStorage = (MtpStorage) this.mStorageMap.get(storagePath);
        int externalStorageId = -1;
        if (externalMtpStorage != null) {
            externalStorageId = externalMtpStorage.getStorageId();
        }
        if (DEBUG) {
            Log.d(TAG, "externalStoragePath = " + storagePath + ", storageID = " + storageID + ", externalStorageId = " + externalStorageId);
        }
        if (storageID == -1) {
            if (format == 0) {
                if (parent == 0) {
                    where = null;
                    whereArgs = null;
                } else {
                    if (parent == -1) {
                        parent = 0;
                    }
                    where = PARENT_WHERE;
                    whereArgs = new String[1];
                    whereArgs[0] = Integer.toString(parent);
                }
            } else if (parent == 0) {
                where = FORMAT_WHERE;
                whereArgs = new String[1];
                whereArgs[0] = Integer.toString(format);
            } else {
                if (parent == -1) {
                    parent = 0;
                }
                where = FORMAT_PARENT_WHERE;
                whereArgs = new String[2];
                whereArgs[0] = Integer.toString(format);
                whereArgs[1] = Integer.toString(parent);
            }
        } else if (format == 0) {
            if (parent == 0) {
                where = STORAGE_WHERE;
                whereArgs = new String[1];
                whereArgs[0] = Integer.toString(storageID);
            } else {
                if (parent == -1) {
                    parent = 0;
                }
                where = STORAGE_PARENT_WHERE;
                whereArgs = new String[2];
                whereArgs[0] = Integer.toString(storageID);
                whereArgs[1] = Integer.toString(parent);
            }
        } else if (parent == 0) {
            where = STORAGE_FORMAT_WHERE;
            whereArgs = new String[2];
            whereArgs[0] = Integer.toString(storageID);
            whereArgs[1] = Integer.toString(format);
        } else {
            if (parent == -1) {
                parent = 0;
            }
            where = STORAGE_FORMAT_PARENT_WHERE;
            whereArgs = new String[3];
            whereArgs[0] = Integer.toString(storageID);
            whereArgs[1] = Integer.toString(format);
            whereArgs[2] = Integer.toString(parent);
        }
        if (this.mSubDirectoriesWhere != null) {
            if (where == null) {
                where = this.mSubDirectoriesWhere;
                whereArgs = this.mSubDirectoriesWhereArgs;
            } else {
                where = where + " AND " + this.mSubDirectoriesWhere;
                String[] newWhereArgs = new String[(whereArgs.length + this.mSubDirectoriesWhereArgs.length)];
                i = 0;
                while (i < whereArgs.length) {
                    newWhereArgs[i] = whereArgs[i];
                    i++;
                }
                for (String str : this.mSubDirectoriesWhereArgs) {
                    newWhereArgs[i] = str;
                    i++;
                }
                whereArgs = newWhereArgs;
                if (externalMtpStorage != null && storageID == externalStorageId) {
                    count = newWhereArgs.length;
                    try {
                        int subDirectoriesWhereArgsLength = this.mSubDirectoriesWhereArgs.length;
                        if (DEBUG) {
                            Log.d(TAG, "subDirectoriesWhereArgsLength = " + subDirectoriesWhereArgsLength + ", count = " + count);
                        }
                        for (int k = count - subDirectoriesWhereArgsLength; k < count; k++) {
                            int m = k - (count - subDirectoriesWhereArgsLength);
                            if (m % 2 == 0) {
                                newWhereArgs[k] = storagePath + "/" + this.mSubRelativePath[m / 2];
                            } else {
                                newWhereArgs[k] = storagePath + "/" + this.mSubRelativePath[m / 2] + "/%";
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (parent == 0) {
                if (DEBUG) {
                    Log.d(TAG, "createObjectQuery: mObjectsUri = " + this.mObjectsUri + ", parent = 0" + ", mRootDirectoriesWhere = " + this.mRootDirectoriesWhere);
                }
                String[] selection = this.mSubDirectories;
                if (externalMtpStorage != null && storageID == externalStorageId) {
                    selection = this.mExternalSubDirectories;
                }
                return this.mMediaProvider.query(this.mObjectsUri, ID_PROJECTION, this.mRootDirectoriesWhere, selection, null, null);
            }
        }
        String whereArgsString = "";
        StringBuilder builder = new StringBuilder();
        count = whereArgs.length;
        for (i = 0; i < count; i++) {
            builder.append(whereArgs[i]);
            if (i != count - 1) {
                builder.append(", ");
            }
        }
        whereArgsString = builder.toString();
        if (DEBUG) {
            Log.d(TAG, "createObjectQuery: mObjectsUri = " + this.mObjectsUri + ", where = " + where + ", whereArgs = " + whereArgsString);
        }
        return this.mMediaProvider.query(this.mObjectsUri, ID_PROJECTION, where, whereArgs, null, null);
    }

    private int[] getObjectList(int storageID, int format, int parent) {
        Cursor cursor = null;
        try {
            cursor = createObjectQuery(storageID, format, parent);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            int count = cursor.getCount();
            if (DEBUG) {
                Log.d(TAG, "getObjectList: storageID = " + storageID + ", count = " + count);
            }
            if (count > 0) {
                int[] result = new int[count];
                for (int i = 0; i < count; i++) {
                    cursor.moveToNext();
                    result[i] = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
                return result;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectList", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int getNumObjects(int storageID, int format, int parent) {
        Cursor cursor = null;
        try {
            cursor = createObjectQuery(storageID, format, parent);
            if (cursor != null) {
                if (DEBUG) {
                    Log.d(TAG, "getNumObjects: storageID = " + storageID + ", c.getCount = " + cursor.getCount());
                }
                int count = cursor.getCount();
                if (cursor != null) {
                    cursor.close();
                }
                return count;
            }
            if (cursor != null) {
                cursor.close();
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getNumObjects", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int[] getSupportedPlaybackFormats() {
        return new int[]{12288, 12289, 12292, 12293, 12296, 12297, 12299, MtpConstants.FORMAT_EXIF_JPEG, MtpConstants.FORMAT_TIFF_EP, MtpConstants.FORMAT_BMP, MtpConstants.FORMAT_GIF, MtpConstants.FORMAT_JFIF, MtpConstants.FORMAT_PNG, MtpConstants.FORMAT_TIFF, MtpConstants.FORMAT_WMA, MtpConstants.FORMAT_OGG, MtpConstants.FORMAT_AAC, MtpConstants.FORMAT_MP4_CONTAINER, MtpConstants.FORMAT_MP2, MtpConstants.FORMAT_3GP_CONTAINER, MtpConstants.FORMAT_ABSTRACT_AV_PLAYLIST, MtpConstants.FORMAT_WPL_PLAYLIST, MtpConstants.FORMAT_M3U_PLAYLIST, MtpConstants.FORMAT_PLS_PLAYLIST, MtpConstants.FORMAT_XML_DOCUMENT, MtpConstants.FORMAT_FLAC, MtpConstants.FORMAT_DNG};
    }

    private int[] getSupportedCaptureFormats() {
        return null;
    }

    private int[] getSupportedObjectProperties(int format) {
        switch (format) {
            case 12296:
            case 12297:
            case MtpConstants.FORMAT_WMA /*47361*/:
            case MtpConstants.FORMAT_OGG /*47362*/:
            case MtpConstants.FORMAT_AAC /*47363*/:
                return AUDIO_PROPERTIES;
            case 12299:
            case MtpConstants.FORMAT_WMV /*47489*/:
            case MtpConstants.FORMAT_3GP_CONTAINER /*47492*/:
                return VIDEO_PROPERTIES;
            case MtpConstants.FORMAT_EXIF_JPEG /*14337*/:
            case MtpConstants.FORMAT_BMP /*14340*/:
            case MtpConstants.FORMAT_GIF /*14343*/:
            case MtpConstants.FORMAT_PNG /*14347*/:
            case MtpConstants.FORMAT_DNG /*14353*/:
                return IMAGE_PROPERTIES;
            default:
                return FILE_PROPERTIES;
        }
    }

    private int[] getSupportedDeviceProperties() {
        return new int[]{MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER, MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME, MtpConstants.DEVICE_PROPERTY_IMAGE_SIZE, MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL};
    }

    private MtpPropertyList getObjectPropertyList(int handle, int format, int property, int groupCode, int depth) {
        if (DEBUG) {
            Log.d(TAG, "getObjectPropertyList: handle = 0x" + Long.toHexString((long) handle) + ", property = 0x" + Long.toHexString((long) property));
        }
        if (groupCode != 0) {
            Log.i(TAG, "getObjectPropertyList RESPONSE_SPECIFICATION_BY_GROUP_UNSUPPORTED");
            return new MtpPropertyList(0, MtpConstants.RESPONSE_SPECIFICATION_BY_GROUP_UNSUPPORTED);
        }
        MtpPropertyGroup propertyGroup;
        if (property == -1) {
            if (!(format != 0 || handle == 0 || handle == -1)) {
                format = getObjectFormat(handle);
            }
            propertyGroup = (MtpPropertyGroup) this.mPropertyGroupsByFormat.get(Integer.valueOf(format));
            if (propertyGroup == null) {
                propertyGroup = new MtpPropertyGroup(this, this.mMediaProvider, this.mVolumeName, getSupportedObjectProperties(format));
                this.mPropertyGroupsByFormat.put(Integer.valueOf(format), propertyGroup);
            }
        } else {
            propertyGroup = (MtpPropertyGroup) this.mPropertyGroupsByProperty.get(Integer.valueOf(property));
            if (propertyGroup == null) {
                int[] propertyList = new int[1];
                propertyList[0] = property;
                propertyGroup = new MtpPropertyGroup(this, this.mMediaProvider, this.mVolumeName, propertyList);
                this.mPropertyGroupsByProperty.put(Integer.valueOf(property), propertyGroup);
            }
        }
        return propertyGroup.getPropertyList(handle, format, depth);
    }

    private int renameFile(int handle, String newName) {
        Cursor cursor = null;
        String path = null;
        String[] whereArgs = new String[1];
        whereArgs[0] = Integer.toString(handle);
        try {
            cursor = this.mMediaProvider.query(this.mObjectsUri, PATH_PROJECTION, ID_WHERE, whereArgs, null, null);
            if (cursor != null && cursor.moveToNext()) {
                path = cursor.getString(1);
            }
            if (cursor != null) {
                cursor.close();
            }
            if (path == null) {
                return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
            }
            if (isStorageSubDirectory(path)) {
                return MtpConstants.RESPONSE_OBJECT_WRITE_PROTECTED;
            }
            if (this.mRenameMap != null && this.mRenameMap.containsKey(path.toLowerCase(Locale.US))) {
                return MtpConstants.RESPONSE_OBJECT_WRITE_PROTECTED;
            }
            File oldFile = new File(path);
            int lastSlash = path.lastIndexOf(47);
            if (lastSlash <= 1) {
                return 8194;
            }
            String newPath = path.substring(0, lastSlash + 1) + newName;
            File newFile = new File(newPath);
            if (oldFile.renameTo(newFile)) {
                ContentValues values = new ContentValues();
                values.put("_data", newPath);
                int updated = 0;
                try {
                    updated = this.mMediaProvider.update(this.mObjectsUri, values, ID_WHERE, whereArgs);
                } catch (Exception e) {
                    Log.e(TAG, "RemoteException in mMediaProvider.update", e);
                }
                if (updated == 0) {
                    Log.e(TAG, "Unable to update path for " + path + " to " + newPath);
                    newFile.renameTo(oldFile);
                    return 8194;
                }
                if (newFile.isDirectory()) {
                    if (oldFile.getName().startsWith(".") && !newPath.startsWith(".")) {
                        try {
                            this.mMediaProvider.call(MediaStore.UNHIDE_CALL, newPath, null);
                        } catch (RemoteException e2) {
                            Log.e(TAG, "failed to unhide/rescan for " + newPath);
                        }
                    }
                } else if (oldFile.getName().toLowerCase(Locale.US).equals(MediaStore.MEDIA_IGNORE_FILENAME) && !newPath.toLowerCase(Locale.US).equals(MediaStore.MEDIA_IGNORE_FILENAME)) {
                    try {
                        this.mMediaProvider.call(MediaStore.UNHIDE_CALL, oldFile.getParent(), null);
                    } catch (RemoteException e3) {
                        Log.e(TAG, "failed to unhide/rescan for " + newPath);
                    }
                }
                return MtpConstants.RESPONSE_OK;
            }
            Log.w(TAG, "renaming " + path + " to " + newPath + " failed");
            return 8194;
        } catch (RemoteException e4) {
            Log.e(TAG, "RemoteException in getObjectFilePath", e4);
            if (cursor != null) {
                cursor.close();
            }
            return 8194;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private int setObjectProperty(int handle, int property, long intValue, String stringValue) {
        switch (property) {
            case MtpConstants.PROPERTY_OBJECT_FILE_NAME /*56327*/:
                return renameFile(handle, stringValue);
            default:
                return MtpConstants.RESPONSE_OBJECT_PROP_NOT_SUPPORTED;
        }
    }

    private int getDeviceProperty(int property, long[] outIntValue, char[] outStringValue) {
        if (DEBUG) {
            Log.d(TAG, "getDeviceProperty  property = 0x" + Integer.toHexString(property));
        }
        switch (property) {
            case MtpConstants.DEVICE_PROPERTY_IMAGE_SIZE /*20483*/:
                Display display = ((WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                String imageSize = Integer.toString(display.getMaximumSizeDimension()) + "x" + Integer.toString(display.getMaximumSizeDimension());
                imageSize.getChars(0, imageSize.length(), outStringValue, 0);
                outStringValue[imageSize.length()] = 0;
                return MtpConstants.RESPONSE_OK;
            case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER /*54273*/:
            case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME /*54274*/:
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
                    return MtpConstants.RESPONSE_OK;
                }
                Log.i(TAG, "getDeviceProperty  length = " + length);
                if (property == 54274) {
                    String deviceName;
                    boolean isExpVersion = !SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("CN");
                    Log.d(TAG, "getDeviceProperty isExpVersion=" + isExpVersion);
                    String model;
                    String isConfidentialVersion;
                    Object[] objArr;
                    if (isExpVersion) {
                        model = SystemProperties.get("ro.oppo.market.name");
                        Log.d(TAG, "getDeviceProperty  3333 model=" + model);
                        isConfidentialVersion = SystemProperties.get("persist.version.confidential");
                        if (model.startsWith("OPPO ") || isConfidentialVersion.equals("true")) {
                            deviceName = model;
                        } else {
                            objArr = new Object[2];
                            objArr[0] = SystemProperties.get("ro.product.manufacturer");
                            objArr[1] = SystemProperties.get("ro.oppo.market.name");
                            deviceName = String.format("%s %s", objArr);
                        }
                    } else {
                        model = SystemProperties.get("ro.oppo.market.name", "MTP Device");
                        if (model.equals("MTP Device")) {
                            model = SystemProperties.get("ro.product.model", "MTP Device");
                            Log.d(TAG, "getDeviceMarketName ro.product.model = " + model);
                        }
                        isConfidentialVersion = SystemProperties.get("persist.version.confidential");
                        if (model.startsWith("OPPO ") || isConfidentialVersion.equals("true")) {
                            deviceName = model;
                        } else {
                            objArr = new Object[2];
                            objArr[0] = SystemProperties.get("ro.product.manufacturer");
                            objArr[1] = model;
                            deviceName = String.format("%s %s", objArr);
                        }
                    }
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
                return MtpConstants.RESPONSE_OK;
            default:
                return MtpConstants.RESPONSE_DEVICE_PROP_NOT_SUPPORTED;
        }
    }

    private int setDeviceProperty(int property, long intValue, String stringValue) {
        switch (property) {
            case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER /*54273*/:
            case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME /*54274*/:
                int i;
                Editor e = this.mDeviceProperties.edit();
                e.putString(Integer.toString(property), stringValue);
                if (e.commit()) {
                    i = MtpConstants.RESPONSE_OK;
                } else {
                    i = 8194;
                }
                return i;
            default:
                return MtpConstants.RESPONSE_DEVICE_PROP_NOT_SUPPORTED;
        }
    }

    private boolean getObjectInfo(int handle, int[] outStorageFormatParent, char[] outName, long[] outCreatedModified) {
        Cursor cursor = null;
        if (DEBUG) {
            Log.d(TAG, "getObjectInfo");
        }
        try {
            ContentProviderClient contentProviderClient = this.mMediaProvider;
            Uri uri = this.mObjectsUri;
            String[] strArr = OBJECT_INFO_PROJECTION;
            String str = ID_WHERE;
            String[] strArr2 = new String[1];
            strArr2[0] = Integer.toString(handle);
            cursor = contentProviderClient.query(uri, strArr, str, strArr2, null, null);
            if (cursor == null || !cursor.moveToNext()) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            outStorageFormatParent[0] = cursor.getInt(1);
            outStorageFormatParent[1] = cursor.getInt(2);
            outStorageFormatParent[2] = cursor.getInt(3);
            String path = cursor.getString(4);
            int lastSlash = path.lastIndexOf(47);
            int start = lastSlash >= 0 ? lastSlash + 1 : 0;
            int end = path.length();
            if (end - start > 255) {
                end = start + 255;
            }
            path.getChars(start, end, outName, 0);
            outName[end - start] = 0;
            outCreatedModified[0] = cursor.getLong(5);
            outCreatedModified[1] = cursor.getLong(6);
            if (outCreatedModified[0] == 0) {
                outCreatedModified[0] = outCreatedModified[1];
            }
            if (cursor != null) {
                cursor.close();
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectInfo", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int getObjectFilePath(int handle, char[] outFilePath, long[] outFileLengthFormat) {
        if (DEBUG) {
            Log.d(TAG, "getObjectFilePath handle = " + Integer.toHexString(handle));
        }
        if (handle == 0) {
            this.mMediaStoragePath.getChars(0, this.mMediaStoragePath.length(), outFilePath, 0);
            outFilePath[this.mMediaStoragePath.length()] = 0;
            outFileLengthFormat[0] = 0;
            outFileLengthFormat[1] = 12289;
            return MtpConstants.RESPONSE_OK;
        }
        Cursor c = null;
        try {
            ContentProviderClient contentProviderClient = this.mMediaProvider;
            Uri uri = this.mObjectsUri;
            String[] strArr = PATH_FORMAT_PROJECTION;
            String str = ID_WHERE;
            String[] strArr2 = new String[1];
            strArr2[0] = Integer.toString(handle);
            c = contentProviderClient.query(uri, strArr, str, strArr2, null, null);
            if (c == null || !c.moveToNext()) {
                Log.e(TAG, "getObjectFilePath RESPONSE_INVALID_OBJECT_HANDLE, handle = " + handle);
                if (c != null) {
                    c.close();
                }
                return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
            }
            String path = c.getString(1);
            path.getChars(0, path.length(), outFilePath, 0);
            outFilePath[path.length()] = 0;
            outFileLengthFormat[0] = new File(path).length();
            outFileLengthFormat[1] = c.getLong(2);
            if (DEBUG) {
                Log.d(TAG, "getObjectFilePath RESPONSE_OK: path = " + path);
            }
            if (c != null) {
                c.close();
            }
            return MtpConstants.RESPONSE_OK;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectFilePath", e);
            if (c != null) {
                c.close();
            }
            return 8194;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    private int getObjectFormat(int handle) {
        Cursor cursor = null;
        try {
            ContentProviderClient contentProviderClient = this.mMediaProvider;
            Uri uri = this.mObjectsUri;
            String[] strArr = FORMAT_PROJECTION;
            String str = ID_WHERE;
            String[] strArr2 = new String[1];
            strArr2[0] = Integer.toString(handle);
            cursor = contentProviderClient.query(uri, strArr, str, strArr2, null, null);
            if (cursor == null || !cursor.moveToNext()) {
                if (cursor != null) {
                    cursor.close();
                }
                return -1;
            }
            int i = cursor.getInt(1);
            if (cursor != null) {
                cursor.close();
            }
            return i;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectFilePath", e);
            if (cursor != null) {
                cursor.close();
            }
            return -1;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private int deleteFile(int handle) {
        this.mDatabaseModified = true;
        if (DEBUG) {
            Log.d(TAG, "deleteFile: handle = 0x" + Integer.toHexString(handle));
        }
        Cursor c = null;
        try {
            ContentProviderClient contentProviderClient = this.mMediaProvider;
            Uri uri = this.mObjectsUri;
            String[] strArr = PATH_FORMAT_PROJECTION;
            String str = ID_WHERE;
            String[] strArr2 = new String[1];
            strArr2[0] = Integer.toString(handle);
            c = contentProviderClient.query(uri, strArr, str, strArr2, null, null);
            if (c == null || !c.moveToNext()) {
                if (c != null) {
                    c.close();
                }
                return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
            }
            String path = c.getString(1);
            int format = c.getInt(2);
            if (path == null || format == 0) {
                if (c != null) {
                    c.close();
                }
                return 8194;
            } else if (this.mSubDirectories != null && this.mSubDirectories.length > 0 && path != null && new File(path).isDirectory()) {
                if (c != null) {
                    c.close();
                }
                return MtpConstants.RESPONSE_OBJECT_WRITE_PROTECTED;
            } else if (isStorageSubDirectory(path)) {
                if (c != null) {
                    c.close();
                }
                return MtpConstants.RESPONSE_OBJECT_WRITE_PROTECTED;
            } else {
                if (format == 12289) {
                    Uri uri2 = Files.getMtpObjectsUri(this.mVolumeName);
                    strArr = new String[3];
                    strArr[0] = path + "/%";
                    strArr[1] = Integer.toString(path.length() + 1);
                    strArr[2] = path + "/";
                    int delete = this.mMediaProvider.delete(uri2, "_data LIKE ?1 AND lower(substr(_data,1,?2))=lower(?3)", strArr);
                }
                if (this.mMediaProvider.delete(Files.getMtpObjectsUri(this.mVolumeName, (long) handle), null, null) > 0) {
                    if (format != 12289 && path.toLowerCase(Locale.US).endsWith("/.nomedia")) {
                        try {
                            this.mMediaProvider.call(MediaStore.UNHIDE_CALL, path.substring(0, path.lastIndexOf("/")), null);
                        } catch (RemoteException e) {
                            Log.e(TAG, "failed to unhide/rescan for " + path);
                        }
                    }
                    if (c != null) {
                        c.close();
                    }
                    return MtpConstants.RESPONSE_OK;
                }
                if (c != null) {
                    c.close();
                }
                return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "RemoteException in deleteFile", e2);
            if (c != null) {
                c.close();
            }
            return 8194;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    private int[] getObjectReferences(int handle) {
        Uri uri = Files.getMtpReferencesUri(this.mVolumeName, (long) handle);
        Cursor cursor = null;
        try {
            cursor = this.mMediaProvider.query(uri, ID_PROJECTION, null, null, null, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            int count = cursor.getCount();
            if (DEBUG) {
                Log.d(TAG, "getObjectReferences: uri = " + uri + ", count = " + count);
            }
            if (count > 0) {
                int[] result = new int[count];
                for (int i = 0; i < count; i++) {
                    cursor.moveToNext();
                    result[i] = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
                return result;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectList", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int setObjectReferences(int handle, int[] references) {
        this.mDatabaseModified = true;
        Uri uri = Files.getMtpReferencesUri(this.mVolumeName, (long) handle);
        int count = references.length;
        ContentValues[] valuesList = new ContentValues[count];
        for (int i = 0; i < count; i++) {
            ContentValues values = new ContentValues();
            values.put("_id", Integer.valueOf(references[i]));
            valuesList[i] = values;
        }
        try {
            if (this.mMediaProvider.bulkInsert(uri, valuesList) > 0) {
                return MtpConstants.RESPONSE_OK;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in setObjectReferences", e);
        }
        return 8194;
    }

    private void sessionStarted() {
        if (DEBUG) {
            Log.d(TAG, "sessionStarted");
        }
        this.mDatabaseModified = false;
    }

    private void sessionEnded() {
        if (DEBUG) {
            Log.d(TAG, "sessionEnded, mDatabaseModified = " + this.mDatabaseModified);
        }
        if (this.mDatabaseModified) {
            this.mContext.sendBroadcast(new Intent(MediaStore.ACTION_MTP_SESSION_END));
            this.mDatabaseModified = false;
        }
    }

    private boolean inSubDir(String path) {
        if (DEBUG) {
            Log.d(TAG, "inSubDir: path = " + path);
        }
        if (path == null) {
            return false;
        }
        int i;
        boolean res = false;
        int count = this.mSubDirectories.length;
        String[] lSubDirs = new String[(count * 2)];
        String tmpPath = OppoUsbEnvironment.getExternalPath(this.mContext);
        for (i = 0; i < count; i++) {
            lSubDirs[i] = tmpPath + "/" + this.mSubRelativePath[i];
        }
        tmpPath = OppoUsbEnvironment.getInternalPath(this.mContext);
        for (i = count; i < count * 2; i++) {
            lSubDirs[i] = tmpPath + "/" + this.mSubRelativePath[i - count];
        }
        int pathLength = path.length();
        for (i = 0; i < lSubDirs.length && !res; i++) {
            String subdir = lSubDirs[i];
            int subdirLength = subdir.length();
            if (subdirLength < pathLength && path.charAt(subdirLength) == '/' && path.toLowerCase(Locale.US).startsWith(subdir.toLowerCase(Locale.US))) {
                res = true;
            }
        }
        return res;
    }

    public String[] getPtpDirectories() {
        return this.mSubDirectories;
    }

    public String[] getPtpDirectoriesName() {
        return this.mSubDirectoriesName;
    }

    public HashMap<String, String> getRenameMap() {
        return this.mRenameMap;
    }

    public void setRenameMap(HashMap<String, String> renameMap) {
        this.mRenameMap = renameMap;
    }
}
