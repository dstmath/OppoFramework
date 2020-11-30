package com.android.server.net;

import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.os.Binder;
import android.os.DropBoxManager;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.net.VpnInfo;
import com.android.internal.util.FileRotator;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.job.controllers.JobStatus;
import com.google.android.collect.Sets;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import libcore.io.IoUtils;

public class NetworkStatsRecorder {
    private static final boolean DUMP_BEFORE_DELETE = true;
    private static final boolean LOGD = false;
    private static final boolean LOGV = false;
    private static final String TAG = "NetworkStatsRecorder";
    private static final String TAG_NETSTATS_DUMP = "netstats_dump";
    private boolean mBackupForDebug;
    private final long mBucketDuration;
    private WeakReference<NetworkStatsCollection> mComplete;
    private final String mCookie;
    private long mCreateTime;
    private final DropBoxManager mDropBox;
    private NetworkStats mLastSnapshot;
    private boolean mLogOutDetail;
    private final NetworkStats.NonMonotonicObserver<String> mObserver;
    private final boolean mOnlyTags;
    private final NetworkStatsCollection mPending;
    private final CombiningRewriter mPendingRewriter;
    private long mPersistThresholdBytes;
    private final FileRotator mRotator;
    private final NetworkStatsCollection mSinceBoot;

    public NetworkStatsRecorder() {
        this.mPersistThresholdBytes = 2097152;
        this.mCreateTime = 0;
        this.mLogOutDetail = false;
        this.mBackupForDebug = false;
        this.mRotator = null;
        this.mObserver = null;
        this.mDropBox = null;
        this.mCookie = null;
        this.mBucketDuration = 31449600000L;
        this.mOnlyTags = false;
        this.mPending = null;
        this.mSinceBoot = new NetworkStatsCollection(this.mBucketDuration);
        this.mPendingRewriter = null;
    }

    public NetworkStatsRecorder(FileRotator rotator, NetworkStats.NonMonotonicObserver<String> observer, DropBoxManager dropBox, String cookie, long bucketDuration, boolean onlyTags) {
        this.mPersistThresholdBytes = 2097152;
        this.mCreateTime = 0;
        this.mLogOutDetail = false;
        this.mBackupForDebug = false;
        this.mRotator = (FileRotator) Preconditions.checkNotNull(rotator, "missing FileRotator");
        this.mObserver = (NetworkStats.NonMonotonicObserver) Preconditions.checkNotNull(observer, "missing NonMonotonicObserver");
        this.mDropBox = (DropBoxManager) Preconditions.checkNotNull(dropBox, "missing DropBoxManager");
        this.mCookie = cookie;
        this.mBucketDuration = bucketDuration;
        this.mOnlyTags = onlyTags;
        this.mPending = new NetworkStatsCollection(bucketDuration);
        this.mSinceBoot = new NetworkStatsCollection(bucketDuration);
        this.mPendingRewriter = new CombiningRewriter(this.mPending);
        this.mCreateTime = System.currentTimeMillis();
    }

    private String getPrefix() {
        return this.mCookie;
    }

    public void openSpecialDebug() {
        this.mLogOutDetail = true;
        this.mBackupForDebug = true;
    }

    public void setPersistThreshold(long thresholdBytes) {
        this.mPersistThresholdBytes = MathUtils.constrain(thresholdBytes, 1024, 104857600);
    }

    public void resetLocked() {
        this.mLastSnapshot = null;
        NetworkStatsCollection networkStatsCollection = this.mPending;
        if (networkStatsCollection != null) {
            networkStatsCollection.reset();
        }
        NetworkStatsCollection networkStatsCollection2 = this.mSinceBoot;
        if (networkStatsCollection2 != null) {
            networkStatsCollection2.reset();
        }
        WeakReference<NetworkStatsCollection> weakReference = this.mComplete;
        if (weakReference != null) {
            weakReference.clear();
        }
    }

    public NetworkStats.Entry getTotalSinceBootLocked(NetworkTemplate template) {
        return this.mSinceBoot.getSummary(template, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME, 3, Binder.getCallingUid()).getTotal((NetworkStats.Entry) null);
    }

    public NetworkStatsCollection getSinceBoot() {
        return this.mSinceBoot;
    }

    public NetworkStatsCollection getOrLoadCompleteLocked() {
        Preconditions.checkNotNull(this.mRotator, "missing FileRotator");
        WeakReference<NetworkStatsCollection> weakReference = this.mComplete;
        NetworkStatsCollection res = weakReference != null ? weakReference.get() : null;
        if (res == null) {
            if (this.mLogOutDetail) {
                Log.d(TAG, "getOrLoadCompleteLocked res is null");
            }
            res = loadLocked(Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME);
            if (this.mLogOutDetail) {
                Log.d(TAG, "getOrLoadCompleteLocked after load, res is:");
                res.logoutData("getOrLoadCompleteLocked");
            }
            this.mComplete = new WeakReference<>(res);
        }
        return res;
    }

