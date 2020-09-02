package com.android.server.coloros;

import android.app.AlarmManager;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.PowerManagerInternal;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.LocalServices;
import com.android.server.am.IColorAbnormalAppManager;
import com.android.server.pm.Settings;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.voiceinteraction.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;

public class OppoSysStateManager {
    private static final String ACTION_FEEDBACK_NOT_RESTRICT_PKG = "android.intent.action.OPPO_GUARDELF_FEEDBACK_NOT_RESTRICT_PKG";
    private static final String ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP = "android.intent.action.OPPO_GUARD_ELF_MONITOR_FORCESTOP";
    private static final long DELAY_CAMERA_MONITOR = 300000;
    private static final String HANDLE_RESULT_NAVIGATION = "navigation";
    private static final String HANDLE_RESULT_NOT_RESTRICT = "notRestrict";
    private static final String HANDLE_RESULT_PALYBACK = "playback";
    private static final String TAG = "OppoSysStateManager";
    private static final String TYPE_ALARM = "alarm";
    private static final String TYPE_WAKELOCK = "wakelock";
    private static OppoSysStateManager mOppoSysStateManager = null;
    /* access modifiers changed from: private */
    public boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private AlarmManager mAlarmManager;
    private final AlarmManager.OnAlarmListener mCameraAlarmListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.coloros.OppoSysStateManager.AnonymousClass1 */

