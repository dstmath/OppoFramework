package com.android.server.face.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import com.android.server.face.tool.ExHandler;
import com.android.server.face.utils.LogUtil;

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
public class SensorManagerClient {
    public static final int MSG_REGISTER_LIGHT_SENSOR = 1;
    public static final int MSG_UNREGISTER_LIGHT_SENSOR = 2;
    private static final String TAG = "FaceService.SensorManagerClient";
    private static Object mMutex;
    private static SensorManagerClient mSingleInstance;
    private final int FLAG_CLEAR_LIGHT_INTENSITY_FOR_CAMERA_SERVICE;
    private final int FLAG_SET_LIGHT_INTENSITY_FOR_CAMERA_SERVICE;
    private final String PROP_LIGHT_INTENSITY_FOR_CAMERA_SERVICE;
    private final Context mContext;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHasNotified;
    private boolean mIsRegistered;
    private LightSensorListener mLightSensorListener;
    private Looper mLooper;
    private Sensor mSensor;
    private SensorManager mSensorManager;

    private class LightSensorListener implements SensorEventListener {
        private int mLightSensor;
        private final boolean mbLightSet;

        /* synthetic */ LightSensorListener(SensorManagerClient this$0, LightSensorListener lightSensorListener) {
            this();
        }

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
                SystemProperties.set("oppo.camera.lightIntensity", Integer.toString(this.mLightSensor | 536870912));
                LogUtil.d(SensorManagerClient.TAG, "set PROP_LIGHT_INTENSITY_FOR_CAMERA_SERVICE for cameraService, mLightSensor = " + this.mLightSensor);
            }
        }

        public boolean shouldNotify() {
            if (SensorManagerClient.this.mHasNotified) {
                return false;
            }
            SensorManagerClient.this.mHasNotified = true;
            return true;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.face.sensor.SensorManagerClient.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.face.sensor.SensorManagerClient.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.face.sensor.SensorManagerClient.<clinit>():void");
    }

    public SensorManagerClient(Context context) {
        this.mIsRegistered = false;
        this.mSensorManager = null;
        this.mHasNotified = false;
        this.mLightSensorListener = null;
        this.PROP_LIGHT_INTENSITY_FOR_CAMERA_SERVICE = "oppo.camera.lightIntensity";
        this.FLAG_SET_LIGHT_INTENSITY_FOR_CAMERA_SERVICE = 536870912;
        this.FLAG_CLEAR_LIGHT_INTENSITY_FOR_CAMERA_SERVICE = 1073741824;
        this.mContext = context;
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        this.mSensor = this.mSensorManager.getDefaultSensor(5);
        this.mLightSensorListener = new LightSensorListener(this, null);
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
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        SensorManagerClient.this.handleRegister();
                        break;
                    case 2:
                        SensorManagerClient.this.handleUnRegister();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public static SensorManagerClient getInstance(Context context) {
        synchronized (mMutex) {
            if (mSingleInstance == null) {
                mSingleInstance = new SensorManagerClient(context);
            }
        }
        return mSingleInstance;
    }

    public void register() {
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    public void unRegister() {
        this.mHandler.obtainMessage(2).sendToTarget();
    }

    private void handleRegister() {
        synchronized (mMutex) {
            if (!(this.mIsRegistered || this.mLightSensorListener == null || this.mSensorManager == null)) {
                this.mIsRegistered = true;
                this.mHasNotified = false;
                LogUtil.d(TAG, "registerListener");
                this.mSensorManager.registerListener(this.mLightSensorListener, this.mSensor, 0);
                LogUtil.d(TAG, "registerListener finish");
            }
        }
    }

    private void handleUnRegister() {
        synchronized (mMutex) {
            if (!(!this.mIsRegistered || this.mLightSensorListener == null || this.mSensorManager == null)) {
                LogUtil.d(TAG, "unregisterListener");
                this.mIsRegistered = false;
                this.mSensorManager.unregisterListener(this.mLightSensorListener);
                this.mHasNotified = false;
                SystemProperties.set("oppo.camera.lightIntensity", Integer.toString(this.mLightSensorListener.getLightSensorVaule() | 1073741824));
            }
        }
    }
}
