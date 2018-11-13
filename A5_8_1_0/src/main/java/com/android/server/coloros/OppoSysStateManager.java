package com.android.server.coloros;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
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
import com.android.server.am.OppoAbnormalAppManager;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
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
    private static OppoSysStateManager mOppoSysStateManager = null;
    private boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private AlarmManager mAlarmManager;
    private final OnAlarmListener mCameraAlarmListener = new OnAlarmListener() {
        public void onAlarm() {
            OppoSysStateManager.this.mCameraAlarmSetted = false;
            if (OppoSysStateManager.this.DEBUG) {
                Slog.d(OppoSysStateManager.TAG, "CameraMonitor");
            }
            if (OppoSysStateManager.this.mIsScreenOn) {
                if (OppoSysStateManager.this.DEBUG) {
                    Slog.d(OppoSysStateManager.TAG, "CameraMonitor: screen on");
                }
                return;
            }
            int uid = OppoSysStateManager.this.getCameraWorkingUid();
            if (OppoSysStateManager.this.DEBUG) {
                Slog.d(OppoSysStateManager.TAG, "CameraMonitor: uid=" + uid);
            }
            if (uid >= 0) {
                ArrayList<String> uidList = new ArrayList();
                String[] packages = OppoSysStateManager.this.getPackagesForUid(uid);
                for (String pkg : packages) {
                    uidList.add("[ " + pkg + " ]    ");
                    if (OppoSysStateManager.this.DEBUG) {
                        Slog.d(OppoSysStateManager.TAG, "CameraMonitor: pkg=" + pkg);
                    }
                }
                Intent intent = new Intent(OppoSysStateManager.ACTION_OPPO_GUARD_ELF_MONITOR_FORCESTOP);
                intent.putStringArrayListExtra("data", uidList);
                intent.putExtra(SoundModelContract.KEY_TYPE, "camera");
                OppoSysStateManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        }
    };
    volatile boolean mCameraAlarmSetted = false;
    private Context mContext;
    private Handler mHandler;
    volatile boolean mIsCharging = false;
    volatile boolean mIsScreenOn = true;
    SparseArray<Integer> mListCameraOn = new SparseArray();
    ArrayList<String> mListNotRestrict = new ArrayList();
    List<String> mListOppoGuardElfNavigation = new ArrayList();
    List<String> mListOppoGuardElfPlayback = new ArrayList();
    List<String> mListOppoGuardElfWhitelist = new ArrayList();
    volatile boolean mListSensorInited = false;
    SparseArray<Integer> mListSensorType = new SparseArray();
    private IPackageManager mPkm;
    BroadcastReceiver mProcessResultReceiver = new BroadcastReceiver() {
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
        }
    };
    private boolean mRcvInited = false;
    volatile boolean mRestrictStartupBg = false;
    SparseArray<SparseArray<SensorStatistics>> mSensorList = new SparseArray();

    private final class LocalService extends OppoSysStateManagerInternal {
        /* synthetic */ LocalService(OppoSysStateManager this$0, LocalService -this1) {
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
                if (isScreenOn) {
                    OppoSysStateManager.this.cancelCameraMonitor();
                    return;
                }
                synchronized (OppoSysStateManager.this.mListNotRestrict) {
                    OppoSysStateManager.this.mListNotRestrict.clear();
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

        public void noteStartSensor(int uid, int handle) {
            OppoSysStateManager.this.getSensor(uid, OppoSysStateManager.this.sensorHandleToType(handle)).start();
        }

        public void noteStopSensor(int uid, int handle) {
            OppoSysStateManager.this.getSensor(uid, OppoSysStateManager.this.sensorHandleToType(handle)).stop();
        }

        public void noteCameraOnOff(int uid, boolean on) {
            synchronized (OppoSysStateManager.this.mListCameraOn) {
                int nesting = 0;
                if (OppoSysStateManager.this.mListCameraOn.get(uid) != null) {
                    nesting = ((Integer) OppoSysStateManager.this.mListCameraOn.get(uid)).intValue();
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

        public void noteCameraReset() {
            synchronized (OppoSysStateManager.this.mListCameraOn) {
                OppoSysStateManager.this.mListCameraOn.clear();
            }
        }
    }

    private class SensorStatistics {
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
            if (this.mNesting != 0) {
                this.mNesting--;
            }
        }

        public boolean isWorking() {
            return this.mNesting > 0;
        }
    }

    private OppoSysStateManager() {
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
        return this.mIsScreenOn ^ 1;
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
            this.mContext = context;
            this.mHandler = handler;
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

    public List<String> getOppoGuardElfWhiteList() {
        synchronized (this.mListOppoGuardElfWhitelist) {
            if (this.mListOppoGuardElfWhitelist.isEmpty()) {
                return null;
            }
            List arrayList = new ArrayList(this.mListOppoGuardElfWhitelist);
            return arrayList;
        }
    }

    public List<String> getOppoGuardElfPlayback() {
        synchronized (this.mListOppoGuardElfPlayback) {
            if (this.mListOppoGuardElfPlayback.isEmpty()) {
                return null;
            }
            List arrayList = new ArrayList(this.mListOppoGuardElfPlayback);
            return arrayList;
        }
    }

    public List<String> getOppoGuardElfNavigation() {
        synchronized (this.mListOppoGuardElfNavigation) {
            if (this.mListOppoGuardElfNavigation.isEmpty()) {
                return null;
            }
            List arrayList = new ArrayList(this.mListOppoGuardElfNavigation);
            return arrayList;
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
            SparseArray<SensorStatistics> sensorPerUid = (SparseArray) this.mSensorList.get(uid);
            if (sensorPerUid == null) {
                return false;
            }
            SensorStatistics sensor = (SensorStatistics) sensorPerUid.get(type);
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
            SparseArray<SensorStatistics> sensorPerUid = (SparseArray) this.mSensorList.get(uid);
            if (sensorPerUid == null) {
                return false;
            }
            int count = sensorPerUid.size();
            for (int i = 0; i < count; i++) {
                if (((SensorStatistics) sensorPerUid.valueAt(i)).isWorking()) {
                    isWorking = true;
                    break;
                }
            }
        }
    }

    public boolean isSensorUsedEver(int uid, int type) {
        synchronized (this.mSensorList) {
            SparseArray<SensorStatistics> sensorPerUid = (SparseArray) this.mSensorList.get(uid);
            if (sensorPerUid == null) {
                return false;
            } else if (((SensorStatistics) sensorPerUid.get(type)) == null) {
                return false;
            } else {
                return true;
            }
        }
    }

    private SensorStatistics getSensor(int uid, int type) {
        SensorStatistics sensor;
        synchronized (this.mSensorList) {
            SparseArray<SensorStatistics> sensorPerUid = (SparseArray) this.mSensorList.get(uid);
            if (sensorPerUid == null) {
                sensorPerUid = new SparseArray();
                this.mSensorList.put(uid, sensorPerUid);
            }
            sensor = (SensorStatistics) sensorPerUid.get(type);
            if (sensor == null) {
                sensor = new SensorStatistics(uid, type);
                sensorPerUid.put(type, sensor);
            }
        }
        return sensor;
    }

    private boolean isCameraWorking() {
        boolean z;
        synchronized (this.mListCameraOn) {
            int cnt = 0;
            for (int i = 0; i < this.mListCameraOn.size(); i++) {
                if (((Integer) this.mListCameraOn.valueAt(i)).intValue() > 0) {
                    cnt++;
                }
            }
            z = cnt == 1;
        }
        return z;
    }

    public int getCameraWorkingUid() {
        synchronized (this.mListCameraOn) {
            int uid = -1;
            int cnt = 0;
            for (int i = 0; i < this.mListCameraOn.size(); i++) {
                if (((Integer) this.mListCameraOn.valueAt(i)).intValue() > 0) {
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

    private void scheduleCameraMonitor() {
        if (initAlarmManager() && (this.mCameraAlarmSetted ^ 1) != 0 && this.mHandler != null) {
            this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + 300000, "CameraMonitor", this.mCameraAlarmListener, this.mHandler);
            this.mCameraAlarmSetted = true;
        }
    }

    private void cancelCameraMonitor() {
        if (this.mCameraAlarmSetted && initAlarmManager()) {
            this.mAlarmManager.cancel(this.mCameraAlarmListener);
            this.mCameraAlarmSetted = false;
        }
    }

    private boolean initAlarmManager() {
        if (this.mContext != null && this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        }
        return this.mAlarmManager != null;
    }

    private String[] getPackagesForUid(int uid) {
        String[] packages = null;
        if (!initIPackageManager()) {
            return packages;
        }
        try {
            packages = this.mPkm.getPackagesForUid(uid);
        } catch (Exception e) {
            Slog.d(TAG, "getPackagesForUid exception");
        }
        return packages;
    }

    private boolean initIPackageManager() {
        if (this.mContext != null && this.mPkm == null) {
            this.mPkm = Stub.asInterface(ServiceManager.getService("package"));
        }
        return this.mPkm != null;
    }

    private boolean initSensorTypeList() {
        if (this.mListSensorInited) {
            return true;
        }
        if (this.mContext == null) {
            Slog.d(TAG, "initSensorTypeList: mContext is null.");
            return false;
        }
        SensorManager sm = (SensorManager) this.mContext.getSystemService("sensor");
        if (sm == null) {
            Slog.d(TAG, "initSensorTypeList: SensorManager is null.");
            return false;
        }
        List<Sensor> listAllSensor = sm.getSensorList(-1);
        synchronized (this.mListSensorType) {
            for (int k = 0; k < listAllSensor.size(); k++) {
                Sensor sensor = (Sensor) listAllSensor.get(k);
                this.mListSensorType.put(sensor.getHandle(), Integer.valueOf(sensor.getType()));
            }
        }
        this.mListSensorInited = true;
        return this.mListSensorInited;
    }

    private int sensorHandleToType(int handle) {
        if (!initSensorTypeList()) {
            return 0;
        }
        int intValue;
        synchronized (this.mListSensorType) {
            intValue = ((Integer) this.mListSensorType.get(handle)).intValue();
        }
        return intValue;
    }
}
