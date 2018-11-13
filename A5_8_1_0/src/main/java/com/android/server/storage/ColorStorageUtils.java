package com.android.server.storage;

import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.util.Slog;
import java.util.Formatter;

public class ColorStorageUtils {
    private static final long DATA_FULL_THRES_128G = 1048576000;
    private static final long DATA_FULL_THRES_16G = 838860800;
    private static final long DATA_FULL_THRES_256G = 1048576000;
    private static final long DATA_FULL_THRES_32G = 1048576000;
    private static final long DATA_FULL_THRES_64G = 1048576000;
    private static final long DATA_LOW_THRES_128G = 7340032000L;
    private static final long DATA_LOW_THRES_16G = 1310720000;
    private static final long DATA_LOW_THRES_256G = 7340032000L;
    private static final long DATA_LOW_THRES_32G = 3932160000L;
    private static final long DATA_LOW_THRES_64G = 4718592000L;
    public static final int DATA_SIMU_MODE_ENOUGH = 103;
    public static final int DATA_SIMU_MODE_FULL = 102;
    public static final int DATA_SIMU_MODE_LOW = 101;
    public static final int DATA_SIMU_MODE_OFF = 0;
    public static final int DATA_SIMU_MODE_REAL_FULL = 104;
    private static final long DATA_SIZE_128G = 137438953472L;
    private static final long DATA_SIZE_16G = 17179869184L;
    private static final long DATA_SIZE_256G = 274877906944L;
    private static final long DATA_SIZE_32G = 34359738368L;
    private static final long DATA_SIZE_64G = 68719476736L;
    private static final long GB_BYTES = 1073741824;
    private static final long KB_BYTES = 1024;
    private static final long MB_BYTES = 1048576;
    private static final String TAG = "DeviceStorageMonitor";
    private static long sDataFreeSpace;
    private static long sDataFullThreshold;
    private static long sDataLowThreshold;
    private static int sDataSimuMode = 0;
    private static StatFs sFileStatsData;
    private static long sShowTotalData;
    private static long sTotalData;

    public static long getShowTotalData() {
        if (isConfidentialVersion()) {
            return DATA_SIZE_16G;
        }
        return sShowTotalData;
    }

    public static long getActualShowTotalData() {
        return sShowTotalData;
    }

    public static long getTotalData() {
        return sTotalData;
    }

    public static long getDataLowThreshold() {
        return sDataLowThreshold;
    }

    public static long getDataFullThreshold() {
        return sDataFullThreshold;
    }

    public static long getLastDataFreeSpace() {
        return sDataFreeSpace;
    }

    public static void setDataSimuMode(int mode) {
        sDataSimuMode = mode;
    }

    public static void getDataThreshold() {
        if (sFileStatsData == null) {
            sFileStatsData = new StatFs(Environment.getDataDirectory().getAbsolutePath());
            sTotalData = sFileStatsData.getBlockCountLong() * sFileStatsData.getBlockSizeLong();
            if (sTotalData > DATA_SIZE_128G) {
                sDataLowThreshold = 7340032000L;
                sDataFullThreshold = 1048576000;
                sShowTotalData = DATA_SIZE_256G;
            } else if (sTotalData > DATA_SIZE_64G) {
                sDataLowThreshold = 7340032000L;
                sDataFullThreshold = 1048576000;
                sShowTotalData = DATA_SIZE_128G;
            } else if (sTotalData > DATA_SIZE_32G) {
                sDataLowThreshold = DATA_LOW_THRES_64G;
                sDataFullThreshold = 1048576000;
                sShowTotalData = DATA_SIZE_64G;
            } else if (sTotalData > DATA_SIZE_16G) {
                sDataLowThreshold = DATA_LOW_THRES_32G;
                sDataFullThreshold = 1048576000;
                sShowTotalData = DATA_SIZE_32G;
            } else {
                sDataLowThreshold = DATA_LOW_THRES_16G;
                sDataFullThreshold = DATA_FULL_THRES_16G;
                sShowTotalData = DATA_SIZE_16G;
            }
            Slog.d(TAG, "dataLowThreshold = " + formatBytesLocked(sDataLowThreshold));
            Slog.d(TAG, "dataFullThreshold = " + formatBytesLocked(sDataFullThreshold));
            Slog.d(TAG, "totalData = " + formatBytesLocked(sShowTotalData));
        }
    }

    public static long getDataFreeSpace() {
        long freeDataSpace = -1;
        if (sDataSimuMode == 101) {
            freeDataSpace = sDataLowThreshold - 1048576;
        } else if (sDataSimuMode == 102) {
            freeDataSpace = sDataFullThreshold - 1048576;
        } else if (sDataSimuMode == 103) {
            freeDataSpace = sDataLowThreshold + 629145600;
        } else if (sDataSimuMode == 104) {
            freeDataSpace = 1;
        }
        if (freeDataSpace != -1) {
            Slog.d(TAG, "getDataFreeSpace: test mode=" + sDataSimuMode + ", freeDataSpace=" + formatBytesLocked(freeDataSpace));
            sDataFreeSpace = freeDataSpace;
            return freeDataSpace;
        } else if (sFileStatsData == null) {
            Slog.d(TAG, "getDataFreeSpace: fileStatsData is null!!!");
            freeDataSpace = sDataLowThreshold + 629145600;
            sDataFreeSpace = freeDataSpace;
            return freeDataSpace;
        } else {
            try {
                sFileStatsData.restat(Environment.getDataDirectory().getAbsolutePath());
                freeDataSpace = sFileStatsData.getAvailableBlocksLong() * sFileStatsData.getBlockSizeLong();
            } catch (IllegalArgumentException e) {
                Slog.d(TAG, "getDataFreeSpace: IllegalArgumentException.");
            }
            sDataFreeSpace = freeDataSpace;
            return freeDataSpace;
        }
    }

    public static String formatBytesLocked(long bytes) {
        StringBuilder formatBuilder = new StringBuilder(32);
        Formatter formatter = new Formatter(formatBuilder);
        formatBuilder.setLength(0);
        if (bytes < 1024) {
            return bytes + "B";
        }
        if (bytes < 1048576) {
            formatter.format("%.2fKB", new Object[]{Double.valueOf(((double) bytes) / 1024.0d)});
            return formatBuilder.toString();
        } else if (bytes < GB_BYTES) {
            formatter.format("%.2fMB", new Object[]{Double.valueOf(((double) bytes) / 1048576.0d)});
            return formatBuilder.toString();
        } else {
            formatter.format("%.2fGB", new Object[]{Double.valueOf(((double) bytes) / 1.073741824E9d)});
            return formatBuilder.toString();
        }
    }

    public static boolean isConfidentialVersion() {
        return SystemProperties.getBoolean("persist.version.confidential", false);
    }
}
