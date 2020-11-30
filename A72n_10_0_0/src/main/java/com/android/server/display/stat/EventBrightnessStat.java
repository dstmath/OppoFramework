package com.android.server.display.stat;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SystemSensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.UiModeManagerService;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.stat.BackLightStat;
import java.util.ArrayList;
import java.util.Locale;

public class EventBrightnessStat implements BackLightStat.Callback {
    private static final int BRIGHTNESS_BY_USER_TIMEOUT = 500;
    public static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int EVENT_CACHE_SIZE = 30;
    private static final int EVENT_ID_EVENT_STAT = 790;
    private static final String EVENT_TYPE_BY_USER = "by_user";
    private static final String EVENT_TYPE_SET_SPEC_BTN = "set_spec_btn";
    private static final String EVENT_TYPE_SWITCH_MODE = "switch_mode";
    private static final String KEY_APP_NAME = "pkgName";
    private static final String KEY_AUTO_MODE = "auto_mode";
    private static final String KEY_ENV_LUXS = "env_luxs";
    private static final String KEY_EVENT = "event";
    private static final String KEY_EVENT_ALL_DATA = "event_all_data";
    private static final String KEY_EVENT_SOURCE = "event_source";
    private static final String KEY_EVENT_TIME = "event_time";
    private static final String KEY_FROM_BY_USER = "from_by_user";
    private static final String KEY_SAVE_POWER_MODE = "save_power_mode";
    private static final String KEY_TO_BY_USER = "to_by_user";
    private static final int LUX_CACHE_SIZE = 10;
    private static final int MSG_BRIGHTNESS_BY_USER = 4003;
    private static final int MSG_BRIGHTNESS_MAX = 4004;
    private static final int MSG_BRIGHTNESS_MODE_CHANGE = 4002;
    private static final int MSG_LUX_CHANGE = 4006;
    private static final int MSG_REGIST_SENSORS = 4010;
    private static final int MSG_SAVE_POWER_MODE_CHANGE = 4005;
    private static final int MSG_SCREEN_OFF = 4000;
    private static final int MSG_SCREEN_ON = 4001;
    private static final int MSG_SET_SPEC_BTN = 4011;
    private static final int MSG_UNREGIST_LIGHT_SENSOR = 4008;
    private static final int MSG_UNREGIST_POSTURE_SENSOR = 4009;
    private static final int MSG_UPDATE_EVENT_DATA = 4007;
    private static final int MSG_UPDATE_POSE_LUX_DATE = 4012;
    private static final int SAMPLING_PERIOD_US = 1000;
    private static final int SENSOR_DATA_TIME = 2000;
    private static final int SENSOR_TIMEOUT = 60000;
    private static final int SPEC_SENSOR_TYPE = 33171099;
    private static final String TAG = "EventBrightnessStat";
    private static volatile EventBrightnessStat sEventBrightnessStat;
    private ArrayList<String> mA_SensorInfos = new ArrayList<>(10);
    private Sensor mAccelSensor;
    private BackLightStat mBackLightStat;
    private boolean mBootCompleted = false;
    private boolean mByUser = false;
    private boolean mByUserShake = false;
    private Context mContext;
    private int mCurrLux = -1;
    private int mCurrMode = 0;
    private int mCurrTarget = -1;
    private ArrayList<EventInfo> mEvents = new ArrayList<>(30);
    private ArrayList<String> mG_SensorInfos = new ArrayList<>(10);
    private Sensor mGyrosSensor;
    private EventBackLightHandler mHandler;
    private int mLastTarget = -1;
    private Sensor mLightSensor;
    private boolean mLightSensorEnabled = false;
    private final SensorEventListener mLightSensorListener = new SensorEventListener() {
        /* class com.android.server.display.stat.EventBrightnessStat.AnonymousClass1 */

        public void onSensorChanged(SensorEvent event) {
            EventBrightnessStat.this.mCurrLux = (int) event.values[0];
            EventBrightnessStat.this.handleLuxChange();
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private final Object mLock = new Object();
    private ArrayList<Integer> mLuxs = new ArrayList<>(10);
    private int mNormalLightSensorRate = 250;
    private boolean mPostureSensorEnabled = false;
    private SensorEventListener mPostureSensorListener = new SensorEventListener() {
        /* class com.android.server.display.stat.EventBrightnessStat.AnonymousClass2 */

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            int type = event.sensor.getType();
            if (1 == type) {
                EventBrightnessStat.this.handleA_SensorChange(String.format(Locale.US, "(%.4f;%.4f;%.4f)", Float.valueOf(event.values[0]), Float.valueOf(event.values[1]), Float.valueOf(event.values[2])));
            } else if (4 == type) {
                EventBrightnessStat.this.handleG_SensorChange(String.format(Locale.US, "(%.4f;%.4f;%.4f)", Float.valueOf(event.values[0]), Float.valueOf(event.values[1]), Float.valueOf(event.values[2])));
            }
        }
    };
    private int mPowerState = 2;
    private int mSavePowerMode = -1;
    private SensorManager mSensorManager = null;
    private boolean mSupportStat = false;
    private int mUpdateTarget = 0;
    private String mVersion = null;

    private EventBrightnessStat(Context context, BackLightStat stat) {
        this.mContext = context;
        this.mBackLightStat = stat;
    }

    public static EventBrightnessStat getInstance(Context context, BackLightStat stat) {
        if (sEventBrightnessStat == null) {
            synchronized (EventBrightnessStat.class) {
                if (sEventBrightnessStat == null) {
                    sEventBrightnessStat = new EventBrightnessStat(context, stat);
                }
            }
        }
        return sEventBrightnessStat;
    }

    public void init(Handler handler) {
        this.mHandler = new EventBackLightHandler(handler.getLooper());
        this.mCurrMode = Settings.System.getInt(this.mContext.getContentResolver(), "screen_brightness_mode", 0);
        this.mSensorManager = new SystemSensorManager(this.mContext, this.mHandler.getLooper());
        this.mLightSensor = getLightSensor(this.mSensorManager.getDefaultSensor(5));
        this.mAccelSensor = this.mSensorManager.getDefaultSensor(1);
        this.mGyrosSensor = this.mSensorManager.getDefaultSensor(4);
        this.mBootCompleted = true;
        this.mVersion = this.mBackLightStat.getVersion();
        this.mSupportStat = this.mBackLightStat.getBackLightStatSupport();
    }

    /* access modifiers changed from: package-private */
    public final class EventInfo {
        EnvState envState = new EnvState();
        PhoneState phoneState = new PhoneState();
        UserState userState = new UserState();

        EventInfo() {
        }

        public String toString() {
            return "EventInfo{phoneState=" + this.phoneState + "  envState=" + this.envState + "  userState=" + this.userState + '}';
        }
    }

    /* access modifiers changed from: package-private */
    public final class PhoneState {
        int autoMode = 0;
        int currLev = -1;
        String event = null;
        int lastLev = -1;
        String pkgName = null;
        String posture = null;
        int savePowerMode = 0;
        String source = "com.android.settings";
        String time = null;

        PhoneState() {
        }

        public String toString() {
            return "[pkgName='" + this.pkgName + "'  posture='" + this.posture + "'  source='" + this.source + "'  lastLev=" + this.lastLev + "  currLev=" + this.currLev + "  autoMode=" + this.autoMode + " savePowerMode=" + this.savePowerMode + ']';
        }
    }

    /* access modifiers changed from: package-private */
    public final class EnvState {
        ArrayList<Integer> luxs = null;

        EnvState() {
        }

        public String toString() {
            return "[luxs=" + EventBrightnessStat.this.formatList(this.luxs) + ']';
        }
    }

    /* access modifiers changed from: package-private */
    public final class UserState {
        String state;

        UserState() {
        }

        public String toString() {
            return "[state='" + this.state + "']";
        }
    }

    public String formatList(ArrayList<Integer> list) {
        StringBuilder sb = new StringBuilder(100);
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            sb.append(StringUtils.SPACE);
        }
        return sb.toString().trim();
    }

