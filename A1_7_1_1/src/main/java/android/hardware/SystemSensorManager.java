package android.hardware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager.DynamicSensorCallback;
import android.hardware.camera2.params.TonemapCurve;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SystemSensorManager extends SensorManager {
    private static boolean DEBUG_DYNAMIC_SENSOR;
    @GuardedBy("sLock")
    private static InjectEventQueue sInjectEventQueue;
    private static final Object sLock = null;
    @GuardedBy("sLock")
    private static boolean sNativeClassInited;
    private final Context mContext;
    private BroadcastReceiver mDynamicSensorBroadcastReceiver;
    private HashMap<DynamicSensorCallback, Handler> mDynamicSensorCallbacks;
    private boolean mDynamicSensorListDirty;
    private List<Sensor> mFullDynamicSensorsList;
    private final ArrayList<Sensor> mFullSensorsList;
    private final HashMap<Integer, Sensor> mHandleToSensor;
    private final Looper mMainLooper;
    private final long mNativeInstance;
    private final HashMap<SensorEventListener, SensorEventQueue> mSensorListeners;
    private final int mTargetSdkLevel;
    private final HashMap<TriggerEventListener, TriggerEventQueue> mTriggerListeners;

    /* renamed from: android.hardware.SystemSensorManager$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ SystemSensorManager this$0;
        final /* synthetic */ List val$addedList;
        final /* synthetic */ DynamicSensorCallback val$callback;
        final /* synthetic */ List val$removedList;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.SystemSensorManager.1.<init>(android.hardware.SystemSensorManager, java.util.List, android.hardware.SensorManager$DynamicSensorCallback, java.util.List):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass1(android.hardware.SystemSensorManager r1, java.util.List r2, android.hardware.SensorManager.DynamicSensorCallback r3, java.util.List r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.SystemSensorManager.1.<init>(android.hardware.SystemSensorManager, java.util.List, android.hardware.SensorManager$DynamicSensorCallback, java.util.List):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.SystemSensorManager.1.<init>(android.hardware.SystemSensorManager, java.util.List, android.hardware.SensorManager$DynamicSensorCallback, java.util.List):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.SystemSensorManager.1.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.SystemSensorManager.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.SystemSensorManager.1.run():void");
        }
    }

    /* renamed from: android.hardware.SystemSensorManager$2 */
    class AnonymousClass2 extends BroadcastReceiver {
        final /* synthetic */ SystemSensorManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.SystemSensorManager.2.<init>(android.hardware.SystemSensorManager):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass2(android.hardware.SystemSensorManager r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.SystemSensorManager.2.<init>(android.hardware.SystemSensorManager):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.SystemSensorManager.2.<init>(android.hardware.SystemSensorManager):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.hardware.SystemSensorManager.2.onReceive(android.content.Context, android.content.Intent):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.hardware.SystemSensorManager.2.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.SystemSensorManager.2.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    private static abstract class BaseEventQueue {
        protected static final int OPERATING_MODE_DATA_INJECTION = 1;
        protected static final int OPERATING_MODE_NORMAL = 0;
        private final SparseBooleanArray mActiveSensors;
        private final CloseGuard mCloseGuard;
        protected final SystemSensorManager mManager;
        protected final SparseIntArray mSensorAccuracies;
        private long nSensorEventQueue;

        private static native void nativeDestroySensorEventQueue(long j);

        private static native int nativeDisableSensor(long j, int i);

        private static native int nativeEnableSensor(long j, int i, int i2, int i3);

        private static native int nativeFlushSensor(long j);

        private static native long nativeInitBaseEventQueue(long j, WeakReference<BaseEventQueue> weakReference, MessageQueue messageQueue, String str, int i, String str2);

        private static native int nativeInjectSensorData(long j, int i, float[] fArr, int i2, long j2);

        protected abstract void addSensorEvent(Sensor sensor);

        protected abstract void dispatchFlushCompleteEvent(int i);

        protected abstract void dispatchSensorEvent(int i, float[] fArr, int i2, long j);

        protected abstract void removeSensorEvent(Sensor sensor);

        BaseEventQueue(Looper looper, SystemSensorManager manager, int mode, String packageName) {
            this.mActiveSensors = new SparseBooleanArray();
            this.mSensorAccuracies = new SparseIntArray();
            this.mCloseGuard = CloseGuard.get();
            if (packageName == null) {
                packageName = "";
            }
            this.nSensorEventQueue = nativeInitBaseEventQueue(manager.mNativeInstance, new WeakReference(this), looper.getQueue(), packageName, mode, manager.mContext.getOpPackageName());
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
            if (this.nSensorEventQueue != 0) {
                return nativeFlushSensor(this.nSensorEventQueue);
            }
            throw new NullPointerException();
        }

        public boolean hasSensors() {
            return this.mActiveSensors.indexOfValue(true) >= 0;
        }

        protected void finalize() throws Throwable {
            try {
                dispose(true);
            } finally {
                super.finalize();
            }
        }

        private void dispose(boolean finalized) {
            if (this.mCloseGuard != null) {
                if (finalized) {
                    this.mCloseGuard.warnIfOpen();
                }
                this.mCloseGuard.close();
            }
            if (this.nSensorEventQueue != 0) {
                nativeDestroySensorEventQueue(this.nSensorEventQueue);
                this.nSensorEventQueue = 0;
            }
        }

        private int enableSensor(Sensor sensor, int rateUs, int maxBatchReportLatencyUs) {
            if (this.nSensorEventQueue == 0) {
                throw new NullPointerException();
            } else if (sensor != null) {
                return nativeEnableSensor(this.nSensorEventQueue, sensor.getHandle(), rateUs, maxBatchReportLatencyUs);
            } else {
                throw new NullPointerException();
            }
        }

        protected int injectSensorDataBase(int handle, float[] values, int accuracy, long timestamp) {
            return nativeInjectSensorData(this.nSensorEventQueue, handle, values, accuracy, timestamp);
        }

        private int disableSensor(Sensor sensor) {
            if (this.nSensorEventQueue == 0) {
                throw new NullPointerException();
            } else if (sensor != null) {
                return nativeDisableSensor(this.nSensorEventQueue, sensor.getHandle());
            } else {
                throw new NullPointerException();
            }
        }

        protected void dispatchAdditionalInfoEvent(int handle, int type, int serial, float[] floatValues, int[] intValues) {
        }
    }

    final class InjectEventQueue extends BaseEventQueue {
        final /* synthetic */ SystemSensorManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.SystemSensorManager.InjectEventQueue.<init>(android.hardware.SystemSensorManager, android.os.Looper, android.hardware.SystemSensorManager, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public InjectEventQueue(android.hardware.SystemSensorManager r1, android.os.Looper r2, android.hardware.SystemSensorManager r3, java.lang.String r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.SystemSensorManager.InjectEventQueue.<init>(android.hardware.SystemSensorManager, android.os.Looper, android.hardware.SystemSensorManager, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.SystemSensorManager.InjectEventQueue.<init>(android.hardware.SystemSensorManager, android.os.Looper, android.hardware.SystemSensorManager, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.SystemSensorManager.InjectEventQueue.addSensorEvent(android.hardware.Sensor):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected void addSensorEvent(android.hardware.Sensor r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.SystemSensorManager.InjectEventQueue.addSensorEvent(android.hardware.Sensor):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.SystemSensorManager.InjectEventQueue.addSensorEvent(android.hardware.Sensor):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.SystemSensorManager.InjectEventQueue.dispatchFlushCompleteEvent(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected void dispatchFlushCompleteEvent(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.SystemSensorManager.InjectEventQueue.dispatchFlushCompleteEvent(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.SystemSensorManager.InjectEventQueue.dispatchFlushCompleteEvent(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.SystemSensorManager.InjectEventQueue.dispatchSensorEvent(int, float[], int, long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected void dispatchSensorEvent(int r1, float[] r2, int r3, long r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.SystemSensorManager.InjectEventQueue.dispatchSensorEvent(int, float[], int, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.SystemSensorManager.InjectEventQueue.dispatchSensorEvent(int, float[], int, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.hardware.SystemSensorManager.InjectEventQueue.injectSensorData(int, float[], int, long):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        int injectSensorData(int r1, float[] r2, int r3, long r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.hardware.SystemSensorManager.InjectEventQueue.injectSensorData(int, float[], int, long):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.SystemSensorManager.InjectEventQueue.injectSensorData(int, float[], int, long):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.SystemSensorManager.InjectEventQueue.removeSensorEvent(android.hardware.Sensor):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected void removeSensorEvent(android.hardware.Sensor r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.SystemSensorManager.InjectEventQueue.removeSensorEvent(android.hardware.Sensor):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.SystemSensorManager.InjectEventQueue.removeSensorEvent(android.hardware.Sensor):void");
        }
    }

    static final class SensorEventQueue extends BaseEventQueue {
        int MAX_STEP_INTERVAL;
        float mLastStep;
        private final SensorEventListener mListener;
        private final SparseArray<SensorEvent> mSensorsEvents;

        public SensorEventQueue(SensorEventListener listener, Looper looper, SystemSensorManager manager, String packageName) {
            super(looper, manager, 0, packageName);
            this.mSensorsEvents = new SparseArray();
            this.mLastStep = TonemapCurve.LEVEL_BLACK;
            this.MAX_STEP_INTERVAL = 100;
            this.mListener = listener;
        }

        public void addSensorEvent(Sensor sensor) {
            SensorEvent t = new SensorEvent(Sensor.getMaxLengthValuesArray(sensor, this.mManager.mTargetSdkLevel));
            synchronized (this.mSensorsEvents) {
                this.mSensorsEvents.put(sensor.getHandle(), t);
            }
        }

        public void removeSensorEvent(Sensor sensor) {
            synchronized (this.mSensorsEvents) {
                this.mSensorsEvents.delete(sensor.getHandle());
            }
        }

        protected void dispatchSensorEvent(int handle, float[] values, int inAccuracy, long timestamp) {
            Sensor sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle));
            if (sensor != null) {
                SensorEvent t;
                synchronized (this.mSensorsEvents) {
                    t = (SensorEvent) this.mSensorsEvents.get(handle);
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
                        if (sensor.getType() == 19 && (this.mLastStep == TonemapCurve.LEVEL_BLACK || Math.abs(values[0] - this.mLastStep) >= ((float) this.MAX_STEP_INTERVAL))) {
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

        protected void dispatchFlushCompleteEvent(int handle) {
            if (this.mListener instanceof SensorEventListener2) {
                Sensor sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle));
                if (sensor != null) {
                    ((SensorEventListener2) this.mListener).onFlushCompleted(sensor);
                }
            }
        }

        protected void dispatchAdditionalInfoEvent(int handle, int type, int serial, float[] floatValues, int[] intValues) {
            if (this.mListener instanceof SensorEventCallback) {
                Sensor sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle));
                if (sensor != null) {
                    ((SensorEventCallback) this.mListener).onSensorAdditionalInfo(new SensorAdditionalInfo(sensor, type, serial, intValues, floatValues));
                }
            }
        }
    }

    static final class TriggerEventQueue extends BaseEventQueue {
        private final TriggerEventListener mListener;
        private final SparseArray<TriggerEvent> mTriggerEvents;

        public TriggerEventQueue(TriggerEventListener listener, Looper looper, SystemSensorManager manager, String packageName) {
            super(looper, manager, 0, packageName);
            this.mTriggerEvents = new SparseArray();
            this.mListener = listener;
        }

        public void addSensorEvent(Sensor sensor) {
            TriggerEvent t = new TriggerEvent(Sensor.getMaxLengthValuesArray(sensor, this.mManager.mTargetSdkLevel));
            synchronized (this.mTriggerEvents) {
                this.mTriggerEvents.put(sensor.getHandle(), t);
            }
        }

        public void removeSensorEvent(Sensor sensor) {
            synchronized (this.mTriggerEvents) {
                this.mTriggerEvents.delete(sensor.getHandle());
            }
        }

        protected void dispatchSensorEvent(int handle, float[] values, int accuracy, long timestamp) {
            Sensor sensor = (Sensor) this.mManager.mHandleToSensor.get(Integer.valueOf(handle));
            if (sensor != null) {
                TriggerEvent t;
                synchronized (this.mTriggerEvents) {
                    t = (TriggerEvent) this.mTriggerEvents.get(handle);
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

        protected void dispatchFlushCompleteEvent(int handle) {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.SystemSensorManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.SystemSensorManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.SystemSensorManager.<clinit>():void");
    }

    private static native void nativeClassInit();

    private static native long nativeCreate(String str);

    private static native void nativeGetDynamicSensors(long j, List<Sensor> list);

    private static native boolean nativeGetSensorAtIndex(long j, Sensor sensor, int i);

    private static native boolean nativeIsDataInjectionEnabled(long j);

    public SystemSensorManager(Context context, Looper mainLooper) {
        this.mFullSensorsList = new ArrayList();
        this.mFullDynamicSensorsList = new ArrayList();
        this.mDynamicSensorListDirty = true;
        this.mHandleToSensor = new HashMap();
        this.mSensorListeners = new HashMap();
        this.mTriggerListeners = new HashMap();
        this.mDynamicSensorCallbacks = new HashMap();
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

    protected List<Sensor> getFullSensorList() {
        return this.mFullSensorsList;
    }

    protected List<Sensor> getFullDynamicSensorList() {
        setupDynamicSensorBroadcastReceiver();
        updateDynamicSensorList();
        return this.mFullDynamicSensorsList;
    }

    protected boolean registerListenerImpl(SensorEventListener listener, Sensor sensor, int delayUs, Handler handler, int maxBatchReportLatencyUs, int reservedFlags) {
        if (listener == null || sensor == null) {
            Log.e("SensorManager", "sensor or listener is null");
            return false;
        } else if (sensor.getReportingMode() == 2) {
            Log.e("SensorManager", "Trigger Sensors should use the requestTriggerSensor.");
            return false;
        } else if (maxBatchReportLatencyUs < 0 || delayUs < 0) {
            Log.e("SensorManager", "maxBatchReportLatencyUs and delayUs should be non-negative");
            return false;
        } else {
            Log.v("SensorManager", "RegisterListener " + sensor.getName() + " type:" + sensor.getType() + " delay:" + delayUs + "us by " + listener.getClass().getName());
            synchronized (this.mSensorListeners) {
                SensorEventQueue queue = (SensorEventQueue) this.mSensorListeners.get(listener);
                if (queue == null) {
                    String fullClassName;
                    Looper looper = handler != null ? handler.getLooper() : this.mMainLooper;
                    if (listener.getClass().getEnclosingClass() != null) {
                        fullClassName = listener.getClass().getEnclosingClass().getName();
                    } else {
                        fullClassName = listener.getClass().getName();
                    }
                    queue = new SensorEventQueue(listener, looper, this, fullClassName);
                    if (queue.addSensor(sensor, delayUs, maxBatchReportLatencyUs)) {
                        this.mSensorListeners.put(listener, queue);
                        return true;
                    }
                    queue.dispose();
                    return false;
                }
                boolean addSensor = queue.addSensor(sensor, delayUs, maxBatchReportLatencyUs);
                return addSensor;
            }
        }
    }

    protected void unregisterListenerImpl(SensorEventListener listener, Sensor sensor) {
        if (listener != null) {
            Log.v("SensorManager", "unRegisterListener by " + listener.getClass().getName());
        }
        if (sensor == null || sensor.getReportingMode() != 2) {
            synchronized (this.mSensorListeners) {
                SensorEventQueue queue = (SensorEventQueue) this.mSensorListeners.get(listener);
                if (queue != null) {
                    boolean result;
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

    protected boolean requestTriggerSensorImpl(TriggerEventListener listener, Sensor sensor) {
        if (sensor == null) {
            throw new IllegalArgumentException("sensor cannot be null");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else if (sensor.getReportingMode() != 2) {
            return false;
        } else {
            synchronized (this.mTriggerListeners) {
                TriggerEventQueue queue = (TriggerEventQueue) this.mTriggerListeners.get(listener);
                if (queue == null) {
                    String fullClassName;
                    if (listener.getClass().getEnclosingClass() != null) {
                        fullClassName = listener.getClass().getEnclosingClass().getName();
                    } else {
                        fullClassName = listener.getClass().getName();
                    }
                    queue = new TriggerEventQueue(listener, this.mMainLooper, this, fullClassName);
                    if (queue.addSensor(sensor, 0, 0)) {
                        this.mTriggerListeners.put(listener, queue);
                        return true;
                    }
                    queue.dispose();
                    return false;
                }
                boolean addSensor = queue.addSensor(sensor, 0, 0);
                return addSensor;
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0027, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean cancelTriggerSensorImpl(TriggerEventListener listener, Sensor sensor, boolean disable) {
        if (sensor != null && sensor.getReportingMode() != 2) {
            return false;
        }
        synchronized (this.mTriggerListeners) {
            TriggerEventQueue queue = (TriggerEventQueue) this.mTriggerListeners.get(listener);
            if (queue != null) {
                boolean result;
                if (sensor == null) {
                    result = queue.removeAllSensors();
                } else {
                    result = queue.removeSensor(sensor, disable);
                }
                if (result && !queue.hasSensors()) {
                    this.mTriggerListeners.remove(listener);
                    queue.dispose();
                }
            } else {
                return false;
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0023, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean flushImpl(SensorEventListener listener) {
        boolean z = false;
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        synchronized (this.mSensorListeners) {
            SensorEventQueue queue = (SensorEventQueue) this.mSensorListeners.get(listener);
            if (queue == null) {
                return false;
            } else if (queue.flush() == 0) {
                z = true;
            }
        }
    }

    protected boolean initDataInjectionImpl(boolean enable) {
        synchronized (sLock) {
            if (enable) {
                if (!nativeIsDataInjectionEnabled(this.mNativeInstance)) {
                    Log.e("SensorManager", "Data Injection mode not enabled");
                    return false;
                } else if (sInjectEventQueue == null) {
                    sInjectEventQueue = new InjectEventQueue(this, this.mMainLooper, this, this.mContext.getPackageName());
                }
            } else if (sInjectEventQueue != null) {
                sInjectEventQueue.dispose();
                sInjectEventQueue = null;
            }
            return true;
        }
    }

    /* JADX WARNING: Missing block: B:15:0x002e, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean injectSensorDataImpl(Sensor sensor, float[] values, int accuracy, long timestamp) {
        synchronized (sLock) {
            if (sInjectEventQueue == null) {
                Log.e("SensorManager", "Data injection mode not activated before calling injectSensorData");
                return false;
            }
            int ret = sInjectEventQueue.injectSensorData(sensor.getHandle(), values, accuracy, timestamp);
            if (ret != 0) {
                sInjectEventQueue.dispose();
                sInjectEventQueue = null;
            }
            boolean z = ret == 0;
        }
    }

    private void cleanupSensorConnection(Sensor sensor) {
        this.mHandleToSensor.remove(Integer.valueOf(sensor.getHandle()));
        HashMap hashMap;
        if (sensor.getReportingMode() == 2) {
            hashMap = this.mTriggerListeners;
            synchronized (hashMap) {
                for (TriggerEventListener l : this.mTriggerListeners.keySet()) {
                    if (DEBUG_DYNAMIC_SENSOR) {
                        Log.i("SensorManager", "removed trigger listener" + l.toString() + " due to sensor disconnection");
                    }
                    cancelTriggerSensorImpl(l, sensor, true);
                }
            }
        } else {
            hashMap = this.mSensorListeners;
            synchronized (hashMap) {
                for (SensorEventListener l2 : this.mSensorListeners.keySet()) {
                    if (DEBUG_DYNAMIC_SENSOR) {
                        Log.i("SensorManager", "removed event listener" + l2.toString() + " due to sensor disconnection");
                    }
                    unregisterListenerImpl(l2, sensor);
                }
            }
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void updateDynamicSensorList() {
        /*
        r18 = this;
        r0 = r18;
        r15 = r0.mFullDynamicSensorsList;
        monitor-enter(r15);
        r0 = r18;	 Catch:{ all -> 0x0069 }
        r14 = r0.mDynamicSensorListDirty;	 Catch:{ all -> 0x0069 }
        if (r14 == 0) goto L_0x00cb;	 Catch:{ all -> 0x0069 }
    L_0x000b:
        r8 = new java.util.ArrayList;	 Catch:{ all -> 0x0069 }
        r8.<init>();	 Catch:{ all -> 0x0069 }
        r0 = r18;	 Catch:{ all -> 0x0069 }
        r0 = r0.mNativeInstance;	 Catch:{ all -> 0x0069 }
        r16 = r0;	 Catch:{ all -> 0x0069 }
        r0 = r16;	 Catch:{ all -> 0x0069 }
        nativeGetDynamicSensors(r0, r8);	 Catch:{ all -> 0x0069 }
        r13 = new java.util.ArrayList;	 Catch:{ all -> 0x0069 }
        r13.<init>();	 Catch:{ all -> 0x0069 }
        r2 = new java.util.ArrayList;	 Catch:{ all -> 0x0069 }
        r2.<init>();	 Catch:{ all -> 0x0069 }
        r10 = new java.util.ArrayList;	 Catch:{ all -> 0x0069 }
        r10.<init>();	 Catch:{ all -> 0x0069 }
        r0 = r18;	 Catch:{ all -> 0x0069 }
        r14 = r0.mFullDynamicSensorsList;	 Catch:{ all -> 0x0069 }
        r4 = diffSortedSensorList(r14, r8, r13, r2, r10);	 Catch:{ all -> 0x0069 }
        if (r4 == 0) goto L_0x00c6;	 Catch:{ all -> 0x0069 }
    L_0x0034:
        r14 = DEBUG_DYNAMIC_SENSOR;	 Catch:{ all -> 0x0069 }
        if (r14 == 0) goto L_0x0043;	 Catch:{ all -> 0x0069 }
    L_0x0038:
        r14 = "SensorManager";	 Catch:{ all -> 0x0069 }
        r16 = "DYNS dynamic sensor list cached should be updated";	 Catch:{ all -> 0x0069 }
        r0 = r16;	 Catch:{ all -> 0x0069 }
        android.util.Log.i(r14, r0);	 Catch:{ all -> 0x0069 }
    L_0x0043:
        r0 = r18;	 Catch:{ all -> 0x0069 }
        r0.mFullDynamicSensorsList = r13;	 Catch:{ all -> 0x0069 }
        r12 = r2.iterator();	 Catch:{ all -> 0x0069 }
    L_0x004b:
        r14 = r12.hasNext();	 Catch:{ all -> 0x0069 }
        if (r14 == 0) goto L_0x006c;	 Catch:{ all -> 0x0069 }
    L_0x0051:
        r11 = r12.next();	 Catch:{ all -> 0x0069 }
        r11 = (android.hardware.Sensor) r11;	 Catch:{ all -> 0x0069 }
        r0 = r18;	 Catch:{ all -> 0x0069 }
        r14 = r0.mHandleToSensor;	 Catch:{ all -> 0x0069 }
        r16 = r11.getHandle();	 Catch:{ all -> 0x0069 }
        r16 = java.lang.Integer.valueOf(r16);	 Catch:{ all -> 0x0069 }
        r0 = r16;	 Catch:{ all -> 0x0069 }
        r14.put(r0, r11);	 Catch:{ all -> 0x0069 }
        goto L_0x004b;
    L_0x0069:
        r14 = move-exception;
        monitor-exit(r15);
        throw r14;
    L_0x006c:
        r9 = new android.os.Handler;	 Catch:{ all -> 0x0069 }
        r0 = r18;	 Catch:{ all -> 0x0069 }
        r14 = r0.mContext;	 Catch:{ all -> 0x0069 }
        r14 = r14.getMainLooper();	 Catch:{ all -> 0x0069 }
        r9.<init>(r14);	 Catch:{ all -> 0x0069 }
        r0 = r18;	 Catch:{ all -> 0x0069 }
        r14 = r0.mDynamicSensorCallbacks;	 Catch:{ all -> 0x0069 }
        r14 = r14.entrySet();	 Catch:{ all -> 0x0069 }
        r6 = r14.iterator();	 Catch:{ all -> 0x0069 }
    L_0x0085:
        r14 = r6.hasNext();	 Catch:{ all -> 0x0069 }
        if (r14 == 0) goto L_0x00b0;	 Catch:{ all -> 0x0069 }
    L_0x008b:
        r5 = r6.next();	 Catch:{ all -> 0x0069 }
        r5 = (java.util.Map.Entry) r5;	 Catch:{ all -> 0x0069 }
        r3 = r5.getKey();	 Catch:{ all -> 0x0069 }
        r3 = (android.hardware.SensorManager.DynamicSensorCallback) r3;	 Catch:{ all -> 0x0069 }
        r14 = r5.getValue();	 Catch:{ all -> 0x0069 }
        if (r14 != 0) goto L_0x00a9;	 Catch:{ all -> 0x0069 }
    L_0x009d:
        r7 = r9;	 Catch:{ all -> 0x0069 }
    L_0x009e:
        r14 = new android.hardware.SystemSensorManager$1;	 Catch:{ all -> 0x0069 }
        r0 = r18;	 Catch:{ all -> 0x0069 }
        r14.<init>(r0, r2, r3, r10);	 Catch:{ all -> 0x0069 }
        r7.post(r14);	 Catch:{ all -> 0x0069 }
        goto L_0x0085;	 Catch:{ all -> 0x0069 }
    L_0x00a9:
        r7 = r5.getValue();	 Catch:{ all -> 0x0069 }
        r7 = (android.os.Handler) r7;	 Catch:{ all -> 0x0069 }
        goto L_0x009e;	 Catch:{ all -> 0x0069 }
    L_0x00b0:
        r12 = r10.iterator();	 Catch:{ all -> 0x0069 }
    L_0x00b4:
        r14 = r12.hasNext();	 Catch:{ all -> 0x0069 }
        if (r14 == 0) goto L_0x00c6;	 Catch:{ all -> 0x0069 }
    L_0x00ba:
        r11 = r12.next();	 Catch:{ all -> 0x0069 }
        r11 = (android.hardware.Sensor) r11;	 Catch:{ all -> 0x0069 }
        r0 = r18;	 Catch:{ all -> 0x0069 }
        r0.cleanupSensorConnection(r11);	 Catch:{ all -> 0x0069 }
        goto L_0x00b4;	 Catch:{ all -> 0x0069 }
    L_0x00c6:
        r14 = 0;	 Catch:{ all -> 0x0069 }
        r0 = r18;	 Catch:{ all -> 0x0069 }
        r0.mDynamicSensorListDirty = r14;	 Catch:{ all -> 0x0069 }
    L_0x00cb:
        monitor-exit(r15);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.SystemSensorManager.updateDynamicSensorList():void");
    }

    private void setupDynamicSensorBroadcastReceiver() {
        if (this.mDynamicSensorBroadcastReceiver == null) {
            this.mDynamicSensorBroadcastReceiver = new AnonymousClass2(this);
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

    protected void registerDynamicSensorCallbackImpl(DynamicSensorCallback callback, Handler handler) {
        if (DEBUG_DYNAMIC_SENSOR) {
            Log.i("SensorManager", "DYNS Register dynamic sensor callback");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        } else if (!this.mDynamicSensorCallbacks.containsKey(callback)) {
            setupDynamicSensorBroadcastReceiver();
            this.mDynamicSensorCallbacks.put(callback, handler);
        }
    }

    protected void unregisterDynamicSensorCallbackImpl(DynamicSensorCallback callback) {
        if (DEBUG_DYNAMIC_SENSOR) {
            Log.i("SensorManager", "Removing dynamic sensor listerner");
        }
        this.mDynamicSensorCallbacks.remove(callback);
    }

    private static boolean diffSortedSensorList(List<Sensor> oldList, List<Sensor> newList, List<Sensor> updated, List<Sensor> added, List<Sensor> removed) {
        boolean changed = false;
        int i = 0;
        int j = 0;
        while (true) {
            if (j < oldList.size() && (i >= newList.size() || ((Sensor) newList.get(i)).getHandle() > ((Sensor) oldList.get(j)).getHandle())) {
                changed = true;
                if (removed != null) {
                    removed.add((Sensor) oldList.get(j));
                }
                j++;
            } else if (i < newList.size() && (j >= oldList.size() || ((Sensor) newList.get(i)).getHandle() < ((Sensor) oldList.get(j)).getHandle())) {
                changed = true;
                if (added != null) {
                    added.add((Sensor) newList.get(i));
                }
                if (updated != null) {
                    updated.add((Sensor) newList.get(i));
                }
                i++;
            } else if (i >= newList.size() || j >= oldList.size() || ((Sensor) newList.get(i)).getHandle() != ((Sensor) oldList.get(j)).getHandle()) {
                return changed;
            } else {
                if (updated != null) {
                    updated.add((Sensor) oldList.get(j));
                }
                i++;
                j++;
            }
        }
        return changed;
    }
}
