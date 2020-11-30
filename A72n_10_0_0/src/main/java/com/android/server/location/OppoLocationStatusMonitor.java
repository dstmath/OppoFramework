package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.location.interfaces.IPswLocationStatusMonitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import oppo.util.OppoStatistics;

public class OppoLocationStatusMonitor implements IPswLocationStatusMonitor {
    private static final String ACTION_DATE_CHANGED = "android.intent.action.DATE_CHANGED";
    private static final String APP_GET_GNSS_POSITION_ERROR = "getGnssPositionError";
    private static final int APP_GET_GNSS_POSITION_ERROR_ID = 1;
    private static final int FLAG_BG_LOCATION_PERMISSION_NOT_GRANTED = 2;
    private static final int FLAG_LOCATION_PERMISSION_NOT_GRANTED = 1;
    private static final int FLAG_LOCATION_REPORT_INTERVAL_CHANGED = 16;
    private static final int FLAG_LOCATION_REQUEST_BLOCKED = 8;
    private static final int FLAG_LOCATION_SERVICE_DISALBED = 4;
    private static final int FLAG_LOST_SATE_BIGGER_5_SEC = 64;
    private static final int FLAG_NAVIGATION_TIME_OUT = 128;
    private static final int FLAG_TTFF_BIGGER_5_SEC = 32;
    private static final String FW_VERSION = "1.1.0";
    private static final int GNSS_CLOSED = 16;
    private static final int GNSS_FIXED = 4;
    private static final int GNSS_LOST_SV = 8;
    private static final int GNSS_LOST_SV_NO_FIX = 4;
    private static final int GNSS_LOST_SV_TIMEOUT = 2;
    private static final int GNSS_NOT_FIX = 2;
    private static final int GNSS_NO_FIX = 3;
    private static final int GNSS_NO_STATUS = 0;
    private static final int GNSS_SCENE_INDOOR_STATUS = 1;
    private static final int GNSS_SCENE_OUTDOOR_STATUS = 2;
    private static final int GNSS_SCENE_UNKNOW_STATUS = 0;
    private static final int GNSS_STARTED = 1;
    private static final int GNSS_TIMEOUT_FIX = 32;
    private static final int GNSS_TTFF_TIMEOUT = 1;
    private static final int GNSS_TTFF_TIMEOUT_FIX = 5;
    private static final int GPS_LOST_POSITION_THROULD = 6000;
    private static final int GPS_MAX_INTERNAL_TIME = 2000;
    private static final int GPS_WARNING_TTFF_THROULD = 5000;
    private static final String KEY_LOCATION_ST_CONFIG = "config_locationStatisticsConfig";
    private static final String KEY_LOCATION_ST_ENABLED = "config_locationStatisticsEnabled";
    private static final String LOGTAG_GPSLOCATION = "30101";
    private static final int MAP_NAVIGATION_TIMEOUT = 9;
    private static final int MSG_BD_REPORT_NUMBER_TIMER = 8;
    private static final int MSG_REPORT_BLOCK_APP = 7;
    private static final int MSG_UPLOAD_DATA_TO_SERVER = 101;
    private static final String OPPO_LBS_CONFIG_UPDATE_ACTION = "com.android.location.oppo.lbsconfig.update.success";
    private static final int REMOVE_ALL_LISTENER = 6;
    private static final int REPORT_BLOCK_EXCEPTION_TIME_INTERAL = 3600000;
    private static final String TAG = "OppoLocationStatusMonitor";
    private static final int TOP_SV_CN0 = 4;
    @GuardedBy({"mLock"})
    private String mBlockedPakcageNames;
    private final BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private GnssMonitorHandler mGnssMonitorHandler;
    private GnssStatisticInfo mGnssStatisticInfo;
    private GnssStatusMonitorListener mGnssStatusMonitorListener;
    private AtomicInteger mGnssWorkStatus;
    private boolean mIsDebug;
    private boolean mIsLocationStEnabled;
    private long mLastFixTime;
    private long mLastRecordStartTime;
    private LocationManager mLocationManager;
    private int mLocationStConfig;
    private final Object mLock;
    private MapNavigationInfo mMapNavigationInfo;
    private StableSpaceStack mMaxCn0;
    private AtomicBoolean mMonitorWorkFlag;
    private AtomicLong mNoPositionTime;
    private PositionListener mPositionListener;
    private RecordGnssSession mRecordGnssSession;
    private OppoLbsRomUpdateUtil mRomUpdateUtil;
    private long mStartTime;
    private int mSvHavAlm;
    private int mSvInUse;
    private int mSvSum;
    private StableSpaceStack mTopFourCn0;

