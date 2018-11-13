package com.android.server.net;

import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.net.NetworkStats.NonMonotonicObserver;
import android.net.NetworkTemplate;
import android.os.DropBoxManager;
import android.util.Log;
import android.util.MathUtils;
import com.android.internal.net.VpnInfo;
import com.android.internal.util.FileRotator;
import com.android.internal.util.FileRotator.Rewriter;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.job.controllers.JobStatus;
import com.android.server.storage.OppoDeviceStorageMonitorService;
import com.google.android.collect.Sets;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import libcore.io.IoUtils;

public class NetworkStatsRecorderWithProcInfo {
    private static final boolean DUMP_BEFORE_DELETE = true;
    private static final boolean LOGD = false;
    private static final boolean LOGV = false;
    private static final String TAG = "NetworkStatsRecorderWithProcInfo";
    private static final String TAG_NETSTATS_DUMP = "netstats_dump";
    private boolean mActiveIfacesHasInit = false;
    private final long mBucketDuration;
    private WeakReference<NetworkStatsCollectionWithProcInfo> mComplete;
    private final String mCookie;
    private final DropBoxManager mDropBox;
    private NetworkStats mLastSnapshot;
    private final NonMonotonicObserver<String> mObserver;
    private final boolean mOnlyTags;
    private final NetworkStatsCollectionWithProcInfo mPending;
    private final CombiningRewriter mPendingRewriter;
    private long mPersistThresholdBytes = 2097152;
    private final FileRotator mRotator;
    private final NetworkStatsCollectionWithProcInfo mSinceBoot;

    private static class CombiningRewriter implements Rewriter {
        private final NetworkStatsCollectionWithProcInfo mCollection;

