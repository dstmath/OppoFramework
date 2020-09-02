package com.android.server.storage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.util.Slog;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.EventLogTags;
import com.android.server.am.ColorMultiAppManagerService;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorTypeCastingHelper;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import oppo.util.OppoStatistics;

public class ColorDeviceStorageMonitorService extends DeviceStorageMonitorService {
    private static final String CLEAR_SCAN_MODE = "DEEP_CLEAN";
    private static final String DATA_SPACE_MONITOR_CURVE_SWITCH = "data_space_monitor_curve_switch";
    private static final long DEFAULT_LOG_DELTA_BYTES = 67108864;
    private static final long DELAY_FIRST_CHECK = 7000;
    private static final String ID_DATA_FULL_ENTER = "data_storage_full_enter";
    private static final String ID_DATA_FULL_EXIT = "data_storage_full_exit";
    private static final String ID_DATA_LOW_ENTER = "data_storage_low_enter";
    private static final String ID_DATA_LOW_EXIT = "data_storage_low_exit";
    public static final long KB_BYTES = 1024;
    private static final int MAX_FREE_STORAGE_FAIL = 3;
    public static final long MB_BYTES = 1048576;
    private static final int MODE_DEEP = 2;
    private static final long NUMBER_THREE = 3;
    private static final String OPPO_ACTION_DATA_LEVEL_CHANGE = "oppo.intent.action.DATA_LEVEL_CHANGE";
    private static final String OPPO_ACTION_FILE_CLEANUP = "com.oppo.cleandroid.ui.ClearMainActivity";
    private static final String PERMISSION_OPPO_COMPONENT_SAFE = "oppo.permission.OPPO_COMPONENT_SAFE";
    private static final String PROP_DATA_BYTES = "sys.data.free.bytes";
    private static final String PROP_DATA_LEVEL = "sys.data.free.level";
    private static final String PROP_DATA_LEVEL_CHANGE_BOOT = "sys.data.level.boot";
    private static final String SRC_NOTIFICATION_FULL = "StorageNotificationFull";
    private static final String SRC_NOTIFICATION_LOW = "StorageNotificationLow";
    private static final String TAG = "DeviceStorageMonitor";
    private static final long THRESHOLD_DATA_FULL = 52428800;
    public static final long THRESHOLD_DELTA_DATA_LOW = 314572800;
    private static final boolean sLocalLOGV = false;
    private OppoBaseDeviceStorageMonitorService mBase = typeCasting(this);
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.storage.ColorDeviceStorageMonitorService.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.LOCALE_CHANGED") && ColorDeviceStorageMonitorService.this.mDataLowFlag && ColorDeviceStorageMonitorService.this.mNotificationDataShowed) {
                ColorDeviceStorageMonitorService.this.oppoCancelNotification(23);
                ColorDeviceStorageMonitorService.this.oppoNotificationData();
            }
        }
    };
    private int mCntFreeStorageFail;
    private Context mContext;
    private long mDataFree;
    private boolean mDataFullFlag = sLocalLOGV;
    private long mDataFullThreshold;
    private int mDataLevel = 0;
    /* access modifiers changed from: private */
    public boolean mDataLowFlag = sLocalLOGV;
    private long mDataLowThreshold;
    private boolean mDataRealFullFlag = sLocalLOGV;
    private boolean mDisableCleanFunc = sLocalLOGV;
    private boolean mFirstCheck = true;
    private Handler mHandler;
    private long mLastDataFree;
    private final Object mLock = new Object();
    /* access modifiers changed from: private */
    public boolean mNotificationDataShowed = sLocalLOGV;
    private boolean mOppoDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) sLocalLOGV);
    private long mTotalData;

    private void maybeFreeStorage() {
        if (this.mCntFreeStorageFail <= 3) {
            StorageManager storage = (StorageManager) getContext().getSystemService(StorageManager.class);
            for (VolumeInfo vol : storage.getWritablePrivateVolumes()) {
                File file = vol.getPath();
                long lowBytes = storage.getStorageLowBytes(file);
                if (file.getUsableSpace() < (NUMBER_THREE * lowBytes) / 2) {
                    try {
                        ServiceManager.getService(BrightnessConstants.AppSplineXml.TAG_PACKAGE).freeStorage(vol.getFsUuid(), 2 * lowBytes, 0);
                    } catch (IOException e) {
                        this.mCntFreeStorageFail++;
                    }
                }
            }
        }
    }

    private void oppoCheckData() {
        int dataLevel = this.mDataLevel;
        long dataFree = ColorStorageUtils.getDataFreeSpace();
        this.mDataFree = dataFree;
        OppoBaseDeviceStorageMonitorService oppoBaseDeviceStorageMonitorService = this.mBase;
        if (oppoBaseDeviceStorageMonitorService != null) {
            if (oppoBaseDeviceStorageMonitorService.mOppoForceLevle == 1) {
                dataFree = this.mDataLowThreshold - MB_BYTES;
                this.mDataLowFlag = sLocalLOGV;
                Slog.d(TAG, "oppoCheckData: mOppoForceLevle is low!!!");
            } else if (this.mBase.mOppoForceLevle == 2) {
                dataFree = 0;
                this.mDataFullFlag = sLocalLOGV;
                Slog.d(TAG, "oppoCheckData: mOppoForceLevle is full!!!");
            } else if (this.mBase.mOppoForceLevle == 0) {
                this.mDataLowFlag = true;
                this.mDataFullFlag = true;
                Slog.d(TAG, "oppoCheckData: mOppoForceLevle is normal!!!");
            }
        }
        int i = -1;
        if (dataFree > this.mDataFullThreshold && dataFree < this.mDataLowThreshold) {
            if (!this.mDataLowFlag) {
                StringBuilder sb = new StringBuilder();
                sb.append("data become low. freeStorage=");
                sb.append(this.mDataFree);
                sb.append(", forceLevle=");
                OppoBaseDeviceStorageMonitorService oppoBaseDeviceStorageMonitorService2 = this.mBase;
                sb.append(oppoBaseDeviceStorageMonitorService2 != null ? oppoBaseDeviceStorageMonitorService2.mOppoForceLevle : -1);
                Slog.d(TAG, sb.toString());
                oppoCancelNotification(23);
                oppoNotificationData();
                sendDataLowBroadcast();
                uploadDcsEvent(ID_DATA_LOW_ENTER, new HashMap<>());
                this.mDataLowFlag = true;
                this.mCntFreeStorageFail = 0;
            }
            dataLevel = 1;
        } else if (dataFree >= this.mDataLowThreshold + THRESHOLD_DELTA_DATA_LOW) {
            if (this.mDataFullFlag) {
                Slog.d(TAG, "data from full to not low.");
                this.mDataFullFlag = sLocalLOGV;
                uploadDcsEvent(ID_DATA_FULL_EXIT, new HashMap<>());
            }
            if (this.mDataLowFlag) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("data available. freeStorage=");
                sb2.append(this.mDataFree);
                sb2.append(", forceLevle=");
                OppoBaseDeviceStorageMonitorService oppoBaseDeviceStorageMonitorService3 = this.mBase;
                sb2.append(oppoBaseDeviceStorageMonitorService3 != null ? oppoBaseDeviceStorageMonitorService3.mOppoForceLevle : -1);
                Slog.d(TAG, sb2.toString());
                oppoCancelNotification(23);
                this.mNotificationDataShowed = sLocalLOGV;
                sendDataNotLowBroadcast();
                this.mDataLowFlag = sLocalLOGV;
                uploadDcsEvent(ID_DATA_LOW_EXIT, new HashMap<>());
            }
            dataLevel = 0;
        }
        long j = this.mDataFullThreshold;
        if (dataFree <= j) {
            if (!this.mDataLowFlag) {
                sendDataLowBroadcast();
                this.mDataLowFlag = true;
                uploadDcsEvent(ID_DATA_LOW_ENTER, new HashMap<>());
                this.mCntFreeStorageFail = 0;
                Slog.d(TAG, "data from not low to full.");
            }
            if (!this.mDataFullFlag) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("data become full, freeStorage=");
                sb3.append(this.mDataFree);
                sb3.append(", forceLevle=");
                OppoBaseDeviceStorageMonitorService oppoBaseDeviceStorageMonitorService4 = this.mBase;
                if (oppoBaseDeviceStorageMonitorService4 != null) {
                    i = oppoBaseDeviceStorageMonitorService4.mOppoForceLevle;
                }
                sb3.append(i);
                Slog.d(TAG, sb3.toString());
                this.mDataFullFlag = true;
                oppoCancelNotification(23);
                oppoNotificationData();
                uploadDcsEvent(ID_DATA_FULL_ENTER, new HashMap<>());
            }
            dataLevel = 2;
        } else if (dataFree >= j + THRESHOLD_DELTA_DATA_LOW && dataFree < this.mDataLowThreshold + THRESHOLD_DELTA_DATA_LOW) {
            if (this.mDataFullFlag) {
                StringBuilder sb4 = new StringBuilder();
                sb4.append("data become not full, freeStorage=");
                sb4.append(this.mDataFree);
                sb4.append(", forceLevle=");
                OppoBaseDeviceStorageMonitorService oppoBaseDeviceStorageMonitorService5 = this.mBase;
                if (oppoBaseDeviceStorageMonitorService5 != null) {
                    i = oppoBaseDeviceStorageMonitorService5.mOppoForceLevle;
                }
                sb4.append(i);
                Slog.d(TAG, sb4.toString());
                this.mDataFullFlag = sLocalLOGV;
                oppoCancelNotification(23);
                oppoNotificationData();
                uploadDcsEvent(ID_DATA_FULL_EXIT, new HashMap<>());
            }
            dataLevel = 1;
        }
        if (dataFree <= THRESHOLD_DATA_FULL) {
            if (!this.mDataRealFullFlag) {
                sendDataFullBroadcast();
                this.mDataRealFullFlag = true;
            }
        } else if (this.mDataRealFullFlag) {
            sendDataNotFullBroadcast();
            this.mDataRealFullFlag = sLocalLOGV;
        }
        if (Math.abs(this.mLastDataFree - this.mDataFree) > DEFAULT_LOG_DELTA_BYTES || this.mDataLevel != dataLevel) {
            EventLogTags.writeStorageState(StorageManager.UUID_PRIVATE_INTERNAL, this.mDataLevel, dataLevel, this.mDataFree, this.mTotalData);
            this.mLastDataFree = this.mDataFree;
        }
        if (dataLevel != this.mDataLevel && !this.mDisableCleanFunc) {
            if (this.mFirstCheck) {
                SystemProperties.set(PROP_DATA_LEVEL_CHANGE_BOOT, String.valueOf(true));
            }
            SystemProperties.set(PROP_DATA_LEVEL, String.valueOf(dataLevel));
            SystemProperties.set(PROP_DATA_BYTES, String.valueOf(dataFree));
            Intent intent = new Intent(OPPO_ACTION_DATA_LEVEL_CHANGE);
            intent.setPackage("com.coloros.oppoguardelf");
            intent.putExtra("level", dataLevel);
            intent.putExtra("dataFree", dataFree);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "oppo.permission.OPPO_COMPONENT_SAFE");
            Slog.d(TAG, "sendBroadcast OPPO_ACTION_DATA_LEVEL_CHANGE");
        }
        this.mDataLevel = dataLevel;
        this.mFirstCheck = sLocalLOGV;
    }

    private void sendDataLowBroadcast() {
        int seq = ((AtomicInteger) OppoMirrorDeviceStorageMonitorService.mSeq.get(this)).get();
        this.mContext.sendStickyBroadcastAsUser(new Intent("android.intent.action.DEVICE_STORAGE_LOW").addFlags(85983232).putExtra("seq", seq), UserHandle.ALL);
        Slog.d(TAG, "sendDataLowBroadcast. seq=" + seq);
    }

    private void sendDataNotLowBroadcast() {
        int seq = ((AtomicInteger) OppoMirrorDeviceStorageMonitorService.mSeq.get(this)).get();
        Intent lowIntent = new Intent("android.intent.action.DEVICE_STORAGE_LOW").addFlags(85983232).putExtra("seq", seq);
        Intent notLowIntent = new Intent("android.intent.action.DEVICE_STORAGE_OK").addFlags(85983232).putExtra("seq", seq);
        this.mContext.removeStickyBroadcastAsUser(lowIntent, UserHandle.ALL);
        this.mContext.sendBroadcastAsUser(notLowIntent, UserHandle.ALL);
        Slog.d(TAG, "sendDataNotLowBroadcast. seq=" + seq);
    }

    private void sendDataFullBroadcast() {
        this.mContext.sendStickyBroadcastAsUser(new Intent("android.intent.action.DEVICE_STORAGE_FULL").addFlags(ColorMultiAppManagerService.FLAG_MULTI_APP).putExtra("seq", ((AtomicInteger) OppoMirrorDeviceStorageMonitorService.mSeq.get(this)).get()), UserHandle.ALL);
        Slog.d(TAG, "sendDataFullBroadcast.");
    }

    private void sendDataNotFullBroadcast() {
        int seq = ((AtomicInteger) OppoMirrorDeviceStorageMonitorService.mSeq.get(this)).get();
        Intent fullIntent = new Intent("android.intent.action.DEVICE_STORAGE_FULL").addFlags(ColorMultiAppManagerService.FLAG_MULTI_APP).putExtra("seq", seq);
        Intent notFullIntent = new Intent("android.intent.action.DEVICE_STORAGE_NOT_FULL").addFlags(ColorMultiAppManagerService.FLAG_MULTI_APP).putExtra("seq", seq);
        this.mContext.removeStickyBroadcastAsUser(fullIntent, UserHandle.ALL);
        this.mContext.sendBroadcastAsUser(notFullIntent, UserHandle.ALL);
        Slog.d(TAG, "sendDataNotFullBroadcast.");
    }

    public void onBootPhase(int phase) {
        ColorDeviceStorageMonitorService.super.onBootPhase(phase);
        if (1000 == phase) {
            Slog.d(TAG, "onBootPhase: PHASE_BOOT_COMPLETED");
            OppoBaseDeviceStorageMonitorService base = typeCasting(this);
            if (base != null) {
                base.reScheduleCheck((long) DELAY_FIRST_CHECK);
            }
        }
    }

    public void onStart() {
        ColorDeviceStorageMonitorService.super.onStart();
        this.mContext = getContext();
        OppoBaseDeviceStorageMonitorService oppoBaseDeviceStorageMonitorService = this.mBase;
        if (oppoBaseDeviceStorageMonitorService != null) {
            this.mHandler = new Handler(oppoBaseDeviceStorageMonitorService.looperStorageMonitor);
        } else {
            this.mHandler = new Handler();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, "oppo.permission.OPPO_COMPONENT_SAFE", this.mHandler);
        ColorStorageUtils.getDataThreshold();
        this.mTotalData = ColorStorageUtils.getTotalData();
        this.mDataLowThreshold = ColorStorageUtils.getDataLowThreshold();
        this.mDataFullThreshold = ColorStorageUtils.getDataFullThreshold();
        this.mDisableCleanFunc = this.mContext.getPackageManager().hasSystemFeature("oppo.phonemanager.disable.clean");
        if (this.mOppoDebug) {
            Slog.d(TAG, "onStart: hasFeature oppo.phonemanager.disable.clean = " + this.mDisableCleanFunc);
        }
    }

    public ColorDeviceStorageMonitorService(Context context) {
        super(context);
    }

    private boolean isNeedDataSpaceMonitorCurve() {
        if (Settings.Global.getInt(this.mContext.getContentResolver(), DATA_SPACE_MONITOR_CURVE_SWITCH, 1) == 1) {
            return true;
        }
        return sLocalLOGV;
    }

    /* access modifiers changed from: private */
    public void oppoNotificationData() {
        CharSequence title;
        String source;
        if (!this.mNotificationDataShowed && !this.mDisableCleanFunc) {
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
            PendingIntent intent = PendingIntent.getActivityAsUser(this.mContext, 0, getFileCleanUpIntent(source), 0, null, UserHandle.CURRENT);
            Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
            bigTextStyle.setBigContentTitle(title);
            bigTextStyle.bigText(details);
            Notification notification = new Notification.Builder(this.mContext, SystemNotificationChannels.ALERTS).setContentTitle(title).setContentText(details).setSmallIcon(201852148).setContentIntent(intent).setVisibility(1).setCategory("sys").setShowWhen(true).setStyle(bigTextStyle).build();
            notification.flags |= 32;
            mNotificationMgr.notifyAsUser(null, 23, notification, UserHandle.ALL);
            this.mNotificationDataShowed = true;
            Slog.d(TAG, "oppoNotificationData: send notification.");
        }
    }

    /* access modifiers changed from: private */
    public void oppoCancelNotification(int id) {
        ((NotificationManager) this.mContext.getSystemService("notification")).cancelAsUser(null, id, UserHandle.ALL);
        if (id == 23) {
            this.mNotificationDataShowed = sLocalLOGV;
        }
        Slog.d(TAG, "oppoCancelNotification: id=" + id);
    }

    private void uploadDcsEvent(String eventId, HashMap<String, String> eventMap) {
        eventMap.put("showTotalSpace", String.valueOf(ColorStorageUtils.getActualShowTotalData()));
        eventMap.put("freeSpace", String.valueOf(this.mDataFree));
        OppoStatistics.onCommon(this.mContext, "20120", eventId, eventMap, (boolean) sLocalLOGV);
    }

    private boolean isNormalBoot() {
        String cryptState = SystemProperties.get("vold.decrypt", "trigger_restart_framework");
        if ("trigger_restart_framework".equals(cryptState)) {
            return true;
        }
        Slog.d(TAG, "cryptState = " + cryptState);
        return sLocalLOGV;
    }

    /* access modifiers changed from: package-private */
    public long getMemoryLowThresholdInternal() {
        return this.mDataLowThreshold;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002f, code lost:
        return;
     */
    public void oppoCheckStorage() {
        synchronized (this.mLock) {
            if (isNormalBoot()) {
                if (!isDeviceProvisioned()) {
                    Slog.d(TAG, "oppoCheckStorage: DEVICE_PROVISIONED is not set!!!!!!");
                } else if (!SystemProperties.getBoolean("persist.sys.devicestoragemonitor.disable", (boolean) sLocalLOGV)) {
                    maybeFreeStorage();
                    oppoCheckData();
                } else if (this.mOppoDebug) {
                    Slog.d(TAG, "oppoCheckStorage: device storage monitor disable.");
                }
            }
        }
    }

    private boolean isDeviceProvisioned() {
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return sLocalLOGV;
    }

    private Intent getFileCleanUpIntent(String source) {
        Intent i = new Intent(OPPO_ACTION_FILE_CLEANUP);
        i.putExtra("enter_from", source);
        i.putExtra(CLEAR_SCAN_MODE, 2);
        i.addFlags(603979776);
        return i;
    }

    /* access modifiers changed from: package-private */
    public boolean oppoSimulationTest(String[] args, PrintWriter pw) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        if (args.length < 1) {
            return sLocalLOGV;
        }
        String cmd = args[0];
        if ("testSimuDataLow".equals(cmd)) {
            ColorStorageUtils.setDataSimuMode(101);
            pw.print("Simulate to Data Low!");
        } else if ("testSimuDataFull".equals(cmd)) {
            ColorStorageUtils.setDataSimuMode(ColorStorageUtils.DATA_SIMU_MODE_FULL);
            pw.print("Simulate to Data Full!");
        } else if ("testSimuDataEnough".equals(cmd)) {
            ColorStorageUtils.setDataSimuMode(ColorStorageUtils.DATA_SIMU_MODE_ENOUGH);
            pw.print("Simulate to Data Not Low!");
        } else if ("testExit".equals(cmd)) {
            ColorStorageUtils.setDataSimuMode(0);
            pw.print("Simulate Exit!");
        } else if (!"testSimuDataRealFull".equals(cmd)) {
            return sLocalLOGV;
        } else {
            ColorStorageUtils.setDataSimuMode(ColorStorageUtils.DATA_SIMU_MODE_REAL_FULL);
            pw.print("Simulate to Data Real Full!");
        }
        OppoBaseDeviceStorageMonitorService oppoBaseDeviceStorageMonitorService = this.mBase;
        if (oppoBaseDeviceStorageMonitorService != null) {
            oppoBaseDeviceStorageMonitorService.reScheduleCheck(0);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void oppoDumpImpl(PrintWriter pw) {
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
    }

    private static OppoBaseDeviceStorageMonitorService typeCasting(DeviceStorageMonitorService dsms) {
        if (dsms != null) {
            return (OppoBaseDeviceStorageMonitorService) ColorTypeCastingHelper.typeCasting(OppoBaseDeviceStorageMonitorService.class, dsms);
        }
        return null;
    }
}
