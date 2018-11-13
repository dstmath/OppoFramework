package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class DiskStatsService extends Binder {
    private static final String TAG = "DiskStatsService";
    private final Context mContext;

    public DiskStatsService(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:41:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x008f  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x009e A:{SYNTHETIC, Splitter: B:25:0x009e} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x008f  */
    /* JADX WARNING: Removed duplicated region for block: B:41:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00a7 A:{SYNTHETIC, Splitter: B:30:0x00a7} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        IOException e;
        long after;
        Throwable th;
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        byte[] junk = new byte[512];
        for (int i = 0; i < junk.length; i++) {
            junk[i] = (byte) i;
        }
        File tmp = new File(Environment.getDataDirectory(), "system/perftest.tmp");
        FileOutputStream fos = null;
        IOException error = null;
        long before = SystemClock.uptimeMillis();
        try {
            FileOutputStream fos2 = new FileOutputStream(tmp);
            try {
                fos2.write(junk);
                if (fos2 != null) {
                    try {
                        fos2.close();
                    } catch (IOException e2) {
                    }
                }
                fos = fos2;
            } catch (IOException e3) {
                e = e3;
                fos = fos2;
                error = e;
                if (fos != null) {
                }
                after = SystemClock.uptimeMillis();
                if (tmp.exists()) {
                }
                if (error != null) {
                }
                reportFreeSpace(Environment.getDataDirectory(), "Data", pw);
                reportFreeSpace(Environment.getDownloadCacheDirectory(), "Cache", pw);
                reportFreeSpace(new File("/system"), "System", pw);
                if (StorageManager.isFileEncryptedNativeOnly()) {
                }
            } catch (Throwable th2) {
                th = th2;
                fos = fos2;
                if (fos != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e = e4;
            error = e;
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e5) {
                }
            }
            after = SystemClock.uptimeMillis();
            if (tmp.exists()) {
            }
            if (error != null) {
            }
            reportFreeSpace(Environment.getDataDirectory(), "Data", pw);
            reportFreeSpace(Environment.getDownloadCacheDirectory(), "Cache", pw);
            reportFreeSpace(new File("/system"), "System", pw);
            if (StorageManager.isFileEncryptedNativeOnly()) {
            }
        } catch (Throwable th3) {
            th = th3;
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e6) {
                }
            }
            throw th;
        }
        after = SystemClock.uptimeMillis();
        if (tmp.exists()) {
            tmp.delete();
        }
        if (error != null) {
            pw.print("Test-Error: ");
            pw.println(error.toString());
        } else {
            pw.print("Latency: ");
            pw.print(after - before);
            pw.println("ms [512B Data Write]");
        }
        reportFreeSpace(Environment.getDataDirectory(), "Data", pw);
        reportFreeSpace(Environment.getDownloadCacheDirectory(), "Cache", pw);
        reportFreeSpace(new File("/system"), "System", pw);
        if (StorageManager.isFileEncryptedNativeOnly()) {
            pw.println("File-based Encryption: true");
        }
    }

    private void reportFreeSpace(File path, String name, PrintWriter pw) {
        try {
            StatFs statfs = new StatFs(path.getPath());
            long bsize = (long) statfs.getBlockSize();
            long avail = (long) statfs.getAvailableBlocks();
            long total = (long) statfs.getBlockCount();
            if (bsize <= 0 || total <= 0) {
                throw new IllegalArgumentException("Invalid stat: bsize=" + bsize + " avail=" + avail + " total=" + total);
            }
            pw.print(name);
            pw.print("-Free: ");
            pw.print((avail * bsize) / 1024);
            pw.print("K / ");
            pw.print((total * bsize) / 1024);
            pw.print("K total = ");
            pw.print((100 * avail) / total);
            pw.println("% free");
        } catch (IllegalArgumentException e) {
            pw.print(name);
            pw.print("-Error: ");
            pw.println(e.toString());
        }
    }
}
