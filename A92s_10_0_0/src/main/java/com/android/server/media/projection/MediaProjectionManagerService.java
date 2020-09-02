package com.android.server.media.projection;

import android.app.ActivityManagerInternal;
import android.app.AppOpsManager;
import android.app.IProcessObserver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.MediaRouter;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionCallback;
import android.media.projection.IMediaProjectionManager;
import android.media.projection.IMediaProjectionWatcherCallback;
import android.media.projection.MediaProjectionInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.util.DumpUtils;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;

public final class MediaProjectionManagerService extends SystemService implements Watchdog.Monitor {
    private static final boolean REQUIRE_FG_SERVICE_FOR_PROJECTION = true;
    private static final String TAG = "MediaProjectionManagerService";
    private final String OPPOSCREENRECORDER = "com.coloros.screenrecorder";
    /* access modifiers changed from: private */
    public final ActivityManagerInternal mActivityManagerInternal;
    /* access modifiers changed from: private */
    public final AppOpsManager mAppOps;
    /* access modifiers changed from: private */
    public final CallbackDelegate mCallbackDelegate;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final Map<IBinder, IBinder.DeathRecipient> mDeathEaters;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public MediaRouter.RouteInfo mMediaRouteInfo;
    private final MediaRouter mMediaRouter;
    private final MediaRouterCallback mMediaRouterCallback;
    /* access modifiers changed from: private */
    public final CallbackDelegate mOppoCallbackDelegate;
    /* access modifiers changed from: private */
    public MediaProjection mOppoProjectionGrant;
    private IBinder mOppoProjectionToken;
    /* access modifiers changed from: private */
    public final PackageManager mPackageManager;
    /* access modifiers changed from: private */
    public MediaProjection mProjectionGrant;
    private IBinder mProjectionToken;

