package com.android.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Slog;
import android.util.Xml;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.usage.UnixCalendar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class ColorOSDeviceIdleHelper {
    private static final String ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String ACTION_UPLOAD_DEVICEIDLE_COUNT = "coloros.intent.action.UPLOAD_DEVICEIDLE_COUNT";
    private static final String ACTION_WEAK_LIST_INACTIVE = "coloros.intent.action.WEAK_LIST_INACTIVE";
    public static final long ALARM_WINDOW_LENGTH = 180000;
    private static final String ALLOW_PROHIBIT_APP_FILE = "/data/oppo/coloros/oppoguardelf/allow_prohibit_app.xml";
    public static final int ANY_MOTION = 2;
    private static final String AUTO_POWER_PROTECT_SWITCH = "auto_power_protect_state";
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final Uri CONTENT_URI_WHITE_LIST = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final int DEFAULT_THRESH_HOUR_RESTRICT_WEAKLIST = 4;
    public static final long DEFAULT_TOTAL_INTERVAL_TO_IDLE = 1800000;
    private static final long DELAY_WEAK_WHITELIST_INACTIVE = 1800000;
    private static final String DOZE_CONFIG_XML_PATH = "/data/system/doze_config_local.xml";
    private static final String FILTER_NAME = "sys_deviceidle_whitelist";
    public static final int LOCATION = 3;
    private static final int MSG_DEEP_IDLE_START = 2;
    private static final int MSG_IDLE_EXIT = 4;
    private static final int MSG_LIGHT_IDLE_START = 3;
    private static final int MSG_MOTION_DETECT = 7;
    private static final int MSG_SCREEN_OFF = 5;
    private static final int MSG_SCREEN_ON = 6;
    private static final int MSG_WHITELIST_UPDATE = 1;
    private static final String OPPO_CUSTOMIZE_WHITE_FILE_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final String REMOVED_WEAK_LIST_XML_PATH = "/data/system/doze_rmved_weak_list.xml";
    private static final String SAVED_WHITELIST_XML_PATH = "/data/oppo/coloros/oppoguardelf/doze_wl_local.xml";
    private static final String SAVED_WHITE_LIST_USER_SET_XML_PATH = "/data/oppo/coloros/oppoguardelf/doze_wl_user_set_local.xml";
    public static final int SIGNIFICANT_MOTION = 1;
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
    private static final String TAG_RESTRICT_WEAK_LIST = "restrict_weaklist";
    private static final String TAG_THRESH_HOUR_RESTRICT_WEAKLIST = "thresh_hour_rest_weaklist";
    private static final String TAG_TOTAL_INTERVAL_TO_IDLE = "total_interval_idle";
    private static final String TAG_WEAK_LIST = "weak-whitelist";
    private static final int TIME_DELAY = 2000;
    private static final int TIME_ONE_DAY = 86400000;
    private static final int TIME_ONE_SECOND = 1000;
    private static boolean sOppoDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private AlarmManager mAlarmManager;
    private volatile boolean mAutoPowerModesEnabled = true;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS".equals(action)) {
                ArrayList<String> changeList = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST");
                if (changeList != null && changeList.contains(ColorOSDeviceIdleHelper.FILTER_NAME)) {
                    Slog.d(ColorOSDeviceIdleHelper.TAG, "ACTION_ROM_UPDATE_CONFIG_SUCCES");
                    ColorOSDeviceIdleHelper.this.updateWhiteList();
                }
            } else if (ColorOSDeviceIdleHelper.ACTION_WEAK_LIST_INACTIVE.equals(action)) {
                ColorOSDeviceIdleHelper.this.mHasAlarmPending = false;
                if (SystemClock.elapsedRealtime() - ColorOSDeviceIdleHelper.this.mTimestampScreenoff >= ColorOSDeviceIdleHelper.this.mDelayWeakWhiteListInactive) {
                    ColorOSDeviceIdleHelper.this.weakeWhiteListInactive();
                }
            } else if (ColorOSDeviceIdleHelper.ACTION_UPLOAD_DEVICEIDLE_COUNT.equals(action)) {
                ColorOSDeviceIdleHelper.this.mDeviceIdleStatistics.uploadDeviceIdleCount();
                ColorOSDeviceIdleHelper.this.scheduleUploadAlarm();
            } else if ("android.intent.action.PACKAGE_ADDED".equals(action) && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                Uri data = intent.getData();
                if (data != null) {
                    String pkg = data.getSchemeSpecificPart();
                    if (pkg != null && ColorOSDeviceIdleHelper.this.mWhiteListAll.contains(pkg)) {
                        ColorOSDeviceIdleHelper.this.mDeviceIdleController.addPowerSaveWhitelistAppInternal(pkg);
                        if (ColorOSDeviceIdleHelper.sOppoDebug) {
                            Slog.d(ColorOSDeviceIdleHelper.TAG, " ACTION_PACKAGE_ADDED, addPowerSaveWhitelistAppInternal pkg=" + pkg);
                        }
                    }
                }
            }
        }
    };
    ContentObserver mConstants;
    private Context mContext;
    private long mDelayWeakWhiteListInactive = 1800000;
    private DeviceIdleController mDeviceIdleController;
    DeviceIdleStatistics mDeviceIdleStatistics;
    private WorkerHandler mHandler;
    private boolean mHasAlarmPending = false;
    private boolean mIsDeepIdleEntered = false;
    private boolean mIsLightIdleEntered = false;
    private final Object mLock = new Object();
    private PackageManager mPackageManager;
    private PowerManager mPowerManager;
    private boolean mRestrictWeakListApp = false;
    private int mThreshHourRestrictWeakList = 4;
    private long mTimestampScreenoff;
    private long mTotalIntervalToIdle = 1800000;
    private PendingIntent mUploadIntent;
    private UsageStatsManagerInternal mUsageStats;
    private WakeLock mWakelock;
    private PendingIntent mWeakListIntent;
    private ArrayList<String> mWeakWhiteList = new ArrayList();
    private ArrayList<String> mWeakWhiteListThisTime = new ArrayList();
    private ArrayList<String> mWhiteListAll = new ArrayList();

    class DeviceIdleStatistics {
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

        void onDeepIdleStart() {
            if (ColorOSDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorOSDeviceIdleHelper.TAG, "DeviceIdleStatistics: onDeepIdleStart. mScreenOffToFirstIdle=" + this.mScreenOffToFirstIdle);
            }
            if (this.mScreenOffToFirstIdle == 0) {
                this.mScreenOffToFirstIdle = SystemClock.elapsedRealtime() - ColorOSDeviceIdleHelper.this.mTimestampScreenoff;
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                StringBuilder result = new StringBuilder();
                result.append(c.get(11));
                result.append(':');
                result.append(c.get(12));
                this.mFirstIdleTimeStamp = result.toString();
                if (ColorOSDeviceIdleHelper.sOppoDebug) {
                    Slog.d(ColorOSDeviceIdleHelper.TAG, "mFirstIdleTimeStamp=" + this.mFirstIdleTimeStamp);
                }
            }
            this.mTimestampIdleStart = SystemClock.elapsedRealtime();
        }

        void onLightIdleStart() {
            if (ColorOSDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorOSDeviceIdleHelper.TAG, "DeviceIdleStatistics: onLightIdleStart. mScreenOffToFirstLightIdle=" + this.mScreenOffToFirstLightIdle);
            }
            if (this.mScreenOffToFirstLightIdle == 0) {
                this.mScreenOffToFirstLightIdle = SystemClock.elapsedRealtime() - ColorOSDeviceIdleHelper.this.mTimestampScreenoff;
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                StringBuilder result = new StringBuilder();
                result.append(c.get(11));
                result.append(':');
                result.append(c.get(12));
                this.mFirstLightIdleTimeStamp = result.toString();
                if (ColorOSDeviceIdleHelper.sOppoDebug) {
                    Slog.d(ColorOSDeviceIdleHelper.TAG, "mFirstLightIdleTimeStamp=" + this.mFirstLightIdleTimeStamp);
                }
            }
            this.mTimestampLightIdleStart = SystemClock.elapsedRealtime();
        }

        void onIdleExit() {
            long nowElapsed = SystemClock.elapsedRealtime();
            if (nowElapsed > this.mTimestampIdleStart) {
                this.mIdleInterval += nowElapsed - this.mTimestampIdleStart;
            }
            if (nowElapsed > this.mTimestampLightIdleStart) {
                this.mLightIdleInterval += nowElapsed - this.mTimestampLightIdleStart;
            }
            if (ColorOSDeviceIdleHelper.sOppoDebug) {
                Slog.d(ColorOSDeviceIdleHelper.TAG, "DeviceIdleStatistics: onIdleExit. mIdleInterval=" + this.mIdleInterval + ", mLightIdleInterval=" + this.mLightIdleInterval);
            }
        }

        void onMotionDetected(Bundle data) {
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
                if (ColorOSDeviceIdleHelper.sOppoDebug) {
                    Slog.d(ColorOSDeviceIdleHelper.TAG, "DeviceIdleStatistics: onMotionDetected. state=" + state + ", typeMotion=" + typeMotion);
                }
            }
        }

        void uploadDeviceIdleCount() {
            Map<String, String> eventMap = new HashMap();
            eventMap.put("count_idle_enter", Integer.toString(this.mCntIdleEnter));
            eventMap.put("idle_interval", Long.toString(this.mAverageIdleInterval / 1000));
            eventMap.put("light_idle_interval", Long.toString(this.mAverageLightIdleInterval / 1000));
            OppoStatistics.onCommon(ColorOSDeviceIdleHelper.this.mContext, "20120007", "deviceIdle_count", eventMap, false);
            this.mCntIdleEnter = 0;
            this.mAverageIdleInterval = 0;
            this.mAverageLightIdleInterval = 0;
        }

        void uploadDeviceIdleStatistics() {
            if (this.mFirstIdleTimeStamp != null) {
                if (ColorOSDeviceIdleHelper.sOppoDebug) {
                    Slog.d(ColorOSDeviceIdleHelper.TAG, "DeviceIdleStatistics: uploadDeviceIdleStatistics. mFirstIdleTimeStamp=" + this.mFirstIdleTimeStamp);
                }
                Map<String, String> eventMap = new HashMap();
                eventMap.put("first_idle_timestamp", this.mFirstIdleTimeStamp);
                eventMap.put("first_light_idle_timestamp", this.mFirstLightIdleTimeStamp);
                eventMap.put("screenoff_to_first_idle", Long.toString(this.mScreenOffToFirstIdle / 1000));
                eventMap.put("screenoff_to_first_light_idle", Long.toString(this.mScreenOffToFirstLightIdle / 1000));
                eventMap.put("screenoff_interval", Long.toString((SystemClock.elapsedRealtime() - ColorOSDeviceIdleHelper.this.mTimestampScreenoff) / 1000));
                eventMap.put("idle_interval", Long.toString(this.mIdleInterval / 1000));
                eventMap.put("light_idle_interval", Long.toString(this.mLightIdleInterval / 1000));
                if (this.mCntSignificantMotion > 0) {
                    eventMap.put("count_significantMotion", Integer.toString(this.mCntSignificantMotion));
                }
                if (this.mCntAnyMotion > 0) {
                    eventMap.put("count_anyMotion", Integer.toString(this.mCntAnyMotion));
                }
                if (this.mCntLocation > 0) {
                    eventMap.put("count_location", Integer.toString(this.mCntLocation));
                }
                if (this.mCntPendingFail > 0) {
                    eventMap.put("count_pending_fail", Integer.toString(this.mCntPendingFail));
                }
                if (this.mCntSensingFail > 0) {
                    eventMap.put("count_sensing_fail", Integer.toString(this.mCntSensingFail));
                }
                if (this.mCntLocatingFail > 0) {
                    eventMap.put("count_locating_fail", Integer.toString(this.mCntLocatingFail));
                }
                if (this.mCntIdleFail > 0) {
                    eventMap.put("count_idle_fail", Integer.toString(this.mCntIdleFail));
                }
                if (this.mCntIdleMaintenaceFail > 0) {
                    eventMap.put("count_maintenance_fail", Integer.toString(this.mCntIdleMaintenaceFail));
                }
                OppoStatistics.onCommon(ColorOSDeviceIdleHelper.this.mContext, "20120007", "deviceIdle_statistics", eventMap, false);
                this.mCntIdleEnter++;
                this.mAverageIdleInterval = (this.mAverageIdleInterval + this.mIdleInterval) / 2;
                this.mAverageLightIdleInterval = (this.mAverageLightIdleInterval + this.mLightIdleInterval) / 2;
            }
        }

        void reset() {
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

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            ColorOSDeviceIdleHelper.this.mWakelock.acquire();
            if (msg.what == 1) {
                if (ColorOSDeviceIdleHelper.sOppoDebug) {
                    Slog.d(ColorOSDeviceIdleHelper.TAG, "MSG_WHITELIST_UPDATE");
                }
                ColorOSDeviceIdleHelper.this.updateWhiteList();
                ColorOSDeviceIdleHelper.this.updateRemovedWeakListLocked();
            } else if (msg.what == 3) {
                ColorOSDeviceIdleHelper.this.reconfirmWhiteList(msg.getData());
                ColorOSDeviceIdleHelper.this.mDeviceIdleStatistics.onLightIdleStart();
            } else if (msg.what == 2) {
                ColorOSDeviceIdleHelper.this.reconfirmWhiteList(msg.getData());
                ColorOSDeviceIdleHelper.this.weakWhiteListHandle();
                ColorOSDeviceIdleHelper.this.mDeviceIdleStatistics.onDeepIdleStart();
            } else if (msg.what == 4) {
                if (ColorOSDeviceIdleHelper.this.mHasAlarmPending) {
                    ColorOSDeviceIdleHelper.this.mAlarmManager.cancel(ColorOSDeviceIdleHelper.this.mWeakListIntent);
                    ColorOSDeviceIdleHelper.this.mHasAlarmPending = false;
                }
                synchronized (ColorOSDeviceIdleHelper.this.mLock) {
                    ColorOSDeviceIdleHelper.this.addPkgToDeviceIdleController(ColorOSDeviceIdleHelper.this.mWeakWhiteListThisTime);
                    ColorOSDeviceIdleHelper.this.mWeakWhiteListThisTime.clear();
                    ColorOSDeviceIdleHelper.this.saveRemovedWeakWhiteListLocked(null);
                }
                ColorOSDeviceIdleHelper.this.mDeviceIdleStatistics.onIdleExit();
            } else if (msg.what == 5) {
                ColorOSDeviceIdleHelper.this.mTimestampScreenoff = SystemClock.elapsedRealtime();
            } else if (msg.what == 6) {
                ColorOSDeviceIdleHelper.this.mDeviceIdleStatistics.uploadDeviceIdleStatistics();
                ColorOSDeviceIdleHelper.this.mDeviceIdleStatistics.reset();
            } else if (msg.what == 7) {
                if (ColorOSDeviceIdleHelper.sOppoDebug) {
                    Slog.d(ColorOSDeviceIdleHelper.TAG, "MSG_MOTION_DETECT");
                }
                ColorOSDeviceIdleHelper.this.mDeviceIdleStatistics.onMotionDetected(msg.getData());
            }
            ColorOSDeviceIdleHelper.this.mWakelock.release();
        }
    }

    public ColorOSDeviceIdleHelper(Context context, DeviceIdleController controller, ContentObserver constants) {
        this.mContext = context;
        this.mDeviceIdleController = controller;
        this.mConstants = constants;
        this.mDeviceIdleStatistics = new DeviceIdleStatistics();
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mWakelock = this.mPowerManager.newWakeLock(1, TAG);
        this.mWakelock.setReferenceCounted(false);
        HandlerThread hd = new HandlerThread(TAG);
        hd.start();
        this.mHandler = new WorkerHandler(hd.getLooper());
        this.mWeakListIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_WEAK_LIST_INACTIVE), 0);
        this.mUploadIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_UPLOAD_DEVICEIDLE_COUNT), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
        filter.addAction(ACTION_WEAK_LIST_INACTIVE);
        filter.addAction(ACTION_UPLOAD_DEVICEIDLE_COUNT);
        context.registerReceiver(this.mBroadcastReceiver, filter, null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addDataScheme("package");
        context.registerReceiver(this.mBroadcastReceiver, filter, null, this.mHandler);
        this.mHandler.sendEmptyMessageDelayed(1, 2000);
        scheduleUploadAlarm();
        int dozeLocalConfig = getLocalDozeCofigLocked();
        if (dozeLocalConfig == 1) {
            this.mAutoPowerModesEnabled = true;
        } else if (dozeLocalConfig == 0) {
            this.mAutoPowerModesEnabled = false;
        } else {
            this.mAutoPowerModesEnabled = context.getResources().getBoolean(17956946);
        }
        Slog.d(TAG, "init. dozeLocalConfig=" + dozeLocalConfig + ", mAutoPowerModesEnabled=" + this.mAutoPowerModesEnabled);
        this.mUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
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
    }

    public void onScreenOff() {
        this.mHandler.sendEmptyMessage(5);
    }

    public void onScreenOn() {
        this.mHandler.sendEmptyMessage(6);
    }

    private void reconfirmWhiteList(Bundle data) {
        synchronized (this.mLock) {
            if (data == null) {
                return;
            }
            ArrayList<String> whiteListSetByUser = getLocalSavedWhiteListSetByUserLocked();
            ArrayList<String> listPowerSaveUser = data.getStringArrayList("list");
            if (listPowerSaveUser == null) {
                return;
            }
            for (int i = 0; i < listPowerSaveUser.size(); i++) {
                String pkgName = (String) listPowerSaveUser.get(i);
                if (!(this.mWhiteListAll.contains(pkgName) || (whiteListSetByUser.contains(pkgName) ^ 1) == 0)) {
                    this.mDeviceIdleController.removePowerSaveWhitelistAppInternal(pkgName);
                    if (sOppoDebug) {
                        Slog.d(TAG, "reconfirmWhiteList: remove from DeviceIdleController: " + pkgName);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0056 A:{SYNTHETIC, Splitter: B:26:0x0056} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0044 A:{SYNTHETIC, Splitter: B:20:0x0044} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<String> getLocalSavedWhiteListSetByUserLocked() {
        Exception e;
        Throwable th;
        ArrayList<String> list = new ArrayList();
        File file = new File(SAVED_WHITE_LIST_USER_SET_XML_PATH);
        if (!file.exists()) {
            return list;
        }
        FileReader xmlReader = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            FileReader xmlReader2 = new FileReader(file);
            if (xmlReader2 != null) {
                try {
                    parser.setInput(xmlReader2);
                    parseUserSetXml(parser, list);
                } catch (Exception e2) {
                    e = e2;
                    xmlReader = xmlReader2;
                    try {
                        Slog.w(TAG, "getLocalSavedWhiteListSetByUserLocked: Got exception. ", e);
                        if (xmlReader != null) {
                            try {
                                xmlReader.close();
                            } catch (IOException e3) {
                                Slog.w(TAG, "getLocalSavedWhiteListSetByUserLocked: Got exception close xmlReader. ", e3);
                            }
                        }
                        return list;
                    } catch (Throwable th2) {
                        th = th2;
                        if (xmlReader != null) {
                            try {
                                xmlReader.close();
                            } catch (IOException e32) {
                                Slog.w(TAG, "getLocalSavedWhiteListSetByUserLocked: Got exception close xmlReader. ", e32);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    xmlReader = xmlReader2;
                    if (xmlReader != null) {
                    }
                    throw th;
                }
            }
            if (xmlReader2 != null) {
                try {
                    xmlReader2.close();
                } catch (IOException e322) {
                    Slog.w(TAG, "getLocalSavedWhiteListSetByUserLocked: Got exception close xmlReader. ", e322);
                }
            }
            xmlReader = xmlReader2;
        } catch (Exception e4) {
            e = e4;
            Slog.w(TAG, "getLocalSavedWhiteListSetByUserLocked: Got exception. ", e);
            if (xmlReader != null) {
            }
            return list;
        }
        return list;
    }

    private void parseUserSetXml(XmlPullParser parser, ArrayList<String> list) {
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                switch (eventType) {
                    case 2:
                        String strName = parser.getName();
                        eventType = parser.next();
                        String strText = parser.getText();
                        if (TAG_ALL_WHITE_LIST.equals(strName) && !list.contains(strText)) {
                            list.add(strText);
                            break;
                        }
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Slog.w(TAG, "parseXml: Got exception. ", e);
        }
    }

    private void addPkgToDeviceIdleController(ArrayList<String> listPkg) {
        for (int i = 0; i < listPkg.size(); i++) {
            String pkgName = (String) listPkg.get(i);
            if (pkgName != null && !this.mDeviceIdleController.isPowerSaveWhitelistAppInternal(pkgName) && this.mDeviceIdleController.addPowerSaveWhitelistAppInternal(pkgName) && sOppoDebug) {
                Slog.d(TAG, "add to DeviceIdleController: " + pkgName);
            }
        }
    }

    private void weakWhiteListHandle() {
        if (this.mRestrictWeakListApp && !this.mWeakWhiteList.isEmpty()) {
            long nowElapsed = SystemClock.elapsedRealtime();
            long screenOffInterval = nowElapsed - this.mTimestampScreenoff;
            if (screenOffInterval > 0) {
                Calendar cNow = Calendar.getInstance();
                cNow.setTimeInMillis(System.currentTimeMillis());
                int hour = cNow.get(11);
                if (hour >= 0) {
                    Calendar cTomorrow;
                    if (screenOffInterval < this.mDelayWeakWhiteListInactive) {
                        long minRemainDelay = this.mDelayWeakWhiteListInactive - screenOffInterval;
                        if (hour <= this.mThreshHourRestrictWeakList) {
                            this.mAlarmManager.setWindow(2, nowElapsed + minRemainDelay, ALARM_WINDOW_LENGTH, this.mWeakListIntent);
                            this.mHasAlarmPending = true;
                            if (sOppoDebug) {
                                Slog.d(TAG, "set alarm. triggerTime=" + (nowElapsed + minRemainDelay) + ", minRemainDelay=" + minRemainDelay);
                            }
                        } else {
                            cTomorrow = getCalendarOfTomorrowZeroOclock();
                            if (cTomorrow.getTimeInMillis() - cNow.getTimeInMillis() > minRemainDelay) {
                                this.mAlarmManager.setWindow(0, cTomorrow.getTimeInMillis(), ALARM_WINDOW_LENGTH, this.mWeakListIntent);
                                this.mHasAlarmPending = true;
                                if (sOppoDebug) {
                                    Slog.d(TAG, "set alarm. " + cTomorrow.getTime());
                                }
                            } else {
                                this.mAlarmManager.setWindow(2, nowElapsed + minRemainDelay, ALARM_WINDOW_LENGTH, this.mWeakListIntent);
                                this.mHasAlarmPending = true;
                                if (sOppoDebug) {
                                    Slog.d(TAG, "set alarm. triggerTime=" + (nowElapsed + minRemainDelay) + ", minRemainDelay=" + minRemainDelay);
                                }
                            }
                        }
                    } else if (hour <= this.mThreshHourRestrictWeakList) {
                        weakeWhiteListInactive();
                    } else {
                        cTomorrow = getCalendarOfTomorrowZeroOclock();
                        this.mAlarmManager.setWindow(0, cTomorrow.getTimeInMillis(), ALARM_WINDOW_LENGTH, this.mWeakListIntent);
                        this.mHasAlarmPending = true;
                        if (sOppoDebug) {
                            Slog.d(TAG, "set alarm. " + cTomorrow.getTime());
                        }
                    }
                }
            }
        }
    }

    private Calendar getCalendarOfTomorrowZeroOclock() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(11, 0);
        c.set(12, 0);
        c.set(13, 0);
        c.set(14, 0);
        c.add(5, 1);
        return c;
    }

    private void scheduleUploadAlarm() {
        this.mAlarmManager.setExact(3, SystemClock.elapsedRealtime() + UnixCalendar.DAY_IN_MILLIS, this.mUploadIntent);
    }

    private void updateWhiteList() {
        synchronized (this.mLock) {
            ArrayList<String> whiteListAll = new ArrayList();
            ArrayList<String> whiteListWeak = new ArrayList();
            getNewWhiteListLocked(whiteListAll, whiteListWeak);
            getLocalSavedWhiteListLocked();
            if (whiteListChangedHandle(whiteListAll, whiteListWeak)) {
                saveLocalWhiteListLocked();
            }
            this.mConstants.onChange(true, null);
        }
    }

    private void getNewWhiteListLocked(ArrayList<String> whiteListAll, ArrayList<String> whiteListWeak) {
        getListFromProvider(whiteListAll, whiteListWeak);
        getListFromSystem(whiteListAll, whiteListWeak);
        getCustomizeWhiteList(whiteListAll);
    }

    private void weakeWhiteListInactive() {
        synchronized (this.mLock) {
            this.mWeakWhiteListThisTime.clear();
            for (int i = 0; i < this.mWeakWhiteList.size(); i++) {
                String pkgName = (String) this.mWeakWhiteList.get(i);
                if (pkgName != null && this.mDeviceIdleController.isPowerSaveWhitelistAppInternal(pkgName)) {
                    this.mDeviceIdleController.removePowerSaveWhitelistAppInternal(pkgName);
                    this.mWeakWhiteListThisTime.add(pkgName);
                    if (sOppoDebug) {
                        Slog.d(TAG, " remove weake whitlist from DeviceIdleConroller. " + pkgName);
                    }
                }
            }
            if (!this.mWeakWhiteListThisTime.isEmpty()) {
                saveRemovedWeakWhiteListLocked(this.mWeakWhiteListThisTime);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x00ac  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00a5  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getListFromProvider(ArrayList<String> whiteListAll, ArrayList<String> whiteListWeak) {
        Exception e;
        Throwable th;
        Cursor cursor = null;
        String strWhiteList = null;
        try {
            cursor = this.mContext.getContentResolver().query(CONTENT_URI_WHITE_LIST, new String[]{"version", COLUMN_NAME_2}, "filtername=\"sys_deviceidle_whitelist\"", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int versioncolumnIndex = cursor.getColumnIndex("version");
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_2);
                cursor.moveToNext();
                strWhiteList = cursor.getString(xmlcolumnIndex);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e2) {
            Slog.w(TAG, "getDataFromProvider: Got execption. " + e2);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th2) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (strWhiteList == null) {
            Slog.w(TAG, "getDataFromProvider: failed");
            return;
        }
        StringReader strReader = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            StringReader strReader2 = new StringReader(strWhiteList);
            try {
                parser.setInput(strReader2);
                parseXml(parser, whiteListAll, whiteListWeak);
                if (strReader2 != null) {
                    strReader2.close();
                }
                strReader = strReader2;
            } catch (Exception e3) {
                e2 = e3;
                strReader = strReader2;
                try {
                    Slog.w(TAG, "getDataFromProvider: Got execption. ", e2);
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (strReader != null) {
                        strReader.close();
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                strReader = strReader2;
                if (strReader != null) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e2 = e4;
            Slog.w(TAG, "getDataFromProvider: Got execption. ", e2);
            if (strReader != null) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x007f A:{SYNTHETIC, Splitter: B:36:0x007f} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0091 A:{SYNTHETIC, Splitter: B:42:0x0091} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getListFromSystem(ArrayList<String> whiteListAll, ArrayList<String> whiteListWeak) {
        Exception e;
        Throwable th;
        if (whiteListAll.isEmpty() || (whiteListWeak.isEmpty() ^ 1) == 0) {
            File file = new File(SYSTEM_XML_PATH);
            if (file.exists()) {
                FileReader xmlReader = null;
                ArrayList<String> listAll = new ArrayList();
                ArrayList<String> listWeak = new ArrayList();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    FileReader xmlReader2 = new FileReader(file);
                    if (xmlReader2 != null) {
                        try {
                            parser.setInput(xmlReader2);
                            parseXml(parser, listAll, listWeak);
                        } catch (Exception e2) {
                            e = e2;
                            xmlReader = xmlReader2;
                            try {
                                Slog.w(TAG, "getListFromSystem: Got execption. ", e);
                                if (xmlReader != null) {
                                }
                                whiteListAll.addAll(listAll);
                                whiteListWeak.addAll(listWeak);
                                return;
                            } catch (Throwable th2) {
                                th = th2;
                                if (xmlReader != null) {
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            xmlReader = xmlReader2;
                            if (xmlReader != null) {
                                try {
                                    xmlReader.close();
                                } catch (IOException e3) {
                                    Slog.w(TAG, "getListFromSystem: Got execption close xmlReader. ", e3);
                                }
                            }
                            throw th;
                        }
                    }
                    if (xmlReader2 != null) {
                        try {
                            xmlReader2.close();
                        } catch (IOException e32) {
                            Slog.w(TAG, "getListFromSystem: Got execption close xmlReader. ", e32);
                        }
                    }
                    xmlReader = xmlReader2;
                } catch (Exception e4) {
                    e = e4;
                    Slog.w(TAG, "getListFromSystem: Got execption. ", e);
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e322) {
                            Slog.w(TAG, "getListFromSystem: Got execption close xmlReader. ", e322);
                        }
                    }
                    whiteListAll.addAll(listAll);
                    whiteListWeak.addAll(listWeak);
                    return;
                }
                if (!listAll.isEmpty() && whiteListAll.isEmpty()) {
                    whiteListAll.addAll(listAll);
                }
                if (!listWeak.isEmpty() && whiteListWeak.isEmpty()) {
                    whiteListWeak.addAll(listWeak);
                }
                return;
            }
            return;
        }
        Slog.d(TAG, "getDataFromSystem: no need");
    }

    private void parseXml(XmlPullParser parser, ArrayList<String> listAll, ArrayList<String> listWeak) {
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                switch (eventType) {
                    case 2:
                        String strName = parser.getName();
                        eventType = parser.next();
                        String strText = parser.getText();
                        if (!TAG_ALL_WHITE_LIST.equals(strName)) {
                            if (!TAG_WEAK_LIST.equals(strName)) {
                                if (!TAG_TOTAL_INTERVAL_TO_IDLE.equals(strName)) {
                                    if (!TAG_RESTRICT_WEAK_LIST.equals(strName)) {
                                        if (!TAG_THRESH_HOUR_RESTRICT_WEAKLIST.equals(strName)) {
                                            if (!TAG_AUTO_POWER_SAVE_MODES_ENABLED.equals(strName)) {
                                                break;
                                            }
                                            try {
                                                boolean powerModesEnabled = Boolean.parseBoolean(strText);
                                                if (powerModesEnabled != this.mAutoPowerModesEnabled) {
                                                    this.mAutoPowerModesEnabled = powerModesEnabled;
                                                    this.mUsageStats.updateAutoPowerSaveEnable(powerModesEnabled);
                                                    saveDozeCofigLocked();
                                                }
                                            } catch (NumberFormatException e) {
                                                Slog.w(TAG, "auto power save excption.", e);
                                            }
                                            if (!sOppoDebug) {
                                                break;
                                            }
                                            Slog.d(TAG, "auto power save:" + this.mAutoPowerModesEnabled);
                                            break;
                                        }
                                        try {
                                            this.mThreshHourRestrictWeakList = Integer.parseInt(strText);
                                        } catch (NumberFormatException e2) {
                                            this.mThreshHourRestrictWeakList = 4;
                                            Slog.w(TAG, "thresh hour restrict weakelist excption.", e2);
                                        }
                                        if (!sOppoDebug) {
                                            break;
                                        }
                                        Slog.d(TAG, "thresh hour restrict weakelist:" + this.mThreshHourRestrictWeakList);
                                        break;
                                    }
                                    try {
                                        this.mRestrictWeakListApp = Boolean.parseBoolean(strText);
                                    } catch (NumberFormatException e22) {
                                        this.mRestrictWeakListApp = false;
                                        Slog.w(TAG, "Restrict WeakeList excption.", e22);
                                    }
                                    if (!sOppoDebug) {
                                        break;
                                    }
                                    Slog.d(TAG, "restrict weaklist:" + this.mRestrictWeakListApp);
                                    break;
                                }
                                try {
                                    this.mTotalIntervalToIdle = Long.parseLong(strText) * 1000;
                                } catch (NumberFormatException e222) {
                                    this.mTotalIntervalToIdle = 1800000;
                                    Slog.w(TAG, "IntervalToIdle excption.", e222);
                                }
                                if (!sOppoDebug) {
                                    break;
                                }
                                Slog.d(TAG, "interval to idle:" + this.mTotalIntervalToIdle);
                                break;
                            } else if (!listWeak.contains(strText)) {
                                listWeak.add(strText);
                                break;
                            } else {
                                break;
                            }
                        } else if (!listAll.contains(strText)) {
                            listAll.add(strText);
                            break;
                        } else {
                            break;
                        }
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e3) {
            Slog.w(TAG, "parseXml: Got execption. ", e3);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x007e  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00bc A:{SYNTHETIC, Splitter: B:42:0x00bc} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x007e  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00df A:{SYNTHETIC, Splitter: B:48:0x00df} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getCustomizeWhiteList(ArrayList<String> whiteListAll) {
        Exception e;
        int i;
        Throwable th;
        if (this.mContext.getPackageManager() != null && (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom") ^ 1) == 0) {
            File file = new File(OPPO_CUSTOMIZE_WHITE_FILE_PATH);
            if (file.exists()) {
                String pkgName;
                ArrayList<String> listPkg = new ArrayList();
                FileReader xmlReader = null;
                try {
                    FileReader xmlReader2 = new FileReader(file);
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(xmlReader2);
                        int type;
                        do {
                            type = parser.next();
                            if (type == 2) {
                                if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName())) {
                                    pkgName = parser.getAttributeValue(null, "att");
                                    if (pkgName != null) {
                                        listPkg.add(pkgName);
                                    }
                                }
                            }
                        } while (type != 1);
                        if (xmlReader2 != null) {
                            try {
                                xmlReader2.close();
                            } catch (IOException e2) {
                                Slog.e(TAG, "Failed to close state FileInputStream " + e2);
                            }
                        }
                        xmlReader = xmlReader2;
                    } catch (Exception e3) {
                        e = e3;
                        xmlReader = xmlReader2;
                        try {
                            Slog.e(TAG, "failed parsing ", e);
                            if (xmlReader != null) {
                            }
                            while (i < listPkg.size()) {
                            }
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            if (xmlReader != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        xmlReader = xmlReader2;
                        if (xmlReader != null) {
                            try {
                                xmlReader.close();
                            } catch (IOException e22) {
                                Slog.e(TAG, "Failed to close state FileInputStream " + e22);
                            }
                        }
                        throw th;
                    }
                } catch (Exception e4) {
                    e = e4;
                    Slog.e(TAG, "failed parsing ", e);
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e222) {
                            Slog.e(TAG, "Failed to close state FileInputStream " + e222);
                        }
                    }
                    while (i < listPkg.size()) {
                    }
                    return;
                }
                for (i = 0; i < listPkg.size(); i++) {
                    pkgName = (String) listPkg.get(i);
                    if (!whiteListAll.contains(pkgName)) {
                        whiteListAll.add(pkgName);
                    }
                }
                return;
            }
            if (sOppoDebug) {
                Slog.e(TAG, "readCustomizeWhiteList failed: file doesn't exist!");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0037  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x005f A:{SYNTHETIC, Splitter: B:26:0x005f} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0037  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0071 A:{SYNTHETIC, Splitter: B:32:0x0071} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getLocalSavedWhiteListLocked() {
        Exception e;
        Throwable th;
        File file = new File(SAVED_WHITELIST_XML_PATH);
        if (file.exists()) {
            FileReader xmlReader = null;
            ArrayList<String> listAll = new ArrayList();
            ArrayList<String> listWeak = new ArrayList();
            try {
                XmlPullParser parser = Xml.newPullParser();
                FileReader xmlReader2 = new FileReader(file);
                if (xmlReader2 != null) {
                    try {
                        parser.setInput(xmlReader2);
                        parseXml(parser, listAll, listWeak);
                    } catch (Exception e2) {
                        e = e2;
                        xmlReader = xmlReader2;
                        try {
                            Slog.w(TAG, "getLocalSavedWhiteList: Got execption. ", e);
                            if (xmlReader != null) {
                            }
                            if (!listAll.isEmpty()) {
                            }
                            if (!listWeak.isEmpty()) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (xmlReader != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        xmlReader = xmlReader2;
                        if (xmlReader != null) {
                            try {
                                xmlReader.close();
                            } catch (IOException e3) {
                                Slog.w(TAG, "getLocalSavedWhiteList: Got execption close xmlReader. ", e3);
                            }
                        }
                        throw th;
                    }
                }
                if (xmlReader2 != null) {
                    try {
                        xmlReader2.close();
                    } catch (IOException e32) {
                        Slog.w(TAG, "getLocalSavedWhiteList: Got execption close xmlReader. ", e32);
                    }
                }
                xmlReader = xmlReader2;
            } catch (Exception e4) {
                e = e4;
                Slog.w(TAG, "getLocalSavedWhiteList: Got execption. ", e);
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e322) {
                        Slog.w(TAG, "getLocalSavedWhiteList: Got execption close xmlReader. ", e322);
                    }
                }
                if (listAll.isEmpty()) {
                }
                if (listWeak.isEmpty()) {
                }
            }
            if (listAll.isEmpty()) {
                this.mWhiteListAll.addAll(listAll);
            }
            if (listWeak.isEmpty()) {
                this.mWeakWhiteList.addAll(listWeak);
            }
        }
    }

    private boolean whiteListChangedHandle(ArrayList<String> whiteListAll, ArrayList<String> whiteListWeak) {
        int i;
        String pkgName;
        boolean listChanged = false;
        boolean isAutoPowerProtectOn = isAutoPowerProtectOn(this.mContext);
        List<String> listAllow = new ArrayList();
        List<String> listProhibit = new ArrayList();
        if (!isAutoPowerProtectOn) {
            getAllowProhibitList(listAllow, listProhibit);
        }
        for (i = 0; i < whiteListAll.size(); i++) {
            pkgName = (String) whiteListAll.get(i);
            if (!(pkgName == null || this.mWhiteListAll.contains(pkgName))) {
                listChanged = true;
                this.mWhiteListAll.add(pkgName);
                if (isAutoPowerProtectOn || !listProhibit.contains(pkgName)) {
                    if (!this.mDeviceIdleController.isPowerSaveWhitelistAppInternal(pkgName) && this.mDeviceIdleController.addPowerSaveWhitelistAppInternal(pkgName) && sOppoDebug) {
                        Slog.d(TAG, "whiteListChanged: addPowerSaveWhitelist " + pkgName);
                    }
                } else if (sOppoDebug) {
                    Slog.d(TAG, "whiteListChanged: is prohibit. not add " + pkgName);
                }
            }
        }
        for (i = this.mWhiteListAll.size() - 1; i >= 0; i--) {
            pkgName = (String) this.mWhiteListAll.get(i);
            if (!(pkgName == null || whiteListAll.contains(pkgName))) {
                listChanged = true;
                this.mWhiteListAll.remove(pkgName);
                if (sOppoDebug) {
                    Slog.d(TAG, "whiteListChanged: remove old pkg (" + pkgName + ") ");
                }
                if (isAutoPowerProtectOn || !listAllow.contains(pkgName)) {
                    if (this.mDeviceIdleController.isPowerSaveWhitelistAppInternal(pkgName)) {
                        this.mDeviceIdleController.removePowerSaveWhitelistAppInternal(pkgName);
                        if (sOppoDebug) {
                            Slog.d(TAG, "whiteListChanged: removePowerSaveWhitelist " + pkgName);
                        }
                    }
                } else if (sOppoDebug) {
                    Slog.d(TAG, "whiteListChanged: is allow. not remove " + pkgName);
                }
            }
        }
        for (i = 0; i < whiteListWeak.size(); i++) {
            pkgName = (String) whiteListWeak.get(i);
            if (!(pkgName == null || this.mWeakWhiteList.contains(pkgName))) {
                listChanged = true;
                this.mWeakWhiteList.add(pkgName);
                if (sOppoDebug) {
                    Slog.d(TAG, "whiteListChanged: add new weak list pkg (" + pkgName + ")");
                }
            }
        }
        for (i = this.mWeakWhiteList.size() - 1; i >= 0; i--) {
            pkgName = (String) this.mWeakWhiteList.get(i);
            if (!(pkgName == null || whiteListWeak.contains(pkgName))) {
                listChanged = true;
                this.mWeakWhiteList.remove(pkgName);
                if (sOppoDebug) {
                    Slog.d(TAG, "whiteListChanged: remove old weak list pkg (" + pkgName + ")");
                }
            }
        }
        return listChanged;
    }

    /* JADX WARNING: Removed duplicated region for block: B:71:0x01ee A:{SYNTHETIC, Splitter: B:71:0x01ee} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x01c9 A:{SYNTHETIC, Splitter: B:65:0x01c9} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x018a A:{SYNTHETIC, Splitter: B:57:0x018a} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x014c A:{SYNTHETIC, Splitter: B:49:0x014c} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x010f A:{SYNTHETIC, Splitter: B:41:0x010f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveLocalWhiteListLocked() {
        IOException e;
        IllegalArgumentException e2;
        IllegalStateException e3;
        Exception e4;
        Throwable th;
        File file = new File(SAVED_WHITELIST_XML_PATH);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Slog.i(TAG, "saveLocalWhiteListLocked: failed create file /data/oppo/coloros/oppoguardelf/doze_wl_local.xml");
                }
            } catch (IOException e5) {
                Slog.i(TAG, "failed create file " + e5);
            }
        }
        if (file.exists()) {
            FileOutputStream fileos = null;
            try {
                FileOutputStream fileos2 = new FileOutputStream(file);
                try {
                    int i;
                    String pkg;
                    XmlSerializer serializer = Xml.newSerializer();
                    serializer.setOutput(fileos2, "UTF-8");
                    serializer.startDocument(null, Boolean.valueOf(true));
                    serializer.startTag(null, "gs");
                    serializer.startTag(null, "filter-name");
                    serializer.text("doze_local");
                    serializer.endTag(null, "filter-name");
                    for (i = 0; i < this.mWhiteListAll.size(); i++) {
                        pkg = (String) this.mWhiteListAll.get(i);
                        if (pkg != null) {
                            serializer.startTag(null, TAG_ALL_WHITE_LIST);
                            serializer.text(pkg);
                            serializer.endTag(null, TAG_ALL_WHITE_LIST);
                        }
                    }
                    for (i = 0; i < this.mWeakWhiteList.size(); i++) {
                        pkg = (String) this.mWeakWhiteList.get(i);
                        if (pkg != null) {
                            serializer.startTag(null, TAG_WEAK_LIST);
                            serializer.text(pkg);
                            serializer.endTag(null, TAG_WEAK_LIST);
                        }
                    }
                    serializer.endTag(null, "gs");
                    serializer.endDocument();
                    serializer.flush();
                    if (fileos2 != null) {
                        try {
                            fileos2.close();
                        } catch (IOException e52) {
                            Slog.i(TAG, "failed close stream " + e52);
                        }
                    }
                    fileos = fileos2;
                } catch (IllegalArgumentException e6) {
                    e2 = e6;
                    fileos = fileos2;
                    Slog.i(TAG, "failed write file " + e2);
                    if (fileos != null) {
                        try {
                            fileos.close();
                        } catch (IOException e522) {
                            Slog.i(TAG, "failed close stream " + e522);
                        }
                    }
                } catch (IllegalStateException e7) {
                    e3 = e7;
                    fileos = fileos2;
                    Slog.i(TAG, "failed write file " + e3);
                    if (fileos != null) {
                        try {
                            fileos.close();
                        } catch (IOException e5222) {
                            Slog.i(TAG, "failed close stream " + e5222);
                        }
                    }
                } catch (IOException e8) {
                    e5222 = e8;
                    fileos = fileos2;
                    Slog.i(TAG, "failed write file " + e5222);
                    if (fileos != null) {
                        try {
                            fileos.close();
                        } catch (IOException e52222) {
                            Slog.i(TAG, "failed close stream " + e52222);
                        }
                    }
                } catch (Exception e9) {
                    e4 = e9;
                    fileos = fileos2;
                    try {
                        Slog.i(TAG, "failed write file " + e4);
                        if (fileos != null) {
                            try {
                                fileos.close();
                            } catch (IOException e522222) {
                                Slog.i(TAG, "failed close stream " + e522222);
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileos != null) {
                            try {
                                fileos.close();
                            } catch (IOException e5222222) {
                                Slog.i(TAG, "failed close stream " + e5222222);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileos = fileos2;
                    if (fileos != null) {
                    }
                    throw th;
                }
            } catch (IllegalArgumentException e10) {
                e2 = e10;
                Slog.i(TAG, "failed write file " + e2);
                if (fileos != null) {
                }
            } catch (IllegalStateException e11) {
                e3 = e11;
                Slog.i(TAG, "failed write file " + e3);
                if (fileos != null) {
                }
            } catch (IOException e12) {
                e5222222 = e12;
                Slog.i(TAG, "failed write file " + e5222222);
                if (fileos != null) {
                }
            } catch (Exception e13) {
                e4 = e13;
                Slog.i(TAG, "failed write file " + e4);
                if (fileos != null) {
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:57:0x019c A:{SYNTHETIC, Splitter: B:57:0x019c} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x015d A:{SYNTHETIC, Splitter: B:49:0x015d} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x011e A:{SYNTHETIC, Splitter: B:41:0x011e} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x01c1 A:{SYNTHETIC, Splitter: B:63:0x01c1} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveDozeCofigLocked() {
        IOException e;
        IllegalArgumentException e2;
        IllegalStateException e3;
        Exception e4;
        Throwable th;
        File file = new File(DOZE_CONFIG_XML_PATH);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Slog.i(TAG, "saveDozeCofigLocked: failed create file /data/system/doze_config_local.xml");
                }
            } catch (IOException e5) {
                Slog.i(TAG, "failed create file " + e5);
            }
        }
        if (file.exists()) {
            FileOutputStream fileos = null;
            try {
                FileOutputStream fileos2 = new FileOutputStream(file);
                try {
                    XmlSerializer serializer = Xml.newSerializer();
                    serializer.setOutput(fileos2, "UTF-8");
                    serializer.startDocument(null, Boolean.valueOf(true));
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
                    if (fileos2 != null) {
                        try {
                            fileos2.close();
                        } catch (IOException e52) {
                            Slog.i(TAG, "failed close stream " + e52);
                        }
                    }
                    fileos = fileos2;
                } catch (IllegalArgumentException e6) {
                    e2 = e6;
                    fileos = fileos2;
                } catch (IllegalStateException e7) {
                    e3 = e7;
                    fileos = fileos2;
                    Slog.i(TAG, "failed write file " + e3);
                    if (fileos != null) {
                    }
                } catch (IOException e8) {
                    e52 = e8;
                    fileos = fileos2;
                    Slog.i(TAG, "failed write file " + e52);
                    if (fileos != null) {
                    }
                } catch (Exception e9) {
                    e4 = e9;
                    fileos = fileos2;
                    Slog.i(TAG, "failed write file " + e4);
                    if (fileos != null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    fileos = fileos2;
                    if (fileos != null) {
                    }
                    throw th;
                }
            } catch (IllegalArgumentException e10) {
                e2 = e10;
                try {
                    Slog.i(TAG, "failed write file " + e2);
                    if (fileos != null) {
                        try {
                            fileos.close();
                        } catch (IOException e522) {
                            Slog.i(TAG, "failed close stream " + e522);
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (fileos != null) {
                        try {
                            fileos.close();
                        } catch (IOException e5222) {
                            Slog.i(TAG, "failed close stream " + e5222);
                        }
                    }
                    throw th;
                }
            } catch (IllegalStateException e11) {
                e3 = e11;
                Slog.i(TAG, "failed write file " + e3);
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e52222) {
                        Slog.i(TAG, "failed close stream " + e52222);
                    }
                }
            } catch (IOException e12) {
                e52222 = e12;
                Slog.i(TAG, "failed write file " + e52222);
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e522222) {
                        Slog.i(TAG, "failed close stream " + e522222);
                    }
                }
            } catch (Exception e13) {
                e4 = e13;
                Slog.i(TAG, "failed write file " + e4);
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e5222222) {
                        Slog.i(TAG, "failed close stream " + e5222222);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0053 A:{SYNTHETIC, Splitter: B:26:0x0053} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getLocalDozeCofigLocked() {
        Exception e;
        Throwable th;
        int res = -1;
        File file = new File(DOZE_CONFIG_XML_PATH);
        if (!file.exists()) {
            return res;
        }
        FileReader xmlReader = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            FileReader xmlReader2 = new FileReader(file);
            if (xmlReader2 != null) {
                try {
                    parser.setInput(xmlReader2);
                    res = parseDozeCofigXml(parser);
                } catch (Exception e2) {
                    e = e2;
                    xmlReader = xmlReader2;
                } catch (Throwable th2) {
                    th = th2;
                    xmlReader = xmlReader2;
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e3) {
                            Slog.w(TAG, "getDozeCofigLocked: Got execption close xmlReader. ", e3);
                        }
                    }
                    throw th;
                }
            }
            if (xmlReader2 != null) {
                try {
                    xmlReader2.close();
                } catch (IOException e32) {
                    Slog.w(TAG, "getDozeCofigLocked: Got execption close xmlReader. ", e32);
                }
            }
            xmlReader = xmlReader2;
        } catch (Exception e4) {
            e = e4;
            try {
                Slog.w(TAG, "getDozeCofigLocked: Got execption. ", e);
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e322) {
                        Slog.w(TAG, "getDozeCofigLocked: Got execption close xmlReader. ", e322);
                    }
                }
                return res;
            } catch (Throwable th3) {
                th = th3;
                if (xmlReader != null) {
                }
                throw th;
            }
        }
        return res;
    }

    private int parseDozeCofigXml(XmlPullParser parser) {
        int res = -1;
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                switch (eventType) {
                    case 2:
                        String strName = parser.getName();
                        eventType = parser.next();
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
                            if (!sOppoDebug) {
                                break;
                            }
                            Slog.d(TAG, "parseDozeCofigXml: res=" + res);
                            break;
                        }
                        continue;
                    default:
                        break;
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

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00b9 A:{SYNTHETIC, Splitter: B:41:0x00b9} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a6 A:{SYNTHETIC, Splitter: B:35:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0033  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRemovedWeakListLocked() {
        Exception e;
        Throwable th;
        File file = new File(REMOVED_WEAK_LIST_XML_PATH);
        if (file.exists()) {
            FileReader xmlReader = null;
            ArrayList<String> listWeak = new ArrayList();
            try {
                XmlPullParser parser = Xml.newPullParser();
                FileReader xmlReader2 = new FileReader(file);
                if (xmlReader2 != null) {
                    try {
                        parser.setInput(xmlReader2);
                        parseRemovedWeakListLocked(parser, listWeak);
                    } catch (Exception e2) {
                        e = e2;
                        xmlReader = xmlReader2;
                        try {
                            Slog.w(TAG, "updateRemovedWeakListLocked: Got execption. ", e);
                            if (xmlReader != null) {
                                try {
                                    xmlReader.close();
                                } catch (IOException e3) {
                                    Slog.w(TAG, "updateRemovedWeakListLocked: Got execption close xmlReader. ", e3);
                                }
                            }
                            if (!listWeak.isEmpty()) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (xmlReader != null) {
                                try {
                                    xmlReader.close();
                                } catch (IOException e32) {
                                    Slog.w(TAG, "updateRemovedWeakListLocked: Got execption close xmlReader. ", e32);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        xmlReader = xmlReader2;
                        if (xmlReader != null) {
                        }
                        throw th;
                    }
                }
                if (xmlReader2 != null) {
                    try {
                        xmlReader2.close();
                    } catch (IOException e322) {
                        Slog.w(TAG, "updateRemovedWeakListLocked: Got execption close xmlReader. ", e322);
                    }
                }
                xmlReader = xmlReader2;
            } catch (Exception e4) {
                e = e4;
                Slog.w(TAG, "updateRemovedWeakListLocked: Got execption. ", e);
                if (xmlReader != null) {
                }
                if (listWeak.isEmpty()) {
                }
            }
            if (listWeak.isEmpty()) {
                for (int i = 0; i < listWeak.size(); i++) {
                    String pkgName = (String) listWeak.get(i);
                    if (sOppoDebug) {
                        Slog.d(TAG, "updateRemovedWeakListLocked: pkg= " + pkgName);
                    }
                    if (!this.mDeviceIdleController.isPowerSaveWhitelistAppInternal(pkgName)) {
                        if (this.mDeviceIdleController.addPowerSaveWhitelistAppInternal(pkgName)) {
                            if (sOppoDebug) {
                                Slog.d(TAG, "updateRemovedWeakListLocked: addPowerSaveWhitelist " + pkgName);
                            }
                        } else if (sOppoDebug) {
                            Slog.d(TAG, "updateRemovedWeakListLocked: not installed " + pkgName);
                        }
                    }
                }
                saveRemovedWeakWhiteListLocked(null);
            }
        }
    }

    private void parseRemovedWeakListLocked(XmlPullParser parser, ArrayList<String> listWeak) {
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                switch (eventType) {
                    case 2:
                        String strName = parser.getName();
                        eventType = parser.next();
                        String strText = parser.getText();
                        if (TAG_WEAK_LIST.equals(strName) && !listWeak.contains(strText)) {
                            listWeak.add(strText);
                            break;
                        }
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Slog.w(TAG, "parseXml: Got execption. ", e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:60:0x01a0 A:{SYNTHETIC, Splitter: B:60:0x01a0} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0161 A:{SYNTHETIC, Splitter: B:52:0x0161} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0123 A:{SYNTHETIC, Splitter: B:44:0x0123} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00e6 A:{SYNTHETIC, Splitter: B:36:0x00e6} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01c5 A:{SYNTHETIC, Splitter: B:66:0x01c5} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveRemovedWeakWhiteListLocked(List<String> listRmvedWeak) {
        IOException e;
        IllegalArgumentException e2;
        IllegalStateException e3;
        Exception e4;
        Throwable th;
        File file = new File(REMOVED_WEAK_LIST_XML_PATH);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Slog.i(TAG, "saveRemovedWeakWhiteListLocked: failed create file /data/system/doze_rmved_weak_list.xml");
                }
            } catch (IOException e5) {
                Slog.i(TAG, "failed create file " + e5);
            }
        }
        if (file.exists()) {
            FileOutputStream fileos = null;
            try {
                FileOutputStream fileos2 = new FileOutputStream(file);
                try {
                    XmlSerializer serializer = Xml.newSerializer();
                    serializer.setOutput(fileos2, "UTF-8");
                    serializer.startDocument(null, Boolean.valueOf(true));
                    serializer.startTag(null, "gs");
                    serializer.startTag(null, "filter-name");
                    serializer.text("removed_weak_list");
                    serializer.endTag(null, "filter-name");
                    if (listRmvedWeak != null) {
                        for (int i = 0; i < listRmvedWeak.size(); i++) {
                            String pkg = (String) listRmvedWeak.get(i);
                            if (pkg != null) {
                                serializer.startTag(null, TAG_WEAK_LIST);
                                serializer.text(pkg);
                                serializer.endTag(null, TAG_WEAK_LIST);
                            }
                        }
                    }
                    serializer.endTag(null, "gs");
                    serializer.endDocument();
                    serializer.flush();
                    if (fileos2 != null) {
                        try {
                            fileos2.close();
                        } catch (IOException e52) {
                            Slog.i(TAG, "failed close stream " + e52);
                        }
                    }
                    fileos = fileos2;
                } catch (IllegalArgumentException e6) {
                    e2 = e6;
                    fileos = fileos2;
                    Slog.i(TAG, "failed write file " + e2);
                    if (fileos != null) {
                    }
                } catch (IllegalStateException e7) {
                    e3 = e7;
                    fileos = fileos2;
                    Slog.i(TAG, "failed write file " + e3);
                    if (fileos != null) {
                    }
                } catch (IOException e8) {
                    e52 = e8;
                    fileos = fileos2;
                    Slog.i(TAG, "failed write file " + e52);
                    if (fileos != null) {
                    }
                } catch (Exception e9) {
                    e4 = e9;
                    fileos = fileos2;
                    try {
                        Slog.i(TAG, "failed write file " + e4);
                        if (fileos != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileos != null) {
                            try {
                                fileos.close();
                            } catch (IOException e522) {
                                Slog.i(TAG, "failed close stream " + e522);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileos = fileos2;
                    if (fileos != null) {
                    }
                    throw th;
                }
            } catch (IllegalArgumentException e10) {
                e2 = e10;
                Slog.i(TAG, "failed write file " + e2);
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e5222) {
                        Slog.i(TAG, "failed close stream " + e5222);
                    }
                }
            } catch (IllegalStateException e11) {
                e3 = e11;
                Slog.i(TAG, "failed write file " + e3);
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e52222) {
                        Slog.i(TAG, "failed close stream " + e52222);
                    }
                }
            } catch (IOException e12) {
                e52222 = e12;
                Slog.i(TAG, "failed write file " + e52222);
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e522222) {
                        Slog.i(TAG, "failed close stream " + e522222);
                    }
                }
            } catch (Exception e13) {
                e4 = e13;
                Slog.i(TAG, "failed write file " + e4);
                if (fileos != null) {
                    try {
                        fileos.close();
                    } catch (IOException e5222222) {
                        Slog.i(TAG, "failed close stream " + e5222222);
                    }
                }
            }
        }
    }

    private boolean isAutoPowerProtectOn(Context context) {
        if (System.getInt(context.getContentResolver(), AUTO_POWER_PROTECT_SWITCH, 1) != 0) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0056 A:{SYNTHETIC, Splitter: B:29:0x0056} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0044 A:{SYNTHETIC, Splitter: B:23:0x0044} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getAllowProhibitList(List<String> listAllow, List<String> listProhibit) {
        Throwable th;
        if (listAllow != null && listProhibit != null) {
            File file = new File(ALLOW_PROHIBIT_APP_FILE);
            if (file.exists()) {
                FileReader xmlReader = null;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    FileReader xmlReader2 = new FileReader(file);
                    if (xmlReader2 != null) {
                        try {
                            parser.setInput(xmlReader2);
                            parseAllowProhibitList(parser, listAllow, listProhibit);
                        } catch (Exception e) {
                            xmlReader = xmlReader2;
                            try {
                                Slog.d(TAG, "getAllowProhibitList: Got execption.");
                                if (xmlReader != null) {
                                    try {
                                        xmlReader.close();
                                    } catch (IOException e2) {
                                        Slog.d(TAG, "getAllowProhibitList: Got execption close xmlReader IOException. ");
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (xmlReader != null) {
                                    try {
                                        xmlReader.close();
                                    } catch (IOException e3) {
                                        Slog.d(TAG, "getAllowProhibitList: Got execption close xmlReader IOException. ");
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            xmlReader = xmlReader2;
                            if (xmlReader != null) {
                            }
                            throw th;
                        }
                    }
                    if (xmlReader2 != null) {
                        try {
                            xmlReader2.close();
                        } catch (IOException e4) {
                            Slog.d(TAG, "getAllowProhibitList: Got execption close xmlReader IOException. ");
                        }
                    }
                    xmlReader = xmlReader2;
                } catch (Exception e5) {
                    Slog.d(TAG, "getAllowProhibitList: Got execption.");
                    if (xmlReader != null) {
                    }
                }
            }
        }
    }

    private void parseAllowProhibitList(XmlPullParser parser, List<String> listAllow, List<String> listProhibit) {
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                switch (eventType) {
                    case 2:
                        String strName = parser.getName();
                        eventType = parser.next();
                        String strText = parser.getText();
                        if (!TAG_ALLOW_BACK_RUN.equals(strName)) {
                            if (TAG_PROHIBIT_BACK_RUN.equals(strName) && !listProhibit.contains(strText)) {
                                listProhibit.add(strText);
                                break;
                            }
                        } else if (!listAllow.contains(strText)) {
                            listAllow.add(strText);
                            break;
                        } else {
                            break;
                        }
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Slog.d(TAG, "parseXml: Got execption.");
        }
    }

    public void dump(PrintWriter pw) {
        synchronized (this.mLock) {
            pw.println("DftWhiteList:");
            for (int j = 0; j < this.mWhiteListAll.size(); j++) {
                pw.println("  " + ((String) this.mWhiteListAll.get(j)));
            }
        }
    }
}
