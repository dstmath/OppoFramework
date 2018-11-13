package com.android.server;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.UserManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.oppo.IElsaManager;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

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
class LockSettingsStorage {
    private static final String BASE_ZERO_LOCK_PATTERN_FILE = "gatekeeper.gesture.key";
    private static final String CHILD_PROFILE_LOCK_FILE = "gatekeeper.profile.key";
    private static final String[] COLUMNS_FOR_PREFETCH = null;
    private static final String[] COLUMNS_FOR_QUERY = null;
    private static final String COLUMN_KEY = "name";
    private static final String COLUMN_USERID = "user";
    private static final String COLUMN_VALUE = "value";
    private static final boolean DEBUG = false;
    private static final Object DEFAULT = null;
    private static final String LEGACY_LOCK_PASSWORD_FILE = "password.key";
    private static final String LEGACY_LOCK_PATTERN_FILE = "gesture.key";
    private static final String LOCK_PASSWORD_FILE = "gatekeeper.password.key";
    private static final String LOCK_PATTERN_FILE = "gatekeeper.pattern.key";
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String TABLE = "locksettings";
    private static final String TAG = "LockSettingsStorage";
    private final Cache mCache;
    private final Context mContext;
    private final Object mFileWriteLock;
    private final DatabaseHelper mOpenHelper;
    private SparseArray<Integer> mStoredCredentialType;

    public interface Callback {
        void initialize(SQLiteDatabase sQLiteDatabase);
    }

    private static class Cache {
        private final ArrayMap<CacheKey, Object> mCache;
        private final CacheKey mCacheKey;
        private int mVersion;

        private static final class CacheKey {
            static final int TYPE_FETCHED = 2;
            static final int TYPE_FILE = 1;
            static final int TYPE_KEY_VALUE = 0;
            String key;
            int type;
            int userId;

            /* synthetic */ CacheKey(CacheKey cacheKey) {
                this();
            }

            private CacheKey() {
            }

            public CacheKey set(int type, String key, int userId) {
                this.type = type;
                this.key = key;
                this.userId = userId;
                return this;
            }

            public boolean equals(Object obj) {
                boolean z = false;
                if (!(obj instanceof CacheKey)) {
                    return false;
                }
                CacheKey o = (CacheKey) obj;
                if (this.userId == o.userId && this.type == o.type) {
                    z = this.key.equals(o.key);
                }
                return z;
            }

            public int hashCode() {
                return (this.key.hashCode() ^ this.userId) ^ this.type;
            }
        }

        /* synthetic */ Cache(Cache cache) {
            this();
        }

        private Cache() {
            this.mCache = new ArrayMap();
            this.mCacheKey = new CacheKey();
            this.mVersion = 0;
        }

        String peekKeyValue(String key, String defaultValue, int userId) {
            Object cached = peek(0, key, userId);
            return cached == LockSettingsStorage.DEFAULT ? defaultValue : (String) cached;
        }

        boolean hasKeyValue(String key, int userId) {
            return contains(0, key, userId);
        }

        void putKeyValue(String key, String value, int userId) {
            put(0, key, value, userId);
        }

        void putKeyValueIfUnchanged(String key, Object value, int userId, int version) {
            putIfUnchanged(0, key, value, userId, version);
        }

        byte[] peekFile(String fileName) {
            return (byte[]) peek(1, fileName, -1);
        }

        boolean hasFile(String fileName) {
            return contains(1, fileName, -1);
        }

        void putFile(String key, byte[] value) {
            put(1, key, value, -1);
        }

        void putFileIfUnchanged(String key, byte[] value, int version) {
            putIfUnchanged(1, key, value, -1, version);
        }

        void setFetched(int userId) {
            put(2, "isFetched", "true", userId);
        }

        boolean isFetched(int userId) {
            return contains(2, IElsaManager.EMPTY_PACKAGE, userId);
        }

        private synchronized void put(int type, String key, Object value, int userId) {
            this.mCache.put(new CacheKey().set(type, key, userId), value);
            this.mVersion++;
        }

        private synchronized void putIfUnchanged(int type, String key, Object value, int userId, int version) {
            if (!contains(type, key, userId) && this.mVersion == version) {
                put(type, key, value, userId);
            }
        }

