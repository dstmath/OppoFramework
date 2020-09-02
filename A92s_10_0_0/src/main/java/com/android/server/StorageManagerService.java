package com.android.server;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.admin.SecurityLog;
import android.app.usage.StorageStatsManager;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ProviderInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.ObbInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IStoraged;
import android.os.IVold;
import android.os.IVoldListener;
import android.os.IVoldTaskListener;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ParcelableException;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.os.storage.DiskInfo;
import android.os.storage.IObbActionListener;
import android.os.storage.IStorageEventListener;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageShutdownObserver;
import android.os.storage.StorageManager;
import android.os.storage.StorageManagerInternal;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.sysprop.VoldProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.DataUnit;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsService;
import com.android.internal.os.AppFuseMount;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.FuseUnavailableMountException;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.CollectionUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.HexDump;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.BatteryService;
import com.android.server.UiModeManagerService;
import com.android.server.Watchdog;
import com.android.server.backup.BackupPasswordManager;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.notification.ZenModeHelper;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.storage.AppFuseBridge;
import com.android.server.usage.UnixCalendar;
import com.android.server.wm.ActivityTaskManagerInternal;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import libcore.io.IoUtils;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class StorageManagerService extends IStorageManager.Stub implements Watchdog.Monitor, ActivityTaskManagerInternal.ScreenObserver {
    private static final String[] ALL_STORAGE_PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static final String ATTR_CREATED_MILLIS = "createdMillis";
    private static final String ATTR_FS_UUID = "fsUuid";
    private static final String ATTR_LAST_BENCH_MILLIS = "lastBenchMillis";
    private static final String ATTR_LAST_SEEN_MILLIS = "lastSeenMillis";
    private static final String ATTR_LAST_TRIM_MILLIS = "lastTrimMillis";
    private static final String ATTR_NICKNAME = "nickname";
    private static final String ATTR_PART_GUID = "partGuid";
    private static final String ATTR_PRIMARY_STORAGE_UUID = "primaryStorageUuid";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_USER_FLAGS = "userFlags";
    private static final String ATTR_VERSION = "version";
    private static final int CRYPTO_ALGORITHM_KEY_SIZE = 128;
    public static final String[] CRYPTO_TYPES = {"password", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR, "pattern", "pin"};
    private static final boolean DEBUG_EVENTS = false;
    private static final boolean DEBUG_OBB = false;
    private static final int DPOLICY_BALANCE = 4;
    private static final int DPOLICY_BG = 0;
    private static final int DPOLICY_PERFORMANCE = 5;
    private static final boolean EMULATE_FBE_SUPPORTED = true;
    /* access modifiers changed from: private */
    public static final boolean ENABLE_ISOLATED_STORAGE = StorageManager.hasIsolatedStorage();
    private static final int H_ABORT_IDLE_MAINT = 12;
    private static final int H_BOOT_COMPLETED = 13;
    private static final int H_COMPLETE_UNLOCK_USER = 14;
    private static final int H_DAEMON_CONNECTED = 2;
    protected static final int H_DPOLICY_EXPECT = 15;
    private static final int H_FSTRIM = 4;
    protected static final int H_FSYNC_PROTECT = 16;
    private static final int H_INTERNAL_BROADCAST = 7;
    private static final int H_PARTITION_FORGET = 9;
    private static final int H_RESET = 10;
    private static final int H_RUN_IDLE_MAINT = 11;
    private static final int H_SHUTDOWN = 3;
    private static final int H_SYSTEM_READY = 1;
    private static final int H_VOLUME_BROADCAST = 6;
    private static final int H_VOLUME_MOUNT = 5;
    private static final int H_VOLUME_UNMOUNT = 8;
    private static final long IDLE_THRESHOLD = 600000;
    private static final String ISOLATED_STORAGE_ENABLED = "isolated_storage_enabled";
    private static final String LAST_FSTRIM_FILE = "last-fstrim";
    private static final boolean LOCAL_LOGV = Log.isLoggable(TAG, 2);
    private static final int MOVE_STATUS_COPY_FINISHED = 82;
    private static final int OBB_FLUSH_MOUNT_STATE = 2;
    private static final int OBB_RUN_ACTION = 1;
    private static final int PBKDF2_HASH_ROUNDS = 1024;
    private static final String TAG = "StorageManagerService";
    private static final String TAG_STORAGE_BENCHMARK = "storage_benchmark";
    private static final String TAG_STORAGE_TRIM = "storage_trim";
    private static final String TAG_VOLUME = "volume";
    private static final String TAG_VOLUMES = "volumes";
    private static final int VERSION_ADD_PRIMARY = 2;
    private static final int VERSION_FIX_PRIMARY = 3;
    private static final int VERSION_INIT = 1;
    private static final boolean WATCHDOG_ENABLE = true;
    private static final String ZRAM_ENABLED_PROPERTY = "persist.sys.zram_enabled";
    static StorageManagerService sSelf = null;
    @GuardedBy({"mAppFuseLock"})
    private AppFuseBridge mAppFuseBridge = null;
    private final Object mAppFuseLock = new Object();
    private IAppOpsCallback.Stub mAppOpsCallback = new IAppOpsCallback.Stub() {
        /* class com.android.server.StorageManagerService.AnonymousClass13 */

        public void opChanged(int op, int uid, String packageName) throws RemoteException {
            if (StorageManagerService.ENABLE_ISOLATED_STORAGE) {
                StorageManagerService storageManagerService = StorageManagerService.this;
                storageManagerService.remountUidExternalStorage(uid, storageManagerService.getMountMode(uid, packageName));
            }
        }
    };
    protected boolean mBatteryFresh;
    private volatile boolean mBootCompleted = false;
    /* access modifiers changed from: private */
    public final Callbacks mCallbacks;
    protected boolean mChargeOn;
    protected final Context mContext;
    protected volatile int mCurrentUserId = 0;
    private volatile boolean mDaemonConnected = false;
    @GuardedBy({"mLock"})
    private ArrayMap<String, CountDownLatch> mDiskScanLatches = new ArrayMap<>();
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public ArrayMap<String, DiskInfo> mDisks = new ArrayMap<>();
    protected boolean mFsyncProtect;
    protected final Handler mHandler;
    private IAppOpsService mIAppOpsService;
    /* access modifiers changed from: private */
    public IPackageManager mIPackageManager;
    /* access modifiers changed from: private */
    public long mLastMaintenance;
    /* access modifiers changed from: private */
    public final File mLastMaintenanceFile;
    private final IVoldListener mListener = new IVoldListener.Stub() {
        /* class com.android.server.StorageManagerService.AnonymousClass5 */

        /* JADX WARNING: Removed duplicated region for block: B:16:0x0032  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x0038  */
        @Override // android.os.IVoldListener
        public void onDiskCreated(String diskId, int flags) {
            synchronized (StorageManagerService.this.mLock) {
                String value = SystemProperties.get("persist.sys.adoptable");
                char c = 65535;
                int hashCode = value.hashCode();
                if (hashCode != 464944051) {
                    if (hashCode == 1528363547 && value.equals("force_off")) {
                        c = 1;
                        if (c == 0) {
                            flags |= 1;
                        } else if (c == 1) {
                            flags &= -2;
                        }
                        StorageManagerService.this.mDisks.put(diskId, new DiskInfo(diskId, flags));
                    }
                } else if (value.equals("force_on")) {
                    c = 0;
                    if (c == 0) {
                    }
                    StorageManagerService.this.mDisks.put(diskId, new DiskInfo(diskId, flags));
                }
                if (c == 0) {
                }
                StorageManagerService.this.mDisks.put(diskId, new DiskInfo(diskId, flags));
            }
        }

        @Override // android.os.IVoldListener
        public void onDiskScanned(String diskId) {
            synchronized (StorageManagerService.this.mLock) {
                DiskInfo disk = (DiskInfo) StorageManagerService.this.mDisks.get(diskId);
                if (disk != null) {
                    StorageManagerService.this.onDiskScannedLocked(disk);
                }
            }
        }

        @Override // android.os.IVoldListener
        public void onDiskMetadataChanged(String diskId, long sizeBytes, String label, String sysPath) {
            synchronized (StorageManagerService.this.mLock) {
                DiskInfo disk = (DiskInfo) StorageManagerService.this.mDisks.get(diskId);
                if (disk != null) {
                    disk.size = sizeBytes;
                    disk.label = label;
                    disk.sysPath = sysPath;
                }
            }
        }

        @Override // android.os.IVoldListener
        public void onDiskDestroyed(String diskId) {
            synchronized (StorageManagerService.this.mLock) {
                DiskInfo disk = (DiskInfo) StorageManagerService.this.mDisks.remove(diskId);
                if (disk != null) {
                    StorageManagerService.this.mCallbacks.notifyDiskDestroyed(disk);
                }
            }
        }

        @Override // android.os.IVoldListener
        public void onVolumeCreated(String volId, int type, String diskId, String partGuid) {
            synchronized (StorageManagerService.this.mLock) {
                VolumeInfo vol = new VolumeInfo(volId, type, (DiskInfo) StorageManagerService.this.mDisks.get(diskId), partGuid);
                StorageManagerService.this.mVolumes.put(volId, vol);
                if (type == 0) {
                    StorageManagerService.this.mOppoStorageFeature.onVolumeCheckingLocked(vol, StorageManagerService.this.mCurrentUserId);
                } else {
                    StorageManagerService.this.onVolumeCreatedLocked(vol);
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0031, code lost:
            return;
         */
        @Override // android.os.IVoldListener
        public void onVolumeStateChanged(String volId, int state) {
            synchronized (StorageManagerService.this.mLock) {
                VolumeInfo vol = StorageManagerService.this.mVolumes.get(volId);
                if (vol != null) {
                    int oldState = vol.state;
                    if (!StorageManagerService.this.mOppoStorageFeature.changeVolumeReadOnlyStateLocked(vol, state, StorageManagerService.this.mSystemUnlockedUsers.length)) {
                        vol.state = state;
                        StorageManagerService.this.onVolumeStateChangedLocked(vol, oldState, state);
                    }
                }
            }
        }

        @Override // android.os.IVoldListener
        public void onVolumeMetadataChanged(String volId, String fsType, String fsUuid, String fsLabel) {
            synchronized (StorageManagerService.this.mLock) {
                VolumeInfo vol = StorageManagerService.this.mVolumes.get(volId);
                if (vol != null) {
                    vol.fsType = fsType;
                    vol.fsUuid = fsUuid;
                    vol.fsLabel = fsLabel;
                }
            }
        }

        @Override // android.os.IVoldListener
        public void onVolumePathChanged(String volId, String path) {
            synchronized (StorageManagerService.this.mLock) {
                VolumeInfo vol = StorageManagerService.this.mVolumes.get(volId);
                if (vol != null) {
                    vol.path = path;
                }
            }
        }

        @Override // android.os.IVoldListener
        public void onVolumeInternalPathChanged(String volId, String internalPath) {
            synchronized (StorageManagerService.this.mLock) {
                VolumeInfo vol = StorageManagerService.this.mVolumes.get(volId);
                if (vol != null) {
                    vol.internalPath = internalPath;
                }
            }
        }

        @Override // android.os.IVoldListener
        public void onVolumeDestroyed(String volId) {
            synchronized (StorageManagerService.this.mLock) {
                StorageManagerService.this.mVolumes.remove(volId);
            }
        }

        @Override // android.os.IVoldListener
        public void onDiskStateChanged(String diskId) {
            Slog.d(StorageManagerService.TAG, "onDiskStateChanged, diskId:" + diskId);
            synchronized (StorageManagerService.this.mLock) {
                StorageManagerService.this.mOppoStorageFeature.onDiskStateChangedLocked((DiskInfo) StorageManagerService.this.mDisks.get(diskId), StorageManagerService.this.mVolumes.size(), StorageManagerService.this.mSystemUnlockedUsers.length);
            }
        }

        @Override // android.os.IVoldListener
        public void onVolumeChecked(String volId, int type, String diskId, String partGuid) {
            synchronized (StorageManagerService.this.mLock) {
                Slog.i(StorageManagerService.TAG, "onVolumeChecked,volId=" + volId + ",type=" + type + ",VolumeInfo.TYPE_PUBLIC=" + 0 + ",diskId=" + diskId + ",partGuid=" + partGuid);
                StorageManagerService.this.onVolumeCreatedLocked(StorageManagerService.this.mVolumes.get(volId));
            }
        }
    };
    @GuardedBy({"mLock"})
    private int[] mLocalUnlockedUsers = EmptyArray.INT;
    protected final Object mLock = LockGuard.installNewLock(4);
    private final LockPatternUtils mLockPatternUtils;
    /* access modifiers changed from: private */
    public boolean mMaintAborted;
    /* access modifiers changed from: private */
    public boolean mMaintPrepared;
    @GuardedBy({"mLock"})
    private IPackageMoveObserver mMoveCallback;
    @GuardedBy({"mLock"})
    private String mMoveTargetUuid;
    @GuardedBy({"mAppFuseLock"})
    private int mNextAppFuseName = 0;
    /* access modifiers changed from: private */
    public final ObbActionHandler mObbActionHandler;
    /* access modifiers changed from: private */
    public final Map<IBinder, List<ObbState>> mObbMounts = new HashMap();
    /* access modifiers changed from: private */
    public final Map<String, ObbState> mObbPathToStateMap = new HashMap();
    /* access modifiers changed from: private */
    public IOppoStorageManagerFeature mOppoStorageFeature = null;
    private final Object mPackagesLock = new Object();
    private PackageManagerInternal mPmInternal;
    @GuardedBy({"mLock"})
    private String mPrimaryStorageUuid;
    @GuardedBy({"mLock"})
    private ArrayMap<String, VolumeRecord> mRecords = new ArrayMap<>();
    private final ContentResolver mResolver;
    /* access modifiers changed from: private */
    public long mScreenOffTime;
    protected boolean mScreenOn;
    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        /* class com.android.server.StorageManagerService.AnonymousClass2 */

        /* JADX WARNING: Removed duplicated region for block: B:23:0x0062 A[Catch:{ Exception -> 0x014f }] */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0064 A[Catch:{ Exception -> 0x014f }] */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x006f A[Catch:{ Exception -> 0x014f }] */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x0070 A[Catch:{ Exception -> 0x014f }] */
        /* JADX WARNING: Removed duplicated region for block: B:36:0x009c A[Catch:{ Exception -> 0x014f }] */
        /* JADX WARNING: Removed duplicated region for block: B:53:? A[RETURN, SYNTHETIC] */
        public void onReceive(Context context, Intent intent) {
            boolean mChargeOnCurr;
            boolean mBatteryFreshCurr;
            String action = intent.getAction();
            try {
                boolean mFsyncProtectCurr = true;
                if (!action.equals("android.intent.action.SCREEN_ON")) {
                    if (!action.equals("android.intent.action.DREAMING_STOPPED")) {
                        if (!action.equals("android.intent.action.SCREEN_OFF")) {
                            if (!action.equals("android.intent.action.DREAMING_STARTED")) {
                                if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                                    int status = intent.getIntExtra("status", -1);
                                    int level = intent.getIntExtra("level", -1);
                                    int scale = intent.getIntExtra("scale", -1);
                                    if (status != 2) {
                                        if (status != 5) {
                                            mChargeOnCurr = false;
                                            mBatteryFreshCurr = (((float) level) / ((float) scale)) * 100.0f <= 30.0f;
                                            if ((((float) level) / ((float) scale)) * 100.0f < 5.0f) {
                                                mFsyncProtectCurr = false;
                                            }
                                            if (!(StorageManagerService.this.mChargeOn == mChargeOnCurr && StorageManagerService.this.mBatteryFresh == mBatteryFreshCurr)) {
                                                StorageManagerService.this.mChargeOn = mChargeOnCurr;
                                                StorageManagerService.this.mBatteryFresh = mBatteryFreshCurr;
                                                StorageManagerService.this.mHandler.obtainMessage(15, StorageManagerService.this.getParamStrForOF2FS()).sendToTarget();
                                            }
                                            if (StorageManagerService.this.mFsyncProtect == mFsyncProtectCurr) {
                                                StorageManagerService.this.mFsyncProtect = mFsyncProtectCurr;
                                                StorageManagerService.this.mHandler.obtainMessage(16, StorageManagerService.this.mFsyncProtect ? "1" : "0").sendToTarget();
                                                return;
                                            }
                                            return;
                                        }
                                    }
                                    mChargeOnCurr = true;
                                    if ((((float) level) / ((float) scale)) * 100.0f <= 30.0f) {
                                    }
                                    if ((((float) level) / ((float) scale)) * 100.0f < 5.0f) {
                                    }
                                    StorageManagerService.this.mChargeOn = mChargeOnCurr;
                                    StorageManagerService.this.mBatteryFresh = mBatteryFreshCurr;
                                    StorageManagerService.this.mHandler.obtainMessage(15, StorageManagerService.this.getParamStrForOF2FS()).sendToTarget();
                                    if (StorageManagerService.this.mFsyncProtect == mFsyncProtectCurr) {
                                    }
                                }
                                return;
                            }
                        }
                        StorageManagerService.this.mScreenOn = false;
                        long unused = StorageManagerService.this.mScreenOffTime = System.currentTimeMillis();
                        Slog.w(StorageManagerService.TAG, "turn screenOff, maintPrepared:" + StorageManagerService.this.mMaintPrepared);
                        if (StorageManagerService.this.mMaintPrepared) {
                            MountServiceIdler.scheduleIdlePass(StorageManagerService.this.mContext, 600000);
                        }
                        StorageManagerService.this.mHandler.obtainMessage(15, StorageManagerService.this.getParamStrForOF2FS()).sendToTarget();
                        return;
                    }
                }
                StorageManagerService.this.mScreenOn = true;
                StorageManagerService.this.abortIdleMaint(null);
                Slog.w(StorageManagerService.TAG, "turn screenOn, maintPrepared:" + StorageManagerService.this.mMaintPrepared);
                if (StorageManagerService.this.mMaintPrepared) {
                    MountServiceIdler.cancleSchedule(StorageManagerService.this.mContext);
                }
                StorageManagerService.this.mHandler.obtainMessage(15, StorageManagerService.this.getParamStrForOF2FS()).sendToTarget();
            } catch (Exception e) {
                Slog.wtf(StorageManagerService.TAG, e);
            }
        }
    };
    private volatile boolean mSecureKeyguardShowing = true;
    private final AtomicFile mSettingsFile;
    private IOppoStorageManagerCallback mStorageCallback = new IOppoStorageManagerCallback() {
        /* class com.android.server.StorageManagerService.AnonymousClass14 */

        @Override // com.android.server.IOppoStorageManagerCallback
        public VolumeInfo getVolumeInfoByIndexLocked(int index) {
            if (index < 0 || index >= StorageManagerService.this.mVolumes.size()) {
                return null;
            }
            return StorageManagerService.this.mVolumes.valueAt(index);
        }

        @Override // com.android.server.IOppoStorageManagerCallback
        public int getSystemUnlockedUserIdByIndexLocked(int index) {
            if (index < 0 || index >= StorageManagerService.this.mSystemUnlockedUsers.length) {
                return -1;
            }
            return StorageManagerService.this.mSystemUnlockedUsers[index];
        }

        @Override // com.android.server.IOppoStorageManagerCallback
        public void onCheckBeforeMount(String volumeInfoId) {
            if (volumeInfoId != null && StorageManagerService.this.mVold != null) {
                try {
                    StorageManagerService.this.mVold.checkBeforeMount(volumeInfoId);
                } catch (Exception e) {
                    Slog.wtf(StorageManagerService.TAG, e);
                }
            }
        }
    };
    private final StorageManagerInternalImpl mStorageManagerInternal = new StorageManagerInternalImpl();
    /* access modifiers changed from: private */
    public volatile IStoraged mStoraged;
    private volatile boolean mSystemReady = false;
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public int[] mSystemUnlockedUsers = EmptyArray.INT;
    private BroadcastReceiver mUserReceiver = new BroadcastReceiver() {
        /* class com.android.server.StorageManagerService.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            Preconditions.checkArgument(userId >= 0);
            try {
                if ("android.intent.action.USER_ADDED".equals(action)) {
                    StorageManagerService.this.mVold.onUserAdded(userId, ((UserManager) StorageManagerService.this.mContext.getSystemService(UserManager.class)).getUserSerialNumber(userId));
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    synchronized (StorageManagerService.this.mVolumes) {
                        int size = StorageManagerService.this.mVolumes.size();
                        for (int i = 0; i < size; i++) {
                            VolumeInfo vol = StorageManagerService.this.mVolumes.valueAt(i);
                            if (vol.mountUserId == userId) {
                                vol.mountUserId = -10000;
                                StorageManagerService.this.mHandler.obtainMessage(8, vol).sendToTarget();
                            }
                        }
                    }
                    StorageManagerService.this.mVold.onUserRemoved(userId);
                }
            } catch (Exception e) {
                Slog.wtf(StorageManagerService.TAG, e);
            }
        }
    };
    private final CopyOnWriteArrayList<VolumeInfo> mVisibleVols = new CopyOnWriteArrayList<>();
    /* access modifiers changed from: private */
    public volatile IVold mVold;
    @GuardedBy({"mLock"})
    protected final ArrayMap<String, VolumeInfo> mVolumes = new ArrayMap<>();

    public static class Lifecycle extends SystemService {
        protected StorageManagerService mStorageManagerService;

        public Lifecycle(Context context) {
            super(context);
        }

        /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.StorageManagerService, android.os.IBinder] */
        @Override // com.android.server.SystemService
        public void onStart() {
            this.mStorageManagerService = new StorageManagerService(getContext());
            publishBinderService("mount", this.mStorageManagerService);
            this.mStorageManagerService.start();
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            if (phase == 500) {
                this.mStorageManagerService.servicesReady();
            } else if (phase == 550) {
                this.mStorageManagerService.systemReady();
            } else if (phase == 1000) {
                this.mStorageManagerService.bootCompleted();
            }
        }

        @Override // com.android.server.SystemService
        public void onSwitchUser(int userHandle) {
            this.mStorageManagerService.mCurrentUserId = userHandle;
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userHandle) {
            this.mStorageManagerService.onUnlockUser(userHandle);
        }

        @Override // com.android.server.SystemService
        public void onCleanupUser(int userHandle) {
            this.mStorageManagerService.onCleanupUser(userHandle);
        }
    }

    private VolumeInfo findVolumeByIdOrThrow(String id) {
        synchronized (this.mLock) {
            VolumeInfo vol = this.mVolumes.get(id);
            if (vol != null) {
                return vol;
            }
            throw new IllegalArgumentException("No volume found for ID " + id);
        }
    }

    private String findVolumeIdForPathOrThrow(String path) {
        synchronized (this.mLock) {
            int i = 0;
            while (i < this.mVolumes.size()) {
                VolumeInfo vol = this.mVolumes.valueAt(i);
                if (vol.path == null || !path.startsWith(vol.path)) {
                    i++;
                } else {
                    String str = vol.id;
                    return str;
                }
            }
            throw new IllegalArgumentException("No volume found for path " + path);
        }
    }

    /* access modifiers changed from: private */
    public VolumeRecord findRecordForPath(String path) {
        synchronized (this.mLock) {
            int i = 0;
            while (i < this.mVolumes.size()) {
                VolumeInfo vol = this.mVolumes.valueAt(i);
                if (vol.path == null || !path.startsWith(vol.path)) {
                    i++;
                } else {
                    VolumeRecord volumeRecord = this.mRecords.get(vol.fsUuid);
                    return volumeRecord;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public String scrubPath(String path) {
        if (path.startsWith(Environment.getDataDirectory().getAbsolutePath())) {
            return "internal";
        }
        VolumeRecord rec = findRecordForPath(path);
        if (rec == null || rec.createdMillis == 0) {
            return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
        }
        return "ext:" + ((int) ((System.currentTimeMillis() - rec.createdMillis) / UnixCalendar.WEEK_IN_MILLIS)) + "w";
    }

    private VolumeInfo findStorageForUuid(String volumeUuid) {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        if (Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, volumeUuid)) {
            return storage.findVolumeById("emulated");
        }
        if (Objects.equals("primary_physical", volumeUuid)) {
            return storage.getPrimaryPhysicalVolume();
        }
        return storage.findEmulatedForPrivate(storage.findVolumeByUuid(volumeUuid));
    }

    private boolean shouldBenchmark() {
        long benchInterval = Settings.Global.getLong(this.mContext.getContentResolver(), "storage_benchmark_interval", UnixCalendar.WEEK_IN_MILLIS);
        if (benchInterval == -1) {
            return false;
        }
        if (benchInterval == 0) {
            return true;
        }
        synchronized (this.mLock) {
            for (int i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = this.mVolumes.valueAt(i);
                VolumeRecord rec = this.mRecords.get(vol.fsUuid);
                if (vol.isMountedWritable() && rec != null && System.currentTimeMillis() - rec.lastBenchMillis >= benchInterval) {
                    return true;
                }
            }
            return false;
        }
    }

    private CountDownLatch findOrCreateDiskScanLatch(String diskId) {
        CountDownLatch latch;
        synchronized (this.mLock) {
            latch = this.mDiskScanLatches.get(diskId);
            if (latch == null) {
                latch = new CountDownLatch(1);
                this.mDiskScanLatches.put(diskId, latch);
            }
        }
        return latch;
    }

    /* access modifiers changed from: package-private */
    public class ObbState implements IBinder.DeathRecipient {
        final String canonicalPath;
        final int nonce;
        final int ownerGid;
        final String rawPath;
        final IObbActionListener token;
        String volId;

        public ObbState(String rawPath2, String canonicalPath2, int callingUid, IObbActionListener token2, int nonce2, String volId2) {
            this.rawPath = rawPath2;
            this.canonicalPath = canonicalPath2;
            this.ownerGid = UserHandle.getSharedAppGid(callingUid);
            this.token = token2;
            this.nonce = nonce2;
            this.volId = volId2;
        }

        public IBinder getBinder() {
            return this.token.asBinder();
        }

        public void binderDied() {
            StorageManagerService.this.mObbActionHandler.sendMessage(StorageManagerService.this.mObbActionHandler.obtainMessage(1, new UnmountObbAction(this, true)));
        }

        public void link() throws RemoteException {
            getBinder().linkToDeath(this, 0);
        }

        public void unlink() {
            getBinder().unlinkToDeath(this, 0);
        }

        public String toString() {
            return "ObbState{" + "rawPath=" + this.rawPath + ",canonicalPath=" + this.canonicalPath + ",ownerGid=" + this.ownerGid + ",token=" + this.token + ",binder=" + getBinder() + ",volId=" + this.volId + '}';
        }
    }

    class StorageManagerServiceHandler extends Handler {
        public StorageManagerServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (!StorageManagerService.this.mOppoStorageFeature.onStorageManagerMessageHandle(msg)) {
                Slog.d(StorageManagerService.TAG, "handleMessage:" + msg.what + ", handled and finished by oppo storage feature.");
                return;
            }
            int i = 0;
            switch (msg.what) {
                case 1:
                    StorageManagerService.this.handleSystemReady();
                    return;
                case 2:
                    StorageManagerService.this.handleDaemonConnected();
                    return;
                case 3:
                    IStorageShutdownObserver obs = (IStorageShutdownObserver) msg.obj;
                    boolean success = false;
                    try {
                        StorageManagerService.this.mVold.shutdown();
                        success = true;
                    } catch (Exception e) {
                        Slog.wtf(StorageManagerService.TAG, e);
                    }
                    if (obs != null) {
                        if (!success) {
                            i = -1;
                        }
                        try {
                            obs.onShutDownComplete(i);
                            return;
                        } catch (Exception e2) {
                            return;
                        }
                    } else {
                        return;
                    }
                case 4:
                    Slog.i(StorageManagerService.TAG, "Running fstrim idle maintenance");
                    try {
                        long unused = StorageManagerService.this.mLastMaintenance = System.currentTimeMillis();
                        StorageManagerService.this.mLastMaintenanceFile.setLastModified(StorageManagerService.this.mLastMaintenance);
                    } catch (Exception e3) {
                        Slog.e(StorageManagerService.TAG, "Unable to record last fstrim!");
                    }
                    StorageManagerService.this.fstrim(0, null);
                    Runnable callback = (Runnable) msg.obj;
                    if (callback != null) {
                        callback.run();
                        return;
                    }
                    return;
                case 5:
                    VolumeInfo vol = (VolumeInfo) msg.obj;
                    if (StorageManagerService.this.isMountDisallowed(vol)) {
                        Slog.i(StorageManagerService.TAG, "Ignoring mount " + vol.getId() + " due to policy");
                        return;
                    }
                    StorageManagerService.this.mount(vol);
                    return;
                case 6:
                    StorageVolume userVol = (StorageVolume) msg.obj;
                    String envState = userVol.getState();
                    Slog.d(StorageManagerService.TAG, "Volume " + userVol.getId() + " broadcasting " + envState + " to " + userVol.getOwner());
                    String action = VolumeInfo.getBroadcastForEnvironment(envState);
                    if (action != null) {
                        Intent intent = new Intent(action, Uri.fromFile(userVol.getPathFile()));
                        intent.putExtra("android.os.storage.extra.STORAGE_VOLUME", userVol);
                        intent.addFlags(83886080);
                        StorageManagerService.this.mContext.sendBroadcastAsUser(intent, userVol.getOwner());
                        return;
                    }
                    return;
                case 7:
                    StorageManagerService.this.mContext.sendBroadcastAsUser((Intent) msg.obj, UserHandle.ALL, "android.permission.WRITE_MEDIA_STORAGE");
                    return;
                case 8:
                    StorageManagerService.this.unmount((VolumeInfo) msg.obj);
                    return;
                case 9:
                    VolumeRecord rec = (VolumeRecord) msg.obj;
                    StorageManagerService.this.forgetPartition(rec.partGuid, rec.fsUuid);
                    return;
                case 10:
                    StorageManagerService.this.resetIfBootedAndConnected();
                    return;
                case 11:
                    Slog.i(StorageManagerService.TAG, "Running idle maintenance");
                    StorageManagerService.this.runIdleMaint((Runnable) msg.obj);
                    return;
                case 12:
                    Slog.i(StorageManagerService.TAG, "Aborting idle maintenance");
                    StorageManagerService.this.abortIdleMaint((Runnable) msg.obj);
                    return;
                case 13:
                    StorageManagerService.this.handleBootCompleted();
                    return;
                case 14:
                    StorageManagerService.this.completeUnlockUser(((Integer) msg.obj).intValue());
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public String getParamStrForOF2FS() {
        if (this.mScreenOn) {
            return "0";
        }
        if (this.mChargeOn) {
            return "5";
        }
        if (this.mBatteryFresh) {
            return "4";
        }
        return "0";
    }

    private void waitForLatch(CountDownLatch latch, String condition, long timeoutMillis) throws TimeoutException {
        long startMillis = SystemClock.elapsedRealtime();
        while (!latch.await(5000, TimeUnit.MILLISECONDS)) {
            try {
                Slog.w(TAG, "Thread " + Thread.currentThread().getName() + " still waiting for " + condition + "...");
            } catch (InterruptedException e) {
                Slog.w(TAG, "Interrupt while waiting for " + condition);
            }
            if (timeoutMillis > 0 && SystemClock.elapsedRealtime() > startMillis + timeoutMillis) {
                throw new TimeoutException("Thread " + Thread.currentThread().getName() + " gave up waiting for " + condition + " after " + timeoutMillis + "ms");
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSystemReady() {
        MountServiceIdler.scheduleIdlePass(this.mContext);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("zram_enabled"), false, new ContentObserver(null) {
            /* class com.android.server.StorageManagerService.AnonymousClass3 */

            public void onChange(boolean selfChange) {
                StorageManagerService.this.refreshZramSettings();
            }
        });
        refreshZramSettings();
        if (!SystemProperties.get(ZRAM_ENABLED_PROPERTY).equals("0") && this.mContext.getResources().getBoolean(17891604)) {
            ZramWriteback.scheduleZramWriteback(this.mContext);
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("isolated_storage_remote"), false, new ContentObserver(null) {
            /* class com.android.server.StorageManagerService.AnonymousClass4 */

            public void onChange(boolean selfChange) {
                StorageManagerService.this.refreshIsolatedStorageSettings();
            }
        });
        DeviceConfig.addOnPropertiesChangedListener("storage", this.mContext.getMainExecutor(), new DeviceConfig.OnPropertiesChangedListener() {
            /* class com.android.server.$$Lambda$StorageManagerService$js3bHvdd2Mf8gztNxvL27JoT034 */

            public final void onPropertiesChanged(DeviceConfig.Properties properties) {
                StorageManagerService.this.lambda$handleSystemReady$0$StorageManagerService(properties);
            }
        });
        refreshIsolatedStorageSettings();
    }

    public /* synthetic */ void lambda$handleSystemReady$0$StorageManagerService(DeviceConfig.Properties properties) {
        refreshIsolatedStorageSettings();
    }

    /* access modifiers changed from: private */
    public void refreshZramSettings() {
        String desiredPropertyValue;
        String propertyValue = SystemProperties.get(ZRAM_ENABLED_PROPERTY);
        if (!"".equals(propertyValue)) {
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "zram_enabled", 1) != 0) {
                desiredPropertyValue = "1";
            } else {
                desiredPropertyValue = "0";
            }
            if (!desiredPropertyValue.equals(propertyValue)) {
                SystemProperties.set(ZRAM_ENABLED_PROPERTY, desiredPropertyValue);
                if (desiredPropertyValue.equals("1") && this.mContext.getResources().getBoolean(17891604)) {
                    ZramWriteback.scheduleZramWriteback(this.mContext);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void refreshIsolatedStorageSettings() {
        boolean res;
        Settings.Global.putString(this.mResolver, "isolated_storage_remote", DeviceConfig.getProperty("storage", ISOLATED_STORAGE_ENABLED));
        int local = Settings.Global.getInt(this.mContext.getContentResolver(), "isolated_storage_local", 0);
        int remote = Settings.Global.getInt(this.mContext.getContentResolver(), "isolated_storage_remote", 0);
        if (local == -1) {
            res = false;
        } else if (local == 1) {
            res = true;
        } else if (remote == -1) {
            res = false;
        } else if (remote == 1) {
            res = true;
        } else {
            res = true;
        }
        Slog.d(TAG, "Isolated storage local flag " + local + " and remote flag " + remote + " resolved to " + res);
        SystemProperties.set("persist.sys.isolated_storage", Boolean.toString(res));
    }

    @Deprecated
    private void killMediaProvider(List<UserInfo> users) {
        if (users != null) {
            long token = Binder.clearCallingIdentity();
            try {
                for (UserInfo user : users) {
                    if (!user.isSystemOnly()) {
                        ProviderInfo provider = this.mPmInternal.resolveContentProvider("media", 786432, user.id);
                        if (provider != null) {
                            try {
                                ActivityManager.getService().killApplication(provider.applicationInfo.packageName, UserHandle.getAppId(provider.applicationInfo.uid), -1, "vold reset");
                                break;
                            } catch (RemoteException e) {
                            }
                        } else {
                            continue;
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    @GuardedBy({"mLock"})
    private void addInternalVolumeLocked() {
        VolumeInfo internal = new VolumeInfo("private", 1, (DiskInfo) null, (String) null);
        internal.state = 2;
        internal.path = Environment.getDataDirectory().getAbsolutePath();
        this.mVolumes.put(internal.id, internal);
    }

    private void initIfBootedAndConnected() {
        Slog.d(TAG, "Thinking about init, mBootCompleted=" + this.mBootCompleted + ", mDaemonConnected=" + this.mDaemonConnected);
        if (this.mBootCompleted && this.mDaemonConnected && !StorageManager.isFileEncryptedNativeOnly()) {
            boolean initLocked = StorageManager.isFileEncryptedEmulatedOnly();
            Slog.d(TAG, "Setting up emulation state, initlocked=" + initLocked);
            for (UserInfo user : ((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers()) {
                if (initLocked) {
                    try {
                        this.mVold.lockUserKey(user.id);
                    } catch (Exception e) {
                        Slog.wtf(TAG, e);
                    }
                } else {
                    this.mVold.unlockUserKey(user.id, user.serialNumber, encodeBytes(null), encodeBytes(null));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void resetIfBootedAndConnected() {
        int[] systemUnlockedUsers;
        Slog.d(TAG, "Thinking about reset, mBootCompleted=" + this.mBootCompleted + ", mDaemonConnected=" + this.mDaemonConnected);
        if (this.mBootCompleted && this.mDaemonConnected) {
            List<UserInfo> users = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers();
            killMediaProvider(users);
            synchronized (this.mLock) {
                systemUnlockedUsers = this.mSystemUnlockedUsers;
                this.mDisks.clear();
                this.mVolumes.clear();
                addInternalVolumeLocked();
            }
            try {
                this.mVold.reset();
                for (UserInfo user : users) {
                    this.mVold.onUserAdded(user.id, user.serialNumber);
                }
                for (int userId : systemUnlockedUsers) {
                    this.mVold.onUserStarted(userId);
                    this.mStoraged.onUserStarted(userId);
                }
                if (this.mOppoStorageFeature.shouldHandleKeyguardStateChange(this.mSecureKeyguardShowing)) {
                    this.mVold.onSecureKeyguardStateChanged(this.mSecureKeyguardShowing);
                }
                this.mStorageManagerInternal.onReset(this.mVold);
            } catch (Exception e) {
                Slog.wtf(TAG, e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUnlockUser(int userId) {
        Slog.d(TAG, "onUnlockUser " + userId);
        this.mOppoStorageFeature.onUnlockUser(userId);
        try {
            this.mVold.onUserStarted(userId);
            this.mStoraged.onUserStarted(userId);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
        this.mHandler.obtainMessage(14, Integer.valueOf(userId)).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void completeUnlockUser(int userId) {
        if (userId == 0) {
            this.mPmInternal.migrateLegacyObbData();
        }
        synchronized (this.mLock) {
            for (int i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = this.mVolumes.valueAt(i);
                if (vol.isVisibleForRead(userId) && vol.isMountedReadable()) {
                    StorageVolume userVol = vol.buildStorageVolume(this.mContext, userId, false);
                    this.mHandler.obtainMessage(6, userVol).sendToTarget();
                    String envState = VolumeInfo.getEnvironmentForState(vol.getState());
                    this.mCallbacks.notifyStorageStateChanged(userVol.getPath(), envState, envState);
                }
            }
            this.mSystemUnlockedUsers = ArrayUtils.appendInt(this.mSystemUnlockedUsers, userId);
        }
    }

    /* access modifiers changed from: private */
    public void onCleanupUser(int userId) {
        Slog.d(TAG, "onCleanupUser " + userId);
        try {
            this.mVold.onUserStopped(userId);
            this.mStoraged.onUserStopped(userId);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
        synchronized (this.mLock) {
            this.mSystemUnlockedUsers = ArrayUtils.removeInt(this.mSystemUnlockedUsers, userId);
        }
    }

    private boolean supportsBlockCheckpoint() throws RemoteException {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        return this.mVold.supportsBlockCheckpoint();
    }

    @Override // com.android.server.wm.ActivityTaskManagerInternal.ScreenObserver
    public void onAwakeStateChanged(boolean isAwake) {
    }

    @Override // com.android.server.wm.ActivityTaskManagerInternal.ScreenObserver
    public void onKeyguardStateChanged(boolean isShowing) {
        this.mSecureKeyguardShowing = isShowing && ((KeyguardManager) this.mContext.getSystemService(KeyguardManager.class)).isDeviceSecure();
        if (this.mOppoStorageFeature.shouldHandleKeyguardStateChange(this.mSecureKeyguardShowing)) {
            try {
                this.mVold.onSecureKeyguardStateChanged(this.mSecureKeyguardShowing);
            } catch (Exception e) {
                Slog.wtf(TAG, e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void runIdleMaintenance(Runnable callback) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(4, callback));
    }

    public void runMaintenance() {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        runIdleMaintenance(null);
    }

    public long lastMaintenance() {
        return this.mLastMaintenance;
    }

    public void onDaemonConnected() {
        this.mDaemonConnected = true;
        this.mHandler.obtainMessage(2).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void handleDaemonConnected() {
        initIfBootedAndConnected();
        resetIfBootedAndConnected();
        if ("".equals(VoldProperties.encrypt_progress().orElse(""))) {
            copyLocaleFromMountService();
        }
    }

    private void copyLocaleFromMountService() {
        try {
            String systemLocale = getField("SystemLocale");
            if (!TextUtils.isEmpty(systemLocale)) {
                Slog.d(TAG, "Got locale " + systemLocale + " from mount service");
                Locale locale = Locale.forLanguageTag(systemLocale);
                Configuration config = new Configuration();
                config.setLocale(locale);
                try {
                    ActivityManager.getService().updatePersistentConfiguration(config);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error setting system locale from mount service", e);
                }
                Slog.d(TAG, "Setting system properties to " + systemLocale + " from mount service");
                SystemProperties.set("persist.sys.locale", locale.toLanguageTag());
            }
        } catch (RemoteException e2) {
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void onDiskScannedLocked(DiskInfo disk) {
        int volumeCount = 0;
        for (int i = 0; i < this.mVolumes.size(); i++) {
            if (Objects.equals(disk.id, this.mVolumes.valueAt(i).getDiskId())) {
                volumeCount++;
            }
        }
        Intent intent = new Intent("android.os.storage.action.DISK_SCANNED");
        intent.addFlags(83886080);
        intent.putExtra("android.os.storage.extra.DISK_ID", disk.id);
        intent.putExtra("android.os.storage.extra.VOLUME_COUNT", volumeCount);
        this.mHandler.obtainMessage(7, intent).sendToTarget();
        CountDownLatch latch = this.mDiskScanLatches.remove(disk.id);
        if (latch != null) {
            latch.countDown();
        }
        disk.volumeCount = volumeCount;
        this.mCallbacks.notifyDiskScanned(disk, volumeCount);
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void onVolumeCreatedLocked(VolumeInfo vol) {
        if (this.mPmInternal.isOnlyCoreApps()) {
            Slog.d(TAG, "System booted in core-only mode; ignoring volume " + vol.getId());
        } else if (vol.type == 2) {
            VolumeInfo privateVol = ((StorageManager) this.mContext.getSystemService(StorageManager.class)).findPrivateForEmulated(vol);
            if (Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, this.mPrimaryStorageUuid) && "private".equals(privateVol.id)) {
                Slog.v(TAG, "Found primary storage at " + vol);
                vol.mountFlags = vol.mountFlags | 1;
                vol.mountFlags = vol.mountFlags | 2;
                this.mHandler.obtainMessage(5, vol).sendToTarget();
            } else if (Objects.equals(privateVol.fsUuid, this.mPrimaryStorageUuid)) {
                Slog.v(TAG, "Found primary storage at " + vol);
                vol.mountFlags = vol.mountFlags | 1;
                vol.mountFlags = vol.mountFlags | 2;
                this.mHandler.obtainMessage(5, vol).sendToTarget();
            }
        } else if (vol.type == 0) {
            if (Objects.equals("primary_physical", this.mPrimaryStorageUuid) && vol.disk.isDefaultPrimary()) {
                Slog.v(TAG, "Found primary storage at " + vol);
                vol.mountFlags = vol.mountFlags | 1;
                vol.mountFlags = vol.mountFlags | 2;
            }
            if (vol.disk.isAdoptable()) {
                vol.mountFlags |= 2;
            }
            vol.mountFlags |= 2;
            vol.mountUserId = this.mCurrentUserId;
            this.mHandler.obtainMessage(5, vol).sendToTarget();
        } else if (vol.type == 1) {
            this.mHandler.obtainMessage(5, vol).sendToTarget();
        } else if (vol.type == 5) {
            vol.mountUserId = this.mCurrentUserId;
            this.mHandler.obtainMessage(5, vol).sendToTarget();
        } else {
            Slog.d(TAG, "Skipping automatic mounting of " + vol);
        }
    }

    private boolean isBroadcastWorthy(VolumeInfo vol) {
        int type = vol.getType();
        if (type != 0 && type != 1 && type != 2 && type != 5) {
            return false;
        }
        int state = vol.getState();
        return state == 0 || state == 8 || state == 2 || state == 3 || state == 5 || state == 6;
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void onVolumeStateChangedLocked(VolumeInfo vol, int oldState, int newState) {
        if (!TextUtils.isEmpty(vol.fsUuid)) {
            VolumeRecord rec = this.mRecords.get(vol.fsUuid);
            if (rec == null) {
                rec = new VolumeRecord(vol.type, vol.fsUuid);
                rec.partGuid = vol.partGuid;
                rec.createdMillis = System.currentTimeMillis();
                if (vol.type == 1) {
                    rec.nickname = vol.disk.getDescription();
                }
                this.mRecords.put(rec.fsUuid, rec);
            } else if (TextUtils.isEmpty(rec.partGuid)) {
                rec.partGuid = vol.partGuid;
            }
            rec.lastSeenMillis = System.currentTimeMillis();
            writeSettingsLocked();
        }
        this.mCallbacks.notifyVolumeStateChanged(vol, oldState, newState);
        if (this.mBootCompleted && isBroadcastWorthy(vol)) {
            Intent intent = new Intent("android.os.storage.action.VOLUME_STATE_CHANGED");
            intent.putExtra("android.os.storage.extra.VOLUME_ID", vol.id);
            intent.putExtra("android.os.storage.extra.VOLUME_STATE", newState);
            intent.putExtra("android.os.storage.extra.FS_UUID", vol.fsUuid);
            intent.addFlags(83886080);
            this.mHandler.obtainMessage(7, intent).sendToTarget();
        }
        String oldStateEnv = VolumeInfo.getEnvironmentForState(oldState);
        String newStateEnv = VolumeInfo.getEnvironmentForState(newState);
        if (!Objects.equals(oldStateEnv, newStateEnv)) {
            int[] iArr = this.mSystemUnlockedUsers;
            for (int userId : iArr) {
                if (vol.isVisibleForRead(userId) && this.mOppoStorageFeature.shouldNotifyVolumeStateChanged(newStateEnv, userId, vol)) {
                    StorageVolume userVol = vol.buildStorageVolume(this.mContext, userId, false);
                    this.mHandler.obtainMessage(6, userVol).sendToTarget();
                    this.mCallbacks.notifyStorageStateChanged(userVol.getPath(), oldStateEnv, newStateEnv);
                }
            }
        }
        if ((vol.type == 0 || vol.type == 5) && vol.state == 5) {
            ObbActionHandler obbActionHandler = this.mObbActionHandler;
            obbActionHandler.sendMessage(obbActionHandler.obtainMessage(2, vol.path));
        }
        maybeLogMediaMount(vol, newState);
    }

    private void maybeLogMediaMount(VolumeInfo vol, int newState) {
        DiskInfo disk;
        if (SecurityLog.isLoggingEnabled() && (disk = vol.getDisk()) != null && (disk.flags & 12) != 0) {
            String label = disk.label != null ? disk.label.trim() : "";
            if (newState == 2 || newState == 3) {
                SecurityLog.writeEvent(210013, new Object[]{vol.path, label});
            } else if (newState == 0 || newState == 8) {
                SecurityLog.writeEvent(210014, new Object[]{vol.path, label});
            }
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void onMoveStatusLocked(int status) {
        IPackageMoveObserver iPackageMoveObserver = this.mMoveCallback;
        if (iPackageMoveObserver == null) {
            Slog.w(TAG, "Odd, status but no move requested");
            return;
        }
        try {
            iPackageMoveObserver.onStatusChanged(-1, status, -1);
        } catch (RemoteException e) {
        }
        if (status == 82) {
            Slog.d(TAG, "Move to " + this.mMoveTargetUuid + " copy phase finshed; persisting");
            this.mPrimaryStorageUuid = this.mMoveTargetUuid;
            writeSettingsLocked();
        }
        if (PackageManager.isMoveStatusFinished(status)) {
            Slog.d(TAG, "Move to " + this.mMoveTargetUuid + " finished with status " + status);
            this.mMoveCallback = null;
            this.mMoveTargetUuid = null;
        }
    }

    private void enforcePermission(String perm) {
        this.mContext.enforceCallingOrSelfPermission(perm, perm);
    }

    /* access modifiers changed from: private */
    public boolean isMountDisallowed(VolumeInfo vol) {
        UserManager userManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        boolean isUsbRestricted = false;
        if (vol.disk != null && vol.disk.isUsb()) {
            isUsbRestricted = userManager.hasUserRestriction("no_usb_file_transfer", Binder.getCallingUserHandle());
        }
        boolean isTypeRestricted = false;
        if (vol.type == 0 || vol.type == 1 || vol.type == 5) {
            isTypeRestricted = userManager.hasUserRestriction("no_physical_media", Binder.getCallingUserHandle());
        }
        if (isUsbRestricted || isTypeRestricted) {
            return true;
        }
        return false;
    }

    private void enforceAdminUser() {
        UserManager um = (UserManager) this.mContext.getSystemService("user");
        int callingUserId = UserHandle.getCallingUserId();
        long token = Binder.clearCallingIdentity();
        try {
            if (!um.getUserInfo(callingUserId).isAdmin()) {
                throw new SecurityException("Only admin users can adopt sd cards");
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public StorageManagerService(Context context) {
        sSelf = this;
        SystemProperties.set("sys.isolated_storage_snapshot", Boolean.toString(SystemProperties.getBoolean("persist.sys.isolated_storage", true)));
        this.mContext = context;
        this.mResolver = this.mContext.getContentResolver();
        this.mCallbacks = new Callbacks(FgThread.get().getLooper());
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        HandlerThread hthread = new HandlerThread(TAG);
        hthread.start();
        this.mHandler = new StorageManagerServiceHandler(hthread.getLooper());
        this.mObbActionHandler = new ObbActionHandler(IoThread.get().getLooper());
        this.mLastMaintenanceFile = new File(new File(Environment.getDataDirectory(), "system"), LAST_FSTRIM_FILE);
        if (!this.mLastMaintenanceFile.exists()) {
            try {
                new FileOutputStream(this.mLastMaintenanceFile).close();
            } catch (IOException e) {
                Slog.e(TAG, "Unable to create fstrim record " + this.mLastMaintenanceFile.getPath());
            }
        } else {
            this.mLastMaintenance = this.mLastMaintenanceFile.lastModified();
        }
        this.mScreenOn = true;
        this.mMaintPrepared = false;
        this.mMaintAborted = false;
        this.mChargeOn = false;
        this.mBatteryFresh = false;
        this.mFsyncProtect = false;
        this.mSettingsFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), "storage.xml"), "storage-settings");
        synchronized (this.mLock) {
            readSettingsLocked();
        }
        LocalServices.addService(StorageManagerInternal.class, this.mStorageManagerInternal);
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_ADDED");
        userFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(this.mUserReceiver, userFilter, null, this.mHandler);
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction("android.intent.action.SCREEN_ON");
        screenFilter.addAction("android.intent.action.SCREEN_OFF");
        screenFilter.addAction("android.intent.action.DREAMING_STARTED");
        screenFilter.addAction("android.intent.action.DREAMING_STOPPED");
        screenFilter.addAction("android.intent.action.BATTERY_CHANGED");
        this.mContext.registerReceiver(this.mScreenReceiver, screenFilter, null, this.mHandler);
        synchronized (this.mLock) {
            addInternalVolumeLocked();
        }
        Watchdog.getInstance().addMonitor(this);
        initOppoStorageFeature();
    }

    public void start() {
        lambda$connect$1$StorageManagerService();
    }

    /* access modifiers changed from: private */
    /* renamed from: connect */
    public void lambda$connect$1$StorageManagerService() {
        IBinder binder = ServiceManager.getService("storaged");
        if (binder != null) {
            try {
                binder.linkToDeath(new IBinder.DeathRecipient() {
                    /* class com.android.server.StorageManagerService.AnonymousClass6 */

                    public void binderDied() {
                        Slog.w(StorageManagerService.TAG, "storaged died; reconnecting");
                        IStoraged unused = StorageManagerService.this.mStoraged = null;
                        StorageManagerService.this.lambda$connect$1$StorageManagerService();
                    }
                }, 0);
            } catch (RemoteException e) {
                binder = null;
            }
        }
        if (binder != null) {
            this.mStoraged = IStoraged.Stub.asInterface(binder);
        } else {
            Slog.w(TAG, "storaged not found; trying again");
        }
        IBinder binder2 = ServiceManager.getService("vold");
        if (binder2 != null) {
            try {
                binder2.linkToDeath(new IBinder.DeathRecipient() {
                    /* class com.android.server.StorageManagerService.AnonymousClass7 */

                    public void binderDied() {
                        Slog.w(StorageManagerService.TAG, "vold died; reconnecting");
                        IVold unused = StorageManagerService.this.mVold = null;
                        StorageManagerService.this.lambda$connect$1$StorageManagerService();
                    }
                }, 0);
            } catch (RemoteException e2) {
                binder2 = null;
            }
        }
        if (binder2 != null) {
            this.mVold = IVold.Stub.asInterface(binder2);
            try {
                this.mVold.setListener(this.mListener);
            } catch (RemoteException e3) {
                this.mVold = null;
                Slog.w(TAG, "vold listener rejected; trying again", e3);
            }
        } else {
            Slog.w(TAG, "vold not found; trying again");
        }
        if (this.mStoraged == null || this.mVold == null) {
            BackgroundThread.getHandler().postDelayed(new Runnable() {
                /* class com.android.server.$$Lambda$StorageManagerService$cjI7lxk7c1DOoxF4YyMUk3dhoPA */

                public final void run() {
                    StorageManagerService.this.lambda$connect$1$StorageManagerService();
                }
            }, 1000);
        } else {
            onDaemonConnected();
        }
    }

    /* access modifiers changed from: private */
    public void servicesReady() {
        this.mPmInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mIPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService(com.android.server.pm.Settings.ATTR_PACKAGE));
        this.mIAppOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        try {
            this.mIAppOpsService.startWatchingMode(66, (String) null, this.mAppOpsCallback);
            this.mIAppOpsService.startWatchingMode(87, (String) null, this.mAppOpsCallback);
        } catch (RemoteException e) {
        }
    }

    private static long getLastAccessTime(AppOpsManager manager, int uid, String packageName, int[] ops) {
        long maxTime = 0;
        for (AppOpsManager.PackageOps pkg : CollectionUtils.emptyIfNull(manager.getOpsForPackage(uid, packageName, ops))) {
            for (AppOpsManager.OpEntry op : CollectionUtils.emptyIfNull(pkg.getOps())) {
                maxTime = Math.max(maxTime, op.getLastAccessTime(13));
            }
        }
        return maxTime;
    }

    /* access modifiers changed from: private */
    public void systemReady() {
        ((ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class)).registerScreenObserver(this);
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void bootCompleted() {
        this.mBootCompleted = true;
        this.mHandler.obtainMessage(13).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void handleBootCompleted() {
        initIfBootedAndConnected();
        resetIfBootedAndConnected();
    }

    private String getDefaultPrimaryStorageUuid() {
        if (SystemProperties.getBoolean("ro.vold.primary_physical", false)) {
            return "primary_physical";
        }
        return StorageManager.UUID_PRIVATE_INTERNAL;
    }

    @GuardedBy({"mLock"})
    private void readSettingsLocked() {
        this.mRecords.clear();
        this.mPrimaryStorageUuid = getDefaultPrimaryStorageUuid();
        FileInputStream fis = null;
        try {
            fis = this.mSettingsFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(fis, StandardCharsets.UTF_8.name());
            while (true) {
                int type = in.next();
                boolean validAttr = true;
                if (type == 1) {
                    break;
                } else if (type == 2) {
                    String tag = in.getName();
                    if (TAG_VOLUMES.equals(tag)) {
                        int version = XmlUtils.readIntAttribute(in, "version", 1);
                        boolean primaryPhysical = SystemProperties.getBoolean("ro.vold.primary_physical", false);
                        if (version < 3) {
                            if (version < 2 || primaryPhysical) {
                                validAttr = false;
                            }
                        }
                        if (validAttr) {
                            this.mPrimaryStorageUuid = XmlUtils.readStringAttribute(in, ATTR_PRIMARY_STORAGE_UUID);
                        }
                    } else if (TAG_VOLUME.equals(tag)) {
                        VolumeRecord rec = readVolumeRecord(in);
                        this.mRecords.put(rec.fsUuid, rec);
                    }
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e2) {
            Slog.wtf(TAG, "Failed reading metadata", e2);
        } catch (XmlPullParserException e3) {
            Slog.wtf(TAG, "Failed reading metadata", e3);
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
        IoUtils.closeQuietly(fis);
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void writeSettingsLocked() {
        try {
            FileOutputStream fos = this.mSettingsFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.startTag(null, TAG_VOLUMES);
            XmlUtils.writeIntAttribute(out, "version", 3);
            XmlUtils.writeStringAttribute(out, ATTR_PRIMARY_STORAGE_UUID, this.mPrimaryStorageUuid);
            int size = this.mRecords.size();
            for (int i = 0; i < size; i++) {
                writeVolumeRecord(out, this.mRecords.valueAt(i));
            }
            out.endTag(null, TAG_VOLUMES);
            out.endDocument();
            this.mSettingsFile.finishWrite(fos);
        } catch (IOException e) {
            if (0 != 0) {
                this.mSettingsFile.failWrite(null);
            }
        }
    }

    public static VolumeRecord readVolumeRecord(XmlPullParser in) throws IOException {
        VolumeRecord meta = new VolumeRecord(XmlUtils.readIntAttribute(in, "type"), XmlUtils.readStringAttribute(in, ATTR_FS_UUID));
        meta.partGuid = XmlUtils.readStringAttribute(in, ATTR_PART_GUID);
        meta.nickname = XmlUtils.readStringAttribute(in, ATTR_NICKNAME);
        meta.userFlags = XmlUtils.readIntAttribute(in, ATTR_USER_FLAGS);
        meta.createdMillis = XmlUtils.readLongAttribute(in, ATTR_CREATED_MILLIS, 0);
        meta.lastSeenMillis = XmlUtils.readLongAttribute(in, ATTR_LAST_SEEN_MILLIS, 0);
        meta.lastTrimMillis = XmlUtils.readLongAttribute(in, ATTR_LAST_TRIM_MILLIS, 0);
        meta.lastBenchMillis = XmlUtils.readLongAttribute(in, ATTR_LAST_BENCH_MILLIS, 0);
        return meta;
    }

    public static void writeVolumeRecord(XmlSerializer out, VolumeRecord rec) throws IOException {
        out.startTag(null, TAG_VOLUME);
        XmlUtils.writeIntAttribute(out, "type", rec.type);
        XmlUtils.writeStringAttribute(out, ATTR_FS_UUID, rec.fsUuid);
        XmlUtils.writeStringAttribute(out, ATTR_PART_GUID, rec.partGuid);
        XmlUtils.writeStringAttribute(out, ATTR_NICKNAME, rec.nickname);
        XmlUtils.writeIntAttribute(out, ATTR_USER_FLAGS, rec.userFlags);
        XmlUtils.writeLongAttribute(out, ATTR_CREATED_MILLIS, rec.createdMillis);
        XmlUtils.writeLongAttribute(out, ATTR_LAST_SEEN_MILLIS, rec.lastSeenMillis);
        XmlUtils.writeLongAttribute(out, ATTR_LAST_TRIM_MILLIS, rec.lastTrimMillis);
        XmlUtils.writeLongAttribute(out, ATTR_LAST_BENCH_MILLIS, rec.lastBenchMillis);
        out.endTag(null, TAG_VOLUME);
    }

    public void registerListener(IStorageEventListener listener) {
        this.mCallbacks.register(listener);
    }

    public void unregisterListener(IStorageEventListener listener) {
        this.mCallbacks.unregister(listener);
    }

    public void shutdown(IStorageShutdownObserver observer) {
        enforcePermission("android.permission.SHUTDOWN");
        Slog.i(TAG, "Shutting down");
        this.mHandler.obtainMessage(3, observer).sendToTarget();
    }

    public void mount(String volId) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        VolumeInfo vol = findVolumeByIdOrThrow(volId);
        if (!isMountDisallowed(vol)) {
            mount(vol);
            return;
        }
        throw new SecurityException("Mounting " + volId + " restricted by policy");
    }

    /* access modifiers changed from: private */
    public void mount(VolumeInfo vol) {
        try {
            this.mVold.mount(vol.id, vol.mountFlags, vol.mountUserId);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void unmount(String volId) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        unmount(findVolumeByIdOrThrow(volId));
    }

    /* access modifiers changed from: private */
    public void unmount(VolumeInfo vol) {
        try {
            this.mVold.unmount(vol.id);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void format(String volId) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        try {
            this.mVold.format(findVolumeByIdOrThrow(volId).id, UiModeManagerService.Shell.NIGHT_MODE_STR_AUTO);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void benchmark(String volId, final IVoldTaskListener listener) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        try {
            this.mVold.benchmark(volId, new IVoldTaskListener.Stub() {
                /* class com.android.server.StorageManagerService.AnonymousClass8 */

                @Override // android.os.IVoldTaskListener
                public void onStatus(int status, PersistableBundle extras) {
                    StorageManagerService.this.dispatchOnStatus(listener, status, extras);
                }

                @Override // android.os.IVoldTaskListener
                public void onFinished(int status, PersistableBundle extras) {
                    StorageManagerService.this.dispatchOnFinished(listener, status, extras);
                    String path = extras.getString("path");
                    String ident = extras.getString("ident");
                    long create = extras.getLong("create");
                    long run = extras.getLong("run");
                    long destroy = extras.getLong("destroy");
                    ((DropBoxManager) StorageManagerService.this.mContext.getSystemService(DropBoxManager.class)).addText(StorageManagerService.TAG_STORAGE_BENCHMARK, StorageManagerService.this.scrubPath(path) + StringUtils.SPACE + ident + StringUtils.SPACE + create + StringUtils.SPACE + run + StringUtils.SPACE + destroy);
                    synchronized (StorageManagerService.this.mLock) {
                        VolumeRecord rec = StorageManagerService.this.findRecordForPath(path);
                        if (rec != null) {
                            rec.lastBenchMillis = System.currentTimeMillis();
                            StorageManagerService.this.writeSettingsLocked();
                        }
                    }
                }
            });
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void partitionPublic(String diskId) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        CountDownLatch latch = findOrCreateDiskScanLatch(diskId);
        try {
            this.mVold.partition(diskId, 0, -1);
            waitForLatch(latch, "partitionPublic", 180000);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void partitionPrivate(String diskId) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        enforceAdminUser();
        CountDownLatch latch = findOrCreateDiskScanLatch(diskId);
        try {
            this.mVold.partition(diskId, 1, -1);
            waitForLatch(latch, "partitionPrivate", 180000);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void partitionMixed(String diskId, int ratio) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        enforceAdminUser();
        CountDownLatch latch = findOrCreateDiskScanLatch(diskId);
        try {
            this.mVold.partition(diskId, 2, ratio);
            waitForLatch(latch, "partitionMixed", 180000);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void setVolumeNickname(String fsUuid, String nickname) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        Preconditions.checkNotNull(fsUuid);
        synchronized (this.mLock) {
            VolumeRecord rec = this.mRecords.get(fsUuid);
            rec.nickname = nickname;
            this.mCallbacks.notifyVolumeRecordChanged(rec);
            writeSettingsLocked();
        }
    }

    public void setVolumeUserFlags(String fsUuid, int flags, int mask) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        Preconditions.checkNotNull(fsUuid);
        synchronized (this.mLock) {
            VolumeRecord rec = this.mRecords.get(fsUuid);
            rec.userFlags = (rec.userFlags & (~mask)) | (flags & mask);
            this.mCallbacks.notifyVolumeRecordChanged(rec);
            writeSettingsLocked();
        }
    }

    public void forgetVolume(String fsUuid) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        Preconditions.checkNotNull(fsUuid);
        synchronized (this.mLock) {
            VolumeRecord rec = this.mRecords.remove(fsUuid);
            if (rec != null && !TextUtils.isEmpty(rec.partGuid)) {
                this.mHandler.obtainMessage(9, rec).sendToTarget();
            }
            this.mCallbacks.notifyVolumeForgotten(fsUuid);
            if (Objects.equals(this.mPrimaryStorageUuid, fsUuid)) {
                this.mPrimaryStorageUuid = getDefaultPrimaryStorageUuid();
                this.mHandler.obtainMessage(10).sendToTarget();
            }
            writeSettingsLocked();
        }
    }

    public void forgetAllVolumes() {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        synchronized (this.mLock) {
            for (int i = 0; i < this.mRecords.size(); i++) {
                String fsUuid = this.mRecords.keyAt(i);
                VolumeRecord rec = this.mRecords.valueAt(i);
                if (!TextUtils.isEmpty(rec.partGuid)) {
                    this.mHandler.obtainMessage(9, rec).sendToTarget();
                }
                this.mCallbacks.notifyVolumeForgotten(fsUuid);
            }
            this.mRecords.clear();
            if (!Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, this.mPrimaryStorageUuid)) {
                this.mPrimaryStorageUuid = getDefaultPrimaryStorageUuid();
            }
            writeSettingsLocked();
            this.mHandler.obtainMessage(10).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public void forgetPartition(String partGuid, String fsUuid) {
        try {
            this.mVold.forgetPartition(partGuid, fsUuid);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void fstrim(int flags, final IVoldTaskListener listener) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        try {
            if (needsCheckpoint()) {
                if (supportsBlockCheckpoint()) {
                    Slog.i(TAG, "Skipping fstrim - block based checkpoint in progress");
                    return;
                }
            }
            this.mVold.fstrim(flags, new IVoldTaskListener.Stub() {
                /* class com.android.server.StorageManagerService.AnonymousClass9 */

                @Override // android.os.IVoldTaskListener
                public void onStatus(int status, PersistableBundle extras) {
                    StorageManagerService.this.dispatchOnStatus(listener, status, extras);
                    if (status == 0) {
                        String path = extras.getString("path");
                        long bytes = extras.getLong("bytes");
                        long time = extras.getLong("time");
                        ((DropBoxManager) StorageManagerService.this.mContext.getSystemService(DropBoxManager.class)).addText(StorageManagerService.TAG_STORAGE_TRIM, StorageManagerService.this.scrubPath(path) + StringUtils.SPACE + bytes + StringUtils.SPACE + time);
                        synchronized (StorageManagerService.this.mLock) {
                            VolumeRecord rec = StorageManagerService.this.findRecordForPath(path);
                            if (rec != null) {
                                rec.lastTrimMillis = System.currentTimeMillis();
                                StorageManagerService.this.writeSettingsLocked();
                            }
                        }
                    }
                }

                @Override // android.os.IVoldTaskListener
                public void onFinished(int status, PersistableBundle extras) {
                    StorageManagerService.this.dispatchOnFinished(listener, status, extras);
                }
            });
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean idleMaintable() {
        this.mMaintPrepared = true;
        Slog.w(TAG, "idleMaintable, screenOn:" + this.mScreenOn);
        if (this.mScreenOn) {
            return false;
        }
        long timeToIdle = 600000 - (System.currentTimeMillis() - this.mScreenOffTime);
        Slog.w(TAG, "idleMaintable, timeToIdle:" + timeToIdle);
        if (timeToIdle <= 0) {
            return true;
        }
        MountServiceIdler.scheduleIdlePass(this.mContext, timeToIdle);
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean maintAborted() {
        return this.mMaintAborted;
    }

    /* access modifiers changed from: package-private */
    public void runIdleMaint(final Runnable callback) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        this.mMaintAborted = false;
        try {
            if (needsCheckpoint()) {
                if (supportsBlockCheckpoint()) {
                    Slog.i(TAG, "Skipping idle maintenance - block based checkpoint in progress");
                    return;
                }
            }
            this.mVold.runIdleMaint(new IVoldTaskListener.Stub() {
                /* class com.android.server.StorageManagerService.AnonymousClass10 */

                @Override // android.os.IVoldTaskListener
                public void onStatus(int status, PersistableBundle extras) {
                }

                @Override // android.os.IVoldTaskListener
                public void onFinished(int status, PersistableBundle extras) {
                    Slog.w(StorageManagerService.TAG, "run idle maint finished, maintAborted:" + StorageManagerService.this.mMaintAborted);
                    if (!StorageManagerService.this.mMaintAborted) {
                        boolean unused = StorageManagerService.this.mMaintPrepared = false;
                        try {
                            long unused2 = StorageManagerService.this.mLastMaintenance = System.currentTimeMillis();
                            StorageManagerService.this.mLastMaintenanceFile.setLastModified(StorageManagerService.this.mLastMaintenance);
                        } catch (Exception e) {
                            Slog.e(StorageManagerService.TAG, "Unable to record last maintenance!");
                        }
                    }
                    if (callback != null) {
                        BackgroundThread.getHandler().post(callback);
                    }
                }
            });
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void runIdleMaintenance() {
        runIdleMaint(null);
    }

    /* access modifiers changed from: package-private */
    public void abortIdleMaint(final Runnable callback) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        this.mMaintAborted = true;
        try {
            this.mVold.abortIdleMaint(new IVoldTaskListener.Stub() {
                /* class com.android.server.StorageManagerService.AnonymousClass11 */

                @Override // android.os.IVoldTaskListener
                public void onStatus(int status, PersistableBundle extras) {
                }

                @Override // android.os.IVoldTaskListener
                public void onFinished(int status, PersistableBundle extras) {
                    if (callback != null) {
                        BackgroundThread.getHandler().post(callback);
                    }
                }
            });
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void abortIdleMaintenance() {
        abortIdleMaint(null);
    }

    /* access modifiers changed from: private */
    public void remountUidExternalStorage(int uid, int mode) {
        try {
            this.mVold.remountUid(uid, mode);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void setDebugFlags(int flags, int mask) {
        int value;
        String value2;
        String value3;
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        boolean enabled = false;
        if ((mask & 4) != 0) {
            if (StorageManager.isFileEncryptedNativeOnly()) {
                throw new IllegalStateException("Emulation not supported on device with native FBE");
            } else if (!this.mLockPatternUtils.isCredentialRequiredToDecrypt(false)) {
                long token = Binder.clearCallingIdentity();
                try {
                    SystemProperties.set("persist.sys.emulate_fbe", Boolean.toString((flags & 4) != 0));
                    ((PowerManager) this.mContext.getSystemService(PowerManager.class)).reboot(null);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new IllegalStateException("Emulation requires disabling 'Secure start-up' in Settings > Security");
            }
        }
        if ((mask & 3) != 0) {
            if ((flags & 1) != 0) {
                value3 = "force_on";
            } else if ((flags & 2) != 0) {
                value3 = "force_off";
            } else {
                value3 = "";
            }
            long token2 = Binder.clearCallingIdentity();
            try {
                SystemProperties.set("persist.sys.adoptable", value3);
                this.mHandler.obtainMessage(10).sendToTarget();
            } finally {
                Binder.restoreCallingIdentity(token2);
            }
        }
        if ((mask & 24) != 0) {
            if ((flags & 8) != 0) {
                value2 = "force_on";
            } else if ((flags & 16) != 0) {
                value2 = "force_off";
            } else {
                value2 = "";
            }
            long token3 = Binder.clearCallingIdentity();
            try {
                SystemProperties.set("persist.sys.sdcardfs", value2);
                this.mHandler.obtainMessage(10).sendToTarget();
            } finally {
                Binder.restoreCallingIdentity(token3);
            }
        }
        if ((mask & 32) != 0) {
            if ((flags & 32) != 0) {
                enabled = true;
            }
            long token4 = Binder.clearCallingIdentity();
            try {
                SystemProperties.set("persist.sys.virtual_disk", Boolean.toString(enabled));
                this.mHandler.obtainMessage(10).sendToTarget();
            } finally {
                Binder.restoreCallingIdentity(token4);
            }
        }
        if (mask != false && true) {
            if ((flags & 64) != 0) {
                value = 1;
            } else if ((flags & 128) != 0) {
                value = -1;
            } else {
                value = 0;
            }
            long token5 = Binder.clearCallingIdentity();
            try {
                Settings.Global.putInt(this.mContext.getContentResolver(), "isolated_storage_local", value);
                refreshIsolatedStorageSettings();
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.$$Lambda$StorageManagerService$uqP0K_3jWGo_BEhUmSlAIUImjM4 */

                    public final void run() {
                        StorageManagerService.this.lambda$setDebugFlags$2$StorageManagerService();
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token5);
            }
        }
    }

    public /* synthetic */ void lambda$setDebugFlags$2$StorageManagerService() {
        ((PowerManager) this.mContext.getSystemService(PowerManager.class)).reboot(null);
    }

    public String getPrimaryStorageUuid() {
        String str;
        synchronized (this.mLock) {
            str = this.mPrimaryStorageUuid;
        }
        return str;
    }

    public void setPrimaryStorageUuid(String volumeUuid, IPackageMoveObserver callback) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        synchronized (this.mLock) {
            if (Objects.equals(this.mPrimaryStorageUuid, volumeUuid)) {
                throw new IllegalArgumentException("Primary storage already at " + volumeUuid);
            } else if (this.mMoveCallback == null) {
                this.mMoveCallback = callback;
                this.mMoveTargetUuid = volumeUuid;
                for (UserInfo user : ((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers()) {
                    if (StorageManager.isFileEncryptedNativeOrEmulated() && !isUserKeyUnlocked(user.id)) {
                        Slog.w(TAG, "Failing move due to locked user " + user.id);
                        onMoveStatusLocked(-10);
                        return;
                    }
                }
                if (!Objects.equals("primary_physical", this.mPrimaryStorageUuid)) {
                    if (!Objects.equals("primary_physical", volumeUuid)) {
                        VolumeInfo from = findStorageForUuid(this.mPrimaryStorageUuid);
                        VolumeInfo to = findStorageForUuid(volumeUuid);
                        if (from == null) {
                            Slog.w(TAG, "Failing move due to missing from volume " + this.mPrimaryStorageUuid);
                            onMoveStatusLocked(-6);
                            return;
                        } else if (to == null) {
                            Slog.w(TAG, "Failing move due to missing to volume " + volumeUuid);
                            onMoveStatusLocked(-6);
                            return;
                        } else {
                            try {
                                this.mVold.moveStorage(from.id, to.id, new IVoldTaskListener.Stub() {
                                    /* class com.android.server.StorageManagerService.AnonymousClass12 */

                                    @Override // android.os.IVoldTaskListener
                                    public void onStatus(int status, PersistableBundle extras) {
                                        synchronized (StorageManagerService.this.mLock) {
                                            StorageManagerService.this.onMoveStatusLocked(status);
                                        }
                                    }

                                    @Override // android.os.IVoldTaskListener
                                    public void onFinished(int status, PersistableBundle extras) {
                                    }
                                });
                                return;
                            } catch (Exception e) {
                                Slog.wtf(TAG, e);
                                return;
                            }
                        }
                    }
                }
                Slog.d(TAG, "Skipping move to/from primary physical");
                onMoveStatusLocked(82);
                onMoveStatusLocked(-100);
                this.mHandler.obtainMessage(10).sendToTarget();
            } else {
                throw new IllegalStateException("Move already in progress");
            }
        }
    }

    /* access modifiers changed from: private */
    public void warnOnNotMounted() {
        synchronized (this.mLock) {
            int i = 0;
            while (i < this.mVolumes.size()) {
                VolumeInfo vol = this.mVolumes.valueAt(i);
                if (!vol.isPrimary() || !vol.isMountedWritable()) {
                    i++;
                } else {
                    return;
                }
            }
            Slog.w(TAG, "No primary storage mounted!");
        }
    }

    /* access modifiers changed from: private */
    public boolean isUidOwnerOfPackageOrSystem(String packageName, int callerUid) {
        if (callerUid == 1000) {
            return true;
        }
        if (packageName == null) {
            return false;
        }
        if (callerUid == this.mPmInternal.getPackageUid(packageName, 268435456, UserHandle.getUserId(callerUid))) {
            return true;
        }
        return false;
    }

    public String getMountedObbPath(String rawPath) {
        ObbState state;
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        warnOnNotMounted();
        synchronized (this.mObbMounts) {
            state = this.mObbPathToStateMap.get(rawPath);
        }
        if (state != null) {
            return findVolumeByIdOrThrow(state.volId).getPath().getAbsolutePath();
        }
        Slog.w(TAG, "Failed to find OBB mounted at " + rawPath);
        return null;
    }

    public boolean isObbMounted(String rawPath) {
        boolean containsKey;
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        synchronized (this.mObbMounts) {
            containsKey = this.mObbPathToStateMap.containsKey(rawPath);
        }
        return containsKey;
    }

    public void mountObb(String rawPath, String canonicalPath, String key, IObbActionListener token, int nonce, ObbInfo obbInfo) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        Preconditions.checkNotNull(canonicalPath, "canonicalPath cannot be null");
        Preconditions.checkNotNull(token, "token cannot be null");
        Preconditions.checkNotNull(obbInfo, "obbIfno cannot be null");
        int callingUid = Binder.getCallingUid();
        ObbAction action = new MountObbAction(new ObbState(rawPath, canonicalPath, callingUid, token, nonce, null), key, callingUid, obbInfo);
        ObbActionHandler obbActionHandler = this.mObbActionHandler;
        obbActionHandler.sendMessage(obbActionHandler.obtainMessage(1, action));
    }

    public void unmountObb(String rawPath, boolean force, IObbActionListener token, int nonce) {
        ObbState existingState;
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        synchronized (this.mObbMounts) {
            existingState = this.mObbPathToStateMap.get(rawPath);
        }
        if (existingState != null) {
            ObbAction action = new UnmountObbAction(new ObbState(rawPath, existingState.canonicalPath, Binder.getCallingUid(), token, nonce, existingState.volId), force);
            ObbActionHandler obbActionHandler = this.mObbActionHandler;
            obbActionHandler.sendMessage(obbActionHandler.obtainMessage(1, action));
            return;
        }
        Slog.w(TAG, "Unknown OBB mount at " + rawPath);
    }

    public int getEncryptionState() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        try {
            return this.mVold.fdeComplete();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return -1;
        }
    }

    public int decryptStorage(String password) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        if (!TextUtils.isEmpty(password)) {
            try {
                this.mVold.fdeCheckPassword(password);
                this.mHandler.postDelayed(new Runnable() {
                    /* class com.android.server.$$Lambda$StorageManagerService$2rDVUdOXDdDWJMpN6OOM5W8W9RY */

                    public final void run() {
                        StorageManagerService.this.lambda$decryptStorage$3$StorageManagerService();
                    }
                }, 1000);
                return 0;
            } catch (ServiceSpecificException e) {
                Slog.e(TAG, "fdeCheckPassword failed", e);
                return e.errorCode;
            } catch (Exception e2) {
                Slog.wtf(TAG, e2);
                return -1;
            }
        } else {
            throw new IllegalArgumentException("password cannot be empty");
        }
    }

    public /* synthetic */ void lambda$decryptStorage$3$StorageManagerService() {
        try {
            this.mVold.fdeRestart();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public int encryptStorage(int type, String password) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        if (type == 1) {
            password = "";
        } else if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("password cannot be empty");
        }
        try {
            this.mVold.fdeEnable(type, password, 0);
            return 0;
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return -1;
        }
    }

    public int changeEncryptionPassword(int type, String password) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        if (StorageManager.isFileEncryptedNativeOnly()) {
            return -1;
        }
        if (type == 1) {
            password = "";
        } else if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("password cannot be empty");
        }
        try {
            this.mVold.fdeChangePassword(type, password);
            return 0;
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return -1;
        }
    }

    public int verifyEncryptionPassword(String password) throws RemoteException {
        if (Binder.getCallingUid() == 1000) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
            if (!TextUtils.isEmpty(password)) {
                try {
                    this.mVold.fdeVerifyPassword(password);
                    return 0;
                } catch (Exception e) {
                    Slog.wtf(TAG, e);
                    return -1;
                }
            } else {
                throw new IllegalArgumentException("password cannot be empty");
            }
        } else {
            throw new SecurityException("no permission to access the crypt keeper");
        }
    }

    public int getPasswordType() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        try {
            return this.mVold.fdeGetPasswordType();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return -1;
        }
    }

    public void setField(String field, String contents) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        if (StorageManager.isBlockEncrypted()) {
            try {
                this.mVold.fdeSetField(field, contents);
            } catch (Exception e) {
                Slog.wtf(TAG, e);
            }
        }
    }

    public String getField(String field) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        if (!StorageManager.isBlockEncrypted()) {
            return null;
        }
        try {
            return this.mVold.fdeGetField(field);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return null;
        }
    }

    public boolean isConvertibleToFBE() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        try {
            return this.mVold.isConvertibleToFbe();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return false;
        }
    }

    public boolean supportsCheckpoint() throws RemoteException {
        if (Binder.getCallingUid() == 1000) {
            return this.mVold.supportsCheckpoint();
        }
        throw new SecurityException("no permission to check filesystem checkpoint support");
    }

    public void startCheckpoint(int numTries) throws RemoteException {
        if (Binder.getCallingUid() == 1000) {
            this.mVold.startCheckpoint(numTries);
            return;
        }
        throw new SecurityException("no permission to start filesystem checkpoint");
    }

    public void commitChanges() throws RemoteException {
        if (Binder.getCallingUid() == 1000) {
            this.mVold.commitChanges();
            return;
        }
        throw new SecurityException("no permission to commit checkpoint changes");
    }

    public boolean needsCheckpoint() throws RemoteException {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        return this.mVold.needsCheckpoint();
    }

    public void abortChanges(String message, boolean retry) throws RemoteException {
        if (Binder.getCallingUid() == 1000) {
            this.mVold.abortChanges(message, retry);
            return;
        }
        throw new SecurityException("no permission to commit checkpoint changes");
    }

    public String getPassword() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "only keyguard can retrieve password");
        try {
            return this.mVold.fdeGetPassword();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return null;
        }
    }

    public void clearPassword() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "only keyguard can clear password");
        try {
            this.mVold.fdeClearPassword();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void createUserKey(int userId, int serialNumber, boolean ephemeral) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.createUserKey(userId, serialNumber, ephemeral);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void destroyUserKey(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.destroyUserKey(userId);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    private String encodeBytes(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return "!";
        }
        return HexDump.toHexString(bytes);
    }

    public void addUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.addUserKeyAuth(userId, serialNumber, encodeBytes(token), encodeBytes(secret));
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void fixateNewestUserKeyAuth(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.fixateNewestUserKeyAuth(userId);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void unlockUserKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        Slog.d(TAG, "unlockUserKey: " + userId);
        enforcePermission("android.permission.STORAGE_INTERNAL");
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            if (!this.mLockPatternUtils.isSecure(userId) || !ArrayUtils.isEmpty(secret)) {
                try {
                    this.mVold.unlockUserKey(userId, serialNumber, encodeBytes(token), encodeBytes(secret));
                } catch (Exception e) {
                    Slog.wtf(TAG, e);
                    return;
                }
            } else {
                throw new IllegalStateException("Secret required to unlock secure user " + userId);
            }
        }
        synchronized (this.mLock) {
            this.mLocalUnlockedUsers = ArrayUtils.appendInt(this.mLocalUnlockedUsers, userId);
        }
    }

    public void lockUserKey(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.lockUserKey(userId);
            synchronized (this.mLock) {
                this.mLocalUnlockedUsers = ArrayUtils.removeInt(this.mLocalUnlockedUsers, userId);
            }
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public boolean isUserKeyUnlocked(int userId) {
        boolean contains;
        synchronized (this.mLock) {
            contains = ArrayUtils.contains(this.mLocalUnlockedUsers, userId);
        }
        return contains;
    }

    private boolean isSystemUnlocked(int userId) {
        boolean contains;
        synchronized (this.mLock) {
            contains = ArrayUtils.contains(this.mSystemUnlockedUsers, userId);
        }
        return contains;
    }

    public void prepareUserStorage(String volumeUuid, int userId, int serialNumber, int flags) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.prepareUserStorage(volumeUuid, userId, serialNumber, flags);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void destroyUserStorage(String volumeUuid, int userId, int flags) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.destroyUserStorage(volumeUuid, userId, flags);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    class AppFuseMountScope extends AppFuseBridge.MountScope {
        private boolean mMounted = false;

        public AppFuseMountScope(int uid, int mountId) {
            super(uid, mountId);
        }

        @Override // com.android.server.storage.AppFuseBridge.MountScope
        public ParcelFileDescriptor open() throws NativeDaemonConnectorException {
            try {
                FileDescriptor fd = StorageManagerService.this.mVold.mountAppFuse(this.uid, this.mountId);
                this.mMounted = true;
                return new ParcelFileDescriptor(fd);
            } catch (Exception e) {
                throw new NativeDaemonConnectorException("Failed to mount", e);
            }
        }

        @Override // com.android.server.storage.AppFuseBridge.MountScope
        public ParcelFileDescriptor openFile(int mountId, int fileId, int flags) throws NativeDaemonConnectorException {
            try {
                return new ParcelFileDescriptor(StorageManagerService.this.mVold.openAppFuseFile(this.uid, mountId, fileId, flags));
            } catch (Exception e) {
                throw new NativeDaemonConnectorException("Failed to open", e);
            }
        }

        @Override // java.lang.AutoCloseable
        public void close() throws Exception {
            if (this.mMounted) {
                StorageManagerService.this.mVold.unmountAppFuse(this.uid, this.mountId);
                this.mMounted = false;
            }
        }
    }

    public AppFuseMount mountProxyFileDescriptorBridge() {
        AppFuseMount appFuseMount;
        Slog.v(TAG, "mountProxyFileDescriptorBridge");
        int uid = Binder.getCallingUid();
        while (true) {
            synchronized (this.mAppFuseLock) {
                boolean newlyCreated = false;
                if (this.mAppFuseBridge == null) {
                    this.mAppFuseBridge = new AppFuseBridge();
                    new Thread(this.mAppFuseBridge, AppFuseBridge.TAG).start();
                    newlyCreated = true;
                }
                try {
                    int name = this.mNextAppFuseName;
                    this.mNextAppFuseName = name + 1;
                    try {
                        appFuseMount = new AppFuseMount(name, this.mAppFuseBridge.addBridge(new AppFuseMountScope(uid, name)));
                    } catch (FuseUnavailableMountException e) {
                        if (newlyCreated) {
                            Slog.e(TAG, "", e);
                            return null;
                        }
                        this.mAppFuseBridge = null;
                    }
                } catch (NativeDaemonConnectorException e2) {
                    throw e2.rethrowAsParcelableException();
                }
            }
            return appFuseMount;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0027, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0028, code lost:
        android.util.Slog.v(com.android.server.StorageManagerService.TAG, "The mount point has already been invalid", r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002f, code lost:
        return null;
     */
    public ParcelFileDescriptor openProxyFileDescriptor(int mountId, int fileId, int mode) {
        Slog.v(TAG, "mountProxyFileDescriptor");
        int mode2 = mode & 805306368;
        synchronized (this.mAppFuseLock) {
            if (this.mAppFuseBridge == null) {
                Slog.e(TAG, "FuseBridge has not been created");
                return null;
            }
            ParcelFileDescriptor openFile = this.mAppFuseBridge.openFile(mountId, fileId, mode2);
            return openFile;
        }
    }

    public void mkdirs(String callingPkg, String appPath) {
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(callingUid);
        Environment.UserEnvironment userEnv = new Environment.UserEnvironment(userId);
        String propertyName = "sys.user." + userId + ".ce_available";
        if (!isUserKeyUnlocked(userId)) {
            throw new IllegalStateException("Failed to prepare " + appPath);
        } else if (userId != 0 || SystemProperties.getBoolean(propertyName, false)) {
            ((AppOpsManager) this.mContext.getSystemService("appops")).checkPackage(callingUid, callingPkg);
            try {
                File appFile = new File(appPath).getCanonicalFile();
                if (FileUtils.contains(userEnv.buildExternalStorageAppDataDirs(callingPkg), appFile) || FileUtils.contains(userEnv.buildExternalStorageAppObbDirs(callingPkg), appFile) || FileUtils.contains(userEnv.buildExternalStorageAppMediaDirs(callingPkg), appFile)) {
                    String appPath2 = appFile.getAbsolutePath();
                    if (!appPath2.endsWith(SliceClientPermissions.SliceAuthority.DELIMITER)) {
                        appPath2 = appPath2 + SliceClientPermissions.SliceAuthority.DELIMITER;
                    }
                    try {
                        this.mVold.mkdirs(appPath2);
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to prepare " + appPath2 + ": " + e);
                    }
                } else {
                    throw new SecurityException("Invalid mkdirs path: " + appFile);
                }
            } catch (IOException e2) {
                throw new IllegalStateException("Failed to resolve " + appPath + ": " + e2);
            }
        } else {
            throw new IllegalStateException("Failed to prepare " + appPath);
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0113, code lost:
        if (r18 != false) goto L_0x0168;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0115, code lost:
        android.util.Slog.w(com.android.server.StorageManagerService.TAG, "No primary storage defined yet; hacking together a stub");
        r0 = android.os.SystemProperties.getBoolean("ro.vold.primary_physical", false);
        r3 = android.os.Environment.getLegacyExternalStorageDirectory();
        r17.add(0, new android.os.storage.StorageVolume("stub_primary", r3, r3, r38.mContext.getString(17039374), true, r0, !r0, false, 0, new android.os.UserHandle(r4), null, "removed"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0174, code lost:
        return (android.os.storage.StorageVolume[]) r17.toArray(new android.os.storage.StorageVolume[r17.size()]);
     */
    public StorageVolume[] getVolumeList(int uid, String packageName, int flags) {
        boolean forWrite;
        boolean realState;
        ArrayList<StorageVolume> res;
        boolean match;
        String str = packageName;
        int userId = UserHandle.getUserId(uid);
        boolean forWrite2 = (flags & 256) != 0;
        boolean realState2 = (flags & 512) != 0;
        boolean includeInvisible = (flags & 1024) != 0;
        boolean systemUserUnlocked = isSystemUnlocked(0);
        long token = Binder.clearCallingIdentity();
        try {
            boolean userKeyUnlocked = isUserKeyUnlocked(userId);
            boolean storagePermission = this.mStorageManagerInternal.hasExternalStorage(uid, str);
            Binder.restoreCallingIdentity(token);
            ArrayList<StorageVolume> res2 = new ArrayList<>();
            synchronized (this.mLock) {
                boolean foundPrimary = false;
                int i = 0;
                while (i < this.mVolumes.size()) {
                    try {
                        VolumeInfo vol = this.mVolumes.valueAt(i);
                        int type = vol.getType();
                        if (type == 0 || type == 2 || type == 5) {
                            if (forWrite2) {
                                try {
                                    match = vol.isVisibleForWrite(userId);
                                } catch (Throwable th) {
                                    th = th;
                                    throw th;
                                }
                            } else {
                                match = vol.isVisibleForRead(userId) || (includeInvisible && vol.getPath() != null);
                            }
                            if (!match) {
                                forWrite = forWrite2;
                                res = res2;
                                realState = realState2;
                            } else {
                                boolean reportUnmounted = false;
                                if (!systemUserUnlocked) {
                                    reportUnmounted = true;
                                    forWrite = forWrite2;
                                } else {
                                    forWrite = forWrite2;
                                    if (vol.getType() == 2 && !userKeyUnlocked) {
                                        reportUnmounted = true;
                                    } else if (!storagePermission && !realState2) {
                                        reportUnmounted = true;
                                    }
                                }
                                try {
                                    StorageVolume userVol = vol.buildStorageVolume(this.mContext, userId, reportUnmounted);
                                    if (vol.isPrimary()) {
                                        res = res2;
                                        realState = realState2;
                                        res.add(0, userVol);
                                        if (userId == 0 && "com.android.providers.media".equals(str)) {
                                            res.add(0, vol.buildStorageVolume(this.mContext, (int) ZenModeHelper.OPPO_MULTI_USER_ID, reportUnmounted));
                                        }
                                        foundPrimary = true;
                                    } else {
                                        res = res2;
                                        realState = realState2;
                                        res.add(userVol);
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            }
                        } else {
                            forWrite = forWrite2;
                            res = res2;
                            realState = realState2;
                        }
                        i++;
                        str = packageName;
                        realState2 = realState;
                        res2 = res;
                        forWrite2 = forWrite;
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            }
        } catch (Throwable th4) {
            Binder.restoreCallingIdentity(token);
            throw th4;
        }
    }

    public DiskInfo[] getDisks() {
        DiskInfo[] res;
        synchronized (this.mLock) {
            res = new DiskInfo[this.mDisks.size()];
            for (int i = 0; i < this.mDisks.size(); i++) {
                res[i] = this.mDisks.valueAt(i);
            }
        }
        return res;
    }

    public VolumeInfo[] getVolumes(int flags) {
        VolumeInfo[] res;
        synchronized (this.mLock) {
            res = new VolumeInfo[this.mVolumes.size()];
            for (int i = 0; i < this.mVolumes.size(); i++) {
                res[i] = this.mVolumes.valueAt(i);
            }
        }
        return res;
    }

    public VolumeRecord[] getVolumeRecords(int flags) {
        VolumeRecord[] res;
        synchronized (this.mLock) {
            res = new VolumeRecord[this.mRecords.size()];
            for (int i = 0; i < this.mRecords.size(); i++) {
                res[i] = this.mRecords.valueAt(i);
            }
        }
        return res;
    }

    public long getCacheQuotaBytes(String volumeUuid, int uid) {
        if (uid != Binder.getCallingUid()) {
            this.mContext.enforceCallingPermission("android.permission.STORAGE_INTERNAL", TAG);
        }
        long token = Binder.clearCallingIdentity();
        try {
            return ((StorageStatsManager) this.mContext.getSystemService(StorageStatsManager.class)).getCacheQuotaBytes(volumeUuid, uid);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public long getCacheSizeBytes(String volumeUuid, int uid) {
        if (uid != Binder.getCallingUid()) {
            this.mContext.enforceCallingPermission("android.permission.STORAGE_INTERNAL", TAG);
        }
        long token = Binder.clearCallingIdentity();
        try {
            long cacheBytes = ((StorageStatsManager) this.mContext.getSystemService(StorageStatsManager.class)).queryStatsForUid(volumeUuid, uid).getCacheBytes();
            Binder.restoreCallingIdentity(token);
            return cacheBytes;
        } catch (IOException e) {
            throw new ParcelableException(e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private int adjustAllocateFlags(int flags, int callingUid, String callingPackage) {
        if ((flags & 1) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ALLOCATE_AGGRESSIVE", TAG);
        }
        int flags2 = flags & -3 & -5;
        AppOpsManager appOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        long token = Binder.clearCallingIdentity();
        try {
            if (appOps.isOperationActive(26, callingUid, callingPackage)) {
                Slog.d(TAG, "UID " + callingUid + " is actively using camera; letting them defy reserved cached data");
                flags2 |= 4;
            }
            return flags2;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.Math.max(long, long):long}
     arg types: [int, long]
     candidates:
      ClspMth{java.lang.Math.max(double, double):double}
      ClspMth{java.lang.Math.max(int, int):int}
      ClspMth{java.lang.Math.max(float, float):float}
      ClspMth{java.lang.Math.max(long, long):long} */
    public long getAllocatableBytes(String volumeUuid, int flags, String callingPackage) {
        int flags2 = adjustAllocateFlags(flags, Binder.getCallingUid(), callingPackage);
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        StorageStatsManager stats = (StorageStatsManager) this.mContext.getSystemService(StorageStatsManager.class);
        long token = Binder.clearCallingIdentity();
        try {
            File path = storage.findPathForUuid(volumeUuid);
            long usable = path.getUsableSpace();
            long lowReserved = storage.getStorageLowBytes(path);
            long fullReserved = storage.getStorageFullBytes(path);
            if (stats.isQuotaSupported(volumeUuid)) {
                long cacheClearable = Math.max(0L, stats.getCacheBytes(volumeUuid) - storage.getStorageCacheBytes(path, flags2));
                if ((flags2 & 1) != 0) {
                    try {
                        long max = Math.max(0L, (usable + cacheClearable) - fullReserved);
                        Binder.restoreCallingIdentity(token);
                        return max;
                    } catch (IOException e) {
                        e = e;
                        try {
                            throw new ParcelableException(e);
                        } catch (Throwable th) {
                            e = th;
                            Binder.restoreCallingIdentity(token);
                            throw e;
                        }
                    }
                } else {
                    long max2 = Math.max(0L, (usable + cacheClearable) - lowReserved);
                    Binder.restoreCallingIdentity(token);
                    return max2;
                }
            } else if ((flags2 & 1) != 0) {
                long max3 = Math.max(0L, usable - fullReserved);
                Binder.restoreCallingIdentity(token);
                return max3;
            } else {
                long max4 = Math.max(0L, usable - lowReserved);
                Binder.restoreCallingIdentity(token);
                return max4;
            }
        } catch (IOException e2) {
            e = e2;
            throw new ParcelableException(e);
        } catch (Throwable th2) {
            e = th2;
            Binder.restoreCallingIdentity(token);
            throw e;
        }
    }

    public void allocateBytes(String volumeUuid, long bytes, int flags, String callingPackage) {
        long bytes2;
        int flags2 = adjustAllocateFlags(flags, Binder.getCallingUid(), callingPackage);
        long allocatableBytes = getAllocatableBytes(volumeUuid, flags2, callingPackage);
        if (bytes <= allocatableBytes) {
            StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
            long token = Binder.clearCallingIdentity();
            try {
                File path = storage.findPathForUuid(volumeUuid);
                if ((flags2 & 1) != 0) {
                    bytes2 = bytes + storage.getStorageFullBytes(path);
                } else {
                    bytes2 = bytes + storage.getStorageLowBytes(path);
                }
                this.mPmInternal.freeStorage(volumeUuid, bytes2, flags2);
                Binder.restoreCallingIdentity(token);
            } catch (IOException e) {
                throw new ParcelableException(e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            throw new ParcelableException(new IOException("Failed to allocate " + bytes + " because only " + allocatableBytes + " allocatable"));
        }
    }

    /* access modifiers changed from: private */
    public void addObbStateLocked(ObbState obbState) throws RemoteException {
        IBinder binder = obbState.getBinder();
        List<ObbState> obbStates = this.mObbMounts.get(binder);
        if (obbStates == null) {
            obbStates = new ArrayList();
            this.mObbMounts.put(binder, obbStates);
        } else {
            for (ObbState o : obbStates) {
                if (o.rawPath.equals(obbState.rawPath)) {
                    throw new IllegalStateException("Attempt to add ObbState twice. This indicates an error in the StorageManagerService logic.");
                }
            }
        }
        obbStates.add(obbState);
        try {
            obbState.link();
            this.mObbPathToStateMap.put(obbState.rawPath, obbState);
        } catch (RemoteException e) {
            obbStates.remove(obbState);
            if (obbStates.isEmpty()) {
                this.mObbMounts.remove(binder);
            }
            throw e;
        }
    }

    /* access modifiers changed from: private */
    public void removeObbStateLocked(ObbState obbState) {
        IBinder binder = obbState.getBinder();
        List<ObbState> obbStates = this.mObbMounts.get(binder);
        if (obbStates != null) {
            if (obbStates.remove(obbState)) {
                obbState.unlink();
            }
            if (obbStates.isEmpty()) {
                this.mObbMounts.remove(binder);
            }
        }
        this.mObbPathToStateMap.remove(obbState.rawPath);
    }

    /* access modifiers changed from: private */
    public class ObbActionHandler extends Handler {
        ObbActionHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                ((ObbAction) msg.obj).execute(this);
            } else if (i == 2) {
                String path = (String) msg.obj;
                synchronized (StorageManagerService.this.mObbMounts) {
                    List<ObbState> obbStatesToRemove = new LinkedList<>();
                    for (ObbState state : StorageManagerService.this.mObbPathToStateMap.values()) {
                        if (state.canonicalPath.startsWith(path)) {
                            obbStatesToRemove.add(state);
                        }
                    }
                    for (ObbState obbState : obbStatesToRemove) {
                        StorageManagerService.this.removeObbStateLocked(obbState);
                        try {
                            obbState.token.onObbResult(obbState.rawPath, obbState.nonce, 2);
                        } catch (RemoteException e) {
                            Slog.i(StorageManagerService.TAG, "Couldn't send unmount notification for  OBB: " + obbState.rawPath);
                        }
                    }
                }
            }
        }
    }

    private static class ObbException extends Exception {
        public final int status;

        public ObbException(int status2, String message) {
            super(message);
            this.status = status2;
        }

        public ObbException(int status2, Throwable cause) {
            super(cause.getMessage(), cause);
            this.status = status2;
        }
    }

    abstract class ObbAction {
        ObbState mObbState;

        /* access modifiers changed from: package-private */
        public abstract void handleExecute() throws ObbException;

        ObbAction(ObbState obbState) {
            this.mObbState = obbState;
        }

        public void execute(ObbActionHandler handler) {
            try {
                handleExecute();
            } catch (ObbException e) {
                notifyObbStateChange(e);
            }
        }

        /* access modifiers changed from: protected */
        public void notifyObbStateChange(ObbException e) {
            Slog.w(StorageManagerService.TAG, e);
            notifyObbStateChange(e.status);
        }

        /* access modifiers changed from: protected */
        public void notifyObbStateChange(int status) {
            ObbState obbState = this.mObbState;
            if (obbState != null && obbState.token != null) {
                try {
                    this.mObbState.token.onObbResult(this.mObbState.rawPath, this.mObbState.nonce, status);
                } catch (RemoteException e) {
                    Slog.w(StorageManagerService.TAG, "StorageEventListener went away while calling onObbStateChanged");
                }
            }
        }
    }

    class MountObbAction extends ObbAction {
        private final int mCallingUid;
        private final String mKey;
        private ObbInfo mObbInfo;

        MountObbAction(ObbState obbState, String key, int callingUid, ObbInfo obbInfo) {
            super(obbState);
            this.mKey = key;
            this.mCallingUid = callingUid;
            this.mObbInfo = obbInfo;
        }

        @Override // com.android.server.StorageManagerService.ObbAction
        public void handleExecute() throws ObbException {
            boolean isMounted;
            String binderKey;
            StorageManagerService.this.warnOnNotMounted();
            if (StorageManagerService.this.isUidOwnerOfPackageOrSystem(this.mObbInfo.packageName, this.mCallingUid)) {
                synchronized (StorageManagerService.this.mObbMounts) {
                    isMounted = StorageManagerService.this.mObbPathToStateMap.containsKey(this.mObbState.rawPath);
                }
                if (!isMounted) {
                    if (this.mKey == null) {
                        binderKey = "";
                    } else {
                        try {
                            binderKey = new BigInteger(SecretKeyFactory.getInstance(BackupPasswordManager.PBKDF_CURRENT).generateSecret(new PBEKeySpec(this.mKey.toCharArray(), this.mObbInfo.salt, 1024, 128)).getEncoded()).toString(16);
                        } catch (GeneralSecurityException e) {
                            throw new ObbException(20, e);
                        }
                    }
                    try {
                        this.mObbState.volId = StorageManagerService.this.mVold.createObb(this.mObbState.canonicalPath, binderKey, this.mObbState.ownerGid);
                        StorageManagerService.this.mVold.mount(this.mObbState.volId, 0, -1);
                        synchronized (StorageManagerService.this.mObbMounts) {
                            StorageManagerService.this.addObbStateLocked(this.mObbState);
                        }
                        notifyObbStateChange(1);
                    } catch (Exception e2) {
                        throw new ObbException(21, e2);
                    }
                } else {
                    throw new ObbException(24, "Attempt to mount OBB which is already mounted: " + this.mObbInfo.filename);
                }
            } else {
                throw new ObbException(25, "Denied attempt to mount OBB " + this.mObbInfo.filename + " which is owned by " + this.mObbInfo.packageName);
            }
        }

        public String toString() {
            return "MountObbAction{" + this.mObbState + '}';
        }
    }

    class UnmountObbAction extends ObbAction {
        private final boolean mForceUnmount;

        UnmountObbAction(ObbState obbState, boolean force) {
            super(obbState);
            this.mForceUnmount = force;
        }

        @Override // com.android.server.StorageManagerService.ObbAction
        public void handleExecute() throws ObbException {
            ObbState existingState;
            StorageManagerService.this.warnOnNotMounted();
            synchronized (StorageManagerService.this.mObbMounts) {
                existingState = (ObbState) StorageManagerService.this.mObbPathToStateMap.get(this.mObbState.rawPath);
            }
            if (existingState == null) {
                throw new ObbException(23, "Missing existingState");
            } else if (existingState.ownerGid != this.mObbState.ownerGid) {
                notifyObbStateChange(new ObbException(25, "Permission denied to unmount OBB " + existingState.rawPath + " (owned by GID " + existingState.ownerGid + ")"));
            } else {
                try {
                    StorageManagerService.this.mVold.unmount(this.mObbState.volId);
                    StorageManagerService.this.mVold.destroyObb(this.mObbState.volId);
                    this.mObbState.volId = null;
                    synchronized (StorageManagerService.this.mObbMounts) {
                        StorageManagerService.this.removeObbStateLocked(existingState);
                    }
                    notifyObbStateChange(2);
                } catch (Exception e) {
                    throw new ObbException(22, e);
                }
            }
        }

        public String toString() {
            return "UnmountObbAction{" + this.mObbState + ",force=" + this.mForceUnmount + '}';
        }
    }

    /* access modifiers changed from: private */
    public void dispatchOnStatus(IVoldTaskListener listener, int status, PersistableBundle extras) {
        if (listener != null) {
            try {
                listener.onStatus(status, extras);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void dispatchOnFinished(IVoldTaskListener listener, int status, PersistableBundle extras) {
        if (listener != null) {
            try {
                listener.onFinished(status, extras);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public int getMountMode(int uid, String packageName) {
        int mode = getMountModeInternal(uid, packageName);
        if (LOCAL_LOGV) {
            Slog.v(TAG, "Resolved mode " + mode + " for " + packageName + SliceClientPermissions.SliceAuthority.DELIMITER + UserHandle.formatUid(uid));
        }
        return mode;
    }

    private int getMountModeInternal(int uid, String packageName) {
        boolean hasLegacy = false;
        try {
            if (Process.isIsolated(uid)) {
                return 0;
            }
            String[] packagesForUid = this.mIPackageManager.getPackagesForUid(uid);
            if (ArrayUtils.isEmpty(packagesForUid)) {
                return 0;
            }
            if (packageName == null) {
                packageName = packagesForUid[0];
            }
            if (this.mPmInternal.isInstantApp(packageName, UserHandle.getUserId(uid))) {
                return 0;
            }
            boolean hasRead = StorageManager.checkPermissionAndCheckOp(this.mContext, false, 0, uid, packageName, "android.permission.READ_EXTERNAL_STORAGE", 59);
            boolean hasWrite = StorageManager.checkPermissionAndCheckOp(this.mContext, false, 0, uid, packageName, "android.permission.WRITE_EXTERNAL_STORAGE", 60);
            if ((this.mIPackageManager.checkUidPermission("android.permission.WRITE_MEDIA_STORAGE", uid) == 0) && hasWrite) {
                return 6;
            }
            boolean hasInstall = this.mIPackageManager.checkUidPermission("android.permission.INSTALL_PACKAGES", uid) == 0;
            boolean hasInstallOp = false;
            int length = packagesForUid.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                if (this.mIAppOpsService.checkOperation(66, uid, packagesForUid[i]) == 0) {
                    hasInstallOp = true;
                    break;
                }
                i++;
            }
            if ((hasInstall || hasInstallOp) && hasWrite) {
                return 3;
            }
            if (this.mIAppOpsService.checkOperation(87, uid, packageName) == 0) {
                hasLegacy = true;
            }
            if (hasLegacy && hasWrite) {
                return 3;
            }
            if (!hasLegacy || !hasRead) {
                return 1;
            }
            return 2;
        } catch (RemoteException e) {
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public static class Callbacks extends Handler {
        private static final int MSG_DISK_DESTROYED = 6;
        private static final int MSG_DISK_SCANNED = 5;
        private static final int MSG_STORAGE_STATE_CHANGED = 1;
        private static final int MSG_VOLUME_FORGOTTEN = 4;
        private static final int MSG_VOLUME_RECORD_CHANGED = 3;
        private static final int MSG_VOLUME_STATE_CHANGED = 2;
        private final RemoteCallbackList<IStorageEventListener> mCallbacks = new RemoteCallbackList<>();

        public Callbacks(Looper looper) {
            super(looper);
        }

        public void register(IStorageEventListener callback) {
            this.mCallbacks.register(callback);
        }

        public void unregister(IStorageEventListener callback) {
            this.mCallbacks.unregister(callback);
        }

        public void handleMessage(Message msg) {
            SomeArgs args = (SomeArgs) msg.obj;
            int n = this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    invokeCallback(this.mCallbacks.getBroadcastItem(i), msg.what, args);
                } catch (RemoteException e) {
                }
            }
            this.mCallbacks.finishBroadcast();
            args.recycle();
        }

        private void invokeCallback(IStorageEventListener callback, int what, SomeArgs args) throws RemoteException {
            switch (what) {
                case 1:
                    callback.onStorageStateChanged((String) args.arg1, (String) args.arg2, (String) args.arg3);
                    return;
                case 2:
                    callback.onVolumeStateChanged((VolumeInfo) args.arg1, args.argi2, args.argi3);
                    return;
                case 3:
                    callback.onVolumeRecordChanged((VolumeRecord) args.arg1);
                    return;
                case 4:
                    callback.onVolumeForgotten((String) args.arg1);
                    return;
                case 5:
                    callback.onDiskScanned((DiskInfo) args.arg1, args.argi2);
                    return;
                case 6:
                    callback.onDiskDestroyed((DiskInfo) args.arg1);
                    return;
                default:
                    return;
            }
        }

        /* access modifiers changed from: private */
        public void notifyStorageStateChanged(String path, String oldState, String newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = path;
            args.arg2 = oldState;
            args.arg3 = newState;
            obtainMessage(1, args).sendToTarget();
        }

        /* access modifiers changed from: private */
        public void notifyVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = vol.clone();
            args.argi2 = oldState;
            args.argi3 = newState;
            obtainMessage(2, args).sendToTarget();
        }

        /* access modifiers changed from: private */
        public void notifyVolumeRecordChanged(VolumeRecord rec) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = rec.clone();
            obtainMessage(3, args).sendToTarget();
        }

        /* access modifiers changed from: private */
        public void notifyVolumeForgotten(String fsUuid) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = fsUuid;
            obtainMessage(4, args).sendToTarget();
        }

        /* access modifiers changed from: private */
        public void notifyDiskScanned(DiskInfo disk, int volumeCount) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk.clone();
            args.argi2 = volumeCount;
            obtainMessage(5, args).sendToTarget();
        }

        /* access modifiers changed from: private */
        public void notifyDiskDestroyed(DiskInfo disk) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk.clone();
            obtainMessage(6, args).sendToTarget();
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, writer)) {
            IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ", 160);
            synchronized (this.mLock) {
                pw.println("Disks:");
                pw.increaseIndent();
                for (int i = 0; i < this.mDisks.size(); i++) {
                    this.mDisks.valueAt(i).dump(pw);
                }
                pw.decreaseIndent();
                pw.println();
                pw.println("Volumes:");
                pw.increaseIndent();
                for (int i2 = 0; i2 < this.mVolumes.size(); i2++) {
                    VolumeInfo vol = this.mVolumes.valueAt(i2);
                    if (!"private".equals(vol.id)) {
                        vol.dump(pw);
                    }
                }
                pw.decreaseIndent();
                pw.println();
                pw.println("Records:");
                pw.increaseIndent();
                for (int i3 = 0; i3 < this.mRecords.size(); i3++) {
                    this.mRecords.valueAt(i3).dump(pw);
                }
                pw.decreaseIndent();
                pw.println();
                pw.println("Primary storage UUID: " + this.mPrimaryStorageUuid);
                pw.println();
                Pair<String, Long> pair = StorageManager.getPrimaryStoragePathAndSize();
                if (pair == null) {
                    pw.println("Internal storage total size: N/A");
                } else {
                    pw.print("Internal storage (");
                    pw.print((String) pair.first);
                    pw.print(") total size: ");
                    pw.print(pair.second);
                    pw.print(" (");
                    pw.print(DataUnit.MEBIBYTES.toBytes(((Long) pair.second).longValue()));
                    pw.println(" MiB)");
                }
                pw.println();
                pw.println("Local unlocked users: " + Arrays.toString(this.mLocalUnlockedUsers));
                pw.println("System unlocked users: " + Arrays.toString(this.mSystemUnlockedUsers));
                ContentResolver cr = this.mContext.getContentResolver();
                pw.println();
                pw.println("Isolated storage, local feature flag: " + Settings.Global.getInt(cr, "isolated_storage_local", 0));
                pw.println("Isolated storage, remote feature flag: " + Settings.Global.getInt(cr, "isolated_storage_remote", 0));
                pw.println("Isolated storage, resolved: " + StorageManager.hasIsolatedStorage());
            }
            synchronized (this.mObbMounts) {
                pw.println();
                pw.println("mObbMounts:");
                pw.increaseIndent();
                for (Map.Entry<IBinder, List<ObbState>> e : this.mObbMounts.entrySet()) {
                    pw.println(e.getKey() + ":");
                    pw.increaseIndent();
                    for (ObbState obbState : e.getValue()) {
                        pw.println(obbState);
                    }
                    pw.decreaseIndent();
                }
                pw.decreaseIndent();
                pw.println();
                pw.println("mObbPathToStateMap:");
                pw.increaseIndent();
                for (Map.Entry<String, ObbState> e2 : this.mObbPathToStateMap.entrySet()) {
                    pw.print(e2.getKey());
                    pw.print(" -> ");
                    pw.println(e2.getValue());
                }
                pw.decreaseIndent();
            }
            pw.println();
            pw.print("Last maintenance: ");
            pw.println(TimeUtils.formatForLogging(this.mLastMaintenance));
        }
    }

    @Override // com.android.server.Watchdog.Monitor
    public void monitor() {
        try {
            this.mVold.monitor();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    private final class StorageManagerInternalImpl extends StorageManagerInternal {
        private final CopyOnWriteArrayList<StorageManagerInternal.ExternalStorageMountPolicy> mPolicies;
        @GuardedBy({"mResetListeners"})
        private final List<StorageManagerInternal.ResetListener> mResetListeners;

        private StorageManagerInternalImpl() {
            this.mPolicies = new CopyOnWriteArrayList<>();
            this.mResetListeners = new ArrayList();
        }

        public void addExternalStoragePolicy(StorageManagerInternal.ExternalStorageMountPolicy policy) {
            this.mPolicies.add(policy);
        }

        public void onExternalStoragePolicyChanged(int uid, String packageName) {
            StorageManagerService.this.remountUidExternalStorage(uid, getExternalStorageMountMode(uid, packageName));
        }

        public int getExternalStorageMountMode(int uid, String packageName) {
            if (StorageManagerService.ENABLE_ISOLATED_STORAGE) {
                return StorageManagerService.this.getMountMode(uid, packageName);
            }
            if (packageName == null) {
                try {
                    packageName = StorageManagerService.this.mIPackageManager.getPackagesForUid(uid)[0];
                } catch (RemoteException e) {
                }
            }
            int mountMode = Integer.MAX_VALUE;
            Iterator<StorageManagerInternal.ExternalStorageMountPolicy> it = this.mPolicies.iterator();
            while (it.hasNext()) {
                int policyMode = it.next().getMountMode(uid, packageName);
                if (policyMode == 0) {
                    return 0;
                }
                mountMode = Math.min(mountMode, policyMode);
            }
            if (mountMode == Integer.MAX_VALUE) {
                return 0;
            }
            return mountMode;
        }

        public void addResetListener(StorageManagerInternal.ResetListener listener) {
            synchronized (this.mResetListeners) {
                this.mResetListeners.add(listener);
            }
        }

        public void onReset(IVold vold) {
            synchronized (this.mResetListeners) {
                for (StorageManagerInternal.ResetListener listener : this.mResetListeners) {
                    listener.onReset(vold);
                }
            }
        }

        public boolean hasExternalStorage(int uid, String packageName) {
            if (uid == 1000) {
                return true;
            }
            if (!StorageManagerService.ENABLE_ISOLATED_STORAGE) {
                Iterator<StorageManagerInternal.ExternalStorageMountPolicy> it = this.mPolicies.iterator();
                while (it.hasNext()) {
                    if (!it.next().hasExternalStorage(uid, packageName)) {
                        return false;
                    }
                }
                return true;
            } else if (StorageManagerService.this.getMountMode(uid, packageName) != 0) {
                return true;
            } else {
                return false;
            }
        }

        public void onAppOpsChanged(int code, int uid, String packageName, int mode) {
            if (mode != 0) {
                return;
            }
            if (code == 59 || code == 60 || code == 66) {
                long token = Binder.clearCallingIdentity();
                try {
                    if (((UserManagerInternal) LocalServices.getService(UserManagerInternal.class)).isUserInitialized(UserHandle.getUserId(uid))) {
                        onExternalStoragePolicyChanged(uid, packageName);
                    }
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }
    }

    private void initOppoStorageFeature() {
        this.mOppoStorageFeature = OppoFeatureCache.getOrCreate(IOppoStorageManagerFeature.DEFAULT, new Object[]{this.mContext});
        this.mOppoStorageFeature.setOppoStorageManagerCallback(this.mStorageCallback);
        this.mOppoStorageFeature.setStorageManagerHandler(this.mHandler);
    }
}
