package com.android.server.fingerprint.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.server.display.OppoBrightUtils;
import com.android.server.fingerprint.tool.ExHandler;
import com.android.server.fingerprint.util.LogUtil;

public class ProximitySensorManager {
    public static final int MSG_REGISTER_PROXIMITY_SENSOR = 1;
    public static final int MSG_UNREGISTER_PROXIMITY_SENSOR = 2;
    public static final String TAG = "FingerprintService.ProximitySensorManager";
    private static Object sMutex = new Object();
    private static ProximitySensorManager sSingleInstance;
    private boolean mDoubleHomeGestureEnabled = false;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsEnableProximitySensor = false;
    private boolean mIsKeyguardAuthenticationStarted = false;
    private boolean mIsKeyguardTouchMonitorStarted = false;
    private boolean mIsNearState = false;
    private boolean mIsRegistered = false;
    private boolean mIsScreenOff = false;
    private IProximitySensorEventListener mListener;
    private Object mLock = new Object();
    private Looper mLooper;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent arg0) {
            if (arg0.values[0] == OppoBrightUtils.MIN_LUX_LIMITI) {
                ProximitySensorManager.this.mIsNearState = true;
            } else {
                ProximitySensorManager.this.mIsNearState = false;
            }
            ProximitySensorManager.this.mListener.onSensorChanged(ProximitySensorManager.this.mIsNearState);
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }
    };
    private SensorManager mSensorManager;

    public static void initPsensorManager(Context c, IProximitySensorEventListener listener) {
        getProximitySensorManager(c, listener);
    }

    public static ProximitySensorManager getProximitySensorManager(Context c, IProximitySensorEventListener listener) {
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new ProximitySensorManager(c, listener);
            }
        }
        return sSingleInstance;
    }

    public static ProximitySensorManager getProximitySensorManager() {
        return sSingleInstance;
    }

    public ProximitySensorManager(Context context, IProximitySensorEventListener listener) {
        this.mListener = listener;
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        this.mSensor = this.mSensorManager.getDefaultSensor(8);
        this.mHandlerThread = new HandlerThread("ProximitySensorManager thread");
        this.mHandlerThread.start();
        this.mLooper = this.mHandlerThread.getLooper();
        if (this.mLooper == null) {
            LogUtil.e(TAG, "mLooper null");
        }
        initHandler();
    }

    public void register() {
        synchronized (this.mLock) {
            if (!this.mIsRegistered) {
                this.mIsRegistered = true;
                this.mListener.onRegisterStateChanged(this.mIsRegistered);
                LogUtil.d(TAG, "registerListener");
                this.mSensorManager.registerListener(this.mSensorEventListener, this.mSensor, 2);
            }
        }
    }

    public void unRegister() {
        synchronized (this.mLock) {
            if (this.mIsRegistered) {
                LogUtil.d(TAG, "unregisterListener");
                this.mIsRegistered = false;
                this.mListener.onRegisterStateChanged(this.mIsRegistered);
                this.mSensorManager.unregisterListener(this.mSensorEventListener);
            }
        }
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        ProximitySensorManager.this.register();
                        break;
                    case 2:
                        ProximitySensorManager.this.unRegister();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public void onDoubleHomeGestureEnabled(boolean isEnable) {
        this.mDoubleHomeGestureEnabled = isEnable;
        this.mIsEnableProximitySensor = this.mDoubleHomeGestureEnabled ? this.mIsScreenOff : false;
        LogUtil.d(TAG, "mDoubleHomeGestureEnabled = " + this.mDoubleHomeGestureEnabled + ", mIsEnableProximitySensor = " + this.mIsEnableProximitySensor);
        notifyProximitySensorStateChanged(this.mIsEnableProximitySensor);
    }

    public void onAuthenticationStarted(boolean isAuthenticationStarted) {
        this.mIsKeyguardAuthenticationStarted = isAuthenticationStarted;
        this.mIsEnableProximitySensor = this.mIsKeyguardAuthenticationStarted ? this.mIsScreenOff : false;
        LogUtil.d(TAG, "mIsKeyguardAuthenticationStarted = " + this.mIsKeyguardAuthenticationStarted + ", mIsEnableProximitySensor = " + this.mIsEnableProximitySensor);
        notifyProximitySensorStateChanged(this.mIsEnableProximitySensor);
    }

    public void onTouchMonitorStarted(boolean isTouchMonitorStarted) {
        this.mIsKeyguardTouchMonitorStarted = isTouchMonitorStarted;
        this.mIsEnableProximitySensor = this.mIsKeyguardTouchMonitorStarted ? this.mIsScreenOff : false;
        LogUtil.d(TAG, "mIsKeyguardTouchMonitorStarted = " + this.mIsKeyguardTouchMonitorStarted + ", mIsEnableProximitySensor = " + this.mIsEnableProximitySensor);
        notifyProximitySensorStateChanged(this.mIsEnableProximitySensor);
    }

    public void dispatchScreenOff(boolean isScreenOff) {
        this.mIsScreenOff = isScreenOff;
        boolean z = ((this.mIsKeyguardAuthenticationStarted && this.mIsScreenOff) || (this.mDoubleHomeGestureEnabled && this.mIsScreenOff)) ? true : this.mIsKeyguardTouchMonitorStarted ? this.mIsScreenOff : false;
        this.mIsEnableProximitySensor = z;
        LogUtil.d(TAG, "mIsScreenOff = " + this.mIsScreenOff + ", mDoubleHomeGestureEnabled = " + this.mDoubleHomeGestureEnabled + ", mIsKeyguardTouchMonitorStarted = " + this.mIsKeyguardTouchMonitorStarted + ", mIsEnableProximitySensor = " + this.mIsEnableProximitySensor);
        notifyProximitySensorStateChanged(this.mIsEnableProximitySensor);
    }

    public void notifyProximitySensorStateChanged(boolean isEnable) {
        if (isEnable) {
            this.mHandler.obtainMessage(1).sendToTarget();
        } else {
            this.mHandler.obtainMessage(2).sendToTarget();
        }
    }
}
