package android.hardware;

import android.annotation.UnsupportedAppUsage;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.MemoryFile;
import android.os.MessageQueue;
import android.system.Os;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import dalvik.system.CloseGuard;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemSensorManager extends SensorManager {
    private static final boolean DEBUG_DYNAMIC_SENSOR = true;
    private static final int MAX_LISTENER_COUNT = 128;
    private static final int MIN_DIRECT_CHANNEL_BUFFER_SIZE = 104;
    @GuardedBy({"sLock"})
    private static InjectEventQueue sInjectEventQueue = null;
    private static final Object sLock = new Object();
    @GuardedBy({"sLock"})
    private static boolean sNativeClassInited = false;
    /* access modifiers changed from: private */
    public final Context mContext;
    private BroadcastReceiver mDynamicSensorBroadcastReceiver;
    private HashMap<SensorManager.DynamicSensorCallback, Handler> mDynamicSensorCallbacks = new HashMap<>();
    /* access modifiers changed from: private */
    public boolean mDynamicSensorListDirty = true;
    private List<Sensor> mFullDynamicSensorsList = new ArrayList();
    private final ArrayList<Sensor> mFullSensorsList = new ArrayList<>();
    /* access modifiers changed from: private */
    public final HashMap<Integer, Sensor> mHandleToSensor = new HashMap<>();
    private final Looper mMainLooper;
    /* access modifiers changed from: private */
    public final long mNativeInstance;
    private final HashMap<SensorEventListener, SensorEventQueue> mSensorListeners = new HashMap<>();
    /* access modifiers changed from: private */
    public final int mTargetSdkLevel;
    private final HashMap<TriggerEventListener, TriggerEventQueue> mTriggerListeners = new HashMap<>();

    private static native void nativeClassInit();

    private static native int nativeConfigDirectChannel(long j, int i, int i2, int i3);

    private static native long nativeCreate(String str);

    private static native int nativeCreateDirectChannel(long j, long j2, int i, int i2, HardwareBuffer hardwareBuffer);

    private static native void nativeDestroyDirectChannel(long j, int i);

    private static native void nativeGetDynamicSensors(long j, List<Sensor> list);

    private static native boolean nativeGetSensorAtIndex(long j, Sensor sensor, int i);

    private static native boolean nativeIsDataInjectionEnabled(long j);

    private static native int nativeSetOperationParameter(long j, int i, int i2, float[] fArr, int[] iArr);

    private static native void nativeUpdateSensorConfing(long j);

    public SystemSensorManager(Context context, Looper mainLooper) {
        synchronized (sLock) {
            if (!sNativeClassInited) {
                sNativeClassInited = true;
                nativeClassInit();
            }
        }
        this.mMainLooper = mainLooper;
        this.mTargetSdkLevel = context.getApplicationInfo().targetSdkVersion;
        this.mContext = context;
        this.mNativeInstance = nativeCreate(context.getOpPackageName());
        int index = 0;
        while (true) {
            Sensor sensor = new Sensor();
            if (nativeGetSensorAtIndex(this.mNativeInstance, sensor, index)) {
                this.mFullSensorsList.add(sensor);
                this.mHandleToSensor.put(Integer.valueOf(sensor.getHandle()), sensor);
                index++;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public List<Sensor> getFullSensorList() {
        return this.mFullSensorsList;
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public List<Sensor> getFullDynamicSensorList() {
        setupDynamicSensorBroadcastReceiver();
        updateDynamicSensorList();
        return this.mFullDynamicSensorsList;
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public boolean registerListenerImpl(SensorEventListener listener, Sensor sensor, int delayUs, Handler handler, int maxBatchReportLatencyUs, int reservedFlags) {
        String fullClassName;
        if (listener == null || sensor == null) {
            Log.e("SensorManager", "sensor or listener is null");
            return false;
        }
        ((IColorAntiVirusBehaviorManager) OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0])).setAction_SensorManager_registerListenerImp(Os.getuid(), sensor.getType());
        if (sensor.getReportingMode() == 2) {
            Log.e("SensorManager", "Trigger Sensors should use the requestTriggerSensor.");
            return false;
        } else if (maxBatchReportLatencyUs < 0 || delayUs < 0) {
            Log.e("SensorManager", "maxBatchReportLatencyUs and delayUs should be non-negative");
            return false;
        } else {
            Log.v("SensorManager", "RegisterListener " + sensor.getName() + " type:" + sensor.getType() + " delay:" + delayUs + "us by " + listener.getClass().getName());
            if (this.mSensorListeners.size() < 128) {
                synchronized (this.mSensorListeners) {
                    SensorEventQueue queue = this.mSensorListeners.get(listener);
                    if (queue == null) {
                        Looper looper = handler != null ? handler.getLooper() : this.mMainLooper;
                        if (listener.getClass().getEnclosingClass() != null) {
                            fullClassName = listener.getClass().getEnclosingClass().getName();
                        } else {
                            fullClassName = listener.getClass().getName();
                        }
                        SensorEventQueue queue2 = new SensorEventQueue(listener, looper, this, fullClassName);
                        if (!queue2.addSensor(sensor, delayUs, maxBatchReportLatencyUs)) {
                            queue2.dispose();
                            return false;
                        }
                        this.mSensorListeners.put(listener, queue2);
                        return true;
                    }
                    boolean addSensor = queue.addSensor(sensor, delayUs, maxBatchReportLatencyUs);
                    return addSensor;
                }
            }
            throw new IllegalStateException("register failed, the sensor listeners size has exceeded the maximum limit 128");
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public void unregisterListenerImpl(SensorEventListener listener, Sensor sensor) {
        boolean result;
        if (listener != null) {
            Log.v("SensorManager", "unRegisterListener by " + listener.getClass().getName());
        }
        if (sensor == null || sensor.getReportingMode() != 2) {
            synchronized (this.mSensorListeners) {
                SensorEventQueue queue = this.mSensorListeners.get(listener);
                if (queue != null) {
                    if (sensor == null) {
                        result = queue.removeAllSensors();
                    } else {
                        result = queue.removeSensor(sensor, true);
                    }
                    if (result && !queue.hasSensors()) {
                        this.mSensorListeners.remove(listener);
                        queue.dispose();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public boolean requestTriggerSensorImpl(TriggerEventListener listener, Sensor sensor) {
        String fullClassName;
        if (sensor == null) {
            throw new IllegalArgumentException("sensor cannot be null");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else if (sensor.getReportingMode() != 2) {
            return false;
        } else {
            if (this.mTriggerListeners.size() < 128) {
                synchronized (this.mTriggerListeners) {
                    TriggerEventQueue queue = this.mTriggerListeners.get(listener);
                    if (queue == null) {
                        if (listener.getClass().getEnclosingClass() != null) {
                            fullClassName = listener.getClass().getEnclosingClass().getName();
                        } else {
                            fullClassName = listener.getClass().getName();
                        }
                        TriggerEventQueue queue2 = new TriggerEventQueue(listener, this.mMainLooper, this, fullClassName);
                        if (!queue2.addSensor(sensor, 0, 0)) {
                            queue2.dispose();
                            return false;
                        }
                        this.mTriggerListeners.put(listener, queue2);
                        return true;
                    }
                    boolean addSensor = queue.addSensor(sensor, 0, 0);
                    return addSensor;
                }
            }
            throw new IllegalStateException("request failed, the trigger listeners size has exceeded the maximum limit 128");
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0034, code lost:
        return r0;
     */
    @Override // android.hardware.SensorManager
    public boolean cancelTriggerSensorImpl(TriggerEventListener listener, Sensor sensor, boolean disable) {
        boolean result;
        if (sensor != null && sensor.getReportingMode() != 2) {
            return false;
        }
        synchronized (this.mTriggerListeners) {
            TriggerEventQueue queue = this.mTriggerListeners.get(listener);
            if (queue == null) {
                return false;
            }
            if (sensor == null) {
                result = queue.removeAllSensors();
            } else {
                result = queue.removeSensor(sensor, disable);
            }
            if (result && !queue.hasSensors()) {
                this.mTriggerListeners.remove(listener);
                queue.dispose();
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001a, code lost:
        return r2;
     */
    @Override // android.hardware.SensorManager
    public boolean flushImpl(SensorEventListener listener) {
        if (listener != null) {
            synchronized (this.mSensorListeners) {
                SensorEventQueue queue = this.mSensorListeners.get(listener);
                boolean z = false;
                if (queue == null) {
                    return false;
                }
                if (queue.flush() == 0) {
                    z = true;
                }
            }
        } else {
            throw new IllegalArgumentException("listener cannot be null");
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004a, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0058, code lost:
        return true;
     */
    @Override // android.hardware.SensorManager
    public boolean initDataInjectionImpl(boolean enable) {
        synchronized (sLock) {
            boolean z = true;
            if (enable) {
                if (!nativeIsDataInjectionEnabled(this.mNativeInstance)) {
                    Log.e("SensorManager", "Data Injection mode not enabled");
                    return false;
                }
                if (sInjectEventQueue == null) {
                    try {
                        sInjectEventQueue = new InjectEventQueue(this.mMainLooper, this, this.mContext.getPackageName());
                    } catch (RuntimeException e) {
                        Log.e("SensorManager", "Cannot create InjectEventQueue: " + e);
                    }
                }
                if (sInjectEventQueue == null) {
                    z = false;
                }
            } else if (sInjectEventQueue != null) {
                sInjectEventQueue.dispose();
                sInjectEventQueue = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
        return r2;
     */
    @Override // android.hardware.SensorManager
    public boolean injectSensorDataImpl(Sensor sensor, float[] values, int accuracy, long timestamp) {
        synchronized (sLock) {
            boolean z = false;
            if (sInjectEventQueue == null) {
                Log.e("SensorManager", "Data injection mode not activated before calling injectSensorData");
                return false;
            }
            int ret = sInjectEventQueue.injectSensorData(sensor.getHandle(), values, accuracy, timestamp);
            if (ret != 0) {
                sInjectEventQueue.dispose();
                sInjectEventQueue = null;
            }
            if (ret == 0) {
                z = true;
            }
        }
    }

    private void cleanupSensorConnection(Sensor sensor) {
        this.mHandleToSensor.remove(Integer.valueOf(sensor.getHandle()));
        if (sensor.getReportingMode() == 2) {
            synchronized (this.mTriggerListeners) {
                for (TriggerEventListener l : new HashMap<>(this.mTriggerListeners).keySet()) {
                    Log.i("SensorManager", "removed trigger listener" + l.toString() + " due to sensor disconnection");
                    cancelTriggerSensorImpl(l, sensor, true);
                }
            }
            return;
        }
        synchronized (this.mSensorListeners) {
            for (SensorEventListener l2 : new HashMap<>(this.mSensorListeners).keySet()) {
                Log.i("SensorManager", "removed event listener" + l2.toString() + " due to sensor disconnection");
                unregisterListenerImpl(l2, sensor);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateDynamicSensorList() {
        synchronized (this.mFullDynamicSensorsList) {
            if (this.mDynamicSensorListDirty) {
                List<Sensor> list = new ArrayList<>();
                nativeGetDynamicSensors(this.mNativeInstance, list);
                List<Sensor> updatedList = new ArrayList<>();
                final List<Sensor> addedList = new ArrayList<>();
                final List<Sensor> removedList = new ArrayList<>();
                if (diffSortedSensorList(this.mFullDynamicSensorsList, list, updatedList, addedList, removedList)) {
                    Log.i("SensorManager", "DYNS dynamic sensor list cached should be updated");
                    this.mFullDynamicSensorsList = updatedList;
                    for (Sensor s : addedList) {
                        this.mHandleToSensor.put(Integer.valueOf(s.getHandle()), s);
                    }
                    Handler mainHandler = new Handler(this.mContext.getMainLooper());
                    for (Map.Entry<SensorManager.DynamicSensorCallback, Handler> entry : this.mDynamicSensorCallbacks.entrySet()) {
                        final SensorManager.DynamicSensorCallback callback = entry.getKey();
                        (entry.getValue() == null ? mainHandler : entry.getValue()).post(new Runnable() {
                            /* class android.hardware.SystemSensorManager.AnonymousClass1 */

                            public void run() {
                                for (Sensor s : addedList) {
                                    callback.onDynamicSensorConnected(s);
                                }
                                for (Sensor s2 : removedList) {
                                    callback.onDynamicSensorDisconnected(s2);
                                }
                            }
                        });
                    }
                    for (Sensor s2 : removedList) {
                        cleanupSensorConnection(s2);
                    }
                }
                this.mDynamicSensorListDirty = false;
            }
        }
    }

    private void setupDynamicSensorBroadcastReceiver() {
        if (this.mDynamicSensorBroadcastReceiver == null) {
            this.mDynamicSensorBroadcastReceiver = new BroadcastReceiver() {
                /* class android.hardware.SystemSensorManager.AnonymousClass2 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction() == Intent.ACTION_DYNAMIC_SENSOR_CHANGED) {
                        Log.i("SensorManager", "DYNS received DYNAMIC_SENSOR_CHANED broadcast");
                        boolean unused = SystemSensorManager.this.mDynamicSensorListDirty = true;
                        SystemSensorManager.this.updateDynamicSensorList();
                    }
                }
            };
            IntentFilter filter = new IntentFilter("dynamic_sensor_change");
            filter.addAction(Intent.ACTION_DYNAMIC_SENSOR_CHANGED);
            this.mContext.registerReceiver(this.mDynamicSensorBroadcastReceiver, filter);
        }
    }

    private void teardownDynamicSensorBroadcastReceiver() {
        this.mDynamicSensorCallbacks.clear();
        this.mContext.unregisterReceiver(this.mDynamicSensorBroadcastReceiver);
        this.mDynamicSensorBroadcastReceiver = null;
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public void registerDynamicSensorCallbackImpl(SensorManager.DynamicSensorCallback callback, Handler handler) {
        Log.i("SensorManager", "DYNS Register dynamic sensor callback");
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        } else if (!this.mDynamicSensorCallbacks.containsKey(callback)) {
            setupDynamicSensorBroadcastReceiver();
            this.mDynamicSensorCallbacks.put(callback, handler);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public void unregisterDynamicSensorCallbackImpl(SensorManager.DynamicSensorCallback callback) {
        Log.i("SensorManager", "Removing dynamic sensor listerner");
        this.mDynamicSensorCallbacks.remove(callback);
    }

    private static boolean diffSortedSensorList(List<Sensor> oldList, List<Sensor> newList, List<Sensor> updated, List<Sensor> added, List<Sensor> removed) {
        boolean changed = false;
        int i = 0;
        int j = 0;
        while (true) {
            if (j < oldList.size() && (i >= newList.size() || newList.get(i).getHandle() > oldList.get(j).getHandle())) {
                changed = true;
                if (removed != null) {
                    removed.add(oldList.get(j));
                }
                j++;
            } else if (i < newList.size() && (j >= oldList.size() || newList.get(i).getHandle() < oldList.get(j).getHandle())) {
                changed = true;
                if (added != null) {
                    added.add(newList.get(i));
                }
                if (updated != null) {
                    updated.add(newList.get(i));
                }
                i++;
            } else if (i >= newList.size() || j >= oldList.size() || newList.get(i).getHandle() != oldList.get(j).getHandle()) {
                return changed;
            } else {
                if (updated != null) {
                    updated.add(oldList.get(j));
                }
                i++;
                j++;
            }
        }
        return changed;
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public int configureDirectChannelImpl(SensorDirectChannel channel, Sensor sensor, int rate) {
        if (!channel.isOpen()) {
            throw new IllegalStateException("channel is closed");
        } else if (rate < 0 || rate > 3) {
            throw new IllegalArgumentException("rate parameter invalid");
        } else if (sensor != null || rate == 0) {
            int ret = nativeConfigDirectChannel(this.mNativeInstance, channel.getNativeHandle(), sensor == null ? -1 : sensor.getHandle(), rate);
            if (rate == 0) {
                if (ret == 0) {
                    return 1;
                }
                return 0;
            } else if (ret > 0) {
                return ret;
            } else {
                return 0;
            }
        } else {
            throw new IllegalArgumentException("when sensor is null, rate can only be DIRECT_RATE_STOP");
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public SensorDirectChannel createDirectChannelImpl(MemoryFile memoryFile, HardwareBuffer hardwareBuffer) {
        long size;
        int type;
        int id;
        if (memoryFile != null) {
            try {
                int fd = memoryFile.getFileDescriptor().getInt$();
                if (memoryFile.length() >= 104) {
                    long size2 = (long) memoryFile.length();
                    int id2 = nativeCreateDirectChannel(this.mNativeInstance, size2, 1, fd, null);
                    if (id2 > 0) {
                        size = size2;
                        id = id2;
                        type = 1;
                    } else {
                        throw new UncheckedIOException(new IOException("create MemoryFile direct channel failed " + id2));
                    }
                } else {
                    throw new IllegalArgumentException("Size of MemoryFile has to be greater than 104");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("MemoryFile object is not valid");
            }
        } else if (hardwareBuffer == null) {
            throw new NullPointerException("shared memory object cannot be null");
        } else if (hardwareBuffer.getFormat() != 33) {
            throw new IllegalArgumentException("Format of HardwareBuffer must be BLOB");
        } else if (hardwareBuffer.getHeight() != 1) {
            throw new IllegalArgumentException("Height of HardwareBuffer must be 1");
        } else if (hardwareBuffer.getWidth() < 104) {
            throw new IllegalArgumentException("Width if HaradwareBuffer must be greater than 104");
        } else if ((hardwareBuffer.getUsage() & 8388608) != 0) {
            long size3 = (long) hardwareBuffer.getWidth();
            int id3 = nativeCreateDirectChannel(this.mNativeInstance, size3, 2, -1, hardwareBuffer);
            if (id3 > 0) {
                size = size3;
                id = id3;
                type = 2;
            } else {
                throw new UncheckedIOException(new IOException("create HardwareBuffer direct channel failed " + id3));
            }
        } else {
            throw new IllegalArgumentException("HardwareBuffer must set usage flag USAGE_SENSOR_DIRECT_DATA");
        }
        return new SensorDirectChannel(this, id, type, size);
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public void destroyDirectChannelImpl(SensorDirectChannel channel) {
        if (channel != null) {
            nativeDestroyDirectChannel(this.mNativeInstance, channel.getNativeHandle());
        }
    }

    private static abstract class BaseEventQueue {
        protected static final int OPERATING_MODE_DATA_INJECTION = 1;
        protected static final int OPERATING_MODE_NORMAL = 0;
        private final SparseBooleanArray mActiveSensors = new SparseBooleanArray();
        private final CloseGuard mCloseGuard = CloseGuard.get();
        protected final SystemSensorManager mManager;
        private long mNativeSensorEventQueue;
        protected final SparseIntArray mSensorAccuracies = new SparseIntArray();

        private static native void nativeDestroySensorEventQueue(long j);

        private static native int nativeDisableSensor(long j, int i);

        private static native int nativeEnableSensor(long j, int i, int i2, int i3);

        private static native int nativeFlushSensor(long j);

        private static native long nativeInitBaseEventQueue(long j, WeakReference<BaseEventQueue> weakReference, MessageQueue messageQueue, String str, int i, String str2);

        private static native int nativeInjectSensorData(long j, int i, float[] fArr, int i2, long j2);

        /* access modifiers changed from: protected */
        public abstract void addSensorEvent(Sensor sensor);

        /* access modifiers changed from: protected */
        @UnsupportedAppUsage
        public abstract void dispatchFlushCompleteEvent(int i);

        /* access modifiers changed from: protected */
        @UnsupportedAppUsage
        public abstract void dispatchSensorEvent(int i, float[] fArr, int i2, long j);

        /* access modifiers changed from: protected */
        public abstract void removeSensorEvent(Sensor sensor);

        BaseEventQueue(Looper looper, SystemSensorManager manager, int mode, String packageName) {
            this.mNativeSensorEventQueue = nativeInitBaseEventQueue(manager.mNativeInstance, new WeakReference(this), looper.getQueue(), packageName == null ? "" : packageName, mode, manager.mContext.getOpPackageName());
            this.mCloseGuard.open("dispose");
            this.mManager = manager;
        }

        public void dispose() {
            dispose(false);
        }

        public boolean addSensor(Sensor sensor, int delayUs, int maxBatchReportLatencyUs) {
            int handle = sensor.getHandle();
            if (sensor.getType() == 2 || sensor.getType() == 3) {
                this.mSensorAccuracies.put(sensor.getHandle(), -1);
            }
            if (this.mActiveSensors.get(handle)) {
                return false;
            }
            this.mActiveSensors.put(handle, true);
            addSensorEvent(sensor);
            if (enableSensor(sensor, delayUs, maxBatchReportLatencyUs) == 0 || (maxBatchReportLatencyUs != 0 && (maxBatchReportLatencyUs <= 0 || enableSensor(sensor, delayUs, 0) == 0))) {
                return true;
            }
            removeSensor(sensor, false);
            return false;
        }

        public boolean removeAllSensors() {
            for (int i = 0; i < this.mActiveSensors.size(); i++) {
                if (this.mActiveSensors.valueAt(i)) {
                    int handle = this.mActiveSensors.keyAt(i);
                    Sensor sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle));
                    if (sensor != null) {
                        disableSensor(sensor);
                        this.mActiveSensors.put(handle, false);
                        removeSensorEvent(sensor);
                    }
                }
            }
            return true;
        }

        public boolean removeSensor(Sensor sensor, boolean disable) {
            if (!this.mActiveSensors.get(sensor.getHandle())) {
                return false;
            }
            if (disable) {
                disableSensor(sensor);
            }
            this.mActiveSensors.put(sensor.getHandle(), false);
            removeSensorEvent(sensor);
            return true;
        }

        public int flush() {
            long j = this.mNativeSensorEventQueue;
            if (j != 0) {
                return nativeFlushSensor(j);
            }
            throw new NullPointerException();
        }

        public boolean hasSensors() {
            return this.mActiveSensors.indexOfValue(true) >= 0;
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            try {
                dispose(true);
            } finally {
                super.finalize();
            }
        }

        private void dispose(boolean finalized) {
            CloseGuard closeGuard = this.mCloseGuard;
            if (closeGuard != null) {
                if (finalized) {
                    closeGuard.warnIfOpen();
                }
                this.mCloseGuard.close();
            }
            long j = this.mNativeSensorEventQueue;
            if (j != 0) {
                nativeDestroySensorEventQueue(j);
                this.mNativeSensorEventQueue = 0;
            }
        }

        private int enableSensor(Sensor sensor, int rateUs, int maxBatchReportLatencyUs) {
            long j = this.mNativeSensorEventQueue;
            if (j == 0) {
                throw new NullPointerException();
            } else if (sensor != null) {
                return nativeEnableSensor(j, sensor.getHandle(), rateUs, maxBatchReportLatencyUs);
            } else {
                throw new NullPointerException();
            }
        }

        /* access modifiers changed from: protected */
        public int injectSensorDataBase(int handle, float[] values, int accuracy, long timestamp) {
            return nativeInjectSensorData(this.mNativeSensorEventQueue, handle, values, accuracy, timestamp);
        }

        private int disableSensor(Sensor sensor) {
            long j = this.mNativeSensorEventQueue;
            if (j == 0) {
                throw new NullPointerException();
            } else if (sensor != null) {
                return nativeDisableSensor(j, sensor.getHandle());
            } else {
                throw new NullPointerException();
            }
        }

        /* access modifiers changed from: protected */
        @UnsupportedAppUsage
        public void dispatchAdditionalInfoEvent(int handle, int type, int serial, float[] floatValues, int[] intValues) {
        }
    }

    static final class SensorEventQueue extends BaseEventQueue {
        int MAX_STEP_INTERVAL = 100;
        float mLastStep = 0.0f;
        private final SensorEventListener mListener;
        private final SparseArray<SensorEvent> mSensorsEvents = new SparseArray<>();

        public SensorEventQueue(SensorEventListener listener, Looper looper, SystemSensorManager manager, String packageName) {
            super(looper, manager, 0, packageName);
            this.mListener = listener;
        }

        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void addSensorEvent(Sensor sensor) {
            SensorEvent t = new SensorEvent(Sensor.getMaxLengthValuesArray(sensor, this.mManager.mTargetSdkLevel));
            synchronized (this.mSensorsEvents) {
                this.mSensorsEvents.put(sensor.getHandle(), t);
            }
        }

        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void removeSensorEvent(Sensor sensor) {
            synchronized (this.mSensorsEvents) {
                this.mSensorsEvents.delete(sensor.getHandle());
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void dispatchSensorEvent(int handle, float[] values, int inAccuracy, long timestamp) {
            SensorEvent t;
            Sensor sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle));
            if (sensor != null) {
                synchronized (this.mSensorsEvents) {
                    t = this.mSensorsEvents.get(handle);
                }
                if (t != null) {
                    System.arraycopy(values, 0, t.values, 0, t.values.length);
                    t.timestamp = timestamp;
                    t.accuracy = inAccuracy;
                    t.sensor = sensor;
                    int accuracy = this.mSensorAccuracies.get(handle);
                    if (t.accuracy >= 0 && accuracy != t.accuracy) {
                        this.mSensorAccuracies.put(handle, t.accuracy);
                        this.mListener.onAccuracyChanged(t.sensor, t.accuracy);
                    }
                    try {
                        if (sensor.getType() == 19 && (this.mLastStep == 0.0f || Math.abs(values[0] - this.mLastStep) >= ((float) this.MAX_STEP_INTERVAL))) {
                            Log.v("SensorManager", "step counter dispatchSensorEvent step " + values[0] + " to + " + this.mListener);
                            this.mLastStep = values[0];
                        }
                    } catch (Exception e) {
                        Log.e("SensorManager", "step counter error e = " + e.toString());
                    }
                    this.mListener.onSensorChanged(t);
                }
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void dispatchFlushCompleteEvent(int handle) {
            Sensor sensor;
            if ((this.mListener instanceof SensorEventListener2) && (sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle))) != null) {
                ((SensorEventListener2) this.mListener).onFlushCompleted(sensor);
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void dispatchAdditionalInfoEvent(int handle, int type, int serial, float[] floatValues, int[] intValues) {
            Sensor sensor;
            if ((this.mListener instanceof SensorEventCallback) && (sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle))) != null) {
                ((SensorEventCallback) this.mListener).onSensorAdditionalInfo(new SensorAdditionalInfo(sensor, type, serial, intValues, floatValues));
            }
        }
    }

    static final class TriggerEventQueue extends BaseEventQueue {
        private final TriggerEventListener mListener;
        private final SparseArray<TriggerEvent> mTriggerEvents = new SparseArray<>();

        public TriggerEventQueue(TriggerEventListener listener, Looper looper, SystemSensorManager manager, String packageName) {
            super(looper, manager, 0, packageName);
            this.mListener = listener;
        }

        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void addSensorEvent(Sensor sensor) {
            TriggerEvent t = new TriggerEvent(Sensor.getMaxLengthValuesArray(sensor, this.mManager.mTargetSdkLevel));
            synchronized (this.mTriggerEvents) {
                this.mTriggerEvents.put(sensor.getHandle(), t);
            }
        }

        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void removeSensorEvent(Sensor sensor) {
            synchronized (this.mTriggerEvents) {
                this.mTriggerEvents.delete(sensor.getHandle());
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void dispatchSensorEvent(int handle, float[] values, int accuracy, long timestamp) {
            TriggerEvent t;
            Sensor sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle));
            if (sensor != null) {
                synchronized (this.mTriggerEvents) {
                    t = this.mTriggerEvents.get(handle);
                }
                if (t == null) {
                    Log.e("SensorManager", "Error: Trigger Event is null for Sensor: " + sensor);
                    return;
                }
                System.arraycopy(values, 0, t.values, 0, t.values.length);
                t.timestamp = timestamp;
                t.sensor = sensor;
                this.mManager.cancelTriggerSensorImpl(this.mListener, sensor, false);
                this.mListener.onTrigger(t);
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void dispatchFlushCompleteEvent(int handle) {
        }
    }

    final class InjectEventQueue extends BaseEventQueue {
        public InjectEventQueue(Looper looper, SystemSensorManager manager, String packageName) {
            super(looper, manager, 1, packageName);
        }

        /* access modifiers changed from: package-private */
        public int injectSensorData(int handle, float[] values, int accuracy, long timestamp) {
            return injectSensorDataBase(handle, values, accuracy, timestamp);
        }

        /* access modifiers changed from: protected */
        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void dispatchSensorEvent(int handle, float[] values, int accuracy, long timestamp) {
        }

        /* access modifiers changed from: protected */
        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void dispatchFlushCompleteEvent(int handle) {
        }

        /* access modifiers changed from: protected */
        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void addSensorEvent(Sensor sensor) {
        }

        /* access modifiers changed from: protected */
        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void removeSensorEvent(Sensor sensor) {
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public boolean setOperationParameterImpl(SensorAdditionalInfo parameter) {
        int handle = -1;
        if (parameter.sensor != null) {
            handle = parameter.sensor.getHandle();
        }
        return nativeSetOperationParameter(this.mNativeInstance, handle, parameter.type, parameter.floatValues, parameter.intValues) == 0;
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public void updateSensorConfingImpl() {
        Log.v("SensorManager", "updateSensorConfingImpl");
        nativeUpdateSensorConfing(this.mNativeInstance);
    }
}
