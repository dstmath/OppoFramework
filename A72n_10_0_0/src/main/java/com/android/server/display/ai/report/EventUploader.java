package com.android.server.display.ai.report;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings;
import com.android.server.display.ai.utils.ColorAILog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import oppo.util.OppoStatistics;

public class EventUploader {
    private static final float ALL_AUTO_PERCENT = 0.95f;
    private static final float ALL_MANUAL_PERCENT = 0.05f;
    private static final float AUTO_PERCENT = 0.5f;
    private static final String BLANK_SIGN = " ";
    private static final int CACHE_LIMIT_COUNT = 300;
    public static final int DRAGGING_EVENT = 1;
    private static final String EVENT_ID_BRIGHTNESS_EVENTS = "ai_brightness_events";
    private static final String EVENT_ID_BRIGHTNESS_STATISTICS = "ai_brightness_statistics";
    private static final String KEY_AUTO_DRAG_COUNT = "auto_drag_count";
    private static final String KEY_AUTO_MODE_TIME = "auto_mode_time";
    private static final String KEY_BRIGHTNESS_EVENTS = "brightness_events";
    private static final String KEY_DAY = "day";
    private static final String KEY_LOW_BRIGHTNESS_COUNT = "low_brightness_count";
    private static final String KEY_MANUAL_MODE_TIME = "manual_mode_time";
    private static final String KEY_MODE_TIME_PERCENT = "mode_time_percent";
    private static final String KEY_UPLOAD_REASON = "upload_reason";
    private static final String KEY_USER_CLASS = "user_class";
    public static final String LOG_TAG = "ai_brightness";
    public static final int LOW_BRIGHTNESS_EVENT = 2;
    private static final int MAX_COUNT_IN_ONE_EVENT = 50;
    public static final int SWITCH_EVENT = 0;
    private static final String TAG = "EventUploader";
    public static final int UNLOCK_EXCEPTION_EVENT = 3;
    private static final String VALUE_UPLOAD_REASON_DAILY = "daily";
    private static final String VALUE_UPLOAD_REASON_FULL = "full";
    private static final String VALUE_UPLOAD_REASON_SHUT_DOWN = "shut_down";
    private static final String VALUE_UPLOAD_REASON_UPGRADE = "app_upgrade";
    private static final String VALUE_USER_CLASS_ALL_AUTO = "all_auto";
    private static final String VALUE_USER_CLASS_ALL_MANUAL = "all_manual";
    private static final String VALUE_USER_CLASS_AUTO = "auto";
    private static final String VALUE_USER_CLASS_ERROR = "error";
    private static final String VALUE_USER_CLASS_MANUAL = "manual";
    private int mAutoMode;
    private long mAutoModeTime;
    private Context mContext;
    private int mDragCount;
    private final ArrayList<String> mEventCacheList = new ArrayList<>();
    private EventReceiver mEventReceiver;
    private Calendar mLastCalendar = null;
    private long mLastModeChangeTime;
    private long mLastScreenOffTime = 0;
    private int mLowBrightnessCount;
    private long mManualModeTime;
    private int mPid;
    private long mScreenOffTime;

    /* access modifiers changed from: private */
    public static class EventInfo {
        private Calendar mCalendar = Calendar.getInstance();
        private String mEventString;
        private Object[] mEventValues;

        EventInfo(int pid, Object... eventValues) {
            this.mEventValues = eventValues;
            String dataString = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(this.mCalendar.getTime());
            StringBuilder sb = new StringBuilder();
            sb.append(dataString);
            sb.append(EventUploader.BLANK_SIGN);
            sb.append(pid);
            sb.append(EventUploader.BLANK_SIGN);
            sb.append((long) Process.myTid());
            sb.append(EventUploader.BLANK_SIGN);
            int length = eventValues.length;
            for (int i = 0; i < length; i++) {
                Object eventValue = eventValues[i];
                if (i != length - 1) {
                    sb.append(eventValue.toString());
                    sb.append(",");
                } else {
                    sb.append(eventValue.toString());
                }
            }
            this.mEventString = sb.toString();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getEventString() {
            return this.mEventString;
        }
    }

