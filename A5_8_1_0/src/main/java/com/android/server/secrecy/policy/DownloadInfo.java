package com.android.server.secrecy.policy;

import com.android.server.secrecy.policy.util.LogUtil;
import com.android.server.secrecy.policy.util.Utils;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class DownloadInfo {
    private static String TAG = "SecrecyService.FlashInfo";
    private String mCurrentDownloadDate;
    private boolean mCurrentDownloadInternal;
    private String mCurrentDownloadStatus;
    private String mCurrentDownloadTime;
    private long mCurrentDownloadTimeInMillis;
    private long mLastDownloadTimeInMillis;

    public void readDownloadInfo() {
        this.mCurrentDownloadStatus = Utils.getDownloadStatusString();
        this.mCurrentDownloadInternal = Utils.isFlashedInternal(this.mCurrentDownloadStatus);
        this.mCurrentDownloadDate = Utils.getDownloadDate(this.mCurrentDownloadStatus);
        this.mCurrentDownloadTime = Utils.getDownloadTime(this.mCurrentDownloadStatus);
        this.mCurrentDownloadTimeInMillis = Utils.getFlashIimeInMillis(this.mCurrentDownloadDate, this.mCurrentDownloadTime);
    }

    public boolean isCurrentDownloadInternal() {
        return this.mCurrentDownloadInternal;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println("mCurrentDownloadInternal   = " + this.mCurrentDownloadInternal);
        pw.print(prefix);
        pw.println("mCurrentDownloadStatus = " + this.mCurrentDownloadStatus);
        pw.print(prefix);
        pw.println("mCurrentDownloadTimeInMillis = " + this.mCurrentDownloadTimeInMillis + ", mLastDownloadTimeInMillis = " + this.mLastDownloadTimeInMillis);
        pw.print(prefix);
    }

    public long getCurrentDownloadTimeInMills() {
        return this.mCurrentDownloadTimeInMillis;
    }

    public long getLastDownloadTimeInMills() {
        return this.mLastDownloadTimeInMillis;
    }

    public void setLastDownloadTimeInMills(String lastDownloadTimeInMillis) {
        if (lastDownloadTimeInMillis != null) {
            this.mLastDownloadTimeInMillis = Long.parseLong(lastDownloadTimeInMillis);
        }
        LogUtil.d(TAG, "setLastDownloadTimeInMills, mLastDownloadTimeInMillis = " + this.mLastDownloadTimeInMillis + ", lastDownloadTimeInMillis = " + lastDownloadTimeInMillis);
    }
}