    public NetworkStatsCollection getOrLoadPartialLocked(long start, long end) {
        Preconditions.checkNotNull(this.mRotator, "missing FileRotator");
        WeakReference<NetworkStatsCollection> weakReference = this.mComplete;
        NetworkStatsCollection res = weakReference != null ? weakReference.get() : null;
        if (res == null) {
            return loadLocked(start, end);
        }
        return res;
    }

    private NetworkStatsCollection loadLocked(long start, long end) {
        NetworkStatsCollection res = new NetworkStatsCollection(this.mBucketDuration);
        try {
            this.mRotator.readMatching(res, start, end);
            res.recordCollection(this.mPending);
        } catch (IOException e) {
            Log.e(TAG, "problem completely reading network stats", e);
            recoverFromWtf();
        } catch (OutOfMemoryError e2) {
            Log.wtf(TAG, "problem completely reading network stats", e2);
            recoverFromWtf();
        } catch (Exception e3) {
            Log.e(TAG, "problem completely reading network stats", e3);
            recoverFromWtf();
        }
        return res;
    }

    public void recordSnapshotLocked(NetworkStats snapshot, Map<String, NetworkIdentitySet> ifaceIdent, VpnInfo[] vpnArray, long currentTimeMillis) {
        NetworkStats delta;
        HashSet<String> unknownIfaces = Sets.newHashSet();
        if (snapshot != null) {
            if (NetworkStatsService.LOG_NET_STATS) {
                if (snapshot.size() > 0) {
                    Slog.d(TAG, "recordSnapshotLocked." + getPrefix() + ":snapshot size:" + snapshot.size());
                } else {
                    Slog.w(TAG, "recordSnapshotLocked." + getPrefix() + ":snapshot is empty.");
                }
            }
            if (this.mLastSnapshot == null) {
                this.mLastSnapshot = snapshot;
                if (NetworkStatsService.LOG_NET_STATS) {
                    Slog.w(TAG, "recordSnapshotLocked." + getPrefix() + ":first snapshot is bootstrap and don't record.");
                    return;
                }
                return;
            }
            WeakReference<NetworkStatsCollection> weakReference = this.mComplete;
            NetworkStatsCollection complete = weakReference != null ? weakReference.get() : null;
            NetworkStats delta2 = NetworkStats.subtract(snapshot, this.mLastSnapshot, this.mObserver, this.mCookie);
            long start = currentTimeMillis - delta2.getElapsedRealtime();
            if (vpnArray != null) {
                for (VpnInfo info : vpnArray) {
                    delta2.migrateTun(info.ownerUid, info.vpnIface, info.primaryUnderlyingIface);
                }
            }
            if (this.mLogOutDetail) {
                Log.d(TAG, "recordSnapshotLocked." + getPrefix() + " delta is bellow:");
                StringBuilder sb = new StringBuilder();
                sb.append(getPrefix());
                sb.append("-delta");
                delta2.logoutData(sb.toString());
            }
            NetworkStats.Entry entry = null;
            int i = 0;
            while (i < delta2.size()) {
                NetworkStats.Entry entry2 = delta2.getValues(i, entry);
                if (this.mLogOutDetail) {
                    Log.d(TAG, "recordSnapshotLocked:process entry[" + i + "]:" + entry2);
                }
                if (entry2.isNegative()) {
                    NetworkStats.NonMonotonicObserver<String> nonMonotonicObserver = this.mObserver;
                    if (nonMonotonicObserver != null) {
                        nonMonotonicObserver.foundNonMonotonic(delta2, i, this.mCookie);
                    }
                    entry2.rxBytes = Math.max(entry2.rxBytes, 0L);
                    entry2.rxPackets = Math.max(entry2.rxPackets, 0L);
                    entry2.txBytes = Math.max(entry2.txBytes, 0L);
                    entry2.txPackets = Math.max(entry2.txPackets, 0L);
                    entry2.operations = Math.max(entry2.operations, 0L);
                }
                NetworkIdentitySet ident = ifaceIdent.get(entry2.iface);
                if (ident == null) {
                    unknownIfaces.add(entry2.iface);
                    if (this.mLogOutDetail) {
                        Log.d(TAG, "recordSnapshotLocked:entry[" + i + "] has unknownIfaces, ignore.");
                        entry = entry2;
                        delta = delta2;
                    } else {
                        entry = entry2;
                        delta = delta2;
                    }
                } else if (entry2.isEmpty()) {
                    entry = entry2;
                    delta = delta2;
                } else if (entry2.isNegative()) {
                    Slog.e(TAG, "tried recording negative data:" + entry2);
                    entry = entry2;
                    delta = delta2;
                } else if ((entry2.tag == 0) != this.mOnlyTags) {
                    NetworkStatsCollection networkStatsCollection = this.mPending;
                    if (networkStatsCollection != null) {
                        networkStatsCollection.recordData(ident, entry2.uid, entry2.set, entry2.tag, start, currentTimeMillis, entry2);
                    }
                    if (this.mLogOutDetail) {
                        Slog.i(TAG, "recordSnapshotLocked: ident[" + ident + "]");
                    }
                    NetworkStatsCollection networkStatsCollection2 = this.mSinceBoot;
                    if (networkStatsCollection2 != null) {
                        networkStatsCollection2.recordData(ident, entry2.uid, entry2.set, entry2.tag, start, currentTimeMillis, entry2);
                    }
                    if (complete != null) {
                        delta = delta2;
                        complete.recordData(ident, entry2.uid, entry2.set, entry2.tag, start, currentTimeMillis, entry2);
                        entry = entry2;
                    } else {
                        delta = delta2;
                        entry = entry2;
                    }
                } else {
                    delta = delta2;
                    if (this.mLogOutDetail) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("recordSnapshotLocked:entry[");
                        sb2.append(i);
                        sb2.append("] tag not suit.entry.tag:");
                        entry = entry2;
                        sb2.append(entry.tag);
                        sb2.append(", mOnlyTags");
                        Log.d(TAG, sb2.toString());
                    } else {
                        entry = entry2;
                    }
                }
                i++;
                delta2 = delta;
            }
            this.mLastSnapshot = snapshot;
        } else if (NetworkStatsService.LOG_NET_STATS) {
            Slog.w(TAG, "recordSnapshotLocked." + getPrefix() + ":snapshot is null.");
        }
    }

    public void maybePersistLocked(long currentTimeMillis) {
        Preconditions.checkNotNull(this.mRotator, "missing FileRotator");
        if (this.mPending.getTotalBytes() >= this.mPersistThresholdBytes) {
            forcePersistLocked(currentTimeMillis);
        } else {
            this.mRotator.maybeRotate(currentTimeMillis);
        }
    }

    public void forcePersistLocked(long currentTimeMillis) {
        Preconditions.checkNotNull(this.mRotator, "missing FileRotator");
        if (this.mPending.isDirty()) {
            try {
                this.mRotator.rewriteActive(this.mPendingRewriter, currentTimeMillis);
                this.mRotator.maybeRotate(currentTimeMillis);
                this.mPending.reset();
            } catch (IOException e) {
                Log.e(TAG, "problem persisting pending stats", e);
                recoverFromWtf();
            } catch (OutOfMemoryError e2) {
                Log.wtf(TAG, "problem persisting pending stats", e2);
                recoverFromWtf();
            }
        }
    }

    public void removeUidsLocked(int[] uids) {
        FileRotator fileRotator = this.mRotator;
        if (fileRotator != null) {
            try {
                fileRotator.rewriteAll(new RemoveUidRewriter(this.mBucketDuration, uids));
            } catch (IOException e) {
                Log.e(TAG, "problem removing UIDs " + Arrays.toString(uids), e);
                recoverFromWtf();
            } catch (OutOfMemoryError e2) {
                Log.wtf(TAG, "problem removing UIDs " + Arrays.toString(uids), e2);
                recoverFromWtf();
            }
        }
        NetworkStatsCollection networkStatsCollection = this.mPending;
        if (networkStatsCollection != null) {
            networkStatsCollection.removeUids(uids);
        }
        NetworkStatsCollection networkStatsCollection2 = this.mSinceBoot;
        if (networkStatsCollection2 != null) {
            networkStatsCollection2.removeUids(uids);
        }
        NetworkStats networkStats = this.mLastSnapshot;
        if (networkStats != null) {
            networkStats.removeUids(uids);
        }
        WeakReference<NetworkStatsCollection> weakReference = this.mComplete;
        NetworkStatsCollection complete = weakReference != null ? weakReference.get() : null;
        if (complete != null) {
            complete.removeUids(uids);
        }
    }

    /* access modifiers changed from: private */
    public class CombiningRewriter implements FileRotator.Rewriter {
        private final NetworkStatsCollection mCollection;

        public CombiningRewriter(NetworkStatsCollection collection) {
            this.mCollection = (NetworkStatsCollection) Preconditions.checkNotNull(collection, "missing NetworkStatsCollection");
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
            NetworkStatsRecorder.this.writeToDebugBackupFile(this.mCollection);
            this.mCollection.reset();
        }
    }

    public void writeToDebugBackupFile(NetworkStatsCollection collection) {
        if (this.mBackupForDebug) {
            if (collection == null) {
                Slog.w(TAG, "writeToDebugBackupFile, collection is empty.");
                return;
            }
            try {
                FileWriter fileWriter = new FileWriter(new File("data/system/netstats/backup-" + this.mCookie + "-" + this.mCreateTime + ".txt"));
                BufferedWriter buffWriter = new BufferedWriter(fileWriter);
                collection.writeToBackupFile(buffWriter, this.mCookie);
                buffWriter.flush();
                buffWriter.close();
                fileWriter.close();
            } catch (IOException ioExce) {
                Slog.w(TAG, "writeToDebugBackupFile failed.", ioExce);
            } catch (Exception e) {
                Slog.w(TAG, "writeToDebugBackupFile failed.", e);
            }
        }
    }

    public static class RemoveUidRewriter implements FileRotator.Rewriter {
        private final NetworkStatsCollection mTemp;
        private final int[] mUids;

        public RemoveUidRewriter(long bucketDuration, int[] uids) {
            this.mTemp = new NetworkStatsCollection(bucketDuration);
            this.mUids = uids;
        }

        public void reset() {
            this.mTemp.reset();
        }

        public void read(InputStream in) throws IOException {
            this.mTemp.read(in);
            this.mTemp.clearDirty();
            this.mTemp.removeUids(this.mUids);
        }

        public boolean shouldWrite() {
            return this.mTemp.isDirty();
        }

        public void write(OutputStream out) throws IOException {
            this.mTemp.write(new DataOutputStream(out));
        }
    }

    public void importLegacyNetworkLocked(File file) throws IOException {
        Preconditions.checkNotNull(this.mRotator, "missing FileRotator");
        this.mRotator.deleteAll();
        NetworkStatsCollection collection = new NetworkStatsCollection(this.mBucketDuration);
        collection.readLegacyNetwork(file);
        long startMillis = collection.getStartMillis();
        long endMillis = collection.getEndMillis();
        if (!collection.isEmpty()) {
            this.mRotator.rewriteActive(new CombiningRewriter(collection), startMillis);
            this.mRotator.maybeRotate(endMillis);
        }
    }

    public void importLegacyUidLocked(File file) throws IOException {
        Preconditions.checkNotNull(this.mRotator, "missing FileRotator");
        this.mRotator.deleteAll();
        NetworkStatsCollection collection = new NetworkStatsCollection(this.mBucketDuration);
        collection.readLegacyUid(file, this.mOnlyTags);
        long startMillis = collection.getStartMillis();
        long endMillis = collection.getEndMillis();
        if (!collection.isEmpty()) {
            this.mRotator.rewriteActive(new CombiningRewriter(collection), startMillis);
            this.mRotator.maybeRotate(endMillis);
        }
    }

    public void dumpLocked(IndentingPrintWriter pw, boolean fullHistory) {
        if (this.mPending != null) {
            pw.print("Pending bytes: ");
            pw.println(this.mPending.getTotalBytes());
        }
        if (fullHistory) {
            pw.println("Complete history:");
            getOrLoadCompleteLocked().dump(pw);
            return;
        }
        pw.println("History since boot:");
        this.mSinceBoot.dump(pw);
    }

    public void writeToProtoLocked(ProtoOutputStream proto, long tag) {
        long start = proto.start(tag);
        NetworkStatsCollection networkStatsCollection = this.mPending;
        if (networkStatsCollection != null) {
            proto.write(1112396529665L, networkStatsCollection.getTotalBytes());
        }
        getOrLoadCompleteLocked().writeToProto(proto, 1146756268034L);
        proto.end(start);
    }

    public void dumpCheckin(PrintWriter pw, long start, long end) {
        getOrLoadPartialLocked(start, end).dumpCheckin(pw, start, end);
    }

    private void recoverFromWtf() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            this.mRotator.dumpAll(os);
        } catch (IOException e) {
            os.reset();
        } catch (Throwable th) {
            IoUtils.closeQuietly(os);
            throw th;
        }
        IoUtils.closeQuietly(os);
        this.mDropBox.addData(TAG_NETSTATS_DUMP, os.toByteArray(), 0);
        this.mRotator.deleteAll();
    }
}
