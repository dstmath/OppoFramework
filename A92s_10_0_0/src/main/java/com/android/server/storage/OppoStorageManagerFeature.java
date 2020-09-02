package com.android.server.storage;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ProviderInfo;
import android.content.pm.UserInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.OppoBaseEnvironment;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.DiskInfo;
import android.os.storage.OppoBaseVolumeInfo;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.util.Slog;
import com.android.server.IOppoStorageManagerCallback;
import com.android.server.IOppoStorageManagerFeature;
import com.android.server.LocalServices;
import com.color.util.ColorTypeCastingHelper;
import com.oppo.hypnus.Hypnus;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class OppoStorageManagerFeature implements IOppoStorageManagerFeature {
    private static final String COPY_OF_ACTION_MEDIA_MOUNTED_RO = "android.intent.action.MEDIA_MOUNTED_RO";
    private static final int DPOLICY_BALANCE = 4;
    private static final int DPOLICY_BG = 0;
    private static final int DPOLICY_PERFORMANCE = 5;
    protected static final int H_DPOLICY_EXPECT = 15;
    protected static final int H_FSYNC_PROTECT = 16;
    private static final int H_VOLUME_BROADCAST_STORAGE_RO = 61;
    private static final int H_VOLUME_CHECK = 60;
    private static final String TAG = "OppoStorageManagerFeature";
    private static Hypnus mHypnus = null;
    private static boolean mOppoDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static OppoStorageManagerFeature sOppoStorageManagerFeature = null;
    private Handler mAospStorageManagerHandler;
    private Context mContext;
    private boolean mIsInited;
    private boolean mIsStartUser;
    private PackageManagerInternal mPmInternal;
    private IOppoStorageManagerCallback mStorageManagerCallback;

    private OppoStorageManagerFeature() {
        this.mContext = null;
        this.mAospStorageManagerHandler = null;
        this.mPmInternal = null;
        this.mStorageManagerCallback = null;
        this.mIsStartUser = false;
        this.mIsInited = false;
    }

    private static class InstanceHolder {
        static final OppoStorageManagerFeature INSTANCE = new OppoStorageManagerFeature();

        private InstanceHolder() {
        }
    }

    public static OppoStorageManagerFeature getInstance(Context context) {
        if (mOppoDebug) {
            Slog.d(TAG, "getInstance.");
        }
        OppoStorageManagerFeature instance = InstanceHolder.INSTANCE;
        instance.init(context);
        return instance;
    }

    private void init(Context context) {
        if (!this.mIsInited) {
            if (context == null) {
                Slog.e(TAG, "failed to init for null context!");
                return;
            }
            this.mContext = context;
            if (mOppoDebug) {
                Slog.d(TAG, "OppoStorageManagerFeature init.");
            }
            SystemProperties.set("persist.sys.sd_shutdown", "false");
            SystemProperties.set("persist.sys.pending.list", "false");
            this.mIsInited = true;
        }
    }

    public void setStorageManagerHandler(Handler handler) {
        if (mOppoDebug) {
            Slog.d(TAG, "impl setStorageManagerHandler");
        }
        this.mAospStorageManagerHandler = handler;
    }

    public void setOppoStorageManagerCallback(IOppoStorageManagerCallback callback) {
        if (mOppoDebug) {
            Slog.d(TAG, "impl setOppoStorageManagerCallback");
        }
        this.mStorageManagerCallback = callback;
    }

    public boolean shouldHandleKeyguardStateChange(boolean isSecureKeyguardShowing) {
        if (mOppoDebug) {
            Slog.d(TAG, "impl shouldHandleKeyguardStateChange, isSecureKeyguardShowing:" + isSecureKeyguardShowing);
        }
        SystemProperties.set("persist.sys.securekeyguard.isShowing", isSecureKeyguardShowing ? "true" : "false");
        if (!SystemProperties.getBoolean("persist.sys.pending.list", false)) {
            if (mOppoDebug) {
                Slog.i(TAG, "shouldHandleKeyguardStateChange, do not have pending list");
            }
            return false;
        } else if (!mOppoDebug) {
            return true;
        } else {
            Slog.i(TAG, "shouldHandleKeyguardStateChange, have pending list");
            return true;
        }
    }

    public boolean changeVolumeReadOnlyStateLocked(VolumeInfo vol, int newState, int unlockedUsersSize) {
        if (vol == null) {
            Slog.e(TAG, "impl changeVolumeReadOnlyStateLocked: illegal vol");
            return false;
        }
        if (mOppoDebug) {
            Slog.i(TAG, "impl changeVolumeReadOnlyStateLocked volId=" + vol.getId() + "newState=" + newState + ", VolumeInfo.STATE_MOUNTED_READ_ONLY=" + 3);
        }
        if (newState == 3) {
            setReadonlyTypeForVolumeInfo(vol, 1);
            if (unlockedUsersSize > 0) {
                if (this.mStorageManagerCallback != null) {
                    for (int index = 0; index < unlockedUsersSize; index++) {
                        int userId = this.mStorageManagerCallback.getSystemUnlockedUserIdByIndexLocked(index);
                        if (vol.isVisibleForRead(userId)) {
                            StorageVolume userVol = vol.buildStorageVolume(this.mContext, userId, false);
                            Handler handler = this.mAospStorageManagerHandler;
                            if (handler != null) {
                                handler.obtainMessage(H_VOLUME_BROADCAST_STORAGE_RO, userVol).sendToTarget();
                            } else {
                                Slog.e(TAG, "changeVolumeReadOnlyStateLocked: fatal exception with handler uninit");
                            }
                        }
                    }
                } else {
                    Slog.e(TAG, "mpl changeVolumeReadOnlyStateLocked: fatal exception for callback uninit!");
                }
            }
            return true;
        }
        if (newState == 2) {
            setReadonlyTypeForVolumeInfo(vol, 0);
        } else {
            setReadonlyTypeForVolumeInfo(vol, -1);
        }
        return false;
    }

    public boolean shouldNotifyVolumeStateChanged(String newStateEnv, int userId, VolumeInfo vol) {
        if (mOppoDebug) {
            Slog.i(TAG, "impl shouldNotifyVolumeStateChanged volId=" + vol.getId() + ", userId=" + userId);
        }
        if ((!Objects.equals(newStateEnv, "unmounted") && !Objects.equals(newStateEnv, "mounted") && !Objects.equals(newStateEnv, "ejecting") && !Objects.equals(newStateEnv, "bad_removal")) || vol.getPathForUser(userId) != null) {
            return true;
        }
        if (!mOppoDebug) {
            return false;
        }
        Slog.d(TAG, "volume path is null, will not send brocast, volumeInfo=" + vol);
        return false;
    }

    public void onVolumeCheckingLocked(VolumeInfo vol, int currentUserId) {
        if (vol == null) {
            Slog.e(TAG, "impl onVolumeCheckingLocked:illegal vol");
            return;
        }
        if (mOppoDebug) {
            Slog.d(TAG, "impl onVolumeCheckingLocked start id=" + vol.getId());
        }
        speedupForIOPerformance(12, 10000);
        vol.mountFlags |= 2;
        vol.mountUserId = currentUserId;
        PackageManagerInternal pkgMgr = getPackageManagerInternal();
        if (pkgMgr == null || !pkgMgr.isOnlyCoreApps()) {
            Handler handler = this.mAospStorageManagerHandler;
            if (handler != null) {
                handler.obtainMessage(H_VOLUME_CHECK, vol).sendToTarget();
            } else {
                Slog.e(TAG, "impl onVolumeCheckingLocked:fatal exception for handler uninit.");
            }
        } else if (mOppoDebug) {
            Slog.d(TAG, "System booted in core-only mode; ignoring volume id=" + vol.getId());
        }
    }

    public void onUnlockUser(int userId) {
        if (mOppoDebug) {
            Slog.d(TAG, "impl onUnlockUser");
        }
        this.mIsStartUser = true;
    }

    public boolean onStorageManagerMessageHandle(Message msg) {
        if (msg == null) {
            return true;
        }
        if (mOppoDebug) {
            Slog.d(TAG, "impl onStorageManagerMessageHandle, msg: " + msg.what);
        }
        int i = msg.what;
        if (i == 3) {
            return onMessageShutdown();
        }
        if (i == 4) {
            return onMessageFStrim();
        }
        if (i == DPOLICY_PERFORMANCE) {
            return onMessageVolumeMount(msg);
        }
        if (i == H_DPOLICY_EXPECT) {
            onMessageDPolicyExpect(msg);
            return false;
        } else if (i == H_FSYNC_PROTECT) {
            onMessageFsyncProtect(msg);
            return false;
        } else if (i == H_VOLUME_CHECK) {
            return onMessageVolumeCheck(msg);
        } else {
            if (i != H_VOLUME_BROADCAST_STORAGE_RO) {
                return true;
            }
            return onMessageVolumeBroadcastStorageRO(msg);
        }
    }

    public void onDiskStateChangedLocked(DiskInfo disk, int volumesSize, int unlockedUsersSize) {
        DiskInfo diskInfo;
        if (mOppoDebug) {
            Slog.d(TAG, "impl onDiskStateChanged");
        }
        if (disk == null) {
            Slog.e(TAG, "impl onDiskStateChanged: illegal disk!");
        } else if (this.mStorageManagerCallback == null) {
            Slog.e(TAG, "impl onDiskStateChanged: fatal exception for callback uninit!!");
        } else {
            boolean found = false;
            VolumeInfo voltmp = null;
            if (volumesSize > 0) {
                for (int i = 0; i < volumesSize; i++) {
                    VolumeInfo vol = this.mStorageManagerCallback.getVolumeInfoByIndexLocked(i);
                    if (!(vol == null || (diskInfo = vol.getDisk()) == null || diskInfo != disk)) {
                        if (vol.state == 2) {
                            setReadonlyTypeForVolumeInfo(vol, 2);
                            if (!found) {
                                voltmp = vol;
                            }
                            found = true;
                        } else {
                            setReadonlyTypeForVolumeInfo(vol, -1);
                        }
                    }
                }
            }
            if (found && unlockedUsersSize > 0) {
                for (int index = 0; index < unlockedUsersSize; index++) {
                    int userId = this.mStorageManagerCallback.getSystemUnlockedUserIdByIndexLocked(index);
                    if (voltmp.isVisibleForRead(userId)) {
                        StorageVolume userVol = voltmp.buildStorageVolume(this.mContext, userId, false);
                        Handler handler = this.mAospStorageManagerHandler;
                        if (handler != null) {
                            handler.obtainMessage(H_VOLUME_BROADCAST_STORAGE_RO, userVol).sendToTarget();
                        } else {
                            Slog.e(TAG, "impl onDiskStateChanged: fatal exception for hanlder uninit!");
                        }
                    }
                }
            }
        }
    }

    private boolean onMessageFStrim() {
        if (!OppoBaseEnvironment.isWhiteListMcp()) {
            return true;
        }
        int battery = ((BatteryManager) this.mContext.getSystemService("batterymanager")).getIntProperty(4);
        if (mOppoDebug) {
            Slog.i(TAG, "check battery=" + battery + " before do fstrim");
        }
        if (battery >= H_DPOLICY_EXPECT || !mOppoDebug) {
            return true;
        }
        Slog.i(TAG, "check battery, too low battery, return");
        return true;
    }

    private boolean onMessageShutdown() {
        if (mOppoDebug) {
            Slog.i(TAG, "H_SHUTDOWN!");
        }
        killMediaProvider(((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers());
        return true;
    }

    private boolean onMessageVolumeMount(Message msg) {
        if (mOppoDebug) {
            Slog.i(TAG, "onMessageVolumeMount impl");
        }
        VolumeInfo vol = (VolumeInfo) msg.obj;
        if (vol.type != 0 || this.mIsStartUser) {
            if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom") && vol.type == 0 && vol.getDisk().isUsb()) {
                boolean is_otg_switch = SystemProperties.getBoolean("persist.vendor.otg.switch", true);
                boolean is_otg_support = SystemProperties.getBoolean("persist.sys.oppo.otg_support", true);
                if (mOppoDebug) {
                    Slog.i(TAG, "is_otg_switch =" + is_otg_switch);
                }
                if (mOppoDebug) {
                    Slog.i(TAG, "is_otg_support =" + is_otg_support);
                }
                if (!is_otg_switch || !is_otg_support) {
                    return false;
                }
            }
            speedupForIOPerformance(12, 0);
            return true;
        }
        if (mOppoDebug) {
            Slog.i(TAG, vol.getId() + " not ready , retry and sendMessageDelayed 1s");
        }
        Handler handler = this.mAospStorageManagerHandler;
        if (handler != null) {
            Message delaymsg = handler.obtainMessage(DPOLICY_PERFORMANCE);
            delaymsg.obj = vol;
            this.mAospStorageManagerHandler.sendMessageDelayed(delaymsg, 1000);
        } else {
            Slog.e(TAG, "onMessageVolumeMount:fatal exception for handler uninit before apply!!");
        }
        return false;
    }

    private void killMediaProvider(List<UserInfo> users) {
        if (users != null) {
            PackageManagerInternal pkgMgr = getPackageManagerInternal();
            if (pkgMgr == null) {
                Slog.e(TAG, "killMediaProvider failed for pkgMgr unavailable!");
                return;
            }
            long token = Binder.clearCallingIdentity();
            try {
                for (UserInfo user : users) {
                    if (!user.isSystemOnly()) {
                        ProviderInfo provider = pkgMgr.resolveContentProvider("media", 786432, user.id);
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

    private void speedupForIOPerformance(int action, int timeout) {
        if (mHypnus == null) {
            mHypnus = Hypnus.getHypnus();
        }
        Hypnus hypnus = mHypnus;
        if (hypnus != null) {
            hypnus.hypnusSetAction(action, timeout);
        }
    }

    private boolean onMessageVolumeCheck(Message msg) {
        VolumeInfo vol = (VolumeInfo) msg.obj;
        if (vol == null) {
            Slog.e(TAG, "onMessageVolumeCheck:vol is empty.");
            return true;
        }
        if (mOppoDebug) {
            Slog.i(TAG, "H_VOLUME_CHECK volId=" + vol.getId());
        }
        if (vol.mountUserId != 0 || SystemProperties.getBoolean("vold.storage.prepared", false)) {
            IOppoStorageManagerCallback iOppoStorageManagerCallback = this.mStorageManagerCallback;
            if (iOppoStorageManagerCallback != null) {
                try {
                    iOppoStorageManagerCallback.onCheckBeforeMount(vol.getId());
                } catch (Exception e) {
                    Slog.wtf(TAG, e);
                }
            } else {
                Slog.e(TAG, "onMessageVolumeCheck: fatal exception for callback uninit!");
            }
            return false;
        }
        if (mOppoDebug) {
            Slog.i(TAG, "H_VOLUME_CHECK not ready, retry and sendMessageDelayed");
        }
        Handler handler = this.mAospStorageManagerHandler;
        if (handler != null) {
            Message delaymsg = handler.obtainMessage(H_VOLUME_CHECK);
            delaymsg.obj = vol;
            this.mAospStorageManagerHandler.sendMessageDelayed(delaymsg, 1500);
        } else {
            Slog.e(TAG, "onMessageVolumeCheck: fatal exception for handler uninit before apply!!");
        }
        return false;
    }

    private boolean onMessageVolumeBroadcastStorageRO(Message msg) {
        StorageVolume userVol = (StorageVolume) msg.obj;
        if (userVol == null) {
            Slog.e(TAG, "onMessageVolumeBroadcastStorageRO:userVol is empty.");
            return false;
        }
        if (mOppoDebug) {
            Slog.d(TAG, "Volume " + userVol.getId() + " broadcasting " + "mounted_ro" + " to " + userVol.getOwner());
        }
        Intent intent = new Intent(COPY_OF_ACTION_MEDIA_MOUNTED_RO, Uri.fromFile(userVol.getPathFile()));
        intent.putExtra("android.os.storage.extra.STORAGE_VOLUME", userVol);
        intent.addFlags(67108864);
        this.mContext.sendBroadcastAsUser(intent, userVol.getOwner());
        return false;
    }

    private PackageManagerInternal getPackageManagerInternal() {
        if (this.mPmInternal == null) {
            this.mPmInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        }
        return this.mPmInternal;
    }

    private void setReadonlyTypeForVolumeInfo(VolumeInfo vol, int typeValue) {
        OppoBaseVolumeInfo oppoVolumeInfo = typeCastingForVolumeInfo(vol);
        if (oppoVolumeInfo != null) {
            oppoVolumeInfo.setReadOnlyTypeValue(typeValue);
        } else {
            Slog.w("OppoBaseVolumeInfo", "type cast failed.");
        }
    }

    private OppoBaseVolumeInfo typeCastingForVolumeInfo(VolumeInfo vol) {
        return (OppoBaseVolumeInfo) ColorTypeCastingHelper.typeCasting(OppoBaseVolumeInfo.class, vol);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00ab, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ac, code lost:
        if (r5 != null) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00ae, code lost:
        $closeResource(r2, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00b1, code lost:
        throw r3;
     */
    private void onMessageDPolicyExpect(Message msg) {
        String actionParam = msg.obj != null ? (String) msg.obj : null;
        if (actionParam == null) {
            actionParam = "0";
        }
        Path dir = Paths.get("/sys/fs/f2fs/", new String[0]);
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        Iterator<Path> it = stream.iterator();
        while (it.hasNext()) {
            String file = dir.toString() + "/" + it.next().getFileName().toString() + "/dpolicy_expect";
            if (!Files.notExists(Paths.get(file, new String[0]), new LinkOption[0])) {
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(file));
                    bw.write(actionParam);
                    bw.close();
                    BufferedWriter bw2 = null;
                    if (bw2 != null) {
                        try {
                            bw2.close();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e2) {
                    Slog.e(TAG, "onMessageDPolicyExpect write failed for:" + file);
                    if (bw != null) {
                        bw.close();
                    }
                } catch (Throwable th) {
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (Exception e3) {
                        }
                    }
                    throw th;
                }
            }
        }
        try {
            $closeResource(null, stream);
        } catch (IOException | DirectoryIteratorException e4) {
            Slog.e(TAG, "onMessageDPolicyExpect failed!", e4);
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00ab, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ac, code lost:
        if (r5 != null) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00ae, code lost:
        $closeResource(r2, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00b1, code lost:
        throw r3;
     */
    private void onMessageFsyncProtect(Message msg) {
        String actionParam = msg.obj != null ? (String) msg.obj : null;
        if (actionParam == null) {
            actionParam = "0";
        }
        Path dir = Paths.get("/sys/fs/f2fs/", new String[0]);
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        Iterator<Path> it = stream.iterator();
        while (it.hasNext()) {
            String file = dir.toString() + "/" + it.next().getFileName().toString() + "/fsync_protect";
            if (!Files.notExists(Paths.get(file, new String[0]), new LinkOption[0])) {
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(file));
                    bw.write(actionParam);
                    bw.close();
                    BufferedWriter bw2 = null;
                    if (bw2 != null) {
                        try {
                            bw2.close();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e2) {
                    Slog.e(TAG, "onMessageFsyncProtect write failed for:" + file);
                    if (bw != null) {
                        bw.close();
                    }
                } catch (Throwable th) {
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (Exception e3) {
                        }
                    }
                    throw th;
                }
            }
        }
        try {
            $closeResource(null, stream);
        } catch (IOException | DirectoryIteratorException e4) {
            Slog.e(TAG, "onMessageFsyncProtect failed!", e4);
        }
    }
}