    public MediaProjectionManagerService(Context context) {
        super(context);
        this.mContext = context;
        this.mDeathEaters = new ArrayMap();
        this.mCallbackDelegate = new CallbackDelegate();
        this.mOppoCallbackDelegate = new CallbackDelegate();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mPackageManager = this.mContext.getPackageManager();
        this.mMediaRouter = (MediaRouter) this.mContext.getSystemService("media_router");
        this.mMediaRouterCallback = new MediaRouterCallback();
        Watchdog.getInstance().addMonitor(this);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.media.projection.MediaProjectionManagerService$BinderService, android.os.IBinder] */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("media_projection", new BinderService(), false);
        this.mMediaRouter.addCallback(4, this.mMediaRouterCallback, 8);
        this.mActivityManagerInternal.registerProcessObserver(new IProcessObserver.Stub() {
            /* class com.android.server.media.projection.MediaProjectionManagerService.AnonymousClass1 */

            public void onForegroundActivitiesChanged(int pid, int uid, boolean fg) {
            }

            public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) {
                MediaProjectionManagerService.this.handleForegroundServicesChanged(pid, uid, serviceTypes);
            }

            public void onProcessDied(int pid, int uid) {
            }
        });
    }

    @Override // com.android.server.SystemService
    public void onSwitchUser(int userId) {
        this.mMediaRouter.rebindAsUser(userId);
        synchronized (this.mLock) {
            if (this.mProjectionGrant != null) {
                this.mProjectionGrant.stop();
            }
        }
        MediaProjection mediaProjection = this.mOppoProjectionGrant;
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
    }

    @Override // com.android.server.Watchdog.Monitor
    public void monitor() {
        synchronized (this.mLock) {
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0040, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0042, code lost:
        return;
     */
    public void handleForegroundServicesChanged(int pid, int uid, int serviceTypes) {
        synchronized (this.mLock) {
            if (this.mProjectionGrant != null) {
                if (this.mProjectionGrant.uid == uid) {
                    if (this.mProjectionGrant.requiresForegroundService()) {
                        if (this.mOppoProjectionGrant != null) {
                            if (this.mOppoProjectionGrant.uid == uid) {
                                if (this.mOppoProjectionGrant.requiresForegroundService()) {
                                    if ((serviceTypes & 32) == 0) {
                                        this.mProjectionGrant.stop();
                                        this.mOppoProjectionGrant.stop();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void startProjectionLocked(MediaProjection projection) {
        if (this.mProjectionGrant != null && !projection.packageName.equals("com.coloros.screenrecorder")) {
            this.mProjectionGrant.stop();
        }
        if (this.mOppoProjectionGrant != null && projection.packageName.equals("com.coloros.screenrecorder")) {
            this.mOppoProjectionGrant.stop();
        }
        MediaRouter.RouteInfo routeInfo = this.mMediaRouteInfo;
        if (!(routeInfo == null || routeInfo.getStatusCode() == 6)) {
            this.mMediaRouter.getFallbackRoute().select();
        }
        if (projection.packageName.equals("com.coloros.screenrecorder")) {
            this.mOppoProjectionGrant = projection;
            this.mOppoProjectionToken = projection.asBinder();
        } else {
            this.mProjectionGrant = projection;
            this.mProjectionToken = projection.asBinder();
        }
        dispatchStart(projection);
    }

    /* access modifiers changed from: private */
    public void stopProjectionLocked(MediaProjection projection) {
        if (projection.packageName.equals("com.coloros.screenrecorder")) {
            this.mOppoProjectionGrant = null;
            this.mOppoProjectionToken = null;
        } else {
            this.mProjectionToken = null;
            this.mProjectionGrant = null;
        }
        dispatchStop(projection);
    }

    /* access modifiers changed from: private */
    public void addCallback(final IMediaProjectionWatcherCallback callback) {
        IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
            /* class com.android.server.media.projection.MediaProjectionManagerService.AnonymousClass2 */

            public void binderDied() {
                MediaProjectionManagerService.this.removeCallback(callback);
            }
        };
        synchronized (this.mLock) {
            this.mCallbackDelegate.add(callback);
            linkDeathRecipientLocked(callback, deathRecipient);
        }
    }

    /* access modifiers changed from: private */
    public void removeCallback(IMediaProjectionWatcherCallback callback) {
        synchronized (this.mLock) {
            unlinkDeathRecipientLocked(callback);
            this.mCallbackDelegate.remove(callback);
        }
    }

    private void linkDeathRecipientLocked(IMediaProjectionWatcherCallback callback, IBinder.DeathRecipient deathRecipient) {
        try {
            IBinder token = callback.asBinder();
            token.linkToDeath(deathRecipient, 0);
            this.mDeathEaters.put(token, deathRecipient);
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to link to death for media projection monitoring callback", e);
        }
    }

    private void unlinkDeathRecipientLocked(IMediaProjectionWatcherCallback callback) {
        IBinder token = callback.asBinder();
        IBinder.DeathRecipient deathRecipient = this.mDeathEaters.remove(token);
        if (deathRecipient != null) {
            token.unlinkToDeath(deathRecipient, 0);
        }
    }

    private void dispatchStart(MediaProjection projection) {
        if (projection.packageName.equals("com.coloros.screenrecorder")) {
            this.mOppoCallbackDelegate.dispatchStart(projection);
        } else {
            this.mCallbackDelegate.dispatchStart(projection);
        }
    }

    private void dispatchStop(MediaProjection projection) {
        if (projection.packageName.equals("com.coloros.screenrecorder")) {
            this.mOppoCallbackDelegate.dispatchStop(projection);
        } else {
            this.mCallbackDelegate.dispatchStop(projection);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001e, code lost:
        return r2;
     */
    public boolean isValidMediaProjection(IBinder token) {
        synchronized (this.mLock) {
            boolean z = false;
            if (this.mProjectionToken == null || this.mOppoProjectionToken == null) {
                if (this.mProjectionToken != null) {
                    boolean equals = this.mProjectionToken.equals(token);
                    return equals;
                } else if (this.mOppoProjectionToken == null) {
                    return false;
                } else {
                    boolean equals2 = this.mOppoProjectionToken.equals(token);
                    return equals2;
                }
            } else if (this.mProjectionToken.equals(token) || this.mOppoProjectionToken.equals(token)) {
                z = true;
            }
        }
    }

    /* access modifiers changed from: private */
    public MediaProjectionInfo getActiveProjectionInfo() {
        synchronized (this.mLock) {
            if (this.mOppoProjectionGrant != null) {
                MediaProjectionInfo projectionInfo = this.mOppoProjectionGrant.getProjectionInfo();
                return projectionInfo;
            } else if (this.mProjectionGrant == null) {
                return null;
            } else {
                MediaProjectionInfo projectionInfo2 = this.mProjectionGrant.getProjectionInfo();
                return projectionInfo2;
            }
        }
    }

    /* access modifiers changed from: private */
    public void dump(PrintWriter pw) {
        pw.println("MEDIA PROJECTION MANAGER (dumpsys media_projection)");
        synchronized (this.mLock) {
            pw.println("Media Projection: ");
            if (this.mProjectionGrant != null) {
                this.mProjectionGrant.dump(pw);
            } else if (this.mOppoProjectionGrant != null) {
                this.mOppoProjectionGrant.dump(pw);
            } else {
                pw.println("null");
            }
        }
    }

    private final class BinderService extends IMediaProjectionManager.Stub {
        private BinderService() {
        }

        public boolean hasProjectionPermission(int uid, String packageName) {
            long token = Binder.clearCallingIdentity();
            try {
                return false | (checkPermission(packageName, "android.permission.CAPTURE_VIDEO_OUTPUT") || MediaProjectionManagerService.this.mAppOps.noteOpNoThrow(46, uid, packageName) == 0);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public IMediaProjection createProjection(int uid, String packageName, int type, boolean isPermanentGrant) {
            if (MediaProjectionManagerService.this.mContext.checkCallingPermission("android.permission.MANAGE_MEDIA_PROJECTION") != 0) {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to grant projection permission");
            } else if (packageName == null || packageName.isEmpty()) {
                throw new IllegalArgumentException("package name must not be empty");
            } else {
                UserHandle callingUser = Binder.getCallingUserHandle();
                long callingToken = Binder.clearCallingIdentity();
                try {
                    ApplicationInfo ai = MediaProjectionManagerService.this.mPackageManager.getApplicationInfoAsUser(packageName, 0, callingUser);
                    try {
                        MediaProjection projection = new MediaProjection(type, uid, packageName, ai.targetSdkVersion, ai.isPrivilegedApp());
                        if (isPermanentGrant) {
                            MediaProjectionManagerService.this.mAppOps.setMode(46, projection.uid, projection.packageName, 0);
                        }
                        return projection;
                    } finally {
                        Binder.restoreCallingIdentity(callingToken);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    throw new IllegalArgumentException("No package matching :" + packageName);
                }
            }
        }

        public boolean isValidMediaProjection(IMediaProjection projection) {
            return MediaProjectionManagerService.this.isValidMediaProjection(projection.asBinder());
        }

        public MediaProjectionInfo getActiveProjectionInfo() {
            if (MediaProjectionManagerService.this.mContext.checkCallingPermission("android.permission.MANAGE_MEDIA_PROJECTION") == 0) {
                long token = Binder.clearCallingIdentity();
                try {
                    return MediaProjectionManagerService.this.getActiveProjectionInfo();
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to add projection callbacks");
            }
        }

        public void stopActiveProjection() {
            if (MediaProjectionManagerService.this.mContext.checkCallingPermission("android.permission.MANAGE_MEDIA_PROJECTION") == 0) {
                long token = Binder.clearCallingIdentity();
                try {
                    if (MediaProjectionManagerService.this.mProjectionGrant != null) {
                        MediaProjectionManagerService.this.mProjectionGrant.stop();
                    }
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to add projection callbacks");
            }
        }

        public void addCallback(IMediaProjectionWatcherCallback callback) {
            if (MediaProjectionManagerService.this.mContext.checkCallingPermission("android.permission.MANAGE_MEDIA_PROJECTION") == 0) {
                long token = Binder.clearCallingIdentity();
                try {
                    MediaProjectionManagerService.this.addCallback(callback);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to add projection callbacks");
            }
        }

        public void removeCallback(IMediaProjectionWatcherCallback callback) {
            if (MediaProjectionManagerService.this.mContext.checkCallingPermission("android.permission.MANAGE_MEDIA_PROJECTION") == 0) {
                long token = Binder.clearCallingIdentity();
                try {
                    MediaProjectionManagerService.this.removeCallback(callback);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to remove projection callbacks");
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(MediaProjectionManagerService.this.mContext, MediaProjectionManagerService.TAG, pw)) {
                long token = Binder.clearCallingIdentity();
                try {
                    MediaProjectionManagerService.this.dump(pw);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }

        private boolean checkPermission(String packageName, String permission) {
            return MediaProjectionManagerService.this.mContext.getPackageManager().checkPermission(permission, packageName) == 0;
        }
    }

    /* access modifiers changed from: private */
    public final class MediaProjection extends IMediaProjection.Stub {
        private IMediaProjectionCallback mCallback;
        private IBinder.DeathRecipient mDeathEater;
        private final boolean mIsPrivileged;
        private final int mTargetSdkVersion;
        private IBinder mToken;
        private int mType;
        public final String packageName;
        public final int uid;
        public final UserHandle userHandle;

        MediaProjection(int type, int uid2, String packageName2, int targetSdkVersion, boolean isPrivileged) {
            this.mType = type;
            this.uid = uid2;
            this.packageName = packageName2;
            this.userHandle = new UserHandle(UserHandle.getUserId(uid2));
            this.mTargetSdkVersion = targetSdkVersion;
            this.mIsPrivileged = isPrivileged;
        }

        public boolean canProjectVideo() {
            int i = this.mType;
            return i == 1 || i == 0;
        }

        public boolean canProjectSecureVideo() {
            return false;
        }

        public boolean canProjectAudio() {
            int i = this.mType;
            return i == 1 || i == 2 || i == 0;
        }

        public int applyVirtualDisplayFlags(int flags) {
            int i = this.mType;
            if (i == 0) {
                return (flags & -9) | 18;
            }
            if (i == 1) {
                return (flags & -18) | 10;
            }
            if (i == 2) {
                return (flags & -9) | 19;
            }
            throw new RuntimeException("Unknown MediaProjection type");
        }

        public void start(final IMediaProjectionCallback callback) {
            if (callback != null) {
                synchronized (MediaProjectionManagerService.this.mLock) {
                    if (MediaProjectionManagerService.this.isValidMediaProjection(asBinder())) {
                        Slog.w(MediaProjectionManagerService.TAG, "UID " + Binder.getCallingUid() + " attempted to start already started MediaProjection");
                        return;
                    }
                    if (requiresForegroundService()) {
                        if (!MediaProjectionManagerService.this.mActivityManagerInternal.hasRunningForegroundService(this.uid, 32)) {
                            throw new SecurityException("Media projections require a foreground service of type ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION");
                        }
                    }
                    this.mCallback = callback;
                    registerCallback(this.mCallback);
                    try {
                        this.mToken = callback.asBinder();
                        this.mDeathEater = new IBinder.DeathRecipient() {
                            /* class com.android.server.media.projection.MediaProjectionManagerService.MediaProjection.AnonymousClass1 */

                            public void binderDied() {
                                MediaProjection.this.unregisterCallback(callback);
                                MediaProjection.this.stop();
                            }
                        };
                        this.mToken.linkToDeath(this.mDeathEater, 0);
                        MediaProjectionManagerService.this.startProjectionLocked(this);
                    } catch (RemoteException e) {
                        Slog.w(MediaProjectionManagerService.TAG, "MediaProjectionCallbacks must be valid, aborting MediaProjection", e);
                    }
                }
            } else {
                throw new IllegalArgumentException("callback must not be null");
            }
        }

        public void stop() {
            synchronized (MediaProjectionManagerService.this.mLock) {
                if (!MediaProjectionManagerService.this.isValidMediaProjection(asBinder())) {
                    Slog.w(MediaProjectionManagerService.TAG, "Attempted to stop inactive MediaProjection (uid=" + Binder.getCallingUid() + ", pid=" + Binder.getCallingPid() + ")");
                    return;
                }
                MediaProjectionManagerService.this.stopProjectionLocked(this);
                this.mToken.unlinkToDeath(this.mDeathEater, 0);
                this.mToken = null;
                unregisterCallback(this.mCallback);
                this.mCallback = null;
            }
        }

        public void registerCallback(IMediaProjectionCallback callback) {
            if (callback == null) {
                throw new IllegalArgumentException("callback must not be null");
            } else if (this.packageName.equals("com.coloros.screenrecorder")) {
                MediaProjectionManagerService.this.mOppoCallbackDelegate.add(callback);
            } else {
                MediaProjectionManagerService.this.mCallbackDelegate.add(callback);
            }
        }

        public void unregisterCallback(IMediaProjectionCallback callback) {
            if (callback == null) {
                throw new IllegalArgumentException("callback must not be null");
            } else if (this.packageName.equals("com.coloros.screenrecorder")) {
                MediaProjectionManagerService.this.mOppoCallbackDelegate.remove(callback);
            } else {
                MediaProjectionManagerService.this.mCallbackDelegate.remove(callback);
            }
        }

        public MediaProjectionInfo getProjectionInfo() {
            return new MediaProjectionInfo(this.packageName, this.userHandle);
        }

        /* access modifiers changed from: package-private */
        public boolean requiresForegroundService() {
            return this.mTargetSdkVersion >= 29 && !this.mIsPrivileged;
        }

        public void dump(PrintWriter pw) {
            pw.println("(" + this.packageName + ", uid=" + this.uid + "): " + MediaProjectionManagerService.typeToString(this.mType));
        }
    }

    private class MediaRouterCallback extends MediaRouter.SimpleCallback {
        private MediaRouterCallback() {
        }

        public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
            synchronized (MediaProjectionManagerService.this.mLock) {
                if ((type & 4) != 0) {
                    MediaRouter.RouteInfo unused = MediaProjectionManagerService.this.mMediaRouteInfo = info;
                    if (MediaProjectionManagerService.this.mProjectionGrant != null) {
                        MediaProjectionManagerService.this.mProjectionGrant.stop();
                    }
                    if (MediaProjectionManagerService.this.mOppoProjectionGrant != null) {
                        MediaProjectionManagerService.this.mOppoProjectionGrant.stop();
                    }
                }
            }
        }

        public void onRouteUnselected(MediaRouter route, int type, MediaRouter.RouteInfo info) {
            if (MediaProjectionManagerService.this.mMediaRouteInfo == info) {
                MediaRouter.RouteInfo unused = MediaProjectionManagerService.this.mMediaRouteInfo = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class CallbackDelegate {
        private Map<IBinder, IMediaProjectionCallback> mClientCallbacks = new ArrayMap();
        private Handler mHandler = new Handler(Looper.getMainLooper(), null, true);
        private Object mLock = new Object();
        private Map<IBinder, IMediaProjectionWatcherCallback> mWatcherCallbacks = new ArrayMap();

        public void add(IMediaProjectionCallback callback) {
            synchronized (this.mLock) {
                this.mClientCallbacks.put(callback.asBinder(), callback);
            }
        }

        public void add(IMediaProjectionWatcherCallback callback) {
            synchronized (this.mLock) {
                this.mWatcherCallbacks.put(callback.asBinder(), callback);
            }
        }

        public void remove(IMediaProjectionCallback callback) {
            synchronized (this.mLock) {
                this.mClientCallbacks.remove(callback.asBinder());
            }
        }

        public void remove(IMediaProjectionWatcherCallback callback) {
            synchronized (this.mLock) {
                this.mWatcherCallbacks.remove(callback.asBinder());
            }
        }

        public void dispatchStart(MediaProjection projection) {
            if (projection == null) {
                Slog.e(MediaProjectionManagerService.TAG, "Tried to dispatch start notification for a null media projection. Ignoring!");
                return;
            }
            synchronized (this.mLock) {
                for (IMediaProjectionWatcherCallback callback : this.mWatcherCallbacks.values()) {
                    this.mHandler.post(new WatcherStartCallback(projection.getProjectionInfo(), callback));
                }
            }
        }

        public void dispatchStop(MediaProjection projection) {
            if (projection == null) {
                Slog.e(MediaProjectionManagerService.TAG, "Tried to dispatch stop notification for a null media projection. Ignoring!");
                return;
            }
            synchronized (this.mLock) {
                for (IMediaProjectionCallback callback : this.mClientCallbacks.values()) {
                    this.mHandler.post(new ClientStopCallback(callback));
                }
                for (IMediaProjectionWatcherCallback callback2 : this.mWatcherCallbacks.values()) {
                    this.mHandler.post(new WatcherStopCallback(projection.getProjectionInfo(), callback2));
                }
            }
        }
    }

    private static final class WatcherStartCallback implements Runnable {
        private IMediaProjectionWatcherCallback mCallback;
        private MediaProjectionInfo mInfo;

        public WatcherStartCallback(MediaProjectionInfo info, IMediaProjectionWatcherCallback callback) {
            this.mInfo = info;
            this.mCallback = callback;
        }

        public void run() {
            try {
                this.mCallback.onStart(this.mInfo);
            } catch (RemoteException e) {
                Slog.w(MediaProjectionManagerService.TAG, "Failed to notify media projection has stopped", e);
            }
        }
    }

    private static final class WatcherStopCallback implements Runnable {
        private IMediaProjectionWatcherCallback mCallback;
        private MediaProjectionInfo mInfo;

        public WatcherStopCallback(MediaProjectionInfo info, IMediaProjectionWatcherCallback callback) {
            this.mInfo = info;
            this.mCallback = callback;
        }

        public void run() {
            try {
                this.mCallback.onStop(this.mInfo);
            } catch (RemoteException e) {
                Slog.w(MediaProjectionManagerService.TAG, "Failed to notify media projection has stopped", e);
            }
        }
    }

    private static final class ClientStopCallback implements Runnable {
        private IMediaProjectionCallback mCallback;

        public ClientStopCallback(IMediaProjectionCallback callback) {
            this.mCallback = callback;
        }

        public void run() {
            try {
                this.mCallback.onStop();
            } catch (RemoteException e) {
                Slog.w(MediaProjectionManagerService.TAG, "Failed to notify media projection has stopped", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public static String typeToString(int type) {
        if (type == 0) {
            return "TYPE_SCREEN_CAPTURE";
        }
        if (type == 1) {
            return "TYPE_MIRRORING";
        }
        if (type != 2) {
            return Integer.toString(type);
        }
        return "TYPE_PRESENTATION";
    }
}
