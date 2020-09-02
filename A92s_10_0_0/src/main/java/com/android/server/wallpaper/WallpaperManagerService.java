package com.android.server.wallpaper;

import android.annotation.OppoHook;
import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IWallpaperManager;
import android.app.IWallpaperManagerCallback;
import android.app.PendingIntent;
import android.app.UserSwitchObserver;
import android.app.WallpaperColors;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IRemoteCallback;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.service.wallpaper.IWallpaperConnection;
import android.service.wallpaper.IWallpaperEngine;
import android.service.wallpaper.IWallpaperService;
import android.system.ErrnoException;
import android.system.Os;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.Xml;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.WindowManager;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.JournaledFile;
import com.android.server.BatteryService;
import com.android.server.EventLogTags;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.Settings;
import com.android.server.wallpaper.WallpaperManagerService;
import com.android.server.wm.WindowManagerInternal;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

@OppoHook(level = OppoHook.OppoHookType.CHANGE_ACCESS, note = "gaoliang@Plf.Keyguard, 2012.08.27:[- +public @hide]modify for oppo wallpaper.", property = OppoHook.OppoRomType.ROM)
public class WallpaperManagerService extends IWallpaperManager.Stub implements IWallpaperManagerService {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final boolean DEBUG_LIVE = true;
    static final int EXTRACT_WALLPAPER_COLOR_DELAY_TIME = 150;
    private static final int MAX_BITMAP_SIZE = 104857600;
    private static final int MAX_WALLPAPER_COMPONENT_LOG_LENGTH = 128;
    private static final long MIN_WALLPAPER_CRASH_TIME = 10000;
    private static final String TAG = "WallpaperManagerService";
    static final String WALLPAPER = "wallpaper_orig";
    static final String WALLPAPER_CROP = "wallpaper";
    static final String WALLPAPER_INFO = "wallpaper_info.xml";
    static final String WALLPAPER_LOCK_CROP = "wallpaper_lock";
    static final String WALLPAPER_LOCK_ORIG = "wallpaper_lock_orig";
    /* access modifiers changed from: private */
    public static final String[] sPerUserFiles = {WALLPAPER, WALLPAPER_CROP, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP, WALLPAPER_INFO};
    private final AppOpsManager mAppOpsManager;
    private WallpaperColors mCacheDefaultImageWallpaperColors;
    /* access modifiers changed from: private */
    public final SparseArray<SparseArray<RemoteCallbackList<IWallpaperManagerCallback>>> mColorsChangedListeners;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentUserId = -10000;
    /* access modifiers changed from: private */
    public final ComponentName mDefaultWallpaperComponent;
    private SparseArray<DisplayData> mDisplayDatas = new SparseArray<>();
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.android.server.wallpaper.WallpaperManagerService.AnonymousClass1 */

        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mLastWallpaper != null) {
                    WallpaperData targetWallpaper = null;
                    if (WallpaperManagerService.this.mLastWallpaper.connection.containsDisplay(displayId)) {
                        targetWallpaper = WallpaperManagerService.this.mLastWallpaper;
                    } else if (WallpaperManagerService.this.mFallbackWallpaper.connection.containsDisplay(displayId)) {
                        targetWallpaper = WallpaperManagerService.this.mFallbackWallpaper;
                    }
                    if (targetWallpaper != null) {
                        WallpaperConnection.DisplayConnector connector = targetWallpaper.connection.getDisplayConnectorOrCreate(displayId);
                        if (connector != null) {
                            connector.disconnectLocked();
                            targetWallpaper.connection.removeDisplayConnector(displayId);
                            WallpaperManagerService.this.removeDisplayData(displayId);
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                for (int i = WallpaperManagerService.this.mColorsChangedListeners.size() - 1; i >= 0; i--) {
                    ((SparseArray) WallpaperManagerService.this.mColorsChangedListeners.valueAt(i)).delete(displayId);
                }
            }
        }

        public void onDisplayChanged(int displayId) {
        }
    };
    /* access modifiers changed from: private */
    public final DisplayManager mDisplayManager;
    /* access modifiers changed from: private */
    public WallpaperData mFallbackWallpaper;
    private final IPackageManager mIPackageManager;
    /* access modifiers changed from: private */
    public final IWindowManager mIWindowManager;
    /* access modifiers changed from: private */
    public final ComponentName mImageWallpaper;
    /* access modifiers changed from: private */
    public boolean mInAmbientMode;
    private IWallpaperManagerCallback mKeyguardListener;
    /* access modifiers changed from: private */
    public WallpaperData mLastWallpaper;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public final SparseArray<WallpaperData> mLockWallpaperMap = new SparseArray<>();
    private final MyPackageMonitor mMonitor;
    private Runnable mNotifyWallpaperColorsChangeRunnable = null;
    /* access modifiers changed from: private */
    public boolean mShuttingDown;
    private final SparseBooleanArray mUserRestorecon = new SparseBooleanArray();
    private boolean mWaitingForUnlock;
    /* access modifiers changed from: private */
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "gaoliang@Plf.Keyguard, 2012.08.27:add for oppo-wallpaper", property = OppoHook.OppoRomType.ROM)
    public WallpaperHelper mWallpaperHelper = null;
    private int mWallpaperId;
    /* access modifiers changed from: private */
    public final SparseArray<WallpaperData> mWallpaperMap = new SparseArray<>();
    /* access modifiers changed from: private */
    public final WindowManagerInternal mWindowManagerInternal;

    @OppoHook(level = OppoHook.OppoHookType.NEW_CLASS, note = "gaoliang@Plf.Keyguard, 2012.08.27:add for oppo-wallpaper", property = OppoHook.OppoRomType.ROM)
    public class WallpaperHelper {
        private static final String FORBID_SETTING_WALLPAPER_STRING = "oppo.forbid.setting.wallpaper";
        public final float WALLPAPER_SCREENS_SPAN = 2.0f;
        private boolean mIsExpVersion = false;
        private boolean mIsForbidSettingWallpaper = false;
        /* access modifiers changed from: private */
        public int mWidthOfDefaultWallpaper = -1;

        public WallpaperHelper(Context context) {
            this.mWidthOfDefaultWallpaper = OppoWallpaperManagerServiceHelper.getDefaultWallpaperWidth(context);
            this.mIsExpVersion = context.getPackageManager().hasSystemFeature("oppo.version.exp");
            this.mIsForbidSettingWallpaper = context.getPackageManager().hasSystemFeature(FORBID_SETTING_WALLPAPER_STRING);
        }

        private void setDimensionHints_extra(int width, int height) throws RemoteException {
            WallpaperManagerService.this.checkPermission("android.permission.SET_WALLPAPER_HINTS");
            synchronized (WallpaperManagerService.this.mLock) {
                int userId = UserHandle.getCallingUserId();
                WallpaperData unused = WallpaperManagerService.this.getWallpaperSafeLocked(userId, 1);
                if (width <= 0 || height <= 0) {
                    throw new IllegalArgumentException("width and height must be > 0");
                }
                DisplayData wpdData = WallpaperManagerService.this.getDisplayDataOrCreate(0);
                if (!(width == wpdData.mWidth && height == wpdData.mHeight)) {
                    wpdData.mWidth = width;
                    wpdData.mHeight = height;
                    WallpaperManagerService.this.saveSettingsLocked(userId);
                }
            }
        }

        private void getCurrentImageWallpaperInfo(Point bmpInfo, int userId) {
            WallpaperManagerService wallpaperManagerService = WallpaperManagerService.this;
            ParcelFileDescriptor fd = wallpaperManagerService.getWallpaper(wallpaperManagerService.mContext.getOpPackageName(), null, 1, null, userId);
            if (WallpaperManagerService.DEBUG) {
                Slog.v(WallpaperManagerService.TAG, "getCurrentImageWallpaperInfo(): fd = " + fd);
            }
            if (fd != null) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);
                    bmpInfo.x = options.outWidth;
                    bmpInfo.y = options.outHeight;
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                } catch (OutOfMemoryError e2) {
                    Slog.w(WallpaperManagerService.TAG, "getCurrentImageWallpaperInfo(): Can't decode file", e2);
                    fd.close();
                } catch (Throwable th) {
                    try {
                        fd.close();
                    } catch (IOException e3) {
                    }
                    throw th;
                }
            }
        }

        /* access modifiers changed from: private */
        public void adjustWallpaperWidth(int userId) {
            try {
                int desiredMinimumWidth = WallpaperManagerService.this.getWidthHint(0);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((WindowManager) WallpaperManagerService.this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
                int maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
                int minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
                if (WallpaperManagerService.DEBUG) {
                    Slog.d(WallpaperManagerService.TAG, "adjustWallpaperWidth desiredMinimumWidth = " + desiredMinimumWidth + ", displayMetrics.widthPixels = " + displayMetrics.widthPixels + ", displayMetrics.heightPixels = " + displayMetrics.heightPixels);
                }
                Point bitmapSize = new Point(-1, -1);
                getCurrentImageWallpaperInfo(bitmapSize, userId);
                Slog.d(WallpaperManagerService.TAG, "adjustWallpaperWidth new bitmap width = " + bitmapSize.x);
                Slog.d(WallpaperManagerService.TAG, "adjustWallpaperWidth new bitmap height = " + bitmapSize.y);
                if (bitmapSize.x > 0) {
                    int bmWidth = bitmapSize.x;
                    int bmHeight = bitmapSize.y;
                    if (WallpaperManagerService.DEBUG) {
                        Slog.d(WallpaperManagerService.TAG, "adjustWallpaperWidth bmWidth = " + bmWidth + ", bmHeight = " + bmHeight);
                    }
                    float ratio = 1.0f;
                    if (bmHeight < maxDim) {
                        ratio = (float) (maxDim / bmHeight);
                    }
                    if (((float) bmWidth) * ratio <= ((float) minDim) && desiredMinimumWidth != minDim) {
                        setDimensionHints_extra(minDim, maxDim);
                    } else if (((float) bmWidth) * ratio > ((float) minDim) && desiredMinimumWidth <= minDim) {
                        setDimensionHints_extra(Math.max((int) (((float) minDim) * 2.0f), maxDim), maxDim);
                    }
                } else if (-1 != this.mWidthOfDefaultWallpaper && desiredMinimumWidth != this.mWidthOfDefaultWallpaper) {
                    setDimensionHints_extra(this.mWidthOfDefaultWallpaper, maxDim);
                }
            } catch (RemoteException e) {
            }
        }

        /* access modifiers changed from: private */
        public boolean isWallpaperExist(int userId) {
            return new File(WallpaperManagerService.getWallpaperDir(userId), WallpaperManagerService.WALLPAPER).exists();
        }

        /* access modifiers changed from: private */
        public boolean isForbidSettingWallpaperVersion() {
            return this.mIsForbidSettingWallpaper;
        }

        private boolean isExpVersion() {
            return this.mIsExpVersion;
        }
    }

    public static class Lifecycle extends SystemService {
        private IWallpaperManagerService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        @Override // com.android.server.SystemService
        public void onStart() {
            try {
                this.mService = (IWallpaperManagerService) Class.forName(getContext().getResources().getString(17039774)).getConstructor(Context.class).newInstance(getContext());
                publishBinderService(WallpaperManagerService.WALLPAPER_CROP, this.mService);
            } catch (Exception exp) {
                Slog.wtf(WallpaperManagerService.TAG, "Failed to instantiate WallpaperManagerService", exp);
            }
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            IWallpaperManagerService iWallpaperManagerService = this.mService;
            if (iWallpaperManagerService != null) {
                iWallpaperManagerService.onBootPhase(phase);
            }
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userHandle) {
            IWallpaperManagerService iWallpaperManagerService = this.mService;
            if (iWallpaperManagerService != null) {
                iWallpaperManagerService.onUnlockUser(userHandle);
            }
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
                    try {
                        wallpaper = (WallpaperData) WallpaperManagerService.this.mLockWallpaperMap.get(this.mUserId);
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                if (wallpaper == null) {
                    wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(this.mUserId);
                }
            }
            return wallpaper != null ? wallpaper : this.mWallpaper;
        }

        /* JADX WARNING: Removed duplicated region for block: B:81:0x017f A[SYNTHETIC, Splitter:B:81:0x017f] */
        @OppoHook(level = OppoHook.OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.Keyguard, 2012.08.27:add for oppo-wallpaper", property = OppoHook.OppoRomType.ROM)
        public void onEvent(int event, String path) {
            int notifyColorsWhich;
            WallpaperData wallpaper;
            WallpaperData wallpaper2;
            if (path != null) {
                boolean moved = event == 128;
                boolean written = event == 8 || moved;
                File changedFile = new File(this.mWallpaperDir, path);
                boolean sysWallpaperChanged = this.mWallpaperFile.equals(changedFile);
                boolean lockWallpaperChanged = this.mWallpaperLockFile.equals(changedFile);
                notifyColorsWhich = 0;
                WallpaperData wallpaper3 = dataForEvent(sysWallpaperChanged, lockWallpaperChanged);
                if (WallpaperManagerService.DEBUG) {
                    Slog.v(WallpaperManagerService.TAG, "Wallpaper file change: evt=" + event + " path=" + path + " sys=" + sysWallpaperChanged + " lock=" + lockWallpaperChanged + " imagePending=" + wallpaper3.imageWallpaperPending + " whichPending=0x" + Integer.toHexString(wallpaper3.whichPending) + " written=" + written);
                }
                if (!moved || !lockWallpaperChanged) {
                    synchronized (WallpaperManagerService.this.mLock) {
                        if (sysWallpaperChanged || lockWallpaperChanged) {
                            try {
                                if (wallpaper3.wallpaperComponent != null && event == 8) {
                                    try {
                                        if (!wallpaper3.imageWallpaperPending) {
                                            wallpaper = wallpaper3;
                                        }
                                    } catch (Throwable th) {
                                        th = th;
                                        throw th;
                                    }
                                }
                                if (written) {
                                    if (WallpaperManagerService.DEBUG) {
                                        Slog.v(WallpaperManagerService.TAG, "Wallpaper written; generating crop");
                                    }
                                    SELinux.restorecon(changedFile);
                                    if (moved) {
                                        if (WallpaperManagerService.DEBUG) {
                                            Slog.v(WallpaperManagerService.TAG, "moved-to, therefore restore; reloading metadata");
                                        }
                                        WallpaperManagerService.this.loadSettingsLocked(wallpaper3.userId, true);
                                    }
                                    WallpaperManagerService.this.generateCrop(wallpaper3);
                                    if (WallpaperManagerService.DEBUG) {
                                        Slog.v(WallpaperManagerService.TAG, "Crop done; invoking completion callback");
                                    }
                                    wallpaper3.imageWallpaperPending = false;
                                    if (sysWallpaperChanged) {
                                        try {
                                            WallpaperManagerService.this.mWallpaperHelper.adjustWallpaperWidth(wallpaper3.userId);
                                            wallpaper2 = wallpaper3;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            throw th;
                                        }
                                        try {
                                            boolean unused = WallpaperManagerService.this.bindWallpaperComponentLocked(WallpaperManagerService.this.mImageWallpaper, true, false, wallpaper2, null);
                                            notifyColorsWhich = 0 | 1;
                                        } catch (Throwable th3) {
                                            th = th3;
                                            throw th;
                                        }
                                    } else {
                                        wallpaper2 = wallpaper3;
                                    }
                                    if (!lockWallpaperChanged) {
                                        wallpaper = wallpaper2;
                                        try {
                                            if ((wallpaper.whichPending & 2) != 0) {
                                            }
                                            WallpaperManagerService.this.saveSettingsLocked(wallpaper.userId);
                                            if (wallpaper.setComplete != null) {
                                                try {
                                                    wallpaper.setComplete.onWallpaperChanged();
                                                } catch (RemoteException e) {
                                                }
                                            }
                                        } catch (Throwable th4) {
                                            th = th4;
                                            throw th;
                                        }
                                    } else {
                                        wallpaper = wallpaper2;
                                    }
                                    if (WallpaperManagerService.DEBUG) {
                                        Slog.i(WallpaperManagerService.TAG, "Lock-relevant wallpaper changed");
                                    }
                                    if (!lockWallpaperChanged) {
                                        WallpaperManagerService.this.mLockWallpaperMap.remove(wallpaper.userId);
                                    }
                                    WallpaperManagerService.this.notifyLockWallpaperChanged();
                                    notifyColorsWhich |= 2;
                                    WallpaperManagerService.this.saveSettingsLocked(wallpaper.userId);
                                    if (wallpaper.setComplete != null) {
                                    }
                                } else {
                                    wallpaper = wallpaper3;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                throw th;
                            }
                        } else {
                            wallpaper = wallpaper3;
                        }
                    }
                } else {
                    if (WallpaperManagerService.DEBUG) {
                        Slog.i(WallpaperManagerService.TAG, "Sys -> lock MOVED_TO");
                    }
                    SELinux.restorecon(changedFile);
                    WallpaperManagerService.this.notifyLockWallpaperChanged();
                    if (wallpaper3.needFastSet) {
                        wallpaper3.needFastSet = false;
                        WallpaperManagerService.this.notifyWallpaperColorsChangedInThread(wallpaper3, 2);
                        return;
                    }
                    WallpaperManagerService.this.notifyWallpaperColorsChanged(wallpaper3, 2);
                    return;
                }
            } else {
                return;
            }
            if (notifyColorsWhich == 0) {
                return;
            }
            if (wallpaper.needFastSet) {
                wallpaper.needFastSet = false;
                WallpaperManagerService.this.notifyWallpaperColorsChangedInThread(wallpaper, notifyColorsWhich);
                return;
            }
            WallpaperManagerService.this.notifyWallpaperColorsChanged(wallpaper, notifyColorsWhich);
        }
    }

    /* access modifiers changed from: private */
    public void notifyLockWallpaperChanged() {
        IWallpaperManagerCallback cb = this.mKeyguardListener;
        if (cb != null) {
            try {
                cb.onWallpaperChanged();
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyWallpaperColorsChanged(WallpaperData wallpaper, int which) {
        if (wallpaper.connection != null) {
            wallpaper.connection.forEachDisplayConnector(new Consumer(wallpaper, which) {
                /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$la7x4YHAl88Cd6HFTscnLBbKfI */
                private final /* synthetic */ WallpaperManagerService.WallpaperData f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    WallpaperManagerService.this.lambda$notifyWallpaperColorsChanged$0$WallpaperManagerService(this.f$1, this.f$2, (WallpaperManagerService.WallpaperConnection.DisplayConnector) obj);
                }
            });
        } else {
            notifyWallpaperColorsChangedOnDisplay(wallpaper, which, 0);
        }
    }

    public /* synthetic */ void lambda$notifyWallpaperColorsChanged$0$WallpaperManagerService(WallpaperData wallpaper, int which, WallpaperConnection.DisplayConnector connector) {
        notifyWallpaperColorsChangedOnDisplay(wallpaper, which, connector.mDisplayId);
    }

    private RemoteCallbackList<IWallpaperManagerCallback> getWallpaperCallbacks(int userId, int displayId) {
        SparseArray<RemoteCallbackList<IWallpaperManagerCallback>> displayListeners = this.mColorsChangedListeners.get(userId);
        if (displayListeners != null) {
            return displayListeners.get(displayId);
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003f, code lost:
        notifyColorListeners(r7.primaryColors, r8, r7.userId, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0046, code lost:
        if (r3 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0048, code lost:
        extractColors(r7);
        r0 = r6.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004d, code lost:
        monitor-enter(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0050, code lost:
        if (r7.primaryColors != null) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0052, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0053, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0055, code lost:
        notifyColorListeners(r7.primaryColors, r8, r7.userId, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        return;
     */
    public void notifyWallpaperColorsChangedOnDisplay(WallpaperData wallpaper, int which, int displayId) {
        synchronized (this.mLock) {
            RemoteCallbackList<IWallpaperManagerCallback> currentUserColorListeners = getWallpaperCallbacks(wallpaper.userId, displayId);
            RemoteCallbackList<IWallpaperManagerCallback> userAllColorListeners = getWallpaperCallbacks(-1, displayId);
            if (!emptyCallbackList(currentUserColorListeners) || !emptyCallbackList(userAllColorListeners)) {
                if (DEBUG) {
                    Slog.v(TAG, "notifyWallpaperColorsChangedOnDisplay " + which);
                }
                boolean needsExtraction = wallpaper.primaryColors == null;
            }
        }
    }

    private static <T extends IInterface> boolean emptyCallbackList(RemoteCallbackList<T> list) {
        return list == null || list.getRegisteredCallbackCount() == 0;
    }

    private void notifyColorListeners(WallpaperColors wallpaperColors, int which, int userId, int displayId) {
        IWallpaperManagerCallback keyguardListener;
        ArrayList<IWallpaperManagerCallback> colorListeners = new ArrayList<>();
        synchronized (this.mLock) {
            RemoteCallbackList<IWallpaperManagerCallback> currentUserColorListeners = getWallpaperCallbacks(userId, displayId);
            RemoteCallbackList<IWallpaperManagerCallback> userAllColorListeners = getWallpaperCallbacks(-1, displayId);
            keyguardListener = this.mKeyguardListener;
            if (currentUserColorListeners != null) {
                int count = currentUserColorListeners.beginBroadcast();
                for (int i = 0; i < count; i++) {
                    colorListeners.add(currentUserColorListeners.getBroadcastItem(i));
                }
                currentUserColorListeners.finishBroadcast();
            }
            if (userAllColorListeners != null) {
                int count2 = userAllColorListeners.beginBroadcast();
                for (int i2 = 0; i2 < count2; i2++) {
                    colorListeners.add(userAllColorListeners.getBroadcastItem(i2));
                }
                userAllColorListeners.finishBroadcast();
            }
        }
        int count3 = colorListeners.size();
        for (int i3 = 0; i3 < count3; i3++) {
            try {
                colorListeners.get(i3).onWallpaperColorsChanged(wallpaperColors, which, userId);
            } catch (RemoteException e) {
            }
        }
        if (keyguardListener != null && displayId == 0) {
            try {
                keyguardListener.onWallpaperColorsChanged(wallpaperColors, which, userId);
            } catch (RemoteException e2) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x0083  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x008b  */
    private void extractColors(WallpaperData wallpaper) {
        boolean imageWallpaper;
        int wallpaperId;
        WallpaperColors colors;
        String cropFile = null;
        boolean defaultImageWallpaper = false;
        if (wallpaper.equals(this.mFallbackWallpaper)) {
            synchronized (this.mLock) {
                if (this.mFallbackWallpaper.primaryColors == null) {
                    WallpaperColors colors2 = extractDefaultImageWallpaperColors();
                    synchronized (this.mLock) {
                        this.mFallbackWallpaper.primaryColors = colors2;
                    }
                    return;
                }
                return;
            }
        }
        synchronized (this.mLock) {
            if (!this.mImageWallpaper.equals(wallpaper.wallpaperComponent)) {
                if (wallpaper.wallpaperComponent != null) {
                    imageWallpaper = false;
                    if (!imageWallpaper && wallpaper.cropFile != null && wallpaper.cropFile.exists()) {
                        cropFile = wallpaper.cropFile.getAbsolutePath();
                    } else if (imageWallpaper && !wallpaper.cropExists() && !wallpaper.sourceExists()) {
                        defaultImageWallpaper = true;
                    }
                    wallpaperId = wallpaper.wallpaperId;
                }
            }
            imageWallpaper = true;
            if (!imageWallpaper) {
            }
            defaultImageWallpaper = true;
            wallpaperId = wallpaper.wallpaperId;
        }
        WallpaperColors colors3 = null;
        if (cropFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(cropFile);
            if (bitmap != null) {
                colors3 = WallpaperColors.fromBitmap(bitmap);
                bitmap.recycle();
            }
        } else if (defaultImageWallpaper) {
            colors = extractDefaultImageWallpaperColors();
            if (colors != null) {
                Slog.w(TAG, "Cannot extract colors because wallpaper could not be read.");
                return;
            }
            synchronized (this.mLock) {
                if (wallpaper.wallpaperId == wallpaperId) {
                    wallpaper.primaryColors = colors;
                    saveSettingsLocked(wallpaper.userId);
                } else {
                    Slog.w(TAG, "Not setting primary colors since wallpaper changed");
                }
            }
            return;
        }
        colors = colors3;
        if (colors != null) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0017, code lost:
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r1 = android.app.WallpaperManager.openDefaultWallpaper(r5.mContext, 1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0020, code lost:
        if (r1 != null) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        android.util.Slog.w(com.android.server.wallpaper.WallpaperManagerService.TAG, "Can't open default wallpaper stream");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002a, code lost:
        if (r1 == null) goto L_0x002f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002c, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x002f, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0030, code lost:
        r2 = android.graphics.BitmapFactory.decodeStream(r1, null, new android.graphics.BitmapFactory.Options());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0039, code lost:
        if (r2 == null) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x003b, code lost:
        r0 = android.app.WallpaperColors.fromBitmap(r2);
        r2.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0043, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0049, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x004a, code lost:
        if (r1 != null) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0050, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0051, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0054, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0055, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0056, code lost:
        android.util.Slog.w(com.android.server.wallpaper.WallpaperManagerService.TAG, "Can't close default wallpaper stream", r1);
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x005f, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0060, code lost:
        android.util.Slog.w(com.android.server.wallpaper.WallpaperManagerService.TAG, "Can't decode default wallpaper stream", r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x006a, code lost:
        android.util.Slog.e(com.android.server.wallpaper.WallpaperManagerService.TAG, "Extract default image wallpaper colors failed");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0074, code lost:
        monitor-enter(r5.mLock);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
        r5.mCacheDefaultImageWallpaperColors = r1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0072  */
    private WallpaperColors extractDefaultImageWallpaperColors() {
        WallpaperColors colors;
        if (DEBUG) {
            Slog.d(TAG, "Extract default image wallpaper colors");
        }
        synchronized (this.mLock) {
            if (this.mCacheDefaultImageWallpaperColors != null) {
                WallpaperColors wallpaperColors = this.mCacheDefaultImageWallpaperColors;
                return wallpaperColors;
            }
        }
        WallpaperColors colors2 = colors;
        if (colors2 != null) {
        }
        return colors2;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.graphics.BitmapRegionDecoder.newInstance(java.lang.String, boolean):android.graphics.BitmapRegionDecoder throws java.io.IOException}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.graphics.BitmapRegionDecoder.newInstance(java.io.FileDescriptor, boolean):android.graphics.BitmapRegionDecoder throws java.io.IOException}
      ClspMth{android.graphics.BitmapRegionDecoder.newInstance(java.io.InputStream, boolean):android.graphics.BitmapRegionDecoder throws java.io.IOException}
      ClspMth{android.graphics.BitmapRegionDecoder.newInstance(java.lang.String, boolean):android.graphics.BitmapRegionDecoder throws java.io.IOException} */
    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x0509 A[Catch:{ Exception -> 0x051a, OutOfMemoryError -> 0x04e6, all -> 0x04d8, all -> 0x053a }] */
    /* JADX WARNING: Removed duplicated region for block: B:184:0x0514 A[Catch:{ Exception -> 0x051a, OutOfMemoryError -> 0x04e6, all -> 0x04d8, all -> 0x053a }] */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x052b A[Catch:{ Exception -> 0x051a, OutOfMemoryError -> 0x04e6, all -> 0x04d8, all -> 0x053a }] */
    /* JADX WARNING: Removed duplicated region for block: B:198:0x0552  */
    /* JADX WARNING: Removed duplicated region for block: B:201:0x0564  */
    /* JADX WARNING: Removed duplicated region for block: B:205:? A[RETURN, SYNTHETIC] */
    public void generateCrop(WallpaperData wallpaper) {
        boolean success;
        FileOutputStream f;
        BufferedOutputStream bos;
        boolean success2;
        int destWidth;
        int newHeight;
        boolean success3 = false;
        DisplayData wpData = getDisplayDataOrCreate(0);
        Rect cropHint = new Rect(wallpaper.cropHint);
        DisplayInfo displayInfo = new DisplayInfo();
        this.mDisplayManager.getDisplay(0).getDisplayInfo(displayInfo);
        if (DEBUG) {
            Slog.v(TAG, "Generating crop for new wallpaper(s): 0x" + Integer.toHexString(wallpaper.whichPending) + " to " + wallpaper.cropFile.getName() + " crop=(" + cropHint.width() + 'x' + cropHint.height() + ") dim=(" + wpData.mWidth + 'x' + wpData.mHeight + ')');
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(wallpaper.wallpaperFile.getAbsolutePath(), options);
        if (options.outWidth > 0) {
            if (options.outHeight > 0) {
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
                    needCrop = options.outHeight > cropHint.height() || options.outWidth > cropHint.width();
                }
                boolean needScale = wpData.mHeight != cropHint.height() || cropHint.height() > GLHelper.getMaxTextureSize() || cropHint.width() > GLHelper.getMaxTextureSize();
                if (needScale) {
                    if (((int) (((float) cropHint.width()) * (((float) wpData.mHeight) / ((float) cropHint.height())))) < displayInfo.logicalWidth) {
                        cropHint.bottom = (int) (((float) cropHint.width()) * (((float) displayInfo.logicalHeight) / ((float) displayInfo.logicalWidth)));
                        needCrop = true;
                    }
                }
                if (DEBUG) {
                    Slog.v(TAG, "crop: w=" + cropHint.width() + " h=" + cropHint.height());
                    Slog.v(TAG, "dims: w=" + wpData.mWidth + " h=" + wpData.mHeight);
                    Slog.v(TAG, "meas: w=" + options.outWidth + " h=" + options.outHeight);
                    Slog.v(TAG, "crop?=" + needCrop + " scale?=" + needScale);
                }
                if (needCrop || needScale) {
                    FileOutputStream f2 = null;
                    BufferedOutputStream bos2 = null;
                    BitmapRegionDecoder decoder = null;
                    try {
                        decoder = BitmapRegionDecoder.newInstance(wallpaper.wallpaperFile.getAbsolutePath(), false);
                        int scale = 1;
                        while (true) {
                            success2 = success3;
                            if (scale * 2 <= cropHint.height() / wpData.mHeight) {
                                scale *= 2;
                                success3 = success2;
                            } else {
                                try {
                                    break;
                                } catch (Exception e) {
                                    e = e;
                                    if (DEBUG) {
                                    }
                                    IoUtils.closeQuietly(bos2);
                                    IoUtils.closeQuietly(f2);
                                    success = success2;
                                    if (!success) {
                                    }
                                    if (wallpaper.cropFile.exists()) {
                                    }
                                } catch (OutOfMemoryError e2) {
                                    Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + decoder);
                                    if (decoder != null) {
                                    }
                                    if (!wallpaper.wallpaperFile.delete()) {
                                    }
                                    IoUtils.closeQuietly(bos2);
                                    IoUtils.closeQuietly(f2);
                                    success = success2;
                                    if (!success) {
                                    }
                                    if (wallpaper.cropFile.exists()) {
                                    }
                                } catch (Throwable th) {
                                    th = th;
                                    f = null;
                                    bos = null;
                                    IoUtils.closeQuietly(bos);
                                    IoUtils.closeQuietly(f);
                                    throw th;
                                }
                            }
                        }
                        options.inSampleSize = scale;
                        options.inJustDecodeBounds = false;
                        Rect estimateCrop = new Rect(cropHint);
                        estimateCrop.scale(1.0f / ((float) options.inSampleSize));
                        float hRatio = ((float) wpData.mHeight) / ((float) estimateCrop.height());
                        int destHeight = (int) (((float) estimateCrop.height()) * hRatio);
                        try {
                            destWidth = (int) (((float) estimateCrop.width()) * hRatio);
                            f = null;
                        } catch (Exception e3) {
                            e = e3;
                            if (DEBUG) {
                            }
                            IoUtils.closeQuietly(bos2);
                            IoUtils.closeQuietly(f2);
                            success = success2;
                            if (!success) {
                            }
                            if (wallpaper.cropFile.exists()) {
                            }
                        } catch (OutOfMemoryError e4) {
                            Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + decoder);
                            if (decoder != null) {
                            }
                            if (!wallpaper.wallpaperFile.delete()) {
                            }
                            IoUtils.closeQuietly(bos2);
                            IoUtils.closeQuietly(f2);
                            success = success2;
                            if (!success) {
                            }
                            if (wallpaper.cropFile.exists()) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            f = null;
                            bos = null;
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(f);
                            throw th;
                        }
                        try {
                            if (destWidth > GLHelper.getMaxTextureSize()) {
                                try {
                                    newHeight = (int) (((float) wpData.mHeight) / hRatio);
                                } catch (Exception e5) {
                                    e = e5;
                                    f2 = null;
                                    if (DEBUG) {
                                    }
                                    IoUtils.closeQuietly(bos2);
                                    IoUtils.closeQuietly(f2);
                                    success = success2;
                                    if (!success) {
                                    }
                                    if (wallpaper.cropFile.exists()) {
                                    }
                                } catch (OutOfMemoryError e6) {
                                    f2 = null;
                                    Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + decoder);
                                    if (decoder != null) {
                                    }
                                    if (!wallpaper.wallpaperFile.delete()) {
                                    }
                                    IoUtils.closeQuietly(bos2);
                                    IoUtils.closeQuietly(f2);
                                    success = success2;
                                    if (!success) {
                                    }
                                    if (wallpaper.cropFile.exists()) {
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    bos = null;
                                    IoUtils.closeQuietly(bos);
                                    IoUtils.closeQuietly(f);
                                    throw th;
                                }
                                try {
                                    int newWidth = (int) (((float) wpData.mWidth) / hRatio);
                                    if (DEBUG) {
                                        try {
                                            Slog.v(TAG, "Invalid crop dimensions, trying to adjust.");
                                        } catch (Exception e7) {
                                            e = e7;
                                            f2 = null;
                                            if (DEBUG) {
                                            }
                                            IoUtils.closeQuietly(bos2);
                                            IoUtils.closeQuietly(f2);
                                            success = success2;
                                            if (!success) {
                                            }
                                            if (wallpaper.cropFile.exists()) {
                                            }
                                        } catch (OutOfMemoryError e8) {
                                            f2 = null;
                                            Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + decoder);
                                            if (decoder != null) {
                                            }
                                            if (!wallpaper.wallpaperFile.delete()) {
                                            }
                                            IoUtils.closeQuietly(bos2);
                                            IoUtils.closeQuietly(f2);
                                            success = success2;
                                            if (!success) {
                                            }
                                            if (wallpaper.cropFile.exists()) {
                                            }
                                        } catch (Throwable th4) {
                                            th = th4;
                                            bos = null;
                                            IoUtils.closeQuietly(bos);
                                            IoUtils.closeQuietly(f);
                                            throw th;
                                        }
                                    }
                                    estimateCrop.set(cropHint);
                                    estimateCrop.left += (cropHint.width() - newWidth) / 2;
                                    estimateCrop.top += (cropHint.height() - newHeight) / 2;
                                    estimateCrop.right = estimateCrop.left + newWidth;
                                    estimateCrop.bottom = estimateCrop.top + newHeight;
                                    cropHint.set(estimateCrop);
                                    estimateCrop.scale(1.0f / ((float) options.inSampleSize));
                                } catch (Exception e9) {
                                    e = e9;
                                    f2 = null;
                                    if (DEBUG) {
                                    }
                                    IoUtils.closeQuietly(bos2);
                                    IoUtils.closeQuietly(f2);
                                    success = success2;
                                    if (!success) {
                                    }
                                    if (wallpaper.cropFile.exists()) {
                                    }
                                } catch (OutOfMemoryError e10) {
                                    f2 = null;
                                    Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + decoder);
                                    if (decoder != null) {
                                    }
                                    if (!wallpaper.wallpaperFile.delete()) {
                                    }
                                    IoUtils.closeQuietly(bos2);
                                    IoUtils.closeQuietly(f2);
                                    success = success2;
                                    if (!success) {
                                    }
                                    if (wallpaper.cropFile.exists()) {
                                    }
                                } catch (Throwable th5) {
                                    th = th5;
                                    bos = null;
                                    IoUtils.closeQuietly(bos);
                                    IoUtils.closeQuietly(f);
                                    throw th;
                                }
                            }
                            try {
                                int safeHeight = (int) (((float) estimateCrop.height()) * hRatio);
                                int safeWidth = (int) (((float) estimateCrop.width()) * hRatio);
                                if (DEBUG) {
                                    Slog.v(TAG, "Decode parameters:");
                                    StringBuilder sb = new StringBuilder();
                                    bos = null;
                                    try {
                                        sb.append("  cropHint=");
                                        sb.append(cropHint);
                                        sb.append(", estimateCrop=");
                                        sb.append(estimateCrop);
                                        Slog.v(TAG, sb.toString());
                                        Slog.v(TAG, "  down sampling=" + options.inSampleSize + ", hRatio=" + hRatio);
                                        Slog.v(TAG, "  dest=" + destWidth + "x" + destHeight);
                                        Slog.v(TAG, "  safe=" + safeWidth + "x" + safeHeight);
                                        StringBuilder sb2 = new StringBuilder();
                                        sb2.append("  maxTextureSize=");
                                        sb2.append(GLHelper.getMaxTextureSize());
                                        Slog.v(TAG, sb2.toString());
                                    } catch (Exception e11) {
                                        e = e11;
                                        bos2 = null;
                                        f2 = null;
                                        if (DEBUG) {
                                            Slog.e(TAG, "Error decoding crop", e);
                                        }
                                        IoUtils.closeQuietly(bos2);
                                        IoUtils.closeQuietly(f2);
                                        success = success2;
                                        if (!success) {
                                        }
                                        if (wallpaper.cropFile.exists()) {
                                        }
                                    } catch (OutOfMemoryError e12) {
                                        bos2 = null;
                                        f2 = null;
                                        Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + decoder);
                                        if (decoder != null) {
                                            decoder.recycle();
                                        }
                                        if (!wallpaper.wallpaperFile.delete()) {
                                            Slog.e(TAG, "generateCrop wallpaper.wallpaperFile.delete() fail!");
                                        }
                                        IoUtils.closeQuietly(bos2);
                                        IoUtils.closeQuietly(f2);
                                        success = success2;
                                        if (!success) {
                                        }
                                        if (wallpaper.cropFile.exists()) {
                                        }
                                    } catch (Throwable th6) {
                                        th = th6;
                                        IoUtils.closeQuietly(bos);
                                        IoUtils.closeQuietly(f);
                                        throw th;
                                    }
                                } else {
                                    bos = null;
                                }
                                Bitmap cropped = decoder.decodeRegion(cropHint, options);
                                decoder.recycle();
                                if (cropped == null) {
                                    Slog.e(TAG, "Could not decode new wallpaper");
                                    success = success2;
                                    bos2 = bos;
                                } else {
                                    Bitmap finalCrop = Bitmap.createScaledBitmap(cropped, safeWidth, safeHeight, true);
                                    if (DEBUG) {
                                        Slog.v(TAG, "Final extract:");
                                        Slog.v(TAG, "  dims: w=" + wpData.mWidth + " h=" + wpData.mHeight);
                                        Slog.v(TAG, "  out: w=" + finalCrop.getWidth() + " h=" + finalCrop.getHeight());
                                    }
                                    if (finalCrop.getByteCount() <= MAX_BITMAP_SIZE) {
                                        FileOutputStream f3 = new FileOutputStream(wallpaper.cropFile);
                                        try {
                                            bos2 = new BufferedOutputStream(f3, 32768);
                                            try {
                                                finalCrop.compress(Bitmap.CompressFormat.JPEG, 100, bos2);
                                                bos2.flush();
                                                success = true;
                                                f = f3;
                                            } catch (Exception e13) {
                                                e = e13;
                                                f2 = f3;
                                                if (DEBUG) {
                                                }
                                                IoUtils.closeQuietly(bos2);
                                                IoUtils.closeQuietly(f2);
                                                success = success2;
                                                if (!success) {
                                                }
                                                if (wallpaper.cropFile.exists()) {
                                                }
                                            } catch (OutOfMemoryError e14) {
                                                f2 = f3;
                                                Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + decoder);
                                                if (decoder != null) {
                                                }
                                                if (!wallpaper.wallpaperFile.delete()) {
                                                }
                                                IoUtils.closeQuietly(bos2);
                                                IoUtils.closeQuietly(f2);
                                                success = success2;
                                                if (!success) {
                                                }
                                                if (wallpaper.cropFile.exists()) {
                                                }
                                            } catch (Throwable th7) {
                                                th = th7;
                                                f = f3;
                                                bos = bos2;
                                                IoUtils.closeQuietly(bos);
                                                IoUtils.closeQuietly(f);
                                                throw th;
                                            }
                                        } catch (Exception e15) {
                                            e = e15;
                                            f2 = f3;
                                            bos2 = bos;
                                            if (DEBUG) {
                                            }
                                            IoUtils.closeQuietly(bos2);
                                            IoUtils.closeQuietly(f2);
                                            success = success2;
                                            if (!success) {
                                            }
                                            if (wallpaper.cropFile.exists()) {
                                            }
                                        } catch (OutOfMemoryError e16) {
                                            f2 = f3;
                                            bos2 = bos;
                                            Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + decoder);
                                            if (decoder != null) {
                                            }
                                            if (!wallpaper.wallpaperFile.delete()) {
                                            }
                                            IoUtils.closeQuietly(bos2);
                                            IoUtils.closeQuietly(f2);
                                            success = success2;
                                            if (!success) {
                                            }
                                            if (wallpaper.cropFile.exists()) {
                                            }
                                        } catch (Throwable th8) {
                                            th = th8;
                                            f = f3;
                                            IoUtils.closeQuietly(bos);
                                            IoUtils.closeQuietly(f);
                                            throw th;
                                        }
                                    } else {
                                        throw new RuntimeException("Too large bitmap, limit=104857600");
                                    }
                                }
                                IoUtils.closeQuietly(bos2);
                                IoUtils.closeQuietly(f);
                            } catch (Exception e17) {
                                e = e17;
                                f2 = null;
                                if (DEBUG) {
                                }
                                IoUtils.closeQuietly(bos2);
                                IoUtils.closeQuietly(f2);
                                success = success2;
                                if (!success) {
                                }
                                if (wallpaper.cropFile.exists()) {
                                }
                            } catch (OutOfMemoryError e18) {
                                f2 = null;
                                Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + decoder);
                                if (decoder != null) {
                                }
                                if (!wallpaper.wallpaperFile.delete()) {
                                }
                                IoUtils.closeQuietly(bos2);
                                IoUtils.closeQuietly(f2);
                                success = success2;
                                if (!success) {
                                }
                                if (wallpaper.cropFile.exists()) {
                                }
                            } catch (Throwable th9) {
                                th = th9;
                                bos = null;
                                IoUtils.closeQuietly(bos);
                                IoUtils.closeQuietly(f);
                                throw th;
                            }
                        } catch (Exception e19) {
                            e = e19;
                            f2 = null;
                            if (DEBUG) {
                            }
                            IoUtils.closeQuietly(bos2);
                            IoUtils.closeQuietly(f2);
                            success = success2;
                            if (!success) {
                            }
                            if (wallpaper.cropFile.exists()) {
                            }
                        } catch (OutOfMemoryError e20) {
                            f2 = null;
                            Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + decoder);
                            if (decoder != null) {
                            }
                            if (!wallpaper.wallpaperFile.delete()) {
                            }
                            IoUtils.closeQuietly(bos2);
                            IoUtils.closeQuietly(f2);
                            success = success2;
                            if (!success) {
                            }
                            if (wallpaper.cropFile.exists()) {
                            }
                        } catch (Throwable th10) {
                            th = th10;
                            bos = null;
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(f);
                            throw th;
                        }
                    } catch (Exception e21) {
                        e = e21;
                        success2 = false;
                        if (DEBUG) {
                        }
                        IoUtils.closeQuietly(bos2);
                        IoUtils.closeQuietly(f2);
                        success = success2;
                        if (!success) {
                        }
                        if (wallpaper.cropFile.exists()) {
                        }
                    } catch (OutOfMemoryError e22) {
                        success2 = false;
                        Slog.e(TAG, "generateCrop OutOfMemoryError decoder = " + decoder);
                        if (decoder != null) {
                        }
                        if (!wallpaper.wallpaperFile.delete()) {
                        }
                        IoUtils.closeQuietly(bos2);
                        IoUtils.closeQuietly(f2);
                        success = success2;
                        if (!success) {
                        }
                        if (wallpaper.cropFile.exists()) {
                        }
                    } catch (Throwable th11) {
                        th = th11;
                        f = f2;
                        bos = bos2;
                        IoUtils.closeQuietly(bos);
                        IoUtils.closeQuietly(f);
                        throw th;
                    }
                    if (!success) {
                        Slog.e(TAG, "Unable to apply new wallpaper");
                        wallpaper.cropFile.delete();
                    }
                    if (wallpaper.cropFile.exists()) {
                        boolean didRestorecon = SELinux.restorecon(wallpaper.cropFile.getAbsoluteFile());
                        if (DEBUG) {
                            Slog.v(TAG, "restorecon() of crop file returned " + didRestorecon);
                            return;
                        }
                        return;
                    }
                    return;
                }
                long estimateSize = (long) (options.outWidth * options.outHeight * 4);
                if (estimateSize < 104857600) {
                    success3 = FileUtils.copyFile(wallpaper.wallpaperFile, wallpaper.cropFile);
                }
                if (!success) {
                    wallpaper.cropFile.delete();
                }
                if (DEBUG) {
                    Slog.v(TAG, "Null crop of new wallpaper, estimate size=" + estimateSize + ", success=" + success);
                }
                if (!success) {
                }
                if (wallpaper.cropFile.exists()) {
                }
            }
        }
        Slog.w(TAG, "Invalid wallpaper data");
        success = false;
        if (!success) {
        }
        if (wallpaper.cropFile.exists()) {
        }
    }

    /* access modifiers changed from: package-private */
    @OppoHook(level = OppoHook.OppoHookType.NEW_FIELD, note = "XinYang.Hu@Apps.Launcher, 2019/1/4:add for set wallpaper fast", property = OppoHook.OppoRomType.ROM)
    public static class WallpaperData {
        boolean allowBackup;
        /* access modifiers changed from: private */
        public RemoteCallbackList<IWallpaperManagerCallback> callbacks = new RemoteCallbackList<>();
        WallpaperConnection connection;
        final File cropFile;
        final Rect cropHint = new Rect(0, 0, 0, 0);
        boolean imageWallpaperPending;
        long lastDiedTime;
        String name = "";
        boolean needFastSet;
        ComponentName nextWallpaperComponent;
        WallpaperColors primaryColors;
        IWallpaperManagerCallback setComplete;
        int userId;
        ComponentName wallpaperComponent;
        final File wallpaperFile;
        int wallpaperId;
        WallpaperObserver wallpaperObserver;
        boolean wallpaperUpdating;
        int whichPending;

        WallpaperData(int userId2, String inputFileName, String cropFileName) {
            this.userId = userId2;
            File wallpaperDir = WallpaperManagerService.getWallpaperDir(userId2);
            this.wallpaperFile = new File(wallpaperDir, inputFileName);
            this.cropFile = new File(wallpaperDir, cropFileName);
        }

        /* access modifiers changed from: package-private */
        public boolean cropExists() {
            return this.cropFile.exists();
        }

        /* access modifiers changed from: package-private */
        public boolean sourceExists() {
            return this.wallpaperFile.exists();
        }
    }

    /* access modifiers changed from: private */
    public static final class DisplayData {
        final int mDisplayId;
        int mHeight = -1;
        final Rect mPadding = new Rect(0, 0, 0, 0);
        int mWidth = -1;

        DisplayData(int displayId) {
            this.mDisplayId = displayId;
        }
    }

    /* access modifiers changed from: private */
    public void removeDisplayData(int displayId) {
        this.mDisplayDatas.remove(displayId);
    }

    /* access modifiers changed from: private */
    public DisplayData getDisplayDataOrCreate(int displayId) {
        DisplayData wpdData = this.mDisplayDatas.get(displayId);
        if (wpdData != null) {
            return wpdData;
        }
        DisplayData wpdData2 = new DisplayData(displayId);
        ensureSaneWallpaperDisplaySize(wpdData2, displayId);
        this.mDisplayDatas.append(displayId, wpdData2);
        return wpdData2;
    }

    private void ensureSaneWallpaperDisplaySize(DisplayData wpdData, int displayId) {
        int baseSize = getMaximumSizeDimension(displayId);
        if (wpdData.mWidth < baseSize) {
            wpdData.mWidth = baseSize;
        }
        if (wpdData.mHeight < baseSize) {
            wpdData.mHeight = baseSize;
        }
    }

    private int getMaximumSizeDimension(int displayId) {
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display == null) {
            Slog.w(TAG, "Invalid displayId=" + displayId + StringUtils.SPACE + Debug.getCallers(4));
            display = this.mDisplayManager.getDisplay(0);
        }
        return display.getMaximumSizeDimension();
    }

    /* access modifiers changed from: package-private */
    public void forEachDisplayData(Consumer<DisplayData> action) {
        for (int i = this.mDisplayDatas.size() - 1; i >= 0; i--) {
            action.accept(this.mDisplayDatas.valueAt(i));
        }
    }

    /* access modifiers changed from: package-private */
    public int makeWallpaperIdLocked() {
        int i;
        do {
            this.mWallpaperId++;
            i = this.mWallpaperId;
        } while (i == 0);
        return i;
    }

    /* access modifiers changed from: private */
    public boolean supportsMultiDisplay(WallpaperConnection connection) {
        if (connection == null) {
            return false;
        }
        if (connection.mInfo == null || connection.mInfo.supportsMultipleDisplays()) {
            return true;
        }
        return false;
    }

    private void updateFallbackConnection() {
        WallpaperData wallpaperData = this.mLastWallpaper;
        if (wallpaperData != null && this.mFallbackWallpaper != null) {
            WallpaperConnection systemConnection = wallpaperData.connection;
            WallpaperConnection fallbackConnection = this.mFallbackWallpaper.connection;
            if (fallbackConnection == null) {
                Slog.w(TAG, "Fallback wallpaper connection has not been created yet!!");
            } else if (!supportsMultiDisplay(systemConnection)) {
                fallbackConnection.appendConnectorWithCondition(new Predicate() {
                    /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$SxaUJpgTTfzUoz6u3AWuAOQdoNw */

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return WallpaperManagerService.lambda$updateFallbackConnection$2(WallpaperManagerService.WallpaperConnection.this, (Display) obj);
                    }
                });
                fallbackConnection.forEachDisplayConnector(new Consumer(fallbackConnection) {
                    /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$tRb4SPHGj0pcxb3p7arcqKFqs08 */
                    private final /* synthetic */ WallpaperManagerService.WallpaperConnection f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        WallpaperManagerService.this.lambda$updateFallbackConnection$3$WallpaperManagerService(this.f$1, (WallpaperManagerService.WallpaperConnection.DisplayConnector) obj);
                    }
                });
            } else if (fallbackConnection.mDisplayConnector.size() != 0) {
                fallbackConnection.forEachDisplayConnector($$Lambda$WallpaperManagerService$pVmree9DyIpBSg0s3RDK3MDesvs.INSTANCE);
                fallbackConnection.mDisplayConnector.clear();
            }
        }
    }

    static /* synthetic */ void lambda$updateFallbackConnection$1(WallpaperConnection.DisplayConnector connector) {
        if (connector.mEngine != null) {
            connector.disconnectLocked();
        }
    }

    static /* synthetic */ boolean lambda$updateFallbackConnection$2(WallpaperConnection fallbackConnection, Display display) {
        return fallbackConnection.isUsableDisplay(display) && display.getDisplayId() != 0 && !fallbackConnection.containsDisplay(display.getDisplayId());
    }

    public /* synthetic */ void lambda$updateFallbackConnection$3$WallpaperManagerService(WallpaperConnection fallbackConnection, WallpaperConnection.DisplayConnector connector) {
        if (connector.mEngine == null) {
            connector.connectLocked(fallbackConnection, this.mFallbackWallpaper);
        }
    }

    /* access modifiers changed from: package-private */
    public class WallpaperConnection extends IWallpaperConnection.Stub implements ServiceConnection {
        private static final long WALLPAPER_RECONNECT_TIMEOUT_MS = 10000;
        final int mClientUid;
        /* access modifiers changed from: private */
        public SparseArray<DisplayConnector> mDisplayConnector = new SparseArray<>();
        final WallpaperInfo mInfo;
        IRemoteCallback mReply;
        /* access modifiers changed from: private */
        public Runnable mResetRunnable = new Runnable() {
            /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$WallpaperConnection$QhODF3vswnwSYvDbeEhU85gOBw */

            public final void run() {
                WallpaperManagerService.WallpaperConnection.this.lambda$new$0$WallpaperManagerService$WallpaperConnection();
            }
        };
        IWallpaperService mService;
        WallpaperData mWallpaper;

        /* access modifiers changed from: private */
        public final class DisplayConnector {
            boolean mDimensionsChanged;
            final int mDisplayId;
            /* access modifiers changed from: package-private */
            public IWallpaperEngine mEngine;
            boolean mPaddingChanged;
            final Binder mToken = new Binder();

            DisplayConnector(int displayId) {
                this.mDisplayId = displayId;
            }

            /* access modifiers changed from: package-private */
            public void ensureStatusHandled() {
                DisplayData wpdData = WallpaperManagerService.this.getDisplayDataOrCreate(this.mDisplayId);
                if (this.mDimensionsChanged) {
                    try {
                        this.mEngine.setDesiredSize(wpdData.mWidth, wpdData.mHeight);
                    } catch (RemoteException e) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to set wallpaper dimensions", e);
                    }
                    this.mDimensionsChanged = false;
                }
                if (this.mPaddingChanged) {
                    try {
                        this.mEngine.setDisplayPadding(wpdData.mPadding);
                    } catch (RemoteException e2) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to set wallpaper padding", e2);
                    }
                    this.mPaddingChanged = false;
                }
            }

            /* access modifiers changed from: package-private */
            public void connectLocked(WallpaperConnection connection, WallpaperData wallpaper) {
                if (connection.mService == null) {
                    Slog.w(WallpaperManagerService.TAG, "WallpaperService is not connected yet");
                    return;
                }
                if (WallpaperManagerService.DEBUG) {
                    Slog.v(WallpaperManagerService.TAG, "Adding window token: " + this.mToken);
                }
                try {
                    WallpaperManagerService.this.mIWindowManager.addWindowToken(this.mToken, 2013, this.mDisplayId);
                    DisplayData wpdData = WallpaperManagerService.this.getDisplayDataOrCreate(this.mDisplayId);
                    try {
                        connection.mService.attach(connection, this.mToken, 2013, false, wpdData.mWidth, wpdData.mHeight, wpdData.mPadding, this.mDisplayId);
                    } catch (RemoteException e) {
                        Slog.w(WallpaperManagerService.TAG, "Failed attaching wallpaper on display", e);
                        if (wallpaper != null && !wallpaper.wallpaperUpdating && connection.getConnectedEngineSize() == 0) {
                            boolean unused = WallpaperManagerService.this.bindWallpaperComponentLocked(null, false, false, wallpaper, null);
                        }
                    }
                } catch (RemoteException e2) {
                    Slog.e(WallpaperManagerService.TAG, "Failed add wallpaper window token on display " + this.mDisplayId, e2);
                }
            }

            /* access modifiers changed from: package-private */
            public void disconnectLocked() {
                if (WallpaperManagerService.DEBUG) {
                    Slog.v(WallpaperManagerService.TAG, "Removing window token: " + this.mToken);
                }
                try {
                    WallpaperManagerService.this.mIWindowManager.removeWindowToken(this.mToken, this.mDisplayId);
                } catch (RemoteException e) {
                }
                try {
                    if (this.mEngine != null) {
                        this.mEngine.destroy();
                    }
                } catch (RemoteException e2) {
                }
                this.mEngine = null;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0055, code lost:
            return;
         */
        public /* synthetic */ void lambda$new$0$WallpaperManagerService$WallpaperConnection() {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mShuttingDown) {
                    Slog.i(WallpaperManagerService.TAG, "Ignoring relaunch timeout during shutdown");
                } else if (!this.mWallpaper.wallpaperUpdating && this.mWallpaper.userId == WallpaperManagerService.this.mCurrentUserId) {
                    Slog.w(WallpaperManagerService.TAG, "Wallpaper reconnect timed out for " + this.mWallpaper.wallpaperComponent + ", reverting to built-in wallpaper!");
                    WallpaperManagerService.this.clearWallpaperLocked(true, 1, this.mWallpaper.userId, null);
                }
            }
        }

        WallpaperConnection(WallpaperInfo info, WallpaperData wallpaper, int clientUid) {
            this.mInfo = info;
            this.mWallpaper = wallpaper;
            this.mClientUid = clientUid;
            initDisplayState();
        }

        private void initDisplayState() {
            if (this.mWallpaper.equals(WallpaperManagerService.this.mFallbackWallpaper)) {
                return;
            }
            if (WallpaperManagerService.this.supportsMultiDisplay(this)) {
                appendConnectorWithCondition(new Predicate() {
                    /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$WallpaperConnection$NrNkceFJLqjCb8eAxErUhpLd5c8 */

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return WallpaperManagerService.WallpaperConnection.this.isUsableDisplay((Display) obj);
                    }
                });
            } else {
                this.mDisplayConnector.append(0, new DisplayConnector(0));
            }
        }

        /* access modifiers changed from: private */
        public void appendConnectorWithCondition(Predicate<Display> tester) {
            Display[] displays = WallpaperManagerService.this.mDisplayManager.getDisplays();
            for (Display display : displays) {
                if (tester.test(display)) {
                    int displayId = display.getDisplayId();
                    if (this.mDisplayConnector.get(displayId) == null) {
                        this.mDisplayConnector.append(displayId, new DisplayConnector(displayId));
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        public boolean isUsableDisplay(Display display) {
            if (display == null || !display.hasAccess(this.mClientUid)) {
                return false;
            }
            int displayId = display.getDisplayId();
            if (displayId == 0) {
                return true;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                return WallpaperManagerService.this.mWindowManagerInternal.shouldShowSystemDecorOnDisplay(displayId);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        /* access modifiers changed from: package-private */
        public void forEachDisplayConnector(Consumer<DisplayConnector> action) {
            for (int i = this.mDisplayConnector.size() - 1; i >= 0; i--) {
                action.accept(this.mDisplayConnector.valueAt(i));
            }
        }

        /* access modifiers changed from: package-private */
        public int getConnectedEngineSize() {
            int engineSize = 0;
            for (int i = this.mDisplayConnector.size() - 1; i >= 0; i--) {
                if (this.mDisplayConnector.valueAt(i).mEngine != null) {
                    engineSize++;
                }
            }
            return engineSize;
        }

        /* access modifiers changed from: package-private */
        public DisplayConnector getDisplayConnectorOrCreate(int displayId) {
            DisplayConnector connector = this.mDisplayConnector.get(displayId);
            if (connector != null || !isUsableDisplay(WallpaperManagerService.this.mDisplayManager.getDisplay(displayId))) {
                return connector;
            }
            DisplayConnector connector2 = new DisplayConnector(displayId);
            this.mDisplayConnector.append(displayId, connector2);
            return connector2;
        }

        /* access modifiers changed from: package-private */
        public boolean containsDisplay(int displayId) {
            return this.mDisplayConnector.get(displayId) != null;
        }

        /* access modifiers changed from: package-private */
        public void removeDisplayConnector(int displayId) {
            if (this.mDisplayConnector.get(displayId) != null) {
                this.mDisplayConnector.remove(displayId);
            }
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (this.mWallpaper.connection == this) {
                    this.mService = IWallpaperService.Stub.asInterface(service);
                    WallpaperManagerService.this.attachServiceLocked(this, this.mWallpaper);
                    if (!this.mWallpaper.equals(WallpaperManagerService.this.mFallbackWallpaper)) {
                        WallpaperManagerService.this.saveSettingsLocked(this.mWallpaper.userId);
                    }
                    FgThread.getHandler().removeCallbacks(this.mResetRunnable);
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (WallpaperManagerService.this.mLock) {
                Slog.w(WallpaperManagerService.TAG, "Wallpaper service gone: " + name);
                if (!Objects.equals(name, this.mWallpaper.wallpaperComponent)) {
                    Slog.e(WallpaperManagerService.TAG, "Does not match expected wallpaper component " + this.mWallpaper.wallpaperComponent);
                }
                this.mService = null;
                forEachDisplayConnector($$Lambda$WallpaperManagerService$WallpaperConnection$87DhM3RJJxRNtgkHmd_gtnGkz4.INSTANCE);
                if (this.mWallpaper.connection == this && !this.mWallpaper.wallpaperUpdating) {
                    WallpaperManagerService.this.mContext.getMainThreadHandler().postDelayed(new Runnable() {
                        /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$WallpaperConnection$Yk86TTURTI5B9DzxOzMQGDq7aQU */

                        public final void run() {
                            WallpaperManagerService.WallpaperConnection.this.lambda$onServiceDisconnected$2$WallpaperManagerService$WallpaperConnection();
                        }
                    }, 1000);
                }
            }
        }

        public /* synthetic */ void lambda$onServiceDisconnected$2$WallpaperManagerService$WallpaperConnection() {
            processDisconnect(this);
        }

        public void scheduleTimeoutLocked() {
            Handler fgHandler = FgThread.getHandler();
            fgHandler.removeCallbacks(this.mResetRunnable);
            fgHandler.postDelayed(this.mResetRunnable, 10000);
            Slog.i(WallpaperManagerService.TAG, "Started wallpaper reconnect timeout for " + this.mWallpaper.wallpaperComponent);
        }

        private void processDisconnect(ServiceConnection connection) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (connection == this.mWallpaper.connection) {
                    ComponentName wpService = this.mWallpaper.wallpaperComponent;
                    if (!this.mWallpaper.wallpaperUpdating && this.mWallpaper.userId == WallpaperManagerService.this.mCurrentUserId && !Objects.equals(WallpaperManagerService.this.mDefaultWallpaperComponent, wpService) && !Objects.equals(WallpaperManagerService.this.mImageWallpaper, wpService)) {
                        if (this.mWallpaper.lastDiedTime == 0 || this.mWallpaper.lastDiedTime + 10000 <= SystemClock.uptimeMillis()) {
                            this.mWallpaper.lastDiedTime = SystemClock.uptimeMillis();
                            WallpaperManagerService.this.clearWallpaperComponentLocked(this.mWallpaper);
                            if (WallpaperManagerService.this.bindWallpaperComponentLocked(wpService, false, false, this.mWallpaper, null)) {
                                this.mWallpaper.connection.scheduleTimeoutLocked();
                            } else {
                                Slog.w(WallpaperManagerService.TAG, "Reverting to built-in wallpaper!");
                                WallpaperManagerService.this.clearWallpaperLocked(true, 1, this.mWallpaper.userId, null);
                            }
                        } else {
                            Slog.w(WallpaperManagerService.TAG, "Reverting to built-in wallpaper!");
                            boolean isImageWallpaper = WallpaperManagerService.this.mImageWallpaper != null ? WallpaperManagerService.this.mImageWallpaper.equals(this.mWallpaper.wallpaperComponent) : false;
                            Slog.d(WallpaperManagerService.TAG, "onServiceDisconnected isImageWallpaper = " + isImageWallpaper);
                            if (!isImageWallpaper) {
                                WallpaperManagerService.this.clearWallpaperLocked(true, 1, this.mWallpaper.userId, null);
                            }
                        }
                        String flattened = wpService.flattenToString();
                        EventLog.writeEvent((int) EventLogTags.WP_WALLPAPER_CRASHED, flattened.substring(0, Math.min(flattened.length(), 128)));
                    }
                } else {
                    Slog.i(WallpaperManagerService.TAG, "Wallpaper changed during disconnect tracking; ignoring");
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0035, code lost:
            if (r1 == 0) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0037, code lost:
            com.android.server.wallpaper.WallpaperManagerService.access$3300(r4.this$0, r4.mWallpaper, r1, r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            return;
         */
        public void onWallpaperColorsChanged(WallpaperColors primaryColors, int displayId) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (!WallpaperManagerService.this.mImageWallpaper.equals(this.mWallpaper.wallpaperComponent)) {
                    this.mWallpaper.primaryColors = primaryColors;
                    int which = 1;
                    if (displayId == 0 && ((WallpaperData) WallpaperManagerService.this.mLockWallpaperMap.get(this.mWallpaper.userId)) == null) {
                        which = 1 | 2;
                    }
                }
            }
        }

        @OppoHook(level = OppoHook.OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.LauncherCenter, 2016.10.01:add for if wallpaper changed, send wallpaper change broadcast", property = OppoHook.OppoRomType.ROM)
        public void attachEngine(IWallpaperEngine engine, int displayId) {
            synchronized (WallpaperManagerService.this.mLock) {
                DisplayConnector connector = getDisplayConnectorOrCreate(displayId);
                if (connector == null) {
                    try {
                        engine.destroy();
                    } catch (RemoteException e) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to destroy engine", e);
                    }
                } else {
                    connector.mEngine = engine;
                    connector.ensureStatusHandled();
                    WallpaperManagerService.this.notifyCallbacksLocked(this.mWallpaper);
                    if (this.mInfo != null && this.mInfo.supportsAmbientMode() && displayId == 0) {
                        try {
                            connector.mEngine.setInAmbientMode(WallpaperManagerService.this.mInAmbientMode, 0);
                        } catch (RemoteException e2) {
                            Slog.w(WallpaperManagerService.TAG, "Failed to set ambient mode state", e2);
                        }
                    }
                    try {
                        connector.mEngine.requestWallpaperColors();
                    } catch (RemoteException e3) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to request wallpaper colors", e3);
                    }
                }
            }
        }

        public void engineShown(IWallpaperEngine engine) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (this.mReply != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        this.mReply.sendResult((Bundle) null);
                    } catch (RemoteException e) {
                        Binder.restoreCallingIdentity(ident);
                    }
                    this.mReply = null;
                }
            }
        }

        public ParcelFileDescriptor setWallpaper(String name) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (this.mWallpaper.connection != this) {
                    return null;
                }
                ParcelFileDescriptor updateWallpaperBitmapLocked = WallpaperManagerService.this.updateWallpaperBitmapLocked(name, this.mWallpaper, null);
                return updateWallpaperBitmapLocked;
            }
        }
    }

    class MyPackageMonitor extends PackageMonitor {
        MyPackageMonitor() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:17:0x008d, code lost:
            return;
         */
        public void onPackageUpdateFinished(String packageName, int uid) {
            ComponentName wpService;
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId == getChangingUserId()) {
                    WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                    if (!(wallpaper == null || (wpService = wallpaper.wallpaperComponent) == null || !wpService.getPackageName().equals(packageName))) {
                        Slog.i(WallpaperManagerService.TAG, "Wallpaper " + wpService + " update has finished");
                        wallpaper.wallpaperUpdating = false;
                        WallpaperManagerService.this.clearWallpaperComponentLocked(wallpaper);
                        if (!WallpaperManagerService.this.bindWallpaperComponentLocked(wpService, false, false, wallpaper, null)) {
                            Slog.w(WallpaperManagerService.TAG, "Wallpaper " + wpService + " no longer available; reverting to default");
                            WallpaperManagerService.this.clearWallpaperLocked(false, 1, wallpaper.userId, null);
                        }
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0040, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0042, code lost:
            return;
         */
        public void onPackageModified(String packageName) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId == getChangingUserId()) {
                    WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                    if (wallpaper != null) {
                        if (wallpaper.wallpaperComponent != null) {
                            if (wallpaper.wallpaperComponent.getPackageName().equals(packageName)) {
                                doPackagesChangedLocked(true, wallpaper);
                            }
                        }
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:17:0x006b, code lost:
            return;
         */
        public void onPackageUpdateStarted(String packageName, int uid) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId == getChangingUserId()) {
                    WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                    if (!(wallpaper == null || wallpaper.wallpaperComponent == null || !wallpaper.wallpaperComponent.getPackageName().equals(packageName))) {
                        Slog.i(WallpaperManagerService.TAG, "Wallpaper service " + wallpaper.wallpaperComponent + " is updating");
                        wallpaper.wallpaperUpdating = true;
                        if (wallpaper.connection != null) {
                            FgThread.getHandler().removeCallbacks(wallpaper.connection.mResetRunnable);
                        }
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0031, code lost:
            return r1;
         */
        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            synchronized (WallpaperManagerService.this.mLock) {
                boolean changed = false;
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return false;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (wallpaper != null) {
                    changed = false | doPackagesChangedLocked(doit, wallpaper);
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x002e, code lost:
            return;
         */
        public void onSomePackagesChanged() {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId == getChangingUserId()) {
                    WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                    if (wallpaper != null) {
                        doPackagesChangedLocked(true, wallpaper);
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean doPackagesChangedLocked(boolean doit, WallpaperData wallpaper) {
            int change;
            int change2;
            boolean changed = false;
            if (wallpaper.wallpaperComponent != null && ((change2 = isPackageDisappearing(wallpaper.wallpaperComponent.getPackageName())) == 3 || change2 == 2)) {
                changed = true;
                if (doit) {
                    Slog.w(WallpaperManagerService.TAG, "Wallpaper uninstalled, removing: " + wallpaper.wallpaperComponent);
                    WallpaperManagerService.this.clearWallpaperLocked(false, 1, wallpaper.userId, null);
                }
            }
            if (wallpaper.nextWallpaperComponent != null && ((change = isPackageDisappearing(wallpaper.nextWallpaperComponent.getPackageName())) == 3 || change == 2)) {
                wallpaper.nextWallpaperComponent = null;
            }
            if (wallpaper.wallpaperComponent != null && isPackageModified(wallpaper.wallpaperComponent.getPackageName())) {
                try {
                    WallpaperManagerService.this.mContext.getPackageManager().getServiceInfo(wallpaper.wallpaperComponent, 786432);
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.w(WallpaperManagerService.TAG, "Wallpaper component gone, removing: " + wallpaper.wallpaperComponent);
                    WallpaperManagerService.this.clearWallpaperLocked(false, 1, wallpaper.userId, null);
                }
            }
            if (wallpaper.nextWallpaperComponent != null && isPackageModified(wallpaper.nextWallpaperComponent.getPackageName())) {
                try {
                    WallpaperManagerService.this.mContext.getPackageManager().getServiceInfo(wallpaper.nextWallpaperComponent, 786432);
                } catch (PackageManager.NameNotFoundException e2) {
                    wallpaper.nextWallpaperComponent = null;
                }
            }
            return changed;
        }
    }

    @OppoHook(level = OppoHook.OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.Keyguard, 2012.08.27:add for oppo-wallpaper", property = OppoHook.OppoRomType.ROM)
    public WallpaperManagerService(Context context) {
        if (DEBUG) {
            Slog.v(TAG, "WallpaperService startup");
        }
        this.mContext = context;
        this.mShuttingDown = false;
        this.mImageWallpaper = ComponentName.unflattenFromString(context.getResources().getString(17040121));
        this.mDefaultWallpaperComponent = WallpaperManager.getDefaultWallpaperComponent(context);
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mIPackageManager = AppGlobals.getPackageManager();
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService(DisplayManager.class);
        this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
        this.mMonitor = new MyPackageMonitor();
        this.mWallpaperHelper = new WallpaperHelper(this.mContext);
        this.mColorsChangedListeners = new SparseArray<>();
        LocalServices.addService(WallpaperManagerInternal.class, new LocalService());
    }

    private final class LocalService extends WallpaperManagerInternal {
        private LocalService() {
        }

        @Override // com.android.server.wallpaper.WallpaperManagerInternal
        public void onDisplayReady(int displayId) {
            WallpaperManagerService.this.onDisplayReadyInternal(displayId);
        }
    }

    /* access modifiers changed from: package-private */
    public void initialize() {
        this.mMonitor.register(this.mContext, null, UserHandle.ALL, true);
        getWallpaperDir(0).mkdirs();
        loadSettingsLocked(0, false);
        getWallpaperSafeLocked(0, 1);
    }

    /* access modifiers changed from: private */
    public static File getWallpaperDir(int userId) {
        return Environment.getUserSystemDirectory(userId);
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        super.finalize();
        for (int i = 0; i < this.mWallpaperMap.size(); i++) {
            this.mWallpaperMap.valueAt(i).wallpaperObserver.stopWatching();
        }
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        if (DEBUG) {
            Slog.v(TAG, "systemReady");
        }
        initialize();
        WallpaperData wallpaper = this.mWallpaperMap.get(0);
        if (this.mImageWallpaper.equals(wallpaper.nextWallpaperComponent)) {
            if (!wallpaper.cropExists()) {
                if (DEBUG) {
                    Slog.i(TAG, "No crop; regenerating from source");
                }
                generateCrop(wallpaper);
            }
            if (!wallpaper.cropExists()) {
                if (DEBUG) {
                    Slog.i(TAG, "Unable to regenerate crop; resetting");
                }
                clearWallpaperLocked(false, 1, 0, null);
            }
        } else if (DEBUG) {
            Slog.i(TAG, "Nondefault wallpaper component; gracefully ignoring");
        }
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wallpaper.WallpaperManagerService.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    WallpaperManagerService.this.onRemoveUser(intent.getIntExtra("android.intent.extra.user_handle", -10000));
                }
            }
        }, userFilter);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wallpaper.WallpaperManagerService.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
                    if (WallpaperManagerService.DEBUG) {
                        Slog.i(WallpaperManagerService.TAG, "Shutting down");
                    }
                    synchronized (WallpaperManagerService.this.mLock) {
                        boolean unused = WallpaperManagerService.this.mShuttingDown = true;
                    }
                }
            }
        }, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"));
        try {
            ActivityManager.getService().registerUserSwitchObserver(new UserSwitchObserver() {
                /* class com.android.server.wallpaper.WallpaperManagerService.AnonymousClass4 */

                public void onUserSwitching(int newUserId, IRemoteCallback reply) {
                    WallpaperManagerService.this.switchUser(newUserId, reply);
                }
            }, TAG);
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
    }

    public String getName() {
        String str;
        if (Binder.getCallingUid() == 1000) {
            synchronized (this.mLock) {
                str = this.mWallpaperMap.get(0).name;
            }
            return str;
        }
        throw new RuntimeException("getName() can only be called from the system process");
    }

    /* access modifiers changed from: package-private */
    public void stopObserver(WallpaperData wallpaper) {
        if (wallpaper != null && wallpaper.wallpaperObserver != null) {
            wallpaper.wallpaperObserver.stopWatching();
            wallpaper.wallpaperObserver = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void stopObserversLocked(int userId) {
        stopObserver(this.mWallpaperMap.get(userId));
        stopObserver(this.mLockWallpaperMap.get(userId));
        this.mWallpaperMap.remove(userId);
        this.mLockWallpaperMap.remove(userId);
    }

    @Override // com.android.server.wallpaper.IWallpaperManagerService
    public void onBootPhase(int phase) {
        if (phase == 550) {
            systemReady();
        } else if (phase == 600) {
            switchUser(0, null);
        }
    }

    @Override // com.android.server.wallpaper.IWallpaperManagerService
    public void onUnlockUser(final int userId) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId == userId) {
                if (this.mWaitingForUnlock) {
                    WallpaperData systemWallpaper = getWallpaperSafeLocked(userId, 1);
                    switchWallpaper(systemWallpaper, null);
                    notifyCallbacksLocked(systemWallpaper);
                }
                if (!this.mUserRestorecon.get(userId)) {
                    this.mUserRestorecon.put(userId, true);
                    BackgroundThread.getHandler().post(new Runnable() {
                        /* class com.android.server.wallpaper.WallpaperManagerService.AnonymousClass5 */

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

    /* access modifiers changed from: package-private */
    public void onRemoveUser(int userId) {
        if (userId >= 1) {
            File wallpaperDir = getWallpaperDir(userId);
            synchronized (this.mLock) {
                stopObserversLocked(userId);
                for (String filename : sPerUserFiles) {
                    new File(wallpaperDir, filename).delete();
                }
                this.mUserRestorecon.delete(userId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void switchUser(int userId, IRemoteCallback reply) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId != userId) {
                this.mCurrentUserId = userId;
                WallpaperData systemWallpaper = getWallpaperSafeLocked(userId, 1);
                WallpaperData tmpLockWallpaper = this.mLockWallpaperMap.get(userId);
                WallpaperData lockWallpaper = tmpLockWallpaper == null ? systemWallpaper : tmpLockWallpaper;
                if (systemWallpaper.wallpaperObserver == null) {
                    systemWallpaper.wallpaperObserver = new WallpaperObserver(systemWallpaper);
                    systemWallpaper.wallpaperObserver.startWatching();
                }
                switchWallpaper(systemWallpaper, reply);
                FgThread.getHandler().post(new Runnable(systemWallpaper, lockWallpaper) {
                    /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$xeJGAwCI8tssclwKFf8jMsYdoKQ */
                    private final /* synthetic */ WallpaperManagerService.WallpaperData f$1;
                    private final /* synthetic */ WallpaperManagerService.WallpaperData f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        WallpaperManagerService.this.lambda$switchUser$4$WallpaperManagerService(this.f$1, this.f$2);
                    }
                });
            }
        }
    }

    public /* synthetic */ void lambda$switchUser$4$WallpaperManagerService(WallpaperData systemWallpaper, WallpaperData lockWallpaper) {
        notifyWallpaperColorsChanged(systemWallpaper, 1);
        notifyWallpaperColorsChanged(lockWallpaper, 2);
        notifyWallpaperColorsChanged(this.mFallbackWallpaper, 1);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006f, code lost:
        return;
     */
    public void switchWallpaper(WallpaperData wallpaper, IRemoteCallback reply) {
        ServiceInfo si;
        synchronized (this.mLock) {
            try {
                this.mWaitingForUnlock = false;
                ComponentName cname = wallpaper.wallpaperComponent != null ? wallpaper.wallpaperComponent : wallpaper.nextWallpaperComponent;
                if (!bindWallpaperComponentLocked(cname, true, false, wallpaper, reply)) {
                    try {
                        si = this.mIPackageManager.getServiceInfo(cname, (int) DumpState.DUMP_DOMAIN_PREFERRED, wallpaper.userId);
                    } catch (RemoteException e) {
                        si = null;
                    }
                    if (si == null) {
                        Slog.w(TAG, "Failure starting previous wallpaper; clearing");
                        clearWallpaperLocked(false, 1, wallpaper.userId, reply);
                    } else {
                        Slog.w(TAG, "Wallpaper isn't direct boot aware; using fallback until unlocked");
                        wallpaper.wallpaperComponent = wallpaper.nextWallpaperComponent;
                        WallpaperData fallback = new WallpaperData(wallpaper.userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
                        ensureSaneWallpaperData(fallback, 0);
                        bindWallpaperComponentLocked(this.mImageWallpaper, true, false, fallback, reply);
                        this.mWaitingForUnlock = true;
                    }
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    public void clearWallpaper(String callingPackage, int which, int userId) {
        if (DEBUG) {
            Slog.v(TAG, "clearWallpaper");
        }
        checkPermission("android.permission.SET_WALLPAPER");
        if (isWallpaperSupported(callingPackage) && isSetWallpaperAllowed(callingPackage)) {
            int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "clearWallpaper", null);
            WallpaperData data = null;
            synchronized (this.mLock) {
                clearWallpaperLocked(false, which, userId2, null);
                if (which == 2) {
                    data = this.mLockWallpaperMap.get(userId2);
                }
                if (which == 1 || data == null) {
                    data = this.mWallpaperMap.get(userId2);
                }
            }
            if (data != null) {
                notifyWallpaperColorsChanged(data, which);
                notifyWallpaperColorsChanged(this.mFallbackWallpaper, 1);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00bf A[SYNTHETIC, Splitter:B:56:0x00bf] */
    public void clearWallpaperLocked(boolean defaultFailed, int which, int userId, IRemoteCallback reply) {
        WallpaperData wallpaper;
        Bundle bundle;
        ComponentName componentName;
        if (which == 1 || which == 2) {
            if (which == 2) {
                WallpaperData wallpaper2 = this.mLockWallpaperMap.get(userId);
                if (wallpaper2 != null) {
                    wallpaper = wallpaper2;
                } else if (DEBUG) {
                    Slog.i(TAG, "Lock wallpaper already cleared");
                    return;
                } else {
                    return;
                }
            } else {
                WallpaperData wallpaper3 = this.mWallpaperMap.get(userId);
                if (wallpaper3 == null) {
                    loadSettingsLocked(userId, false);
                    wallpaper = this.mWallpaperMap.get(userId);
                } else {
                    wallpaper = wallpaper3;
                }
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
                                if (DEBUG) {
                                    Slog.i(TAG, "Notifying keyguard of lock wallpaper clear");
                                }
                                try {
                                    cb.onWallpaperChanged();
                                } catch (RemoteException e) {
                                }
                            }
                            saveSettingsLocked(userId);
                            return;
                        }
                    }
                    RuntimeException e2 = null;
                    try {
                        wallpaper.primaryColors = null;
                        wallpaper.imageWallpaperPending = false;
                        if (userId != this.mCurrentUserId) {
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        if (defaultFailed) {
                            componentName = this.mImageWallpaper;
                        } else {
                            componentName = null;
                        }
                        bundle = null;
                        try {
                            if (bindWallpaperComponentLocked(componentName, true, false, wallpaper, reply)) {
                                Binder.restoreCallingIdentity(ident);
                                return;
                            }
                        } catch (IllegalArgumentException e3) {
                            e1 = e3;
                            e2 = e1;
                            Slog.e(TAG, "Default wallpaper component not found!", e2);
                            clearWallpaperComponentLocked(wallpaper);
                            if (reply != null) {
                            }
                            Binder.restoreCallingIdentity(ident);
                        }
                        Slog.e(TAG, "Default wallpaper component not found!", e2);
                        clearWallpaperComponentLocked(wallpaper);
                        if (reply != null) {
                            try {
                                reply.sendResult(bundle);
                            } catch (RemoteException e4) {
                            }
                        }
                        Binder.restoreCallingIdentity(ident);
                    } catch (IllegalArgumentException e5) {
                        e1 = e5;
                        bundle = null;
                        e2 = e1;
                        Slog.e(TAG, "Default wallpaper component not found!", e2);
                        clearWallpaperComponentLocked(wallpaper);
                        if (reply != null) {
                        }
                        Binder.restoreCallingIdentity(ident);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        } else {
            throw new IllegalArgumentException("Must specify exactly one kind of wallpaper to clear");
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean hasNamedWallpaper(String name) {
        synchronized (this.mLock) {
            long ident = Binder.clearCallingIdentity();
            try {
                List<UserInfo> users = ((UserManager) this.mContext.getSystemService("user")).getUsers();
                Binder.restoreCallingIdentity(ident);
                for (UserInfo user : users) {
                    if (!user.isManagedProfile()) {
                        WallpaperData wd = this.mWallpaperMap.get(user.id);
                        if (wd == null) {
                            loadSettingsLocked(user.id, false);
                            wd = this.mWallpaperMap.get(user.id);
                        }
                        if (wd != null && name.equals(wd.name)) {
                            return true;
                        }
                    }
                }
                return false;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }
    }

    private boolean isValidDisplay(int displayId) {
        return this.mDisplayManager.getDisplay(displayId) != null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0073, code lost:
        return;
     */
    public void setDimensionHints(int width, int height, String callingPackage, int displayId) throws RemoteException {
        checkPermission("android.permission.SET_WALLPAPER_HINTS");
        if (isWallpaperSupported(callingPackage)) {
            int width2 = Math.min(width, GLHelper.getMaxTextureSize());
            int height2 = Math.min(height, GLHelper.getMaxTextureSize());
            synchronized (this.mLock) {
                int userId = UserHandle.getCallingUserId();
                WallpaperData wallpaper = getWallpaperSafeLocked(userId, 1);
                if (width2 <= 0 || height2 <= 0) {
                    throw new IllegalArgumentException("width and height must be > 0");
                } else if (isValidDisplay(displayId)) {
                    DisplayData wpdData = getDisplayDataOrCreate(displayId);
                    if (!(width2 == wpdData.mWidth && height2 == wpdData.mHeight)) {
                        wpdData.mWidth = width2;
                        wpdData.mHeight = height2;
                        if (displayId == 0) {
                            saveSettingsLocked(userId);
                        }
                        if (this.mCurrentUserId == userId) {
                            if (wallpaper.connection != null) {
                                WallpaperConnection.DisplayConnector connector = wallpaper.connection.getDisplayConnectorOrCreate(displayId);
                                IWallpaperEngine engine = connector != null ? connector.mEngine : null;
                                if (engine != null) {
                                    try {
                                        engine.setDesiredSize(width2, height2);
                                    } catch (RemoteException e) {
                                    }
                                    notifyCallbacksLocked(wallpaper);
                                } else if (!(wallpaper.connection.mService == null || connector == null)) {
                                    connector.mDimensionsChanged = true;
                                }
                            }
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Cannot find display with id=" + displayId);
                }
            }
        }
    }

    public int getWidthHint(int displayId) throws RemoteException {
        synchronized (this.mLock) {
            if (!isValidDisplay(displayId)) {
                throw new IllegalArgumentException("Cannot find display with id=" + displayId);
            } else if (this.mWallpaperMap.get(UserHandle.getCallingUserId()) == null) {
                return 0;
            } else {
                int i = getDisplayDataOrCreate(displayId).mWidth;
                return i;
            }
        }
    }

    public int getHeightHint(int displayId) throws RemoteException {
        synchronized (this.mLock) {
            if (!isValidDisplay(displayId)) {
                throw new IllegalArgumentException("Cannot find display with id=" + displayId);
            } else if (this.mWallpaperMap.get(UserHandle.getCallingUserId()) == null) {
                return 0;
            } else {
                int i = getDisplayDataOrCreate(displayId).mHeight;
                return i;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0070, code lost:
        return;
     */
    public void setDisplayPadding(Rect padding, String callingPackage, int displayId) {
        checkPermission("android.permission.SET_WALLPAPER_HINTS");
        if (isWallpaperSupported(callingPackage)) {
            synchronized (this.mLock) {
                if (isValidDisplay(displayId)) {
                    int userId = UserHandle.getCallingUserId();
                    WallpaperData wallpaper = getWallpaperSafeLocked(userId, 1);
                    if (padding.left < 0 || padding.top < 0 || padding.right < 0 || padding.bottom < 0) {
                        throw new IllegalArgumentException("padding must be positive: " + padding);
                    }
                    DisplayData wpdData = getDisplayDataOrCreate(displayId);
                    if (!padding.equals(wpdData.mPadding)) {
                        wpdData.mPadding.set(padding);
                        if (displayId == 0) {
                            saveSettingsLocked(userId);
                        }
                        if (this.mCurrentUserId == userId) {
                            if (wallpaper.connection != null) {
                                WallpaperConnection.DisplayConnector connector = wallpaper.connection.getDisplayConnectorOrCreate(displayId);
                                IWallpaperEngine engine = connector != null ? connector.mEngine : null;
                                if (engine != null) {
                                    try {
                                        engine.setDisplayPadding(padding);
                                    } catch (RemoteException e) {
                                    }
                                    notifyCallbacksLocked(wallpaper);
                                } else if (!(wallpaper.connection.mService == null || connector == null)) {
                                    connector.mPaddingChanged = true;
                                }
                            }
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Cannot find display with id=" + displayId);
                }
            }
        }
    }

    public ParcelFileDescriptor getWallpaper(String callingPkg, IWallpaperManagerCallback cb, int which, Bundle outParams, int wallpaperUserId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_WALLPAPER_INTERNAL") != 0) {
            ((StorageManager) this.mContext.getSystemService(StorageManager.class)).checkPermissionReadImages(true, Binder.getCallingPid(), Binder.getCallingUid(), callingPkg);
        }
        int wallpaperUserId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), wallpaperUserId, false, true, "getWallpaper", null);
        if (which == 1 || which == 2) {
            synchronized (this.mLock) {
                WallpaperData wallpaper = (which == 2 ? this.mLockWallpaperMap : this.mWallpaperMap).get(wallpaperUserId2);
                if (wallpaper == null) {
                    return null;
                }
                DisplayData wpdData = getDisplayDataOrCreate(0);
                if (outParams != null) {
                    try {
                        outParams.putInt("width", wpdData.mWidth);
                        outParams.putInt("height", wpdData.mHeight);
                    } catch (FileNotFoundException e) {
                        Slog.w(TAG, "Error getting wallpaper", e);
                        return null;
                    }
                }
                if (cb != null) {
                    wallpaper.callbacks.register(cb);
                }
                if (!wallpaper.cropFile.exists()) {
                    return null;
                }
                ParcelFileDescriptor open = ParcelFileDescriptor.open(wallpaper.cropFile, 268435456);
                return open;
            }
        }
        throw new IllegalArgumentException("Must specify exactly one kind of wallpaper to read");
    }

    public WallpaperInfo getWallpaperInfo(int userId) {
        int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "getWallpaperInfo", null);
        synchronized (this.mLock) {
            WallpaperData wallpaper = this.mWallpaperMap.get(userId2);
            if (wallpaper == null || wallpaper.connection == null) {
                return null;
            }
            WallpaperInfo wallpaperInfo = wallpaper.connection.mInfo;
            return wallpaperInfo;
        }
    }

    public int getWallpaperIdForUser(int which, int userId) {
        int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "getWallpaperIdForUser", null);
        if (which == 1 || which == 2) {
            SparseArray<WallpaperData> map = which == 2 ? this.mLockWallpaperMap : this.mWallpaperMap;
            synchronized (this.mLock) {
                WallpaperData wallpaper = map.get(userId2);
                if (wallpaper == null) {
                    return -1;
                }
                int i = wallpaper.wallpaperId;
                return i;
            }
        }
        throw new IllegalArgumentException("Must specify exactly one kind of wallpaper");
    }

    public void registerWallpaperColorsCallback(IWallpaperManagerCallback cb, int userId, int displayId) {
        int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, "registerWallpaperColorsCallback", null);
        synchronized (this.mLock) {
            SparseArray<RemoteCallbackList<IWallpaperManagerCallback>> userDisplayColorsChangedListeners = this.mColorsChangedListeners.get(userId2);
            if (userDisplayColorsChangedListeners == null) {
                userDisplayColorsChangedListeners = new SparseArray<>();
                this.mColorsChangedListeners.put(userId2, userDisplayColorsChangedListeners);
            }
            RemoteCallbackList<IWallpaperManagerCallback> displayChangedListeners = userDisplayColorsChangedListeners.get(displayId);
            if (displayChangedListeners == null) {
                displayChangedListeners = new RemoteCallbackList<>();
                userDisplayColorsChangedListeners.put(displayId, displayChangedListeners);
            }
            displayChangedListeners.register(cb);
        }
    }

    public void unregisterWallpaperColorsCallback(IWallpaperManagerCallback cb, int userId, int displayId) {
        RemoteCallbackList<IWallpaperManagerCallback> displayChangedListeners;
        int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, "unregisterWallpaperColorsCallback", null);
        synchronized (this.mLock) {
            SparseArray<RemoteCallbackList<IWallpaperManagerCallback>> userDisplayColorsChangedListeners = this.mColorsChangedListeners.get(userId2);
            if (!(userDisplayColorsChangedListeners == null || (displayChangedListeners = userDisplayColorsChangedListeners.get(displayId)) == null)) {
                displayChangedListeners.unregister(cb);
            }
        }
    }

    public void setInAmbientMode(boolean inAmbientMode, long animationDuration) {
        IWallpaperEngine engine;
        synchronized (this.mLock) {
            this.mInAmbientMode = inAmbientMode;
            WallpaperData data = this.mWallpaperMap.get(this.mCurrentUserId);
            if (data == null || data.connection == null || (data.connection.mInfo != null && !data.connection.mInfo.supportsAmbientMode())) {
                engine = null;
            } else {
                engine = data.connection.getDisplayConnectorOrCreate(0).mEngine;
            }
        }
        if (engine != null) {
            try {
                engine.setInAmbientMode(inAmbientMode, animationDuration);
            } catch (RemoteException e) {
            }
        }
    }

    public boolean setLockWallpaperCallback(IWallpaperManagerCallback cb) {
        checkPermission("android.permission.INTERNAL_SYSTEM_WINDOW");
        synchronized (this.mLock) {
            this.mKeyguardListener = cb;
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0046, code lost:
        if (r0 == false) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0048, code lost:
        extractColors(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004b, code lost:
        r1 = r9.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004d, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r3 = r2.primaryColors;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0050, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0051, code lost:
        return r3;
     */
    public WallpaperColors getWallpaperColors(int which, int userId, int displayId) throws RemoteException {
        boolean shouldExtract = true;
        if (which == 2 || which == 1) {
            int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "getWallpaperColors", null);
            WallpaperData wallpaperData = null;
            synchronized (this.mLock) {
                if (which == 2) {
                    try {
                        wallpaperData = this.mLockWallpaperMap.get(userId2);
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                if (wallpaperData == null) {
                    wallpaperData = findWallpaperAtDisplay(userId2, displayId);
                }
                if (wallpaperData == null) {
                    return null;
                }
                if (wallpaperData.primaryColors != null) {
                    shouldExtract = false;
                }
            }
        } else {
            throw new IllegalArgumentException("which should be either FLAG_LOCK or FLAG_SYSTEM");
        }
    }

    private WallpaperData findWallpaperAtDisplay(int userId, int displayId) {
        WallpaperData wallpaperData = this.mFallbackWallpaper;
        if (wallpaperData == null || wallpaperData.connection == null || !this.mFallbackWallpaper.connection.containsDisplay(displayId)) {
            return this.mWallpaperMap.get(userId);
        }
        return this.mFallbackWallpaper;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.am.IColorMultiAppManager.getCorrectUserId(int, java.lang.String, boolean):int
     arg types: [int, java.lang.String, int]
     candidates:
      com.android.server.am.IColorMultiAppManager.getCorrectUserId(int, int, java.lang.String):int
      com.android.server.am.IColorMultiAppManager.getCorrectUserId(int, java.lang.String, boolean):int */
    @OppoHook(level = OppoHook.OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.LauncherCenter,2016.09.06:Modify to forbid third part launchers to set wallpaper.", property = OppoHook.OppoRomType.ROM)
    public ParcelFileDescriptor setWallpaper(String name, String callingPackage, Rect cropHint, boolean allowBackup, Bundle extras, int which, IWallpaperManagerCallback completion, int userId) {
        Rect cropHint2;
        WallpaperData systemWallpaper;
        int which2 = which;
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(56, Binder.getCallingUid());
        int userId2 = ActivityManager.handleIncomingUser(getCallingPid(), getCallingUid(), userId, false, true, "changing wallpaper", null);
        checkPermission("android.permission.SET_WALLPAPER");
        Slog.i(TAG, "setWallpaper name:" + name + ", callingPackage:" + callingPackage + ", which:" + which2);
        boolean needFastSet = false;
        if ((65536 & which2) != 0) {
            if (this.mContext.checkCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE") == 0) {
                which2 &= -65537;
                needFastSet = true;
                if (DEBUG) {
                    Slog.d(TAG, "setWallpaper. has fast set flag. which = " + which2 + ", callingPackage = " + callingPackage);
                }
            } else {
                Slog.i(TAG, "setWallpaper. Invalid package has no permission! which = " + which2 + ", callingPackage = " + callingPackage);
            }
        }
        if ((which2 & 3) != 0) {
            if (isWallpaperSupported(callingPackage)) {
                if (isSetWallpaperAllowed(callingPackage)) {
                    int userId3 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getCorrectUserId(userId2, callingPackage, true);
                    WallpaperHelper wallpaperHelper = this.mWallpaperHelper;
                    if (wallpaperHelper == null || !wallpaperHelper.isForbidSettingWallpaperVersion()) {
                        if (cropHint == null) {
                            cropHint2 = new Rect(0, 0, 0, 0);
                        } else if (cropHint.isEmpty() || cropHint.left < 0 || cropHint.top < 0) {
                            throw new IllegalArgumentException("Invalid crop rect supplied: " + cropHint);
                        } else {
                            cropHint2 = cropHint;
                        }
                        synchronized (this.mLock) {
                            try {
                                if (DEBUG) {
                                    Slog.v(TAG, "setWallpaper which=0x" + Integer.toHexString(which2));
                                }
                                if (which2 == 1 && this.mLockWallpaperMap.get(userId3) == null) {
                                    if (DEBUG) {
                                        Slog.i(TAG, "Migrating system->lock to preserve");
                                    }
                                    migrateSystemToLockWallpaperLocked(userId3);
                                }
                                WallpaperData wallpaper = getWallpaperSafeLocked(userId3, which2);
                                if ((which2 & 2) != 0 && "com.android.keyguard".equals(callingPackage) && (systemWallpaper = getWallpaperSafeLocked(userId3, 1)) != null && systemWallpaper.wallpaperObserver == null) {
                                    systemWallpaper.wallpaperObserver = new WallpaperObserver(systemWallpaper);
                                    systemWallpaper.wallpaperObserver.startWatching();
                                    Slog.i(TAG, "setWallpaper: from keyguard, WallpaperObserver startWatching!!!");
                                }
                                long ident = Binder.clearCallingIdentity();
                                try {
                                    ParcelFileDescriptor pfd = updateWallpaperBitmapLocked(name, wallpaper, extras);
                                    if (pfd != null) {
                                        wallpaper.imageWallpaperPending = true;
                                        wallpaper.whichPending = which2;
                                        try {
                                            wallpaper.setComplete = completion;
                                            wallpaper.cropHint.set(cropHint2);
                                            try {
                                                wallpaper.allowBackup = allowBackup;
                                                wallpaper.needFastSet = needFastSet;
                                            } catch (Throwable th) {
                                                th = th;
                                            }
                                        } catch (Throwable th2) {
                                            th = th2;
                                            Binder.restoreCallingIdentity(ident);
                                            throw th;
                                        }
                                    }
                                    Binder.restoreCallingIdentity(ident);
                                    return pfd;
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                throw th;
                            }
                        }
                    } else {
                        Slog.e(TAG, "setWallpaper: forbid setting wallpaper version, just return!!!");
                        return null;
                    }
                }
            }
            return null;
        }
        Slog.e(TAG, "Must specify a valid wallpaper category to set");
        throw new IllegalArgumentException("Must specify a valid wallpaper category to set");
    }

    private void migrateSystemToLockWallpaperLocked(int userId) {
        WallpaperData sysWP = this.mWallpaperMap.get(userId);
        if (sysWP != null) {
            WallpaperData lockWP = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
            lockWP.wallpaperId = sysWP.wallpaperId;
            lockWP.cropHint.set(sysWP.cropHint);
            lockWP.allowBackup = sysWP.allowBackup;
            lockWP.primaryColors = sysWP.primaryColors;
            try {
                Os.rename(sysWP.wallpaperFile.getAbsolutePath(), lockWP.wallpaperFile.getAbsolutePath());
                Os.rename(sysWP.cropFile.getAbsolutePath(), lockWP.cropFile.getAbsolutePath());
                this.mLockWallpaperMap.put(userId, lockWP);
            } catch (ErrnoException e) {
                Slog.e(TAG, "Can't migrate system wallpaper: " + e.getMessage());
                lockWP.wallpaperFile.delete();
                lockWP.cropFile.delete();
            }
        } else if (DEBUG) {
            Slog.i(TAG, "No system wallpaper?  Not tracking for lock-only");
        }
    }

    /* access modifiers changed from: package-private */
    public ParcelFileDescriptor updateWallpaperBitmapLocked(String name, WallpaperData wallpaper, Bundle extras) {
        if (name == null) {
            name = "";
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
            wallpaper.primaryColors = null;
            if (DEBUG) {
                Slog.v(TAG, "updateWallpaperBitmapLocked() : id=" + wallpaper.wallpaperId + " name=" + name + " file=" + wallpaper.wallpaperFile.getName());
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
        WallpaperData wallpaper;
        int userId2 = ActivityManager.handleIncomingUser(getCallingPid(), getCallingUid(), userId, false, true, "changing live wallpaper", null);
        checkPermission("android.permission.SET_WALLPAPER_COMPONENT");
        WallpaperHelper wallpaperHelper = this.mWallpaperHelper;
        if (wallpaperHelper == null || !wallpaperHelper.isForbidSettingWallpaperVersion()) {
            int which = 1;
            boolean shouldNotifyColors = false;
            synchronized (this.mLock) {
                if (DEBUG) {
                    Slog.v(TAG, "setWallpaperComponent name=" + name);
                }
                wallpaper = this.mWallpaperMap.get(userId2);
                if (wallpaper != null) {
                    long ident = Binder.clearCallingIdentity();
                    if (this.mImageWallpaper.equals(wallpaper.wallpaperComponent) && this.mLockWallpaperMap.get(userId2) == null) {
                        migrateSystemToLockWallpaperLocked(userId2);
                    }
                    if (this.mLockWallpaperMap.get(userId2) == null) {
                        which = 1 | 2;
                    }
                    try {
                        wallpaper.imageWallpaperPending = false;
                        boolean same = changingToSame(name, wallpaper);
                        if (bindWallpaperComponentLocked(name, false, true, wallpaper, null)) {
                            if (!same) {
                                wallpaper.primaryColors = null;
                            }
                            wallpaper.wallpaperId = makeWallpaperIdLocked();
                            notifyCallbacksLocked(wallpaper);
                            shouldNotifyColors = true;
                        }
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    throw new IllegalStateException("Wallpaper not yet initialized for user " + userId2);
                }
            }
            if (shouldNotifyColors) {
                notifyWallpaperColorsChanged(wallpaper, which);
                notifyWallpaperColorsChanged(this.mFallbackWallpaper, 1);
                return;
            }
            return;
        }
        Slog.d(TAG, "setWallpaperComponent: forbid setting wallpaper version, just return!!!");
    }

    private boolean changingToSame(ComponentName componentName, WallpaperData wallpaper) {
        if (wallpaper.connection == null) {
            return false;
        }
        if (wallpaper.wallpaperComponent == null) {
            if (componentName != null) {
                return false;
            }
            if (DEBUG) {
                Slog.v(TAG, "changingToSame: still using default");
            }
            return true;
        } else if (!wallpaper.wallpaperComponent.equals(componentName)) {
            return false;
        } else {
            if (DEBUG) {
                Slog.v(TAG, "same wallpaper");
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x0236  */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x023b  */
    public boolean bindWallpaperComponentLocked(ComponentName componentName, boolean force, boolean fromUser, WallpaperData wallpaper, IRemoteCallback reply) {
        ComponentName componentName2 = componentName;
        Slog.v(TAG, "bindWallpaperComponentLocked: componentName=" + componentName2);
        if (!force && changingToSame(componentName2, wallpaper)) {
            return true;
        }
        if (componentName2 == null) {
            try {
                componentName2 = this.mDefaultWallpaperComponent;
                if (componentName2 == null) {
                    try {
                        componentName2 = this.mImageWallpaper;
                        Slog.v(TAG, "No default component; using image wallpaper");
                    } catch (RemoteException e) {
                        e = e;
                        String msg = "Remote exception for " + componentName2 + StringUtils.LF + e;
                        if (!fromUser) {
                        }
                    }
                }
            } catch (RemoteException e2) {
                e = e2;
                String msg2 = "Remote exception for " + componentName2 + StringUtils.LF + e;
                if (!fromUser) {
                    Slog.w(TAG, msg2);
                    return false;
                }
                throw new IllegalArgumentException(msg2);
            }
        }
        int serviceUserId = wallpaper.userId;
        ServiceInfo si = this.mIPackageManager.getServiceInfo(componentName2, 4224, serviceUserId);
        if (si == null) {
            Slog.w(TAG, "Attempted wallpaper " + componentName2 + " is unavailable");
            return false;
        } else if (!"android.permission.BIND_WALLPAPER".equals(si.permission)) {
            String msg3 = "Selected service does not have android.permission.BIND_WALLPAPER: " + componentName2;
            if (!fromUser) {
                Slog.w(TAG, msg3);
                return false;
            }
            throw new SecurityException(msg3);
        } else {
            WallpaperInfo wi = null;
            Intent intent = new Intent("android.service.wallpaper.WallpaperService");
            if (componentName2 != null && !componentName2.equals(this.mImageWallpaper)) {
                List<ResolveInfo> ris = this.mIPackageManager.queryIntentServices(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 128, serviceUserId).getList();
                int i = 0;
                while (true) {
                    if (i >= ris.size()) {
                        break;
                    }
                    ServiceInfo rsi = ris.get(i).serviceInfo;
                    if (!rsi.name.equals(si.name) || !rsi.packageName.equals(si.packageName)) {
                        i++;
                    } else {
                        try {
                            wi = new WallpaperInfo(this.mContext, ris.get(i));
                            break;
                        } catch (XmlPullParserException e3) {
                            if (!fromUser) {
                                Slog.w(TAG, e3);
                                return false;
                            }
                            throw new IllegalArgumentException(e3);
                        } catch (IOException e4) {
                            if (!fromUser) {
                                Slog.w(TAG, e4);
                                return false;
                            }
                            throw new IllegalArgumentException(e4);
                        }
                    }
                }
                if (wi == null) {
                    String msg4 = "Selected service is not a wallpaper: " + componentName2;
                    if (!fromUser) {
                        Slog.w(TAG, msg4);
                        return false;
                    }
                    throw new SecurityException(msg4);
                }
            }
            if (wi == null || !wi.supportsAmbientMode() || this.mIPackageManager.checkPermission("android.permission.AMBIENT_WALLPAPER", wi.getPackageName(), serviceUserId) == 0) {
                if (DEBUG) {
                    Slog.v(TAG, "Binding to:" + componentName2);
                }
                WallpaperConnection newConn = new WallpaperConnection(wi, wallpaper, this.mIPackageManager.getPackageUid(componentName2.getPackageName(), 268435456, wallpaper.userId));
                intent.setComponent(componentName2);
                intent.putExtra("android.intent.extra.client_label", 17041219);
                intent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivityAsUser(this.mContext, 0, Intent.createChooser(new Intent("android.intent.action.SET_WALLPAPER"), this.mContext.getText(17039657)), 0, null, new UserHandle(serviceUserId)));
                if (!this.mContext.bindServiceAsUser(intent, newConn, 570429441, new UserHandle(serviceUserId))) {
                    String msg5 = "Unable to bind service: " + componentName2;
                    if (!fromUser) {
                        Slog.w(TAG, msg5);
                        return false;
                    }
                    throw new IllegalArgumentException(msg5);
                }
                if (wallpaper.userId == this.mCurrentUserId && this.mLastWallpaper != null && !wallpaper.equals(this.mFallbackWallpaper)) {
                    detachWallpaperLocked(this.mLastWallpaper);
                }
                wallpaper.wallpaperComponent = componentName2;
                wallpaper.connection = newConn;
                try {
                    newConn.mReply = reply;
                    if (wallpaper.userId == this.mCurrentUserId && !wallpaper.equals(this.mFallbackWallpaper)) {
                        this.mLastWallpaper = wallpaper;
                    }
                    updateFallbackConnection();
                    return true;
                } catch (RemoteException e5) {
                    e = e5;
                    String msg22 = "Remote exception for " + componentName2 + StringUtils.LF + e;
                    if (!fromUser) {
                    }
                }
            } else {
                String msg6 = "Selected service does not have android.permission.AMBIENT_WALLPAPER: " + componentName2;
                if (!fromUser) {
                    Slog.w(TAG, msg6);
                    return false;
                }
                throw new SecurityException(msg6);
            }
        }
    }

    private void detachWallpaperLocked(WallpaperData wallpaper) {
        if (wallpaper.connection != null) {
            if (wallpaper.connection.mReply != null) {
                try {
                    wallpaper.connection.mReply.sendResult((Bundle) null);
                } catch (RemoteException e) {
                }
                wallpaper.connection.mReply = null;
            }
            try {
                if (wallpaper.connection.mService != null) {
                    wallpaper.connection.mService.detach();
                }
            } catch (RemoteException e2) {
                Slog.w(TAG, "Failed detaching wallpaper service ", e2);
            }
            this.mContext.unbindService(wallpaper.connection);
            wallpaper.connection.forEachDisplayConnector($$Lambda$havGP5uMdRgWQrLydPeIOu1qDGE.INSTANCE);
            wallpaper.connection.mService = null;
            wallpaper.connection.mDisplayConnector.clear();
            wallpaper.connection = null;
            if (wallpaper == this.mLastWallpaper) {
                this.mLastWallpaper = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public void clearWallpaperComponentLocked(WallpaperData wallpaper) {
        wallpaper.wallpaperComponent = null;
        detachWallpaperLocked(wallpaper);
    }

    /* access modifiers changed from: private */
    public void attachServiceLocked(WallpaperConnection conn, WallpaperData wallpaper) {
        conn.forEachDisplayConnector(new Consumer(wallpaper) {
            /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$UhAlBGB5jhuZrLndUPRmIvoHRZc */
            private final /* synthetic */ WallpaperManagerService.WallpaperData f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((WallpaperManagerService.WallpaperConnection.DisplayConnector) obj).connectLocked(WallpaperManagerService.WallpaperConnection.this, this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    public void notifyCallbacksLocked(WallpaperData wallpaper) {
        int n = wallpaper.callbacks.beginBroadcast();
        for (int i = 0; i < n; i++) {
            try {
                wallpaper.callbacks.getBroadcastItem(i).onWallpaperChanged();
            } catch (RemoteException e) {
            }
        }
        wallpaper.callbacks.finishBroadcast();
        this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.WALLPAPER_CHANGED"), new UserHandle(this.mCurrentUserId));
    }

    /* access modifiers changed from: private */
    public void checkPermission(String permission) {
        if (this.mContext.checkCallingOrSelfPermission(permission) != 0) {
            throw new SecurityException("Access denied to process: " + Binder.getCallingPid() + ", must have permission " + permission);
        }
    }

    public boolean isWallpaperSupported(String callingPackage) {
        return this.mAppOpsManager.checkOpNoThrow(48, Binder.getCallingUid(), callingPackage) == 0;
    }

    public boolean isSetWallpaperAllowed(String callingPackage) {
        if (!Arrays.asList(this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid())).contains(callingPackage)) {
            return false;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService(DevicePolicyManager.class);
        if (dpm.isDeviceOwnerApp(callingPackage) || dpm.isProfileOwnerApp(callingPackage)) {
            return true;
        }
        return true ^ ((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_set_wallpaper");
    }

    public boolean isWallpaperBackupEligible(int which, int userId) {
        WallpaperData wallpaper;
        if (Binder.getCallingUid() == 1000) {
            if (which == 2) {
                wallpaper = this.mLockWallpaperMap.get(userId);
            } else {
                wallpaper = this.mWallpaperMap.get(userId);
            }
            if (wallpaper != null) {
                return wallpaper.allowBackup;
            }
            return false;
        }
        throw new SecurityException("Only the system may call isWallpaperBackupEligible");
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004c, code lost:
        return;
     */
    public void onDisplayReadyInternal(int displayId) {
        synchronized (this.mLock) {
            if (this.mLastWallpaper != null) {
                if (supportsMultiDisplay(this.mLastWallpaper.connection)) {
                    WallpaperConnection.DisplayConnector connector = this.mLastWallpaper.connection.getDisplayConnectorOrCreate(displayId);
                    if (connector != null) {
                        connector.connectLocked(this.mLastWallpaper.connection, this.mLastWallpaper);
                    }
                } else if (this.mFallbackWallpaper != null) {
                    WallpaperConnection.DisplayConnector connector2 = this.mFallbackWallpaper.connection.getDisplayConnectorOrCreate(displayId);
                    if (connector2 != null) {
                        connector2.connectLocked(this.mFallbackWallpaper.connection, this.mFallbackWallpaper);
                    }
                } else {
                    Slog.w(TAG, "No wallpaper can be added to the new display");
                }
            }
        }
    }

    private static JournaledFile makeJournaledFile(int userId) {
        String base = new File(getWallpaperDir(userId), WALLPAPER_INFO).getAbsolutePath();
        File file = new File(base);
        return new JournaledFile(file, new File(base + ".tmp"));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.io.FileOutputStream.<init>(java.io.File, boolean):void throws java.io.FileNotFoundException}
     arg types: [java.io.File, int]
     candidates:
      ClspMth{java.io.FileOutputStream.<init>(java.lang.String, boolean):void throws java.io.FileNotFoundException}
      ClspMth{java.io.FileOutputStream.<init>(java.io.File, boolean):void throws java.io.FileNotFoundException} */
    /* access modifiers changed from: private */
    public void saveSettingsLocked(int userId) {
        JournaledFile journal = makeJournaledFile(userId);
        try {
            XmlSerializer out = new FastXmlSerializer();
            FileOutputStream fstream = new FileOutputStream(journal.chooseForWrite(), false);
            BufferedOutputStream stream = new BufferedOutputStream(fstream);
            out.setOutput(stream, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            WallpaperData wallpaper = this.mWallpaperMap.get(userId);
            if (wallpaper != null) {
                writeWallpaperAttributes(out, "wp", wallpaper);
            }
            WallpaperData wallpaper2 = this.mLockWallpaperMap.get(userId);
            if (wallpaper2 != null) {
                writeWallpaperAttributes(out, "kwp", wallpaper2);
            }
            out.endDocument();
            stream.flush();
            FileUtils.sync(fstream);
            stream.close();
            journal.commit();
        } catch (IOException e) {
            IoUtils.closeQuietly((AutoCloseable) null);
            journal.rollback();
        }
    }

    private void writeWallpaperAttributes(XmlSerializer out, String tag, WallpaperData wallpaper) throws IllegalArgumentException, IllegalStateException, IOException {
        if (DEBUG) {
            Slog.v(TAG, "writeWallpaperAttributes id=" + wallpaper.wallpaperId);
        }
        DisplayData wpdData = getDisplayDataOrCreate(0);
        out.startTag(null, tag);
        out.attribute(null, "id", Integer.toString(wallpaper.wallpaperId));
        out.attribute(null, "width", Integer.toString(wpdData.mWidth));
        out.attribute(null, "height", Integer.toString(wpdData.mHeight));
        out.attribute(null, "cropLeft", Integer.toString(wallpaper.cropHint.left));
        out.attribute(null, "cropTop", Integer.toString(wallpaper.cropHint.top));
        out.attribute(null, "cropRight", Integer.toString(wallpaper.cropHint.right));
        out.attribute(null, "cropBottom", Integer.toString(wallpaper.cropHint.bottom));
        if (wpdData.mPadding.left != 0) {
            out.attribute(null, "paddingLeft", Integer.toString(wpdData.mPadding.left));
        }
        if (wpdData.mPadding.top != 0) {
            out.attribute(null, "paddingTop", Integer.toString(wpdData.mPadding.top));
        }
        if (wpdData.mPadding.right != 0) {
            out.attribute(null, "paddingRight", Integer.toString(wpdData.mPadding.right));
        }
        if (wpdData.mPadding.bottom != 0) {
            out.attribute(null, "paddingBottom", Integer.toString(wpdData.mPadding.bottom));
        }
        if (wallpaper.primaryColors != null) {
            int colorsCount = wallpaper.primaryColors.getMainColors().size();
            out.attribute(null, "colorsCount", Integer.toString(colorsCount));
            if (colorsCount > 0) {
                for (int i = 0; i < colorsCount; i++) {
                    out.attribute(null, "colorValue" + i, Integer.toString(((Color) wallpaper.primaryColors.getMainColors().get(i)).toArgb()));
                }
            }
            out.attribute(null, "colorHints", Integer.toString(wallpaper.primaryColors.getColorHints()));
        }
        out.attribute(null, Settings.ATTR_NAME, wallpaper.name);
        if (wallpaper.wallpaperComponent != null && !wallpaper.wallpaperComponent.equals(this.mImageWallpaper)) {
            out.attribute(null, "component", wallpaper.wallpaperComponent.flattenToShortString());
        }
        if (wallpaper.allowBackup) {
            out.attribute(null, BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD, TemperatureProvider.SWITCH_ON);
        }
        out.endTag(null, tag);
    }

    private void migrateFromOld() {
        File preNWallpaper = new File(getWallpaperDir(0), WALLPAPER_CROP);
        File originalWallpaper = new File("/data/data/com.android.settings/files/wallpaper");
        File newWallpaper = new File(getWallpaperDir(0), WALLPAPER);
        if (preNWallpaper.exists()) {
            if (!newWallpaper.exists()) {
                if (DEBUG) {
                    Slog.i(TAG, "Migrating wallpaper schema");
                }
                FileUtils.copyFile(preNWallpaper, newWallpaper);
            }
        } else if (originalWallpaper.exists()) {
            if (DEBUG) {
                Slog.i(TAG, "Migrating antique wallpaper schema");
            }
            File oldInfo = new File("/data/system/wallpaper_info.xml");
            if (oldInfo.exists()) {
                oldInfo.renameTo(new File(getWallpaperDir(0), WALLPAPER_INFO));
            }
            FileUtils.copyFile(originalWallpaper, preNWallpaper);
            originalWallpaper.renameTo(newWallpaper);
        }
    }

    private int getAttributeInt(XmlPullParser parser, String name, int defValue) {
        String value = parser.getAttributeValue(null, name);
        if (value == null) {
            return defValue;
        }
        return Integer.parseInt(value);
    }

    /* access modifiers changed from: private */
    public WallpaperData getWallpaperSafeLocked(int userId, int which) {
        SparseArray<WallpaperData> whichSet = which == 2 ? this.mLockWallpaperMap : this.mWallpaperMap;
        WallpaperData wallpaper = whichSet.get(userId);
        if (wallpaper != null) {
            return wallpaper;
        }
        loadSettingsLocked(userId, false);
        WallpaperData wallpaper2 = whichSet.get(userId);
        if (wallpaper2 != null) {
            return wallpaper2;
        }
        if (which == 2) {
            WallpaperData wallpaper3 = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
            this.mLockWallpaperMap.put(userId, wallpaper3);
            ensureSaneWallpaperData(wallpaper3, 0);
            return wallpaper3;
        }
        Slog.wtf(TAG, "Didn't find wallpaper in non-lock case!");
        WallpaperData wallpaper4 = new WallpaperData(userId, WALLPAPER, WALLPAPER_CROP);
        this.mWallpaperMap.put(userId, wallpaper4);
        ensureSaneWallpaperData(wallpaper4, 0);
        return wallpaper4;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x02b8  */
    /* JADX WARNING: Removed duplicated region for block: B:134:0x02be  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x02ec  */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x0330  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0332  */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x0372  */
    /* JADX WARNING: Removed duplicated region for block: B:153:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00c2 A[Catch:{ FileNotFoundException -> 0x01c3, NullPointerException -> 0x01be, NumberFormatException -> 0x01b9, XmlPullParserException -> 0x01b4, IOException -> 0x01af, IndexOutOfBoundsException -> 0x01aa }] */
    @OppoHook(level = OppoHook.OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.Keyguard,2012.08.27:add for oppo-wallpaper", property = OppoHook.OppoRomType.ROM)
    public void loadSettingsLocked(int userId, boolean keepDimensionHints) {
        WallpaperData wallpaper;
        int i;
        WallpaperData lockWallpaper;
        FileInputStream stream;
        JournaledFile journal;
        JournaledFile journal2;
        JournaledFile journal3 = makeJournaledFile(userId);
        FileInputStream stream2 = null;
        File file = journal3.chooseForRead();
        WallpaperData wallpaper2 = this.mWallpaperMap.get(userId);
        if (wallpaper2 == null) {
            migrateFromOld();
            WallpaperData wallpaper3 = new WallpaperData(userId, WALLPAPER, WALLPAPER_CROP);
            wallpaper3.allowBackup = true;
            this.mWallpaperMap.put(userId, wallpaper3);
            if (!wallpaper3.cropExists()) {
                if (wallpaper3.sourceExists()) {
                    generateCrop(wallpaper3);
                } else {
                    Slog.i(TAG, "No static wallpaper imagery; defaults will be shown");
                }
            }
            initializeFallbackWallpaper();
            wallpaper = wallpaper3;
        } else {
            wallpaper = wallpaper2;
        }
        boolean success = false;
        DisplayData wpdData = getDisplayDataOrCreate(0);
        try {
            stream2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, StandardCharsets.UTF_8.name());
                while (true) {
                    int type = parser.next();
                    if (type == 2) {
                        String tag = parser.getName();
                        if ("wp".equals(tag)) {
                            try {
                                parseWallpaperAttributes(parser, wallpaper, keepDimensionHints);
                                journal = journal3;
                                journal2 = null;
                            } catch (FileNotFoundException e) {
                                Slog.w(TAG, "no current wallpaper -- first boot?");
                                IoUtils.closeQuietly(stream2);
                                if (success) {
                                }
                                if (success) {
                                }
                                Display d = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                DisplayMetrics displayMetrics = new DisplayMetrics();
                                d.getRealMetrics(displayMetrics);
                                int maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
                                int minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
                                wallpaper.name = "";
                                wpdData.mPadding.set(0, 0, 0, 0);
                                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                wpdData.mHeight = maxDim;
                                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                i = 0;
                                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (NullPointerException e2) {
                                e = e2;
                                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                IoUtils.closeQuietly(stream2);
                                if (success) {
                                }
                                if (success) {
                                }
                                Display d2 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                DisplayMetrics displayMetrics2 = new DisplayMetrics();
                                d2.getRealMetrics(displayMetrics2);
                                int maxDim2 = Math.max(displayMetrics2.widthPixels, displayMetrics2.heightPixels);
                                int minDim2 = Math.min(displayMetrics2.widthPixels, displayMetrics2.heightPixels);
                                wallpaper.name = "";
                                wpdData.mPadding.set(0, 0, 0, 0);
                                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim2 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                wpdData.mHeight = maxDim2;
                                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                i = 0;
                                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (NumberFormatException e3) {
                                e = e3;
                                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                IoUtils.closeQuietly(stream2);
                                if (success) {
                                }
                                if (success) {
                                }
                                Display d22 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                DisplayMetrics displayMetrics22 = new DisplayMetrics();
                                d22.getRealMetrics(displayMetrics22);
                                int maxDim22 = Math.max(displayMetrics22.widthPixels, displayMetrics22.heightPixels);
                                int minDim22 = Math.min(displayMetrics22.widthPixels, displayMetrics22.heightPixels);
                                wallpaper.name = "";
                                wpdData.mPadding.set(0, 0, 0, 0);
                                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim22 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                wpdData.mHeight = maxDim22;
                                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                i = 0;
                                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (XmlPullParserException e4) {
                                e = e4;
                                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                IoUtils.closeQuietly(stream2);
                                if (success) {
                                }
                                if (success) {
                                }
                                Display d222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                DisplayMetrics displayMetrics222 = new DisplayMetrics();
                                d222.getRealMetrics(displayMetrics222);
                                int maxDim222 = Math.max(displayMetrics222.widthPixels, displayMetrics222.heightPixels);
                                int minDim222 = Math.min(displayMetrics222.widthPixels, displayMetrics222.heightPixels);
                                wallpaper.name = "";
                                wpdData.mPadding.set(0, 0, 0, 0);
                                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                wpdData.mHeight = maxDim222;
                                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                i = 0;
                                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (IOException e5) {
                                e = e5;
                                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                IoUtils.closeQuietly(stream2);
                                if (success) {
                                }
                                if (success) {
                                }
                                Display d2222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                DisplayMetrics displayMetrics2222 = new DisplayMetrics();
                                d2222.getRealMetrics(displayMetrics2222);
                                int maxDim2222 = Math.max(displayMetrics2222.widthPixels, displayMetrics2222.heightPixels);
                                int minDim2222 = Math.min(displayMetrics2222.widthPixels, displayMetrics2222.heightPixels);
                                wallpaper.name = "";
                                wpdData.mPadding.set(0, 0, 0, 0);
                                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim2222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                wpdData.mHeight = maxDim2222;
                                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                i = 0;
                                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (IndexOutOfBoundsException e6) {
                                e = e6;
                                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                IoUtils.closeQuietly(stream2);
                                if (success) {
                                }
                                if (success) {
                                }
                                Display d22222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                DisplayMetrics displayMetrics22222 = new DisplayMetrics();
                                d22222.getRealMetrics(displayMetrics22222);
                                int maxDim22222 = Math.max(displayMetrics22222.widthPixels, displayMetrics22222.heightPixels);
                                int minDim22222 = Math.min(displayMetrics22222.widthPixels, displayMetrics22222.heightPixels);
                                wallpaper.name = "";
                                wpdData.mPadding.set(0, 0, 0, 0);
                                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim22222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                wpdData.mHeight = maxDim22222;
                                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                i = 0;
                                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            }
                            try {
                                String comp = parser.getAttributeValue(null, "component");
                                if (comp != null) {
                                    try {
                                        journal2 = ComponentName.unflattenFromString(comp);
                                    } catch (FileNotFoundException e7) {
                                    } catch (NullPointerException e8) {
                                        e = e8;
                                        Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                        IoUtils.closeQuietly(stream2);
                                        if (success) {
                                        }
                                        if (success) {
                                        }
                                        Display d222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                        DisplayMetrics displayMetrics222222 = new DisplayMetrics();
                                        d222222.getRealMetrics(displayMetrics222222);
                                        int maxDim222222 = Math.max(displayMetrics222222.widthPixels, displayMetrics222222.heightPixels);
                                        int minDim222222 = Math.min(displayMetrics222222.widthPixels, displayMetrics222222.heightPixels);
                                        wallpaper.name = "";
                                        wpdData.mPadding.set(0, 0, 0, 0);
                                        wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                        wpdData.mHeight = maxDim222222;
                                        Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                        i = 0;
                                        wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                        lockWallpaper = this.mLockWallpaperMap.get(userId);
                                        if (lockWallpaper == null) {
                                        }
                                    } catch (NumberFormatException e9) {
                                        e = e9;
                                        Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                        IoUtils.closeQuietly(stream2);
                                        if (success) {
                                        }
                                        if (success) {
                                        }
                                        Display d2222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                        DisplayMetrics displayMetrics2222222 = new DisplayMetrics();
                                        d2222222.getRealMetrics(displayMetrics2222222);
                                        int maxDim2222222 = Math.max(displayMetrics2222222.widthPixels, displayMetrics2222222.heightPixels);
                                        int minDim2222222 = Math.min(displayMetrics2222222.widthPixels, displayMetrics2222222.heightPixels);
                                        wallpaper.name = "";
                                        wpdData.mPadding.set(0, 0, 0, 0);
                                        wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim2222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                        wpdData.mHeight = maxDim2222222;
                                        Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                        i = 0;
                                        wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                        lockWallpaper = this.mLockWallpaperMap.get(userId);
                                        if (lockWallpaper == null) {
                                        }
                                    } catch (XmlPullParserException e10) {
                                        e = e10;
                                        Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                        IoUtils.closeQuietly(stream2);
                                        if (success) {
                                        }
                                        if (success) {
                                        }
                                        Display d22222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                        DisplayMetrics displayMetrics22222222 = new DisplayMetrics();
                                        d22222222.getRealMetrics(displayMetrics22222222);
                                        int maxDim22222222 = Math.max(displayMetrics22222222.widthPixels, displayMetrics22222222.heightPixels);
                                        int minDim22222222 = Math.min(displayMetrics22222222.widthPixels, displayMetrics22222222.heightPixels);
                                        wallpaper.name = "";
                                        wpdData.mPadding.set(0, 0, 0, 0);
                                        wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim22222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                        wpdData.mHeight = maxDim22222222;
                                        Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                        i = 0;
                                        wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                        lockWallpaper = this.mLockWallpaperMap.get(userId);
                                        if (lockWallpaper == null) {
                                        }
                                    } catch (IOException e11) {
                                        e = e11;
                                        Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                        IoUtils.closeQuietly(stream2);
                                        if (success) {
                                        }
                                        if (success) {
                                        }
                                        Display d222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                        DisplayMetrics displayMetrics222222222 = new DisplayMetrics();
                                        d222222222.getRealMetrics(displayMetrics222222222);
                                        int maxDim222222222 = Math.max(displayMetrics222222222.widthPixels, displayMetrics222222222.heightPixels);
                                        int minDim222222222 = Math.min(displayMetrics222222222.widthPixels, displayMetrics222222222.heightPixels);
                                        wallpaper.name = "";
                                        wpdData.mPadding.set(0, 0, 0, 0);
                                        wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                        wpdData.mHeight = maxDim222222222;
                                        Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                        i = 0;
                                        wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                        lockWallpaper = this.mLockWallpaperMap.get(userId);
                                        if (lockWallpaper == null) {
                                        }
                                    } catch (IndexOutOfBoundsException e12) {
                                        e = e12;
                                        Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                        IoUtils.closeQuietly(stream2);
                                        if (success) {
                                        }
                                        if (success) {
                                        }
                                        Display d2222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                        DisplayMetrics displayMetrics2222222222 = new DisplayMetrics();
                                        d2222222222.getRealMetrics(displayMetrics2222222222);
                                        int maxDim2222222222 = Math.max(displayMetrics2222222222.widthPixels, displayMetrics2222222222.heightPixels);
                                        int minDim2222222222 = Math.min(displayMetrics2222222222.widthPixels, displayMetrics2222222222.heightPixels);
                                        wallpaper.name = "";
                                        wpdData.mPadding.set(0, 0, 0, 0);
                                        wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim2222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                        wpdData.mHeight = maxDim2222222222;
                                        Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                        i = 0;
                                        wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                        lockWallpaper = this.mLockWallpaperMap.get(userId);
                                        if (lockWallpaper == null) {
                                        }
                                    }
                                }
                                wallpaper.nextWallpaperComponent = journal2;
                                if (wallpaper.nextWallpaperComponent != null) {
                                    stream = stream2;
                                    try {
                                        if (PackageManagerService.PLATFORM_PACKAGE_NAME.equals(wallpaper.nextWallpaperComponent.getPackageName())) {
                                        }
                                        if (DEBUG) {
                                            Slog.v(TAG, "mWidth:" + wpdData.mWidth);
                                            Slog.v(TAG, "mHeight:" + wpdData.mHeight);
                                            Slog.v(TAG, "cropRect:" + wallpaper.cropHint);
                                            Slog.v(TAG, "primaryColors:" + wallpaper.primaryColors);
                                            Slog.v(TAG, "mName:" + wallpaper.name);
                                            Slog.v(TAG, "mNextWallpaperComponent:" + wallpaper.nextWallpaperComponent);
                                        }
                                    } catch (FileNotFoundException e13) {
                                        stream2 = stream;
                                        Slog.w(TAG, "no current wallpaper -- first boot?");
                                        IoUtils.closeQuietly(stream2);
                                        if (success) {
                                        }
                                        if (success) {
                                        }
                                        Display d22222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                        DisplayMetrics displayMetrics22222222222 = new DisplayMetrics();
                                        d22222222222.getRealMetrics(displayMetrics22222222222);
                                        int maxDim22222222222 = Math.max(displayMetrics22222222222.widthPixels, displayMetrics22222222222.heightPixels);
                                        int minDim22222222222 = Math.min(displayMetrics22222222222.widthPixels, displayMetrics22222222222.heightPixels);
                                        wallpaper.name = "";
                                        wpdData.mPadding.set(0, 0, 0, 0);
                                        wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim22222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                        wpdData.mHeight = maxDim22222222222;
                                        Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                        i = 0;
                                        wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                        lockWallpaper = this.mLockWallpaperMap.get(userId);
                                        if (lockWallpaper == null) {
                                        }
                                    } catch (NullPointerException e14) {
                                        e = e14;
                                        stream2 = stream;
                                        Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                        IoUtils.closeQuietly(stream2);
                                        if (success) {
                                        }
                                        if (success) {
                                        }
                                        Display d222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                        DisplayMetrics displayMetrics222222222222 = new DisplayMetrics();
                                        d222222222222.getRealMetrics(displayMetrics222222222222);
                                        int maxDim222222222222 = Math.max(displayMetrics222222222222.widthPixels, displayMetrics222222222222.heightPixels);
                                        int minDim222222222222 = Math.min(displayMetrics222222222222.widthPixels, displayMetrics222222222222.heightPixels);
                                        wallpaper.name = "";
                                        wpdData.mPadding.set(0, 0, 0, 0);
                                        wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                        wpdData.mHeight = maxDim222222222222;
                                        Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                        i = 0;
                                        wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                        lockWallpaper = this.mLockWallpaperMap.get(userId);
                                        if (lockWallpaper == null) {
                                        }
                                    } catch (NumberFormatException e15) {
                                        e = e15;
                                        stream2 = stream;
                                        Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                        IoUtils.closeQuietly(stream2);
                                        if (success) {
                                        }
                                        if (success) {
                                        }
                                        Display d2222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                        DisplayMetrics displayMetrics2222222222222 = new DisplayMetrics();
                                        d2222222222222.getRealMetrics(displayMetrics2222222222222);
                                        int maxDim2222222222222 = Math.max(displayMetrics2222222222222.widthPixels, displayMetrics2222222222222.heightPixels);
                                        int minDim2222222222222 = Math.min(displayMetrics2222222222222.widthPixels, displayMetrics2222222222222.heightPixels);
                                        wallpaper.name = "";
                                        wpdData.mPadding.set(0, 0, 0, 0);
                                        wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim2222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                        wpdData.mHeight = maxDim2222222222222;
                                        Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                        i = 0;
                                        wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                        lockWallpaper = this.mLockWallpaperMap.get(userId);
                                        if (lockWallpaper == null) {
                                        }
                                    } catch (XmlPullParserException e16) {
                                        e = e16;
                                        stream2 = stream;
                                        Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                        IoUtils.closeQuietly(stream2);
                                        if (success) {
                                        }
                                        if (success) {
                                        }
                                        Display d22222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                        DisplayMetrics displayMetrics22222222222222 = new DisplayMetrics();
                                        d22222222222222.getRealMetrics(displayMetrics22222222222222);
                                        int maxDim22222222222222 = Math.max(displayMetrics22222222222222.widthPixels, displayMetrics22222222222222.heightPixels);
                                        int minDim22222222222222 = Math.min(displayMetrics22222222222222.widthPixels, displayMetrics22222222222222.heightPixels);
                                        wallpaper.name = "";
                                        wpdData.mPadding.set(0, 0, 0, 0);
                                        wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim22222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                        wpdData.mHeight = maxDim22222222222222;
                                        Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                        i = 0;
                                        wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                        lockWallpaper = this.mLockWallpaperMap.get(userId);
                                        if (lockWallpaper == null) {
                                        }
                                    } catch (IOException e17) {
                                        e = e17;
                                        stream2 = stream;
                                        Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                        IoUtils.closeQuietly(stream2);
                                        if (success) {
                                        }
                                        if (success) {
                                        }
                                        Display d222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                        DisplayMetrics displayMetrics222222222222222 = new DisplayMetrics();
                                        d222222222222222.getRealMetrics(displayMetrics222222222222222);
                                        int maxDim222222222222222 = Math.max(displayMetrics222222222222222.widthPixels, displayMetrics222222222222222.heightPixels);
                                        int minDim222222222222222 = Math.min(displayMetrics222222222222222.widthPixels, displayMetrics222222222222222.heightPixels);
                                        wallpaper.name = "";
                                        wpdData.mPadding.set(0, 0, 0, 0);
                                        wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                        wpdData.mHeight = maxDim222222222222222;
                                        Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                        i = 0;
                                        wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                        lockWallpaper = this.mLockWallpaperMap.get(userId);
                                        if (lockWallpaper == null) {
                                        }
                                    } catch (IndexOutOfBoundsException e18) {
                                        e = e18;
                                        stream2 = stream;
                                        Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                        IoUtils.closeQuietly(stream2);
                                        if (success) {
                                        }
                                        if (success) {
                                        }
                                        Display d2222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                        DisplayMetrics displayMetrics2222222222222222 = new DisplayMetrics();
                                        d2222222222222222.getRealMetrics(displayMetrics2222222222222222);
                                        int maxDim2222222222222222 = Math.max(displayMetrics2222222222222222.widthPixels, displayMetrics2222222222222222.heightPixels);
                                        int minDim2222222222222222 = Math.min(displayMetrics2222222222222222.widthPixels, displayMetrics2222222222222222.heightPixels);
                                        wallpaper.name = "";
                                        wpdData.mPadding.set(0, 0, 0, 0);
                                        wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim2222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                        wpdData.mHeight = maxDim2222222222222222;
                                        Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                        i = 0;
                                        wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                        lockWallpaper = this.mLockWallpaperMap.get(userId);
                                        if (lockWallpaper == null) {
                                        }
                                    }
                                } else {
                                    stream = stream2;
                                }
                                wallpaper.nextWallpaperComponent = this.mImageWallpaper;
                                if (DEBUG) {
                                }
                            } catch (FileNotFoundException e19) {
                                Slog.w(TAG, "no current wallpaper -- first boot?");
                                IoUtils.closeQuietly(stream2);
                                if (success) {
                                }
                                if (success) {
                                }
                                Display d22222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                DisplayMetrics displayMetrics22222222222222222 = new DisplayMetrics();
                                d22222222222222222.getRealMetrics(displayMetrics22222222222222222);
                                int maxDim22222222222222222 = Math.max(displayMetrics22222222222222222.widthPixels, displayMetrics22222222222222222.heightPixels);
                                int minDim22222222222222222 = Math.min(displayMetrics22222222222222222.widthPixels, displayMetrics22222222222222222.heightPixels);
                                wallpaper.name = "";
                                wpdData.mPadding.set(0, 0, 0, 0);
                                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim22222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                wpdData.mHeight = maxDim22222222222222222;
                                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                i = 0;
                                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (NullPointerException e20) {
                                e = e20;
                                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                IoUtils.closeQuietly(stream2);
                                if (success) {
                                }
                                if (success) {
                                }
                                Display d222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                DisplayMetrics displayMetrics222222222222222222 = new DisplayMetrics();
                                d222222222222222222.getRealMetrics(displayMetrics222222222222222222);
                                int maxDim222222222222222222 = Math.max(displayMetrics222222222222222222.widthPixels, displayMetrics222222222222222222.heightPixels);
                                int minDim222222222222222222 = Math.min(displayMetrics222222222222222222.widthPixels, displayMetrics222222222222222222.heightPixels);
                                wallpaper.name = "";
                                wpdData.mPadding.set(0, 0, 0, 0);
                                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                wpdData.mHeight = maxDim222222222222222222;
                                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                i = 0;
                                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (NumberFormatException e21) {
                                e = e21;
                                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                IoUtils.closeQuietly(stream2);
                                if (success) {
                                }
                                if (success) {
                                }
                                Display d2222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                DisplayMetrics displayMetrics2222222222222222222 = new DisplayMetrics();
                                d2222222222222222222.getRealMetrics(displayMetrics2222222222222222222);
                                int maxDim2222222222222222222 = Math.max(displayMetrics2222222222222222222.widthPixels, displayMetrics2222222222222222222.heightPixels);
                                int minDim2222222222222222222 = Math.min(displayMetrics2222222222222222222.widthPixels, displayMetrics2222222222222222222.heightPixels);
                                wallpaper.name = "";
                                wpdData.mPadding.set(0, 0, 0, 0);
                                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim2222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                wpdData.mHeight = maxDim2222222222222222222;
                                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                i = 0;
                                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (XmlPullParserException e22) {
                                e = e22;
                                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                IoUtils.closeQuietly(stream2);
                                if (success) {
                                }
                                if (success) {
                                }
                                Display d22222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                DisplayMetrics displayMetrics22222222222222222222 = new DisplayMetrics();
                                d22222222222222222222.getRealMetrics(displayMetrics22222222222222222222);
                                int maxDim22222222222222222222 = Math.max(displayMetrics22222222222222222222.widthPixels, displayMetrics22222222222222222222.heightPixels);
                                int minDim22222222222222222222 = Math.min(displayMetrics22222222222222222222.widthPixels, displayMetrics22222222222222222222.heightPixels);
                                wallpaper.name = "";
                                wpdData.mPadding.set(0, 0, 0, 0);
                                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim22222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                wpdData.mHeight = maxDim22222222222222222222;
                                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                i = 0;
                                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (IOException e23) {
                                e = e23;
                                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                IoUtils.closeQuietly(stream2);
                                if (success) {
                                }
                                if (success) {
                                }
                                Display d222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                DisplayMetrics displayMetrics222222222222222222222 = new DisplayMetrics();
                                d222222222222222222222.getRealMetrics(displayMetrics222222222222222222222);
                                int maxDim222222222222222222222 = Math.max(displayMetrics222222222222222222222.widthPixels, displayMetrics222222222222222222222.heightPixels);
                                int minDim222222222222222222222 = Math.min(displayMetrics222222222222222222222.widthPixels, displayMetrics222222222222222222222.heightPixels);
                                wallpaper.name = "";
                                wpdData.mPadding.set(0, 0, 0, 0);
                                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                wpdData.mHeight = maxDim222222222222222222222;
                                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                i = 0;
                                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (IndexOutOfBoundsException e24) {
                                e = e24;
                                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                                IoUtils.closeQuietly(stream2);
                                if (success) {
                                }
                                if (success) {
                                }
                                Display d2222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                                DisplayMetrics displayMetrics2222222222222222222222 = new DisplayMetrics();
                                d2222222222222222222222.getRealMetrics(displayMetrics2222222222222222222222);
                                int maxDim2222222222222222222222 = Math.max(displayMetrics2222222222222222222222.widthPixels, displayMetrics2222222222222222222222.heightPixels);
                                int minDim2222222222222222222222 = Math.min(displayMetrics2222222222222222222222.widthPixels, displayMetrics2222222222222222222222.heightPixels);
                                wallpaper.name = "";
                                wpdData.mPadding.set(0, 0, 0, 0);
                                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim2222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                                wpdData.mHeight = maxDim2222222222222222222222;
                                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                                i = 0;
                                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            }
                        } else {
                            journal = journal3;
                            stream = stream2;
                            if ("kwp".equals(tag)) {
                                WallpaperData lockWallpaper2 = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper2 == null) {
                                    lockWallpaper2 = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
                                    this.mLockWallpaperMap.put(userId, lockWallpaper2);
                                }
                                parseWallpaperAttributes(parser, lockWallpaper2, false);
                            }
                        }
                    } else {
                        journal = journal3;
                        stream = stream2;
                    }
                    if (type == 1) {
                        break;
                    }
                    journal3 = journal;
                    stream2 = stream;
                }
                success = true;
                stream2 = stream;
            } catch (FileNotFoundException e25) {
                Slog.w(TAG, "no current wallpaper -- first boot?");
                IoUtils.closeQuietly(stream2);
                if (success) {
                }
                if (success) {
                }
                Display d22222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                DisplayMetrics displayMetrics22222222222222222222222 = new DisplayMetrics();
                d22222222222222222222222.getRealMetrics(displayMetrics22222222222222222222222);
                int maxDim22222222222222222222222 = Math.max(displayMetrics22222222222222222222222.widthPixels, displayMetrics22222222222222222222222.heightPixels);
                int minDim22222222222222222222222 = Math.min(displayMetrics22222222222222222222222.widthPixels, displayMetrics22222222222222222222222.heightPixels);
                wallpaper.name = "";
                wpdData.mPadding.set(0, 0, 0, 0);
                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim22222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                wpdData.mHeight = maxDim22222222222222222222222;
                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                i = 0;
                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                lockWallpaper = this.mLockWallpaperMap.get(userId);
                if (lockWallpaper == null) {
                }
            } catch (NullPointerException e26) {
                e = e26;
                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                IoUtils.closeQuietly(stream2);
                if (success) {
                }
                if (success) {
                }
                Display d222222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                DisplayMetrics displayMetrics222222222222222222222222 = new DisplayMetrics();
                d222222222222222222222222.getRealMetrics(displayMetrics222222222222222222222222);
                int maxDim222222222222222222222222 = Math.max(displayMetrics222222222222222222222222.widthPixels, displayMetrics222222222222222222222222.heightPixels);
                int minDim222222222222222222222222 = Math.min(displayMetrics222222222222222222222222.widthPixels, displayMetrics222222222222222222222222.heightPixels);
                wallpaper.name = "";
                wpdData.mPadding.set(0, 0, 0, 0);
                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim222222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                wpdData.mHeight = maxDim222222222222222222222222;
                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                i = 0;
                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                lockWallpaper = this.mLockWallpaperMap.get(userId);
                if (lockWallpaper == null) {
                }
            } catch (NumberFormatException e27) {
                e = e27;
                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                IoUtils.closeQuietly(stream2);
                if (success) {
                }
                if (success) {
                }
                Display d2222222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                DisplayMetrics displayMetrics2222222222222222222222222 = new DisplayMetrics();
                d2222222222222222222222222.getRealMetrics(displayMetrics2222222222222222222222222);
                int maxDim2222222222222222222222222 = Math.max(displayMetrics2222222222222222222222222.widthPixels, displayMetrics2222222222222222222222222.heightPixels);
                int minDim2222222222222222222222222 = Math.min(displayMetrics2222222222222222222222222.widthPixels, displayMetrics2222222222222222222222222.heightPixels);
                wallpaper.name = "";
                wpdData.mPadding.set(0, 0, 0, 0);
                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim2222222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                wpdData.mHeight = maxDim2222222222222222222222222;
                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                i = 0;
                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                lockWallpaper = this.mLockWallpaperMap.get(userId);
                if (lockWallpaper == null) {
                }
            } catch (XmlPullParserException e28) {
                e = e28;
                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                IoUtils.closeQuietly(stream2);
                if (success) {
                }
                if (success) {
                }
                Display d22222222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                DisplayMetrics displayMetrics22222222222222222222222222 = new DisplayMetrics();
                d22222222222222222222222222.getRealMetrics(displayMetrics22222222222222222222222222);
                int maxDim22222222222222222222222222 = Math.max(displayMetrics22222222222222222222222222.widthPixels, displayMetrics22222222222222222222222222.heightPixels);
                int minDim22222222222222222222222222 = Math.min(displayMetrics22222222222222222222222222.widthPixels, displayMetrics22222222222222222222222222.heightPixels);
                wallpaper.name = "";
                wpdData.mPadding.set(0, 0, 0, 0);
                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim22222222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                wpdData.mHeight = maxDim22222222222222222222222222;
                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                i = 0;
                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                lockWallpaper = this.mLockWallpaperMap.get(userId);
                if (lockWallpaper == null) {
                }
            } catch (IOException e29) {
                e = e29;
                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                IoUtils.closeQuietly(stream2);
                if (success) {
                }
                if (success) {
                }
                Display d222222222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                DisplayMetrics displayMetrics222222222222222222222222222 = new DisplayMetrics();
                d222222222222222222222222222.getRealMetrics(displayMetrics222222222222222222222222222);
                int maxDim222222222222222222222222222 = Math.max(displayMetrics222222222222222222222222222.widthPixels, displayMetrics222222222222222222222222222.heightPixels);
                int minDim222222222222222222222222222 = Math.min(displayMetrics222222222222222222222222222.widthPixels, displayMetrics222222222222222222222222222.heightPixels);
                wallpaper.name = "";
                wpdData.mPadding.set(0, 0, 0, 0);
                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim222222222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                wpdData.mHeight = maxDim222222222222222222222222222;
                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                i = 0;
                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                lockWallpaper = this.mLockWallpaperMap.get(userId);
                if (lockWallpaper == null) {
                }
            } catch (IndexOutOfBoundsException e30) {
                e = e30;
                Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
                IoUtils.closeQuietly(stream2);
                if (success) {
                }
                if (success) {
                }
                Display d2222222222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                DisplayMetrics displayMetrics2222222222222222222222222222 = new DisplayMetrics();
                d2222222222222222222222222222.getRealMetrics(displayMetrics2222222222222222222222222222);
                int maxDim2222222222222222222222222222 = Math.max(displayMetrics2222222222222222222222222222.widthPixels, displayMetrics2222222222222222222222222222.heightPixels);
                int minDim2222222222222222222222222222 = Math.min(displayMetrics2222222222222222222222222222.widthPixels, displayMetrics2222222222222222222222222222.heightPixels);
                wallpaper.name = "";
                wpdData.mPadding.set(0, 0, 0, 0);
                wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim2222222222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
                wpdData.mHeight = maxDim2222222222222222222222222222;
                Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
                i = 0;
                wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
                lockWallpaper = this.mLockWallpaperMap.get(userId);
                if (lockWallpaper == null) {
                }
            }
        } catch (FileNotFoundException e31) {
            Slog.w(TAG, "no current wallpaper -- first boot?");
            IoUtils.closeQuietly(stream2);
            if (success) {
            }
            if (success) {
            }
            Display d22222222222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics displayMetrics22222222222222222222222222222 = new DisplayMetrics();
            d22222222222222222222222222222.getRealMetrics(displayMetrics22222222222222222222222222222);
            int maxDim22222222222222222222222222222 = Math.max(displayMetrics22222222222222222222222222222.widthPixels, displayMetrics22222222222222222222222222222.heightPixels);
            int minDim22222222222222222222222222222 = Math.min(displayMetrics22222222222222222222222222222.widthPixels, displayMetrics22222222222222222222222222222.heightPixels);
            wallpaper.name = "";
            wpdData.mPadding.set(0, 0, 0, 0);
            wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim22222222222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
            wpdData.mHeight = maxDim22222222222222222222222222222;
            Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
            i = 0;
            wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
            lockWallpaper = this.mLockWallpaperMap.get(userId);
            if (lockWallpaper == null) {
            }
        } catch (NullPointerException e32) {
            e = e32;
            Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
            IoUtils.closeQuietly(stream2);
            if (success) {
            }
            if (success) {
            }
            Display d222222222222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics displayMetrics222222222222222222222222222222 = new DisplayMetrics();
            d222222222222222222222222222222.getRealMetrics(displayMetrics222222222222222222222222222222);
            int maxDim222222222222222222222222222222 = Math.max(displayMetrics222222222222222222222222222222.widthPixels, displayMetrics222222222222222222222222222222.heightPixels);
            int minDim222222222222222222222222222222 = Math.min(displayMetrics222222222222222222222222222222.widthPixels, displayMetrics222222222222222222222222222222.heightPixels);
            wallpaper.name = "";
            wpdData.mPadding.set(0, 0, 0, 0);
            wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim222222222222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
            wpdData.mHeight = maxDim222222222222222222222222222222;
            Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
            i = 0;
            wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
            lockWallpaper = this.mLockWallpaperMap.get(userId);
            if (lockWallpaper == null) {
            }
        } catch (NumberFormatException e33) {
            e = e33;
            Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
            IoUtils.closeQuietly(stream2);
            if (success) {
            }
            if (success) {
            }
            Display d2222222222222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics displayMetrics2222222222222222222222222222222 = new DisplayMetrics();
            d2222222222222222222222222222222.getRealMetrics(displayMetrics2222222222222222222222222222222);
            int maxDim2222222222222222222222222222222 = Math.max(displayMetrics2222222222222222222222222222222.widthPixels, displayMetrics2222222222222222222222222222222.heightPixels);
            int minDim2222222222222222222222222222222 = Math.min(displayMetrics2222222222222222222222222222222.widthPixels, displayMetrics2222222222222222222222222222222.heightPixels);
            wallpaper.name = "";
            wpdData.mPadding.set(0, 0, 0, 0);
            wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim2222222222222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
            wpdData.mHeight = maxDim2222222222222222222222222222222;
            Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
            i = 0;
            wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
            lockWallpaper = this.mLockWallpaperMap.get(userId);
            if (lockWallpaper == null) {
            }
        } catch (XmlPullParserException e34) {
            e = e34;
            Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
            IoUtils.closeQuietly(stream2);
            if (success) {
            }
            if (success) {
            }
            Display d22222222222222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics displayMetrics22222222222222222222222222222222 = new DisplayMetrics();
            d22222222222222222222222222222222.getRealMetrics(displayMetrics22222222222222222222222222222222);
            int maxDim22222222222222222222222222222222 = Math.max(displayMetrics22222222222222222222222222222222.widthPixels, displayMetrics22222222222222222222222222222222.heightPixels);
            int minDim22222222222222222222222222222222 = Math.min(displayMetrics22222222222222222222222222222222.widthPixels, displayMetrics22222222222222222222222222222222.heightPixels);
            wallpaper.name = "";
            wpdData.mPadding.set(0, 0, 0, 0);
            wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim22222222222222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
            wpdData.mHeight = maxDim22222222222222222222222222222222;
            Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
            i = 0;
            wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
            lockWallpaper = this.mLockWallpaperMap.get(userId);
            if (lockWallpaper == null) {
            }
        } catch (IOException e35) {
            e = e35;
            Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
            IoUtils.closeQuietly(stream2);
            if (success) {
            }
            if (success) {
            }
            Display d222222222222222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics displayMetrics222222222222222222222222222222222 = new DisplayMetrics();
            d222222222222222222222222222222222.getRealMetrics(displayMetrics222222222222222222222222222222222);
            int maxDim222222222222222222222222222222222 = Math.max(displayMetrics222222222222222222222222222222222.widthPixels, displayMetrics222222222222222222222222222222222.heightPixels);
            int minDim222222222222222222222222222222222 = Math.min(displayMetrics222222222222222222222222222222222.widthPixels, displayMetrics222222222222222222222222222222222.heightPixels);
            wallpaper.name = "";
            wpdData.mPadding.set(0, 0, 0, 0);
            wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim222222222222222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
            wpdData.mHeight = maxDim222222222222222222222222222222222;
            Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
            i = 0;
            wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
            lockWallpaper = this.mLockWallpaperMap.get(userId);
            if (lockWallpaper == null) {
            }
        } catch (IndexOutOfBoundsException e36) {
            e = e36;
            Slog.w(TAG, "failed parsing " + file + StringUtils.SPACE + e);
            IoUtils.closeQuietly(stream2);
            if (success) {
            }
            if (success) {
            }
            Display d2222222222222222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics displayMetrics2222222222222222222222222222222222 = new DisplayMetrics();
            d2222222222222222222222222222222222.getRealMetrics(displayMetrics2222222222222222222222222222222222);
            int maxDim2222222222222222222222222222222222 = Math.max(displayMetrics2222222222222222222222222222222222.widthPixels, displayMetrics2222222222222222222222222222222222.heightPixels);
            int minDim2222222222222222222222222222222222 = Math.min(displayMetrics2222222222222222222222222222222222.widthPixels, displayMetrics2222222222222222222222222222222222.heightPixels);
            wallpaper.name = "";
            wpdData.mPadding.set(0, 0, 0, 0);
            wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim2222222222222222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
            wpdData.mHeight = maxDim2222222222222222222222222222222222;
            Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
            i = 0;
            wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
            lockWallpaper = this.mLockWallpaperMap.get(userId);
            if (lockWallpaper == null) {
            }
        }
        IoUtils.closeQuietly(stream2);
        if (success) {
            this.mLockWallpaperMap.remove(userId);
        } else if (wallpaper.wallpaperId <= 0) {
            wallpaper.wallpaperId = makeWallpaperIdLocked();
            if (DEBUG) {
                Slog.w(TAG, "Didn't set wallpaper id in loadSettingsLocked(" + userId + "); now " + wallpaper.wallpaperId);
            }
        }
        if (success || !this.mWallpaperHelper.isWallpaperExist(userId)) {
            Display d22222222222222222222222222222222222 = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics displayMetrics22222222222222222222222222222222222 = new DisplayMetrics();
            d22222222222222222222222222222222222.getRealMetrics(displayMetrics22222222222222222222222222222222222);
            int maxDim22222222222222222222222222222222222 = Math.max(displayMetrics22222222222222222222222222222222222.widthPixels, displayMetrics22222222222222222222222222222222222.heightPixels);
            int minDim22222222222222222222222222222222222 = Math.min(displayMetrics22222222222222222222222222222222222.widthPixels, displayMetrics22222222222222222222222222222222222.heightPixels);
            wallpaper.name = "";
            wpdData.mPadding.set(0, 0, 0, 0);
            wpdData.mWidth = -1 != this.mWallpaperHelper.mWidthOfDefaultWallpaper ? minDim22222222222222222222222222222222222 : this.mWallpaperHelper.mWidthOfDefaultWallpaper;
            wpdData.mHeight = maxDim22222222222222222222222222222222222;
            Slog.d(TAG, "loadSettingsLocked wpdData.mWidth = " + wpdData.mWidth + " wpdData.mHeight = " + wpdData.mHeight);
            i = 0;
            wallpaper.cropHint.set(0, 0, wpdData.mWidth, wpdData.mHeight);
        } else {
            i = 0;
        }
        lockWallpaper = this.mLockWallpaperMap.get(userId);
        if (lockWallpaper == null) {
            ensureSaneWallpaperData(lockWallpaper, i);
        }
    }

    private void initializeFallbackWallpaper() {
        if (this.mFallbackWallpaper == null) {
            if (DEBUG) {
                Slog.d(TAG, "Initialize fallback wallpaper");
            }
            this.mFallbackWallpaper = new WallpaperData(0, WALLPAPER, WALLPAPER_CROP);
            WallpaperData wallpaperData = this.mFallbackWallpaper;
            wallpaperData.allowBackup = false;
            wallpaperData.wallpaperId = makeWallpaperIdLocked();
            bindWallpaperComponentLocked(this.mImageWallpaper, true, false, this.mFallbackWallpaper, null);
        }
    }

    private void ensureSaneWallpaperData(WallpaperData wallpaper, int displayId) {
        DisplayData size = getDisplayDataOrCreate(displayId);
        if (displayId != 0) {
            return;
        }
        if (wallpaper.cropHint.width() <= 0 || wallpaper.cropHint.height() <= 0) {
            wallpaper.cropHint.set(0, 0, size.mWidth, size.mHeight);
        }
    }

    private void parseWallpaperAttributes(XmlPullParser parser, WallpaperData wallpaper, boolean keepDimensionHints) {
        String idString = parser.getAttributeValue(null, "id");
        if (idString != null) {
            int id = Integer.parseInt(idString);
            wallpaper.wallpaperId = id;
            if (id > this.mWallpaperId) {
                this.mWallpaperId = id;
            }
        } else {
            wallpaper.wallpaperId = makeWallpaperIdLocked();
        }
        DisplayData wpData = getDisplayDataOrCreate(0);
        if (!keepDimensionHints) {
            wpData.mWidth = Integer.parseInt(parser.getAttributeValue(null, "width"));
            wpData.mHeight = Integer.parseInt(parser.getAttributeValue(null, "height"));
        }
        wallpaper.cropHint.left = getAttributeInt(parser, "cropLeft", 0);
        wallpaper.cropHint.top = getAttributeInt(parser, "cropTop", 0);
        wallpaper.cropHint.right = getAttributeInt(parser, "cropRight", 0);
        wallpaper.cropHint.bottom = getAttributeInt(parser, "cropBottom", 0);
        wpData.mPadding.left = getAttributeInt(parser, "paddingLeft", 0);
        wpData.mPadding.top = getAttributeInt(parser, "paddingTop", 0);
        wpData.mPadding.right = getAttributeInt(parser, "paddingRight", 0);
        wpData.mPadding.bottom = getAttributeInt(parser, "paddingBottom", 0);
        int colorsCount = getAttributeInt(parser, "colorsCount", 0);
        if (colorsCount > 0) {
            Color primary = null;
            Color secondary = null;
            Color tertiary = null;
            for (int i = 0; i < colorsCount; i++) {
                Color color = Color.valueOf(getAttributeInt(parser, "colorValue" + i, 0));
                if (i != 0) {
                    if (i != 1) {
                        if (i != 2) {
                            break;
                        }
                        tertiary = color;
                    } else {
                        secondary = color;
                    }
                } else {
                    primary = color;
                }
            }
            wallpaper.primaryColors = new WallpaperColors(primary, secondary, tertiary, getAttributeInt(parser, "colorHints", 0));
        }
        wallpaper.name = parser.getAttributeValue(null, Settings.ATTR_NAME);
        wallpaper.allowBackup = TemperatureProvider.SWITCH_ON.equals(parser.getAttributeValue(null, BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD));
    }

    public void settingsRestored() {
        WallpaperData wallpaper;
        boolean success;
        if (Binder.getCallingUid() == 1000) {
            if (DEBUG) {
                Slog.v(TAG, "settingsRestored");
            }
            synchronized (this.mLock) {
                loadSettingsLocked(0, false);
                wallpaper = this.mWallpaperMap.get(0);
                wallpaper.wallpaperId = makeWallpaperIdLocked();
                wallpaper.allowBackup = true;
                if (wallpaper.nextWallpaperComponent == null || wallpaper.nextWallpaperComponent.equals(this.mImageWallpaper)) {
                    if ("".equals(wallpaper.name)) {
                        if (DEBUG) {
                            Slog.v(TAG, "settingsRestored: name is empty");
                        }
                        success = true;
                    } else {
                        if (DEBUG) {
                            Slog.v(TAG, "settingsRestored: attempting to restore named resource");
                        }
                        success = restoreNamedResourceLocked(wallpaper);
                    }
                    if (DEBUG) {
                        Slog.v(TAG, "settingsRestored: success=" + success + " id=" + wallpaper.wallpaperId);
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
                wallpaper.name = "";
                getWallpaperDir(0).delete();
            }
            synchronized (this.mLock) {
                saveSettingsLocked(0);
            }
            return;
        }
        throw new RuntimeException("settingsRestored() can only be called from the system process");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0130, code lost:
        if (0 != 0) goto L_0x0132;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0132, code lost:
        android.os.FileUtils.sync(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0135, code lost:
        libcore.io.IoUtils.closeQuietly((java.lang.AutoCloseable) null);
        libcore.io.IoUtils.closeQuietly((java.lang.AutoCloseable) null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x015a, code lost:
        if (0 != 0) goto L_0x0132;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0180, code lost:
        if (0 != 0) goto L_0x0132;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:?, code lost:
        return false;
     */
    private boolean restoreNamedResourceLocked(WallpaperData wallpaper) {
        String pkg;
        String ident;
        String type;
        if (wallpaper.name.length() <= 4 || !"res:".equals(wallpaper.name.substring(0, 4))) {
            return false;
        }
        String resName = wallpaper.name.substring(4);
        int colon = resName.indexOf(58);
        if (colon > 0) {
            pkg = resName.substring(0, colon);
        } else {
            pkg = null;
        }
        int slash = resName.lastIndexOf(47);
        if (slash > 0) {
            ident = resName.substring(slash + 1);
        } else {
            ident = null;
        }
        if (colon <= 0 || slash <= 0 || slash - colon <= 1) {
            type = null;
        } else {
            type = resName.substring(colon + 1, slash);
        }
        if (pkg == null || ident == null || type == null) {
            return false;
        }
        try {
            Resources r = this.mContext.createPackageContext(pkg, 4).getResources();
            int resId = r.getIdentifier(resName, null, null);
            if (resId == 0) {
                Slog.e(TAG, "couldn't resolve identifier pkg=" + pkg + " type=" + type + " ident=" + ident);
                IoUtils.closeQuietly((AutoCloseable) null);
                if (0 != 0) {
                    FileUtils.sync(null);
                }
                if (0 != 0) {
                    FileUtils.sync(null);
                }
                IoUtils.closeQuietly((AutoCloseable) null);
                IoUtils.closeQuietly((AutoCloseable) null);
                return false;
            }
            InputStream res = r.openRawResource(resId);
            if (wallpaper.wallpaperFile.exists()) {
                wallpaper.wallpaperFile.delete();
                wallpaper.cropFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(wallpaper.wallpaperFile);
            FileOutputStream cos = new FileOutputStream(wallpaper.cropFile);
            byte[] buffer = new byte[32768];
            while (true) {
                int amt = res.read(buffer);
                if (amt > 0) {
                    fos.write(buffer, 0, amt);
                    cos.write(buffer, 0, amt);
                } else {
                    Slog.v(TAG, "Restored wallpaper: " + resName);
                    IoUtils.closeQuietly(res);
                    FileUtils.sync(fos);
                    FileUtils.sync(cos);
                    IoUtils.closeQuietly(fos);
                    IoUtils.closeQuietly(cos);
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "Package name " + pkg + " not found");
            IoUtils.closeQuietly((AutoCloseable) null);
            if (0 != 0) {
                FileUtils.sync(null);
            }
        } catch (Resources.NotFoundException e2) {
            Slog.e(TAG, "Resource not found: " + -1);
            IoUtils.closeQuietly((AutoCloseable) null);
            if (0 != 0) {
                FileUtils.sync(null);
            }
        } catch (IOException e3) {
            Slog.e(TAG, "IOException while restoring wallpaper ", e3);
            IoUtils.closeQuietly((AutoCloseable) null);
            if (0 != 0) {
                FileUtils.sync(null);
            }
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            if (0 != 0) {
                FileUtils.sync(null);
            }
            if (0 != 0) {
                FileUtils.sync(null);
            }
            IoUtils.closeQuietly((AutoCloseable) null);
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            synchronized (this.mLock) {
                pw.println("System wallpaper state:");
                for (int i = 0; i < this.mWallpaperMap.size(); i++) {
                    WallpaperData wallpaper = this.mWallpaperMap.valueAt(i);
                    pw.print(" User ");
                    pw.print(wallpaper.userId);
                    pw.print(": id=");
                    pw.println(wallpaper.wallpaperId);
                    pw.println(" Display state:");
                    forEachDisplayData(new Consumer(pw) {
                        /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$VlOcXJ2BasDkYqNidSTRvwHBpM */
                        private final /* synthetic */ PrintWriter f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            WallpaperManagerService.lambda$dump$6(this.f$0, (WallpaperManagerService.DisplayData) obj);
                        }
                    });
                    pw.print("  mCropHint=");
                    pw.println(wallpaper.cropHint);
                    pw.print("  mName=");
                    pw.println(wallpaper.name);
                    pw.print("  mAllowBackup=");
                    pw.println(wallpaper.allowBackup);
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
                        conn.forEachDisplayConnector(new Consumer(pw) {
                            /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$fLM_YLhVBfWS7QM0taqHXvJ4Uc */
                            private final /* synthetic */ PrintWriter f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                WallpaperManagerService.lambda$dump$7(this.f$0, (WallpaperManagerService.WallpaperConnection.DisplayConnector) obj);
                            }
                        });
                        pw.print("    mService=");
                        pw.println(conn.mService);
                        pw.print("    mLastDiedTime=");
                        pw.println(wallpaper.lastDiedTime - SystemClock.uptimeMillis());
                    }
                }
                pw.println("Lock wallpaper state:");
                for (int i2 = 0; i2 < this.mLockWallpaperMap.size(); i2++) {
                    WallpaperData wallpaper2 = this.mLockWallpaperMap.valueAt(i2);
                    pw.print(" User ");
                    pw.print(wallpaper2.userId);
                    pw.print(": id=");
                    pw.println(wallpaper2.wallpaperId);
                    pw.print("  mCropHint=");
                    pw.println(wallpaper2.cropHint);
                    pw.print("  mName=");
                    pw.println(wallpaper2.name);
                    pw.print("  mAllowBackup=");
                    pw.println(wallpaper2.allowBackup);
                }
                pw.println("Fallback wallpaper state:");
                pw.print(" User ");
                pw.print(this.mFallbackWallpaper.userId);
                pw.print(": id=");
                pw.println(this.mFallbackWallpaper.wallpaperId);
                pw.print("  mCropHint=");
                pw.println(this.mFallbackWallpaper.cropHint);
                pw.print("  mName=");
                pw.println(this.mFallbackWallpaper.name);
                pw.print("  mAllowBackup=");
                pw.println(this.mFallbackWallpaper.allowBackup);
                if (this.mFallbackWallpaper.connection != null) {
                    WallpaperConnection conn2 = this.mFallbackWallpaper.connection;
                    pw.print("  Fallback Wallpaper connection ");
                    pw.print(conn2);
                    pw.println(":");
                    if (conn2.mInfo != null) {
                        pw.print("    mInfo.component=");
                        pw.println(conn2.mInfo.getComponent());
                    }
                    conn2.forEachDisplayConnector(new Consumer(pw) {
                        /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$VUhQWq8Flr0dsQqeVHhHT8jU7qY */
                        private final /* synthetic */ PrintWriter f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            WallpaperManagerService.lambda$dump$8(this.f$0, (WallpaperManagerService.WallpaperConnection.DisplayConnector) obj);
                        }
                    });
                    pw.print("    mService=");
                    pw.println(conn2.mService);
                    pw.print("    mLastDiedTime=");
                    pw.println(this.mFallbackWallpaper.lastDiedTime - SystemClock.uptimeMillis());
                }
            }
        }
    }

    static /* synthetic */ void lambda$dump$6(PrintWriter pw, DisplayData wpSize) {
        pw.print("  displayId=");
        pw.println(wpSize.mDisplayId);
        pw.print("  mWidth=");
        pw.print(wpSize.mWidth);
        pw.print("  mHeight=");
        pw.println(wpSize.mHeight);
        pw.print("  mPadding=");
        pw.println(wpSize.mPadding);
    }

    static /* synthetic */ void lambda$dump$7(PrintWriter pw, WallpaperConnection.DisplayConnector connector) {
        pw.print("     mDisplayId=");
        pw.println(connector.mDisplayId);
        pw.print("     mToken=");
        pw.println(connector.mToken);
        pw.print("     mEngine=");
        pw.println(connector.mEngine);
    }

    static /* synthetic */ void lambda$dump$8(PrintWriter pw, WallpaperConnection.DisplayConnector connector) {
        pw.print("     mDisplayId=");
        pw.println(connector.mDisplayId);
        pw.print("     mToken=");
        pw.println(connector.mToken);
        pw.print("     mEngine=");
        pw.println(connector.mEngine);
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@ROM.Framework, add for BPM", property = OppoHook.OppoRomType.ROM)
    public ComponentName getLiveComponent() {
        WallpaperData wallpaper = this.mWallpaperMap.get(this.mCurrentUserId);
        if (wallpaper.wallpaperComponent == null || wallpaper.wallpaperComponent.getClassName().equals("com.android.systemui.ImageWallpaper")) {
            return null;
        }
        return wallpaper.wallpaperComponent;
    }

    /* access modifiers changed from: private */
    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "XinYang.Hu@App.Launcher, add for set wallpaper fast", property = OppoHook.OppoRomType.ROM)
    public void notifyWallpaperColorsChangedInThread(final WallpaperData wallpaperData, final int notifyColorsWhich) {
        if (DEBUG) {
            Slog.w(TAG, "notifyWallpaperColorsChangedInThread. wallpaperData = " + wallpaperData);
        }
        if (this.mNotifyWallpaperColorsChangeRunnable != null) {
            FgThread.getHandler().removeCallbacks(this.mNotifyWallpaperColorsChangeRunnable);
        }
        this.mNotifyWallpaperColorsChangeRunnable = new Runnable() {
            /* class com.android.server.wallpaper.WallpaperManagerService.AnonymousClass6 */

            public void run() {
                if (WallpaperManagerService.DEBUG) {
                    Slog.w(WallpaperManagerService.TAG, "notifyWallpaperColorsChangedInThread. start notifyWallpaperColorsChanged");
                }
                WallpaperManagerService.this.notifyWallpaperColorsChanged(wallpaperData, notifyColorsWhich);
            }
        };
        FgThread.getHandler().postDelayed(this.mNotifyWallpaperColorsChangeRunnable, 150);
    }
}
