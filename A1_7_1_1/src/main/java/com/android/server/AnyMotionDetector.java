package com.android.server;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Slog;
import com.android.server.display.OppoBrightUtils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.oppo.IElsaManager;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class AnyMotionDetector {
    private static final long ACCELEROMETER_DATA_TIMEOUT_MILLIS = 3000;
    public static boolean DEBUG = false;
    private static final long ORIENTATION_MEASUREMENT_DURATION_MILLIS = 2500;
    private static final long ORIENTATION_MEASUREMENT_INTERVAL_MILLIS = 5000;
    public static final int RESULT_MOVED = 1;
    public static final int RESULT_STATIONARY = 0;
    public static final int RESULT_UNKNOWN = -1;
    private static final int SAMPLING_INTERVAL_MILLIS = 40;
    private static final int STALE_MEASUREMENT_TIMEOUT_MILLIS = 120000;
    private static final int STATE_ACTIVE = 1;
    private static final int STATE_INACTIVE = 0;
    private static final String TAG = "AnyMotionDetector";
    private static final long WAKELOCK_TIMEOUT_MILLIS = 30000;
    private final float THRESHOLD_ENERGY;
    private Sensor mAccelSensor;
    private DeviceIdleCallback mCallback;
    private Vector3 mCurrentGravityVector;
    private final Handler mHandler;
    private final SensorEventListener mListener;
    private final Object mLock;
    private boolean mMeasurementInProgress;
    private final Runnable mMeasurementTimeout;
    private int mNumSufficientSamples;
    private Vector3 mPreviousGravityVector;
    private RunningSignalStats mRunningStats;
    private SensorManager mSensorManager;
    private final Runnable mSensorRestart;
    private int mState;
    private final float mThresholdAngle;
    private WakeLock mWakeLock;
    private final Runnable mWakelockTimeout;

    interface DeviceIdleCallback {
        void onAnyMotionResult(int i);
    }

    private static class RunningSignalStats {
        Vector3 currentVector;
        float energy;
        Vector3 previousVector;
        Vector3 runningSum;
        int sampleCount;

        public RunningSignalStats() {
            reset();
        }

        public void reset() {
            this.previousVector = null;
            this.currentVector = null;
            this.runningSum = new Vector3(0, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
            this.energy = OppoBrightUtils.MIN_LUX_LIMITI;
            this.sampleCount = 0;
        }

        public void accumulate(Vector3 v) {
            if (v == null) {
                if (AnyMotionDetector.DEBUG) {
                    Slog.i(AnyMotionDetector.TAG, "Cannot accumulate a null vector.");
                }
                return;
            }
            this.sampleCount++;
            this.runningSum = this.runningSum.plus(v);
            this.previousVector = this.currentVector;
            this.currentVector = v;
            if (this.previousVector != null) {
                Vector3 dv = this.currentVector.minus(this.previousVector);
                float incrementalEnergy = ((dv.x * dv.x) + (dv.y * dv.y)) + (dv.z * dv.z);
                this.energy += incrementalEnergy;
                if (AnyMotionDetector.DEBUG) {
                    Slog.i(AnyMotionDetector.TAG, "Accumulated vector " + this.currentVector.toString() + ", runningSum = " + this.runningSum.toString() + ", incrementalEnergy = " + incrementalEnergy + ", energy = " + this.energy);
                }
            }
        }

        public Vector3 getRunningAverage() {
            if (this.sampleCount > 0) {
                return this.runningSum.times(1.0f / ((float) this.sampleCount));
            }
            return null;
        }

        public float getEnergy() {
            return this.energy;
        }

        public int getSampleCount() {
            return this.sampleCount;
        }

        public String toString() {
            return (((IElsaManager.EMPTY_PACKAGE + "previousVector = " + (this.previousVector == null ? "null" : this.previousVector.toString())) + ", currentVector = " + (this.currentVector == null ? "null" : this.currentVector.toString())) + ", sampleCount = " + this.sampleCount) + ", energy = " + this.energy;
        }
    }

    public static final class Vector3 {
        public long timeMillisSinceBoot;
        public float x;
        public float y;
        public float z;

        public Vector3(long timeMillisSinceBoot, float x, float y, float z) {
            this.timeMillisSinceBoot = timeMillisSinceBoot;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float norm() {
            return (float) Math.sqrt((double) dotProduct(this));
        }

        public Vector3 normalized() {
            float mag = norm();
            return new Vector3(this.timeMillisSinceBoot, this.x / mag, this.y / mag, this.z / mag);
        }

        public float angleBetween(Vector3 other) {
            float degrees = Math.abs((float) Math.toDegrees(Math.atan2((double) cross(other).norm(), (double) dotProduct(other))));
            Slog.d(AnyMotionDetector.TAG, "angleBetween: this = " + toString() + ", other = " + other.toString() + ", degrees = " + degrees);
            return degrees;
        }

        public Vector3 cross(Vector3 v) {
            return new Vector3(v.timeMillisSinceBoot, (this.y * v.z) - (this.z * v.y), (this.z * v.x) - (this.x * v.z), (this.x * v.y) - (this.y * v.x));
        }

        public String toString() {
            return (((IElsaManager.EMPTY_PACKAGE + "timeMillisSinceBoot=" + this.timeMillisSinceBoot) + " | x=" + this.x) + ", y=" + this.y) + ", z=" + this.z;
        }

        public float dotProduct(Vector3 v) {
            return ((this.x * v.x) + (this.y * v.y)) + (this.z * v.z);
        }

        public Vector3 times(float val) {
            return new Vector3(this.timeMillisSinceBoot, this.x * val, this.y * val, this.z * val);
        }

        public Vector3 plus(Vector3 v) {
            return new Vector3(v.timeMillisSinceBoot, v.x + this.x, v.y + this.y, v.z + this.z);
        }

        public Vector3 minus(Vector3 v) {
            return new Vector3(v.timeMillisSinceBoot, this.x - v.x, this.y - v.y, this.z - v.z);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.AnyMotionDetector.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.AnyMotionDetector.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.AnyMotionDetector.<clinit>():void");
    }

    public AnyMotionDetector(PowerManager pm, Handler handler, SensorManager sm, DeviceIdleCallback callback, float thresholdAngle) {
        this.THRESHOLD_ENERGY = 5.0f;
        this.mLock = new Object();
        this.mCurrentGravityVector = null;
        this.mPreviousGravityVector = null;
        this.mCallback = null;
        this.mListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                int status = -1;
                synchronized (AnyMotionDetector.this.mLock) {
                    AnyMotionDetector.this.mRunningStats.accumulate(new Vector3(SystemClock.elapsedRealtime(), event.values[0], event.values[1], event.values[2]));
                    if (AnyMotionDetector.this.mRunningStats.getSampleCount() >= AnyMotionDetector.this.mNumSufficientSamples) {
                        status = AnyMotionDetector.this.stopOrientationMeasurementLocked();
                    }
                }
                if (status != -1) {
                    AnyMotionDetector.this.mHandler.removeCallbacks(AnyMotionDetector.this.mWakelockTimeout);
                    AnyMotionDetector.this.mCallback.onAnyMotionResult(status);
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mSensorRestart = new Runnable() {
            public void run() {
                synchronized (AnyMotionDetector.this.mLock) {
                    AnyMotionDetector.this.startOrientationMeasurementLocked();
                }
            }
        };
        this.mMeasurementTimeout = new Runnable() {
            public void run() {
                int status;
                synchronized (AnyMotionDetector.this.mLock) {
                    if (AnyMotionDetector.DEBUG) {
                        Slog.i(AnyMotionDetector.TAG, "mMeasurementTimeout. Failed to collect sufficient accel data within 3000 ms. Stopping orientation measurement.");
                    }
                    status = AnyMotionDetector.this.stopOrientationMeasurementLocked();
                }
                if (status != -1) {
                    AnyMotionDetector.this.mHandler.removeCallbacks(AnyMotionDetector.this.mWakelockTimeout);
                    AnyMotionDetector.this.mCallback.onAnyMotionResult(status);
                }
            }
        };
        this.mWakelockTimeout = new Runnable() {
            public void run() {
                synchronized (AnyMotionDetector.this.mLock) {
                    AnyMotionDetector.this.stop();
                }
            }
        };
        if (DEBUG) {
            Slog.d(TAG, "AnyMotionDetector instantiated.");
        }
        synchronized (this.mLock) {
            this.mWakeLock = pm.newWakeLock(1, TAG);
            this.mWakeLock.setReferenceCounted(false);
            this.mHandler = handler;
            this.mSensorManager = sm;
            this.mAccelSensor = this.mSensorManager.getDefaultSensor(1);
            this.mMeasurementInProgress = false;
            this.mState = 0;
            this.mCallback = callback;
            this.mThresholdAngle = thresholdAngle;
            this.mRunningStats = new RunningSignalStats();
            this.mNumSufficientSamples = (int) Math.ceil(62.5d);
            if (DEBUG) {
                Slog.d(TAG, "mNumSufficientSamples = " + this.mNumSufficientSamples);
            }
        }
    }

    public void checkForAnyMotion() {
        if (DEBUG) {
            Slog.d(TAG, "checkForAnyMotion(). mState = " + this.mState);
        }
        if (this.mState != 1) {
            synchronized (this.mLock) {
                this.mState = 1;
                if (DEBUG) {
                    Slog.d(TAG, "Moved from STATE_INACTIVE to STATE_ACTIVE.");
                }
                this.mCurrentGravityVector = null;
                this.mPreviousGravityVector = null;
                this.mWakeLock.acquire();
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, this.mWakelockTimeout), WAKELOCK_TIMEOUT_MILLIS);
                startOrientationMeasurementLocked();
            }
        }
    }

    public void stop() {
        synchronized (this.mLock) {
            if (this.mState == 1) {
                this.mState = 0;
                if (DEBUG) {
                    Slog.d(TAG, "Moved from STATE_ACTIVE to STATE_INACTIVE.");
                }
            }
            if (this.mMeasurementInProgress) {
                this.mMeasurementInProgress = false;
                this.mSensorManager.unregisterListener(this.mListener);
            }
            this.mHandler.removeCallbacks(this.mMeasurementTimeout);
            this.mHandler.removeCallbacks(this.mSensorRestart);
            this.mCurrentGravityVector = null;
            this.mPreviousGravityVector = null;
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
                this.mHandler.removeCallbacks(this.mWakelockTimeout);
            }
        }
    }

    private void startOrientationMeasurementLocked() {
        if (DEBUG) {
            Slog.d(TAG, "startOrientationMeasurementLocked: mMeasurementInProgress=" + this.mMeasurementInProgress + ", (mAccelSensor != null)=" + (this.mAccelSensor != null));
        }
        if (!this.mMeasurementInProgress && this.mAccelSensor != null) {
            if (this.mSensorManager.registerListener(this.mListener, this.mAccelSensor, EventLogTags.VOLUME_CHANGED)) {
                this.mMeasurementInProgress = true;
                this.mRunningStats.reset();
            }
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, this.mMeasurementTimeout), ACCELEROMETER_DATA_TIMEOUT_MILLIS);
        }
    }

    private int stopOrientationMeasurementLocked() {
        if (DEBUG) {
            Slog.d(TAG, "stopOrientationMeasurement. mMeasurementInProgress=" + this.mMeasurementInProgress);
        }
        int status = -1;
        if (this.mMeasurementInProgress) {
            this.mSensorManager.unregisterListener(this.mListener);
            this.mHandler.removeCallbacks(this.mMeasurementTimeout);
            this.mMeasurementInProgress = false;
            this.mPreviousGravityVector = this.mCurrentGravityVector;
            this.mCurrentGravityVector = this.mRunningStats.getRunningAverage();
            if (this.mRunningStats.getSampleCount() == 0) {
                Slog.w(TAG, "No accelerometer data acquired for orientation measurement.");
            }
            if (DEBUG) {
                Slog.d(TAG, "mRunningStats = " + this.mRunningStats.toString());
                String currentGravityVectorString = this.mCurrentGravityVector == null ? "null" : this.mCurrentGravityVector.toString();
                String previousGravityVectorString = this.mPreviousGravityVector == null ? "null" : this.mPreviousGravityVector.toString();
                Slog.d(TAG, "mCurrentGravityVector = " + currentGravityVectorString);
                Slog.d(TAG, "mPreviousGravityVector = " + previousGravityVectorString);
            }
            this.mRunningStats.reset();
            status = getStationaryStatus();
            if (DEBUG) {
                Slog.d(TAG, "getStationaryStatus() returned " + status);
            }
            if (status != -1) {
                if (this.mWakeLock.isHeld()) {
                    this.mWakeLock.release();
                    this.mHandler.removeCallbacks(this.mWakelockTimeout);
                }
                if (DEBUG) {
                    Slog.d(TAG, "Moved from STATE_ACTIVE to STATE_INACTIVE. status = " + status);
                }
                this.mState = 0;
            } else {
                if (DEBUG) {
                    Slog.d(TAG, "stopOrientationMeasurementLocked(): another measurement scheduled in 5000 milliseconds.");
                }
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, this.mSensorRestart), 5000);
            }
        }
        return status;
    }

    public int getStationaryStatus() {
        if (this.mPreviousGravityVector == null || this.mCurrentGravityVector == null) {
            return -1;
        }
        float angle = this.mPreviousGravityVector.normalized().angleBetween(this.mCurrentGravityVector.normalized());
        if (DEBUG) {
            Slog.d(TAG, "getStationaryStatus: angle = " + angle + " energy = " + this.mRunningStats.getEnergy());
        }
        if (angle < this.mThresholdAngle && this.mRunningStats.getEnergy() < 5.0f) {
            return 0;
        }
        if (Float.isNaN(angle)) {
            return 1;
        }
        long diffTime = this.mCurrentGravityVector.timeMillisSinceBoot - this.mPreviousGravityVector.timeMillisSinceBoot;
        if (diffTime <= JobStatus.DEFAULT_TRIGGER_MAX_DELAY) {
            return 1;
        }
        if (DEBUG) {
            Slog.d(TAG, "getStationaryStatus: mPreviousGravityVector is too stale at " + diffTime + " ms ago. Returning RESULT_UNKNOWN.");
        }
        return -1;
    }
}
