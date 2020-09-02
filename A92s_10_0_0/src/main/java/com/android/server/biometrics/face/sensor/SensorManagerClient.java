package com.android.server.biometrics.face.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import com.android.server.biometrics.face.tool.ExHandler;
import com.android.server.biometrics.face.utils.LogUtil;

public class SensorManagerClient {
    private static final int FLAG_CLEAR_LIGHT_INTENSITY_FOR_CAMERA_SERVICE = 1073741824;
    private static final int FLAG_SET_LIGHT_INTENSITY_FOR_CAMERA_SERVICE = 536870912;
    public static final int MSG_REGISTER_LIGHT_SENSOR = 1;
    public static final int MSG_UNREGISTER_LIGHT_SENSOR = 2;
    private static final String PROP_LIGHT_INTENSITY_FOR_CAMERA_SERVICE = "sys.camera.lightIntensity";
    private static final String TAG = "FaceService.SensorManagerClient";
    private static Object sMutex = new Object();
    private static SensorManagerClient sSingleInstance;
    private final Context mContext;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    /* access modifiers changed from: private */
    public boolean mHasNotified = false;
    private boolean mIsRegistered = false;
    private LightSensorListener mLightSensorListener = null;
    private Looper mLooper;
    private Sensor mSensor;
    private SensorManager mSensorManager = null;

    public SensorManagerClient(Context context) {
        this.mContext = context;
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        this.mSensor = this.mSensorManager.getDefaultSensor(5);
        this.mLightSensorListener = new LightSensorListener();
        this.mHandlerThread = new HandlerThread("SensorManagerClient thread");
        this.mHandlerThread.start();
        this.mLooper = this.mHandlerThread.getLooper();
        if (this.mLooper == null) {
            LogUtil.e(TAG, "mLooper null");
        }
        initHandler();
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            /* class com.android.server.biometrics.face.sensor.SensorManagerClient.AnonymousClass1 */

            @Override // com.android.server.biometrics.face.tool.ExHandler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    SensorManagerClient.this.handleRegister();
                } else if (i == 2) {
                    SensorManagerClient.this.handleUnRegister();
                }
                super.handleMessage(msg);
            }
        };
    }

    public static SensorManagerClient getInstance(Context context) {
        SensorManagerClient sensorManagerClient;
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new SensorManagerClient(context);
            }
            sensorManagerClient = sSingleInstance;
        }
        return sensorManagerClient;
    }

    public void register() {
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    public void unRegister() {
        this.mHandler.obtainMessage(2).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void handleRegister() {
        synchronized (sMutex) {
            if (!(this.mIsRegistered || this.mLightSensorListener == null || this.mSensorManager == null)) {
                this.mIsRegistered = true;
                this.mHasNotified = false;
                LogUtil.d(TAG, "registerListener");
                this.mSensorManager.registerListener(this.mLightSensorListener, this.mSensor, 0);
                LogUtil.d(TAG, "registerListener finish");
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleUnRegister() {
        synchronized (sMutex) {
            if (!(!this.mIsRegistered || this.mLightSensorListener == null || this.mSensorManager == null)) {
                LogUtil.d(TAG, "unregisterListener");
                this.mIsRegistered = false;
                this.mSensorManager.unregisterListener(this.mLightSensorListener);
                this.mHasNotified = false;
                SystemProperties.set(PROP_LIGHT_INTENSITY_FOR_CAMERA_SERVICE, Integer.toString(this.mLightSensorListener.getLightSensorVaule() | FLAG_CLEAR_LIGHT_INTENSITY_FOR_CAMERA_SERVICE));
            }
        }
    }

    private class LightSensorListener implements SensorEventListener {
        private int mLightSensor;
        private final boolean mbLightSet;

        private LightSensorListener() {
            this.mbLightSet = false;
            this.mLightSensor = 0;
        }

        public int getLightSensorVaule() {
            return this.mLightSensor;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            this.mLightSensor = (int) event.values[0];
            if (shouldNotify()) {
                LogUtil.d(SensorManagerClient.TAG, "onSensorChanged PROP_LIGHT_INTENSITY_FOR_CAMERA_SERVICE");
                SystemProperties.set(SensorManagerClient.PROP_LIGHT_INTENSITY_FOR_CAMERA_SERVICE, Integer.toString(this.mLightSensor | SensorManagerClient.FLAG_SET_LIGHT_INTENSITY_FOR_CAMERA_SERVICE));
                LogUtil.d(SensorManagerClient.TAG, "set PROP_LIGHT_INTENSITY_FOR_CAMERA_SERVICE for cameraService, mLightSensor = " + this.mLightSensor);
            }
        }

        public boolean shouldNotify() {
            if (SensorManagerClient.this.mHasNotified) {
                return false;
            }
            boolean unused = SensorManagerClient.this.mHasNotified = true;
            return true;
        }
    }
}
