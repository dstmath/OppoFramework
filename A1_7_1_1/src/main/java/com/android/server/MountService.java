package com.android.server;

import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.ObbInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.Environment.UserEnvironment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.OnCloseListener;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.DiskInfo;
import android.os.storage.IMountService.Stub;
import android.os.storage.IMountServiceListener;
import android.os.storage.IMountShutdownObserver;
import android.os.storage.IObbActionListener;
import android.os.storage.MountServiceInternal;
import android.os.storage.MountServiceInternal.ExternalStorageMountPolicy;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.Xml;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IMediaContainerService;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.HexDump;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.NativeDaemonConnector.Command;
import com.android.server.NativeDaemonConnector.SensitiveArg;
import com.android.server.Watchdog.Monitor;
import com.android.server.am.OppoProcessManager;
import com.android.server.job.controllers.JobStatus;
import com.android.server.location.LocationFudger;
import com.android.server.oppo.IElsaManager;
import com.android.server.pm.PackageManagerService;
import com.android.server.secrecy.policy.DecryptTool;
import com.android.server.usage.UnixCalendar;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import com.google.android.collect.Lists;
import com.mediatek.storage.StorageManagerEx;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
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
class MountService extends Stub implements INativeDaemonConnectorCallbacks, Monitor {
    public static final String ACTION_ENCRYPTION_TYPE_CHANGED = "com.mediatek.intent.extra.ACTION_ENCRYPTION_TYPE_CHANGED";
    private static final String ATTR_CREATED_MILLIS = "createdMillis";
    private static final String ATTR_FORCE_ADOPTABLE = "forceAdoptable";
    private static final String ATTR_FS_UUID = "fsUuid";
    private static final String ATTR_LAST_BENCH_MILLIS = "lastBenchMillis";
    private static final String ATTR_LAST_TRIM_MILLIS = "lastTrimMillis";
    private static final String ATTR_NICKNAME = "nickname";
    private static final String ATTR_PART_GUID = "partGuid";
    private static final String ATTR_PRIMARY_STORAGE_UUID = "primaryStorageUuid";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_USER_FLAGS = "userFlags";
    private static final String ATTR_VERSION = "version";
    private static final String BOOT_IPO = "android.intent.action.ACTION_BOOT_IPO";
    private static final String CRYPTD_TAG = "CryptdConnector";
    private static final int CRYPTO_ALGORITHM_KEY_SIZE = 128;
    public static final String[] CRYPTO_TYPES = null;
    private static final boolean DEBUG_EVENTS = true;
    private static final boolean DEBUG_OBB = true;
    static final ComponentName DEFAULT_CONTAINER_COMPONENT = null;
    private static final Object FORMAT_LOCK = null;
    private static final int H_DAEMON_CONNECTED = 2;
    private static final int H_FSTRIM = 4;
    private static final int H_INTERNAL_BROADCAST = 7;
    private static final int H_PARTITION_FORGET = 9;
    private static final int H_RESET = 10;
    private static final int H_SHUTDOWN = 3;
    private static final int H_SYSTEM_READY = 1;
    private static final int H_VOLUME_BROADCAST = 6;
    private static final int H_VOLUME_BROADCAST_STORAGE_RO = 61;
    private static final int H_VOLUME_MOUNT = 5;
    private static final int H_VOLUME_UNMOUNT = 8;
    private static final String INSERT_OTG = "insert_otg";
    private static final String LAST_FSTRIM_FILE = "last-fstrim";
    private static final boolean LOG_ENABLE = false;
    private static final int MAX_CONTAINERS = 250;
    private static final int MOVE_STATUS_COPY_FINISHED = 82;
    private static final int OBB_FLUSH_MOUNT_STATE = 5;
    private static final int OBB_MCS_BOUND = 2;
    private static final int OBB_MCS_RECONNECT = 4;
    private static final int OBB_MCS_UNBIND = 3;
    private static final int OBB_RUN_ACTION = 1;
    private static final String OMADM_SD_FORMAT = "com.mediatek.dm.LAWMO_WIPE";
    private static final Object OMADM_SYNC_LOCK = null;
    private static final String OMADM_USB_DISABLE = "com.mediatek.dm.LAWMO_LOCK";
    private static final String OMADM_USB_ENABLE = "com.mediatek.dm.LAWMO_UNLOCK";
    private static final int PBKDF2_HASH_ROUNDS = 1024;
    private static final String PRIVACY_PROTECTION_LOCK = "com.mediatek.ppl.NOTIFY_LOCK";
    private static final String PRIVACY_PROTECTION_UNLOCK = "com.mediatek.ppl.NOTIFY_UNLOCK";
    private static final String PRIVACY_PROTECTION_WIPE = "com.mediatek.ppl.NOTIFY_MOUNT_SERVICE_WIPE";
    private static final String PRIVACY_PROTECTION_WIPE_DONE = "com.mediatek.ppl.MOUNT_SERVICE_WIPE_RESPONSE";
    private static final String PROP_DM_APP = "ro.mtk_dm_app";
    private static final String PROP_VOLD_DECRYPT = "vold.decrypt";
    private static final String TAG = "MountService";
    private static final String TAG_STORAGE_BENCHMARK = "storage_benchmark";
    private static final String TAG_STORAGE_TRIM = "storage_trim";
    private static final String TAG_VOLUME = "volume";
    private static final String TAG_VOLUMES = "volumes";
    private static final Object TURNONUSB_SYNC_LOCK = null;
    private static final int VERSION_ADD_PRIMARY = 2;
    private static final int VERSION_FIX_PRIMARY = 3;
    private static final int VERSION_INIT = 1;
    private static final String VOLD_TAG = "VoldConnector";
    private static final boolean WATCHDOG_ENABLE = false;
    private static boolean isBootingPhase;
    private static boolean isIPOBooting;
    private static boolean isShuttingDown;
    private static int mMyOppoPid;
    private static boolean mPanicDebug;
    static MountService sSelf;
    private boolean isDiskInsert;
    private final HashSet<String> mAsecMountSet;
    private final CountDownLatch mAsecsScanned;
    private volatile boolean mBootCompleted;
    private final BroadcastReceiver mBootIPOReceiver;
    private final Callbacks mCallbacks;
    private final CountDownLatch mConnectedSignal;
    private final NativeDaemonConnector mConnector;
    private final Thread mConnectorThread;
    private IMediaContainerService mContainerService;
    private final Context mContext;
    private final NativeDaemonConnector mCryptConnector;
    private final Thread mCryptConnectorThread;
    private volatile int mCurrentUserId;
    private final BroadcastReceiver mDMReceiver;
    private volatile boolean mDaemonConnected;
    private final DefaultContainerConnection mDefContainerConn;
    @GuardedBy("mLock")
    private ArrayMap<String, CountDownLatch> mDiskScanLatches;
    @GuardedBy("mLock")
    private ArrayMap<String, DiskInfo> mDisks;
    @GuardedBy("mLock")
    private boolean mForceAdoptable;
    private final Handler mHandler;
    private boolean mIsStartUser;
    private boolean mIsTurnOnOffUsb;
    private boolean mIsUsbConnected;
    private long mLastMaintenance;
    private final File mLastMaintenanceFile;
    @GuardedBy("mLock")
    private int[] mLocalUnlockedUsers;
    private final Object mLock;
    private final LockPatternUtils mLockPatternUtils;
    private final MountServiceInternalImpl mMountServiceInternal;
    @GuardedBy("mLock")
    private IPackageMoveObserver mMoveCallback;
    @GuardedBy("mLock")
    private String mMoveTargetUuid;
    private final ObbActionHandler mObbActionHandler;
    private final Map<IBinder, List<ObbState>> mObbMounts;
    private final Map<String, ObbState> mObbPathToStateMap;
    private int mOldEncryptionType;
    private PackageManagerService mPms;
    @GuardedBy("mLock")
    private String mPrimaryStorageUuid;
    private final BroadcastReceiver mPrivacyProtectionReceiver;
    @GuardedBy("mLock")
    private ArrayMap<String, VolumeRecord> mRecords;
    private boolean mSendUmsConnectedOnBoot;
    private final AtomicFile mSettingsFile;
    private volatile boolean mSystemReady;
    @GuardedBy("mLock")
    private int[] mSystemUnlockedUsers;
    private int mUMSCount;
    private boolean mUmsEnabling;
    private final Object mUnmountLock;
    @GuardedBy("mUnmountLock")
    private CountDownLatch mUnmountSignal;
    private final BroadcastReceiver mUsbReceiver;
    private BroadcastReceiver mUserReceiver;
    @GuardedBy("mLock")
    private final ArrayMap<String, VolumeInfo> mVolumes;

    private static class Callbacks extends Handler {
        private static final int MSG_DISK_DESTROYED = 6;
        private static final int MSG_DISK_SCANNED = 5;
        private static final int MSG_STORAGE_STATE_CHANGED = 1;
        private static final int MSG_UMS_CONNECTION_CHANGED = 7;
        private static final int MSG_VOLUME_FORGOTTEN = 4;
        private static final int MSG_VOLUME_RECORD_CHANGED = 3;
        private static final int MSG_VOLUME_STATE_CHANGED = 2;
        private final RemoteCallbackList<IMountServiceListener> mCallbacks = new RemoteCallbackList();

        public Callbacks(Looper looper) {
            super(looper);
        }

        public void register(IMountServiceListener callback) {
            this.mCallbacks.register(callback);
        }

        public void unregister(IMountServiceListener callback) {
            this.mCallbacks.unregister(callback);
        }

        public void handleMessage(Message msg) {
            SomeArgs args = msg.obj;
            int n = this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IMountServiceListener callback = (IMountServiceListener) this.mCallbacks.getBroadcastItem(i);
                int pid = this.mCallbacks.getBroadcastItemPid(i);
                if (MountService.mMyOppoPid == -1) {
                    MountService.mMyOppoPid = Process.myPid();
                }
                if (MountService.mMyOppoPid != pid) {
                    Process.sendSignal(pid, 18);
                    OppoBPMHelper.recordResumeLog(pid, OppoProcessManager.RESUME_REASON_MOUNT_STR);
                }
                try {
                    invokeCallback(callback, msg.what, args);
                } catch (RemoteException e) {
                }
            }
            this.mCallbacks.finishBroadcast();
            args.recycle();
        }