        public CombiningRewriter(NetworkStatsCollectionWithProcInfo collection) {
            this.mCollection = (NetworkStatsCollectionWithProcInfo) Preconditions.checkNotNull(collection, "missing NetworkStatsCollectionWithProcInfo");
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

    public static class RemoveUidRewriter implements Rewriter {
        private final NetworkStatsCollectionWithProcInfo mTemp;
        private final int[] mUids;

        public RemoveUidRewriter(long bucketDuration, int[] uids) {
            this.mTemp = new NetworkStatsCollectionWithProcInfo(bucketDuration);
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

    public NetworkStatsRecorderWithProcInfo(FileRotator rotator, NonMonotonicObserver<String> observer, DropBoxManager dropBox, String cookie, long bucketDuration, boolean onlyTags) {
        this.mRotator = (FileRotator) Preconditions.checkNotNull(rotator, "missing FileRotator");
        this.mObserver = (NonMonotonicObserver) Preconditions.checkNotNull(observer, "missing NonMonotonicObserver");
        this.mDropBox = (DropBoxManager) Preconditions.checkNotNull(dropBox, "missing DropBoxManager");
        this.mCookie = cookie;
        this.mBucketDuration = bucketDuration;
        this.mOnlyTags = onlyTags;
        this.mPending = new NetworkStatsCollectionWithProcInfo(bucketDuration);
        this.mSinceBoot = new NetworkStatsCollectionWithProcInfo(bucketDuration);
        this.mPendingRewriter = new CombiningRewriter(this.mPending);
    }

    private String getPrefix() {
        return this.mCookie;
    }

    public void setPersistThreshold(long thresholdBytes) {
        this.mPersistThresholdBytes = MathUtils.constrain(thresholdBytes, OppoDeviceStorageMonitorService.KB_BYTES, 104857600);
    }

    public void resetLocked() {
        this.mLastSnapshot = null;
        this.mPending.reset();
        this.mSinceBoot.reset();
        this.mComplete.clear();
    }

    public void resetLastSnapshot(NetworkStats snapshotAfterReset) {
        this.mLastSnapshot = snapshotAfterReset;
    }

    public Entry getTotalSinceBootLocked(NetworkTemplate template) {
        return this.mSinceBoot.getSummary(template, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME).getTotal(null);
    }

    public NetworkStatsCollectionWithProcInfo getOrLoadCompleteLocked() {
        IOException e;
        NetworkStatsCollectionWithProcInfo complete = this.mComplete != null ? (NetworkStatsCollectionWithProcInfo) this.mComplete.get() : null;
        if (complete == null) {
            if (NetworkStatsService.LOG_NET_STATS_PROC) {
                Log.d(TAG, "getOrLoadCompleteLocked() reading from disk for " + this.mCookie);
            }
            NetworkStatsCollectionWithProcInfo complete2;
            try {
                complete2 = new NetworkStatsCollectionWithProcInfo(this.mBucketDuration);
                try {
                    this.mRotator.readMatching(complete2, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME);
                    if (NetworkStatsService.LOG_NET_STATS_PROC) {
                        complete2.logoutData("pid-complete-before-record");
                    }
                    complete2.recordCollection(this.mPending);
                    if (NetworkStatsService.LOG_NET_STATS_PROC) {
                        complete2.logoutData("pid-complete-after-record");
                    }
                    this.mComplete = new WeakReference(complete2);
                    return complete2;
                } catch (IOException e2) {
                    e = e2;
                    Log.wtf(TAG, "problem completely reading network stats", e);
                    recoverFromWtf();
                    return complete2;
                }
            } catch (IOException e3) {
                e = e3;
                complete2 = complete;
                Log.wtf(TAG, "problem completely reading network stats", e);
                recoverFromWtf();
                return complete2;
            }
        } else if (!NetworkStatsService.LOG_NET_STATS_PROC) {
            return complete;
        } else {
            Log.d(TAG, "getOrLoadCompleteLocked() return complete directly.");
            return complete;
        }
    }

    public void recordSnapshotLocked(NetworkStats snapshot, Map<String, NetworkIdentitySet> ifaceIdent, VpnInfo[] vpnArray, long currentTimeMillis) {
        HashSet<String> unknownIfaces = Sets.newHashSet();
        if (snapshot == null) {
            if (NetworkStatsService.LOG_NET_STATS_PROC) {
                Log.w(TAG, "recordSnapshotLocked." + getPrefix() + ":snapshot is null.");
            }
            return;
        }
        if (NetworkStatsService.LOG_NET_STATS_PROC) {
            if (snapshot.size() > 0) {
                Log.d(TAG, "recordSnapshotLocked." + getPrefix() + ":snapshot size:" + snapshot.size());
            } else {
                Log.w(TAG, "recordSnapshotLocked." + getPrefix() + ":snapshot is empty.");
            }
        }
        if (this.mLastSnapshot == null) {
            this.mLastSnapshot = snapshot;
            Log.w(TAG, "recordSnapshotLocked." + getPrefix() + ":first snapshot is bootstrap and don't record.");
            if (NetworkStatsService.LOG_NET_STATS_PROC) {
                Log.d(TAG, "recordSnapshotLocked." + getPrefix() + " first snapshot is bootstrap, content is:");
                this.mLastSnapshot.logoutData("first-snapshot");
            }
            return;
        }
        if (NetworkStatsService.LOG_NET_STATS_PROC) {
            Log.d(TAG, "recordSnapshotLocked." + getPrefix() + " mLastSnapshot is bellow:");
            this.mLastSnapshot.logoutData("pid-lastSnapshot");
        }
        NetworkStatsCollectionWithProcInfo complete = this.mComplete != null ? (NetworkStatsCollectionWithProcInfo) this.mComplete.get() : null;
        NetworkStats delta = NetworkStats.subtract(snapshot, this.mLastSnapshot, this.mObserver, this.mCookie);
        if (NetworkStatsService.LOG_NET_STATS_PROC) {
            Log.d(TAG, "recordSnapshotLocked." + getPrefix() + " delta is bellow:");
            delta.logoutData(getPrefix() + "-delta");
        }
        long end = currentTimeMillis;
        long start = currentTimeMillis - delta.getElapsedRealtime();
        if (vpnArray != null) {
            for (VpnInfo info : vpnArray) {
                delta.migrateTun(info.ownerUid, info.vpnIface, info.primaryUnderlyingIface);
            }
        }
        if (NetworkStatsService.LOG_NET_STATS_PROC) {
            this.mPending.logoutData("mPending-01");
            Log.d(TAG, "recordSnapshotLocked." + getPrefix() + " mSinceBoot is null ? :" + (this.mSinceBoot == null));
        }
        Entry entry = null;
        for (int i = 0; i < delta.size(); i++) {
            entry = delta.getValues(i, entry);
            if (NetworkStatsService.LOG_NET_STATS_PROC) {
                Log.d(TAG, "recordSnapshotLocked:process entry[" + i + "]:" + entry);
            }
            NetworkIdentitySet ident = (NetworkIdentitySet) ifaceIdent.get(entry.iface);
            if (ident == null) {
                if (NetworkStatsService.LOG_NET_STATS_PROC) {
                    Log.d(TAG, "recordSnapshotLocked:entry[" + i + "] has unknownIfaces, ignore.");
                }
                unknownIfaces.add(entry.iface);
            } else if (!entry.isEmpty()) {
                if ((entry.tag == 0) != this.mOnlyTags) {
                    if (NetworkStatsService.LOG_NET_STATS_PROC) {
                        Log.d(TAG, "recordData to mPending-01, entry[" + i + "]:" + entry);
                    }
                    this.mPending.recordData(ident, entry.uid, entry.set, entry.tag, start, currentTimeMillis, entry);
                    if (NetworkStatsService.LOG_NET_STATS_PROC) {
                        Log.d(TAG, "recordData to mPending-02, entry[" + i + "]:" + entry);
                    }
                    if (this.mSinceBoot != null) {
                        if (NetworkStatsService.LOG_NET_STATS_PROC) {
                            Log.d(TAG, "recordData to mSinceBoot-01, entry[" + i + "]:" + entry);
                        }
                        this.mSinceBoot.recordData(ident, entry.uid, entry.set, entry.tag, start, currentTimeMillis, entry);
                        if (NetworkStatsService.LOG_NET_STATS_PROC) {
                            Log.d(TAG, "recordData to mSinceBoot-02, entry[" + i + "]:" + entry);
                        }
                    }
                    if (complete != null) {
                        if (NetworkStatsService.LOG_NET_STATS_PROC) {
                            Log.d(TAG, "recordData to complete-01, entry[" + i + "]:" + entry);
                        }
                        complete.recordData(ident, entry.uid, entry.set, entry.tag, start, currentTimeMillis, entry);
                        if (NetworkStatsService.LOG_NET_STATS_PROC) {
                            Log.d(TAG, "recordData to complete-02, entry[" + i + "]:" + entry);
                        }
                    }
                } else if (NetworkStatsService.LOG_NET_STATS_PROC) {
                    Log.d(TAG, "recordSnapshotLocked:entry[" + i + "] tag not suit.entry.tag:" + entry.tag + ", mOnlyTags");
                }
            } else if (NetworkStatsService.LOG_NET_STATS_PROC) {
                Log.d(TAG, "recordSnapshotLocked:entry[" + i + "] is empty, ignore.");
            }
        }
        if (NetworkStatsService.LOG_NET_STATS_PROC) {
            this.mPending.logoutData("mPending-02");
        }
        if (this.mActiveIfacesHasInit) {
            this.mLastSnapshot = snapshot;
        }
    }

    public void maybePersistLocked(long currentTimeMillis) {
        if (this.mPending.getTotalBytes() >= this.mPersistThresholdBytes) {
            forcePersistLocked(currentTimeMillis);
        } else {
            this.mRotator.maybeRotate(currentTimeMillis);
        }
    }

    public void forcePersistLocked(long currentTimeMillis) {
        if (this.mPending.isDirty()) {
            try {
                this.mRotator.rewriteActive(this.mPendingRewriter, currentTimeMillis);
                this.mRotator.maybeRotate(currentTimeMillis);
                this.mPending.reset();
            } catch (IOException e) {
                Log.w(TAG, "problem persisting pending stats", e);
                recoverFromWtf();
            }
        }
    }

    public void removeUidsLocked(int[] uids) {
        try {
            this.mRotator.rewriteAll(new RemoveUidRewriter(this.mBucketDuration, uids));
        } catch (IOException e) {
            Log.wtf(TAG, "problem removing UIDs " + Arrays.toString(uids), e);
            recoverFromWtf();
        }
        this.mPending.removeUids(uids);
        this.mSinceBoot.removeUids(uids);
        if (this.mLastSnapshot != null) {
            this.mLastSnapshot = this.mLastSnapshot.withoutUids(uids);
        }
        NetworkStatsCollectionWithProcInfo complete = this.mComplete != null ? (NetworkStatsCollectionWithProcInfo) this.mComplete.get() : null;
        if (complete != null) {
            complete.removeUids(uids);
        }
    }

    public void setActiveIfacesInitState(boolean state) {
        this.mActiveIfacesHasInit = state;
    }

    public void importLegacyNetworkLocked(File file) throws IOException {
        this.mRotator.deleteAll();
        NetworkStatsCollectionWithProcInfo collection = new NetworkStatsCollectionWithProcInfo(this.mBucketDuration);
        collection.readLegacyNetwork(file);
        long startMillis = collection.getStartMillis();
        long endMillis = collection.getEndMillis();
        if (!collection.isEmpty()) {
            this.mRotator.rewriteActive(new CombiningRewriter(collection), startMillis);
            this.mRotator.maybeRotate(endMillis);
        }
    }

    public void importLegacyUidLocked(File file) throws IOException {
        this.mRotator.deleteAll();
        NetworkStatsCollectionWithProcInfo collection = new NetworkStatsCollectionWithProcInfo(this.mBucketDuration);
        collection.readLegacyUid(file, this.mOnlyTags);
        long startMillis = collection.getStartMillis();
        long endMillis = collection.getEndMillis();
        if (!collection.isEmpty()) {
            this.mRotator.rewriteActive(new CombiningRewriter(collection), startMillis);
            this.mRotator.maybeRotate(endMillis);
        }
    }

    public void dumpLocked(IndentingPrintWriter pw, boolean fullHistory) {
        pw.print("Pending bytes: ");
        pw.println(this.mPending.getTotalBytes());
        if (fullHistory) {
            pw.println("Complete history:");
            getOrLoadCompleteLocked().dump(pw);
            return;
        }
        pw.println("History since boot:");
        this.mSinceBoot.dump(pw);
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
        Log.w(TAG, "recoverFromWtf.");
        this.mRotator.deleteAll();
    }
}