    public EventUploader(Context context) {
        this.mContext = context;
        this.mPid = Process.myPid();
        this.mLastModeChangeTime = getElapsedRealtimeWhenScreenOn();
        this.mLastCalendar = Calendar.getInstance();
        this.mEventReceiver = new EventReceiver();
        this.mAutoMode = Settings.System.getInt(this.mContext.getContentResolver(), "screen_brightness_mode", 0);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        context.registerReceiver(this.mEventReceiver, intentFilter);
    }

    public void reportEvent(Object... eventValues) {
        try {
            if (eventValues[0] instanceof Integer) {
                int intValue = ((Integer) eventValues[0]).intValue();
                if (intValue == 0) {
                    Object argAutoMode = eventValues[1];
                    if (argAutoMode instanceof Integer) {
                        int autoMode = ((Integer) argAutoMode).intValue();
                        long now = getElapsedRealtimeWhenScreenOn();
                        if (autoMode == 1) {
                            this.mManualModeTime += now - this.mLastModeChangeTime;
                            ColorAILog.i(TAG, "new autoMode:" + autoMode + ", mManualModeTime:" + (this.mManualModeTime / 1000));
                        } else if (autoMode == 0) {
                            this.mAutoModeTime += now - this.mLastModeChangeTime;
                            ColorAILog.i(TAG, "new autoMode:" + autoMode + ", mAutoModeTime:" + (this.mAutoModeTime / 1000));
                        }
                        this.mLastModeChangeTime = now;
                        this.mAutoMode = autoMode;
                    } else {
                        ColorAILog.w(TAG, "reportEvent SWITCH_EVENT arg error, argAutoMode:" + argAutoMode);
                    }
                } else if (intValue == 1) {
                    this.mDragCount++;
                } else if (intValue == 2) {
                    this.mLowBrightnessCount++;
                }
            }
            this.mEventCacheList.add(new EventInfo(this.mPid, eventValues).getEventString());
            if (this.mEventCacheList.size() >= CACHE_LIMIT_COUNT) {
                uploadEvents(VALUE_UPLOAD_REASON_FULL);
            }
        } catch (Exception e) {
            ColorAILog.e(TAG, "reportEvent error:" + e);
        }
    }

    public void release() {
        uploadEvents(VALUE_UPLOAD_REASON_UPGRADE);
        try {
            this.mContext.unregisterReceiver(this.mEventReceiver);
        } catch (Exception e) {
            ColorAILog.i(TAG, "release error:" + e);
        }
    }

