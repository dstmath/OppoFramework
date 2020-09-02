package com.android.server.location;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import java.util.ArrayList;

public class OppoMotionDetector {
    private static final long ACCELEROMETER_DATA_TIMEOUT_MILLIS = 3000;
    public static boolean DEBUG = false;
    public static final int RESULT_MOVED = 1;
    public static final int RESULT_STATIONARY = 0;
    public static final int RESULT_UNKNOWN = -1;
    private static final int STALE_MEASUREMENT_TIMEOUT_MILLIS = 120000;
    private static final int STATE_ACTIVE = 1;
    private static final int STATE_INACTIVE = 0;
    private static final String TAG = "OppoMotionDetector";
    private Sensor mAccelSensor;
    /* access modifiers changed from: private */
    public DeviceIdleCallback mCallback = null;
    private OppoMotionConfig mConfig = null;
    private Vector3 mCurrentGravityVector = null;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mIsKilledRunnable = false;
    private final SensorEventListener mListener = new SensorEventListener() {
        /* class com.android.server.location.OppoMotionDetector.AnonymousClass1 */

        public void onSensorChanged(SensorEvent event) {
            synchronized (OppoMotionDetector.this.mLock) {
                OppoMotionDetector.this.mRunningStats.accumulate(new Vector3(SystemClock.elapsedRealtime(), event.values[0], event.values[1], event.values[2]));
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private boolean mMeasurementInProgress;
    /* access modifiers changed from: private */
    public final Runnable mMeasurementUpdate = new Runnable() {
        /* class com.android.server.location.OppoMotionDetector.AnonymousClass2 */

        public void run() {
            synchronized (OppoMotionDetector.this.mLock) {
                if (OppoMotionDetector.DEBUG) {
                    Log.i(OppoMotionDetector.TAG, "mMeasurementUpdate. Failed to collect sufficient accel data within 3000 ms. Stopping orientation measurement.");
                }
                int status = OppoMotionDetector.this.getStationaryStatus();
                if (OppoMotionDetector.DEBUG) {
                    Log.d(OppoMotionDetector.TAG, "getStationaryStatus() returned " + status);
                }
                if (status != -1) {
                    OppoMotionDetector.this.mCallback.onAnyMotionResult(status);
                }
                if (!OppoMotionDetector.this.mIsKilledRunnable) {
                    OppoMotionDetector.this.mHandler.sendMessageDelayed(Message.obtain(OppoMotionDetector.this.mHandler, OppoMotionDetector.this.mMeasurementUpdate), OppoMotionDetector.this.mMinResultTimer);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final long mMinResultTimer;
    private Vector3 mPreviousGravityVector = null;
    /* access modifiers changed from: private */
    public RunningSignalStats mRunningStats;
    private SensorManager mSensorManager;
    private int mState;

    public interface DeviceIdleCallback {
        void onAnyMotionResult(int i);
    }

    public OppoMotionDetector(SensorManager sm, DeviceIdleCallback callback, Handler handler, OppoMotionConfig config, long minTime) {
        if (DEBUG) {
            Log.d(TAG, "OppoMotionDetector instantiated.");
        }
        synchronized (this.mLock) {
            this.mSensorManager = sm;
            this.mAccelSensor = this.mSensorManager.getDefaultSensor(1);
            this.mConfig = config;
            this.mCallback = callback;
            this.mHandler = handler;
            this.mMinResultTimer = minTime;
            this.mMeasurementInProgress = false;
            this.mState = 0;
            this.mRunningStats = new RunningSignalStats(this.mConfig.getSampleNum());
        }
    }

    public void startMotion() {
        if (DEBUG) {
            Log.d(TAG, "startMotion.mConfig = " + this.mConfig.toString());
        }
        synchronized (this.mLock) {
            if (this.mState != 1) {
                if (DEBUG) {
                    Log.d(TAG, "startMonitor(). mState = " + this.mState);
                }
                this.mState = 1;
                this.mCurrentGravityVector = null;
                this.mPreviousGravityVector = null;
                startOrientationMeasurementLocked();
            }
        }
    }

    public void stopMotion() {
        synchronized (this.mLock) {
            stopOrientationMeasurementLocked();
            if (this.mState == 1) {
                this.mState = 0;
                if (DEBUG) {
                    Log.d(TAG, "Moved from STATE_ACTIVE to STATE_INACTIVE.");
                }
            }
            this.mCurrentGravityVector = null;
            this.mPreviousGravityVector = null;
        }
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    private void startOrientationMeasurementLocked() {
        Sensor sensor;
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("startOrientationMeasurementLocked: mMeasurementInProgress=");
            sb.append(this.mMeasurementInProgress);
            sb.append(", (mAccelSensor != null)=");
            sb.append(this.mAccelSensor != null);
            Log.d(TAG, sb.toString());
        }
        if (!this.mMeasurementInProgress && (sensor = this.mAccelSensor) != null) {
            if (this.mSensorManager.registerListener(this.mListener, sensor, this.mConfig.getInterval() * 1000)) {
                this.mMeasurementInProgress = true;
                this.mRunningStats.reset();
            }
            this.mIsKilledRunnable = false;
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, this.mMeasurementUpdate), ACCELEROMETER_DATA_TIMEOUT_MILLIS);
        }
    }

    private void stopOrientationMeasurementLocked() {
        if (DEBUG) {
            Log.d(TAG, "stopOrientationMeasurement. mMeasurementInProgress=" + this.mMeasurementInProgress);
        }
        if (this.mMeasurementInProgress) {
            this.mIsKilledRunnable = true;
            this.mHandler.removeCallbacks(this.mMeasurementUpdate);
            this.mSensorManager.unregisterListener(this.mListener);
            this.mMeasurementInProgress = false;
            this.mRunningStats.reset();
        }
    }

    /* access modifiers changed from: private */
    public int getStationaryStatus() {
        if (this.mRunningStats.getSampleCount() == 0) {
            return -1;
        }
        this.mPreviousGravityVector = this.mCurrentGravityVector;
        this.mCurrentGravityVector = this.mRunningStats.getRunningAverage();
        if (DEBUG) {
            Log.d(TAG, "mRunningStats = " + this.mRunningStats.toString());
            Vector3 vector3 = this.mCurrentGravityVector;
            String previousGravityVectorString = "null";
            String currentGravityVectorString = vector3 == null ? previousGravityVectorString : vector3.toString();
            Vector3 vector32 = this.mPreviousGravityVector;
            if (vector32 != null) {
                previousGravityVectorString = vector32.toString();
            }
            Log.d(TAG, "mCurrentGravityVector = " + currentGravityVectorString);
            Log.d(TAG, "mPreviousGravityVector = " + previousGravityVectorString);
        }
        Vector3 vector33 = this.mPreviousGravityVector;
        if (vector33 == null || this.mCurrentGravityVector == null) {
            return -1;
        }
        float angle = vector33.normalized().angleBetween(this.mCurrentGravityVector.normalized());
        if (DEBUG) {
            Log.d(TAG, "getStationaryStatus: angle = " + angle + " energy = " + this.mRunningStats.getEnergy());
        }
        if (angle < this.mConfig.getThresholdAngle() && this.mRunningStats.getEnergy() < this.mConfig.getThresholdEnergy()) {
            return 0;
        }
        if (Float.isNaN(angle)) {
            return 1;
        }
        long diffTime = this.mCurrentGravityVector.timeMillisSinceBoot - this.mPreviousGravityVector.timeMillisSinceBoot;
        if (diffTime <= 120000) {
            return 1;
        }
        if (DEBUG) {
            Log.d(TAG, "getStationaryStatus: mPreviousGravityVector is too stale at " + diffTime + " ms ago. Returning RESULT_UNKNOWN.");
        }
        return -1;
    }

    public static final class Vector3 {
        public float incrementalEnergy = 0.0f;
        public long timeMillisSinceBoot;
        public float x;
        public float y;
        public float z;

        public Vector3(long timeMillisSinceBoot2, float x2, float y2, float z2) {
            this.timeMillisSinceBoot = timeMillisSinceBoot2;
            this.x = x2;
            this.y = y2;
            this.z = z2;
        }

        public void setEnergy(float energy) {
            this.incrementalEnergy = energy;
        }

        public float norm() {
            return (float) Math.sqrt((double) dotProduct(this));
        }

        public Vector3 normalized() {
            float mag = norm();
            return new Vector3(this.timeMillisSinceBoot, this.x / mag, this.y / mag, this.z / mag);
        }

        public float angleBetween(Vector3 other) {
            return Math.abs((float) Math.toDegrees(Math.atan2((double) cross(other).norm(), (double) dotProduct(other))));
        }

        public Vector3 cross(Vector3 v) {
            long j = v.timeMillisSinceBoot;
            float f = this.y;
            float f2 = v.z;
            float f3 = this.z;
            float f4 = v.y;
            float f5 = (f * f2) - (f3 * f4);
            float f6 = v.x;
            float f7 = this.x;
            return new Vector3(j, f5, (f3 * f6) - (f2 * f7), (f7 * f4) - (f * f6));
        }

        public String toString() {
            return ((((StringUtils.EMPTY + "timeMillisSinceBoot=" + this.timeMillisSinceBoot) + " | x=" + this.x) + ", y=" + this.y) + ", z=" + this.z) + ", energy=" + this.incrementalEnergy;
        }

        public float dotProduct() {
            float f = this.x;
            float f2 = this.y;
            float f3 = (f * f) + (f2 * f2);
            float f4 = this.z;
            return f3 + (f4 * f4);
        }

        public float dotProduct(Vector3 v) {
            return (this.x * v.x) + (this.y * v.y) + (this.z * v.z);
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

    /* access modifiers changed from: private */
    public static class RunningSignalStats {
        Vector3 currentVector;
        float energy;
        int mMaxSamplesNum;
        ArrayList<Vector3> mMeasureList = new ArrayList<>();
        Vector3 previousVector;
        Vector3 runningSum;
        int sampleCount;

        public RunningSignalStats(int maxNum) {
            this.mMaxSamplesNum = maxNum;
            reset();
        }

        public void reset() {
            this.previousVector = null;
            this.currentVector = null;
            this.runningSum = new Vector3(0, 0.0f, 0.0f, 0.0f);
            this.mMeasureList.clear();
            this.energy = 0.0f;
            this.sampleCount = 0;
        }

        public void accumulate(Vector3 v) {
            if (v != null) {
                this.sampleCount++;
                this.runningSum = this.runningSum.plus(v);
                this.previousVector = this.currentVector;
                this.currentVector = v;
                Vector3 vector3 = this.previousVector;
                if (vector3 != null) {
                    float incrementalEnergy = this.currentVector.minus(vector3).dotProduct();
                    this.currentVector.setEnergy(incrementalEnergy);
                    this.energy += incrementalEnergy;
                    if (this.sampleCount > this.mMaxSamplesNum) {
                        Vector3 fv = this.mMeasureList.get(0);
                        this.runningSum = this.runningSum.minus(fv);
                        this.energy -= fv.incrementalEnergy;
                        this.mMeasureList.remove(0);
                    }
                } else {
                    this.currentVector.setEnergy(0.0f);
                }
                this.mMeasureList.add(this.currentVector);
            } else if (OppoMotionDetector.DEBUG) {
                Log.i(OppoMotionDetector.TAG, "Cannot accumulate a null vector.");
            }
        }

        public Vector3 getRunningAverage() {
            if (this.sampleCount <= 0) {
                return null;
            }
            return this.runningSum.times(1.0f / ((float) this.mMeasureList.size()));
        }

        public float getEnergy() {
            return this.energy;
        }

        public int getSampleCount() {
            return this.sampleCount;
        }

        public String toString() {
            Vector3 vector3 = this.currentVector;
            String previousVectorString = "null";
            String currentVectorString = vector3 == null ? previousVectorString : vector3.toString();
            Vector3 vector32 = this.previousVector;
            if (vector32 != null) {
                previousVectorString = vector32.toString();
            }
            return (((StringUtils.EMPTY + "previousVector = " + previousVectorString) + ", currentVector = " + currentVectorString) + ", sampleCount = " + this.sampleCount) + ", energy = " + this.energy;
        }
    }
}