        private synchronized boolean contains(int type, String key, int userId) {
            return this.mCache.containsKey(this.mCacheKey.set(type, key, userId));
        }

        private synchronized Object peek(int type, String key, int userId) {
            return this.mCache.get(this.mCacheKey.set(type, key, userId));
        }

        private synchronized int getVersion() {
            return this.mVersion;
        }

        synchronized void removeUser(int userId) {
            for (int i = this.mCache.size() - 1; i >= 0; i--) {
                if (((CacheKey) this.mCache.keyAt(i)).userId == userId) {
                    this.mCache.removeAt(i);
                }
            }
            this.mVersion++;
        }

        synchronized void clear() {
            this.mCache.clear();
            this.mVersion++;
        }
    }

    static class CredentialHash {
        static final int TYPE_NONE = -1;
        static final int TYPE_PASSWORD = 2;
        static final int TYPE_PATTERN = 1;
        static final int VERSION_GATEKEEPER = 1;
        static final int VERSION_LEGACY = 0;
        byte[] hash;
        boolean isBaseZeroPattern;
        int version;

        CredentialHash(byte[] hash, int version) {
            this.hash = hash;
            this.version = version;
            this.isBaseZeroPattern = false;
        }

        CredentialHash(byte[] hash, boolean isBaseZeroPattern) {
            this.hash = hash;
            this.version = 1;
            this.isBaseZeroPattern = isBaseZeroPattern;
        }
    }

    class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "locksettings.db";
        private static final int DATABASE_VERSION = 2;
        private static final String TAG = "LockSettingsDB";
        private final Callback mCallback;

        public DatabaseHelper(Context context, Callback callback) {
            super(context, DATABASE_NAME, null, 2);
            setWriteAheadLoggingEnabled(true);
            this.mCallback = callback;
        }

