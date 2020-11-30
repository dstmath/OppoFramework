package com.android.server.display;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class OppoAutoBrightControllerHelper {
    private static boolean DEBUG = true;
    private static final int MSG_FIRST_INDEX = 200;
    private static final int MSG_REPORT_AUTO_BRIGHTNESS = 201;
    private static final int MSG_REPORT_AUTO_MANUL_BRIGHTNESS = 202;
    private static final int MSG_UPDATE_BRIGHTNESS_AFTER_PROXIMITY = 203;
    private static final String TAG = "OppoAutoBrighControllerHelper";
    private static OppoBrightUtils mOppoBrightUtils;
    private long mAutoDurationTime = System.currentTimeMillis();
    private long mAutoManulDurationTime = System.currentTimeMillis();
    private long mAutoManulTime = System.currentTimeMillis();
    private long mAutoTime = System.currentTimeMillis();
    OppoAutomaticBrightnessController mAutomaticBC;
    private Context mContext;
    private DisplayAutoBrightnessHandler mHandler = null;
    private float mLastAutoBrightness = 127.0f;
    private long mProximitySensorChangeTime = 0;
    public final SensorEventListener mProximitySensorListener = new SensorEventListener() {
        /* class com.android.server.display.OppoAutoBrightControllerHelper.AnonymousClass3 */
        private boolean mPrevProximityNear = false;

        public void onSensorChanged(SensorEvent event) {
            long time = SystemClock.uptimeMillis();
            if (((double) event.values[0]) == 0.0d) {
                OppoAutomaticBrightnessController oppoAutomaticBrightnessController = OppoAutoBrightControllerHelper.this.mAutomaticBC;
                OppoAutomaticBrightnessController.mProximityNear = true;
                if (OppoAutoBrightControllerHelper.DEBUG) {
                    Slog.d(OppoAutoBrightControllerHelper.TAG, "Proximity is near");
                }
            } else {
                OppoAutomaticBrightnessController oppoAutomaticBrightnessController2 = OppoAutoBrightControllerHelper.this.mAutomaticBC;
                if (OppoAutomaticBrightnessController.mProximityNear) {
                    OppoAutoBrightControllerHelper.this.mAutomaticBC.setProximityNearPara();
                    OppoAutoBrightControllerHelper.this.mHandler.removeMessages(OppoAutoBrightControllerHelper.MSG_UPDATE_BRIGHTNESS_AFTER_PROXIMITY);
                    OppoAutoBrightControllerHelper.this.mHandler.sendEmptyMessageDelayed(OppoAutoBrightControllerHelper.MSG_UPDATE_BRIGHTNESS_AFTER_PROXIMITY, OppoAutoBrightControllerHelper.this.mAutomaticBC.getDarkeningLightDebounceConfigTime());
                }
                OppoAutomaticBrightnessController oppoAutomaticBrightnessController3 = OppoAutoBrightControllerHelper.this.mAutomaticBC;
                OppoAutomaticBrightnessController.mProximityNear = false;
            }
            if (this.mPrevProximityNear) {
                OppoAutomaticBrightnessController oppoAutomaticBrightnessController4 = OppoAutoBrightControllerHelper.this.mAutomaticBC;
                if (!OppoAutomaticBrightnessController.mProximityNear) {
                    OppoAutoBrightControllerHelper.this.mProximitySensorChangeTime = time;
                }
            }
            OppoAutomaticBrightnessController oppoAutomaticBrightnessController5 = OppoAutoBrightControllerHelper.this.mAutomaticBC;
            this.mPrevProximityNear = OppoAutomaticBrightnessController.mProximityNear;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private HashMap<String, String> mReportMap;
    private long mZeroStartTime = 0;
    private TimerTask mZeroTask;
    private Timer mZeroTimer;
    private boolean mbStartTimer = false;
    private Handler zeroHandler = new Handler() {
        /* class com.android.server.display.OppoAutoBrightControllerHelper.AnonymousClass2 */

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                OppoAutoBrightControllerHelper.this.mAutomaticBC.zeroHandlerSetPara();
            }
            if (OppoAutoBrightControllerHelper.this.mbStartTimer) {
                OppoAutoBrightControllerHelper.this.stopZeroTimer();
            }
        }
    };

    public OppoAutoBrightControllerHelper(OppoAutomaticBrightnessController abc, Context context, Handler handler) {
        this.mAutomaticBC = abc;
        this.mContext = context;
        this.mHandler = new DisplayAutoBrightnessHandler(handler.getLooper());
        mOppoBrightUtils = OppoBrightUtils.getInstance();
    }

    private void startZeroTimerInternal() {
        synchronized (this) {
            if (this.mZeroTimer == null) {
                this.mZeroTimer = new Timer();
            }
            if (this.mZeroTask == null) {
                this.mZeroTask = new TimerTask() {
                    /* class com.android.server.display.OppoAutoBrightControllerHelper.AnonymousClass1 */

                    public void run() {
                        Message msg = new Message();
                        msg.what = 1;
                        OppoAutoBrightControllerHelper.this.zeroHandler.sendMessage(msg);
                    }
                };
            }
            if (!(this.mZeroTimer == null || this.mZeroTask == null)) {
                this.mZeroTimer.schedule(this.mZeroTask, 5000, 5000);
            }
        }
    }

    public boolean isStartZeroTimer() {
        return this.mbStartTimer;
    }

    public void startZeroTimer(long time) {
        this.mZeroStartTime = time;
        this.mbStartTimer = true;
        startZeroTimerInternal();
    }

    public void stopZeroTimer() {
        if (this.mbStartTimer) {
            synchronized (this) {
                try {
                    this.mbStartTimer = false;
                    if (this.mZeroTimer != null) {
                        this.mZeroTimer.cancel();
                        this.mZeroTimer = null;
                    }
                    if (this.mZeroTask != null) {
                        this.mZeroTask.cancel();
                        this.mZeroTask = null;
                    }
                } catch (NullPointerException e) {
                    Slog.i(TAG, "stopZeroTimer null pointer", e);
                }
            }
        }
    }

    public long getProximitySensorChangeTime() {
        return this.mProximitySensorChangeTime;
    }

    public void setAutoReportTime() {
        this.mAutoTime = System.currentTimeMillis();
        this.mAutoDurationTime = System.currentTimeMillis();
        this.mAutoManulTime = System.currentTimeMillis();
        this.mAutoManulDurationTime = System.currentTimeMillis();
    }

    public void setAutoManulReportMessage(long currentTime) {
        if (currentTime - this.mAutoManulTime > 2000) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_REPORT_AUTO_MANUL_BRIGHTNESS), 2000);
            this.mAutoManulTime = currentTime;
        }
    }

    public void setAutoReportMessage(long currentTime, boolean directset) {
        if (directset) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_REPORT_AUTO_BRIGHTNESS), 2000);
            OppoDisplayPowerControlBrightnessHelper.setManulReportTime();
        } else if (currentTime - this.mAutoTime > 2000) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_REPORT_AUTO_BRIGHTNESS), 2000);
            this.mAutoTime = currentTime;
        }
    }

    /* access modifiers changed from: private */
    public class DisplayAutoBrightnessHandler extends Handler {
        public DisplayAutoBrightnessHandler(Looper looper) {
            super(looper);
            Slog.d(OppoAutoBrightControllerHelper.TAG, "DisplayAutoBrightnessHandler init");
        }

        public void handleMessage(Message msg) {
            Slog.d(OppoAutoBrightControllerHelper.TAG, "handleMessage:" + msg.what);
            switch (msg.what) {
                case OppoAutoBrightControllerHelper.MSG_REPORT_AUTO_BRIGHTNESS /* 201 */:
                    if (OppoAutoBrightControllerHelper.this.mReportMap == null) {
                        OppoAutoBrightControllerHelper.this.mReportMap = new HashMap();
                    }
                    OppoAutoBrightControllerHelper.this.mReportMap.put("AutoBrightness", String.valueOf(OppoAutoBrightControllerHelper.this.mLastAutoBrightness));
                    OppoAutoBrightControllerHelper.this.mAutoDurationTime = System.currentTimeMillis();
                    OppoAutoBrightControllerHelper oppoAutoBrightControllerHelper = OppoAutoBrightControllerHelper.this;
                    oppoAutoBrightControllerHelper.mLastAutoBrightness = (float) oppoAutoBrightControllerHelper.mAutomaticBC.getAutomaticScreenBrightness();
                    OppoAutoBrightControllerHelper.this.mReportMap.clear();
                    return;
                case OppoAutoBrightControllerHelper.MSG_REPORT_AUTO_MANUL_BRIGHTNESS /* 202 */:
                    if (OppoAutoBrightControllerHelper.this.mReportMap == null) {
                        OppoAutoBrightControllerHelper.this.mReportMap = new HashMap();
                    }
                    OppoAutoBrightControllerHelper.this.mReportMap.put("AutoManulBrightness", String.valueOf(OppoAutoBrightControllerHelper.this.mLastAutoBrightness));
                    OppoAutoBrightControllerHelper.this.mReportMap.put("AutoManulDurationTime", OppoAutoBrightControllerHelper.mOppoBrightUtils.getDurationTime(System.currentTimeMillis() - OppoAutoBrightControllerHelper.this.mAutoManulDurationTime));
                    OppoAutoBrightControllerHelper.this.mReportMap.put("PackageName", OppoAutoBrightControllerHelper.mOppoBrightUtils.getTopPackageName());
                    OppoAutoBrightControllerHelper.this.mReportMap.put("SystemTime", OppoAutoBrightControllerHelper.mOppoBrightUtils.getSystemTime());
                    OppoAutoBrightControllerHelper.mOppoBrightUtils.autoBackLightReport("20180002", OppoAutoBrightControllerHelper.this.mReportMap);
                    OppoAutoBrightControllerHelper.this.mAutoDurationTime = System.currentTimeMillis();
                    OppoAutoBrightControllerHelper.this.mAutoManulDurationTime = System.currentTimeMillis();
                    OppoAutoBrightControllerHelper oppoAutoBrightControllerHelper2 = OppoAutoBrightControllerHelper.this;
                    OppoBrightUtils unused = OppoAutoBrightControllerHelper.mOppoBrightUtils;
                    oppoAutoBrightControllerHelper2.mLastAutoBrightness = (float) OppoBrightUtils.mManualBrightness;
                    OppoAutoBrightControllerHelper.this.mReportMap.clear();
                    return;
                case OppoAutoBrightControllerHelper.MSG_UPDATE_BRIGHTNESS_AFTER_PROXIMITY /* 203 */:
                    OppoAutoBrightControllerHelper.this.mAutomaticBC.callbackUpdateBrightness();
                    return;
                default:
                    return;
            }
        }
    }
}
