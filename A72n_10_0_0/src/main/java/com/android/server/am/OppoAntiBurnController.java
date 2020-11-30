package com.android.server.am;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.IApplicationThread;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.OppoBaseEnvironment;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
import com.android.server.wm.ActivityTaskManagerService;
import com.oppo.OppoRomUpdateHelper;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoAntiBurnController implements IOppoAntiBurnController {
    private static final String DATA_FILE_DIR = "data/system/sys_screen_antiburn_config.xml";
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String FILTER_NAME = "sys_screen_antiburn_config";
    private static final String KEY_APP_FALLBACK_CFG = "fallback";
    private static final String KEY_COMPAT_VERS_CODE = "compatVers";
    private static final String KEY_CONFIG_VER_LIST = "configs";
    private static final String KEY_PKG_NAME = "pkgName";
    private static final String KEY_SPECIAL_ACTION = "specialActions";
    private static final String SYS_FILE_DIR = (OppoBaseEnvironment.getOppoProductDirectory().getAbsolutePath() + "/etc/sys_screen_antiburn_config.xml");
    private static final String TAG = "OppoAntiBurnController";
    private ActivityManagerService mAMS;
    private Context mContext;
    private Handler mHandler;
    private boolean mIsInit = false;
    private long mLastVersion = 0;
    private OppoAntiBurnHelper mOppoAntiBurnHelper = null;
    private OppoAppThreadExtendCallback mOppoAppThreadExtendCallback = null;
    private IPackageManager mPM;
    private final HashMap<String, AppConfig> mParsedAppConfigs = new HashMap<>();

    @Override // com.android.server.am.IOppoAntiBurnController
    public void init(ActivityManagerService ams, ActivityTaskManagerService atms, IPackageManager pm) {
        if (!this.mIsInit) {
            if (DEBUG) {
                Log.d(TAG, "Screen Anti Burn Init");
            }
            this.mContext = ams.mContext;
            this.mPM = pm;
            this.mAMS = ams;
            this.mHandler = new XHandler(this.mAMS.mBgHandler.getLooper());
            FetchConfig();
            this.mIsInit = true;
        }
    }

    @Override // com.android.server.am.IOppoAntiBurnController
    public void setAppThreadExtend(OppoAppThreadExtendCallback callback) {
        this.mOppoAppThreadExtendCallback = callback;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleNotifyChangedApp(AppConfig config, int pkgUid) {
        ActivityManagerService activityManagerService = this.mAMS;
        if (activityManagerService != null && activityManagerService.mBgHandler != null) {
            if (DEBUG) {
                Log.d(TAG, "scheduleNotifyChangedApp, pkgUid:" + pkgUid);
            }
            this.mAMS.mBgHandler.post(new ConfigRunnable(config, pkgUid));
        }
    }

    /* access modifiers changed from: package-private */
    public class ConfigRunnable implements Runnable {
        AppConfig config;
        String targetPkgName;
        int targetPkgUid;

        ConfigRunnable(AppConfig appConfig, int pkgUid) {
            this.targetPkgName = appConfig.getPkgName();
            this.config = appConfig.clone();
            this.targetPkgUid = pkgUid;
        }

        public void run() {
            synchronized (OppoAntiBurnController.this.mAMS) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    synchronized (OppoAntiBurnController.this.mAMS.mPidsSelfLocked) {
                        List<IApplicationThread> apps = OppoAntiBurnController.this.findTargetAppThread(this.targetPkgName, this.targetPkgUid);
                        if (apps.size() < 1) {
                            Log.d(OppoAntiBurnController.TAG, "No Running Process Related To " + this.targetPkgName);
                            return;
                        }
                        for (IApplicationThread app : apps) {
                            try {
                                String json = this.config.flattenToIPCParams(this.targetPkgUid);
                                if (OppoAntiBurnController.this.mOppoAppThreadExtendCallback != null) {
                                    OppoAntiBurnController.this.mOppoAppThreadExtendCallback.dispatchOnlineConfig(app, json);
                                }
                            } catch (Exception e) {
                                Log.w(OppoAntiBurnController.TAG, "Dispatch Config Exception " + e.getMessage() + ", Target = " + this.targetPkgName);
                            }
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    @Override // com.android.server.am.IOppoAntiBurnController
    public void dispatchConfig(IApplicationThread appThread, ApplicationInfo appInfo) {
        if (appThread == null || appInfo == null) {
            if (DEBUG) {
                Log.d(TAG, "Skip dispatch,reason:appThread/appInfo is null");
            }
        } else if (!isTargetApp(appInfo.packageName)) {
            if (DEBUG) {
                Log.d(TAG, "Skip dispatch,reason:Not Target App " + appInfo.packageName);
            }
        } else if (!this.mIsInit) {
            if (DEBUG) {
                Log.d(TAG, "Skip dispatch,reason:Not init");
            }
        } else if (!appInfo.packageName.contains(":")) {
            try {
                String jsonStr = this.mParsedAppConfigs.get(appInfo.packageName).flattenToIPCParams(appInfo.uid);
                if (this.mOppoAppThreadExtendCallback != null) {
                    this.mOppoAppThreadExtendCallback.dispatchOnlineConfig(appThread, jsonStr);
                }
            } catch (Exception e) {
                Log.w(TAG, "Dispatch Config Exception " + e.getMessage() + ", Target = " + appInfo.packageName);
            }
        } else if (DEBUG) {
            Log.d(TAG, "Skip Dispatch To Sub-Process. Name = " + appInfo.packageName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<IApplicationThread> findTargetAppThread(String pkgName, int pkgUid) {
        List<IApplicationThread> appThreads = new ArrayList<>();
        if (this.mAMS == null || !isTargetApp(pkgName)) {
            return appThreads;
        }
        for (int i = 0; i < this.mAMS.mPidsSelfLocked.size(); i++) {
            try {
                ProcessRecord p = this.mAMS.mPidsSelfLocked.valueAt(i);
                if (p.uid == pkgUid) {
                    if (p.thread != null) {
                        if (p.pkgList.containsKey(pkgName)) {
                            if (DEBUG) {
                                Log.d(TAG, "Find Process For " + pkgName);
                            }
                            appThreads.add(p.thread);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        return appThreads;
    }

    @Override // com.android.server.am.IOppoAntiBurnController
    public void notifyPackageChanged(String pkgName, int uid, String changeType) {
        boolean isTargetPkg = isTargetApp(pkgName);
        if (DEBUG) {
            Log.d(TAG, "notify Package Changed { " + pkgName + " } uid = " + uid + ", Type = " + changeType + ", isTargetPkg " + isTargetPkg);
        }
        if (isTargetPkg) {
            Message msg = Message.obtain();
            msg.what = 1002;
            Bundle params = new Bundle();
            params.putString(KEY_PKG_NAME, pkgName);
            params.putString("pkgChangeAction", changeType);
            params.putInt(WatchlistLoggingHandler.WatchlistEventKeys.UID, uid);
            msg.setData(params);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getVersionCode(String pkgName) {
        IPackageManager iPackageManager = this.mPM;
        if (iPackageManager == null) {
            return 0;
        }
        try {
            PackageInfo packageInfo = iPackageManager.getPackageInfo(pkgName, 0, UserHandle.getCallingUserId());
            if (packageInfo == null) {
                return 0;
            }
            return packageInfo.versionCode;
        } catch (Exception e) {
            Log.d(TAG, "App:" + pkgName + " Get Version Code Exception " + e.getMessage());
            return 0;
        }
    }

    private int getPackageUid(String pkgName, int userID) {
        try {
            return AppGlobals.getPackageManager().getPackageUid(pkgName, 0, userID);
        } catch (RemoteException e) {
            return -1;
        }
    }

    private boolean isTargetApp(String pkgName) {
        return this.mParsedAppConfigs.containsKey(pkgName);
    }

    private void FetchConfig() {
        scheduleFetchConfig();
    }

    class XHandler extends Handler {
        static final String KEY_PKG_CHANGE_ACTION = "pkgChangeAction";
        static final String KEY_PKG_NAME = "pkgName";
        static final String KEY_UID = "uid";
        static final int MSG_FETCH_CONFIG = 1000;
        static final int MSG_PACKAGE_CHANGED = 1002;

        XHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i == 1000) {
                OppoAntiBurnController.this.handleFetchConfig();
            } else if (i == 1002) {
                String pkgName = msg.getData().getString(KEY_PKG_NAME);
                String pkgChangeAction = msg.getData().getString(KEY_PKG_CHANGE_ACTION);
                OppoAntiBurnController.this.handlePackageChange(pkgName, msg.getData().getInt("uid"), pkgChangeAction);
            }
        }
    }

    private void scheduleFetchConfig() {
        Handler handler;
        if (this.mContext == null || (handler = this.mHandler) == null) {
            Log.v(TAG, "skip fetch config");
            return;
        }
        handler.removeMessages(1000);
        Handler handler2 = this.mHandler;
        handler2.sendMessage(Message.obtain(handler2, 1000));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFetchConfig() {
        if (DEBUG) {
            Log.d(TAG, "handleFetchConfig");
        }
        Context context = this.mContext;
        if (context != null) {
            this.mOppoAntiBurnHelper = new OppoAntiBurnHelper(context);
            this.mOppoAntiBurnHelper.initUpdateBroadcastReceiver();
        }
    }

    /* access modifiers changed from: package-private */
    public void handlePackageChange(String pkgName, int uid, String changeType) {
        if (DEBUG) {
            Log.d(TAG, "handlePackageChange changeType" + changeType);
        }
        AppConfig appItem = this.mParsedAppConfigs.get(pkgName);
        if (appItem != null && changeType.equals("android.intent.action.PACKAGE_ADDED")) {
            appItem.markMostMatchCfg();
        }
    }

    /* access modifiers changed from: private */
    public class OppoAntiBurnHelper extends OppoRomUpdateHelper {
        static final String TAG = "OppoAntiBurnHelper";
        private static final String TAG_AppConfig = "AppConfig";
        private static final String TAG_VERSION = "version";

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void changeFilePermisson(String filename) {
            File file = new File(filename);
            if (file.exists()) {
                boolean result = file.setReadable(true, false);
                Slog.i(TAG, "setReadable result :" + result);
                return;
            }
            Slog.i(TAG, "filename :" + filename + " is not exist");
        }

        private class OppoResolotionInfo extends OppoRomUpdateHelper.UpdateInfo {
            public OppoResolotionInfo() {
                super(OppoAntiBurnHelper.this);
            }

            public void parseContentFromXML(String content) {
                if (content != null) {
                    OppoAntiBurnHelper.this.changeFilePermisson(OppoAntiBurnController.DATA_FILE_DIR);
                    StringReader strReader = null;
                    FileReader xmlReader = null;
                    OppoAntiBurnController.this.mParsedAppConfigs.clear();
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        StringReader strReader2 = new StringReader(content);
                        parser.setInput(strReader2);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            if (eventType != 0) {
                                if (eventType == 2) {
                                    if (parser.getName().equals(OppoAntiBurnHelper.TAG_AppConfig)) {
                                        parser.next();
                                        String text = parser.getText();
                                        JSONObject json = new JSONObject();
                                        try {
                                            json = new JSONObject(text);
                                        } catch (JSONException e) {
                                        }
                                        AppConfig appItem = new AppConfig();
                                        if (appItem.parseAppConfig(json)) {
                                            OppoAntiBurnController.this.mParsedAppConfigs.put(appItem.getPkgName(), appItem);
                                        }
                                    }
                                }
                            }
                        }
                        if (0 != 0) {
                            try {
                                xmlReader.close();
                            } catch (IOException e2) {
                                Slog.i(OppoAntiBurnHelper.TAG, "Got execption close permReader.", e2);
                            }
                        }
                        strReader2.close();
                        Slog.d(OppoAntiBurnHelper.TAG, "load data end ");
                    } catch (XmlPullParserException e3) {
                        Slog.i(OppoAntiBurnHelper.TAG, "Got execption parsing permissions.", e3);
                        if (0 != 0) {
                            try {
                                xmlReader.close();
                            } catch (IOException e4) {
                                Slog.i(OppoAntiBurnHelper.TAG, "Got execption close permReader.", e4);
                                return;
                            }
                        }
                        if (0 != 0) {
                            strReader.close();
                        }
                    } catch (IOException e5) {
                        Slog.i(OppoAntiBurnHelper.TAG, "Got execption parsing permissions.", e5);
                        if (0 != 0) {
                            try {
                                xmlReader.close();
                            } catch (IOException e6) {
                                Slog.i(OppoAntiBurnHelper.TAG, "Got execption close permReader.", e6);
                                return;
                            }
                        }
                        if (0 != 0) {
                            strReader.close();
                        }
                    } catch (Throwable th) {
                        if (0 != 0) {
                            try {
                                xmlReader.close();
                            } catch (IOException e7) {
                                Slog.i(OppoAntiBurnHelper.TAG, "Got execption close permReader.", e7);
                                throw th;
                            }
                        }
                        if (0 != 0) {
                            strReader.close();
                        }
                        throw th;
                    }
                }
            }
        }

        public OppoAntiBurnHelper(Context context) {
            super(context, OppoAntiBurnController.FILTER_NAME, OppoAntiBurnController.SYS_FILE_DIR, OppoAntiBurnController.DATA_FILE_DIR);
            setUpdateInfo(new OppoResolotionInfo(), new OppoResolotionInfo());
            try {
                init();
                changeFilePermisson(OppoAntiBurnController.DATA_FILE_DIR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void init() {
            if (OppoAntiBurnController.SYS_FILE_DIR != null) {
                File datafile = new File(OppoAntiBurnController.DATA_FILE_DIR);
                File sysfile = new File(OppoAntiBurnController.SYS_FILE_DIR);
                if (!datafile.exists()) {
                    Slog.d(TAG, "datafile not exist try to load from system");
                    if (sysfile.exists()) {
                        parseContentFromXML(readFromFile(sysfile));
                    }
                } else if (sysfile.exists()) {
                    long dataversion = getConfigVersion(OppoAntiBurnController.DATA_FILE_DIR);
                    long sysversion = getConfigVersion(OppoAntiBurnController.SYS_FILE_DIR);
                    Slog.d(TAG, "dataversion:" + dataversion + " sysversion:" + sysversion);
                    if (dataversion >= sysversion) {
                        parseContentFromXML(readFromFile(datafile));
                    } else {
                        parseContentFromXML(readFromFile(sysfile));
                    }
                } else {
                    Slog.d(TAG, "systemfile not exist try to load from data");
                    parseContentFromXML(readFromFile(datafile));
                }
            }
        }

        private long getConfigVersion(String configPath) {
            if (configPath == null) {
                return 0;
            }
            long version = 0;
            try {
                FileReader xmlReader = new FileReader(configPath);
                XmlPullParser parser = Xml.newPullParser();
                try {
                    parser.setInput(xmlReader);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        if (eventType != 0) {
                            if (eventType == 2) {
                                String tagName = parser.getName();
                                Slog.d(TAG, "getConfigVersion called  tagname:" + tagName);
                                if ("version".equals(tagName)) {
                                    parser.next();
                                    try {
                                        version = Long.parseLong(parser.getText());
                                    } catch (NumberFormatException e) {
                                        Slog.e(TAG, "version convert fail");
                                    }
                                    try {
                                        xmlReader.close();
                                    } catch (IOException e2) {
                                        Slog.e(TAG, "" + e2);
                                    }
                                    return version;
                                }
                            }
                        }
                    }
                    try {
                        xmlReader.close();
                    } catch (IOException e3) {
                        Slog.e(TAG, "" + e3);
                    }
                    return 0;
                } catch (XmlPullParserException e4) {
                    Slog.e(TAG, "" + e4);
                    try {
                        xmlReader.close();
                    } catch (IOException e5) {
                        Slog.e(TAG, "" + e5);
                    }
                    return 0;
                } catch (Exception e6) {
                    Slog.e(TAG, "" + e6);
                    try {
                        xmlReader.close();
                    } catch (IOException e7) {
                        Slog.e(TAG, "" + e7);
                    }
                    return 0;
                } catch (Throwable th) {
                    try {
                        xmlReader.close();
                    } catch (IOException e8) {
                        Slog.e(TAG, "" + e8);
                    }
                    throw th;
                }
            } catch (Exception e9) {
                return 0;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class AppConfig {
        private final List<AppConfigVersionItem> allVersionConfigs = new ArrayList();
        private int appFallBackItemIndex = -1;
        private final Object mAccessLock = new Object();
        private int mostMatchItemIndex = -1;
        private String pkgKey;
        private HashMap<Integer, UIDRecord> uidRecordMap = new HashMap<>();

        AppConfig() {
        }

        /* access modifiers changed from: private */
        public class UIDRecord {
            boolean changed;
            AppConfigVersionItem refinedOutputCfg;

            private UIDRecord() {
                this.changed = false;
            }
        }

        /* access modifiers changed from: package-private */
        public String getPkgName() {
            return this.pkgKey;
        }

        /* access modifiers changed from: package-private */
        public void setPkgName(String pkgName) {
            this.pkgKey = pkgName;
        }

        private UIDRecord getUIDRecord(int uid) {
            UIDRecord curUserRecord = this.uidRecordMap.get(Integer.valueOf(uid));
            if (curUserRecord != null) {
                return curUserRecord;
            }
            UIDRecord curUserRecord2 = new UIDRecord();
            this.uidRecordMap.put(Integer.valueOf(uid), curUserRecord2);
            return curUserRecord2;
        }

        /* access modifiers changed from: package-private */
        public List<Integer> getInfectedUids() {
            ArrayList arrayList;
            synchronized (this.mAccessLock) {
                arrayList = new ArrayList(this.uidRecordMap.keySet());
            }
            return arrayList;
        }

        /* access modifiers changed from: package-private */
        public final AppConfigVersionItem getFinalEffectiveConfig(int uid) {
            synchronized (this.mAccessLock) {
                UIDRecord curUIDRecord = getUIDRecord(uid);
                if (curUIDRecord.refinedOutputCfg == null || curUIDRecord.changed) {
                    curUIDRecord.refinedOutputCfg = getMostMatchRawCfg().cloneCfg();
                    curUIDRecord.changed = false;
                    return curUIDRecord.refinedOutputCfg;
                }
                return curUIDRecord.refinedOutputCfg;
            }
        }

        private AppConfigVersionItem getMostMatchRawCfg() {
            synchronized (this.mAccessLock) {
                if (this.mostMatchItemIndex != -1) {
                    return this.allVersionConfigs.get(this.mostMatchItemIndex);
                }
                return this.allVersionConfigs.get(this.appFallBackItemIndex);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean parseAppConfig(JSONObject appJsonObj) {
            if (appJsonObj == null) {
                return false;
            }
            try {
                synchronized (this.mAccessLock) {
                    if (this.allVersionConfigs.size() > 0) {
                        this.allVersionConfigs.clear();
                        this.appFallBackItemIndex = -1;
                        this.mostMatchItemIndex = -1;
                    }
                    this.pkgKey = appJsonObj.optString(OppoAntiBurnController.KEY_PKG_NAME);
                    getUIDRecord(AppGlobals.getPackageManager().getPackageUid(this.pkgKey, 0, ActivityManager.getCurrentUser()));
                    JSONArray versionsConfig = appJsonObj.optJSONArray(OppoAntiBurnController.KEY_CONFIG_VER_LIST);
                    if (versionsConfig != null && versionsConfig.length() > 0) {
                        for (int idx = 0; idx < versionsConfig.length(); idx++) {
                            AppConfigVersionItem versionItem = new AppConfigVersionItem();
                            if (versionItem.parse(versionsConfig.optJSONObject(idx))) {
                                this.allVersionConfigs.add(versionItem);
                            } else {
                                Log.d(OppoAntiBurnController.TAG, "Parse Verison Item Fail For " + this.pkgKey);
                            }
                        }
                    }
                    if (OppoAntiBurnController.DEBUG) {
                        Log.d(OppoAntiBurnController.TAG, "parse app:" + this.pkgKey);
                    }
                    markMostMatchCfg();
                }
                return true;
            } catch (Exception e) {
                Log.e(OppoAntiBurnController.TAG, "parseAppConfig, " + e.getMessage(), new Exception());
                return false;
            }
        }

        private void notifyAffectedApp() {
            List<Integer> infectivePkgUids = getInfectedUids();
            if (OppoAntiBurnController.DEBUG) {
                Log.d(OppoAntiBurnController.TAG, "notifyAffectedApp:" + this.pkgKey);
            }
            for (Integer num : infectivePkgUids) {
                int pkgUid = num.intValue();
                if (OppoAntiBurnController.DEBUG) {
                    Log.d(OppoAntiBurnController.TAG, "notifyAffectedApp, infectivePkgUids:" + pkgUid);
                }
                OppoAntiBurnController.this.scheduleNotifyChangedApp(this, pkgUid);
            }
        }

        /* access modifiers changed from: package-private */
        public void markMostMatchCfg() {
            synchronized (this.mAccessLock) {
                int curVersionCode = OppoAntiBurnController.this.getVersionCode(this.pkgKey);
                int i = 0;
                while (true) {
                    if (i >= this.allVersionConfigs.size()) {
                        break;
                    }
                    AppConfigVersionItem item = this.allVersionConfigs.get(i);
                    if (item.isFallBackConfig) {
                        this.appFallBackItemIndex = i;
                        if (this.mostMatchItemIndex == -1) {
                            this.mostMatchItemIndex = i;
                        }
                    }
                    if (item.compatVers.contains(Integer.valueOf(curVersionCode))) {
                        this.mostMatchItemIndex = i;
                        break;
                    }
                    i++;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public String flattenToIPCParams(int uid) {
            String jSONObject;
            synchronized (this.mAccessLock) {
                JSONObject jsonObject = new JSONObject();
                AppConfigVersionItem finalConfig = getFinalEffectiveConfig(uid);
                try {
                    jsonObject.put(OppoAntiBurnController.KEY_PKG_NAME, this.pkgKey);
                    if (!"".equals(finalConfig.specialAction)) {
                        jsonObject.put(OppoAntiBurnController.KEY_SPECIAL_ACTION, finalConfig.specialAction);
                    }
                    jSONObject = jsonObject.toString();
                } catch (JSONException e) {
                    return null;
                }
            }
            return jSONObject;
        }

        public AppConfig clone() {
            AppConfig item;
            synchronized (this.mAccessLock) {
                item = new AppConfig();
                item.pkgKey = this.pkgKey;
                item.allVersionConfigs.addAll(this.allVersionConfigs);
                item.uidRecordMap.putAll(this.uidRecordMap);
                item.appFallBackItemIndex = this.appFallBackItemIndex;
                item.mostMatchItemIndex = this.mostMatchItemIndex;
            }
            return item;
        }

        /* access modifiers changed from: package-private */
        public List<AppConfigVersionItem> getAllVersionConfigs() {
            List<AppConfigVersionItem> list;
            synchronized (this.mAccessLock) {
                list = this.allVersionConfigs;
            }
            return list;
        }
    }

    /* access modifiers changed from: package-private */
    public class AppConfigVersionItem {
        final List<Integer> compatVers = new ArrayList();
        boolean isFallBackConfig = false;
        String specialAction = "";

        AppConfigVersionItem() {
        }

        /* access modifiers changed from: package-private */
        public boolean parse(JSONObject json) {
            try {
                this.isFallBackConfig = json.optBoolean(OppoAntiBurnController.KEY_APP_FALLBACK_CFG, false);
                JSONArray compatVerJA = json.optJSONArray(OppoAntiBurnController.KEY_COMPAT_VERS_CODE);
                if (compatVerJA != null) {
                    for (int idx = 0; idx < compatVerJA.length(); idx++) {
                        this.compatVers.add(Integer.valueOf(compatVerJA.optInt(idx)));
                    }
                }
                JSONArray specialActionJsonArray = json.optJSONArray(OppoAntiBurnController.KEY_SPECIAL_ACTION);
                if (specialActionJsonArray == null) {
                    return true;
                }
                this.specialAction = specialActionJsonArray.toString();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public AppConfigVersionItem cloneCfg() {
            AppConfigVersionItem clone = new AppConfigVersionItem();
            clone.isFallBackConfig = this.isFallBackConfig;
            clone.compatVers.addAll(this.compatVers);
            clone.specialAction = this.specialAction;
            return clone;
        }
    }
}
