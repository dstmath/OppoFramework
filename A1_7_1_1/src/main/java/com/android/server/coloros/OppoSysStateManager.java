package com.android.server.coloros;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.PowerManagerInternal;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.LocalServices;
import com.android.server.am.OppoAbnormalAppManager;
import java.util.ArrayList;

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
public class OppoSysStateManager {
    private static final String ACTION_FEEDBACK_NOT_RESTRICT_PKG = "android.intent.action.OPPO_GUARDELF_FEEDBACK_NOT_RESTRICT_PKG";
    private static final String TAG = "OppoSysStateManager";
    private static OppoSysStateManager mOppoSysStateManager;
    private boolean DEBUG;
    boolean mIsCharging;
    boolean mIsScreenOn;
    ArrayList<String> mListNotRestrict;
    BroadcastReceiver mProcessResultReceiver;
    private boolean mRcvInited;
    boolean mRestrictStartupBg;
    SparseArray<SparseArray<Sensor>> mSensorList;

    private final class LocalService extends OppoSysStateManagerInternal {
        /* synthetic */ LocalService(OppoSysStateManager this$0, LocalService localService) {
            this();
        }

        private LocalService() {
        }

        public void onWakefulnessChanged(int wakefulness) {
            boolean isScreenOn = PowerManagerInternal.isInteractive(wakefulness);
            if (isScreenOn != OppoSysStateManager.this.mIsScreenOn) {
                OppoSysStateManager.this.mIsScreenOn = isScreenOn;
                OppoSysStateManager.this.judgeRestrictStartupBg();
                if (OppoSysStateManager.this.DEBUG) {
                    Slog.d(OppoSysStateManager.TAG, "onWakefulnessChanged: wakefulness=" + PowerManagerInternal.wakefulnessToString(wakefulness) + ", mIsScreenOn=" + OppoSysStateManager.this.mIsScreenOn + ", mIsCharging=" + OppoSysStateManager.this.mIsCharging + ", mRestrictStartupBg=" + OppoSysStateManager.this.mRestrictStartupBg);
                }
                OppoAbnormalAppManager.getInstance().updateScreenStatus(OppoSysStateManager.this.mIsScreenOn);
                if (!isScreenOn) {
                    synchronized (OppoSysStateManager.this.mListNotRestrict) {
                        OppoSysStateManager.this.mListNotRestrict.clear();
                    }
                }
            }
        }

        public void onPlugChanged(int plugType) {
            boolean isCharging = plugType != 0;
            if (isCharging != OppoSysStateManager.this.mIsCharging) {
                OppoSysStateManager.this.mIsCharging = isCharging;
                OppoSysStateManager.this.judgeRestrictStartupBg();
                if (OppoSysStateManager.this.DEBUG) {
                    Slog.d(OppoSysStateManager.TAG, "onPlugChanged: plugType=" + plugType + ", mIsScreenOn=" + OppoSysStateManager.this.mIsScreenOn + ", mIsCharging=" + OppoSysStateManager.this.mIsCharging + ", mRestrictStartupBg=" + OppoSysStateManager.this.mRestrictStartupBg);
                }
            }
        }

        public void noteStartSensor(int uid, int type) {
            OppoSysStateManager.this.getSensor(uid, type).start();
        }

        public void noteStopSensor(int uid, int type) {
            OppoSysStateManager.this.getSensor(uid, type).stop();
        }
    }

    private class Sensor {
        int mNesting;
        int mType;
        int mUid;

        public Sensor(int uid, int type) {
            this.mType = type;
            this.mUid = uid;
        }

        public void start() {
            this.mNesting++;
        }

        public void stop() {
            if (this.mNesting != 0) {
                this.mNesting--;
            }
        }

        public boolean isWorking() {
            return this.mNesting > 0;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.coloros.OppoSysStateManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.coloros.OppoSysStateManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.coloros.OppoSysStateManager.<clinit>():void");
    }

