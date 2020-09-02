package com.android.server.statusbar;

import android.app.IColorClickTopCallback;
import android.app.IColorStatusBar;
import android.app.IColorStatusBarManager;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.wm.WindowManagerService;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import java.util.Arrays;
import java.util.List;

public class ColorStatusBarManagerService extends StatusBarManagerService implements IColorStatusBarManager {
    private static boolean DEBUG_PANIC = false;
    private static final List<String> ONE_KEY_LOCK_SCREEN_WHITE_LIST = Arrays.asList("com.coloros.onekeylockscreen", "com.oppo.launcher");
    public static final int STATUS_BAR_FUNCTION_CODE_LOCK_SCREEN = 1;
    private static final String TAG = "ColorStatusBarManagerService";
    private RemoteCallbackList<IColorClickTopCallback> mClickTopCallbackList = new RemoteCallbackList<>();
    private Handler mColorHandler = null;
    private final ColorStatusBarManagerInternal mColorInternalService = new ColorStatusBarManagerInternal() {
        /* class com.android.server.statusbar.ColorStatusBarManagerService.AnonymousClass1 */

        @Override // com.android.server.statusbar.ColorStatusBarManagerInternal
        public void updateNavBarVisibility(int navBarVis) {
            ColorStatusBarManagerService.this.updateNavBarVisibility(navBarVis);
        }

        @Override // com.android.server.statusbar.ColorStatusBarManagerInternal
        public void updateNavBarVisibilityWithPkg(int navBarVis, String title) {
            ColorStatusBarManagerService.this.updateNavBarVisibility(navBarVis, title);
        }
    };
    private IColorStatusBar mColorStatusBar = null;
    private Context mContext;
    private boolean mTopIsFullscreen;

    public ColorStatusBarManagerService(Context context, WindowManagerService windowManager) {
        super(context, windowManager);
        this.mContext = context;
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        HandlerThread handlerThread = new HandlerThread("ColorClickTopThread");
        handlerThread.start();
        this.mColorHandler = new Handler(handlerThread.getLooper());
        LocalServices.addService(ColorStatusBarManagerInternal.class, this.mColorInternalService);
    }

    public void topIsFullscreen(boolean fullscreen) {
        this.mTopIsFullscreen = fullscreen;
        if (this.mColorStatusBar != null) {
            try {
                Slog.i(TAG, " topIsFullscreen fullscreen: " + fullscreen);
                this.mColorStatusBar.topIsFullscreen(fullscreen);
            } catch (RemoteException e) {
            }
        }
    }

    public boolean getTopIsFullscreen() {
        Slog.i(TAG, " getTopIsFullscreen mTopIsFullscreen: " + this.mTopIsFullscreen);
        return this.mTopIsFullscreen;
    }

    public void notifyMultiWindowFocusChanged(int state) {
        if (this.mColorStatusBar != null) {
            try {
                Slog.i(TAG, " notifyMultiWindowFocusChanged state: " + state);
                this.mColorStatusBar.notifyMultiWindowFocusChanged(state);
            } catch (RemoteException e) {
            }
        }
    }

    public void toggleSplitScreen(int mode) {
        if (Binder.getCallingUid() == 1000 && this.mColorStatusBar != null) {
            try {
                Slog.i(TAG, " toggleSplitScreen mode: " + mode);
                this.mColorStatusBar.toggleSplitScreen(mode);
            } catch (RemoteException e) {
            }
        }
    }