        public void onAlarm() {
            OppoSysStateManager oppoSysStateManager = OppoSysStateManager.this;
            oppoSysStateManager.mCameraAlarmSetted = false;
            if (oppoSysStateManager.DEBUG) {
                Slog.d(OppoSysStateManager.TAG, "CameraMonitor");
            }
            if (!OppoSysStateManager.this.mIsScreenOn) {
                int uid = OppoSysStateManager.this.getCameraWorkingUid();
                if (OppoSysStateManager.this.DEBUG) {
                    Slog.d(OppoSysStateManager.TAG, "CameraMonitor: uid=" + uid);
                }
                if (uid >= 10000) {
                    ArrayList<String> uidList = new ArrayList<>();
                    String[] packages = OppoSysStateManager.this.getPackagesForUid(uid);
                    if (packages != null) {
                        for (String pkg : packages) {
                            uidList.add("[ " + pkg + " ]    ");
                            if (OppoSysStateManager.this.DEBUG) {
                                Slog.d(OppoSysStateManager.TAG, "CameraMonitor: pkg=" + pkg);
                            }
                        }
                        Intent intent = new Intent(OppoSysStateManager.ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP);
                        intent.putStringArrayListExtra("data", uidList);
                        intent.putExtra(DatabaseHelper.SoundModelContract.KEY_TYPE, "camera");
                        OppoSysStateManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    }
                }
            } else if (OppoSysStateManager.this.DEBUG) {
                Slog.d(OppoSysStateManager.TAG, "CameraMonitor: screen on");
            }
        }
    };
    volatile boolean mCameraAlarmSetted = false;
    /* access modifiers changed from: private */
    public Context mContext;
    private Handler mHandler;
    volatile boolean mIsCharging = false;
    volatile boolean mIsScreenOn = true;
    SparseArray<Integer> mListCameraOn = new SparseArray<>();
    ArrayList<String> mListNotRestrictAlarm = new ArrayList<>();
    ArrayList<String> mListNotRestrictWakeLock = new ArrayList<>();
    List<String> mListOppoGuardElfNavigation = new ArrayList();
    List<String> mListOppoGuardElfPlayback = new ArrayList();
    List<String> mListOppoGuardElfWhitelist = new ArrayList();
    volatile boolean mListSensorInited = false;
    SparseArray<Integer> mListSensorType = new SparseArray<>();
    private IPackageManager mPkm;
    BroadcastReceiver mProcessResultReceiver = new BroadcastReceiver() {
        /* class com.android.server.coloros.OppoSysStateManager.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            String pkgName;
            if (OppoSysStateManager.ACTION_FEEDBACK_NOT_RESTRICT_PKG.equals(intent.getAction()) && (pkgName = intent.getStringExtra("PkgName")) != null) {
                String type = intent.getStringExtra(DatabaseHelper.SoundModelContract.KEY_TYPE);
                if (OppoSysStateManager.this.DEBUG) {
                    Slog.d(OppoSysStateManager.TAG, "onReceive FEEDBACK_NOT_RESTRICT_PKG: pkgName=" + pkgName + ", type=" + type);
                }
                if (type == null) {
                    synchronized (OppoSysStateManager.this.mListNotRestrictAlarm) {
                        if (!OppoSysStateManager.this.mListNotRestrictAlarm.contains(pkgName)) {
                            OppoSysStateManager.this.mListNotRestrictAlarm.add(pkgName);
                        }
                    }
                    synchronized (OppoSysStateManager.this.mListNotRestrictWakeLock) {
                        if (!OppoSysStateManager.this.mListNotRestrictWakeLock.contains(pkgName)) {
                            OppoSysStateManager.this.mListNotRestrictWakeLock.add(pkgName);
                        }
                    }
                } else if (OppoSysStateManager.TYPE_ALARM.equals(type)) {
                    synchronized (OppoSysStateManager.this.mListNotRestrictAlarm) {
                        if (!OppoSysStateManager.this.mListNotRestrictAlarm.contains(pkgName)) {
                            OppoSysStateManager.this.mListNotRestrictAlarm.add(pkgName);
                        }
                    }
                } else if (OppoSysStateManager.TYPE_WAKELOCK.equals(type)) {
                    synchronized (OppoSysStateManager.this.mListNotRestrictWakeLock) {
                        if (!OppoSysStateManager.this.mListNotRestrictWakeLock.contains(pkgName)) {
                            OppoSysStateManager.this.mListNotRestrictWakeLock.add(pkgName);
                        }
                    }
                }
                String reason = intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (OppoSysStateManager.HANDLE_RESULT_NOT_RESTRICT.equals(reason)) {
                        if (!OppoSysStateManager.this.mListOppoGuardElfWhitelist.contains(pkgName)) {
                            OppoSysStateManager.this.mListOppoGuardElfWhitelist.add(pkgName);
                            if (OppoSysStateManager.this.DEBUG) {
                                Slog.d(OppoSysStateManager.TAG, "feedback: whitelist add " + pkgName);
                            }
                        }
                    } else if (OppoSysStateManager.HANDLE_RESULT_PALYBACK.equals(reason)) {
                        if (!OppoSysStateManager.this.mListOppoGuardElfPlayback.contains(pkgName)) {
                            OppoSysStateManager.this.mListOppoGuardElfPlayback.add(pkgName);
                            if (OppoSysStateManager.this.DEBUG) {
                                Slog.d(OppoSysStateManager.TAG, "feedback: playback add " + pkgName);
                            }
                        }
                    } else if (OppoSysStateManager.HANDLE_RESULT_NAVIGATION.equals(reason) && !OppoSysStateManager.this.mListOppoGuardElfNavigation.contains(pkgName)) {
                        OppoSysStateManager.this.mListOppoGuardElfNavigation.add(pkgName);
                        if (OppoSysStateManager.this.DEBUG) {
                            Slog.d(OppoSysStateManager.TAG, "feedback: navigation add " + pkgName);
                        }
                    }
                }
            }
        }
    };
    private boolean mRcvInited = false;
    volatile boolean mRestrictStartupBg = false;
    SparseArray<SparseArray<SensorStatistics>> mSensorList = new SparseArray<>();

    private OppoSysStateManager() {
        LocalServices.addService(OppoSysStateManagerInternal.class, new LocalService());
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
            context.registerReceiver(this.mProcessResultReceiver, filter, "oppo.permission.OPPO_COMPONENT_SAFE", handler);
            this.mContext = context;
            this.mHandler = handler;
        }
    }

    public boolean isNotRestrictPkgAlarm(String pkg) {
        synchronized (this.mListNotRestrictAlarm) {
            if (this.mListNotRestrictAlarm.contains(pkg)) {
                return true;
            }
            return false;
        }
    }

    public boolean isNotRestrictPkgWakeLock(String pkg) {
        synchronized (this.mListNotRestrictWakeLock) {
            if (this.mListNotRestrictWakeLock.contains(pkg)) {
                return true;
            }
            return false;
        }
    }

    public List<String> getOppoGuardElfWhiteList() {
        synchronized (this.mListOppoGuardElfWhitelist) {
            if (this.mListOppoGuardElfWhitelist.isEmpty()) {
                return null;
            }
            ArrayList arrayList = new ArrayList(this.mListOppoGuardElfWhitelist);
            return arrayList;
        }
    }

    public List<String> getOppoGuardElfPlayback() {
        synchronized (this.mListOppoGuardElfPlayback) {
            if (this.mListOppoGuardElfPlayback.isEmpty()) {
                return null;
            }
            ArrayList arrayList = new ArrayList(this.mListOppoGuardElfPlayback);
            return arrayList;
        }
    }

    public List<String> getOppoGuardElfNavigation() {
        synchronized (this.mListOppoGuardElfNavigation) {
            if (this.mListOppoGuardElfNavigation.isEmpty()) {
                return null;
            }
            ArrayList arrayList = new ArrayList(this.mListOppoGuardElfNavigation);
            return arrayList;
        }
    }

    /* access modifiers changed from: private */
    public void judgeRestrictStartupBg() {
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
            SparseArray<SensorStatistics> sensorPerUid = this.mSensorList.get(uid);
            if (sensorPerUid == null) {
                return false;
            }
            SensorStatistics sensor = sensorPerUid.get(type);
            if (sensor == null) {
                return false;
            }
            boolean isWorking = sensor.isWorking();
            return isWorking;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0029, code lost:
        return r1;
     */
    public boolean isSensorWorking(int uid) {
        synchronized (this.mSensorList) {
            boolean isWorking = false;
            SparseArray<SensorStatistics> sensorPerUid = this.mSensorList.get(uid);
            if (sensorPerUid == null) {
                return false;
            }
            int count = sensorPerUid.size();
            int i = 0;
            while (true) {
                if (i >= count) {
                    break;
                } else if (sensorPerUid.valueAt(i).isWorking()) {
                    isWorking = true;
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    public boolean isSensorUsedEver(int uid, int type) {
        synchronized (this.mSensorList) {
            SparseArray<SensorStatistics> sensorPerUid = this.mSensorList.get(uid);
            if (sensorPerUid == null) {
                return false;
            }
            if (sensorPerUid.get(type) == null) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public SensorStatistics getSensor(int uid, int type) {
        SensorStatistics sensor;
        synchronized (this.mSensorList) {
            SparseArray<SensorStatistics> sensorPerUid = this.mSensorList.get(uid);
            if (sensorPerUid == null) {
                sensorPerUid = new SparseArray<>();
                this.mSensorList.put(uid, sensorPerUid);
            }
            sensor = sensorPerUid.get(type);
            if (sensor == null) {
                sensor = new SensorStatistics(uid, type);
                sensorPerUid.put(type, sensor);
            }
        }
        return sensor;
    }

    /* access modifiers changed from: private */
    public class SensorStatistics {
        int mNesting;
        int mType;
        int mUid;

        public SensorStatistics(int uid, int type) {
            this.mType = type;
            this.mUid = uid;
        }

        public void start() {
            this.mNesting++;
        }

        public void stop() {
            int i = this.mNesting;
            if (i != 0) {
                this.mNesting = i - 1;
            }
        }

        public boolean isWorking() {
            return this.mNesting > 0;
        }
    }

    private final class LocalService extends OppoSysStateManagerInternal {
        private LocalService() {
        }

        @Override // com.android.server.coloros.OppoSysStateManagerInternal
        public void onWakefulnessChanged(int wakefulness) {
            boolean isScreenOn = PowerManagerInternal.isInteractive(wakefulness);
            if (isScreenOn != OppoSysStateManager.this.mIsScreenOn) {
                OppoSysStateManager oppoSysStateManager = OppoSysStateManager.this;
                oppoSysStateManager.mIsScreenOn = isScreenOn;
                oppoSysStateManager.judgeRestrictStartupBg();
                if (OppoSysStateManager.this.DEBUG) {
                    Slog.d(OppoSysStateManager.TAG, "onWakefulnessChanged: wakefulness=" + PowerManagerInternal.wakefulnessToString(wakefulness) + ", mIsScreenOn=" + OppoSysStateManager.this.mIsScreenOn + ", mIsCharging=" + OppoSysStateManager.this.mIsCharging + ", mRestrictStartupBg=" + OppoSysStateManager.this.mRestrictStartupBg);
                }
                OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).updateScreenStatus(OppoSysStateManager.this.mIsScreenOn);
                if (!isScreenOn) {
                    synchronized (OppoSysStateManager.this.mListNotRestrictAlarm) {
                        OppoSysStateManager.this.mListNotRestrictAlarm.clear();
                    }
                    synchronized (OppoSysStateManager.this.mListNotRestrictWakeLock) {
                        OppoSysStateManager.this.mListNotRestrictWakeLock.clear();
                    }
                    synchronized (OppoSysStateManager.this.mListOppoGuardElfWhitelist) {
                        OppoSysStateManager.this.mListOppoGuardElfWhitelist.clear();
                    }
                    synchronized (OppoSysStateManager.this.mListOppoGuardElfPlayback) {
                        OppoSysStateManager.this.mListOppoGuardElfPlayback.clear();
                    }
                    synchronized (OppoSysStateManager.this.mListOppoGuardElfNavigation) {
                        OppoSysStateManager.this.mListOppoGuardElfNavigation.clear();
                    }
                    if (OppoSysStateManager.this.isCameraWorking()) {
                        OppoSysStateManager.this.scheduleCameraMonitor();
                        return;
                    }
                    return;
                }
                OppoSysStateManager.this.cancelCameraMonitor();
            }
        }

        @Override // com.android.server.coloros.OppoSysStateManagerInternal
        public void onPlugChanged(int plugType) {
            boolean isCharging = plugType != 0;
            if (isCharging != OppoSysStateManager.this.mIsCharging) {
                OppoSysStateManager oppoSysStateManager = OppoSysStateManager.this;
                oppoSysStateManager.mIsCharging = isCharging;
                oppoSysStateManager.judgeRestrictStartupBg();
                if (OppoSysStateManager.this.DEBUG) {
                    Slog.d(OppoSysStateManager.TAG, "onPlugChanged: plugType=" + plugType + ", mIsScreenOn=" + OppoSysStateManager.this.mIsScreenOn + ", mIsCharging=" + OppoSysStateManager.this.mIsCharging + ", mRestrictStartupBg=" + OppoSysStateManager.this.mRestrictStartupBg);
                }
            }
        }

        @Override // com.android.server.coloros.OppoSysStateManagerInternal
        public void noteStartSensor(int uid, int handle) {
            OppoSysStateManager.this.getSensor(uid, OppoSysStateManager.this.sensorHandleToType(handle)).start();
        }

        @Override // com.android.server.coloros.OppoSysStateManagerInternal
        public void noteStopSensor(int uid, int handle) {
            OppoSysStateManager.this.getSensor(uid, OppoSysStateManager.this.sensorHandleToType(handle)).stop();
        }

        @Override // com.android.server.coloros.OppoSysStateManagerInternal
        public void noteCameraOnOff(int uid, boolean on) {
            synchronized (OppoSysStateManager.this.mListCameraOn) {
                int nesting = 0;
                if (OppoSysStateManager.this.mListCameraOn.get(uid) != null) {
                    nesting = OppoSysStateManager.this.mListCameraOn.get(uid).intValue();
                }
                if (on) {
                    OppoSysStateManager.this.mListCameraOn.put(uid, Integer.valueOf(nesting + 1));
                    if (!OppoSysStateManager.this.mIsScreenOn) {
                        OppoSysStateManager.this.scheduleCameraMonitor();
                    }
                } else if (nesting > 0) {
                    OppoSysStateManager.this.mListCameraOn.put(uid, Integer.valueOf(nesting - 1));
                }
                if (!OppoSysStateManager.this.isCameraWorking()) {
                    OppoSysStateManager.this.cancelCameraMonitor();
                }
            }
        }

        @Override // com.android.server.coloros.OppoSysStateManagerInternal
        public void noteCameraReset() {
            synchronized (OppoSysStateManager.this.mListCameraOn) {
                OppoSysStateManager.this.mListCameraOn.clear();
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isCameraWorking() {
        boolean z;
        synchronized (this.mListCameraOn) {
            int cnt = 0;
            z = false;
            for (int i = 0; i < this.mListCameraOn.size(); i++) {
                if (this.mListCameraOn.valueAt(i).intValue() > 0) {
                    cnt++;
                }
            }
            if (cnt == 1) {
                z = true;
            }
        }
        return z;
    }

    public int getCameraWorkingUid() {
        synchronized (this.mListCameraOn) {
            int uid = -1;
            int cnt = 0;
            for (int i = 0; i < this.mListCameraOn.size(); i++) {
                if (this.mListCameraOn.valueAt(i).intValue() > 0) {
                    cnt++;
                    uid = this.mListCameraOn.keyAt(i);
                    if (this.DEBUG) {
                        Slog.d(TAG, "getCameraWorkingUid: uid=" + uid + ", nesting=" + this.mListCameraOn.valueAt(i));
                    }
                }
            }
            if (cnt == 1) {
                return uid;
            }
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public void scheduleCameraMonitor() {
        if (initAlarmManager() && !this.mCameraAlarmSetted && this.mHandler != null) {
            this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + 300000, "CameraMonitor", this.mCameraAlarmListener, this.mHandler);
            this.mCameraAlarmSetted = true;
        }
    }

    /* access modifiers changed from: private */
    public void cancelCameraMonitor() {
        if (this.mCameraAlarmSetted && initAlarmManager()) {
            this.mAlarmManager.cancel(this.mCameraAlarmListener);
            this.mCameraAlarmSetted = false;
        }
    }

    private boolean initAlarmManager() {
        Context context = this.mContext;
        if (context != null && this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) context.getSystemService(TYPE_ALARM);
        }
        return this.mAlarmManager != null;
    }

    /* access modifiers changed from: private */
    public String[] getPackagesForUid(int uid) {
        if (!initIPackageManager()) {
            return null;
        }
        try {
            return this.mPkm.getPackagesForUid(uid);
        } catch (Exception e) {
            Slog.d(TAG, "getPackagesForUid exception");
            return null;
        }
    }

    private boolean initIPackageManager() {
        if (this.mContext != null && this.mPkm == null) {
            this.mPkm = IPackageManager.Stub.asInterface(ServiceManager.getService(Settings.ATTR_PACKAGE));
        }
        return this.mPkm != null;
    }

    private boolean initSensorTypeList() {
        if (this.mListSensorInited) {
            return true;
        }
        Context context = this.mContext;
        if (context == null) {
            Slog.d(TAG, "initSensorTypeList: mContext is null.");
            return false;
        }
        SensorManager sm = (SensorManager) context.getSystemService("sensor");
        if (sm == null) {
            Slog.d(TAG, "initSensorTypeList: SensorManager is null.");
            return false;
        }
        List<Sensor> listAllSensor = sm.getSensorList(-1);
        synchronized (this.mListSensorType) {
            for (int k = 0; k < listAllSensor.size(); k++) {
                Sensor sensor = listAllSensor.get(k);
                this.mListSensorType.put(sensor.getHandle(), Integer.valueOf(sensor.getType()));
            }
        }
        this.mListSensorInited = true;
        return this.mListSensorInited;
    }

    /* access modifiers changed from: private */
    public int sensorHandleToType(int handle) {
        int intValue;
        if (!initSensorTypeList()) {
            return 0;
        }
        synchronized (this.mListSensorType) {
            intValue = this.mListSensorType.get(handle).intValue();
        }
        return intValue;
    }
}
