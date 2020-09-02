package com.android.server.am;

import android.os.Handler;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

public class ColorAbnormalAppStatistics {
    private static final String TAG = "ColorAbnormalAppManager";
    static final String TYPE_ACTIVITY = "activity";
    static final int TYPE_ACTIVITY_PASS = 2;
    static final int TYPE_ACTIVITY_RESTRICT = 1;
    static final String TYPE_BROADCAST = "broadcast";
    static final int TYPE_BROADCAST_PASS = 4;
    static final int TYPE_BROADCAST_RESTRICT = 3;
    static final int TYPE_OTHER_PASS = 12;
    static final int TYPE_OTHER_RESTRICT = 11;
    static final String TYPE_PROVIDER = "content provider";
    static final int TYPE_PROVIDER_PASS = 8;
    static final int TYPE_PROVIDER_RESTRICT = 7;
    static final String TYPE_REPEAT = "repeat";
    static final int TYPE_REPEAT_PASS = 21;
    static final int TYPE_REPEAT_RESTRICT = 20;
    static final String TYPE_RESTART = "restart";
    static final int TYPE_RESTART_PASS = 10;
    static final int TYPE_RESTART_RESTRICT = 9;
    static final String TYPE_SERVICE = "service";
    static final int TYPE_SERVICE_PASS = 6;
    static final int TYPE_SERVICE_RESTRICT = 5;
    static final String UPLOAD_AAR_KEY_A_PASS_NUM = "a_pnum";
    static final String UPLOAD_AAR_KEY_A_RESTRICT_NUM = "a_rnum";
    static final String UPLOAD_AAR_KEY_B_PASS_NUM = "b_pnum";
    static final String UPLOAD_AAR_KEY_B_RESTRICT_NUM = "b_rnum";
    static final String UPLOAD_AAR_KEY_OTHER_PASS_NUM = "other_pnum";
    static final String UPLOAD_AAR_KEY_OTHER_RESTRICT_NUM = "other_rnum";
    static final String UPLOAD_AAR_KEY_PKGNAME = "pkg";
    static final String UPLOAD_AAR_KEY_P_PASS_NUM = "p_pnum";
    static final String UPLOAD_AAR_KEY_P_RESTRICT_NUM = "p_rnum";
    static final String UPLOAD_AAR_KEY_REPEAT_PASS_NUM = "repeat_pnum";
    static final String UPLOAD_AAR_KEY_REPEAT_RESTRICT_NUM = "repeat_rnum";
    static final String UPLOAD_AAR_KEY_R_PASS_NUM = "r_pnum";
    static final String UPLOAD_AAR_KEY_R_RESTRICT_NUM = "r_rnum";
    static final String UPLOAD_AAR_KEY_S_PASS_NUM = "s_pnum";
    static final String UPLOAD_AAR_KEY_S_RESTRICT_NUM = "s_rnum";
    static final String UPLOAD_ABNORMAL_APP_RESTRICT_EVENTID = "abnormalApp_restrict";
    static final int UPLOAD_COUNT_NUM = 200;
    static final String UPLOAD_LOGTAG = "20089";
    private static ColorAbnormalAppStatistics mColorAbnormalAppStatistics = null;
    private IColorAbnormalAppManager mAbnormalAppManager = null;
    private ActivityManagerService mAms = null;
    /* access modifiers changed from: private */
    public List<ColorAbnormalAppInfo> mCollectAbnormalAppInfoList = new ArrayList();
    private Handler mHandler = null;
    /* access modifiers changed from: private */
    public final Object mListLock = new Object();
    private boolean mSwitchMonitor = false;

    private ColorAbnormalAppStatistics() {
    }

    public static final ColorAbnormalAppStatistics getInstance() {
        if (mColorAbnormalAppStatistics == null) {
            mColorAbnormalAppStatistics = new ColorAbnormalAppStatistics();
        }
        return mColorAbnormalAppStatistics;
    }

    /* access modifiers changed from: protected */
    public void init(IColorAbnormalAppManager abnormalAppManager, Handler handler, ActivityManagerService ams) {
        this.mAbnormalAppManager = abnormalAppManager;
        this.mHandler = handler;
        this.mAms = ams;
        this.mSwitchMonitor = ColorAppStartupManagerUtils.getInstance().isForumVersion();
    }

