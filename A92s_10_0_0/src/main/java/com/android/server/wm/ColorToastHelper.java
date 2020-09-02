package com.android.server.wm;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.OppoActivityManager;
import android.app.OppoWhiteListManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import android.view.ColorBaseLayoutParams;
import android.view.IWindow;
import com.android.server.am.ColorAppCrashClearManager;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.storage.ColorStorageUtils;
import com.color.settings.ColorSettings;
import com.color.settings.ColorSettingsChangeListener;
import com.color.util.ColorTypeCastingHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class ColorToastHelper {
    private static final int ACTION_POP_ADD = 1;
    private static final int ACTION_POP_REMOVE = 0;
    static boolean DEBUG_DETAIL = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static final String DEFAULT_SEITCH_OFF = "off";
    private static final String FEATURE_OPPO_CMCC_TEST = "oppo.system.cmcc.test";
    private static final String KEY_FLOAT_TYPE = "floatType";
    private static final String KEY_PACKAGE_NAME = "pkgName";
    private static final String KEY_SHOWORHIDE = "showOrHide";
    private static final String KEY_SHOW_REASON = "showReason";
    private static final String KEY_TOP_PACKAGE_NAME = "topPkgName";
    private static final int MIN_FLOATWINDOW_SIZE = 3;
    static boolean NOTIFY_POPUP = SystemProperties.getBoolean("persist.sys.popup_notifier", false);
    public static final String PKG_PERFIX_COLOR = "com.coloros.";
    public static final String PKG_PERFIX_NEARME = "com.nearme.";
    public static final String PKG_PERFIX_OPPO = "com.oppo.";
    private static final String STATIC_ACTION = "oppo.action.FLOAT_WINDOW_DATA_COLLECTION";
    private static final String SYSTEM_ERROR_LOG_EVENTID = "SystemAlertErrorWinodwEvent";
    private static final String TAG = "ColorToastHelper";
    private static final String TOAST_FILTER_FILE_PATH = "//data//oppo//coloros//toast//toast.xml";
    private static final String TOAST_WHITELIST_FILE_PATH = "//data//oppo/coloros//toast//float_window_white_list.xml";
    private static final String UPLOAD_LOGTAG = "20089";
    private static final String UPLOAD_LOG_EVENTID = "FloatWindowInterceptEvent";
    private static final String VALUE_HIDE = "hide";
    private static final String VALUE_REASON_FOREGROUND = "reasonForeground";
    private static final String VALUE_REASON_USERALLOW = "reasonUserAllow";
    private static final String VALUE_REASON_WHITELIST = "reasonWhitelist";
    private static final String VALUE_SHOW = "show";
    private static final String VALUE_TYPE_SYSTEM_ALERT = "typeSystemAlert";
    private static final String VALUE_TYPE_TOAST = "typeToast";
    /* access modifiers changed from: private */
    public static boolean mDynamicDebug = false;
    private static UserFileChangeListener mUserFileChangeListener;
    /* access modifiers changed from: private */
    public static OppoBaseWindowManagerService sBaseWms;
    private static HashSet<String> sDefaultOpenSet = new HashSet<>();
    /* access modifiers changed from: private */
    public static Map<Integer, HashSet<String>> sDefaultOpenSetAllUser = new HashMap();
    private static boolean sDefaultSwitch = false;
    private static ColorToastHelper sInstance = null;
    private static boolean sIsCmccTestVersion = false;
    /* access modifiers changed from: private */
    public static List<String> sListToastWindow = new ArrayList();
    /* access modifiers changed from: private */
    public static Map<Integer, List<String>> sListToastWindowAllUser = new HashMap();
    private static DataFileListener sToastAppListener = null;
    /* access modifiers changed from: private */
    public static Map<String, String> sToastAppMap;
    /* access modifiers changed from: private */
    public static Map<Integer, Map<String, String>> sToastAppMapAllUser = new HashMap();
    /* access modifiers changed from: private */
    public static ReentrantLock sToastLock = new ReentrantLock();
    private static final ToastWindowHandler sToastWindowHandler = new ToastWindowHandler(sToastWindowThread.getLooper());
    private static final HandlerThread sToastWindowThread = new HandlerThread("ToastWindowThread");
    private static final Handler sUserFileChnageHandler = new Handler(sUserFileChnageThread.getLooper());
    private static final HandlerThread sUserFileChnageThread = new HandlerThread("UserFileChnageThread");
    /* access modifiers changed from: private */
    public static WindowManagerService sWMService;
    /* access modifiers changed from: private */
    public static List<String> sWhiteList;
    /* access modifiers changed from: private */
    public static Map<Integer, List<String>> sWhiteListAllUser = new HashMap();
    private static DataFileListener sWhiteListListener = null;
    private Map<Integer, List<String>> mCheckedFloatWindowSet = new HashMap();
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public HandlerFloatWindow mHandlerFloatWindow;
    WindowState mNextWin = null;
    private OppoActivityManager mOppoAm;
    WindowState mPreWin = null;
    private HandlerThread mThreadFloatWindow;
    private UserChangeReceiver mUserChangeReceiver = null;

    static {
        sToastAppMap = new HashMap();
        sWhiteList = new ArrayList();
        sToastAppMap = getToastAppMapPri();
        sWhiteList = getToastWhiteListPri();
        new DataFileListener(TOAST_FILTER_FILE_PATH).startWatching();
        new DataFileListener(TOAST_WHITELIST_FILE_PATH).startWatching();
        sToastWindowThread.start();
        sUserFileChnageThread.start();
    }

    public static ColorToastHelper getInstance() {
        if (sInstance == null) {
            synchronized (ColorToastHelper.class) {
                if (sInstance == null) {
                    sInstance = new ColorToastHelper();
                }
            }
        }
        return sInstance;
    }

    public static Map<String, String> getToastAppMap(int userid) {
        initObserver();
        if (userid == 0 || !sToastAppMapAllUser.containsKey(Integer.valueOf(userid))) {
            return sToastAppMap;
        }
        return sToastAppMapAllUser.get(Integer.valueOf(userid));
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX DEBUG: Type inference failed for r1v6. Raw type applied. Possible types: java.util.Map<java.lang.String, java.lang.String> */
    /* access modifiers changed from: private */
    public static Map<String, String> getToastAppMapPri() {
        if (mDynamicDebug) {
            Log.d(TAG, "getToastAppMap()");
        }
        Map<String, String> resultMap = new HashMap<>();
        sToastLock.lock();
        FileInputStream inputStream = null;
        inputStream = null;
        try {
            File file = new File(TOAST_FILTER_FILE_PATH);
            if (file.exists()) {
                inputStream = new FileInputStream(file);
                resultMap = readToastFromXML(inputStream);
            } else {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    e = e2;
                }
            }
        } catch (Exception e3) {
            Log.e(TAG, "getToastAppMap() error !");
            e3.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    e = e4;
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            sToastLock.unlock();
            throw th;
        }
        sToastLock.unlock();
        return resultMap;
        e.printStackTrace();
        sToastLock.unlock();
        return resultMap;
    }

    private static Map<String, String> readToastFromXML(InputStream stream) {
        int type;
        Map<String, String> toastMap = new HashMap<>();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            do {
                type = parser.next();
                if (type == 2 && "toast".equals(parser.getName())) {
                    String name = null;
                    String text = null;
                    try {
                        name = parser.getAttributeValue(null, "packagename");
                        text = parser.nextText();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!(name == null || text == null)) {
                        toastMap.put(name, text);
                        continue;
                    }
                }
            } while (type != 1);
        } catch (Exception e2) {
            Log.e(TAG, "readToastFromXML() error");
            e2.printStackTrace();
        }
        return toastMap;
    }

    public static void setToastMap(Map<String, String> toastMap) {
        if (mDynamicDebug) {
            Log.d(TAG, "setToastMap()");
        }
        if (toastMap == null || toastMap.isEmpty()) {
            Log.e(TAG, "setToastMap() empty map, return.");
            return;
        }
        sToastLock.lock();
        FileOutputStream outputStream = null;
        try {
            File file = new File(TOAST_FILTER_FILE_PATH);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                new FileOutputStream(file).close();
            } catch (IOException e2) {
                e = e2;
            }
        } catch (Exception e3) {
            Log.e(TAG, "setToastMap() error!");
            e3.printStackTrace();
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e4) {
                    e = e4;
                }
            }
        } catch (Throwable th) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            sToastLock.unlock();
            throw th;
        }
        sToastLock.unlock();
        e.printStackTrace();
        sToastLock.unlock();
    }

    public static void setToastMap(final Context context, final Map<String, String> toastMap, final int userid) {
        try {
            sToastWindowHandler.postAtFrontOfQueue(new Runnable() {
                /* class com.android.server.wm.ColorToastHelper.AnonymousClass1 */

                public void run() {
                    if (ColorToastHelper.mDynamicDebug) {
                        Log.d(ColorToastHelper.TAG, "setToastMap()");
                    }
                    Map map = toastMap;
                    if (map == null || map.isEmpty()) {
                        Log.e(ColorToastHelper.TAG, "setToastMap() empty map, return.");
                        return;
                    }
                    ColorToastHelper.sToastLock.lock();
                    OutputStream outputStream = null;
                    try {
                        outputStream = ColorSettings.writeConfigAsUser(context, "toast/toast.xml", userid, 0);
                        boolean unused = ColorToastHelper.writeToastToXML(outputStream, toastMap);
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e = e;
                            }
                        }
                    } catch (Exception e2) {
                        Log.e(ColorToastHelper.TAG, "setToastMap() error!");
                        e2.printStackTrace();
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e3) {
                                e = e3;
                            }
                        }
                    } catch (Throwable th) {
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e4) {
                                e4.printStackTrace();
                            }
                        }
                        ColorToastHelper.sToastLock.unlock();
                        throw th;
                    }
                    ColorToastHelper.sToastLock.unlock();
                    e.printStackTrace();
                    ColorToastHelper.sToastLock.unlock();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "setToastMap() exception");
        }
    }

    /* access modifiers changed from: private */
    public static boolean writeToastToXML(OutputStream stream, Map<String, String> marketMap) {
        try {
            XmlSerializer out = Xml.newSerializer();
            out.setOutput(stream, "utf-8");
            out.startDocument(null, true);
            out.text("\r\n");
            out.startTag(null, "toastlist");
            for (String name : marketMap.keySet()) {
                out.text("\r\n");
                out.text("\t");
                out.startTag(null, "toast");
                out.attribute(null, "packagename", name);
                out.text(marketMap.get(name));
                out.endTag(null, "toast");
            }
            out.text("\r\n");
            out.endTag(null, "toastlist");
            out.text("\r\n");
            out.endDocument();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "writeToastToXML() error!");
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getToastWhiteList(int userid) {
        if (sWhiteListAllUser.containsKey(Integer.valueOf(userid))) {
            return sWhiteListAllUser.get(Integer.valueOf(userid));
        }
        return sWhiteList;
    }

    /* access modifiers changed from: private */
    public static List<String> getToastWhiteListPri() {
        int type;
        String name;
        List<String> sWhiteList2 = new ArrayList<>();
        sToastLock.lock();
        File file = new File(TOAST_WHITELIST_FILE_PATH);
        FileInputStream inputStream = null;
        if (file.exists()) {
            try {
                FileInputStream inputStream2 = new FileInputStream(file);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(inputStream2, null);
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tag = parser.getName();
                        if (ColorAppCrashClearManager.CRASH_CLEAR_NAME.equals(tag)) {
                            String switchStr = parser.getAttributeValue(null, "defaultswitch");
                            if (switchStr != null && !switchStr.isEmpty()) {
                                if (switchStr.equalsIgnoreCase(DEFAULT_SEITCH_OFF)) {
                                    sDefaultSwitch = false;
                                } else {
                                    sDefaultSwitch = true;
                                }
                            }
                            continue;
                        } else if ("force_open".equals(tag)) {
                            String name2 = parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME);
                            if (name2 != null && !name2.isEmpty()) {
                                sWhiteList2.add(name2);
                                continue;
                            }
                        } else if ("default_open".equals(tag) && (name = parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME)) != null && !name.isEmpty()) {
                            sDefaultOpenSet.add(name);
                            continue;
                        }
                    }
                } while (type != 1);
                try {
                    inputStream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e2) {
                Log.e(TAG, "getToastWhiteList() error!");
                e2.printStackTrace();
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                throw th;
            }
        } else {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e4) {
                e4.printStackTrace();
            }
        }
        sToastLock.unlock();
        return sWhiteList2;
    }

    public static boolean getDefaultSwitch() {
        if (sIsCmccTestVersion) {
            return true;
        }
        return sDefaultSwitch;
    }

    private static class DataFileListener extends FileObserver {
        String mObserverPath = null;

        public DataFileListener(String path) {
            super(path, 264);
            this.mObserverPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8) {
                String str = this.mObserverPath;
                if (str != null) {
                    File file = new File(str);
                    if (!file.exists()) {
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (this.mObserverPath.equals(ColorToastHelper.TOAST_FILTER_FILE_PATH)) {
                        Map unused = ColorToastHelper.sToastAppMap = ColorToastHelper.getToastAppMapPri();
                    } else if (this.mObserverPath.equals(ColorToastHelper.TOAST_WHITELIST_FILE_PATH)) {
                        List unused2 = ColorToastHelper.sWhiteList = ColorToastHelper.getToastWhiteListPri();
                        if (ColorToastHelper.sToastAppMap != null && !ColorToastHelper.sToastAppMap.isEmpty()) {
                            Map<String, String> tmpToastAppMap = new HashMap<>();
                            tmpToastAppMap.putAll(ColorToastHelper.sToastAppMap);
                            for (String pkgName : tmpToastAppMap.keySet()) {
                                if (ColorToastHelper.sWhiteList.contains(pkgName)) {
                                    ColorToastHelper.sToastAppMap.remove(pkgName);
                                }
                            }
                            ColorToastHelper.setToastMap(ColorToastHelper.sToastAppMap);
                        }
                    }
                    if (ColorToastHelper.sWMService != null) {
                        Log.v(ColorToastHelper.TAG, "sWMService.updateAppOpsState()");
                        if (!(ColorToastHelper.sBaseWms == null || ColorToastHelper.sBaseWms.mColorWmsInner == null)) {
                            ColorToastHelper.sBaseWms.mColorWmsInner.updateAppOpsState();
                        }
                    }
                }
                if (ColorToastHelper.mDynamicDebug) {
                    Log.d(ColorToastHelper.TAG, "event " + event + " , mObserverPath " + this.mObserverPath);
                }
            }
        }
    }

    public static boolean skipPackage(Context context, String packageName, int userId) {
        List<String> globalWhiteList = new OppoWhiteListManager(context).getGlobalWhiteList();
        return globalWhiteList != null ? isOppoApp(packageName) || isSystemApp(context, packageName, userId) || globalWhiteList.contains(packageName) : isOppoApp(packageName) || isSystemApp(context, packageName, userId);
    }

    private static boolean isOppoApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (packageName.startsWith(PKG_PERFIX_OPPO) || packageName.startsWith(PKG_PERFIX_COLOR) || packageName.startsWith(PKG_PERFIX_NEARME)) {
            return true;
        }
        return false;
    }

    private static boolean isSystemApp(Context context, String packageName, int userId) {
        boolean result = false;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfoAsUser(packageName, 0, userId);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, packageName + " not found !");
            e.printStackTrace();
        }
        if (info == null) {
            return false;
        }
        if ((info.flags & 1) != 0) {
            result = true;
        }
        return result;
    }

    public static boolean shouldCloseToast(Context context, String packageName, int userId) {
        if (!SystemProperties.getBoolean("persist.sys.permission.enable", false) || skipPackage(context, packageName, userId) || getToastWhiteList(userId).contains(packageName)) {
            return false;
        }
        Map<String, String> toastMap = getToastAppMap(userId);
        if (!toastMap.containsKey(packageName)) {
            HashSet<String> defaultOpenSet = getDefaultOpenSet(userId);
            if (getDefaultSwitch() || defaultOpenSet.contains(packageName) || hasAppOp(context, 24, packageName)) {
                toastMap.put(packageName, "1");
                setToastMap(context, toastMap, userId);
                return false;
            }
            toastMap.put(packageName, "0");
            setToastMap(context, toastMap, userId);
            sendToastDataStatic(context, packageName);
            return true;
        } else if (!toastMap.get(packageName).equals("0")) {
            return false;
        } else {
            Log.v(TAG, "package : " + packageName + " ; view : " + packageName);
            sendToastDataStatic(context, packageName);
            return true;
        }
    }

    public static boolean isPackageToastClosed(String packageName) {
        List<String> list;
        int userid = ActivityManager.getCurrentUser();
        if (userid == 0 || !sWhiteListAllUser.containsKey(Integer.valueOf(userid))) {
            if ((userid != 0 || (list = sWhiteList) == null || !list.contains(packageName)) && SystemProperties.getBoolean("persist.sys.permission.enable", false) && getToastAppMap(userid).get(packageName) != null && getToastAppMap(userid).get(packageName).equals("0")) {
                return true;
            }
            return false;
        } else if (sWhiteListAllUser.get(Integer.valueOf(userid)).contains(packageName)) {
            return false;
        } else {
            return false;
        }
    }

    public static void setWMService(WindowManagerService service) {
        if (sWMService == null) {
            sWMService = service;
            sBaseWms = typeCasting(service);
            if (sWMService.mContext != null) {
                sIsCmccTestVersion = sWMService.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_CMCC_TEST);
            }
        }
    }

    private static void sendToastDataStatic(Context context, String packageName) {
        Message msg = sToastWindowHandler.obtainMessage(ColorStorageUtils.DATA_SIMU_MODE_ENOUGH);
        Bundle bundle = new Bundle();
        bundle.putString("packageName", packageName);
        msg.setData(bundle);
        msg.sendToTarget();
    }

    private static class ToastWindowHandler extends Handler {
        static final int MSG_CLOSE_TOAST_DATA = 103;

        public ToastWindowHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Bundle bundle;
            String packageName;
            if (!(msg.what != 103 || (bundle = msg.getData()) == null || (packageName = bundle.getString("packageName")) == null || ColorToastHelper.sWMService == null || ColorToastHelper.sWMService.mContext == null)) {
                ColorToastHelper.sListToastWindow.add(packageName);
                if (ColorToastHelper.sListToastWindow.size() >= 3) {
                    Intent intent = new Intent(ColorToastHelper.STATIC_ACTION);
                    intent.putStringArrayListExtra(ColorToastHelper.KEY_PACKAGE_NAME, new ArrayList<>(ColorToastHelper.sListToastWindow));
                    ColorToastHelper.sWMService.mContext.sendBroadcast(intent);
                    ColorToastHelper.sListToastWindow.clear();
                }
            }
            super.handleMessage(msg);
        }
    }

    private static boolean hasAppOp(Context context, int appOp, String packageName) {
        try {
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService("appops");
            ApplicationInfo appInfo = null;
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 4096);
            if (packageInfo != null) {
                appInfo = packageInfo.applicationInfo;
            }
            if (appInfo == null) {
                return false;
            }
            return appOpsManager.checkOpNoThrow(appOp, appInfo.uid, packageName) == 0;
        } catch (Exception e) {
            Log.d(TAG, "check op error!");
            return false;
        }
    }

    private static void initObserver() {
        try {
            if (sWhiteListListener == null || sToastAppListener == null) {
                sWhiteListListener = new DataFileListener(TOAST_WHITELIST_FILE_PATH);
                sToastAppListener = new DataFileListener(TOAST_FILTER_FILE_PATH);
                sWhiteListListener.startWatching();
                sToastAppListener.startWatching();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isForumVersion() {
        String ver = SystemProperties.get("ro.build.version.opporom");
        if (ver == null) {
            return false;
        }
        String ver2 = ver.toLowerCase();
        if (ver2.endsWith("alpha") || ver2.endsWith("beta")) {
            return true;
        }
        return false;
    }

    private void statisticsFloatWindowInfo(String packageName, String type, String show, String reason) {
        if (isForumVersion()) {
            Map<String, String> statisticsMap = new HashMap<>();
            statisticsMap.put(KEY_PACKAGE_NAME, packageName);
            statisticsMap.put(KEY_FLOAT_TYPE, type);
            statisticsMap.put(KEY_SHOWORHIDE, show);
            if (reason != null) {
                statisticsMap.put(KEY_SHOW_REASON, reason);
            }
            OppoStatistics.onCommon(this.mContext, UPLOAD_LOGTAG, UPLOAD_LOG_EVENTID, statisticsMap, false);
        }
    }

    private void statisticsSystemErrorWindow(String packageName, String topPkgName) {
        if (isForumVersion()) {
            Map<String, String> statisticsMap = new HashMap<>();
            statisticsMap.put(KEY_PACKAGE_NAME, packageName);
            statisticsMap.put(KEY_TOP_PACKAGE_NAME, topPkgName);
            OppoStatistics.onCommon(this.mContext, UPLOAD_LOGTAG, SYSTEM_ERROR_LOG_EVENTID, statisticsMap, false);
        }
    }

    public void handleWindow(WindowState win, Session session, IWindow client, int type) {
        WindowState parentWindowState;
        WindowManagerService windowManagerService;
        ColorBaseLayoutParams params;
        String topAppName = "";
        Boolean isUserToast = false;
        int wUserId = UserHandle.getUserId(win.getOwningUid());
        try {
            if (!(this.mOppoAm == null || this.mOppoAm.getTopActivityComponentName() == null)) {
                topAppName = this.mOppoAm.getTopActivityComponentName().getPackageName();
            }
            if (!(win.mAppOp != 45 || (params = (ColorBaseLayoutParams) ColorTypeCastingHelper.typeCasting(ColorBaseLayoutParams.class, win.mAttrs)) == null || (params.colorFlags & 1) == 0)) {
                if (mDynamicDebug) {
                    Log.d(TAG, "handleWindow, is user toast!" + win.getOwningPackage());
                }
                isUserToast = true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (win.mAppOp != -1 && (windowManagerService = sWMService) != null && windowManagerService.mAppOps != null) {
            int startOpResult = sWMService.mAppOps.startOpNoThrow(win.mAppOp, win.getOwningUid(), win.getOwningPackage(), true);
            if (startOpResult != 0 && startOpResult != 3) {
                if (24 == win.mAppOp) {
                    HandlerFloatWindow handlerFloatWindow = this.mHandlerFloatWindow;
                    handlerFloatWindow.sendMessage(handlerFloatWindow.obtainMessage(1, session));
                }
                if (24 != win.mAppOp) {
                    getColorWindowStateInner(win).setAppOpVisibilityLw(false);
                } else if (SystemProperties.getBoolean("persist.sys.permission.enable", false)) {
                    if (!topAppName.equals(win.getOwningPackage())) {
                        getColorWindowStateInner(win).setAppOpVisibilityLw(false);
                        HandlerFloatWindow handlerFloatWindow2 = this.mHandlerFloatWindow;
                        handlerFloatWindow2.sendMessage(handlerFloatWindow2.obtainMessage(1, session));
                        statisticsFloatWindowInfo(win.getOwningPackage(), VALUE_TYPE_SYSTEM_ALERT, VALUE_HIDE, null);
                    } else {
                        getColorWindowStateInner(win).setAppOpVisibilityLw(true);
                        HandlerFloatWindow handlerFloatWindow3 = this.mHandlerFloatWindow;
                        handlerFloatWindow3.sendMessage(handlerFloatWindow3.obtainMessage(1, session));
                        statisticsFloatWindowInfo(win.getOwningPackage(), VALUE_TYPE_SYSTEM_ALERT, VALUE_SHOW, VALUE_REASON_FOREGROUND);
                    }
                    if (this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(wUserId))) {
                        this.mCheckedFloatWindowSet.get(Integer.valueOf(wUserId)).add(win.getOwningPackage());
                    } else {
                        List<String> checkedFloatWindow = new ArrayList<>();
                        checkedFloatWindow.add(win.getOwningPackage());
                        this.mCheckedFloatWindowSet.put(Integer.valueOf(wUserId), checkedFloatWindow);
                    }
                } else {
                    getColorWindowStateInner(win).setAppOpVisibilityLw(false);
                }
            } else if (45 == win.mAppOp) {
                if (isUserToast.booleanValue() && shouldCloseToast(this.mContext, win.getOwningPackage(), wUserId)) {
                    if (!topAppName.equals(win.getOwningPackage())) {
                        getColorWindowStateInner(win).setAppOpVisibilityLw(false);
                        statisticsFloatWindowInfo(win.getOwningPackage(), VALUE_TYPE_TOAST, VALUE_HIDE, null);
                    } else {
                        statisticsFloatWindowInfo(win.getOwningPackage(), VALUE_TYPE_TOAST, VALUE_SHOW, VALUE_REASON_FOREGROUND);
                    }
                    if (this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(wUserId)) && !this.mCheckedFloatWindowSet.get(Integer.valueOf(wUserId)).contains(win.getOwningPackage())) {
                        this.mCheckedFloatWindowSet.get(Integer.valueOf(wUserId)).add(win.getOwningPackage());
                    } else if (!this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(wUserId))) {
                        List<String> checkedFloatWindow2 = new ArrayList<>();
                        checkedFloatWindow2.add(win.getOwningPackage());
                        this.mCheckedFloatWindowSet.put(Integer.valueOf(wUserId), checkedFloatWindow2);
                    }
                }
            } else if (24 != win.mAppOp) {
            } else {
                if (2010 == type) {
                    statisticsSystemErrorWindow(win.getOwningPackage(), topAppName);
                }
            }
        } else if (win.isChildWindow() && (parentWindowState = win.getParentWindow()) != null && parentWindowState.mAppOp == 45 && !getColorWindowStateInner(parentWindowState).getAppOpVisibility()) {
            getColorWindowStateInner(win).setAppOpVisibilityLw(false);
        }
    }

    public void init(WindowManagerService service, Context context) {
        this.mContext = context;
        this.mOppoAm = new OppoActivityManager();
        this.mThreadFloatWindow = new HandlerThread("ThreadFloatWindow");
        this.mThreadFloatWindow.start();
        this.mHandlerFloatWindow = new HandlerFloatWindow(this.mThreadFloatWindow.getLooper());
        this.mHandlerFloatWindow.sendEmptyMessageDelayed(2, 600000);
        setWMService(service);
        initReceiver();
        registerLogModule();
    }

    private void initReceiver() {
        registerReceiverForUserChange();
        mUserFileChangeListener = new UserFileChangeListener(sUserFileChnageHandler);
        ColorSettings.registerChangeListener(this.mContext, (String) null, 0, mUserFileChangeListener);
    }

    public final class HandlerFloatWindow extends Handler {
        private static final String ACTION_PERMISSION_PROTECT_NOTIFY = "com.oppo.permissionprotect.notify";
        public static final int ADD_FLOAT_WINDOW_PACKAGE = 1;
        public static final int HEARTBEAT = 2;
        public static final int HEARTBEAT_TEN_MINUTES = 600000;
        private static final int MIN_FLOATWINDOW_SIZE = 3;
        private static final int NOTIFY_PERMISSION_DENIED = 3;
        private static final String PERMISSION_FLOAT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";
        private static final long PERMISSION_TOAST_INTERVAL = 2000;
        public static final int POPUP_NOTIFY = 4;
        public static final String TAG_TYPE = "Type";
        public static final String TYPE_CHILD_WINDOW = "ChildWindow";
        private final String ACTION = ColorToastHelper.STATIC_ACTION;
        private List<String> mListFloatWindow = new ArrayList();

        public HandlerFloatWindow(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                String[] pkgNameArray = ColorToastHelper.this.mContext.getPackageManager().getPackagesForUid(((Session) msg.obj).mUid);
                if (pkgNameArray != null) {
                    String pkgName = pkgNameArray[0];
                    this.mListFloatWindow.add(pkgName);
                    if (this.mListFloatWindow.size() >= 3) {
                        sendAndClear();
                    }
                    Message m = Message.obtain();
                    m.what = 3;
                    m.obj = pkgName;
                    ColorToastHelper.this.mHandlerFloatWindow.removeMessages(3);
                    ColorToastHelper.this.mHandlerFloatWindow.sendMessageDelayed(m, PERMISSION_TOAST_INTERVAL);
                }
            } else if (i == 2) {
                sendAndClear();
                sendEmptyMessageDelayed(2, 600000);
            } else if (i == 3) {
                Intent intent = new Intent(ACTION_PERMISSION_PROTECT_NOTIFY);
                intent.putExtra("PackageName", msg.obj.toString());
                intent.putExtra("Permission", PERMISSION_FLOAT_WINDOW);
                ColorToastHelper.this.mContext.sendBroadcast(intent);
            } else if (i == 4) {
                PopupInfo popupInfo = (PopupInfo) msg.obj;
                ColorToastHelper.this.sendPopupWinBroadcast(popupInfo, UserHandle.getUserId(popupInfo.uid));
            }
        }

        private void sendAndClear() {
            if (this.mListFloatWindow.size() > 0) {
                Intent intent = new Intent(ColorToastHelper.STATIC_ACTION);
                intent.putStringArrayListExtra(ColorToastHelper.KEY_PACKAGE_NAME, new ArrayList<>(this.mListFloatWindow));
                ColorToastHelper.this.mContext.sendBroadcast(intent);
                this.mListFloatWindow.clear();
            }
        }
    }

    public void sendPopUpNotifyMessage(WindowState win) {
        if (NOTIFY_POPUP && getColorWindowStateInner(win).getAppOpVisibility()) {
            PopupInfo popupInfo = new PopupInfo(0, win.getOwningPackage(), win.getOwningUid(), win.mAttrs.type);
            HandlerFloatWindow handlerFloatWindow = this.mHandlerFloatWindow;
            handlerFloatWindow.sendMessage(handlerFloatWindow.obtainMessage(4, popupInfo));
        }
    }

    public void sendBroadcastForFloatWindow(boolean state, final String packageName) {
        if (!state) {
            this.mHandlerFloatWindow.post(new Runnable() {
                /* class com.android.server.wm.ColorToastHelper.AnonymousClass2 */

                public void run() {
                    Intent intent = new Intent("com.oppo.permissionprotect.notify");
                    intent.putExtra("PackageName", packageName);
                    intent.putExtra("Permission", "android.permission.SYSTEM_ALERT_WINDOW");
                    intent.putExtra(HandlerFloatWindow.TAG_TYPE, HandlerFloatWindow.TYPE_CHILD_WINDOW);
                    ColorToastHelper.this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
                }
            });
        }
    }

    public class PopupInfo {
        int action;
        String pkg;
        int type;
        int uid;

        public PopupInfo(int action2, String pkg2, int uid2, int type2) {
            this.action = action2;
            this.pkg = pkg2;
            this.uid = uid2;
            this.type = type2;
        }

        public String toString() {
            return "action=" + this.action + " pkg=" + this.pkg + " uid=" + this.uid + " type=" + this.type;
        }
    }

    /* access modifiers changed from: private */
    public void sendPopupWinBroadcast(PopupInfo popupInfo, int userid) {
        if (this.mContext != null) {
            int type = popupInfo.type;
            if (type == 2 || type == 2002 || type == 2003 || type == 2005 || type == 2008 || type == 2010) {
                Intent intent = new Intent("action.oppo.popup.notify");
                intent.putExtra("action", popupInfo.action);
                intent.putExtra("pkg", popupInfo.pkg);
                intent.putExtra("uid", popupInfo.uid);
                intent.putExtra("type", type);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, "android.permission.RETRIEVE_WINDOW_CONTENT");
                Log.d("popnotify", popupInfo.toString());
            }
        }
    }

    public void updateFloatWindowState(WindowState preWin, WindowState nextWin) {
        WindowState windowState;
        Map<Integer, List<String>> map;
        OppoBaseWindowManagerService oppoBaseWindowManagerService;
        int userid = ActivityManager.getCurrentUser();
        this.mPreWin = preWin != null ? preWin : this.mPreWin;
        this.mNextWin = nextWin != null ? nextWin : this.mNextWin;
        WindowState windowState2 = this.mPreWin;
        if (windowState2 != null && (windowState = this.mNextWin) != null && windowState2 != windowState) {
            String prePkg = windowState2.getOwningPackage();
            String nextPkg = this.mNextWin.getOwningPackage();
            if (prePkg != null && nextPkg != null && !prePkg.equals(nextPkg) && (map = this.mCheckedFloatWindowSet) != null) {
                if (map.containsKey(Integer.valueOf(userid)) && this.mCheckedFloatWindowSet.get(Integer.valueOf(userid)).contains(prePkg)) {
                    if (!getToastAppMap(userid).containsKey(prePkg) || !getToastAppMap(userid).get(prePkg).equals("1")) {
                        OppoBaseWindowManagerService oppoBaseWindowManagerService2 = sBaseWms;
                        if (!(oppoBaseWindowManagerService2 == null || oppoBaseWindowManagerService2.mColorWmsInner == null)) {
                            sBaseWms.mColorWmsInner.updateAppOpsState(prePkg, false);
                        }
                    } else {
                        this.mCheckedFloatWindowSet.get(Integer.valueOf(userid)).remove(prePkg);
                    }
                }
                if (this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(userid)) && this.mCheckedFloatWindowSet.get(Integer.valueOf(userid)).contains(nextPkg) && (oppoBaseWindowManagerService = sBaseWms) != null && oppoBaseWindowManagerService.mColorWmsInner != null) {
                    sBaseWms.mColorWmsInner.updateAppOpsState(nextPkg, true);
                }
            }
        }
    }

    public void handlePackageIntent(Intent intent) {
        int userId = ActivityManager.getCurrentUser();
        String packageName = intent.getData().getEncodedSchemeSpecificPart();
        if (packageName != null && this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(userId)) && this.mCheckedFloatWindowSet.get(Integer.valueOf(userId)).contains(packageName)) {
            this.mCheckedFloatWindowSet.get(Integer.valueOf(userId)).remove(packageName);
        }
    }

    public void handleAlertWindowSet(int mode, WindowState win) {
        int userId = UserHandle.getUserId(win.getOwningUid());
        if ((mode == 0 || mode == 3) && this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(userId)) && this.mCheckedFloatWindowSet.get(Integer.valueOf(userId)).contains(win.getOwningPackage())) {
            this.mCheckedFloatWindowSet.get(Integer.valueOf(userId)).remove(win.getOwningPackage());
        } else if (mode != 0 && mode != 3) {
            if (this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(userId)) && !this.mCheckedFloatWindowSet.get(Integer.valueOf(userId)).contains(win.getOwningPackage())) {
                this.mCheckedFloatWindowSet.get(Integer.valueOf(userId)).add(win.getOwningPackage());
            } else if (!this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(userId))) {
                List<String> checkedFloatWindow = new ArrayList<>();
                checkedFloatWindow.add(win.getOwningPackage());
                this.mCheckedFloatWindowSet.put(Integer.valueOf(userId), checkedFloatWindow);
            }
        }
    }

    public void handleFloatWindowSet(boolean b, WindowState win) {
        int userId = UserHandle.getUserId(win.getOwningUid());
        if (!b && this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(userId)) && this.mCheckedFloatWindowSet.get(Integer.valueOf(userId)).contains(win.getOwningPackage())) {
            this.mCheckedFloatWindowSet.remove(win.getOwningPackage());
        } else if (b && this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(userId)) && !this.mCheckedFloatWindowSet.get(Integer.valueOf(userId)).contains(win.getOwningPackage())) {
            this.mCheckedFloatWindowSet.get(Integer.valueOf(userId)).add(win.getOwningPackage());
        } else if (b && !this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(userId))) {
            List<String> checkedFloatWindow = new ArrayList<>();
            checkedFloatWindow.add(win.getOwningPackage());
            this.mCheckedFloatWindowSet.put(Integer.valueOf(userId), checkedFloatWindow);
        }
    }

    public boolean checkFloatWindowSet(String packageName) {
        int userid = ActivityManager.getCurrentUser();
        if (!this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(userid)) || !this.mCheckedFloatWindowSet.get(Integer.valueOf(userid)).contains(packageName)) {
            return false;
        }
        return true;
    }

    public void removeFromFloatWindowSet(String packageName) {
        int userid = ActivityManager.getCurrentUser();
        if (this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(userid)) && this.mCheckedFloatWindowSet.get(Integer.valueOf(userid)).contains(packageName)) {
            this.mCheckedFloatWindowSet.get(Integer.valueOf(userid)).remove(packageName);
        }
    }

    public void addToFloatWindowSet(String packageName) {
        int userId = ActivityManager.getCurrentUser();
        if (this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(userId)) && !this.mCheckedFloatWindowSet.get(Integer.valueOf(userId)).contains(packageName)) {
            this.mCheckedFloatWindowSet.get(Integer.valueOf(userId)).add(packageName);
        } else if (!this.mCheckedFloatWindowSet.containsKey(Integer.valueOf(userId))) {
            List<String> checkedFloatWindow = new ArrayList<>();
            checkedFloatWindow.add(packageName);
            this.mCheckedFloatWindowSet.put(Integer.valueOf(userId), checkedFloatWindow);
        }
    }

    private void registerReceiverForUserChange() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_STARTED");
        intentFilter.addAction("android.intent.action.USER_STOPPED");
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mUserChangeReceiver = new UserChangeReceiver();
        this.mContext.registerReceiver(this.mUserChangeReceiver, intentFilter);
    }

    private class UserChangeReceiver extends BroadcastReceiver {
        private UserChangeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            if (userId != -10000) {
                String action = intent.getAction();
                if ("android.intent.action.USER_ADDED".equals(action) || "android.intent.action.USER_SWITCHED".equals(action)) {
                    if (!ColorToastHelper.sToastAppMapAllUser.containsKey(Integer.valueOf(userId))) {
                        ColorToastHelper.sToastAppMapAllUser.put(Integer.valueOf(userId), new HashMap());
                    }
                    if (!ColorToastHelper.sWhiteListAllUser.containsKey(Integer.valueOf(userId))) {
                        ColorToastHelper.sWhiteListAllUser.put(Integer.valueOf(userId), new ArrayList());
                    }
                    if (!ColorToastHelper.sDefaultOpenSetAllUser.containsKey(Integer.valueOf(userId))) {
                        ColorToastHelper.sDefaultOpenSetAllUser.put(Integer.valueOf(userId), new HashSet<>());
                    }
                    if (!ColorToastHelper.sListToastWindowAllUser.containsKey(Integer.valueOf(userId))) {
                        ColorToastHelper.sListToastWindowAllUser.put(Integer.valueOf(userId), new ArrayList());
                    }
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    ColorToastHelper.sToastAppMapAllUser.remove(Integer.valueOf(userId));
                    ColorToastHelper.sWhiteListAllUser.remove(Integer.valueOf(userId));
                    ColorToastHelper.sDefaultOpenSetAllUser.remove(Integer.valueOf(userId));
                    ColorToastHelper.sListToastWindowAllUser.remove(Integer.valueOf(userId));
                }
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX DEBUG: Type inference failed for r1v6. Raw type applied. Possible types: java.util.Map<java.lang.String, java.lang.String> */
    /* access modifiers changed from: private */
    public static Map<String, String> getToastAppMapPri(Context context, int userId) {
        if (mDynamicDebug) {
            Log.d(TAG, "getToastAppMap for user:" + userId);
        }
        Map<String, String> resultMap = new HashMap<>();
        sToastLock.lock();
        InputStream inputStream = null;
        try {
            InputStream inputStream2 = ColorSettings.readConfigAsUser(context, "toast/toast.xml", userId, 0);
            if (inputStream2 != null) {
                resultMap = readToastFromXML(inputStream2);
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e) {
                    e = e;
                }
            }
        } catch (Exception e2) {
            Log.e(TAG, "getToastAppMap() error !");
            e2.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e = e3;
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            sToastLock.unlock();
            throw th;
        }
        sToastLock.unlock();
        return resultMap;
        e.printStackTrace();
        sToastLock.unlock();
        return resultMap;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c8, code lost:
        if (com.android.server.wm.ColorToastHelper.sDefaultOpenSetAllUser.containsKey(java.lang.Integer.valueOf(r15)) != false) goto L_0x00ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00ca, code lost:
        com.android.server.wm.ColorToastHelper.sDefaultOpenSetAllUser.remove(java.lang.Integer.valueOf(r15));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00d3, code lost:
        com.android.server.wm.ColorToastHelper.sDefaultOpenSetAllUser.put(java.lang.Integer.valueOf(r15), r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00fc, code lost:
        if (com.android.server.wm.ColorToastHelper.sDefaultOpenSetAllUser.containsKey(java.lang.Integer.valueOf(r15)) == false) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ff, code lost:
        com.android.server.wm.ColorToastHelper.sToastLock.unlock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0104, code lost:
        return r1;
     */
    public static List<String> getToastWhiteListPri(Context context, int userId) {
        int type;
        String name;
        List<String> whiteList = new ArrayList<>();
        HashSet<String> defaultOpenSet = new HashSet<>();
        sToastLock.lock();
        InputStream inputStream = null;
        try {
            InputStream inputStream2 = ColorSettings.readConfigAsUser(context, "toast/float_window_white_list.xml", userId, 0);
            if (inputStream2 != null) {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(inputStream2, null);
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tag = parser.getName();
                        if (ColorAppCrashClearManager.CRASH_CLEAR_NAME.equals(tag)) {
                            String switchStr = parser.getAttributeValue(null, "defaultswitch");
                            if (switchStr != null && !switchStr.isEmpty()) {
                                if (switchStr.equalsIgnoreCase(DEFAULT_SEITCH_OFF)) {
                                    sDefaultSwitch = false;
                                } else {
                                    sDefaultSwitch = true;
                                }
                            }
                            continue;
                        } else if ("force_open".equals(tag)) {
                            String name2 = parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME);
                            if (name2 != null && !name2.isEmpty()) {
                                Log.e(TAG, "getToastWhiteListPri name:" + name2);
                                whiteList.add(name2);
                                continue;
                            }
                        } else if ("default_open".equals(tag) && (name = parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME)) != null && !name.isEmpty()) {
                            Log.e(TAG, "getToastWhiteListPri defaultOpenSet name:" + name);
                            defaultOpenSet.add(name);
                            continue;
                        }
                    }
                } while (type != 1);
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e2) {
            Log.e(TAG, "getToastWhiteList() error!");
            e2.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            if (sDefaultOpenSetAllUser.containsKey(Integer.valueOf(userId))) {
                sDefaultOpenSetAllUser.remove(Integer.valueOf(userId));
            }
            sDefaultOpenSetAllUser.put(Integer.valueOf(userId), defaultOpenSet);
            throw th;
        }
    }

    private static HashSet<String> getDefaultOpenSet(int userid) {
        if (sDefaultOpenSetAllUser.containsKey(Integer.valueOf(userid))) {
            return sDefaultOpenSetAllUser.get(Integer.valueOf(userid));
        }
        return sDefaultOpenSet;
    }

    private class UserFileChangeListener extends ColorSettingsChangeListener {
        UserFileChangeListener(Handler handler) {
            super(handler);
        }

        public void onSettingsChange(boolean selfChange, String path, int userId) {
            if (userId != 0) {
                if (path.equalsIgnoreCase("/toast/toast.xml")) {
                    if (ColorToastHelper.sToastAppMapAllUser.containsKey(Integer.valueOf(userId))) {
                        ColorToastHelper.sToastAppMapAllUser.remove(Integer.valueOf(userId));
                    }
                    ColorToastHelper.sToastAppMapAllUser.put(Integer.valueOf(userId), ColorToastHelper.getToastAppMapPri(ColorToastHelper.this.mContext, userId));
                } else if (path.equalsIgnoreCase("/toast/float_window_white_list.xml")) {
                    if (ColorToastHelper.sWhiteListAllUser.containsKey(Integer.valueOf(userId))) {
                        ColorToastHelper.sWhiteListAllUser.remove(Integer.valueOf(userId));
                    }
                    ColorToastHelper.sWhiteListAllUser.put(Integer.valueOf(userId), ColorToastHelper.getToastWhiteListPri(ColorToastHelper.this.mContext, userId));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static OppoBaseWindowManagerService typeCasting(WindowManagerService wms) {
        if (wms != null) {
            return (OppoBaseWindowManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseWindowManagerService.class, wms);
        }
        return null;
    }

    private static IColorWindowStateInner getColorWindowStateInner(WindowState win) {
        OppoBaseWindowState baseWindowState = (OppoBaseWindowState) ColorTypeCastingHelper.typeCasting(OppoBaseWindowState.class, win);
        if (baseWindowState != null) {
            return baseWindowState.mColorWindowStateInner;
        }
        return IColorWindowStateInner.DEFAULT;
    }

    private static Object callMethodByReflect(Object object, String methodName) {
        try {
            Method method = object.getClass().getMethod(methodName, new Class[0]);
            method.setAccessible(true);
            return method.invoke(object, new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return false;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return false;
        }
    }

    private void setDynamicDebugSwitch(boolean on) {
        mDynamicDebug = on;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        StringBuilder sb = new StringBuilder();
        sb.append("mDynamicDebug = ");
        getInstance();
        sb.append(mDynamicDebug);
        Slog.i(TAG, sb.toString());
        getInstance().setDynamicDebugSwitch(on);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("mDynamicDebug = ");
        getInstance();
        sb2.append(mDynamicDebug);
        Slog.i(TAG, sb2.toString());
    }

    public void dump(String arg) {
        Slog.i(TAG, "#####dump####");
        if (!"float".equals(arg)) {
            Slog.i(TAG, "invalid arg:" + arg);
        }
    }

    private void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorToastHelper.class.getName());
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }

    public void updateWindowSet(int op, final String packageName) {
        synchronized (sWMService.mGlobalLock) {
            try {
                Slog.i(TAG, "opChanged, updateWindowSet");
                PackageManager pm = this.mContext.getPackageManager();
                int userId = ActivityManager.getCurrentUser();
                if (packageName == null || pm == null) {
                    Slog.v(TAG, "onOpChanged, no need to update, package = " + packageName);
                } else {
                    int mode = sWMService.mAppOps.checkOpNoThrow(op, pm.getPackageUidAsUser(packageName, userId), packageName);
                    if ((mode == 0 || mode == 3) && this.mCheckedFloatWindowSet.get(Integer.valueOf(userId)).contains(packageName)) {
                        sToastWindowHandler.post(new Runnable() {
                            /* class com.android.server.wm.ColorToastHelper.AnonymousClass3 */

                            public void run() {
                                if (ColorToastHelper.sBaseWms == null) {
                                    OppoBaseWindowManagerService unused = ColorToastHelper.sBaseWms = ColorToastHelper.typeCasting(ColorToastHelper.sWMService);
                                }
                                if (ColorToastHelper.sBaseWms != null) {
                                    ColorToastHelper.sBaseWms.updateAppOpsState(packageName, true);
                                }
                            }
                        });
                        this.mCheckedFloatWindowSet.get(Integer.valueOf(userId)).remove(packageName);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Slog.e(TAG, "packageName not found exception");
            } catch (Exception e2) {
                Slog.e(TAG, "updateWindowSet error");
            }
        }
    }
}
