package com.android.server.am;

import android.app.AlertDialog;
import android.app.AppGlobals;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.Xml;
import android.view.ColorBaseLayoutParams;
import android.view.WindowManager;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.wm.IColorActivityRecordEx;
import com.color.util.ColorTypeCastingHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class ColorAppCrashClearManager implements IColorAppCrashClearManager {
    private static final long CLEAR_MAP_TIME = 600000;
    public static final String CLEAR_TIME = "c";
    public static final String CRASH_CLEAR_NAME = "p";
    private static final long CRASH_CLEAR_TIMEOUT = 1800000;
    public static final int CRASH_CONUNT = 3;
    public static final String CRASH_COUNT = "n";
    public static final String CRASH_TIMEOUT = "o";
    private static final int DATA_CLEAR_MESSAGE = 1;
    public static final String FEATURE = "f";
    private static final String OPPO_CRASH_CLEAR_CONFIG_PATH = "/data/oppo/coloros/config/crashclear_white_list.xml";
    private static final String OPPO_CRASH_CLEAR_PATH = "/data/oppo/coloros/config";
    private static final String TAG = "OppoCrashClearManager";
    private static ColorAppCrashClearManager mOppoCrashCleanManager = null;
    private boolean isReady = false;
    private FileObserverPolicy mClearConfigFileObserver = null;
    private long mClearTime = CLEAR_MAP_TIME;
    private IColorActivityManagerServiceEx mColorAmsEx;
    private Context mContext;
    private long mCrashClearTimeout = 1800000;
    public int mCrashCount = 3;
    private boolean mFeature = true;
    private ClearHandler mHandler;
    private HandlerThread mHandlerThread;
    private long mLastClearMapTime = -1;
    private final Object mObjectLock = new Object();
    public final HashMap<String, ColorAppStartInfo> mProcessCrashCount = new HashMap<>();
    private List<String> mSkipClearList;
    private List<String> skipCrashApp = Arrays.asList("com.android.dialer", "com.android.mms", "com.android.mms.service", "com.android.contacts", "com.android.providers.telephony", "com.android.providers.contacts");

    private class ClearHandler extends Handler {
        public ClearHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            ProcessRecord app = null;
            if (msg.obj != null) {
                app = (ProcessRecord) msg.obj;
            }
            ColorAppCrashClearManager.this.dataClearAlert(app);
        }
    }

    private ColorAppCrashClearManager() {
        initDir();
        initFileObserver();
        readConfigFile();
    }

    private void initDir() {
        File crashClearFilePath = new File(OPPO_CRASH_CLEAR_PATH);
        File crashClearConfigPath = new File(OPPO_CRASH_CLEAR_CONFIG_PATH);
        try {
            if (!crashClearFilePath.exists()) {
                crashClearFilePath.mkdirs();
            }
            if (!crashClearConfigPath.exists()) {
                crashClearConfigPath.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "init crashClearConfigPath Dir failed!!!");
        }
    }

    public void init(IColorActivityManagerServiceEx amsEx) {
        if (amsEx != null) {
            this.mColorAmsEx = amsEx;
            this.mContext = amsEx.getActivityManagerService().mContext;
        }
        this.isReady = true;
        this.mHandlerThread = new HandlerThread("oppocrashclear");
        this.mHandlerThread.start();
        this.mHandler = new ClearHandler(this.mHandlerThread.getLooper());
    }

    public static final ColorAppCrashClearManager getInstance() {
        if (mOppoCrashCleanManager == null) {
            mOppoCrashCleanManager = new ColorAppCrashClearManager();
        }
        return mOppoCrashCleanManager;
    }

    private void initFileObserver() {
        this.mClearConfigFileObserver = new FileObserverPolicy(OPPO_CRASH_CLEAR_CONFIG_PATH);
        this.mClearConfigFileObserver.startWatching();
    }

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(ColorAppCrashClearManager.OPPO_CRASH_CLEAR_CONFIG_PATH)) {
                Log.i(ColorAppCrashClearManager.TAG, "onEvent: focusPath = OPPO_CRASH_CLEAR_CONFIG_PATH");
                ColorAppCrashClearManager.this.readConfigFile();
            }
        }
    }

    /* access modifiers changed from: private */
    public void readConfigFile() {
        File xmlFile = new File(OPPO_CRASH_CLEAR_CONFIG_PATH);
        if (!xmlFile.exists()) {
            this.mSkipClearList = this.skipCrashApp;
            return;
        }
        FileReader xmlReader = null;
        StringReader strReader = null;
        try {
            this.mSkipClearList = new ArrayList();
            XmlPullParser parser = Xml.newPullParser();
            try {
                FileReader xmlReader2 = new FileReader(xmlFile);
                parser.setInput(xmlReader2);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    if (eventType != 0 && eventType == 2) {
                        if (parser.getName().equals(CRASH_CLEAR_NAME)) {
                            parser.next();
                            updateCrashClearName(parser.getAttributeValue(null, "att"));
                        } else if (parser.getName().equals(CRASH_COUNT)) {
                            parser.next();
                            updateCrashCount(parser.getAttributeValue(null, "att"));
                        } else if (parser.getName().equals(CRASH_TIMEOUT)) {
                            parser.next();
                            updateCrashTimeout(parser.getAttributeValue(null, "att"));
                        } else if (parser.getName().equals(FEATURE)) {
                            parser.next();
                            updateFeature(parser.getAttributeValue(null, "att"));
                        } else if (parser.getName().equals(CLEAR_TIME)) {
                            parser.next();
                            updateClearTime(parser.getAttributeValue(null, "att"));
                        }
                    }
                }
                try {
                    if (this.mSkipClearList.isEmpty()) {
                        this.mSkipClearList = this.skipCrashApp;
                    }
                    xmlReader2.close();
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (IOException e) {
                    Log.w(TAG, "Got execption close permReader.", e);
                }
            } catch (FileNotFoundException e2) {
                Log.w(TAG, "Couldn't find or open alarm_filter_packages file " + xmlFile);
                try {
                    if (this.mSkipClearList.isEmpty()) {
                        this.mSkipClearList = this.skipCrashApp;
                    }
                    if (xmlReader != null) {
                        xmlReader.close();
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (IOException e3) {
                    Log.w(TAG, "Got execption close permReader.", e3);
                }
            }
        } catch (Exception e4) {
            Log.w(TAG, "Got execption parsing permissions.", e4);
            if (this.mSkipClearList.isEmpty()) {
                this.mSkipClearList = this.skipCrashApp;
            }
            if (xmlReader != null) {
                xmlReader.close();
            }
            if (strReader != null) {
                strReader.close();
            }
        } catch (Throwable th) {
            try {
                if (this.mSkipClearList.isEmpty()) {
                    this.mSkipClearList = this.skipCrashApp;
                }
                if (xmlReader != null) {
                    xmlReader.close();
                }
                if (strReader != null) {
                    strReader.close();
                }
            } catch (IOException e5) {
                Log.w(TAG, "Got execption close permReader.", e5);
            }
            throw th;
        }
    }

    private void updateCrashClearName(String tagName) {
        if (tagName != null && tagName != "") {
            this.mSkipClearList.add(tagName);
        }
    }

    private void updateCrashCount(String crashCount) {
        if (crashCount != null) {
            try {
                this.mCrashCount = Integer.parseInt(crashCount);
            } catch (NumberFormatException e) {
                this.mCrashCount = 3;
                Log.e(TAG, "updateCrashCount NumberFormatException: ", e);
            }
        }
    }

    private void updateCrashTimeout(String crashTimeout) {
        if (crashTimeout != null) {
            try {
                this.mCrashClearTimeout = Long.parseLong(crashTimeout);
            } catch (NumberFormatException e) {
                this.mCrashClearTimeout = 1800000;
                Log.e(TAG, "updateCrashTimeout NumberFormatException: ", e);
            }
        }
    }

    private void updateFeature(String feature) {
        if (feature != null) {
            try {
                this.mFeature = Boolean.parseBoolean(feature);
            } catch (Exception e) {
                this.mFeature = true;
                Log.e(TAG, "updateFeature NumberFormatException: ", e);
            }
        }
    }

    private void updateClearTime(String clearTime) {
        if (clearTime != null) {
            try {
                this.mClearTime = Long.parseLong(clearTime);
            } catch (Exception e) {
                this.mClearTime = CLEAR_MAP_TIME;
                Log.e(TAG, "updateClearTime NumberFormatException: ", e);
            }
        }
    }

    public void collectCrashInfo(IColorActivityRecordEx r) {
        ApplicationInfo info;
        if (this.mFeature && this.isReady && r != null && !OppoSysStateManager.getInstance().isScreenOff() && (info = r.getAppliationInfo()) != null && !this.mSkipClearList.contains(info.packageName) && !OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).inRestrictAppList(info.packageName, UserHandle.getUserId(info.uid))) {
            long startTime = SystemClock.elapsedRealtime();
            if (this.mLastClearMapTime < 0) {
                this.mLastClearMapTime = startTime;
            }
            synchronized (this.mObjectLock) {
                if (startTime > this.mLastClearMapTime + this.mCrashClearTimeout) {
                    try {
                        Iterator<String> iterator = this.mProcessCrashCount.keySet().iterator();
                        while (iterator.hasNext()) {
                            if (startTime > this.mClearTime + this.mProcessCrashCount.get(iterator.next()).getFirstStartTime()) {
                                iterator.remove();
                            }
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "collectCrashInfo Exception: " + e);
                    } finally {
                        this.mLastClearMapTime = -1;
                    }
                }
                ColorAppStartInfo crashInfo = this.mProcessCrashCount.get(info.processName);
                if (crashInfo == null) {
                    ColorAppStartInfo crashInfo2 = new ColorAppStartInfo();
                    crashInfo2.setFirstStartTime(startTime);
                    crashInfo2.setProcessName(info.processName);
                    crashInfo2.mLaunchedFromPackage = r.getLaunchedFromPackage();
                    this.mProcessCrashCount.put(info.processName, crashInfo2);
                } else {
                    if (startTime > this.mClearTime + crashInfo.getFirstStartTime()) {
                        crashInfo.setCrashCount(0);
                    }
                    crashInfo.mLaunchedFromPackage = r.getLaunchedFromPackage();
                    crashInfo.setFirstStartTime(startTime);
                }
            }
        }
    }

    public void clearAppUserData(ProcessRecord app) {
        if (this.mFeature && !OppoSysStateManager.getInstance().isScreenOff() && app != null) {
            OppoBaseProcessRecord baseApp = typeCasting(app);
            if (baseApp != null && !baseApp.getIsANR()) {
                long now = SystemClock.elapsedRealtime();
                ColorAppStartInfo crashCountInfo = this.mProcessCrashCount.get(app.info.processName);
                if (crashCountInfo != null && isHomeProcess(crashCountInfo.mLaunchedFromPackage)) {
                    crashCountInfo.setCrashCount(crashCountInfo.getCrashCount() + 1);
                    if (crashCountInfo.getCrashCount() >= this.mCrashCount) {
                        boolean visb = false;
                        if (crashCountInfo.getFirstStartTime() < 0) {
                            visb = true;
                        } else if (now > crashCountInfo.getFirstStartTime() + 60000) {
                            visb = true;
                        }
                        if (!visb) {
                            sendDataClear(app);
                        }
                    }
                    crashCountInfo.setFirstStartTime(now);
                }
            }
            if (baseApp != null) {
                baseApp.setIsANR(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendDataClear(ProcessRecord app) {
        ClearHandler clearHandler = this.mHandler;
        if (clearHandler == null || this.mContext == null) {
            Log.i(TAG, "ClearHandler: mHandler is null!!!");
        } else if (app != null) {
            clearHandler.removeMessages(1);
            Message mg = this.mHandler.obtainMessage(1);
            mg.obj = app;
            this.mHandler.sendMessage(mg);
        }
    }

    /* access modifiers changed from: private */
    public void dataClearAlert(final ProcessRecord app) {
        CharSequence name;
        Context context = this.mContext;
        if (context != null && app != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (app.pkgList.size() != 1 || (name = this.mContext.getPackageManager().getApplicationLabel(app.info)) == null) {
                builder.setTitle(this.mContext.getResources().getString(201590110, app.processName.toString()));
            } else {
                builder.setTitle(this.mContext.getResources().getString(201590110, name.toString(), app.info.processName));
            }
            builder.setPositiveButton(this.mContext.getResources().getString(201590111), new DialogInterface.OnClickListener() {
                /* class com.android.server.am.ColorAppCrashClearManager.AnonymousClass1 */

                public void onClick(DialogInterface dialog, int whichButton) {
                    ColorAppCrashClearManager.this.clearData(app);
                }
            });
            builder.setNegativeButton(201590112, (DialogInterface.OnClickListener) null);
            AlertDialog dataClearAlert = builder.create();
            dataClearAlert.getWindow().setType(2003);
            dataClearAlert.setCancelable(false);
            WindowManager.LayoutParams p = dataClearAlert.getWindow().getAttributes();
            ColorBaseLayoutParams baseLp = typeCasting(p);
            if (baseLp != null) {
                baseLp.ignoreHomeMenuKey = 1;
            }
            p.privateFlags |= 16;
            dataClearAlert.getWindow().setAttributes(p);
            dataClearAlert.show();
        }
    }

    /* access modifiers changed from: private */
    public void clearData(ProcessRecord app) {
        if (app != null && app.info != null && app.info.processName != null) {
            try {
                synchronized (this.mObjectLock) {
                    this.mProcessCrashCount.remove(app.info.processName);
                }
                IPackageManager pm = AppGlobals.getPackageManager();
                Log.i(TAG, "clearApplicationUserData more than 3 times app:" + app);
                pm.clearApplicationUserData(app.info.packageName, (IPackageDataObserver) null, app.userId);
            } catch (Exception e) {
                Log.w(TAG, "Exception has crashed too many " + app, e);
            }
        }
    }

    private List<ResolveInfo> queryHomeResolveInfo() {
        Intent mHomeIntent = new Intent("android.intent.action.MAIN", (Uri) null);
        mHomeIntent.addCategory("android.intent.category.HOME");
        return this.mContext.getPackageManager().queryIntentActivities(mHomeIntent, 270532608);
    }

    private boolean isHomeProcess(String packageName) {
        List<ResolveInfo> mHomeList;
        if (!(packageName == null || this.mContext == null || (mHomeList = queryHomeResolveInfo()) == null)) {
            for (ResolveInfo ri : mHomeList) {
                if (packageName.equals(ri.activityInfo.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void resetStartTime(IColorActivityRecordEx record) {
        ColorAppStartInfo crashCount;
        if (record != null && record.getAppliationInfo() != null && (crashCount = this.mProcessCrashCount.get(record.getAppliationInfo().processName)) != null) {
            crashCount.setFirstStartTime(-1);
        }
    }

    static OppoBaseProcessRecord typeCasting(ProcessRecord pr) {
        if (pr != null) {
            return (OppoBaseProcessRecord) ColorTypeCastingHelper.typeCasting(OppoBaseProcessRecord.class, pr);
        }
        return null;
    }

    static ColorBaseLayoutParams typeCasting(WindowManager.LayoutParams lp) {
        if (lp != null) {
            return (ColorBaseLayoutParams) ColorTypeCastingHelper.typeCasting(ColorBaseLayoutParams.class, lp);
        }
        return null;
    }
}
