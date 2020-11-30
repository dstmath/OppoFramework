package com.android.server.oppo;

import android.os.Environment;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.FileRotator;
import com.android.internal.util.Preconditions;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Iterator;

public class OppoJunkRecorder {
    private static final int FILE_MAGIC = 1247104587;
    public static final String JUNK_ENABLE_PROP = "persist.sys.oppo.junklog";
    public static final String JUNK_RECORD_BASEDIR = "junk";
    public static final long JUNK_RECORD_DELETE_AGE = 432000000;
    public static final long JUNK_RECORD_ROTATE_AGE = 86400000;
    private static final boolean LOGD = false;
    private static final boolean LOGV = false;
    private static final String TAG = "OppoJunkRecorder";
    private static final int VERSION_UNIFIED_INIT = 16;
    private static OppoJunkRecorder sInstance;
    public static Object sLock = new Object();
    private boolean mEnable = false;
    private final OppoJunkRecordCollection mPending;
    private final CombiningRewriter mPendingRewriter;
    private long mPersistThresholdBytes = 102400;
    private final FileRotator mRotator;

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
        File baseDir = new File(new File(Environment.getDataDirectory(), "system"), JUNK_RECORD_BASEDIR);
        baseDir.mkdirs();
        this.mRotator = new FileRotator(baseDir, JUNK_RECORD_BASEDIR, 86400000, (long) JUNK_RECORD_DELETE_AGE);
        this.mPending = new OppoJunkRecordCollection();
        this.mPendingRewriter = new CombiningRewriter(this.mPending);
        this.mEnable = SystemProperties.getBoolean(JUNK_ENABLE_PROP, false);
    }

    /* access modifiers changed from: private */
    public static class OppoJunkRecordCollection implements FileRotator.Reader {
        private ArrayList<OppoJunkRecordCollectionData> mDatas = new ArrayList<>();
        private boolean mDirty;
        private long mTotalBytes;

        /* access modifiers changed from: private */
        public static class OppoJunkRecordCollectionData {
            public String content;
            public String packageName;
            public long timeMillis;
            public String type;

            public OppoJunkRecordCollectionData(long currentTimeMillis, String type2, String packageName2, String content2) {
                String str = "N/A";
                this.type = type2 == null ? str : type2;
                this.content = content2 == null ? str : content2;
                this.packageName = packageName2 != null ? packageName2 : str;
                this.timeMillis = currentTimeMillis;
            }

            public int getBytes() {
                String str = this.type;
                int i = 0;
                int result = 0 + (str != null ? str.getBytes().length : 0);
                String str2 = this.content;
                int result2 = result + (str2 != null ? str2.getBytes().length : 0);
                String str3 = this.packageName;
                if (str3 != null) {
                    i = str3.getBytes().length;
                }
                return result2 + i + 8;
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
            if (magic == OppoJunkRecorder.FILE_MAGIC) {
                int version = in.readInt();
                if (version == 16) {
                    int size = in.readInt();
                    for (int i = 0; i < size; i++) {
                        recordData(in.readLong(), in.readUTF(), in.readUTF(), in.readUTF());
                    }
                    return;
                }
                throw new ProtocolException("unexpected version: " + version);
            }
            throw new ProtocolException("unexpected magic: " + magic);
        }

        public void write(DataOutputStream out) throws IOException {
            out.writeInt(OppoJunkRecorder.FILE_MAGIC);
            out.writeInt(16);
            out.writeInt(this.mDatas.size());
            Iterator<OppoJunkRecordCollectionData> it = this.mDatas.iterator();
            while (it.hasNext()) {
                OppoJunkRecordCollectionData data = it.next();
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

    /* access modifiers changed from: private */
    public static class CombiningRewriter implements FileRotator.Rewriter {
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
        this.mEnable = SystemProperties.getBoolean(JUNK_ENABLE_PROP, false);
    }
}
