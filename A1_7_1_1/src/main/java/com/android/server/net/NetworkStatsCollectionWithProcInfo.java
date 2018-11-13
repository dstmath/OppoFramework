package com.android.server.net;

import android.net.NetworkIdentity;
import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FileRotator.Reader;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.job.controllers.JobStatus;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import libcore.io.IoUtils;

public class NetworkStatsCollectionWithProcInfo implements Reader {
    private static final int FILE_MAGIC = 1095648596;
    private static final String TAG = "NetworkStatsCollectionWithProcInfo";
    private static final int VERSION_NETWORK_INIT = 1;
    private static final int VERSION_UID_INIT = 1;
    private static final int VERSION_UID_WITH_IDENT = 2;
    private static final int VERSION_UID_WITH_SET = 4;
    private static final int VERSION_UID_WITH_TAG = 3;
    private static final int VERSION_UNIFIED_INIT = 16;
    private final long mBucketDuration;
    private boolean mDirty;
    private long mEndMillis;
    private long mStartMillis;
    private ArrayMap<KeyWithProcInfo, NetworkStatsHistory> mStats = new ArrayMap();
    private long mTotalBytes;

    private static class KeyWithProcInfo implements Comparable<KeyWithProcInfo> {
        private final int hashCode;
        public final NetworkIdentitySet ident;
        public String procName;
        public final int set;
        public final int tag;
        public final int uid;

        public KeyWithProcInfo(NetworkIdentitySet ident, int uid, int set, int tag, String procName) {
            this.ident = ident;
            this.uid = uid;
            this.set = set;
            this.tag = tag;
            this.procName = procName;
            this.hashCode = Objects.hash(new Object[]{ident, Integer.valueOf(uid), Integer.valueOf(set), Integer.valueOf(tag), procName});
        }

