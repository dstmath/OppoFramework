package com.android.server.storage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Environment;
import android.os.FileObserver;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.util.ArrayMap;
import android.util.DataUnit;
import android.util.Slog;
import android.util.StatsLog;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.EventLogTags;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.InstructionSets;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.Settings;
import com.android.server.utils.PriorityDump;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class DeviceStorageMonitorService extends OppoBaseDeviceStorageMonitorService {
    private static final long BOOT_IMAGE_STORAGE_REQUIREMENT = DataUnit.MEBIBYTES.toBytes(250);
    private static final long DEFAULT_CHECK_INTERVAL = 30000;
    private static final long DEFAULT_LOG_DELTA_BYTES = DataUnit.MEBIBYTES.toBytes(64);
    public static final String EXTRA_SEQUENCE = "seq";
    private static final int MSG_CHECK = 1;
    static final int OPTION_FORCE_UPDATE = 1;
    static final String SERVICE = "devicestoragemonitor";
    private static final String TAG = "DeviceStorageMonitorService";
    private static final String TV_NOTIFICATION_CHANNEL_ID = "devicestoragemonitor.tv";
    private CacheFileDeletedObserver mCacheFileDeletedObserver;
    private volatile int mForceLevel = -1;
    private final DeviceStorageMonitorInternal mLocalService = new DeviceStorageMonitorInternal() {
        /* class com.android.server.storage.DeviceStorageMonitorService.AnonymousClass1 */

        @Override // com.android.server.storage.DeviceStorageMonitorInternal
        public void checkMemory() {
            DeviceStorageMonitorService.this.mHandler.removeMessages(1);
            DeviceStorageMonitorService.this.mHandler.obtainMessage(1).sendToTarget();
        }

        @Override // com.android.server.storage.DeviceStorageMonitorInternal
        public boolean isMemoryLow() {
            return Environment.getDataDirectory().getUsableSpace() < getMemoryLowThreshold();
        }

        @Override // com.android.server.storage.DeviceStorageMonitorInternal
        public long getMemoryLowThreshold() {
            PackageManager packageManager = DeviceStorageMonitorService.this.getContext().getPackageManager();
            if (packageManager == null || !packageManager.isClosedSuperFirewall()) {
                return DeviceStorageMonitorService.this.getMemoryLowThresholdInternal();
            }
            return ((StorageManager) DeviceStorageMonitorService.this.getContext().getSystemService(StorageManager.class)).getStorageLowBytes(Environment.getDataDirectory());
        }
    };
    private NotificationManager mNotifManager;
    private final Binder mRemoteService = new Binder() {
        /* class com.android.server.storage.DeviceStorageMonitorService.AnonymousClass2 */

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(DeviceStorageMonitorService.this.getContext(), DeviceStorageMonitorService.TAG, pw)) {
                DeviceStorageMonitorService.this.dumpImpl(fd, pw, args);
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new Shell().exec(this, in, out, err, args, callback, resultReceiver);
        }
    };
    protected final AtomicInteger mSeq = new AtomicInteger(1);
    private final ArrayMap<UUID, State> mStates = new ArrayMap<>();

    private static class State {
        private static final int LEVEL_FULL = 2;
        private static final int LEVEL_LOW = 1;
        private static final int LEVEL_NORMAL = 0;
        private static final int LEVEL_UNKNOWN = -1;
        public long lastUsableBytes;
        public int level;

        private State() {
            this.level = 0;
            this.lastUsableBytes = JobStatus.NO_LATEST_RUNTIME;
        }

        /* access modifiers changed from: private */
        public static boolean isEntering(int level2, int oldLevel, int newLevel) {
            return newLevel >= level2 && (oldLevel < level2 || oldLevel == -1);
        }

        /* access modifiers changed from: private */
        public static boolean isLeaving(int level2, int oldLevel, int newLevel) {
            return newLevel < level2 && (oldLevel >= level2 || oldLevel == -1);
        }

        /* access modifiers changed from: private */
        public static String levelToString(int level2) {
            if (level2 == -1) {
                return "UNKNOWN";
            }
            if (level2 == 0) {
                return PriorityDump.PRIORITY_ARG_NORMAL;
            }
            if (level2 == 1) {
                return "LOW";
            }
            if (level2 != 2) {
                return Integer.toString(level2);
            }
            return "FULL";
        }
    }

    private State findOrCreateState(UUID uuid) {
        State state = this.mStates.get(uuid);
        if (state != null) {
            return state;
        }
        State state2 = new State();
        this.mStates.put(uuid, state2);
        return state2;
    }

    private void check() {
        int newLevel;
        int oldLevel;
        int newLevel2;
        StorageManager storage = (StorageManager) getContext().getSystemService(StorageManager.class);
        int seq = this.mSeq.get();
        for (VolumeInfo vol : storage.getWritablePrivateVolumes()) {
            File file = vol.getPath();
            long fullBytes = storage.getStorageFullBytes(file);
            long lowBytes = storage.getStorageLowBytes(file);
            if (file.getUsableSpace() < (3 * lowBytes) / 2) {
                try {
                    ((PackageManagerService) ServiceManager.getService(Settings.ATTR_PACKAGE)).freeStorage(vol.getFsUuid(), lowBytes * 2, 0);
                } catch (IOException e) {
                    Slog.w(TAG, e);
                }
            }
            UUID uuid = StorageManager.convert(vol.getFsUuid());
            State state = findOrCreateState(uuid);
            long totalBytes = file.getTotalSpace();
            long usableBytes = file.getUsableSpace();
            int oldLevel2 = state.level;
            if (this.mForceLevel != -1) {
                oldLevel = -1;
                newLevel = this.mForceLevel;
            } else if (usableBytes <= fullBytes) {
                newLevel = 2;
                oldLevel = oldLevel2;
            } else if (usableBytes <= lowBytes) {
                newLevel = 1;
                oldLevel = oldLevel2;
            } else if (!StorageManager.UUID_DEFAULT.equals(uuid) || isBootImageOnDisk() || usableBytes >= BOOT_IMAGE_STORAGE_REQUIREMENT) {
                newLevel = 0;
                oldLevel = oldLevel2;
            } else {
                newLevel = 1;
                oldLevel = oldLevel2;
            }
            if (Math.abs(state.lastUsableBytes - usableBytes) > DEFAULT_LOG_DELTA_BYTES || oldLevel != newLevel) {
                newLevel2 = newLevel;
                EventLogTags.writeStorageState(uuid.toString(), oldLevel, newLevel2, usableBytes, totalBytes);
                state.lastUsableBytes = usableBytes;
            } else {
                newLevel2 = newLevel;
            }
            updateNotifications(vol, oldLevel, newLevel2);
            updateBroadcasts(vol, oldLevel, newLevel2, seq);
            state.level = newLevel2;
            storage = storage;
        }
        if (!this.mHandler.hasMessages(1)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 30000);
        }
    }

    public DeviceStorageMonitorService(Context context) {
        super(context);
    }

    private static boolean isBootImageOnDisk() {
        for (String instructionSet : InstructionSets.getAllDexCodeInstructionSets()) {
            if (!VMRuntime.isBootClassPathOnDisk(instructionSet)) {
                return false;
            }
        }
        return true;
    }

    @Override // com.android.server.storage.OppoBaseDeviceStorageMonitorService, com.android.server.SystemService
    public void onStart() {
        Context context = getContext();
        this.mNotifManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        this.mCacheFileDeletedObserver = new CacheFileDeletedObserver();
        this.mCacheFileDeletedObserver.startWatching();
        if (context.getPackageManager().hasSystemFeature("android.software.leanback")) {
            this.mNotifManager.createNotificationChannel(new NotificationChannel(TV_NOTIFICATION_CHANNEL_ID, context.getString(17039860), 4));
        }
        publishBinderService(SERVICE, this.mRemoteService);
        publishLocalService(DeviceStorageMonitorInternal.class, this.mLocalService);
    }

    class Shell extends ShellCommand {
        Shell() {
        }

        public int onCommand(String cmd) {
            return DeviceStorageMonitorService.this.onShellCommand(this, cmd);
        }

        public void onHelp() {
            DeviceStorageMonitorService.dumpHelp(getOutPrintWriter());
        }
    }

    /* access modifiers changed from: package-private */
    public int parseOptions(Shell shell) {
        int opts = 0;
        while (true) {
            String opt = shell.getNextOption();
            if (opt == null) {
                return opts;
            }
            if ("-f".equals(opt)) {
                opts |= 1;
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* access modifiers changed from: package-private */
    public int onShellCommand(Shell shell, String cmd) {
        boolean z;
        if (cmd == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        switch (cmd.hashCode()) {
            case 88200241:
                if (cmd.equals("force-full")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 108404047:
                if (cmd.equals("reset")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 1526871410:
                if (cmd.equals("force-low")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            case 1692300408:
                if (cmd.equals("force-not-low")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            int opts = parseOptions(shell);
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            this.mForceLevel = 1;
            this.mOppoForceLevle = 1;
            int seq = this.mSeq.incrementAndGet();
            if ((opts & 1) != 0) {
                this.mHandler.removeMessages(1);
                this.mHandler.obtainMessage(1).sendToTarget();
                pw.println(seq);
            }
        } else if (z) {
            int opts2 = parseOptions(shell);
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            this.mForceLevel = 0;
            this.mOppoForceLevle = 0;
            int seq2 = this.mSeq.incrementAndGet();
            if ((opts2 & 1) != 0) {
                this.mHandler.removeMessages(1);
                this.mHandler.obtainMessage(1).sendToTarget();
                pw.println(seq2);
            }
        } else if (z) {
            int opts3 = parseOptions(shell);
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            this.mForceLevel = -1;
            this.mOppoForceLevle = -1;
            int seq3 = this.mSeq.incrementAndGet();
            if ((opts3 & 1) != 0) {
                this.mHandler.removeMessages(1);
                this.mHandler.obtainMessage(1).sendToTarget();
                pw.println(seq3);
            }
        } else if (!z) {
            return shell.handleDefaultCommands(cmd);
        } else {
            int opts4 = parseOptions(shell);
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            this.mOppoForceLevle = 2;
            if ((opts4 & 1) != 0) {
                this.mHandler.removeMessages(1);
                this.mHandler.obtainMessage(1).sendToTarget();
            }
        }
        return 0;
    }

    static void dumpHelp(PrintWriter pw) {
        pw.println("Device storage monitor service (devicestoragemonitor) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  force-low [-f]");
        pw.println("    Force storage to be low, freezing storage state.");
        pw.println("    -f: force a storage change broadcast be sent, prints new sequence.");
        pw.println("  force-not-low [-f]");
        pw.println("    Force storage to not be low, freezing storage state.");
        pw.println("    -f: force a storage change broadcast be sent, prints new sequence.");
        pw.println("  reset [-f]");
        pw.println("    Unfreeze storage state, returning to current real values.");
        pw.println("    -f: force a storage change broadcast be sent, prints new sequence.");
    }

    /* access modifiers changed from: package-private */
    public void dumpImpl(FileDescriptor fd, PrintWriter _pw, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(_pw, "  ");
        if (!oppoSimulationTest(args, pw)) {
            if (args == null || args.length == 0 || "-a".equals(args[0])) {
                pw.println("Known volumes:");
                pw.increaseIndent();
                for (int i = 0; i < this.mStates.size(); i++) {
                    UUID uuid = this.mStates.keyAt(i);
                    State state = this.mStates.valueAt(i);
                    if (StorageManager.UUID_DEFAULT.equals(uuid)) {
                        pw.println("Default:");
                    } else {
                        pw.println(uuid + ":");
                    }
                    pw.increaseIndent();
                    pw.printPair("level", State.levelToString(state.level));
                    pw.printPair("lastUsableBytes", Long.valueOf(state.lastUsableBytes));
                    pw.println();
                    pw.decreaseIndent();
                }
                pw.decreaseIndent();
                pw.println();
                pw.printPair("mSeq", Integer.valueOf(this.mSeq.get()));
                pw.printPair("mForceState", State.levelToString(this.mForceLevel));
                pw.println();
                pw.println();
                oppoDumpImpl(pw);
                return;
            }
            new Shell().exec(this.mRemoteService, null, fd, null, args, null, new ResultReceiver(null));
        }
    }

    private void updateNotifications(VolumeInfo vol, int oldLevel, int newLevel) {
        CharSequence details;
        Context context = getContext();
        UUID uuid = StorageManager.convert(vol.getFsUuid());
        if (State.isEntering(1, oldLevel, newLevel)) {
            Intent lowMemIntent = new Intent("android.os.storage.action.MANAGE_STORAGE");
            lowMemIntent.putExtra("android.os.storage.extra.UUID", uuid);
            lowMemIntent.addFlags(268435456);
            CharSequence title = context.getText(17040298);
            int i = 17040296;
            if (StorageManager.UUID_DEFAULT.equals(uuid)) {
                if (!isBootImageOnDisk()) {
                    i = 17040297;
                }
                details = context.getText(i);
            } else {
                details = context.getText(17040296);
            }
            Notification notification = new Notification.Builder(context, SystemNotificationChannels.ALERTS).setSmallIcon(17303511).setTicker(title).setColor(context.getColor(17170460)).setContentTitle(title).setContentText(details).setContentIntent(PendingIntent.getActivityAsUser(context, 0, lowMemIntent, 0, null, UserHandle.CURRENT)).setStyle(new Notification.BigTextStyle().bigText(details)).setVisibility(1).setCategory("sys").extend(new Notification.TvExtender().setChannelId(TV_NOTIFICATION_CHANNEL_ID)).build();
            notification.flags |= 32;
            this.mNotifManager.notifyAsUser(uuid.toString(), 23, notification, UserHandle.ALL);
            StatsLog.write(130, Objects.toString(vol.getDescription()), 2);
        } else if (State.isLeaving(1, oldLevel, newLevel)) {
            this.mNotifManager.cancelAsUser(uuid.toString(), 23, UserHandle.ALL);
            StatsLog.write(130, Objects.toString(vol.getDescription()), 1);
        }
    }

    private void updateBroadcasts(VolumeInfo vol, int oldLevel, int newLevel, int seq) {
        if (Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, vol.getFsUuid())) {
            Intent lowIntent = new Intent("android.intent.action.DEVICE_STORAGE_LOW").addFlags(85983232).putExtra(EXTRA_SEQUENCE, seq);
            Intent notLowIntent = new Intent("android.intent.action.DEVICE_STORAGE_OK").addFlags(85983232).putExtra(EXTRA_SEQUENCE, seq);
            if (State.isEntering(1, oldLevel, newLevel)) {
                getContext().sendStickyBroadcastAsUser(lowIntent, UserHandle.ALL);
            } else if (State.isLeaving(1, oldLevel, newLevel)) {
                getContext().removeStickyBroadcastAsUser(lowIntent, UserHandle.ALL);
                getContext().sendBroadcastAsUser(notLowIntent, UserHandle.ALL);
            }
            Intent fullIntent = new Intent("android.intent.action.DEVICE_STORAGE_FULL").addFlags(67108864).putExtra(EXTRA_SEQUENCE, seq);
            Intent notFullIntent = new Intent("android.intent.action.DEVICE_STORAGE_NOT_FULL").addFlags(67108864).putExtra(EXTRA_SEQUENCE, seq);
            if (State.isEntering(2, oldLevel, newLevel)) {
                getContext().sendStickyBroadcastAsUser(fullIntent, UserHandle.ALL);
            } else if (State.isLeaving(2, oldLevel, newLevel)) {
                getContext().removeStickyBroadcastAsUser(fullIntent, UserHandle.ALL);
                getContext().sendBroadcastAsUser(notFullIntent, UserHandle.ALL);
            }
        }
    }

    private static class CacheFileDeletedObserver extends FileObserver {
        public CacheFileDeletedObserver() {
            super(Environment.getDownloadCacheDirectory().getAbsolutePath(), 512);
        }

        public void onEvent(int event, String path) {
            EventLogTags.writeCacheFileDeleted(path);
        }
    }
}