        private void invokeCallback(IMountServiceListener callback, int what, SomeArgs args) throws RemoteException {
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
                case 7:
                    callback.onUsbMassStorageConnectionChanged(((Boolean) args.arg1).booleanValue());
                    return;
                default:
                    return;
            }
        }

        private void notifyStorageStateChanged(String path, String oldState, String newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = path;
            args.arg2 = oldState;
            args.arg3 = newState;
            obtainMessage(1, args).sendToTarget();
        }

        private void notifyVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = vol.clone();
            args.argi2 = oldState;
            args.argi3 = newState;
            obtainMessage(2, args).sendToTarget();
        }

        private void notifyVolumeRecordChanged(VolumeRecord rec) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = rec.clone();
            obtainMessage(3, args).sendToTarget();
        }

        private void notifyVolumeForgotten(String fsUuid) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = fsUuid;
            obtainMessage(4, args).sendToTarget();
        }

        private void notifyDiskScanned(DiskInfo disk, int volumeCount) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk.clone();
            args.argi2 = volumeCount;
            obtainMessage(5, args).sendToTarget();
        }

        private void notifyDiskDestroyed(DiskInfo disk) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk.clone();
            obtainMessage(6, args).sendToTarget();
        }

        private void onUsbMassStorageConnectionChanged(boolean connected) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Boolean.valueOf(connected);
            obtainMessage(7, args).sendToTarget();
        }
    }

    class DefaultContainerConnection implements ServiceConnection {
        DefaultContainerConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Slog.i(MountService.TAG, "onServiceConnected");
            MountService.this.mObbActionHandler.sendMessage(MountService.this.mObbActionHandler.obtainMessage(2, IMediaContainerService.Stub.asInterface(service)));
        }

        public void onServiceDisconnected(ComponentName name) {
            Slog.i(MountService.TAG, "onServiceDisconnected");
        }
    }

    public static class Lifecycle extends SystemService {
        private MountService mMountService;
        private String oldDefaultPath = IElsaManager.EMPTY_PACKAGE;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            if (MountService.mPanicDebug) {
                Slog.d(MountService.TAG, "MountService onStart");
            }
            MountService.isBootingPhase = true;
            this.mMountService = new MountService(getContext());
            publishBinderService(OppoProcessManager.RESUME_REASON_MOUNT_STR, this.mMountService);
            this.mMountService.start();
            this.oldDefaultPath = MountService.sSelf.getDefaultPath();
            Slog.d(MountService.TAG, "get Default path onStart default path=" + this.oldDefaultPath);
        }

        public void onBootPhase(int phase) {
            if (MountService.mPanicDebug) {
                Slog.d(MountService.TAG, "MountService onBootPhase");
            }
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
                this.mMountService.systemReady();
            } else if (phase == 1000) {
                this.mMountService.bootCompleted();
            }
            if (phase == 1000) {
                if (MountService.mPanicDebug) {
                    Slog.d(MountService.TAG, "MountService onBootPhase: PHASE_BOOT_COMPLETED");
                }
                MountService.isBootingPhase = false;
                if (!this.oldDefaultPath.contains("emulated") && !IElsaManager.EMPTY_PACKAGE.equals(this.oldDefaultPath)) {
                    if (MountService.mPanicDebug) {
                        Slog.d(MountService.TAG, "set defaut path to " + this.oldDefaultPath);
                    }
                    MountService.sSelf.setDefaultPath(this.oldDefaultPath);
                    MountService.sSelf.updateDefaultPathIfNeed();
                }
            }
        }

        public void onSwitchUser(int userHandle) {
            this.mMountService.mCurrentUserId = userHandle;
        }

        public void onUnlockUser(int userHandle) {
            if (MountService.mPanicDebug) {
                Slog.d(MountService.TAG, "MountService onUnlockUser, userHandle=" + userHandle);
            }
            this.mMountService.onUnlockUser(userHandle);
        }

        public void onCleanupUser(int userHandle) {
            if (MountService.mPanicDebug) {
                Slog.d(MountService.TAG, "MountService onCleanupUser, userHandle=" + userHandle);
            }
            this.mMountService.onCleanupUser(userHandle);
        }
    }

    abstract class ObbAction {
        private static final int MAX_RETRIES = 3;
        ObbState mObbState;
        private int mRetries;

        abstract void handleError();

        abstract void handleExecute() throws RemoteException, IOException;

        ObbAction(ObbState obbState) {
            this.mObbState = obbState;
        }

        public void execute(ObbActionHandler handler) {
            try {
                Slog.i(MountService.TAG, "Starting to execute action: " + toString());
                this.mRetries++;
                if (this.mRetries > 3) {
                    Slog.w(MountService.TAG, "Failed to invoke remote methods on default container service. Giving up");
                    MountService.this.mObbActionHandler.sendEmptyMessage(3);
                    handleError();
                    return;
                }
                handleExecute();
                Slog.i(MountService.TAG, "Posting install MCS_UNBIND");
                MountService.this.mObbActionHandler.sendEmptyMessage(3);
            } catch (RemoteException e) {
                Slog.i(MountService.TAG, "Posting install MCS_RECONNECT");
                MountService.this.mObbActionHandler.sendEmptyMessage(4);
            } catch (Exception e2) {
                Slog.d(MountService.TAG, "Error handling OBB action", e2);
                handleError();
                MountService.this.mObbActionHandler.sendEmptyMessage(3);
            }
        }

        protected ObbInfo getObbInfo() throws IOException {
            ObbInfo obbInfo;
            try {
                obbInfo = MountService.this.mContainerService.getObbInfo(this.mObbState.canonicalPath);
            } catch (RemoteException e) {
                Slog.d(MountService.TAG, "Couldn't call DefaultContainerService to fetch OBB info for " + this.mObbState.canonicalPath);
                obbInfo = null;
            }
            if (obbInfo != null) {
                return obbInfo;
            }
            throw new IOException("Couldn't read OBB file: " + this.mObbState.canonicalPath);
        }

        protected void sendNewStatusOrIgnore(int status) {
            if (this.mObbState != null && this.mObbState.token != null) {
                try {
                    this.mObbState.token.onObbResult(this.mObbState.rawPath, this.mObbState.nonce, status);
                } catch (RemoteException e) {
                    Slog.w(MountService.TAG, "MountServiceListener went away while calling onObbStateChanged");
                }
            }
        }
    }

    class MountObbAction extends ObbAction {
        private final int mCallingUid;
        private final String mKey;

        MountObbAction(ObbState obbState, String key, int callingUid) {
            super(obbState);
            this.mKey = key;
            this.mCallingUid = callingUid;
        }

        public void handleExecute() throws IOException, RemoteException {
            MountService.this.waitForReady();
            MountService.this.warnOnNotMounted();
            ObbInfo obbInfo = getObbInfo();
            if (MountService.this.isUidOwnerOfPackageOrSystem(obbInfo.packageName, this.mCallingUid)) {
                boolean isMounted;
                synchronized (MountService.this.mObbMounts) {
                    isMounted = MountService.this.mObbPathToStateMap.containsKey(this.mObbState.rawPath);
                }
                if (isMounted) {
                    Slog.w(MountService.TAG, "Attempt to mount OBB which is already mounted: " + obbInfo.filename);
                    sendNewStatusOrIgnore(24);
                    return;
                }
                String hashedKey;
                if (this.mKey == null) {
                    hashedKey = "none";
                } else {
                    try {
                        hashedKey = new BigInteger(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(new PBEKeySpec(this.mKey.toCharArray(), obbInfo.salt, 1024, 128)).getEncoded()).toString(16);
                    } catch (NoSuchAlgorithmException e) {
                        Slog.e(MountService.TAG, "Could not load PBKDF2 algorithm", e);
                        sendNewStatusOrIgnore(20);
                        return;
                    } catch (InvalidKeySpecException e2) {
                        Slog.e(MountService.TAG, "Invalid key spec when loading PBKDF2 algorithm", e2);
                        sendNewStatusOrIgnore(20);
                        return;
                    }
                }
                int rc = 0;
                try {
                    Object[] objArr = new Object[4];
                    objArr[0] = OppoProcessManager.RESUME_REASON_MOUNT_STR;
                    objArr[1] = this.mObbState.canonicalPath;
                    objArr[2] = new SensitiveArg(hashedKey);
                    objArr[3] = Integer.valueOf(this.mObbState.ownerGid);
                    MountService.this.mConnector.execute("obb", objArr);
                } catch (NativeDaemonConnectorException e3) {
                    if (e3.getCode() != VoldResponseCode.OpFailedStorageBusy) {
                        rc = -1;
                    }
                }
                if (rc == 0) {
                    Slog.d(MountService.TAG, "Successfully mounted OBB " + this.mObbState.canonicalPath);
                    synchronized (MountService.this.mObbMounts) {
                        MountService.this.addObbStateLocked(this.mObbState);
                    }
                    sendNewStatusOrIgnore(1);
                } else {
                    Slog.e(MountService.TAG, "Couldn't mount OBB file: " + rc);
                    sendNewStatusOrIgnore(21);
                }
                return;
            }
            Slog.w(MountService.TAG, "Denied attempt to mount OBB " + obbInfo.filename + " which is owned by " + obbInfo.packageName);
            sendNewStatusOrIgnore(25);
        }

        public void handleError() {
            sendNewStatusOrIgnore(20);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("MountObbAction{");
            sb.append(this.mObbState);
            sb.append('}');
            return sb.toString();
        }
    }

    class MountServiceHandler extends Handler {
        public MountServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            VolumeInfo vol;
            StorageVolume userVol;
            Intent intent;
            NativeDaemonConnector -get3;
            String str;
            String action;
            switch (msg.what) {
                case 1:
                    if (MountService.mPanicDebug) {
                        Slog.i(MountService.TAG, "handleMessage:H_SYSTEM_READY");
                    }
                    MountService.this.handleSystemReady();
                    break;
                case 2:
                    if (MountService.mPanicDebug) {
                        Slog.i(MountService.TAG, "H_DAEMON_CONNECTED");
                    }
                    MountService.this.handleDaemonConnected();
                    break;
                case 3:
                    Slog.i(MountService.TAG, "H_SHUTDOWN!");
                    String actionShutdown = "android.intent.action.MEDIA_UNMOUNTED";
                    synchronized (MountService.this.mVolumes) {
                        for (int i = 0; i < MountService.this.mVolumes.size(); i++) {
                            vol = (VolumeInfo) MountService.this.mVolumes.valueAt(i);
                            Slog.i(MountService.TAG, "H_SHUTDOWN000000, vol=" + vol);
                            for (int userId : MountService.this.mSystemUnlockedUsers) {
                                if (vol.isVisibleForRead(userId) && vol.isMountedReadable()) {
                                    userVol = vol.buildStorageVolume(MountService.this.mContext, userId, false);
                                    intent = new Intent("android.intent.action.MEDIA_UNMOUNTED", Uri.fromFile(userVol.getPathFile()));
                                    intent.putExtra("android.os.storage.extra.STORAGE_VOLUME", userVol);
                                    intent.addFlags(67108864);
                                    Slog.i(MountService.TAG, "H_SHUTDOWN, intent=" + intent + ", userVol=" + userVol);
                                    MountService.this.mContext.sendBroadcastAsUser(intent, userVol.getOwner());
                                }
                            }
                        }
                    }
                    MountService.isShuttingDown = true;
                    IMountShutdownObserver obs = msg.obj;
                    boolean success = false;
                    try {
                        -get3 = MountService.this.mConnector;
                        str = MountService.TAG_VOLUME;
                        Object[] objArr = new Object[1];
                        objArr[0] = "shutdown";
                        success = -get3.execute(str, objArr).isClassOk();
                    } catch (NativeDaemonConnectorException e) {
                    }
                    if (obs != null) {
                        try {
                            obs.onShutDownComplete(success ? 0 : -1);
                        } catch (RemoteException e2) {
                        }
                    }
                    if (MountService.mPanicDebug) {
                        Slog.i(MountService.TAG, "finsh shut down");
                    }
                    MountService.isShuttingDown = false;
                    break;
                case 4:
                    if (MountService.mPanicDebug) {
                        Slog.i(MountService.TAG, "H_FSTRIM");
                    }
                    if (!MountService.this.isReady()) {
                        Slog.i(MountService.TAG, "fstrim requested, but no daemon connection yet; trying again");
                        sendMessageDelayed(obtainMessage(4, msg.obj), 1000);
                        break;
                    }
                    Slog.i(MountService.TAG, "Running fstrim idle maintenance");
                    try {
                        MountService.this.mLastMaintenance = System.currentTimeMillis();
                        MountService.this.mLastMaintenanceFile.setLastModified(MountService.this.mLastMaintenance);
                    } catch (Exception e3) {
                        Slog.e(MountService.TAG, "Unable to record last fstrim!");
                    }
                    boolean shouldBenchmark = MountService.this.shouldBenchmark();
                    try {
                        NativeDaemonConnector -get32 = MountService.this.mConnector;
                        String str2 = "fstrim";
                        Object[] objArr2 = new Object[1];
                        objArr2[0] = shouldBenchmark ? "dotrimbench" : "dotrim";
                        -get32.execute(str2, objArr2);
                    } catch (NativeDaemonConnectorException e4) {
                        Slog.e(MountService.TAG, "Failed to run fstrim!");
                    }
                    Runnable callback = msg.obj;
                    if (callback != null) {
                        callback.run();
                        break;
                    }
                    break;
                case 5:
                    if (MountService.mPanicDebug) {
                        Slog.i(MountService.TAG, "H_VOLUME_MOUNT");
                    }
                    vol = (VolumeInfo) msg.obj;
                    if (vol.type != 0 || MountService.this.mIsStartUser) {
                        if (!MountService.this.isMountDisallowed(vol)) {
                            int rc = 0;
                            try {
                                -get3 = MountService.this.mConnector;
                                str = MountService.TAG_VOLUME;
                                String[] strArr = new Object[4];
                                strArr[0] = OppoProcessManager.RESUME_REASON_MOUNT_STR;
                                strArr[1] = vol.id;
                                strArr[2] = Integer.valueOf(vol.mountFlags);
                                strArr[3] = Integer.valueOf(vol.mountUserId);
                                -get3.execute(3000, str, strArr);
                            } catch (NativeDaemonConnectorException ignored) {
                                rc = ignored.getCode();
                                Slog.w(MountService.TAG, "mount volume fail, ignored=" + ignored);
                            }
                            if (rc != 0) {
                                MountService.this.isDiskInsert = false;
                                if (MountService.mPanicDebug) {
                                    Slog.w(MountService.TAG, "mount volume fail, vol=" + vol + ", return code=" + rc);
                                    break;
                                }
                            }
                            synchronized (MountService.this.mLock) {
                                VolumeInfo curVol = (VolumeInfo) MountService.this.mVolumes.get(vol.getId());
                            }
                        } else {
                            Slog.i(MountService.TAG, "Ignoring mount " + vol.getId() + " due to policy");
                            break;
                        }
                    }
                    Slog.i(MountService.TAG, vol.getId() + " sendMessageDelayed 1s");
                    Message delaymsg = MountService.this.mHandler.obtainMessage(5);
                    delaymsg.obj = vol;
                    MountService.this.mHandler.sendMessageDelayed(delaymsg, 1000);
                    return;
                    break;
                case 6:
                    if (MountService.mPanicDebug) {
                        Slog.i(MountService.TAG, "H_VOLUME_BROADCAST");
                    }
                    userVol = (StorageVolume) msg.obj;
                    String envState = userVol.getState();
                    Slog.d(MountService.TAG, "Volume " + userVol.getId() + " broadcasting " + envState + " to " + userVol.getOwner());
                    action = VolumeInfo.getBroadcastForEnvironment(envState);
                    if (action != null) {
                        intent = new Intent(action, Uri.fromFile(userVol.getPathFile()));
                        intent.putExtra("android.os.storage.extra.STORAGE_VOLUME", userVol);
                        intent.addFlags(67108864);
                        if (MountService.mPanicDebug) {
                            Slog.i(MountService.TAG, "sendBroadcastAsUser, intent=" + intent + ", userVol=" + userVol);
                        }
                        MountService.this.mContext.sendBroadcastAsUser(intent, userVol.getOwner());
                        break;
                    }
                    break;
                case 7:
                    MountService.this.mContext.sendBroadcastAsUser((Intent) msg.obj, UserHandle.ALL, "android.permission.WRITE_MEDIA_STORAGE");
                    break;
                case 8:
                    MountService.this.unmount(((VolumeInfo) msg.obj).getId());
                    break;
                case 9:
                    MountService.this.forgetPartition(msg.obj);
                    break;
                case 10:
                    MountService.this.resetIfReadyAndConnected();
                    break;
                case MountService.H_VOLUME_BROADCAST_STORAGE_RO /*61*/:
                    userVol = (StorageVolume) msg.obj;
                    Slog.d(MountService.TAG, "Volume " + userVol.getId() + " broadcasting " + "mounted_ro" + " to " + userVol.getOwner());
                    action = "android.intent.action.MEDIA_MOUNTED_RO";
                    intent = new Intent("android.intent.action.MEDIA_MOUNTED_RO", Uri.fromFile(userVol.getPathFile()));
                    intent.putExtra("android.os.storage.extra.STORAGE_VOLUME", userVol);
                    intent.addFlags(67108864);
                    MountService.this.mContext.sendBroadcastAsUser(intent, userVol.getOwner());
                    break;
            }
        }
    }

    private final class MountServiceInternalImpl extends MountServiceInternal {
        private final CopyOnWriteArrayList<ExternalStorageMountPolicy> mPolicies;

        /* synthetic */ MountServiceInternalImpl(MountService this$0, MountServiceInternalImpl mountServiceInternalImpl) {
            this();
        }

        private MountServiceInternalImpl() {
            this.mPolicies = new CopyOnWriteArrayList();
        }

        public void addExternalStoragePolicy(ExternalStorageMountPolicy policy) {
            this.mPolicies.add(policy);
        }

        public void onExternalStoragePolicyChanged(int uid, String packageName) {
            MountService.this.remountUidExternalStorage(uid, getExternalStorageMountMode(uid, packageName));
        }

        public int getExternalStorageMountMode(int uid, String packageName) {
            int mountMode = Integer.MAX_VALUE;
            for (ExternalStorageMountPolicy policy : this.mPolicies) {
                int policyMode = policy.getMountMode(uid, packageName);
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

        public boolean hasExternalStorage(int uid, String packageName) {
            if (uid == 1000) {
                return true;
            }
            for (ExternalStorageMountPolicy policy : this.mPolicies) {
                if (!policy.hasExternalStorage(uid, packageName)) {
                    return false;
                }
            }
            return true;
        }
    }

    private class ObbActionHandler extends Handler {
        private final List<ObbAction> mActions = new LinkedList();
        private boolean mBound = false;

        ObbActionHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            ObbAction action;
            switch (msg.what) {
                case 1:
                    action = msg.obj;
                    Slog.i(MountService.TAG, "OBB_RUN_ACTION: " + action.toString());
                    if (this.mBound || connectToService()) {
                        this.mActions.add(action);
                        break;
                    }
                    Slog.e(MountService.TAG, "Failed to bind to media container service");
                    action.handleError();
                    return;
                case 2:
                    Slog.i(MountService.TAG, "OBB_MCS_BOUND");
                    if (msg.obj != null) {
                        MountService.this.mContainerService = (IMediaContainerService) msg.obj;
                    }
                    if (MountService.this.mContainerService != null) {
                        if (this.mActions.size() <= 0) {
                            Slog.w(MountService.TAG, "Empty queue");
                            break;
                        }
                        action = (ObbAction) this.mActions.get(0);
                        if (action != null) {
                            action.execute(this);
                            break;
                        }
                    }
                    Slog.e(MountService.TAG, "Cannot bind to media container service");
                    for (ObbAction action2 : this.mActions) {
                        action2.handleError();
                    }
                    this.mActions.clear();
                    break;
                    break;
                case 3:
                    Slog.i(MountService.TAG, "OBB_MCS_UNBIND");
                    if (this.mActions.size() > 0) {
                        this.mActions.remove(0);
                    }
                    if (this.mActions.size() == 0) {
                        if (this.mBound) {
                            disconnectService();
                            break;
                        }
                    }
                    MountService.this.mObbActionHandler.sendEmptyMessage(2);
                    break;
                    break;
                case 4:
                    Slog.i(MountService.TAG, "OBB_MCS_RECONNECT");
                    if (this.mActions.size() > 0) {
                        if (this.mBound) {
                            disconnectService();
                        }
                        if (!connectToService()) {
                            Slog.e(MountService.TAG, "Failed to bind to media container service");
                            for (ObbAction action22 : this.mActions) {
                                action22.handleError();
                            }
                            this.mActions.clear();
                            break;
                        }
                    }
                    break;
                case 5:
                    String path = msg.obj;
                    Slog.i(MountService.TAG, "Flushing all OBB state for path " + path);
                    synchronized (MountService.this.mObbMounts) {
                        List<ObbState> obbStatesToRemove = new LinkedList();
                        for (ObbState state : MountService.this.mObbPathToStateMap.values()) {
                            if (state.canonicalPath.startsWith(path)) {
                                obbStatesToRemove.add(state);
                            }
                        }
                        for (ObbState obbState : obbStatesToRemove) {
                            Slog.i(MountService.TAG, "Removing state for " + obbState.rawPath);
                            MountService.this.removeObbStateLocked(obbState);
                            try {
                                obbState.token.onObbResult(obbState.rawPath, obbState.nonce, 2);
                            } catch (RemoteException e) {
                                Slog.i(MountService.TAG, "Couldn't send unmount notification for  OBB: " + obbState.rawPath);
                            }
                        }
                    }
            }
        }

        private boolean connectToService() {
            Slog.i(MountService.TAG, "Trying to bind to DefaultContainerService");
            if (!MountService.this.mContext.bindServiceAsUser(new Intent().setComponent(MountService.DEFAULT_CONTAINER_COMPONENT), MountService.this.mDefContainerConn, 1, UserHandle.SYSTEM)) {
                return false;
            }
            this.mBound = true;
            return true;
        }

        private void disconnectService() {
            MountService.this.mContainerService = null;
            this.mBound = false;
            MountService.this.mContext.unbindService(MountService.this.mDefContainerConn);
        }
    }

    class ObbState implements DeathRecipient {
        final String canonicalPath;
        final int nonce;
        final int ownerGid;
        final String rawPath;
        final IObbActionListener token;

        public ObbState(String rawPath, String canonicalPath, int callingUid, IObbActionListener token, int nonce) {
            this.rawPath = rawPath;
            this.canonicalPath = canonicalPath;
            this.ownerGid = UserHandle.getSharedAppGid(callingUid);
            this.token = token;
            this.nonce = nonce;
        }

        public IBinder getBinder() {
            return this.token.asBinder();
        }

        public void binderDied() {
            MountService.this.mObbActionHandler.sendMessage(MountService.this.mObbActionHandler.obtainMessage(1, new UnmountObbAction(this, true)));
        }

        public void link() throws RemoteException {
            getBinder().linkToDeath(this, 0);
        }

        public void unlink() {
            getBinder().unlinkToDeath(this, 0);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("ObbState{");
            sb.append("rawPath=").append(this.rawPath);
            sb.append(",canonicalPath=").append(this.canonicalPath);
            sb.append(",ownerGid=").append(this.ownerGid);
            sb.append(",token=").append(this.token);
            sb.append(",binder=").append(getBinder());
            sb.append('}');
            return sb.toString();
        }
    }

    class UnmountObbAction extends ObbAction {
        private final boolean mForceUnmount;

        UnmountObbAction(ObbState obbState, boolean force) {
            super(obbState);
            this.mForceUnmount = force;
        }

        public void handleExecute() throws IOException {
            ObbState existingState;
            MountService.this.waitForReady();
            MountService.this.warnOnNotMounted();
            synchronized (MountService.this.mObbMounts) {
                existingState = (ObbState) MountService.this.mObbPathToStateMap.get(this.mObbState.rawPath);
            }
            if (existingState == null) {
                sendNewStatusOrIgnore(23);
            } else if (existingState.ownerGid != this.mObbState.ownerGid) {
                Slog.w(MountService.TAG, "Permission denied attempting to unmount OBB " + existingState.rawPath + " (owned by GID " + existingState.ownerGid + ")");
                sendNewStatusOrIgnore(25);
            } else {
                int rc = 0;
                try {
                    Object[] objArr = new Object[2];
                    objArr[0] = "unmount";
                    objArr[1] = this.mObbState.canonicalPath;
                    Command cmd = new Command("obb", objArr);
                    if (this.mForceUnmount) {
                        cmd.appendArg("force");
                    }
                    MountService.this.mConnector.execute(cmd);
                } catch (NativeDaemonConnectorException e) {
                    int code = e.getCode();
                    if (code == VoldResponseCode.OpFailedStorageBusy) {
                        rc = -7;
                    } else if (code == VoldResponseCode.OpFailedStorageNotFound) {
                        rc = 0;
                    } else {
                        rc = -1;
                    }
                }
                if (rc == 0) {
                    synchronized (MountService.this.mObbMounts) {
                        MountService.this.removeObbStateLocked(existingState);
                    }
                    sendNewStatusOrIgnore(2);
                } else {
                    Slog.w(MountService.TAG, "Could not unmount OBB: " + existingState);
                    sendNewStatusOrIgnore(22);
                }
            }
        }

        public void handleError() {
            sendNewStatusOrIgnore(20);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("UnmountObbAction{");
            sb.append(this.mObbState);
            sb.append(",force=");
            sb.append(this.mForceUnmount);
            sb.append('}');
            return sb.toString();
        }
    }

    class VoldResponseCode {
        public static final int AsecListResult = 111;
        public static final int AsecPathResult = 211;
        public static final int BENCHMARK_RESULT = 661;
        public static final int CryptfsGetfieldResult = 113;
        public static final int DISK_CREATED = 640;
        public static final int DISK_DESTROYED = 649;
        public static final int DISK_LABEL_CHANGED = 642;
        public static final int DISK_SCANNED = 643;
        public static final int DISK_SIZE_CHANGED = 641;
        public static final int DISK_STATE_CHANGED = 670;
        public static final int DISK_SYS_PATH_CHANGED = 644;
        public static final int MOVE_STATUS = 660;
        public static final int OpFailedMediaBlank = 402;
        public static final int OpFailedMediaCorrupt = 403;
        public static final int OpFailedNoMedia = 401;
        public static final int OpFailedStorageBusy = 405;
        public static final int OpFailedStorageNotFound = 406;
        public static final int OpFailedVolNotMounted = 404;
        public static final int ShareEnabledResult = 212;
        public static final int ShareStatusResult = 210;
        public static final int StorageUsersListResult = 112;
        public static final int TRIM_RESULT = 662;
        public static final int VOLUME_CREATED = 650;
        public static final int VOLUME_DESTROYED = 659;
        public static final int VOLUME_FS_LABEL_CHANGED = 654;
        public static final int VOLUME_FS_TYPE_CHANGED = 652;
        public static final int VOLUME_FS_UUID_CHANGED = 653;
        public static final int VOLUME_INTERNAL_PATH_CHANGED = 656;
        public static final int VOLUME_PATH_CHANGED = 655;
        public static final int VOLUME_STATE_CHANGED = 651;
        public static final int VolumeListResult = 110;

        VoldResponseCode() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.MountService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.MountService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.MountService.<clinit>():void");
    }

    private VolumeInfo findVolumeByIdOrThrow(String id) {
        synchronized (this.mLock) {
            VolumeInfo vol = (VolumeInfo) this.mVolumes.get(id);
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
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
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

    private VolumeRecord findRecordForPath(String path) {
        synchronized (this.mLock) {
            int i = 0;
            while (i < this.mVolumes.size()) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                if (vol.path == null || !path.startsWith(vol.path)) {
                    i++;
                } else {
                    VolumeRecord volumeRecord = (VolumeRecord) this.mRecords.get(vol.fsUuid);
                    return volumeRecord;
                }
            }
            return null;
        }
    }

    private String scrubPath(String path) {
        if (path.startsWith(Environment.getDataDirectory().getAbsolutePath())) {
            return DecryptTool.UNLOCK_TYPE_INTERNAL;
        }
        VolumeRecord rec = findRecordForPath(path);
        if (rec == null || rec.createdMillis == 0) {
            return "unknown";
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
        long benchInterval = Global.getLong(this.mContext.getContentResolver(), "storage_benchmark_interval", UnixCalendar.WEEK_IN_MILLIS);
        if (benchInterval == -1) {
            return false;
        }
        if (benchInterval == 0) {
            return true;
        }
        synchronized (this.mLock) {
            int i = 0;
            while (i < this.mVolumes.size()) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                VolumeRecord rec = (VolumeRecord) this.mRecords.get(vol.fsUuid);
                if (!vol.isMountedWritable() || rec == null || System.currentTimeMillis() - rec.lastBenchMillis < benchInterval) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    private CountDownLatch findOrCreateDiskScanLatch(String diskId) {
        CountDownLatch latch;
        synchronized (this.mLock) {
            latch = (CountDownLatch) this.mDiskScanLatches.get(diskId);
            if (latch == null) {
                latch = new CountDownLatch(1);
                this.mDiskScanLatches.put(diskId, latch);
            }
        }
        return latch;
    }

    private static String escapeNull(String arg) {
        if (TextUtils.isEmpty(arg)) {
            return "!";
        }
        if (arg.indexOf(0) == -1 && arg.indexOf(32) == -1) {
            return arg;
        }
        throw new IllegalArgumentException(arg);
    }

    public void waitForAsecScan() {
        waitForLatch(this.mAsecsScanned, "mAsecsScanned");
    }

    private void waitForReady() {
        waitForLatch(this.mConnectedSignal, "mConnectedSignal");
    }

    private void waitForLatch(CountDownLatch latch, String condition) {
        try {
            waitForLatch(latch, condition, -1);
        } catch (TimeoutException e) {
        }
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

    private boolean isReady() {
        try {
            return this.mConnectedSignal.await(0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    private void handleSystemReady() {
        initIfReadyAndConnected();
        resetIfReadyAndConnected();
        if (this.mSendUmsConnectedOnBoot) {
            sendUmsIntent(true);
            this.mSendUmsConnectedOnBoot = false;
        }
        MountServiceIdler.scheduleIdlePass(this.mContext);
    }

    @Deprecated
    private void killMediaProvider(List<UserInfo> users) {
        if (users != null) {
            long token = Binder.clearCallingIdentity();
            try {
                for (UserInfo user : users) {
                    if (!user.isSystemOnly()) {
                        ProviderInfo provider = this.mPms.resolveContentProvider(OppoProcessManager.RESUME_REASON_MEDIA_STR, 786432, user.id);
                        if (provider != null) {
                            try {
                                ActivityManagerNative.getDefault().killApplication(provider.applicationInfo.packageName, UserHandle.getAppId(provider.applicationInfo.uid), -1, "vold reset");
                                break;
                            } catch (RemoteException e) {
                            }
                        } else {
                            continue;
                        }
                    }
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    private void addInternalVolumeLocked() {
        VolumeInfo internal = new VolumeInfo("private", 1, null, null);
        internal.state = 2;
        internal.path = Environment.getDataDirectory().getAbsolutePath();
        this.mVolumes.put(internal.id, internal);
    }

    private void initIfReadyAndConnected() {
        Slog.d(TAG, "Thinking about init, mSystemReady=" + this.mSystemReady + ", mDaemonConnected=" + this.mDaemonConnected);
        if (this.mSystemReady && this.mDaemonConnected && !StorageManager.isFileEncryptedNativeOnly()) {
            boolean initLocked = StorageManager.isFileEncryptedEmulatedOnly();
            Slog.d(TAG, "Setting up emulation state, initlocked=" + initLocked);
            for (UserInfo user : ((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers()) {
                Object[] objArr;
                if (initLocked) {
                    try {
                        objArr = new Object[2];
                        objArr[0] = "lock_user_key";
                        objArr[1] = Integer.valueOf(user.id);
                        this.mCryptConnector.execute("cryptfs", objArr);
                    } catch (NativeDaemonConnectorException e) {
                        Slog.w(TAG, "Failed to init vold", e);
                    }
                } else {
                    objArr = new Object[5];
                    objArr[0] = "unlock_user_key";
                    objArr[1] = Integer.valueOf(user.id);
                    objArr[2] = Integer.valueOf(user.serialNumber);
                    objArr[3] = "!";
                    objArr[4] = "!";
                    this.mCryptConnector.execute("cryptfs", objArr);
                }
            }
        }
    }

    private void resetIfReadyAndConnected() {
        Slog.d(TAG, "Thinking about reset, mSystemReady=" + this.mSystemReady + ", mDaemonConnected=" + this.mDaemonConnected);
        if (this.mSystemReady && this.mDaemonConnected) {
            List<UserInfo> users = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers();
            killMediaProvider(users);
            synchronized (this.mLock) {
                int[] systemUnlockedUsers = this.mSystemUnlockedUsers;
                this.mDisks.clear();
                this.mVolumes.clear();
                addInternalVolumeLocked();
            }
            try {
                NativeDaemonConnector nativeDaemonConnector = this.mConnector;
                String str = TAG_VOLUME;
                Object[] objArr = new Object[1];
                objArr[0] = "reset";
                nativeDaemonConnector.execute(str, objArr);
                for (UserInfo user : users) {
                    nativeDaemonConnector = this.mConnector;
                    str = TAG_VOLUME;
                    objArr = new Object[3];
                    objArr[0] = "user_added";
                    objArr[1] = Integer.valueOf(user.id);
                    objArr[2] = Integer.valueOf(user.serialNumber);
                    nativeDaemonConnector.execute(str, objArr);
                }
                for (int userId : systemUnlockedUsers) {
                    NativeDaemonConnector nativeDaemonConnector2 = this.mConnector;
                    String str2 = TAG_VOLUME;
                    Object[] objArr2 = new Object[2];
                    objArr2[0] = "user_started";
                    objArr2[1] = Integer.valueOf(userId);
                    nativeDaemonConnector2.execute(str2, objArr2);
                }
            } catch (NativeDaemonConnectorException e) {
                Slog.w(TAG, "Failed to reset vold", e);
            }
        }
    }

    private void onUnlockUser(int userId) {
        Slog.d(TAG, "onUnlockUser " + userId);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = TAG_VOLUME;
            Object[] objArr = new Object[2];
            objArr[0] = "user_started";
            objArr[1] = Integer.valueOf(userId);
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
        }
        this.mIsStartUser = true;
        synchronized (this.mVolumes) {
            for (int i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
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

    private void onCleanupUser(int userId) {
        Slog.d(TAG, "onCleanupUser " + userId);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = TAG_VOLUME;
            Object[] objArr = new Object[2];
            objArr[0] = "user_stopped";
            objArr[1] = Integer.valueOf(userId);
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
        }
        synchronized (this.mVolumes) {
            this.mSystemUnlockedUsers = ArrayUtils.removeInt(this.mSystemUnlockedUsers, userId);
        }
    }

    void runIdleMaintenance(Runnable callback) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(4, callback));
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

    private void handleDaemonConnected() {
        if (mPanicDebug) {
            Slog.i(TAG, "handleDaemonConnected");
        }
        initIfReadyAndConnected();
        resetIfReadyAndConnected();
        this.mConnectedSignal.countDown();
        if (this.mConnectedSignal.getCount() == 0) {
            if (IElsaManager.EMPTY_PACKAGE.equals(SystemProperties.get("vold.encrypt_progress"))) {
                copyLocaleFromMountService();
            }
            this.mPms.scanAvailableAsecs();
            this.mAsecsScanned.countDown();
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
                    ActivityManagerNative.getDefault().updatePersistentConfiguration(config);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error setting system locale from mount service", e);
                }
                Slog.d(TAG, "Setting system properties to " + systemLocale + " from mount service");
                SystemProperties.set("persist.sys.locale", locale.toLanguageTag());
            }
        } catch (RemoteException e2) {
        }
    }

    public boolean onCheckHoldWakeLock(int code) {
        return false;
    }

    public boolean onEvent(int code, String raw, String[] cooked) {
        boolean onEventLocked;
        synchronized (this.mLock) {
            onEventLocked = onEventLocked(code, raw, cooked);
        }
        return onEventLocked;
    }

    private boolean onEventLocked(int code, String raw, String[] cooked) {
        String id;
        DiskInfo disk;
        StringBuilder builder;
        int i;
        VolumeInfo vol;
        String path;
        VolumeRecord rec;
        switch (code) {
            case VoldResponseCode.DISK_CREATED /*640*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "DISK_CREATED");
                }
                if (cooked.length == 3) {
                    id = cooked[1];
                    int flags = Integer.parseInt(cooked[2]);
                    if (SystemProperties.getBoolean("persist.fw.force_adoptable", false) || this.mForceAdoptable) {
                        flags |= 1;
                    }
                    if (StorageManager.isFileEncryptedNativeOnly()) {
                        flags &= -2;
                    }
                    this.mDisks.put(id, new DiskInfo(id, flags));
                    if (mPanicDebug) {
                        Slog.d(TAG, "create diskInfo=" + this.mDisks.get(id));
                    }
                    this.isDiskInsert = true;
                    break;
                }
                break;
            case VoldResponseCode.DISK_SIZE_CHANGED /*641*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "DISK_SIZE_CHANGED");
                }
                if (cooked.length == 3) {
                    disk = (DiskInfo) this.mDisks.get(cooked[1]);
                    if (disk != null) {
                        if (mPanicDebug) {
                            Slog.d(TAG, "disk size change from + " + disk.size + " to " + Long.parseLong(cooked[2]));
                        }
                        disk.size = Long.parseLong(cooked[2]);
                        break;
                    }
                }
                break;
            case VoldResponseCode.DISK_LABEL_CHANGED /*642*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "DISK_LABEL_CHANGED");
                }
                disk = (DiskInfo) this.mDisks.get(cooked[1]);
                if (disk != null) {
                    builder = new StringBuilder();
                    for (i = 2; i < cooked.length; i++) {
                        builder.append(cooked[i]).append(' ');
                    }
                    disk.label = builder.toString().trim();
                    if (mPanicDebug) {
                        Slog.d(TAG, "DISK_LABEL_CHANGED, new label = " + disk.label + ", diskInfo=" + disk);
                        break;
                    }
                    break;
                }
                break;
            case VoldResponseCode.DISK_SCANNED /*643*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "DISK_SCANNED");
                }
                if (cooked.length == 2) {
                    disk = (DiskInfo) this.mDisks.get(cooked[1]);
                    if (disk != null) {
                        onDiskScannedLocked(disk);
                        break;
                    }
                }
                break;
            case VoldResponseCode.DISK_SYS_PATH_CHANGED /*644*/:
                if (cooked.length == 3) {
                    disk = (DiskInfo) this.mDisks.get(cooked[1]);
                    if (disk != null) {
                        disk.sysPath = cooked[2];
                        break;
                    }
                }
                break;
            case VoldResponseCode.DISK_DESTROYED /*649*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "DISK_DESTROYED");
                }
                if (cooked.length == 2) {
                    disk = (DiskInfo) this.mDisks.remove(cooked[1]);
                    if (disk != null) {
                        this.mCallbacks.notifyDiskDestroyed(disk);
                    }
                    updateDefaultPathIfNeed();
                    break;
                }
                break;
            case VoldResponseCode.VOLUME_CREATED /*650*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "VOLUME_CREATED");
                }
                id = cooked[1];
                VolumeInfo volumeInfo = new VolumeInfo(id, Integer.parseInt(cooked[2]), (DiskInfo) this.mDisks.get(TextUtils.nullIfEmpty(cooked[3])), TextUtils.nullIfEmpty(cooked[4]));
                this.mVolumes.put(id, volumeInfo);
                onVolumeCreatedLocked(volumeInfo);
                if (mPanicDebug) {
                    Slog.d(TAG, "create volumeInfo=" + this.mVolumes.get(id));
                    break;
                }
                break;
            case VoldResponseCode.VOLUME_STATE_CHANGED /*651*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "VOLUME_STATE_CHANGED");
                }
                if (cooked.length == 3) {
                    vol = (VolumeInfo) this.mVolumes.get(cooked[1]);
                    if (vol != null) {
                        int oldState = vol.state;
                        int newState = Integer.parseInt(cooked[2]);
                        if (newState != 3) {
                            if (newState == 2) {
                                vol.readOnlyType = 0;
                            } else {
                                vol.readOnlyType = -1;
                            }
                            vol.state = newState;
                            onVolumeStateChangedLocked(vol, oldState, newState);
                            break;
                        }
                        vol.readOnlyType = 1;
                        for (int userId : this.mSystemUnlockedUsers) {
                            if (vol.isVisibleForRead(userId)) {
                                this.mHandler.obtainMessage(H_VOLUME_BROADCAST_STORAGE_RO, vol.buildStorageVolume(this.mContext, userId, false)).sendToTarget();
                            }
                        }
                        break;
                    }
                }
                break;
            case VoldResponseCode.VOLUME_FS_TYPE_CHANGED /*652*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "VOLUME_FS_TYPE_CHANGED");
                }
                if (cooked.length == 3) {
                    vol = (VolumeInfo) this.mVolumes.get(cooked[1]);
                    if (vol != null) {
                        vol.fsType = cooked[2];
                        if (mPanicDebug) {
                            Slog.d(TAG, "new fsType=" + vol.fsType + ", volumeInfo=" + vol);
                            break;
                        }
                    }
                }
                break;
            case VoldResponseCode.VOLUME_FS_UUID_CHANGED /*653*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "VOLUME_FS_UUID_CHANGED");
                }
                if (cooked.length == 3) {
                    vol = (VolumeInfo) this.mVolumes.get(cooked[1]);
                    if (vol != null) {
                        vol.fsUuid = cooked[2];
                        if (mPanicDebug) {
                            Slog.d(TAG, "new fsUuid=" + vol.fsUuid + ", volumeInfo=" + vol);
                            break;
                        }
                    }
                }
                break;
            case VoldResponseCode.VOLUME_FS_LABEL_CHANGED /*654*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "VOLUME_FS_LABEL_CHANGED");
                }
                vol = (VolumeInfo) this.mVolumes.get(cooked[1]);
                if (vol != null) {
                    builder = new StringBuilder();
                    for (i = 2; i < cooked.length; i++) {
                        builder.append(cooked[i]).append(' ');
                    }
                    vol.fsLabel = builder.toString().trim();
                    if (mPanicDebug) {
                        Slog.d(TAG, "new fsLabel=" + vol.fsLabel + ", volumeInfo=" + vol);
                        break;
                    }
                    break;
                }
                break;
            case VoldResponseCode.VOLUME_PATH_CHANGED /*655*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "VOLUME_PATH_CHANGED");
                }
                if (cooked.length == 3) {
                    vol = (VolumeInfo) this.mVolumes.get(cooked[1]);
                    if (vol != null) {
                        vol.path = cooked[2];
                        if (mPanicDebug) {
                            Slog.d(TAG, "new path= " + vol.path + ", volumeInfo=" + vol);
                            break;
                        }
                    }
                }
                break;
            case VoldResponseCode.VOLUME_INTERNAL_PATH_CHANGED /*656*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "VOLUME_INTERNAL_PATH_CHANGED");
                }
                if (cooked.length == 3) {
                    vol = (VolumeInfo) this.mVolumes.get(cooked[1]);
                    if (vol != null) {
                        vol.internalPath = cooked[2];
                        if (mPanicDebug) {
                            Slog.d(TAG, "new internal path= " + vol.internalPath + ", volumeInfo=" + vol);
                            break;
                        }
                    }
                }
                break;
            case VoldResponseCode.VOLUME_DESTROYED /*659*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "VOLUME_DESTROYED");
                }
                if (cooked.length == 2) {
                    if (mPanicDebug) {
                        Slog.d(TAG, "destroyed volumeInfo=" + this.mVolumes.get(cooked[1]));
                    }
                    this.mVolumes.remove(cooked[1]);
                    break;
                }
                break;
            case VoldResponseCode.MOVE_STATUS /*660*/:
                if (mPanicDebug) {
                    Slog.d(TAG, "MOVE_STATUS");
                }
                onMoveStatusLocked(Integer.parseInt(cooked[1]));
                break;
            case VoldResponseCode.BENCHMARK_RESULT /*661*/:
                if (cooked.length == 7) {
                    path = cooked[1];
                    String ident = cooked[2];
                    long create = Long.parseLong(cooked[3]);
                    long drop = Long.parseLong(cooked[4]);
                    ((DropBoxManager) this.mContext.getSystemService(DropBoxManager.class)).addText(TAG_STORAGE_BENCHMARK, scrubPath(path) + " " + ident + " " + create + " " + Long.parseLong(cooked[5]) + " " + Long.parseLong(cooked[6]));
                    rec = findRecordForPath(path);
                    if (rec != null) {
                        rec.lastBenchMillis = System.currentTimeMillis();
                        writeSettingsLocked();
                        break;
                    }
                }
                break;
            case VoldResponseCode.TRIM_RESULT /*662*/:
                if (cooked.length == 4) {
                    path = cooked[1];
                    ((DropBoxManager) this.mContext.getSystemService(DropBoxManager.class)).addText(TAG_STORAGE_TRIM, scrubPath(path) + " " + Long.parseLong(cooked[2]) + " " + Long.parseLong(cooked[3]));
                    rec = findRecordForPath(path);
                    if (rec != null) {
                        rec.lastTrimMillis = System.currentTimeMillis();
                        writeSettingsLocked();
                        break;
                    }
                }
                break;
            case VoldResponseCode.DISK_STATE_CHANGED /*670*/:
                disk = (DiskInfo) this.mDisks.get(cooked[1]);
                if (disk != null) {
                    boolean found = false;
                    VolumeInfo voltmp = null;
                    for (i = 0; i < this.mVolumes.size(); i++) {
                        vol = (VolumeInfo) this.mVolumes.valueAt(i);
                        DiskInfo diskInfo = vol.getDisk();
                        if (diskInfo != null && diskInfo == disk) {
                            if (vol.state == 2) {
                                vol.readOnlyType = 2;
                                if (!found) {
                                    voltmp = vol;
                                }
                                found = true;
                            } else {
                                vol.readOnlyType = -1;
                            }
                        }
                    }
                    if (found) {
                        for (int userId2 : this.mSystemUnlockedUsers) {
                            if (voltmp.isVisibleForRead(userId2)) {
                                this.mHandler.obtainMessage(H_VOLUME_BROADCAST_STORAGE_RO, voltmp.buildStorageVolume(this.mContext, userId2, false)).sendToTarget();
                            }
                        }
                        break;
                    }
                }
                break;
            default:
                Slog.d(TAG, "Unhandled vold event " + code);
                break;
        }
        return true;
    }

    private void onDiskScannedLocked(DiskInfo disk) {
        if (mPanicDebug) {
            Slog.d(TAG, "onDiskScannedLocked, diskInfo=" + disk);
        }
        int volumeCount = 0;
        for (int i = 0; i < this.mVolumes.size(); i++) {
            if (Objects.equals(disk.id, ((VolumeInfo) this.mVolumes.valueAt(i)).getDiskId())) {
                volumeCount++;
            }
        }
        if (mPanicDebug) {
            Slog.d(TAG, "this disk has " + volumeCount + " volumes");
        }
        Intent intent = new Intent("android.os.storage.action.DISK_SCANNED");
        intent.addFlags(83886080);
        intent.putExtra("android.os.storage.extra.DISK_ID", disk.id);
        intent.putExtra("android.os.storage.extra.VOLUME_COUNT", volumeCount);
        if (mPanicDebug) {
            Slog.d(TAG, "sendBroadcastAsUser, intent=" + intent + ", disk.id=" + disk.id + ", volumeCount=" + volumeCount);
        }
        this.mHandler.obtainMessage(7, intent).sendToTarget();
        CountDownLatch latch = (CountDownLatch) this.mDiskScanLatches.remove(disk.id);
        if (latch != null) {
            latch.countDown();
        }
        disk.volumeCount = volumeCount;
        this.mCallbacks.notifyDiskScanned(disk, volumeCount);
    }

    private void onVolumeCreatedLocked(VolumeInfo vol) {
        if (this.mPms.isOnlyCoreApps()) {
            Slog.d(TAG, "System booted in core-only mode; ignoring volume " + vol.getId());
            return;
        }
        if (mPanicDebug) {
            Slog.d(TAG, "onVolumeCreatedLocked, volumeInfo=" + vol);
        }
        if (vol.type == 2) {
            VolumeInfo privateVol = ((StorageManager) this.mContext.getSystemService(StorageManager.class)).findPrivateForEmulated(vol);
            if (mPanicDebug) {
                Slog.d(TAG, "privateVol=" + privateVol);
            }
            if (Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, this.mPrimaryStorageUuid) && "private".equals(privateVol.id)) {
                Slog.v(TAG, "Found primary storage at " + vol);
                vol.mountFlags |= 1;
                vol.mountFlags |= 2;
                this.mHandler.obtainMessage(5, vol).sendToTarget();
            } else if (Objects.equals(privateVol.fsUuid, this.mPrimaryStorageUuid)) {
                Slog.v(TAG, "Found primary storage at " + vol);
                vol.mountFlags |= 1;
                vol.mountFlags |= 2;
                this.mHandler.obtainMessage(5, vol).sendToTarget();
            }
        } else if (vol.type == 0) {
            if (Objects.equals("primary_physical", this.mPrimaryStorageUuid) && vol.disk.isDefaultPrimary()) {
                Slog.v(TAG, "Found primary storage at " + vol);
                vol.mountFlags |= 1;
                vol.mountFlags |= 2;
            }
            if (vol.disk.isAdoptable() || vol.isPhoneStorage()) {
                vol.mountFlags |= 2;
            }
            vol.mountFlags |= 2;
            vol.mountUserId = this.mCurrentUserId;
            this.mHandler.obtainMessage(5, vol).sendToTarget();
        } else if (vol.type == 1) {
            this.mHandler.obtainMessage(5, vol).sendToTarget();
        } else {
            Slog.d(TAG, "Skipping automatic mounting of " + vol);
        }
    }

    private boolean isBroadcastWorthy(VolumeInfo vol) {
        switch (vol.getType()) {
            case 0:
            case 1:
            case 2:
                switch (vol.getState()) {
                    case 0:
                    case 2:
                    case 3:
                    case 5:
                    case 6:
                    case 8:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    private void onVolumeStateChangedLocked(VolumeInfo vol, int oldState, int newState) {
        if (mPanicDebug) {
            Slog.d(TAG, "onVolumeStateChangedLocked, oldState=" + VolumeInfo.getEnvironmentForState(oldState) + ", newState=" + VolumeInfo.getEnvironmentForState(newState) + ", volumeInfo=" + vol);
        }
        if (vol.isMountedReadable() && !TextUtils.isEmpty(vol.fsUuid)) {
            VolumeRecord rec = (VolumeRecord) this.mRecords.get(vol.fsUuid);
            if (rec == null) {
                rec = new VolumeRecord(vol.type, vol.fsUuid);
                rec.partGuid = vol.partGuid;
                rec.createdMillis = System.currentTimeMillis();
                if (vol.type == 1) {
                    rec.nickname = vol.disk.getDescription();
                }
                this.mRecords.put(rec.fsUuid, rec);
                writeSettingsLocked();
            } else if (TextUtils.isEmpty(rec.partGuid)) {
                rec.partGuid = vol.partGuid;
                writeSettingsLocked();
            }
        }
        this.mCallbacks.notifyVolumeStateChanged(vol, oldState, newState);
        if (this.mBootCompleted && isBroadcastWorthy(vol)) {
            Intent intent = new Intent("android.os.storage.action.VOLUME_STATE_CHANGED");
            intent.putExtra("android.os.storage.extra.VOLUME_ID", vol.id);
            intent.putExtra("android.os.storage.extra.VOLUME_STATE", newState);
            intent.putExtra("android.os.storage.extra.FS_UUID", vol.fsUuid);
            intent.addFlags(83886080);
            if (mPanicDebug) {
                Slog.d(TAG, "sendBroadcastAsUser, intent=" + intent + ", vol.id=" + vol.id + ", newState=" + newState + ", vol.fsUuid=" + vol.fsUuid + ", flags=FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT");
            }
            this.mHandler.obtainMessage(7, intent).sendToTarget();
        }
        String oldStateEnv = VolumeInfo.getEnvironmentForState(oldState);
        String newStateEnv = VolumeInfo.getEnvironmentForState(newState);
        if (!Objects.equals(oldStateEnv, newStateEnv)) {
            for (int userId : this.mSystemUnlockedUsers) {
                if (vol.isVisibleForRead(userId)) {
                    if ((!Objects.equals(newStateEnv, "unmounted") && !Objects.equals(newStateEnv, "mounted") && !Objects.equals(newStateEnv, "ejecting") && !Objects.equals(newStateEnv, "bad_removal")) || vol.getPathForUser(userId) != null) {
                        StorageVolume userVol = vol.buildStorageVolume(this.mContext, userId, false);
                        this.mHandler.obtainMessage(6, userVol).sendToTarget();
                        if (mPanicDebug) {
                            Slog.d(TAG, "notify callbacks StorageStateChanged, storageVolume=" + userVol);
                        }
                        this.mCallbacks.notifyStorageStateChanged(userVol.getPath(), oldStateEnv, newStateEnv);
                    } else if (mPanicDebug) {
                        Slog.d(TAG, "volume path is null, will not send brocast,volumeInfo=" + vol);
                    }
                }
            }
        }
        if (vol.type == 0 && vol.state == 5) {
            this.mObbActionHandler.sendMessage(this.mObbActionHandler.obtainMessage(5, vol.path));
        }
        updateDefaultPathIfNeed();
    }

    private void onMoveStatusLocked(int status) {
        if (this.mMoveCallback == null) {
            Slog.w(TAG, "Odd, status but no move requested");
            return;
        }
        try {
            this.mMoveCallback.onStatusChanged(-1, status, -1);
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

    private boolean isMountDisallowed(VolumeInfo vol) {
        UserManager userManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        boolean isUsbRestricted = false;
        if (vol.disk != null && vol.disk.isUsb()) {
            isUsbRestricted = userManager.hasUserRestriction("no_usb_file_transfer", Binder.getCallingUserHandle());
        }
        boolean isTypeRestricted = false;
        if (vol.type == 0 || vol.type == 1) {
            isTypeRestricted = userManager.hasUserRestriction("no_physical_media", Binder.getCallingUserHandle());
        }
        return !isUsbRestricted ? isTypeRestricted : true;
    }

    private void enforceAdminUser() {
        UserManager um = (UserManager) this.mContext.getSystemService("user");
        int callingUserId = UserHandle.getCallingUserId();
        long token = Binder.clearCallingIdentity();
        try {
            boolean isAdmin = um.getUserInfo(callingUserId).isAdmin();
            if (!isAdmin) {
                throw new SecurityException("Only admin users can adopt sd cards");
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public MountService(Context context) {
        this.mLock = new Object();
        this.mLocalUnlockedUsers = EmptyArray.INT;
        this.mSystemUnlockedUsers = EmptyArray.INT;
        this.mDisks = new ArrayMap();
        this.mVolumes = new ArrayMap();
        this.mRecords = new ArrayMap();
        this.mDiskScanLatches = new ArrayMap();
        this.mCurrentUserId = 0;
        this.mSystemReady = false;
        this.mBootCompleted = false;
        this.mDaemonConnected = false;
        this.mConnectedSignal = new CountDownLatch(2);
        this.mAsecsScanned = new CountDownLatch(1);
        this.mUnmountLock = new Object();
        this.mAsecMountSet = new HashSet();
        this.mObbMounts = new HashMap();
        this.mObbPathToStateMap = new HashMap();
        this.mMountServiceInternal = new MountServiceInternalImpl(this, null);
        this.mDefContainerConn = new DefaultContainerConnection();
        this.mContainerService = null;
        this.mUserReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean z = true;
                String action = intent.getAction();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userId < 0) {
                    z = false;
                }
                Preconditions.checkArgument(z);
                try {
                    NativeDaemonConnector -get3;
                    String str;
                    Object[] objArr;
                    if ("android.intent.action.USER_ADDED".equals(action)) {
                        if (MountService.mPanicDebug) {
                            Slog.i(MountService.TAG, "onReceive:ACTION_USER_ADDED");
                        }
                        int userSerialNumber = ((UserManager) MountService.this.mContext.getSystemService(UserManager.class)).getUserSerialNumber(userId);
                        -get3 = MountService.this.mConnector;
                        str = MountService.TAG_VOLUME;
                        objArr = new Object[3];
                        objArr[0] = "user_added";
                        objArr[1] = Integer.valueOf(userId);
                        objArr[2] = Integer.valueOf(userSerialNumber);
                        -get3.execute(str, objArr);
                    } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                        synchronized (MountService.this.mVolumes) {
                            int size = MountService.this.mVolumes.size();
                            for (int i = 0; i < size; i++) {
                                VolumeInfo vol = (VolumeInfo) MountService.this.mVolumes.valueAt(i);
                                if (vol.mountUserId == userId) {
                                    vol.mountUserId = 0;
                                }
                            }
                        }
                        if (MountService.mPanicDebug) {
                            Slog.i(MountService.TAG, "onReceive:ACTION_USER_REMOVED");
                        }
                        -get3 = MountService.this.mConnector;
                        str = MountService.TAG_VOLUME;
                        objArr = new Object[2];
                        objArr[0] = "user_removed";
                        objArr[1] = Integer.valueOf(userId);
                        -get3.execute(str, objArr);
                    } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
                        if (MountService.mPanicDebug) {
                            Slog.i(MountService.TAG, "ACTION_USER_SWITCHED");
                        }
                        MountService.this.mCurrentUserId = userId;
                        MountService.this.updateDefaultPathForUserSwitch();
                    }
                } catch (NativeDaemonConnectorException e) {
                    Slog.w(MountService.TAG, "Failed to send user details to vold", e);
                }
            }
        };
        this.mIsStartUser = false;
        this.isDiskInsert = false;
        this.mDMReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("com.mediatek.dm.LAWMO_UNLOCK")) {
                    if (MountService.mPanicDebug) {
                        Slog.d(MountService.TAG, "mDMReceiver USB enable");
                    }
                    new Thread() {
                        public void run() {
                            synchronized (MountService.OMADM_SYNC_LOCK) {
                                MountService.this.enableUSBFuction(true);
                            }
                        }
                    }.start();
                } else if (action.equals("com.mediatek.dm.LAWMO_LOCK")) {
                    if (MountService.mPanicDebug) {
                        Slog.d(MountService.TAG, "mDMReceiver USB disable");
                    }
                    new Thread() {
                        public void run() {
                            synchronized (MountService.OMADM_SYNC_LOCK) {
                                MountService.this.enableUSBFuction(false);
                            }
                        }
                    }.start();
                } else if (action.equals(MountService.OMADM_SD_FORMAT)) {
                    if (MountService.mPanicDebug) {
                        Slog.d(MountService.TAG, "mDMReceiver format SD");
                    }
                    new Thread() {
                        public void run() {
                            VolumeInfo[] volumes = MountService.this.getVolumes(0);
                            synchronized (MountService.OMADM_SYNC_LOCK) {
                                int userId = MountService.this.mCurrentUserId;
                                for (VolumeInfo vol : volumes) {
                                    if (vol.isVisible() && vol.getType() == 0) {
                                        try {
                                            if (vol.getState() == 2) {
                                                MountService.this.unmount(vol.getId());
                                                int j = 0;
                                                while (j < 20) {
                                                    AnonymousClass3.sleep(1000);
                                                    if (vol.getState() != 0) {
                                                        j++;
                                                    } else if (MountService.mPanicDebug) {
                                                        Slog.d(MountService.TAG, "Unmount Succeeded, volume=" + vol);
                                                    }
                                                }
                                            } else if (vol.getState() == 9) {
                                                if (MountService.mPanicDebug) {
                                                    Slog.d(MountService.TAG, "volume is shared, unshared firstly, volume=" + vol);
                                                }
                                                MountService.this.doShareUnshareVolume(vol.getId(), false);
                                            }
                                            MountService.this.format(vol.getId());
                                            if (MountService.mPanicDebug) {
                                                Slog.d(MountService.TAG, "format Succeed! volume=" + vol);
                                            }
                                        } catch (InterruptedException e) {
                                            Slog.e(MountService.TAG, "SD format exception", e);
                                        } catch (IllegalArgumentException e2) {
                                            Slog.e(MountService.TAG, "SD format exception", e2);
                                        } catch (SecurityException e3) {
                                            Slog.e(MountService.TAG, "SD format exception", e3);
                                        } catch (NullPointerException e4) {
                                            Slog.e(MountService.TAG, "SD format exception", e4);
                                        }
                                    } else if (MountService.mPanicDebug) {
                                        Slog.d(MountService.TAG, "no need format, skip volume=" + vol);
                                    }
                                }
                            }
                        }
                    }.start();
                }
            }
        };
        this.mPrivacyProtectionReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("com.mediatek.ppl.NOTIFY_UNLOCK")) {
                    if (MountService.mPanicDebug) {
                        Slog.i(MountService.TAG, "Privacy Protection unlock!");
                    }
                    new Thread() {
                        public void run() {
                            MountService.this.enableUSBFuction(true);
                        }
                    }.start();
                } else if (action.equals("com.mediatek.ppl.NOTIFY_LOCK")) {
                    if (MountService.mPanicDebug) {
                        Slog.i(MountService.TAG, "Privacy Protection lock!");
                    }
                    new Thread() {
                        public void run() {
                            MountService.this.enableUSBFuction(false);
                        }
                    }.start();
                } else if (action.equals(MountService.PRIVACY_PROTECTION_WIPE)) {
                    if (MountService.mPanicDebug) {
                        Slog.i(MountService.TAG, "Privacy Protection wipe!");
                    }
                    MountService.this.formatPhoneStorageAndExternalSDCard();
                }
            }
        };
        this.mIsTurnOnOffUsb = false;
        this.mIsUsbConnected = false;
        this.mSendUmsConnectedOnBoot = false;
        this.mUMSCount = 0;
        this.mUsbReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (MountService.mPanicDebug) {
                    Slog.i(MountService.TAG, "mUsbReceiver onReceive, intent=" + intent);
                }
                boolean isConnected = (intent.getBooleanExtra("connected", false) && intent.getBooleanExtra("mass_storage", false)) ? !intent.getBooleanExtra("SettingUsbCharging", false) : false;
                MountService.this.mIsUsbConnected = isConnected;
                MountService.this.notifyShareAvailabilityChange(isConnected);
            }
        };
        this.mBootIPOReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                new Thread() {
                    public void run() {
                        if (MountService.mPanicDebug) {
                            Slog.d(MountService.TAG, "MountService BOOT_IPO");
                        }
                        MountService.isIPOBooting = true;
                        try {
                            if (MountService.mPanicDebug) {
                                Slog.d(MountService.TAG, "Notify VOLD IPO startup");
                            }
                            NativeDaemonConnector -get3 = MountService.this.mConnector;
                            String str = MountService.TAG_VOLUME;
                            Object[] objArr = new Object[2];
                            objArr[0] = "ipo";
                            objArr[1] = "startup";
                            -get3.execute(str, objArr);
                        } catch (NativeDaemonConnectorException e) {
                            Slog.e(MountService.TAG, "Error reinit SD card while IPO", e);
                        }
                        MountService.this.waitAllVolumeMounted();
                        if (MountService.mPanicDebug) {
                            Slog.d(MountService.TAG, "MountService BOOT_IPO finish");
                        }
                        MountService.isIPOBooting = false;
                        MountService.this.updateDefaultPathIfNeed();
                    }
                }.start();
            }
        };
        this.mOldEncryptionType = -1;
        sSelf = this;
        this.mContext = context;
        this.mCallbacks = new Callbacks(FgThread.get().getLooper());
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mPms = (PackageManagerService) ServiceManager.getService("package");
        HandlerThread hthread = new HandlerThread(TAG);
        hthread.start();
        this.mHandler = new MountServiceHandler(hthread.getLooper());
        this.mObbActionHandler = new ObbActionHandler(IoThread.get().getLooper());
        this.mLastMaintenanceFile = new File(new File(Environment.getDataDirectory(), "system"), LAST_FSTRIM_FILE);
        if (this.mLastMaintenanceFile.exists()) {
            this.mLastMaintenance = this.mLastMaintenanceFile.lastModified();
        } else {
            try {
                new FileOutputStream(this.mLastMaintenanceFile).close();
            } catch (IOException e) {
                Slog.e(TAG, "Unable to create fstrim record " + this.mLastMaintenanceFile.getPath());
            }
        }
        this.mSettingsFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), "storage.xml"));
        synchronized (this.mLock) {
            readSettingsLocked();
        }
        LocalServices.addService(MountServiceInternal.class, this.mMountServiceInternal);
        this.mConnector = new NativeDaemonConnector(this, "vold", 500, VOLD_TAG, 25, null);
        if (LOG_ENABLE) {
            this.mConnector.setDebug(true);
        } else {
            this.mConnector.setDebug(false);
        }
        this.mConnector.setWarnIfHeld(this.mLock);
        this.mConnectorThread = new Thread(this.mConnector, VOLD_TAG);
        this.mCryptConnector = new NativeDaemonConnector(this, "cryptd", 500, CRYPTD_TAG, 25, null);
        this.mCryptConnector.setDebug(true);
        this.mCryptConnectorThread = new Thread(this.mCryptConnector, CRYPTD_TAG);
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_ADDED");
        userFilter.addAction("android.intent.action.USER_REMOVED");
        userFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(this.mUserReceiver, userFilter, null, this.mHandler);
        synchronized (this.mLock) {
            addInternalVolumeLocked();
        }
        initMTKFeature();
    }

    private void start() {
        this.mConnectorThread.start();
        this.mCryptConnectorThread.start();
    }

    private void systemReady() {
        this.mSystemReady = true;
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    private void bootCompleted() {
        this.mBootCompleted = true;
    }

    private String getDefaultPrimaryStorageUuid() {
        if (SystemProperties.getBoolean("ro.vold.primary_physical", false)) {
            return "primary_physical";
        }
        return StorageManager.UUID_PRIVATE_INTERNAL;
    }

    private void readSettingsLocked() {
        if (mPanicDebug) {
            Slog.i(TAG, "readSettingsLocked");
        }
        this.mRecords.clear();
        this.mPrimaryStorageUuid = getDefaultPrimaryStorageUuid();
        this.mForceAdoptable = false;
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = this.mSettingsFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(autoCloseable, StandardCharsets.UTF_8.name());
            loop0:
            while (true) {
                int type = in.next();
                if (type == 1) {
                    break loop0;
                } else if (type == 2) {
                    String tag = in.getName();
                    if (TAG_VOLUMES.equals(tag)) {
                        int version = XmlUtils.readIntAttribute(in, "version", 1);
                        boolean primaryPhysical = SystemProperties.getBoolean("ro.vold.primary_physical", false);
                        boolean validAttr = version < 3 ? version >= 2 && !primaryPhysical : true;
                        if (validAttr) {
                            this.mPrimaryStorageUuid = XmlUtils.readStringAttribute(in, ATTR_PRIMARY_STORAGE_UUID);
                        }
                        this.mForceAdoptable = XmlUtils.readBooleanAttribute(in, ATTR_FORCE_ADOPTABLE, false);
                        if (mPanicDebug) {
                            Slog.i(TAG, "read start tag: version=" + version + ", primaryPhysical=" + primaryPhysical + ", mPrimaryStorageUuid=" + this.mPrimaryStorageUuid + ", mForceAdoptable=" + this.mForceAdoptable);
                        }
                    } else if (TAG_VOLUME.equals(tag)) {
                        VolumeRecord rec = readVolumeRecord(in);
                        if (mPanicDebug) {
                            Slog.i(TAG, "read volume tag: volumeRecode=" + rec);
                        }
                        this.mRecords.put(rec.fsUuid, rec);
                    }
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e2) {
            Slog.wtf(TAG, "Failed reading metadata", e2);
        } catch (XmlPullParserException e3) {
            Slog.wtf(TAG, "Failed reading metadata", e3);
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    private void writeSettingsLocked() {
        if (mPanicDebug) {
            Slog.i(TAG, "writeSettingsLocked");
        }
        try {
            FileOutputStream fos = this.mSettingsFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.startTag(null, TAG_VOLUMES);
            XmlUtils.writeIntAttribute(out, "version", 3);
            XmlUtils.writeStringAttribute(out, ATTR_PRIMARY_STORAGE_UUID, this.mPrimaryStorageUuid);
            XmlUtils.writeBooleanAttribute(out, ATTR_FORCE_ADOPTABLE, this.mForceAdoptable);
            if (mPanicDebug) {
                Slog.i(TAG, "write start tag: version=3, mPrimaryStorageUuid=" + this.mPrimaryStorageUuid + ", mForceAdoptable=" + this.mForceAdoptable);
            }
            int size = this.mRecords.size();
            for (int i = 0; i < size; i++) {
                VolumeRecord rec = (VolumeRecord) this.mRecords.valueAt(i);
                if (mPanicDebug) {
                    Slog.i(TAG, "write volume record: " + rec);
                }
                writeVolumeRecord(out, rec);
            }
            out.endTag(null, TAG_VOLUMES);
            out.endDocument();
            this.mSettingsFile.finishWrite(fos);
        } catch (IOException e) {
            if (null != null) {
                this.mSettingsFile.failWrite(null);
            }
        }
    }

    public static VolumeRecord readVolumeRecord(XmlPullParser in) throws IOException {
        VolumeRecord meta = new VolumeRecord(XmlUtils.readIntAttribute(in, "type"), XmlUtils.readStringAttribute(in, ATTR_FS_UUID));
        meta.partGuid = XmlUtils.readStringAttribute(in, ATTR_PART_GUID);
        meta.nickname = XmlUtils.readStringAttribute(in, ATTR_NICKNAME);
        meta.userFlags = XmlUtils.readIntAttribute(in, ATTR_USER_FLAGS);
        meta.createdMillis = XmlUtils.readLongAttribute(in, ATTR_CREATED_MILLIS);
        meta.lastTrimMillis = XmlUtils.readLongAttribute(in, ATTR_LAST_TRIM_MILLIS);
        meta.lastBenchMillis = XmlUtils.readLongAttribute(in, ATTR_LAST_BENCH_MILLIS);
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
        XmlUtils.writeLongAttribute(out, ATTR_LAST_TRIM_MILLIS, rec.lastTrimMillis);
        XmlUtils.writeLongAttribute(out, ATTR_LAST_BENCH_MILLIS, rec.lastBenchMillis);
        out.endTag(null, TAG_VOLUME);
    }

    public void registerListener(IMountServiceListener listener) {
        this.mCallbacks.register(listener);
    }

    public void unregisterListener(IMountServiceListener listener) {
        this.mCallbacks.unregister(listener);
    }

    public void shutdown(IMountShutdownObserver observer) {
        enforcePermission("android.permission.SHUTDOWN");
        Slog.i(TAG, "Shutting down");
        waitMTKNetlogStopped();
        this.mHandler.obtainMessage(3, observer).sendToTarget();
    }

    public boolean isUsbMassStorageConnected() {
        if (mPanicDebug) {
            Slog.i(TAG, "isUsbMassStorageConnected");
        }
        waitForReady();
        if (getUmsEnabling()) {
            Slog.i(TAG, "isUsbMassStorageConnected return true");
            return true;
        }
        if (mPanicDebug) {
            Slog.i(TAG, "isUsbMassStorageConnected return " + this.mIsUsbConnected);
        }
        return this.mIsUsbConnected;
    }

    public void setUsbMassStorageEnabled(boolean enable) {
        if (mPanicDebug) {
            Slog.d(TAG, "setUsbMassStorageEnabled, enable=" + enable);
        }
        waitForReady();
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        validateUserRestriction("no_usb_file_transfer");
        int userId = this.mCurrentUserId;
        this.mIsTurnOnOffUsb = true;
        synchronized (this.mLock) {
            for (int i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                if (vol.isAllowUsbMassStorage(userId)) {
                    if (enable) {
                        setUmsEnabling(true);
                        if (vol.getState() == 2 || vol.getState() == 3) {
                            Slog.d(TAG, "setUsbMassStorageEnabled, first unmount volume=" + vol);
                            unmount(vol.getId());
                            if (mPanicDebug) {
                                Slog.d(TAG, "setUsbMassStorageEnabled, second share volume");
                            }
                            doShareUnshareVolume(vol.getId(), true);
                        } else if (vol.getState() == 0) {
                            if (mPanicDebug) {
                                Slog.d(TAG, "setUsbMassStorageEnabled, just share volume");
                            }
                            doShareUnshareVolume(vol.getId(), true);
                        }
                        setUmsEnabling(false);
                    } else if (!(vol.getState() == 7 || vol.getState() == 8)) {
                        if (mPanicDebug) {
                            Slog.d(TAG, "setUsbMassStorageEnabled, first unshare volume=" + vol);
                        }
                        doShareUnshareVolume(vol.getId(), false);
                        if (mPanicDebug) {
                            Slog.d(TAG, "setUsbMassStorageEnabled, second mount volume");
                        }
                        mount(vol.getId());
                    }
                } else if (mPanicDebug) {
                    Slog.d(TAG, "no need share, skip volume=" + vol);
                }
            }
        }
        this.mIsTurnOnOffUsb = false;
    }

    public boolean isUsbMassStorageEnabled() {
        if (mPanicDebug) {
            Slog.i(TAG, "isUsbMassStorageEnabled");
        }
        waitForReady();
        boolean result = false;
        synchronized (this.mVolumes) {
            for (int i = 0; i < this.mVolumes.size(); i++) {
                if (isVolumeSharedEnable((VolumeInfo) this.mVolumes.valueAt(i))) {
                    result = true;
                    break;
                }
            }
        }
        if (mPanicDebug) {
            Slog.i(TAG, "isUsbMassStorageEnabled return + " + result);
        }
        return result;
    }

    public String getVolumeState(String mountPoint) {
        throw new UnsupportedOperationException();
    }

    public boolean isExternalStorageEmulated() {
        throw new UnsupportedOperationException();
    }

    public int mountVolume(String path) {
        if (mPanicDebug) {
            Slog.i(TAG, "mountVolume, path=" + path);
        }
        mount(findVolumeIdForPathOrThrow(path));
        return 0;
    }

    public void unmountVolume(String path, boolean force, boolean removeEncryption) {
        if (mPanicDebug) {
            Slog.i(TAG, "unmountVolume, path=" + path);
        }
        unmount(findVolumeIdForPathOrThrow(path));
    }

    public int formatVolume(String path) {
        if (mPanicDebug) {
            Slog.i(TAG, "formatVolume, path=" + path);
        }
        format(findVolumeIdForPathOrThrow(path));
        return 0;
    }

    public void mount(String volId) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        VolumeInfo vol = findVolumeByIdOrThrow(volId);
        if (mPanicDebug) {
            Slog.i(TAG, "mount, volId=" + volId + ", volumeInfo=" + vol);
        }
        if (isMountDisallowed(vol)) {
            throw new SecurityException("Mounting " + volId + " restricted by policy");
        }
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = TAG_VOLUME;
            Object[] objArr = new Object[4];
            objArr[0] = OppoProcessManager.RESUME_REASON_MOUNT_STR;
            objArr[1] = vol.id;
            objArr[2] = Integer.valueOf(vol.mountFlags);
            objArr[3] = Integer.valueOf(vol.mountUserId);
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            if (mPanicDebug) {
                Slog.e(TAG, OppoProcessManager.RESUME_REASON_MOUNT_STR + vol + "ERROR!!");
            }
        }
    }

    public void unmount(String volId) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        VolumeInfo vol = findVolumeByIdOrThrow(volId);
        if (mPanicDebug) {
            Slog.i(TAG, "unmount, volId=" + volId + ", volumeInfo=" + vol);
        }
        if (vol.isPrimaryPhysical()) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mUnmountLock) {
                    this.mUnmountSignal = new CountDownLatch(1);
                    this.mPms.updateExternalMediaStatus(false, true);
                    waitForLatch(this.mUnmountSignal, "mUnmountSignal");
                    this.mUnmountSignal = null;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = TAG_VOLUME;
            Object[] objArr = new Object[2];
            objArr[0] = "unmount";
            objArr[1] = vol.id;
            nativeDaemonConnector.execute(str, objArr);
            updateDefaultPathIfNeed();
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void format(String volId) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        waitForReady();
        VolumeInfo vol = findVolumeByIdOrThrow(volId);
        if (mPanicDebug) {
            Slog.i(TAG, "format, volId=" + volId + ", volumeInfo=" + vol);
        }
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = TAG_VOLUME;
            Object[] objArr = new Object[3];
            objArr[0] = "format";
            objArr[1] = vol.id;
            objArr[2] = "auto";
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public long benchmark(String volId) {
        if (mPanicDebug) {
            Slog.i(TAG, "benchmark, volId=" + volId);
        }
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        waitForReady();
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = TAG_VOLUME;
            Object[] objArr = new Object[2];
            objArr[0] = "benchmark";
            objArr[1] = volId;
            return Long.parseLong(nativeDaemonConnector.execute(ColorOSDeviceIdleHelper.ALARM_WINDOW_LENGTH, str, objArr).getMessage());
        } catch (NativeDaemonTimeoutException e) {
            return JobStatus.NO_LATEST_RUNTIME;
        } catch (NativeDaemonConnectorException e2) {
            throw e2.rethrowAsParcelableException();
        }
    }

    public void partitionPublic(String diskId) {
        if (mPanicDebug) {
            Slog.i(TAG, "partitionPublic, diskId=" + diskId);
        }
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        waitForReady();
        CountDownLatch latch = findOrCreateDiskScanLatch(diskId);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = TAG_VOLUME;
            Object[] objArr = new Object[3];
            objArr[0] = "partition";
            objArr[1] = diskId;
            objArr[2] = "public";
            nativeDaemonConnector.execute(str, objArr);
            waitForLatch(latch, "partitionPublic", LocationFudger.FASTEST_INTERVAL_MS);
            if (mPanicDebug) {
                Slog.i(TAG, "partitionPublic return");
            }
        } catch (NativeDaemonConnectorException e) {
            Slog.i(TAG, "partitionPublic NativeDaemonConnectorException, e=" + e.getMessage());
            popFormatFailToast();
            throw e.rethrowAsParcelableException();
        } catch (TimeoutException e2) {
            if (mPanicDebug) {
                Slog.i(TAG, "partitionPublic timeout exception, e=" + e2.getMessage());
            }
            popFormatFailToast();
            throw new IllegalStateException(e2);
        }
    }

    public void partitionPrivate(String diskId) {
        if (mPanicDebug) {
            Slog.i(TAG, "partitionPrivate, diskId=" + diskId);
        }
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        enforceAdminUser();
        waitForReady();
        CountDownLatch latch = findOrCreateDiskScanLatch(diskId);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = TAG_VOLUME;
            Object[] objArr = new Object[3];
            objArr[0] = "partition";
            objArr[1] = diskId;
            objArr[2] = "private";
            nativeDaemonConnector.execute(str, objArr);
            waitForLatch(latch, "partitionPrivate", LocationFudger.FASTEST_INTERVAL_MS);
            if (mPanicDebug) {
                Slog.i(TAG, "partitionPrivate return");
            }
        } catch (NativeDaemonConnectorException e) {
            if (mPanicDebug) {
                Slog.i(TAG, "partitionPrivate NativeDaemonConnectorException, e=" + e.getMessage());
            }
            popFormatFailToast();
            throw e.rethrowAsParcelableException();
        } catch (TimeoutException e2) {
            if (mPanicDebug) {
                Slog.i(TAG, "partitionPrivate timeout exception, e=" + e2.getMessage());
            }
            popFormatFailToast();
            throw new IllegalStateException(e2);
        }
    }

    public void partitionMixed(String diskId, int ratio) {
        if (mPanicDebug) {
            Slog.i(TAG, "partitionMixed, diskId=" + diskId + ", ratio=" + ratio);
        }
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        enforceAdminUser();
        waitForReady();
        CountDownLatch latch = findOrCreateDiskScanLatch(diskId);
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = TAG_VOLUME;
            Object[] objArr = new Object[4];
            objArr[0] = "partition";
            objArr[1] = diskId;
            objArr[2] = "mixed";
            objArr[3] = Integer.valueOf(ratio);
            nativeDaemonConnector.execute(str, objArr);
            waitForLatch(latch, "partitionMixed", ColorOSDeviceIdleHelper.ALARM_WINDOW_LENGTH);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        } catch (TimeoutException e2) {
            throw new IllegalStateException(e2);
        }
    }

    public void setVolumeNickname(String fsUuid, String nickname) {
        if (mPanicDebug) {
            Slog.i(TAG, "setVolumeNickname, fsUuid=" + fsUuid + ", nickname=" + nickname);
        }
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        Preconditions.checkNotNull(fsUuid);
        synchronized (this.mLock) {
            VolumeRecord rec = (VolumeRecord) this.mRecords.get(fsUuid);
            rec.nickname = nickname;
            this.mCallbacks.notifyVolumeRecordChanged(rec);
            writeSettingsLocked();
        }
    }

    public void setVolumeUserFlags(String fsUuid, int flags, int mask) {
        if (mPanicDebug) {
            Slog.i(TAG, "setVolumeUserFlags, fsUuid=" + fsUuid + ", flags=" + flags + ", mask=" + mask);
        }
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        Preconditions.checkNotNull(fsUuid);
        synchronized (this.mLock) {
            VolumeRecord rec = (VolumeRecord) this.mRecords.get(fsUuid);
            rec.userFlags = (rec.userFlags & (~mask)) | (flags & mask);
            this.mCallbacks.notifyVolumeRecordChanged(rec);
            writeSettingsLocked();
        }
    }

    public void forgetVolume(String fsUuid) {
        if (mPanicDebug) {
            Slog.i(TAG, "forgetVolume, fsUuid=" + fsUuid);
        }
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        Preconditions.checkNotNull(fsUuid);
        synchronized (this.mLock) {
            VolumeRecord rec = (VolumeRecord) this.mRecords.remove(fsUuid);
            if (!(rec == null || TextUtils.isEmpty(rec.partGuid))) {
                this.mHandler.obtainMessage(9, rec.partGuid).sendToTarget();
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
        if (mPanicDebug) {
            Slog.i(TAG, "forgetAllVolumes");
        }
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        synchronized (this.mLock) {
            for (int i = 0; i < this.mRecords.size(); i++) {
                String fsUuid = (String) this.mRecords.keyAt(i);
                VolumeRecord rec = (VolumeRecord) this.mRecords.valueAt(i);
                if (!TextUtils.isEmpty(rec.partGuid)) {
                    this.mHandler.obtainMessage(9, rec.partGuid).sendToTarget();
                }
                this.mCallbacks.notifyVolumeForgotten(fsUuid);
            }
            this.mRecords.clear();
            if (!Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, this.mPrimaryStorageUuid)) {
                this.mPrimaryStorageUuid = getDefaultPrimaryStorageUuid();
            }
            writeSettingsLocked();
            resetIfReadyAndConnected();
        }
    }

    private void forgetPartition(String partGuid) {
        if (mPanicDebug) {
            Slog.i(TAG, "forgetPartition, partGuid=" + partGuid);
        }
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = TAG_VOLUME;
            Object[] objArr = new Object[2];
            objArr[0] = "forget_partition";
            objArr[1] = partGuid;
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            Slog.w(TAG, "Failed to forget key for " + partGuid + ": " + e);
        }
    }

    private void remountUidExternalStorage(int uid, int mode) {
        waitForReady();
        String modeName = "none";
        switch (mode) {
            case 1:
                modeName = "default";
                break;
            case 2:
                modeName = "read";
                break;
            case 3:
                modeName = "write";
                break;
        }
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = TAG_VOLUME;
            Object[] objArr = new Object[3];
            objArr[0] = "remount_uid";
            objArr[1] = Integer.valueOf(uid);
            objArr[2] = modeName;
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            Slog.w(TAG, "Failed to remount UID " + uid + " as " + modeName + ": " + e);
        }
    }

    public void setDebugFlags(int flags, int mask) {
        long token;
        if (mPanicDebug) {
            Slog.i(TAG, "setDebugFlags, flags=" + flags + ", mask=" + mask);
        }
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        if ((mask & 2) != 0) {
            if (StorageManager.isFileEncryptedNativeOnly()) {
                throw new IllegalStateException("Emulation not available on device with native FBE");
            } else if (this.mLockPatternUtils.isCredentialRequiredToDecrypt(false)) {
                throw new IllegalStateException("Emulation requires disabling 'Secure start-up' in Settings > Security");
            } else {
                token = Binder.clearCallingIdentity();
                boolean emulateFbe = (flags & 2) != 0;
                try {
                    if (this.mContext != null && this.mContext.getPackageManager().isFullFunctionMode()) {
                        SystemProperties.set("persist.sys.allcommode", "true");
                        SystemProperties.set("persist.sys.usb.config", "adb");
                    }
                    SystemProperties.set("persist.sys.emulate_fbe", Boolean.toString(emulateFbe));
                    ((PowerManager) this.mContext.getSystemService(PowerManager.class)).reboot(null);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }
        if ((mask & 1) != 0) {
            if (StorageManager.isFileEncryptedNativeOnly()) {
                throw new IllegalStateException("Adoptable storage not available on device with native FBE");
            }
            synchronized (this.mLock) {
                this.mForceAdoptable = (flags & 1) != 0;
                writeSettingsLocked();
                this.mHandler.obtainMessage(10).sendToTarget();
            }
        }
        if ((mask & 12) != 0) {
            String value;
            if ((flags & 4) != 0) {
                value = "force_on";
            } else if ((flags & 8) != 0) {
                value = "force_off";
            } else {
                value = IElsaManager.EMPTY_PACKAGE;
            }
            token = Binder.clearCallingIdentity();
            try {
                SystemProperties.set("persist.sys.sdcardfs", value);
                this.mHandler.obtainMessage(10).sendToTarget();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public String getPrimaryStorageUuid() {
        String str;
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        synchronized (this.mLock) {
            if (mPanicDebug) {
                Slog.i(TAG, "getPrimaryStorageUuid, mPrimaryStorageUuid=" + this.mPrimaryStorageUuid);
            }
            str = this.mPrimaryStorageUuid;
        }
        return str;
    }

    public void setPrimaryStorageUuid(String volumeUuid, IPackageMoveObserver callback) {
        if (mPanicDebug) {
            Slog.i(TAG, "setPrimaryStorageUuid, volumeUuid=" + volumeUuid);
        }
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        synchronized (this.mLock) {
            if (Objects.equals(this.mPrimaryStorageUuid, volumeUuid)) {
                throw new IllegalArgumentException("Primary storage already at " + volumeUuid);
            } else if (this.mMoveCallback != null) {
                throw new IllegalStateException("Move already in progress");
            } else {
                this.mMoveCallback = callback;
                this.mMoveTargetUuid = volumeUuid;
                if (Objects.equals("primary_physical", this.mPrimaryStorageUuid) || Objects.equals("primary_physical", volumeUuid)) {
                    Slog.d(TAG, "Skipping move to/from primary physical");
                    onMoveStatusLocked(82);
                    onMoveStatusLocked(-100);
                    this.mHandler.obtainMessage(10).sendToTarget();
                    return;
                }
                VolumeInfo from = findStorageForUuid(this.mPrimaryStorageUuid);
                VolumeInfo to = findStorageForUuid(volumeUuid);
                if (from == null) {
                    Slog.w(TAG, "Failing move due to missing from volume " + this.mPrimaryStorageUuid);
                    onMoveStatusLocked(-6);
                } else if (to == null) {
                    Slog.w(TAG, "Failing move due to missing to volume " + volumeUuid);
                    onMoveStatusLocked(-6);
                } else {
                    try {
                        NativeDaemonConnector nativeDaemonConnector = this.mConnector;
                        String str = TAG_VOLUME;
                        Object[] objArr = new Object[3];
                        objArr[0] = "move_storage";
                        objArr[1] = from.id;
                        objArr[2] = to.id;
                        nativeDaemonConnector.execute(str, objArr);
                    } catch (NativeDaemonConnectorException e) {
                        throw e.rethrowAsParcelableException();
                    }
                }
            }
        }
    }

    public int[] getStorageUsers(String path) {
        if (mPanicDebug) {
            Slog.i(TAG, "getStorageUsers, path=" + path);
        }
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        try {
            Object[] objArr = new Object[2];
            objArr[0] = SoundModelContract.KEY_USERS;
            objArr[1] = path;
            String[] r = NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("storage", objArr), 112);
            int[] data = new int[r.length];
            int i = 0;
            while (i < r.length) {
                String[] tok = r[i].split(" ");
                try {
                    data[i] = Integer.parseInt(tok[0]);
                    i++;
                } catch (NumberFormatException e) {
                    String str = TAG;
                    objArr = new Object[1];
                    objArr[0] = tok[0];
                    Slog.e(str, String.format("Error parsing pid %s", objArr));
                    return new int[0];
                }
            }
            return data;
        } catch (NativeDaemonConnectorException e2) {
            Slog.e(TAG, "Failed to retrieve storage users list", e2);
            return new int[0];
        }
    }

    private void warnOnNotMounted() {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                if (vol.isPrimary() && vol.isMountedWritable()) {
                    return;
                }
            }
            Slog.w(TAG, "No primary storage mounted!");
        }
    }

    public String[] getSecureContainerList() {
        enforcePermission("android.permission.ASEC_ACCESS");
        waitForReady();
        warnOnNotMounted();
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "list";
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("asec", objArr), 111);
        } catch (NativeDaemonConnectorException e) {
            return new String[0];
        }
    }

    public int createSecureContainer(String id, int sizeMb, String fstype, String key, int ownerUid, boolean external) {
        if (mPanicDebug) {
            Slog.i(TAG, "createSecureContainer, id=" + id + ", sizeMb=" + sizeMb + ", fstype=" + fstype + ", key=" + key);
        }
        enforcePermission("android.permission.ASEC_CREATE");
        waitForReady();
        warnOnNotMounted();
        int rc = 0;
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "asec";
            Object[] objArr = new Object[7];
            objArr[0] = "create";
            objArr[1] = id;
            objArr[2] = Integer.valueOf(sizeMb);
            objArr[3] = fstype;
            objArr[4] = new SensitiveArg(key);
            objArr[5] = Integer.valueOf(ownerUid);
            objArr[6] = external ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0";
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            rc = -1;
        }
        if (rc == 0) {
            synchronized (this.mAsecMountSet) {
                this.mAsecMountSet.add(id);
            }
        }
        return rc;
    }

    public int resizeSecureContainer(String id, int sizeMb, String key) {
        if (mPanicDebug) {
            Slog.i(TAG, "resizeSecureContainer, id=" + id + ", sizeMb=" + sizeMb + ", key=" + key);
        }
        enforcePermission("android.permission.ASEC_CREATE");
        waitForReady();
        warnOnNotMounted();
        try {
            Object[] objArr = new Object[4];
            objArr[0] = "resize";
            objArr[1] = id;
            objArr[2] = Integer.valueOf(sizeMb);
            objArr[3] = new SensitiveArg(key);
            this.mConnector.execute("asec", objArr);
            return 0;
        } catch (NativeDaemonConnectorException e) {
            return -1;
        }
    }

    public int finalizeSecureContainer(String id) {
        if (mPanicDebug) {
            Slog.i(TAG, "finalizeSecureContainer, id=" + id);
        }
        enforcePermission("android.permission.ASEC_CREATE");
        warnOnNotMounted();
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "finalize";
            objArr[1] = id;
            this.mConnector.execute("asec", objArr);
            return 0;
        } catch (NativeDaemonConnectorException e) {
            return -1;
        }
    }

    public int fixPermissionsSecureContainer(String id, int gid, String filename) {
        if (mPanicDebug) {
            Slog.i(TAG, "fixPermissionsSecureContainer, id=" + id + ", gid=" + gid + ", filename=" + filename);
        }
        enforcePermission("android.permission.ASEC_CREATE");
        warnOnNotMounted();
        try {
            Object[] objArr = new Object[4];
            objArr[0] = "fixperms";
            objArr[1] = id;
            objArr[2] = Integer.valueOf(gid);
            objArr[3] = filename;
            this.mConnector.execute("asec", objArr);
            return 0;
        } catch (NativeDaemonConnectorException e) {
            return -1;
        }
    }

    public int destroySecureContainer(String id, boolean force) {
        if (mPanicDebug) {
            Slog.i(TAG, "destroySecureContainer, id=" + id + ", force=" + force);
        }
        enforcePermission("android.permission.ASEC_DESTROY");
        waitForReady();
        warnOnNotMounted();
        Runtime.getRuntime().gc();
        int rc = 0;
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "destroy";
            objArr[1] = id;
            Command cmd = new Command("asec", objArr);
            if (force) {
                cmd.appendArg("force");
            }
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            if (e.getCode() == VoldResponseCode.OpFailedStorageBusy) {
                rc = -7;
            } else {
                rc = -1;
            }
        }
        if (rc == 0) {
            synchronized (this.mAsecMountSet) {
                if (this.mAsecMountSet.contains(id)) {
                    this.mAsecMountSet.remove(id);
                }
            }
        }
        return rc;
    }

    /* JADX WARNING: Missing block: B:12:0x005a, code:
            r2 = 0;
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            r4 = r8.mConnector;
            r5 = "asec";
            r6 = new java.lang.Object[5];
            r6[0] = com.android.server.am.OppoProcessManager.RESUME_REASON_MOUNT_STR;
            r6[1] = r9;
            r6[2] = new com.android.server.NativeDaemonConnector.SensitiveArg(r10);
            r6[3] = java.lang.Integer.valueOf(r11);
     */
    /* JADX WARNING: Missing block: B:15:0x007b, code:
            if (r12 == false) goto L_0x0095;
     */
    /* JADX WARNING: Missing block: B:16:0x007d, code:
            r3 = "ro";
     */
    /* JADX WARNING: Missing block: B:17:0x0080, code:
            r6[4] = r3;
            r4.execute(r5, r6);
     */
    /* JADX WARNING: Missing block: B:18:0x0086, code:
            if (r2 != 0) goto L_0x0091;
     */
    /* JADX WARNING: Missing block: B:19:0x0088, code:
            r4 = r8.mAsecMountSet;
     */
    /* JADX WARNING: Missing block: B:20:0x008a, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            r8.mAsecMountSet.add(r9);
     */
    /* JADX WARNING: Missing block: B:23:0x0090, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:24:0x0091, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            r3 = "rw";
     */
    /* JADX WARNING: Missing block: B:30:0x0099, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:32:0x00a0, code:
            if (r1.getCode() != com.android.server.MountService.VoldResponseCode.OpFailedStorageBusy) goto L_0x00a2;
     */
    /* JADX WARNING: Missing block: B:33:0x00a2, code:
            r2 = -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int mountSecureContainer(String id, String key, int ownerUid, boolean readOnly) {
        if (mPanicDebug) {
            Slog.i(TAG, "mountSecureContainer, id=" + id + ", key=" + key + ", ownerUid=" + ownerUid + ", readOnly=" + readOnly);
        }
        enforcePermission("android.permission.ASEC_MOUNT_UNMOUNT");
        waitForReady();
        warnOnNotMounted();
        synchronized (this.mAsecMountSet) {
            if (this.mAsecMountSet.contains(id)) {
                return -6;
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0051, code:
            java.lang.Runtime.getRuntime().gc();
            java.lang.System.runFinalization();
            r3 = 0;
     */
    /* JADX WARNING: Missing block: B:17:?, code:
            r5 = new java.lang.Object[2];
            r5[0] = "unmount";
            r5[1] = r9;
            r0 = new com.android.server.NativeDaemonConnector.Command("asec", r5);
     */
    /* JADX WARNING: Missing block: B:18:0x0070, code:
            if (r10 == false) goto L_0x0078;
     */
    /* JADX WARNING: Missing block: B:19:0x0072, code:
            r0.appendArg("force");
     */
    /* JADX WARNING: Missing block: B:20:0x0078, code:
            r8.mConnector.execute(r0);
     */
    /* JADX WARNING: Missing block: B:21:0x007d, code:
            if (r3 != 0) goto L_0x0088;
     */
    /* JADX WARNING: Missing block: B:22:0x007f, code:
            r5 = r8.mAsecMountSet;
     */
    /* JADX WARNING: Missing block: B:23:0x0081, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:25:?, code:
            r8.mAsecMountSet.remove(r9);
     */
    /* JADX WARNING: Missing block: B:26:0x0087, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:27:0x0088, code:
            return r3;
     */
    /* JADX WARNING: Missing block: B:31:0x008c, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:33:0x0093, code:
            if (r2.getCode() == com.android.server.MountService.VoldResponseCode.OpFailedStorageBusy) goto L_0x0095;
     */
    /* JADX WARNING: Missing block: B:34:0x0095, code:
            r3 = -7;
     */
    /* JADX WARNING: Missing block: B:35:0x0097, code:
            r3 = -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int unmountSecureContainer(String id, boolean force) {
        if (mPanicDebug) {
            Slog.i(TAG, "unmountSecureContainer, id=" + id + ", force=" + force);
        }
        enforcePermission("android.permission.ASEC_MOUNT_UNMOUNT");
        waitForReady();
        warnOnNotMounted();
        synchronized (this.mAsecMountSet) {
            if (!this.mAsecMountSet.contains(id)) {
                if (mPanicDebug) {
                    Slog.i(TAG, "OperationFailedStorageNotMounted");
                }
                return -5;
            }
        }
    }

    public boolean isSecureContainerMounted(String id) {
        boolean contains;
        enforcePermission("android.permission.ASEC_ACCESS");
        waitForReady();
        warnOnNotMounted();
        synchronized (this.mAsecMountSet) {
            contains = this.mAsecMountSet.contains(id);
        }
        return contains;
    }

    /* JADX WARNING: Missing block: B:14:0x004c, code:
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:16:?, code:
            r4 = new java.lang.Object[3];
            r4[0] = "rename";
            r4[1] = r8;
            r4[2] = r9;
            r7.mConnector.execute("asec", r4);
     */
    /* JADX WARNING: Missing block: B:22:0x0069, code:
            r1 = -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int renameSecureContainer(String oldId, String newId) {
        if (mPanicDebug) {
            Slog.i(TAG, "renameSecureContainer, oldId=" + oldId + ", newId=" + newId);
        }
        enforcePermission("android.permission.ASEC_RENAME");
        waitForReady();
        warnOnNotMounted();
        synchronized (this.mAsecMountSet) {
            if (this.mAsecMountSet.contains(oldId) || this.mAsecMountSet.contains(newId)) {
                return -6;
            }
        }
        return rc;
    }

    public String getSecureContainerPath(String id) {
        if (mPanicDebug) {
            Slog.i(TAG, "getSecureContainerPath, id=" + id);
        }
        enforcePermission("android.permission.ASEC_ACCESS");
        waitForReady();
        warnOnNotMounted();
        Object[] objArr;
        try {
            objArr = new Object[2];
            objArr[0] = "path";
            objArr[1] = id;
            NativeDaemonEvent event = this.mConnector.execute("asec", objArr);
            event.checkCode(211);
            return event.getMessage();
        } catch (NativeDaemonConnectorException e) {
            int code = e.getCode();
            if (code == VoldResponseCode.OpFailedStorageNotFound) {
                String str = TAG;
                objArr = new Object[1];
                objArr[0] = id;
                Slog.i(str, String.format("Container '%s' not found", objArr));
                return null;
            }
            objArr = new Object[1];
            objArr[0] = Integer.valueOf(code);
            throw new IllegalStateException(String.format("Unexpected response code %d", objArr));
        }
    }

    public String getSecureContainerFilesystemPath(String id) {
        if (mPanicDebug) {
            Slog.i(TAG, "getSecureContainerFilesystemPath, id=" + id);
        }
        enforcePermission("android.permission.ASEC_ACCESS");
        waitForReady();
        warnOnNotMounted();
        Object[] objArr;
        try {
            objArr = new Object[2];
            objArr[0] = "fspath";
            objArr[1] = id;
            NativeDaemonEvent event = this.mConnector.execute("asec", objArr);
            event.checkCode(211);
            return event.getMessage();
        } catch (NativeDaemonConnectorException e) {
            int code = e.getCode();
            if (code == VoldResponseCode.OpFailedStorageNotFound) {
                String str = TAG;
                objArr = new Object[1];
                objArr[0] = id;
                Slog.i(str, String.format("Container '%s' not found", objArr));
                return null;
            }
            objArr = new Object[1];
            objArr[0] = Integer.valueOf(code);
            throw new IllegalStateException(String.format("Unexpected response code %d", objArr));
        }
    }

    public void finishMediaUpdate() {
        if (mPanicDebug) {
            Slog.i(TAG, "finishMediaUpdate");
        }
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("no permission to call finishMediaUpdate()");
        } else if (this.mUnmountSignal != null) {
            this.mUnmountSignal.countDown();
        } else {
            Slog.w(TAG, "Odd, nobody asked to unmount?");
        }
    }

    private boolean isUidOwnerOfPackageOrSystem(String packageName, int callerUid) {
        boolean z = true;
        if (callerUid == 1000) {
            return true;
        }
        if (packageName == null) {
            return false;
        }
        int packageUid = this.mPms.getPackageUid(packageName, 268435456, UserHandle.getUserId(callerUid));
        Slog.d(TAG, "packageName = " + packageName + ", packageUid = " + packageUid + ", callerUid = " + callerUid);
        if (callerUid != packageUid) {
            z = false;
        }
        return z;
    }

    public String getMountedObbPath(String rawPath) {
        ObbState state;
        if (mPanicDebug) {
            Slog.i(TAG, "getMountedObbPath, rawPath=" + rawPath);
        }
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        waitForReady();
        warnOnNotMounted();
        synchronized (this.mObbMounts) {
            state = (ObbState) this.mObbPathToStateMap.get(rawPath);
        }
        if (state == null) {
            Slog.w(TAG, "Failed to find OBB mounted at " + rawPath);
            return null;
        }
        Object[] objArr;
        try {
            objArr = new Object[2];
            objArr[0] = "path";
            objArr[1] = state.canonicalPath;
            NativeDaemonEvent event = this.mConnector.execute("obb", objArr);
            event.checkCode(211);
            return event.getMessage();
        } catch (NativeDaemonConnectorException e) {
            int code = e.getCode();
            if (code == VoldResponseCode.OpFailedStorageNotFound) {
                return null;
            }
            objArr = new Object[1];
            objArr[0] = Integer.valueOf(code);
            throw new IllegalStateException(String.format("Unexpected response code %d", objArr));
        }
    }

    public boolean isObbMounted(String rawPath) {
        boolean containsKey;
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        synchronized (this.mObbMounts) {
            containsKey = this.mObbPathToStateMap.containsKey(rawPath);
        }
        return containsKey;
    }

    public void mountObb(String rawPath, String canonicalPath, String key, IObbActionListener token, int nonce) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        Preconditions.checkNotNull(canonicalPath, "canonicalPath cannot be null");
        Preconditions.checkNotNull(token, "token cannot be null");
        int callingUid = Binder.getCallingUid();
        ObbAction action = new MountObbAction(new ObbState(rawPath, canonicalPath, callingUid, token, nonce), key, callingUid);
        this.mObbActionHandler.sendMessage(this.mObbActionHandler.obtainMessage(1, action));
        Slog.i(TAG, "Send to OBB handler: " + action.toString());
    }

    public void unmountObb(String rawPath, boolean force, IObbActionListener token, int nonce) {
        ObbState existingState;
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        synchronized (this.mObbMounts) {
            existingState = (ObbState) this.mObbPathToStateMap.get(rawPath);
        }
        if (existingState != null) {
            String str = rawPath;
            ObbAction action = new UnmountObbAction(new ObbState(str, existingState.canonicalPath, Binder.getCallingUid(), token, nonce), force);
            this.mObbActionHandler.sendMessage(this.mObbActionHandler.obtainMessage(1, action));
            Slog.i(TAG, "Send to OBB handler: " + action.toString());
            return;
        }
        Slog.w(TAG, "Unknown OBB mount at " + rawPath);
    }

    public int getEncryptionState() {
        if (mPanicDebug) {
            Slog.i(TAG, "getEncryptionState");
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        waitForReady();
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "cryptocomplete";
            return Integer.parseInt(this.mCryptConnector.execute("cryptfs", objArr).getMessage());
        } catch (NumberFormatException e) {
            Slog.w(TAG, "Unable to parse result from cryptfs cryptocomplete");
            return -1;
        } catch (NativeDaemonConnectorException e2) {
            Slog.w(TAG, "Error in communicating with cryptfs in validating");
            return -1;
        }
    }

    public int decryptStorage(String password) {
        if (LOG_ENABLE) {
            Slog.i(TAG, "decryptStorage, password=" + password);
        }
        if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("password cannot be empty");
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        waitForReady();
        Slog.i(TAG, "decrypting storage...");
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "checkpw";
            objArr[1] = new SensitiveArg(password);
            int code = Integer.parseInt(this.mCryptConnector.execute("cryptfs", objArr).getMessage());
            if (code == 0) {
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        try {
                            Object[] objArr = new Object[1];
                            objArr[0] = "restart";
                            MountService.this.mCryptConnector.execute("cryptfs", objArr);
                        } catch (NativeDaemonConnectorException e) {
                            Slog.e(MountService.TAG, "problem executing in background", e);
                        }
                    }
                }, 1000);
            }
            return code;
        } catch (NativeDaemonConnectorException e) {
            return e.getCode();
        }
    }

    public int encryptStorage(int type, String password) {
        if (LOG_ENABLE) {
            Slog.i(TAG, "encryptStorage, type=" + CRYPTO_TYPES[type] + ", password=" + password);
        }
        if (!TextUtils.isEmpty(password) || type == 1) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
            waitForReady();
            Slog.i(TAG, "encrypting storage...");
            waitMtkLogStopped();
            Object[] objArr;
            if (type == 1) {
                try {
                    objArr = new Object[3];
                    objArr[0] = "enablecrypto";
                    objArr[1] = "inplace";
                    objArr[2] = CRYPTO_TYPES[type];
                    this.mCryptConnector.execute("cryptfs", objArr);
                } catch (NativeDaemonConnectorException e) {
                    return e.getCode();
                }
            }
            objArr = new Object[4];
            objArr[0] = "enablecrypto";
            objArr[1] = "inplace";
            objArr[2] = CRYPTO_TYPES[type];
            objArr[3] = new SensitiveArg(password);
            this.mCryptConnector.execute("cryptfs", objArr);
            return 0;
        }
        throw new IllegalArgumentException("password cannot be empty");
    }

    public int changeEncryptionPassword(int type, String password) {
        if (LOG_ENABLE) {
            Slog.i(TAG, "changeEncryptionPassword, type=" + CRYPTO_TYPES[type] + ", password=" + password);
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        if (!false) {
            return 0;
        }
        waitForReady();
        Slog.i(TAG, "changing encryption password...");
        if (this.mOldEncryptionType == -1) {
            this.mOldEncryptionType = getPasswordType();
        }
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "changepw";
            objArr[1] = CRYPTO_TYPES[type];
            objArr[2] = new SensitiveArg(password);
            NativeDaemonEvent event = this.mCryptConnector.execute("cryptfs", objArr);
            if (type != this.mOldEncryptionType) {
                Slog.i(TAG, "Encryption type changed from " + this.mOldEncryptionType + " to " + type);
                this.mOldEncryptionType = type;
                sendEncryptionTypeIntent();
            }
            return Integer.parseInt(event.getMessage());
        } catch (NativeDaemonConnectorException e) {
            return e.getCode();
        }
    }

    public int verifyEncryptionPassword(String password) throws RemoteException {
        if (LOG_ENABLE) {
            Slog.i(TAG, "verifyEncryptionPassword, password=" + password);
        }
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("no permission to access the crypt keeper");
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("password cannot be empty");
        }
        waitForReady();
        Slog.i(TAG, "validating encryption password...");
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "verifypw";
            objArr[1] = new SensitiveArg(password);
            NativeDaemonEvent event = this.mCryptConnector.execute("cryptfs", objArr);
            Slog.i(TAG, "cryptfs verifypw => " + event.getMessage());
            return Integer.parseInt(event.getMessage());
        } catch (NativeDaemonConnectorException e) {
            return e.getCode();
        }
    }

    public int getPasswordType() {
        if (mPanicDebug) {
            Slog.i(TAG, "getPasswordType");
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.STORAGE_INTERNAL", "no permission to access the crypt keeper");
        waitForReady();
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "getpwtype";
            NativeDaemonEvent event = this.mCryptConnector.execute("cryptfs", objArr);
            for (int i = 0; i < CRYPTO_TYPES.length; i++) {
                if (mPanicDebug) {
                    Slog.i(TAG, "CRYPTO_TYPES[" + i + "]=" + CRYPTO_TYPES[i] + ", event.getMessage()=" + event.getMessage());
                }
                if (CRYPTO_TYPES[i].equals(event.getMessage())) {
                    if (mPanicDebug) {
                        Slog.i(TAG, "return CRYPTO_TYPES=" + CRYPTO_TYPES[i]);
                    }
                    return i;
                }
            }
            throw new IllegalStateException("unexpected return from cryptfs");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setField(String field, String contents) throws RemoteException {
        if (mPanicDebug) {
            Slog.i(TAG, "setField, field=" + field + ", contens=" + contents);
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.STORAGE_INTERNAL", "no permission to access the crypt keeper");
        waitForReady();
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "setfield";
            objArr[1] = field;
            objArr[2] = contents;
            NativeDaemonEvent execute = this.mCryptConnector.execute("cryptfs", objArr);
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "setField caught NativeDaemonConnectorException ", e);
        }
    }

    public String getField(String field) throws RemoteException {
        if (mPanicDebug) {
            Slog.i(TAG, "getField, field=" + field);
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.STORAGE_INTERNAL", "no permission to access the crypt keeper");
        waitForReady();
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "getfield";
            objArr[1] = field;
            String[] contents = NativeDaemonEvent.filterMessageList(this.mCryptConnector.executeForList("cryptfs", objArr), 113);
            String result = new String();
            for (String content : contents) {
                result = result + content;
            }
            if (mPanicDebug) {
                Slog.i(TAG, "getField, return " + result);
            }
            return result;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "getField caught NativeDaemonConnectorException ", e);
            return IElsaManager.EMPTY_PACKAGE;
        }
    }

    public boolean isConvertibleToFBE() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STORAGE_INTERNAL", "no permission to access the crypt keeper");
        waitForReady();
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "isConvertibleToFBE";
            if (Integer.parseInt(this.mCryptConnector.execute("cryptfs", objArr).getMessage()) != 0) {
                return true;
            }
            return false;
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public String getPassword() throws RemoteException {
        if (mPanicDebug) {
            Slog.i(TAG, "getPassword");
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_KEYGUARD_SECURE_STORAGE", "only keyguard can retrieve password");
        this.mContext.enforceCallingOrSelfPermission("android.permission.STORAGE_INTERNAL", "no permission to access the crypt keeper");
        if (isReady()) {
            try {
                Object[] objArr = new Object[1];
                objArr[0] = "getpw";
                NativeDaemonEvent event = this.mCryptConnector.execute("cryptfs", objArr);
                if (!"-1".equals(event.getMessage())) {
                    return event.getMessage();
                }
                if (mPanicDebug) {
                    Slog.i(TAG, "no password, reutn null");
                }
                return null;
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            } catch (IllegalArgumentException e2) {
                Slog.e(TAG, "Invalid response to getPassword");
                return null;
            }
        }
        if (mPanicDebug) {
            Slog.i(TAG, "not ready, reutn null");
        }
        return new String();
    }

    public void clearPassword() throws RemoteException {
        if (mPanicDebug) {
            Slog.i(TAG, "clearPassword");
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.STORAGE_INTERNAL", "only keyguard can clear password");
        if (isReady()) {
            try {
                Object[] objArr = new Object[1];
                objArr[0] = "clearpw";
                NativeDaemonEvent event = this.mCryptConnector.execute("cryptfs", objArr);
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
    }

    public void createUserKey(int userId, int serialNumber, boolean ephemeral) {
        int i = 1;
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mCryptConnector;
            String str = "cryptfs";
            Object[] objArr = new Object[4];
            objArr[0] = "create_user_key";
            objArr[1] = Integer.valueOf(userId);
            objArr[2] = Integer.valueOf(serialNumber);
            if (!ephemeral) {
                i = 0;
            }
            objArr[3] = Integer.valueOf(i);
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void destroyUserKey(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "destroy_user_key";
            objArr[1] = Integer.valueOf(userId);
            this.mCryptConnector.execute("cryptfs", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    private SensitiveArg encodeBytes(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return new SensitiveArg("!");
        }
        return new SensitiveArg(HexDump.toHexString(bytes));
    }

    public void changeUserKey(int userId, int serialNumber, byte[] token, byte[] oldSecret, byte[] newSecret) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            Object[] objArr = new Object[6];
            objArr[0] = "change_user_key";
            objArr[1] = Integer.valueOf(userId);
            objArr[2] = Integer.valueOf(serialNumber);
            objArr[3] = encodeBytes(token);
            objArr[4] = encodeBytes(oldSecret);
            objArr[5] = encodeBytes(newSecret);
            this.mCryptConnector.execute("cryptfs", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void addUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            Object[] objArr = new Object[5];
            objArr[0] = "add_user_key_auth";
            objArr[1] = Integer.valueOf(userId);
            objArr[2] = Integer.valueOf(serialNumber);
            objArr[3] = encodeBytes(token);
            objArr[4] = encodeBytes(secret);
            this.mCryptConnector.execute("cryptfs", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void fixateNewestUserKeyAuth(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "fixate_newest_user_key_auth";
            objArr[1] = Integer.valueOf(userId);
            this.mCryptConnector.execute("cryptfs", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void unlockUserKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            if (this.mLockPatternUtils.isSecure(userId) && ArrayUtils.isEmpty(token)) {
                throw new IllegalStateException("Token required to unlock secure user " + userId);
            }
            try {
                Object[] objArr = new Object[5];
                objArr[0] = "unlock_user_key";
                objArr[1] = Integer.valueOf(userId);
                objArr[2] = Integer.valueOf(serialNumber);
                objArr[3] = encodeBytes(token);
                objArr[4] = encodeBytes(secret);
                this.mCryptConnector.execute("cryptfs", objArr);
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
        synchronized (this.mLock) {
            this.mLocalUnlockedUsers = ArrayUtils.appendInt(this.mLocalUnlockedUsers, userId);
        }
    }

    public void lockUserKey(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "lock_user_key";
            objArr[1] = Integer.valueOf(userId);
            this.mCryptConnector.execute("cryptfs", objArr);
            synchronized (this.mLock) {
                this.mLocalUnlockedUsers = ArrayUtils.removeInt(this.mLocalUnlockedUsers, userId);
            }
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public boolean isUserKeyUnlocked(int userId) {
        boolean contains;
        synchronized (this.mLock) {
            contains = ArrayUtils.contains(this.mLocalUnlockedUsers, userId);
        }
        return contains;
    }

    public void prepareUserStorage(String volumeUuid, int userId, int serialNumber, int flags) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            Object[] objArr = new Object[5];
            objArr[0] = "prepare_user_storage";
            objArr[1] = escapeNull(volumeUuid);
            objArr[2] = Integer.valueOf(userId);
            objArr[3] = Integer.valueOf(serialNumber);
            objArr[4] = Integer.valueOf(flags);
            this.mCryptConnector.execute("cryptfs", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void destroyUserStorage(String volumeUuid, int userId, int flags) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            Object[] objArr = new Object[4];
            objArr[0] = "destroy_user_storage";
            objArr[1] = escapeNull(volumeUuid);
            objArr[2] = Integer.valueOf(userId);
            objArr[3] = Integer.valueOf(flags);
            this.mCryptConnector.execute("cryptfs", objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public ParcelFileDescriptor mountAppFuse(final String name) throws RemoteException {
        try {
            final int uid = Binder.getCallingUid();
            final int pid = Binder.getCallingPid();
            Object[] objArr = new Object[4];
            objArr[0] = OppoProcessManager.RESUME_REASON_MOUNT_STR;
            objArr[1] = Integer.valueOf(uid);
            objArr[2] = Integer.valueOf(pid);
            objArr[3] = name;
            NativeDaemonEvent event = this.mConnector.execute("appfuse", objArr);
            if (event.getFileDescriptors() != null) {
                return ParcelFileDescriptor.fromFd(event.getFileDescriptors()[0], this.mHandler, new OnCloseListener() {
                    public void onClose(IOException e) {
                        try {
                            Object[] objArr = new Object[4];
                            objArr[0] = "unmount";
                            objArr[1] = Integer.valueOf(uid);
                            objArr[2] = Integer.valueOf(pid);
                            objArr[3] = name;
                            NativeDaemonEvent execute = MountService.this.mConnector.execute("appfuse", objArr);
                        } catch (NativeDaemonConnectorException e2) {
                            Log.e(MountService.TAG, "Failed to unmount appfuse.");
                        }
                    }
                });
            }
            throw new RemoteException("AppFuse FD from vold is null.");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        } catch (IOException e2) {
            throw new RemoteException(e2.getMessage());
        }
    }

    public int mkdirs(String callingPkg, String appPath) {
        if (mPanicDebug) {
            Slog.i(TAG, "mkdirs, callingPkg=" + callingPkg + ", appPath=" + appPath);
        }
        UserEnvironment userEnv = new UserEnvironment(UserHandle.getUserId(Binder.getCallingUid()));
        ((AppOpsManager) this.mContext.getSystemService("appops")).checkPackage(Binder.getCallingUid(), callingPkg);
        try {
            File appFile = new File(appPath).getCanonicalFile();
            if (FileUtils.contains(userEnv.buildExternalStorageAppDataDirs(callingPkg), appFile) || FileUtils.contains(userEnv.buildExternalStorageAppObbDirs(callingPkg), appFile) || FileUtils.contains(userEnv.buildExternalStorageAppMediaDirs(callingPkg), appFile)) {
                appPath = appFile.getAbsolutePath();
                if (!appPath.endsWith("/")) {
                    appPath = appPath + "/";
                }
                try {
                    NativeDaemonConnector nativeDaemonConnector = this.mConnector;
                    String str = TAG_VOLUME;
                    Object[] objArr = new Object[2];
                    objArr[0] = "mkdirs";
                    objArr[1] = appPath;
                    nativeDaemonConnector.execute(str, objArr);
                    return 0;
                } catch (NativeDaemonConnectorException e) {
                    return e.getCode();
                }
            }
            throw new SecurityException("Invalid mkdirs path: " + appFile);
        } catch (IOException e2) {
            Slog.e(TAG, "Failed to resolve " + appPath + ": " + e2);
            return -1;
        }
    }

    public StorageVolume[] getVolumeList(int uid, String packageName, int flags) {
        int userId = UserHandle.getUserId(uid);
        boolean forWrite = (flags & 256) != 0;
        boolean realState = (flags & 512) != 0;
        boolean includeInvisible = (flags & 1024) != 0;
        long token = Binder.clearCallingIdentity();
        try {
            boolean userKeyUnlocked = isUserKeyUnlocked(userId);
            boolean storagePermission = this.mMountServiceInternal.hasExternalStorage(uid, packageName);
            boolean foundPrimary = false;
            ArrayList<StorageVolume> res = new ArrayList();
            synchronized (this.mLock) {
                for (int i = 0; i < this.mVolumes.size(); i++) {
                    VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                    switch (vol.getType()) {
                        case 0:
                        case 2:
                            boolean match = forWrite ? vol.isVisibleForWrite(userId) : !vol.isVisibleForRead(userId) ? includeInvisible && vol.getPath() != null : true;
                            if (!match) {
                                break;
                            }
                            boolean reportUnmounted = false;
                            if (vol.getType() == 2 && !userKeyUnlocked) {
                                reportUnmounted = true;
                            } else if (!(storagePermission || realState)) {
                                reportUnmounted = true;
                            }
                            StorageVolume userVol = vol.buildStorageVolume(this.mContext, userId, reportUnmounted);
                            if (!vol.isPrimary()) {
                                res.add(userVol);
                                break;
                            }
                            res.add(0, userVol);
                            foundPrimary = true;
                            break;
                        default:
                            break;
                    }
                }
            }
            if (!foundPrimary) {
                Log.w(TAG, "No primary storage defined yet; hacking together a stub");
                boolean primaryPhysical = SystemProperties.getBoolean("ro.vold.primary_physical", false);
                String id = "stub_primary";
                boolean removable = primaryPhysical;
                String state = "removed";
                res.add(0, new StorageVolume("stub_primary", 0, Environment.getLegacyExternalStorageDirectory(), this.mContext.getString(17039374), true, primaryPhysical, !primaryPhysical, 0, false, 0, new UserHandle(userId), null, "removed"));
            }
            return (StorageVolume[]) res.toArray(new StorageVolume[res.size()]);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public DiskInfo[] getDisks() {
        DiskInfo[] res;
        synchronized (this.mLock) {
            res = new DiskInfo[this.mDisks.size()];
            for (int i = 0; i < this.mDisks.size(); i++) {
                res[i] = (DiskInfo) this.mDisks.valueAt(i);
            }
        }
        return res;
    }

    public VolumeInfo[] getVolumes(int flags) {
        VolumeInfo[] res;
        synchronized (this.mLock) {
            res = new VolumeInfo[this.mVolumes.size()];
            for (int i = 0; i < this.mVolumes.size(); i++) {
                res[i] = (VolumeInfo) this.mVolumes.valueAt(i);
            }
        }
        return res;
    }

    public VolumeRecord[] getVolumeRecords(int flags) {
        VolumeRecord[] res;
        synchronized (this.mLock) {
            res = new VolumeRecord[this.mRecords.size()];
            for (int i = 0; i < this.mRecords.size(); i++) {
                res[i] = (VolumeRecord) this.mRecords.valueAt(i);
            }
        }
        return res;
    }

    private void addObbStateLocked(ObbState obbState) throws RemoteException {
        IBinder binder = obbState.getBinder();
        List<ObbState> obbStates = (List) this.mObbMounts.get(binder);
        if (obbStates == null) {
            obbStates = new ArrayList();
            this.mObbMounts.put(binder, obbStates);
        } else {
            for (ObbState o : obbStates) {
                if (o.rawPath.equals(obbState.rawPath)) {
                    throw new IllegalStateException("Attempt to add ObbState twice. This indicates an error in the MountService logic.");
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

    private void removeObbStateLocked(ObbState obbState) {
        IBinder binder = obbState.getBinder();
        List<ObbState> obbStates = (List) this.mObbMounts.get(binder);
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

    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ", 160);
        synchronized (this.mLock) {
            int i;
            pw.println("Disks:");
            pw.increaseIndent();
            for (i = 0; i < this.mDisks.size(); i++) {
                ((DiskInfo) this.mDisks.valueAt(i)).dump(pw);
            }
            pw.decreaseIndent();
            pw.println();
            pw.println("Volumes:");
            pw.increaseIndent();
            for (i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                if (!"private".equals(vol.id)) {
                    vol.dump(pw);
                }
            }
            pw.decreaseIndent();
            pw.println();
            pw.println("Records:");
            pw.increaseIndent();
            for (i = 0; i < this.mRecords.size(); i++) {
                ((VolumeRecord) this.mRecords.valueAt(i)).dump(pw);
            }
            pw.decreaseIndent();
            pw.println();
            pw.println("Primary storage UUID: " + this.mPrimaryStorageUuid);
            pw.println("Force adoptable: " + this.mForceAdoptable);
            pw.println();
            pw.println("Local unlocked users: " + Arrays.toString(this.mLocalUnlockedUsers));
            pw.println("System unlocked users: " + Arrays.toString(this.mSystemUnlockedUsers));
        }
        synchronized (this.mObbMounts) {
            pw.println();
            pw.println("mObbMounts:");
            pw.increaseIndent();
            for (Entry<IBinder, List<ObbState>> e : this.mObbMounts.entrySet()) {
                pw.println(e.getKey() + ":");
                pw.increaseIndent();
                for (ObbState obbState : (List) e.getValue()) {
                    pw.println(obbState);
                }
                pw.decreaseIndent();
            }
            pw.decreaseIndent();
            pw.println();
            pw.println("mObbPathToStateMap:");
            pw.increaseIndent();
            for (Entry<String, ObbState> e2 : this.mObbPathToStateMap.entrySet()) {
                pw.print((String) e2.getKey());
                pw.print(" -> ");
                pw.println(e2.getValue());
            }
            pw.decreaseIndent();
        }
        pw.println();
        pw.println("mConnector:");
        pw.increaseIndent();
        this.mConnector.dump(fd, pw, args);
        pw.decreaseIndent();
        pw.println();
        pw.println("mCryptConnector:");
        pw.increaseIndent();
        this.mCryptConnector.dump(fd, pw, args);
        pw.decreaseIndent();
        pw.println();
        pw.print("Last maintenance: ");
        pw.println(TimeUtils.formatForLogging(this.mLastMaintenance));
    }

    public void monitor() {
        if (this.mConnector != null) {
            this.mConnector.monitor();
        }
        if (this.mCryptConnector != null) {
            this.mCryptConnector.monitor();
        }
    }

    private void initMTKFeature() {
        registerDMAPPReceiver();
        registerPrivacyProtectionReceiver();
        registerUsbStateReceiver();
        registerBootIPOReceiver();
    }

    private void popFormatFailToast() {
        this.mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(MountService.this.mContext, MountService.this.mContext.getString(134545638), 1).show();
            }
        });
    }

    private void waitMtkLogStopped() {
        if (mPanicDebug) {
            Slog.i(TAG, "waitMtkLogStopped...");
        }
        VolumeInfo emulatedVolume = null;
        synchronized (this.mLock) {
            for (int i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                if (vol.getType() == 2 && vol.getState() == 2) {
                    emulatedVolume = vol;
                    break;
                }
            }
        }
        if (emulatedVolume == null) {
            if (mPanicDebug) {
                Slog.i(TAG, "cannot find emulated volume, return");
            }
            return;
        }
        StorageVolume userVol = emulatedVolume.buildStorageVolume(this.mContext, this.mCurrentUserId, false);
        Intent intent = new Intent("android.intent.action.MEDIA_EJECT", Uri.fromFile(userVol.getPathFile()));
        intent.putExtra("android.os.storage.extra.STORAGE_VOLUME", userVol);
        intent.addFlags(67108864);
        if (mPanicDebug) {
            Slog.i(TAG, "sendBroadcastAsUser, intent=" + intent + ", userVol=" + userVol);
        }
        this.mContext.sendBroadcastAsUser(intent, userVol.getOwner());
        int tryCount = 0;
        String netlog = "debug.mtklog.netlog.Running";
        String mdlogger = "debug.mdlogger.Running";
        String MBlog = "debug.MB.running";
        String GPSlog = "debug.gpsdbglog.enable";
        while (true) {
            if (SystemProperties.get("debug.mtklog.netlog.Running", "0").equals("0") && SystemProperties.get("debug.mdlogger.Running", "0").equals("0") && SystemProperties.get("debug.MB.running", "0").equals("0") && SystemProperties.get("debug.gpsdbglog.enable", "0").equals("0")) {
                break;
            } else if (tryCount == 60) {
                Slog.i(TAG, "try count = 60, break");
                break;
            } else {
                try {
                    if (mPanicDebug) {
                        Slog.i(TAG, "debug.mtklog.netlog.Running=" + SystemProperties.get("debug.mtklog.netlog.Running"));
                        Slog.i(TAG, "debug.mdlogger.Running=" + SystemProperties.get("debug.mdlogger.Running"));
                        Slog.i(TAG, "debug.MB.running=" + SystemProperties.get("debug.MB.running"));
                        Slog.i(TAG, "debug.gpsdbglog.enable=" + SystemProperties.get("debug.gpsdbglog.enable"));
                    }
                    Thread.sleep(500);
                    tryCount++;
                } catch (Exception e) {
                }
            }
        }
        if (tryCount != 60) {
            try {
                Thread.sleep(3000);
            } catch (Exception e2) {
            }
        }
        if (mPanicDebug) {
            Slog.i(TAG, "waitMtkLogStopped done");
        }
    }

    public void setDefaultPath(String path) {
        if (path == null) {
            if (mPanicDebug) {
                Slog.e(TAG, "setDefaultPath error! path=null");
            }
            return;
        }
        try {
            SystemProperties.set("persist.sys.sd.defaultpath", path);
            if (mPanicDebug) {
                Slog.e(TAG, "setDefaultPath new path=" + path);
            }
        } catch (IllegalArgumentException e) {
            if (mPanicDebug) {
                Slog.e(TAG, "IllegalArgumentException when set default path:", e);
            }
        }
    }

    private void updateDefaultPathForUserSwitch() {
        if (mPanicDebug) {
            Slog.i(TAG, "updateDefaultPathForUserSwitch");
        }
        String defaultPath = getDefaultPath();
        if (mPanicDebug) {
            Slog.i(TAG, "current default path = " + defaultPath);
        }
        if (defaultPath.contains("emulated")) {
            defaultPath = "/storage/emulated/" + this.mCurrentUserId;
            if (mPanicDebug) {
                Slog.i(TAG, "change default path to " + defaultPath);
            }
            setDefaultPath(defaultPath);
            return;
        }
        updateDefaultPathIfNeed();
    }

    private void updateDefaultPathIfNeed() {
        if (mPanicDebug) {
            Slog.i(TAG, "updateDefaultPathIfNeed");
        }
        if (isBootingPhase) {
            if (mPanicDebug) {
                Slog.i(TAG, "In booting phase, don't update default path");
            }
        } else if (isIPOBooting) {
            if (mPanicDebug) {
                Slog.i(TAG, "In IPO booting phase, don't update default path");
            }
        } else if (isShuttingDown) {
            if (mPanicDebug) {
                Slog.i(TAG, "In shutting down, don't update default path");
            }
        } else {
            int i;
            VolumeInfo vol;
            String defaultPath = getDefaultPath();
            if (mPanicDebug) {
                Slog.i(TAG, "current default path = " + defaultPath);
            }
            String newPath = IElsaManager.EMPTY_PACKAGE;
            boolean needChange = false;
            boolean isFindCurrentDefaultPathVolume = false;
            int userId = this.mCurrentUserId;
            synchronized (this.mLock) {
                for (i = 0; i < this.mVolumes.size(); i++) {
                    vol = (VolumeInfo) this.mVolumes.valueAt(i);
                    File pathFile = vol.getPathForUser(userId);
                    if (pathFile != null && pathFile.getAbsolutePath().equals(defaultPath)) {
                        Slog.i(TAG, "find default path volume= " + vol);
                        isFindCurrentDefaultPathVolume = true;
                        if (vol.getState() != 2) {
                            if (mPanicDebug) {
                                Slog.i(TAG, "old default path is not mounted");
                            }
                            needChange = true;
                        } else if (!vol.isVisibleForWrite(userId)) {
                            if (mPanicDebug) {
                                Slog.i(TAG, "old default path is not visible for write, userId=" + userId);
                            }
                            needChange = true;
                        } else if (mPanicDebug) {
                            Slog.i(TAG, "old default path is visible for write, userId=" + userId);
                        }
                    }
                }
            }
            if (needChange || !isFindCurrentDefaultPathVolume) {
                Slog.i(TAG, "need change default path " + defaultPath);
                synchronized (this.mLock) {
                    i = 0;
                    while (i < this.mVolumes.size()) {
                        vol = (VolumeInfo) this.mVolumes.valueAt(i);
                        if (vol.getState() == 2 && vol.isVisibleForWrite(userId)) {
                            newPath = vol.getPathForUser(userId).getAbsolutePath();
                            if (mPanicDebug) {
                                Slog.i(TAG, "updateDefaultPathIfNeed from " + defaultPath + " to " + newPath);
                            }
                            setDefaultPath(newPath);
                            popDefaultPathChangedToast();
                        } else {
                            i++;
                        }
                    }
                }
                if (newPath.equals(IElsaManager.EMPTY_PACKAGE) && mPanicDebug) {
                    Slog.i(TAG, "not find mounted and visible volume, keep old default path:" + defaultPath);
                }
            } else if (mPanicDebug) {
                Slog.i(TAG, "no need change default path, keep default path:" + defaultPath);
            }
        }
    }

    private String getDefaultPath() {
        StorageManagerEx sm = new StorageManagerEx();
        return StorageManagerEx.getDefaultPath();
    }

    private boolean isShowDefaultPathDialog(VolumeInfo curVol) {
        boolean z = true;
        if (mPanicDebug) {
            Slog.i(TAG, "isShowDefaultPathDialog, curVol=" + curVol);
        }
        if (curVol == null) {
            if (mPanicDebug) {
                Slog.i(TAG, "curVolume is null, skip it.");
            }
            return false;
        } else if (isBootingPhase) {
            if (mPanicDebug) {
                Slog.i(TAG, "in booting phase, not show defaultPathDialog, skip it.");
            }
            return false;
        } else if (isIPOBooting) {
            if (mPanicDebug) {
                Slog.i(TAG, "in IPO booting phase, not show defaultPathDialog, skip it.");
            }
            return false;
        } else if (curVol.getState() != 2 && curVol.getState() != 1) {
            if (mPanicDebug) {
                Slog.i(TAG, "this volume state is not mounted/checking, skip it.");
            }
            return false;
        } else if (this.isDiskInsert) {
            this.isDiskInsert = false;
            int mountCount = 0;
            if (!"file".equalsIgnoreCase(SystemProperties.get("ro.crypto.type", IElsaManager.EMPTY_PACKAGE)) && SystemProperties.get(PROP_VOLD_DECRYPT).equals("trigger_restart_min_framework")) {
                if (mPanicDebug) {
                    Slog.i(TAG, "PROP_VOLD_DECRYPT=trigger_restart_min_framework, return false");
                }
                return false;
            } else if (curVol.getType() != 2 || curVol.getDiskId() == null) {
                int userId = this.mCurrentUserId;
                synchronized (this.mLock) {
                    for (int i = 0; i < this.mVolumes.size(); i++) {
                        VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                        if (vol.getState() == 2 && vol.isVisibleForWrite(userId)) {
                            Slog.i(TAG, "find a visibe & mounted volume, volumeId=" + vol.getId());
                            mountCount++;
                        }
                    }
                    if (mPanicDebug) {
                        Slog.i(TAG, "mount and visible volumes count=" + mountCount);
                    }
                }
                if (mountCount <= 1) {
                    z = false;
                }
                return z;
            } else {
                if (mPanicDebug) {
                    Slog.i(TAG, "isShowDefaultPathDialog, emulated volume, return false");
                }
                return false;
            }
        } else {
            if (mPanicDebug) {
                Slog.i(TAG, "not disk insert, no need show dialog, return false");
            }
            return false;
        }
    }

    private void popDefaultPathChangedToast() {
        this.mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(MountService.this.mContext, MountService.this.mContext.getString(134545529), 1).show();
            }
        });
    }

    private void showDefaultPathDialog(VolumeInfo vol) {
        if (mPanicDebug) {
            Slog.i(TAG, "showDefaultPathDialog, vol=" + vol);
        }
        int userId = this.mCurrentUserId;
        if (vol.isVisibleForWrite(userId)) {
            Intent intent = new Intent("com.mediatek.storage.StorageDefaultPathDialog");
            intent.setFlags(268435456);
            if (vol.isUSBOTG()) {
                intent.putExtra(INSERT_OTG, true);
            }
            this.mContext.startActivityAsUser(intent, new UserHandle(userId));
            return;
        }
        if (mPanicDebug) {
            Slog.i(TAG, "showDefaultPathDialog,but vol is not visible to userID=" + userId + ", volumeInfo=" + vol);
        }
    }

    private void registerDMAPPReceiver() {
        if (SystemProperties.get(PROP_DM_APP).equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            IntentFilter DMFilter = new IntentFilter();
            DMFilter.addAction("com.mediatek.dm.LAWMO_UNLOCK");
            DMFilter.addAction("com.mediatek.dm.LAWMO_LOCK");
            DMFilter.addAction(OMADM_SD_FORMAT);
            this.mContext.registerReceiver(this.mDMReceiver, DMFilter, null, this.mHandler);
        }
    }

    private void enableUSBFuction(boolean enable) {
        waitForReady();
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "USB";
            Object[] objArr = new Object[1];
            objArr[0] = enable ? "enable" : "disable";
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "enableUSBFunction failed, ", e);
        }
    }

    private void registerPrivacyProtectionReceiver() {
        IntentFilter privacyProtectionFilter = new IntentFilter();
        privacyProtectionFilter.addAction("com.mediatek.ppl.NOTIFY_LOCK");
        privacyProtectionFilter.addAction("com.mediatek.ppl.NOTIFY_UNLOCK");
        privacyProtectionFilter.addAction(PRIVACY_PROTECTION_WIPE);
        this.mContext.registerReceiver(this.mPrivacyProtectionReceiver, privacyProtectionFilter, null, this.mHandler);
    }

    private ArrayList<VolumeInfo> findVolumeListNeedFormat() {
        Slog.i(TAG, "findVolumeListNeedFormat");
        ArrayList<VolumeInfo> tempVolumes = Lists.newArrayList();
        synchronized (this.mLock) {
            for (int i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                if ((!vol.isUSBOTG() && vol.isVisible() && vol.getType() == 0) || (vol.getType() == 1 && vol.getDiskId() != null)) {
                    tempVolumes.add(vol);
                    if (mPanicDebug) {
                        Slog.i(TAG, "i will try to format volume= " + vol);
                    }
                }
            }
        }
        return tempVolumes;
    }

    private void formatPhoneStorageAndExternalSDCard() {
        final ArrayList<VolumeInfo> tempVolumes = findVolumeListNeedFormat();
        new Thread() {
            public void run() {
                synchronized (MountService.FORMAT_LOCK) {
                    int userId = MountService.this.mCurrentUserId;
                    for (int i = 0; i < tempVolumes.size(); i++) {
                        VolumeInfo vol = (VolumeInfo) tempVolumes.get(i);
                        if (vol.getType() != 1 || vol.getDiskId() == null) {
                            int j;
                            if (vol.getState() == 1) {
                                if (MountService.mPanicDebug) {
                                    Slog.i(MountService.TAG, "volume is checking, wait..");
                                }
                                j = 0;
                                while (j < 30) {
                                    try {
                                        AnonymousClass10.sleep(1000);
                                    } catch (InterruptedException ex) {
                                        Slog.e(MountService.TAG, "Exception when wait!", ex);
                                    }
                                    if (vol.getState() == 1) {
                                        j++;
                                    } else if (MountService.mPanicDebug) {
                                        Slog.i(MountService.TAG, "volume wait checking done!");
                                    }
                                }
                            }
                            if (vol.getState() == 2) {
                                if (MountService.mPanicDebug) {
                                    Slog.i(MountService.TAG, "volume is mounted, unmount firstly, volume=" + vol);
                                }
                                MountService.this.unmount(vol.getId());
                                j = 0;
                                while (j < 30) {
                                    try {
                                        AnonymousClass10.sleep(1000);
                                    } catch (InterruptedException ex2) {
                                        Slog.e(MountService.TAG, "Exception when wait!", ex2);
                                    }
                                    if (vol.getState() != 0) {
                                        j++;
                                    } else if (MountService.mPanicDebug) {
                                        Slog.i(MountService.TAG, "wait unmount done!");
                                    }
                                }
                            }
                            if (vol.getState() == 9) {
                                if (MountService.mPanicDebug) {
                                    Slog.i(MountService.TAG, "volume is shared, unshared firstly volume=" + vol);
                                }
                                MountService.this.doShareUnshareVolume(vol.getId(), false);
                                j = 0;
                                while (j < 30) {
                                    try {
                                        AnonymousClass10.sleep(1000);
                                    } catch (InterruptedException ex22) {
                                        Slog.e(MountService.TAG, "Exception when wait!", ex22);
                                    }
                                    if (vol.getState() != 0) {
                                        j++;
                                    } else if (MountService.mPanicDebug) {
                                        Slog.i(MountService.TAG, "wait unshare done!");
                                    }
                                }
                            }
                            MountService.this.format(vol.getId());
                            if (MountService.mPanicDebug) {
                                Slog.d(MountService.TAG, "format Succeed! volume=" + vol);
                            }
                        } else {
                            if (MountService.mPanicDebug) {
                                Slog.i(MountService.TAG, "use partition public to format, volume= " + vol);
                            }
                            MountService.this.partitionPublic(vol.getDiskId());
                            if (vol.getFsUuid() != null) {
                                MountService.this.forgetVolume(vol.getFsUuid());
                            }
                        }
                    }
                    Intent intent = new Intent(MountService.PRIVACY_PROTECTION_WIPE_DONE);
                    MountService.this.mContext.sendBroadcast(intent);
                    if (MountService.mPanicDebug) {
                        Slog.d(MountService.TAG, "Privacy Protection wipe: send " + intent);
                    }
                }
            }
        }.start();
    }

    private void registerUsbStateReceiver() {
        this.mContext.registerReceiver(this.mUsbReceiver, new IntentFilter("android.hardware.usb.action.USB_STATE"), null, this.mHandler);
    }

    private void notifyShareAvailabilityChange(boolean isConnected) {
        if (mPanicDebug) {
            Slog.i(TAG, "notifyShareAvailabilityChange, isConnected=" + isConnected);
        }
        this.mCallbacks.onUsbMassStorageConnectionChanged(isConnected);
        if (this.mSystemReady) {
            sendUmsIntent(isConnected);
        } else {
            this.mSendUmsConnectedOnBoot = isConnected;
        }
        if (!isConnected) {
            boolean needTurnOff = false;
            if (this.mIsTurnOnOffUsb) {
                needTurnOff = true;
            } else {
                synchronized (this.mLock) {
                    for (int i = 0; i < this.mVolumes.size(); i++) {
                        if (((VolumeInfo) this.mVolumes.valueAt(i)).getState() == 9) {
                            needTurnOff = true;
                            break;
                        }
                    }
                }
            }
            if (needTurnOff) {
                new Thread("MountService#turnOffUMS") {
                    public void run() {
                        synchronized (MountService.TURNONUSB_SYNC_LOCK) {
                            MountService.this.setUsbMassStorageEnabled(false);
                        }
                    }
                }.start();
            }
        }
    }

    private void doShareUnshareVolume(String volId, boolean enable) {
        if (mPanicDebug) {
            Slog.i(TAG, "doShareUnshareVolume, volId=" + volId + ", enable=" + enable);
        }
        VolumeInfo vol = findVolumeByIdOrThrow(volId);
        Slog.i(TAG, "doShareUnshareVolume, find volumeInfo=" + vol);
        if (vol.getType() == 2) {
            if (mPanicDebug) {
                Slog.i(TAG, "emulated storage no need to share/unshare");
            }
            return;
        }
        try {
            String str;
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str2 = TAG_VOLUME;
            Object[] objArr = new Object[3];
            if (enable) {
                str = "share";
            } else {
                str = "unshare";
            }
            objArr[0] = str;
            objArr[1] = volId;
            objArr[2] = "ums";
            nativeDaemonConnector.execute(str2, objArr);
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to share/unshare", e);
        }
    }

    private boolean getUmsEnabling() {
        return this.mUmsEnabling;
    }

    private void setUmsEnabling(boolean enable) {
        this.mUmsEnabling = enable;
    }

    private void sendUmsIntent(boolean c) {
        this.mContext.sendBroadcastAsUser(new Intent(c ? "android.intent.action.UMS_CONNECTED" : "android.intent.action.UMS_DISCONNECTED"), UserHandle.ALL);
    }

    private boolean isVolumeSharedEnable(VolumeInfo vol) {
        if (vol.isAllowUsbMassStorage(this.mCurrentUserId)) {
            boolean result = doGetVolumeShared(vol.getId());
            if (mPanicDebug) {
                Slog.i(TAG, "isVolumeSharedEnable return " + result);
            }
            return result;
        }
        if (mPanicDebug) {
            Slog.i(TAG, "not able to shared Volume=" + vol);
        }
        return false;
    }

    private boolean doGetVolumeShared(String volId) {
        if (mPanicDebug) {
            Slog.i(TAG, "doGetVolumeShared volId=" + volId);
        }
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = TAG_VOLUME;
            Object[] objArr = new Object[3];
            objArr[0] = "shared";
            objArr[1] = volId;
            objArr[2] = "ums";
            NativeDaemonEvent event = nativeDaemonConnector.execute(str, objArr);
            if (event.getCode() == 212) {
                return event.getMessage().endsWith("enabled");
            }
            return false;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to read response to volume shared " + volId + " ums");
            return false;
        }
    }

    private void validateUserRestriction(String restriction) {
        UserManager um = (UserManager) this.mContext.getSystemService("user");
        if (um != null && um.hasUserRestriction(restriction, Binder.getCallingUserHandle())) {
            throw new SecurityException("User has restriction " + restriction);
        }
    }

    private void registerBootIPOReceiver() {
        IntentFilter bootIPOFilter = new IntentFilter();
        bootIPOFilter.addAction("android.intent.action.ACTION_BOOT_IPO");
        this.mContext.registerReceiver(this.mBootIPOReceiver, bootIPOFilter, null, this.mHandler);
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0050 A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void waitAllVolumeMounted() {
        try {
            if (mPanicDebug) {
                Slog.d(TAG, "waitAllVolumeMounted when ipo startup");
            }
            int retryCount = 0;
            while (retryCount < 10) {
                boolean isNeedWait = false;
                VolumeInfo[] volumes = getVolumes(0);
                for (VolumeInfo vol : volumes) {
                    if ((vol.getType() == 0 || vol.getType() == 2) && vol.isVisibleForWrite(this.mCurrentUserId) && vol.getState() != 2) {
                        if (mPanicDebug) {
                            Slog.i(TAG, "volume is not mounted, wait...");
                        }
                        isNeedWait = true;
                        retryCount++;
                        Thread.sleep(1000);
                        continue;
                        if (!isNeedWait) {
                            if (mPanicDebug) {
                                Slog.i(TAG, "all visible volume is mounted");
                                return;
                            }
                            return;
                        }
                    }
                }
                if (isNeedWait) {
                }
            }
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0039  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void waitMTKNetlogStopped() {
        if (mPanicDebug) {
            Slog.i(TAG, "waitMTKNetlogStopped...");
        }
        int tryCount = 0;
        String netlog = "debug.mtklog.netlog.Running";
        while (!SystemProperties.get("debug.mtklog.netlog.Running", "0").equals("0")) {
            if (tryCount == 60) {
                if (mPanicDebug) {
                    Slog.i(TAG, "try count = 60, break");
                }
                if (!mPanicDebug) {
                    Slog.i(TAG, "waitMTKNetlogStopped done");
                    return;
                }
                return;
            }
            try {
                if (mPanicDebug) {
                    Slog.i(TAG, "debug.mtklog.netlog.Running=" + SystemProperties.get("debug.mtklog.netlog.Running"));
                }
                Thread.sleep(500);
                tryCount++;
            } catch (Exception e) {
            }
        }
        if (!mPanicDebug) {
        }
    }

    private void sendEncryptionTypeIntent() {
        this.mContext.sendBroadcastAsUser(new Intent(ACTION_ENCRYPTION_TYPE_CHANGED), UserHandle.ALL);
    }

    public boolean isSetPrimaryStorageUuidFinished() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mMoveCallback == null;
        }
        return z;
    }
}
