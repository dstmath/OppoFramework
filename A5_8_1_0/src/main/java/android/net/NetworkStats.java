package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import com.android.internal.util.ArrayUtils;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import libcore.util.EmptyArray;

public class NetworkStats implements Parcelable {
    public static final Creator<NetworkStats> CREATOR = new Creator<NetworkStats>() {
        public NetworkStats createFromParcel(Parcel in) {
            return new NetworkStats(in);
        }

        public NetworkStats[] newArray(int size) {
            return new NetworkStats[size];
        }
    };
    private static final boolean DEBUG = false;
    public static final String IFACE_ALL = null;
    public static final int METERED_ALL = -1;
    public static final int METERED_NO = 0;
    public static final int METERED_YES = 1;
    public static final int ROAMING_ALL = -1;
    public static final int ROAMING_NO = 0;
    public static final int ROAMING_YES = 1;
    public static final int SET_ALL = -1;
    public static final int SET_DBG_VPN_IN = 1001;
    public static final int SET_DBG_VPN_OUT = 1002;
    public static final int SET_DEBUG_START = 1000;
    public static final int SET_DEFAULT = 0;
    public static final int SET_FOREGROUND = 1;
    public static final int STATS_PER_IFACE = 0;
    public static final int STATS_PER_UID = 1;
    public static final int SUPPORT_EXTEND_DATA_NO = 0;
    public static final int SUPPORT_EXTEND_DATA_YES = 1;
    private static final String TAG = "NetworkStats";
    public static final int TAG_ALL = -1;
    public static final int TAG_NONE = 0;
    public static final int UID_ALL = -1;
    private int capacity;
    private String[] comms;
    private int containExtendData = 0;
    private long elapsedRealtime;
    private String[] iface;
    private int[] metered;
    private long[] operations;
    private long[] pids;
    private int[] roaming;
    private long[] rxBytes;
    private long[] rxPackets;
    private int[] set;
    private int size;
    private int[] tag;
    private long[] txBytes;
    private long[] txPackets;
    private int[] uid;

    private class DataComparator implements Comparator {
        private boolean mProcessRootUid = false;
        private int mTotalSize = 0;

        public DataComparator(boolean processRootUid, int totalSize) {
            this.mProcessRootUid = processRootUid;
            this.mTotalSize = totalSize;
        }

        public boolean isProcessRootUid() {
            return this.mProcessRootUid;
        }

        public int compare(Object first, Object second) {
            int firstIndex = ((Integer) first).intValue();
            int secondIndex = ((Integer) second).intValue();
            if (firstIndex >= this.mTotalSize || secondIndex >= this.mTotalSize) {
                Log.w("DataComparator", "compare, index out of index!");
                return 1;
            } else if (firstIndex < 0 || secondIndex < 0) {
                return 1;
            } else {
                int firstUid = NetworkStats.this.uid[firstIndex];
                int secondUid = NetworkStats.this.uid[secondIndex];
                long firstTotal;
                long secondTotal;
                if (this.mProcessRootUid) {
                    if (firstUid == 0 && secondUid == 0) {
                        firstTotal = NetworkStats.this.rxBytes[firstIndex] + NetworkStats.this.txBytes[firstIndex];
                        secondTotal = NetworkStats.this.rxBytes[secondIndex] + NetworkStats.this.txBytes[secondIndex];
                        if (firstTotal > secondTotal) {
                            return -1;
                        }
                        if (firstTotal == secondTotal) {
                            return 0;
                        }
                        return 1;
                    } else if (firstUid == 0 && secondUid != 0) {
                        return -1;
                    } else {
                        if (firstUid == 0 || secondUid != 0) {
                            return 0;
                        }
                        return 1;
                    }
                } else if (firstUid != 0 && secondUid != 0) {
                    firstTotal = NetworkStats.this.rxBytes[firstIndex] + NetworkStats.this.txBytes[firstIndex];
                    secondTotal = NetworkStats.this.rxBytes[secondIndex] + NetworkStats.this.txBytes[secondIndex];
                    if (firstTotal > secondTotal) {
                        return -1;
                    }
                    if (firstTotal == secondTotal) {
                        return 0;
                    }
                    return 1;
                } else if (firstUid != 0 && secondUid == 0) {
                    return -1;
                } else {
                    if (firstUid != 0 || secondUid == 0) {
                        return 0;
                    }
                    return 1;
                }
            }
        }
    }

    public static class Entry {
        public String comm;
        public String iface;
        public int metered;
        public long operations;
        public long pid;
        public int roaming;
        public long rxBytes;
        public long rxPackets;
        public int set;
        public int tag;
        public long txBytes;
        public long txPackets;
        public int uid;

        public Entry() {
            this(NetworkStats.IFACE_ALL, -1, 0, 0, 0, 0, 0, 0, 0);
        }

        public Entry(long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
            this(NetworkStats.IFACE_ALL, -1, 0, 0, rxBytes, rxPackets, txBytes, txPackets, operations);
        }

        public Entry(String iface, int uid, int set, int tag, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
            this(iface, uid, set, tag, 0, 0, rxBytes, rxPackets, txBytes, txPackets, -1, null, operations);
        }

        public Entry(String iface, int uid, int set, int tag, int metered, int roaming, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
            this.iface = iface;
            this.uid = uid;
            this.set = set;
            this.tag = tag;
            this.metered = metered;
            this.roaming = roaming;
            this.rxBytes = rxBytes;
            this.rxPackets = rxPackets;
            this.txBytes = txBytes;
            this.txPackets = txPackets;
            this.operations = operations;
            this.pid = -1;
            this.comm = null;
        }

        public Entry(String iface, int uid, int set, int tag, int metered, int roaming, long rxBytes, long rxPackets, long txBytes, long txPackets, long pid, String comm, long operations) {
            this.iface = iface;
            this.uid = uid;
            this.set = set;
            this.tag = tag;
            this.metered = metered;
            this.roaming = roaming;
            this.rxBytes = rxBytes;
            this.rxPackets = rxPackets;
            this.txBytes = txBytes;
            this.txPackets = txPackets;
            this.operations = operations;
            this.pid = pid;
            this.comm = comm;
        }

