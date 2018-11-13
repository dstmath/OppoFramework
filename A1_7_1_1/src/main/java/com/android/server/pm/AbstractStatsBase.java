package com.android.server.pm;

import android.os.Environment;
import android.os.SystemClock;
import android.util.AtomicFile;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class AbstractStatsBase<T> {
    private static final int WRITE_INTERVAL_MS = 0;
    private final String mBackgroundThreadName;
    private final AtomicBoolean mBackgroundWriteRunning;
    private final Object mFileLock;
    private final String mFileName;
    private final AtomicLong mLastTimeWritten;
    private final boolean mLock;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.AbstractStatsBase.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.AbstractStatsBase.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.AbstractStatsBase.<clinit>():void");
    }

    protected abstract void readInternal(T t);

    protected abstract void writeInternal(T t);

    protected AbstractStatsBase(String fileName, String threadName, boolean lock) {
        this.mFileLock = new Object();
        this.mLastTimeWritten = new AtomicLong(0);
        this.mBackgroundWriteRunning = new AtomicBoolean(false);
        this.mFileName = fileName;
        this.mBackgroundThreadName = threadName;
        this.mLock = lock;
    }

    protected AtomicFile getFile() {
        return new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), this.mFileName));
    }

    void writeNow(T data) {
        writeImpl(data);
        this.mLastTimeWritten.set(SystemClock.elapsedRealtime());
    }

    boolean maybeWriteAsync(final T data) {
        if ((SystemClock.elapsedRealtime() - this.mLastTimeWritten.get() < ((long) WRITE_INTERVAL_MS) && !PackageManagerService.DEBUG_DEXOPT) || !this.mBackgroundWriteRunning.compareAndSet(false, true)) {
            return false;
        }
        new Thread(this.mBackgroundThreadName) {
            public void run() {
                try {
                    AbstractStatsBase.this.writeImpl(data);
                    AbstractStatsBase.this.mLastTimeWritten.set(SystemClock.elapsedRealtime());
                } finally {
                    AbstractStatsBase.this.mBackgroundWriteRunning.set(false);
                }
            }
        }.start();
        return true;
    }

    private void writeImpl(T data) {
        if (this.mLock) {
            synchronized (data) {
                synchronized (this.mFileLock) {
                    writeInternal(data);
                }
            }
            return;
        }
        synchronized (this.mFileLock) {
            writeInternal(data);
        }
    }

    void read(T data) {
        if (this.mLock) {
            synchronized (data) {
                synchronized (this.mFileLock) {
                    readInternal(data);
                }
            }
        } else {
            synchronized (this.mFileLock) {
                readInternal(data);
            }
        }
        this.mLastTimeWritten.set(SystemClock.elapsedRealtime());
    }
}