    /* access modifiers changed from: protected */
    public void collectAbnormalAppInfo(String pkgName, String hostType, boolean isRestrict) {
        IColorAbnormalAppManager iColorAbnormalAppManager;
        if (isNeedUpload() && this.mHandler != null && (iColorAbnormalAppManager = this.mAbnormalAppManager) != null) {
            if (iColorAbnormalAppManager.getDynamicDebug()) {
                Slog.d(TAG, "collectAppStartInfo: pkgName = " + pkgName + " hostType = " + hostType + " isRestrict = " + isRestrict);
            }
            this.mHandler.post(new CollectAbnormalAppRunnable(pkgName, hostType, isRestrict));
        }
    }

    private class CollectAbnormalAppRunnable implements Runnable {
        private String mHostType;
        private boolean mIsRestrict;
        private String mPkgName;

        public CollectAbnormalAppRunnable(String pkgName, String hostType, boolean isRestrict) {
            this.mIsRestrict = isRestrict;
            this.mPkgName = pkgName;
            this.mHostType = hostType;
        }

        public void run() {
            int length;
            ColorAbnormalAppStatistics.this.collectAbnormalAppInfo(this.mIsRestrict, this.mPkgName, this.mHostType);
            synchronized (ColorAbnormalAppStatistics.this.mListLock) {
                length = ColorAbnormalAppStatistics.this.mCollectAbnormalAppInfoList.size();
            }
            if (length >= ColorAppStartupManagerUtils.getInstance().getCheckCount()) {
                ColorAbnormalAppStatistics.this.uploadAbnormalAppInfoList();
            }
        }
    }

    /* access modifiers changed from: private */
    public void collectAbnormalAppInfo(boolean isRestrict, String pkgName, String hostType) {
        int type;
        if (TYPE_ACTIVITY.equals(hostType)) {
            if (isRestrict) {
                type = 1;
            } else {
                type = 2;
            }
        } else if (TYPE_BROADCAST.equals(hostType)) {
            if (isRestrict) {
                type = 3;
            } else {
                type = 4;
            }
        } else if (TYPE_SERVICE.equals(hostType)) {
            if (isRestrict) {
                type = 5;
            } else {
                type = 6;
            }
        } else if (TYPE_PROVIDER.equals(hostType)) {
            if (isRestrict) {
                type = 7;
            } else {
                type = 8;
            }
        } else if (TYPE_RESTART.equals(hostType)) {
            if (isRestrict) {
                type = 9;
            } else {
                type = 10;
            }
        } else if (TYPE_REPEAT.equals(hostType)) {
            if (isRestrict) {
                type = TYPE_REPEAT_RESTRICT;
            } else {
                type = TYPE_REPEAT_PASS;
            }
        } else if (isRestrict) {
            type = 11;
        } else {
            type = TYPE_OTHER_PASS;
        }
        ColorAbnormalAppInfo appInfo = getAbnormalAppInfo(pkgName);
        if (appInfo == null) {
            ColorAbnormalAppInfo info = ColorAbnormalAppInfo.builder(isRestrict, pkgName, type);
            synchronized (this.mListLock) {
                this.mCollectAbnormalAppInfoList.add(info);
            }
            return;
        }
        appInfo.increaseTypeCount(isRestrict, type);
    }

    private ColorAbnormalAppInfo getAbnormalAppInfo(String pkgName) {
        synchronized (this.mListLock) {
            for (ColorAbnormalAppInfo appInfo : this.mCollectAbnormalAppInfoList) {
                if (appInfo.getPkgName().equals(pkgName)) {
                    return appInfo;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void uploadAbnormalAppInfoList() {
        if (!this.mCollectAbnormalAppInfoList.isEmpty() && this.mAbnormalAppManager != null) {
            List<Map<String, String>> uploadList = new ArrayList<>();
            synchronized (this.mListLock) {
                int length = this.mCollectAbnormalAppInfoList.size();
                for (int i = 0; i < length; i++) {
                    ColorAbnormalAppInfo appInfo = this.mCollectAbnormalAppInfoList.get(i);
                    if (appInfo != null) {
                        uploadList.add(appInfo.getAbnormalAppMap());
                    }
                }
                this.mCollectAbnormalAppInfoList.clear();
            }
            if (this.mAbnormalAppManager.getDynamicDebug()) {
                Slog.d(TAG, "AbnormalAppList size " + uploadList.size());
                Iterator<Map<String, String>> it = uploadList.iterator();
                while (it.hasNext()) {
                    Slog.d(TAG, "AbnormalAppList info " + it.next());
                }
            }
            ActivityManagerService activityManagerService = this.mAms;
            if (activityManagerService != null) {
                OppoStatistics.onCommon(activityManagerService.mContext, UPLOAD_LOGTAG, UPLOAD_ABNORMAL_APP_RESTRICT_EVENTID, uploadList, false);
            }
        }
    }

    public boolean isNeedUpload() {
        return this.mSwitchMonitor;
    }
}
