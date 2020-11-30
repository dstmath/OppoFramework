package com.oppo.internal.telephony.explock;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.telephony.Rlog;
import com.android.internal.telephony.OemConstant;
import java.util.Locale;

public class OemSensorStateMonitor extends Handler implements SensorEventListener {
    private static final float FAR_THRESHOLD = 5.0f;
    private static final int STEP_WRITE_INTERVAL = 10;
    public static final String TAG = "OemSensorStateMonitor";
    private static int mCurrentStepCounter = 0;
    private static int mLastSensorState;
    private static float mMaxValue = FAR_THRESHOLD;
    private static Sensor mProximitySensor = null;
    private static Sensor mStepCounterSensor = null;
    private static Context sContext = null;
    private static OemSensorStateMonitor sInstance = null;
    private volatile boolean mIsProximListen = false;
    private volatile boolean mIsStepCounterListen = false;
    private int mProximityEventCount = 0;
    private SensorManager mSM = null;

    public static synchronized OemSensorStateMonitor getDefault(Context context) {
        OemSensorStateMonitor oemSensorStateMonitor;
        synchronized (OemSensorStateMonitor.class) {
            if (sInstance == null) {
                sContext = context;
                sInstance = new OemSensorStateMonitor(context);
            }
            logd(TAG);
            oemSensorStateMonitor = sInstance;
        }
        return oemSensorStateMonitor;
    }

    public OemSensorStateMonitor(Context context) {
        this.mSM = (SensorManager) context.getSystemService("sensor");
        SensorManager sensorManager = this.mSM;
        if (sensorManager != null) {
            mProximitySensor = sensorManager.getDefaultSensor(8);
            mStepCounterSensor = this.mSM.getDefaultSensor(19);
            Sensor sensor = mProximitySensor;
            if (sensor != null) {
                mMaxValue = sensor.getMaximumRange();
            }
        }
        mLastSensorState = 0;
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.values != null && event.values.length != 0) {
            if (event.sensor.getType() == 19) {
                if (mCurrentStepCounter + 10 <= ((int) event.values[0])) {
                    setSensorChangedInfo(19, ((int) event.values[0]) - mCurrentStepCounter, 1, true);
                    mCurrentStepCounter = (int) event.values[0];
                    logd("onSensorChanged step counter:" + String.format(Locale.US, "%d\n", Integer.valueOf((int) event.values[0])));
                    logd("onSensorChanged step mcurrent counter:" + String.format(Locale.US, "%d\n", Integer.valueOf(mCurrentStepCounter)));
                }
            } else if (event.sensor.getType() == 8) {
                int state = getStateFromValue(event.values[0]);
                synchronized (event) {
                    if (state != mLastSensorState) {
                        this.mProximityEventCount++;
                        mLastSensorState = state;
                        logd("onSensorChanged mProximityEventCount = " + this.mProximityEventCount);
                        setSensorChangedInfo(8, 1, state, true);
                    }
                }
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private int getStateFromValue(float value) {
        return (value > FAR_THRESHOLD || value == mMaxValue) ? 1 : 0;
    }

    public void notifyRegisterSensor(int type) {
        Sensor sensor;
        Sensor sensor2;
        SensorManager sensorManager = this.mSM;
        if (sensorManager == null || (sensor2 = mProximitySensor) == null || 8 != type) {
            SensorManager sensorManager2 = this.mSM;
            if (sensorManager2 != null && (sensor = mStepCounterSensor) != null && 19 == type) {
                sensorManager2.registerListener(this, sensor, 3);
                return;
            }
            return;
        }
        sensorManager.registerListener(this, sensor2, 3);
    }

    public void notifyUnRegisterSensor(int type) {
        Sensor sensor;
        Sensor sensor2;
        SensorManager sensorManager = this.mSM;
        if (sensorManager == null || (sensor2 = mProximitySensor) == null || 8 != type) {
            SensorManager sensorManager2 = this.mSM;
            if (sensorManager2 != null && (sensor = mStepCounterSensor) != null && 19 == type) {
                sensorManager2.unregisterListener(this, sensor);
                return;
            }
            return;
        }
        sensorManager.unregisterListener(this, sensor2);
    }

    public void setSensorChangedInfo(int type, int changeCount, int status, boolean isWorking) {
        OemRegionLockMonitorManager orm = OemRegionLockMonitorManager.getInstance();
        if (orm != null) {
            orm.updateSensorChangedInfo(type, changeCount, status, isWorking);
        }
    }

    static void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(TAG, s);
        }
    }

    static void loge(String s) {
        Rlog.e(TAG, s);
    }
}