        private void createTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE locksettings (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,user INTEGER,value TEXT);");
        }

        public void onCreate(SQLiteDatabase db) {
            createTable(db);
            this.mCallback.initialize(db);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
            int upgradeVersion = oldVersion;
            if (oldVersion == 1) {
                upgradeVersion = 2;
            }
            if (upgradeVersion != 2) {
                Log.w(TAG, "Failed to upgrade database!");
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.LockSettingsStorage.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.LockSettingsStorage.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.LockSettingsStorage.<clinit>():void");
    }

    public LockSettingsStorage(Context context, Callback callback) {
        this.mCache = new Cache();
        this.mFileWriteLock = new Object();
        this.mContext = context;
        this.mOpenHelper = new DatabaseHelper(context, callback);
        this.mStoredCredentialType = new SparseArray();
    }

    public void writeKeyValue(String key, String value, int userId) {
        writeKeyValue(this.mOpenHelper.getWritableDatabase(), key, value, userId);
    }

    public void writeKeyValue(SQLiteDatabase db, String key, String value, int userId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_KEY, key);
        cv.put(COLUMN_USERID, Integer.valueOf(userId));
        cv.put(COLUMN_VALUE, value);
        db.beginTransaction();
        try {
            String[] strArr = new String[2];
            strArr[0] = key;
            strArr[1] = Integer.toString(userId);
            db.delete(TABLE, "name=? AND user=?", strArr);
            db.insert(TABLE, null, cv);
            db.setTransactionSuccessful();
            this.mCache.putKeyValue(key, value, userId);
        } finally {
            db.endTransaction();
        }
    }

    /* JADX WARNING: Missing block: B:11:0x001c, code:
            r9 = DEFAULT;
            r4 = new java.lang.String[2];
            r4[0] = java.lang.Integer.toString(r15);
            r4[1] = r13;
            r8 = r12.mOpenHelper.getReadableDatabase().query(TABLE, COLUMNS_FOR_QUERY, "user=? AND name=?", r4, null, null, null);
     */
    /* JADX WARNING: Missing block: B:12:0x003e, code:
            if (r8 == null) goto L_0x004d;
     */
    /* JADX WARNING: Missing block: B:14:0x0044, code:
            if (r8.moveToFirst() == false) goto L_0x004a;
     */
    /* JADX WARNING: Missing block: B:15:0x0046, code:
            r9 = r8.getString(0);
     */
    /* JADX WARNING: Missing block: B:16:0x004a, code:
            r8.close();
     */
    /* JADX WARNING: Missing block: B:17:0x004d, code:
            r12.mCache.putKeyValueIfUnchanged(r13, r9, r15, r10);
     */
    /* JADX WARNING: Missing block: B:18:0x0054, code:
            if (r9 != DEFAULT) goto L_0x005a;
     */
    /* JADX WARNING: Missing block: B:19:0x0056, code:
            return r14;
     */
    /* JADX WARNING: Missing block: B:23:0x005a, code:
            r14 = (java.lang.String) r9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String readKeyValue(String key, String defaultValue, int userId) {
        synchronized (this.mCache) {
            if (this.mCache.hasKeyValue(key, userId)) {
                String peekKeyValue = this.mCache.peekKeyValue(key, defaultValue, userId);
                return peekKeyValue;
            }
            int version = this.mCache.getVersion();
        }
    }

    /* JADX WARNING: Missing block: B:10:0x001c, code:
            r4 = new java.lang.String[1];
            r4[0] = java.lang.Integer.toString(r15);
            r8 = r14.mOpenHelper.getReadableDatabase().query(TABLE, COLUMNS_FOR_PREFETCH, "user=?", r4, null, null, null);
     */
    /* JADX WARNING: Missing block: B:11:0x0038, code:
            if (r8 == null) goto L_0x0054;
     */
    /* JADX WARNING: Missing block: B:13:0x003e, code:
            if (r8.moveToNext() == false) goto L_0x0051;
     */
    /* JADX WARNING: Missing block: B:14:0x0040, code:
            r14.mCache.putKeyValueIfUnchanged(r8.getString(0), r8.getString(1), r15, r11);
     */
    /* JADX WARNING: Missing block: B:18:0x0051, code:
            r8.close();
     */
    /* JADX WARNING: Missing block: B:19:0x0054, code:
            readPasswordHash(r15);
            readPatternHash(r15);
     */
    /* JADX WARNING: Missing block: B:20:0x005a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void prefetchUser(int userId) {
        synchronized (this.mCache) {
            if (this.mCache.isFetched(userId)) {
            } else {
                this.mCache.setFetched(userId);
                int version = this.mCache.getVersion();
            }
        }
    }

    public int getStoredCredentialType(int userId) {
        Integer cachedStoredCredentialType = (Integer) this.mStoredCredentialType.get(userId);
        if (cachedStoredCredentialType != null) {
            return cachedStoredCredentialType.intValue();
        }
        int storedCredentialType;
        if (readPatternHash(userId) != null) {
            CredentialHash password = readPasswordHash(userId);
            if (password == null) {
                storedCredentialType = 1;
            } else if (password.version == 1) {
                storedCredentialType = 2;
            } else {
                storedCredentialType = 1;
            }
        } else if (readPasswordHash(userId) != null) {
            storedCredentialType = 2;
        } else {
            storedCredentialType = -1;
        }
        this.mStoredCredentialType.put(userId, Integer.valueOf(storedCredentialType));
        return storedCredentialType;
    }

    public CredentialHash readPasswordHash(int userId) {
        byte[] stored = readFile(getLockPasswordFilename(userId));
        if (stored != null && stored.length > 0) {
            return new CredentialHash(stored, 1);
        }
        stored = readFile(getLegacyLockPasswordFilename(userId));
        if (stored == null || stored.length <= 0) {
            return null;
        }
        return new CredentialHash(stored, 0);
    }

    public CredentialHash readPatternHash(int userId) {
        byte[] stored = readFile(getLockPatternFilename(userId));
        if (stored != null && stored.length > 0) {
            return new CredentialHash(stored, 1);
        }
        stored = readFile(getBaseZeroLockPatternFilename(userId));
        if (stored != null && stored.length > 0) {
            return new CredentialHash(stored, true);
        }
        stored = readFile(getLegacyLockPatternFilename(userId));
        if (stored == null || stored.length <= 0) {
            return null;
        }
        return new CredentialHash(stored, 0);
    }

    public void removeChildProfileLock(int userId) {
        try {
            deleteFile(getChildProfileLockFile(userId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeChildProfileLock(int userId, byte[] lock) {
        writeFile(getChildProfileLockFile(userId), lock);
    }

    public byte[] readChildProfileLock(int userId) {
        return readFile(getChildProfileLockFile(userId));
    }

    public boolean hasChildProfileLock(int userId) {
        return hasFile(getChildProfileLockFile(userId));
    }

    public boolean hasPassword(int userId) {
        if (hasFile(getLockPasswordFilename(userId))) {
            return true;
        }
        return hasFile(getLegacyLockPasswordFilename(userId));
    }

    public boolean hasPattern(int userId) {
        if (hasFile(getLockPatternFilename(userId)) || hasFile(getBaseZeroLockPatternFilename(userId))) {
            return true;
        }
        return hasFile(getLegacyLockPatternFilename(userId));
    }

    private boolean hasFile(String name) {
        byte[] contents = readFile(name);
        if (contents == null || contents.length <= 0) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x007b A:{SYNTHETIC, Splitter: B:31:0x007b} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x009e A:{SYNTHETIC, Splitter: B:37:0x009e} */
    /* JADX WARNING: Missing block: B:11:0x001a, code:
            r1 = null;
            r3 = null;
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            r2 = new java.io.RandomAccessFile(r10, "r");
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            r3 = new byte[((int) r2.length())];
            r2.readFully(r3, 0, r3.length);
            r2.close();
     */
    /* JADX WARNING: Missing block: B:16:0x0033, code:
            if (r2 == null) goto L_0x0038;
     */
    /* JADX WARNING: Missing block: B:18:?, code:
            r2.close();
     */
    /* JADX WARNING: Missing block: B:25:0x0042, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:26:0x0043, code:
            android.util.Slog.e(TAG, "Error closing file " + r0);
     */
    /* JADX WARNING: Missing block: B:27:0x005e, code:
            r0 = e;
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            android.util.Slog.e(TAG, "Cannot read file " + r0);
     */
    /* JADX WARNING: Missing block: B:30:0x0079, code:
            if (r1 != null) goto L_0x007b;
     */
    /* JADX WARNING: Missing block: B:32:?, code:
            r1.close();
     */
    /* JADX WARNING: Missing block: B:33:0x007f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:34:0x0080, code:
            android.util.Slog.e(TAG, "Error closing file " + r0);
     */
    /* JADX WARNING: Missing block: B:35:0x009b, code:
            r5 = th;
     */
    /* JADX WARNING: Missing block: B:36:0x009c, code:
            if (r1 != null) goto L_0x009e;
     */
    /* JADX WARNING: Missing block: B:38:?, code:
            r1.close();
     */
    /* JADX WARNING: Missing block: B:39:0x00a1, code:
            throw r5;
     */
    /* JADX WARNING: Missing block: B:40:0x00a2, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:41:0x00a3, code:
            android.util.Slog.e(TAG, "Error closing file " + r0);
     */
    /* JADX WARNING: Missing block: B:42:0x00be, code:
            r5 = th;
     */
    /* JADX WARNING: Missing block: B:43:0x00bf, code:
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:44:0x00c1, code:
            r0 = e;
     */
    /* JADX WARNING: Missing block: B:45:0x00c2, code:
            r1 = r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] readFile(String name) {
        int version;
        synchronized (this.mCache) {
            if (this.mCache.hasFile(name)) {
                byte[] peekFile = this.mCache.peekFile(name);
                return peekFile;
            }
            version = this.mCache.getVersion();
        }
        RandomAccessFile randomAccessFile = raf;
        this.mCache.putFileIfUnchanged(name, stored, version);
        return stored;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x001b A:{SYNTHETIC, Splitter: B:12:0x001b} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x008f A:{SYNTHETIC, Splitter: B:41:0x008f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeFile(String name, byte[] hash) {
        IOException e;
        Throwable th;
        synchronized (this.mFileWriteLock) {
            RandomAccessFile raf = null;
            try {
                RandomAccessFile raf2 = new RandomAccessFile(name, "rw");
                if (hash != null) {
                    try {
                        if (hash.length != 0) {
                            raf2.write(hash, 0, hash.length);
                            raf2.close();
                            if (raf2 != null) {
                                try {
                                    raf2.close();
                                } catch (IOException e2) {
                                    Slog.e(TAG, "Error closing file " + e2);
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            raf = raf2;
                            this.mCache.putFile(name, hash);
                            return;
                        }
                    } catch (IOException e3) {
                        e2 = e3;
                        raf = raf2;
                    } catch (Throwable th3) {
                        th = th3;
                        raf = raf2;
                        if (raf != null) {
                            try {
                                raf.close();
                            } catch (IOException e22) {
                                Slog.e(TAG, "Error closing file " + e22);
                            }
                        }
                        throw th;
                    }
                }
                raf2.setLength(0);
                raf2.close();
                if (raf2 != null) {
                }
                raf = raf2;
            } catch (IOException e4) {
                e22 = e4;
                try {
                    Slog.e(TAG, "Error writing to file " + e22);
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e222) {
                            Slog.e(TAG, "Error closing file " + e222);
                        } catch (Throwable th4) {
                            th = th4;
                        }
                    }
                    this.mCache.putFile(name, hash);
                    return;
                } catch (Throwable th5) {
                    th = th5;
                    if (raf != null) {
                    }
                    throw th;
                }
            }
            this.mCache.putFile(name, hash);
            return;
        }
        throw th;
    }

    private void deleteFile(String name) {
        synchronized (this.mFileWriteLock) {
            File file = new File(name);
            if (file.exists()) {
                file.delete();
                this.mCache.putFile(name, null);
            }
        }
    }

    public void writePatternHash(byte[] hash, int userId) {
        int i;
        SparseArray sparseArray = this.mStoredCredentialType;
        if (hash == null) {
            i = -1;
        } else {
            i = 1;
        }
        sparseArray.put(userId, Integer.valueOf(i));
        writeFile(getLockPatternFilename(userId), hash);
        clearPasswordHash(userId);
    }

    private void clearPatternHash(int userId) {
        writeFile(getLockPatternFilename(userId), null);
    }

    public void writePasswordHash(byte[] hash, int userId) {
        int i;
        SparseArray sparseArray = this.mStoredCredentialType;
        if (hash == null) {
            i = -1;
        } else {
            i = 2;
        }
        sparseArray.put(userId, Integer.valueOf(i));
        writeFile(getLockPasswordFilename(userId), hash);
        clearPatternHash(userId);
    }

    private void clearPasswordHash(int userId) {
        writeFile(getLockPasswordFilename(userId), null);
    }

    String getLockPatternFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LOCK_PATTERN_FILE);
    }

    String getLockPasswordFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LOCK_PASSWORD_FILE);
    }

    String getLegacyLockPatternFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LEGACY_LOCK_PATTERN_FILE);
    }

    String getLegacyLockPasswordFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LEGACY_LOCK_PASSWORD_FILE);
    }

    private String getBaseZeroLockPatternFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, BASE_ZERO_LOCK_PATTERN_FILE);
    }

    String getChildProfileLockFile(int userId) {
        return getLockCredentialFilePathForUser(userId, CHILD_PROFILE_LOCK_FILE);
    }

    private String getLockCredentialFilePathForUser(int userId, String basename) {
        String dataSystemDirectory = Environment.getDataDirectory().getAbsolutePath() + SYSTEM_DIRECTORY;
        if (userId == 0) {
            return dataSystemDirectory + basename;
        }
        return new File(Environment.getUserSystemDirectory(userId), basename).getAbsolutePath();
    }

    public void removeUser(int userId) {
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        if (((UserManager) this.mContext.getSystemService(COLUMN_USERID)).getProfileParent(userId) == null) {
            synchronized (this.mFileWriteLock) {
                String name = getLockPasswordFilename(userId);
                File file = new File(name);
                if (file.exists()) {
                    file.delete();
                    this.mCache.putFile(name, null);
                }
                name = getLockPatternFilename(userId);
                file = new File(name);
                if (file.exists()) {
                    file.delete();
                    this.mCache.putFile(name, null);
                }
            }
        } else {
            removeChildProfileLock(userId);
        }
        try {
            db.beginTransaction();
            db.delete(TABLE, "user='" + userId + "'", null);
            db.setTransactionSuccessful();
            this.mCache.removeUser(userId);
        } finally {
            db.endTransaction();
        }
    }

    void closeDatabase() {
        this.mOpenHelper.close();
    }

    void clearCache() {
        this.mCache.clear();
    }
}
