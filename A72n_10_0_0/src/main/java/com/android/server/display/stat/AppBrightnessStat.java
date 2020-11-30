package com.android.server.display.stat;

import android.content.Context;
import android.net.util.NetworkConstants;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.SystemService;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.IColorEyeProtectManager;
import com.android.server.display.OppoBrightUtils;
import com.android.server.display.stat.BackLightStat;
import com.android.server.pm.CompatibilityHelper;
import com.color.app.ColorAppEnterInfo;
import com.color.app.ColorAppExitInfo;
import com.color.app.ColorAppSwitchConfig;
import com.color.app.ColorAppSwitchManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AppBrightnessStat implements BackLightStat.Callback {
    public static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String DEFAULT_LAUNCHER_APP = "com.oppo.launcher";
    private static final int[] DEFAULT_LEVELS_MONITOR = {20, 40, 80, 200, IColorEyeProtectManager.LEVEL_COLOR_MATRIX_COLOR, SystemService.PHASE_THIRD_PARTY_APPS_CAN_START, 800, OppoBrightUtils.TEN_BITS_MAXBRIGHTNESS, NetworkConstants.ETHER_MTU, 2000, OppoBrightUtils.ELEVEN_BITS_MAXBRIGHTNESS};
    private static final int DEFAULT_MONITOR_APP_CACHE_SIZE = 30;
    private static final int DEFAUTL_MIN_MINUTES_3 = 180000;
    private static final int EVENT_ID_APP_LEVEL = 770;
    private static final String KEY_APP_LEVELS_COUNT = "app_levels_count";
    private static final String KEY_APP_LEVELS_MAX = "app_levels_max";
    private static final String KEY_APP_LEVEL_TIME = "app_levels_time";
    private static final String KEY_APP_NAME = "pkgName";
    private static final String KEY_APP_TOTAL_TIME = "app_total_time";
    private static final int MSG_APP_ENTER = 2001;
    private static final int MSG_APP_EXIT = 2002;
    private static final int MSG_SCREEN_OFF = 2003;
    private static final int MSG_SCREEN_ON = 2004;
    private static final int MSG_UPDATE_APP_LEVEL_TIME = 2000;
    private static final String TAG = "AppBrightnessStat";
    private static volatile AppBrightnessStat sAppBrightnessStat;
    private AppBackLightInfo[] mAppInfoAarry = new AppBackLightInfo[2];
    private ArrayList<AppBackLightInfo> mApps = new ArrayList<>(30);
    private BackLightStat mBackLightStat;
    private boolean mBootCompleted = false;
    private boolean mByUser = false;
    private Context mContext;
    private String mCurrPkg = null;
    private int mCurrTarget = -1;
    private AppBackLightHandler mHandler;
    private String mLastPkg = null;
    private int mLastTarget = -1;
    private ArrayList<Integer> mLevels = new ArrayList<>(20);
    private final Object mLock = new Object();
    private int mPowerState = 2;
    private boolean mSupportAppStat = false;
    private int mUpdateTarget = 0;
    private String mVersion = null;

    private AppBrightnessStat(Context context, BackLightStat stat) {
        this.mContext = context;
        this.mBackLightStat = stat;
    }

    public static AppBrightnessStat getInstance(Context context, BackLightStat stat) {
        if (sAppBrightnessStat == null) {
            synchronized (AppBrightnessStat.class) {
                if (sAppBrightnessStat == null) {
                    sAppBrightnessStat = new AppBrightnessStat(context, stat);
                }
            }
        }
        return sAppBrightnessStat;
    }

    public void init(Handler handler) {
        ColorAppSwitchManager.getInstance().registerAppSwitchObserver(this.mContext, new AppSwitchObserver(), (ColorAppSwitchConfig) null);
        this.mHandler = new AppBackLightHandler(handler.getLooper());
        this.mLevels.clear();
        this.mApps.clear();
        loadConfig();
        if (this.mLevels.size() == 0) {
            int i = 0;
            while (true) {
                int[] iArr = DEFAULT_LEVELS_MONITOR;
                if (i >= iArr.length) {
                    break;
                }
                this.mLevels.add(Integer.valueOf(iArr[i]));
                i++;
            }
        }
        addDefaultApp();
        this.mBootCompleted = true;
    }

    private void addDefaultApp() {
        AppBackLightInfo defaultApp = new AppBackLightInfo(DEFAULT_LAUNCHER_APP, false, 0, 0, this.mLevels);
        defaultApp.inPkg = true;
        defaultApp.startTime = 0;
        defaultApp.totalTime = 0;
        this.mCurrPkg = DEFAULT_LAUNCHER_APP;
        this.mApps.add(defaultApp);
        Slog.d(TAG, "addDefaultApp:" + this.mApps);
    }

    public void loadConfig() {
        this.mSupportAppStat = this.mBackLightStat.getBackLightStatSupport();
        ArrayList<Integer> levels = this.mBackLightStat.getBackLightStatAppLevels();
        if (levels != null) {
            for (int i = 0; i < levels.size(); i++) {
                this.mLevels.add(levels.get(i));
            }
        }
        this.mVersion = this.mBackLightStat.getVersion();
    }

    private void uploadData(String reason) {
        int maxLev;
        long now = this.mBackLightStat.uptimeMillis();
        handleLevelChanged();
        int appSize = this.mApps.size();
        int i = 0;
        while (i < appSize) {
            StringBuilder sb = new StringBuilder((int) CompatibilityHelper.FORCE_DELAY_TO_USE_POST);
            AppBackLightInfo appInfo = this.mApps.get(i);
            String pkg = appInfo.pkgName;
            long totalTime = appInfo.totalTime;
            ArrayList<AppBackLightInfo.LevelStatInfo> levels = appInfo.levelInfos;
            int size = levels.size();
            if (size > 0) {
                maxLev = levels.get(size - 1).key;
            } else {
                maxLev = 0;
            }
            sb.append(KEY_APP_NAME);
            sb.append(",");
            sb.append(pkg);
            sb.append(",");
            sb.append(KEY_APP_LEVELS_COUNT);
            sb.append(",");
            sb.append(size);
            sb.append(",");
            if (maxLev > 0) {
                sb.append(KEY_APP_LEVELS_MAX);
                sb.append(",");
                sb.append(maxLev);
                sb.append(",");
            }
            sb.append(KEY_APP_TOTAL_TIME);
            sb.append(",");
            sb.append(totalTime);
            sb.append(",");
            int j = 0;
            while (j < size) {
                AppBackLightInfo.LevelStatInfo value = levels.get(j);
                sb.append("key_" + value.key);
                sb.append(",");
                sb.append(value.totalTime);
                sb.append(",");
                j++;
                pkg = pkg;
                maxLev = maxLev;
                now = now;
            }
            String manu = this.mBackLightStat.getLcdManufacture();
            String time = this.mBackLightStat.getCurrSimpleFormatTime();
            sb.append(BackLightStat.KEY_LCD_MANU);
            sb.append(",");
            sb.append(manu);
            sb.append(",");
            sb.append(BackLightStat.KEY_VERSION);
            sb.append(",");
            sb.append(this.mVersion);
            sb.append(",");
            sb.append(BackLightStat.KEY_UPLOAD_TIME);
            sb.append(",");
            sb.append(time);
            sb.append(",");
            sb.append(BackLightStat.KEY_UPLOAD_REASON);
            sb.append(",");
            sb.append(reason);
            String uploadData = sb.toString();
            if (totalTime >= 180000) {
                this.mBackLightStat.reportBackLightInfor(770, uploadData);
            }
            if (DEBUG) {
                Slog.d(TAG, "uploadData size=" + uploadData.length() + StringUtils.SPACE + uploadData);
            }
            appInfo.inPkg = false;
            appInfo.totalTime = 0;
            appInfo.startTime = 0;
            int j2 = 0;
            while (j2 < size) {
                AppBackLightInfo.LevelStatInfo levInfo = levels.get(j2);
                levInfo.inRegion = false;
                levInfo.totalTime = 0;
                levInfo.startTime = 0;
                j2++;
                sb = sb;
            }
            i++;
            appSize = appSize;
            now = now;
        }
        AppBackLightInfo appInfo2 = findCurrAppInfo(this.mCurrPkg);
        if (appInfo2 != null) {
            appInfo2.inPkg = true;
            appInfo2.startTime = now;
            appInfo2.totalTime = 0;
            AppBackLightInfo.LevelStatInfo levInfo2 = findCurrLevInfo(appInfo2.levelInfos);
            if (levInfo2 != null) {
                levInfo2.inRegion = true;
                levInfo2.startTime = now;
                levInfo2.totalTime = 0;
                return;
            }
            Slog.e(TAG, "upload currPkg=" + this.mCurrPkg);
            return;
        }
        Slog.e(TAG, "upload currPkg=" + this.mCurrPkg + StringUtils.SPACE + appInfo2);
    }

    private void uploadDataApp(AppBackLightInfo info) {
        if (info != null) {
            String pkg = info.pkgName;
            ArrayList<AppBackLightInfo.LevelStatInfo> levels = info.levelInfos;
            StringBuilder sb = new StringBuilder((int) CompatibilityHelper.FORCE_DELAY_TO_USE_POST);
            sb.append(KEY_APP_NAME);
            sb.append(",");
            sb.append(pkg);
            sb.append(",");
            int size = levels.size();
            sb.append(KEY_APP_LEVELS_COUNT);
            sb.append(",");
            sb.append(size);
            sb.append(",");
            if (size > 0) {
                sb.append(KEY_APP_LEVELS_MAX);
                sb.append(",");
                sb.append(levels.get(size - 1).key);
                sb.append(",");
            }
            sb.append(KEY_APP_TOTAL_TIME);
            sb.append(",");
            sb.append(info.totalTime);
            sb.append(",");
            for (int i = 0; i < levels.size(); i++) {
                AppBackLightInfo.LevelStatInfo value = levels.get(i);
                sb.append("key_" + value.key);
                sb.append(",");
                sb.append(value.totalTime);
                sb.append(",");
            }
            String manu = this.mBackLightStat.getLcdManufacture();
            String time = this.mBackLightStat.getCurrSimpleFormatTime();
            sb.append(BackLightStat.KEY_LCD_MANU);
            sb.append(",");
            sb.append(manu);
            sb.append(",");
            sb.append(BackLightStat.KEY_VERSION);
            sb.append(",");
            sb.append(this.mVersion);
            sb.append(",");
            sb.append(BackLightStat.KEY_UPLOAD_TIME);
            sb.append(",");
            sb.append(time);
            sb.append(",");
            sb.append(BackLightStat.KEY_UPLOAD_REASON);
            sb.append(",");
            sb.append(BackLightStat.VALUE_UPLOAD_CACHE_FULL);
            String uploadData = sb.toString();
            this.mBackLightStat.reportBackLightInfor(770, uploadData);
            Slog.d(TAG, "uploadData reason=cache_full " + uploadData);
        }
    }

    @Override // com.android.server.display.stat.BackLightStat.Callback
    public void onReceive(String action, Object... values) {
        if (!TextUtils.isEmpty(action) && this.mSupportAppStat) {
            if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                uploadData(BackLightStat.VALUE_UPLOAD_SHUT_DOWN);
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                this.mPowerState = 2;
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                this.mPowerState = 1;
            } else if ("android.intent.action.REBOOT".equals(action)) {
                uploadData(BackLightStat.VALUE_UPLOAD_REBOOT);
            } else if (BackLightStat.ACTION_ON_ALARM.equals(action)) {
                uploadData(BackLightStat.VALUE_UPLOAD_ON_ALARM);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class AppBackLightInfo {
        boolean inPkg = false;
        final int levSize;
        final ArrayList<LevelStatInfo> levelInfos = new ArrayList<>(20);
        final ArrayList<Integer> levels;
        final String pkgName;
        long startTime = 0;
        long totalTime = 0;

        /* access modifiers changed from: package-private */
        public class LevelStatInfo {
            boolean inRegion = false;
            final int key;
            long startTime = 0;
            long totalTime = 0;

            public LevelStatInfo(int key2, boolean inRegion2, long totalTime2, long startTime2) {
                this.key = key2;
                this.inRegion = inRegion2;
                this.totalTime = totalTime2;
                this.startTime = startTime2;
            }

            public String toString() {
                return "LevInfo{key=" + this.key + ", inRegion=" + this.inRegion + ", totalTime=" + this.totalTime + ", startTime=" + this.startTime + '}';
            }
        }

        public AppBackLightInfo(String pkgName2) {
            this.pkgName = pkgName2;
            this.levSize = 0;
            this.levels = null;
        }

        public AppBackLightInfo(String pkgName2, boolean inPkg2, long totalTime2, long startTime2, ArrayList<Integer> levels2) {
            this.pkgName = pkgName2;
            this.inPkg = inPkg2;
            this.totalTime = totalTime2;
            this.startTime = startTime2;
            this.levels = levels2;
            this.levSize = levels2.size();
            this.levelInfos.clear();
            for (int i = 0; i < this.levSize; i++) {
                this.levelInfos.add(new LevelStatInfo(levels2.get(i).intValue(), false, 0, 0));
            }
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return Objects.equals(this.pkgName, ((AppBackLightInfo) o).pkgName);
        }

        private String toStringMap(HashMap<Integer, LevelStatInfo> map) {
            StringBuilder sb = new StringBuilder(100);
            for (Map.Entry<Integer, LevelStatInfo> entry : map.entrySet()) {
                int level = entry.getKey().intValue();
                sb.append("(" + level + ",");
                LevelStatInfo value = entry.getValue();
                sb.append(value.totalTime + "~" + value.startTime + ") ");
            }
            return sb.toString();
        }

        private String toStringArrayList(ArrayList<LevelStatInfo> list) {
            StringBuilder sb = new StringBuilder(100);
            for (int i = 0; i < list.size(); i++) {
                LevelStatInfo info = list.get(i);
                sb.append("(" + info.key + "," + info.inRegion + ",");
                StringBuilder sb2 = new StringBuilder();
                sb2.append(info.totalTime);
                sb2.append("~");
                sb2.append(info.startTime);
                sb2.append(") ");
                sb.append(sb2.toString());
            }
            return sb.toString();
        }

        public String toString() {
            return "AppBackLightInfo{pkgName=" + this.pkgName + ", inPkg=" + this.inPkg + ", totalTime=" + this.totalTime + ", startTime=" + this.startTime + ", levels=" + toStringArrayList(this.levelInfos) + '}';
        }
    }

    /* access modifiers changed from: private */
    public final class AppBackLightHandler extends Handler {
        public AppBackLightHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2000:
                    AppBrightnessStat.this.handleLevelChanged();
                    return;
                case AppBrightnessStat.MSG_APP_ENTER /* 2001 */:
                    AppBrightnessStat.this.handleAppEnter();
                    return;
                case AppBrightnessStat.MSG_APP_EXIT /* 2002 */:
                default:
                    return;
                case AppBrightnessStat.MSG_SCREEN_OFF /* 2003 */:
                    AppBrightnessStat.this.handleScreenOff();
                    return;
                case AppBrightnessStat.MSG_SCREEN_ON /* 2004 */:
                    AppBrightnessStat.this.handleScreenON();
                    return;
            }
        }
    }

    private void updateAppLevelTime() {
        for (int i = 0; i < this.mApps.size(); i++) {
            AppBackLightInfo updateApp = this.mApps.get(i);
            String updatePkg = updateApp.pkgName;
            ArrayList<AppBackLightInfo.LevelStatInfo> levels = updateApp.levelInfos;
            long now = SystemClock.elapsedRealtime();
            if (!TextUtils.isEmpty(updatePkg) && updatePkg.equals(this.mCurrPkg)) {
                if (updateApp.startTime > 0) {
                    updateApp.totalTime += now - updateApp.startTime;
                    updateApp.startTime = now;
                }
                int j = 0;
                while (true) {
                    if (j >= levels.size()) {
                        break;
                    }
                    AppBackLightInfo.LevelStatInfo info = levels.get(j);
                    if (!isValidBrightness(this.mLastTarget) || this.mLastTarget > info.key) {
                        j++;
                    } else if (info.startTime > 0) {
                        info.totalTime += now - info.startTime;
                        info.startTime = 0;
                    }
                }
                int j2 = 0;
                while (true) {
                    if (j2 >= levels.size()) {
                        break;
                    }
                    AppBackLightInfo.LevelStatInfo info2 = levels.get(j2);
                    if (this.mCurrTarget <= info2.key) {
                        info2.startTime = now;
                        break;
                    }
                    j2++;
                }
            }
        }
    }

    private void findCurrAppAndLastAppInfo(String lastPkg, String currPkg) {
        int size = this.mApps.size();
        if (this.mApps.contains(new AppBackLightInfo(lastPkg))) {
            this.mAppInfoAarry[0] = null;
            int i = 0;
            while (true) {
                if (i >= size) {
                    break;
                }
                AppBackLightInfo info = this.mApps.get(i);
                if (info.pkgName.equals(lastPkg) && info.inPkg) {
                    this.mAppInfoAarry[0] = info;
                    break;
                }
                i++;
            }
        } else {
            this.mAppInfoAarry[0] = null;
        }
        if (this.mApps.contains(new AppBackLightInfo(currPkg))) {
            this.mAppInfoAarry[1] = null;
            for (int i2 = 0; i2 < size; i2++) {
                AppBackLightInfo info2 = this.mApps.get(i2);
                if (info2.pkgName.equals(currPkg) && !info2.inPkg) {
                    this.mAppInfoAarry[1] = info2;
                    return;
                }
            }
            return;
        }
        this.mAppInfoAarry[1] = null;
    }

    private void calcLastAppTimes(AppBackLightInfo lastInfo, long now) {
        if (lastInfo != null) {
            ArrayList<AppBackLightInfo.LevelStatInfo> levs = lastInfo.levelInfos;
            int size = levs.size();
            if (lastInfo.inPkg && lastInfo.startTime != 0) {
                lastInfo.totalTime += now - lastInfo.startTime;
                lastInfo.startTime = 0;
                lastInfo.inPkg = false;
                for (int i = 0; i < size; i++) {
                    AppBackLightInfo.LevelStatInfo info = levs.get(i);
                    if (info.inRegion && info.startTime != 0) {
                        info.totalTime += now - info.startTime;
                        info.startTime = 0;
                        info.inRegion = false;
                        return;
                    }
                }
            }
        }
    }

    private AppBackLightInfo.LevelStatInfo findCurrLevInfo(ArrayList<AppBackLightInfo.LevelStatInfo> levs) {
        int size = levs.size();
        for (int i = 0; i < size; i++) {
            AppBackLightInfo.LevelStatInfo first = levs.get(0);
            AppBackLightInfo.LevelStatInfo end = levs.get(size - 1);
            if (first.key >= this.mCurrTarget) {
                return first;
            }
            if (i < size - 1) {
                AppBackLightInfo.LevelStatInfo maxInfo = levs.get(i + 1);
                int min = levs.get(i).key;
                int max = maxInfo.key;
                int i2 = this.mCurrTarget;
                if (i2 > min && i2 <= max) {
                    return maxInfo;
                }
            }
            if (this.mCurrTarget >= end.key) {
                return end;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAppEnter() {
        AppBackLightInfo newApp = new AppBackLightInfo(this.mCurrPkg, false, 0, 0, this.mLevels);
        long now = this.mBackLightStat.uptimeMillis();
        findCurrAppAndLastAppInfo(this.mLastPkg, this.mCurrPkg);
        AppBackLightInfo[] appBackLightInfoArr = this.mAppInfoAarry;
        AppBackLightInfo lastInfo = appBackLightInfoArr[0];
        AppBackLightInfo currInfo = appBackLightInfoArr[1];
        if (DEBUG) {
            Slog.d(TAG, "lastInfo:" + lastInfo + "  currInfo:" + currInfo);
        }
        if (lastInfo != null) {
            calcLastAppTimes(lastInfo, now);
            if (currInfo != null) {
                currInfo.inPkg = true;
                currInfo.startTime = now;
                AppBackLightInfo.LevelStatInfo currLevInfo = findCurrLevInfo(currInfo.levelInfos);
                if (currLevInfo != null) {
                    currLevInfo.inRegion = true;
                    currLevInfo.startTime = now;
                    return;
                }
                return;
            }
            if (this.mApps.size() >= 30) {
                AppBackLightInfo removeApp = null;
                long totalTime = this.mApps.get(0).totalTime;
                for (int i = 1; i < this.mApps.size(); i++) {
                    AppBackLightInfo app = this.mApps.get(i);
                    if (totalTime >= app.totalTime) {
                        removeApp = app;
                    }
                    totalTime = app.totalTime;
                }
                if (removeApp != null) {
                    if (removeApp.totalTime >= 180000) {
                        uploadDataApp(removeApp);
                    }
                    this.mApps.remove(removeApp);
                } else {
                    this.mApps.remove(0);
                }
            }
            if (TextUtils.isEmpty(newApp.pkgName) || newApp.levSize <= 0) {
                Slog.e(TAG, "newApp=" + newApp.pkgName + " size=" + newApp.levSize);
                return;
            }
            newApp.inPkg = true;
            newApp.startTime = now;
            newApp.totalTime = 0;
            AppBackLightInfo.LevelStatInfo currLevInfo2 = findCurrLevInfo(newApp.levelInfos);
            if (currLevInfo2 != null) {
                currLevInfo2.inRegion = true;
                currLevInfo2.startTime = now;
                currLevInfo2.totalTime = 0;
            }
            this.mApps.add(newApp);
            return;
        }
        Slog.e(TAG, "lastPkg=" + this.mLastPkg + " currPkg=" + this.mCurrPkg);
    }

    private AppBackLightInfo findCurrAppInfo(String currPkg) {
        int size = this.mApps.size();
        if (!this.mApps.contains(new AppBackLightInfo(currPkg))) {
            return null;
        }
        for (int i = 0; i < size; i++) {
            AppBackLightInfo info = this.mApps.get(i);
            if (info.pkgName.equals(currPkg)) {
                return info;
            }
        }
        return null;
    }

    private AppBackLightInfo.LevelStatInfo findLastLevInfo(ArrayList<AppBackLightInfo.LevelStatInfo> levs) {
        int size = levs.size();
        for (int i = 0; i < size; i++) {
            AppBackLightInfo.LevelStatInfo levInfo = levs.get(i);
            if (levInfo.inRegion) {
                return levInfo;
            }
        }
        return null;
    }

    private void calcCurrAppTimes(AppBackLightInfo currInfo, long now) {
        ArrayList<AppBackLightInfo.LevelStatInfo> levInfos = currInfo.levelInfos;
        levInfos.size();
        if (currInfo.inPkg) {
            if (currInfo.startTime != 0) {
                currInfo.totalTime += now - currInfo.startTime;
            }
            currInfo.startTime = now;
        }
        AppBackLightInfo.LevelStatInfo lastLevInfo = findLastLevInfo(levInfos);
        if (lastLevInfo == null || lastLevInfo.startTime == 0) {
            Slog.e(TAG, "find lastLevInfo null");
        } else {
            lastLevInfo.totalTime += now - lastLevInfo.startTime;
            lastLevInfo.inRegion = false;
            lastLevInfo.startTime = 0;
        }
        AppBackLightInfo.LevelStatInfo currLevInfo = findCurrLevInfo(levInfos);
        if (currLevInfo != null) {
            currLevInfo.inRegion = true;
            currLevInfo.startTime = now;
        } else {
            Slog.e(TAG, "calc currPkg=" + this.mCurrPkg + "  brightness changed:" + this.mLastTarget + "->" + this.mUpdateTarget);
        }
        if (DEBUG) {
            Slog.d(TAG, "last:" + lastLevInfo + "  curr:" + currLevInfo);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLevelChanged() {
        long now = this.mBackLightStat.uptimeMillis();
        AppBackLightInfo currInfo = findCurrAppInfo(this.mCurrPkg);
        if (currInfo != null) {
            calcCurrAppTimes(currInfo, now);
            return;
        }
        Slog.e(TAG, "currPkg=" + this.mCurrPkg + "  brightness changed:" + this.mLastTarget + "->" + this.mUpdateTarget);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOff() {
        for (int i = 0; i < this.mApps.size(); i++) {
            AppBackLightInfo offApp = this.mApps.get(i);
            String offPkg = offApp.pkgName;
            ArrayList<AppBackLightInfo.LevelStatInfo> levels = offApp.levelInfos;
            long now = SystemClock.elapsedRealtime();
            if (!TextUtils.isEmpty(offPkg)) {
                if (offPkg.equals(this.mCurrPkg) && offApp.startTime > 0) {
                    offApp.totalTime += now - offApp.startTime;
                }
                offApp.startTime = 0;
                boolean find = false;
                for (int j = 0; j < levels.size(); j++) {
                    AppBackLightInfo.LevelStatInfo value = levels.get(j);
                    if (offPkg.equals(this.mCurrPkg) && !find && isValidBrightness(this.mCurrTarget) && this.mCurrTarget <= value.key) {
                        find = true;
                        if (value.startTime > 0) {
                            value.totalTime += now - value.startTime;
                        }
                    }
                    value.startTime = 0;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenON() {
        for (int i = 0; i < this.mApps.size(); i++) {
            AppBackLightInfo onApp = this.mApps.get(i);
            String onPkg = onApp.pkgName;
            ArrayList<AppBackLightInfo.LevelStatInfo> levels = onApp.levelInfos;
            long now = SystemClock.elapsedRealtime();
            if (!TextUtils.isEmpty(onPkg)) {
                if (onPkg.equals(this.mCurrPkg)) {
                    onApp.startTime = now;
                } else {
                    onApp.startTime = 0;
                }
                boolean find = false;
                for (int j = 0; j < levels.size(); j++) {
                    AppBackLightInfo.LevelStatInfo value = levels.get(j);
                    if (!onPkg.equals(this.mCurrPkg) || find) {
                        value.startTime = 0;
                    } else if (isValidBrightness(this.mCurrTarget) && this.mCurrTarget <= value.key) {
                        find = true;
                        value.startTime = now;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class AppSwitchObserver implements ColorAppSwitchManager.OnAppSwitchObserver {
        private AppSwitchObserver() {
        }

        public void onAppEnter(ColorAppEnterInfo info) {
            AppBrightnessStat.this.mCurrPkg = info.targetName;
            if (AppBrightnessStat.this.mSupportAppStat) {
                AppBrightnessStat.this.sendAppEnter();
            }
        }

        public void onAppExit(ColorAppExitInfo info) {
            AppBrightnessStat.this.mLastPkg = info.targetName;
            boolean unused = AppBrightnessStat.this.mSupportAppStat;
        }

        public void onActivityEnter(ColorAppEnterInfo info) {
        }

        public void onActivityExit(ColorAppExitInfo info) {
        }
    }

    public String getCurrentPkg() {
        return this.mCurrPkg;
    }

    public String getLastPkg() {
        return this.mLastPkg;
    }

    private void sendUpdateAppLevelTime() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2000));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAppEnter() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_APP_ENTER));
    }

    private void sendScreenOff() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_SCREEN_OFF));
    }

    private void sendScreenON() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_SCREEN_ON));
    }

    public void setCurrTarget(int state, int currTarget, boolean byUser) {
        int i;
        int i2;
        boolean changed = false;
        boolean lastByUser = this.mByUser;
        if (currTarget != this.mCurrTarget && !byUser) {
            this.mCurrTarget = currTarget;
        }
        this.mPowerState = state;
        this.mByUser = byUser;
        if ((lastByUser && !byUser) || !lastByUser) {
            changed = true;
        }
        if (this.mSupportAppStat && this.mBootCompleted && changed && (i = this.mUpdateTarget) != (i2 = this.mCurrTarget)) {
            this.mLastTarget = i;
            this.mUpdateTarget = i2;
            if (DEBUG) {
                Slog.d(TAG, "setCurrTarget changed " + this.mLastTarget + "->" + this.mUpdateTarget);
            }
            sendUpdateAppLevelTime();
        }
    }

    private boolean isValidBrightness(int value) {
        return value > 0;
    }
}
