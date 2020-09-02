package com.color.inner.net;

import android.net.NetworkStats;
import android.util.Log;

public class NetworkStatsWrapper {
    private static final String TAG = "NetworkStatsWrapper";
    private NetworkStats mNetworkStats;

    NetworkStatsWrapper(NetworkStats networkStats) {
        this.mNetworkStats = networkStats;
    }

    public long getTotalBytes() {
        try {
            NetworkStats.Entry entry = this.mNetworkStats.getTotal((NetworkStats.Entry) null);
            return entry.rxBytes + entry.txBytes;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }

    public int size() {
        try {
            return this.mNetworkStats.size();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }

    public EntryWrapper getValues(int i, EntryWrapper entryWrapper) {
        try {
            return new EntryWrapper(this.mNetworkStats.getValues(i, entryWrapper == null ? null : entryWrapper.mEntry));
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static class EntryWrapper {
        private static final String TAG_Entry = "EntryWrapper";
        NetworkStats.Entry mEntry;

        private EntryWrapper(NetworkStats.Entry entry) {
            this.mEntry = entry;
        }

        public long getRxBytes() {
            try {
                return this.mEntry.rxBytes;
            } catch (Throwable e) {
                Log.e(TAG_Entry, e.toString());
                return 0;
            }
        }

        public long getTxBytes() {
            try {
                return this.mEntry.txBytes;
            } catch (Throwable e) {
                Log.e(TAG_Entry, e.toString());
                return 0;
            }
        }
    }
}
