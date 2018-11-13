package com.android.server.am;

import android.os.SystemProperties;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OppoCrashStatisticsManager {
    private static final long CRASH_TIMEOUT = 300000;
    public static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String TAG = "OppoCrashDataStatistics";
    private static OppoCrashStatisticsManager mOppoCrashManager = null;
    private ActivityManagerService mActivityManager = null;
    private ActivityManagerService mAms = null;
    List<OppoCrashStatisticsInfo> mCrashAppList = new ArrayList();
    private final Object mLock = new Object();
    private ProcessRecord mProcessRecord;
    private String mTag;

    public static OppoCrashStatisticsManager getInstance() {
        if (mOppoCrashManager == null) {
            mOppoCrashManager = new OppoCrashStatisticsManager();
        }
        return mOppoCrashManager;
    }

    public void setActivityManager(ActivityManagerService ams) {
        this.mAms = ams;
    }

    public boolean collectCrashAppInfo() {
        if (this.mProcessRecord == null) {
            return false;
        }
        String processName = this.mProcessRecord.processName;
        if (processName == null) {
            return false;
        }
        synchronized (this.mLock) {
            OppoCrashStatisticsInfo appCrashInfo = getCrashAppInfoInList(processName);
            if (DEBUG) {
                Slog.v(TAG, "collectCrashAppInfo appCrashInfo==" + appCrashInfo);
            }
            if (appCrashInfo == null) {
                OppoCrashStatisticsInfo info = new OppoCrashStatisticsInfo(processName, System.currentTimeMillis());
                this.mCrashAppList.add(info);
                Slog.v(TAG, "collectCrashAppInfo add==" + info);
                return true;
            } else if (System.currentTimeMillis() > appCrashInfo.getFirstStartTime() + 300000) {
                appCrashInfo.setFirstStartTime(System.currentTimeMillis());
                return true;
            } else {
                return false;
            }
        }
    }

    public OppoCrashStatisticsInfo getCrashAppInfoInList(String processName) {
        OppoCrashStatisticsInfo resultInfo = null;
        Iterator it = this.mCrashAppList.iterator();
        while (it.hasNext()) {
            OppoCrashStatisticsInfo appinfo = (OppoCrashStatisticsInfo) it.next();
            String proName = appinfo.getProcessName();
            if (proName != null && proName.equals(processName)) {
                resultInfo = appinfo;
            } else if (System.currentTimeMillis() > appinfo.getFirstStartTime() + 300000) {
                it.remove();
            }
        }
        return resultInfo;
    }

    public void setProcessRecord(ProcessRecord processRecord) {
        this.mProcessRecord = processRecord;
    }

    public String getProcessName() {
        if (this.mProcessRecord == null) {
            return null;
        }
        return this.mProcessRecord.processName;
    }
}