    private OppoSysStateManager() {
        this.DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        this.mRcvInited = false;
        this.mListNotRestrict = new ArrayList();
        this.mRestrictStartupBg = false;
        this.mIsScreenOn = true;
        this.mIsCharging = false;
        this.mSensorList = new SparseArray();
        this.mProcessResultReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (OppoSysStateManager.ACTION_FEEDBACK_NOT_RESTRICT_PKG.equals(intent.getAction())) {
                    String pkgName = intent.getStringExtra("PkgName");
                    if (pkgName != null) {
                        if (OppoSysStateManager.this.DEBUG) {
                            Slog.d(OppoSysStateManager.TAG, "onReceive ACTION_FEEDBACK_NOT_RESTRICT_PKG: pkgName=" + pkgName);
                        }
                        synchronized (OppoSysStateManager.this.mListNotRestrict) {
                            if (!OppoSysStateManager.this.mListNotRestrict.contains(pkgName)) {
                                OppoSysStateManager.this.mListNotRestrict.add(pkgName);
                                if (OppoSysStateManager.this.DEBUG) {
                                    Slog.d(OppoSysStateManager.TAG, "onReceive ACTION_FEEDBACK_NOT_RESTRICT_PKG: mListNotRestrict.add " + pkgName);
                                }
                            }
                        }
                    }
                }
            }
        };
        LocalServices.addService(OppoSysStateManagerInternal.class, new LocalService(this, null));
    }

    public static OppoSysStateManager getInstance() {
        if (mOppoSysStateManager == null) {
            mOppoSysStateManager = new OppoSysStateManager();
        }
        return mOppoSysStateManager;
    }

    public boolean restrictStartupBg() {
        return this.mRestrictStartupBg;
    }

    public boolean isScreenOn() {
        return this.mIsScreenOn;
    }

    public boolean isScreenOff() {
        return !this.mIsScreenOn;
    }

    public boolean isCharging() {
        return this.mIsCharging;
    }

    public void initOppoGuardElfRcv(Context context, Handler handler) {
        if (!this.mRcvInited) {
            this.mRcvInited = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_FEEDBACK_NOT_RESTRICT_PKG);
            context.registerReceiver(this.mProcessResultReceiver, filter, null, handler);
        }
    }

    public boolean isNotRestrictPkg(String pkg) {
        synchronized (this.mListNotRestrict) {
            if (this.mListNotRestrict.contains(pkg)) {
                return true;
            }
            return false;
        }
    }

    private void judgeRestrictStartupBg() {
        if (this.mIsScreenOn) {
            this.mRestrictStartupBg = false;
        } else if (this.mIsCharging) {
            this.mRestrictStartupBg = false;
        } else {
            this.mRestrictStartupBg = true;
        }
    }

    public boolean isSensorWorking(int uid, int type) {
        synchronized (this.mSensorList) {
            SparseArray<Sensor> sensorPerUid = (SparseArray) this.mSensorList.get(uid);
            if (sensorPerUid == null) {
                return false;
            }
            Sensor sensor = (Sensor) sensorPerUid.get(type);
            if (sensor == null) {
                return false;
            }
            boolean isWorking = sensor.isWorking();
            return isWorking;
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0026, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isSensorWorking(int uid) {
        synchronized (this.mSensorList) {
            boolean isWorking = false;
            SparseArray<Sensor> sensorPerUid = (SparseArray) this.mSensorList.get(uid);
            if (sensorPerUid == null) {
                return false;
            }
            int count = sensorPerUid.size();
            for (int i = 0; i < count; i++) {
                if (((Sensor) sensorPerUid.valueAt(i)).isWorking()) {
                    isWorking = true;
                    break;
                }
            }
        }
    }

    public boolean isSensorUsedEver(int uid, int type) {
        synchronized (this.mSensorList) {
            SparseArray<Sensor> sensorPerUid = (SparseArray) this.mSensorList.get(uid);
            if (sensorPerUid == null) {
                return false;
            } else if (((Sensor) sensorPerUid.get(type)) == null) {
                return false;
            } else {
                return true;
            }
        }
    }

    private Sensor getSensor(int uid, int type) {
        Sensor sensor;
        synchronized (this.mSensorList) {
            SparseArray<Sensor> sensorPerUid = (SparseArray) this.mSensorList.get(uid);
            if (sensorPerUid == null) {
                sensorPerUid = new SparseArray();
                this.mSensorList.put(uid, sensorPerUid);
            }
            sensor = (Sensor) sensorPerUid.get(type);
            if (sensor == null) {
                sensor = new Sensor(uid, type);
                sensorPerUid.put(type, sensor);
            }
        }
        return sensor;
    }
}
