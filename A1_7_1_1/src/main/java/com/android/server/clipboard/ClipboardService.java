package com.android.server.clipboard;

import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Context;
import android.content.IClipboard.Stub;
import android.content.IOnPrimaryClipChangedListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.am.OppoMultiAppManager;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import oppo.util.OppoStatistics;

public class ClipboardService extends Stub {
    private static final String TAG = "ClipboardService";
    private final IActivityManager mAm;
    private final AppOpsManager mAppOps;
    private SparseArray<PerUserClipboard> mClipboards = new SparseArray();
    private final Context mContext;
    private final IBinder mPermissionOwner;
    private final PackageManager mPm;
    private final IUserManager mUm;

    private class ListenerInfo {
        final String mPackageName;
        final int mUid;

        ListenerInfo(int uid, String packageName) {
            this.mUid = uid;
            this.mPackageName = packageName;
        }
    }

    private class PerUserClipboard {
        final HashSet<String> activePermissionOwners = new HashSet();
        ClipData primaryClip;
        final RemoteCallbackList<IOnPrimaryClipChangedListener> primaryClipListeners = new RemoteCallbackList();
        final int userId;

        PerUserClipboard(int userId) {
            this.userId = userId;
        }
    }

    public ClipboardService(Context context) {
        this.mContext = context;
        this.mAm = ActivityManagerNative.getDefault();
        this.mPm = context.getPackageManager();
        this.mUm = (IUserManager) ServiceManager.getService("user");
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        IBinder permOwner = null;
        try {
            permOwner = this.mAm.newUriPermissionOwner("clipboard");
        } catch (RemoteException e) {
            Slog.w("clipboard", "AM dead", e);
        }
        this.mPermissionOwner = permOwner;
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    ClipboardService.this.removeClipboard(intent.getIntExtra("android.intent.extra.user_handle", 0));
                }
            }
        }, userFilter);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf("clipboard", "Exception: ", e);
            }
            throw e;
        }
    }

    private PerUserClipboard getClipboard() {
        return getClipboard(UserHandle.getCallingUserId());
    }

    private PerUserClipboard getClipboard(int userId) {
        PerUserClipboard puc;
        synchronized (this.mClipboards) {
            if (userId == OppoMultiAppManager.USER_ID) {
                userId = 0;
            }
            puc = (PerUserClipboard) this.mClipboards.get(userId);
            if (puc == null) {
                puc = new PerUserClipboard(userId);
                this.mClipboards.put(userId, puc);
            }
        }
        return puc;
    }

    private void removeClipboard(int userId) {
        synchronized (this.mClipboards) {
            this.mClipboards.remove(userId);
        }
    }

    /* JADX WARNING: Missing block: B:57:0x0159, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPrimaryClip(ClipData clip, String callingPackage) {
        Throwable th;
        synchronized (this) {
            if (clip != null) {
                try {
                    if (clip.getItemCount() <= 0) {
                        throw new IllegalArgumentException("No items");
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Remote Exception calling UserManager: " + e);
                } catch (Throwable th2) {
                    th = th2;
                }
            }
            int callingUid = Binder.getCallingUid();
            if (this.mAppOps.noteOp(30, callingUid, callingPackage) != 0) {
                return;
            }
            checkDataOwnerLocked(clip, callingUid);
            int userId = UserHandle.getUserId(callingUid);
            PerUserClipboard clipboard = getClipboard(userId);
            revokeUris(clipboard);
            String forePkg = null;
            ComponentName cn = null;
            try {
                cn = this.mAm.getTopAppName();
            } catch (RemoteException e2) {
            }
            if (cn != null) {
                forePkg = cn.getPackageName();
            }
            if (!(forePkg == null || forePkg.equals(callingPackage))) {
                Slog.w(TAG, "backgroud clip use from " + callingPackage);
                HashMap<String, String> map = new HashMap();
                map.put("pkgname", callingPackage);
                map.put("forePkg", forePkg);
                OppoStatistics.onCommon(this.mContext, DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "back_setclip", map, false);
            }
            setPrimaryClipInternal(clipboard, clip);
            List<UserInfo> related = getRelatedProfiles(userId);
            if (related != null) {
                int size = related.size();
                if (size > 1) {
                    int i;
                    boolean canCopy = false;
                    canCopy = !this.mUm.getUserRestrictions(userId).getBoolean("no_cross_profile_copy_paste");
                    if (canCopy) {
                        ClipData clip2 = new ClipData(clip);
                        try {
                            for (i = clip2.getItemCount() - 1; i >= 0; i--) {
                                clip2.setItemAt(i, new Item(clip2.getItemAt(i)));
                            }
                            clip2.fixUrisLight(userId);
                            clip = clip2;
                        } catch (Throwable th3) {
                            th = th3;
                            clip = clip2;
                            throw th;
                        }
                    }
                    clip = null;
                    for (i = 0; i < size; i++) {
                        int id = ((UserInfo) related.get(i)).id;
                        if (id != userId) {
                            setPrimaryClipInternal(getClipboard(id), clip);
                        }
                    }
                }
            }
        }
    }

    List<UserInfo> getRelatedProfiles(int userId) {
        long origId = Binder.clearCallingIdentity();
        try {
            List<UserInfo> related = this.mUm.getProfiles(userId, true);
            Binder.restoreCallingIdentity(origId);
            return related;
        } catch (RemoteException e) {
            Slog.e(TAG, "Remote Exception calling UserManager: " + e);
            Binder.restoreCallingIdentity(origId);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }

    void setPrimaryClipInternal(PerUserClipboard clipboard, ClipData clip) {
        clipboard.activePermissionOwners.clear();
        if (clip != null || clipboard.primaryClip != null) {
            clipboard.primaryClip = clip;
            long ident = Binder.clearCallingIdentity();
            int n = clipboard.primaryClipListeners.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ListenerInfo li = (ListenerInfo) clipboard.primaryClipListeners.getBroadcastCookie(i);
                    if (this.mAppOps.checkOpNoThrow(29, li.mUid, li.mPackageName) == 0) {
                        ((IOnPrimaryClipChangedListener) clipboard.primaryClipListeners.getBroadcastItem(i)).dispatchPrimaryClipChanged();
                    }
                } catch (RemoteException e) {
                } catch (Throwable th) {
                    clipboard.primaryClipListeners.finishBroadcast();
                    Binder.restoreCallingIdentity(ident);
                }
            }
            clipboard.primaryClipListeners.finishBroadcast();
            Binder.restoreCallingIdentity(ident);
        }
    }

    public ClipData getPrimaryClip(String pkg) {
        synchronized (this) {
            if (this.mAppOps.noteOp(29, Binder.getCallingUid(), pkg) != 0 || isDeviceLocked()) {
                return null;
            }
            addActiveOwnerLocked(Binder.getCallingUid(), pkg);
            ClipData clipData = getClipboard().primaryClip;
            return clipData;
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0017, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:14:0x0027, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ClipDescription getPrimaryClipDescription(String callingPackage) {
        ClipDescription clipDescription = null;
        synchronized (this) {
            if (this.mAppOps.checkOp(29, Binder.getCallingUid(), callingPackage) != 0 || isDeviceLocked()) {
            } else {
                PerUserClipboard clipboard = getClipboard();
                if (clipboard.primaryClip != null) {
                    clipDescription = clipboard.primaryClip.getDescription();
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0017, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:14:0x0022, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasPrimaryClip(String callingPackage) {
        boolean z = false;
        synchronized (this) {
            if (this.mAppOps.checkOp(29, Binder.getCallingUid(), callingPackage) != 0 || isDeviceLocked()) {
            } else if (getClipboard().primaryClip != null) {
                z = true;
            }
        }
    }

    public void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener listener, String callingPackage) {
        synchronized (this) {
            getClipboard().primaryClipListeners.register(listener, new ListenerInfo(Binder.getCallingUid(), callingPackage));
        }
    }

    public void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener listener) {
        synchronized (this) {
            getClipboard().primaryClipListeners.unregister(listener);
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0017, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:18:0x0035, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasClipboardText(String callingPackage) {
        boolean z = false;
        synchronized (this) {
            if (this.mAppOps.checkOp(29, Binder.getCallingUid(), callingPackage) != 0 || isDeviceLocked()) {
            } else {
                PerUserClipboard clipboard = getClipboard();
                if (clipboard.primaryClip != null) {
                    CharSequence text = clipboard.primaryClip.getItemAt(0).getText();
                    if (text != null && text.length() > 0) {
                        z = true;
                    }
                } else {
                    return false;
                }
            }
        }
    }

    private boolean isDeviceLocked() {
        int callingUserId = UserHandle.getCallingUserId();
        if (OppoMultiAppManager.USER_ID == callingUserId) {
            callingUserId = 0;
        }
        long token = Binder.clearCallingIdentity();
        try {
            KeyguardManager keyguardManager = (KeyguardManager) this.mContext.getSystemService(KeyguardManager.class);
            boolean isDeviceLocked = keyguardManager != null ? keyguardManager.isDeviceLocked(callingUserId) : false;
            Binder.restoreCallingIdentity(token);
            return isDeviceLocked;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    private final void checkUriOwnerLocked(Uri uri, int uid) {
        if ("content".equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mAm.checkGrantUriPermission(uid, null, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(uid)));
            } catch (RemoteException e) {
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private final void checkItemOwnerLocked(Item item, int uid) {
        if (item.getUri() != null) {
            checkUriOwnerLocked(item.getUri(), uid);
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            checkUriOwnerLocked(intent.getData(), uid);
        }
    }

    private final void checkDataOwnerLocked(ClipData data, int uid) {
        int N = data.getItemCount();
        for (int i = 0; i < N; i++) {
            checkItemOwnerLocked(data.getItemAt(i), uid);
        }
    }

    private final void grantUriLocked(Uri uri, String pkg, int userId) {
        long ident = Binder.clearCallingIdentity();
        try {
            int sourceUserId = ContentProvider.getUserIdFromUri(uri, userId);
            this.mAm.grantUriPermissionFromOwner(this.mPermissionOwner, Process.myUid(), pkg, ContentProvider.getUriWithoutUserId(uri), 1, sourceUserId, userId);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private final void grantItemLocked(Item item, String pkg, int userId) {
        if (item.getUri() != null) {
            grantUriLocked(item.getUri(), pkg, userId);
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            grantUriLocked(intent.getData(), pkg, userId);
        }
    }

    private final void addActiveOwnerLocked(int uid, String pkg) {
        IPackageManager pm = AppGlobals.getPackageManager();
        int targetUserHandle = UserHandle.getCallingUserId();
        long oldIdentity = Binder.clearCallingIdentity();
        try {
            PackageInfo pi = pm.getPackageInfo(pkg, 0, targetUserHandle);
            if (pi == null) {
                throw new IllegalArgumentException("Unknown package " + pkg);
            } else if (UserHandle.isSameApp(pi.applicationInfo.uid, uid)) {
                PerUserClipboard clipboard = getClipboard();
                if (clipboard.primaryClip != null && !clipboard.activePermissionOwners.contains(pkg)) {
                    int N = clipboard.primaryClip.getItemCount();
                    for (int i = 0; i < N; i++) {
                        grantItemLocked(clipboard.primaryClip.getItemAt(i), pkg, UserHandle.getUserId(uid));
                    }
                    clipboard.activePermissionOwners.add(pkg);
                }
            } else {
                throw new SecurityException("Calling uid " + uid + " does not own package " + pkg);
            }
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(oldIdentity);
        }
    }

    private final void revokeUriLocked(Uri uri) {
        int userId = ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(Binder.getCallingUid()));
        long ident = Binder.clearCallingIdentity();
        try {
            this.mAm.revokeUriPermissionFromOwner(this.mPermissionOwner, ContentProvider.getUriWithoutUserId(uri), 3, userId);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private final void revokeItemLocked(Item item) {
        if (item.getUri() != null) {
            revokeUriLocked(item.getUri());
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            revokeUriLocked(intent.getData());
        }
    }

    private final void revokeUris(PerUserClipboard clipboard) {
        if (clipboard.primaryClip != null) {
            int N = clipboard.primaryClip.getItemCount();
            for (int i = 0; i < N; i++) {
                revokeItemLocked(clipboard.primaryClip.getItemAt(i));
            }
        }
    }
}