    private long getElapsedRealtimeWhenScreenOn() {
        ColorAILog.i(TAG, "getElapsedRealtimeWhenScreenOn, mScreenOffTime:" + (this.mScreenOffTime / 1000) + "s");
        return SystemClock.elapsedRealtime() - this.mScreenOffTime;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadEvents(String reason) {
        int size;
        ColorAILog.i(TAG, "uploadEvents reason:" + reason);
        long lastDuration = getElapsedRealtimeWhenScreenOn() - this.mLastModeChangeTime;
        int i = this.mAutoMode;
        if (i == 1) {
            this.mAutoModeTime += lastDuration;
            ColorAILog.i(TAG, "uploadEvents add the last duration, mAutoMode:" + this.mAutoMode + ", mAutoModeTime:" + (this.mAutoModeTime / 1000) + ", lastDuration:" + (lastDuration / 1000));
        } else if (i == 0) {
            this.mManualModeTime += lastDuration;
            ColorAILog.i(TAG, "uploadEvents add the last duration, mAutoMode:" + this.mAutoMode + ", mManualModeTime:" + (this.mManualModeTime / 1000) + ", lastDuration:" + (lastDuration / 1000));
        }
        int size2 = this.mEventCacheList.size();
        HashMap<String, String> logMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        int i2 = 0;
        while (i2 < size2) {
            sb.append(";");
            sb.append(this.mEventCacheList.get(i2));
            int index = i2 + 1;
            if (index % MAX_COUNT_IN_ONE_EVENT == 0 || index == size2) {
                String eventsString = sb.toString();
                logMap.put(KEY_BRIGHTNESS_EVENTS, eventsString);
                ColorAILog.i(TAG, "uploadEvents progress:" + index + "/" + size2);
                size = size2;
                OppoStatistics.onCommon(this.mContext, "ai_brightness", EVENT_ID_BRIGHTNESS_EVENTS, logMap, false);
                ColorAILog.i(TAG, "uploadEvents eventsString:" + eventsString);
                HashMap<String, String> logMap2 = new HashMap<>();
                sb = new StringBuilder();
                logMap = logMap2;
            } else {
                size = size2;
            }
            i2++;
            size2 = size;
        }
        long j = this.mAutoModeTime;
        long j2 = this.mManualModeTime;
        float percent = j + j2 != 0 ? ((float) j) / ((float) (j + j2)) : 0.0f;
        String dataString = "";
        if (this.mLastCalendar != null) {
            dataString = new SimpleDateFormat("MMddHHmm", Locale.getDefault()).format(this.mLastCalendar.getTime());
        }
        logMap.put(KEY_USER_CLASS, getUserClass(percent));
        logMap.put(KEY_MODE_TIME_PERCENT, String.valueOf(percent));
        logMap.put(KEY_AUTO_DRAG_COUNT, String.valueOf(this.mDragCount));
        logMap.put(KEY_LOW_BRIGHTNESS_COUNT, String.valueOf(this.mLowBrightnessCount));
        logMap.put(KEY_AUTO_MODE_TIME, "" + (this.mAutoModeTime / 1000) + "s");
        logMap.put(KEY_MANUAL_MODE_TIME, "" + (this.mManualModeTime / 1000) + "s");
        logMap.put(KEY_DAY, dataString);
        logMap.put(KEY_UPLOAD_REASON, reason);
        OppoStatistics.onCommon(this.mContext, "ai_brightness", EVENT_ID_BRIGHTNESS_STATISTICS, logMap, false);
        this.mDragCount = 0;
        this.mAutoModeTime = 0;
        this.mManualModeTime = 0;
        this.mEventCacheList.clear();
    }

    private String getUserClass(float percent) {
        if (percent > ALL_AUTO_PERCENT) {
            return VALUE_USER_CLASS_ALL_AUTO;
        }
        if (percent >= AUTO_PERCENT) {
            return VALUE_USER_CLASS_AUTO;
        }
        if (percent < AUTO_PERCENT && percent >= ALL_MANUAL_PERCENT) {
            return VALUE_USER_CLASS_MANUAL;
        }
        if (percent >= 0.0f) {
            return VALUE_USER_CLASS_ALL_MANUAL;
        }
        return VALUE_USER_CLASS_ERROR;
    }

    /* access modifiers changed from: private */
    public class EventReceiver extends BroadcastReceiver {
        private EventReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                ColorAILog.i(EventUploader.TAG, "onReceive, ShutDownReceiver, uploadEvents");
                EventUploader.this.uploadEvents(EventUploader.VALUE_UPLOAD_REASON_SHUT_DOWN);
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                ColorAILog.i(EventUploader.TAG, "onReceive, ACTION_SCREEN_ON");
                if (EventUploader.this.mLastScreenOffTime != 0) {
                    EventUploader.this.mScreenOffTime += SystemClock.elapsedRealtime() - EventUploader.this.mLastScreenOffTime;
                    ColorAILog.i(EventUploader.TAG, "onReceive, ACTION_SCREEN_ON, mScreenOffTime:" + EventUploader.this.mScreenOffTime);
                }
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                ColorAILog.i(EventUploader.TAG, "onReceive, ACTION_SCREEN_OFF, uploadEvents");
                EventUploader.this.mLastScreenOffTime = SystemClock.elapsedRealtime();
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(5);
                int lastDay = EventUploader.this.mLastCalendar != null ? EventUploader.this.mLastCalendar.get(5) : 0;
                ColorAILog.i(EventUploader.TAG, "reportEvent day:" + day + ", lastDay:" + lastDay + ", mEventCacheList.size():" + EventUploader.this.mEventCacheList.size());
                if (lastDay != 0 && day != lastDay) {
                    EventUploader.this.uploadEvents(EventUploader.VALUE_UPLOAD_REASON_DAILY);
                    EventUploader.this.mLastCalendar = calendar;
                }
            }
        }
    }
}
