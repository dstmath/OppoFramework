package com.android.server.oppo;

import android.os.Environment;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.FileRotator;
import com.android.internal.util.FileRotator.Reader;
import com.android.internal.util.FileRotator.Rewriter;
import com.android.internal.util.Preconditions;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.util.ArrayList;

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
public class OppoJunkRecorder {
    private static final int FILE_MAGIC = 1247104587;
    public static final String JUNK_RECORD_BASEDIR = "junk";
    public static final long JUNK_RECORD_DELETE_AGE = 432000000;
    public static final long JUNK_RECORD_ROTATE_AGE = 86400000;
    private static final boolean LOGD = false;
    private static final boolean LOGV = false;
    private static final String TAG = "OppoJunkRecorder";
    private static final int VERSION_UNIFIED_INIT = 16;
    private static OppoJunkRecorder sInstance;
    public static Object sLock;
    private boolean mEnable;
    private final OppoJunkRecordCollection mPending;
    private final CombiningRewriter mPendingRewriter;
    private long mPersistThresholdBytes;
    private final FileRotator mRotator;

    private static class CombiningRewriter implements Rewriter {
        private final OppoJunkRecordCollection mCollection;

        public CombiningRewriter(OppoJunkRecordCollection collection) {
            this.mCollection = (OppoJunkRecordCollection) Preconditions.checkNotNull(collection, "missing OppoJunkRecordCollection");
        }

        public void reset() {
        }

        public void read(InputStream in) throws IOException {
            this.mCollection.read(in);
        }

        public boolean shouldWrite() {
            return true;
        }

        public void write(OutputStream out) throws IOException {
            this.mCollection.write(new DataOutputStream(out));
            this.mCollection.reset();
        }
    }

    private static class OppoJunkRecordCollection implements Reader {
        private ArrayList<OppoJunkRecordCollectionData> mDatas = new ArrayList();
        private boolean mDirty;
        private long mTotalBytes;

        private static class OppoJunkRecordCollectionData {
            public String content;
            public String packageName;
            public long timeMillis;
            public String type;

            public OppoJunkRecordCollectionData(long currentTimeMillis, String type, String packageName, String content) {
                if (type == null) {
                    type = "N/A";
                }
                this.type = type;
                if (content == null) {
                    content = "N/A";
                }
                this.content = content;
                if (packageName == null) {
                    packageName = "N/A";
                }
                this.packageName = packageName;
                this.timeMillis = currentTimeMillis;
            }

            public int getBytes() {
                int length;
                int i = 0;
                if (this.type != null) {
                    length = this.type.getBytes().length;
                } else {
                    length = 0;
                }
                int result = length + 0;
                if (this.content != null) {
                    length = this.content.getBytes().length;
                } else {
                    length = 0;
                }
                result += length;
                if (this.packageName != null) {
                    i = this.packageName.getBytes().length;
                }
                return (result + i) + 8;
            }
        }

        public OppoJunkRecordCollection() {
            reset();
        }

        public void reset() {
            this.mDatas.clear();
            this.mTotalBytes = 0;
            this.mDirty = false;
        }

        public boolean isDirty() {
            return this.mDirty;
        }

        public void clearDirty() {
            this.mDirty = false;
        }

        public long getTotalBytes() {
            return this.mTotalBytes;
        }

        public void read(InputStream in) throws IOException {
            read(new DataInputStream(in));
        }

        public void read(DataInputStream in) throws IOException {
            int magic = in.readInt();
            if (magic != OppoJunkRecorder.FILE_MAGIC) {
                throw new ProtocolException("unexpected magic: " + magic);
            }
            int version = in.readInt();
            switch (version) {
                case 16:
                    int size = in.readInt();
                    for (int i = 0; i < size; i++) {
                        recordData(in.readLong(), in.readUTF(), in.readUTF(), in.readUTF());
                    }
                    return;
                default:
                    throw new ProtocolException("unexpected version: " + version);
            }
        }

        public void write(DataOutputStream out) throws IOException {
            out.writeInt(OppoJunkRecorder.FILE_MAGIC);
            out.writeInt(16);
            out.writeInt(this.mDatas.size());
            for (OppoJunkRecordCollectionData data : this.mDatas) {
                out.writeLong(data.timeMillis);
                out.writeUTF(data.type);
                out.writeUTF(data.packageName);
                out.writeUTF(data.content);
            }
            out.flush();
        }

        public void recordData(long currentTimeMillis, String type, String packageName, String content) {
            OppoJunkRecordCollectionData data = new OppoJunkRecordCollectionData(currentTimeMillis, type, packageName, content);
            this.mDatas.add(data);
            this.mTotalBytes += (long) data.getBytes();
            this.mDirty = true;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.OppoJunkRecorder.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.OppoJunkRecorder.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.oppo.OppoJunkRecorder.<clinit>():void");
    }

    public static OppoJunkRecorder getInstance() {
        OppoJunkRecorder oppoJunkRecorder;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new OppoJunkRecorder();
            }
            oppoJunkRecorder = sInstance;
        }
        return oppoJunkRecorder;
    }

    private OppoJunkRecorder() {
        this.mPersistThresholdBytes = 102400;
        this.mEnable = false;
        File baseDir = new File(new File(Environment.getDataDirectory(), "system"), JUNK_RECORD_BASEDIR);
        baseDir.mkdirs();
        this.mRotator = new FileRotator(baseDir, JUNK_RECORD_BASEDIR, 86400000, JUNK_RECORD_DELETE_AGE);
        this.mPending = new OppoJunkRecordCollection();
        this.mPendingRewriter = new CombiningRewriter(this.mPending);
        this.mEnable = SystemProperties.getBoolean("persist.sys.oppo.junklog", false);
    }

    public void maybePersist(long currentTimeMillis) {
        synchronized (sLock) {
            if (this.mPending.getTotalBytes() >= this.mPersistThresholdBytes) {
                forcePersist(currentTimeMillis);
            } else {
                this.mRotator.maybeRotate(currentTimeMillis);
            }
        }
    }

    public void forcePersist(long currentTimeMillis) {
        synchronized (sLock) {
            if (this.mPending.isDirty()) {
                try {
                    this.mRotator.rewriteActive(this.mPendingRewriter, currentTimeMillis);
                    this.mRotator.maybeRotate(currentTimeMillis);
                    this.mPending.reset();
                } catch (IOException e) {
                    Log.wtf(TAG, "problem persisting pending stats", e);
                } catch (OutOfMemoryError e2) {
                    Log.wtf(TAG, "problem persisting pending stats", e2);
                }
            }
        }
        return;
    }

    public void reportJunkEvent(String type, String packageName, String content) {
        if (this.mEnable) {
            long currentTimeMillis = System.currentTimeMillis();
            Slog.d(TAG, "reportJunkFromApp:" + type + ":" + packageName);
            synchronized (sLock) {
                this.mPending.recordData(currentTimeMillis, type, packageName, content);
                maybePersist(currentTimeMillis);
            }
        }
    }

    public void reset() {
        synchronized (sLock) {
            this.mPending.reset();
        }
    }

    public boolean isEnable() {
        return this.mEnable;
    }

    public void resetEnable() {
        this.mEnable = SystemProperties.getBoolean("persist.sys.oppo.junklog", false);
    }
}
