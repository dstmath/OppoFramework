package com.android.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStatsManagerInternal;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.Xml;
import com.android.server.am.ColorAppCrashClearManager;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.pm.IColorFullmodeManager;
import com.android.server.power.OppoPowerManagerInternal;
import com.android.server.storage.ColorDeviceStorageMonitorService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ColorDeviceIdleHelper implements IColorDeviceIdleHelper {
    private static final String ACTION_MODIFY_POWERSAVE_WHITELIST = "coloros.intent.action.MODIFY_POWERSAVE_WHITELIST_CUSTOM";
    private static final String ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String ACTION_SMART_DOZE_CHANGE = "oppo.intent.action_GUARDELF_SMART_DOZE_CHANGE";
    private static final String ACTION_UPLOAD_DEVICEIDLE_COUNT = "coloros.intent.action.UPLOAD_DEVICEIDLE_COUNT";
    public static final long ALARM_WINDOW_LENGTH = 180000;
    private static final String ALLOW_PROHIBIT_APP_FILE = "/data/oppo/coloros/oppoguardelf/allow_prohibit_app.xml";
    public static final int ANY_MOTION = 2;
    private static final String AUTO_POWER_PROTECT_SWITCH = "auto_power_protect_state";
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final Uri CONTENT_URI_WHITE_LIST = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final String CUSTOM_DOZE_WHITELIST_XML_PATH = "/data/oppo/coloros/oppoguardelf/custom_doze_whitelist.xml";
    public static final long DEFAULT_TOTAL_INTERVAL_TO_IDLE = 1800000;
    private static final String DOZE_CONFIG_XML_PATH = "/data/system/doze_config_local.xml";
    private static final String FILTER_NAME = "sys_deviceidle_whitelist";
    private static final long IDLE_PENDING_TIMEOUT = 300000;
    private static final long IDLE_TIMEOUT = 3600000;
    private static final String KEY_MODIFY_POWERSAVE_WHITELIST_OP = "op";
    private static final String KEY_MODIFY_POWERSAVE_WHITELIST_OP_ADD = "add";
    private static final String KEY_MODIFY_POWERSAVE_WHITELIST_OP_REMOVE = "remove";
    private static final String KEY_MODIFY_POWERSAVE_WHITELIST_PKGS = "packages";
    public static final int LOCATION = 3;
    private static final long MIN_SCREEN_ON_DURATION = 30000;
    private static final int MSG_DEEP_IDLE_START = 2;
    private static final int MSG_IDLE_EXIT = 4;
    private static final int MSG_LIGHT_IDLE_START = 3;
    private static final int MSG_MOTION_DETECT = 7;
    private static final int MSG_SCREEN_ON = 6;
    private static final int MSG_WHITELIST_UPDATE = 1;
    private static final String OPPO_CUSTOMIZE_WHITE_FILE_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final String SAVED_WHITELIST_XML_PATH = "/data/oppo/coloros/oppoguardelf/doze_wl_local.xml";
    private static final String SAVED_WHITE_LIST_USER_SET_XML_PATH = "/data/oppo/coloros/oppoguardelf/doze_wl_user_set_local.xml";
    public static final int SIGNIFICANT_MOTION = 1;
    private static final String SMART_DOZE_CONFIG_XML_PATH = "/data/oppo/coloros/oppoguardelf/smart_doze_config_local.xml";
    private static final String SMART_DOZE_REASON = "smartdoze";
    public static final int STATE_ACTIVE = 0;
    public static final int STATE_IDLE = 5;
    public static final int STATE_IDLE_MAINTENANCE = 6;
    public static final int STATE_IDLE_PENDING = 2;
    public static final int STATE_LOCATING = 4;
    public static final int STATE_SENSING = 3;
    private static final String SYSTEM_XML_PATH = "/system/oppo/sys_deviceidle_whitelist.xml";
    private static final String TAG = "DeviceIdleHelper";
    private static final String TAG_ALLOW_BACK_RUN = "allow";
    private static final String TAG_ALL_WHITE_LIST = "wl";
    public static final String TAG_AUTO_POWER_SAVE_MODES_ENABLED = "auto_power_save_enable";
    private static final String TAG_PROHIBIT_BACK_RUN = "prohibit";
    private static final String TAG_TOTAL_INTERVAL_TO_IDLE = "total_interval_idle";
    static final long THRESH_SPEED = 10;
    private static final int TIME_DELAY = 2000;
    private static final int TIME_ONE_DAY = 86400000;
    private static final int TIME_ONE_SECOND = 1000;
    private static ColorDeviceIdleHelper mInstance = null;
    private static boolean sOppoDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private AlarmManager mAlarmManager;
    private volatile boolean mAutoPowerModesEnabled = true;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.ColorDeviceIdleHelper.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            ArrayList<String> packages;
            Uri data;
            String pkg;
            String action = intent.getAction();
            if (ColorDeviceIdleHelper.ACTION_ROM_UPDATE_CONFIG_SUCCES.equals(action)) {
                ArrayList<String> changeList = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST");
                if (changeList != null && changeList.contains(ColorDeviceIdleHelper.FILTER_NAME)) {
                    Slog.d(ColorDeviceIdleHelper.TAG, "ACTION_ROM_UPDATE_CONFIG_SUCCES");
                    ColorDeviceIdleHelper.this.updateWhiteList();
                }
            } else if (ColorDeviceIdleHelper.ACTION_UPLOAD_DEVICEIDLE_COUNT.equals(action)) {
                ColorDeviceIdleHelper.this.mDeviceIdleStatistics.uploadDeviceIdleCount();
                ColorDeviceIdleHelper.this.scheduleUploadAlarm();
            } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false) && (data = intent.getData()) != null && (pkg = data.getSchemeSpecificPart()) != null && ColorDeviceIdleHelper.this.mWhiteListAll.contains(pkg)) {
                    ColorDeviceIdleHelper.this.mDeviceIdleController.addPowerSaveWhitelistAppInternal(pkg);
                    if (ColorDeviceIdleHelper.sOppoDebug) {
                        Slog.d(ColorDeviceIdleHelper.TAG, " ACTION_PACKAGE_ADDED, addPowerSaveWhitelistAppInternal pkg=" + pkg);
                    }
                }
            } else if (ColorDeviceIdleHelper.ACTION_MODIFY_POWERSAVE_WHITELIST.equals(action) && ColorDeviceIdleHelper.this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom") && (packages = intent.getStringArrayListExtra(ColorDeviceIdleHelper.KEY_MODIFY_POWERSAVE_WHITELIST_PKGS)) != null && packages.size() > 0) {
                boolean isChanged = false;
                synchronized (ColorDeviceIdleHelper.this.mLock) {
                    String op = intent.getStringExtra(ColorDeviceIdleHelper.KEY_MODIFY_POWERSAVE_WHITELIST_OP);
                    if (ColorDeviceIdleHelper.KEY_MODIFY_POWERSAVE_WHITELIST_OP_ADD.equals(op)) {
                        Iterator<String> it = packages.iterator();
                        while (it.hasNext()) {
                            String pkg2 = it.next();
                            if (!ColorDeviceIdleHelper.this.mCustomDozeWhiteList.contains(pkg2)) {
                                isChanged = true;
                                ColorDeviceIdleHelper.this.mCustomDozeWhiteList.add(pkg2);
                                Slog.d(ColorDeviceIdleHelper.TAG, "custom whiteListChanged: add new pkg (" + pkg2 + ")");
                            }
                        }
                    } else if (ColorDeviceIdleHelper.KEY_MODIFY_POWERSAVE_WHITELIST_OP_REMOVE.equals(op)) {
                        Iterator<String> it2 = packages.iterator();
                        while (it2.hasNext()) {
                            String pkg3 = it2.next();
                            if (ColorDeviceIdleHelper.this.mCustomDozeWhiteList.contains(pkg3)) {
                                isChanged = true;
                                ColorDeviceIdleHelper.this.mCustomDozeWhiteList.remove(pkg3);
                                Slog.d(ColorDeviceIdleHelper.TAG, "custom whiteListChanged: remove old pkg (" + pkg3 + ")");
                            }
                        }
                    }
                    if (isChanged) {
                        ColorDeviceIdleHelper.this.saveCustomWhiteListToFileLocked(ColorDeviceIdleHelper.CUSTOM_DOZE_WHITELIST_XML_PATH, ColorDeviceIdleHelper.this.mCustomDozeWhiteList);
                    }
                }
                if (isChanged) {
                    ColorDeviceIdleHelper.this.updateWhiteList();
                }
            }
        }
    };
    private ContentObserver mConstants;
    private Context mContext;
    private List<String> mCustomDozeWhiteList = new ArrayList();
    private DeviceIdleController mDeviceIdleController;
    private DeviceIdleStatistics mDeviceIdleStatistics;
    private WorkerHandler mHandler;
    private IColorDeviceIdleControllerInner mInner;
    private boolean mIsDeepIdleEntered = false;
    private boolean mIsInited = false;
    private boolean mIsLightIdleEntered = false;
    TrafficRecord mLastDeepTrafficRecord;
    private boolean mLastEnteredDeepSleep;
    TrafficRecord mLastLightTrafficRecord;
    private long mLastWakeupTime = Long.MIN_VALUE;
    private PowerManagerInternal mLocalPowerManager;
    private final Object mLock = new Object();
    private OppoPowerManagerInternal mOppoLocalPowerManager;
    private final ArrayMap<String, Integer> mPSWhitelistUserApps = new ArrayMap<>();
    private PackageManager mPackageManager;
    private PowerManager mPowerManager;
    SmartDozeHelperInner mSmartDozeHelperInner = null;
    private long mTimestampScreenoff;
    private long mTotalIntervalToIdle = DEFAULT_TOTAL_INTERVAL_TO_IDLE;
    private PendingIntent mUploadIntent;
    private UsageStatsManagerInternal mUsageStats;
    private PowerManager.WakeLock mWakelock;
    private ArrayList<String> mWhiteListAll = new ArrayList<>();

    private ColorDeviceIdleHelper() {
    }

    public static ColorDeviceIdleHelper getInstance() {
        if (mInstance == null) {
            synchronized (ColorDeviceIdleHelper.class) {
                if (mInstance == null) {
                    mInstance = new ColorDeviceIdleHelper();
                }
            }
        }
        return mInstance;
    }

    public void initArgs(Context context, DeviceIdleController controller, ContentObserver constants, IColorDeviceIdleControllerInner inner) {
        this.mContext = context;
        this.mDeviceIdleController = controller;
        this.mConstants = constants;
        this.mInner = inner;
        this.mDeviceIdleStatistics = new DeviceIdleStatistics();
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mWakelock = this.mPowerManager.newWakeLock(1, TAG);
        this.mWakelock.setReferenceCounted(false);
        HandlerThread hd = new HandlerThread(TAG);
        hd.start();
        this.mHandler = new WorkerHandler(hd.getLooper());
        this.mIsInited = true;
        this.mUploadIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_UPLOAD_DEVICEIDLE_COUNT), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ROM_UPDATE_CONFIG_SUCCES);
        filter.addAction(ACTION_UPLOAD_DEVICEIDLE_COUNT);
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            filter.addAction(ACTION_MODIFY_POWERSAVE_WHITELIST);
        }
        context.registerReceiver(this.mBroadcastReceiver, filter, "oppo.permission.OPPO_COMPONENT_SAFE", this.mHandler);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.PACKAGE_ADDED");
        filter2.addDataScheme(BrightnessConstants.AppSplineXml.TAG_PACKAGE);
        context.registerReceiver(this.mBroadcastReceiver, filter2, null, this.mHandler);
        this.mHandler.sendEmptyMessageDelayed(1, 2000);
        scheduleUploadAlarm();
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            readCustomWhiteListToFileLocked(CUSTOM_DOZE_WHITELIST_XML_PATH, this.mCustomDozeWhiteList);
        }
        int dozeLocalConfig = getLocalDozeCofigLocked();
        if (dozeLocalConfig == 1) {
            this.mAutoPowerModesEnabled = true;
        } else if (dozeLocalConfig == 0) {
            this.mAutoPowerModesEnabled = false;
        } else {
            this.mAutoPowerModesEnabled = context.getResources().getBoolean(17891433);
        }
        Slog.d(TAG, "init. dozeLocalConfig=" + dozeLocalConfig + ", mAutoPowerModesEnabled=" + this.mAutoPowerModesEnabled);
        this.mUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        this.mLocalPowerManager = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mOppoLocalPowerManager = (OppoPowerManagerInternal) LocalServices.getService(OppoPowerManagerInternal.class);
        if (OppoFeatureCache.get(IColorSmartDozeHelper.DEFAULT).isSupportSmartDoze()) {
            this.mSmartDozeHelperInner = new SmartDozeHelperInner();
        }
    }

    public boolean isAutoPowerModesEnabled() {
        return this.mAutoPowerModesEnabled;
    }

    public long getTotalIntervalToIdle() {
        return this.mTotalIntervalToIdle;
    }

    public void onMotionDetected(int state, int typeMotion) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 7;
        Bundle data = new Bundle();
        data.putInt("state", state);
        data.putInt("typeMotion", typeMotion);
        msg.setData(data);
        this.mHandler.sendMessage(msg);
    }

    public void onDeepIdleOn(ArrayList<String> listPowerSaveUser) {
        if (!this.mIsDeepIdleEntered) {
            this.mIsDeepIdleEntered = true;
            Message msg = this.mHandler.obtainMessage();
            msg.what = 2;
            Bundle data = new Bundle();
            data.putStringArrayList("list", listPowerSaveUser);
            msg.setData(data);
            this.mHandler.sendMessage(msg);
        }
        this.mLastEnteredDeepSleep = true;
    }

    public void onLightIdleOn(ArrayList<String> listPowerSaveUser) {
        if (!this.mIsLightIdleEntered) {
            this.mIsLightIdleEntered = true;
            Message msg = this.mHandler.obtainMessage();
            msg.what = 3;
            Bundle data = new Bundle();
            data.putStringArrayList("list", listPowerSaveUser);
            msg.setData(data);
            this.mHandler.sendMessage(msg);
        }
    }

    public void onIdleExit() {
        if (this.mIsLightIdleEntered || this.mIsDeepIdleEntered) {
            this.mHandler.sendEmptyMessage(4);
        }
        this.mIsLightIdleEntered = false;
        this.mIsDeepIdleEntered = false;
        if (this.mSmartDozeHelperInner != null && OppoFeatureCache.get(IColorSmartDozeHelper.DEFAULT).isSupportSmartDoze()) {
            this.mSmartDozeHelperInner.setSmartDozeMode(false);
        }
    }

    public boolean onScreenOff() {
        long nowElapsed = SystemClock.elapsedRealtime();
        this.mTimestampScreenoff = nowElapsed;
        boolean shouldQuick = false;
        if (nowElapsed - this.mLastWakeupTime < 30000 && this.mLastEnteredDeepSleep) {
            String shortScreenOnStatus = this.mOppoLocalPowerManager.getShortScreenOnStatus();
            if ("shortSreenOn".equals(shortScreenOnStatus)) {
                shouldQuick = true;
            }
            uploadQuickEnterIdle(shortScreenOnStatus, this.mOppoLocalPowerManager.getScreenOnReason(), this.mOppoLocalPowerManager.getSleepReason());
        }
        this.mLastEnteredDeepSleep = false;
        return shouldQuick;
    }

    public void onScreenOn() {
        this.mHandler.sendEmptyMessage(6);
        this.mLastWakeupTime = SystemClock.elapsedRealtime();
    }

    /* access modifiers changed from: private */
    public class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            ColorDeviceIdleHelper.this.mWakelock.acquire();
            if (msg.what == 1) {
                if (ColorDeviceIdleHelper.sOppoDebug) {
                    Slog.d(ColorDeviceIdleHelper.TAG, "MSG_WHITELIST_UPDATE");
                }
                ColorDeviceIdleHelper.this.updateWhiteList();
            } else if (msg.what == 3) {
                ColorDeviceIdleHelper.this.reconfirmWhiteList(msg.getData());
                ColorDeviceIdleHelper.this.mDeviceIdleStatistics.onLightIdleStart();
            } else if (msg.what == 2) {
                ColorDeviceIdleHelper.this.reconfirmWhiteList(msg.getData());
                ColorDeviceIdleHelper.this.mDeviceIdleStatistics.onDeepIdleStart();
            } else if (msg.what == 4) {
                ColorDeviceIdleHelper.this.mDeviceIdleStatistics.onIdleExit();
            } else if (msg.what == 6) {
                ColorDeviceIdleHelper.this.mDeviceIdleStatistics.uploadDeviceIdleStatistics();
                ColorDeviceIdleHelper.this.mDeviceIdleStatistics.reset();
            } else if (msg.what == 7) {
                if (ColorDeviceIdleHelper.sOppoDebug) {
                    Slog.d(ColorDeviceIdleHelper.TAG, "MSG_MOTION_DETECT");
                }
                ColorDeviceIdleHelper.this.mDeviceIdleStatistics.onMotionDetected(msg.getData());
            }
            ColorDeviceIdleHelper.this.mWakelock.release();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reconfirmWhiteList(Bundle data) {
        synchronized (this.mLock) {
            if (data != null) {
                ArrayList<String> whiteListSetByUser = getLocalSavedWhiteListSetByUserLocked();
                ArrayList<String> listPowerSaveUser = data.getStringArrayList("list");
                if (listPowerSaveUser != null) {
                    boolean isAutoPowerProtectOn = isAutoPowerProtectOn(this.mContext);
                    List<String> listAllow = new ArrayList<>();
                    List<String> listProhibit = new ArrayList<>();
                    if (!isAutoPowerProtectOn) {
                        getAllowProhibitList(listAllow, listProhibit);
                    }
                    for (int i = 0; i < listPowerSaveUser.size(); i++) {
                        String pkgName = listPowerSaveUser.get(i);
                        if (this.mSmartDozeHelperInner != null && this.mSmartDozeHelperInner.getSmartWhiteLists() != null && this.mSmartDozeHelperInner.getSmartWhiteLists().contains(pkgName) && OppoFeatureCache.get(IColorSmartDozeHelper.DEFAULT).isInSmartDozeMode()) {
                            if (sOppoDebug) {
                                Slog.d(TAG, "in smart doze, do not remove whitelist, pkg = " + pkgName);
                            }
                            if (!isAutoPowerProtectOn) {
                                if (!listProhibit.contains(pkgName)) {
                                }
                            }
                        }
                        if (!isClosedSuperFirewall(this.mContext.getPackageManager())) {
                            if (isAutoPowerProtectOn || !listAllow.contains(pkgName)) {
                                if (!this.mWhiteListAll.contains(pkgName) && !whiteListSetByUser.contains(pkgName)) {
                                    this.mDeviceIdleController.removePowerSaveWhitelistAppInternal(pkgName);
                                    if (sOppoDebug) {
                                        Slog.d(TAG, "reconfirmWhiteList: remove from DeviceIdleController: " + pkgName);
                                    }
                                }
                            } else if (sOppoDebug) {
                                Slog.d(TAG, "reconfirmWhiteList: is allow. not remove " + pkgName);
                            }
                        }
                    }
                    for (int i2 = 0; i2 < this.mWhiteListAll.size(); i2++) {
                        String pkgName2 = this.mWhiteListAll.get(i2);
                        if (pkgName2 != null) {
                            if (isAutoPowerProtectOn || !listProhibit.contains(pkgName2) || this.mCustomDozeWhiteList.contains(pkgName2)) {
                                if (!this.mDeviceIdleController.isPowerSaveWhitelistAppInternal(pkgName2) && this.mDeviceIdleController.addPowerSaveWhitelistAppInternal(pkgName2) && sOppoDebug) {
                                    Slog.d(TAG, "reconfirmWhiteList: addPowerSaveWhitelist " + pkgName2);
                                }
                            } else if (sOppoDebug) {
                                Slog.d(TAG, "reconfirmWhiteList: prohibit pkg=" + pkgName2);
                            }
                        }
                    }
                }
            }
        }
    }

    private ArrayList<String> getLocalSavedWhiteListSetByUserLocked() {
        ArrayList<String> list = new ArrayList<>();
        File file = new File(SAVED_WHITE_LIST_USER_SET_XML_PATH);
        if (!file.exists()) {
            return list;
        }
        FileReader xmlReader = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            xmlReader = new FileReader(file);
            parser.setInput(xmlReader);
            parseUserSetXml(parser, list);
            try {
                xmlReader.close();
            } catch (IOException e) {
                Slog.w(TAG, "getLocalSavedWhiteListSetByUserLocked: Got exception close xmlReader. ", e);
            }
        } catch (Exception e2) {
            Slog.w(TAG, "getLocalSavedWhiteListSetByUserLocked: Got exception. ", e2);
            if (xmlReader != null) {
                xmlReader.close();
            }
        } catch (Throwable th) {
            if (xmlReader != null) {
                try {
                    xmlReader.close();
                } catch (IOException e3) {
                    Slog.w(TAG, "getLocalSavedWhiteListSetByUserLocked: Got exception close xmlReader. ", e3);
                }
            }
            throw th;
        }
        return list;
    }

    private void parseUserSetXml(XmlPullParser parser, ArrayList<String> list) {
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String strName = parser.getName();
                        parser.next();
                        String strText = parser.getText();
                        if (TAG_ALL_WHITE_LIST.equals(strName) && !list.contains(strText)) {
                            list.add(strText);
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Slog.w(TAG, "parseXml: Got exception. ", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleUploadAlarm() {
        this.mAlarmManager.setExact(3, SystemClock.elapsedRealtime() + 86400000, this.mUploadIntent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateWhiteList() {
        synchronized (this.mLock) {
            ArrayList<String> whiteListAll = new ArrayList<>();
            getNewWhiteListLocked(whiteListAll);
            getLocalSavedWhiteListLocked();
            if (whiteListChangedHandle(whiteListAll)) {
                saveLocalWhiteListLocked();
            }
            this.mConstants.onChange(true, null);
        }
    }

    private void getNewWhiteListLocked(ArrayList<String> whiteListAll) {
        getListFromProvider(whiteListAll);
        getListFromSystem(whiteListAll);
        getCustomizeWhiteList(whiteListAll);
        addNfcJapanFelica(whiteListAll);
        addUqMoble(whiteListAll);
        addJpSoftBank(whiteListAll);
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            whiteListAll.addAll(this.mCustomDozeWhiteList);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0051, code lost:
        if (0 == 0) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0054, code lost:
        if (r5 != null) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0056, code lost:
        android.util.Slog.w(com.android.server.ColorDeviceIdleHelper.TAG, "getDataFromProvider: failed");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005b, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005c, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r2 = android.util.Xml.newPullParser();
        r1 = new java.io.StringReader(r5);
        r2.setInput(r1);
        parseXml(r2, r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x006e, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0072, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0074, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        android.util.Slog.w(com.android.server.ColorDeviceIdleHelper.TAG, "getDataFromProvider: Got execption. ", r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0079, code lost:
        if (r1 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007d, code lost:
        if (r1 != null) goto L_0x007f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x007f, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0082, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0037, code lost:
        if (r4 != null) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0039, code lost:
        r4.close();
     */
    private void getListFromProvider(ArrayList<String> whiteListAll) {
        Cursor cursor = null;
        String strWhiteList = null;
        try {
            cursor = this.mContext.getContentResolver().query(CONTENT_URI_WHITE_LIST, new String[]{"version", COLUMN_NAME_2}, "filtername=\"sys_deviceidle_whitelist\"", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.getColumnIndex("version");
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_2);
                cursor.moveToNext();
                strWhiteList = cursor.getString(xmlcolumnIndex);
            }
        } catch (Exception e) {
            Slog.w(TAG, "getDataFromProvider: Got execption. " + e);
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    private void getListFromSystem(ArrayList<String> whiteListAll) {
        if (!whiteListAll.isEmpty()) {
            Slog.d(TAG, "getDataFromSystem: no need");
            return;
        }
        File file = new File(SYSTEM_XML_PATH);
        if (file.exists()) {
            FileReader xmlReader = null;
            ArrayList<String> listAll = new ArrayList<>();
            try {
                XmlPullParser parser = Xml.newPullParser();
                xmlReader = new FileReader(file);
                parser.setInput(xmlReader);
                parseXml(parser, listAll);
                try {
                    xmlReader.close();
                } catch (IOException e) {
                    Slog.w(TAG, "getListFromSystem: Got execption close xmlReader. ", e);
                }
            } catch (Exception e2) {
                Slog.w(TAG, "getListFromSystem: Got execption. ", e2);
                if (xmlReader != null) {
                    xmlReader.close();
                }
            } catch (Throwable th) {
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e3) {
                        Slog.w(TAG, "getListFromSystem: Got execption close xmlReader. ", e3);
                    }
                }
                throw th;
            }
            if (!listAll.isEmpty() && whiteListAll.isEmpty()) {
                whiteListAll.addAll(listAll);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void parseXml(XmlPullParser parser, ArrayList<String> listAll) {
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String strName = parser.getName();
                        parser.next();
                        String strText = parser.getText();
                        if (TAG_ALL_WHITE_LIST.equals(strName)) {
                            if (!listAll.contains(strText)) {
                                listAll.add(strText);
                            }
                        } else if (TAG_TOTAL_INTERVAL_TO_IDLE.equals(strName)) {
                            try {
                                this.mTotalIntervalToIdle = Long.parseLong(strText) * 1000;
                            } catch (NumberFormatException e) {
                                this.mTotalIntervalToIdle = DEFAULT_TOTAL_INTERVAL_TO_IDLE;
                                Slog.w(TAG, "IntervalToIdle excption.", e);
                            }
                            if (sOppoDebug) {
                                Slog.d(TAG, "interval to idle:" + this.mTotalIntervalToIdle);
                            }
                        } else if (TAG_AUTO_POWER_SAVE_MODES_ENABLED.equals(strName)) {
                            try {
                                boolean powerModesEnabled = Boolean.parseBoolean(strText);
                                if (powerModesEnabled != this.mAutoPowerModesEnabled) {
                                    this.mAutoPowerModesEnabled = powerModesEnabled;
                                    saveDozeCofigLocked();
                                }
                            } catch (NumberFormatException e2) {
                                Slog.w(TAG, "auto power save excption.", e2);
                            }
                            if (sOppoDebug) {
                                Slog.d(TAG, "auto power save:" + this.mAutoPowerModesEnabled);
                            }
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e3) {
            Slog.w(TAG, "parseXml: Got execption. ", e3);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x009f  */
    private void getCustomizeWhiteList(ArrayList<String> whiteListAll) {
        ArrayList<String> listPkg;
        int i;
        StringBuilder sb;
        int type;
        String pkgName;
        if (this.mContext.getPackageManager() != null && this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            File file = new File(OPPO_CUSTOMIZE_WHITE_FILE_PATH);
            if (file.exists()) {
                listPkg = new ArrayList<>();
                FileReader xmlReader = null;
                try {
                    FileReader xmlReader2 = new FileReader(file);
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(xmlReader2);
                    do {
                        type = parser.next();
                        if (type == 2 && ColorAppCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName()) && (pkgName = parser.getAttributeValue(null, "att")) != null) {
                            listPkg.add(pkgName);
                        }
                    } while (type != 1);
                    try {
                        xmlReader2.close();
                    } catch (IOException e) {
                        e = e;
                        sb = new StringBuilder();
                    }
                } catch (Exception e2) {
                    Slog.e(TAG, "failed parsing ", e2);
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e3) {
                            e = e3;
                            sb = new StringBuilder();
                        }
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e4) {
                            Slog.e(TAG, "Failed to close state FileInputStream " + e4);
                        }
                    }
                    throw th;
                }
                for (i = 0; i < listPkg.size(); i++) {
                    String pkgName2 = listPkg.get(i);
                    if (!whiteListAll.contains(pkgName2)) {
                        whiteListAll.add(pkgName2);
                    }
                }
            } else if (sOppoDebug) {
                Slog.e(TAG, "readCustomizeWhiteList failed: file doesn't exist!");
                return;
            } else {
                return;
            }
        } else {
            return;
        }
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Slog.e(TAG, sb.toString());
        while (i < listPkg.size()) {
        }
    }

    private void addNfcJapanFelica(ArrayList<String> whiteListAll) {
        if (SystemProperties.getBoolean("nfc.support_japan_felica", false)) {
            whiteListAll.add("com.felicanetworks.mfm.main");
            Slog.d(TAG, "addNfcJapanFelica");
        }
    }

    private void addUqMoble(ArrayList<String> whiteListAll) {
        String operator = SystemProperties.get("ro.oppo.operator", "unkown");
        Slog.d(TAG, "operator=" + operator);
        if ("UQMOBILE".equals(operator)) {
            whiteListAll.add("com.access_company.android.nfcommunicator");
            Slog.d(TAG, "addUqMoble");
        }
    }

    private void addJpSoftBank(ArrayList<String> whiteListAll) {
        if (this.mContext.getPackageManager().hasSystemFeature("coloros.customize.guardelf.jp.softbank.keepalive") || this.mContext.getPackageManager().hasSystemFeature("coloros.customize.guardelf.ymobile.keepalive")) {
            whiteListAll.add("com.opposs.marketdemo2");
            whiteListAll.add("com.opposs.marketdemo13");
            whiteListAll.add("jp.softbank.android.mpi.manager");
            whiteListAll.add("jp.softbank.mb.addressbooksync");
            whiteListAll.add("jp.softbank.mb.addressbookdelivery");
            whiteListAll.add("jp.ymobile.android.mpi.manager");
            Slog.d(TAG, "JPSOFTBANK");
        }
    }

    private void getLocalSavedWhiteListLocked() {
        File file = new File(SAVED_WHITELIST_XML_PATH);
        if (file.exists()) {
            FileReader xmlReader = null;
            ArrayList<String> listAll = new ArrayList<>();
            try {
                XmlPullParser parser = Xml.newPullParser();
                xmlReader = new FileReader(file);
                parser.setInput(xmlReader);
                parseXml(parser, listAll);
                try {
                    xmlReader.close();
                } catch (IOException e) {
                    Slog.w(TAG, "getLocalSavedWhiteList: Got execption close xmlReader. ", e);
                }
            } catch (Exception e2) {
                Slog.w(TAG, "getLocalSavedWhiteList: Got execption. ", e2);
                if (xmlReader != null) {
                    xmlReader.close();
                }
            } catch (Throwable th) {
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e3) {
                        Slog.w(TAG, "getLocalSavedWhiteList: Got execption close xmlReader. ", e3);
                    }
                }
                throw th;
            }
            if (!listAll.isEmpty()) {
                this.mWhiteListAll.addAll(listAll);
            }
        }
    }

    private boolean whiteListChangedHandle(ArrayList<String> whiteListAll) {
        boolean listChanged = false;
        boolean isAutoPowerProtectOn = isAutoPowerProtectOn(this.mContext);
        List<String> listAllow = new ArrayList<>();
        List<String> listProhibit = new ArrayList<>();
        if (!isAutoPowerProtectOn) {
            getAllowProhibitList(listAllow, listProhibit);
        }
        for (int i = 0; i < whiteListAll.size(); i++) {
            String pkgName = whiteListAll.get(i);
            if (pkgName != null) {
                if (!this.mWhiteListAll.contains(pkgName)) {
                    listChanged = true;
                    this.mWhiteListAll.add(pkgName);
                    if (sOppoDebug) {
                        Slog.d(TAG, "whiteListChanged: add new pkg (" + pkgName + ")");
                    }
                }
                if (isAutoPowerProtectOn || !listProhibit.contains(pkgName)) {
                    if (!this.mDeviceIdleController.isPowerSaveWhitelistAppInternal(pkgName) && this.mDeviceIdleController.addPowerSaveWhitelistAppInternal(pkgName) && sOppoDebug) {
                        Slog.d(TAG, "whiteListChanged: addPowerSaveWhitelist " + pkgName);
                    }
                } else if (sOppoDebug) {
                    Slog.d(TAG, "whiteListChanged: is prohibit. not add " + pkgName);
                }
            }
        }
        for (int i2 = this.mWhiteListAll.size() - 1; i2 >= 0; i2--) {
            String pkgName2 = this.mWhiteListAll.get(i2);
            if (pkgName2 != null && !whiteListAll.contains(pkgName2)) {
                listChanged = true;
                this.mWhiteListAll.remove(pkgName2);
                if (sOppoDebug) {
                    Slog.d(TAG, "whiteListChanged: remove old pkg (" + pkgName2 + ") ");
                }
                if (isAutoPowerProtectOn || !listAllow.contains(pkgName2)) {
                    if (this.mDeviceIdleController.isPowerSaveWhitelistAppInternal(pkgName2)) {
                        this.mDeviceIdleController.removePowerSaveWhitelistAppInternal(pkgName2);
                        if (sOppoDebug) {
                            Slog.d(TAG, "whiteListChanged: removePowerSaveWhitelist " + pkgName2);
                        }
                    }
                } else if (sOppoDebug) {
                    Slog.d(TAG, "whiteListChanged: is allow. not remove " + pkgName2);
                }
            }
        }
        return listChanged;
    }

    private void saveLocalWhiteListLocked() {
        StringBuilder sb;
        File file = new File(SAVED_WHITELIST_XML_PATH);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Slog.i(TAG, "saveLocalWhiteListLocked: failed create file /data/oppo/coloros/oppoguardelf/doze_wl_local.xml");
                }
            } catch (IOException e) {
                Slog.i(TAG, "failed create file " + e);
            }
        }
        if (file.exists()) {
            FileOutputStream fileos = null;
            try {
                FileOutputStream fileos2 = new FileOutputStream(file);
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(fileos2, "UTF-8");
                serializer.startDocument(null, true);
                serializer.startTag(null, "gs");
                serializer.startTag(null, "filter-name");
                serializer.text("doze_local");
                serializer.endTag(null, "filter-name");
                for (int i = 0; i < this.mWhiteListAll.size(); i++) {
                    String pkg = this.mWhiteListAll.get(i);
                    if (pkg != null) {
                        serializer.startTag(null, TAG_ALL_WHITE_LIST);
                        serializer.text(pkg);
                        serializer.endTag(null, TAG_ALL_WHITE_LIST);
                    }
                }
                serializer.endTag(null, "gs");
                serializer.endDocument();
                serializer.flush();
                try {
                    fileos2.close();
                    return;
                } catch (IOException e2) {
                    e = e2;
                    sb = new StringBuilder();
                }
            } catch (IllegalArgumentException e3) {
                Slog.i(TAG, "failed write file " + e3);
                if (0 != 0) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e4) {
                        e = e4;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IllegalStateException e5) {
                Slog.i(TAG, "failed write file " + e5);
                if (0 != 0) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e6) {
                        e = e6;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IOException e7) {
                Slog.i(TAG, "failed write file " + e7);
                if (0 != 0) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e8) {
                        e = e8;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Exception e9) {
                Slog.i(TAG, "failed write file " + e9);
                if (0 != 0) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e10) {
                        e = e10;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        fileos.close();
                    } catch (IOException e11) {
                        Slog.i(TAG, "failed close stream " + e11);
                    }
                }
                throw th;
            }
        } else {
            return;
        }
        sb.append("failed close stream ");
        sb.append(e);
        Slog.i(TAG, sb.toString());
    }

    private void saveDozeCofigLocked() {
        StringBuilder sb;
        File file = new File(DOZE_CONFIG_XML_PATH);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Slog.i(TAG, "saveDozeCofigLocked: failed create file /data/system/doze_config_local.xml");
                }
            } catch (IOException e) {
                Slog.i(TAG, "failed create file " + e);
            }
        }
        if (file.exists()) {
            FileOutputStream fileos = null;
            try {
                FileOutputStream fileos2 = new FileOutputStream(file);
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(fileos2, "UTF-8");
                serializer.startDocument(null, true);
                serializer.startTag(null, "gs");
                serializer.startTag(null, "filter-name");
                serializer.text("doze_config");
                serializer.endTag(null, "filter-name");
                serializer.startTag(null, TAG_AUTO_POWER_SAVE_MODES_ENABLED);
                if (this.mAutoPowerModesEnabled) {
                    serializer.text("true");
                } else {
                    serializer.text("false");
                }
                serializer.endTag(null, TAG_AUTO_POWER_SAVE_MODES_ENABLED);
                serializer.endTag(null, "gs");
                serializer.endDocument();
                serializer.flush();
                try {
                    fileos2.close();
                    return;
                } catch (IOException e2) {
                    e = e2;
                    sb = new StringBuilder();
                }
            } catch (IllegalArgumentException e3) {
                Slog.i(TAG, "failed write file " + e3);
                if (0 != 0) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e4) {
                        e = e4;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IllegalStateException e5) {
                Slog.i(TAG, "failed write file " + e5);
                if (0 != 0) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e6) {
                        e = e6;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IOException e7) {
                Slog.i(TAG, "failed write file " + e7);
                if (0 != 0) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e8) {
                        e = e8;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Exception e9) {
                Slog.i(TAG, "failed write file " + e9);
                if (0 != 0) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e10) {
                        e = e10;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        fileos.close();
                    } catch (IOException e11) {
                        Slog.i(TAG, "failed close stream " + e11);
                    }
                }
                throw th;
            }
        } else {
            return;
        }
        sb.append("failed close stream ");
        sb.append(e);
        Slog.i(TAG, sb.toString());
    }

    private int getLocalDozeCofigLocked() {
        int res = -1;
        File file = new File(DOZE_CONFIG_XML_PATH);
        if (!file.exists()) {
            return -1;
        }
        FileReader xmlReader = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            xmlReader = new FileReader(file);
            parser.setInput(xmlReader);
            res = parseDozeCofigXml(parser);
            try {
                xmlReader.close();
            } catch (IOException e) {
                Slog.w(TAG, "getDozeCofigLocked: Got execption close xmlReader. ", e);
            }
        } catch (Exception e2) {
            Slog.w(TAG, "getDozeCofigLocked: Got execption. ", e2);
            if (xmlReader != null) {
                xmlReader.close();
            }
        } catch (Throwable th) {
            if (xmlReader != null) {
                try {
                    xmlReader.close();
                } catch (IOException e3) {
                    Slog.w(TAG, "getDozeCofigLocked: Got execption close xmlReader. ", e3);
                }
            }
            throw th;
        }
        return res;
    }

    private int parseDozeCofigXml(XmlPullParser parser) {
        int res = -1;
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String strName = parser.getName();
                        parser.next();
                        String strText = parser.getText();
                        if (TAG_AUTO_POWER_SAVE_MODES_ENABLED.equals(strName)) {
                            try {
                                if (Boolean.parseBoolean(strText)) {
                                    res = 1;
                                } else {
                                    res = 0;
                                }
                            } catch (NumberFormatException e) {
                                Slog.w(TAG, "parseDozeCofigXml NumberFormatException.", e);
                            }
                            if (sOppoDebug) {
                                Slog.d(TAG, "parseDozeCofigXml: res=" + res);
                            }
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e2) {
            Slog.w(TAG, "parseDozeCofigXml: Got execption. ", e2);
        }
        if (sOppoDebug) {
            Slog.d(TAG, "parseDozeCofigXml: res=" + res);
        }
        return res;
    }

    /* JADX INFO: Multiple debug info for r5v1 java.io.FileOutputStream: [D('fileos' java.io.FileOutputStream), D('e' java.io.IOException)] */
    public void saveCustomWhiteListToFileLocked(String path, List<String> list) {
        StringBuilder sb;
        if (path != null && list != null) {
            File file = new File(path);
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        return;
                    }
                } catch (IOException e) {
                    Slog.i(TAG, "failed create file " + e);
                }
            }
            FileOutputStream fileos = null;
            try {
                FileOutputStream fileos2 = new FileOutputStream(file);
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(fileos2, "UTF-8");
                serializer.startDocument(null, true);
                serializer.startTag(null, "gs");
                for (int i = 0; i < list.size(); i++) {
                    String pkg = list.get(i);
                    if (pkg != null) {
                        serializer.startTag(null, ColorAppCrashClearManager.CRASH_CLEAR_NAME);
                        serializer.attribute(null, "att", pkg);
                        serializer.endTag(null, ColorAppCrashClearManager.CRASH_CLEAR_NAME);
                    }
                }
                serializer.endTag(null, "gs");
                serializer.endDocument();
                serializer.flush();
                try {
                    fileos2.close();
                    return;
                } catch (IOException e2) {
                    e = e2;
                    sb = new StringBuilder();
                }
            } catch (IllegalArgumentException e3) {
                Slog.i(TAG, "failed write file " + e3);
                if (0 != 0) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e4) {
                        e = e4;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IllegalStateException e5) {
                Slog.i(TAG, "failed write file " + e5);
                if (0 != 0) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e6) {
                        e = e6;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IOException e7) {
                Slog.i(TAG, "failed write file " + e7);
                if (0 != 0) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e8) {
                        e = e8;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Exception e9) {
                Slog.i(TAG, "failed write file " + e9);
                if (0 != 0) {
                    try {
                        fileos.close();
                        return;
                    } catch (IOException e10) {
                        e = e10;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        fileos.close();
                    } catch (IOException e11) {
                        Slog.i(TAG, "failed close stream " + e11);
                    }
                }
                throw th;
            }
        } else {
            return;
        }
        sb.append("failed close stream ");
        sb.append(e);
        Slog.i(TAG, sb.toString());
    }

    public void readCustomWhiteListToFileLocked(String path, List<String> whiteListAll) {
        StringBuilder sb;
        int type;
        if (whiteListAll == null) {
            Slog.d(TAG, "readCustomWhiteListToFileLocked: no need");
            return;
        }
        File file = new File(path);
        if (file.exists()) {
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(file);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                do {
                    type = parser.next();
                    if (type == 2 && ColorAppCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName())) {
                        String pkg = parser.getAttributeValue(null, "att");
                        Slog.i(TAG, "readCustomWhiteListToFileLocked pkg:" + pkg);
                        if (pkg != null) {
                            whiteListAll.add(pkg);
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
            } catch (NullPointerException e2) {
                Slog.i(TAG, "failed parsing " + e2);
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
            } catch (NumberFormatException e4) {
                Slog.i(TAG, "failed parsing " + e4);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e5) {
                        e = e5;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (XmlPullParserException e6) {
                Slog.i(TAG, "failed parsing " + e6);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e7) {
                        e = e7;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IOException e8) {
                Slog.i(TAG, "failed IOException " + e8);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e9) {
                        e = e9;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e10) {
                        Slog.i(TAG, "Failed to close state FileInputStream " + e10);
                    }
                }
                throw th;
            }
        } else {
            return;
        }
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Slog.i(TAG, sb.toString());
    }

    private boolean isAutoPowerProtectOn(Context context) {
        if (Settings.System.getInt(context.getContentResolver(), AUTO_POWER_PROTECT_SWITCH, 1) != 0) {
            return true;
        }
        return false;
    }

    private void getAllowProhibitList(List<String> listAllow, List<String> listProhibit) {
        if (listAllow != null && listProhibit != null) {
            File file = new File(ALLOW_PROHIBIT_APP_FILE);
            if (file.exists()) {
                FileReader xmlReader = null;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    xmlReader = new FileReader(file);
                    parser.setInput(xmlReader);
                    parseAllowProhibitList(parser, listAllow, listProhibit);
                    try {
                        xmlReader.close();
                    } catch (IOException e) {
                        Slog.d(TAG, "getAllowProhibitList: Got execption close xmlReader IOException. ");
                    }
                } catch (Exception e2) {
                    Slog.d(TAG, "getAllowProhibitList: Got execption.");
                    if (xmlReader != null) {
                        xmlReader.close();
                    }
                } catch (Throwable th) {
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e3) {
                            Slog.d(TAG, "getAllowProhibitList: Got execption close xmlReader IOException. ");
                        }
                    }
                    throw th;
                }
            }
        }
    }

    private void parseAllowProhibitList(XmlPullParser parser, List<String> listAllow, List<String> listProhibit) {
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String strName = parser.getName();
                        parser.next();
                        String strText = parser.getText();
                        if (TAG_ALLOW_BACK_RUN.equals(strName)) {
                            if (!listAllow.contains(strText)) {
                                listAllow.add(strText);
                            }
                        } else if (TAG_PROHIBIT_BACK_RUN.equals(strName) && !listProhibit.contains(strText)) {
                            listProhibit.add(strText);
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Slog.d(TAG, "parseXml: Got execption.");
        }
    }

    private void uploadQuickEnterIdle(String status, String screenOnReason, String sleeReason) {
        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("quickEnterDeviceidle", "shortSreenOn".equals(status) ? "true" : status);
        eventMap.put("screenOnReason", screenOnReason);
        eventMap.put("sleeReason", sleeReason);
        OppoStatistics.onCommon(this.mContext, "20120", "quick_deviceidle_enter", eventMap, false);
    }

    public void dump(PrintWriter pw) {
        synchronized (this.mLock) {
            pw.println("DftWhiteList:");
            for (int j = 0; j < this.mWhiteListAll.size(); j++) {
                pw.println("  " + this.mWhiteListAll.get(j));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class DeviceIdleStatistics {
        long mAverageIdleInterval;
        long mAverageLightIdleInterval;
        int mCntAnyMotion;
        int mCntIdleEnter;
        int mCntIdleFail;
        int mCntIdleMaintenaceFail;
        int mCntLocatingFail;
        int mCntLocation;
        int mCntPendingFail;
        int mCntSensingFail;
        int mCntSignificantMotion;
        String mFirstIdleTimeStamp;
        String mFirstLightIdleTimeStamp;
        long mIdleInterval;
        long mLightIdleInterval;
        long mScreenOffInterval;
        long mScreenOffToFirstIdle;
        long mScreenOffToFirstLightIdle;
        long mTimestampIdleStart;
        long mTimestampLightIdleStart;

        DeviceIdleStatistics() {
        }

        /* JADX DEBUG: TODO: convert one arg to string using `String.valueOf()`, args: [(wrap: int : 0x0045: INVOKE  (r3v1 int) = (r0v4 'c' java.util.Calendar A[D('c' java.util.Calendar)]), (11 int) type: VIRTUAL call: java.util.Calendar.get(int):int), (':' char), (wrap: int : 0x0053: INVOKE  (r3v4 int) = (r0v4 'c' java.util.Calendar A[D('c' java.util.Calendar)]), (12 int) type: VIRTUAL call: java.util.Calendar.get(int):int)] */
        /* access modifiers changed from: package-private */
        public void onDeepIdleStart() {
            if (ColorDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorDeviceIdleHelper.TAG, "DeviceIdleStatistics: onDeepIdleStart. mScreenOffToFirstIdle=" + this.mScreenOffToFirstIdle);
            }
            if (this.mScreenOffToFirstIdle == 0) {
                this.mScreenOffToFirstIdle = SystemClock.elapsedRealtime() - ColorDeviceIdleHelper.this.mTimestampScreenoff;
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                StringBuilder result = new StringBuilder();
                result.append(c.get(11));
                result.append(':');
                result.append(c.get(12));
                this.mFirstIdleTimeStamp = result.toString();
                if (ColorDeviceIdleHelper.sOppoDebug) {
                    Slog.d(ColorDeviceIdleHelper.TAG, "mFirstIdleTimeStamp=" + this.mFirstIdleTimeStamp);
                }
            }
            this.mTimestampIdleStart = SystemClock.elapsedRealtime();
        }

        /* JADX DEBUG: TODO: convert one arg to string using `String.valueOf()`, args: [(wrap: int : 0x0045: INVOKE  (r3v1 int) = (r0v4 'c' java.util.Calendar A[D('c' java.util.Calendar)]), (11 int) type: VIRTUAL call: java.util.Calendar.get(int):int), (':' char), (wrap: int : 0x0053: INVOKE  (r3v4 int) = (r0v4 'c' java.util.Calendar A[D('c' java.util.Calendar)]), (12 int) type: VIRTUAL call: java.util.Calendar.get(int):int)] */
        /* access modifiers changed from: package-private */
        public void onLightIdleStart() {
            if (ColorDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorDeviceIdleHelper.TAG, "DeviceIdleStatistics: onLightIdleStart. mScreenOffToFirstLightIdle=" + this.mScreenOffToFirstLightIdle);
            }
            if (this.mScreenOffToFirstLightIdle == 0) {
                this.mScreenOffToFirstLightIdle = SystemClock.elapsedRealtime() - ColorDeviceIdleHelper.this.mTimestampScreenoff;
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                StringBuilder result = new StringBuilder();
                result.append(c.get(11));
                result.append(':');
                result.append(c.get(12));
                this.mFirstLightIdleTimeStamp = result.toString();
                if (ColorDeviceIdleHelper.sOppoDebug) {
                    Slog.d(ColorDeviceIdleHelper.TAG, "mFirstLightIdleTimeStamp=" + this.mFirstLightIdleTimeStamp);
                }
            }
            this.mTimestampLightIdleStart = SystemClock.elapsedRealtime();
        }

        /* access modifiers changed from: package-private */
        public void onIdleExit() {
            long nowElapsed = SystemClock.elapsedRealtime();
            long j = this.mTimestampIdleStart;
            if (nowElapsed > j) {
                this.mIdleInterval += nowElapsed - j;
            }
            long j2 = this.mTimestampLightIdleStart;
            if (nowElapsed > j2) {
                this.mLightIdleInterval += nowElapsed - j2;
            }
            if (ColorDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorDeviceIdleHelper.TAG, "DeviceIdleStatistics: onIdleExit. mIdleInterval=" + this.mIdleInterval + ", mLightIdleInterval=" + this.mLightIdleInterval);
            }
        }

        /* access modifiers changed from: package-private */
        public void onMotionDetected(Bundle data) {
            if (data != null) {
                int state = data.getInt("state");
                int typeMotion = data.getInt("typeMotion");
                if (state == 2) {
                    this.mCntPendingFail++;
                } else if (state == 3) {
                    this.mCntSensingFail++;
                } else if (state == 4) {
                    this.mCntLocatingFail++;
                } else if (state == 5) {
                    this.mCntIdleFail++;
                } else if (state == 6) {
                    this.mCntIdleMaintenaceFail++;
                }
                if (typeMotion == 1) {
                    this.mCntSignificantMotion++;
                } else if (typeMotion == 2) {
                    this.mCntAnyMotion++;
                }
                if (ColorDeviceIdleHelper.sOppoDebug) {
                    Slog.d(ColorDeviceIdleHelper.TAG, "DeviceIdleStatistics: onMotionDetected. state=" + state + ", typeMotion=" + typeMotion);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void uploadDeviceIdleCount() {
            Map<String, String> eventMap = new HashMap<>();
            eventMap.put("count_idle_enter", Integer.toString(this.mCntIdleEnter));
            eventMap.put("idle_interval", Long.toString(this.mAverageIdleInterval / 1000));
            eventMap.put("light_idle_interval", Long.toString(this.mAverageLightIdleInterval / 1000));
            OppoStatistics.onCommon(ColorDeviceIdleHelper.this.mContext, "20120007", "deviceIdle_count", eventMap, false);
            this.mCntIdleEnter = 0;
            this.mAverageIdleInterval = 0;
            this.mAverageLightIdleInterval = 0;
        }

        /* access modifiers changed from: package-private */
        public void uploadDeviceIdleStatistics() {
            if (this.mFirstIdleTimeStamp != null) {
                if (ColorDeviceIdleHelper.sOppoDebug) {
                    Slog.d(ColorDeviceIdleHelper.TAG, "DeviceIdleStatistics: uploadDeviceIdleStatistics. mFirstIdleTimeStamp=" + this.mFirstIdleTimeStamp);
                }
                Map<String, String> eventMap = new HashMap<>();
                eventMap.put("first_idle_timestamp", this.mFirstIdleTimeStamp);
                eventMap.put("first_light_idle_timestamp", this.mFirstLightIdleTimeStamp);
                eventMap.put("screenoff_to_first_idle", Long.toString(this.mScreenOffToFirstIdle / 1000));
                eventMap.put("screenoff_to_first_light_idle", Long.toString(this.mScreenOffToFirstLightIdle / 1000));
                eventMap.put("screenoff_interval", Long.toString((SystemClock.elapsedRealtime() - ColorDeviceIdleHelper.this.mTimestampScreenoff) / 1000));
                eventMap.put("idle_interval", Long.toString(this.mIdleInterval / 1000));
                eventMap.put("light_idle_interval", Long.toString(this.mLightIdleInterval / 1000));
                int i = this.mCntSignificantMotion;
                if (i > 0) {
                    eventMap.put("count_significantMotion", Integer.toString(i));
                }
                int i2 = this.mCntAnyMotion;
                if (i2 > 0) {
                    eventMap.put("count_anyMotion", Integer.toString(i2));
                }
                int i3 = this.mCntLocation;
                if (i3 > 0) {
                    eventMap.put("count_location", Integer.toString(i3));
                }
                int i4 = this.mCntPendingFail;
                if (i4 > 0) {
                    eventMap.put("count_pending_fail", Integer.toString(i4));
                }
                int i5 = this.mCntSensingFail;
                if (i5 > 0) {
                    eventMap.put("count_sensing_fail", Integer.toString(i5));
                }
                int i6 = this.mCntLocatingFail;
                if (i6 > 0) {
                    eventMap.put("count_locating_fail", Integer.toString(i6));
                }
                int i7 = this.mCntIdleFail;
                if (i7 > 0) {
                    eventMap.put("count_idle_fail", Integer.toString(i7));
                }
                int i8 = this.mCntIdleMaintenaceFail;
                if (i8 > 0) {
                    eventMap.put("count_maintenance_fail", Integer.toString(i8));
                }
                OppoStatistics.onCommon(ColorDeviceIdleHelper.this.mContext, "20120007", "deviceIdle_statistics", eventMap, false);
                this.mCntIdleEnter++;
                this.mAverageIdleInterval = (this.mAverageIdleInterval + this.mIdleInterval) / 2;
                this.mAverageLightIdleInterval = (this.mAverageLightIdleInterval + this.mLightIdleInterval) / 2;
            }
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.mFirstIdleTimeStamp = null;
            this.mFirstLightIdleTimeStamp = null;
            this.mScreenOffToFirstIdle = 0;
            this.mScreenOffToFirstLightIdle = 0;
            this.mScreenOffInterval = 0;
            this.mIdleInterval = 0;
            this.mLightIdleInterval = 0;
            this.mCntSignificantMotion = 0;
            this.mCntAnyMotion = 0;
            this.mCntLocation = 0;
            this.mCntPendingFail = 0;
            this.mCntSensingFail = 0;
            this.mCntLocatingFail = 0;
            this.mCntIdleFail = 0;
            this.mCntIdleMaintenaceFail = 0;
        }
    }

    public void motionDetected(int mState, int type) {
        int state = 0;
        if (mState == 2) {
            state = 2;
        } else if (mState == 3) {
            state = 3;
        } else if (mState == 4) {
            state = 4;
        } else if (mState == 5) {
            state = 5;
        } else if (mState == 6) {
            state = 6;
        }
        if (state != 0) {
            onMotionDetected(state, type);
        }
    }

    class TrafficRecord {
        long mTimeStamp;
        long mTotalTraffic;

        private TrafficRecord(long traffic, long timeStamp) {
            this.mTotalTraffic = traffic;
            this.mTimeStamp = timeStamp;
        }
    }

    public boolean isDeepInTraffic() {
        if (this.mLastDeepTrafficRecord == null) {
            Slog.d(TAG, "isDeepInTraffic: mLastDeepTrafficRecord is null!");
            return false;
        }
        long nowTraffic = getTotalTraffic();
        long nowElapsed = SystemClock.elapsedRealtime();
        long deltaTraffic = nowTraffic - this.mLastDeepTrafficRecord.mTotalTraffic;
        long deltaTime = (nowElapsed - this.mLastDeepTrafficRecord.mTimeStamp) / 1000;
        if (deltaTime == 0) {
            if (sOppoDebug) {
                Slog.d(TAG, "isDeepInTraffic: deltaTime is zero!");
            }
            return false;
        }
        long speed = (deltaTraffic / ColorDeviceStorageMonitorService.KB_BYTES) / deltaTime;
        if (sOppoDebug) {
            Slog.d(TAG, "isDeepInTraffic: speed=" + speed + "KB/s. deltaTraffic=" + Formatter.formatFileSize(this.mContext, deltaTraffic) + ", deltaTime=" + deltaTime + "s");
        }
        if (speed < THRESH_SPEED) {
            return false;
        }
        this.mLastDeepTrafficRecord = new TrafficRecord(nowTraffic, nowElapsed);
        return true;
    }

    public boolean isLightInTraffic() {
        if (this.mLastLightTrafficRecord == null) {
            Slog.d(TAG, "isLightInTraffic: mLastLightTrafficRecord is null!");
            return false;
        }
        long nowTraffic = getTotalTraffic();
        long nowElapsed = SystemClock.elapsedRealtime();
        long deltaTraffic = nowTraffic - this.mLastLightTrafficRecord.mTotalTraffic;
        long deltaTime = (nowElapsed - this.mLastLightTrafficRecord.mTimeStamp) / 1000;
        if (deltaTime == 0) {
            if (sOppoDebug) {
                Slog.d(TAG, "isLightInTraffic: deltaTime is zero!");
            }
            return false;
        }
        long speed = (deltaTraffic / ColorDeviceStorageMonitorService.KB_BYTES) / deltaTime;
        if (sOppoDebug) {
            Slog.d(TAG, "isLightInTraffic: speed=" + speed + "KB/s. deltaTraffic=" + Formatter.formatFileSize(this.mContext, deltaTraffic) + ", deltaTime=" + deltaTime + "s");
        }
        if (speed < THRESH_SPEED) {
            return false;
        }
        this.mLastLightTrafficRecord = new TrafficRecord(nowTraffic, nowElapsed);
        return true;
    }

    private long getTotalTraffic() {
        long rx = TrafficStats.getTotalRxBytes();
        long tx = TrafficStats.getTotalTxBytes();
        if (sOppoDebug) {
            Slog.d(TAG, "getTotalTrafficKB: rx=" + Formatter.formatFileSize(this.mContext, rx) + ", tx=" + Formatter.formatFileSize(this.mContext, tx) + ", total=" + Formatter.formatFileSize(this.mContext, rx + tx));
        }
        return rx + tx;
    }

    public void updateLastLightTrafficRecord() {
        this.mLastLightTrafficRecord = new TrafficRecord(getTotalTraffic(), SystemClock.elapsedRealtime());
    }

    public void updateLastDeepTrafficRecord() {
        this.mLastDeepTrafficRecord = new TrafficRecord(getTotalTraffic(), SystemClock.elapsedRealtime());
    }

    public void enterDeepSleepQuickly() {
        IColorDeviceIdleControllerInner iColorDeviceIdleControllerInner = this.mInner;
        if (iColorDeviceIdleControllerInner != null) {
            int mIdleState = iColorDeviceIdleControllerInner.getState();
            boolean mIdleDeepEnabled = this.mInner.getDeepEnabled();
            this.mInner.getNextIdlePendingDelay();
            this.mInner.getNextIdleDelay();
            if (mIdleState == 0 && mIdleDeepEnabled) {
                this.mInner.resetIdleManagementLocked();
                this.mDeviceIdleController.startMonitoringMotionLocked();
                this.mDeviceIdleController.scheduleAlarmLocked((long) ALARM_WINDOW_LENGTH, false);
                EventLogTags.writeDeviceIdle(4, "no activity");
                if (OppoBaseDeviceIdleController.DEBUG_OPPO) {
                    Slog.d(TAG, "Moved from STATE_ACTIVE to STATE_LOCATING");
                }
                this.mInner.setState(4);
                this.mInner.setDeepEnabled(mIdleDeepEnabled);
                this.mInner.setNextIdlePendingDelay(300000);
                this.mInner.setNextIdleDelay((long) IDLE_TIMEOUT);
            }
        }
    }

    public void removePackage(Intent intent) {
        Integer uidInteger;
        String packageName = intent.getData().getSchemeSpecificPart();
        IColorDeviceIdleControllerInner iColorDeviceIdleControllerInner = this.mInner;
        if (iColorDeviceIdleControllerInner != null) {
            iColorDeviceIdleControllerInner.getPowerSaveWhitelistUserApps();
        }
        synchronized (this.mDeviceIdleController) {
            uidInteger = this.mPSWhitelistUserApps.get(packageName);
        }
        if (uidInteger != null) {
            int uid = uidInteger.intValue();
            try {
                int getUid = UserHandle.getAppId(this.mContext.getPackageManager().getApplicationInfo(packageName, 8192).uid);
                if (uid != getUid) {
                    this.mDeviceIdleController.removePowerSaveWhitelistAppInternal(packageName);
                    this.mDeviceIdleController.addPowerSaveWhitelistAppInternal(packageName);
                    Slog.d(TAG, "replace uid in PowerSaveWhitelist! name=" + packageName + " uid=" + getUid);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Slog.d(TAG, "PackageNameNotFoundException = " + e);
            }
        } else {
            Slog.d(TAG, "pacakge removed: name=" + packageName + " not in PowerSaveWhitelist");
        }
    }

    private boolean isClosedSuperFirewall(PackageManager packageManager) {
        if (OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public class SmartDozeHelperInner {
        private PendingIntent mAlarmExemptionPendingIntent;
        private AlarmManager mAlarmManager;
        BroadcastReceiver mGpsExemptionBroadcast = new BroadcastReceiver() {
            /* class com.android.server.ColorDeviceIdleHelper.SmartDozeHelperInner.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                if (ColorSmartDozeHelper.ACTION_OPPO_SMARTDOZE_GPS_EXEMPTION_CHANGED.equals(intent.getAction()) && (!intent.getBooleanExtra("state", false)) && OppoFeatureCache.get(IColorSmartDozeHelper.DEFAULT).isInSmartDozeMode()) {
                    SmartDozeHelperInner.this.startMonitoringMotion();
                }
            }
        };
        private PendingIntent mGpsExemptionPendingIntent;
        private final MotionListener mMotionListener = new MotionListener();
        private Sensor mMotionSensor;
        private SensorManager mSensorManager;
        BroadcastReceiver mSmartDozeReceiver = new BroadcastReceiver() {
            /* class com.android.server.ColorDeviceIdleHelper.SmartDozeHelperInner.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ColorDeviceIdleHelper.ACTION_SMART_DOZE_CHANGE.equals(action)) {
                    if (ColorDeviceIdleHelper.sOppoDebug) {
                        Slog.d(ColorDeviceIdleHelper.TAG, "receive smart doze intent, action = " + action);
                    }
                    if (ColorDeviceIdleHelper.this.isAutoPowerModesEnabled()) {
                        SmartDozeHelperInner.this.updateSmartDozeWhiteList();
                        if (SmartDozeHelperInner.this.quickEnterDoze("smartdoze")) {
                            SmartDozeHelperInner.this.setSmartDozeWhiteList();
                            SmartDozeHelperInner.this.cancelSensorCheck();
                            SmartDozeHelperInner.this.startMonitoringMotion();
                            SmartDozeHelperInner smartDozeHelperInner = SmartDozeHelperInner.this;
                            smartDozeHelperInner.sendAlarmExemption(ColorDeviceIdleHelper.this.mContext);
                            if (ColorDeviceIdleHelper.sOppoDebug) {
                                Slog.d(ColorDeviceIdleHelper.TAG, "in smart doze mode, cancel motion check");
                            }
                        }
                        if (ColorDeviceIdleHelper.sOppoDebug) {
                            Slog.d(ColorDeviceIdleHelper.TAG, "in smart doze mode, cancel motion check");
                        }
                    }
                }
            }
        };
        ArraySet<String> mSmartDozeWhitlist;
        private boolean mUseMotionSensor;

        SmartDozeHelperInner() {
            init();
        }

        /* access modifiers changed from: package-private */
        public void init() {
            Slog.d(ColorDeviceIdleHelper.TAG, "smartdoze helper init");
            this.mSmartDozeWhitlist = new ArraySet<>();
            this.mSensorManager = (SensorManager) ColorDeviceIdleHelper.this.mContext.getSystemService("sensor");
            this.mAlarmManager = (AlarmManager) ColorDeviceIdleHelper.this.mContext.getSystemService("alarm");
            initSensor(ColorDeviceIdleHelper.this.mContext);
            IntentFilter smartDozeFilter = new IntentFilter(ColorDeviceIdleHelper.ACTION_SMART_DOZE_CHANGE);
            IntentFilter gpsExemptionFilter = new IntentFilter(ColorSmartDozeHelper.ACTION_OPPO_SMARTDOZE_GPS_EXEMPTION_CHANGED);
            ColorDeviceIdleHelper.this.mContext.registerReceiver(this.mSmartDozeReceiver, smartDozeFilter, "oppo.permission.OPPO_COMPONENT_SAFE", ColorDeviceIdleHelper.this.mHandler);
            ColorDeviceIdleHelper.this.mContext.registerReceiver(this.mGpsExemptionBroadcast, gpsExemptionFilter, "oppo.permission.OPPO_COMPONENT_SAFE", ColorDeviceIdleHelper.this.mHandler);
        }

        /* access modifiers changed from: package-private */
        public void initSensor(Context context) {
            if (context != null) {
                this.mUseMotionSensor = context.getResources().getBoolean(17891365);
                if (this.mUseMotionSensor) {
                    int sigMotionSensorId = context.getResources().getInteger(17694742);
                    if (sigMotionSensorId > 0) {
                        this.mMotionSensor = this.mSensorManager.getDefaultSensor(sigMotionSensorId, true);
                    }
                    if (this.mMotionSensor == null && context.getResources().getBoolean(17891363)) {
                        this.mMotionSensor = this.mSensorManager.getDefaultSensor(26, true);
                    }
                }
                if (this.mMotionSensor == null) {
                    this.mMotionSensor = this.mSensorManager.getDefaultSensor(17, true);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void updateSmartDozeWhiteList() {
            if (ColorDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorDeviceIdleHelper.TAG, "updateSmartDozeWhiteList");
            }
            ArrayList<String> smartDozeWhitelist = getSmartWhiteListFromPath();
            if (smartDozeWhitelist != null) {
                if (!this.mSmartDozeWhitlist.containsAll(smartDozeWhitelist) || !smartDozeWhitelist.containsAll(this.mSmartDozeWhitlist)) {
                    this.mSmartDozeWhitlist.clear();
                    this.mSmartDozeWhitlist.addAll(smartDozeWhitelist);
                }
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
            if (com.android.server.ColorDeviceIdleHelper.sOppoDebug == false) goto L_0x003d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0025, code lost:
            android.util.Slog.d(com.android.server.ColorDeviceIdleHelper.TAG, "mSmartDozeWhitlist : " + r3.mSmartDozeWhitlist);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x003d, code lost:
            addWhitelists(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0040, code lost:
            return;
         */
        public void setSmartDozeWhiteList() {
            if (ColorDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorDeviceIdleHelper.TAG, "setSmartDozeWhiteList");
            }
            ArraySet whitelist = new ArraySet();
            synchronized (this) {
                if (this.mSmartDozeWhitlist != null) {
                    whitelist.addAll((ArraySet) this.mSmartDozeWhitlist);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void addWhitelists(ArraySet<String> whitelists) {
            if (ColorDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorDeviceIdleHelper.TAG, "addWhitelists: whitelists" + whitelists);
            }
            if (whitelists != null && !whitelists.isEmpty()) {
                synchronized (ColorDeviceIdleHelper.this.mDeviceIdleController) {
                    if (ColorDeviceIdleHelper.this.mInner != null) {
                        boolean isWhitelistChanged = false;
                        ArrayMap powerSaveWhitelistUserApps = ColorDeviceIdleHelper.this.mInner.getPowerSaveWhitelistUserApps();
                        if (powerSaveWhitelistUserApps != null) {
                            Iterator<String> it = whitelists.iterator();
                            while (it.hasNext()) {
                                String pkg = it.next();
                                try {
                                    ApplicationInfo ai = ColorDeviceIdleHelper.this.mContext.getPackageManager().getApplicationInfo(pkg, 4194304);
                                    if (!OppoFeatureCache.get(IColorGoogleDozeRestrict.DEFAULT).interceptWhitelistOperation(ai, pkg, false, false, true)) {
                                        if (powerSaveWhitelistUserApps.put(pkg, Integer.valueOf(UserHandle.getAppId(ai.uid))) == null) {
                                            isWhitelistChanged = true;
                                        }
                                    }
                                } catch (PackageManager.NameNotFoundException e) {
                                    Slog.e(ColorDeviceIdleHelper.TAG, "NameNotFoundException" + e);
                                }
                            }
                            if (isWhitelistChanged) {
                                OppoFeatureCache.get(IColorGoogleDozeRestrict.DEFAULT).oppoUpdateWhitelist();
                            }
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void setSmartDozeMode(boolean enable) {
            Slog.d(ColorDeviceIdleHelper.TAG, "setSmartDozeMode: enable = " + enable);
            if (!enable) {
                OppoFeatureCache.get(IColorSmartDozeHelper.DEFAULT).exitSmartDoze();
                stopMonitoringMotion();
                PendingIntent pendingIntent = this.mAlarmExemptionPendingIntent;
                if (pendingIntent != null) {
                    this.mAlarmManager.cancel(pendingIntent);
                }
                PendingIntent pendingIntent2 = this.mGpsExemptionPendingIntent;
                if (pendingIntent2 != null) {
                    this.mAlarmManager.cancel(pendingIntent2);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private synchronized ArraySet<String> getSmartWhiteLists() {
            return this.mSmartDozeWhitlist;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean quickEnterDoze(String reason) {
            Slog.d(ColorDeviceIdleHelper.TAG, "smartdoze:quick enter doze");
            synchronized (ColorDeviceIdleHelper.this.mDeviceIdleController) {
                boolean z = false;
                if (ColorDeviceIdleHelper.this.mInner == null) {
                    return false;
                }
                if (ColorDeviceIdleHelper.this.mInner.getState() == 0) {
                    return false;
                }
                ColorDeviceIdleHelper.this.mInner.setState(4);
                ColorDeviceIdleHelper.this.mDeviceIdleController.stepIdleStateLocked(reason);
                if (ColorDeviceIdleHelper.this.mDeviceIdleController.getState() == 5) {
                    z = true;
                }
                return z;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void cancelSensorCheck() {
            synchronized (ColorDeviceIdleHelper.this.mDeviceIdleController) {
                ColorDeviceIdleHelper.this.mDeviceIdleController.stopMonitoringMotionLocked();
            }
        }

        /* access modifiers changed from: package-private */
        public void sendAlarmExemption(Context context) {
            if (ColorDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorDeviceIdleHelper.TAG, "sendAlarmExemption: ");
            }
            if (this.mAlarmExemptionPendingIntent == null) {
                this.mAlarmExemptionPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ColorSmartDozeHelper.ACTION_OPPO_SMARTDOZE_ALARM_EXEMPTION_END), 0);
            }
            this.mAlarmManager.setExactAndAllowWhileIdle(2, ColorSmartDozeHelper.ALARM_EXEPTION_TIME + SystemClock.elapsedRealtime(), this.mAlarmExemptionPendingIntent);
        }

        /* access modifiers changed from: package-private */
        public void sendGpsExemptionChanged(Context context, boolean exemption) {
            if (ColorDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorDeviceIdleHelper.TAG, "sendGpsExemptionChanged: ");
            }
            Intent intent = new Intent(ColorSmartDozeHelper.ACTION_OPPO_SMARTDOZE_GPS_EXEMPTION_CHANGED);
            intent.putExtra("state", exemption);
            context.sendBroadcast(intent);
        }

        /* access modifiers changed from: package-private */
        public void setGpsExemptionAlarmTimeout(Context context) {
            if (ColorDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorDeviceIdleHelper.TAG, "setGpsExemptionAlarmTimeout: ");
            }
            long triggerTime = ColorSmartDozeHelper.GPS_EXEPTION_TIME + SystemClock.elapsedRealtime();
            if (this.mGpsExemptionPendingIntent == null) {
                Intent intent = new Intent(ColorSmartDozeHelper.ACTION_OPPO_SMARTDOZE_GPS_EXEMPTION_CHANGED);
                intent.putExtra("state", false);
                this.mGpsExemptionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            }
            this.mAlarmManager.setExactAndAllowWhileIdle(2, triggerTime, this.mGpsExemptionPendingIntent);
        }

        /* access modifiers changed from: package-private */
        public void doMotion() {
            if (ColorDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorDeviceIdleHelper.TAG, "doMotion: ");
            }
            if (OppoFeatureCache.get(IColorSmartDozeHelper.DEFAULT).isInSmartDozeMode()) {
                stopMonitoringMotion();
                sendGpsExemptionChanged(ColorDeviceIdleHelper.this.mContext, true);
                setGpsExemptionAlarmTimeout(ColorDeviceIdleHelper.this.mContext);
            }
        }

        private ArrayList getSmartWhiteListFromPath() {
            File file = new File(ColorDeviceIdleHelper.SMART_DOZE_CONFIG_XML_PATH);
            if (!file.exists()) {
                return null;
            }
            FileReader xmlReader = null;
            ArrayList<String> listAll = new ArrayList<>();
            try {
                XmlPullParser parser = Xml.newPullParser();
                xmlReader = new FileReader(file);
                parser.setInput(xmlReader);
                ColorDeviceIdleHelper.this.parseXml(parser, listAll);
                try {
                    xmlReader.close();
                } catch (IOException e) {
                    Slog.w(ColorDeviceIdleHelper.TAG, "getLocalSavedWhiteList: Got execption close xmlReader. ", e);
                }
            } catch (Exception e2) {
                Slog.w(ColorDeviceIdleHelper.TAG, "getLocalSavedWhiteList: Got execption. ", e2);
                if (xmlReader != null) {
                    xmlReader.close();
                }
            } catch (Throwable th) {
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e3) {
                        Slog.w(ColorDeviceIdleHelper.TAG, "getLocalSavedWhiteList: Got execption close xmlReader. ", e3);
                    }
                }
                throw th;
            }
            return listAll;
        }

        /* access modifiers changed from: package-private */
        public void startMonitoringMotion() {
            if (this.mMotionSensor != null && !this.mMotionListener.mActive) {
                registerMotionSensor();
            }
        }

        /* access modifiers changed from: package-private */
        public void stopMonitoringMotion() {
            if (this.mMotionSensor != null && this.mMotionListener.mActive) {
                unregisterMotionSensor();
            }
        }

        /* access modifiers changed from: package-private */
        public final class MotionListener extends TriggerEventListener implements SensorEventListener {
            boolean mActive = false;

            MotionListener() {
            }

            public void onTrigger(TriggerEvent event) {
                if (this.mActive) {
                    SmartDozeHelperInner.this.doMotion();
                }
            }

            public void onSensorChanged(SensorEvent event) {
                SmartDozeHelperInner.this.doMotion();
                if (this.mActive) {
                    SmartDozeHelperInner.this.mSensorManager.unregisterListener(this, SmartDozeHelperInner.this.mMotionSensor);
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        }

        /* access modifiers changed from: package-private */
        public boolean registerMotionSensor() {
            boolean success;
            if (this.mMotionSensor.getReportingMode() == 2) {
                success = this.mSensorManager.requestTriggerSensor(this.mMotionListener, this.mMotionSensor);
            } else {
                success = this.mSensorManager.registerListener(this.mMotionListener, this.mMotionSensor, 3);
            }
            if (ColorDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorDeviceIdleHelper.TAG, "registerMotionSensor: " + success);
            }
            if (success) {
                this.mMotionListener.mActive = true;
            } else {
                this.mMotionListener.mActive = false;
                Slog.e(ColorDeviceIdleHelper.TAG, "Unable to register for " + this.mMotionSensor);
            }
            return success;
        }

        /* access modifiers changed from: package-private */
        public void unregisterMotionSensor() {
            if (ColorDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorDeviceIdleHelper.TAG, "unregisterMotionSensor: ");
            }
            if (this.mMotionSensor.getReportingMode() == 2) {
                this.mSensorManager.cancelTriggerSensor(this.mMotionListener, this.mMotionSensor);
            } else {
                this.mSensorManager.unregisterListener(this.mMotionListener);
            }
            this.mMotionListener.mActive = false;
        }
    }

    public boolean isInited() {
        return this.mIsInited;
    }
}
