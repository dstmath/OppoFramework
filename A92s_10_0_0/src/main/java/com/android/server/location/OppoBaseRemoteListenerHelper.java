package com.android.server.location;

import android.content.Context;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.server.PswServiceFactory;
import com.android.server.location.interfaces.IPswLbsRomUpdateUtil;

public abstract class OppoBaseRemoteListenerHelper {
    protected static boolean DEBUG = true;
    protected static final int RUNNABLE_COUNT_LIMIT = 200;
    private static final String TAG = "OppoBaseRemoteListenerHelper";
    protected int mInsertedRunnableCount = 0;
    protected boolean mIsNmeaFilterEnable;
    protected final Object mLock = new Object();
    protected int mLogCount = 0;
    protected OppoNmeaController mNmeafilter = null;

    /* access modifiers changed from: protected */
    public abstract void handlerPost(Runnable runnable);

    public OppoBaseRemoteListenerHelper(Context context) {
        this.mNmeafilter = new OppoNmeaController(context);
        this.mIsNmeaFilterEnable = this.mNmeafilter.getNmeaFilter();
    }

    public void setDebug(boolean isDebug) {
        DEBUG = isDebug;
    }

    /* access modifiers changed from: protected */
    public void onPost(Runnable runnable) {
        if (this.mIsNmeaFilterEnable) {
            synchronized (this.mLock) {
                if (this.mInsertedRunnableCount < 200) {
                    if (DEBUG) {
                        Log.v(TAG, "post runnable for current size: " + this.mInsertedRunnableCount);
                    }
                    handlerPost(runnable);
                    this.mInsertedRunnableCount++;
                } else {
                    if (this.mLogCount % 10 == 0) {
                        Log.d(TAG, "Skip post runnable due to size overflow size: " + this.mInsertedRunnableCount);
                        this.mLogCount = 0;
                    }
                    this.mLogCount++;
                }
            }
            return;
        }
        handlerPost(runnable);
    }

    /* access modifiers changed from: protected */
    public void onRun() {
        if (this.mIsNmeaFilterEnable) {
            synchronized (this.mLock) {
                if (DEBUG) {
                    Log.v(TAG, "pop runnable for current size: " + this.mInsertedRunnableCount);
                }
                this.mInsertedRunnableCount--;
            }
        }
    }

    public class OppoNmeaController {
        private static final String KEY_GNSS_NMEA_FILTER_ENABLED = "config_gnssNmeaFilterEnabled";
        private static final String TAG = "OppoNmeaController";
        private Context mContext = null;
        @GuardedBy({"mLock"})
        private boolean mGnssNmeaFilter = true;
        private final Object mLock = new Object();
        private IPswLbsRomUpdateUtil mRomUpdateUtil = null;

        public OppoNmeaController(Context context) {
            this.mContext = context;
            this.mRomUpdateUtil = PswServiceFactory.getInstance().getFeature(IPswLbsRomUpdateUtil.DEFAULT, new Object[]{this.mContext});
            this.mGnssNmeaFilter = this.mRomUpdateUtil.getBoolean(KEY_GNSS_NMEA_FILTER_ENABLED);
        }

        public boolean getNmeaFilter() {
            Log.d(TAG, "GnssNmeaFilter is" + this.mGnssNmeaFilter);
            return this.mGnssNmeaFilter;
        }
    }
}