    private OppoLocationStatusMonitor() {
        this.mLock = new Object();
        this.mContext = null;
        this.mLocationManager = null;
        this.mPositionListener = null;
        this.mGnssStatusMonitorListener = null;
        this.mMonitorWorkFlag = new AtomicBoolean(false);
        this.mGnssWorkStatus = new AtomicInteger(0);
        this.mStartTime = 0;
        this.mLastFixTime = 0;
        this.mGnssStatisticInfo = null;
        this.mNoPositionTime = new AtomicLong(0);
        this.mGnssMonitorHandler = null;
        this.mRecordGnssSession = null;
        this.mBlockedPakcageNames = StringUtils.EMPTY;
        this.mMapNavigationInfo = null;
        this.mRomUpdateUtil = null;
        this.mIsLocationStEnabled = true;
        this.mLocationStConfig = 0;
        this.mIsDebug = Log.isLoggable(TAG, 3);
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.location.OppoLocationStatusMonitor.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    if (action.equals(OppoLocationStatusMonitor.ACTION_DATE_CHANGED)) {
                        if (OppoLocationStatusMonitor.this.mBlockedPakcageNames != null || !StringUtils.EMPTY.equals(OppoLocationStatusMonitor.this.mBlockedPakcageNames)) {
                            OppoLocationStatusMonitor.this.mGnssMonitorHandler.sendEmptyMessage(OppoLocationStatusMonitor.MSG_REPORT_BLOCK_APP);
                        }
                    } else if (action.equals(OppoLocationStatusMonitor.OPPO_LBS_CONFIG_UPDATE_ACTION)) {
                        OppoLocationStatusMonitor.this.updateRusData();
                    }
                }
            }
        };
    }

    private static class GenerateSingletonInstance {
        private static final OppoLocationStatusMonitor INSTANCE = new OppoLocationStatusMonitor();

        private GenerateSingletonInstance() {
        }
    }

    public static OppoLocationStatusMonitor getInstance() {
        return GenerateSingletonInstance.INSTANCE;
    }

    public void init(Context context) {
        if (this.mContext == null) {
            this.mContext = context;
            this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
            this.mGnssStatusMonitorListener = new GnssStatusMonitorListener();
            this.mPositionListener = new PositionListener();
            this.mGnssStatisticInfo = new GnssStatisticInfo();
            this.mMaxCn0 = new StableSpaceStack(20);
            this.mTopFourCn0 = new StableSpaceStack(20);
            this.mRecordGnssSession = new RecordGnssSession();
            HandlerThread localThread = new HandlerThread(TAG);
            localThread.start();
            this.mGnssMonitorHandler = new GnssMonitorHandler(localThread.getLooper());
            this.mMapNavigationInfo = new MapNavigationInfo(localThread.getLooper());
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_DATE_CHANGED);
            this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, null);
            updateRusData();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRusData() {
        if (this.mRomUpdateUtil == null) {
            this.mRomUpdateUtil = OppoLbsRomUpdateUtil.getInstall(this.mContext);
        }
        this.mIsLocationStEnabled = this.mRomUpdateUtil.getBoolean(KEY_LOCATION_ST_ENABLED);
        if (this.mIsLocationStEnabled) {
            this.mLocationStConfig = this.mRomUpdateUtil.getInt(KEY_LOCATION_ST_CONFIG);
        } else {
            this.mLocationStConfig = 0;
        }
        if (!this.mIsLocationStEnabled && this.mMonitorWorkFlag.get()) {
            this.mGnssMonitorHandler.sendEmptyMessage(REMOVE_ALL_LISTENER);
        }
    }

    public void startRecordMonitor() {
        if (!this.mIsLocationStEnabled) {
            logd("location st is disabled, forbided rejister!!");
        } else if (this.mMonitorWorkFlag.get() || this.mLocationManager == null || this.mGnssStatusMonitorListener == null || this.mPositionListener == null) {
            logd("failed to registe monitor!!");
        } else {
            this.mMonitorWorkFlag.set(true);
            this.mLocationManager.requestLocationUpdates("passive", 0, 0.0f, this.mPositionListener);
            this.mLocationManager.registerGnssStatusCallback(this.mGnssStatusMonitorListener);
            resetGlobalValueLocked();
            this.mGnssWorkStatus.set(1);
            logd("register listener sucess");
            MapNavigationInfo mapNavigationInfo = this.mMapNavigationInfo;
            if (mapNavigationInfo != null) {
                mapNavigationInfo.startMapNavigationMonitor();
            }
        }
    }

    public void stopRecordMonitor() {
        if (this.mMonitorWorkFlag.get() && this.mLocationManager != null && this.mGnssStatusMonitorListener != null && this.mPositionListener != null) {
            this.mMonitorWorkFlag.set(false);
            this.mLocationManager.removeUpdates(this.mPositionListener);
            this.mLocationManager.unregisterGnssStatusCallback(this.mGnssStatusMonitorListener);
            Log.v(TAG, "remove listener sucess");
            this.mGnssMonitorHandler.removeMessages(8);
        }
    }

    public void recordPermissionNotGranted(int allowedResolutionLevel, int requiredResolutionLevel, String extraString) {
        if ((this.mLocationStConfig & 1) == 0) {
            loge("location permission st is disabled!!");
        } else if (allowedResolutionLevel < requiredResolutionLevel) {
            reportIssueLocked(1, extraString);
        }
    }

    public void recordBgPermissionDisable(String packageName) {
        if ((this.mLocationStConfig & 2) == 0) {
            loge("bg permission st is disabled!!");
        } else if (packageName == null) {
            loge("location disable params is null!");
        } else if (!"android".equals(packageName)) {
            reportIssueLocked(2, packageName);
        }
    }

    public void recordLocationDisable(String provider, String packageName) {
        if ((this.mLocationStConfig & 4) == 0) {
            loge("location st is disabled!!");
        } else if (provider == null || packageName == null) {
            loge("location disable params is null!");
        } else if ("gps".equals(provider) && !"android".equals(packageName)) {
            reportIssueLocked(4, packageName);
        }
    }

    public void recordLocationBlocked(String packageName) {
        if ((this.mLocationStConfig & 8) == 0) {
            loge("blocked app location st is disabled!!");
        } else if (packageName == null) {
            loge("join a null apckage!");
        } else {
            String str = this.mBlockedPakcageNames;
            if (str == null || StringUtils.EMPTY.equals(str)) {
                this.mBlockedPakcageNames = packageName;
            } else if (!this.mBlockedPakcageNames.contains(packageName)) {
                this.mBlockedPakcageNames += ", " + packageName;
            }
        }
    }

    public void recordLocationIntervalChanged(String packageName) {
        if ((this.mLocationStConfig & 16) == 0) {
            loge("location frequency st is disabled!!");
        } else if (packageName == null) {
            loge("location fre a null apckage!");
        } else if (!"android".equals(packageName)) {
            reportIssueLocked(16, packageName);
        }
    }

    public void setGpsBackgroundFlag(String packageName, boolean flag) {
        if (!this.mIsLocationStEnabled) {
            loge("location bg st is disabled!!");
            return;
        }
        if (this.mRecordGnssSession == null) {
            this.mRecordGnssSession = new RecordGnssSession();
        }
        this.mRecordGnssSession.updateForeground(packageName, "gps", flag);
    }

    public void startRequesting(String packageName, String providerName) {
        if (!this.mIsLocationStEnabled) {
            loge("location bg st is disabled!!");
            return;
        }
        if (this.mRecordGnssSession == null) {
            this.mRecordGnssSession = new RecordGnssSession();
        }
        synchronized (this.mLock) {
            this.mRecordGnssSession.updateRecordPackages(providerName, packageName, true);
        }
    }

    public void stopRequesting(String packageName, String providerName) {
        if (!this.mIsLocationStEnabled) {
            loge("location bg st is disabled!!");
            return;
        }
        if (this.mRecordGnssSession == null) {
            this.mRecordGnssSession = new RecordGnssSession();
        }
        synchronized (this.mLock) {
            this.mRecordGnssSession.updateRecordPackages(providerName, packageName, false);
        }
    }

    public void updateForeground(String packageName, String providerName, boolean isForeground) {
        if (!this.mIsLocationStEnabled) {
            loge("location bg st is disabled!!");
            return;
        }
        if (this.mRecordGnssSession == null) {
            this.mRecordGnssSession = new RecordGnssSession();
        }
        this.mRecordGnssSession.updateForeground(packageName, providerName, isForeground);
    }

    public void sendExtraCommand(String providerName, String command, Bundle extras) {
        MapNavigationInfo mapNavigationInfo = this.mMapNavigationInfo;
        if (mapNavigationInfo != null) {
            mapNavigationInfo.sendExtraCommand(providerName, command, extras);
        }
    }

    public void checkLocationHasChanged(String provider, String packageName, int hashCode) {
        MapNavigationInfo mapNavigationInfo = this.mMapNavigationInfo;
        if (mapNavigationInfo != null) {
            mapNavigationInfo.checkLocationHasChanged(provider, packageName, hashCode);
        }
    }

    public int generateStatusChangedExtra(String provider, String packageName, Bundle extras, int status) {
        MapNavigationInfo mapNavigationInfo = this.mMapNavigationInfo;
        if (mapNavigationInfo != null) {
            return mapNavigationInfo.generateStatusChangedExtra(provider, packageName, extras, status);
        }
        return status;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private boolean reportIssueLocked(int exceptionType, String extraString) {
        logd("start :  issueType : " + exceptionType + " extraString : " + extraString);
        if (exceptionType <= 0 || extraString == null) {
            logd("Gee data is null!!");
            return false;
        }
        HashMap<String, String> updateMapData = new HashMap<>();
        updateMapData.put("utcTime", Long.toString(System.currentTimeMillis()));
        updateMapData.put("extraString", extraString);
        updateMapData.put("exceptionType", StringUtils.EMPTY + exceptionType);
        this.mGnssMonitorHandler.obtainMessage(MSG_UPLOAD_DATA_TO_SERVER, 1, 0, updateMapData).sendToTarget();
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void uploadDataToServer(int eventId, HashMap<String, String> updateData) {
        String event = null;
        if (1 == eventId) {
            event = APP_GET_GNSS_POSITION_ERROR;
        }
        if (event != null && updateData != null) {
            try {
                OppoStatistics.onCommon(this.mContext, "30101", event, updateData, false);
            } catch (Exception e) {
                loge("Catch an exception when common!!");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void resetGlobalValueLocked() {
        this.mSvSum = 0;
        this.mSvHavAlm = 0;
        this.mSvInUse = 0;
        this.mNoPositionTime.set(0);
        this.mLastFixTime = 0;
        this.mMaxCn0.clear();
        this.mTopFourCn0.clear();
    }

    /* access modifiers changed from: private */
    public class GnssStatusMonitorListener extends GnssStatus.Callback {
        private GnssStatusMonitorListener() {
        }

        public void onStarted() {
            OppoLocationStatusMonitor.this.mStartTime = System.currentTimeMillis();
            OppoLocationStatusMonitor.this.mGnssWorkStatus.set(2);
        }

        public void onStopped() {
            long intervalTime = System.currentTimeMillis() - OppoLocationStatusMonitor.this.mStartTime;
            if (2 == OppoLocationStatusMonitor.this.mGnssWorkStatus.get() && intervalTime > 5000) {
                OppoLocationStatusMonitor.this.mNoPositionTime.set(intervalTime);
                OppoLocationStatusMonitor.this.mGnssMonitorHandler.sendEmptyMessage(3);
            }
            long intervalTime2 = System.currentTimeMillis() - OppoLocationStatusMonitor.this.mLastFixTime;
            if (intervalTime2 > 6000 && OppoLocationStatusMonitor.this.mGnssWorkStatus.get() == 8) {
                OppoLocationStatusMonitor.this.mNoPositionTime.set(intervalTime2);
                OppoLocationStatusMonitor.this.mGnssMonitorHandler.sendEmptyMessage(4);
            }
            OppoLocationStatusMonitor.this.mGnssWorkStatus.set(16);
            OppoLocationStatusMonitor.this.mGnssMonitorHandler.sendEmptyMessage(OppoLocationStatusMonitor.REMOVE_ALL_LISTENER);
            OppoLocationStatusMonitor.this.mGnssMonitorHandler.removeMessages(8);
        }

        public void onFirstFix(int ttffMillis) {
            OppoLocationStatusMonitor.this.mNoPositionTime.set((long) ttffMillis);
            OppoLocationStatusMonitor oppoLocationStatusMonitor = OppoLocationStatusMonitor.this;
            oppoLocationStatusMonitor.logd("the first fixed is " + ttffMillis);
        }

        public void onSatelliteStatusChanged(GnssStatus status) {
            long currentTime = System.currentTimeMillis();
            recordSvCn0Locked(status);
            int i = OppoLocationStatusMonitor.this.mGnssWorkStatus.get();
            if (i == 2) {
                OppoLocationStatusMonitor.this.logd("onSatelliteStatusChanged -- GNSS_NOT_FIX");
                OppoLocationStatusMonitor.this.mNoPositionTime.set(currentTime - OppoLocationStatusMonitor.this.mStartTime);
                if (OppoLocationStatusMonitor.this.mNoPositionTime.get() > 5000) {
                    OppoLocationStatusMonitor.this.mGnssMonitorHandler.sendEmptyMessage(1);
                }
            } else if (i == 8) {
                OppoLocationStatusMonitor.this.logd("onSatelliteStatusChanged -- GNSS_LOST_SV");
                OppoLocationStatusMonitor.this.mNoPositionTime.set(currentTime - OppoLocationStatusMonitor.this.mLastFixTime);
                if (OppoLocationStatusMonitor.this.mNoPositionTime.get() > 6000) {
                    OppoLocationStatusMonitor.this.mGnssMonitorHandler.sendEmptyMessage(2);
                }
            } else if (i == 32) {
                OppoLocationStatusMonitor oppoLocationStatusMonitor = OppoLocationStatusMonitor.this;
                oppoLocationStatusMonitor.logd("mPositionTime = " + OppoLocationStatusMonitor.this.mNoPositionTime.get());
                OppoLocationStatusMonitor.this.mGnssMonitorHandler.sendEmptyMessage(OppoLocationStatusMonitor.GNSS_TTFF_TIMEOUT_FIX);
            }
            if (OppoLocationStatusMonitor.this.mLastFixTime != 0 && System.currentTimeMillis() - OppoLocationStatusMonitor.this.mLastFixTime > 2000 && OppoLocationStatusMonitor.this.mGnssWorkStatus.get() == 4) {
                OppoLocationStatusMonitor.this.mGnssWorkStatus.set(8);
                OppoLocationStatusMonitor.this.mLastFixTime = System.currentTimeMillis();
            }
        }

        @GuardedBy({"mLock"})
        private void recordSvCn0Locked(GnssStatus status) {
            int workStatusFlag = OppoLocationStatusMonitor.this.mGnssWorkStatus.get();
            if (workStatusFlag == 8 || workStatusFlag == 2 || workStatusFlag == 32) {
                OppoLocationStatusMonitor.this.mSvSum = status.getSatelliteCount();
                OppoLocationStatusMonitor.this.mMaxCn0.addItem(getMaxCn0(status));
                OppoLocationStatusMonitor.this.mTopFourCn0.addItem(getTopFourCn0(status));
                OppoLocationStatusMonitor.this.mSvHavAlm = getHavAlmaSvSum(status);
            }
            OppoLocationStatusMonitor.this.mSvInUse = getSvInUseSum(status);
        }

        private float getMaxCn0(GnssStatus status) {
            float max = 0.0f;
            for (int i = 0; i < status.getSatelliteCount(); i++) {
                if (status.getCn0DbHz(i) > max) {
                    max = status.getCn0DbHz(i);
                }
            }
            return max;
        }

        private float getTopFourCn0(GnssStatus status) {
            ArrayList<Float> sortCn0 = new ArrayList<>();
            for (int i = 0; i < status.getSatelliteCount(); i++) {
                sortCn0.add(Float.valueOf(status.getCn0DbHz(i)));
            }
            Collections.sort(sortCn0);
            float averageCn0 = 0.0f;
            for (int i2 = 4; i2 > 0; i2--) {
                if (sortCn0.size() - i2 >= 0) {
                    averageCn0 += sortCn0.get(sortCn0.size() - i2).floatValue();
                }
            }
            int minSum = 4;
            if (4 >= sortCn0.size()) {
                minSum = sortCn0.size();
            }
            if (minSum != 0) {
                return averageCn0 / ((float) minSum);
            }
            return averageCn0;
        }

        private int getHavAlmaSvSum(GnssStatus status) {
            int sum = 0;
            for (int i = 0; i < status.getSatelliteCount(); i++) {
                if (((double) status.getAzimuthDegrees(i)) > 0.0d && ((double) status.getElevationDegrees(i)) > 0.0d) {
                    sum++;
                }
            }
            return sum;
        }

        private int getSvInUseSum(GnssStatus status) {
            int sum = 0;
            for (int i = 0; i < status.getSatelliteCount(); i++) {
                if (status.usedInFix(i)) {
                    sum++;
                }
            }
            return sum;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String logMsg) {
        if (this.mIsDebug) {
            Log.d(TAG, logMsg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String logMsg) {
        Log.e(TAG, logMsg);
    }

    /* access modifiers changed from: private */
    public class PositionListener implements LocationListener {
        private PositionListener() {
        }

        public void onLocationChanged(Location location) {
            if (location == null) {
                OppoLocationStatusMonitor.this.loge("location is null!!");
            } else if ("gps".equals(location.getProvider())) {
                long currentTime = System.currentTimeMillis();
                OppoLocationStatusMonitor.this.mGnssWorkStatus.set(4);
                if (0 != OppoLocationStatusMonitor.this.mLastFixTime && currentTime - OppoLocationStatusMonitor.this.mLastFixTime > 5000) {
                    OppoLocationStatusMonitor.this.mNoPositionTime.set(currentTime - OppoLocationStatusMonitor.this.mLastFixTime);
                }
                OppoLocationStatusMonitor.this.mLastFixTime = currentTime;
                if (OppoLocationStatusMonitor.this.mNoPositionTime.get() > 6000) {
                    OppoLocationStatusMonitor.this.mGnssWorkStatus.set(32);
                }
                if (OppoLocationStatusMonitor.this.mMapNavigationInfo != null) {
                    OppoLocationStatusMonitor.this.mMapNavigationInfo.setPositionTimer(currentTime);
                }
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            OppoLocationStatusMonitor oppoLocationStatusMonitor = OppoLocationStatusMonitor.this;
            oppoLocationStatusMonitor.logd(provider + "status change" + status);
        }

        public void onProviderEnabled(String provider) {
            OppoLocationStatusMonitor oppoLocationStatusMonitor = OppoLocationStatusMonitor.this;
            oppoLocationStatusMonitor.logd(provider + "enable");
        }

        public void onProviderDisabled(String provider) {
            OppoLocationStatusMonitor oppoLocationStatusMonitor = OppoLocationStatusMonitor.this;
            oppoLocationStatusMonitor.logd(provider + "disable");
        }
    }

    /* access modifiers changed from: private */
    public class GnssStatisticInfo {
        private static final long INTERVAL_UPDATE_DATA = 20000;
        private float averageTopFourthCn;
        private int errorType;
        private int inUseSVofFix;
        private int inViewSum;
        private HashMap<Integer, Long> mSendTime;
        private float maxCnDuringFix;
        private String packageName;
        private int svHaveAlmSum;
        private long ttff;

        private GnssStatisticInfo() {
            this.errorType = 0;
            this.ttff = 0;
            this.inViewSum = 0;
            this.inUseSVofFix = 0;
            this.svHaveAlmSum = 0;
            this.maxCnDuringFix = 0.0f;
            this.averageTopFourthCn = 0.0f;
            this.packageName = StringUtils.EMPTY;
            this.mSendTime = new HashMap<>();
        }

        public void setTtff(int ttff2) {
            this.ttff = (long) ttff2;
        }

        public void setInUseSVofFix(int inUseSVofFix2) {
            this.inUseSVofFix = inUseSVofFix2;
        }

        /* JADX WARNING: Removed duplicated region for block: B:22:0x0063  */
        /* JADX WARNING: Removed duplicated region for block: B:69:0x020f  */
        public void generateGnssStatisticInfo(int type) {
            boolean needSend;
            Date currentTime = new Date(System.currentTimeMillis());
            long utcTime = System.currentTimeMillis();
            if (1 == type || 2 == type) {
                if (this.mSendTime.containsKey(Integer.valueOf(type)) && utcTime - this.mSendTime.get(Integer.valueOf(type)).longValue() >= INTERVAL_UPDATE_DATA) {
                    needSend = true;
                    if (!needSend) {
                    }
                } else if (!this.mSendTime.containsKey(Integer.valueOf(type))) {
                    needSend = true;
                    if (!needSend) {
                        synchronized (OppoLocationStatusMonitor.this.mLock) {
                            try {
                                this.errorType = type;
                                this.ttff = OppoLocationStatusMonitor.this.mNoPositionTime.get();
                                this.inViewSum = OppoLocationStatusMonitor.this.mSvSum;
                                this.svHaveAlmSum = OppoLocationStatusMonitor.this.mSvHavAlm;
                                this.inUseSVofFix = OppoLocationStatusMonitor.this.mSvInUse;
                                this.maxCnDuringFix = OppoLocationStatusMonitor.this.mMaxCn0.getAvrage();
                                this.averageTopFourthCn = OppoLocationStatusMonitor.this.mTopFourCn0.getAvrage();
                                this.packageName = StringUtils.EMPTY;
                                HashMap<String, PackageStatistics> recordPackages = OppoLocationStatusMonitor.this.mRecordGnssSession.getRecordPackages();
                                boolean onlyBlockedApp = true;
                                for (String key : recordPackages.keySet()) {
                                    String requestTimeString = StringUtils.EMPTY;
                                    PackageStatistics stat = recordPackages.get(key);
                                    int i = stat.getRecordKey();
                                    while (i > 0) {
                                        try {
                                            Date tempStartTime = new Date(stat.getRequestStartTime(i));
                                            requestTimeString = requestTimeString + tempStartTime + "~" + new Date(stat.getRequestEndTime(i)) + "&";
                                            i--;
                                            needSend = needSend;
                                            utcTime = utcTime;
                                        } catch (Throwable th) {
                                            th = th;
                                            throw th;
                                        }
                                    }
                                    if (0 != stat.getCurrentStartTime() && 0 == stat.getCurrentEndTime()) {
                                        requestTimeString = requestTimeString + new Date(stat.getCurrentStartTime()) + "~" + currentTime;
                                    }
                                    if (!StringUtils.EMPTY.equals(requestTimeString) && requestTimeString.length() != 0) {
                                        if (this.packageName.equals(StringUtils.EMPTY)) {
                                            this.packageName = "[" + key + ":" + requestTimeString + "]";
                                        } else {
                                            this.packageName += "[" + key + ":" + requestTimeString + "]";
                                        }
                                    }
                                    if (stat.getAllowGetGpsBackgroundFlag()) {
                                        onlyBlockedApp = false;
                                    }
                                    needSend = needSend;
                                    utcTime = utcTime;
                                }
                                if (OppoLocationStatusMonitor.MAP_NAVIGATION_TIMEOUT == type) {
                                    OppoLocationStatusMonitor.this.logd(toString());
                                    OppoLocationStatusMonitor.this.reportIssueLocked(OppoLocationStatusMonitor.FLAG_NAVIGATION_TIME_OUT, toString());
                                } else if (!onlyBlockedApp) {
                                    OppoLocationStatusMonitor.this.logd(toString());
                                    OppoLocationStatusMonitor.this.reportIssueLocked(OppoLocationStatusMonitor.FLAG_LOST_SATE_BIGGER_5_SEC, toString());
                                }
                                this.mSendTime.put(Integer.valueOf(this.errorType), Long.valueOf(utcTime));
                                return;
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                    } else {
                        return;
                    }
                }
            } else if (3 == type || 4 == type || OppoLocationStatusMonitor.GNSS_TTFF_TIMEOUT_FIX == type || OppoLocationStatusMonitor.MAP_NAVIGATION_TIMEOUT == type) {
                needSend = true;
                if (!needSend) {
                }
            }
            needSend = false;
            if (!needSend) {
            }
        }

        public String toString() {
            String navigation;
            StringBuilder s = new StringBuilder();
            s.append("GnssStatisticInfo{");
            s.append("errorType=");
            s.append(this.errorType);
            s.append(",ttff=");
            s.append(this.ttff);
            s.append(",inViewSum=");
            s.append(this.inViewSum);
            s.append(",svHaveAlmSum=");
            s.append(this.svHaveAlmSum);
            s.append(",inUseSVofFix=");
            s.append(this.inUseSVofFix);
            s.append(",maxCnDuringFix=");
            s.append(this.maxCnDuringFix);
            s.append(",averageTopFourthCn=");
            s.append(this.averageTopFourthCn);
            s.append(",packageName=");
            s.append(this.packageName);
            if (!(OppoLocationStatusMonitor.this.mMapNavigationInfo == null || (navigation = OppoLocationStatusMonitor.this.mMapNavigationInfo.generateNavigationInfo()) == null)) {
                s.append(navigation);
            }
            s.append("}");
            return s.toString();
        }
    }

    /* access modifiers changed from: private */
    public class GnssMonitorHandler extends Handler {
        public GnssMonitorHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != OppoLocationStatusMonitor.MSG_UPLOAD_DATA_TO_SERVER) {
                switch (i) {
                    case 1:
                        if (OppoLocationStatusMonitor.this.mIsLocationStEnabled || (OppoLocationStatusMonitor.this.mLocationStConfig & 32) != 0) {
                            OppoLocationStatusMonitor.this.mGnssStatisticInfo.generateGnssStatisticInfo(1);
                        }
                        OppoLocationStatusMonitor.this.resetGlobalValueLocked();
                        return;
                    case 2:
                        if (OppoLocationStatusMonitor.this.mIsLocationStEnabled || (OppoLocationStatusMonitor.this.mLocationStConfig & OppoLocationStatusMonitor.FLAG_LOST_SATE_BIGGER_5_SEC) != 0) {
                            OppoLocationStatusMonitor.this.mGnssStatisticInfo.generateGnssStatisticInfo(2);
                        }
                        OppoLocationStatusMonitor.this.resetGlobalValueLocked();
                        return;
                    case 3:
                        if (OppoLocationStatusMonitor.this.mIsLocationStEnabled || (OppoLocationStatusMonitor.this.mLocationStConfig & 32) != 0) {
                            OppoLocationStatusMonitor.this.mGnssStatisticInfo.generateGnssStatisticInfo(3);
                        }
                        OppoLocationStatusMonitor.this.resetGlobalValueLocked();
                        return;
                    case 4:
                        if (OppoLocationStatusMonitor.this.mIsLocationStEnabled || (OppoLocationStatusMonitor.this.mLocationStConfig & OppoLocationStatusMonitor.FLAG_LOST_SATE_BIGGER_5_SEC) != 0) {
                            OppoLocationStatusMonitor.this.mGnssStatisticInfo.generateGnssStatisticInfo(4);
                        }
                        OppoLocationStatusMonitor.this.resetGlobalValueLocked();
                        return;
                    case OppoLocationStatusMonitor.GNSS_TTFF_TIMEOUT_FIX /* 5 */:
                        if (OppoLocationStatusMonitor.this.mIsLocationStEnabled || (OppoLocationStatusMonitor.this.mLocationStConfig & 32) != 0) {
                            OppoLocationStatusMonitor.this.mGnssStatisticInfo.generateGnssStatisticInfo(OppoLocationStatusMonitor.GNSS_TTFF_TIMEOUT_FIX);
                        }
                        OppoLocationStatusMonitor.this.resetGlobalValueLocked();
                        return;
                    case OppoLocationStatusMonitor.REMOVE_ALL_LISTENER /* 6 */:
                        OppoLocationStatusMonitor.this.stopRecordMonitor();
                        synchronized (OppoLocationStatusMonitor.this.mLock) {
                            OppoLocationStatusMonitor.this.mRecordGnssSession.clearRecordPackages();
                        }
                        OppoLocationStatusMonitor.this.resetGlobalValueLocked();
                        return;
                    case OppoLocationStatusMonitor.MSG_REPORT_BLOCK_APP /* 7 */:
                        OppoLocationStatusMonitor oppoLocationStatusMonitor = OppoLocationStatusMonitor.this;
                        oppoLocationStatusMonitor.reportIssueLocked(8, oppoLocationStatusMonitor.mBlockedPakcageNames);
                        OppoLocationStatusMonitor.this.mBlockedPakcageNames = StringUtils.EMPTY;
                        return;
                    default:
                        return;
                }
            } else {
                OppoLocationStatusMonitor.this.uploadDataToServer(msg.arg1, (HashMap) msg.obj);
            }
        }
    }

    /* access modifiers changed from: private */
    public class StableSpaceStack {
        private ArrayList<Float> mBaseArray = new ArrayList<>();
        private boolean mFullFlag = false;
        private int mSeek = 0;
        private int mSpace = 0;

        public StableSpaceStack(int space) {
            this.mSpace = space;
            this.mSeek = 0;
        }

        public void addItem(float item) {
            if (this.mFullFlag) {
                this.mSeek = 0;
            }
            int i = this.mSeek;
            if (i < this.mSpace && this.mFullFlag) {
                this.mBaseArray.remove(i);
                this.mBaseArray.set(this.mSeek, Float.valueOf(item));
                this.mSeek++;
            }
            if (this.mSeek < this.mSpace) {
                this.mBaseArray.add(Float.valueOf(item));
                this.mSeek++;
            }
            if (this.mSeek >= this.mSpace) {
                this.mFullFlag = true;
            }
        }

        public float getAvrage() {
            float maxCnDuringFix = 0.0f;
            for (int i = 0; i < this.mBaseArray.size(); i++) {
                maxCnDuringFix += this.mBaseArray.get(i).floatValue();
            }
            if (this.mBaseArray.size() != 0) {
                return maxCnDuringFix / ((float) this.mBaseArray.size());
            }
            return 0.0f;
        }

        public void clear() {
            this.mSeek = 0;
            this.mFullFlag = false;
            this.mBaseArray.clear();
        }
    }

    /* access modifiers changed from: private */
    public class RecordGnssSession {
        private HashMap<String, PackageStatistics> mGnssUsageMap = new HashMap<>();

        public RecordGnssSession() {
        }

        @GuardedBy({"mLock"})
        public void updateRecordPackages(String providerName, String packageName, boolean start) {
            if ("gps".equals(providerName)) {
                long currentTime = System.currentTimeMillis();
                if (start) {
                    if (this.mGnssUsageMap.containsKey(packageName)) {
                        PackageStatistics statisticsInfo = this.mGnssUsageMap.get(packageName);
                        if (0 == statisticsInfo.getCurrentStartTime() || 0 != statisticsInfo.getCurrentEndTime()) {
                            statisticsInfo.setCurrentStartTime(currentTime);
                        } else {
                            Log.v(OppoLocationStatusMonitor.TAG, "give up it, I have record it early");
                        }
                    } else {
                        PackageStatistics packageUseGnssTime = new PackageStatistics();
                        packageUseGnssTime.setCurrentStartTime(currentTime);
                        packageUseGnssTime.setPackageName(packageName);
                        this.mGnssUsageMap.put(packageName, packageUseGnssTime);
                    }
                } else if (!this.mGnssUsageMap.containsKey(packageName)) {
                    Log.e(OppoLocationStatusMonitor.TAG, "bad msg, I don't record " + packageName + "start time");
                } else {
                    PackageStatistics packageUseGnssTime2 = this.mGnssUsageMap.get(packageName);
                    packageUseGnssTime2.setCurrentEndTime(currentTime);
                    if (!packageUseGnssTime2.updateRequestHistoryTime()) {
                        Log.v(OppoLocationStatusMonitor.TAG, "this time: " + currentTime + "record fail");
                        packageUseGnssTime2.setCurrentStartTime(0);
                        packageUseGnssTime2.setCurrentEndTime(0);
                    }
                }
            }
        }

        @GuardedBy({"mLock"})
        public void clearRecordPackages() {
            ArrayList<String> readyRemovePackage = new ArrayList<>();
            for (String key : this.mGnssUsageMap.keySet()) {
                PackageStatistics tempPackageUseGnssTime = this.mGnssUsageMap.get(key);
                if (0 == tempPackageUseGnssTime.getCurrentStartTime() && 0 == tempPackageUseGnssTime.getCurrentEndTime()) {
                    Log.e(OppoLocationStatusMonitor.TAG, "will remove" + key);
                    readyRemovePackage.add(key);
                } else {
                    Log.e(OppoLocationStatusMonitor.TAG, "don't remove" + key);
                    tempPackageUseGnssTime.cleanGnssRequestHistoryMap();
                }
            }
            Iterator<String> it = readyRemovePackage.iterator();
            while (it.hasNext()) {
                String key2 = it.next();
                Log.e(OppoLocationStatusMonitor.TAG, "remove" + key2);
                this.mGnssUsageMap.remove(key2);
            }
        }

        @GuardedBy({"mLock"})
        public HashMap<String, PackageStatistics> getRecordPackages() {
            HashMap<String, PackageStatistics> recordPackageReqeustGnssTimeMap = new HashMap<>();
            for (String key : this.mGnssUsageMap.keySet()) {
                recordPackageReqeustGnssTimeMap.put(key, new PackageStatistics(this.mGnssUsageMap.get(key)));
            }
            return recordPackageReqeustGnssTimeMap;
        }

        @GuardedBy({"mLock"})
        public void updateForeground(String packageName, String providerName, boolean isForeground) {
            PackageStatistics stats;
            if ("gps".equals(providerName) && this.mGnssUsageMap.containsKey(packageName) && (stats = this.mGnssUsageMap.get(packageName)) != null) {
                stats.setAllowGetGpsBackgroundFlag(isForeground);
            }
        }

        @GuardedBy({"mLock"})
        public boolean getAllowGetGpsBackgroundFlag(String packageName) {
            if (this.mGnssUsageMap.containsKey(packageName)) {
                return this.mGnssUsageMap.get(packageName).getAllowGetGpsBackgroundFlag();
            }
            return true;
        }
    }

    public class PackageStatistics {
        private boolean allowGetGpsBackgroundFlag;
        private long currentEndTime;
        private long currentStartTime;
        private String packageName;
        private int recordKey;
        private HashMap<Integer, Long> useGnssEndTimeHistoryMap;
        private HashMap<Integer, Long> useGnssStartTimeHistoryMap;

        public PackageStatistics() {
            this.packageName = null;
            this.useGnssStartTimeHistoryMap = new HashMap<>();
            this.useGnssEndTimeHistoryMap = new HashMap<>();
            this.currentStartTime = 0;
            this.currentEndTime = 0;
            this.recordKey = 0;
            this.allowGetGpsBackgroundFlag = false;
            this.currentStartTime = 0;
            this.currentEndTime = 0;
            this.recordKey = 0;
            this.allowGetGpsBackgroundFlag = true;
        }

        public PackageStatistics(PackageStatistics oldPackageUseGnssTime) {
            this.packageName = null;
            this.useGnssStartTimeHistoryMap = new HashMap<>();
            this.useGnssEndTimeHistoryMap = new HashMap<>();
            this.currentStartTime = 0;
            this.currentEndTime = 0;
            this.recordKey = 0;
            this.allowGetGpsBackgroundFlag = false;
            this.packageName = oldPackageUseGnssTime.packageName;
            this.useGnssStartTimeHistoryMap.putAll(oldPackageUseGnssTime.useGnssStartTimeHistoryMap);
            this.useGnssEndTimeHistoryMap.putAll(oldPackageUseGnssTime.useGnssEndTimeHistoryMap);
            this.currentStartTime = oldPackageUseGnssTime.currentStartTime;
            this.currentEndTime = oldPackageUseGnssTime.currentEndTime;
            this.recordKey = oldPackageUseGnssTime.recordKey;
            this.allowGetGpsBackgroundFlag = oldPackageUseGnssTime.allowGetGpsBackgroundFlag;
        }

        public void setPackageName(String packageName2) {
            this.packageName = packageName2;
        }

        public String getPackageName() {
            return this.packageName;
        }

        public void setCurrentStartTime(long currentStartTime2) {
            this.currentStartTime = currentStartTime2;
        }

        public long getCurrentStartTime() {
            return this.currentStartTime;
        }

        public void setCurrentEndTime(long currentEndTime2) {
            this.currentEndTime = currentEndTime2;
        }

        public long getCurrentEndTime() {
            return this.currentEndTime;
        }

        private int generateRecordKey() {
            int i = this.recordKey + 1;
            this.recordKey = i;
            return i;
        }

        public int getRecordKey() {
            return this.recordKey;
        }

        public boolean updateRequestHistoryTime() {
            if (0 == this.currentStartTime || 0 == this.currentEndTime) {
                return false;
            }
            OppoLocationStatusMonitor.this.logd("Will update the history time");
            int mainKey = generateRecordKey();
            this.useGnssStartTimeHistoryMap.put(Integer.valueOf(mainKey), Long.valueOf(this.currentStartTime));
            this.useGnssEndTimeHistoryMap.put(Integer.valueOf(mainKey), Long.valueOf(this.currentEndTime));
            this.currentStartTime = 0;
            this.currentEndTime = 0;
            return true;
        }

        public long getRequestStartTime(int key) {
            HashMap<Integer, Long> hashMap = this.useGnssStartTimeHistoryMap;
            if (hashMap == null || !hashMap.containsKey(Integer.valueOf(key))) {
                return 0;
            }
            return this.useGnssStartTimeHistoryMap.get(Integer.valueOf(key)).longValue();
        }

        public long getRequestEndTime(int key) {
            HashMap<Integer, Long> hashMap = this.useGnssEndTimeHistoryMap;
            if (hashMap == null || !hashMap.containsKey(Integer.valueOf(key))) {
                return 0;
            }
            return this.useGnssEndTimeHistoryMap.get(Integer.valueOf(key)).longValue();
        }

        public void cleanGnssRequestHistoryMap() {
            this.useGnssStartTimeHistoryMap.clear();
            this.useGnssEndTimeHistoryMap.clear();
            this.recordKey = 0;
        }

        public void setAllowGetGpsBackgroundFlag(boolean flag) {
            this.allowGetGpsBackgroundFlag = flag;
        }

        public boolean getAllowGetGpsBackgroundFlag() {
            return this.allowGetGpsBackgroundFlag;
        }
    }

    /* access modifiers changed from: private */
    public class MapNavigationInfo extends RecordGnssSession {
        private static final int AMAP_ERROR_STATUS_CODE = 100001;
        private static final String AMAP_PACKAGE_NAME = "com.androits.gps.test.test";
        private static final long AMAP_REPORT_TIMEOUT = 2000;
        private static final String BD_PACKAGE_NAME = "com.baidu.BaiduMap";
        private static final int BD_REPORT_NUMBER_TIME_INTERAL = 1000;
        private static final String COMMAND_AMAP_INIT = "send_amap_init";
        private static final String COMMAND_GET_GPS_DATA = "get_gps_data";
        private static final String COMMAND_GPS_TIMEOUT = "send_gps_timeout";
        private static final String COMMAND_SCENE_INDOOR = "send_scene_indoor";
        private static final String COMMAND_SCENE_OUTDOOR = "send_scene_outdoor";
        private static final String COMMAND_SCENE_UNKNOW = "send_scene_unknow";
        private static final String COMMAND_START_NAVI = "send_navi_start";
        private static final String COMMAND_STOP_NAVI = "send_navi_stop";
        private static final String KEY_ADIU = "adiu";
        private static final String KEY_CHIP_OK = "chip_ok";
        private static final String KEY_CONTROL_OK = "control_ok";
        private static final String KEY_CUID = "cuid";
        private static final String KEY_IMEI = "imei";
        private static final String KEY_INTERFACE_OK = "interface_ok";
        private static final String KEY_LISTENER_HASH_CODE = "listenerHashcode";
        private static final String KEY_POSITION_NUMBER_5S = "position_ok_number_5s";
        private static final String KEY_REPORT_NUMBER_5S = "report_number_5s";
        private static final String KEY_SATELLITE_USED = "satellite_used";
        private static final String KEY_SYSTEM_OK = "system_ok";
        private static final String KEY_USED_NUM = "satellite_used";
        private static final String KEY_UUID = "uuid";
        private static final long MAX_TIME_FOR_REPORT_TIMEOUT = 1200;
        private String mAmapAdiu = null;
        private int mAmapListenerHashCode = -1;
        private AtomicLong mAmapLocationTime = new AtomicLong(0);
        @GuardedBy({"mLock"})
        private HashMap<String, PackageStatistics> mAmapNavigationStatistics = new HashMap<>();
        private String mAmapUuid = null;
        private String mBdCuid = null;
        private AtomicInteger mBdPositionNumber5s = new AtomicInteger(0);
        private AtomicInteger mBdReportNumber5s = new AtomicInteger(0);
        private AtomicLong mBdReportTime = new AtomicLong(0);
        private MapNavigationHandler mMapHandler = null;
        private AtomicInteger mMapNavigationStatus = new AtomicInteger(0);
        private AtomicLong mPositionTime = new AtomicLong(0);
        private AtomicInteger mSceneStatus = new AtomicInteger(0);

        public MapNavigationInfo(Looper loop) {
            super();
            this.mMapHandler = new MapNavigationHandler(loop);
        }

        public void checkLocationHasChanged(String provider, String packageName, int hashCode) {
            if ((OppoLocationStatusMonitor.this.mLocationStConfig & OppoLocationStatusMonitor.FLAG_NAVIGATION_TIME_OUT) == 0) {
                return;
            }
            if ("gps".equals(provider) && AMAP_PACKAGE_NAME.equals(packageName)) {
                int i = this.mAmapListenerHashCode;
                if (i <= 0) {
                    OppoLocationStatusMonitor.this.logd(StringUtils.EMPTY);
                } else if (hashCode == i) {
                    this.mAmapLocationTime.set(System.currentTimeMillis());
                }
            } else if ("gps".equals(provider) && BD_PACKAGE_NAME.equals(packageName)) {
                this.mBdReportTime.set(System.currentTimeMillis());
                OppoLocationStatusMonitor oppoLocationStatusMonitor = OppoLocationStatusMonitor.this;
                oppoLocationStatusMonitor.logd("baidu time --> " + this.mBdReportTime.get());
            }
        }

        public int generateStatusChangedExtra(String provider, String packageName, Bundle extras, int status) {
            String str;
            if ((OppoLocationStatusMonitor.this.mLocationStConfig & OppoLocationStatusMonitor.FLAG_NAVIGATION_TIME_OUT) == 0 || !AMAP_PACKAGE_NAME.equals(packageName) || (str = this.mAmapUuid) == null) {
                return status;
            }
            extras.putString(KEY_UUID, str);
            extras.putInt("satellite_used", OppoLocationStatusMonitor.this.mSvInUse);
            if (AMAP_REPORT_TIMEOUT < System.currentTimeMillis() - this.mAmapLocationTime.get()) {
                extras.putInt(KEY_INTERFACE_OK, 0);
            } else {
                extras.putInt(KEY_INTERFACE_OK, 1);
            }
            extras.putInt(KEY_CONTROL_OK, 1);
            extras.putInt(KEY_SYSTEM_OK, 1);
            if (MAX_TIME_FOR_REPORT_TIMEOUT < System.currentTimeMillis() - this.mPositionTime.get()) {
                extras.putInt(KEY_CHIP_OK, 1);
            } else {
                extras.putInt(KEY_CHIP_OK, 0);
            }
            this.mAmapUuid = null;
            return AMAP_ERROR_STATUS_CODE;
        }

        public void startMapNavigationMonitor() {
            if ((OppoLocationStatusMonitor.this.mLocationStConfig & OppoLocationStatusMonitor.FLAG_NAVIGATION_TIME_OUT) != 0) {
                this.mBdPositionNumber5s.set(0);
                this.mBdReportNumber5s.set(0);
                this.mMapHandler.sendEmptyMessage(8);
            }
        }

        public void setPositionTimer(long currentTime) {
            this.mPositionTime.set(currentTime);
        }

        public String generateNavigationInfo() {
            if ((OppoLocationStatusMonitor.this.mLocationStConfig & OppoLocationStatusMonitor.FLAG_NAVIGATION_TIME_OUT) == 0) {
                OppoLocationStatusMonitor.this.logd("Feature is disable!");
                return null;
            }
            String navigationInfo = ((String) null) + ",SceneStatus=" + this.mSceneStatus.get();
            if (this.mAmapAdiu != null) {
                navigationInfo = navigationInfo + ",adiu=" + this.mAmapAdiu + ",";
            }
            if (this.mBdCuid != null) {
                navigationInfo = navigationInfo + ",cuid=" + this.mBdCuid;
            }
            HashMap<String, PackageStatistics> hashMap = this.mAmapNavigationStatistics;
            if (hashMap != null && hashMap.size() > 0) {
                for (String key : this.mAmapNavigationStatistics.keySet()) {
                    if (key == null) {
                        OppoLocationStatusMonitor.this.logd("got the null key!!");
                    } else {
                        String requestTimeString = StringUtils.EMPTY;
                        PackageStatistics stat = this.mAmapNavigationStatistics.get(key);
                        for (int i = stat.getRecordKey(); i > 0; i--) {
                            requestTimeString = requestTimeString + new Date(stat.getRequestStartTime(i)) + "~" + new Date(stat.getRequestEndTime(i)) + "&";
                        }
                        if (0 != stat.getCurrentStartTime() && 0 == stat.getCurrentEndTime()) {
                            requestTimeString = requestTimeString + new Date(stat.getCurrentStartTime()) + "~" + new Date(System.currentTimeMillis());
                        }
                        if (!StringUtils.EMPTY.equals(requestTimeString) && requestTimeString.length() != 0) {
                            navigationInfo = navigationInfo + "[" + key + ":" + requestTimeString + "]";
                        }
                    }
                }
            }
            return navigationInfo;
        }

        public void cleanNavigationMap() {
            HashMap<String, PackageStatistics> hashMap = this.mAmapNavigationStatistics;
            if (hashMap != null) {
                hashMap.clear();
            }
        }

        public void sendExtraCommand(String providerName, String command, Bundle extras) {
            if ((OppoLocationStatusMonitor.this.mLocationStConfig & OppoLocationStatusMonitor.FLAG_NAVIGATION_TIME_OUT) == 0) {
                OppoLocationStatusMonitor.this.logd("Feature is disable!");
            } else if (!"gps".equals(providerName)) {
                OppoLocationStatusMonitor oppoLocationStatusMonitor = OppoLocationStatusMonitor.this;
                oppoLocationStatusMonitor.logd("This is not GPS command, we ignore it! -- " + providerName);
            } else if (command == null || extras == null) {
                OppoLocationStatusMonitor.this.logd("The params is null, we ignore it!!");
            } else if (COMMAND_AMAP_INIT.equals(command)) {
                this.mAmapListenerHashCode = extras.getInt(KEY_LISTENER_HASH_CODE, -1);
                String adiu = extras.getString(KEY_ADIU);
                OppoLocationStatusMonitor oppoLocationStatusMonitor2 = OppoLocationStatusMonitor.this;
                oppoLocationStatusMonitor2.logd("init command, hashCode : " + this.mAmapListenerHashCode + ", adiu : " + adiu);
                initAmapNavigationStatistics(adiu);
            } else if (COMMAND_START_NAVI.equals(command)) {
                String adiu2 = extras.getString(KEY_ADIU);
                OppoLocationStatusMonitor oppoLocationStatusMonitor3 = OppoLocationStatusMonitor.this;
                oppoLocationStatusMonitor3.logd("start command, hashCode : " + this.mAmapListenerHashCode + ", adiu : " + adiu2);
                updateAmapNavigationStatistics(adiu2, true);
            } else if (COMMAND_STOP_NAVI.equals(command)) {
                OppoLocationStatusMonitor.this.logd("stop command!");
                updateAmapNavigationStatistics(null, false);
            } else if (COMMAND_SCENE_INDOOR.equals(command)) {
                this.mSceneStatus.set(1);
            } else if (COMMAND_SCENE_OUTDOOR.equals(command)) {
                this.mSceneStatus.set(2);
            } else if (COMMAND_SCENE_UNKNOW.equals(command)) {
                this.mSceneStatus.set(0);
            } else if (COMMAND_GPS_TIMEOUT.equals(command)) {
                if (extras.containsKey(KEY_UUID)) {
                    handleAmapTimeoutEvent(extras);
                } else {
                    handleBdTimeoutEvent(extras, true);
                }
            } else if (COMMAND_GET_GPS_DATA.equals(command)) {
                handleBdTimeoutEvent(extras, false);
            } else {
                OppoLocationStatusMonitor oppoLocationStatusMonitor4 = OppoLocationStatusMonitor.this;
                oppoLocationStatusMonitor4.logd("unrecognize command : " + command);
            }
        }

        private void initAmapNavigationStatistics(String adiu) {
            PackageStatistics statistics;
            if (this.mAmapNavigationStatistics == null) {
                this.mAmapNavigationStatistics = new HashMap<>();
            }
            if (adiu != null) {
                String str = this.mAmapAdiu;
                if (str == null || !adiu.equals(str)) {
                    String str2 = this.mAmapAdiu;
                    if (!(str2 == null || (statistics = this.mAmapNavigationStatistics.get(str2)) == null)) {
                        statistics.setCurrentEndTime(System.currentTimeMillis());
                        statistics.updateRequestHistoryTime();
                        this.mAmapNavigationStatistics.put(this.mAmapAdiu, statistics);
                    }
                    this.mAmapAdiu = adiu;
                    if (!this.mAmapNavigationStatistics.containsKey(this.mAmapAdiu)) {
                        OppoLocationStatusMonitor.this.logd("New a object for the adiu!");
                        PackageStatistics statistics2 = new PackageStatistics();
                        statistics2.setPackageName(AMAP_PACKAGE_NAME);
                        this.mAmapNavigationStatistics.put(this.mAmapAdiu, statistics2);
                    }
                    OppoLocationStatusMonitor oppoLocationStatusMonitor = OppoLocationStatusMonitor.this;
                    oppoLocationStatusMonitor.logd("running init command, changed the Adiu to " + this.mAmapAdiu);
                }
            }
        }

        private void updateAmapNavigationStatistics(String adiu, boolean startNavi) {
            PackageStatistics statistics;
            String str;
            PackageStatistics statistics2;
            OppoLocationStatusMonitor oppoLocationStatusMonitor = OppoLocationStatusMonitor.this;
            oppoLocationStatusMonitor.logd("update -- startNavi : " + startNavi + ", adiu : " + adiu + ", amapAdiu : " + this.mAmapAdiu);
            if (this.mAmapNavigationStatistics == null) {
                this.mAmapNavigationStatistics = new HashMap<>();
            }
            if (startNavi && adiu != null && ((str = this.mAmapAdiu) == null || !adiu.equals(str))) {
                String str2 = this.mAmapAdiu;
                if (!(str2 == null || (statistics2 = this.mAmapNavigationStatistics.get(str2)) == null)) {
                    OppoLocationStatusMonitor oppoLocationStatusMonitor2 = OppoLocationStatusMonitor.this;
                    oppoLocationStatusMonitor2.logd("Set exist package's end time " + System.currentTimeMillis());
                    statistics2.setCurrentEndTime(System.currentTimeMillis());
                    statistics2.updateRequestHistoryTime();
                    this.mAmapNavigationStatistics.put(this.mAmapAdiu, statistics2);
                }
                this.mAmapAdiu = adiu;
                if (!this.mAmapNavigationStatistics.containsKey(this.mAmapAdiu)) {
                    OppoLocationStatusMonitor.this.logd("New a object for the adiu!");
                    PackageStatistics statistics3 = new PackageStatistics();
                    statistics3.setPackageName(AMAP_PACKAGE_NAME);
                    this.mAmapNavigationStatistics.put(this.mAmapAdiu, statistics3);
                }
                OppoLocationStatusMonitor oppoLocationStatusMonitor3 = OppoLocationStatusMonitor.this;
                oppoLocationStatusMonitor3.logd("running start navigation command, changed the Adiu to " + this.mAmapAdiu);
            }
            String str3 = this.mAmapAdiu;
            if (str3 != null && (statistics = this.mAmapNavigationStatistics.get(str3)) != null) {
                if (startNavi) {
                    statistics.setCurrentStartTime(System.currentTimeMillis());
                } else {
                    this.mAmapAdiu = null;
                    statistics.setCurrentEndTime(System.currentTimeMillis());
                    statistics.updateRequestHistoryTime();
                }
                this.mAmapNavigationStatistics.put(this.mAmapAdiu, statistics);
            }
        }

        private void handleAmapTimeoutEvent(Bundle extras) {
            int hashCode = extras.getInt(KEY_LISTENER_HASH_CODE, -1);
            if (!(-1 == hashCode || this.mAmapListenerHashCode == hashCode)) {
                OppoLocationStatusMonitor.this.logd("Hash code has changed!!!");
                this.mAmapListenerHashCode = hashCode;
            }
            this.mAmapUuid = extras.getString(KEY_UUID);
            String str = this.mAmapUuid;
            if (str == null) {
                OppoLocationStatusMonitor.this.logd("The timeout command is invalid for uuid is null!");
                return;
            }
            String[] strArr = str.split("#");
            String str2 = this.mAmapAdiu;
            if (str2 == null) {
                this.mAmapAdiu = strArr[0];
            } else if (!this.mAmapUuid.contains(str2)) {
                OppoLocationStatusMonitor.this.loge("adiu has changed!!");
                this.mAmapAdiu = strArr[0];
            }
            extras.putInt("satellite_used", OppoLocationStatusMonitor.this.mSvInUse);
            if (AMAP_REPORT_TIMEOUT < System.currentTimeMillis() - this.mAmapLocationTime.get()) {
                extras.putInt(KEY_INTERFACE_OK, 0);
            } else {
                extras.putInt(KEY_INTERFACE_OK, 1);
            }
            extras.putInt(KEY_CONTROL_OK, 1);
            extras.putInt(KEY_SYSTEM_OK, 1);
            if (MAX_TIME_FOR_REPORT_TIMEOUT < System.currentTimeMillis() - this.mPositionTime.get()) {
                extras.putInt(KEY_CHIP_OK, 1);
            } else {
                extras.putInt(KEY_CHIP_OK, 0);
            }
            this.mAmapUuid = null;
            this.mMapHandler.sendEmptyMessage(OppoLocationStatusMonitor.MAP_NAVIGATION_TIMEOUT);
        }

        private void handleBdTimeoutEvent(Bundle extras, boolean update) {
            String cuid = extras.getString(KEY_CUID);
            if (cuid == null) {
                OppoLocationStatusMonitor.this.logd("Error: cuid is null!!");
            } else {
                String str = this.mBdCuid;
                if (str == null || cuid.equals(str)) {
                    OppoLocationStatusMonitor.this.logd("BdCuid has changed.");
                    this.mBdCuid = cuid;
                }
            }
            OppoLocationStatusMonitor oppoLocationStatusMonitor = OppoLocationStatusMonitor.this;
            oppoLocationStatusMonitor.logd("running Baidu Map timeout " + this.mBdReportNumber5s.get() + ", " + this.mBdPositionNumber5s.get());
            extras.putInt("satellite_used", OppoLocationStatusMonitor.this.mSvInUse);
            extras.putInt(KEY_REPORT_NUMBER_5S, this.mBdReportNumber5s.get());
            extras.putInt(KEY_POSITION_NUMBER_5S, this.mBdPositionNumber5s.get());
            if (update) {
                this.mMapHandler.sendEmptyMessage(OppoLocationStatusMonitor.MAP_NAVIGATION_TIMEOUT);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleBdReportTimer() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.mPositionTime.get() > MAX_TIME_FOR_REPORT_TIMEOUT && this.mBdPositionNumber5s.get() > 0) {
                this.mBdPositionNumber5s.getAndDecrement();
                OppoLocationStatusMonitor oppoLocationStatusMonitor = OppoLocationStatusMonitor.this;
                oppoLocationStatusMonitor.logd("bd position timeout is : " + this.mBdPositionNumber5s.get());
            } else if (currentTime - this.mPositionTime.get() <= MAX_TIME_FOR_REPORT_TIMEOUT && this.mBdPositionNumber5s.get() < OppoLocationStatusMonitor.GNSS_TTFF_TIMEOUT_FIX) {
                this.mBdPositionNumber5s.getAndIncrement();
                OppoLocationStatusMonitor oppoLocationStatusMonitor2 = OppoLocationStatusMonitor.this;
                oppoLocationStatusMonitor2.logd("bd position timein is : " + this.mBdPositionNumber5s.get());
            }
            if (currentTime - this.mBdReportTime.get() > MAX_TIME_FOR_REPORT_TIMEOUT && this.mBdReportNumber5s.get() > 0) {
                this.mBdReportNumber5s.getAndDecrement();
                OppoLocationStatusMonitor oppoLocationStatusMonitor3 = OppoLocationStatusMonitor.this;
                oppoLocationStatusMonitor3.logd("bd report timeout is : " + this.mBdReportNumber5s.get());
            } else if (currentTime - this.mBdReportTime.get() <= MAX_TIME_FOR_REPORT_TIMEOUT && this.mBdReportNumber5s.get() < OppoLocationStatusMonitor.GNSS_TTFF_TIMEOUT_FIX) {
                this.mBdReportNumber5s.getAndIncrement();
                OppoLocationStatusMonitor oppoLocationStatusMonitor4 = OppoLocationStatusMonitor.this;
                oppoLocationStatusMonitor4.logd("bd report timeout is : " + this.mBdReportNumber5s.get());
            }
        }

        /* access modifiers changed from: private */
        public class MapNavigationHandler extends Handler {
            public MapNavigationHandler(Looper looper) {
                super(looper, null, true);
            }

            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 8) {
                    MapNavigationInfo.this.handleBdReportTimer();
                    sendEmptyMessageDelayed(8, 1000);
                } else if (i == OppoLocationStatusMonitor.MAP_NAVIGATION_TIMEOUT) {
                    if (OppoLocationStatusMonitor.this.mIsLocationStEnabled || (OppoLocationStatusMonitor.this.mLocationStConfig & OppoLocationStatusMonitor.FLAG_NAVIGATION_TIME_OUT) != 0) {
                        OppoLocationStatusMonitor.this.mGnssStatisticInfo.generateGnssStatisticInfo(OppoLocationStatusMonitor.MAP_NAVIGATION_TIMEOUT);
                    }
                }
            }
        }
    }
}
