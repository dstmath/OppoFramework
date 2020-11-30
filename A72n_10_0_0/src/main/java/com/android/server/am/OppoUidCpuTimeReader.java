package com.android.server.am;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Slog;
import android.util.SparseLongArray;
import com.android.internal.os.KernelCpuUidTimeReader;
import com.android.server.display.ai.utils.BrightnessConstants;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class OppoUidCpuTimeReader {
    public static final String ACTION_DETECT_APP_CPU_TIME = "oppo.intent.action.ACTION_DETECT_APP_CPU_TIME";
    private static final int ANDROID_M_CPU_UNIT_DEN = 10;
    private static final String CPU_FILE_PATH = ("/sys/devices/system/cpu" + File.separator);
    private static final int MAX_APP_NUM = 5;
    private static final String TAG = "OppoUidCpuTimeReader";
    private static final Comparator<UidTime> sLoadComparator = new Comparator<UidTime>() {
        /* class com.android.server.am.OppoUidCpuTimeReader.AnonymousClass1 */

        public final int compare(UidTime uta, UidTime utb) {
            long ta = uta.t;
            long tb = utb.t;
            if (ta != tb) {
                return ta > tb ? -1 : 1;
            }
            return 0;
        }
    };
    private long lastTimeMs;
    private Context mContext;
    private int mCoreNum;
    private KernelCpuUidTimeReader.KernelCpuUidUserSysTimeReader mKernelUidCpuTimeReader;
    public final ArrayList<UidTime> mUidTime;
    private boolean mUidTimeSorted;
    public SparseLongArray mUpdatedUids;
    private long totalTimeMs;

    public OppoUidCpuTimeReader(Context context) {
        this.mUpdatedUids = new SparseLongArray();
        this.mKernelUidCpuTimeReader = new KernelCpuUidTimeReader.KernelCpuUidUserSysTimeReader(true);
        this.mUidTime = new ArrayList<>();
        this.mUidTimeSorted = true;
        this.lastTimeMs = 0;
        this.totalTimeMs = 0;
        this.mCoreNum = 1;
        this.mCoreNum = getAllCoreNum();
        this.mContext = context;
    }

    public void update() {
        long startTimeMs = SystemClock.elapsedRealtime();
        this.totalTimeMs = startTimeMs - this.lastTimeMs;
        this.mKernelUidCpuTimeReader.readDelta(new KernelCpuUidTimeReader.Callback() {
            /* class com.android.server.am.$$Lambda$OppoUidCpuTimeReader$BINI4nYSvQcnmvDMZVQ0qc1s7bM */

            public final void onUidCpuTime(int i, Object obj) {
                OppoUidCpuTimeReader.this.lambda$update$0$OppoUidCpuTimeReader(i, (long[]) obj);
            }
        });
        this.mUidTimeSorted = false;
        this.lastTimeMs = startTimeMs;
        Slog.d(TAG, " totalTimeMs =" + this.totalTimeMs + "ms");
    }

    public /* synthetic */ void lambda$update$0$OppoUidCpuTimeReader(int uid, long[] time) {
        SparseLongArray sparseLongArray = this.mUpdatedUids;
        if (sparseLongArray != null) {
            sparseLongArray.put(uid, time[0]);
        }
    }

    public String getNameByUid(int id) {
        return this.mContext.getPackageManager().getNameForUid(id);
    }

    public void upLoadUCTData() {
        Intent intent = new Intent(ACTION_DETECT_APP_CPU_TIME);
        int N = 5;
        if (this.mUidTime.size() <= 5) {
            N = this.mUidTime.size();
        }
        intent.putExtra("size", N);
        for (int i = 0; i < N; i++) {
            UidTime ut = this.mUidTime.get(i);
            ut.name = getNameByUid(ut.uid);
            if (ut.name == null) {
                ut.name = "noName";
            }
            intent.putExtra("Top" + (i + 1) + ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, ut.uid);
            intent.putExtra("Top" + (i + 1) + BrightnessConstants.AppSplineXml.TAG_APP, ut.name);
            intent.putExtra("Top" + (i + 1) + BrightnessConstants.AppSplineXml.TAG_TRAIN_TIME, ut.t);
            Slog.d(TAG, " ut.uid = " + ut.uid + " ut.name=" + ut.name + " tt.t = " + ut.t);
        }
        this.mContext.sendBroadcast(intent);
    }

    public List<String> getUidCpuWorkingStats() {
        int N = 5;
        if (this.mUidTime.size() <= 5) {
            N = this.mUidTime.size();
        }
        List<String> returnList = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            UidTime ut = this.mUidTime.get(i);
            ut.name = getNameByUid(ut.uid);
            if (ut.name == null) {
                ut.name = "noName";
            }
            Slog.d(TAG, " mCoreNum = " + this.mCoreNum + " ut.name=" + ut.name + " tt.t = " + ut.t);
            returnList.add("uid = " + ut.uid + " precent = " + ((float) ((ut.t * 100) / ((this.totalTimeMs * ((long) this.mCoreNum)) * 1000))) + " name= " + ut.name);
        }
        return returnList;
    }

    /* access modifiers changed from: package-private */
    public static class CpuFilter implements FileFilter {
        CpuFilter() {
        }

        public boolean accept(File pathname) {
            if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                return true;
            }
            return false;
        }
    }

    private int getAllCoreNum() {
        try {
            File[] files = new File(CPU_FILE_PATH).listFiles(new CpuFilter());
            if (files == null) {
                Slog.d(TAG, "files is null.");
                return 1;
            }
            Slog.d(TAG, "CPU Count: " + files.length);
            return files.length;
        } catch (Exception e) {
            Slog.i(TAG, "CPU Count: Failed. e =" + e);
            return 1;
        }
    }

    public void calTopUid() {
        if (!this.mUidTimeSorted) {
            this.mUidTime.clear();
            int N = this.mUpdatedUids.size();
            for (int i = 0; i < N; i++) {
                UidTime ut = new UidTime();
                ut.uid = this.mUpdatedUids.keyAt(i);
                ut.t = this.mUpdatedUids.valueAt(i);
                this.mUidTime.add(ut);
            }
            Collections.sort(this.mUidTime, sLoadComparator);
            this.mUidTimeSorted = true;
        }
    }

    public UidTime getUidTime(int index) {
        return this.mUidTime.get(index);
    }

    public int getUidTimeSize() {
        return this.mUidTime.size();
    }

    public class UidTime {
        public String name;
        public long t;
        public int uid;

        public UidTime() {
        }
    }
}
