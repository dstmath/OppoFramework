package com.android.server.display.stat;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.usb.descriptors.UsbTerminalTypes;
import com.oppo.atlas.OppoAtlasManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BackLightStat {
    public static final String ACTION_BRIGHTNESS_MODE = "backlight.stat.action.BRIGHTNESS_MODE";
    public static final String ACTION_LUX_CHANGE = "backlight.stat.action.LUX_CHANGE";
    public static final String ACTION_ON_ALARM = "backlight.stat.action.ON_ALARM";
    public static final String ACTION_SAVE_POWER_MODE = "backlight.stat.action.SAVE_POWER_MODE";
    public static final String ACTION_SET_SPEC_BTN = "backlight.stat.action.SET_SPEC_BTN";
    public static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final int DEFAULT_HOUR_OF_DAY = 17;
    private static final String DEFAULT_MANUFACTURE = "UNKNOWN";
    public static final int DEFAULT_MINUTE = 0;
    public static final int DEFUALT_INTERVAL_DAY = 1;
    public static final int DEFUALT_SECOND = 0;
    private static final String DEVICE_INFO_PATH = "/proc/devinfo/lcd";
    private static final String KEY_DEVICE_MANUFACTURE = "Device manufacture:";
    private static final String KEY_DEVICE_VERSION = "Device version:";
    public static final String KEY_LCD_MANU = "lcd_manu";
    public static final String KEY_UPLOAD_REASON = "upload_reason";
    public static final String KEY_UPLOAD_TIME = "upload_time";
    public static final String KEY_VERSION = "backlight_ver";
    public static final int MSG_REPORT_ALARM_TIME_UP = 1001;
    public static final int MSG_REPORT_BACKLIGHT_INFO = 1000;
    private static final String TAG = "BackLightStat";
    public static final String VALUE_UPLOAD_CACHE_FULL = "cache_full";
    public static final String VALUE_UPLOAD_ON_ALARM = "on_alarm";
    public static final String VALUE_UPLOAD_REBOOT = "reboot";
    public static final String VALUE_UPLOAD_SHUT_DOWN = "shut_down";
    private static volatile BackLightStat sBackLightStat;
    private AlarmManager mAlarmManager;
    private AppBrightnessStat mAppStat = null;
    private ArrayList<Callback> mCallbacks = new ArrayList<>(5);
    private Context mContext = null;
    private String mCurrPkg = null;
    private int mCurrTarget = -1;
    private DisplayXmlParser mDispXmlParser;
    private EventBrightnessStat mEventStat = null;
    private BackLightStatHandler mHandler = null;
    private String mLastPkg = null;
    private long mLastScreenOffTime = 0;
    private final Object mLock = new Object();
    private int mLowBtn = -1;
    private String mManufacture = DEFAULT_MANUFACTURE;
    private int mMaxBtn = -1;
    private int mSavePowerMode = -1;
    private long mScreenOffTotalTime = 0;
    private int mScreenState = 0;
    private SettingsObserver mSettingsObserver;
    private TimeBrightnessStat mTimeStat = null;

    /* access modifiers changed from: package-private */
    public interface Callback {
        void onReceive(String str, Object... objArr);
    }

    private BackLightStat(Context context) {
        this.mContext = context;
    }

    public static BackLightStat getInstance(Context context) {
        if (sBackLightStat == null) {
            synchronized (BackLightStat.class) {
                if (sBackLightStat == null) {
                    sBackLightStat = new BackLightStat(context);
                }
            }
        }
        return sBackLightStat;
    }

    private void registerReceiver(Callback callback) {
        if (callback != null) {
            this.mCallbacks.add(callback);
        }
    }

    private void retryInitVariable() {
        int i = this.mMaxBtn;
        if (i > 0) {
            setMaxBtn(i);
        }
        int i2 = this.mLowBtn;
        if (i2 > 0) {
            setLowBtn(i2);
        }
        int i3 = this.mSavePowerMode;
        if (i3 != -1) {
            handleSavePowerMode(i3);
        }
        int i4 = this.mCurrTarget;
        if (i4 != -1) {
            setCurrTarget(this.mScreenState, i4, false);
        }
    }

    public void onBootPhase(int phase) {
        if (phase == 1000) {
            HandlerThread thread = new HandlerThread(TAG);
            thread.start();
            this.mHandler = new BackLightStatHandler(thread.getLooper());
            this.mCallbacks.clear();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.ACTION_SHUTDOWN");
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.TIME_SET");
            filter.addAction("android.intent.action.TIMEZONE_CHANGED");
            filter.addAction("android.intent.action.REBOOT");
            this.mContext.registerReceiver(new EventReceiver(), filter, null, this.mHandler);
            ContentResolver resolver = this.mContext.getContentResolver();
            this.mSettingsObserver = new SettingsObserver(this.mHandler);
            resolver.registerContentObserver(Settings.System.getUriFor("screen_brightness_mode"), false, this.mSettingsObserver, -1);
            synchronized (this.mLock) {
                this.mAppStat = AppBrightnessStat.getInstance(this.mContext, this);
                this.mAppStat.init(this.mHandler);
                registerReceiver(this.mAppStat);
                this.mTimeStat = TimeBrightnessStat.getInstance(this.mContext, this);
                this.mTimeStat.init(this.mHandler);
                registerReceiver(this.mTimeStat);
                this.mEventStat = EventBrightnessStat.getInstance(this.mContext, this);
                this.mEventStat.init(this.mHandler);
                registerReceiver(this.mEventStat);
            }
            this.mManufacture = getDeviceManufacture();
            retryInitVariable();
            scheduleReportData();
        } else if (phase == 500) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
            this.mDispXmlParser = DisplayXmlParser.getInstance(this.mContext);
            DisplayXmlParser displayXmlParser = this.mDispXmlParser;
            if (displayXmlParser != null) {
                displayXmlParser.initUpdateBroadcastReceiver();
            }
        }
        Slog.d(TAG, "bootPhase=" + phase);
    }

    public void setScreenState(int state) {
        if (this.mScreenState != state) {
            if (DEBUG) {
                Slog.d(TAG, "setScreenState: state=" + state);
            }
            this.mScreenState = state;
            long now = SystemClock.elapsedRealtime();
            if (state == 2) {
                long j = this.mLastScreenOffTime;
                if (j != 0) {
                    this.mScreenOffTotalTime += now - j;
                }
            } else if (state == 1) {
                this.mLastScreenOffTime = now;
            }
        }
    }

    public void setCurrTarget(int state, int currTarget, boolean byUser) {
        this.mCurrTarget = currTarget;
        this.mScreenState = state;
        synchronized (this.mLock) {
            if (this.mAppStat != null) {
                this.mAppStat.setCurrTarget(state, currTarget, byUser);
            }
            if (this.mTimeStat != null) {
                this.mTimeStat.setCurrTarget(state, currTarget, byUser);
            }
            if (this.mEventStat != null) {
                this.mEventStat.setCurrTarget(state, currTarget, byUser);
            }
        }
    }

    public void setLux(int lux) {
        handleLux(lux);
    }

    public void setMaxBtn(int maxBtn) {
        synchronized (this.mLock) {
            if (this.mTimeStat != null) {
                this.mTimeStat.setMaxBtn(maxBtn);
            }
            this.mMaxBtn = maxBtn;
        }
        if (DEBUG) {
            Slog.d(TAG, "setMaxBtn maxbtn=" + maxBtn);
        }
    }

    public void setLowBtn(int lowBtn) {
        synchronized (this.mLock) {
            if (this.mTimeStat != null) {
                this.mTimeStat.setLowBtn(lowBtn);
            }
            this.mLowBtn = lowBtn;
        }
        if (DEBUG) {
            Slog.d(TAG, "setLowBtn lowbtn=" + lowBtn);
        }
    }

    public void setSavePowerMode(int savePowerMode) {
        synchronized (this.mLock) {
            if (this.mSavePowerMode != savePowerMode) {
                handleSavePowerMode(savePowerMode);
            }
        }
        this.mSavePowerMode = savePowerMode;
    }

    public void setSpecBrightness(int gear, String reason, int rate) {
        if (this.mScreenState == 2) {
            handleSetSpecBtn(gear, reason, rate);
        }
    }

    public static String getDeviceManufacture() {
        File file = new File(DEVICE_INFO_PATH);
        String manufacture = DEFAULT_MANUFACTURE;
        if (!file.exists()) {
            Slog.e(TAG, "File " + DEVICE_INFO_PATH + " is not exist...");
            return DEFAULT_MANUFACTURE;
        } else if (!file.canRead()) {
            Slog.e(TAG, "No permission to read " + DEVICE_INFO_PATH);
            return DEFAULT_MANUFACTURE;
        } else {
            BufferedReader bufferedReader = null;
            try {
                BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = bufferedReader2.readLine();
                    if (line != null) {
                        Slog.i(TAG, line);
                        if (line.contains(KEY_DEVICE_VERSION)) {
                            String[] lineSplit = line.split(":");
                            if (lineSplit.length > 1) {
                                manufacture = lineSplit[1].trim();
                                Slog.i(TAG, "version : " + manufacture);
                            }
                        }
                        if (line.contains(KEY_DEVICE_MANUFACTURE)) {
                            String[] lineSplit2 = line.split(":");
                            if (lineSplit2.length > 1) {
                                manufacture = lineSplit2[1].trim();
                                Slog.i(TAG, "manufacture : " + manufacture);
                                break;
                            }
                        }
                    }
                }
                try {
                    bufferedReader2.close();
                    break;
                } catch (IOException e) {
                    Slog.i(TAG, "bufferedReader.close IOException caught");
                }
            } catch (FileNotFoundException e2) {
                Slog.i(TAG, "FileNotFoundException caught");
                if (0 != 0) {
                    bufferedReader.close();
                }
            } catch (IOException e3) {
                Slog.i(TAG, "IOException caught");
                if (0 != 0) {
                    bufferedReader.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e4) {
                        Slog.i(TAG, "bufferedReader.close IOException caught");
                    }
                }
                throw th;
            }
            return manufacture;
        }
    }

    public String getLcdManufacture() {
        return this.mManufacture;
    }

    public String getCurrDetailFormatTime() {
        return new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    public String getCurrSimpleFormatTime() {
        return new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    public String getCurrentPkg() {
        AppBrightnessStat appBrightnessStat = this.mAppStat;
        if (appBrightnessStat != null) {
            return appBrightnessStat.getCurrentPkg();
        }
        return null;
    }

    public String getLastPkg() {
        AppBrightnessStat appBrightnessStat = this.mAppStat;
        if (appBrightnessStat != null) {
            return appBrightnessStat.getLastPkg();
        }
        return null;
    }

    public ArrayList<Integer> getBackLightStatAppLevels() {
        DisplayXmlParser displayXmlParser = this.mDispXmlParser;
        if (displayXmlParser != null) {
            return displayXmlParser.getBackLightStatAppLevels();
        }
        return null;
    }

    public ArrayList<Integer> getBackLightStatLuxLevels() {
        DisplayXmlParser displayXmlParser = this.mDispXmlParser;
        if (displayXmlParser != null) {
            return displayXmlParser.getBackLightStatLuxLevels();
        }
        return null;
    }

    public ArrayList<Integer> getBackLightStatDurLevels() {
        DisplayXmlParser displayXmlParser = this.mDispXmlParser;
        if (displayXmlParser != null) {
            return displayXmlParser.getBackLightStatDurLevels();
        }
        return null;
    }

    public int getBackLightStatMaxLux() {
        DisplayXmlParser displayXmlParser = this.mDispXmlParser;
        if (displayXmlParser != null) {
            return displayXmlParser.getBackLightStatMaxLux();
        }
        return 8600;
    }

    public boolean getBackLightStatSupport() {
        DisplayXmlParser displayXmlParser = this.mDispXmlParser;
        if (displayXmlParser != null) {
            return displayXmlParser.getBackLightStatSupport();
        }
        return false;
    }

    public String getVersion() {
        DisplayXmlParser displayXmlParser = this.mDispXmlParser;
        if (displayXmlParser != null) {
            return displayXmlParser.getVersion();
        }
        return null;
    }

    public long uptimeMillis() {
        return SystemClock.elapsedRealtime() - this.mScreenOffTotalTime;
    }

    private void scheduleReportData() {
        this.mAlarmManager.cancel(new AlarmManager.OnAlarmListener() {
            /* class com.android.server.display.stat.$$Lambda$BackLightStat$U1PTz1EO5EdQ_gPRDzKcjCSx0Y */

            public final void onAlarm() {
                BackLightStat.this.onAlarm();
            }
        });
        Calendar calendar = Calendar.getInstance();
        calendar.set(11, 17);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        if (calendar.getTime().before(new Date())) {
            calendar.add(5, 1);
        }
        this.mAlarmManager.setExact(0, calendar.getTimeInMillis(), TAG, new AlarmManager.OnAlarmListener() {
            /* class com.android.server.display.stat.$$Lambda$BackLightStat$U1PTz1EO5EdQ_gPRDzKcjCSx0Y */

            public final void onAlarm() {
                BackLightStat.this.onAlarm();
            }
        }, this.mHandler);
    }

    /* access modifiers changed from: private */
    public void onAlarm() {
        scheduleReportData();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1001));
    }

    public void reportBackLightInfor(int eventID, String reportMsg) {
        Message msg = this.mHandler.obtainMessage(1000);
        msg.arg1 = eventID;
        msg.obj = reportMsg;
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportInfo(Message msg) {
        int eventID = msg.arg1;
        String reportMsg = (String) msg.obj;
        OppoAtlasManager instance = OppoAtlasManager.getInstance((Context) null);
        instance.setEvent((int) UsbTerminalTypes.TERMINAL_USB_STREAMING, "EventID," + eventID + "," + reportMsg);
        if (DEBUG) {
            Slog.d(TAG, "EventID:" + eventID + " reportMsg:" + reportMsg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyReceiver(String action, Object... values) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onReceive(action, values);
        }
        if (DEBUG) {
            Slog.d(TAG, "notifyReceiver action=" + action);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSettingsChangedLocked(boolean selfChange, Uri uri) {
        int autoMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -2);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            Callback callback = this.mCallbacks.get(i);
            if (Settings.System.getUriFor("screen_brightness_mode").equals(uri)) {
                callback.onReceive(ACTION_BRIGHTNESS_MODE, Integer.valueOf(autoMode));
            }
        }
        if (DEBUG) {
            Slog.d(TAG, "SettingsChanged selfChange=" + selfChange + " uri=" + uri);
        }
    }

    private void handleSavePowerMode(int savePowerMode) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onReceive(ACTION_SAVE_POWER_MODE, Integer.valueOf(savePowerMode));
        }
        if (DEBUG) {
            Slog.d(TAG, "handleSavePowerMode:" + savePowerMode);
        }
    }

    private void handleLux(int lux) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onReceive(ACTION_LUX_CHANGE, Integer.valueOf(lux));
        }
    }

    private void handleSetSpecBtn(int gear, String reason, int rate) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onReceive(ACTION_SET_SPEC_BTN, Integer.valueOf(gear), reason, Integer.valueOf(rate));
        }
        if (DEBUG) {
            Slog.d(TAG, "handleSetSpecBtn:gear=" + gear + " reason=" + reason + " rate=" + rate);
        }
    }

    /* access modifiers changed from: private */
    public final class BackLightStatHandler extends Handler {
        public BackLightStatHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1000) {
                BackLightStat.this.reportInfo(msg);
            } else if (i == 1001) {
                BackLightStat.this.notifyReceiver(BackLightStat.ACTION_ON_ALARM, new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class EventReceiver extends BroadcastReceiver {
        private EventReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                SystemClock.elapsedRealtime();
                if (!"android.intent.action.TIME_SET".equals(action) && !"android.intent.action.TIMEZONE_CHANGED".equals(action)) {
                    if ("android.intent.action.SCREEN_ON".equals(action)) {
                        BackLightStat.this.mScreenState = 2;
                    } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                        BackLightStat.this.mScreenState = 1;
                    }
                }
            }
            BackLightStat.this.notifyReceiver(action, new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            synchronized (BackLightStat.this.mLock) {
                BackLightStat.this.handleSettingsChangedLocked(selfChange, uri);
            }
        }
    }
}