    public boolean setStatusBarFunction(int functionCode, String pkgName) {
        String[] sourcePackages = this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid());
        if (sourcePackages != null) {
            for (String packageName : sourcePackages) {
                if (1 == functionCode && ONE_KEY_LOCK_SCREEN_WHITE_LIST.contains(packageName) && this.mColorStatusBar != null) {
                    try {
                        Slog.i(TAG, " setStatusBarFunction functionCode: " + functionCode + " packageName: " + packageName);
                        this.mColorStatusBar.setStatusBarFunction(functionCode, pkgName);
                        return true;
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    public void registerColorStatusBar(IColorStatusBar callback) {
        Slog.i(TAG, "registerColorStatusBar ");
        this.mColorStatusBar = callback;
    }

    public void registerColorClickTopCallback(IColorClickTopCallback callback) {
        Slog.i(TAG, "registerColorClickTopCallback ");
        this.mClickTopCallbackList.register(callback);
    }

    public void unregisterColorClickTopCallback(IColorClickTopCallback callback) {
        Slog.i(TAG, "unregisterColorClickTopCallback ");
        this.mClickTopCallbackList.unregister(callback);
    }

    public void notifyClickTop() {
        Slog.i(TAG, "notifyClickTop ");
        int n = this.mClickTopCallbackList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            try {
                this.mClickTopCallbackList.getBroadcastItem(i).onClickTopCallback();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mClickTopCallbackList.finishBroadcast();
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 10002:
                data.enforceInterface("android.app.IStatusBarManager");
                registerColorStatusBar(IColorStatusBar.Stub.asInterface(data.readStrongBinder()));
                return true;
            case 10003:
                data.enforceInterface("android.app.IStatusBarManager");
                registerColorClickTopCallback(IColorClickTopCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            case 10004:
                data.enforceInterface("android.app.IStatusBarManager");
                this.mColorHandler.post(new Runnable() {
                    /* class com.android.server.statusbar.ColorStatusBarManagerService.AnonymousClass2 */

                    public void run() {
                        ColorStatusBarManagerService.this.notifyClickTop();
                    }
                });
                return true;
            case 10005:
                data.enforceInterface("android.app.IStatusBarManager");
                unregisterColorClickTopCallback(IColorClickTopCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            case 10006:
                data.enforceInterface("android.app.IStatusBarManager");
                boolean topIsFullscreen = getTopIsFullscreen();
                reply.writeNoException();
                reply.writeInt(topIsFullscreen ? 1 : 0);
                return true;
            case 10007:
                data.enforceInterface("android.app.IStatusBarManager");
                toggleSplitScreen(data.readInt());
                return true;
            case 10008:
                data.enforceInterface("android.app.IStatusBarManager");
                boolean statusBarFunction = setStatusBarFunction(data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeInt(statusBarFunction ? 1 : 0);
                return true;
            case 10009:
                data.enforceInterface("android.app.IStatusBarManager");
                topIsFullscreen(data.readInt() != 0);
                return true;
            case 10010:
                data.enforceInterface("android.app.IStatusBarManager");
                notifyMultiWindowFocusChanged(data.readInt());
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    @Override // com.android.server.statusbar.StatusBarManagerService
    public void expandNotificationsPanel() {
        super.expandNotificationsPanel();
        if (DEBUG_PANIC) {
            int uid = Binder.getCallingUid();
            String pkg = this.mContext.getPackageManager().getNameForUid(uid);
            Slog.v(TAG, " colorCollapsePanels: uid = " + uid + " pkg = " + pkg);
        }
    }

    @Override // com.android.server.statusbar.StatusBarManagerService
    public void disableForUser(int what, IBinder token, String pkg, int userId) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(47, Binder.getCallingUid());
        super.disableForUser(what, token, pkg, userId);
    }

    @Override // com.android.server.statusbar.StatusBarManagerService
    public void setIcon(String slot, String iconPackage, int iconId, int iconLevel, String contentDescription) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(46, Binder.getCallingUid());
        super.setIcon(slot, iconPackage, iconId, iconLevel, contentDescription);
    }

    @Override // com.android.server.statusbar.StatusBarManagerService
    public void setSystemUiVisibility(int displayId, int vis, int mask, String cause) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(48, Binder.getCallingUid());
        super.setSystemUiVisibility(displayId, vis, mask, cause);
    }

    @Override // com.android.server.statusbar.StatusBarManagerService
    public void collapsePanels() {
        super.collapsePanels();
        if (DEBUG_PANIC) {
            int uid = Binder.getCallingUid();
            String pkg = this.mContext.getPackageManager().getNameForUid(uid);
            Slog.v(TAG, " colorCollapsePanels: uid = " + uid + " pkg = " + pkg);
        }
    }

    /* access modifiers changed from: private */
    public void updateNavBarVisibility(int navBarVis) {
        IColorStatusBar iColorStatusBar = this.mColorStatusBar;
        if (iColorStatusBar != null) {
            try {
                iColorStatusBar.updateNavBarVisibility(navBarVis);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateNavBarVisibility(int navBarVis, String title) {
        IColorStatusBar iColorStatusBar = this.mColorStatusBar;
        if (iColorStatusBar != null) {
            try {
                iColorStatusBar.updateNavBarVisibilityWithPkg(navBarVis, title);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
