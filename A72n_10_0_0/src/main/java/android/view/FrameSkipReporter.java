package android.view;

import android.app.ActivityThread;
import android.app.OppoActivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;

/* access modifiers changed from: package-private */
public class FrameSkipReporter {
    private static final String TAG = "Choreographer#FrameSkipReporter";
    private static long mLastSkipTime = 0;
    private static OppoActivityManager mOAms;
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

    public static void report(final boolean isAnimation, final long skippedFrames) {
        final long currentTime = System.currentTimeMillis();
        mPerfDataReporterHandler.post(new Runnable() {
            /* class android.view.FrameSkipReporter.AnonymousClass2 */

            public void run() {
                try {
                    String forePid = SystemProperties.get("debug.junk.process.pid");
                    boolean isForeground = false;
                    int pid = Process.myPid();
                    if (Integer.parseInt(forePid) == pid) {
                        isForeground = true;
                    }
                    Log.d(FrameSkipReporter.TAG, "Process " + ActivityThread.currentProcessName() + "(pid " + pid + ") reported " + skippedFrames + " frame(s) skipped(anim:" + isAnimation + ", fore:" + isForeground + ")");
                    FrameSkipReporter.mOAms.reportSkippedFrames(currentTime, isAnimation, isForeground, skippedFrames);
                } catch (Exception e) {
                    Log.w(FrameSkipReporter.TAG, ActivityThread.currentProcessName() + " failed to report skipped frames, error " + e.toString());
                }
            }
        });
    }
}
