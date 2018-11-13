package com.android.server.wallpaper;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IUserSwitchObserver;
import android.app.IWallpaperManager.Stub;
import android.app.IWallpaperManagerCallback;
import android.app.PendingIntent;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.wallpaper.IWallpaperConnection;
import android.service.wallpaper.IWallpaperEngine;
import android.service.wallpaper.IWallpaperService;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.view.Display;
import android.view.IWindowManager;
import android.view.WindowManager;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.JournaledFile;
import com.android.server.EventLogTags;
import com.android.server.FgThread;
import com.android.server.LocationManagerService;
import com.android.server.SystemService;
import com.android.server.oppo.IElsaManager;
import com.android.server.secrecy.policy.DecryptTool;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
@OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "gaoliang@Plf.Keyguard, 2012.08.27:[- +public @hide]modify for oppo wallpaper.", property = OppoRomType.ROM)
public class WallpaperManagerService extends Stub {
    private static final String ACTION_BG_RELEASE = "ACTION_BG_RELEASE";
    static final boolean DEBUG = false;
    static final int MAX_WALLPAPER_COMPONENT_LOG_LENGTH = 128;
    static final long MIN_WALLPAPER_CRASH_TIME = 10000;
    private static final int MSG_BIND_WP = 10101;
    static final String TAG = "WallpaperManagerService";
    static final String WALLPAPER = "wallpaper_orig";
    static final String WALLPAPER_CROP = "wallpaper";
    static final String WALLPAPER_INFO = "wallpaper_info.xml";
    static final String WALLPAPER_LOCK_CROP = "wallpaper_lock";
    static final String WALLPAPER_LOCK_ORIG = "wallpaper_lock_orig";
    static final String[] sPerUserFiles = null;
    final AppOpsManager mAppOpsManager;
    final Context mContext;
    int mCurrentUserId;
    private boolean mExpectedLiving;
    private final Handler mHandler;
    final IPackageManager mIPackageManager;
    final IWindowManager mIWindowManager;
    final ComponentName mImageWallpaper;
    IWallpaperManagerCallback mKeyguardListener;
    private Intent mLastIntent;
    WallpaperData mLastWallpaper;
    final Object mLock;
    final SparseArray<WallpaperData> mLockWallpaperMap;
    final MyPackageMonitor mMonitor;
    boolean mShuttingDown;
    final SparseArray<Boolean> mUserRestorecon;
    private boolean mVisible;
    boolean mWaitingForUnlock;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "gaoliang@Plf.Keyguard, 2012.08.27:add for oppo-wallpaper", property = OppoRomType.ROM)
    private WallpaperHelper mWallpaperHelper;
    int mWallpaperId;
    final SparseArray<WallpaperData> mWallpaperMap;

    public static class Lifecycle extends SystemService {
        private WallpaperManagerService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            this.mService = new WallpaperManagerService(getContext());
            publishBinderService(WallpaperManagerService.WALLPAPER_CROP, this.mService);
        }

        public void onBootPhase(int phase) {
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
                this.mService.systemReady();
            } else if (phase == 600) {
                this.mService.switchUser(0, null);
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }
    }

    class MyPackageMonitor extends PackageMonitor {
        MyPackageMonitor() {
        }

        /* JADX WARNING: Missing block: B:18:0x005a, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onPackageUpdateFinished(String packageName, int uid) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (!(wallpaper == null || wallpaper.wallpaperComponent == null || !wallpaper.wallpaperComponent.getPackageName().equals(packageName))) {
                    wallpaper.wallpaperUpdating = false;
                    ComponentName comp = wallpaper.wallpaperComponent;
                    WallpaperManagerService.this.clearWallpaperComponentLocked(wallpaper);
                    if (!WallpaperManagerService.this.bindWallpaperComponentLocked(comp, false, false, wallpaper, null)) {
                        Slog.w(WallpaperManagerService.TAG, "Wallpaper no longer available; reverting to default");
                        WallpaperManagerService.this.clearWallpaperLocked(false, 1, wallpaper.userId, null);
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:16:0x0036, code:
            return;
     */
        /* JADX WARNING: Missing block: B:18:0x0038, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onPackageModified(String packageName) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (wallpaper != null) {
                    if (wallpaper.wallpaperComponent == null || !wallpaper.wallpaperComponent.getPackageName().equals(packageName)) {
                    } else {
                        doPackagesChangedLocked(true, wallpaper);
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:18:0x0046, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onPackageUpdateStarted(String packageName, int uid) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (!(wallpaper == null || wallpaper.wallpaperComponent == null || !wallpaper.wallpaperComponent.getPackageName().equals(packageName))) {
                    wallpaper.wallpaperUpdating = true;
                    if (wallpaper.connection != null) {
                        FgThread.getHandler().removeCallbacks(wallpaper.connection.mResetRunnable);
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:15:0x0029, code:
            return r0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            synchronized (WallpaperManagerService.this.mLock) {
                boolean changed = false;
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return false;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (wallpaper != null) {
                    changed = doPackagesChangedLocked(doit, wallpaper);
                }
            }
        }

        /* JADX WARNING: Missing block: B:12:0x0026, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onSomePackagesChanged() {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (wallpaper != null) {
                    doPackagesChangedLocked(true, wallpaper);
                }
            }
        }

        boolean doPackagesChangedLocked(boolean doit, WallpaperData wallpaper) {
            int change;
            boolean changed = false;
            if (wallpaper.wallpaperComponent != null) {
                change = isPackageDisappearing(wallpaper.wallpaperComponent.getPackageName());
                if (change == 3 || change == 2) {
                    changed = true;
                    if (doit) {
                        Slog.w(WallpaperManagerService.TAG, "Wallpaper uninstalled, removing: " + wallpaper.wallpaperComponent);
                        WallpaperManagerService.this.clearWallpaperLocked(false, 1, wallpaper.userId, null);
                    }
                }
            }
            if (wallpaper.nextWallpaperComponent != null) {
                change = isPackageDisappearing(wallpaper.nextWallpaperComponent.getPackageName());
                if (change == 3 || change == 2) {
                    wallpaper.nextWallpaperComponent = null;
                }
            }
            if (wallpaper.wallpaperComponent != null && isPackageModified(wallpaper.wallpaperComponent.getPackageName())) {
                try {
                    WallpaperManagerService.this.mContext.getPackageManager().getServiceInfo(wallpaper.wallpaperComponent, 786432);
                } catch (NameNotFoundException e) {
                    Slog.w(WallpaperManagerService.TAG, "Wallpaper component gone, removing: " + wallpaper.wallpaperComponent);
                    WallpaperManagerService.this.clearWallpaperLocked(false, 1, wallpaper.userId, null);
                }
            }
            if (wallpaper.nextWallpaperComponent != null && isPackageModified(wallpaper.nextWallpaperComponent.getPackageName())) {
                try {
                    WallpaperManagerService.this.mContext.getPackageManager().getServiceInfo(wallpaper.nextWallpaperComponent, 786432);
                } catch (NameNotFoundException e2) {
                    wallpaper.nextWallpaperComponent = null;
                }
            }
            return changed;
        }
    }

    class WallpaperConnection extends IWallpaperConnection.Stub implements ServiceConnection {
        private static final long WALLPAPER_RECONNECT_TIMEOUT_MS = 5000;
        boolean mDimensionsChanged = false;
        IWallpaperEngine mEngine;
        final WallpaperInfo mInfo;
        boolean mPaddingChanged = false;
        IRemoteCallback mReply;
        private Runnable mResetRunnable = new WallpaperManagerService$WallpaperConnection$-void__init__com_android_server_wallpaper_WallpaperManagerService_this$0_android_app_WallpaperInfo_info_com_android_server_wallpaper_WallpaperManagerService$WallpaperData_wallpaper_LambdaImpl0(this);
        IWallpaperService mService;
        final Binder mToken = new Binder();
        WallpaperData mWallpaper;

        /* JADX WARNING: Missing block: B:14:0x0033, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        /* renamed from: -com_android_server_wallpaper_WallpaperManagerService$WallpaperConnection_lambda$1 */
        /* synthetic */ void m12xb0e1252b() {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mShuttingDown) {
                } else if (!this.mWallpaper.wallpaperUpdating && this.mWallpaper.userId == WallpaperManagerService.this.mCurrentUserId) {
                    Slog.w(WallpaperManagerService.TAG, "Wallpaper reconnect timed out, reverting to built-in wallpaper!");
                    WallpaperManagerService.this.clearWallpaperLocked(true, 1, this.mWallpaper.userId, null);
                }
            }
        }

        public WallpaperConnection(WallpaperInfo info, WallpaperData wallpaper) {
            this.mInfo = info;
            this.mWallpaper = wallpaper;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (this.mWallpaper.connection == this) {
                    WallpaperManagerService.this.mExpectedLiving = true;
                    this.mService = IWallpaperService.Stub.asInterface(service);
                    WallpaperManagerService.this.attachServiceLocked(this, this.mWallpaper);
                    WallpaperManagerService.this.saveSettingsLocked(this.mWallpaper.userId);
                    FgThread.getHandler().removeCallbacks(this.mResetRunnable);
                }
            }
        }

        /* JADX WARNING: Missing block: B:26:0x00e5, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onServiceDisconnected(ComponentName name) {
            synchronized (WallpaperManagerService.this.mLock) {
                this.mService = null;
                this.mEngine = null;
                Slog.w(WallpaperManagerService.TAG, "onServiceDisconnected(): " + name);
                WallpaperManagerService.this.mExpectedLiving = false;
                if (WallpaperManagerService.this.isGmoRamOptimizeSupport() && !WallpaperManagerService.this.mVisible) {
                } else if (this.mWallpaper.connection == this) {
                    Slog.w(WallpaperManagerService.TAG, "Wallpaper service gone: " + this.mWallpaper.wallpaperComponent);
                    if (!this.mWallpaper.wallpaperUpdating && this.mWallpaper.userId == WallpaperManagerService.this.mCurrentUserId) {
                        if (this.mWallpaper.lastDiedTime == 0 || this.mWallpaper.lastDiedTime + 10000 <= SystemClock.uptimeMillis()) {
                            this.mWallpaper.lastDiedTime = SystemClock.uptimeMillis();
                            FgThread.getHandler().removeCallbacks(this.mResetRunnable);
                            FgThread.getHandler().postDelayed(this.mResetRunnable, 5000);
                        } else {
                            Slog.w(WallpaperManagerService.TAG, "Reverting to built-in wallpaper!");
                            boolean isImageWallpaper = WallpaperManagerService.this.mImageWallpaper != null ? WallpaperManagerService.this.mImageWallpaper.equals(this.mWallpaper.wallpaperComponent) : false;
                            Slog.d(WallpaperManagerService.TAG, "onServiceDisconnected isImageWallpaper = " + isImageWallpaper);
                            if (!isImageWallpaper) {
                                WallpaperManagerService.this.clearWallpaperLocked(true, 1, this.mWallpaper.userId, null);
                            }
                        }
                        String flattened = name.flattenToString();
                        EventLog.writeEvent(EventLogTags.WP_WALLPAPER_CRASHED, flattened.substring(0, Math.min(flattened.length(), 128)));
                    }
                }
            }
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.LauncherCenter, 2016.10.01:add for if wallpaper changed, send wallpaper change broadcast", property = OppoRomType.ROM)
        public void attachEngine(IWallpaperEngine engine) {
            synchronized (WallpaperManagerService.this.mLock) {
                this.mEngine = engine;
                WallpaperManagerService.this.notifyCallbacksLocked(this.mWallpaper);
                if (this.mDimensionsChanged) {
                    try {
                        this.mEngine.setDesiredSize(this.mWallpaper.width, this.mWallpaper.height);
                    } catch (RemoteException e) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to set wallpaper dimensions", e);
                    }
                    this.mDimensionsChanged = false;
                }
                if (this.mPaddingChanged) {
                    try {
                        this.mEngine.setDisplayPadding(this.mWallpaper.padding);
                    } catch (RemoteException e2) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to set wallpaper padding", e2);
                    }
                    this.mPaddingChanged = false;
                }
            }
            return;
        }

        public void engineShown(IWallpaperEngine engine) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (this.mReply != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        this.mReply.sendResult(null);
                    } catch (RemoteException e) {
                        Binder.restoreCallingIdentity(ident);
                    }
                    this.mReply = null;
                }
            }
            return;
        }

        public ParcelFileDescriptor setWallpaper(String name) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (this.mWallpaper.connection == this) {
                    ParcelFileDescriptor updateWallpaperBitmapLocked = WallpaperManagerService.this.updateWallpaperBitmapLocked(name, this.mWallpaper, null);
                    return updateWallpaperBitmapLocked;
                }
                return null;
            }
        }
    }

    static class WallpaperData {
        boolean allowBackup;
        private RemoteCallbackList<IWallpaperManagerCallback> callbacks = new RemoteCallbackList();
        WallpaperConnection connection;
        final File cropFile;
        final Rect cropHint = new Rect(0, 0, 0, 0);
        int height = -1;
        boolean imageWallpaperPending;
        long lastDiedTime;
        String name = IElsaManager.EMPTY_PACKAGE;
        ComponentName nextWallpaperComponent;
        final Rect padding = new Rect(0, 0, 0, 0);
        IWallpaperManagerCallback setComplete;
        int userId;
        ComponentName wallpaperComponent;
        final File wallpaperFile;
        int wallpaperId;
        WallpaperObserver wallpaperObserver;
        boolean wallpaperUpdating;
        int whichPending;
        int width = -1;

        WallpaperData(int userId, String inputFileName, String cropFileName) {
            this.userId = userId;
            File wallpaperDir = WallpaperManagerService.getWallpaperDir(userId);
            this.wallpaperFile = new File(wallpaperDir, inputFileName);
            this.cropFile = new File(wallpaperDir, cropFileName);
        }

        boolean cropExists() {
            return this.cropFile.exists();
        }
    }

    @OppoHook(level = OppoHookType.NEW_CLASS, note = "gaoliang@Plf.Keyguard, 2012.08.27:add for oppo-wallpaper", property = OppoRomType.ROM)
    public class WallpaperHelper {
        private static final String FORBID_SETTING_WALLPAPER_STRING = "oppo.forbid.setting.wallpaper";
        public final float WALLPAPER_SCREENS_SPAN = 2.0f;
        private boolean mIsExpVersion = false;
        private boolean mIsForbidSettingWallpaper = false;
        private int mWidthOfDefaultWallpaper = -1;

        public WallpaperHelper(Context context) {
            this.mWidthOfDefaultWallpaper = getDefaultWallpaperWidth(context);
            this.mIsExpVersion = context.getPackageManager().hasSystemFeature("oppo.version.exp");
            this.mIsForbidSettingWallpaper = context.getPackageManager().hasSystemFeature(FORBID_SETTING_WALLPAPER_STRING);
        }

        private int getDefaultWallpaperWidth(Context context) {
            int width = -1;
            try {
                Resources res = context.getResources();
                Options options = new Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(res, WallpaperManager.getDefaultWallpaperResID(context), options);
                width = options.outWidth;
                Slog.w(WallpaperManagerService.TAG, "getDefaultWallpaperWidth(): width = " + width);
                return width;
            } catch (OutOfMemoryError e) {
                Slog.w(WallpaperManagerService.TAG, "getDefaultWallpaperWidth(): Can't decode res:", e);
                return width;
            }
        }

        public void setDimensionHints_extra(int width, int height) throws RemoteException {
            WallpaperManagerService.this.checkPermission("android.permission.SET_WALLPAPER_HINTS");
            synchronized (WallpaperManagerService.this.mLock) {
                int userId = UserHandle.getCallingUserId();
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(userId);
                if (wallpaper == null) {
                    throw new IllegalStateException("Wallpaper not yet initialized for user " + userId);
                } else if (width <= 0 || height <= 0) {
                    throw new IllegalArgumentException("width and height must be > 0");
                } else {
                    Point displaySize = WallpaperManagerService.this.getDefaultDisplaySize();
                    width = Math.max(width, displaySize.x);
                    height = Math.max(height, displaySize.y);
                    if (!(width == wallpaper.width && height == wallpaper.height)) {
                        wallpaper.width = width;
                        wallpaper.height = height;
                        WallpaperManagerService.this.saveSettingsLocked(userId);
                    }
                }
            }
        }

        private void getCurrentImageWallpaperInfo(Point bmpInfo, int userId) {
            Bundle params = new Bundle();
            ParcelFileDescriptor fd = WallpaperManagerService.this.getWallpaper(new IWallpaperManagerCallback.Stub() {
                public void onWallpaperChanged() {
                }
            }, 1, params, userId);
            if (fd != null) {
                try {
                    Options options = new Options();
                    options.inJustDecodeBounds = true;
                    Bitmap bm = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);
                    bmpInfo.x = options.outWidth;
                    bmpInfo.y = options.outHeight;
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                } catch (OutOfMemoryError e2) {
                    Slog.w(WallpaperManagerService.TAG, "getCurrentImageWallpaperInfo(): Can't decode file", e2);
                    try {
                        fd.close();
                    } catch (IOException e3) {
                    }
                } catch (Throwable th) {
                    try {
                        fd.close();
                    } catch (IOException e4) {
                    }
                    throw th;
                }
            }
        }

        private void adjustWallpaperWidth(int userId) {
            try {
                int desiredMinimumWidth = WallpaperManagerService.this.getWidthHint();
                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((WindowManager) WallpaperManagerService.this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
                int maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
                int minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
                int dimWidth = minDim;
                int dimHeight = maxDim;
                Point bitmapSize = new Point(-1, -1);
                getCurrentImageWallpaperInfo(bitmapSize, userId);
                Slog.d(WallpaperManagerService.TAG, "new bitmap width = " + bitmapSize.x);
                Slog.d(WallpaperManagerService.TAG, "new bitmap height = " + bitmapSize.y);
                if (bitmapSize.x > 0) {
                    int bmWidth = bitmapSize.x;
                    int bmHeight = bitmapSize.y;
                    float ratio = 1.0f;
                    if (bmHeight < maxDim) {
                        ratio = (float) (maxDim / bmHeight);
                    }
                    if (((float) bmWidth) * ratio <= ((float) minDim) && desiredMinimumWidth != minDim) {
                        setDimensionHints_extra(minDim, maxDim);
                    } else if (((float) bmWidth) * ratio > ((float) minDim) && desiredMinimumWidth <= minDim) {
                        setDimensionHints_extra(Math.max((int) (((float) minDim) * 2.0f), maxDim), maxDim);
                    }
                } else if (!(-1 == this.mWidthOfDefaultWallpaper || desiredMinimumWidth == this.mWidthOfDefaultWallpaper)) {
                    setDimensionHints_extra(this.mWidthOfDefaultWallpaper, maxDim);
                }
            } catch (RemoteException e) {
            }
        }

        private boolean isWallpaperExist(int userId) {
            return new File(WallpaperManagerService.getWallpaperDir(userId), WallpaperManagerService.WALLPAPER).exists();
        }

        private boolean isThirdPartApp(String packageName) {
            if (TextUtils.isEmpty(packageName)) {
                return true;
            }
            try {
                ApplicationInfo appInfo = WallpaperManagerService.this.mContext.getPackageManager().getApplicationInfo(packageName, DumpState.DUMP_PREFERRED_XML);
                if (appInfo != null && (appInfo.flags & 1) == 1) {
                    return false;
                }
            } catch (Exception e) {
                Slog.d(WallpaperManagerService.TAG, "isThirdPartApp e = " + e);
            }
            return true;
        }

        private boolean isThirdPartLauncherCall(String callingPackage) {
            boolean z = false;
            if (TextUtils.isEmpty(callingPackage) || !isThirdPartApp(callingPackage)) {
                return false;
            }
            List apps = null;
            try {
                PackageManager packageManager = WallpaperManagerService.this.mContext.getPackageManager();
                Intent mainIntent = new Intent("android.intent.action.MAIN");
                mainIntent.addCategory("android.intent.category.HOME");
                mainIntent.setPackage(callingPackage);
                apps = packageManager.queryIntentActivities(mainIntent, 0);
            } catch (Exception e) {
                Slog.d(WallpaperManagerService.TAG, "isThirdPartLauncherCall e = " + e);
            }
            if (apps != null && apps.size() > 0) {
                z = true;
            }
            return z;
        }

        private boolean isForbidSettingWallpaperVersion() {
            return this.mIsForbidSettingWallpaper;
        }

        private boolean isExpVersion() {
            return this.mIsExpVersion;
        }
    }

    private class WallpaperObserver extends FileObserver {
        final int mUserId;
        final WallpaperData mWallpaper;
        final File mWallpaperDir;
        final File mWallpaperFile = new File(this.mWallpaperDir, WallpaperManagerService.WALLPAPER);
        final File mWallpaperLockFile = new File(this.mWallpaperDir, WallpaperManagerService.WALLPAPER_LOCK_ORIG);

        public WallpaperObserver(WallpaperData wallpaper) {
            super(WallpaperManagerService.getWallpaperDir(wallpaper.userId).getAbsolutePath(), 1672);
            this.mUserId = wallpaper.userId;
            this.mWallpaperDir = WallpaperManagerService.getWallpaperDir(wallpaper.userId);
            this.mWallpaper = wallpaper;
        }

        private WallpaperData dataForEvent(boolean sysChanged, boolean lockChanged) {
            WallpaperData wallpaper = null;
            synchronized (WallpaperManagerService.this.mLock) {
                if (lockChanged) {
                    wallpaper = (WallpaperData) WallpaperManagerService.this.mLockWallpaperMap.get(this.mUserId);
                }
                if (wallpaper == null) {
                    wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(this.mUserId);
                }
            }
            if (wallpaper != null) {
                return wallpaper;
            }
            return this.mWallpaper;
        }

        /* JADX WARNING: Missing block: B:45:0x00a3, code:
            if (r4.imageWallpaperPending != false) goto L_0x0044;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.Keyguard, 2012.08.27:add for oppo-wallpaper", property = OppoRomType.ROM)
        public void onEvent(int event, String path) {
            if (path != null) {
                boolean moved = event == 128;
                boolean written = event != 8 ? moved : true;
                File changedFile = new File(this.mWallpaperDir, path);
                boolean sysWallpaperChanged = this.mWallpaperFile.equals(changedFile);
                boolean lockWallpaperChanged = this.mWallpaperLockFile.equals(changedFile);
                WallpaperData wallpaper = dataForEvent(sysWallpaperChanged, lockWallpaperChanged);
                if (moved && lockWallpaperChanged) {
                    SELinux.restorecon(changedFile);
                    WallpaperManagerService.this.notifyLockWallpaperChanged();
                    return;
                }
                synchronized (WallpaperManagerService.this.mLock) {
                    if (sysWallpaperChanged || lockWallpaperChanged) {
                        if (wallpaper.wallpaperComponent != null && event == 8) {
                        }
                        if (written) {
                            SELinux.restorecon(changedFile);
                            if (moved) {
                                SELinux.restorecon(changedFile);
                                WallpaperManagerService.this.loadSettingsLocked(wallpaper.userId, true);
                            }
                            WallpaperManagerService.this.generateCrop(wallpaper);
                            wallpaper.imageWallpaperPending = false;
                            if (wallpaper.setComplete != null) {
                                try {
                                    wallpaper.setComplete.onWallpaperChanged();
                                } catch (RemoteException e) {
                                }
                            }
                            if (sysWallpaperChanged) {
                                WallpaperManagerService.this.mWallpaperHelper.adjustWallpaperWidth(wallpaper.userId);
                                WallpaperManagerService.this.bindWallpaperComponentLocked(WallpaperManagerService.this.mImageWallpaper, true, false, wallpaper, null);
                            }
                            if (lockWallpaperChanged || (wallpaper.whichPending & 2) != 0) {
                                if (!lockWallpaperChanged) {
                                    WallpaperManagerService.this.mLockWallpaperMap.remove(wallpaper.userId);
                                }
                                WallpaperManagerService.this.notifyLockWallpaperChanged();
                            }
                            WallpaperManagerService.this.saveSettingsLocked(wallpaper.userId);
                        }
                    }
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wallpaper.WallpaperManagerService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wallpaper.WallpaperManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wallpaper.WallpaperManagerService.<clinit>():void");
    }

    void notifyLockWallpaperChanged() {
        IWallpaperManagerCallback cb = this.mKeyguardListener;
        if (cb != null) {
            try {
                cb.onWallpaperChanged();
            } catch (RemoteException e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:95:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:95:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:95:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x025a A:{Catch:{ all -> 0x0283 }} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0269 A:{Catch:{ all -> 0x0283 }} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:95:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:95:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x025a A:{Catch:{ all -> 0x0283 }} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0269 A:{Catch:{ all -> 0x0283 }} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:95:? A:{SYNTHETIC, RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void generateCrop(WallpaperData wallpaper) {
        BufferedOutputStream bos;
        Object bos2;
        Throwable th;
        boolean success = false;
        Rect cropHint = new Rect(wallpaper.cropHint);
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(wallpaper.wallpaperFile.getAbsolutePath(), options);
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            Slog.e(TAG, "Invalid wallpaper data");
            success = false;
        } else {
            boolean needCrop = false;
            if (cropHint.isEmpty()) {
                cropHint.top = 0;
                cropHint.left = 0;
                cropHint.right = options.outWidth;
                cropHint.bottom = options.outHeight;
            } else {
                cropHint.offset(cropHint.right > options.outWidth ? options.outWidth - cropHint.right : 0, cropHint.bottom > options.outHeight ? options.outHeight - cropHint.bottom : 0);
                if (cropHint.left < 0) {
                    cropHint.left = 0;
                }
                if (cropHint.top < 0) {
                    cropHint.top = 0;
                }
                if (options.outHeight <= cropHint.height()) {
                    if (options.outWidth > cropHint.width()) {
                        needCrop = true;
                    } else {
                        needCrop = false;
                    }
                } else {
                    needCrop = true;
                }
            }
            boolean needScale = wallpaper.height != cropHint.height();
            if (needCrop || needScale) {
                AutoCloseable f = null;
                AutoCloseable bos3 = null;
                BitmapRegionDecoder bitmapRegionDecoder = null;
                try {
                    Options scaler;
                    bitmapRegionDecoder = BitmapRegionDecoder.newInstance(wallpaper.wallpaperFile.getAbsolutePath(), false);
                    int scale = 1;
                    while (scale * 2 < cropHint.height() / wallpaper.height) {
                        scale *= 2;
                    }
                    if (scale > 1) {
                        scaler = new Options();
                        scaler.inSampleSize = scale;
                    } else {
                        scaler = null;
                    }
                    Bitmap cropped = bitmapRegionDecoder.decodeRegion(cropHint, scaler);
                    bitmapRegionDecoder.recycle();
                    if (cropped == null) {
                        Slog.e(TAG, "Could not decode new wallpaper");
                    } else {
                        cropHint.offsetTo(0, 0);
                        cropHint.right /= scale;
                        cropHint.bottom /= scale;
                        Bitmap finalCrop = Bitmap.createScaledBitmap(cropped, (int) (((float) cropHint.width()) * (((float) wallpaper.height) / ((float) cropHint.height()))), wallpaper.height, true);
                        FileOutputStream f2 = new FileOutputStream(wallpaper.cropFile);
                        Object f3;
                        try {
                            BufferedOutputStream bos4 = new BufferedOutputStream(f2, 32768);
                            try {
                                finalCrop.compress(CompressFormat.JPEG, 100, bos4);
                                bos4.flush();
                                success = true;
                                bos = bos4;
                                f3 = f2;
                            } catch (Exception e) {
                                bos = bos4;
                                f3 = f2;
                                IoUtils.closeQuietly(bos);
                                IoUtils.closeQuietly(f);
                                if (!success) {
                                }
                                if (wallpaper.cropFile.exists()) {
                                }
                            } catch (OutOfMemoryError e2) {
                                bos2 = bos4;
                                f3 = f2;
                                try {
                                    Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + bitmapRegionDecoder);
                                    if (bitmapRegionDecoder != null) {
                                    }
                                    if (!wallpaper.wallpaperFile.delete()) {
                                    }
                                    IoUtils.closeQuietly(bos3);
                                    IoUtils.closeQuietly(f);
                                    if (success) {
                                    }
                                    if (wallpaper.cropFile.exists()) {
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    IoUtils.closeQuietly(bos3);
                                    IoUtils.closeQuietly(f);
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                bos2 = bos4;
                                f3 = f2;
                                IoUtils.closeQuietly(bos3);
                                IoUtils.closeQuietly(f);
                                throw th;
                            }
                        } catch (Exception e3) {
                            f3 = f2;
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(f);
                            if (success) {
                            }
                            if (wallpaper.cropFile.exists()) {
                            }
                        } catch (OutOfMemoryError e4) {
                            f3 = f2;
                            Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + bitmapRegionDecoder);
                            if (bitmapRegionDecoder != null) {
                                bitmapRegionDecoder.recycle();
                            }
                            if (wallpaper.wallpaperFile.delete()) {
                                Slog.e(TAG, "generateCrop wallpaper.wallpaperFile.delete() fail!");
                            }
                            IoUtils.closeQuietly(bos3);
                            IoUtils.closeQuietly(f);
                            if (success) {
                            }
                            if (wallpaper.cropFile.exists()) {
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            f3 = f2;
                            IoUtils.closeQuietly(bos3);
                            IoUtils.closeQuietly(f);
                            throw th;
                        }
                    }
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(f);
                } catch (Exception e5) {
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(f);
                    if (success) {
                    }
                    if (wallpaper.cropFile.exists()) {
                    }
                } catch (OutOfMemoryError e6) {
                    Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + bitmapRegionDecoder);
                    if (bitmapRegionDecoder != null) {
                    }
                    if (wallpaper.wallpaperFile.delete()) {
                    }
                    IoUtils.closeQuietly(bos3);
                    IoUtils.closeQuietly(f);
                    if (success) {
                    }
                    if (wallpaper.cropFile.exists()) {
                    }
                }
            } else {
                success = FileUtils.copyFile(wallpaper.wallpaperFile, wallpaper.cropFile);
                if (!success) {
                    wallpaper.cropFile.delete();
                }
            }
        }
        if (success) {
            Slog.e(TAG, "Unable to apply new wallpaper");
            wallpaper.cropFile.delete();
        }
        if (wallpaper.cropFile.exists()) {
            boolean restorecon = SELinux.restorecon(wallpaper.cropFile.getAbsoluteFile());
        }
    }

    int makeWallpaperIdLocked() {
        do {
            this.mWallpaperId++;
        } while (this.mWallpaperId == 0);
        return this.mWallpaperId;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.Keyguard, 2012.08.27:add for oppo-wallpaper", property = OppoRomType.ROM)
    public WallpaperManagerService(Context context) {
        this.mWallpaperHelper = null;
        this.mLock = new Object();
        this.mWallpaperMap = new SparseArray();
        this.mLockWallpaperMap = new SparseArray();
        this.mUserRestorecon = new SparseArray();
        this.mExpectedLiving = true;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == WallpaperManagerService.MSG_BIND_WP) {
                    int userId = UserHandle.getCallingUserId();
                    Slog.v(WallpaperManagerService.TAG, "Receive message MSG_BIND_WP, bind service: " + WallpaperManagerService.this.mLastIntent.getComponent() + ",connection: " + WallpaperManagerService.this.mLastWallpaper.connection);
                    WallpaperManagerService.this.mContext.bindServiceAsUser(WallpaperManagerService.this.mLastIntent, WallpaperManagerService.this.mLastWallpaper.connection, 536870913, new UserHandle(userId));
                }
            }
        };
        this.mContext = context;
        this.mShuttingDown = false;
        this.mImageWallpaper = ComponentName.unflattenFromString(context.getResources().getString(17039423));
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mIPackageManager = AppGlobals.getPackageManager();
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mMonitor = new MyPackageMonitor();
        this.mMonitor.register(context, null, UserHandle.ALL, true);
        getWallpaperDir(0).mkdirs();
        this.mWallpaperHelper = new WallpaperHelper(this.mContext);
        loadSettingsLocked(0, false);
    }

    private static File getWallpaperDir(int userId) {
        return Environment.getUserSystemDirectory(userId);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        for (int i = 0; i < this.mWallpaperMap.size(); i++) {
            ((WallpaperData) this.mWallpaperMap.valueAt(i)).wallpaperObserver.stopWatching();
        }
    }

    void systemReady() {
        WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(0);
        if (this.mImageWallpaper.equals(wallpaper.nextWallpaperComponent)) {
            if (!wallpaper.cropExists()) {
                generateCrop(wallpaper);
            }
            if (!wallpaper.cropExists()) {
                clearWallpaperLocked(false, 1, 0, null);
            }
        }
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    WallpaperManagerService.this.onRemoveUser(intent.getIntExtra("android.intent.extra.user_handle", -10000));
                }
            }
        }, userFilter);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
                    synchronized (WallpaperManagerService.this.mLock) {
                        WallpaperManagerService.this.mShuttingDown = true;
                    }
                }
            }
        }, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"));
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(new IUserSwitchObserver.Stub() {
                public void onUserSwitching(int newUserId, IRemoteCallback reply) {
                    WallpaperManagerService.this.switchUser(newUserId, reply);
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                }

                public void onForegroundProfileSwitch(int newProfileId) {
                }
            }, TAG);
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
    }

    public String getName() {
        if (Binder.getCallingUid() != 1000) {
            throw new RuntimeException("getName() can only be called from the system process");
        }
        String str;
        synchronized (this.mLock) {
            str = ((WallpaperData) this.mWallpaperMap.get(0)).name;
        }
        return str;
    }

    void stopObserver(WallpaperData wallpaper) {
        if (wallpaper != null && wallpaper.wallpaperObserver != null) {
            wallpaper.wallpaperObserver.stopWatching();
            wallpaper.wallpaperObserver = null;
        }
    }

    void stopObserversLocked(int userId) {
        stopObserver((WallpaperData) this.mWallpaperMap.get(userId));
        stopObserver((WallpaperData) this.mLockWallpaperMap.get(userId));
        this.mWallpaperMap.remove(userId);
        this.mLockWallpaperMap.remove(userId);
    }

    void onUnlockUser(final int userId) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId == userId) {
                if (this.mWaitingForUnlock) {
                    switchUser(userId, null);
                }
                if (this.mUserRestorecon.get(userId) != Boolean.TRUE) {
                    this.mUserRestorecon.put(userId, Boolean.TRUE);
                    BackgroundThread.getHandler().post(new Runnable() {
                        public void run() {
                            File wallpaperDir = WallpaperManagerService.getWallpaperDir(userId);
                            for (String filename : WallpaperManagerService.sPerUserFiles) {
                                File f = new File(wallpaperDir, filename);
                                if (f.exists()) {
                                    SELinux.restorecon(f);
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    void onRemoveUser(int userId) {
        if (userId >= 1) {
            File wallpaperDir = getWallpaperDir(userId);
            synchronized (this.mLock) {
                stopObserversLocked(userId);
                for (String filename : sPerUserFiles) {
                    new File(wallpaperDir, filename).delete();
                }
            }
        }
    }

    void switchUser(int userId, IRemoteCallback reply) {
        synchronized (this.mLock) {
            this.mCurrentUserId = userId;
            WallpaperData wallpaper = getWallpaperSafeLocked(userId, 1);
            if (wallpaper.wallpaperObserver == null) {
                wallpaper.wallpaperObserver = new WallpaperObserver(wallpaper);
                wallpaper.wallpaperObserver.startWatching();
            }
            switchWallpaper(wallpaper, reply);
        }
    }

    void switchWallpaper(WallpaperData wallpaper, IRemoteCallback reply) {
        synchronized (this.mLock) {
            this.mWaitingForUnlock = false;
            ComponentName cname = wallpaper.wallpaperComponent != null ? wallpaper.wallpaperComponent : wallpaper.nextWallpaperComponent;
            if (!bindWallpaperComponentLocked(cname, true, false, wallpaper, reply)) {
                ServiceInfo si = null;
                try {
                    si = this.mIPackageManager.getServiceInfo(cname, DumpState.DUMP_DOMAIN_PREFERRED, wallpaper.userId);
                } catch (RemoteException e) {
                }
                if (si == null) {
                    Slog.w(TAG, "Failure starting previous wallpaper; clearing");
                    clearWallpaperLocked(false, 1, wallpaper.userId, reply);
                } else {
                    Slog.w(TAG, "Wallpaper isn't direct boot aware; using fallback until unlocked");
                    wallpaper.wallpaperComponent = wallpaper.nextWallpaperComponent;
                    WallpaperData fallback = new WallpaperData(wallpaper.userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
                    ensureSaneWallpaperData(fallback);
                    bindWallpaperComponentLocked(this.mImageWallpaper, true, false, fallback, reply);
                    this.mWaitingForUnlock = true;
                }
            }
        }
    }

    public void clearWallpaper(String callingPackage, int which, int userId) {
        checkPermission("android.permission.SET_WALLPAPER");
        if (isWallpaperSupported(callingPackage) && isSetWallpaperAllowed(callingPackage)) {
            userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "clearWallpaper", null);
            synchronized (this.mLock) {
                clearWallpaperLocked(false, which, userId, null);
            }
        }
    }

    void clearWallpaperLocked(boolean defaultFailed, int which, int userId, IRemoteCallback reply) {
        if (which == 1 || which == 2) {
            WallpaperData wallpaper;
            if (which == 2) {
                wallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (wallpaper == null) {
                    return;
                }
            }
            wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
            if (wallpaper == null) {
                loadSettingsLocked(userId, false);
                wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
            }
            if (wallpaper != null) {
                long ident = Binder.clearCallingIdentity();
                try {
                    if (wallpaper.wallpaperFile.exists()) {
                        wallpaper.wallpaperFile.delete();
                        wallpaper.cropFile.delete();
                        if (which == 2) {
                            this.mLockWallpaperMap.remove(userId);
                            IWallpaperManagerCallback cb = this.mKeyguardListener;
                            if (cb != null) {
                                try {
                                    cb.onWallpaperChanged();
                                } catch (RemoteException e) {
                                }
                            }
                            saveSettingsLocked(userId);
                            return;
                        }
                    }
                    Throwable e2 = null;
                    try {
                        wallpaper.imageWallpaperPending = false;
                        if (userId != this.mCurrentUserId) {
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        ComponentName componentName;
                        if (defaultFailed) {
                            componentName = this.mImageWallpaper;
                        } else {
                            componentName = null;
                        }
                        if (bindWallpaperComponentLocked(componentName, true, false, wallpaper, reply)) {
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        Slog.e(TAG, "Default wallpaper component not found!", e2);
                        clearWallpaperComponentLocked(wallpaper);
                        if (reply != null) {
                            try {
                                reply.sendResult(null);
                            } catch (RemoteException e3) {
                            }
                        }
                        Binder.restoreCallingIdentity(ident);
                        return;
                    } catch (Throwable e1) {
                        e2 = e1;
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                return;
            }
        }
        throw new IllegalArgumentException("Must specify exactly one kind of wallpaper to read");
    }

    public boolean hasNamedWallpaper(String name) {
        synchronized (this.mLock) {
            long ident = Binder.clearCallingIdentity();
            try {
                List<UserInfo> users = ((UserManager) this.mContext.getSystemService("user")).getUsers();
                Binder.restoreCallingIdentity(ident);
                for (UserInfo user : users) {
                    if (!user.isManagedProfile()) {
                        WallpaperData wd = (WallpaperData) this.mWallpaperMap.get(user.id);
                        if (wd == null) {
                            loadSettingsLocked(user.id, false);
                            wd = (WallpaperData) this.mWallpaperMap.get(user.id);
                        }
                        if (wd != null && name.equals(wd.name)) {
                            return true;
                        }
                    }
                }
                return false;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private Point getDefaultDisplaySize() {
        Point p = new Point();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealSize(p);
        return p;
    }

    /* JADX WARNING: Missing block: B:33:0x0063, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDimensionHints(int width, int height, String callingPackage) throws RemoteException {
        checkPermission("android.permission.SET_WALLPAPER_HINTS");
        if (isWallpaperSupported(callingPackage)) {
            synchronized (this.mLock) {
                int userId = UserHandle.getCallingUserId();
                WallpaperData wallpaper = getWallpaperSafeLocked(userId, 1);
                if (width <= 0 || height <= 0) {
                    throw new IllegalArgumentException("width and height must be > 0");
                }
                Point displaySize = getDefaultDisplaySize();
                width = Math.max(width, displaySize.x);
                height = Math.max(height, displaySize.y);
                if (!(width == wallpaper.width && height == wallpaper.height)) {
                    wallpaper.width = width;
                    wallpaper.height = height;
                    saveSettingsLocked(userId);
                    if (this.mCurrentUserId != userId) {
                    } else if (wallpaper.connection != null) {
                        if (wallpaper.connection.mEngine != null) {
                            try {
                                wallpaper.connection.mEngine.setDesiredSize(width, height);
                            } catch (RemoteException e) {
                            }
                            notifyCallbacksLocked(wallpaper);
                        } else if (wallpaper.connection.mService != null) {
                            wallpaper.connection.mDimensionsChanged = true;
                        }
                    }
                }
            }
        }
    }

    public int getWidthHint() throws RemoteException {
        synchronized (this.mLock) {
            WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(UserHandle.getCallingUserId());
            if (wallpaper != null) {
                int i = wallpaper.width;
                return i;
            }
            return 0;
        }
    }

    public int getHeightHint() throws RemoteException {
        synchronized (this.mLock) {
            WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(UserHandle.getCallingUserId());
            if (wallpaper != null) {
                int i = wallpaper.height;
                return i;
            }
            return 0;
        }
    }

    /* JADX WARNING: Missing block: B:36:0x0071, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDisplayPadding(Rect padding, String callingPackage) {
        checkPermission("android.permission.SET_WALLPAPER_HINTS");
        if (isWallpaperSupported(callingPackage)) {
            synchronized (this.mLock) {
                int userId = UserHandle.getCallingUserId();
                WallpaperData wallpaper = getWallpaperSafeLocked(userId, 1);
                if (padding.left >= 0 && padding.top >= 0) {
                    if (padding.right >= 0 && padding.bottom >= 0) {
                        if (!padding.equals(wallpaper.padding)) {
                            wallpaper.padding.set(padding);
                            saveSettingsLocked(userId);
                            if (this.mCurrentUserId != userId) {
                                return;
                            } else if (wallpaper.connection != null) {
                                if (wallpaper.connection.mEngine != null) {
                                    try {
                                        wallpaper.connection.mEngine.setDisplayPadding(padding);
                                    } catch (RemoteException e) {
                                    }
                                    notifyCallbacksLocked(wallpaper);
                                } else if (wallpaper.connection.mService != null) {
                                    wallpaper.connection.mPaddingChanged = true;
                                }
                            }
                        }
                    }
                }
                throw new IllegalArgumentException("padding must be positive: " + padding);
            }
        }
    }

    public ParcelFileDescriptor getWallpaper(IWallpaperManagerCallback cb, int which, Bundle outParams, int wallpaperUserId) {
        wallpaperUserId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), wallpaperUserId, false, true, "getWallpaper", null);
        if (which == 1 || which == 2) {
            synchronized (this.mLock) {
                SparseArray<WallpaperData> whichSet = which == 2 ? this.mLockWallpaperMap : this.mWallpaperMap;
                WallpaperData wallpaper = (WallpaperData) whichSet.get(wallpaperUserId);
                if (wallpaper == null) {
                    loadSettingsLocked(wallpaperUserId, false);
                    wallpaper = (WallpaperData) whichSet.get(wallpaperUserId);
                    if (wallpaper == null) {
                        return null;
                    }
                }
                if (outParams != null) {
                    try {
                        outParams.putInt("width", wallpaper.width);
                        outParams.putInt("height", wallpaper.height);
                    } catch (FileNotFoundException e) {
                        Slog.w(TAG, "Error getting wallpaper", e);
                        return null;
                    }
                }
                if (cb != null) {
                    wallpaper.callbacks.register(cb);
                }
                if (wallpaper.cropFile.exists()) {
                    ParcelFileDescriptor open = ParcelFileDescriptor.open(wallpaper.cropFile, 268435456);
                    return open;
                }
                return null;
            }
        }
        throw new IllegalArgumentException("Must specify exactly one kind of wallpaper to read");
    }

    /* JADX WARNING: Missing block: B:11:0x002b, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WallpaperInfo getWallpaperInfo(int userId) {
        userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "getWallpaperIdForUser", null);
        synchronized (this.mLock) {
            WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
            if (wallpaper == null || wallpaper.connection == null) {
            } else {
                WallpaperInfo wallpaperInfo = wallpaper.connection.mInfo;
                return wallpaperInfo;
            }
        }
    }

    public int getWallpaperIdForUser(int which, int userId) {
        userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "getWallpaperIdForUser", null);
        if (which == 1 || which == 2) {
            SparseArray<WallpaperData> map = which == 2 ? this.mLockWallpaperMap : this.mWallpaperMap;
            synchronized (this.mLock) {
                WallpaperData wallpaper = (WallpaperData) map.get(userId);
                if (wallpaper != null) {
                    int i = wallpaper.wallpaperId;
                    return i;
                }
                return -1;
            }
        }
        throw new IllegalArgumentException("Must specify exactly one kind of wallpaper");
    }

    public boolean setLockWallpaperCallback(IWallpaperManagerCallback cb) {
        checkPermission("android.permission.INTERNAL_SYSTEM_WINDOW");
        synchronized (this.mLock) {
            this.mKeyguardListener = cb;
        }
        return true;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.LauncherCenter,2016.09.06:Modify to forbid third part launchers to set wallpaper.", property = OppoRomType.ROM)
    public ParcelFileDescriptor setWallpaper(String name, String callingPackage, Rect cropHint, boolean allowBackup, Bundle extras, int which, IWallpaperManagerCallback completion, int userId) {
        userId = ActivityManager.handleIncomingUser(getCallingPid(), getCallingUid(), userId, false, true, "changing wallpaper", null);
        checkPermission("android.permission.SET_WALLPAPER");
        if ((which & 3) == 0) {
            String msg = "Must specify a valid wallpaper category to set";
            Slog.e(TAG, "Must specify a valid wallpaper category to set");
            throw new IllegalArgumentException("Must specify a valid wallpaper category to set");
        } else if (!isWallpaperSupported(callingPackage) || !isSetWallpaperAllowed(callingPackage)) {
            return null;
        } else {
            if (this.mWallpaperHelper != null && this.mWallpaperHelper.isForbidSettingWallpaperVersion()) {
                Slog.d(TAG, "setWallpaper: forbid setting wallpaper version, just return!!!");
                return null;
            } else if (this.mWallpaperHelper == null || this.mWallpaperHelper.isExpVersion() || !this.mWallpaperHelper.isThirdPartLauncherCall(callingPackage)) {
                ParcelFileDescriptor pfd;
                if (cropHint == null) {
                    Rect rect = new Rect(0, 0, 0, 0);
                } else if (cropHint.isEmpty() || cropHint.left < 0 || cropHint.top < 0) {
                    throw new IllegalArgumentException("Invalid crop rect supplied: " + cropHint);
                }
                synchronized (this.mLock) {
                    if (which == 1) {
                        if (this.mLockWallpaperMap.get(userId) == null) {
                            migrateSystemToLockWallpaperLocked(userId);
                        }
                    }
                    WallpaperData wallpaper = getWallpaperSafeLocked(userId, which);
                    if ((which & 2) != 0 && "com.android.keyguard".equals(callingPackage)) {
                        WallpaperData systemWallpaper = getWallpaperSafeLocked(userId, 1);
                        if (systemWallpaper != null && systemWallpaper.wallpaperObserver == null) {
                            systemWallpaper.wallpaperObserver = new WallpaperObserver(systemWallpaper);
                            systemWallpaper.wallpaperObserver.startWatching();
                            Slog.i(TAG, "setWallpaper: from keyguard, WallpaperObserver startWatching!!!");
                        }
                    }
                    long ident = Binder.clearCallingIdentity();
                    try {
                        pfd = updateWallpaperBitmapLocked(name, wallpaper, extras);
                        if (pfd != null) {
                            wallpaper.imageWallpaperPending = true;
                            wallpaper.whichPending = which;
                            wallpaper.setComplete = completion;
                            wallpaper.cropHint.set(cropHint);
                            wallpaper.allowBackup = allowBackup;
                        }
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
                return pfd;
            } else {
                Slog.d(TAG, "setWallpaper: third part launcher setwallpaper, just return!!!");
                return null;
            }
        }
    }

    private void migrateSystemToLockWallpaperLocked(int userId) {
        WallpaperData sysWP = (WallpaperData) this.mWallpaperMap.get(userId);
        if (sysWP != null) {
            WallpaperData lockWP = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
            lockWP.wallpaperId = sysWP.wallpaperId;
            lockWP.cropHint.set(sysWP.cropHint);
            lockWP.width = sysWP.width;
            lockWP.height = sysWP.height;
            lockWP.allowBackup = sysWP.allowBackup;
            try {
                Os.rename(sysWP.wallpaperFile.getAbsolutePath(), lockWP.wallpaperFile.getAbsolutePath());
                Os.rename(sysWP.cropFile.getAbsolutePath(), lockWP.cropFile.getAbsolutePath());
                this.mLockWallpaperMap.put(userId, lockWP);
            } catch (ErrnoException e) {
                Slog.e(TAG, "Can't migrate system wallpaper: " + e.getMessage());
                lockWP.wallpaperFile.delete();
                lockWP.cropFile.delete();
            }
        }
    }

    ParcelFileDescriptor updateWallpaperBitmapLocked(String name, WallpaperData wallpaper, Bundle extras) {
        if (name == null) {
            name = IElsaManager.EMPTY_PACKAGE;
        }
        try {
            File dir = getWallpaperDir(wallpaper.userId);
            if (!dir.exists()) {
                dir.mkdir();
                FileUtils.setPermissions(dir.getPath(), 505, -1, -1);
            }
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(wallpaper.wallpaperFile, 1006632960);
            if (!SELinux.restorecon(wallpaper.wallpaperFile)) {
                return null;
            }
            wallpaper.name = name;
            wallpaper.wallpaperId = makeWallpaperIdLocked();
            if (extras != null) {
                extras.putInt("android.service.wallpaper.extra.ID", wallpaper.wallpaperId);
            }
            return fd;
        } catch (FileNotFoundException e) {
            Slog.w(TAG, "Error setting wallpaper", e);
            return null;
        }
    }

    public void setWallpaperComponentChecked(ComponentName name, String callingPackage, int userId) {
        if (isWallpaperSupported(callingPackage) && isSetWallpaperAllowed(callingPackage)) {
            setWallpaperComponent(name, userId);
        }
    }

    public void setWallpaperComponent(ComponentName name) {
        setWallpaperComponent(name, UserHandle.getCallingUserId());
    }

    private void setWallpaperComponent(ComponentName name, int userId) {
        userId = ActivityManager.handleIncomingUser(getCallingPid(), getCallingUid(), userId, false, true, "changing live wallpaper", null);
        checkPermission("android.permission.SET_WALLPAPER_COMPONENT");
        if (this.mWallpaperHelper == null || !this.mWallpaperHelper.isForbidSettingWallpaperVersion()) {
            synchronized (this.mLock) {
                WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
                if (wallpaper == null) {
                    throw new IllegalStateException("Wallpaper not yet initialized for user " + userId);
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    wallpaper.imageWallpaperPending = false;
                    if (bindWallpaperComponentLocked(name, false, true, wallpaper, null)) {
                        wallpaper.wallpaperId = makeWallpaperIdLocked();
                        notifyCallbacksLocked(wallpaper);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
            return;
        }
        Slog.d(TAG, "setWallpaperComponent: forbid setting wallpaper version, just return!!!");
    }

    boolean bindWallpaperComponentLocked(ComponentName componentName, boolean force, boolean fromUser, WallpaperData wallpaper, IRemoteCallback reply) {
        String msg;
        if (!(force || wallpaper.connection == null)) {
            if (wallpaper.wallpaperComponent == null) {
                if (componentName == null) {
                    return true;
                }
            } else if (wallpaper.wallpaperComponent.equals(componentName)) {
                return true;
            }
        }
        if (componentName == null) {
            try {
                componentName = WallpaperManager.getDefaultWallpaperComponent(this.mContext);
                if (componentName == null) {
                    componentName = this.mImageWallpaper;
                }
            } catch (XmlPullParserException e) {
                if (fromUser) {
                    throw new IllegalArgumentException(e);
                }
                Slog.w(TAG, e);
                return false;
            } catch (IOException e2) {
                if (fromUser) {
                    throw new IllegalArgumentException(e2);
                }
                Slog.w(TAG, e2);
                return false;
            } catch (RemoteException e3) {
                msg = "Remote exception for " + componentName + "\n" + e3;
                if (fromUser) {
                    throw new IllegalArgumentException(msg);
                }
                Slog.w(TAG, msg);
                return false;
            }
        }
        int serviceUserId = wallpaper.userId;
        ServiceInfo si = this.mIPackageManager.getServiceInfo(componentName, 4224, serviceUserId);
        if (si == null) {
            Slog.w(TAG, "Attempted wallpaper " + componentName + " is unavailable");
            return false;
        } else if ("android.permission.BIND_WALLPAPER".equals(si.permission)) {
            WallpaperInfo wallpaperInfo = null;
            Intent intent = new Intent("android.service.wallpaper.WallpaperService");
            if (componentName != null) {
                if (!componentName.equals(this.mImageWallpaper)) {
                    List<ResolveInfo> ris = this.mIPackageManager.queryIntentServices(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 128, serviceUserId).getList();
                    for (int i = 0; i < ris.size(); i++) {
                        ServiceInfo rsi = ((ResolveInfo) ris.get(i)).serviceInfo;
                        if (rsi.name.equals(si.name) && rsi.packageName.equals(si.packageName)) {
                            WallpaperInfo wallpaperInfo2 = new WallpaperInfo(this.mContext, (ResolveInfo) ris.get(i));
                            break;
                        }
                    }
                    if (wallpaperInfo == null) {
                        msg = "Selected service is not a wallpaper: " + componentName;
                        if (fromUser) {
                            throw new SecurityException(msg);
                        }
                        Slog.w(TAG, msg);
                        return false;
                    }
                }
            }
            WallpaperConnection newConn = new WallpaperConnection(wallpaperInfo, wallpaper);
            intent.setComponent(componentName);
            intent.putExtra("android.intent.extra.client_label", 17040509);
            intent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivityAsUser(this.mContext, 0, Intent.createChooser(new Intent("android.intent.action.SET_WALLPAPER"), this.mContext.getText(17040510)), 0, null, new UserHandle(serviceUserId)));
            if (this.mContext.bindServiceAsUser(intent, newConn, 570425345, new UserHandle(serviceUserId))) {
                if (isGmoRamOptimizeSupport()) {
                    this.mLastIntent = intent;
                    ActivityManagerNative.getDefault().setWallpaperProcess(componentName);
                    Slog.v(TAG, "Tell ActivityManager current wallpaper process is " + componentName);
                }
                if (wallpaper.userId == this.mCurrentUserId && this.mLastWallpaper != null) {
                    detachWallpaperLocked(this.mLastWallpaper);
                }
                wallpaper.wallpaperComponent = componentName;
                wallpaper.connection = newConn;
                newConn.mReply = reply;
                try {
                    if (wallpaper.userId == this.mCurrentUserId) {
                        this.mIWindowManager.addWindowToken(newConn.mToken, 2013);
                        this.mLastWallpaper = wallpaper;
                    }
                } catch (RemoteException e4) {
                }
                return true;
            }
            msg = "Unable to bind service: " + componentName;
            if (fromUser) {
                throw new IllegalArgumentException(msg);
            }
            Slog.w(TAG, msg);
            return false;
        } else {
            msg = "Selected service does not require android.permission.BIND_WALLPAPER: " + componentName;
            if (fromUser) {
                throw new SecurityException(msg);
            }
            Slog.w(TAG, msg);
            return false;
        }
    }

    void detachWallpaperLocked(WallpaperData wallpaper) {
        if (wallpaper.connection != null) {
            if (wallpaper.connection.mReply != null) {
                try {
                    wallpaper.connection.mReply.sendResult(null);
                } catch (RemoteException e) {
                }
                wallpaper.connection.mReply = null;
            }
            if (wallpaper.connection.mEngine != null) {
                try {
                    wallpaper.connection.mEngine.destroy();
                } catch (RemoteException e2) {
                }
            }
            this.mContext.unbindService(wallpaper.connection);
            try {
                this.mIWindowManager.removeWindowToken(wallpaper.connection.mToken);
            } catch (RemoteException e3) {
            }
            wallpaper.connection.mService = null;
            wallpaper.connection.mEngine = null;
            wallpaper.connection = null;
        }
    }

    void clearWallpaperComponentLocked(WallpaperData wallpaper) {
        wallpaper.wallpaperComponent = null;
        detachWallpaperLocked(wallpaper);
    }

    void attachServiceLocked(WallpaperConnection conn, WallpaperData wallpaper) {
        try {
            conn.mService.attach(conn, conn.mToken, 2013, false, wallpaper.width, wallpaper.height, wallpaper.padding);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed attaching wallpaper; clearing", e);
            if (!wallpaper.wallpaperUpdating) {
                bindWallpaperComponentLocked(null, false, false, wallpaper, null);
            }
        }
    }

    private void notifyCallbacksLocked(WallpaperData wallpaper) {
        int n = wallpaper.callbacks.beginBroadcast();
        for (int i = 0; i < n; i++) {
            try {
                ((IWallpaperManagerCallback) wallpaper.callbacks.getBroadcastItem(i)).onWallpaperChanged();
            } catch (RemoteException e) {
            }
        }
        wallpaper.callbacks.finishBroadcast();
        this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.WALLPAPER_CHANGED"), new UserHandle(this.mCurrentUserId));
    }

    private void checkPermission(String permission) {
        if (this.mContext.checkCallingOrSelfPermission(permission) != 0) {
            throw new SecurityException("Access denied to process: " + Binder.getCallingPid() + ", must have permission " + permission);
        }
    }

    public boolean isWallpaperSupported(String callingPackage) {
        return this.mAppOpsManager.checkOpNoThrow(48, Binder.getCallingUid(), callingPackage) == 0;
    }

    public boolean isSetWallpaperAllowed(String callingPackage) {
        boolean z = false;
        if (!Arrays.asList(this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid())).contains(callingPackage)) {
            return false;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService(DevicePolicyManager.class);
        if (dpm.isDeviceOwnerApp(callingPackage) || dpm.isProfileOwnerApp(callingPackage)) {
            return true;
        }
        if (!((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_set_wallpaper")) {
            z = true;
        }
        return z;
    }

    public boolean isWallpaperBackupEligible(int which, int userId) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only the system may call isWallpaperBackupEligible");
        }
        WallpaperData wallpaper;
        if (which == 2) {
            wallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
        } else {
            wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
        }
        return wallpaper != null ? wallpaper.allowBackup : false;
    }

    private static JournaledFile makeJournaledFile(int userId) {
        String base = new File(getWallpaperDir(userId), WALLPAPER_INFO).getAbsolutePath();
        return new JournaledFile(new File(base), new File(base + ".tmp"));
    }

    private void saveSettingsLocked(int userId) {
        JournaledFile journal = makeJournaledFile(userId);
        BufferedOutputStream stream = null;
        try {
            XmlSerializer out = new FastXmlSerializer();
            FileOutputStream fstream = new FileOutputStream(journal.chooseForWrite(), false);
            try {
                BufferedOutputStream stream2 = new BufferedOutputStream(fstream);
                FileOutputStream fileOutputStream;
                try {
                    out.setOutput(stream2, StandardCharsets.UTF_8.name());
                    out.startDocument(null, Boolean.valueOf(true));
                    WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
                    if (wallpaper != null) {
                        writeWallpaperAttributes(out, "wp", wallpaper);
                    }
                    wallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                    if (wallpaper != null) {
                        writeWallpaperAttributes(out, "kwp", wallpaper);
                    }
                    out.endDocument();
                    stream2.flush();
                    FileUtils.sync(fstream);
                    stream2.close();
                    journal.commit();
                    fileOutputStream = fstream;
                } catch (IOException e) {
                    stream = stream2;
                    fileOutputStream = fstream;
                    IoUtils.closeQuietly(stream);
                    journal.rollback();
                }
            } catch (IOException e2) {
                IoUtils.closeQuietly(stream);
                journal.rollback();
            }
        } catch (IOException e3) {
            IoUtils.closeQuietly(stream);
            journal.rollback();
        }
    }

    private void writeWallpaperAttributes(XmlSerializer out, String tag, WallpaperData wallpaper) throws IllegalArgumentException, IllegalStateException, IOException {
        out.startTag(null, tag);
        out.attribute(null, DecryptTool.UNLOCK_TYPE_ID, Integer.toString(wallpaper.wallpaperId));
        out.attribute(null, "width", Integer.toString(wallpaper.width));
        out.attribute(null, "height", Integer.toString(wallpaper.height));
        out.attribute(null, "cropLeft", Integer.toString(wallpaper.cropHint.left));
        out.attribute(null, "cropTop", Integer.toString(wallpaper.cropHint.top));
        out.attribute(null, "cropRight", Integer.toString(wallpaper.cropHint.right));
        out.attribute(null, "cropBottom", Integer.toString(wallpaper.cropHint.bottom));
        if (wallpaper.padding.left != 0) {
            out.attribute(null, "paddingLeft", Integer.toString(wallpaper.padding.left));
        }
        if (wallpaper.padding.top != 0) {
            out.attribute(null, "paddingTop", Integer.toString(wallpaper.padding.top));
        }
        if (wallpaper.padding.right != 0) {
            out.attribute(null, "paddingRight", Integer.toString(wallpaper.padding.right));
        }
        if (wallpaper.padding.bottom != 0) {
            out.attribute(null, "paddingBottom", Integer.toString(wallpaper.padding.bottom));
        }
        out.attribute(null, "name", wallpaper.name);
        if (!(wallpaper.wallpaperComponent == null || wallpaper.wallpaperComponent.equals(this.mImageWallpaper))) {
            out.attribute(null, "component", wallpaper.wallpaperComponent.flattenToShortString());
        }
        if (wallpaper.allowBackup) {
            out.attribute(null, "backup", "true");
        }
        out.endTag(null, tag);
    }

    private void migrateFromOld() {
        File oldWallpaper = new File("/data/data/com.android.settings/files/wallpaper");
        File oldInfo = new File("/data/system/wallpaper_info.xml");
        if (oldWallpaper.exists()) {
            oldWallpaper.renameTo(new File(getWallpaperDir(0), WALLPAPER));
        }
        if (oldInfo.exists()) {
            oldInfo.renameTo(new File(getWallpaperDir(0), WALLPAPER_INFO));
        }
    }

    private int getAttributeInt(XmlPullParser parser, String name, int defValue) {
        String value = parser.getAttributeValue(null, name);
        if (value == null) {
            return defValue;
        }
        return Integer.parseInt(value);
    }

    private WallpaperData getWallpaperSafeLocked(int userId, int which) {
        SparseArray<WallpaperData> whichSet = which == 2 ? this.mLockWallpaperMap : this.mWallpaperMap;
        WallpaperData wallpaper = (WallpaperData) whichSet.get(userId);
        if (wallpaper != null) {
            return wallpaper;
        }
        loadSettingsLocked(userId, false);
        wallpaper = (WallpaperData) whichSet.get(userId);
        if (wallpaper != null) {
            return wallpaper;
        }
        if (which == 2) {
            wallpaper = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
            this.mLockWallpaperMap.put(userId, wallpaper);
            ensureSaneWallpaperData(wallpaper);
            return wallpaper;
        }
        Slog.wtf(TAG, "Didn't find wallpaper in non-lock case!");
        wallpaper = new WallpaperData(userId, WALLPAPER, WALLPAPER_CROP);
        this.mWallpaperMap.put(userId, wallpaper);
        ensureSaneWallpaperData(wallpaper);
        return wallpaper;
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x033e  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x033e  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x033e  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x033e  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x033e  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x033e  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x033e  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x033e  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x033e  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x033e  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x033e  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0269  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x033e  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A:{SYNTHETIC, RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.Keyguard,2012.08.27:add for oppo-wallpaper", property = OppoRomType.ROM)
    private void loadSettingsLocked(int userId, boolean keepDimensionHints) {
        WallpaperData wallpaperData;
        WallpaperData lockWallpaper;
        Display d;
        DisplayMetrics displayMetrics;
        int maxDim;
        int minDim;
        NullPointerException e;
        Object stream;
        NumberFormatException e2;
        XmlPullParserException e3;
        IOException e4;
        IndexOutOfBoundsException e5;
        AutoCloseable stream2 = null;
        File file = makeJournaledFile(userId).chooseForRead();
        if (!file.exists()) {
            migrateFromOld();
        }
        WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
        if (wallpaper == null) {
            wallpaperData = new WallpaperData(userId, WALLPAPER, WALLPAPER_CROP);
            wallpaperData.allowBackup = true;
            this.mWallpaperMap.put(userId, wallpaperData);
            if (!wallpaperData.cropExists()) {
                generateCrop(wallpaperData);
            }
        }
        boolean success = false;
        try {
            InputStream fileInputStream = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileInputStream, StandardCharsets.UTF_8.name());
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tag = parser.getName();
                        if ("wp".equals(tag)) {
                            ComponentName unflattenFromString;
                            parseWallpaperAttributes(parser, wallpaper, keepDimensionHints);
                            String comp = parser.getAttributeValue(null, "component");
                            if (comp != null) {
                                unflattenFromString = ComponentName.unflattenFromString(comp);
                            } else {
                                unflattenFromString = null;
                            }
                            wallpaper.nextWallpaperComponent = unflattenFromString;
                            if (wallpaper.nextWallpaperComponent == null || "android".equals(wallpaper.nextWallpaperComponent.getPackageName())) {
                                wallpaper.nextWallpaperComponent = this.mImageWallpaper;
                            }
                        } else if ("kwp".equals(tag)) {
                            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                            if (lockWallpaper == null) {
                                wallpaperData = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
                                this.mLockWallpaperMap.put(userId, wallpaperData);
                            }
                            parseWallpaperAttributes(parser, lockWallpaper, false);
                        }
                    }
                } while (type != 1);
                success = true;
                stream2 = fileInputStream;
            } catch (FileNotFoundException e6) {
                stream2 = fileInputStream;
                Slog.w(TAG, "no current wallpaper -- first boot?");
                IoUtils.closeQuietly(stream2);
                if (success) {
                }
                d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                displayMetrics = new DisplayMetrics();
                d.getRealMetrics(displayMetrics);
                maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
                minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
                wallpaper.padding.set(0, 0, 0, 0);
                wallpaper.name = IElsaManager.EMPTY_PACKAGE;
                if (-1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper) {
                }
                wallpaper.width = minDim;
                wallpaper.height = maxDim;
                Slog.d(TAG, "loadSettingsLocked wallpaper.width = " + wallpaper.width + " wallpaper.height=" + wallpaper.height);
                wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
                lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (lockWallpaper != null) {
                }
            } catch (NullPointerException e7) {
                e = e7;
                stream2 = fileInputStream;
                Slog.w(TAG, "failed parsing " + file + " " + e);
                IoUtils.closeQuietly(stream2);
                if (success) {
                }
                d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                displayMetrics = new DisplayMetrics();
                d.getRealMetrics(displayMetrics);
                maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
                minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
                wallpaper.padding.set(0, 0, 0, 0);
                wallpaper.name = IElsaManager.EMPTY_PACKAGE;
                if (-1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper) {
                }
                wallpaper.width = minDim;
                wallpaper.height = maxDim;
                Slog.d(TAG, "loadSettingsLocked wallpaper.width = " + wallpaper.width + " wallpaper.height=" + wallpaper.height);
                wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
                lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (lockWallpaper != null) {
                }
            } catch (NumberFormatException e8) {
                e2 = e8;
                stream2 = fileInputStream;
                Slog.w(TAG, "failed parsing " + file + " " + e2);
                IoUtils.closeQuietly(stream2);
                if (success) {
                }
                d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                displayMetrics = new DisplayMetrics();
                d.getRealMetrics(displayMetrics);
                maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
                minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
                wallpaper.padding.set(0, 0, 0, 0);
                wallpaper.name = IElsaManager.EMPTY_PACKAGE;
                if (-1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper) {
                }
                wallpaper.width = minDim;
                wallpaper.height = maxDim;
                Slog.d(TAG, "loadSettingsLocked wallpaper.width = " + wallpaper.width + " wallpaper.height=" + wallpaper.height);
                wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
                lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (lockWallpaper != null) {
                }
            } catch (XmlPullParserException e9) {
                e3 = e9;
                stream2 = fileInputStream;
                Slog.w(TAG, "failed parsing " + file + " " + e3);
                IoUtils.closeQuietly(stream2);
                if (success) {
                }
                d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                displayMetrics = new DisplayMetrics();
                d.getRealMetrics(displayMetrics);
                maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
                minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
                wallpaper.padding.set(0, 0, 0, 0);
                wallpaper.name = IElsaManager.EMPTY_PACKAGE;
                if (-1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper) {
                }
                wallpaper.width = minDim;
                wallpaper.height = maxDim;
                Slog.d(TAG, "loadSettingsLocked wallpaper.width = " + wallpaper.width + " wallpaper.height=" + wallpaper.height);
                wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
                lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (lockWallpaper != null) {
                }
            } catch (IOException e10) {
                e4 = e10;
                stream2 = fileInputStream;
                Slog.w(TAG, "failed parsing " + file + " " + e4);
                IoUtils.closeQuietly(stream2);
                if (success) {
                }
                d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                displayMetrics = new DisplayMetrics();
                d.getRealMetrics(displayMetrics);
                maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
                minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
                wallpaper.padding.set(0, 0, 0, 0);
                wallpaper.name = IElsaManager.EMPTY_PACKAGE;
                if (-1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper) {
                }
                wallpaper.width = minDim;
                wallpaper.height = maxDim;
                Slog.d(TAG, "loadSettingsLocked wallpaper.width = " + wallpaper.width + " wallpaper.height=" + wallpaper.height);
                wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
                lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (lockWallpaper != null) {
                }
            } catch (IndexOutOfBoundsException e11) {
                e5 = e11;
                stream2 = fileInputStream;
                Slog.w(TAG, "failed parsing " + file + " " + e5);
                IoUtils.closeQuietly(stream2);
                if (success) {
                }
                d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                displayMetrics = new DisplayMetrics();
                d.getRealMetrics(displayMetrics);
                maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
                minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
                wallpaper.padding.set(0, 0, 0, 0);
                wallpaper.name = IElsaManager.EMPTY_PACKAGE;
                if (-1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper) {
                }
                wallpaper.width = minDim;
                wallpaper.height = maxDim;
                Slog.d(TAG, "loadSettingsLocked wallpaper.width = " + wallpaper.width + " wallpaper.height=" + wallpaper.height);
                wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
                lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (lockWallpaper != null) {
                }
            }
        } catch (FileNotFoundException e12) {
            Slog.w(TAG, "no current wallpaper -- first boot?");
            IoUtils.closeQuietly(stream2);
            if (success) {
            }
            d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            displayMetrics = new DisplayMetrics();
            d.getRealMetrics(displayMetrics);
            maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
            minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
            wallpaper.padding.set(0, 0, 0, 0);
            wallpaper.name = IElsaManager.EMPTY_PACKAGE;
            if (-1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper) {
            }
            wallpaper.width = minDim;
            wallpaper.height = maxDim;
            Slog.d(TAG, "loadSettingsLocked wallpaper.width = " + wallpaper.width + " wallpaper.height=" + wallpaper.height);
            wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
            if (lockWallpaper != null) {
            }
        } catch (NullPointerException e13) {
            e = e13;
            Slog.w(TAG, "failed parsing " + file + " " + e);
            IoUtils.closeQuietly(stream2);
            if (success) {
            }
            d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            displayMetrics = new DisplayMetrics();
            d.getRealMetrics(displayMetrics);
            maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
            minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
            wallpaper.padding.set(0, 0, 0, 0);
            wallpaper.name = IElsaManager.EMPTY_PACKAGE;
            if (-1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper) {
            }
            wallpaper.width = minDim;
            wallpaper.height = maxDim;
            Slog.d(TAG, "loadSettingsLocked wallpaper.width = " + wallpaper.width + " wallpaper.height=" + wallpaper.height);
            wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
            if (lockWallpaper != null) {
            }
        } catch (NumberFormatException e14) {
            e2 = e14;
            Slog.w(TAG, "failed parsing " + file + " " + e2);
            IoUtils.closeQuietly(stream2);
            if (success) {
            }
            d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            displayMetrics = new DisplayMetrics();
            d.getRealMetrics(displayMetrics);
            maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
            minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
            wallpaper.padding.set(0, 0, 0, 0);
            wallpaper.name = IElsaManager.EMPTY_PACKAGE;
            if (-1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper) {
            }
            wallpaper.width = minDim;
            wallpaper.height = maxDim;
            Slog.d(TAG, "loadSettingsLocked wallpaper.width = " + wallpaper.width + " wallpaper.height=" + wallpaper.height);
            wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
            if (lockWallpaper != null) {
            }
        } catch (XmlPullParserException e15) {
            e3 = e15;
            Slog.w(TAG, "failed parsing " + file + " " + e3);
            IoUtils.closeQuietly(stream2);
            if (success) {
            }
            d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            displayMetrics = new DisplayMetrics();
            d.getRealMetrics(displayMetrics);
            maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
            minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
            wallpaper.padding.set(0, 0, 0, 0);
            wallpaper.name = IElsaManager.EMPTY_PACKAGE;
            if (-1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper) {
            }
            wallpaper.width = minDim;
            wallpaper.height = maxDim;
            Slog.d(TAG, "loadSettingsLocked wallpaper.width = " + wallpaper.width + " wallpaper.height=" + wallpaper.height);
            wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
            if (lockWallpaper != null) {
            }
        } catch (IOException e16) {
            e4 = e16;
            Slog.w(TAG, "failed parsing " + file + " " + e4);
            IoUtils.closeQuietly(stream2);
            if (success) {
            }
            d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            displayMetrics = new DisplayMetrics();
            d.getRealMetrics(displayMetrics);
            maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
            minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
            wallpaper.padding.set(0, 0, 0, 0);
            wallpaper.name = IElsaManager.EMPTY_PACKAGE;
            if (-1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper) {
            }
            wallpaper.width = minDim;
            wallpaper.height = maxDim;
            Slog.d(TAG, "loadSettingsLocked wallpaper.width = " + wallpaper.width + " wallpaper.height=" + wallpaper.height);
            wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
            if (lockWallpaper != null) {
            }
        } catch (IndexOutOfBoundsException e17) {
            e5 = e17;
            Slog.w(TAG, "failed parsing " + file + " " + e5);
            IoUtils.closeQuietly(stream2);
            if (success) {
            }
            d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            displayMetrics = new DisplayMetrics();
            d.getRealMetrics(displayMetrics);
            maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
            minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
            wallpaper.padding.set(0, 0, 0, 0);
            wallpaper.name = IElsaManager.EMPTY_PACKAGE;
            if (-1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper) {
            }
            wallpaper.width = minDim;
            wallpaper.height = maxDim;
            Slog.d(TAG, "loadSettingsLocked wallpaper.width = " + wallpaper.width + " wallpaper.height=" + wallpaper.height);
            wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
            if (lockWallpaper != null) {
            }
        }
        IoUtils.closeQuietly(stream2);
        if (success) {
            this.mLockWallpaperMap.remove(userId);
        } else if (wallpaper.wallpaperId <= 0) {
            wallpaper.wallpaperId = makeWallpaperIdLocked();
        }
        if (!(success && this.mWallpaperHelper.isWallpaperExist(userId))) {
            d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            displayMetrics = new DisplayMetrics();
            d.getRealMetrics(displayMetrics);
            maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
            minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
            wallpaper.padding.set(0, 0, 0, 0);
            wallpaper.name = IElsaManager.EMPTY_PACKAGE;
            if (-1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper) {
                minDim = this.mWallpaperHelper.mWidthOfDefaultWallpaper;
            }
            wallpaper.width = minDim;
            wallpaper.height = maxDim;
            Slog.d(TAG, "loadSettingsLocked wallpaper.width = " + wallpaper.width + " wallpaper.height=" + wallpaper.height);
            wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
        }
        lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
        if (lockWallpaper != null) {
            ensureSaneWallpaperData(lockWallpaper);
        }
    }

    private void ensureSaneWallpaperData(WallpaperData wallpaper) {
        int baseSize = getMaximumSizeDimension();
        if (wallpaper.width < baseSize) {
            wallpaper.width = baseSize;
        }
        if (wallpaper.height < baseSize) {
            wallpaper.height = baseSize;
        }
        if (wallpaper.cropHint.width() <= 0 || wallpaper.cropHint.height() <= 0) {
            wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
        }
    }

    private void parseWallpaperAttributes(XmlPullParser parser, WallpaperData wallpaper, boolean keepDimensionHints) {
        String idString = parser.getAttributeValue(null, DecryptTool.UNLOCK_TYPE_ID);
        if (idString != null) {
            int id = Integer.parseInt(idString);
            wallpaper.wallpaperId = id;
            if (id > this.mWallpaperId) {
                this.mWallpaperId = id;
            }
        } else {
            wallpaper.wallpaperId = makeWallpaperIdLocked();
        }
        if (!keepDimensionHints) {
            wallpaper.width = Integer.parseInt(parser.getAttributeValue(null, "width"));
            wallpaper.height = Integer.parseInt(parser.getAttributeValue(null, "height"));
        }
        wallpaper.cropHint.left = getAttributeInt(parser, "cropLeft", 0);
        wallpaper.cropHint.top = getAttributeInt(parser, "cropTop", 0);
        wallpaper.cropHint.right = getAttributeInt(parser, "cropRight", 0);
        wallpaper.cropHint.bottom = getAttributeInt(parser, "cropBottom", 0);
        wallpaper.padding.left = getAttributeInt(parser, "paddingLeft", 0);
        wallpaper.padding.top = getAttributeInt(parser, "paddingTop", 0);
        wallpaper.padding.right = getAttributeInt(parser, "paddingRight", 0);
        wallpaper.padding.bottom = getAttributeInt(parser, "paddingBottom", 0);
        wallpaper.name = parser.getAttributeValue(null, "name");
        wallpaper.allowBackup = "true".equals(parser.getAttributeValue(null, "backup"));
    }

    private int getMaximumSizeDimension() {
        return ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getMaximumSizeDimension();
    }

    public void settingsRestored() {
        if (Binder.getCallingUid() != 1000) {
            throw new RuntimeException("settingsRestored() can only be called from the system process");
        }
        WallpaperData wallpaper;
        boolean success;
        synchronized (this.mLock) {
            loadSettingsLocked(0, false);
            wallpaper = (WallpaperData) this.mWallpaperMap.get(0);
            wallpaper.wallpaperId = makeWallpaperIdLocked();
            wallpaper.allowBackup = true;
            if (wallpaper.nextWallpaperComponent == null || wallpaper.nextWallpaperComponent.equals(this.mImageWallpaper)) {
                if (IElsaManager.EMPTY_PACKAGE.equals(wallpaper.name)) {
                    success = true;
                } else {
                    success = restoreNamedResourceLocked(wallpaper);
                }
                if (success) {
                    generateCrop(wallpaper);
                    bindWallpaperComponentLocked(wallpaper.nextWallpaperComponent, true, false, wallpaper, null);
                }
            } else {
                if (!bindWallpaperComponentLocked(wallpaper.nextWallpaperComponent, false, false, wallpaper, null)) {
                    bindWallpaperComponentLocked(null, false, false, wallpaper, null);
                }
                success = true;
            }
        }
        if (!success) {
            Slog.e(TAG, "Failed to restore wallpaper: '" + wallpaper.name + "'");
            wallpaper.name = IElsaManager.EMPTY_PACKAGE;
            getWallpaperDir(0).delete();
        }
        synchronized (this.mLock) {
            saveSettingsLocked(0);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x0185  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x018a  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0210  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0215  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01dd  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x01e2  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0185  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x018a  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0210  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0215  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01dd  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x01e2  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0226  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x022b  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0226  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x022b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean restoreNamedResourceLocked(WallpaperData wallpaper) {
        Object fos;
        Throwable th;
        IOException e;
        Object cos;
        if (wallpaper.name.length() > 4 && "res:".equals(wallpaper.name.substring(0, 4))) {
            String resName = wallpaper.name.substring(4);
            String pkg = null;
            int colon = resName.indexOf(58);
            if (colon > 0) {
                pkg = resName.substring(0, colon);
            }
            String ident = null;
            int slash = resName.lastIndexOf(47);
            if (slash > 0) {
                ident = resName.substring(slash + 1);
            }
            String type = null;
            if (colon > 0 && slash > 0 && slash - colon > 1) {
                type = resName.substring(colon + 1, slash);
            }
            if (!(pkg == null || ident == null || type == null)) {
                int resId = -1;
                AutoCloseable autoCloseable = null;
                AutoCloseable fos2 = null;
                AutoCloseable cos2 = null;
                try {
                    Resources r = this.mContext.createPackageContext(pkg, 4).getResources();
                    resId = r.getIdentifier(resName, null, null);
                    if (resId == 0) {
                        Slog.e(TAG, "couldn't resolve identifier pkg=" + pkg + " type=" + type + " ident=" + ident);
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(null);
                        return false;
                    }
                    FileOutputStream cos3;
                    autoCloseable = r.openRawResource(resId);
                    if (wallpaper.wallpaperFile.exists()) {
                        wallpaper.wallpaperFile.delete();
                        wallpaper.cropFile.delete();
                    }
                    FileOutputStream fos3 = new FileOutputStream(wallpaper.wallpaperFile);
                    try {
                        cos3 = new FileOutputStream(wallpaper.cropFile);
                    } catch (NameNotFoundException e2) {
                        fos = fos3;
                        try {
                            Slog.e(TAG, "Package name " + pkg + " not found");
                            IoUtils.closeQuietly(autoCloseable);
                            if (fos2 != null) {
                            }
                            if (cos2 != null) {
                            }
                            IoUtils.closeQuietly(fos2);
                            IoUtils.closeQuietly(cos2);
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(autoCloseable);
                            if (fos2 != null) {
                            }
                            if (cos2 != null) {
                            }
                            IoUtils.closeQuietly(fos2);
                            IoUtils.closeQuietly(cos2);
                            throw th;
                        }
                    } catch (NotFoundException e3) {
                        fos = fos3;
                        Slog.e(TAG, "Resource not found: " + resId);
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                        }
                        if (cos2 != null) {
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        return false;
                    } catch (IOException e4) {
                        e = e4;
                        fos = fos3;
                        Slog.e(TAG, "IOException while restoring wallpaper ", e);
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                        }
                        if (cos2 != null) {
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        return false;
                    } catch (Throwable th3) {
                        th = th3;
                        fos = fos3;
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                        }
                        if (cos2 != null) {
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        throw th;
                    }
                    try {
                        byte[] buffer = new byte[32768];
                        while (true) {
                            int amt = autoCloseable.read(buffer);
                            if (amt <= 0) {
                                break;
                            }
                            fos3.write(buffer, 0, amt);
                            cos3.write(buffer, 0, amt);
                        }
                        Slog.v(TAG, "Restored wallpaper: " + resName);
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos3 != null) {
                            FileUtils.sync(fos3);
                        }
                        if (cos3 != null) {
                            FileUtils.sync(cos3);
                        }
                        IoUtils.closeQuietly(fos3);
                        IoUtils.closeQuietly(cos3);
                        return true;
                    } catch (NameNotFoundException e5) {
                        cos2 = cos3;
                        fos2 = fos3;
                        Slog.e(TAG, "Package name " + pkg + " not found");
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                            FileUtils.sync(fos2);
                        }
                        if (cos2 != null) {
                            FileUtils.sync(cos2);
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        return false;
                    } catch (NotFoundException e6) {
                        cos = cos3;
                        fos = fos3;
                        Slog.e(TAG, "Resource not found: " + resId);
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                            FileUtils.sync(fos2);
                        }
                        if (cos2 != null) {
                            FileUtils.sync(cos2);
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        return false;
                    } catch (IOException e7) {
                        e = e7;
                        cos = cos3;
                        fos = fos3;
                        Slog.e(TAG, "IOException while restoring wallpaper ", e);
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                            FileUtils.sync(fos2);
                        }
                        if (cos2 != null) {
                            FileUtils.sync(cos2);
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        return false;
                    } catch (Throwable th4) {
                        th = th4;
                        cos = cos3;
                        fos = fos3;
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                            FileUtils.sync(fos2);
                        }
                        if (cos2 != null) {
                            FileUtils.sync(cos2);
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        throw th;
                    }
                } catch (NameNotFoundException e8) {
                    Slog.e(TAG, "Package name " + pkg + " not found");
                    IoUtils.closeQuietly(autoCloseable);
                    if (fos2 != null) {
                    }
                    if (cos2 != null) {
                    }
                    IoUtils.closeQuietly(fos2);
                    IoUtils.closeQuietly(cos2);
                    return false;
                } catch (NotFoundException e9) {
                    Slog.e(TAG, "Resource not found: " + resId);
                    IoUtils.closeQuietly(autoCloseable);
                    if (fos2 != null) {
                    }
                    if (cos2 != null) {
                    }
                    IoUtils.closeQuietly(fos2);
                    IoUtils.closeQuietly(cos2);
                    return false;
                } catch (IOException e10) {
                    e = e10;
                    Slog.e(TAG, "IOException while restoring wallpaper ", e);
                    IoUtils.closeQuietly(autoCloseable);
                    if (fos2 != null) {
                    }
                    if (cos2 != null) {
                    }
                    IoUtils.closeQuietly(fos2);
                    IoUtils.closeQuietly(cos2);
                    return false;
                }
            }
        }
        return false;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump wallpaper service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (this.mLock) {
            int i;
            WallpaperData wallpaper;
            pw.println("System wallpaper state:");
            for (i = 0; i < this.mWallpaperMap.size(); i++) {
                wallpaper = (WallpaperData) this.mWallpaperMap.valueAt(i);
                pw.print(" User ");
                pw.print(wallpaper.userId);
                pw.print(": id=");
                pw.println(wallpaper.wallpaperId);
                pw.print("  mWidth=");
                pw.print(wallpaper.width);
                pw.print(" mHeight=");
                pw.println(wallpaper.height);
                pw.print("  mCropHint=");
                pw.println(wallpaper.cropHint);
                pw.print("  mPadding=");
                pw.println(wallpaper.padding);
                pw.print("  mName=");
                pw.println(wallpaper.name);
                pw.print("  mWallpaperComponent=");
                pw.println(wallpaper.wallpaperComponent);
                if (wallpaper.connection != null) {
                    WallpaperConnection conn = wallpaper.connection;
                    pw.print("  Wallpaper connection ");
                    pw.print(conn);
                    pw.println(":");
                    if (conn.mInfo != null) {
                        pw.print("    mInfo.component=");
                        pw.println(conn.mInfo.getComponent());
                    }
                    pw.print("    mToken=");
                    pw.println(conn.mToken);
                    pw.print("    mService=");
                    pw.println(conn.mService);
                    pw.print("    mEngine=");
                    pw.println(conn.mEngine);
                    pw.print("    mLastDiedTime=");
                    pw.println(wallpaper.lastDiedTime - SystemClock.uptimeMillis());
                }
            }
            pw.println("Lock wallpaper state:");
            for (i = 0; i < this.mLockWallpaperMap.size(); i++) {
                wallpaper = (WallpaperData) this.mLockWallpaperMap.valueAt(i);
                pw.print(" User ");
                pw.print(wallpaper.userId);
                pw.print(": id=");
                pw.println(wallpaper.wallpaperId);
                pw.print("  mWidth=");
                pw.print(wallpaper.width);
                pw.print(" mHeight=");
                pw.println(wallpaper.height);
                pw.print("  mCropHint=");
                pw.println(wallpaper.cropHint);
                pw.print("  mPadding=");
                pw.println(wallpaper.padding);
                pw.print("  mName=");
                pw.println(wallpaper.name);
            }
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework, add for BPM", property = OppoRomType.ROM)
    public ComponentName getLiveComponent() {
        WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(this.mCurrentUserId);
        if (wallpaper.wallpaperComponent == null || wallpaper.wallpaperComponent.getClassName().equals("com.android.systemui.ImageWallpaper")) {
            return null;
        }
        return wallpaper.wallpaperComponent;
    }

    public void onVisibilityChanged(boolean isVisible) throws RemoteException {
        if (isGmoRamOptimizeSupport() && this.mVisible != isVisible) {
            this.mVisible = isVisible;
            modifyWallpaperAdj(this.mVisible);
            doVisibilityChanged(this.mVisible);
        }
    }

    private void doVisibilityChanged(boolean isVisible) {
        if (isVisible && !this.mExpectedLiving && !this.mLastWallpaper.wallpaperComponent.toString().equals(this.mImageWallpaper.toString())) {
            this.mHandler.removeMessages(MSG_BIND_WP);
            this.mHandler.sendEmptyMessage(MSG_BIND_WP);
        }
    }

    private void modifyWallpaperAdj(boolean isVisible) {
        try {
            ActivityManagerNative.getDefault().updateWallpaperState(isVisible);
        } catch (RemoteException e) {
            Slog.w(TAG, "Modify wallpaper's ADJ, catch RemoteException!!!!!");
        }
    }

    private boolean isGmoRamOptimizeSupport() {
        return SystemProperties.get("ro.mtk_gmo_ram_optimize").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
    }
}