        public boolean isNegative() {
            return this.rxBytes < 0 || this.rxPackets < 0 || this.txBytes < 0 || this.txPackets < 0 || this.operations < 0;
        }

        public boolean isEmpty() {
            if (this.rxBytes == 0 && this.rxPackets == 0 && this.txBytes == 0 && this.txPackets == 0 && this.operations == 0) {
                return true;
            }
            return false;
        }

        public void add(Entry another) {
            this.rxBytes += another.rxBytes;
            this.rxPackets += another.rxPackets;
            this.txBytes += another.txBytes;
            this.txPackets += another.txPackets;
            this.operations += another.operations;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("iface=").append(this.iface);
            builder.append(" uid=").append(this.uid);
            builder.append(" set=").append(NetworkStats.setToString(this.set));
            builder.append(" tag=").append(NetworkStats.tagToString(this.tag));
            builder.append(" metered=").append(NetworkStats.meteredToString(this.metered));
            builder.append(" roaming=").append(NetworkStats.roamingToString(this.roaming));
            builder.append(" rxBytes=").append(this.rxBytes);
            builder.append(" rxPackets=").append(this.rxPackets);
            builder.append(" txBytes=").append(this.txBytes);
            builder.append(" txPackets=").append(this.txPackets);
            builder.append(" operations=").append(this.operations);
            builder.append(" pid=").append(this.pid);
            builder.append(" comm=").append(this.comm);
            return builder.toString();
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry e = (Entry) o;
            if (this.uid == e.uid && this.set == e.set && this.tag == e.tag && this.metered == e.metered && this.roaming == e.roaming && this.rxBytes == e.rxBytes && this.rxPackets == e.rxPackets && this.txBytes == e.txBytes && this.txPackets == e.txPackets && (this.comm == null ? e.comm != null : !this.comm.equals(e.comm)) && this.operations == e.operations) {
                z = this.iface.equals(e.iface);
            }
            return z;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(this.uid), Integer.valueOf(this.set), Integer.valueOf(this.tag), Integer.valueOf(this.metered), Integer.valueOf(this.roaming), this.iface});
        }
    }

    public interface NonMonotonicObserver<C> {
        void foundNonMonotonic(NetworkStats networkStats, int i, NetworkStats networkStats2, int i2, C c);
    }

    public NetworkStats(long elapsedRealtime, int initialSize) {
        this.elapsedRealtime = elapsedRealtime;
        this.size = 0;
        if (initialSize >= 0) {
            this.capacity = initialSize;
            this.iface = new String[initialSize];
            this.uid = new int[initialSize];
            this.set = new int[initialSize];
            this.tag = new int[initialSize];
            this.metered = new int[initialSize];
            this.roaming = new int[initialSize];
            this.rxBytes = new long[initialSize];
            this.rxPackets = new long[initialSize];
            this.txBytes = new long[initialSize];
            this.txPackets = new long[initialSize];
            this.operations = new long[initialSize];
            this.pids = new long[initialSize];
            this.comms = new String[initialSize];
            this.containExtendData = 0;
            return;
        }
        this.capacity = 0;
        this.iface = EmptyArray.STRING;
        this.uid = EmptyArray.INT;
        this.set = EmptyArray.INT;
        this.tag = EmptyArray.INT;
        this.metered = EmptyArray.INT;
        this.roaming = EmptyArray.INT;
        this.rxBytes = EmptyArray.LONG;
        this.rxPackets = EmptyArray.LONG;
        this.txBytes = EmptyArray.LONG;
        this.txPackets = EmptyArray.LONG;
        this.operations = EmptyArray.LONG;
    }

    public NetworkStats(Parcel parcel) {
        this.elapsedRealtime = parcel.readLong();
        this.size = parcel.readInt();
        this.capacity = parcel.readInt();
        this.iface = parcel.createStringArray();
        this.uid = parcel.createIntArray();
        this.set = parcel.createIntArray();
        this.tag = parcel.createIntArray();
        this.metered = parcel.createIntArray();
        this.roaming = parcel.createIntArray();
        this.rxBytes = parcel.createLongArray();
        this.rxPackets = parcel.createLongArray();
        this.txBytes = parcel.createLongArray();
        this.txPackets = parcel.createLongArray();
        this.operations = parcel.createLongArray();
        this.containExtendData = parcel.readInt();
        if (1 == this.containExtendData) {
            this.pids = parcel.createLongArray();
            this.comms = parcel.createStringArray();
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.elapsedRealtime);
        dest.writeInt(this.size);
        dest.writeInt(this.capacity);
        dest.writeStringArray(this.iface);
        dest.writeIntArray(this.uid);
        dest.writeIntArray(this.set);
        dest.writeIntArray(this.tag);
        dest.writeIntArray(this.metered);
        dest.writeIntArray(this.roaming);
        dest.writeLongArray(this.rxBytes);
        dest.writeLongArray(this.rxPackets);
        dest.writeLongArray(this.txBytes);
        dest.writeLongArray(this.txPackets);
        dest.writeLongArray(this.operations);
        dest.writeInt(this.containExtendData);
        if (1 == this.containExtendData) {
            dest.writeLongArray(this.pids);
            dest.writeStringArray(this.comms);
        }
    }

    public NetworkStats clone() {
        NetworkStats clone = new NetworkStats(this.elapsedRealtime, this.size);
        clone.setContainExtendDataFlag(isContainExtendData());
        Entry entry = null;
        for (int i = 0; i < this.size; i++) {
            entry = getValues(i, entry);
            clone.addValues(entry);
        }
        return clone;
    }

    public NetworkStats addIfaceValues(String iface, long rxBytes, long rxPackets, long txBytes, long txPackets) {
        return addValues(iface, -1, 0, 0, rxBytes, rxPackets, txBytes, txPackets, 0);
    }

    public NetworkStats addValues(String iface, int uid, int set, int tag, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
        return addValues(new Entry(iface, uid, set, tag, rxBytes, rxPackets, txBytes, txPackets, operations));
    }

    public NetworkStats addValues(String iface, int uid, int set, int tag, int metered, int roaming, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
        return addValues(new Entry(iface, uid, set, tag, metered, roaming, rxBytes, rxPackets, txBytes, txPackets, -1, null, operations));
    }

    public NetworkStats addValues(String iface, int uid, int set, int tag, int metered, int roaming, long rxBytes, long rxPackets, long txBytes, long txPackets, long pid, String comm, long operations) {
        return addValues(new Entry(iface, uid, set, tag, metered, roaming, rxBytes, rxPackets, txBytes, txPackets, pid, comm, operations));
    }

    public NetworkStats addValues(Entry entry) {
        if (this.size >= this.capacity) {
            int newLength = (Math.max(this.size, 10) * 3) / 2;
            this.iface = (String[]) Arrays.copyOf(this.iface, newLength);
            this.uid = Arrays.copyOf(this.uid, newLength);
            this.set = Arrays.copyOf(this.set, newLength);
            this.tag = Arrays.copyOf(this.tag, newLength);
            this.metered = Arrays.copyOf(this.metered, newLength);
            this.roaming = Arrays.copyOf(this.roaming, newLength);
            this.rxBytes = Arrays.copyOf(this.rxBytes, newLength);
            this.rxPackets = Arrays.copyOf(this.rxPackets, newLength);
            this.txBytes = Arrays.copyOf(this.txBytes, newLength);
            this.txPackets = Arrays.copyOf(this.txPackets, newLength);
            this.operations = Arrays.copyOf(this.operations, newLength);
            this.capacity = newLength;
            if (1 == this.containExtendData) {
                this.pids = Arrays.copyOf(this.pids, newLength);
                this.comms = (String[]) Arrays.copyOf(this.comms, newLength);
            }
        }
        this.iface[this.size] = entry.iface;
        this.uid[this.size] = entry.uid;
        this.set[this.size] = entry.set;
        this.tag[this.size] = entry.tag;
        this.metered[this.size] = entry.metered;
        this.roaming[this.size] = entry.roaming;
        this.rxBytes[this.size] = entry.rxBytes;
        this.rxPackets[this.size] = entry.rxPackets;
        this.txBytes[this.size] = entry.txBytes;
        this.txPackets[this.size] = entry.txPackets;
        this.operations[this.size] = entry.operations;
        if (1 == this.containExtendData) {
            this.pids[this.size] = entry.pid;
            this.comms[this.size] = entry.comm;
        }
        this.size++;
        return this;
    }

    public Entry getValues(int i, Entry recycle) {
        Entry entry = recycle != null ? recycle : new Entry();
        entry.iface = this.iface[i];
        entry.uid = this.uid[i];
        entry.set = this.set[i];
        entry.tag = this.tag[i];
        entry.metered = this.metered[i];
        entry.roaming = this.roaming[i];
        entry.rxBytes = this.rxBytes[i];
        entry.rxPackets = this.rxPackets[i];
        entry.txBytes = this.txBytes[i];
        entry.txPackets = this.txPackets[i];
        if (1 == this.containExtendData) {
            entry.pid = this.pids[i];
            entry.comm = this.comms[i];
        } else {
            entry.pid = 0;
            entry.comm = null;
        }
        entry.operations = this.operations[i];
        return entry;
    }

    public long getElapsedRealtime() {
        return this.elapsedRealtime;
    }

    public void setElapsedRealtime(long time) {
        this.elapsedRealtime = time;
    }

    public long getElapsedRealtimeAge() {
        return SystemClock.elapsedRealtime() - this.elapsedRealtime;
    }

    public int size() {
        return this.size;
    }

    public int internalSize() {
        return this.capacity;
    }

    @Deprecated
    public NetworkStats combineValues(String iface, int uid, int tag, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
        return combineValues(iface, uid, 0, tag, rxBytes, rxPackets, txBytes, txPackets, operations);
    }

    public NetworkStats combineValues(String iface, int uid, int set, int tag, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
        return combineValues(new Entry(iface, uid, set, tag, rxBytes, rxPackets, txBytes, txPackets, operations));
    }

    public NetworkStats combineValues(String iface, int uid, int set, int tag, long rxBytes, long rxPackets, long txBytes, long txPackets, long pid, String comm, long operations) {
        return combineValues(new Entry(iface, uid, set, tag, 0, 0, rxBytes, rxPackets, txBytes, txPackets, pid, comm, operations));
    }

    public boolean isContainExtendData() {
        return 1 == this.containExtendData;
    }

    public void setContainExtendDataFlag(boolean contain) {
        this.containExtendData = contain ? 1 : 0;
    }

    public NetworkStats combineValues(Entry entry) {
        int i;
        if (this.containExtendData == 0) {
            i = findIndex(entry.iface, entry.uid, entry.set, entry.tag, entry.metered, entry.roaming);
        } else {
            i = findIndexWithProcName(entry.iface, entry.uid, entry.set, entry.tag, entry.metered, entry.roaming, entry.comm);
        }
        if (i == -1) {
            addValues(entry);
        } else {
            long[] jArr = this.rxBytes;
            jArr[i] = jArr[i] + entry.rxBytes;
            jArr = this.rxPackets;
            jArr[i] = jArr[i] + entry.rxPackets;
            jArr = this.txBytes;
            jArr[i] = jArr[i] + entry.txBytes;
            jArr = this.txPackets;
            jArr[i] = jArr[i] + entry.txPackets;
            jArr = this.operations;
            jArr[i] = jArr[i] + entry.operations;
        }
        return this;
    }

    public void combineAllValues(NetworkStats another) {
        Entry entry = null;
        for (int i = 0; i < another.size; i++) {
            entry = another.getValues(i, entry);
            combineValues(entry);
        }
    }

    public int findIndex(String iface, int uid, int set, int tag, int metered, int roaming) {
        int i = 0;
        while (i < this.size) {
            if (uid == this.uid[i] && set == this.set[i] && tag == this.tag[i] && metered == this.metered[i] && roaming == this.roaming[i] && Objects.equals(iface, this.iface[i])) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int findIndexWithProcName(String iface, int uid, int set, int tag, int metered, int roaming, String procName) {
        if (procName == null || procName.length() <= 0) {
            Log.w(TAG, "findIndexWithProcName procName is empty.");
            return -1;
        }
        int i = 0;
        while (i < this.size) {
            if (procName.equals(this.comms[i]) && uid == this.uid[i] && set == this.set[i] && tag == this.tag[i] && Objects.equals(iface, this.iface[i]) && metered == this.metered[i] && roaming == this.roaming[i]) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int findIndexHinted(String iface, int uid, int set, int tag, int metered, int roaming, int hintIndex) {
        for (int offset = 0; offset < this.size; offset++) {
            int i;
            int halfOffset = offset / 2;
            if (offset % 2 == 0) {
                i = (hintIndex + halfOffset) % this.size;
            } else {
                i = (((this.size + hintIndex) - halfOffset) - 1) % this.size;
            }
            if (uid == this.uid[i] && set == this.set[i] && tag == this.tag[i] && metered == this.metered[i] && roaming == this.roaming[i] && Objects.equals(iface, this.iface[i])) {
                return i;
            }
        }
        return -1;
    }

    public int findIndexHintedWithProcessInfo(String iface, int uid, int set, int tag, int metered, int roaming, int hintIndex, long pid, String processName) {
        if (!isContainExtendData()) {
            return findIndexHinted(iface, uid, set, tag, metered, roaming, hintIndex);
        }
        for (int offset = 0; offset < this.size; offset++) {
            int i;
            int halfOffset = offset / 2;
            if (offset % 2 == 0) {
                i = (hintIndex + halfOffset) % this.size;
            } else {
                i = (((this.size + hintIndex) - halfOffset) - 1) % this.size;
            }
            if (uid == this.uid[i] && set == this.set[i] && tag == this.tag[i] && metered == this.metered[i] && roaming == this.roaming[i] && Objects.equals(iface, this.iface[i]) && processName != null && this.comms[i] != null && processName.equals(this.comms[i])) {
                return i;
            }
        }
        return -1;
    }

    public void spliceOperationsFrom(NetworkStats stats) {
        for (int i = 0; i < this.size; i++) {
            int j = stats.findIndex(this.iface[i], this.uid[i], this.set[i], this.tag[i], this.metered[i], this.roaming[i]);
            if (j == -1) {
                this.operations[i] = 0;
            } else {
                this.operations[i] = stats.operations[j];
            }
        }
    }

    public String[] getUniqueIfaces() {
        HashSet<String> ifaces = new HashSet();
        if (this.iface != null) {
            for (String iface : this.iface) {
                if (iface != IFACE_ALL) {
                    ifaces.add(iface);
                }
            }
        }
        return (String[]) ifaces.toArray(new String[ifaces.size()]);
    }

    public int[] getUniqueUids() {
        SparseBooleanArray uids = new SparseBooleanArray();
        for (int uid : this.uid) {
            uids.put(uid, true);
        }
        int size = uids.size();
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = uids.keyAt(i);
        }
        return result;
    }

    public long getTotalBytes() {
        Entry entry = getTotal(null);
        return entry.rxBytes + entry.txBytes;
    }

    public Entry getTotal(Entry recycle) {
        return getTotal(recycle, null, -1, false);
    }

    public Entry getTotal(Entry recycle, int limitUid) {
        return getTotal(recycle, null, limitUid, false);
    }

    public Entry getTotal(Entry recycle, HashSet<String> limitIface) {
        return getTotal(recycle, limitIface, -1, false);
    }

    public Entry getTotalIncludingTags(Entry recycle) {
        return getTotal(recycle, null, -1, true);
    }

    private Entry getTotal(Entry recycle, HashSet<String> limitIface, int limitUid, boolean includeTags) {
        Entry entry = recycle != null ? recycle : new Entry();
        entry.iface = IFACE_ALL;
        entry.uid = limitUid;
        entry.set = -1;
        entry.tag = 0;
        entry.metered = -1;
        entry.roaming = -1;
        entry.rxBytes = 0;
        entry.rxPackets = 0;
        entry.txBytes = 0;
        entry.txPackets = 0;
        entry.operations = 0;
        int i = 0;
        while (i < this.size) {
            boolean matchesUid = limitUid == -1 || limitUid == this.uid[i];
            boolean matchesIface = limitIface != null ? limitIface.contains(this.iface[i]) : true;
            if (matchesUid && matchesIface && (this.tag[i] == 0 || (includeTags ^ 1) == 0)) {
                entry.rxBytes += this.rxBytes[i];
                entry.rxPackets += this.rxPackets[i];
                entry.txBytes += this.txBytes[i];
                entry.txPackets += this.txPackets[i];
                entry.operations += this.operations[i];
            }
            i++;
        }
        return entry;
    }

    public long getTotalPackets() {
        long total = 0;
        for (int i = this.size - 1; i >= 0; i--) {
            total += this.rxPackets[i] + this.txPackets[i];
        }
        return total;
    }

    public NetworkStats subtract(NetworkStats right) {
        return subtract(this, right, null, null);
    }

    public static <C> NetworkStats subtract(NetworkStats left, NetworkStats right, NonMonotonicObserver<C> observer, C cookie) {
        return subtract(left, right, observer, cookie, null);
    }

    public static <C> NetworkStats subtract(NetworkStats left, NetworkStats right, NonMonotonicObserver<C> observer, C cookie, NetworkStats recycle) {
        NetworkStats result;
        long deltaRealtime = left.elapsedRealtime - right.elapsedRealtime;
        if (deltaRealtime < 0) {
            if (observer != null) {
                observer.foundNonMonotonic(left, -1, right, -1, cookie);
            }
            deltaRealtime = 0;
        }
        Entry entry = new Entry();
        if (recycle == null || recycle.capacity < left.size) {
            NetworkStats networkStats = new NetworkStats(deltaRealtime, left.size);
        } else {
            result = recycle;
            recycle.size = 0;
            recycle.elapsedRealtime = deltaRealtime;
        }
        boolean containExtendData = left.isContainExtendData();
        result.setContainExtendDataFlag(containExtendData);
        if (right.isContainExtendData() != containExtendData) {
            Log.w(TAG, "subtract, attribute value is not the same. left.containExtendData = " + containExtendData);
        }
        int i = 0;
        while (i < left.size) {
            int j;
            entry.iface = left.iface[i];
            entry.uid = left.uid[i];
            entry.set = left.set[i];
            entry.tag = left.tag[i];
            entry.metered = left.metered[i];
            entry.roaming = left.roaming[i];
            if (left.pids != null && left.pids.length > i) {
                entry.pid = left.pids[i];
            }
            if (left.comms != null && left.comms.length > i) {
                entry.comm = left.comms[i];
            }
            entry.rxBytes = left.rxBytes[i];
            entry.rxPackets = left.rxPackets[i];
            entry.txBytes = left.txBytes[i];
            entry.txPackets = left.txPackets[i];
            entry.operations = left.operations[i];
            if (containExtendData) {
                j = right.findIndexHintedWithProcessInfo(entry.iface, entry.uid, entry.set, entry.tag, entry.metered, entry.roaming, i, entry.pid, entry.comm);
            } else {
                j = right.findIndexHinted(entry.iface, entry.uid, entry.set, entry.tag, entry.metered, entry.roaming, i);
            }
            if (j != -1) {
                entry.rxBytes -= right.rxBytes[j];
                entry.rxPackets -= right.rxPackets[j];
                entry.txBytes -= right.txBytes[j];
                entry.txPackets -= right.txPackets[j];
                entry.operations -= right.operations[j];
            }
            if (entry.isNegative()) {
                if (observer != null) {
                    observer.foundNonMonotonic(left, i, right, j, cookie);
                }
                entry.rxBytes = Math.max(entry.rxBytes, 0);
                entry.rxPackets = Math.max(entry.rxPackets, 0);
                entry.txBytes = Math.max(entry.txBytes, 0);
                entry.txPackets = Math.max(entry.txPackets, 0);
                entry.operations = Math.max(entry.operations, 0);
            }
            result.addValues(entry);
            i++;
        }
        return result;
    }

    public NetworkStats getIncrement(int idx, Entry entry, int reset) {
        if (reset == 1) {
            this.rxBytes[idx] = 0;
            this.rxPackets[idx] = 0;
            this.txBytes[idx] = 0;
            this.txPackets[idx] = 0;
            return this;
        }
        long[] jArr = this.rxBytes;
        jArr[idx] = jArr[idx] - entry.rxBytes;
        jArr = this.rxPackets;
        jArr[idx] = jArr[idx] - entry.rxPackets;
        jArr = this.txBytes;
        jArr[idx] = jArr[idx] - entry.txBytes;
        jArr = this.txPackets;
        jArr[idx] = jArr[idx] - entry.txPackets;
        return this;
    }

    public NetworkStats replaceValues(Entry entry) {
        int i = findIndex(entry.iface, entry.uid, entry.set, entry.tag, entry.metered, entry.roaming);
        if (i == -1) {
            Slog.w(TAG, "replaceValues entry " + entry.iface + " is not found, create new!");
            addValues(entry);
        } else {
            this.rxBytes[i] = entry.rxBytes;
            this.rxPackets[i] = entry.rxPackets;
            this.txBytes[i] = entry.txBytes;
            this.txPackets[i] = entry.txPackets;
            this.operations[i] = entry.operations;
        }
        return this;
    }

    public NetworkStats groupedByIface() {
        NetworkStats stats = new NetworkStats(this.elapsedRealtime, 10);
        stats.setContainExtendDataFlag(isContainExtendData());
        Entry entry = new Entry();
        entry.uid = -1;
        entry.set = -1;
        entry.tag = 0;
        entry.metered = -1;
        entry.roaming = -1;
        entry.operations = 0;
        for (int i = 0; i < this.size; i++) {
            if (this.tag[i] == 0) {
                entry.iface = this.iface[i];
                entry.rxBytes = this.rxBytes[i];
                entry.rxPackets = this.rxPackets[i];
                entry.txBytes = this.txBytes[i];
                entry.txPackets = this.txPackets[i];
                if (isContainExtendData()) {
                    entry.pid = this.pids[i];
                    entry.comm = this.comms[i];
                } else {
                    entry.pid = 0;
                    entry.comm = null;
                }
                stats.combineValues(entry);
            }
        }
        return stats;
    }

    public NetworkStats groupedByUid() {
        NetworkStats stats = new NetworkStats(this.elapsedRealtime, 10);
        stats.setContainExtendDataFlag(isContainExtendData());
        Entry entry = new Entry();
        entry.iface = IFACE_ALL;
        entry.set = -1;
        entry.tag = 0;
        entry.metered = -1;
        entry.roaming = -1;
        for (int i = 0; i < this.size; i++) {
            if (this.tag[i] == 0) {
                entry.uid = this.uid[i];
                entry.rxBytes = this.rxBytes[i];
                entry.rxPackets = this.rxPackets[i];
                entry.txBytes = this.txBytes[i];
                entry.txPackets = this.txPackets[i];
                entry.operations = this.operations[i];
                if (isContainExtendData()) {
                    entry.pid = this.pids[i];
                    entry.comm = this.comms[i];
                } else {
                    entry.pid = 0;
                    entry.comm = null;
                }
                stats.combineValues(entry);
            }
        }
        return stats;
    }

    public NetworkStats withoutUids(int[] uids) {
        NetworkStats stats = new NetworkStats(this.elapsedRealtime, 10);
        stats.setContainExtendDataFlag(isContainExtendData());
        Entry entry = new Entry();
        for (int i = 0; i < this.size; i++) {
            entry = getValues(i, entry);
            if (!ArrayUtils.contains(uids, entry.uid)) {
                stats.addValues(entry);
            }
        }
        return stats;
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("NetworkStats: elapsedRealtime=");
        pw.println(this.elapsedRealtime);
        pw.print("NetworkStats: size=");
        pw.print(this.size);
        pw.print("NetworkStats: containExtendData=");
        pw.print(this.containExtendData);
        int pidsLen = 0;
        int commsLen = 0;
        if (1 == this.containExtendData) {
            if (this.pids != null) {
                pidsLen = this.pids.length;
            }
            if (this.comms != null) {
                commsLen = this.comms.length;
            }
        }
        pw.print(" pids.len =");
        pw.print(pidsLen);
        pw.print(" comms.len =");
        pw.print(commsLen);
        int i = 0;
        while (i < this.size) {
            pw.print(prefix);
            pw.print("  [");
            pw.print(i);
            pw.print("]");
            pw.print(" iface=");
            pw.print(this.iface[i]);
            pw.print(" uid=");
            pw.print(this.uid[i]);
            pw.print(" set=");
            pw.print(setToString(this.set[i]));
            pw.print(" tag=");
            pw.print(tagToString(this.tag[i]));
            pw.print(" metered=");
            pw.print(meteredToString(this.metered[i]));
            pw.print(" roaming=");
            pw.print(roamingToString(this.roaming[i]));
            pw.print(" rxBytes=");
            pw.print(this.rxBytes[i]);
            pw.print(" rxPackets=");
            pw.print(this.rxPackets[i]);
            pw.print(" txBytes=");
            pw.print(this.txBytes[i]);
            pw.print(" txPackets=");
            pw.print(this.txPackets[i]);
            if (1 == this.containExtendData) {
                if (this.pids != null && pidsLen > i) {
                    pw.print(" pids=");
                    pw.print(this.pids[i]);
                }
                if (this.comms != null && commsLen > i) {
                    pw.print(" comms=");
                    pw.print(this.comms[i]);
                }
            }
            pw.print(" operations=");
            pw.println(this.operations[i]);
            i++;
        }
    }

    public static String setToString(int set) {
        switch (set) {
            case -1:
                return "ALL";
            case 0:
                return "DEFAULT";
            case 1:
                return "FOREGROUND";
            case 1001:
                return "DBG_VPN_IN";
            case 1002:
                return "DBG_VPN_OUT";
            default:
                return "UNKNOWN";
        }
    }

    public static String setToCheckinString(int set) {
        switch (set) {
            case -1:
                return "all";
            case 0:
                return "def";
            case 1:
                return "fg";
            case 1001:
                return "vpnin";
            case 1002:
                return "vpnout";
            default:
                return "unk";
        }
    }

    public static boolean setMatches(int querySet, int dataSet) {
        boolean z = true;
        if (querySet == dataSet) {
            return true;
        }
        if (querySet != -1 || dataSet >= 1000) {
            z = false;
        }
        return z;
    }

    public static String tagToString(int tag) {
        return "0x" + Integer.toHexString(tag);
    }

    public static String meteredToString(int metered) {
        switch (metered) {
            case -1:
                return "ALL";
            case 0:
                return "NO";
            case 1:
                return "YES";
            default:
                return "UNKNOWN";
        }
    }

    public static String roamingToString(int roaming) {
        switch (roaming) {
            case -1:
                return "ALL";
            case 0:
                return "NO";
            case 1:
                return "YES";
            default:
                return "UNKNOWN";
        }
    }

    public String toString() {
        if (!SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            return "Log not enable.";
        }
        CharArrayWriter writer = new CharArrayWriter();
        dump("", new PrintWriter(writer));
        return writer.toString();
    }

    public void logoutData(String prefix) {
        StringBuilder builder = new StringBuilder();
        if (prefix == null) {
            prefix = "*";
        }
        Log.d(TAG, "------------ NetworkStats[" + prefix + "] begin: ------------");
        builder.append("NetworkStats: elapsedRealtime=").append(this.elapsedRealtime);
        builder.append(", size=").append(this.size);
        builder.append(", containExtendData=").append(this.containExtendData);
        int pidsLen = 0;
        int commsLen = 0;
        if (1 == this.containExtendData) {
            if (this.pids != null) {
                pidsLen = this.pids.length;
            }
            if (this.comms != null) {
                commsLen = this.comms.length;
            }
        }
        builder.append(", pids.len =").append(pidsLen);
        builder.append(", comms.len =").append(commsLen);
        Log.d(TAG, builder.toString());
        int i = 0;
        while (i < this.size) {
            builder = new StringBuilder();
            builder.append(" [").append(i).append("]");
            builder.append(" iface=").append(this.iface[i]);
            builder.append(" uid=").append(this.uid[i]);
            if (1 == this.containExtendData) {
                if (this.comms != null && commsLen > i) {
                    builder.append(" comms=").append(this.comms[i]);
                }
                if (this.pids != null && pidsLen > i) {
                    builder.append(" pids=").append(this.pids[i]);
                }
            }
            builder.append(" totalBytes=").append(this.rxBytes[i] + this.txBytes[i]);
            builder.append(" set=").append(setToString(this.set[i]));
            builder.append(" tag=").append(tagToString(this.tag[i]));
            builder.append(" roaming=").append(roamingToString(this.roaming[i]));
            builder.append(" rxBytes=").append(this.rxBytes[i]);
            builder.append(" rxPackets=").append(this.rxPackets[i]);
            builder.append(" txBytes=").append(this.txBytes[i]);
            builder.append(" txPackets=").append(this.txPackets[i]);
            builder.append(" operations=").append(this.operations[i]);
            Log.d(TAG, builder.toString());
            i++;
        }
        Log.d(TAG, "------------ -----------end----------- ------------");
    }

    public NetworkStats getSpecifiedTopRecords(int countForRootUid, int countForNonRootUid) {
        int totalSize = size();
        if (countForRootUid >= totalSize || countForNonRootUid >= totalSize) {
            return this;
        }
        try {
            NetworkStats targetRes = new NetworkStats(this.elapsedRealtime, Math.max(countForRootUid, countForNonRootUid));
            targetRes.setContainExtendDataFlag(isContainExtendData());
            doGetTopRecords(countForRootUid, new DataComparator(true, totalSize), targetRes);
            doGetTopRecords(countForNonRootUid, new DataComparator(false, totalSize), targetRes);
            return targetRes;
        } catch (Exception e) {
            Log.w(TAG, "getSpecifiedTopRecords failed, just return all! Error:", e);
            return this;
        }
    }

    public void doGetTopRecords(int countToGet, DataComparator comparator, NetworkStats targetRes) {
        Integer[] sortArray;
        int totalSize = size();
        int batchCount = totalSize / countToGet;
        int remain = totalSize % countToGet;
        int j;
        if (batchCount <= 1) {
            sortArray = new Integer[totalSize];
            for (j = 0; j < totalSize; j++) {
                sortArray[j] = Integer.valueOf(j);
            }
            Arrays.sort(sortArray, comparator);
        } else {
            int k;
            sortArray = new Integer[(countToGet * 2)];
            for (j = 0; j < countToGet; j++) {
                sortArray[j] = Integer.valueOf(j);
            }
            for (int i = 0; i < batchCount - 1; i++) {
                for (k = 0; k < countToGet; k++) {
                    sortArray[countToGet + k] = Integer.valueOf(((i + 1) * countToGet) + k);
                }
                Arrays.sort(sortArray, comparator);
            }
            if (remain > 0) {
                int start = totalSize - remain;
                for (k = 0; k < countToGet; k++) {
                    if (k < remain) {
                        sortArray[countToGet + k] = Integer.valueOf(start + k);
                    } else {
                        sortArray[countToGet + k] = Integer.valueOf(-1);
                    }
                }
                Arrays.sort(sortArray, comparator);
            }
        }
        Entry entry = new Entry();
        boolean doAdd = false;
        boolean isProcessRootUid = comparator.isProcessRootUid();
        for (int index = 0; index < countToGet; index++) {
            int sortIndex = sortArray[index].intValue();
            if (sortIndex < 0 || sortIndex >= totalSize) {
                Log.w(TAG, "sortIndex out of index.");
            } else {
                if (isProcessRootUid) {
                    if (this.uid[sortIndex] == 0) {
                        doAdd = true;
                    }
                } else if (this.uid[sortIndex] != 0) {
                    doAdd = true;
                }
                if (doAdd) {
                    entry = getValues(sortIndex, entry);
                    targetRes.addValues(entry);
                    doAdd = false;
                }
            }
        }
    }

    public boolean getSepecialTotal(Entry limitUidEntry, int limitUid, Entry otherUidEntry) {
        if (limitUidEntry == null) {
            Log.w(TAG, "getSepecialTotal:limitUidEntry null.");
            return false;
        } else if (otherUidEntry == null) {
            Log.w(TAG, "getSepecialTotal:otherUidEntry null.");
            return false;
        } else {
            limitUidEntry.iface = IFACE_ALL;
            limitUidEntry.uid = limitUid;
            limitUidEntry.set = -1;
            limitUidEntry.tag = 0;
            limitUidEntry.rxBytes = 0;
            limitUidEntry.rxPackets = 0;
            limitUidEntry.txBytes = 0;
            limitUidEntry.txPackets = 0;
            limitUidEntry.operations = 0;
            limitUidEntry.metered = 0;
            limitUidEntry.roaming = 0;
            otherUidEntry.iface = IFACE_ALL;
            otherUidEntry.uid = 1;
            otherUidEntry.set = -1;
            otherUidEntry.tag = 0;
            otherUidEntry.rxBytes = 0;
            otherUidEntry.rxPackets = 0;
            otherUidEntry.txBytes = 0;
            otherUidEntry.txPackets = 0;
            otherUidEntry.operations = 0;
            otherUidEntry.metered = 0;
            otherUidEntry.roaming = 0;
            for (int i = 0; i < this.size; i++) {
                if (this.tag[i] == 0) {
                    if (limitUid == this.uid[i]) {
                        limitUidEntry.rxBytes += this.rxBytes[i];
                        limitUidEntry.rxPackets += this.rxPackets[i];
                        limitUidEntry.txBytes += this.txBytes[i];
                        limitUidEntry.txPackets += this.txPackets[i];
                        limitUidEntry.operations += this.operations[i];
                    } else {
                        otherUidEntry.rxBytes += this.rxBytes[i];
                        otherUidEntry.rxPackets += this.rxPackets[i];
                        otherUidEntry.txBytes += this.txBytes[i];
                        otherUidEntry.txPackets += this.txPackets[i];
                        otherUidEntry.operations += this.operations[i];
                    }
                }
            }
            return true;
        }
    }

    public int describeContents() {
        return 0;
    }

    public boolean migrateTun(int tunUid, String tunIface, String underlyingIface) {
        Entry tunIfaceTotal = new Entry();
        Entry underlyingIfaceTotal = new Entry();
        tunAdjustmentInit(tunUid, tunIface, underlyingIface, tunIfaceTotal, underlyingIfaceTotal);
        Entry pool = tunGetPool(tunIfaceTotal, underlyingIfaceTotal);
        if (pool.isEmpty()) {
            return true;
        }
        Entry moved = addTrafficToApplications(tunUid, tunIface, underlyingIface, tunIfaceTotal, pool);
        deductTrafficFromVpnApp(tunUid, underlyingIface, moved);
        if (moved.isEmpty()) {
            return true;
        }
        Slog.wtf(TAG, "Failed to deduct underlying network traffic from VPN package. Moved=" + moved);
        return false;
    }

    private void tunAdjustmentInit(int tunUid, String tunIface, String underlyingIface, Entry tunIfaceTotal, Entry underlyingIfaceTotal) {
        Entry recycle = new Entry();
        int i = 0;
        while (i < this.size) {
            getValues(i, recycle);
            if (recycle.uid == -1) {
                throw new IllegalStateException("Cannot adjust VPN accounting on an iface aggregated NetworkStats.");
            } else if (recycle.set == 1001 || recycle.set == 1002) {
                throw new IllegalStateException("Cannot adjust VPN accounting on a NetworkStats containing SET_DBG_VPN_*");
            } else {
                if (recycle.uid == tunUid && recycle.tag == 0 && Objects.equals(underlyingIface, recycle.iface)) {
                    underlyingIfaceTotal.add(recycle);
                }
                if (recycle.uid != tunUid && recycle.tag == 0 && Objects.equals(tunIface, recycle.iface)) {
                    tunIfaceTotal.add(recycle);
                }
                i++;
            }
        }
    }

    private static Entry tunGetPool(Entry tunIfaceTotal, Entry underlyingIfaceTotal) {
        Entry pool = new Entry();
        pool.rxBytes = Math.min(tunIfaceTotal.rxBytes, underlyingIfaceTotal.rxBytes);
        pool.rxPackets = Math.min(tunIfaceTotal.rxPackets, underlyingIfaceTotal.rxPackets);
        pool.txBytes = Math.min(tunIfaceTotal.txBytes, underlyingIfaceTotal.txBytes);
        pool.txPackets = Math.min(tunIfaceTotal.txPackets, underlyingIfaceTotal.txPackets);
        pool.operations = Math.min(tunIfaceTotal.operations, underlyingIfaceTotal.operations);
        return pool;
    }

    private Entry addTrafficToApplications(int tunUid, String tunIface, String underlyingIface, Entry tunIfaceTotal, Entry pool) {
        Entry moved = new Entry();
        Entry tmpEntry = new Entry();
        tmpEntry.iface = underlyingIface;
        int i = 0;
        while (i < this.size) {
            if (Objects.equals(this.iface[i], tunIface) && this.uid[i] != tunUid) {
                if (tunIfaceTotal.rxBytes > 0) {
                    tmpEntry.rxBytes = (pool.rxBytes * this.rxBytes[i]) / tunIfaceTotal.rxBytes;
                } else {
                    tmpEntry.rxBytes = 0;
                }
                if (tunIfaceTotal.rxPackets > 0) {
                    tmpEntry.rxPackets = (pool.rxPackets * this.rxPackets[i]) / tunIfaceTotal.rxPackets;
                } else {
                    tmpEntry.rxPackets = 0;
                }
                if (tunIfaceTotal.txBytes > 0) {
                    tmpEntry.txBytes = (pool.txBytes * this.txBytes[i]) / tunIfaceTotal.txBytes;
                } else {
                    tmpEntry.txBytes = 0;
                }
                if (tunIfaceTotal.txPackets > 0) {
                    tmpEntry.txPackets = (pool.txPackets * this.txPackets[i]) / tunIfaceTotal.txPackets;
                } else {
                    tmpEntry.txPackets = 0;
                }
                if (tunIfaceTotal.operations > 0) {
                    tmpEntry.operations = (pool.operations * this.operations[i]) / tunIfaceTotal.operations;
                } else {
                    tmpEntry.operations = 0;
                }
                tmpEntry.uid = this.uid[i];
                tmpEntry.tag = this.tag[i];
                tmpEntry.set = this.set[i];
                tmpEntry.metered = this.metered[i];
                tmpEntry.roaming = this.roaming[i];
                combineValues(tmpEntry);
                if (this.tag[i] == 0) {
                    moved.add(tmpEntry);
                    tmpEntry.set = 1001;
                    combineValues(tmpEntry);
                }
            }
            i++;
        }
        return moved;
    }

    private void deductTrafficFromVpnApp(int tunUid, String underlyingIface, Entry moved) {
        moved.uid = tunUid;
        moved.set = 1002;
        moved.tag = 0;
        moved.iface = underlyingIface;
        moved.metered = -1;
        moved.roaming = -1;
        combineValues(moved);
        int idxVpnBackground = findIndex(underlyingIface, tunUid, 0, 0, 0, 0);
        if (idxVpnBackground != -1) {
            tunSubtract(idxVpnBackground, this, moved);
        }
        int idxVpnForeground = findIndex(underlyingIface, tunUid, 1, 0, 0, 0);
        if (idxVpnForeground != -1) {
            tunSubtract(idxVpnForeground, this, moved);
        }
    }

    private static void tunSubtract(int i, NetworkStats left, Entry right) {
        long rxBytes = Math.min(left.rxBytes[i], right.rxBytes);
        long[] jArr = left.rxBytes;
        jArr[i] = jArr[i] - rxBytes;
        right.rxBytes -= rxBytes;
        long rxPackets = Math.min(left.rxPackets[i], right.rxPackets);
        jArr = left.rxPackets;
        jArr[i] = jArr[i] - rxPackets;
        right.rxPackets -= rxPackets;
        long txBytes = Math.min(left.txBytes[i], right.txBytes);
        jArr = left.txBytes;
        jArr[i] = jArr[i] - txBytes;
        right.txBytes -= txBytes;
        long txPackets = Math.min(left.txPackets[i], right.txPackets);
        jArr = left.txPackets;
        jArr[i] = jArr[i] - txPackets;
        right.txPackets -= txPackets;
    }
}
