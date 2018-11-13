package com.android.server.net;

import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.net.NetworkStats.NonMonotonicObserver;
import android.net.NetworkTemplate;
import android.os.DropBoxManager;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import com.android.internal.net.VpnInfo;
import com.android.internal.util.FileRotator;
import com.android.internal.util.FileRotator.Rewriter;
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
    private static final boolean LOGD = true;
    private static final boolean LOGV = true;
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
    private final NonMonotonicObserver<String> mObserver;
    private final boolean mOnlyTags;
    private final NetworkStatsCollection mPending;
    private final CombiningRewriter mPendingRewriter;
    private long mPersistThresholdBytes;
    private final FileRotator mRotator;
    private final NetworkStatsCollection mSinceBoot;

    private class CombiningRewriter implements Rewriter {
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

    public static class RemoveUidRewriter implements Rewriter {
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

    public NetworkStatsRecorder(FileRotator rotator, NonMonotonicObserver<String> observer, DropBoxManager dropBox, String cookie, long bucketDuration, boolean onlyTags) {
        this.mPersistThresholdBytes = 2097152;
        this.mCreateTime = 0;
        this.mLogOutDetail = false;
        this.mBackupForDebug = false;
        this.mRotator = (FileRotator) Preconditions.checkNotNull(rotator, "missing FileRotator");
        this.mObserver = (NonMonotonicObserver) Preconditions.checkNotNull(observer, "missing NonMonotonicObserver");
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
        if (this.mLogOutDetail) {
            Slog.v(TAG, "setPersistThreshold() with " + thresholdBytes);
        }
        this.mPersistThresholdBytes = MathUtils.constrain(thresholdBytes, 1024, 104857600);
    }

    public void resetLocked() {
        this.mLastSnapshot = null;
        if (this.mPending != null) {
            this.mPending.reset();
        }
        if (this.mSinceBoot != null) {
            this.mSinceBoot.reset();
        }
        if (this.mComplete != null) {
            this.mComplete.clear();
        }
    }

    public Entry getTotalSinceBootLocked(NetworkTemplate template) {
        return this.mSinceBoot.getSummary(template, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME, 3).getTotal(null);
    }

    public NetworkStatsCollection getSinceBoot() {
        return this.mSinceBoot;
    }

    public NetworkStatsCollection getOrLoadCompleteLocked() {
        NetworkStatsCollection res = null;
        Preconditions.checkNotNull(this.mRotator, "missing FileRotator");
        if (this.mComplete != null) {
            res = (NetworkStatsCollection) this.mComplete.get();
        }
        if (res == null) {
            if (this.mLogOutDetail) {
                Log.d(TAG, "getOrLoadCompleteLocked res is null");
            }
            res = loadLocked(Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME);
            if (this.mLogOutDetail) {
                Log.d(TAG, "getOrLoadCompleteLocked after load, res is:");
                res.logoutData("getOrLoadCompleteLocked");
            }
            this.mComplete = new WeakReference(res);
        }
        return res;
    }

    public NetworkStatsCollection getOrLoadPartialLocked(long start, long end) {
        NetworkStatsCollection res = null;
        Preconditions.checkNotNull(this.mRotator, "missing FileRotator");
        if (this.mComplete != null) {
            res = (NetworkStatsCollection) this.mComplete.get();
        }
        if (res == null) {
            return loadLocked(start, end);
        }
        return res;
    }

    private NetworkStatsCollection loadLocked(long start, long end) {
        if (this.mLogOutDetail) {
            Slog.d(TAG, "loadLocked() reading from disk for " + this.mCookie);
        }
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
        HashSet<String> unknownIfaces = Sets.newHashSet();
        if (snapshot == null) {
            if (NetworkStatsService.LOG_NET_STATS) {
                Slog.w(TAG, "recordSnapshotLocked." + getPrefix() + ":snapshot is null.");
            }
            return;
        }
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
            }
            return;
        }
        NetworkStatsCollection complete = this.mComplete != null ? (NetworkStatsCollection) this.mComplete.get() : null;
        NetworkStats delta = NetworkStats.subtract(snapshot, this.mLastSnapshot, this.mObserver, this.mCookie);
        long end = currentTimeMillis;
        long start = currentTimeMillis - delta.getElapsedRealtime();
        if (vpnArray != null) {
            if (this.mLogOutDetail) {
                Slog.d(TAG, "vpnArray:" + vpnArray);
            }
            for (VpnInfo info : vpnArray) {
                delta.migrateTun(info.ownerUid, info.vpnIface, info.primaryUnderlyingIface);
            }
        }
        if (this.mLogOutDetail) {
            Log.d(TAG, "recordSnapshotLocked." + getPrefix() + " delta is bellow:");
            delta.logoutData(getPrefix() + "-delta");
        }
        Entry entry = null;
        for (int i = 0; i < delta.size(); i++) {
            entry = delta.getValues(i, entry);
            if (entry.iface != null) {
                NetworkIdentitySet ident = (NetworkIdentitySet) ifaceIdent.get(entry.iface);
                if (ident == null) {
                    unknownIfaces.add(entry.iface);
                    if (this.mLogOutDetail) {
                        Log.d(TAG, "recordSnapshotLocked:entry[" + i + "] has unknownIfaces, ignore.");
                    }
                } else if (entry.isEmpty()) {
                    if (this.mLogOutDetail) {
                        Log.d(TAG, "recordSnapshotLocked:entry[" + i + "] is empty, ignore.");
                    }
                } else if (entry.isNegative()) {
                    Slog.e(TAG, "tried recording negative data:" + entry);
                } else {
                    if ((entry.tag == 0) != this.mOnlyTags) {
                        if (this.mPending != null) {
                            this.mPending.recordData(ident, entry.uid, entry.set, entry.tag, start, currentTimeMillis, entry);
                        }
                        if (this.mLogOutDetail) {
                            Slog.i(TAG, "recordSnapshotLocked: ident[" + ident + "]");
                        }
                        if (this.mSinceBoot != null) {
                            this.mSinceBoot.recordData(ident, entry.uid, entry.set, entry.tag, start, currentTimeMillis, entry);
                        }
                        if (complete != null) {
                            complete.recordData(ident, entry.uid, entry.set, entry.tag, start, currentTimeMillis, entry);
                        }
                    } else if (this.mLogOutDetail) {
                        Log.d(TAG, "recordSnapshotLocked:entry[" + i + "] tag not suit.entry.tag:" + entry.tag + ", mOnlyTags");
                    }
                }
            }
        }
        this.mLastSnapshot = snapshot;
        if (this.mLogOutDetail && unknownIfaces.size() > 0) {
            Slog.w(TAG, "unknown interfaces " + unknownIfaces + ", ignoring those stats");
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
            if (this.mLogOutDetail) {
                Slog.d(TAG, "forcePersistLocked() writing for " + this.mCookie);
            }
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
        NetworkStatsCollection complete = null;
        if (this.mRotator != null) {
            try {
                this.mRotator.rewriteAll(new RemoveUidRewriter(this.mBucketDuration, uids));
            } catch (IOException e) {
                Log.e(TAG, "problem removing UIDs " + Arrays.toString(uids), e);
                recoverFromWtf();
            } catch (OutOfMemoryError e2) {
                Log.wtf(TAG, "problem removing UIDs " + Arrays.toString(uids), e2);
                recoverFromWtf();
            }
        }
        if (this.mPending != null) {
            this.mPending.removeUids(uids);
        }
        if (this.mSinceBoot != null) {
            this.mSinceBoot.removeUids(uids);
        }
        if (this.mLastSnapshot != null) {
            this.mLastSnapshot = this.mLastSnapshot.withoutUids(uids);
        }
        if (this.mComplete != null) {
            complete = (NetworkStatsCollection) this.mComplete.get();
        }
        if (complete != null) {
            complete.removeUids(uids);
        }
    }

    public void writeToDebugBackupFile(NetworkStatsCollection collection) {
        if (!this.mBackupForDebug) {
            return;
        }
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

    public void dumpCheckin(PrintWriter pw, long start, long end) {
        getOrLoadPartialLocked(start, end).dumpCheckin(pw, start, end);
    }

    private void recoverFromWtf() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            this.mRotator.dumpAll(os);
        } catch (IOException e) {
            os.reset();
        } finally {
            IoUtils.closeQuietly(os);
        }
        this.mDropBox.addData(TAG_NETSTATS_DUMP, os.toByteArray(), 0);
        this.mRotator.deleteAll();
    }
}