        public int hashCode() {
            return this.hashCode;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof KeyWithProcInfo)) {
                return false;
            }
            KeyWithProcInfo key = (KeyWithProcInfo) obj;
            if (this.uid == key.uid && this.set == key.set && this.tag == key.tag && Objects.equals(this.ident, key.ident)) {
                z = this.procName.equals(key.procName);
            }
            return z;
        }

        public int compareTo(KeyWithProcInfo another) {
            int res = 0;
            if (!(this.ident == null || another.ident == null)) {
                res = this.ident.compareTo(another.ident);
            }
            if (res == 0) {
                res = Integer.compare(this.uid, another.uid);
            }
            if (res == 0) {
                res = Integer.compare(this.set, another.set);
            }
            if (res == 0) {
                res = Integer.compare(this.tag, another.tag);
            }
            if (res != 0 || this.procName == null || another.procName == null) {
                return res;
            }
            return this.procName.compareTo(another.procName);
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(" KeyWithProcInfo:");
            builder.append(" uid=").append(this.uid);
            builder.append(" set=").append(setToString(this.set));
            builder.append(" tag=").append(tagToString(this.tag));
            builder.append(" procName=").append(this.procName);
            return builder.toString();
        }

        public static String setToString(int set) {
            switch (set) {
                case -1:
                    return "ALL";
                case 0:
                    return "DEFAULT";
                case 1:
                    return "FOREGROUND";
                default:
                    return "UNKNOWN";
            }
        }

        public static String tagToString(int tag) {
            return "0x" + Integer.toHexString(tag);
        }
    }

    public NetworkStatsCollectionWithProcInfo(long bucketDuration) {
        this.mBucketDuration = bucketDuration;
        reset();
    }

    public void reset() {
        this.mStats.clear();
        this.mStartMillis = JobStatus.NO_LATEST_RUNTIME;
        this.mEndMillis = Long.MIN_VALUE;
        this.mTotalBytes = 0;
        this.mDirty = false;
    }

    public long getStartMillis() {
        return this.mStartMillis;
    }

    public long getFirstAtomicBucketMillis() {
        if (this.mStartMillis == JobStatus.NO_LATEST_RUNTIME) {
            return JobStatus.NO_LATEST_RUNTIME;
        }
        return this.mStartMillis + this.mBucketDuration;
    }

    public long getEndMillis() {
        return this.mEndMillis;
    }

    public long getTotalBytes() {
        return this.mTotalBytes;
    }

    public boolean isDirty() {
        return this.mDirty;
    }

    public void clearDirty() {
        this.mDirty = false;
    }

    public boolean isEmpty() {
        return this.mStartMillis == JobStatus.NO_LATEST_RUNTIME && this.mEndMillis == Long.MIN_VALUE;
    }

    public NetworkStatsHistory getHistory(NetworkTemplate template, int uid, int set, int tag, int fields, String procName) {
        return getHistory(template, uid, set, tag, fields, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME, procName);
    }

    public NetworkStatsHistory getHistory(NetworkTemplate template, int uid, int set, int tag, int fields, long start, long end, String procName) {
        NetworkStatsHistory combined = new NetworkStatsHistory(this.mBucketDuration, estimateBuckets(), fields);
        for (int i = 0; i < this.mStats.size(); i++) {
            KeyWithProcInfo key = (KeyWithProcInfo) this.mStats.keyAt(i);
            boolean setMatches = set == -1 || key.set == set;
            if (key.uid == uid && setMatches && key.tag == tag && templateMatches(template, key.ident) && key.procName.equals(procName)) {
                combined.recordHistory((NetworkStatsHistory) this.mStats.valueAt(i), start, end);
            }
        }
        return combined;
    }

    public NetworkStats getSummary(NetworkTemplate template, long start, long end) {
        long now = System.currentTimeMillis();
        NetworkStats stats = new NetworkStats(end - start, 24);
        stats.setContainExtendDataFlag(true);
        Entry entry = new Entry();
        NetworkStatsHistory.Entry historyEntry = null;
        if (start == end) {
            return stats;
        }
        for (int i = 0; i < this.mStats.size(); i++) {
            KeyWithProcInfo key = (KeyWithProcInfo) this.mStats.keyAt(i);
            if (templateMatches(template, key.ident)) {
                historyEntry = ((NetworkStatsHistory) this.mStats.valueAt(i)).getValues(start, end, now, historyEntry);
                entry.iface = NetworkStats.IFACE_ALL;
                entry.uid = key.uid;
                entry.set = key.set;
                entry.tag = key.tag;
                entry.rxBytes = historyEntry.rxBytes;
                entry.rxPackets = historyEntry.rxPackets;
                entry.txBytes = historyEntry.txBytes;
                entry.txPackets = historyEntry.txPackets;
                entry.operations = historyEntry.operations;
                entry.pid = 0;
                entry.comm = key.procName;
                if (!entry.isEmpty()) {
                    stats.combineValues(entry);
                }
            }
        }
        return stats;
    }

    public void recordData(NetworkIdentitySet ident, int uid, int set, int tag, long start, long end, Entry entry) {
        long pid = entry.pid;
        String comm = entry.comm;
        if (comm == null || comm.length() <= 0) {
            Slog.w(TAG, "recordData:comm illegal!");
            return;
        }
        NetworkStatsHistory history = findOrCreateHistory(ident, uid, set, tag, comm);
        history.recordData(start, end, entry);
        noteRecordedHistory(history.getStart(), history.getEnd(), entry.rxBytes + entry.txBytes);
    }

    private void recordHistory(KeyWithProcInfo key, NetworkStatsHistory history) {
        if (history.size() != 0) {
            noteRecordedHistory(history.getStart(), history.getEnd(), history.getTotalBytes());
            NetworkStatsHistory target = (NetworkStatsHistory) this.mStats.get(key);
            if (target == null) {
                target = new NetworkStatsHistory(history.getBucketDuration());
                this.mStats.put(key, target);
            }
            target.recordEntireHistory(history);
        }
    }

    public void recordCollection(NetworkStatsCollectionWithProcInfo another) {
        for (int i = 0; i < another.mStats.size(); i++) {
            recordHistory((KeyWithProcInfo) another.mStats.keyAt(i), (NetworkStatsHistory) another.mStats.valueAt(i));
        }
    }

    private NetworkStatsHistory findOrCreateHistory(NetworkIdentitySet ident, int uid, int set, int tag, String procName) {
        KeyWithProcInfo key = new KeyWithProcInfo(ident, uid, set, tag, procName);
        NetworkStatsHistory existing = (NetworkStatsHistory) this.mStats.get(key);
        NetworkStatsHistory updated = null;
        if (existing == null) {
            updated = new NetworkStatsHistory(this.mBucketDuration, 10);
        } else if (existing.getBucketDuration() != this.mBucketDuration) {
            updated = new NetworkStatsHistory(existing, this.mBucketDuration);
        }
        if (updated == null) {
            return existing;
        }
        this.mStats.put(key, updated);
        return updated;
    }

    private static void writeOptionalString(DataOutputStream out, String value) throws IOException {
        if (value != null) {
            out.writeByte(1);
            out.writeUTF(value);
            return;
        }
        out.writeByte(0);
    }

    private static String readOptionalString(DataInputStream in) throws IOException {
        if (in.readByte() != (byte) 0) {
            return in.readUTF();
        }
        return null;
    }

    public void read(InputStream in) throws IOException {
        read(new DataInputStream(in));
    }

    public void read(DataInputStream in) throws IOException {
        int magic = in.readInt();
        if (magic != FILE_MAGIC) {
            throw new ProtocolException("unexpected magic: " + magic);
        }
        int version = in.readInt();
        switch (version) {
            case 16:
                int identSize = in.readInt();
                for (int i = 0; i < identSize; i++) {
                    NetworkIdentitySet ident = new NetworkIdentitySet(in);
                    int size = in.readInt();
                    for (int j = 0; j < size; j++) {
                        int uid = in.readInt();
                        int set = in.readInt();
                        int tag = in.readInt();
                        String procName = readOptionalString(in);
                        if (procName == null || procName.length() <= 0) {
                            Log.w(TAG, "read data error, procName is empty!");
                            procName = "unKnown";
                        }
                        recordHistory(new KeyWithProcInfo(ident, uid, set, tag, procName), new NetworkStatsHistory(in));
                    }
                }
                return;
            default:
                throw new ProtocolException("unexpected version: " + version);
        }
    }

    public void write(DataOutputStream out) throws IOException {
        ArrayList<KeyWithProcInfo> keys;
        HashMap<NetworkIdentitySet, ArrayList<KeyWithProcInfo>> keysByIdent = Maps.newHashMap();
        for (KeyWithProcInfo key : this.mStats.keySet()) {
            keys = (ArrayList) keysByIdent.get(key.ident);
            if (keys == null) {
                keys = Lists.newArrayList();
                keysByIdent.put(key.ident, keys);
            }
            keys.add(key);
        }
        out.writeInt(FILE_MAGIC);
        out.writeInt(16);
        out.writeInt(keysByIdent.size());
        for (NetworkIdentitySet ident : keysByIdent.keySet()) {
            keys = (ArrayList) keysByIdent.get(ident);
            ident.writeToStream(out);
            out.writeInt(keys.size());
            for (KeyWithProcInfo key2 : keys) {
                NetworkStatsHistory history = (NetworkStatsHistory) this.mStats.get(key2);
                out.writeInt(key2.uid);
                out.writeInt(key2.set);
                out.writeInt(key2.tag);
                writeOptionalString(out, key2.procName);
                history.writeToStream(out);
            }
        }
        out.flush();
    }

    @Deprecated
    public void readLegacyNetwork(File file) throws IOException {
        Throwable th;
        AutoCloseable autoCloseable = null;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new AtomicFile(file).openRead()));
            try {
                int magic = in.readInt();
                if (magic != FILE_MAGIC) {
                    throw new ProtocolException("unexpected magic: " + magic);
                }
                int version = in.readInt();
                switch (version) {
                    case 1:
                        int size = in.readInt();
                        for (int i = 0; i < size; i++) {
                            NetworkIdentitySet ident = new NetworkIdentitySet(in);
                            recordHistory(new KeyWithProcInfo(ident, -1, -1, 0, "unKnown"), new NetworkStatsHistory(in));
                        }
                        IoUtils.closeQuietly(in);
                        return;
                    default:
                        throw new ProtocolException("unexpected version: " + version);
                }
            } catch (FileNotFoundException e) {
                autoCloseable = in;
                IoUtils.closeQuietly(autoCloseable);
            } catch (Throwable th2) {
                th = th2;
                autoCloseable = in;
                IoUtils.closeQuietly(autoCloseable);
                throw th;
            }
        } catch (FileNotFoundException e2) {
            IoUtils.closeQuietly(autoCloseable);
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(autoCloseable);
            throw th;
        }
    }

    @Deprecated
    public void readLegacyUid(File file, boolean onlyTags) throws IOException {
        Throwable th;
        AutoCloseable autoCloseable = null;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new AtomicFile(file).openRead()));
            try {
                int magic = in.readInt();
                if (magic != FILE_MAGIC) {
                    throw new ProtocolException("unexpected magic: " + magic);
                }
                int version = in.readInt();
                switch (version) {
                    case 1:
                    case 2:
                        break;
                    case 3:
                    case 4:
                        int identSize = in.readInt();
                        for (int i = 0; i < identSize; i++) {
                            NetworkIdentitySet ident = new NetworkIdentitySet(in);
                            int size = in.readInt();
                            for (int j = 0; j < size; j++) {
                                int set;
                                int uid = in.readInt();
                                if (version >= 4) {
                                    set = in.readInt();
                                } else {
                                    set = 0;
                                }
                                int tag = in.readInt();
                                KeyWithProcInfo key = new KeyWithProcInfo(ident, uid, set, tag, "unKnown");
                                NetworkStatsHistory history = new NetworkStatsHistory(in);
                                if ((tag == 0) != onlyTags) {
                                    recordHistory(key, history);
                                }
                            }
                        }
                        break;
                    default:
                        throw new ProtocolException("unexpected version: " + version);
                }
                IoUtils.closeQuietly(in);
            } catch (FileNotFoundException e) {
                autoCloseable = in;
                IoUtils.closeQuietly(autoCloseable);
            } catch (Throwable th2) {
                th = th2;
                autoCloseable = in;
                IoUtils.closeQuietly(autoCloseable);
                throw th;
            }
        } catch (FileNotFoundException e2) {
            IoUtils.closeQuietly(autoCloseable);
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(autoCloseable);
            throw th;
        }
    }

    public void removeUids(int[] uids) {
        ArrayList<KeyWithProcInfo> knownKeys = Lists.newArrayList();
        knownKeys.addAll(this.mStats.keySet());
        for (KeyWithProcInfo key : knownKeys) {
            if (ArrayUtils.contains(uids, key.uid)) {
                if (key.tag == 0) {
                    findOrCreateHistory(key.ident, -4, 0, 0, key.procName).recordEntireHistory((NetworkStatsHistory) this.mStats.get(key));
                }
                this.mStats.remove(key);
                this.mDirty = true;
            }
        }
    }

    private void noteRecordedHistory(long startMillis, long endMillis, long totalBytes) {
        if (startMillis < this.mStartMillis) {
            this.mStartMillis = startMillis;
        }
        if (endMillis > this.mEndMillis) {
            this.mEndMillis = endMillis;
        }
        this.mTotalBytes += totalBytes;
        this.mDirty = true;
    }

    private int estimateBuckets() {
        return (int) (Math.min(this.mEndMillis - this.mStartMillis, 3024000000L) / this.mBucketDuration);
    }

    public void dump(IndentingPrintWriter pw) {
        ArrayList<KeyWithProcInfo> keys = Lists.newArrayList();
        keys.addAll(this.mStats.keySet());
        Collections.sort(keys);
        for (KeyWithProcInfo key : keys) {
            pw.print("ident=");
            pw.print(key.ident.toString());
            pw.print(" uid=");
            pw.print(key.uid);
            pw.print(" set=");
            pw.print(NetworkStats.setToString(key.set));
            pw.print(" tag=");
            pw.println(NetworkStats.tagToString(key.tag));
            pw.print(" procName=");
            pw.println(key.procName);
            NetworkStatsHistory history = (NetworkStatsHistory) this.mStats.get(key);
            pw.increaseIndent();
            history.dump(pw, true);
            pw.decreaseIndent();
        }
    }

    public void dumpCheckin(PrintWriter pw, long start, long end) {
        dumpCheckin(pw, start, end, NetworkTemplate.buildTemplateMobileWildcard(), "cell");
        dumpCheckin(pw, start, end, NetworkTemplate.buildTemplateWifiWildcard(), "wifi");
        dumpCheckin(pw, start, end, NetworkTemplate.buildTemplateEthernet(), "eth");
        dumpCheckin(pw, start, end, NetworkTemplate.buildTemplateBluetooth(), "bt");
    }

    private void dumpCheckin(PrintWriter pw, long start, long end, NetworkTemplate groupTemplate, String groupPrefix) {
        int i;
        KeyWithProcInfo key;
        NetworkStatsHistory value;
        ArrayMap<KeyWithProcInfo, NetworkStatsHistory> grouped = new ArrayMap();
        for (i = 0; i < this.mStats.size(); i++) {
            key = (KeyWithProcInfo) this.mStats.keyAt(i);
            value = (NetworkStatsHistory) this.mStats.valueAt(i);
            if (templateMatches(groupTemplate, key.ident)) {
                KeyWithProcInfo groupKey = new KeyWithProcInfo(null, key.uid, key.set, key.tag, key.procName);
                NetworkStatsHistory groupHistory = (NetworkStatsHistory) grouped.get(groupKey);
                if (groupHistory == null) {
                    groupHistory = new NetworkStatsHistory(value.getBucketDuration());
                    grouped.put(groupKey, groupHistory);
                }
                groupHistory.recordHistory(value, start, end);
            }
        }
        for (i = 0; i < grouped.size(); i++) {
            key = (KeyWithProcInfo) grouped.keyAt(i);
            value = (NetworkStatsHistory) grouped.valueAt(i);
            if (value.size() != 0) {
                pw.print("c,");
                pw.print(groupPrefix);
                pw.print(',');
                pw.print(key.uid);
                pw.print(',');
                pw.print(key.procName);
                pw.print(',');
                pw.print(NetworkStats.setToCheckinString(key.set));
                pw.print(',');
                pw.print(key.tag);
                pw.println();
                value.dumpCheckin(pw);
            }
        }
    }

    public void logoutData(String prefix) {
        ArrayList<KeyWithProcInfo> keys = Lists.newArrayList();
        keys.addAll(this.mStats.keySet());
        Collections.sort(keys);
        Log.d(TAG, "[" + prefix + "] begin:");
        int index = 0;
        for (KeyWithProcInfo key : keys) {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("line[").append(index).append("]:").append(key.toString());
            index++;
            String firstLine = strBuilder.toString();
            String secondLine = ((NetworkStatsHistory) this.mStats.get(key)).toString();
            Log.d(TAG, "key = " + firstLine);
            Log.d(TAG, "histroy = " + secondLine);
        }
        Log.d(TAG, "[" + prefix + "] end.");
    }

    private static boolean templateMatches(NetworkTemplate template, NetworkIdentitySet identSet) {
        for (NetworkIdentity ident : identSet) {
            if (template.matches(ident)) {
                return true;
            }
        }
        return false;
    }
}
