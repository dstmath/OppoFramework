package com.android.server.storage;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ColorSystemUpdateDialog;
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
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.UserHandle;
import android.os.storage.DiskInfo;
import android.os.storage.IMountService;
import android.os.storage.IMountService.Stub;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.format.Formatter;
import android.util.DebugUtils;
import android.util.EventLog;
import android.util.Slog;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import com.android.server.EventLogTags;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoProcessManager;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oppo.IElsaManager;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

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
public class OppoDeviceStorageMonitorService extends DeviceStorageMonitorService {
    private static final String ACTION_OPPO_SDCARD_STORAGE_LOW = "android.intent.action.OPPO_SDCARD_STORAGE_LOW";
    private static final String ACTION_OPPO_SDCARD_STORAGE_OK = "android.intent.action.OPPO_SDCARD_STORAGE_OK";
    private static final String ACTION_OPPO_STORAGE_MONITOR_DCS_UPLOADE = "android.intent.action.OPPO_STORAGE_MONITOR_DCS_UPLOADE";
    private static final String ACT_A_KEY_MOVE = "act_a_key_move";
    private static final String ACT_CLEAN_UP_FILE = "act_clean_up_file";
    private static final String ACT_POP_UP = "act_pop_up";
    private static final String ACT_UNINSTALL_APP = "act_uninstall_app";
    private static final boolean ALLOW_UPLOAD_DCS = true;
    private static final String CLEAR_SCAN_MODE = "DEEP_CLEAN";
    private static final int DEBUG_DEBUG_LOGV_MASK = 2;
    private static final int DEBUG_LOCAL_LOGV_MASK = 1;
    private static final int DEFAULT_THRESHOLD_PERCENTAGE = 10;
    private static final long GB_BYTES = 1073741824;
    private static final String ID_DATA_FULL = "dialog_data_full";
    private static final String ID_DATA_LOW = "dialog_data_low_no_AKeyMove";
    private static final String ID_DATA_LOW_WITH_AKEYMOVE = "dialog_data_low_with_AKeyMove";
    private static final String ID_DATA_SD_LOW_STATE = "data_sd_low";
    private static final String ID_SD_LOW = "dialog_sd_low";
    private static final String ID_SD_MOUNT_STATE = "sd_mounted";
    private static final long INTERVAL_DIALOG_DATA = 1800000;
    private static final int MAX_INTERVAL = 21600000;
    private static final long MB_BYTES = 1048576;
    private static final int MODE_DEEP = 2;
    private static final String OPPO_ACTION_DIALOG_DATA = "oppo.intent.action.DIALOG_DATA";
    private static final String OPPO_ACTION_DIALOG_SD = "oppo.intent.action.DIALOG_SD";
    private static final String OPPO_ACTION_FILE_CLEANUP = "com.oppo.cleandroid.ui.ClearMainActivity";
    private static final String OPPO_ACTION_ONE_KEY_MOVE = "com.oppo.filemanager.akeytomove.AKeyToMoveActivity";
    private static final String OPPO_ACTION_OPEN_FILEMANAGER = "android.intent.action.OPEN_FILEMANAGER";
    private static final String OPPO_ACTION_SHOW_LOW_STORAGE_ALERT = "com.oppo.showLowStorageAlert";
    private static final String OPPO_ACTION_TASK_TERMINATION = "oppo.intent.action.TASK_TERMINATION_FOR_LOW_STORAGE";
    private static final String OPPO_ACTION_TOMORROW_ZERO_OCLOCK = "oppo.intent.action.TOMORROW_ZERO_OCLOCK";
    private static final int OPPO_DATA_CRITICAL_LOW = 2;
    private static final int OPPO_DATA_FULL = 6;
    private static final int OPPO_DATA_NOT_FULL = 5;
    private static final long OPPO_DEFAULT_CHECK_INTERVAL = 30000;
    private static final int OPPO_DEVICE_SD_MONITOR = 1;
    private static final int OPPO_DEVICE_SD_MOUNT = 3;
    private static final int OPPO_DEVICE_SD_UNMOUNT = 4;
    private static final long OPPO_FULL_THRESHOLD_MB = 10485760;
    private static final int OPPO_MONITOR_INTERVAL = 30;
    private static final long OPPO_SD_NOT_ENOUGH_MB = 104857600;
    private static final long OPPO_SD_NOT_ENOUGH_TRIM_MB = 52428800;
    private static final long OPPO_SHORT_CHECK_INTERVAL = 10000;
    private static final int OPPO_SHORT_INTERVAL = 10;
    private static final String TAG = "OppoDeviceStorageMonitorService";
    private static final long THRESHOLD_DELTA_DATA_LOW = 104857600;
    private static final long THRESHOLD_SD_SUFFICIENT = 1073741824;
    private static final long TIMESTAMP_BOOT_COMPLETE = 120000;
    private static boolean localLOGV;
    private static AtomicBoolean mCriticalLowDataFlag;
    private static long mLogFlag;
    private List<String> listTaskTermData;
    private List<String> listTaskTermSd;
    private boolean mAllowDialogTaskFinishDataShow;
    private boolean mAllowDialogTaskFinishSdShow;
    private BroadcastReceiver mBroadcastReceiver;
    private int mCntNotifyData;
    private Context mContext;
    private AlertDialog mDialogData;
    private PendingIntent mDialogDataIntent;
    private ColorSystemUpdateDialog mDialogDataMultiKey;
    private AlertDialog mDialogSd;
    private PendingIntent mDialogSdIntent;
    private AlertDialog mDialogTaskFinishData;
    private AlertDialog mDialogTaskFinishSd;
    private final StatFs mFileStatsData;
    private long mFreeExternalSd;
    private WorkerHandler mHandler;
    private HandlerThread mHandlerThread;
    private Intent mIntentDcs;
    private Intent mIntentFileCleanUP;
    private Intent mIntentFileManager;
    private Intent mIntentOneKeyMove;
    private Intent mIntentPackageStorage;
    private boolean mIsSdMounted;
    private boolean mLocaleChanged;
    private final Object mLock;
    private boolean mLowDataFlag;
    private boolean mLowSdFlag;
    private IMountService mMountService;
    private boolean mNotificationDataShowed;
    private long mSdStartTrimThreshold;
    private Intent mSdStorageLowIntent;
    private Intent mSdStorageOkIntent;
    private boolean mSdSufficient;
    private final StorageEventListener mStorageListener;
    private PendingIntent mTomorrowIntent;
    private VolumeInfo mVolumeExternalSd;

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            synchronized (OppoDeviceStorageMonitorService.this.mLock) {
                if (msg.what == 1) {
                    OppoDeviceStorageMonitorService.this.OppoCheckSD();
                    OppoDeviceStorageMonitorService.this.mHandler.sendEmptyMessageDelayed(1, OppoDeviceStorageMonitorService.OPPO_DEFAULT_CHECK_INTERVAL);
                } else if (msg.what == 2) {
                    OppoDeviceStorageMonitorService.this.OppoAlertDialogData();
                    OppoDeviceStorageMonitorService.this.OppoNotificationData();
                } else if (msg.what == 3) {
                    OppoDeviceStorageMonitorService.this.OppoCheckSD();
                    OppoDeviceStorageMonitorService.this.mHandler.sendEmptyMessageDelayed(1, OppoDeviceStorageMonitorService.OPPO_DEFAULT_CHECK_INTERVAL);
                } else if (msg.what == 4) {
                    OppoDeviceStorageMonitorService.this.OppoSdUnmounted();
                } else if (msg.what == 6) {
                    super.-wrap0();
                    if (OppoDeviceStorageMonitorService.this.mDialogData != null && OppoDeviceStorageMonitorService.this.mDialogData.isShowing()) {
                        OppoDeviceStorageMonitorService.this.mDialogData.cancel();
                    }
                    if (OppoDeviceStorageMonitorService.this.mDialogDataMultiKey != null && OppoDeviceStorageMonitorService.this.mDialogDataMultiKey.isShowing()) {
                        OppoDeviceStorageMonitorService.this.mDialogDataMultiKey.cancel();
                    }
                    OppoDeviceStorageMonitorService.this.mLowDataFlag = false;
                    OppoDeviceStorageMonitorService.this.mAllowDialogTaskFinishDataShow = true;
                    OppoDeviceStorageMonitorService.this.mNotificationDataShowed = false;
                    OppoDeviceStorageMonitorService.this.cancelAlarmDialogData();
                    Slog.i(OppoDeviceStorageMonitorService.TAG, "Canceling low data notification");
                } else if (msg.what == 5) {
                    EventLog.writeEvent(EventLogTags.LOW_STORAGE, OppoDeviceStorageMonitorService.this.mFreeMem);
                    if (OppoDeviceStorageMonitorService.this.OppoAlertDialogData()) {
                        OppoDeviceStorageMonitorService.this.mCntNotifyData = 1;
                        OppoDeviceStorageMonitorService.this.scheduleAlarmDialogData(0);
                    } else {
                        OppoDeviceStorageMonitorService.this.mCntNotifyData = 0;
                        OppoDeviceStorageMonitorService.this.scheduleAlarmDialogData(1800000);
                    }
                    OppoDeviceStorageMonitorService.this.OppoNotificationData();
                    OppoDeviceStorageMonitorService.this.mContext.sendStickyBroadcastAsUser(OppoDeviceStorageMonitorService.this.mStorageLowIntent, UserHandle.ALL);
                    OppoDeviceStorageMonitorService.this.mLowDataFlag = true;
                    Slog.i(OppoDeviceStorageMonitorService.TAG, "Sending low data notification");
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.storage.OppoDeviceStorageMonitorService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.storage.OppoDeviceStorageMonitorService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.storage.OppoDeviceStorageMonitorService.<clinit>():void");
    }

    private void OppoSdUnmounted() {
        this.mIsSdMounted = false;
        Slog.d(TAG, "OppoSdUnmounted");
        if (this.mLowSdFlag) {
            this.mLowSdFlag = false;
            Slog.d(TAG, "OppoSdUnmounted: Cancelling notification");
            sdcancelNotification();
        }
        reactDataLowWarning();
    }

    private void OppoSdmounted() {
        OppoCheckSD();
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
        Slog.d(TAG, "getSdFreeSpace: freeExternalSd = " + (freeExternalSd / MB_BYTES) + "MB");
        return freeExternalSd;
    }

    private long getDataFreeSpace() {
        long freeDataSpace = -1;
        try {
            this.mFileStatsData.restat(Environment.getDataDirectory().getAbsolutePath());
            freeDataSpace = ((long) this.mFileStatsData.getAvailableBlocks()) * ((long) this.mFileStatsData.getBlockSize());
        } catch (IllegalArgumentException e) {
            Slog.d(TAG, "getDataFreeSpace: IllegalArgumentException.");
        }
        Slog.d(TAG, "getDataFreeSpace: freeDataSpace = " + (freeDataSpace / MB_BYTES) + "MB");
        return freeDataSpace;
    }

    private final void OppoCheckSD() {
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
                if (localLOGV) {
                    Slog.v(TAG, "mFreeExternalSd = " + (this.mFreeExternalSd / MB_BYTES) + "MB" + ", tid=" + Thread.currentThread().getId());
                }
                if (localLOGV) {
                    Slog.v(TAG, "mSdStartTrimThreshold = " + (this.mSdStartTrimThreshold / MB_BYTES) + "MB");
                }
                if (this.mFreeExternalSd < this.mSdStartTrimThreshold) {
                    if (!this.mLowSdFlag) {
                        Slog.i(TAG, "OppoCheckSD: Running low on SDCARD. Sending notification");
                        sdsendNotification();
                    }
                } else if (this.mLowSdFlag) {
                    Slog.i(TAG, "OppoCheckSD: SDCARD available. Cancelling notification");
                    sdcancelNotification();
                }
                boolean sdSufficientChanged = false;
                if (this.mFreeExternalSd >= 1073741824) {
                    if (!this.mSdSufficient) {
                        this.mSdSufficient = true;
                        sdSufficientChanged = true;
                    }
                } else if (this.mSdSufficient) {
                    this.mSdSufficient = false;
                    sdSufficientChanged = true;
                }
                if (sdSufficientChanged || sdStateChange) {
                    reactDataLowWarning();
                }
            }
        }
    }

    private void reactDataLowWarning() {
        if (this.mLowDataFlag) {
            if (this.mNotificationDataShowed) {
                OppoCancelNotification(1);
                OppoNotificationData();
            }
            if (this.mDialogData != null && this.mDialogData.isShowing()) {
                this.mDialogData.cancel();
                OppoAlertDialogData();
            }
            if (this.mDialogDataMultiKey != null && this.mDialogDataMultiKey.isShowing()) {
                this.mDialogDataMultiKey.cancel();
                OppoAlertDialogData();
            }
        }
    }

    public OppoDeviceStorageMonitorService(Context context) {
        super(context);
        this.mLowSdFlag = false;
        this.mLowDataFlag = false;
        this.mSdSufficient = true;
        this.mIsSdMounted = false;
        this.mLocaleChanged = false;
        this.mIntentDcs = null;
        this.mDialogData = null;
        this.mDialogDataMultiKey = null;
        this.mDialogSd = null;
        this.mDialogTaskFinishData = null;
        this.mDialogTaskFinishSd = null;
        this.mAllowDialogTaskFinishDataShow = true;
        this.mAllowDialogTaskFinishSdShow = true;
        this.mNotificationDataShowed = false;
        this.mMountService = null;
        this.mLock = new Object();
        this.mVolumeExternalSd = null;
        this.listTaskTermData = new ArrayList();
        this.listTaskTermSd = new ArrayList();
        this.mStorageListener = new StorageEventListener() {
            public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
                DiskInfo diskInfo = vol.getDisk();
                if (OppoDeviceStorageMonitorService.localLOGV) {
                    Slog.d(OppoDeviceStorageMonitorService.TAG, "onVolumeStateChanged: id=" + vol.getId() + ", path=" + vol.path + ", type=" + DebugUtils.valueToString(VolumeInfo.class, "TYPE_", vol.getType()) + ", oldState=" + DebugUtils.valueToString(VolumeInfo.class, "STATE_", oldState) + ", newState=" + DebugUtils.valueToString(VolumeInfo.class, "STATE_", newState) + ", diskInfo=" + diskInfo);
                }
                if (diskInfo != null && diskInfo.isSd()) {
                    if (oldState != 2 && newState == 2) {
                        synchronized (OppoDeviceStorageMonitorService.this.mLock) {
                            OppoDeviceStorageMonitorService.this.mVolumeExternalSd = vol;
                            OppoDeviceStorageMonitorService.this.mHandler.removeMessages(1);
                            OppoDeviceStorageMonitorService.this.mHandler.removeMessages(3);
                            OppoDeviceStorageMonitorService.this.mHandler.removeMessages(4);
                            OppoDeviceStorageMonitorService.this.mHandler.sendEmptyMessageDelayed(3, 100);
                        }
                        Slog.d(OppoDeviceStorageMonitorService.TAG, "onVolumeStateChanged: external TF card mounted. id=" + vol.getId() + ", path=" + vol.path + ", oldState=" + DebugUtils.valueToString(VolumeInfo.class, "STATE_", oldState) + ", newState=" + DebugUtils.valueToString(VolumeInfo.class, "STATE_", newState));
                    } else if (newState != 2 && oldState == 2) {
                        synchronized (OppoDeviceStorageMonitorService.this.mLock) {
                            OppoDeviceStorageMonitorService.this.mVolumeExternalSd = null;
                            OppoDeviceStorageMonitorService.this.mHandler.removeMessages(1);
                            OppoDeviceStorageMonitorService.this.mHandler.removeMessages(3);
                            OppoDeviceStorageMonitorService.this.mHandler.removeMessages(4);
                            OppoDeviceStorageMonitorService.this.mHandler.sendEmptyMessage(4);
                        }
                        Slog.d(OppoDeviceStorageMonitorService.TAG, "onVolumeStateChanged: external TF card unmounted. id=" + vol.getId() + ", path=" + vol.path + ", oldState=" + DebugUtils.valueToString(VolumeInfo.class, "STATE_", oldState) + ", newState=" + DebugUtils.valueToString(VolumeInfo.class, "STATE_", newState));
                    }
                }
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.intent.action.LOCALE_CHANGED")) {
                    if (OppoDeviceStorageMonitorService.this.mLowDataFlag && OppoDeviceStorageMonitorService.this.mNotificationDataShowed) {
                        OppoDeviceStorageMonitorService.this.OppoCancelNotification(1);
                        OppoDeviceStorageMonitorService.this.OppoNotificationData();
                    }
                    OppoDeviceStorageMonitorService.this.mLocaleChanged = true;
                } else if (action.equals(OppoDeviceStorageMonitorService.OPPO_ACTION_SHOW_LOW_STORAGE_ALERT)) {
                    OppoDeviceStorageMonitorService.this.OppoAlertDialogData();
                } else if (action.equals("android.intent.action.DATE_CHANGED")) {
                    String str = IElsaManager.EMPTY_PACKAGE;
                    if (OppoDeviceStorageMonitorService.this.mLowDataFlag) {
                        str = str.concat(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                    } else {
                        str = str.concat("0");
                    }
                    if (OppoDeviceStorageMonitorService.this.mLowSdFlag) {
                        str = str.concat(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                    } else {
                        str = str.concat("0");
                    }
                    OppoDeviceStorageMonitorService.this.uploadDcsKvEvent(OppoDeviceStorageMonitorService.ID_DATA_SD_LOW_STATE, str, true);
                    if (OppoDeviceStorageMonitorService.this.mIsSdMounted) {
                        OppoDeviceStorageMonitorService.this.uploadDcsKvEvent(OppoDeviceStorageMonitorService.ID_SD_MOUNT_STATE, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON, true);
                    } else {
                        OppoDeviceStorageMonitorService.this.uploadDcsKvEvent(OppoDeviceStorageMonitorService.ID_SD_MOUNT_STATE, "0", true);
                    }
                } else if (action.equals(OppoDeviceStorageMonitorService.OPPO_ACTION_TASK_TERMINATION)) {
                    String pkg = intent.getStringExtra("package");
                    String space = intent.getStringExtra("space");
                    if (pkg == null || space == null) {
                        Slog.d(OppoDeviceStorageMonitorService.TAG, "OPPO_ACTION_TASK_TERMINATION. pkg=" + pkg + ", space=" + space);
                    } else if ("Phone".equals(space)) {
                        if (OppoDeviceStorageMonitorService.this.listTaskTermData.contains(pkg)) {
                            Slog.d(OppoDeviceStorageMonitorService.TAG, "OPPO_ACTION_TASK_TERMINATION. Phone. pkg(" + pkg + ") has showed TaskTermData before.");
                            return;
                        }
                        long freeDataSpace = OppoDeviceStorageMonitorService.this.getDataFreeSpace();
                        if (freeDataSpace < 0 || freeDataSpace > OppoDeviceStorageMonitorService.this.mMemLowThreshold + 104857600) {
                            Slog.d(OppoDeviceStorageMonitorService.TAG, "OPPO_ACTION_TASK_TERMINATION. freeDataSpace=" + (freeDataSpace / OppoDeviceStorageMonitorService.MB_BYTES) + "MB. ignore.");
                        } else {
                            OppoDeviceStorageMonitorService.this.listTaskTermData.add(pkg);
                            OppoDeviceStorageMonitorService.this.OppoAlertDialogTaskTerminationData();
                        }
                    } else if ("sd".equals(space)) {
                        if (OppoDeviceStorageMonitorService.this.listTaskTermSd.contains(pkg)) {
                            Slog.d(OppoDeviceStorageMonitorService.TAG, "OPPO_ACTION_TASK_TERMINATION. sd. pkg(" + pkg + ") has showed TaskTermSd before.");
                            return;
                        }
                        long freeSdSpace = OppoDeviceStorageMonitorService.this.getSdFreeSpace();
                        if (freeSdSpace < 0 || freeSdSpace > OppoDeviceStorageMonitorService.this.mSdStartTrimThreshold) {
                            Slog.d(OppoDeviceStorageMonitorService.TAG, "OPPO_ACTION_TASK_TERMINATION. freeSdSpace=" + (freeSdSpace / OppoDeviceStorageMonitorService.MB_BYTES) + "MB. ignore.");
                        } else {
                            OppoDeviceStorageMonitorService.this.listTaskTermSd.add(pkg);
                            OppoDeviceStorageMonitorService.this.OppoAlertDialogTaskTerminationSd();
                        }
                    }
                } else if (OppoDeviceStorageMonitorService.OPPO_ACTION_DIALOG_DATA.equals(action)) {
                    if (OppoDeviceStorageMonitorService.this.mCntNotifyData >= 2) {
                        Slog.d(OppoDeviceStorageMonitorService.TAG, "OPPO_ACTION_DIALOG_DATA. CntNotifyData=" + OppoDeviceStorageMonitorService.this.mCntNotifyData);
                        return;
                    }
                    if (OppoDeviceStorageMonitorService.this.OppoAlertDialogData()) {
                        OppoDeviceStorageMonitorService oppoDeviceStorageMonitorService = OppoDeviceStorageMonitorService.this;
                        oppoDeviceStorageMonitorService.mCntNotifyData = oppoDeviceStorageMonitorService.mCntNotifyData + 1;
                    }
                    OppoDeviceStorageMonitorService.this.OppoNotificationData();
                    if (OppoDeviceStorageMonitorService.this.mCntNotifyData < 2) {
                        OppoDeviceStorageMonitorService.this.scheduleAlarmDialogData(0);
                    }
                } else if (OppoDeviceStorageMonitorService.OPPO_ACTION_DIALOG_SD.equals(action)) {
                    if (!OppoDeviceStorageMonitorService.this.OppoAlertDialogSd()) {
                        OppoDeviceStorageMonitorService.this.scheduleAlarmDialogSd(1800000);
                    }
                } else if (OppoDeviceStorageMonitorService.OPPO_ACTION_TOMORROW_ZERO_OCLOCK.equals(action)) {
                    OppoDeviceStorageMonitorService.this.mCntNotifyData = 0;
                    OppoDeviceStorageMonitorService.this.listTaskTermData.clear();
                    OppoDeviceStorageMonitorService.this.listTaskTermSd.clear();
                    if (OppoDeviceStorageMonitorService.this.mLowDataFlag) {
                        OppoDeviceStorageMonitorService.this.scheduleAlarmDialogData(0);
                    }
                    OppoDeviceStorageMonitorService.this.scheduleAlarmTomorrowZeroOclock();
                }
            }
        };
        this.mContext = context;
        Slog.i(TAG, "init!!!");
        this.mHandlerThread = new HandlerThread("OppoDeviceStorageMonitor");
        this.mHandlerThread.start();
        this.mHandler = new WorkerHandler(this.mHandlerThread.getLooper());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        filter.addAction(OPPO_ACTION_SHOW_LOW_STORAGE_ALERT);
        filter.addAction("android.intent.action.DATE_CHANGED");
        filter.addAction(OPPO_ACTION_TASK_TERMINATION);
        filter.addAction(OPPO_ACTION_DIALOG_DATA);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, this.mHandler);
        this.mIntentFileCleanUP = new Intent(OPPO_ACTION_FILE_CLEANUP);
        this.mIntentFileCleanUP.putExtra("enter_from", "StorageMonitor");
        this.mIntentFileCleanUP.putExtra(CLEAR_SCAN_MODE, 2);
        this.mIntentFileCleanUP.addFlags(335544320);
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
        this.mDialogDataIntent = PendingIntent.getBroadcast(context, 0, new Intent(OPPO_ACTION_DIALOG_DATA), 0);
        this.mDialogSdIntent = PendingIntent.getBroadcast(context, 0, new Intent(OPPO_ACTION_DIALOG_SD), 0);
        this.mTomorrowIntent = PendingIntent.getBroadcast(context, 0, new Intent(OPPO_ACTION_TOMORROW_ZERO_OCLOCK), 0);
        this.mSdStartTrimThreshold = OPPO_SD_NOT_ENOUGH_TRIM_MB;
        this.mMountService = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
        ((StorageManager) context.getSystemService(StorageManager.class)).registerListener(this.mStorageListener);
        this.mFileStatsData = new StatFs(Environment.getDataDirectory().getAbsolutePath());
    }

    void sendNotification() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(5);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(5));
            return;
        }
        Slog.e(TAG, "sendNotification: mHandler is null!!!");
    }

    void cancelNotification() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(6);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(6));
            return;
        }
        Slog.e(TAG, "cancelNotification: mHandler is null!!!");
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
            alarmTime = now + ((long) new Random().nextInt(delta));
        }
        ((AlarmManager) this.mContext.getSystemService("alarm")).setExact(1, alarmTime, this.mDialogDataIntent);
        Calendar cTmp = Calendar.getInstance();
        cTmp.setTimeInMillis(alarmTime);
        Slog.d(TAG, "scheduleAlarmDialogData: alarmTime= " + cTmp.getTime());
    }

    private void cancelAlarmDialogData() {
        ((AlarmManager) this.mContext.getSystemService("alarm")).cancel(this.mDialogDataIntent);
    }

    private void scheduleAlarmDialogSd(long delay) {
        long alarmTime = System.currentTimeMillis() + delay;
        ((AlarmManager) this.mContext.getSystemService("alarm")).setExact(1, alarmTime, this.mDialogSdIntent);
        Calendar cTmp = Calendar.getInstance();
        cTmp.setTimeInMillis(alarmTime);
        Slog.d(TAG, "scheduleAlarmDialogSd: alarmTime= " + cTmp.getTime());
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

    private void OppoNotificationData() {
        if (!this.mNotificationDataShowed) {
            PendingIntent intent;
            CharSequence details;
            NotificationManager mNotificationMgr = (NotificationManager) this.mContext.getSystemService(NotificationManagerService.NOTIFICATON_TITLE_NAME);
            CharSequence title = this.mContext.getText(201590065);
            if (this.mIsSdMounted && this.mSdSufficient) {
                intent = PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent(OPPO_ACTION_SHOW_LOW_STORAGE_ALERT), 0, UserHandle.CURRENT);
                details = this.mContext.getText(201590067);
            } else {
                intent = PendingIntent.getActivityAsUser(this.mContext, 0, this.mIntentFileCleanUP, 0, null, UserHandle.CURRENT);
                details = this.mContext.getText(201590067);
            }
            Notification notification = new Builder(this.mContext).setContentTitle(title).setContentText(details).setSmallIcon(201852148).setContentIntent(intent).build();
            notification.flags |= 32;
            mNotificationMgr.notifyAsUser(null, 1, notification, UserHandle.ALL);
            this.mNotificationDataShowed = true;
            Slog.d(TAG, "OppoNotificationData: send notification.");
        }
    }

    private void OppoCancelNotification(int id) {
        ((NotificationManager) this.mContext.getSystemService(NotificationManagerService.NOTIFICATON_TITLE_NAME)).cancelAsUser(null, id, UserHandle.ALL);
        if (id == 1) {
            this.mNotificationDataShowed = false;
        }
    }

    private void sdsendNotification() {
        if (!OppoAlertDialogSd()) {
            scheduleAlarmDialogSd(1800000);
        }
        this.mLowSdFlag = true;
        this.mContext.sendStickyBroadcastAsUser(this.mSdStorageLowIntent, UserHandle.ALL);
    }

    private void sdcancelNotification() {
        if (this.mDialogSd != null && this.mDialogSd.isShowing()) {
            this.mDialogSd.cancel();
        }
        this.mLowSdFlag = false;
        this.mContext.removeStickyBroadcastAsUser(this.mSdStorageLowIntent, UserHandle.ALL);
        this.mContext.sendBroadcastAsUser(this.mSdStorageOkIntent, UserHandle.ALL);
        this.mAllowDialogTaskFinishSdShow = true;
        cancelAlarmDialogSd();
    }

    void sendDataCriticalLow() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(2);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
        } else {
            Slog.i(TAG, "sendDataCriticalLow: mHandler is null!!!");
        }
        mCriticalLowDataFlag.set(true);
    }

    void cancelDataCriticalLow() {
        mCriticalLowDataFlag.set(false);
    }

    private boolean OppoAlertDialogData() {
        if (this.mDialogTaskFinishData == null || !this.mDialogTaskFinishData.isShowing()) {
            if (this.mIsSdMounted && this.mSdSufficient) {
                if (this.mDialogData != null && this.mDialogData.isShowing()) {
                    this.mDialogData.cancel();
                    Slog.d(TAG, "OppoAlertDialogData: cancel old DialogData.");
                }
                if (this.mDialogDataMultiKey == null || !this.mDialogDataMultiKey.isShowing() || !mCriticalLowDataFlag.get() || this.mLocaleChanged) {
                    this.mDialogDataMultiKey = new ColorSystemUpdateDialog.Builder(this.mContext).setTitle(201590065).setMessage(201590089).setItems(201786390, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    OppoDeviceStorageMonitorService.this.mContext.startActivityAsUser(OppoDeviceStorageMonitorService.this.mIntentFileCleanUP, UserHandle.CURRENT);
                                    OppoDeviceStorageMonitorService.this.uploadDcsKvEvent(OppoDeviceStorageMonitorService.ID_DATA_LOW_WITH_AKEYMOVE, OppoDeviceStorageMonitorService.ACT_CLEAN_UP_FILE, false);
                                    return;
                                case 1:
                                    OppoDeviceStorageMonitorService.this.mContext.startActivityAsUser(OppoDeviceStorageMonitorService.this.mIntentOneKeyMove, UserHandle.CURRENT);
                                    OppoDeviceStorageMonitorService.this.uploadDcsKvEvent(OppoDeviceStorageMonitorService.ID_DATA_LOW_WITH_AKEYMOVE, OppoDeviceStorageMonitorService.ACT_A_KEY_MOVE, false);
                                    return;
                                default:
                                    return;
                            }
                        }
                    }).setCancelable(false).create();
                    this.mDialogDataMultiKey.getWindow().setType(2003);
                    ignoreMenuHOmeKey(this.mDialogDataMultiKey.getWindow());
                    this.mDialogDataMultiKey.show();
                    uploadDcsKvEvent(ID_DATA_LOW_WITH_AKEYMOVE, ACT_POP_UP, false);
                    Slog.d(TAG, "OppoAlertDialogData: show DialogDataMultiKey.");
                } else {
                    Slog.d(TAG, "OppoAlertDialogData: Data is critical low. DialogDataMultiKey is showing.");
                    return false;
                }
            }
            if (this.mDialogDataMultiKey != null && this.mDialogDataMultiKey.isShowing()) {
                this.mDialogDataMultiKey.cancel();
                Slog.d(TAG, "OppoAlertDialogData: cancel old DialogDataMultiKey.");
            }
            if (this.mDialogData == null || !this.mDialogData.isShowing() || !mCriticalLowDataFlag.get() || this.mLocaleChanged) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
                builder.setTitle(201590065);
                builder.setMessage(201590066);
                builder.setPositiveButton(201590060, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        OppoDeviceStorageMonitorService.this.mContext.startActivityAsUser(OppoDeviceStorageMonitorService.this.mIntentFileCleanUP, UserHandle.CURRENT);
                        OppoDeviceStorageMonitorService.this.uploadDcsKvEvent(OppoDeviceStorageMonitorService.ID_DATA_LOW, OppoDeviceStorageMonitorService.ACT_CLEAN_UP_FILE, false);
                    }
                });
                builder.setNegativeButton(201590061, null);
                this.mDialogData = builder.create();
                this.mDialogData.getWindow().setType(2003);
                this.mDialogData.setCancelable(false);
                ignoreMenuHOmeKey(this.mDialogData.getWindow());
                this.mDialogData.show();
                uploadDcsKvEvent(ID_DATA_LOW, ACT_POP_UP, false);
                Slog.d(TAG, "OppoAlertDialogData: show DialogData.");
            } else {
                Slog.d(TAG, "OppoAlertDialogData: Data is critical low. DialogData is showing.");
                return false;
            }
            this.mLocaleChanged = false;
            return true;
        }
        Slog.d(TAG, "OppoAlertDialogData: DialogTaskFinishdata is showing.");
        return false;
    }

    private boolean OppoAlertDialogSd() {
        if (this.mDialogTaskFinishSd == null || !this.mDialogTaskFinishSd.isShowing()) {
            if (this.mDialogSd != null && this.mDialogSd.isShowing()) {
                this.mDialogSd.cancel();
                Slog.i(TAG, "OppoAlertDialogSd: cacel old DialogSd");
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
            builder.setTitle(201590063);
            builder.setPositiveButton(201590059, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    OppoDeviceStorageMonitorService.this.mContext.startActivityAsUser(OppoDeviceStorageMonitorService.this.mIntentFileManager, UserHandle.CURRENT);
                    OppoDeviceStorageMonitorService.this.uploadDcsKvEvent(OppoDeviceStorageMonitorService.ID_SD_LOW, OppoDeviceStorageMonitorService.ACT_CLEAN_UP_FILE, false);
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
            uploadDcsKvEvent(ID_SD_LOW, ACT_POP_UP, false);
            Slog.d(TAG, "OppoAlertDialogSd: show DialogSd.");
            return true;
        }
        Slog.d(TAG, "OppoAlertDialogSd: DialogTaskFinishSd is showing.");
        return false;
    }

    private void uploadDcsKvEvent(String id, String act, boolean force) {
        if (!mCriticalLowDataFlag.get() || force) {
            this.mIntentDcs = new Intent(ACTION_OPPO_STORAGE_MONITOR_DCS_UPLOADE);
            this.mIntentDcs.putExtra("eventId", id);
            this.mIntentDcs.putExtra("act", act);
            this.mIntentDcs.addFlags(67108864);
            this.mContext.sendBroadcastAsUser(this.mIntentDcs, UserHandle.ALL);
        }
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

    private void OppoAlertDialogTaskTerminationData() {
        if (this.mAllowDialogTaskFinishDataShow) {
            this.mAllowDialogTaskFinishDataShow = false;
            if (this.mDialogTaskFinishData == null || !this.mDialogTaskFinishData.isShowing()) {
                if (this.mDialogData != null && this.mDialogData.isShowing()) {
                    this.mDialogData.cancel();
                    Slog.d(TAG, "OppoAlertDialogTaskTerminationData: cancel DialogData.");
                }
                if (this.mDialogDataMultiKey != null && this.mDialogDataMultiKey.isShowing()) {
                    this.mDialogDataMultiKey.cancel();
                    Slog.d(TAG, "OppoAlertDialogTaskTerminationData: cancel mDialogDataMultiKey.");
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
                builder.setTitle(201590143);
                builder.setPositiveButton(201590059, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        OppoDeviceStorageMonitorService.this.mContext.startActivityAsUser(OppoDeviceStorageMonitorService.this.mIntentFileCleanUP, UserHandle.CURRENT);
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
                Slog.d(TAG, "OppoAlertDialogTaskTerminationData: show...");
                return;
            }
            Slog.d(TAG, "OppoAlertDialogTaskTerminationData: is showing.");
            return;
        }
        Slog.d(TAG, "OppoAlertDialogTaskTerminationData: has showed before.");
    }

    private void OppoAlertDialogTaskTerminationSd() {
        if (this.mAllowDialogTaskFinishSdShow) {
            this.mAllowDialogTaskFinishSdShow = false;
            if (this.mDialogTaskFinishSd == null || !this.mDialogTaskFinishSd.isShowing()) {
                if (this.mDialogSd != null && this.mDialogSd.isShowing()) {
                    this.mDialogSd.cancel();
                    Slog.d(TAG, "OppoAlertDialogTaskTerminationSd: cacel DialogSd");
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
                Slog.d(TAG, "OppoAlertDialogTaskTerminationSd: show...");
                return;
            }
            Slog.d(TAG, "OppoAlertDialogTaskTerminationSd: is showing.");
            return;
        }
        Slog.d(TAG, "OppoAlertDialogTaskTerminationSd: has showed before.");
    }

    private long parseLogMask(String[] args, long defaultValue) {
        long flag = defaultValue;
        try {
            if (args.length != 1) {
                return flag;
            }
            if (args[0].toLowerCase().startsWith("0x")) {
                return Long.parseLong(args[0].substring(2), 16);
            }
            return Long.parseLong(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return flag;
        }
    }

    private void updateLogMask(PrintWriter pw, String[] args) {
        boolean z;
        mLogFlag = parseLogMask(args, mLogFlag);
        if ((mLogFlag & 1) != 0) {
            z = true;
        } else {
            z = false;
        }
        localLOGV = z;
        pw.print("  mLogFlag=0x" + Long.toHexString(mLogFlag));
        Object[] objArr = new Object[2];
        objArr[0] = Integer.valueOf(1);
        objArr[1] = Boolean.valueOf(localLOGV);
        pw.print(String.format("  localLOGV(0x%1$h)=%2$b", objArr));
        pw.println(IElsaManager.EMPTY_PACKAGE);
    }

    void dumpImpl(PrintWriter pw) {
        super.dumpImpl(pw);
        pw.println("Current OppoDeviceStorageMonitor state:");
        pw.print("  mFreeExternalSd=");
        pw.println(Formatter.formatFileSize(this.mContext, this.mFreeExternalSd));
        pw.print("  mSdStartTrimThreshold=");
        pw.println(Formatter.formatFileSize(this.mContext, this.mSdStartTrimThreshold));
        pw.print("  mLowSdFlag=");
        pw.println(this.mLowSdFlag);
    }
}
