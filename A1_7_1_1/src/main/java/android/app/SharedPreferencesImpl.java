package android.app;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.FileUtils;
import android.os.Looper;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.util.Log;
import com.android.internal.util.XmlUtils;
import com.google.android.collect.Maps;
import dalvik.system.BlockGuard;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import org.xmlpull.v1.XmlPullParserException;

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
final class SharedPreferencesImpl implements SharedPreferences {
    private static final boolean DEBUG = false;
    private static final String TAG = "SharedPreferencesImpl";
    private static final Object mContent = null;
    private final File mBackupFile;
    private int mDiskWritesInFlight;
    private final File mFile;
    private final WeakHashMap<OnSharedPreferenceChangeListener, Object> mListeners;
    private boolean mLoaded;
    private Map<String, Object> mMap;
    private final int mMode;
    private long mStatSize;
    private long mStatTimestamp;
    private final Object mWritingToDiskLock;

    public final class EditorImpl implements Editor {
        private boolean mClear = false;
        private final Map<String, Object> mModified = Maps.newHashMap();

        public Editor putString(String key, String value) {
            synchronized (this) {
                this.mModified.put(key, value);
            }
            return this;
        }

        public Editor putStringSet(String key, Set<String> values) {
            Object obj = null;
            synchronized (this) {
                Map map = this.mModified;
                if (values != null) {
                    obj = new HashSet(values);
                }
                map.put(key, obj);
            }
            return this;
        }

        public Editor putInt(String key, int value) {
            synchronized (this) {
                this.mModified.put(key, Integer.valueOf(value));
            }
            return this;
        }

        public Editor putLong(String key, long value) {
            synchronized (this) {
                this.mModified.put(key, Long.valueOf(value));
            }
            return this;
        }

        public Editor putFloat(String key, float value) {
            synchronized (this) {
                this.mModified.put(key, Float.valueOf(value));
            }
            return this;
        }

        public Editor putBoolean(String key, boolean value) {
            synchronized (this) {
                this.mModified.put(key, Boolean.valueOf(value));
            }
            return this;
        }

        public Editor remove(String key) {
            synchronized (this) {
                this.mModified.put(key, this);
            }
            return this;
        }

        public Editor clear() {
            synchronized (this) {
                this.mClear = true;
            }
            return this;
        }

        public void apply() {
            final MemoryCommitResult mcr = commitToMemory();
            final Runnable awaitCommit = new Runnable() {
                public void run() {
                    try {
                        mcr.writtenToDiskLatch.await();
                    } catch (InterruptedException e) {
                    }
                }
            };
            QueuedWork.add(awaitCommit);
            SharedPreferencesImpl.this.enqueueDiskWrite(mcr, new Runnable() {
                public void run() {
                    awaitCommit.run();
                    QueuedWork.remove(awaitCommit);
                }
            });
            notifyListeners(mcr);
        }

        private MemoryCommitResult commitToMemory() {
            boolean hasListeners = true;
            MemoryCommitResult mcr = new MemoryCommitResult();
            synchronized (SharedPreferencesImpl.this) {
                if (SharedPreferencesImpl.this.mDiskWritesInFlight > 0) {
                    SharedPreferencesImpl.this.mMap = new HashMap(SharedPreferencesImpl.this.mMap);
                }
                mcr.mapToWriteToDisk = SharedPreferencesImpl.this.mMap;
                SharedPreferencesImpl sharedPreferencesImpl = SharedPreferencesImpl.this;
                sharedPreferencesImpl.mDiskWritesInFlight = sharedPreferencesImpl.mDiskWritesInFlight + 1;
                if (SharedPreferencesImpl.this.mListeners.size() <= 0) {
                    hasListeners = false;
                }
                if (hasListeners) {
                    mcr.keysModified = new ArrayList();
                    mcr.listeners = new HashSet(SharedPreferencesImpl.this.mListeners.keySet());
                }
                synchronized (this) {
                    if (this.mClear) {
                        if (!SharedPreferencesImpl.this.mMap.isEmpty()) {
                            mcr.changesMade = true;
                            SharedPreferencesImpl.this.mMap.clear();
                        }
                        this.mClear = false;
                    }
                    for (Entry<String, Object> e : this.mModified.entrySet()) {
                        String k = (String) e.getKey();
                        EditorImpl v = e.getValue();
                        if (v != this && v != null) {
                            if (SharedPreferencesImpl.this.mMap.containsKey(k)) {
                                Object existingValue = SharedPreferencesImpl.this.mMap.get(k);
                                if (existingValue != null && existingValue.equals(v)) {
                                }
                            }
                            SharedPreferencesImpl.this.mMap.put(k, v);
                        } else if (SharedPreferencesImpl.this.mMap.containsKey(k)) {
                            SharedPreferencesImpl.this.mMap.remove(k);
                        }
                        mcr.changesMade = true;
                        if (hasListeners) {
                            mcr.keysModified.add(k);
                        }
                    }
                    this.mModified.clear();
                }
            }
            return mcr;
        }

