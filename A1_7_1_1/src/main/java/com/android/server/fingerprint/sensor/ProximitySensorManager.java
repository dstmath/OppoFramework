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

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ProximitySensorManager {
    public static final int MSG_REGISTER_PROXIMITY_SENSOR = 1;
    public static final int MSG_UNREGISTER_PROXIMITY_SENSOR = 2;
    public static final String TAG = "FingerprintService.ProximitySensorManager";
    private static Object mMutex;
    private static ProximitySensorManager mSingleInstance;
    private boolean mDoubleHomeGestureEnabled;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsEnableProximitySensor;
    private boolean mIsKeyguardAuthenticationStarted;
    private boolean mIsKeyguardTouchMonitorStarted;
    private boolean mIsNearState;
    private boolean mIsRegistered;
    private boolean mIsScreenOff;
    private IProximitySensorEventListener mListener;
    private Object mLock;
    private Looper mLooper;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener;
    private SensorManager mSensorManager;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.sensor.ProximitySensorManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.sensor.ProximitySensorManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.fingerprint.sensor.ProximitySensorManager.<clinit>():void");
    }

    public static void initPsensorManager(Context c, IProximitySensorEventListener listener) {
        getProximitySensorManager(c, listener);
    }

    public static ProximitySensorManager getProximitySensorManager(Context c, IProximitySensorEventListener listener) {
        synchronized (mMutex) {
            if (mSingleInstance == null) {
                mSingleInstance = new ProximitySensorManager(c, listener);
            }
        }
        return mSingleInstance;
    }

    public static ProximitySensorManager getProximitySensorManager() {
        return mSingleInstance;
    }

    public ProximitySensorManager(Context context, IProximitySensorEventListener listener) {
        this.mIsRegistered = false;
        this.mIsNearState = false;
        this.mIsScreenOff = false;
        this.mIsKeyguardAuthenticationStarted = false;
        this.mDoubleHomeGestureEnabled = false;
        this.mIsKeyguardTouchMonitorStarted = false;
        this.mIsEnableProximitySensor = false;
        this.mLock = new Object();
        this.mSensorEventListener = new SensorEventListener() {
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
