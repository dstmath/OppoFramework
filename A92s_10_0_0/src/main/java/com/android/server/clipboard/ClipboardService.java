package com.android.server.clipboard;

import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IUriGrantsManager;
import android.app.KeyguardManager;
import android.app.UriGrantsManager;
import android.common.OppoFeatureCache;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Context;
import android.content.IClipboard;
import android.content.IOnPrimaryClipChangedListener;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.view.autofill.AutofillManagerInternal;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.clipboard.HostClipboardMonitor;
import com.android.server.contentcapture.ContentCaptureManagerInternal;
import com.android.server.uri.UriGrantsManagerInternal;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.android.server.wm.WindowManagerInternal;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import java.util.HashSet;
import java.util.List;

public class ClipboardService extends SystemService {
    private static final boolean IS_EMULATOR = SystemProperties.getBoolean("ro.kernel.qemu", false);
    private static final String TAG = "ClipboardService";
    private final ActivityManagerInternal mAmInternal = ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class));
    private final AppOpsManager mAppOps = ((AppOpsManager) getContext().getSystemService("appops"));
    private final AutofillManagerInternal mAutofillInternal = ((AutofillManagerInternal) LocalServices.getService(AutofillManagerInternal.class));
    /* access modifiers changed from: private */
    public final SparseArray<PerUserClipboard> mClipboards = new SparseArray<>();
    private final ContentCaptureManagerInternal mContentCaptureInternal = ((ContentCaptureManagerInternal) LocalServices.getService(ContentCaptureManagerInternal.class));
    private HostClipboardMonitor mHostClipboardMonitor = null;
    private Thread mHostMonitorThread = null;
    private final IBinder mPermissionOwner = this.mUgmInternal.newUriPermissionOwner("clipboard");
    private final PackageManager mPm = getContext().getPackageManager();
    private final IUriGrantsManager mUgm = UriGrantsManager.getService();
    private final UriGrantsManagerInternal mUgmInternal = ((UriGrantsManagerInternal) LocalServices.getService(UriGrantsManagerInternal.class));
    private final IUserManager mUm = ServiceManager.getService("user");
    private final WindowManagerInternal mWm = ((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class));

    public ClipboardService(Context context) {
        super(context);
        if (IS_EMULATOR) {
            this.mHostClipboardMonitor = new HostClipboardMonitor(new HostClipboardMonitor.HostClipboardCallback() {
                /* class com.android.server.clipboard.ClipboardService.AnonymousClass1 */

                @Override // com.android.server.clipboard.HostClipboardMonitor.HostClipboardCallback
                public void onHostClipboardUpdated(String contents) {
                    ClipData clip = new ClipData("host clipboard", new String[]{"text/plain"}, new ClipData.Item(contents));
                    synchronized (ClipboardService.this.mClipboards) {
                        ClipboardService.this.setPrimaryClipInternal(ClipboardService.this.getClipboard(0), clip, 1000);
                    }
                }
            });
            this.mHostMonitorThread = new Thread(this.mHostClipboardMonitor);
            this.mHostMonitorThread.start();
        }
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.clipboard.ClipboardService$ClipboardImpl, android.os.IBinder] */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("clipboard", new ClipboardImpl());
    }

    @Override // com.android.server.SystemService
    public void onCleanupUser(int userId) {
        synchronized (this.mClipboards) {
            this.mClipboards.remove(userId);
        }
    }

    private class ListenerInfo {
        final String mPackageName;
        final int mUid;

        ListenerInfo(int uid, String packageName) {
            this.mUid = uid;
            this.mPackageName = packageName;
        }
    }

    /* access modifiers changed from: private */
    public class PerUserClipboard {
        final HashSet<String> activePermissionOwners = new HashSet<>();
        ClipData primaryClip;
        final RemoteCallbackList<IOnPrimaryClipChangedListener> primaryClipListeners = new RemoteCallbackList<>();
        int primaryClipUid = 9999;
        final int userId;

        PerUserClipboard(int userId2) {
            this.userId = userId2;
        }
    }

    private boolean isInternalSysWindowAppWithWindowFocus(String callingPackage) {
        if (this.mPm.checkPermission("android.permission.INTERNAL_SYSTEM_WINDOW", callingPackage) != 0 || !this.mWm.isUidFocused(Binder.getCallingUid())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public int getIntendingUserId(String packageName, int userId) {
        int callingUserId = UserHandle.getUserId(Binder.getCallingUid());
        if (!UserManager.supportsMultipleUsers() || callingUserId == userId) {
            return callingUserId;
        }
        return this.mAmInternal.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, 2, "checkClipboardServiceCallingUser", packageName);
    }

    /* access modifiers changed from: private */
    public int getIntendingUid(String packageName, int userId) {
        return UserHandle.getUid(getIntendingUserId(packageName, userId), UserHandle.getAppId(Binder.getCallingUid()));
    }

    private class ClipboardImpl extends IClipboard.Stub {
        private ClipboardImpl() {
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            try {
                return ClipboardService.super.onTransact(code, data, reply, flags);
            } catch (RuntimeException e) {
                if (!(e instanceof SecurityException)) {
                    Slog.wtf("clipboard", "Exception: ", e);
                }
                throw e;
            }
        }

        public void setPrimaryClip(ClipData clip, String callingPackage, int userId) {
            OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(85, Binder.getCallingUid());
            synchronized (this) {
                if (clip != null) {
                    if (clip.getItemCount() > 0) {
                        int intendingUid = ClipboardService.this.getIntendingUid(callingPackage, userId);
                        if (ClipboardService.this.clipboardAccessAllowed(30, callingPackage, intendingUid, UserHandle.getUserId(intendingUid))) {
                            ClipboardService.this.checkDataOwnerLocked(clip, intendingUid);
                            ClipboardService.this.setPrimaryClipInternal(clip, intendingUid);
                            return;
                        }
                        return;
                    }
                }
                throw new IllegalArgumentException("No items");
            }
        }

        public void clearPrimaryClip(String callingPackage, int userId) {
            synchronized (this) {
                int intendingUid = ClipboardService.this.getIntendingUid(callingPackage, userId);
                if (ClipboardService.this.clipboardAccessAllowed(30, callingPackage, intendingUid, UserHandle.getUserId(intendingUid))) {
                    ClipboardService.this.setPrimaryClipInternal(null, intendingUid);
                }
            }
        }

        public ClipData getPrimaryClip(String pkg, int userId) {
            synchronized (this) {
                int intendingUid = ClipboardService.this.getIntendingUid(pkg, userId);
                int intendingUserId = UserHandle.getUserId(intendingUid);
                if (ClipboardService.this.clipboardAccessAllowed(29, pkg, intendingUid, intendingUserId)) {
                    if (!ClipboardService.this.isDeviceLocked(intendingUserId)) {
                        ClipboardService.this.addActiveOwnerLocked(intendingUid, pkg);
                        ClipData clipData = ClipboardService.this.getClipboard(intendingUserId).primaryClip;
                        return clipData;
                    }
                }
                return null;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0032, code lost:
            return r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0034, code lost:
            return null;
         */
        public ClipDescription getPrimaryClipDescription(String callingPackage, int userId) {
            synchronized (this) {
                int intendingUid = ClipboardService.this.getIntendingUid(callingPackage, userId);
                int intendingUserId = UserHandle.getUserId(intendingUid);
                ClipDescription clipDescription = null;
                if (ClipboardService.this.clipboardAccessAllowed(29, callingPackage, intendingUid, intendingUserId)) {
                    if (!ClipboardService.this.isDeviceLocked(intendingUserId)) {
                        PerUserClipboard clipboard = ClipboardService.this.getClipboard(intendingUserId);
                        if (clipboard.primaryClip != null) {
                            clipDescription = clipboard.primaryClip.getDescription();
                        }
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x002b, code lost:
            return r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x002d, code lost:
            return false;
         */
        public boolean hasPrimaryClip(String callingPackage, int userId) {
            synchronized (this) {
                int intendingUid = ClipboardService.this.getIntendingUid(callingPackage, userId);
                int intendingUserId = UserHandle.getUserId(intendingUid);
                boolean z = false;
                if (ClipboardService.this.clipboardAccessAllowed(29, callingPackage, intendingUid, intendingUserId)) {
                    if (!ClipboardService.this.isDeviceLocked(intendingUserId)) {
                        if (ClipboardService.this.getClipboard(intendingUserId).primaryClip != null) {
                            z = true;
                        }
                    }
                }
            }
        }

        public void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener listener, String callingPackage, int userId) {
            synchronized (this) {
                int intendingUid = ClipboardService.this.getIntendingUid(callingPackage, userId);
                ClipboardService.this.getClipboard(UserHandle.getUserId(intendingUid)).primaryClipListeners.register(listener, new ListenerInfo(intendingUid, callingPackage));
            }
        }

        public void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener listener, String callingPackage, int userId) {
            synchronized (this) {
                ClipboardService.this.getClipboard(ClipboardService.this.getIntendingUserId(callingPackage, userId)).primaryClipListeners.unregister(listener);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:15:0x003d, code lost:
            return r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0041, code lost:
            return false;
         */
        public boolean hasClipboardText(String callingPackage, int userId) {
            synchronized (this) {
                int intendingUid = ClipboardService.this.getIntendingUid(callingPackage, userId);
                int intendingUserId = UserHandle.getUserId(intendingUid);
                boolean z = false;
                if (ClipboardService.this.clipboardAccessAllowed(29, callingPackage, intendingUid, intendingUserId)) {
                    if (!ClipboardService.this.isDeviceLocked(intendingUserId)) {
                        PerUserClipboard clipboard = ClipboardService.this.getClipboard(intendingUserId);
                        if (clipboard.primaryClip == null) {
                            return false;
                        }
                        CharSequence text = clipboard.primaryClip.getItemAt(0).getText();
                        if (text != null && text.length() > 0) {
                            z = true;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public PerUserClipboard getClipboard(int userId) {
        PerUserClipboard puc;
        synchronized (this.mClipboards) {
            int userId2 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getCorrectUserId(userId);
            puc = this.mClipboards.get(userId2);
            if (puc == null) {
                puc = new PerUserClipboard(userId2);
                this.mClipboards.put(userId2, puc);
            }
        }
        return puc;
    }

    /* access modifiers changed from: package-private */
    public List<UserInfo> getRelatedProfiles(int userId) {
        long origId = Binder.clearCallingIdentity();
        try {
            return this.mUm.getProfiles(userId, true);
        } catch (RemoteException e) {
            Slog.e(TAG, "Remote Exception calling UserManager: " + e);
            return null;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    private boolean hasRestriction(String restriction, int userId) {
        try {
            return this.mUm.hasUserRestriction(restriction, userId);
        } catch (RemoteException e) {
            Slog.e(TAG, "Remote Exception calling UserManager.getUserRestrictions: ", e);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void setPrimaryClipInternal(ClipData clip, int uid) {
        int size;
        CharSequence text;
        HostClipboardMonitor hostClipboardMonitor = this.mHostClipboardMonitor;
        if (hostClipboardMonitor != null) {
            if (clip == null) {
                hostClipboardMonitor.setHostClipboard("");
            } else if (clip.getItemCount() > 0 && (text = clip.getItemAt(0).getText()) != null) {
                this.mHostClipboardMonitor.setHostClipboard(text.toString());
            }
        }
        int userId = UserHandle.getUserId(uid);
        setPrimaryClipInternal(getClipboard(userId), clip, uid);
        List<UserInfo> related = getRelatedProfiles(userId);
        if (related != null && (size = related.size()) > 1) {
            if (!(!hasRestriction("no_cross_profile_copy_paste", userId))) {
                clip = null;
            } else if (clip != null) {
                clip = new ClipData(clip);
                for (int i = clip.getItemCount() - 1; i >= 0; i--) {
                    clip.setItemAt(i, new ClipData.Item(clip.getItemAt(i)));
                }
                clip.fixUrisLight(userId);
            }
            for (int i2 = 0; i2 < size; i2++) {
                int id = related.get(i2).id;
                if (id != userId && (!hasRestriction("no_sharing_into_profile", id))) {
                    setPrimaryClipInternal(getClipboard(id), clip, uid);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPrimaryClipInternal(PerUserClipboard clipboard, ClipData clip, int uid) {
        ClipDescription description;
        revokeUris(clipboard);
        clipboard.activePermissionOwners.clear();
        if (clip != null || clipboard.primaryClip != null) {
            clipboard.primaryClip = clip;
            if (clip != null) {
                clipboard.primaryClipUid = uid;
            } else {
                clipboard.primaryClipUid = 9999;
            }
            if (!(clip == null || (description = clip.getDescription()) == null)) {
                description.setTimestamp(System.currentTimeMillis());
            }
            long ident = Binder.clearCallingIdentity();
            int n = clipboard.primaryClipListeners.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ListenerInfo li = (ListenerInfo) clipboard.primaryClipListeners.getBroadcastCookie(i);
                    if (clipboardAccessAllowed(29, li.mPackageName, li.mUid, UserHandle.getUserId(li.mUid))) {
                        clipboard.primaryClipListeners.getBroadcastItem(i).dispatchPrimaryClipChanged();
                    }
                } catch (RemoteException e) {
                } catch (Throwable th) {
                    clipboard.primaryClipListeners.finishBroadcast();
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            }
            clipboard.primaryClipListeners.finishBroadcast();
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    public boolean isDeviceLocked(int userId) {
        OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getCorrectUid(UserHandle.getCallingUserId());
        long token = Binder.clearCallingIdentity();
        try {
            KeyguardManager keyguardManager = (KeyguardManager) getContext().getSystemService(KeyguardManager.class);
            return keyguardManager != null && keyguardManager.isDeviceLocked(userId);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private final void checkUriOwnerLocked(Uri uri, int sourceUid) {
        if (uri != null && ActivityTaskManagerInternal.ASSIST_KEY_CONTENT.equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mUgmInternal.checkGrantUriPermission(sourceUid, (String) null, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)));
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private final void checkItemOwnerLocked(ClipData.Item item, int uid) {
        if (item.getUri() != null) {
            checkUriOwnerLocked(item.getUri(), uid);
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            checkUriOwnerLocked(intent.getData(), uid);
        }
    }

    /* access modifiers changed from: private */
    public final void checkDataOwnerLocked(ClipData data, int uid) {
        int N = data.getItemCount();
        for (int i = 0; i < N; i++) {
            checkItemOwnerLocked(data.getItemAt(i), uid);
        }
    }

    private final void grantUriLocked(Uri uri, int sourceUid, String targetPkg, int targetUserId) {
        if (uri != null && ActivityTaskManagerInternal.ASSIST_KEY_CONTENT.equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mUgm.grantUriPermissionFromOwner(this.mPermissionOwner, sourceUid, targetPkg, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)), targetUserId);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
            Binder.restoreCallingIdentity(ident);
        }
    }

    private final void grantItemLocked(ClipData.Item item, int sourceUid, String targetPkg, int targetUserId) {
        if (item.getUri() != null) {
            grantUriLocked(item.getUri(), sourceUid, targetPkg, targetUserId);
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            grantUriLocked(intent.getData(), sourceUid, targetPkg, targetUserId);
        }
    }

    /* access modifiers changed from: private */
    public final void addActiveOwnerLocked(int uid, String pkg) {
        IPackageManager pm = AppGlobals.getPackageManager();
        int targetUserHandle = UserHandle.getCallingUserId();
        long oldIdentity = Binder.clearCallingIdentity();
        try {
            PackageInfo pi = pm.getPackageInfo(pkg, 0, targetUserHandle);
            if (pi == null) {
                throw new IllegalArgumentException("Unknown package " + pkg);
            } else if (UserHandle.isSameApp(pi.applicationInfo.uid, uid)) {
                Binder.restoreCallingIdentity(oldIdentity);
                PerUserClipboard clipboard = getClipboard(UserHandle.getUserId(uid));
                if (clipboard.primaryClip != null && !clipboard.activePermissionOwners.contains(pkg)) {
                    int N = clipboard.primaryClip.getItemCount();
                    for (int i = 0; i < N; i++) {
                        grantItemLocked(clipboard.primaryClip.getItemAt(i), clipboard.primaryClipUid, pkg, UserHandle.getUserId(uid));
                    }
                    clipboard.activePermissionOwners.add(pkg);
                }
            } else {
                throw new SecurityException("Calling uid " + uid + " does not own package " + pkg);
            }
        } catch (RemoteException e) {
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(oldIdentity);
            throw th;
        }
    }

    private final void revokeUriLocked(Uri uri, int sourceUid) {
        if (uri != null && ActivityTaskManagerInternal.ASSIST_KEY_CONTENT.equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mUgmInternal.revokeUriPermissionFromOwner(this.mPermissionOwner, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)));
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private final void revokeItemLocked(ClipData.Item item, int sourceUid) {
        if (item.getUri() != null) {
            revokeUriLocked(item.getUri(), sourceUid);
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            revokeUriLocked(intent.getData(), sourceUid);
        }
    }

    private final void revokeUris(PerUserClipboard clipboard) {
        if (clipboard.primaryClip != null) {
            int N = clipboard.primaryClip.getItemCount();
            for (int i = 0; i < N; i++) {
                revokeItemLocked(clipboard.primaryClip.getItemAt(i), clipboard.primaryClipUid);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean clipboardAccessAllowed(int op, String callingPackage, int uid, int userId) {
        AutofillManagerInternal autofillManagerInternal;
        ContentCaptureManagerInternal contentCaptureManagerInternal;
        boolean allowed = false;
        if (this.mAppOps.noteOp(op, uid, callingPackage) != 0) {
            return false;
        }
        if (this.mPm.checkPermission("android.permission.READ_CLIPBOARD_IN_BACKGROUND", callingPackage) == 0) {
            return true;
        }
        String defaultIme = Settings.Secure.getStringForUser(getContext().getContentResolver(), "default_input_method", userId);
        if (!TextUtils.isEmpty(defaultIme) && ComponentName.unflattenFromString(defaultIme).getPackageName().equals(callingPackage)) {
            return true;
        }
        if (op == 29) {
            if (this.mWm.isUidFocused(uid) || isInternalSysWindowAppWithWindowFocus(callingPackage)) {
                allowed = true;
            }
            if (!allowed && (contentCaptureManagerInternal = this.mContentCaptureInternal) != null) {
                allowed = contentCaptureManagerInternal.isContentCaptureServiceForUser(uid, userId);
            }
            if (!allowed && (autofillManagerInternal = this.mAutofillInternal) != null) {
                allowed = autofillManagerInternal.isAugmentedAutofillServiceForUser(uid, userId);
            }
            if (!allowed) {
                Slog.e(TAG, "Denying clipboard access to " + callingPackage + ", application is not in focus neither is a system service for user " + userId);
            }
            return allowed;
        } else if (op == 30) {
            return true;
        } else {
            throw new IllegalArgumentException("Unknown clipboard appop " + op);
        }
    }
}
