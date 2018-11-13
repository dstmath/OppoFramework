package com.android.server.storage;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.DiskInfo;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageManager.Stub;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings.Global;
import android.util.DebugUtils;
import android.util.Slog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.EventLogTags;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoProcessManager;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.color.app.ColorAlertDialogCustom;
import com.color.util.ColorUnitConversionUtils;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import oppo.util.OppoStatistics;

public class OppoDeviceStorageMonitorService extends DeviceStorageMonitorService {
    private static final String ACTION_OPPO_SDCARD_STORAGE_LOW = "android.intent.action.OPPO_SDCARD_STORAGE_LOW";
    private static final String ACTION_OPPO_SDCARD_STORAGE_OK = "oppo.intent.action.OPPO_SDCARD_STORAGE_OK";
    private static final String ACT_TURNTO_A_KEY_MOVE = "aKeyMove";
    private static final String ACT_TURNTO_CANCEL = "cancel";
    private static final String ACT_TURNTO_CLEANUP = "cleanUp";
    private static final String CLEAR_SCAN_MODE = "DEEP_CLEAN";
    private static final String DATA_SPACE_MONITOR_CURVE_SWITCH = "data_space_monitor_curve_switch";
    private static final long DEFAULT_LOG_DELTA_BYTES = 67108864;
    private static final long DELAY_AFTER_CACEL_DIAL = 300;
    private static final long DELAY_FIRST_CHECK = 7000;
    private static final long DELAY_SD_MOUNT_CHECK = 200;
    private static final int DIALOG_DISPLAY = 0;
    private static final int DIALOG_NO_DISP_SCREEN_LANDSCAPE = -2;
    private static final int DIALOG_NO_DISP_TASK_FINISH_DISPED = -1;
    private static final long GB_BYTES = 1073741824;
    private static final String ID_DATA_FULL_DIAL_TURNTO = "data_full_dial_turn_to";
    private static final String ID_DATA_FULL_ENTER = "data_storage_full_enter";
    private static final String ID_DATA_FULL_EXIT = "data_storage_full_exit";
    private static final String ID_DATA_LOW_DIAL_TURNTO = "data_low_dial_turn_to";
    private static final String ID_DATA_LOW_ENTER = "data_storage_low_enter";
    private static final String ID_DATA_LOW_EXIT = "data_storage_low_exit";
    private static final long INTERVAL_DIALOG_DATA = 1800000;
    public static final long KB_BYTES = 1024;
    private static final int MAX_INTERVAL = 21600000;
    public static final long MB_BYTES = 1048576;
    private static final int MIN_INTERVAL = 60000;
    private static final int MODE_DEEP = 2;
    private static final long NUMBER_THREE = 3;
    private static final String OPPO_ACTION_DIALOG_DATA = "oppo.intent.action.DIALOG_DATA";
    private static final String OPPO_ACTION_DIALOG_SD = "oppo.intent.action.DIALOG_SD";
    private static final String OPPO_ACTION_FILE_CLEANUP = "com.oppo.cleandroid.ui.ClearMainActivity";
    private static final String OPPO_ACTION_ONE_KEY_MOVE = "com.oppo.filemanager.akeytomove.AKeyToMoveActivity";
    private static final String OPPO_ACTION_OPEN_FILEMANAGER = "oppo.intent.action.OPEN_FILEMANAGER";
    private static final String OPPO_ACTION_SHOW_LOW_STORAGE_ALERT = "com.oppo.showLowStorageAlert";
    private static final String OPPO_ACTION_TASK_TERMINATION = "oppo.intent.action.TASK_TERMINATION_FOR_LOW_STORAGE";
    private static final String OPPO_ACTION_TOMORROW_ZERO_OCLOCK = "oppo.intent.action.TOMORROW_ZERO_OCLOCK";
    private static final long OPPO_DEFAULT_CHECK_INTERVAL = 30000;
    private static final int OPPO_DEVICE_SD_UNMOUNT = 101;
    private static final int OPPO_MONITOR_INTERVAL = 30;
    private static final long OPPO_SD_NOT_ENOUGH_TRIM_MB = 52428800;
    private static final long OPPO_SHORT_CHECK_INTERVAL = 10000;
    private static final int OPPO_SHORT_INTERVAL = 10;
    private static final String SRC_DIALOG_APP = "StorageDialogApp";
    private static final String SRC_DIALOG_FULL = "StorageDialogFull";
    private static final String SRC_DIALOG_LOW = "StorageDialogLow";
    private static final String SRC_NOTIFICATION_FULL = "StorageNotificationFull";
    private static final String SRC_NOTIFICATION_LOW = "StorageNotificationLow";
    private static final String TAG = "DeviceStorageMonitor";
    private static final long THRESHOLD_DATA_FULL = 52428800;
    private static final long THRESHOLD_DATA_LOW = 838860800;
    public static final long THRESHOLD_DELTA_DATA_LOW = 314572800;
    private static final long THRESHOLD_SD_SUFFICIENT = 1073741824;
    private static final long TIMESTAMP_BOOT_COMPLETE = 120000;
    private static final boolean sLocalLOGV = false;
    private boolean mAllowDialogTaskFinishDataShow = true;
    private boolean mAllowDialogTaskFinishSdShow = true;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.LOCALE_CHANGED")) {
                if (OppoDeviceStorageMonitorService.this.mDataLowFlag && OppoDeviceStorageMonitorService.this.mNotificationDataShowed) {
                    OppoDeviceStorageMonitorService.this.oppoCancelNotification(23);
                    OppoDeviceStorageMonitorService.this.oppoNotificationData();
                }
            } else if (action.equals(OppoDeviceStorageMonitorService.OPPO_ACTION_SHOW_LOW_STORAGE_ALERT)) {
                OppoDeviceStorageMonitorService.this.oppoAlertDialogData();
            } else if (action.equals("android.intent.action.DATE_CHANGED")) {
                String str = "";
                if (OppoDeviceStorageMonitorService.this.mDataLowFlag) {
                    str = str.concat(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                } else {
                    str = str.concat("0");
                }
                if (OppoDeviceStorageMonitorService.this.mSdLowFlag) {
                    str = str.concat(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                } else {
                    str = str.concat("0");
                }
            } else if (action.equals(OppoDeviceStorageMonitorService.OPPO_ACTION_TASK_TERMINATION)) {
                String pkg = intent.getStringExtra("package");
                String space = intent.getStringExtra("space");
                if (pkg == null || space == null) {
                    Slog.d(OppoDeviceStorageMonitorService.TAG, "TASK TERMINATION. pkg=" + pkg + ", space=" + space);
                } else if ("Phone".equals(space)) {
                    if (OppoDeviceStorageMonitorService.this.mListTaskTermData.contains(pkg)) {
                        Slog.d(OppoDeviceStorageMonitorService.TAG, "TASK TERMINATION. Phone. pkg(" + pkg + ") has showed TaskTermData before.");
                        return;
                    }
                    long freeDataSpace = ColorStorageUtils.getDataFreeSpace();
                    if (freeDataSpace < 0 || freeDataSpace > OppoDeviceStorageMonitorService.this.mDataLowThreshold + OppoDeviceStorageMonitorService.THRESHOLD_DELTA_DATA_LOW) {
                        Slog.d(OppoDeviceStorageMonitorService.TAG, "TASK TERMINATION. freeDataSpace=" + ColorStorageUtils.formatBytesLocked(freeDataSpace) + ". ignore.");
                    } else {
                        OppoDeviceStorageMonitorService.this.mListTaskTermData.add(pkg);
                        OppoDeviceStorageMonitorService.this.oppoAlertDialogTaskTerminationData();
                    }
                } else if ("sd".equals(space)) {
                    if (OppoDeviceStorageMonitorService.this.mListTaskTermSd.contains(pkg)) {
                        Slog.d(OppoDeviceStorageMonitorService.TAG, "TASK TERMINATION. sd. pkg(" + pkg + ") has showed TaskTermSd before.");
                        return;
                    }
                    long freeSdSpace = OppoDeviceStorageMonitorService.this.getSdFreeSpace();
                    if (freeSdSpace < 0 || freeSdSpace > OppoDeviceStorageMonitorService.this.mSdStartTrimThreshold) {
                        Slog.d(OppoDeviceStorageMonitorService.TAG, "TASK TERMINATION. freeSdSpace=" + ColorStorageUtils.formatBytesLocked(freeSdSpace) + ". ignore.");
                    } else {
                        OppoDeviceStorageMonitorService.this.mListTaskTermSd.add(pkg);
                        OppoDeviceStorageMonitorService.this.oppoAlertDialogTaskTerminationSd();
                    }
                }
            } else if (OppoDeviceStorageMonitorService.OPPO_ACTION_DIALOG_DATA.equals(action)) {
                if (OppoDeviceStorageMonitorService.this.mCntNotifyData >= 1) {
                    Slog.d(OppoDeviceStorageMonitorService.TAG, "DIALOG DATA. cntNotifyData=" + OppoDeviceStorageMonitorService.this.mCntNotifyData);
                    return;
                }
                OppoDeviceStorageMonitorService.this.oppoAlertDialogData();
                OppoDeviceStorageMonitorService.this.oppoNotificationData();
                if (OppoDeviceStorageMonitorService.this.mCntNotifyData < 1) {
                    OppoDeviceStorageMonitorService.this.scheduleAlarmDialogData(0);
                }
            } else if (OppoDeviceStorageMonitorService.OPPO_ACTION_DIALOG_SD.equals(action)) {
                if (!OppoDeviceStorageMonitorService.this.oppoAlertDialogSd()) {
                    OppoDeviceStorageMonitorService.this.scheduleAlarmDialogSd(1800000);
                }
            } else if (OppoDeviceStorageMonitorService.OPPO_ACTION_TOMORROW_ZERO_OCLOCK.equals(action)) {
                OppoDeviceStorageMonitorService.this.mCntNotifyData = 0;
                OppoDeviceStorageMonitorService.this.mListTaskTermData.clear();
                OppoDeviceStorageMonitorService.this.mListTaskTermSd.clear();
                if (OppoDeviceStorageMonitorService.this.mDataLowFlag) {
                    OppoDeviceStorageMonitorService.this.scheduleAlarmDialogData(0);
                }
                OppoDeviceStorageMonitorService.this.scheduleAlarmTomorrowZeroOclock();
            } else if ("android.intent.action.CONFIGURATION_CHANGED".equals(action)) {
                int orientation = OppoDeviceStorageMonitorService.this.mContext.getResources().getConfiguration().orientation;
                if (OppoDeviceStorageMonitorService.this.mOppoDebug) {
                    Slog.d(OppoDeviceStorageMonitorService.TAG, " CONFIGURATION CHANGED: orientation=" + orientation + ", Need Display Exit Landscape=" + OppoDeviceStorageMonitorService.this.mNeedDisplayExitLandscape);
                }
                if (1 == orientation) {
                    if (OppoDeviceStorageMonitorService.this.mNeedDisplayExitLandscape) {
                        OppoDeviceStorageMonitorService.this.oppoAlertDialogData();
                    }
                } else if (2 == orientation) {
                    if (OppoDeviceStorageMonitorService.this.mDialogData != null && OppoDeviceStorageMonitorService.this.mDialogData.isShowing()) {
                        OppoDeviceStorageMonitorService.this.mDialogData.cancel();
                        OppoDeviceStorageMonitorService.this.mNeedDisplayExitLandscape = true;
                    }
                    if (OppoDeviceStorageMonitorService.this.mDialogDataMultiKey != null && OppoDeviceStorageMonitorService.this.mDialogDataMultiKey.isShowing()) {
                        OppoDeviceStorageMonitorService.this.mDialogDataMultiKey.cancel();
                        OppoDeviceStorageMonitorService.this.mNeedDisplayExitLandscape = true;
                    }
                }
            }
        }
    };
    private int mCntNotifyData;
    private Context mContext;
    private long mDataFree;
    private boolean mDataFullFlag = false;
    private long mDataFullThreshold;
    private int mDataLevel = 0;
    private boolean mDataLowFlag = false;
    private long mDataLowThreshold;
    private boolean mDataRealFullFlag = false;
    private AlertDialog mDialogData = null;
    private PendingIntent mDialogDataIntent;
    private ColorAlertDialogCustom mDialogDataMultiKey = null;
    private AlertDialog mDialogSd = null;
    private PendingIntent mDialogSdIntent;
    private AlertDialog mDialogTaskFinishData = null;
    private AlertDialog mDialogTaskFinishSd = null;
    private long mFreeExternalSd;
    private WorkerHandler mHandler;
    private Intent mIntentFileManager;
    private Intent mIntentOneKeyMove;
    private Intent mIntentPackageStorage;
    private boolean mIsSdMounted = false;
    private long mLastDataFree;
    private List<String> mListTaskTermData = new ArrayList();
    private List<String> mListTaskTermSd = new ArrayList();
    private final Object mLock = new Object();
    private IStorageManager mMountService = null;
    private boolean mNeedDisplayExitLandscape = false;
    private boolean mNotificationDataShowed = false;
    private boolean mOppoDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private boolean mSdLowFlag = false;
    private long mSdStartTrimThreshold;
    private Intent mSdStorageLowIntent;
    private Intent mSdStorageOkIntent;
    private boolean mSdSufficient = true;
    private final StorageEventListener mStorageListener = new StorageEventListener() {
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            DiskInfo diskInfo = vol.getDisk();
            if (diskInfo != null && (diskInfo.isSd() ^ 1) == 0) {
                if (oldState != 2 && newState == 2) {
                    synchronized (OppoDeviceStorageMonitorService.this.mLock) {
                        OppoDeviceStorageMonitorService.this.mVolumeExternalSd = vol;
                        OppoDeviceStorageMonitorService.this.mHandler.removeMessages(101);
                        OppoDeviceStorageMonitorService.this.reScheduleCheck(OppoDeviceStorageMonitorService.DELAY_SD_MOUNT_CHECK);
                    }
                    Slog.d(OppoDeviceStorageMonitorService.TAG, "onVolumeStateChanged: external TF card mounted. id=" + vol.getId() + ", path=" + vol.path + ", oldState=" + DebugUtils.valueToString(VolumeInfo.class, "STATE_", oldState) + ", newState=" + DebugUtils.valueToString(VolumeInfo.class, "STATE_", newState));
                } else if (newState != 2 && oldState == 2) {
                    synchronized (OppoDeviceStorageMonitorService.this.mLock) {
                        OppoDeviceStorageMonitorService.this.mVolumeExternalSd = null;
                        OppoDeviceStorageMonitorService.this.mHandler.removeMessages(101);
                        OppoDeviceStorageMonitorService.this.mHandler.sendEmptyMessage(101);
                    }
                    Slog.d(OppoDeviceStorageMonitorService.TAG, "onVolumeStateChanged: external TF card unmounted. id=" + vol.getId() + ", path=" + vol.path + ", oldState=" + DebugUtils.valueToString(VolumeInfo.class, "STATE_", oldState) + ", newState=" + DebugUtils.valueToString(VolumeInfo.class, "STATE_", newState));
                }
            }
        }
    };
    private PendingIntent mTomorrowIntent;
    private long mTotalData;
    private VolumeInfo mVolumeExternalSd = null;

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            synchronized (OppoDeviceStorageMonitorService.this.mLock) {
                if (msg.what == 101) {
                    OppoDeviceStorageMonitorService.this.oppoSdUnmounted();
                }
            }
        }
    }

    private void oppoSdUnmounted() {
        this.mIsSdMounted = false;
        Slog.d(TAG, "oppoSdUnmounted");
        if (this.mSdLowFlag) {
            this.mSdLowFlag = false;
            Slog.d(TAG, "oppoSdUnmounted: Cancelling notification");
            sdcancelNotification();
        }
        reactDataLowWarning();
    }

    private long getSdFreeSpace() {
        if (this.mVolumeExternalSd == null || this.mVolumeExternalSd.path == null) {
            return -1;
        }
        File path = this.mVolumeExternalSd.getPath();
        if (path == null || path.getTotalSpace() <= 0) {
            return -1;
        }
        long freeExternalSd = path.getFreeSpace();
        Slog.d(TAG, "getSdFreeSpace: freeExternalSd = " + ColorStorageUtils.formatBytesLocked(freeExternalSd));
        return freeExternalSd;
    }

    private void dataBecomeLow() {
        if (-1 == oppoAlertDialogData()) {
            scheduleAlarmDialogData(1800000);
        }
        oppoCancelNotification(23);
        oppoNotificationData();
    }

    private void dataBecomeFull() {
        if (this.mDialogData != null && this.mDialogData.isShowing()) {
            this.mDialogData.cancel();
            sleepMillis(300);
        }
        if (this.mDialogDataMultiKey != null && this.mDialogDataMultiKey.isShowing()) {
            this.mDialogDataMultiKey.cancel();
            sleepMillis(300);
        }
        if (-1 == oppoAlertDialogData()) {
            scheduleAlarmDialogData(1800000);
        }
        oppoCancelNotification(23);
        oppoNotificationData();
    }

    private void dataBecomeNotLow() {
        oppoCancelNotification(23);
        if (this.mDialogData != null && this.mDialogData.isShowing()) {
            this.mDialogData.cancel();
        }
        if (this.mDialogDataMultiKey != null && this.mDialogDataMultiKey.isShowing()) {
            this.mDialogDataMultiKey.cancel();
        }
        this.mAllowDialogTaskFinishDataShow = true;
        this.mNotificationDataShowed = false;
        cancelAlarmDialogData();
    }

    private void dataBecomeNotFull() {
        boolean shouldShowDialog = false;
        if (this.mDialogData != null && this.mDialogData.isShowing()) {
            this.mDialogData.cancel();
            sleepMillis(300);
            shouldShowDialog = true;
        }
        if (this.mDialogDataMultiKey != null && this.mDialogDataMultiKey.isShowing()) {
            this.mDialogDataMultiKey.cancel();
            sleepMillis(300);
            shouldShowDialog = true;
        }
        if (shouldShowDialog) {
            oppoAlertDialogData();
        }
        oppoCancelNotification(23);
        oppoNotificationData();
    }

    private void oppoCheckData() {
        int dataLevel = this.mDataLevel;
        long dataFree = ColorStorageUtils.getDataFreeSpace();
        this.mDataFree = dataFree;
        if (this.mOppoForceLevle == 1) {
            dataFree = this.mDataLowThreshold - MB_BYTES;
            this.mDataLowFlag = false;
            Slog.d(TAG, "oppoCheckData: mOppoForceLevle is low!!!");
        } else if (this.mOppoForceLevle == 2) {
            dataFree = 0;
            this.mDataFullFlag = false;
            Slog.d(TAG, "oppoCheckData: mOppoForceLevle is full!!!");
        } else if (this.mOppoForceLevle == 0) {
            this.mDataLowFlag = true;
            this.mDataFullFlag = true;
            Slog.d(TAG, "oppoCheckData: mOppoForceLevle is normal!!!");
        }
        if (dataFree > this.mDataFullThreshold && dataFree < this.mDataLowThreshold) {
            if (!this.mDataLowFlag) {
                Slog.d(TAG, "data become low. freeStorage=" + this.mDataFree + ", forceLevle=" + this.mOppoForceLevle);
                this.mCntNotifyData = 0;
                dataBecomeLow();
                scheduleAlarmTomorrowZeroOclock();
                sendDataLowBroadcast();
                uploadDcsEvent(ID_DATA_LOW_ENTER, new HashMap());
                this.mDataLowFlag = true;
            }
            dataLevel = 1;
        } else if (dataFree >= this.mDataLowThreshold + THRESHOLD_DELTA_DATA_LOW) {
            if (this.mDataFullFlag) {
                Slog.d(TAG, "data from full to not low.");
                this.mDataFullFlag = false;
                uploadDcsEvent(ID_DATA_FULL_EXIT, new HashMap());
            }
            if (this.mDataLowFlag) {
                Slog.d(TAG, "data available. freeStorage=" + this.mDataFree + ", forceLevle=" + this.mOppoForceLevle);
                this.mNeedDisplayExitLandscape = false;
                dataBecomeNotLow();
                cancelAlarmTomorrowZeroOclock();
                sendDataNotLowBroadcast();
                this.mDataLowFlag = false;
                uploadDcsEvent(ID_DATA_LOW_EXIT, new HashMap());
            }
            dataLevel = 0;
        }
        if (dataFree <= this.mDataFullThreshold) {
            if (!this.mDataLowFlag) {
                this.mCntNotifyData = 0;
                scheduleAlarmTomorrowZeroOclock();
                sendDataLowBroadcast();
                this.mDataLowFlag = true;
                uploadDcsEvent(ID_DATA_LOW_ENTER, new HashMap());
                Slog.d(TAG, "data from not low to full.");
            }
            if (!this.mDataFullFlag) {
                Slog.d(TAG, "data become full, freeStorage=" + this.mDataFree + ", forceLevle=" + this.mOppoForceLevle);
                this.mCntNotifyData = 0;
                this.mDataFullFlag = true;
                dataBecomeFull();
                uploadDcsEvent(ID_DATA_FULL_ENTER, new HashMap());
            }
            dataLevel = 2;
        } else if (dataFree >= this.mDataFullThreshold + THRESHOLD_DELTA_DATA_LOW && dataFree < this.mDataLowThreshold + THRESHOLD_DELTA_DATA_LOW) {
            if (this.mDataFullFlag) {
                Slog.d(TAG, "data become not full, freeStorage=" + this.mDataFree + ", forceLevle=" + this.mOppoForceLevle);
                this.mDataFullFlag = false;
                dataBecomeNotFull();
                uploadDcsEvent(ID_DATA_FULL_EXIT, new HashMap());
            }
            dataLevel = 1;
        }
        if (dataFree <= 52428800) {
            if (!this.mDataRealFullFlag) {
                sendDataFullBroadcast();
                this.mDataRealFullFlag = true;
            }
        } else if (this.mDataRealFullFlag) {
            sendDataNotFullBroadcast();
            this.mDataRealFullFlag = false;
        }
        if (Math.abs(this.mLastDataFree - this.mDataFree) > DEFAULT_LOG_DELTA_BYTES || this.mDataLevel != dataLevel) {
            EventLogTags.writeStorageState(StorageManager.UUID_PRIVATE_INTERNAL, this.mDataLevel, dataLevel, this.mDataFree, this.mTotalData);
            this.mLastDataFree = this.mDataFree;
        }
        this.mDataLevel = dataLevel;
    }

    private final void oppoCheckSD() {
        if (this.mVolumeExternalSd != null && this.mVolumeExternalSd.path != null) {
            File path = this.mVolumeExternalSd.getPath();
            if (path != null && path.getTotalSpace() > 0) {
                boolean sdStateChange = false;
                if (!this.mIsSdMounted) {
                    this.mIsSdMounted = true;
                    sdStateChange = true;
                    Slog.v(TAG, "mIsSdMounted set true!");
                }
                this.mFreeExternalSd = path.getFreeSpace();
                if (this.mFreeExternalSd < this.mSdStartTrimThreshold) {
                    if (!this.mSdLowFlag) {
                        Slog.d(TAG, "oppoCheckSD: Running low on SDCARD. Sending notification");
                        sdsendNotification();
                    }
                } else if (this.mSdLowFlag) {
                    Slog.d(TAG, "oppoCheckSD: SDCARD available. Cancelling notification");
                    sdcancelNotification();
                }
                boolean sdSufficientChanged = false;
                if (this.mFreeExternalSd >= 1073741824) {
                    if (!this.mSdSufficient) {
                        this.mSdSufficient = true;
                        sdSufficientChanged = true;
                        Slog.i(TAG, "oppoCheckSD: SDCARD Sufficient.");
                    }
                } else if (this.mSdSufficient) {
                    this.mSdSufficient = false;
                    sdSufficientChanged = true;
                    Slog.i(TAG, "oppoCheckSD: SDCARD not Sufficient.");
                }
                if (sdSufficientChanged || sdStateChange) {
                    reactDataLowWarning();
                }
            }
        }
    }

    private void reactDataLowWarning() {
        if (this.mDataLowFlag || this.mDataFullFlag) {
            if (this.mNotificationDataShowed) {
                oppoCancelNotification(23);
                oppoNotificationData();
            }
            if (this.mDialogDataMultiKey != null && this.mDialogDataMultiKey.isShowing()) {
                this.mDialogDataMultiKey.cancel();
                oppoAlertDialogData();
            }
            if (this.mDialogData != null && this.mDialogData.isShowing()) {
                this.mDialogData.cancel();
                oppoAlertDialogData();
            }
        }
    }

    private void sendDataLowBroadcast() {
        int seq = this.mSeq.get();
        this.mContext.sendStickyBroadcastAsUser(new Intent("android.intent.action.DEVICE_STORAGE_LOW").addFlags(85983232).putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, seq), UserHandle.ALL);
        Slog.d(TAG, "sendDataLowBroadcast. seq=" + seq);
    }

    private void sendDataNotLowBroadcast() {
        int seq = this.mSeq.get();
        Intent lowIntent = new Intent("android.intent.action.DEVICE_STORAGE_LOW").addFlags(85983232).putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, seq);
        Intent notLowIntent = new Intent("android.intent.action.DEVICE_STORAGE_OK").addFlags(85983232).putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, seq);
        this.mContext.removeStickyBroadcastAsUser(lowIntent, UserHandle.ALL);
        this.mContext.sendBroadcastAsUser(notLowIntent, UserHandle.ALL);
        Slog.d(TAG, "sendDataNotLowBroadcast. seq=" + seq);
    }

    private void sendDataFullBroadcast() {
        this.mContext.sendStickyBroadcastAsUser(new Intent("android.intent.action.DEVICE_STORAGE_FULL").addFlags(67108864).putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mSeq.get()), UserHandle.ALL);
        Slog.d(TAG, "sendDataFullBroadcast.");
    }

    private void sendDataNotFullBroadcast() {
        int seq = this.mSeq.get();
        Intent fullIntent = new Intent("android.intent.action.DEVICE_STORAGE_FULL").addFlags(67108864).putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, seq);
        Intent notFullIntent = new Intent("android.intent.action.DEVICE_STORAGE_NOT_FULL").addFlags(67108864).putExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, seq);
        this.mContext.removeStickyBroadcastAsUser(fullIntent, UserHandle.ALL);
        this.mContext.sendBroadcastAsUser(notFullIntent, UserHandle.ALL);
        Slog.d(TAG, "sendDataNotFullBroadcast.");
    }

    public void onBootPhase(int phase) {
        super.onBootPhase(phase);
        if (1000 == phase) {
            Slog.d(TAG, "onBootPhase: PHASE_BOOT_COMPLETED");
            reScheduleCheck(DELAY_FIRST_CHECK);
        }
    }

    public void onStart() {
        super.onStart();
        Slog.d(TAG, "onStart!!!");
        this.mContext = getContext();
        this.mHandler = new WorkerHandler(this.looperStorageMonitor);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        filter.addAction(OPPO_ACTION_SHOW_LOW_STORAGE_ALERT);
        filter.addAction("android.intent.action.DATE_CHANGED");
        filter.addAction(OPPO_ACTION_TASK_TERMINATION);
        filter.addAction(OPPO_ACTION_DIALOG_DATA);
        filter.addAction(OPPO_ACTION_TOMORROW_ZERO_OCLOCK);
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, this.mHandler);
        this.mIntentFileManager = new Intent(OPPO_ACTION_OPEN_FILEMANAGER);
        this.mIntentFileManager.addFlags(335544320);
        this.mIntentOneKeyMove = new Intent(OPPO_ACTION_ONE_KEY_MOVE);
        this.mIntentOneKeyMove.addFlags(603979776);
        this.mIntentPackageStorage = new Intent("android.intent.action.MANAGE_PACKAGE_STORAGE");
        this.mIntentPackageStorage.addFlags(335544320);
        this.mSdStorageLowIntent = new Intent(ACTION_OPPO_SDCARD_STORAGE_LOW);
        this.mSdStorageLowIntent.addFlags(67108864);
        this.mSdStorageOkIntent = new Intent(ACTION_OPPO_SDCARD_STORAGE_OK);
        this.mSdStorageOkIntent.addFlags(67108864);
        this.mDialogDataIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(OPPO_ACTION_DIALOG_DATA), 0);
        this.mDialogSdIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(OPPO_ACTION_DIALOG_SD), 0);
        this.mTomorrowIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(OPPO_ACTION_TOMORROW_ZERO_OCLOCK), 0);
        this.mSdStartTrimThreshold = 52428800;
        this.mMountService = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
        ((StorageManager) this.mContext.getSystemService(StorageManager.class)).registerListener(this.mStorageListener);
        ColorStorageUtils.getDataThreshold();
        this.mTotalData = ColorStorageUtils.getTotalData();
        this.mDataLowThreshold = ColorStorageUtils.getDataLowThreshold();
        this.mDataFullThreshold = ColorStorageUtils.getDataFullThreshold();
    }

    public OppoDeviceStorageMonitorService(Context context) {
        super(context);
    }

    private boolean isScreenLandScape() {
        if (this.mContext.getResources().getConfiguration().orientation == 2) {
            return true;
        }
        return false;
    }

    private boolean isNeedDataSpaceMonitorCurve() {
        if (Global.getInt(this.mContext.getContentResolver(), DATA_SPACE_MONITOR_CURVE_SWITCH, 1) == 1) {
            return true;
        }
        return false;
    }

    private void scheduleAlarmDialogData(long delay) {
        long now = System.currentTimeMillis();
        long alarmTime = now + delay;
        if (delay <= 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(now);
            c.set(11, 0);
            c.set(12, 0);
            c.set(13, 0);
            c.set(14, 0);
            c.add(5, 1);
            int delta = MAX_INTERVAL;
            if (c.getTimeInMillis() - now < 21600000) {
                delta = (int) (c.getTimeInMillis() - now);
            }
            int randNum = new Random().nextInt(delta);
            if (randNum < 60000) {
                randNum = 60000;
            }
            alarmTime = now + ((long) randNum);
        }
        ((AlarmManager) this.mContext.getSystemService("alarm")).setExact(1, alarmTime, this.mDialogDataIntent);
        Calendar cTmp = Calendar.getInstance();
        cTmp.setTimeInMillis(alarmTime);
        Slog.d(TAG, "schedule Alarm Dialog Data: alarmTime= " + cTmp.getTime());
    }

    private void cancelAlarmDialogData() {
        ((AlarmManager) this.mContext.getSystemService("alarm")).cancel(this.mDialogDataIntent);
    }

    private void scheduleAlarmDialogSd(long delay) {
        long alarmTime = System.currentTimeMillis() + delay;
        ((AlarmManager) this.mContext.getSystemService("alarm")).setExact(1, alarmTime, this.mDialogSdIntent);
        Calendar cTmp = Calendar.getInstance();
        cTmp.setTimeInMillis(alarmTime);
        Slog.d(TAG, "schedule Alarm Dialog Sd: alarmTime= " + cTmp.getTime());
    }

    private void cancelAlarmDialogSd() {
        ((AlarmManager) this.mContext.getSystemService("alarm")).cancel(this.mDialogSdIntent);
    }

    private void scheduleAlarmTomorrowZeroOclock() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(11, 0);
        c.set(12, 0);
        c.set(13, 0);
        c.set(14, 0);
        c.add(5, 1);
        ((AlarmManager) this.mContext.getSystemService("alarm")).set(0, c.getTimeInMillis(), this.mTomorrowIntent);
    }

    private void cancelAlarmTomorrowZeroOclock() {
        ((AlarmManager) this.mContext.getSystemService("alarm")).cancel(this.mTomorrowIntent);
    }

    private void oppoNotificationData() {
        if (!this.mNotificationDataShowed) {
            CharSequence title;
            String source;
            NotificationManager mNotificationMgr = (NotificationManager) this.mContext.getSystemService("notification");
            if (this.mDataFullFlag) {
                title = this.mContext.getText(201590196);
            } else {
                title = this.mContext.getText(201590195);
            }
            CharSequence details = this.mContext.getText(201590197);
            if (this.mDataLevel == 2) {
                source = SRC_NOTIFICATION_FULL;
            } else {
                source = SRC_NOTIFICATION_LOW;
            }
            Notification notification = new Builder(this.mContext, SystemNotificationChannels.ALERTS).setContentTitle(title).setContentText(details).setSmallIcon(201852148).setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, getFileCleanUpIntent(source), 0, null, UserHandle.CURRENT)).setVisibility(1).setCategory("sys").setShowWhen(true).build();
            notification.flags |= 32;
            mNotificationMgr.notifyAsUser(null, 23, notification, UserHandle.ALL);
            this.mNotificationDataShowed = true;
            Slog.d(TAG, "oppoNotificationData: send notification.");
        }
    }

    private void oppoCancelNotification(int id) {
        ((NotificationManager) this.mContext.getSystemService("notification")).cancelAsUser(null, id, UserHandle.ALL);
        if (id == 23) {
            this.mNotificationDataShowed = false;
        }
        Slog.d(TAG, "oppoCancelNotification: id=" + id);
    }

    private void sdsendNotification() {
        if (!oppoAlertDialogSd()) {
            scheduleAlarmDialogSd(1800000);
        }
        this.mSdLowFlag = true;
        this.mContext.sendStickyBroadcastAsUser(this.mSdStorageLowIntent, UserHandle.ALL);
    }

    private void sdcancelNotification() {
        if (this.mDialogSd != null && this.mDialogSd.isShowing()) {
            this.mDialogSd.cancel();
        }
        this.mSdLowFlag = false;
        this.mContext.removeStickyBroadcastAsUser(this.mSdStorageLowIntent, UserHandle.ALL);
        this.mContext.sendBroadcastAsUser(this.mSdStorageOkIntent, UserHandle.ALL);
        this.mAllowDialogTaskFinishSdShow = true;
        cancelAlarmDialogSd();
    }

    private int oppoAlertDialogData() {
        if (this.mDialogTaskFinishData != null && this.mDialogTaskFinishData.isShowing()) {
            Slog.d(TAG, "oppoAlertDialogData: DialogTaskFinishdata is showing.");
            return -1;
        } else if (isScreenLandScape()) {
            this.mNeedDisplayExitLandscape = true;
            return -2;
        } else {
            View viewLayout;
            int titleId;
            long dataRecommendToClean = (this.mDataLowThreshold - this.mDataFree) + THRESHOLD_DELTA_DATA_LOW;
            if (isNeedDataSpaceMonitorCurve()) {
                viewLayout = View.inflate(this.mContext, 201917594, null);
            } else {
                viewLayout = View.inflate(this.mContext, 201917595, null);
            }
            ((TextView) viewLayout.findViewById(201458995)).setText(String.format(this.mContext.getResources().getString(201590190), new Object[]{byteCountToDisplaySize(dataRecommendToClean)}));
            TextView message5 = (TextView) viewLayout.findViewById(201458996);
            if (!this.mDataFullFlag) {
                message5.setText(this.mContext.getResources().getString(201590191));
            } else if (this.mIsSdMounted && this.mSdSufficient) {
                message5.setText(this.mContext.getResources().getString(201590193));
            } else {
                message5.setText(this.mContext.getResources().getString(201590194));
            }
            if (this.mDataFullFlag) {
                titleId = 201590192;
            } else {
                titleId = 201590200;
            }
            if (this.mIsSdMounted && this.mSdSufficient) {
                if (this.mDialogData != null && this.mDialogData.isShowing()) {
                    this.mDialogData.cancel();
                    sleepMillis(300);
                    Slog.d(TAG, "oppoAlertDialogData: cancel old DialogData.");
                }
                if (this.mDialogDataMultiKey == null || !this.mDialogDataMultiKey.isShowing()) {
                    this.mDialogDataMultiKey = new ColorAlertDialogCustom.Builder(this.mContext).setTitle(titleId).setView(viewLayout).setItems(201786390, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            HashMap<String, String> eventMap = new HashMap();
                            switch (which) {
                                case 0:
                                    String source;
                                    eventMap.put("turnTo", "cleanUp");
                                    if (OppoDeviceStorageMonitorService.this.mDataLevel == 2) {
                                        source = OppoDeviceStorageMonitorService.SRC_DIALOG_FULL;
                                        OppoDeviceStorageMonitorService.this.uploadDcsEvent(OppoDeviceStorageMonitorService.ID_DATA_FULL_DIAL_TURNTO, eventMap);
                                    } else {
                                        source = OppoDeviceStorageMonitorService.SRC_DIALOG_LOW;
                                        OppoDeviceStorageMonitorService.this.uploadDcsEvent(OppoDeviceStorageMonitorService.ID_DATA_LOW_DIAL_TURNTO, eventMap);
                                    }
                                    OppoDeviceStorageMonitorService.this.mContext.startActivityAsUser(OppoDeviceStorageMonitorService.this.getFileCleanUpIntent(source), UserHandle.CURRENT);
                                    if (OppoDeviceStorageMonitorService.this.mOppoDebug) {
                                        Slog.d(OppoDeviceStorageMonitorService.TAG, "DialogDataMultiKey: start cleanup activity");
                                        return;
                                    }
                                    return;
                                case 1:
                                    eventMap.put("turnTo", OppoDeviceStorageMonitorService.ACT_TURNTO_A_KEY_MOVE);
                                    if (OppoDeviceStorageMonitorService.this.mDataLevel == 2) {
                                        OppoDeviceStorageMonitorService.this.uploadDcsEvent(OppoDeviceStorageMonitorService.ID_DATA_FULL_DIAL_TURNTO, eventMap);
                                    } else {
                                        OppoDeviceStorageMonitorService.this.uploadDcsEvent(OppoDeviceStorageMonitorService.ID_DATA_LOW_DIAL_TURNTO, eventMap);
                                    }
                                    OppoDeviceStorageMonitorService.this.mContext.startActivityAsUser(OppoDeviceStorageMonitorService.this.mIntentOneKeyMove, UserHandle.CURRENT);
                                    if (OppoDeviceStorageMonitorService.this.mOppoDebug) {
                                        Slog.d(OppoDeviceStorageMonitorService.TAG, "DialogDataMultiKey: start akeymove activity");
                                        return;
                                    }
                                    return;
                                case 2:
                                    eventMap.put("turnTo", OppoDeviceStorageMonitorService.ACT_TURNTO_CANCEL);
                                    if (OppoDeviceStorageMonitorService.this.mDataLevel == 2) {
                                        OppoDeviceStorageMonitorService.this.uploadDcsEvent(OppoDeviceStorageMonitorService.ID_DATA_FULL_DIAL_TURNTO, eventMap);
                                    } else {
                                        OppoDeviceStorageMonitorService.this.uploadDcsEvent(OppoDeviceStorageMonitorService.ID_DATA_LOW_DIAL_TURNTO, eventMap);
                                    }
                                    if (OppoDeviceStorageMonitorService.this.mOppoDebug) {
                                        Slog.d(OppoDeviceStorageMonitorService.TAG, "DialogDataMultiKey: cancel");
                                        return;
                                    }
                                    return;
                                default:
                                    return;
                            }
                        }
                    }).setCancelable(false).create();
                    this.mDialogDataMultiKey.getWindow().setType(2003);
                    ignoreMenuHOmeKey(this.mDialogDataMultiKey.getWindow());
                    this.mDialogDataMultiKey.show();
                    Slog.d(TAG, "oppoAlertDialogData: show DialogDataMultiKey.");
                } else {
                    this.mDialogDataMultiKey.cancel();
                    Slog.d(TAG, "oppoAlertDialogData: DialogDataMultiKey is showing.");
                    return -1;
                }
            }
            if (this.mDialogDataMultiKey != null && this.mDialogDataMultiKey.isShowing()) {
                this.mDialogDataMultiKey.cancel();
                sleepMillis(300);
                Slog.d(TAG, "oppoAlertDialogData: cancel old DialogDataMultiKey.");
            }
            if (this.mDialogData == null || !this.mDialogData.isShowing()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
                builder.setTitle(titleId);
                builder.setView(viewLayout);
                builder.setPositiveButton(201590060, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String source;
                        HashMap<String, String> eventMap = new HashMap();
                        eventMap.put("turnTo", "cleanUp");
                        if (OppoDeviceStorageMonitorService.this.mDataLevel == 2) {
                            source = OppoDeviceStorageMonitorService.SRC_DIALOG_FULL;
                            OppoDeviceStorageMonitorService.this.uploadDcsEvent(OppoDeviceStorageMonitorService.ID_DATA_FULL_DIAL_TURNTO, eventMap);
                        } else {
                            source = OppoDeviceStorageMonitorService.SRC_DIALOG_LOW;
                            OppoDeviceStorageMonitorService.this.uploadDcsEvent(OppoDeviceStorageMonitorService.ID_DATA_LOW_DIAL_TURNTO, eventMap);
                        }
                        OppoDeviceStorageMonitorService.this.mContext.startActivityAsUser(OppoDeviceStorageMonitorService.this.getFileCleanUpIntent(source), UserHandle.CURRENT);
                        if (OppoDeviceStorageMonitorService.this.mOppoDebug) {
                            Slog.d(OppoDeviceStorageMonitorService.TAG, "DialogData: start cleanup activity");
                        }
                    }
                });
                builder.setNegativeButton(201590061, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        HashMap<String, String> eventMap = new HashMap();
                        eventMap.put("turnTo", OppoDeviceStorageMonitorService.ACT_TURNTO_CANCEL);
                        if (OppoDeviceStorageMonitorService.this.mDataLevel == 2) {
                            OppoDeviceStorageMonitorService.this.uploadDcsEvent(OppoDeviceStorageMonitorService.ID_DATA_FULL_DIAL_TURNTO, eventMap);
                        } else {
                            OppoDeviceStorageMonitorService.this.uploadDcsEvent(OppoDeviceStorageMonitorService.ID_DATA_LOW_DIAL_TURNTO, eventMap);
                        }
                        if (OppoDeviceStorageMonitorService.this.mOppoDebug) {
                            Slog.d(OppoDeviceStorageMonitorService.TAG, "DialogData: cancel");
                        }
                    }
                });
                this.mDialogData = builder.create();
                this.mDialogData.getWindow().setType(2003);
                this.mDialogData.setCancelable(false);
                ignoreMenuHOmeKey(this.mDialogData.getWindow());
                this.mDialogData.show();
                Slog.d(TAG, "oppoAlertDialogData: show DialogData.");
            } else {
                Slog.d(TAG, "oppoAlertDialogData: DialogData is showing.");
                return -1;
            }
            this.mCntNotifyData++;
            this.mNeedDisplayExitLandscape = false;
            return 0;
        }
    }

    private boolean oppoAlertDialogSd() {
        if (this.mDialogTaskFinishSd == null || !this.mDialogTaskFinishSd.isShowing()) {
            if (this.mDialogSd != null && this.mDialogSd.isShowing()) {
                this.mDialogSd.cancel();
                Slog.i(TAG, "oppoAlertDialogSd: cacel old DialogSd");
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
            builder.setTitle(201590063);
            builder.setPositiveButton(201590059, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    OppoDeviceStorageMonitorService.this.mContext.startActivityAsUser(OppoDeviceStorageMonitorService.this.mIntentFileManager, UserHandle.CURRENT);
                }
            });
            builder.setNegativeButton(201590061, null);
            this.mDialogSd = builder.create();
            this.mDialogSd.getWindow().setType(2003);
            ignoreMenuHOmeKey(this.mDialogSd.getWindow());
            this.mDialogSd.setCancelable(false);
            this.mDialogSd.show();
            TextView msg = (TextView) this.mDialogSd.findViewById(16908299);
            if (msg != null) {
                msg.setGravity(17);
            }
            Slog.d(TAG, "oppoAlertDialogSd: show DialogSd.");
            return true;
        }
        Slog.d(TAG, "oppoAlertDialogSd: DialogTaskFinishSd is showing.");
        return false;
    }

    private void uploadDcsEvent(String eventId, HashMap<String, String> eventMap) {
        eventMap.put("showTotalSpace", String.valueOf(ColorStorageUtils.getActualShowTotalData()));
        eventMap.put("freeSpace", String.valueOf(this.mDataFree));
        OppoStatistics.onCommon(this.mContext, DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, eventId, eventMap, false);
    }

    private void dumpVolumeinfo() {
        try {
            for (VolumeInfo vol : this.mMountService.getVolumes(0)) {
                DiskInfo diskInfo = vol.getDisk();
                File path = vol.getPath();
                Slog.d(TAG, "id=" + vol.getId() + ", path=" + vol.path + ", internalPath=" + vol.internalPath + ", diskInfo=" + diskInfo + ", type=" + DebugUtils.valueToString(VolumeInfo.class, "TYPE_", vol.getType()) + ", state=" + DebugUtils.valueToString(VolumeInfo.class, "STATE_", vol.state));
                if (diskInfo != null) {
                    Slog.d(TAG, "isSd=" + diskInfo.isSd() + ", isUsb=" + diskInfo.isUsb());
                }
            }
        } catch (RemoteException e) {
        }
    }

    private void ignoreMenuHOmeKey(Window window) {
        if (window == null) {
            Slog.i(TAG, "ignoreMenuHOmeKey: window is null!");
            return;
        }
        LayoutParams p = window.getAttributes();
        p.ignoreHomeMenuKey = 1;
        window.setAttributes(p);
    }

    private void oppoAlertDialogTaskTerminationData() {
        if (this.mAllowDialogTaskFinishDataShow) {
            this.mAllowDialogTaskFinishDataShow = false;
            if (this.mDialogTaskFinishData == null || !this.mDialogTaskFinishData.isShowing()) {
                if (this.mDialogData != null && this.mDialogData.isShowing()) {
                    this.mDialogData.cancel();
                    Slog.d(TAG, "oppoAlertDialogTaskTerminationData: cancel DialogData.");
                }
                if (this.mDialogDataMultiKey != null && this.mDialogDataMultiKey.isShowing()) {
                    this.mDialogDataMultiKey.cancel();
                    Slog.d(TAG, "oppoAlertDialogTaskTerminationData: cancel mDialogDataMultiKey.");
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
                builder.setTitle(201590143);
                builder.setPositiveButton(201590059, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        OppoDeviceStorageMonitorService.this.mContext.startActivityAsUser(OppoDeviceStorageMonitorService.this.getFileCleanUpIntent(OppoDeviceStorageMonitorService.SRC_DIALOG_APP), UserHandle.CURRENT);
                    }
                });
                builder.setNegativeButton(201590061, null);
                this.mDialogTaskFinishData = builder.create();
                this.mDialogTaskFinishData.getWindow().setType(2003);
                ignoreMenuHOmeKey(this.mDialogTaskFinishData.getWindow());
                this.mDialogTaskFinishData.setCancelable(false);
                this.mDialogTaskFinishData.show();
                TextView msg = (TextView) this.mDialogTaskFinishData.findViewById(16908299);
                if (msg != null) {
                    msg.setGravity(17);
                }
                Slog.d(TAG, "oppoAlertDialogTaskTerminationData: show...");
                return;
            }
            Slog.d(TAG, "oppoAlertDialogTaskTerminationData: is showing.");
            return;
        }
        Slog.d(TAG, "oppoAlertDialogTaskTerminationData: has showed before.");
    }

    private void oppoAlertDialogTaskTerminationSd() {
        if (this.mAllowDialogTaskFinishSdShow) {
            this.mAllowDialogTaskFinishSdShow = false;
            if (this.mDialogTaskFinishSd == null || !this.mDialogTaskFinishSd.isShowing()) {
                if (this.mDialogSd != null && this.mDialogSd.isShowing()) {
                    this.mDialogSd.cancel();
                    Slog.d(TAG, "oppoAlertDialogTaskTerminationSd: cacel DialogSd");
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
                builder.setTitle(201590144);
                builder.setPositiveButton(201590059, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        OppoDeviceStorageMonitorService.this.mContext.startActivityAsUser(OppoDeviceStorageMonitorService.this.mIntentFileManager, UserHandle.CURRENT);
                    }
                });
                builder.setNegativeButton(201590061, null);
                this.mDialogTaskFinishSd = builder.create();
                this.mDialogTaskFinishSd.getWindow().setType(2003);
                ignoreMenuHOmeKey(this.mDialogTaskFinishSd.getWindow());
                this.mDialogTaskFinishSd.setCancelable(false);
                this.mDialogTaskFinishSd.show();
                TextView msg = (TextView) this.mDialogTaskFinishSd.findViewById(16908299);
                if (msg != null) {
                    msg.setGravity(17);
                }
                Slog.d(TAG, "oppoAlertDialogTaskTerminationSd: show...");
                return;
            }
            Slog.d(TAG, "oppoAlertDialogTaskTerminationSd: is showing.");
            return;
        }
        Slog.d(TAG, "oppoAlertDialogTaskTerminationSd: has showed before.");
    }

    private String byteCountToDisplaySize(long size) {
        String dispalySize = "";
        if (this.mContext == null) {
            return dispalySize;
        }
        try {
            return new ColorUnitConversionUtils(this.mContext).getUnitValue(size);
        } catch (Exception e) {
            Slog.e(TAG, "byteCountToDisplaySize e:" + e);
            return dispalySize;
        }
    }

    private boolean isNormalBoot() {
        String cryptState = SystemProperties.get("vold.decrypt", "trigger_restart_framework");
        if ("trigger_restart_framework".equals(cryptState)) {
            return true;
        }
        Slog.d(TAG, "cryptState = " + cryptState);
        return false;
    }

    private void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    long getMemoryLowThresholdInternal() {
        return this.mDataLowThreshold;
    }

    void oppoCheckStorage() {
        synchronized (this.mLock) {
            if (!isNormalBoot()) {
            } else if (isDeviceProvisioned()) {
                oppoCheckSD();
                oppoCheckData();
            } else {
                Slog.d(TAG, "oppoCheckStorage: DEVICE_PROVISIONED is not set!!!!!!");
            }
        }
    }

    private boolean isDeviceProvisioned() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return false;
    }

    private Intent getFileCleanUpIntent(String source) {
        Intent i = new Intent(OPPO_ACTION_FILE_CLEANUP);
        i.putExtra("enter_from", source);
        i.putExtra(CLEAR_SCAN_MODE, 2);
        i.addFlags(603979776);
        return i;
    }

    boolean oppoSimulationTest(String[] args, PrintWriter pw) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        if (args.length < 1) {
            return false;
        }
        String cmd = args[0];
        if ("testSimuDataLow".equals(cmd)) {
            ColorStorageUtils.setDataSimuMode(101);
            pw.print("Simulate to Data Low!");
        } else if ("testSimuDataFull".equals(cmd)) {
            ColorStorageUtils.setDataSimuMode(102);
            pw.print("Simulate to Data Full!");
        } else if ("testSimuDataEnough".equals(cmd)) {
            ColorStorageUtils.setDataSimuMode(103);
            pw.print("Simulate to Data Not Low!");
        } else if ("testExit".equals(cmd)) {
            ColorStorageUtils.setDataSimuMode(0);
            pw.print("Simulate Exit!");
        } else if (!"testSimuDataRealFull".equals(cmd)) {
            return false;
        } else {
            ColorStorageUtils.setDataSimuMode(104);
            pw.print("Simulate to Data Real Full!");
        }
        reScheduleCheck(0);
        return true;
    }

    void oppoDumpImpl(PrintWriter pw) {
        pw.println("Current OppoDeviceStorageMonitor state:");
        pw.print("  mDataFree=");
        pw.print(ColorStorageUtils.formatBytesLocked(this.mDataFree));
        pw.print("  mTotalData=");
        pw.println(ColorStorageUtils.formatBytesLocked(this.mTotalData));
        pw.print("  mMemLowThreshold=");
        pw.print(ColorStorageUtils.formatBytesLocked(this.mDataLowThreshold));
        pw.print("  mMemFullThreshold=");
        pw.println(ColorStorageUtils.formatBytesLocked(this.mDataFullThreshold));
        pw.print("  mDataLowFlag=");
        pw.print(this.mDataLowFlag);
        pw.print("  mDataFullFlag=");
        pw.print(this.mDataFullFlag);
        pw.print("  DataRealFullFlag=");
        pw.println(this.mDataRealFullFlag);
        pw.println();
        pw.print("  mFreeExternalSd=");
        pw.println(ColorStorageUtils.formatBytesLocked(this.mFreeExternalSd));
        pw.print("  mSdStartTrimThreshold=");
        pw.println(ColorStorageUtils.formatBytesLocked(this.mSdStartTrimThreshold));
        pw.print("  mSdLowFlag=");
        pw.println(this.mSdLowFlag);
    }
}