        public boolean commit() {
            MemoryCommitResult mcr = commitToMemory();
            SharedPreferencesImpl.this.enqueueDiskWrite(mcr, null);
            try {
                mcr.writtenToDiskLatch.await();
                notifyListeners(mcr);
                return mcr.writeToDiskResult;
            } catch (InterruptedException e) {
                return false;
            }
        }

        private void notifyListeners(final MemoryCommitResult mcr) {
            if (mcr.listeners != null && mcr.keysModified != null && mcr.keysModified.size() != 0) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    for (int i = mcr.keysModified.size() - 1; i >= 0; i--) {
                        String key = (String) mcr.keysModified.get(i);
                        for (OnSharedPreferenceChangeListener listener : mcr.listeners) {
                            if (listener != null) {
                                listener.onSharedPreferenceChanged(SharedPreferencesImpl.this, key);
                            }
                        }
                    }
                } else {
                    ActivityThread.sMainThreadHandler.post(new Runnable() {
                        public void run() {
                            EditorImpl.this.notifyListeners(mcr);
                        }
                    });
                }
            }
        }
    }

    private static class MemoryCommitResult {
        public boolean changesMade;
        public List<String> keysModified;
        public Set<OnSharedPreferenceChangeListener> listeners;
        public Map<?, ?> mapToWriteToDisk;
        public volatile boolean writeToDiskResult;
        public final CountDownLatch writtenToDiskLatch;

        /* synthetic */ MemoryCommitResult(MemoryCommitResult memoryCommitResult) {
            this();
        }

        private MemoryCommitResult() {
            this.writtenToDiskLatch = new CountDownLatch(1);
            this.writeToDiskResult = false;
        }

        public void setDiskWriteResult(boolean result) {
            this.writeToDiskResult = result;
            this.writtenToDiskLatch.countDown();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.SharedPreferencesImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.SharedPreferencesImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.SharedPreferencesImpl.<clinit>():void");
    }

    SharedPreferencesImpl(File file, int mode) {
        this.mDiskWritesInFlight = 0;
        this.mLoaded = false;
        this.mWritingToDiskLock = new Object();
        this.mListeners = new WeakHashMap();
        this.mFile = file;
        this.mBackupFile = makeBackupFile(file);
        this.mMode = mode;
        this.mLoaded = false;
        this.mMap = null;
        startLoadFromDisk();
    }

    private void startLoadFromDisk() {
        synchronized (this) {
            this.mLoaded = false;
        }
        new Thread("SharedPreferencesImpl-load") {
            public void run() {
                SharedPreferencesImpl.this.loadFromDisk();
            }
        }.start();
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x0092 A:{ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException), Splitter: B:20:0x0041} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00b5 A:{ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException), Splitter: B:22:0x004f} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0057  */
    /* JADX WARNING: Missing block: B:12:0x0022, code:
            if (r9.mFile.exists() == false) goto L_0x002c;
     */
    /* JADX WARNING: Missing block: B:14:0x002a, code:
            if (r9.mFile.canRead() == false) goto L_0x006e;
     */
    /* JADX WARNING: Missing block: B:15:0x002c, code:
            r2 = null;
            r3 = null;
     */
    /* JADX WARNING: Missing block: B:17:?, code:
            r3 = android.system.Os.stat(r9.mFile.getPath());
     */
    /* JADX WARNING: Missing block: B:18:0x003e, code:
            if (r9.mFile.canRead() == false) goto L_0x0056;
     */
    /* JADX WARNING: Missing block: B:19:0x0040, code:
            r4 = null;
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            r5 = new java.io.BufferedInputStream(new java.io.FileInputStream(r9.mFile), 16384);
     */
    /* JADX WARNING: Missing block: B:23:?, code:
            r2 = com.android.internal.util.XmlUtils.readMapXml(r5);
     */
    /* JADX WARNING: Missing block: B:25:?, code:
            libcore.io.IoUtils.closeQuietly(r5);
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            r9.mLoaded = true;
     */
    /* JADX WARNING: Missing block: B:30:0x005a, code:
            if (r2 != null) goto L_0x005c;
     */
    /* JADX WARNING: Missing block: B:31:0x005c, code:
            r9.mMap = r2;
            r9.mStatTimestamp = r3.st_mtime;
            r9.mStatSize = r3.st_size;
     */
    /* JADX WARNING: Missing block: B:32:0x0066, code:
            notifyAll();
     */
    /* JADX WARNING: Missing block: B:38:0x006e, code:
            android.util.Log.w(TAG, "Attempt to read preferences file " + r9.mFile + " without permission");
     */
    /* JADX WARNING: Missing block: B:39:0x0092, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            android.util.Log.w(TAG, "getSharedPreferences", r1);
     */
    /* JADX WARNING: Missing block: B:43:?, code:
            libcore.io.IoUtils.closeQuietly(r4);
     */
    /* JADX WARNING: Missing block: B:45:0x00a2, code:
            r6 = th;
     */
    /* JADX WARNING: Missing block: B:46:0x00a3, code:
            libcore.io.IoUtils.closeQuietly(r4);
     */
    /* JADX WARNING: Missing block: B:47:0x00a6, code:
            throw r6;
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            r9.mMap = new java.util.HashMap();
     */
    /* JADX WARNING: Missing block: B:53:0x00b2, code:
            r6 = th;
     */
    /* JADX WARNING: Missing block: B:54:0x00b3, code:
            r4 = r5;
     */
    /* JADX WARNING: Missing block: B:55:0x00b5, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:56:0x00b6, code:
            r4 = r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadFromDisk() {
        synchronized (this) {
            if (this.mLoaded) {
                return;
            } else if (this.mBackupFile.exists()) {
                this.mFile.delete();
                this.mBackupFile.renameTo(this.mFile);
            }
        }
        synchronized (this) {
        }
    }

    static File makeBackupFile(File prefsFile) {
        return new File(prefsFile.getPath() + ".bak");
    }

    void startReloadIfChangedUnexpectedly() {
        synchronized (this) {
            if (hasFileChangedUnexpectedly()) {
                startLoadFromDisk();
                return;
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:?, code:
            dalvik.system.BlockGuard.getThreadPolicy().onReadFromDisk();
            r1 = android.system.Os.stat(r8.mFile.getPath());
     */
    /* JADX WARNING: Missing block: B:10:0x001b, code:
            monitor-enter(r8);
     */
    /* JADX WARNING: Missing block: B:13:0x0022, code:
            if (r8.mStatTimestamp != r1.st_mtime) goto L_0x002c;
     */
    /* JADX WARNING: Missing block: B:16:0x002a, code:
            if (r8.mStatSize == r1.st_size) goto L_0x0033;
     */
    /* JADX WARNING: Missing block: B:17:0x002c, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:18:0x002d, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:23:0x0032, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:24:0x0033, code:
            r2 = false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean hasFileChangedUnexpectedly() {
        boolean z = true;
        synchronized (this) {
            if (this.mDiskWritesInFlight > 0) {
                return false;
            }
        }
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (this) {
            this.mListeners.put(listener, mContent);
        }
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (this) {
            this.mListeners.remove(listener);
        }
    }

    private void awaitLoadedLocked() {
        if (!this.mLoaded) {
            BlockGuard.getThreadPolicy().onReadFromDisk();
        }
        while (!this.mLoaded) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public Map<String, ?> getAll() {
        Map hashMap;
        synchronized (this) {
            awaitLoadedLocked();
            hashMap = new HashMap(this.mMap);
        }
        return hashMap;
    }

    public String getString(String key, String defValue) {
        String v;
        synchronized (this) {
            awaitLoadedLocked();
            v = (String) this.mMap.get(key);
            if (v == null) {
                v = defValue;
            }
        }
        return v;
    }

    public Set<String> getStringSet(String key, Set<String> defValues) {
        Set<String> v;
        synchronized (this) {
            awaitLoadedLocked();
            v = (Set) this.mMap.get(key);
            if (v == null) {
                v = defValues;
            }
        }
        return v;
    }

    public int getInt(String key, int defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Integer v = (Integer) this.mMap.get(key);
            if (v != null) {
                defValue = v.intValue();
            }
        }
        return defValue;
    }

    public long getLong(String key, long defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Long v = (Long) this.mMap.get(key);
            if (v != null) {
                defValue = v.longValue();
            }
        }
        return defValue;
    }

    public float getFloat(String key, float defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Float v = (Float) this.mMap.get(key);
            if (v != null) {
                defValue = v.floatValue();
            }
        }
        return defValue;
    }

    public boolean getBoolean(String key, boolean defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Boolean v = (Boolean) this.mMap.get(key);
            if (v != null) {
                defValue = v.booleanValue();
            }
        }
        return defValue;
    }

    public boolean contains(String key) {
        boolean containsKey;
        synchronized (this) {
            awaitLoadedLocked();
            containsKey = this.mMap.containsKey(key);
        }
        return containsKey;
    }

    public Editor edit() {
        synchronized (this) {
            awaitLoadedLocked();
        }
        return new EditorImpl();
    }

    private void enqueueDiskWrite(final MemoryCommitResult mcr, final Runnable postWriteRunnable) {
        Runnable writeToDiskRunnable = new Runnable() {
            public void run() {
                synchronized (SharedPreferencesImpl.this.mWritingToDiskLock) {
                    SharedPreferencesImpl.this.writeToFile(mcr);
                }
                synchronized (SharedPreferencesImpl.this) {
                    SharedPreferencesImpl sharedPreferencesImpl = SharedPreferencesImpl.this;
                    sharedPreferencesImpl.mDiskWritesInFlight = sharedPreferencesImpl.mDiskWritesInFlight - 1;
                }
                if (postWriteRunnable != null) {
                    postWriteRunnable.run();
                }
            }
        };
        if (postWriteRunnable == null) {
            boolean wasEmpty;
            synchronized (this) {
                wasEmpty = this.mDiskWritesInFlight == 1;
            }
            if (wasEmpty) {
                writeToDiskRunnable.run();
                return;
            }
        }
        QueuedWork.singleThreadExecutor().execute(writeToDiskRunnable);
    }

    private static FileOutputStream createFileOutputStream(File file) {
        FileOutputStream str = null;
        try {
            str = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            File parent = file.getParentFile();
            if (parent.mkdir()) {
                FileUtils.setPermissions(parent.getPath(), (int) IActivityManager.GET_STICKY_WINDOW_TRANSACTION, -1, -1);
                try {
                    str = new FileOutputStream(file);
                } catch (FileNotFoundException e2) {
                    Log.e(TAG, "Couldn't create SharedPreferences file " + file, e2);
                }
            } else {
                Log.e(TAG, "Couldn't create directory for SharedPreferences file " + file);
                return null;
            }
        }
        return str;
    }

    private void writeToFile(MemoryCommitResult mcr) {
        if (this.mFile.exists()) {
            if (!mcr.changesMade) {
                mcr.setDiskWriteResult(true);
                return;
            } else if (this.mBackupFile.exists()) {
                this.mFile.delete();
            } else if (!this.mFile.renameTo(this.mBackupFile)) {
                Log.e(TAG, "Couldn't rename file " + this.mFile + " to backup file " + this.mBackupFile);
                mcr.setDiskWriteResult(false);
                return;
            }
        }
        try {
            FileOutputStream str = createFileOutputStream(this.mFile);
            if (str == null) {
                mcr.setDiskWriteResult(false);
                return;
            }
            XmlUtils.writeMapXml(mcr.mapToWriteToDisk, str);
            FileUtils.sync(str);
            str.close();
            ContextImpl.setFilePermissionsFromMode(this.mFile.getPath(), this.mMode, 0);
            try {
                StructStat stat = Os.stat(this.mFile.getPath());
                synchronized (this) {
                    this.mStatTimestamp = stat.st_mtime;
                    this.mStatSize = stat.st_size;
                }
            } catch (ErrnoException e) {
            }
            this.mBackupFile.delete();
            mcr.setDiskWriteResult(true);
        } catch (XmlPullParserException e2) {
            Log.w(TAG, "writeToFile: Got exception:", e2);
            if (this.mFile.exists() && !this.mFile.delete()) {
                Log.e(TAG, "Couldn't clean up partially-written file " + this.mFile);
            }
            mcr.setDiskWriteResult(false);
        } catch (IOException e3) {
            Log.w(TAG, "writeToFile: Got exception:", e3);
            Log.e(TAG, "Couldn't clean up partially-written file " + this.mFile);
            mcr.setDiskWriteResult(false);
        }
    }
}
