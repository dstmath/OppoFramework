package com.android.server.locksettings;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.UserInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.PersistentDataBlockManagerInternal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class LockSettingsStorage {
    private static final String BASE_ZERO_LOCK_PATTERN_FILE = "gatekeeper.gesture.key";
    private static final String CHILD_PROFILE_LOCK_FILE = "gatekeeper.profile.key";
    private static final String[] COLUMNS_FOR_PREFETCH = new String[]{COLUMN_KEY, COLUMN_VALUE};
    private static final String[] COLUMNS_FOR_QUERY = new String[]{COLUMN_VALUE};
    private static final String COLUMN_KEY = "name";
    private static final String COLUMN_USERID = "user";
    private static final String COLUMN_VALUE = "value";
    private static final boolean DEBUG = false;
    private static final Object DEFAULT = new Object();
    private static final String LEGACY_LOCK_PASSWORD_FILE = "password.key";
    private static final String LEGACY_LOCK_PATTERN_FILE = "gesture.key";
    private static final String LOCK_PASSWORD_FILE = "gatekeeper.password.key";
    private static final String LOCK_PATTERN_FILE = "gatekeeper.pattern.key";
    private static final String SYNTHETIC_PASSWORD_DIRECTORY = "spblob/";
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String TABLE = "locksettings";
    private static final String TAG = "LockSettingsStorage";
    private final Cache mCache = new Cache();
    private final Context mContext;
    private final Object mFileWriteLock = new Object();
    private final DatabaseHelper mOpenHelper;
    private PersistentDataBlockManagerInternal mPersistentDataBlockManagerInternal;

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

            /* synthetic */ CacheKey(CacheKey -this0) {
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

        /* synthetic */ Cache(Cache -this0) {
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
            return contains(2, "", userId);
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

        synchronized void purgePath(String path) {
            for (int i = this.mCache.size() - 1; i >= 0; i--) {
                CacheKey entry = (CacheKey) this.mCache.keyAt(i);
                if (entry.type == 1 && entry.key.startsWith(path)) {
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

    public static class CredentialHash {
        static final int VERSION_GATEKEEPER = 1;
        static final int VERSION_LEGACY = 0;
        byte[] hash;
        boolean isBaseZeroPattern;
        int type;
        int version;

        private CredentialHash(byte[] hash, int type, int version) {
            this(hash, type, version, false);
        }

        private CredentialHash(byte[] hash, int type, int version, boolean isBaseZeroPattern) {
            if (type != -1) {
                if (hash == null) {
                    throw new RuntimeException("Empty hash for CredentialHash");
                }
            } else if (hash != null) {
                throw new RuntimeException("None type CredentialHash should not have hash");
            }
            this.hash = hash;
            this.type = type;
            this.version = version;
            this.isBaseZeroPattern = isBaseZeroPattern;
        }

        private static CredentialHash createBaseZeroPattern(byte[] hash) {
            return new CredentialHash(hash, 1, 1, true);
        }

        static CredentialHash create(byte[] hash, int type) {
            if (type != -1) {
                return new CredentialHash(hash, type, 1);
            }
            throw new RuntimeException("Bad type for CredentialHash");
        }

        static CredentialHash createEmptyHash() {
            return new CredentialHash(null, -1, 1);
        }

        public byte[] toBytes() {
            Preconditions.checkState(this.isBaseZeroPattern ^ 1, "base zero patterns are not serializable");
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                dos.write(this.version);
                dos.write(this.type);
                if (this.hash == null || this.hash.length <= 0) {
                    dos.writeInt(0);
                } else {
                    dos.writeInt(this.hash.length);
                    dos.write(this.hash);
                }
                dos.close();
                return os.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static CredentialHash fromBytes(byte[] bytes) {
            try {
                DataInputStream is = new DataInputStream(new ByteArrayInputStream(bytes));
                int version = is.read();
                int type = is.read();
                int hashSize = is.readInt();
                byte[] hash = null;
                if (hashSize > 0) {
                    hash = new byte[hashSize];
                    is.readFully(hash);
                }
                return new CredentialHash(hash, type, version);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "locksettings.db";
        private static final int DATABASE_VERSION = 2;
        private static final int IDLE_CONNECTION_TIMEOUT_MS = 30000;
        private static final String TAG = "LockSettingsDB";
        private Callback mCallback;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, 2);
            setWriteAheadLoggingEnabled(true);
            setIdleConnectionTimeout(30000);
        }

        public void setCallback(Callback callback) {
            this.mCallback = callback;
        }

        private void createTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE locksettings (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,user INTEGER,value TEXT);");
        }

        public void onCreate(SQLiteDatabase db) {
            createTable(db);
            if (this.mCallback != null) {
                this.mCallback.initialize(db);
            }
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

    public static class PersistentData {
        public static final PersistentData NONE = new PersistentData(0, -10000, 0, null);
        public static final int TYPE_NONE = 0;
        public static final int TYPE_SP = 1;
        public static final int TYPE_SP_WEAVER = 2;
        static final byte VERSION_1 = (byte) 1;
        static final int VERSION_1_HEADER_SIZE = 10;
        final byte[] payload;
        final int qualityForUi;
        final int type;
        final int userId;

        private PersistentData(int type, int userId, int qualityForUi, byte[] payload) {
            this.type = type;
            this.userId = userId;
            this.qualityForUi = qualityForUi;
            this.payload = payload;
        }

        public static PersistentData fromBytes(byte[] frpData) {
            if (frpData == null || frpData.length == 0) {
                return NONE;
            }
            DataInputStream is = new DataInputStream(new ByteArrayInputStream(frpData));
            try {
                byte version = is.readByte();
                if (version == (byte) 1) {
                    int type = is.readByte() & 255;
                    int userId = is.readInt();
                    int qualityForUi = is.readInt();
                    byte[] payload = new byte[(frpData.length - 10)];
                    System.arraycopy(frpData, 10, payload, 0, payload.length);
                    return new PersistentData(type, userId, qualityForUi, payload);
                }
                Slog.wtf(LockSettingsStorage.TAG, "Unknown PersistentData version code: " + version);
                return null;
            } catch (IOException e) {
                Slog.wtf(LockSettingsStorage.TAG, "Could not parse PersistentData", e);
                return null;
            }
        }

        public static byte[] toBytes(int persistentType, int userId, int qualityForUi, byte[] payload) {
            boolean z = true;
            boolean z2 = false;
            if (persistentType == 0) {
                if (payload != null) {
                    z = false;
                }
                Preconditions.checkArgument(z, "TYPE_NONE must have empty payload");
                return null;
            }
            if (payload != null && payload.length > 0) {
                z2 = true;
            }
            Preconditions.checkArgument(z2, "empty payload must only be used with TYPE_NONE");
            ByteArrayOutputStream os = new ByteArrayOutputStream(payload.length + 10);
            DataOutputStream dos = new DataOutputStream(os);
            try {
                dos.writeByte(1);
                dos.writeByte(persistentType);
                dos.writeInt(userId);
                dos.writeInt(qualityForUi);
                dos.write(payload);
                return os.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("ByteArrayOutputStream cannot throw IOException");
            }
        }
    }

    public LockSettingsStorage(Context context) {
        this.mContext = context;
        this.mOpenHelper = new DatabaseHelper(context);
    }

    public void setDatabaseOnCreateCallback(Callback callback) {
        this.mOpenHelper.setCallback(callback);
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
            db.delete(TABLE, "name=? AND user=?", new String[]{key, Integer.toString(userId)});
            db.insert(TABLE, null, cv);
            db.setTransactionSuccessful();
            this.mCache.putKeyValue(key, value, userId);
        } finally {
            db.endTransaction();
        }
    }

    /* JADX WARNING: Missing block: B:11:0x001c, code:
            r9 = DEFAULT;
            r8 = r12.mOpenHelper.getReadableDatabase().query(TABLE, COLUMNS_FOR_QUERY, "user=? AND name=?", new java.lang.String[]{java.lang.Integer.toString(r15), r13}, null, null, null);
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
            r8 = r14.mOpenHelper.getReadableDatabase().query(TABLE, COLUMNS_FOR_PREFETCH, "user=?", new java.lang.String[]{java.lang.Integer.toString(r15)}, null, null, null);
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
            readCredentialHash(r15);
     */
    /* JADX WARNING: Missing block: B:20:0x0057, code:
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

    private CredentialHash readPasswordHashIfExists(int userId) {
        byte[] stored = readFile(getLockPasswordFilename(userId));
        if (!ArrayUtils.isEmpty(stored)) {
            return new CredentialHash(stored, 2, 1, null);
        }
        stored = readFile(getLegacyLockPasswordFilename(userId));
        if (ArrayUtils.isEmpty(stored)) {
            return null;
        }
        return new CredentialHash(stored, 2, 0, null);
    }

    private CredentialHash readPatternHashIfExists(int userId) {
        byte[] stored = readFile(getLockPatternFilename(userId));
        if (!ArrayUtils.isEmpty(stored)) {
            return new CredentialHash(stored, 1, 1, null);
        }
        stored = readFile(getBaseZeroLockPatternFilename(userId));
        if (!ArrayUtils.isEmpty(stored)) {
            return CredentialHash.createBaseZeroPattern(stored);
        }
        stored = readFile(getLegacyLockPatternFilename(userId));
        if (ArrayUtils.isEmpty(stored)) {
            return null;
        }
        return new CredentialHash(stored, 1, 0, null);
    }

    public CredentialHash readCredentialHash(int userId) {
        CredentialHash passwordHash = readPasswordHashIfExists(userId);
        CredentialHash patternHash = readPatternHashIfExists(userId);
        if (passwordHash == null || patternHash == null) {
            if (passwordHash != null) {
                return passwordHash;
            }
            if (patternHash != null) {
                return patternHash;
            }
            return CredentialHash.createEmptyHash();
        } else if (passwordHash.version == 1) {
            return passwordHash;
        } else {
            return patternHash;
        }
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

    public boolean hasCredential(int userId) {
        return !hasPassword(userId) ? hasPattern(userId) : true;
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
                RandomAccessFile raf2 = new RandomAccessFile(name, "rws");
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

    public void writeCredentialHash(CredentialHash hash, int userId) {
        byte[] patternHash = null;
        byte[] passwordHash = null;
        if (hash.type == 2) {
            passwordHash = hash.hash;
        } else if (hash.type == 1) {
            patternHash = hash.hash;
        }
        writeFile(getLockPasswordFilename(userId), passwordHash);
        writeFile(getLockPatternFilename(userId), patternHash);
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

    public void writeSyntheticPasswordState(int userId, long handle, String name, byte[] data) {
        writeFile(getSynthenticPasswordStateFilePathForUser(userId, handle, name), data);
    }

    public byte[] readSyntheticPasswordState(int userId, long handle, String name) {
        return readFile(getSynthenticPasswordStateFilePathForUser(userId, handle, name));
    }

    public void deleteSyntheticPasswordState(int userId, long handle, String name) {
        String path = getSynthenticPasswordStateFilePathForUser(userId, handle, name);
        File file = new File(path);
        if (file.exists()) {
            try {
                ((StorageManager) this.mContext.getSystemService(StorageManager.class)).secdiscard(file.getAbsolutePath());
            } catch (Exception e) {
                Slog.w(TAG, "Failed to secdiscard " + path, e);
            } finally {
                file.delete();
            }
            this.mCache.putFile(path, null);
        }
    }

    public Map<Integer, List<Long>> listSyntheticPasswordHandlesForAllUsers(String stateName) {
        Map<Integer, List<Long>> result = new ArrayMap();
        for (UserInfo user : UserManager.get(this.mContext).getUsers(false)) {
            result.put(Integer.valueOf(user.id), listSyntheticPasswordHandlesForUser(stateName, user.id));
        }
        return result;
    }

    public List<Long> listSyntheticPasswordHandlesForUser(String stateName, int userId) {
        File baseDir = getSyntheticPasswordDirectoryForUser(userId);
        List<Long> result = new ArrayList();
        File[] files = baseDir.listFiles();
        if (files == null) {
            return result;
        }
        for (File file : files) {
            String[] parts = file.getName().split("\\.");
            if (parts.length == 2 && parts[1].equals(stateName)) {
                try {
                    result.add(Long.valueOf(Long.parseUnsignedLong(parts[0], 16)));
                } catch (NumberFormatException e) {
                    Slog.e(TAG, "Failed to parse handle " + parts[0]);
                }
            }
        }
        return result;
    }

    protected File getSyntheticPasswordDirectoryForUser(int userId) {
        return new File(Environment.getDataSystemDeDirectory(userId), SYNTHETIC_PASSWORD_DIRECTORY);
    }

    protected String getSynthenticPasswordStateFilePathForUser(int userId, long handle, String name) {
        File baseDir = getSyntheticPasswordDirectoryForUser(userId);
        String baseName = String.format("%016x.%s", new Object[]{Long.valueOf(handle), name});
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }
        return new File(baseDir, baseName).getAbsolutePath();
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
        File spStateDir = getSyntheticPasswordDirectoryForUser(userId);
        try {
            db.beginTransaction();
            db.delete(TABLE, "user='" + userId + "'", null);
            db.setTransactionSuccessful();
            this.mCache.removeUser(userId);
            this.mCache.purgePath(spStateDir.getAbsolutePath());
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

    public PersistentDataBlockManagerInternal getPersistentDataBlock() {
        if (this.mPersistentDataBlockManagerInternal == null) {
            this.mPersistentDataBlockManagerInternal = (PersistentDataBlockManagerInternal) LocalServices.getService(PersistentDataBlockManagerInternal.class);
        }
        return this.mPersistentDataBlockManagerInternal;
    }

    public void writePersistentDataBlock(int persistentType, int userId, int qualityForUi, byte[] payload) {
        PersistentDataBlockManagerInternal persistentDataBlock = getPersistentDataBlock();
        if (persistentDataBlock != null) {
            persistentDataBlock.setFrpCredentialHandle(PersistentData.toBytes(persistentType, userId, qualityForUi, payload));
        }
    }

    public PersistentData readPersistentDataBlock() {
        PersistentDataBlockManagerInternal persistentDataBlock = getPersistentDataBlock();
        if (persistentDataBlock == null) {
            return PersistentData.NONE;
        }
        return PersistentData.fromBytes(persistentDataBlock.getFrpCredentialHandle());
    }
}
