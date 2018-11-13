package com.android.server.location;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.display.OppoBrightUtils;
import com.android.server.job.controllers.JobStatus;
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
    private DeviceIdleCallback mCallback = null;
    private OppoMotionConfig mConfig = null;
    private Vector3 mCurrentGravityVector = null;
    private final Handler mHandler;
    private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            synchronized (OppoMotionDetector.this.mLock) {
                OppoMotionDetector.this.mRunningStats.accumulate(new Vector3(SystemClock.elapsedRealtime(), event.values[0], event.values[1], event.values[2]));
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private final Object mLock = new Object();
    private boolean mMeasurementInProgress;
    private final Runnable mMeasurementUpdate = new Runnable() {
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
                OppoMotionDetector.this.mHandler.sendMessageDelayed(Message.obtain(OppoMotionDetector.this.mHandler, OppoMotionDetector.this.mMeasurementUpdate), OppoMotionDetector.this.mMinResultTimer);
            }
        }
    };
    private final long mMinResultTimer;
    private Vector3 mPreviousGravityVector = null;
    private RunningSignalStats mRunningStats;
    private SensorManager mSensorManager;
    private int mState;

    public interface DeviceIdleCallback {
        void onAnyMotionResult(int i);
    }

    private static class RunningSignalStats {
        Vector3 currentVector;
        float energy;
        int mMaxSamplesNum;
        ArrayList<Vector3> mMeasureList = new ArrayList();
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
            this.runningSum = new Vector3(0, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
            this.mMeasureList.clear();
            this.energy = OppoBrightUtils.MIN_LUX_LIMITI;
            this.sampleCount = 0;
        }

        public void accumulate(Vector3 v) {
            if (v == null) {
                if (OppoMotionDetector.DEBUG) {
                    Log.i(OppoMotionDetector.TAG, "Cannot accumulate a null vector.");
                }
                return;
            }
            this.sampleCount++;
            this.runningSum = this.runningSum.plus(v);
            this.previousVector = this.currentVector;
            this.currentVector = v;
            if (this.previousVector != null) {
                float incrementalEnergy = this.currentVector.minus(this.previousVector).dotProduct();
                this.currentVector.setEnergy(incrementalEnergy);
                this.energy += incrementalEnergy;
                if (this.sampleCount > this.mMaxSamplesNum) {
                    Vector3 fv = (Vector3) this.mMeasureList.get(0);
                    this.runningSum = this.runningSum.minus(fv);
                    this.energy -= fv.incrementalEnergy;
                    this.mMeasureList.remove(0);
                }
            } else {
                this.currentVector.setEnergy(OppoBrightUtils.MIN_LUX_LIMITI);
            }
            this.mMeasureList.add(this.currentVector);
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
            return ((("" + "previousVector = " + (this.previousVector == null ? "null" : this.previousVector.toString())) + ", currentVector = " + (this.currentVector == null ? "null" : this.currentVector.toString())) + ", sampleCount = " + this.sampleCount) + ", energy = " + this.energy;
        }
    }

    public static final class Vector3 {
        public float incrementalEnergy = OppoBrightUtils.MIN_LUX_LIMITI;
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
            return new Vector3(v.timeMillisSinceBoot, (this.y * v.z) - (this.z * v.y), (this.z * v.x) - (this.x * v.z), (this.x * v.y) - (this.y * v.x));
        }

        public String toString() {
            return (((("" + "timeMillisSinceBoot=" + this.timeMillisSinceBoot) + " | x=" + this.x) + ", y=" + this.y) + ", z=" + this.z) + ", energy=" + this.incrementalEnergy;
        }

        public float dotProduct() {
            return ((this.x * this.x) + (this.y * this.y)) + (this.z * this.z);
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

    public void setDebug(boolean debug) {
        DEBUG = debug;
    }

    private void startOrientationMeasurementLocked() {
        if (DEBUG) {
            Log.d(TAG, "startOrientationMeasurementLocked: mMeasurementInProgress=" + this.mMeasurementInProgress + ", (mAccelSensor != null)=" + (this.mAccelSensor != null));
        }
        if (!this.mMeasurementInProgress && this.mAccelSensor != null) {
            if (this.mSensorManager.registerListener(this.mListener, this.mAccelSensor, this.mConfig.getInterval() * 1000)) {
                this.mMeasurementInProgress = true;
                this.mRunningStats.reset();
            }
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, this.mMeasurementUpdate), ACCELEROMETER_DATA_TIMEOUT_MILLIS);
        }
    }

    private void stopOrientationMeasurementLocked() {
        if (DEBUG) {
            Log.d(TAG, "stopOrientationMeasurement. mMeasurementInProgress=" + this.mMeasurementInProgress);
        }
        if (this.mMeasurementInProgress) {
            this.mHandler.removeCallbacks(this.mMeasurementUpdate);
            this.mSensorManager.unregisterListener(this.mListener);
            this.mMeasurementInProgress = false;
            this.mRunningStats.reset();
        }
    }

    private int getStationaryStatus() {
        if (this.mRunningStats.getSampleCount() == 0) {
            return -1;
        }
        this.mPreviousGravityVector = this.mCurrentGravityVector;
        this.mCurrentGravityVector = this.mRunningStats.getRunningAverage();
        if (DEBUG) {
            Log.d(TAG, "mRunningStats = " + this.mRunningStats.toString());
            String currentGravityVectorString = this.mCurrentGravityVector == null ? "null" : this.mCurrentGravityVector.toString();
            String previousGravityVectorString = this.mPreviousGravityVector == null ? "null" : this.mPreviousGravityVector.toString();
            Log.d(TAG, "mCurrentGravityVector = " + currentGravityVectorString);
            Log.d(TAG, "mPreviousGravityVector = " + previousGravityVectorString);
        }
        if (this.mPreviousGravityVector == null || this.mCurrentGravityVector == null) {
            return -1;
        }
        float angle = this.mPreviousGravityVector.normalized().angleBetween(this.mCurrentGravityVector.normalized());
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
        if (diffTime <= JobStatus.DEFAULT_TRIGGER_MAX_DELAY) {
            return 1;
        }
        if (DEBUG) {
            Log.d(TAG, "getStationaryStatus: mPreviousGravityVector is too stale at " + diffTime + " ms ago. Returning RESULT_UNKNOWN.");
        }
        return -1;
    }
}
