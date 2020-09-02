package com.android.server;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

class OppoRpmStatsTracker {
    public static boolean DEBUG_DETAIL = true;
    static final String OPPO_RPM_MASTER_STATS = "/d/spm/oppo_rpmh_master_stats";
    static final String OPPO_RPM_STATS = "/d/spm/oppo_rpmh_stats";
    private static final String TAG = "OppoRpmStatsTracker";
    int mAdspStats = 0;
    int mAdspStatsScreenDelta = 0;
    long mAdspStatsTime = 0;
    long mAdspStatsTimeScreenDelta = 0;
    double mAdspSuspendRatio = 0.0d;
    int mApssStats = 0;
    int mApssStatsScreenDelta = 0;
    long mApssStatsTime = 0;
    long mApssStatsTimeScreenDelta = 0;
    double mApssSuspendRatio = 0.0d;
    int mBatteryLevel = 0;
    int mCdspStats = 0;
    int mCdspStatsScreenDelta = 0;
    long mCdspStatsTime = 0;
    long mCdspStatsTimeScreenDelta = 0;
    private Context mContext;
    long mCurrentElapseRealTime = 0;
    int mLastAdspStats = 0;
    long mLastAdspStatsTime = 0;
    int mLastApssStats = 0;
    long mLastApssStatsTime = 0;
    int mLastBatteryLevel = 0;
    int mLastCdspStats = 0;
    long mLastCdspStatsTime = 0;
    int mLastMpssStats = 0;
    long mLastMpssStatsTime = 0;
    int mLastTzStats = 0;
    long mLastTzStatsTime = 0;
    long mLastUpdatedElapseRealTime = 0;
    int mLastVlowStats = 0;
    long mLastVlowStatsTime = 0;
    int mLastVminStats = 0;
    long mLastVminStatsTime = 0;
    int mMpssStats = 0;
    int mMpssStatsScreenDelta = 0;
    long mMpssStatsTime = 0;
    long mMpssStatsTimeScreenDelta = 0;
    double mMpssSuspendRatio = 0.0d;
    int mTzStats = 0;
    int mTzStatsScreenDelta = 0;
    long mTzStatsTime = 0;
    long mTzStatsTimeScreenDelta = 0;
    int mVlowStats = 0;
    int mVlowStatsScreenDelta = 0;
    long mVlowStatsTime = 0;
    long mVlowStatsTimeScreenDelta = 0;
    int mVminStats = 0;
    int mVminStatsScreenDelta = 0;
    long mVminStatsTime = 0;
    long mVminStatsTimeScreenDelta = 0;
    double mVminSuspendRatio = 0.0d;

    public OppoRpmStatsTracker(Context context, Handler handler) {
        this.mContext = context;
    }

    private void measureOppoRpmMasterStatsDelta() {
        Slog.d(TAG, "measureOppoRpmMasterStatsDelta... ");
        long elapsedRealTimeDelta = this.mCurrentElapseRealTime - this.mLastUpdatedElapseRealTime;
        Slog.d(TAG, "elapsedRealTimeDelta :" + elapsedRealTimeDelta);
        if (elapsedRealTimeDelta > 0) {
            this.mApssStatsScreenDelta = this.mApssStats - this.mLastApssStats;
            this.mMpssStatsScreenDelta = this.mMpssStats - this.mLastMpssStats;
            this.mAdspStatsScreenDelta = this.mAdspStats - this.mLastAdspStats;
            this.mCdspStatsScreenDelta = this.mCdspStats - this.mLastCdspStats;
            this.mVlowStatsScreenDelta = this.mVlowStats - this.mLastVlowStats;
            this.mVminStatsScreenDelta = this.mVminStats - this.mLastVminStats;
            this.mTzStatsScreenDelta = this.mTzStats - this.mLastTzStats;
            Slog.d(TAG, "[" + this.mApssStatsScreenDelta + "," + this.mMpssStatsScreenDelta + "," + this.mAdspStatsScreenDelta + "," + this.mCdspStatsScreenDelta + "," + this.mVlowStatsScreenDelta + "," + this.mVminStatsScreenDelta + "," + this.mTzStatsScreenDelta + "]");
            this.mApssStatsTimeScreenDelta = this.mApssStatsTime - this.mLastApssStatsTime;
            this.mMpssStatsTimeScreenDelta = this.mMpssStatsTime - this.mLastMpssStatsTime;
            this.mAdspStatsTimeScreenDelta = this.mAdspStatsTime - this.mLastAdspStatsTime;
            this.mCdspStatsTimeScreenDelta = this.mCdspStatsTime - this.mLastCdspStatsTime;
            this.mTzStatsTimeScreenDelta = this.mTzStatsTime - this.mLastTzStatsTime;
            this.mVlowStatsTimeScreenDelta = this.mVlowStatsTime - this.mLastVlowStatsTime;
            this.mVminStatsTimeScreenDelta = this.mVminStatsTime - this.mLastVminStatsTime;
            Slog.d(TAG, "[" + this.mApssStatsTimeScreenDelta + "," + this.mMpssStatsTimeScreenDelta + "," + this.mAdspStatsTimeScreenDelta + "," + this.mCdspStatsTimeScreenDelta + "," + this.mVlowStatsTimeScreenDelta + "," + this.mVminStatsTimeScreenDelta + "," + this.mTzStatsTimeScreenDelta + "]");
            this.mApssSuspendRatio = (((double) this.mApssStatsTimeScreenDelta) / ((double) elapsedRealTimeDelta)) * 100.0d;
            this.mMpssSuspendRatio = (((double) this.mMpssStatsTimeScreenDelta) / ((double) elapsedRealTimeDelta)) * 100.0d;
            this.mAdspSuspendRatio = (((double) this.mAdspStatsTimeScreenDelta) / ((double) elapsedRealTimeDelta)) * 100.0d;
            this.mVminSuspendRatio = (((double) this.mVminStatsTimeScreenDelta) / ((double) elapsedRealTimeDelta)) * 100.0d;
            StringBuilder sb = new StringBuilder();
            sb.append("ApssSuspendRatio:");
            sb.append(this.mApssSuspendRatio);
            sb.append("  MpssSuspendRatio:");
            sb.append(this.mMpssSuspendRatio);
            sb.append("  AdspSuspendRatio:");
            sb.append(this.mAdspSuspendRatio);
            sb.append("  VminSuspendRatio:");
            sb.append(this.mVminSuspendRatio);
            Slog.d(TAG, sb.toString());
        }
    }

