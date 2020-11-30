package com.android.server.wm;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OppoBaseIntent;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.server.am.ColorAppCrashClearManager;
import com.android.server.am.ColorAppStartupMonitorInfo;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorTypeCastingHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class ColorAppStoreTraffic implements IColorAppStoreTraffic {
    private static final String OPPO_JUMP_STORE_TRACKING_DIR_PATH = "/data/oppo/coloros/trafficProtect/";
    private static final String OPPO_JUMP_STORE_TRACKING_FILE_PATH = "/data/oppo/coloros/trafficProtect/sys_traffic_protecter_config_list.xml";
    private static final String OPPO_TENCENT_INTERCEPT_PATH = "/data/oppo/coloros/startup/tenIntercept.xml";
    private static final String SEND_BROADCAST_TO_PKG = "com.daemon.shelper";
    private static final String START_MARKET_ACTION = "oppo.intent.action.OPPO_STARTUP_MARKET";
    private static final String START_MARKET_ACTION_INTENT_KEY = "com.tencent.mm.intent";
    private static final String START_MARKET_ACTION_SWITCH_KEY = "switchValue";
    private static final String TAG = "ColorAppStoreTraffic";
    private static final String TENCENT_MM_PKG = "com.tencent.mm";
    private static final String UPLOAD_TRACKING_MONITOR_FILE = "/data/oppo/coloros/trafficProtect/trackingUpload.txt";
    private static volatile ColorAppStoreTraffic sColorAppStoreTraffic = null;
    private static boolean sDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private ActivityStack mActivityStack;
    private ColorAppStartupManagerHelper mAppStartupHelper = null;
    private Handler mAsyncHandler = null;
    private ActivityTaskManagerService mAtms = null;
    private List<ColorAppStartupMonitorInfo> mCollentTrackingMonitorList = new ArrayList();
    private boolean mDynamicDebug = false;
    private FileObserverPolicy mInterceptTenFileObserver = null;
    private IColorActivityRecordEx mPreRecord;
    final BroadcastReceiver mShelperActionReceiver = new BroadcastReceiver() {
        /* class com.android.server.wm.ColorAppStoreTraffic.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("oppo.intent.action.SHELPER_FINISH_ACTIVITY")) {
                ColorAppStoreTraffic.this.handleForceFinish();
            }
        }
    };
    private String mTencentInterceptCpn = "com.tencent.mm.plugin.webview.ui.tools.WebViewDownloadUI";
    private int mTencentInterceptSwitchValue = 0;
    private List<List<String>> mTrackBlackList = new ArrayList();
    private List<List<String>> mTrackWhiteList = new ArrayList();
    private boolean mTrackingBundleSwitch = false;
    private FileObserverPolicy mTrackingFileObserver = null;
    private boolean mTrackingSwitch = false;

    private ColorAppStoreTraffic() {
    }

    public static final ColorAppStoreTraffic getInstance() {
        if (sColorAppStoreTraffic == null) {
            synchronized (ColorAppStoreTraffic.class) {
                if (sColorAppStoreTraffic == null) {
                    sColorAppStoreTraffic = new ColorAppStoreTraffic();
                }
            }
        }
        return sColorAppStoreTraffic;
    }

    public void init(IColorActivityTaskManagerServiceEx atmsEx) {
        this.mAtms = atmsEx.getActivityTaskManagerService();
        this.mAppStartupHelper = ColorAppStartupManagerHelper.getInstance();
        HandlerThread thread = new HandlerThread("ColorStoreTraffic");
        thread.start();
        this.mAsyncHandler = new Handler(thread.getLooper());
        initFile();
        readTenInterceptFileAsync();
        readJumpStoreTrackFileAsync();
        IntentFilter shelperAction = new IntentFilter();
        shelperAction.addAction("oppo.intent.action.SHELPER_FINISH_ACTIVITY");
        ActivityTaskManagerService activityTaskManagerService = this.mAtms;
        if (activityTaskManagerService != null) {
            activityTaskManagerService.mContext.registerReceiver(this.mShelperActionReceiver, shelperAction, null, this.mAsyncHandler);
        }
    }

    private void initFile() {
        File interceptTenFile = new File(OPPO_TENCENT_INTERCEPT_PATH);
        File jumpStoreTrackDir = new File(OPPO_JUMP_STORE_TRACKING_DIR_PATH);
        File jumpStoreTrackFile = new File(OPPO_JUMP_STORE_TRACKING_FILE_PATH);
        try {
            if (!interceptTenFile.exists()) {
                interceptTenFile.createNewFile();
            }
            if (!jumpStoreTrackDir.exists()) {
                jumpStoreTrackDir.mkdir();
            }
            if (!jumpStoreTrackFile.exists()) {
                jumpStoreTrackFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.mInterceptTenFileObserver = new FileObserverPolicy(OPPO_TENCENT_INTERCEPT_PATH);
        this.mInterceptTenFileObserver.startWatching();
        this.mTrackingFileObserver = new FileObserverPolicy(OPPO_JUMP_STORE_TRACKING_FILE_PATH);
        this.mTrackingFileObserver.startWatching();
    }

    /* access modifiers changed from: private */
    public class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event != 8) {
                return;
            }
            if (ColorAppStoreTraffic.OPPO_TENCENT_INTERCEPT_PATH.equals(this.focusPath)) {
                Log.i(ColorAppStoreTraffic.TAG, "focusPath OPPO_TEN_INTERCEPT_PATH!");
                ColorAppStoreTraffic.this.readTenInterceptFileAsync();
            } else if (ColorAppStoreTraffic.OPPO_JUMP_STORE_TRACKING_FILE_PATH.equals(this.focusPath)) {
                Log.i(ColorAppStoreTraffic.TAG, "focusPath OPPO_JUMP_STORE_TRACKING_FILE_PATH!");
                ColorAppStoreTraffic.this.readJumpStoreTrackFileAsync();
            }
        }
    }

    public boolean interceptForStoreTraffic(Intent intent, String callingPackage, String calledPackageName, String cpnClassName, int userId) {
        if (this.mTencentInterceptSwitchValue <= 0 || this.mAtms == null || callingPackage == null || calledPackageName == null || cpnClassName == null || intent == null || !callingPackage.equals("com.tencent.mm") || !calledPackageName.equals("com.tencent.mm") || !cpnClassName.equals(this.mTencentInterceptCpn)) {
            return false;
        }
        if (this.mDynamicDebug) {
            Bundle bundle = intent.getExtras();
            String string = "Bundle{";
            for (String key : bundle.keySet()) {
                string = string + " " + key + " => " + bundle.get(key) + ";";
            }
            Log.v(TAG, "interceptForStoreTraffic Bundle = " + (string + " }Bundle"));
        }
        ActivityStack foucedStack = ((ActivityTaskManagerService) this.mAtms).mRootActivityContainer.getTopDisplayFocusedStack();
        if (foucedStack == null) {
            return false;
        }
        ArrayList<IColorActivityRecordEx> list = getLRUActivitiesEx(foucedStack);
        if (list.size() <= 0) {
            return false;
        }
        this.mPreRecord = list.get(list.size() - 1);
        IColorActivityRecordEx iColorActivityRecordEx = this.mPreRecord;
        if (iColorActivityRecordEx == null || iColorActivityRecordEx.getIntent() == null) {
            return false;
        }
        if (this.mPreRecord.getIntent().getComponent().flattenToShortString().contains("com.tencent.mm")) {
            this.mActivityStack = foucedStack;
            OppoBaseIntent baseIntent = typeCasting(intent);
            if (baseIntent != null) {
                baseIntent.setOppoUserId(userId);
            }
            this.mAsyncHandler.post(new SendBroadCastToMarket(this.mAtms.mContext, intent, this.mTencentInterceptSwitchValue));
            return true;
        }
        this.mPreRecord = null;
        return false;
    }

    public ArrayList<IColorActivityRecordEx> getLRUActivitiesEx(ActivityStack foucedStack) {
        ArrayList<IColorActivityRecordEx> list = new ArrayList<>();
        try {
            OppoBaseActivityTaskManagerService baseAtms = typeCasting(this.mAtms);
            Iterator<ActivityRecord> it = (baseAtms != null ? baseAtms.mColorAtmsEx.getColorActivityStackInner(foucedStack).getLRUActivities() : null).iterator();
            while (it.hasNext()) {
                OppoBaseActivityRecord baseAr = (OppoBaseActivityRecord) ColorTypeCastingHelper.typeCasting(OppoBaseActivityRecord.class, it.next());
                if (baseAr != null) {
                    list.add(baseAr.mColorArEx);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private class SendBroadCastToMarket implements Runnable {
        Context context;
        Intent intent;
        int interceptSwitchValue;

        public SendBroadCastToMarket(Context context2, Intent intent2, int interceptSwitchValue2) {
            this.context = context2;
            this.intent = intent2;
            this.interceptSwitchValue = interceptSwitchValue2;
        }

        public void run() {
            Intent intent2 = new Intent(ColorAppStoreTraffic.START_MARKET_ACTION);
            intent2.putExtra(ColorAppStoreTraffic.START_MARKET_ACTION_INTENT_KEY, this.intent);
            intent2.putExtra(ColorAppStoreTraffic.START_MARKET_ACTION_SWITCH_KEY, this.interceptSwitchValue);
            intent2.setPackage(ColorAppStoreTraffic.SEND_BROADCAST_TO_PKG);
            intent2.setFlags(32);
            this.context.sendBroadcastAsUser(intent2, UserHandle.ALL);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readTenInterceptFileAsync() {
        this.mAsyncHandler.post(new Runnable() {
            /* class com.android.server.wm.ColorAppStoreTraffic.AnonymousClass1 */

            public void run() {
                ColorAppStoreTraffic.this.readTenInterceptFile();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readTenInterceptFile() {
        StringBuilder sb;
        int type;
        File tenInterceptFile = new File(OPPO_TENCENT_INTERCEPT_PATH);
        if (tenInterceptFile.exists()) {
            FileInputStream stream = null;
            this.mTencentInterceptSwitchValue = 0;
            try {
                FileInputStream stream2 = new FileInputStream(tenInterceptFile);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tag = parser.getName();
                        if ("url".equals(tag)) {
                            String interceptSwitch = parser.getAttributeValue(null, "switch");
                            String intereptType = parser.getAttributeValue(null, "type");
                            if (interceptSwitch != null && intereptType != null && interceptSwitch.equals("1") && intereptType.equals("0")) {
                                this.mTencentInterceptSwitchValue++;
                                if (sDebug) {
                                    Log.i(TAG, "mTencentInterceptSwitchValue " + this.mTencentInterceptSwitchValue);
                                    continue;
                                } else {
                                    continue;
                                }
                            }
                        } else if ("label".equals(tag)) {
                            String interceptSwitch2 = parser.getAttributeValue(null, "switch");
                            String intereptType2 = parser.getAttributeValue(null, "type");
                            if (interceptSwitch2 != null && intereptType2 != null && interceptSwitch2.equals("1") && intereptType2.equals("1")) {
                                this.mTencentInterceptSwitchValue += 2;
                                if (sDebug) {
                                    Log.i(TAG, "mTencentInterceptSwitchValue " + this.mTencentInterceptSwitchValue);
                                    continue;
                                } else {
                                    continue;
                                }
                            }
                        } else if ("cpn".equals(tag)) {
                            String cpn = parser.nextText();
                            if (!cpn.equals("")) {
                                this.mTencentInterceptCpn = cpn;
                                continue;
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                } while (type != 1);
                try {
                    stream2.close();
                    return;
                } catch (IOException e) {
                    e = e;
                    sb = new StringBuilder();
                }
            } catch (Exception e2) {
                Log.e(TAG, "failed parsing ", e2);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e3) {
                        e = e3;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e4) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e4);
                    }
                }
                throw th;
            }
        } else if (sDebug) {
            Log.e(TAG, "read tenInterceptFile failed: file doesn't exist!");
            return;
        } else {
            return;
        }
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Log.e(TAG, sb.toString());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleForceFinish() {
        IColorActivityRecordEx iColorActivityRecordEx;
        ActivityStack activityStack = this.mActivityStack;
        if (!(activityStack == null || (iColorActivityRecordEx = this.mPreRecord) == null)) {
            this.mAppStartupHelper.requestFinishActivityLocked(activityStack, iColorActivityRecordEx.getAppToken(), 0, null, "resume-exception2", true);
            if (this.mDynamicDebug) {
                Log.d(TAG, "handleForceFinish activityRecord " + this.mPreRecord);
            }
        }
        this.mPreRecord = null;
    }

    public void collectJumpStoreTracking(String callingPackage, String calledPackageName, Intent ephemeralIntent, int callingUid, String cpnClassName) {
        ComponentName topCpn;
        if (callingUid > 10000 && getTrackingSwitch() && callingPackage != null && calledPackageName != null && !callingPackage.equals(calledPackageName) && cpnClassName != null && ephemeralIntent != null && !OppoListManager.getInstance().isSystemApp(callingPackage) && (topCpn = getTopComponentName()) != null && topCpn.getPackageName() != null && callingPackage.equals(topCpn.getPackageName())) {
            collectJumpStoreTrackingInternal(callingPackage, topCpn.getClassName(), calledPackageName, cpnClassName, ephemeralIntent);
        }
    }

    private ComponentName getTopComponentName() {
        ActivityRecord top;
        ComponentName topCpn = null;
        ActivityTaskManagerService activityTaskManagerService = this.mAtms;
        if (!(activityTaskManagerService == null || (top = activityTaskManagerService.mRootActivityContainer.getDefaultDisplay().topRunningActivity()) == null)) {
            topCpn = top.mActivityComponent;
        }
        if (this.mDynamicDebug) {
            Log.d(TAG, "getTopPkgName topCpn = " + topCpn);
        }
        return topCpn;
    }

    private void collectJumpStoreTrackingInternal(String callerPkg, String callerCpn, String calleePkg, String calleeCpn, Intent intent) {
        Handler handler = this.mAsyncHandler;
        if (handler != null) {
            handler.post(new CollectJumpStoreTrackingRunnable(callerPkg, callerCpn, calleePkg, calleeCpn, intent));
        }
    }

    /* access modifiers changed from: private */
    public class CollectJumpStoreTrackingRunnable implements Runnable {
        private String calleeCpn;
        private String calleePkg;
        private String callerCpn;
        private String callerPkg;
        private Intent intent;

        public CollectJumpStoreTrackingRunnable(String callerPkg2, String callerCpn2, String calleePkg2, String calleeCpn2, Intent intent2) {
            this.callerPkg = callerPkg2;
            this.callerCpn = callerCpn2;
            this.calleePkg = calleePkg2;
            this.calleeCpn = calleeCpn2;
            this.intent = intent2;
        }

        public void run() {
            if (ColorAppStoreTraffic.this.mDynamicDebug) {
                Log.i(ColorAppStoreTraffic.TAG, "CollectJumpStoreTrackingRunnable callerPkg=" + this.callerPkg + " callerCpn=" + this.callerCpn + " calleePkg=" + this.calleePkg + " calleeCpn=" + this.calleeCpn + " intent=" + this.intent);
            }
            if (ColorAppStoreTraffic.this.isInTrackWhiteList(this.callerPkg, this.callerCpn, this.calleePkg, this.calleeCpn) && !ColorAppStoreTraffic.this.isInTrackBlackList(this.callerPkg, this.callerCpn, this.calleePkg, this.calleeCpn)) {
                String bundleStr = ColorAppCrashClearManager.CRASH_COUNT;
                if (ColorAppStoreTraffic.this.getTrackingBundleSwitch()) {
                    Bundle bundle = this.intent.getExtras();
                    for (String key : bundle.keySet()) {
                        bundleStr = bundleStr + " " + key + " -> " + bundle.get(key) + ";";
                    }
                }
                ColorAppStartupMonitorInfo appInfo = ColorAppStoreTraffic.this.getTrackingMonitorInfo(this.callerPkg);
                if (appInfo == null) {
                    ColorAppStoreTraffic.this.mCollentTrackingMonitorList.add(ColorAppStartupMonitorInfo.buildTrackingMonitor(this.callerPkg, this.callerCpn, this.calleePkg, this.calleeCpn, this.intent.toInsecureString(), bundleStr));
                    return;
                }
                appInfo.increaseTrackingMonitorCount(this.callerPkg, this.callerCpn, this.calleePkg, this.calleeCpn, this.intent.toInsecureString(), bundleStr);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ColorAppStartupMonitorInfo getTrackingMonitorInfo(String callerPkg) {
        for (ColorAppStartupMonitorInfo appInfo : this.mCollentTrackingMonitorList) {
            if (appInfo.getCallerPkgName().equals(callerPkg)) {
                return appInfo;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void saveTrackingMonitorInfo() {
        if (this.mAsyncHandler != null && !this.mCollentTrackingMonitorList.isEmpty()) {
            this.mAsyncHandler.post(new SaveTrackingMonitorInfo());
        }
    }

    private class SaveTrackingMonitorInfo implements Runnable {
        private SaveTrackingMonitorInfo() {
        }

        public void run() {
            List<List<String>> uploadList = new ArrayList<>();
            try {
                int length = ColorAppStoreTraffic.this.mCollentTrackingMonitorList.size();
                for (int i = 0; i < length; i++) {
                    ColorAppStartupMonitorInfo appInfo = (ColorAppStartupMonitorInfo) ColorAppStoreTraffic.this.mCollentTrackingMonitorList.get(i);
                    if (appInfo != null) {
                        uploadList.addAll(appInfo.getTrackingMonitorList());
                    }
                    if (i == length - 1) {
                        appInfo.clearTrackingMonitorList();
                    }
                }
                if (!uploadList.isEmpty()) {
                    ColorAppStoreTraffic.this.mCollentTrackingMonitorList.clear();
                    for (List list : uploadList) {
                        if (list != null) {
                            StringBuilder strBuiler = new StringBuilder(512);
                            int length2 = list.size();
                            for (int i2 = 0; i2 < length2; i2++) {
                                strBuiler.append(list.get(i2));
                                strBuiler.append("\t");
                            }
                            strBuiler.append("\n");
                            ColorAppStoreTraffic.this.saveTrackingFile(strBuiler.toString(), true);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void saveTrackingFile(String content, boolean append) {
        if (content != null) {
            File file = new File(UPLOAD_TRACKING_MONITOR_FILE);
            FileWriter write = null;
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter write2 = new FileWriter(file, append);
                write2.write(content);
                try {
                    write2.close();
                } catch (IOException e) {
                }
            } catch (IOException e2) {
                if (0 != 0) {
                    write.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        write.close();
                    } catch (IOException e3) {
                    }
                }
                throw th;
            }
        }
    }

    private boolean getTrackingSwitch() {
        return this.mTrackingSwitch;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getTrackingBundleSwitch() {
        return this.mTrackingBundleSwitch;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isInTrackWhiteList(String callerPkg, String callerCpn, String calleePkg, String calleeCpn) {
        boolean result = false;
        synchronized (this.mTrackWhiteList) {
            Iterator<List<String>> it = this.mTrackWhiteList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                List<String> list = it.next();
                if (list.size() == 4) {
                    if (!list.get(0).equals("all") || !list.get(2).equals(calleePkg)) {
                        if (!list.get(0).equals(callerPkg) || !list.get(1).equals("all") || !list.get(2).equals(calleePkg)) {
                            if (list.get(0).equals(callerPkg) && list.get(1).equals(callerCpn) && list.get(2).equals(calleePkg) && (list.get(3).equals("all") || list.get(3).equals(calleeCpn))) {
                                result = true;
                            }
                        } else if (list.get(3).equals("all") || list.get(3).equals(calleeCpn)) {
                            result = true;
                        }
                    } else if (list.get(3).equals("all") || list.get(3).equals(calleeCpn)) {
                        result = true;
                    }
                }
            }
            result = true;
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isInTrackBlackList(String callerPkg, String callerCpn, String calleePkg, String calleeCpn) {
        boolean result = false;
        synchronized (this.mTrackBlackList) {
            Iterator<List<String>> it = this.mTrackBlackList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                List<String> list = it.next();
                if (list.size() == 4) {
                    if (!list.get(0).equals("all") || !list.get(2).equals(calleePkg)) {
                        if (!list.get(0).equals(callerPkg) || !list.get(1).equals("all") || !list.get(2).equals(calleePkg)) {
                            if (list.get(0).equals(callerPkg) && list.get(1).equals(callerCpn) && list.get(2).equals(calleePkg) && (list.get(3).equals("all") || list.get(3).equals(calleeCpn))) {
                                result = true;
                            }
                        } else if (list.get(3).equals("all") || list.get(3).equals(calleeCpn)) {
                            result = true;
                        }
                    } else if (list.get(3).equals("all") || list.get(3).equals(calleeCpn)) {
                        result = true;
                    }
                }
            }
            result = true;
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readJumpStoreTrackFileAsync() {
        this.mAsyncHandler.post(new Runnable() {
            /* class com.android.server.wm.ColorAppStoreTraffic.AnonymousClass3 */

            public void run() {
                ColorAppStoreTraffic.this.readJumpStoreTrackFile();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x01c5  */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x01cb A[SYNTHETIC, Splitter:B:103:0x01cb] */
    /* JADX WARNING: Removed duplicated region for block: B:126:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x01b7 A[SYNTHETIC, Splitter:B:94:0x01b7] */
    private void readJumpStoreTrackFile() {
        String trackingSwitch;
        Throwable th;
        File jumpStoreFile;
        String trackingSwitch2;
        String value;
        String bundleSwitch;
        String trackingSwitch3;
        File jumpStoreFile2 = new File(OPPO_JUMP_STORE_TRACKING_FILE_PATH);
        if (jumpStoreFile2.exists()) {
            ArrayList<List<String>> trackWhiteList = new ArrayList<>();
            ArrayList<List<String>> trackBlackList = new ArrayList<>();
            List<String> callerWhiteList = new ArrayList<>();
            List<String> calleeWhiteList = new ArrayList<>();
            List<String> callerBlackList = new ArrayList<>();
            List<String> calleeBlackList = new ArrayList<>();
            String trackingSwitch4 = "false";
            String trackingBundleSwitch = "false";
            boolean isUpdateSuccess = false;
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(jumpStoreFile2);
                XmlPullParser parser = Xml.newPullParser();
                String str = null;
                parser.setInput(stream, null);
                while (true) {
                    try {
                        int type = parser.next();
                        String tag = parser.getName();
                        if (TextUtils.isEmpty(tag)) {
                            jumpStoreFile = jumpStoreFile2;
                            trackingSwitch = trackingSwitch4;
                            trackingSwitch2 = str;
                        } else if (type != 2) {
                            jumpStoreFile = jumpStoreFile2;
                            trackingSwitch = trackingSwitch4;
                            trackingSwitch2 = null;
                        } else if ("trackMonitor".equals(tag)) {
                            jumpStoreFile = jumpStoreFile2;
                            try {
                                value = parser.getAttributeValue(null, "switch");
                                trackingSwitch = trackingSwitch4;
                            } catch (Exception e) {
                                trackingSwitch = trackingSwitch4;
                                if (stream != null) {
                                }
                                if (!isUpdateSuccess) {
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (stream != null) {
                                }
                                throw th;
                            }
                            try {
                                bundleSwitch = parser.getAttributeValue(null, "bundleSwitch");
                                if (!TextUtils.isEmpty(value)) {
                                    trackingSwitch3 = value;
                                } else {
                                    trackingSwitch3 = trackingSwitch;
                                }
                            } catch (Exception e2) {
                                if (stream != null) {
                                }
                                if (!isUpdateSuccess) {
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                if (stream != null) {
                                }
                                throw th;
                            }
                            try {
                                if (!TextUtils.isEmpty(bundleSwitch)) {
                                    trackingBundleSwitch = bundleSwitch;
                                }
                                trackingSwitch = trackingSwitch3;
                                trackingSwitch2 = null;
                            } catch (Exception e3) {
                                trackingSwitch = trackingSwitch3;
                                if (stream != null) {
                                }
                                if (!isUpdateSuccess) {
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                if (stream != null) {
                                }
                                throw th;
                            }
                        } else {
                            jumpStoreFile = jumpStoreFile2;
                            trackingSwitch = trackingSwitch4;
                            if ("trackWhiteCallee".equals(tag)) {
                                calleeWhiteList.clear();
                                String calleePkg = parser.getAttributeValue(null, "pkg");
                                String calleeCpn = parser.getAttributeValue(null, "cpn");
                                if (!TextUtils.isEmpty(calleePkg) && !TextUtils.isEmpty(calleeCpn)) {
                                    calleeWhiteList.add(calleePkg);
                                    calleeWhiteList.add(calleeCpn);
                                }
                                trackingSwitch2 = null;
                            } else if ("trackWhiteCaller".equals(tag)) {
                                callerWhiteList.clear();
                                String callerPkg = parser.getAttributeValue(null, "pkg");
                                String callerCpn = parser.getAttributeValue(null, "cpn");
                                if (!TextUtils.isEmpty(callerPkg) && !TextUtils.isEmpty(callerCpn) && !calleeWhiteList.isEmpty()) {
                                    callerWhiteList.add(callerPkg);
                                    callerWhiteList.add(callerCpn);
                                    callerWhiteList.addAll(calleeWhiteList);
                                    trackWhiteList.add(callerWhiteList);
                                }
                                trackingSwitch2 = null;
                            } else if ("trackBlackCallee".equals(tag)) {
                                calleeBlackList.clear();
                                String calleePkg2 = parser.getAttributeValue(null, "pkg");
                                String calleeCpn2 = parser.getAttributeValue(null, "cpn");
                                if (!TextUtils.isEmpty(calleePkg2) && !TextUtils.isEmpty(calleeCpn2)) {
                                    calleeBlackList.add(calleePkg2);
                                    calleeBlackList.add(calleeCpn2);
                                }
                                trackingSwitch2 = null;
                            } else if ("trackBlackCaller".equals(tag)) {
                                callerBlackList.clear();
                                trackingSwitch2 = null;
                                String callerPkg2 = parser.getAttributeValue(null, "pkg");
                                String callerCpn2 = parser.getAttributeValue(null, "cpn");
                                if (!TextUtils.isEmpty(callerPkg2) && !TextUtils.isEmpty(callerCpn2) && !calleeBlackList.isEmpty()) {
                                    callerBlackList.add(callerPkg2);
                                    callerBlackList.add(callerCpn2);
                                    callerBlackList.addAll(calleeBlackList);
                                    trackBlackList.add(callerBlackList);
                                }
                            } else {
                                trackingSwitch2 = null;
                            }
                        }
                        if (type == 1) {
                            break;
                        }
                        str = trackingSwitch2;
                        jumpStoreFile2 = jumpStoreFile;
                        trackingSwitch4 = trackingSwitch;
                    } catch (Exception e4) {
                        trackingSwitch = trackingSwitch4;
                        if (stream != null) {
                            stream.close();
                        }
                        if (!isUpdateSuccess) {
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e5) {
                            }
                        }
                        throw th;
                    }
                }
                isUpdateSuccess = true;
                try {
                    stream.close();
                } catch (IOException e6) {
                }
            } catch (Exception e7) {
                trackingSwitch = trackingSwitch4;
                if (stream != null) {
                }
                if (!isUpdateSuccess) {
                }
            } catch (Throwable th6) {
                th = th6;
                if (stream != null) {
                }
                throw th;
            }
            if (!isUpdateSuccess) {
                try {
                    this.mTrackingSwitch = Boolean.parseBoolean(trackingSwitch);
                    this.mTrackingBundleSwitch = Boolean.parseBoolean(trackingBundleSwitch);
                    synchronized (this.mTrackWhiteList) {
                        this.mTrackWhiteList.clear();
                        this.mTrackWhiteList.addAll(trackWhiteList);
                    }
                    synchronized (this.mTrackBlackList) {
                        this.mTrackBlackList.clear();
                        this.mTrackBlackList.addAll(trackBlackList);
                    }
                } catch (Exception e8) {
                }
            }
        } else if (sDebug) {
            Log.e(TAG, "tracking file failed: file doesn't exist!");
        }
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        sDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false) | this.mDynamicDebug;
    }

    private static OppoBaseActivityTaskManagerService typeCasting(ActivityTaskManagerService atms) {
        if (atms != null) {
            return (OppoBaseActivityTaskManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityTaskManagerService.class, atms);
        }
        return null;
    }

    private static OppoBaseIntent typeCasting(Intent intent) {
        if (intent != null) {
            return (OppoBaseIntent) ColorTypeCastingHelper.typeCasting(OppoBaseIntent.class, intent);
        }
        return null;
    }
}
