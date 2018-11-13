package com.android.server.oppo;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoAppStartupManager;
import com.android.server.am.OppoProcessManager;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

public final class OppoFallingMonitor {
    static final int MSG_REG = 10001;
    private static final String TAG = "OppoFallingMonitor";
    private final int FALLINGID = 33171025;
    private final String debugSwitch = "persist.sys.fall.debugswitch";
    private final String enableSwitch = "persist.sys.fall.enable";
    private Context mContext;
    private boolean mDebug = false;
    String mDirPath = "/data/oppo_log/";
    private boolean mEnable = true;
    Handler mFallingHandler;
    HandlerThread mFallingHandlerThread;
    Looper mFallingLooper;
    private Sensor mFallingSensor;
    private SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            Slog.v(OppoFallingMonitor.TAG, "values.length = " + event.values.length);
            String log = String.format("time=%f speed=%f height=%f pitch=%f roo=%f  soft=%f \r\n", new Object[]{Float.valueOf(event.values[0]), Float.valueOf(event.values[1]), Float.valueOf(event.values[2]), Float.valueOf(event.values[3]), Float.valueOf(event.values[4]), Float.valueOf(event.values[5])});
            OppoFallingMonitor.this.handlerUploadDCS(event.values);
            if (OppoFallingMonitor.this.mDebug) {
                Slog.v(OppoFallingMonitor.TAG, log);
                OppoFallingMonitor.this.recordData(log);
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }
    };
    FileWriter mLogWriter;
    private SensorManager mSensorManager;

    class FallingHandler extends Handler {
        FallingHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 10001:
                    OppoFallingMonitor.this.registerSensor();
                    return;
                default:
                    return;
            }
        }
    }

    void initHandlerThread() {
        if (this.mFallingHandlerThread == null) {
            Slog.v(TAG, "initHandlerThread");
            this.mFallingHandlerThread = new HandlerThread("FallingMonitorThread");
            this.mFallingHandlerThread.start();
            this.mFallingLooper = this.mFallingHandlerThread.getLooper();
            this.mFallingHandler = new FallingHandler(this.mFallingLooper);
        }
    }

    void destroyHandlerThread() {
        try {
            if (this.mFallingLooper != null) {
                this.mFallingLooper.quitSafely();
                this.mFallingLooper = null;
            }
            if (this.mFallingHandlerThread != null) {
                this.mFallingHandlerThread.quitSafely();
                this.mFallingHandlerThread = null;
            }
        } catch (Exception e) {
            Slog.v(TAG, "stop Fallingthread error e = " + e.toString());
        }
    }

    void handlerUploadDCS(float[] events) {
        try {
            boolean isScreenOn = ((PowerManager) this.mContext.getSystemService("power")).isScreenOn();
            String appName = getForeGroundPackage();
            Map<String, String> map = new HashMap();
            map.put("time_stamp", String.valueOf(events[0]));
            map.put("speed", String.valueOf(events[1]));
            map.put("height", String.valueOf(events[2]));
            map.put("pitch", String.valueOf(events[3]));
            map.put("roll", String.valueOf(events[4]));
            map.put("hardness", String.valueOf(events[5]));
            map.put("GPS", "empty");
            map.put("screen_on", String.valueOf(isScreenOn));
            map.put(OppoProcessManager.RESUME_REASON_TOPAPP_STR, appName);
            map.put("count", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
            OppoStatistics.onCommon(this.mContext, "CriticalLog", "falling_data", map, false);
            Slog.v(TAG, "falling data upload app:" + appName + " screen_on : " + isScreenOn);
        } catch (Exception e) {
            Slog.e(TAG, "upload falling data error : " + e.toString());
        }
    }

    public String getForeGroundPackage() {
        ComponentName cn = ((ActivityManager) this.mContext.getSystemService(OppoAppStartupManager.TYPE_ACTIVITY)).getTopAppName();
        if (cn != null) {
            return cn.getPackageName();
        }
        return null;
    }

    public OppoFallingMonitor(Context c) {
        this.mContext = c;
        if (SystemProperties.get("persist.sys.fall.debugswitch", "false").equals("false")) {
            this.mDebug = false;
        } else {
            this.mDebug = true;
            Slog.v(TAG, "mDebug = " + this.mDebug);
        }
        if (SystemProperties.get("persist.sys.fall.enable", "true").equals("true")) {
            this.mEnable = true;
        } else {
            this.mEnable = false;
        }
    }

    private void registerSensor() {
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        if (this.mFallingSensor == null) {
            List<Sensor> l = this.mSensorManager.getSensorList(33171025);
            if (l != null && l.size() >= 1) {
                this.mFallingSensor = (Sensor) l.get(0);
            }
            if (this.mFallingSensor == null) {
                Slog.v(TAG, "can not find the sensor");
                return;
            }
            this.mSensorManager.registerListener(this.mListener, this.mFallingSensor, 0, this.mFallingHandler);
        }
    }

    private void unregisterSensor() {
        if (this.mSensorManager != null && this.mFallingSensor != null) {
            this.mSensorManager.unregisterListener(this.mListener, this.mFallingSensor);
            this.mFallingSensor = null;
        }
    }

    public void startMonitor() {
        if (this.mEnable) {
            initHandlerThread();
            Message.obtain(this.mFallingHandler, 10001).sendToTarget();
            return;
        }
        Slog.v(TAG, "mEnable is false");
    }

    public void stopMonitor() {
        unregisterSensor();
        destroyHandlerThread();
    }

    private void recordData(String s) {
        try {
            if (this.mLogWriter == null) {
                this.mLogWriter = new FileWriter(new File(String.format(this.mDirPath + "orientation_log.txt", new Object[0])), true);
            }
            Slog.v(TAG, "write to file");
            this.mLogWriter.append(new StringBuilder(getTimeString() + " ").append(s).toString());
            this.mLogWriter.close();
            this.mLogWriter = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getTimeString() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(Long.valueOf(System.currentTimeMillis()).longValue()));
    }
}
