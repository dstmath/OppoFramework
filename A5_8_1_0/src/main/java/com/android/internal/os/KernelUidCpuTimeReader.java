package com.android.internal.os;

import android.os.SystemClock;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Slog;
import android.util.SparseLongArray;
import android.util.TimeUtils;
import com.android.internal.content.NativeLibraryHelper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class KernelUidCpuTimeReader {
    private static final String TAG = "KernelUidCpuTimeReader";
    private static final String sProcFile = "/proc/uid_cputime/show_uid_stat";
    private static final String sRemoveUidProcFile = "/proc/uid_cputime/remove_uid_range";
    private SparseLongArray mLastSystemTimeUs = new SparseLongArray();
    private long mLastTimeReadUs = 0;
    private SparseLongArray mLastUserTimeUs = new SparseLongArray();

    public interface Callback {
        void onUidCpuTime(int i, long j, long j2);
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x017a A:{SYNTHETIC, Splitter: B:32:0x017a} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x01c5 A:{Catch:{ IOException -> 0x0180 }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x017f A:{SYNTHETIC, Splitter: B:35:0x017f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readDelta(Callback callback) {
        Throwable th;
        long nowUs = SystemClock.elapsedRealtime() * 1000;
        Throwable th2 = null;
        BufferedReader reader = null;
        IOException e;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(sProcFile));
            try {
                SimpleStringSplitter simpleStringSplitter = new SimpleStringSplitter(' ');
                while (true) {
                    String line = reader2.readLine();
                    if (line == null) {
                        break;
                    }
                    simpleStringSplitter.setString(line);
                    String uidStr = simpleStringSplitter.next();
                    int uid = Integer.parseInt(uidStr.substring(0, uidStr.length() - 1), 10);
                    long userTimeUs = Long.parseLong(simpleStringSplitter.next(), 10);
                    long systemTimeUs = Long.parseLong(simpleStringSplitter.next(), 10);
                    if (!(callback == null || this.mLastTimeReadUs == 0)) {
                        long userTimeDeltaUs = userTimeUs;
                        long systemTimeDeltaUs = systemTimeUs;
                        int index = this.mLastUserTimeUs.indexOfKey(uid);
                        if (index >= 0) {
                            userTimeDeltaUs = userTimeUs - this.mLastUserTimeUs.valueAt(index);
                            systemTimeDeltaUs = systemTimeUs - this.mLastSystemTimeUs.valueAt(index);
                            long timeDiffUs = nowUs - this.mLastTimeReadUs;
                            if (userTimeDeltaUs < 0 || systemTimeDeltaUs < 0) {
                                StringBuilder sb = new StringBuilder("Malformed cpu data for UID=");
                                sb.append(uid).append("!\n");
                                sb.append("Time between reads: ");
                                TimeUtils.formatDuration(timeDiffUs / 1000, sb);
                                sb.append("\n");
                                sb.append("Previous times: u=");
                                TimeUtils.formatDuration(this.mLastUserTimeUs.valueAt(index) / 1000, sb);
                                sb.append(" s=");
                                TimeUtils.formatDuration(this.mLastSystemTimeUs.valueAt(index) / 1000, sb);
                                sb.append("\nCurrent times: u=");
                                TimeUtils.formatDuration(userTimeUs / 1000, sb);
                                sb.append(" s=");
                                TimeUtils.formatDuration(systemTimeUs / 1000, sb);
                                sb.append("\nDelta: u=");
                                TimeUtils.formatDuration(userTimeDeltaUs / 1000, sb);
                                sb.append(" s=");
                                TimeUtils.formatDuration(systemTimeDeltaUs / 1000, sb);
                                Slog.e(TAG, sb.toString());
                                userTimeDeltaUs = 0;
                                systemTimeDeltaUs = 0;
                            }
                        }
                        if (!(userTimeDeltaUs == 0 && systemTimeDeltaUs == 0)) {
                            callback.onUidCpuTime(uid, userTimeDeltaUs, systemTimeDeltaUs);
                        }
                    }
                    this.mLastUserTimeUs.put(uid, userTimeUs);
                    this.mLastSystemTimeUs.put(uid, systemTimeUs);
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                    }
                } else {
                    this.mLastTimeReadUs = nowUs;
                }
            } catch (Throwable th4) {
                th = th4;
                reader = reader2;
                if (reader != null) {
                }
                if (th2 == null) {
                }
            }
            Slog.e(TAG, "Failed to read uid_cputime: " + e.getMessage());
            this.mLastTimeReadUs = nowUs;
        } catch (Throwable th5) {
            th = th5;
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable th6) {
                    if (th2 == null) {
                        th2 = th6;
                    } else if (th2 != th6) {
                        th2.addSuppressed(th6);
                    }
                }
            }
            if (th2 == null) {
                try {
                    throw th2;
                } catch (IOException e3) {
                    e = e3;
                }
            } else {
                throw th;
            }
        }
    }

    public void removeUid(int uid) {
        int index = this.mLastSystemTimeUs.indexOfKey(uid);
        if (index >= 0) {
            this.mLastSystemTimeUs.removeAt(index);
            this.mLastUserTimeUs.removeAt(index);
        }
        removeUidsFromKernelModule(uid, uid);
    }

    public void removeUidsInRange(int startUid, int endUid) {
        if (endUid >= startUid) {
            this.mLastSystemTimeUs.put(startUid, 0);
            this.mLastUserTimeUs.put(startUid, 0);
            this.mLastSystemTimeUs.put(endUid, 0);
            this.mLastUserTimeUs.put(endUid, 0);
            int startIndex = this.mLastSystemTimeUs.indexOfKey(startUid);
            int endIndex = this.mLastSystemTimeUs.indexOfKey(endUid);
            this.mLastSystemTimeUs.removeAtRange(startIndex, (endIndex - startIndex) + 1);
            this.mLastUserTimeUs.removeAtRange(startIndex, (endIndex - startIndex) + 1);
            removeUidsFromKernelModule(startUid, endUid);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0090 A:{SYNTHETIC, Splitter: B:22:0x0090} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00a3 A:{Catch:{ IOException -> 0x0096 }} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0095 A:{SYNTHETIC, Splitter: B:25:0x0095} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void removeUidsFromKernelModule(int startUid, int endUid) {
        IOException e;
        Throwable th;
        Throwable th2 = null;
        Slog.d(TAG, "Removing uids " + startUid + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + endUid);
        FileWriter writer = null;
        try {
            FileWriter writer2 = new FileWriter(sRemoveUidProcFile);
            try {
                writer2.write(startUid + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + endUid);
                writer2.flush();
                if (writer2 != null) {
                    try {
                        writer2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                        writer = writer2;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                writer = writer2;
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (IOException e3) {
                        e = e3;
                        Slog.e(TAG, "failed to remove uids " + startUid + " - " + endUid + " from uid_cputime module", e);
                        return;
                    }
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (writer != null) {
            }
            if (th2 == null) {
            }
        }
    }
}