    public void setCurrTarget(int state, int currTarget, boolean byUser) {
        int i;
        int i2;
        boolean changed = false;
        boolean lastByUser = this.mByUser;
        if (byUser && !this.mByUserShake) {
            this.mByUserShake = true;
        }
        if (currTarget != this.mCurrTarget && !byUser) {
            this.mCurrTarget = currTarget;
        }
        this.mPowerState = state;
        this.mByUser = byUser;
        if ((lastByUser && !byUser) || !lastByUser) {
            changed = true;
        }
        if (this.mSupportStat && this.mBootCompleted && changed && (i = this.mUpdateTarget) != (i2 = this.mCurrTarget)) {
            this.mLastTarget = i;
            this.mUpdateTarget = i2;
            if (DEBUG) {
                Slog.d(TAG, "setCurrTarget changed " + this.mLastTarget + "->" + this.mUpdateTarget);
            }
            if (lastByUser && !byUser) {
                sendBrightnessByUser();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBtnByUser() {
        if (this.mEvents.size() >= 30) {
            uploadData(BackLightStat.VALUE_UPLOAD_CACHE_FULL);
        }
        EventInfo event = new EventInfo();
        event.phoneState.pkgName = this.mBackLightStat.getCurrentPkg();
        event.phoneState.posture = getPosture();
        if (event.phoneState.pkgName.equals("com.android.settings")) {
            event.phoneState.source = "com.android.settings";
        } else {
            event.phoneState.source = "com.android.systemui";
        }
        event.phoneState.event = EVENT_TYPE_BY_USER;
        event.phoneState.time = this.mBackLightStat.getCurrDetailFormatTime();
        event.phoneState.lastLev = this.mLastTarget;
        event.phoneState.currLev = this.mCurrTarget;
        event.phoneState.autoMode = this.mCurrMode;
        event.phoneState.savePowerMode = this.mSavePowerMode;
        event.envState.luxs = this.mLuxs;
        event.userState.state = null;
        this.mEvents.add(event);
        this.mByUserShake = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void byUserRegisterSensors() {
        if (this.mCurrMode == 0) {
            this.mLuxs.clear();
            handleLSensor();
        }
        handlePostureSensors();
    }

    private void sendRegisterSensors() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_REGIST_SENSORS));
    }

    private void sendBrightnessByUser() {
        sendRegisterSensors();
        this.mHandler.removeMessages(MSG_BRIGHTNESS_BY_USER);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_BRIGHTNESS_BY_USER), 500);
    }

    private void sendScreenOff() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_SCREEN_OFF));
    }

    private void sendScreenON() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_SCREEN_ON));
    }

    private void sendBrightnessModeChange() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_BRIGHTNESS_MODE_CHANGE));
    }

    private void sendSavePowerModeChange() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_SAVE_POWER_MODE_CHANGE));
    }

    private void sendLuxChange() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_LUX_CHANGE));
    }

    private void sendSetSpecBtn(int gear, String reason, int rate) {
        Message msg = this.mHandler.obtainMessage(MSG_SET_SPEC_BTN);
        msg.arg1 = gear;
        msg.arg2 = rate;
        msg.obj = reason;
        this.mHandler.sendMessage(msg);
    }

    private void uploadData(String reason) {
        for (int i = 0; i < this.mEvents.size(); i++) {
            StringBuilder sb = new StringBuilder(1200);
            EventInfo eventInfo = this.mEvents.get(i);
            sb.append(KEY_APP_NAME);
            sb.append(",");
            sb.append(eventInfo.phoneState.pkgName);
            sb.append(",");
            sb.append(KEY_EVENT_SOURCE);
            sb.append(",");
            sb.append(eventInfo.phoneState.source);
            sb.append(",");
            sb.append(KEY_EVENT);
            sb.append(",");
            sb.append(eventInfo.phoneState.event);
            sb.append(",");
            sb.append(KEY_EVENT_TIME);
            sb.append(",");
            sb.append(eventInfo.phoneState.time);
            sb.append(",");
            sb.append(KEY_FROM_BY_USER);
            sb.append(",");
            sb.append(eventInfo.phoneState.lastLev);
            sb.append(",");
            sb.append(KEY_TO_BY_USER);
            sb.append(",");
            sb.append(eventInfo.phoneState.currLev);
            sb.append(",");
            sb.append(KEY_AUTO_MODE);
            sb.append(",");
            sb.append(eventInfo.phoneState.autoMode);
            sb.append(",");
            sb.append(KEY_SAVE_POWER_MODE);
            sb.append(",");
            sb.append(eventInfo.phoneState.savePowerMode);
            sb.append(",");
            sb.append(KEY_ENV_LUXS);
            sb.append(",");
            sb.append(formatList(eventInfo.envState.luxs));
            sb.append(",");
            sb.append(KEY_EVENT_ALL_DATA);
            sb.append(",");
            sb.append(eventInfo.toString());
            sb.append(",");
            String manu = this.mBackLightStat.getLcdManufacture();
            sb.append(BackLightStat.KEY_LCD_MANU);
            sb.append(",");
            sb.append(manu);
            sb.append(",");
            sb.append(BackLightStat.KEY_VERSION);
            sb.append(",");
            sb.append(this.mVersion);
            sb.append(",");
            String time = this.mBackLightStat.getCurrSimpleFormatTime();
            sb.append(BackLightStat.KEY_UPLOAD_TIME);
            sb.append(",");
            sb.append(time);
            sb.append(",");
            sb.append(BackLightStat.KEY_UPLOAD_REASON);
            sb.append(",");
            sb.append(reason);
            String uploadData = sb.toString();
            this.mBackLightStat.reportBackLightInfor(EVENT_ID_EVENT_STAT, uploadData);
            if (DEBUG) {
                Slog.d(TAG, "uploadData size=" + uploadData.length() + StringUtils.SPACE + uploadData);
            }
        }
        this.mEvents.clear();
    }

    @Override // com.android.server.display.stat.BackLightStat.Callback
    public void onReceive(String action, Object... values) {
        if (!TextUtils.isEmpty(action) && this.mSupportStat) {
            if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                uploadData(BackLightStat.VALUE_UPLOAD_SHUT_DOWN);
            } else if (!"android.intent.action.SCREEN_ON".equals(action)) {
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    unregisterAllSensor();
                } else if ("android.intent.action.REBOOT".equals(action)) {
                    uploadData(BackLightStat.VALUE_UPLOAD_REBOOT);
                } else if (BackLightStat.ACTION_ON_ALARM.equals(action)) {
                    uploadData(BackLightStat.VALUE_UPLOAD_ON_ALARM);
                } else if (BackLightStat.ACTION_BRIGHTNESS_MODE.equals(action)) {
                    if (values != null) {
                        try {
                            if (values[0] instanceof Integer) {
                                this.mCurrMode = ((Integer) values[0]).intValue();
                                sendBrightnessModeChange();
                            }
                        } catch (Exception e) {
                            Slog.e(TAG, "action:" + action + e.toString());
                        }
                    }
                } else if (BackLightStat.ACTION_SAVE_POWER_MODE.equals(action)) {
                    if (values != null) {
                        try {
                            if (values[0] instanceof Integer) {
                                this.mSavePowerMode = ((Integer) values[0]).intValue();
                            }
                        } catch (Exception e2) {
                            Slog.e(TAG, "action:" + action + e2.toString());
                        }
                    }
                } else if (BackLightStat.ACTION_LUX_CHANGE.equals(action)) {
                    if (values != null) {
                        try {
                            if (values[0] instanceof Integer) {
                                this.mCurrLux = ((Integer) values[0]).intValue();
                                sendLuxChange();
                            }
                        } catch (Exception e3) {
                            Slog.e(TAG, "action:" + action + e3.toString());
                        }
                    }
                } else if (BackLightStat.ACTION_SET_SPEC_BTN.equals(action) && values != null) {
                    int gear = -1;
                    String reason = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
                    int rate = -1;
                    try {
                        if (values[0] instanceof Integer) {
                            gear = ((Integer) values[0]).intValue();
                        }
                        if (values[1] instanceof String) {
                            reason = (String) values[1];
                        }
                        if (values[2] instanceof Integer) {
                            rate = ((Integer) values[2]).intValue();
                        }
                        Slog.d(TAG, "handleSetSpecBtn:gear=" + gear + " reason=" + reason + " rate=" + rate);
                        sendSetSpecBtn(gear, reason, rate);
                    } catch (Exception e4) {
                        Slog.e(TAG, "action:" + action + e4.toString());
                    }
                }
            }
        }
    }

    private void getPhoneState(EventInfo event) {
        event.phoneState.pkgName = this.mBackLightStat.getCurrentPkg();
        event.phoneState.posture = null;
        if (event.phoneState.pkgName.equals("com.android.settings")) {
            event.phoneState.source = "com.android.settings";
        } else {
            event.phoneState.source = "com.android.systemui";
        }
        event.phoneState.lastLev = 0;
        event.phoneState.currLev = 0;
        event.phoneState.autoMode = this.mCurrMode;
        event.phoneState.savePowerMode = this.mSavePowerMode;
    }

    private Sensor getLightSensor(Sensor lightSensor) {
        boolean isUsingFusionLight = SystemProperties.getBoolean("ro.oplus.fusionlight", false);
        boolean isUsingFusionLightNaruto = SystemProperties.getBoolean("persist.sys.oppo.fusionlight.naruto", false);
        if (!isUsingFusionLight || !isUsingFusionLightNaruto) {
            return lightSensor;
        }
        return this.mSensorManager.getDefaultSensor(SPEC_SENSOR_TYPE);
    }

    private void unregisterAllSensor() {
        unregisterLightSensor();
        unregisterPostureSensor();
    }

    private void registerLightSensor() {
        if (!this.mLightSensorEnabled) {
            this.mLightSensorEnabled = true;
            this.mLuxs.clear();
            this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, this.mNormalLightSensorRate * 1000, this.mHandler);
            Slog.d(TAG, "registerLightSensor +++ Enter");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterLightSensor() {
        if (this.mLightSensorEnabled) {
            this.mLightSensorEnabled = false;
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
            Slog.d(TAG, "registerLightSensor --- Exit");
        }
    }

    private boolean registerPostureSensor() {
        if (!this.mPostureSensorEnabled) {
            this.mPostureSensorEnabled = true;
            this.mA_SensorInfos.clear();
            this.mG_SensorInfos.clear();
            this.mSensorManager.registerListener(this.mPostureSensorListener, this.mAccelSensor, 3);
            this.mSensorManager.registerListener(this.mPostureSensorListener, this.mGyrosSensor, 3);
            Slog.d(TAG, "registerPostureSensor +++ Enter");
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterPostureSensor() {
        if (this.mPostureSensorEnabled) {
            this.mPostureSensorEnabled = false;
            this.mSensorManager.unregisterListener(this.mPostureSensorListener);
            Slog.d(TAG, "registerPostureSensor --- Exit");
        }
    }

    private void handlePostureSensors() {
        if (DEBUG) {
            Slog.d(TAG, "handleGASensor");
        }
        this.mHandler.removeMessages(MSG_UNREGIST_POSTURE_SENSOR);
        registerPostureSensor();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_UNREGIST_POSTURE_SENSOR), 60000);
    }

    private void handleLSensor() {
        if (DEBUG) {
            Slog.d(TAG, "handleLSensor");
        }
        this.mHandler.removeMessages(MSG_UNREGIST_LIGHT_SENSOR);
        registerLightSensor();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_UNREGIST_LIGHT_SENSOR), 60000);
    }

    private String getPosture() {
        StringBuilder sb = new StringBuilder(300);
        sb.append("A-Sensor{");
        for (int i = 0; i < this.mA_SensorInfos.size(); i++) {
            sb.append(i);
            sb.append(":");
            sb.append(this.mA_SensorInfos.get(i));
        }
        sb.append("} G-Sensor{");
        for (int i2 = 0; i2 < this.mG_SensorInfos.size(); i2++) {
            sb.append(i2);
            sb.append(":");
            sb.append(this.mG_SensorInfos.get(i2));
        }
        sb.append("}");
        return sb.toString();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateEventData(Message msg) {
        EventInfo event = (EventInfo) msg.obj;
        event.phoneState.posture = getPosture();
        if (this.mCurrMode == 1) {
            event.envState.luxs = this.mLuxs;
        }
        this.mEvents.add(event);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBtnModeChange() {
        if (this.mEvents.size() >= 30) {
            uploadData(BackLightStat.VALUE_UPLOAD_CACHE_FULL);
        }
        handlePostureSensors();
        EventInfo event = new EventInfo();
        event.phoneState.pkgName = this.mBackLightStat.getCurrentPkg();
        event.phoneState.posture = null;
        if (event.phoneState.pkgName.equals("com.android.settings")) {
            event.phoneState.source = "com.android.settings";
        } else {
            event.phoneState.source = "com.android.systemui";
        }
        event.phoneState.event = EVENT_TYPE_SWITCH_MODE;
        event.phoneState.time = this.mBackLightStat.getCurrDetailFormatTime();
        event.phoneState.lastLev = this.mCurrTarget;
        event.phoneState.currLev = this.mCurrTarget;
        event.phoneState.autoMode = this.mCurrMode;
        event.phoneState.savePowerMode = this.mSavePowerMode;
        event.envState.luxs = this.mLuxs;
        event.userState.state = null;
        if (this.mCurrMode == 0) {
            this.mLuxs.clear();
        }
        Message msg = this.mHandler.obtainMessage(MSG_UPDATE_EVENT_DATA);
        msg.obj = event;
        this.mHandler.sendMessageDelayed(msg, 2000);
    }

    private void handleSavePowerModeChange() {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLuxChange() {
        synchronized (this.mLock) {
            if (this.mLuxs.size() >= 10) {
                this.mLuxs.remove(0);
            }
            this.mLuxs.add(Integer.valueOf(this.mCurrLux));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleA_SensorChange(String info) {
        if (this.mA_SensorInfos.size() >= 10) {
            this.mA_SensorInfos.remove(0);
        }
        this.mA_SensorInfos.add(info);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleG_SensorChange(String info) {
        if (this.mG_SensorInfos.size() >= 10) {
            this.mG_SensorInfos.remove(0);
        }
        this.mG_SensorInfos.add(info);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetSpecBtn(Message msg) {
        if (this.mEvents.size() >= 30) {
            uploadData(BackLightStat.VALUE_UPLOAD_CACHE_FULL);
        }
        handlePostureSensors();
        if (this.mCurrMode == 0) {
            handleLSensor();
        }
        EventInfo event = new EventInfo();
        event.phoneState.pkgName = this.mBackLightStat.getCurrentPkg();
        event.phoneState.posture = null;
        event.phoneState.source = "temperature Group";
        event.phoneState.event = EVENT_TYPE_SET_SPEC_BTN;
        event.phoneState.time = this.mBackLightStat.getCurrDetailFormatTime();
        event.phoneState.lastLev = this.mCurrTarget;
        event.phoneState.currLev = this.mCurrTarget;
        event.phoneState.autoMode = this.mCurrMode;
        event.phoneState.savePowerMode = this.mSavePowerMode;
        event.envState.luxs = this.mLuxs;
        StringBuilder sb = new StringBuilder(30);
        sb.append("gear:");
        sb.append(msg.arg1);
        sb.append(" rate:");
        sb.append(msg.arg2);
        sb.append(" reason:");
        sb.append(msg.obj);
        event.userState.state = sb.toString();
        Message msg2 = this.mHandler.obtainMessage(MSG_UPDATE_POSE_LUX_DATE);
        msg2.obj = event;
        this.mHandler.sendMessageDelayed(msg2, 2000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdatePoseLuxData(Message msg) {
        EventInfo event = (EventInfo) msg.obj;
        event.phoneState.posture = getPosture();
        event.envState.luxs = this.mLuxs;
        this.mEvents.add(event);
        if (DEBUG) {
            Slog.d(TAG, "event=" + event);
        }
    }

    /* access modifiers changed from: private */
    public final class EventBackLightHandler extends Handler {
        public EventBackLightHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EventBrightnessStat.MSG_SCREEN_OFF /* 4000 */:
                case EventBrightnessStat.MSG_SCREEN_ON /* 4001 */:
                case EventBrightnessStat.MSG_BRIGHTNESS_MAX /* 4004 */:
                case EventBrightnessStat.MSG_SAVE_POWER_MODE_CHANGE /* 4005 */:
                default:
                    return;
                case EventBrightnessStat.MSG_BRIGHTNESS_MODE_CHANGE /* 4002 */:
                    EventBrightnessStat.this.handleBtnModeChange();
                    return;
                case EventBrightnessStat.MSG_BRIGHTNESS_BY_USER /* 4003 */:
                    EventBrightnessStat.this.handleBtnByUser();
                    return;
                case EventBrightnessStat.MSG_LUX_CHANGE /* 4006 */:
                    EventBrightnessStat.this.handleLuxChange();
                    return;
                case EventBrightnessStat.MSG_UPDATE_EVENT_DATA /* 4007 */:
                    EventBrightnessStat.this.updateEventData(msg);
                    return;
                case EventBrightnessStat.MSG_UNREGIST_LIGHT_SENSOR /* 4008 */:
                    Slog.d(EventBrightnessStat.TAG, "unregist L-sensor");
                    EventBrightnessStat.this.unregisterLightSensor();
                    return;
                case EventBrightnessStat.MSG_UNREGIST_POSTURE_SENSOR /* 4009 */:
                    Slog.d(EventBrightnessStat.TAG, "unregist GA-sensor");
                    EventBrightnessStat.this.unregisterPostureSensor();
                    return;
                case EventBrightnessStat.MSG_REGIST_SENSORS /* 4010 */:
                    EventBrightnessStat.this.byUserRegisterSensors();
                    return;
                case EventBrightnessStat.MSG_SET_SPEC_BTN /* 4011 */:
                    EventBrightnessStat.this.handleSetSpecBtn(msg);
                    return;
                case EventBrightnessStat.MSG_UPDATE_POSE_LUX_DATE /* 4012 */:
                    EventBrightnessStat.this.handleUpdatePoseLuxData(msg);
                    return;
            }
        }
    }
}
