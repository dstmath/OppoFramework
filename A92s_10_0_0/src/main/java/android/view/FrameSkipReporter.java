package android.view;

import android.app.ActivityThread;
import android.app.OppoActivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

class FrameSkipReporter {
    private static final String TAG = "Choreographer#FrameSkipReporter";
    private static long mLastSkipTime = 0;
    /* access modifiers changed from: private */
    public static OppoActivityManager mOAms;
    private static Handler mPerfDataReporterHandler;
    private static HandlerThread mPerfDataReporterThread;

    FrameSkipReporter() {
    }

    static {
        mPerfDataReporterHandler = null;
        mPerfDataReporterThread = null;
        mOAms = null;
        mPerfDataReporterThread = new HandlerThread("PerfDataReporter");
        mPerfDataReporterThread.start();
        mPerfDataReporterHandler = mPerfDataReporterThread.getThreadHandler();
        mOAms = new OppoActivityManager();
    }

    public static boolean checkDuplicate(long thisSkipTime, long diff) {
        long j = mLastSkipTime;
        if (j > 0 && thisSkipTime < j + diff) {
            return true;
        }
        mLastSkipTime = thisSkipTime;
        return false;
    }

    public static void report(final long skippedFrames) {
        final long currentTime = System.currentTimeMillis();
        mPerfDataReporterHandler.post(new Runnable() {
            /* class android.view.FrameSkipReporter.AnonymousClass1 */

            public void run() {
                try {
                    Log.d(FrameSkipReporter.TAG, "Process " + ActivityThread.currentProcessName() + "(pid " + Process.myPid() + ") reported " + skippedFrames + " frame(s) skipped");
                    FrameSkipReporter.mOAms.reportSkippedFrames(currentTime, skippedFrames);
                } catch (Exception e) {
                    Log.w(FrameSkipReporter.TAG, ActivityThread.currentProcessName() + " failed to report skipped frames, error " + e.toString());
                }
            }
        });
    }
}