    private void trigger() {
        this.mLastUpdatedElapseRealTime = this.mCurrentElapseRealTime;
        this.mCurrentElapseRealTime = SystemClock.elapsedRealtime();
        getOppoRpmStatsScreen();
        getOppoRpmMasterStatsScreen();
        measureOppoRpmMasterStatsDelta();
    }

    private void getOppoRpmStatsScreen() {
        this.mLastVlowStats = this.mVlowStats;
        this.mLastVlowStatsTime = this.mVlowStatsTime;
        this.mLastVminStats = this.mVminStats;
        this.mLastVminStatsTime = this.mVminStatsTime;
        File file = new File(OPPO_RPM_STATS);
        BufferedReader reader = null;
        Slog.d(TAG, "getOppoRpmStatsScreen:/d/spm/oppo_rpmh_stats");
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(file));
            while (true) {
                String tempString = reader2.readLine();
                if (tempString != null) {
                    Slog.d(TAG, tempString);
                    if (tempString.length() != 0) {
                        String[] rpmStats = tempString.split(":");
                        if (rpmStats.length == 3) {
                            if (rpmStats[0].equals("vlow")) {
                                this.mVlowStats = Integer.parseInt(rpmStats[1], 16);
                                this.mVlowStatsTime = Long.parseLong(rpmStats[2], 16);
                            } else if (rpmStats[0].equals("vmin")) {
                                this.mVminStats = Integer.parseInt(rpmStats[1], 16);
                                this.mVminStatsTime = Long.parseLong(rpmStats[2], 16);
                            }
                        }
                    }
                } else {
                    try {
                        reader2.close();
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (reader != null) {
                reader.close();
            }
        } catch (NumberFormatException e4) {
            e4.printStackTrace();
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
    }

    private void getOppoRpmMasterStatsScreen() {
        this.mLastApssStats = this.mApssStats;
        this.mLastApssStatsTime = this.mApssStatsTime;
        this.mLastMpssStats = this.mMpssStats;
        this.mLastMpssStatsTime = this.mMpssStatsTime;
        this.mLastAdspStats = this.mAdspStats;
        this.mLastAdspStatsTime = this.mAdspStatsTime;
        this.mLastCdspStats = this.mCdspStats;
        this.mLastCdspStatsTime = this.mCdspStatsTime;
        this.mLastTzStats = this.mTzStats;
        this.mLastTzStatsTime = this.mTzStatsTime;
        File file = new File(OPPO_RPM_MASTER_STATS);
        BufferedReader reader = null;
        Slog.d(TAG, "getOppoRpmMasterStatsScreen:/d/spm/oppo_rpmh_master_stats");
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(file));
            while (true) {
                String tempString = reader2.readLine();
                if (tempString != null) {
                    Slog.d(TAG, tempString);
                    if (tempString.length() != 0) {
                        String[] rpmMasterStats = tempString.split(":");
                        if (rpmMasterStats.length == 3) {
                            if (rpmMasterStats[0].equals("APSS")) {
                                this.mApssStats = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mApssStatsTime = Long.parseLong(rpmMasterStats[2], 16);
                            } else if (rpmMasterStats[0].equals("MPSS")) {
                                this.mMpssStats = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mMpssStatsTime = Long.parseLong(rpmMasterStats[2], 16);
                            } else if (rpmMasterStats[0].equals("ADSP")) {
                                this.mAdspStats = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mAdspStatsTime = Long.parseLong(rpmMasterStats[2], 16);
                            } else if (rpmMasterStats[0].equals("CDSP")) {
                                this.mCdspStats = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mCdspStatsTime = Long.parseLong(rpmMasterStats[2], 16);
                            } else if (rpmMasterStats[0].equals("TZ")) {
                                this.mTzStats = Integer.parseInt(rpmMasterStats[1], 16);
                                this.mTzStatsTime = Long.parseLong(rpmMasterStats[2], 16);
                            }
                        }
                    }
                } else {
                    try {
                        reader2.close();
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (reader != null) {
                reader.close();
            }
        } catch (NumberFormatException e4) {
            e4.printStackTrace();
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
    }

    public void onBatteryDrained() {
        Slog.d(TAG, "battery drained... ");
        trigger();
    }

    public void onScreenStateChaned(boolean screenOn) {
    }

    public void onBootCompleted() {
        trigger();
    }
}
