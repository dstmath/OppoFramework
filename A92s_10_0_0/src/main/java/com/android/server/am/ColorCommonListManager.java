package com.android.server.am;

import android.app.AppGlobals;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Telephony;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.ColorAppDownloadTracker;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorPackageFreezeData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class ColorCommonListManager implements IColorCommonListManager {
    private static final String AodUserSetEnable = "Setting_AodEnableImmediate";
    public static final List<String> BLUETOOTH_UID_PROP = Arrays.asList("persist.bluetooth.gatt.uids", "persist.bluetooth.bt.sock.uids");
    public static final String CONFIG_DEFAULT_DIALER = "dialer";
    public static final String CONFIG_DEFAULT_INPUT = "input";
    public static final String CONFIG_DEFAULT_LAUNCHER = "launcher";
    public static final String CONFIG_DEFAULT_LIVE_WALLPAPER = "wallpaper";
    public static final String CONFIG_DEFAULT_SMS = "sms";
    public static final String CONFIG_SCREEN_RECORDER = "recorder";
    public static final String CONFIG_VPN = "vpn";
    public static final String CONFIG_WIDGET = "widget";
    private static final String KEY_BUNBLE_TYPE = "type";
    private static final String KEY_BUNDLE_CFG = "cfg";
    private static final String KEY_BUNDLE_PKG = "pkg";
    private static final String KEY_BUNDLE_UERID = "userId";
    /* access modifiers changed from: private */
    public static final boolean LOG_DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) LOG_DEBUG);
    private static final int MSG_INIT_DATA = 1;
    private static final int MSG_UPDATE_APP_INFO = 2;
    private static final int MSG_WINDOW_STATE_CHANGE = 3;
    private static final String TAG = "ColorCommonListManager";
    private static final String TYPE_PUT = "put";
    private static final String TYPE_RM = "rm";
    private static final String WIDGET_SAVE_FILE_PATH = "data/oppo/coloros/bpm/widget.xml";
    public static final List<String> mSingleAppList = Arrays.asList(CONFIG_DEFAULT_INPUT, CONFIG_DEFAULT_LAUNCHER, CONFIG_DEFAULT_SMS, CONFIG_DEFAULT_DIALER);
    private static ColorCommonListManager sInstance = null;
    private ContentObserver defaultAodObserver = new ContentObserver(this.mHandler) {
        /* class com.android.server.am.ColorCommonListManager.AnonymousClass2 */

        public void onChange(boolean selfChange) {
            ColorCommonListManager.this.updateAODValue();
        }
    };
    private ContentObserver defaultInputObserver = new ContentObserver(this.mHandler) {
        /* class com.android.server.am.ColorCommonListManager.AnonymousClass1 */

        public void onChange(boolean selfChange) {
            ColorCommonListManager.this.updateInputMethod(true);
        }
    };
    private boolean isAodEnable = LOG_DEBUG;
    private ActivityManagerService mAms = null;
    private ArrayList<AppChangedListener> mAppChangedListenerList = new ArrayList<>();
    private AudioManager mAudioManager = null;
    private IColorActivityManagerServiceEx mColorAmsEx = null;
    private ArrayMap<String, SparseArray<List>> mCommonListMap = new ArrayMap<>();
    private Context mContext = null;
    private Handler mHandler = null;
    private LocationManager mLocationManager = null;
    private boolean mLogDynamic = LOG_DEBUG;
    private PowerManager mPowerManager = null;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.am.ColorCommonListManager.AnonymousClass3 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ColorCommonListManager.LOG_DEBUG) {
                Log.d(ColorCommonListManager.TAG, "receiver: " + action);
            }
            if (TextUtils.isEmpty(action)) {
                return;
            }
            if ("android.telecom.action.DEFAULT_DIALER_CHANGED".equals(action)) {
                ColorCommonListManager.this.updateDefaultDialerApp();
            } else if ("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED".equals(action)) {
                ColorCommonListManager.this.updateDefaultSms();
            }
        }
    };
    private TelecomManager mTelecomManager = null;
    private ColorAppDownloadTracker mTracker = null;
    private ArrayList<Integer> mVisibleWindowList = new ArrayList<>();
    private ArrayMap<Integer, List<Integer>> mVisibleWindowMap = new ArrayMap<>();

    interface AppChangedListener {
        void onChanged(int i, String str, String str2, boolean z);
    }

    private ColorCommonListManager() {
    }

    public static ColorCommonListManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorCommonListManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorCommonListManager();
                }
            }
        }
        return sInstance;
    }

    public void init(IColorActivityManagerServiceEx amsEx) {
        if (amsEx != null) {
            this.mColorAmsEx = amsEx;
            this.mAms = amsEx.getActivityManagerService();
            this.mContext = this.mAms.mContext;
            initCore();
            initOther();
        }
    }

    public void putAppInfo(String pkgName, int uid, String config) {
        if (LOG_DEBUG) {
            Log.d(TAG, "putAppInfo " + pkgName + "#" + uid + " : " + config);
        }
        sendMessage(pkgName, uid, config, TYPE_PUT, 0);
    }

    public void removeAppInfo(String pkgName, int uid, String config) {
        if (LOG_DEBUG) {
            Log.d(TAG, "removeAppInfo " + pkgName + "#" + uid + " : " + config);
        }
        sendMessage(pkgName, uid, config, TYPE_RM, 0);
    }

    public void updateAppInfo(String action, int uid, String pkgName, boolean isBind) {
        String type = isBind ? TYPE_PUT : TYPE_RM;
        if ("android.net.VpnService".equals(action)) {
            sendMessage(pkgName, uid, CONFIG_VPN, type, 0);
        } else if ("android.service.wallpaper.WallpaperService".equals(action)) {
            sendMessage(pkgName, uid, CONFIG_DEFAULT_LIVE_WALLPAPER, type, 0);
        } else if ("android.view.InputMethod".equals(action)) {
            sendMessage(pkgName, uid, CONFIG_DEFAULT_INPUT, type, 0);
        }
    }

    public void updateWindowState(int uid, int pid, int windowId, int windowType, boolean isVisible, boolean shown) {
        if (this.mLogDynamic) {
            Log.v(TAG, "noteWindowStateChange uid = " + uid + "  pid = " + pid + "  windowId = " + windowId + "  isVisible = " + isVisible + "  shown = " + shown + "  windowType = " + windowType);
        }
        if (windowType == 2005 || windowType == 2003 || windowType == 2002 || windowType == 2007 || windowType == 2038 || windowType == 2006 || windowType == 2010) {
            sendMessage(3, uid, windowId, isVisible & shown, 0);
        }
    }

    public List<String> getAppInfo(String config, int uid) {
        List<String> appList;
        List<String> list = new ArrayList<>();
        synchronized (this.mCommonListMap) {
            SparseArray<List> valueList = this.mCommonListMap.get(config);
            if (!(valueList == null || (appList = valueList.get(uid)) == null)) {
                list.addAll(appList);
            }
        }
        return list;
    }

    private void initCore() {
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new MainHandler(thread.getLooper());
    }

    private void initOther() {
        registerReceiver();
        registerAodChange();
        initWidgetMap();
        sendMessage(1, 0, 0, LOG_DEBUG, 0);
    }

    private void addConfigInfoList(String pkg, int uid, String cfgName) {
        int pkgUid;
        if (CONFIG_DEFAULT_LAUNCHER.equals(cfgName) && (pkgUid = getPackageUid(pkg, uid)) != -1) {
            uid = pkgUid;
        }
        synchronized (this.mCommonListMap) {
            SparseArray<List> lastMap = this.mCommonListMap.get(cfgName);
            if (lastMap == null) {
                SparseArray<List> lastMap2 = new SparseArray<>();
                List<String> list = new ArrayList<>();
                list.add(pkg);
                lastMap2.put(uid, list);
                this.mCommonListMap.put(cfgName, lastMap2);
            } else {
                List<String> list2 = lastMap.get(uid);
                if (list2 == null) {
                    list2 = new ArrayList<>();
                }
                list2.add(pkg);
                lastMap.put(uid, list2);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002a, code lost:
        return;
     */
    private void removeConfigInfoList(String pkg, int uid, String cfgName) {
        synchronized (this.mCommonListMap) {
            SparseArray<List> lastMap = this.mCommonListMap.get(cfgName);
            if (lastMap != null) {
                List<String> list = lastMap.get(uid);
                if (list != null && list.contains(pkg)) {
                    list.remove(pkg);
                    if (list.isEmpty()) {
                        lastMap.remove(uid);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getPackageUid(String pkgName, int userId) {
        if (TextUtils.isEmpty(pkgName)) {
            return -1;
        }
        try {
            return AppGlobals.getPackageManager().getPackageUid(pkgName, 268435456, userId);
        } catch (Exception e) {
            return -1;
        }
    }

    private void registerInputChange() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("default_input_method"), true, this.defaultInputObserver, -1);
    }

    /* access modifiers changed from: private */
    public void updateInputMethod(boolean dispatch) {
        String inputName = getDefaultInputMethod();
        int uid = getPackageUid(inputName, this.mAms.getCurrentUser().id);
        addConfigInfoList(inputName, uid, CONFIG_DEFAULT_INPUT);
        if (dispatch && !TextUtils.isEmpty(inputName)) {
            dispatchAppChanged(uid, inputName, CONFIG_DEFAULT_INPUT, true);
        }
    }

    private void registerAodChange() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(AodUserSetEnable), true, this.defaultAodObserver, -1);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telecom.action.DEFAULT_DIALER_CHANGED");
        filter.addAction("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, filter, null, this.mHandler);
    }

    /* access modifiers changed from: private */
    public void updateDefaultDialerApp() {
        String dialerPkg = getDefaultDialerApp();
        if (!TextUtils.isEmpty(dialerPkg)) {
            int uid = getPackageUid(dialerPkg, this.mAms.getCurrentUser().id);
            addConfigInfoList(dialerPkg, uid, CONFIG_DEFAULT_DIALER);
            if (LOG_DEBUG) {
                Log.d(TAG, "dialerApp: " + uid + " " + dialerPkg);
            }
        }
    }

    private String getDefaultDialerApp() {
        if (this.mTelecomManager == null) {
            this.mTelecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        }
        try {
            return this.mTelecomManager.getDefaultDialerPackage();
        } catch (Exception e) {
            Log.e(TAG, "getDefaultDialerApp failed e" + e);
            return "";
        }
    }

    /* access modifiers changed from: private */
    public void updateDefaultSms() {
        String smsApp = getDefaultSms();
        if (!TextUtils.isEmpty(smsApp)) {
            int uid = getPackageUid(smsApp, this.mAms.getCurrentUser().id);
            addConfigInfoList(smsApp, uid, CONFIG_DEFAULT_SMS);
            if (LOG_DEBUG) {
                Log.d(TAG, "smsApp: " + uid + " " + smsApp);
            }
        }
    }

    private String getDefaultSms() {
        return Telephony.Sms.getDefaultSmsPackage(this.mContext);
    }

    private void sendMessage(String pkg, int uid, String configName, String type, long delay) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_BUNDLE_PKG, pkg);
        bundle.putInt(KEY_BUNDLE_UERID, uid);
        bundle.putString(KEY_BUNDLE_CFG, configName);
        bundle.putString(KEY_BUNBLE_TYPE, type);
        Message msg = Message.obtain();
        msg.setData(bundle);
        msg.what = 2;
        this.mHandler.sendMessageDelayed(msg, delay);
    }

    private void sendMessage(int what, int uid, int arg2, boolean visible, long delay) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = uid;
        msg.arg2 = arg2;
        msg.obj = Boolean.valueOf(visible);
        this.mHandler.sendMessageDelayed(msg, delay);
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                ColorCommonListManager.this.handInitData();
            } else if (i == 2) {
                ColorCommonListManager.this.handleUpdateAppInfo(msg);
            } else if (i == 3) {
                ColorCommonListManager.this.handleUpdateWindowStateChange(msg);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handInitData() {
        updateDefaultDialerApp();
        updateDefaultSms();
        updateAODValue();
    }

    /* access modifiers changed from: private */
    public void handleUpdateAppInfo(Message msg) {
        if (msg != null && msg.getData() != null) {
            Bundle bundle = msg.getData();
            String pkg = bundle.getString(KEY_BUNDLE_PKG, "");
            int uid = bundle.getInt(KEY_BUNDLE_UERID, 0);
            String type = bundle.getString(KEY_BUNBLE_TYPE, "");
            String cfgName = bundle.getString(KEY_BUNDLE_CFG, "");
            if (TYPE_PUT.equals(type)) {
                addConfigInfoList(pkg, uid, cfgName);
                dispatchAppChanged(uid, pkg, cfgName, true);
                saveWidgetFile(cfgName, pkg, uid, LOG_DEBUG);
            } else if (TYPE_RM.equals(type)) {
                removeConfigInfoList(pkg, uid, cfgName);
                saveWidgetFile(cfgName, pkg, uid, true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0107, code lost:
        if (r3 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0109, code lost:
        dispatchAppChanged(r0, java.lang.String.valueOf(r0), "floatWindow", true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
        return;
     */
    public void handleUpdateWindowStateChange(Message msg) {
        if (msg != null && msg.getData() != null) {
            int uid = msg.arg1;
            int windowId = msg.arg2;
            boolean isVisible = ((Boolean) msg.obj).booleanValue();
            boolean isNotify = LOG_DEBUG;
            synchronized (this.mVisibleWindowMap) {
                boolean inVisibleMap = this.mVisibleWindowMap.containsKey(Integer.valueOf(uid));
                Integer windowid = new Integer(windowId);
                if (isVisible) {
                    if (inVisibleMap) {
                        List<Integer> windowIdList = this.mVisibleWindowMap.get(Integer.valueOf(uid));
                        if (!windowIdList.contains(windowid)) {
                            windowIdList.add(windowid);
                            this.mVisibleWindowMap.put(Integer.valueOf(uid), windowIdList);
                        }
                    } else {
                        List<Integer> windowIdList2 = new ArrayList<>();
                        windowIdList2.add(windowid);
                        this.mVisibleWindowMap.put(Integer.valueOf(uid), windowIdList2);
                    }
                } else if (inVisibleMap) {
                    List<Integer> windowIdList3 = this.mVisibleWindowMap.get(Integer.valueOf(uid));
                    if (windowIdList3.contains(windowid)) {
                        windowIdList3.remove(windowid);
                    }
                    if (windowIdList3.size() > 0) {
                        this.mVisibleWindowMap.put(Integer.valueOf(uid), windowIdList3);
                    } else {
                        this.mVisibleWindowMap.remove(Integer.valueOf(uid));
                    }
                }
                if (!this.mVisibleWindowList.isEmpty() || !this.mVisibleWindowMap.isEmpty()) {
                    int i = 0;
                    if (this.mVisibleWindowList.size() == this.mVisibleWindowMap.size()) {
                        ArrayList<Integer> tmpList = new ArrayList<>();
                        boolean diff = LOG_DEBUG;
                        while (true) {
                            if (i >= this.mVisibleWindowMap.size()) {
                                break;
                            }
                            Integer windowUid = this.mVisibleWindowMap.keyAt(i);
                            tmpList.add(windowUid);
                            if (0 == 0 && !this.mVisibleWindowList.contains(windowUid)) {
                                diff = true;
                                break;
                            }
                            i++;
                        }
                        if (diff) {
                            this.mVisibleWindowList.clear();
                            this.mVisibleWindowList.addAll(tmpList);
                            isNotify = true;
                        }
                    } else {
                        this.mVisibleWindowList.clear();
                        while (i < this.mVisibleWindowMap.size()) {
                            this.mVisibleWindowList.add(this.mVisibleWindowMap.keyAt(i));
                            i++;
                        }
                        isNotify = true;
                    }
                }
            }
        }
    }

    public boolean isFloatWindow(int uid) {
        boolean contains;
        synchronized (this.mVisibleWindowMap) {
            contains = this.mVisibleWindowList.contains(Integer.valueOf(uid));
        }
        return contains;
    }

    private String getDefaultInputMethod() {
        String defaultInput = null;
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            try {
                String inputMethod = Settings.Secure.getStringForUser(activityManagerService.mContext.getContentResolver(), "default_input_method", this.mAms.getCurrentUser().id);
                if (inputMethod != null) {
                    defaultInput = inputMethod.substring(0, inputMethod.indexOf("/"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to get default input method");
            }
        }
        if (LOG_DEBUG) {
            Log.i(TAG, "defaultInputMethod " + defaultInput);
        }
        return defaultInput;
    }

    public List<String> getNavigationList() {
        if (this.mLocationManager == null) {
            this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        }
        List<String> list = new ArrayList<>();
        try {
            List<String> gpsList = (List) callMethodByReflect(this.mLocationManager, "getInUsePackagesList");
            if (gpsList != null) {
                list.addAll(gpsList);
            }
        } catch (Exception e) {
            Log.e(TAG, "getNavigationList faild e " + e);
        }
        return list;
    }

    public List<Integer> getAudioFocus() {
        ProcessRecord proc;
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        }
        List<Integer> pidList = new ArrayList<>();
        try {
            String audioPids = this.mAudioManager.getParameters("get_pid");
            if (audioPids != null) {
                StringTokenizer token = new StringTokenizer(audioPids, ":");
                while (token.hasMoreTokens()) {
                    pidList.add(Integer.valueOf(Integer.valueOf(token.nextToken()).intValue()));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getAudioFocus faild e " + e);
        }
        List<Integer> uidList = new ArrayList<>();
        if (!pidList.isEmpty()) {
            for (int i = 0; i < pidList.size(); i++) {
                synchronized (this.mAms.mPidsSelfLocked) {
                    proc = this.mAms.mPidsSelfLocked.get(pidList.get(i).intValue());
                }
                if (proc != null) {
                    try {
                        uidList.add(Integer.valueOf(proc.uid));
                        if (proc.connections.size() > 0) {
                            Iterator it = proc.connections.iterator();
                            while (it.hasNext()) {
                                ConnectionRecord cr = (ConnectionRecord) it.next();
                                if (cr.binding.service.app != null) {
                                    if (cr.binding.service.appInfo != null) {
                                        String pkg = cr.binding.service.packageName;
                                        int uid = cr.binding.service.app.uid;
                                        if (pkg != null) {
                                            if (!uidList.contains(Integer.valueOf(uid))) {
                                                uidList.add(Integer.valueOf(uid));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e2) {
                        Log.e(TAG, "audioFocus faild " + e2);
                    }
                }
            }
        }
        return uidList;
    }

    private void saveWidgetFile(String cfg, String pkgName, int uid, boolean delete) {
        if (CONFIG_WIDGET.equals(cfg) && ensureCreateFile(WIDGET_SAVE_FILE_PATH)) {
            String str = uid + "#" + pkgName;
            ArrayList<String> list = readWidgetFile();
            if (!delete) {
                list.add(str);
                writeWidgetFile(list);
            } else if (list.contains(str)) {
                list.remove(str);
                writeWidgetFile(list);
            }
        }
    }

    private boolean ensureCreateFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return LOG_DEBUG;
        }
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "ensureCreateFile " + path + " : " + e);
            return LOG_DEBUG;
        }
    }

    private void initWidgetMap() {
        FileReader fr = null;
        BufferedReader reader = null;
        try {
            File file = new File(WIDGET_SAVE_FILE_PATH);
            if (!file.exists()) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fr != null) {
                    try {
                        fr.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            } else {
                FileReader fr2 = new FileReader(file);
                BufferedReader reader2 = new BufferedReader(fr2);
                while (true) {
                    String str = reader2.readLine();
                    if (str != null) {
                        String[] values = str.split("#");
                        if (values != null && values.length == 2) {
                            addConfigInfoList(values[1], Integer.parseInt(values[0]), CONFIG_WIDGET);
                        }
                    } else {
                        try {
                            break;
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                }
                reader2.close();
                try {
                    fr2.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
        } catch (Exception e5) {
            Log.e(TAG, "associateStartFile read execption: " + e5);
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            if (fr != null) {
                fr.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e8) {
                    e8.printStackTrace();
                }
            }
            throw th;
        }
    }

    private ArrayList<String> readWidgetFile() {
        ArrayList<String> list = new ArrayList<>();
        FileReader fr = null;
        BufferedReader reader = null;
        try {
            FileReader fr2 = new FileReader(new File(WIDGET_SAVE_FILE_PATH));
            BufferedReader reader2 = new BufferedReader(fr2);
            while (true) {
                String str = reader2.readLine();
                if (str != null) {
                    list.add(str.trim());
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            reader2.close();
            try {
                fr2.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        } catch (Exception e3) {
            Log.e(TAG, "associateStartFile read execption: " + e3);
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            if (fr != null) {
                fr.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            throw th;
        }
        return list;
    }

    private void writeWidgetFile(ArrayList<String> list) {
        FileOutputStream fos = null;
        try {
            FileOutputStream fos2 = new FileOutputStream(new File(WIDGET_SAVE_FILE_PATH));
            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                fos2.write((iterator.next() + "\n").getBytes());
            }
            try {
                fos2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            if (fos != null) {
                fos.close();
            }
        } catch (Throwable th) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
    }

    public List<Integer> getBluetoothList() {
        List<Integer> list = new ArrayList<>();
        try {
            for (String prop : BLUETOOTH_UID_PROP) {
                String uidString = SystemProperties.get(prop, "");
                if (!TextUtils.isEmpty(uidString)) {
                    for (String uid : uidString.split(":")) {
                        list.add(Integer.valueOf(Integer.parseInt(uid)));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getBluetoothList execption: " + e);
        }
        return list;
    }

    public boolean isProtectedByTraffic(long usedTime, int uid, boolean isFgService, boolean isScreenOn, long screenOffFreezeTime, int lastResumedUid) {
        if (this.mTracker == null) {
            this.mTracker = ColorAppDownloadTracker.getInstance(this.mContext);
        }
        ColorAppDownloadTracker colorAppDownloadTracker = this.mTracker;
        if (colorAppDownloadTracker == null) {
            return LOG_DEBUG;
        }
        long trafficUpdateTime = colorAppDownloadTracker.getTrafficUpdateTime();
        if (trafficUpdateTime == 0 || usedTime <= trafficUpdateTime) {
            if (trafficUpdateTime != 0 && !isScreenOn && screenOffFreezeTime > trafficUpdateTime) {
                if (uid == lastResumedUid) {
                    Log.d(TAG, "uid:" + uid + " is Protected by traffic because of topActivity");
                    return true;
                }
            }
            if (!isFgService || !this.mTracker.isDownloadingApp(uid)) {
                return LOG_DEBUG;
            }
            Log.d(TAG, "uid:" + uid + " is Protected by traffic because of downloading");
            return true;
        }
        Log.d(TAG, "uid:" + uid + " is Protected by traffic because of trafficUpdateTime");
        return true;
    }

    /* access modifiers changed from: private */
    public void updateAODValue() {
        boolean z = true;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), AodUserSetEnable, 0, this.mAms.getCurrentUser().id) != 1) {
            z = false;
        }
        this.isAodEnable = z;
    }

    public boolean isAodEnable() {
        return this.isAodEnable;
    }

    private void dispatchAppChanged(int uid, String pkgName, String cfgName, boolean isAdd) {
        synchronized (this.mAppChangedListenerList) {
            Iterator<AppChangedListener> it = this.mAppChangedListenerList.iterator();
            while (it.hasNext()) {
                it.next().onChanged(uid, pkgName, cfgName, isAdd);
            }
        }
    }

    public void addAppChangedListener(AppChangedListener listener) {
        synchronized (this.mAppChangedListenerList) {
            this.mAppChangedListenerList.add(listener);
        }
    }

    public void removeAppChangedListener(AppChangedListener listener) {
        synchronized (this.mAppChangedListenerList) {
            this.mAppChangedListenerList.remove(listener);
        }
    }

    public void dumpCommonMap() {
        try {
            Log.d(TAG, "CommonList= " + this.mCommonListMap);
            Log.d(TAG, "FloatWindows=" + this.mVisibleWindowList);
        } catch (Exception e) {
            Log.e(TAG, "dumpCommonMap err " + e);
        }
    }

    public void openDynamicLog() {
        this.mLogDynamic = true;
    }

    public List<ColorPackageFreezeData> getRunningProcesses() {
        List<ColorPackageFreezeData> appFreezeList = new ArrayList<>();
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            synchronized (activityManagerService) {
                try {
                    for (int i = this.mAms.mProcessList.mLruProcesses.size() - 1; i >= 0; i--) {
                        ProcessRecord app = (ProcessRecord) this.mAms.mProcessList.mLruProcesses.get(i);
                        if (app != null) {
                            ColorPackageFreezeData data = new ColorPackageFreezeData();
                            data.setPid(app.pid);
                            data.setUid(app.uid);
                            data.setCurAdj(app.curAdj);
                            data.setUserId(app.userId);
                            data.setProcessName(app.processName);
                            String[] pkgList = app.getPackageList();
                            if (pkgList != null) {
                                data.setPackageList(Arrays.asList(pkgList));
                            } else {
                                data.setPackageList((List) null);
                            }
                            appFreezeList.add(data);
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        return appFreezeList;
    }

    private Object callMethodByReflect(Object object, String methodName) {
        try {
            Method method = object.getClass().getDeclaredMethod(methodName, new Class[0]);
            method.setAccessible(true);
            return method.invoke(object, new Object[0]);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
