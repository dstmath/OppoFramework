package com.android.server.wm;

import android.app.IColorKeyguardSessionCallback;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.ColorBaseLayoutParams;
import android.view.IColorWindowManager;
import android.view.IOppoWindowStateObserver;
import android.view.InputChannel;
import android.view.MagnificationSpec;
import android.view.WindowManager;
import com.android.server.LocalServices;
import com.android.server.am.ColorMultiAppManagerService;
import com.color.darkmode.IColorDarkModeListener;
import com.color.util.ColorNavigationBarUtil;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;
import java.util.function.Consumer;

public final class ColorWindowManagerServiceTransactionHelper extends ColorWindowManagerServiceCommonHelper implements IColorWindowManager {
    private static final String TAG = "ColorWindowManagerServiceTransactionHelper";
    private final int FLAG_IGNORE_HOME = 1;
    private final int FLAG_IGNORE_TASK = 2;
    private final Context mContext;
    private boolean mGestureFollowAnimation = false;
    private int mSplitTimeout = -1;

    public ColorWindowManagerServiceTransactionHelper(Context context, IColorWindowManagerServiceEx wmsEx) {
        super(context, wmsEx);
        this.mContext = context;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        MagnificationSpec spec;
        Bundle _arg3;
        if (code == 10014) {
            data.enforceInterface("android.view.IWindowManager");
            boolean checkIsFloatWindowForbidden = checkIsFloatWindowForbidden(data.readString(), data.readInt());
            reply.writeNoException();
            reply.writeInt(checkIsFloatWindowForbidden ? 1 : 0);
            return true;
        } else if (code == 10015) {
            data.enforceInterface("android.view.IWindowManager");
            if (data.readInt() != 0) {
                spec = (MagnificationSpec) MagnificationSpec.CREATOR.createFromParcel(data);
            } else {
                spec = null;
            }
            setMagnificationSpecEx(spec);
            reply.writeNoException();
            return true;
        } else if (code == 10022) {
            data.enforceInterface("android.view.IWindowManager");
            removeWindowShownOnKeyguard();
            reply.writeNoException();
            return true;
        } else if (code != 10025) {
            boolean z = false;
            boolean z2 = false;
            if (code == 10030) {
                data.enforceInterface("android.view.IWindowManager");
                Rect result = getFloatWindowRect(data.readInt());
                reply.writeNoException();
                if (result != null) {
                    reply.writeInt(1);
                    result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code != 10036) {
                switch (code) {
                    case 10010:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean isInputShow = isInputShow();
                        reply.writeNoException();
                        reply.writeInt(isInputShow ? 1 : 0);
                        return true;
                    case 10011:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean isFullScreen = isFullScreen();
                        reply.writeNoException();
                        reply.writeInt(isFullScreen ? 1 : 0);
                        return true;
                    case 10012:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean isStatusBarVisible = isStatusBarVisible();
                        reply.writeNoException();
                        reply.writeInt(isStatusBarVisible ? 1 : 0);
                        return true;
                    default:
                        switch (code) {
                            case 10017:
                                data.enforceInterface("android.view.IWindowManager");
                                requestDismissKeyguard();
                                reply.writeNoException();
                                return true;
                            case 10018:
                                data.enforceInterface("android.view.IWindowManager");
                                boolean isWindowShownForUid = isWindowShownForUid(data.readInt());
                                reply.writeNoException();
                                reply.writeInt(isWindowShownForUid ? 1 : 0);
                                return true;
                            case 10019:
                                data.enforceInterface("android.view.IWindowManager");
                                requestKeyguard(data.readString());
                                reply.writeNoException();
                                return true;
                            case 10020:
                                data.enforceInterface("android.view.IWindowManager");
                                boolean openKeyguardSession = openKeyguardSession(IColorKeyguardSessionCallback.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder(), data.readString());
                                reply.writeNoException();
                                reply.writeInt(openKeyguardSession ? 1 : 0);
                                return true;
                            default:
                                switch (code) {
                                    case 10040:
                                        data.enforceInterface("android.view.IWindowManager");
                                        IBinder _arg0 = data.readStrongBinder();
                                        String _arg1 = data.readString();
                                        InputChannel _arg2 = new InputChannel();
                                        createMonitorInputConsumer(_arg0, _arg1, _arg2);
                                        reply.writeNoException();
                                        reply.writeInt(1);
                                        _arg2.writeToParcel(reply, 1);
                                        return true;
                                    case 10041:
                                        data.enforceInterface("android.view.IWindowManager");
                                        boolean destroyMonitorInputConsumer = destroyMonitorInputConsumer(data.readString());
                                        reply.writeNoException();
                                        reply.writeInt(destroyMonitorInputConsumer ? 1 : 0);
                                        return true;
                                    case 10042:
                                        data.enforceInterface("android.view.IWindowManager");
                                        if (data.readInt() == 1) {
                                            z = true;
                                        }
                                        setGestureFollowAnimation(z);
                                        return true;
                                    default:
                                        switch (code) {
                                            case 10046:
                                                data.enforceInterface("android.view.IWindowManager");
                                                String _arg02 = data.readString();
                                                int _arg12 = data.readInt();
                                                int _arg22 = data.readInt();
                                                if (data.readInt() != 0) {
                                                    _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                                                } else {
                                                    _arg3 = null;
                                                }
                                                startColorDragWindow(_arg02, _arg12, _arg22, _arg3);
                                                reply.writeNoException();
                                                return true;
                                            case 10047:
                                                data.enforceInterface("android.view.IWindowManager");
                                                registerOppoWindowStateObserver(IOppoWindowStateObserver.Stub.asInterface(data.readStrongBinder()));
                                                reply.writeNoException();
                                                return true;
                                            case 10048:
                                                data.enforceInterface("android.view.IWindowManager");
                                                unregisterOppoWindowStateObserver(IOppoWindowStateObserver.Stub.asInterface(data.readStrongBinder()));
                                                reply.writeNoException();
                                                return true;
                                            case 10049:
                                                data.enforceInterface("android.view.IWindowManager");
                                                boolean isInFreeformMode = isInFreeformMode();
                                                reply.writeNoException();
                                                reply.writeInt(isInFreeformMode ? 1 : 0);
                                                return true;
                                            case 10050:
                                                data.enforceInterface("android.view.IWindowManager");
                                                Rect _arg03 = new Rect();
                                                getFreeformStackBounds(_arg03);
                                                reply.writeNoException();
                                                reply.writeInt(1);
                                                _arg03.writeToParcel(reply, 1);
                                                return true;
                                            case 10051:
                                                data.enforceInterface("android.view.IWindowManager");
                                                boolean isActivityNeedPalette = isActivityNeedPalette(data.readString(), data.readString());
                                                reply.writeNoException();
                                                reply.writeInt(isActivityNeedPalette ? 1 : 0);
                                                return true;
                                            case 10052:
                                                data.enforceInterface("android.view.IWindowManager");
                                                int result2 = getNavBarColorFromAdaptation(data.readString(), data.readString());
                                                reply.writeNoException();
                                                reply.writeInt(result2);
                                                return true;
                                            case 10053:
                                                data.enforceInterface("android.view.IWindowManager");
                                                int result3 = getStatusBarColorFromAdaptation(data.readString(), data.readString());
                                                reply.writeNoException();
                                                reply.writeInt(result3);
                                                return true;
                                            case 10054:
                                                data.enforceInterface("android.view.IWindowManager");
                                                int result4 = getImeBgColorFromAdaptation(data.readString());
                                                reply.writeNoException();
                                                reply.writeInt(result4);
                                                return true;
                                            case 10055:
                                                data.enforceInterface("android.view.IWindowManager");
                                                int result5 = getTypedWindowLayer(data.readInt());
                                                reply.writeNoException();
                                                reply.writeInt(result5);
                                                return true;
                                            case 10056:
                                                data.enforceInterface("android.view.IWindowManager");
                                                int result6 = getFocusedWindowIgnoreHomeMenuKey();
                                                reply.writeNoException();
                                                reply.writeInt(result6);
                                                return true;
                                            case 10057:
                                                data.enforceInterface("android.view.IWindowManager");
                                                registerOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener.Stub.asInterface(data.readStrongBinder()));
                                                reply.writeNoException();
                                                return true;
                                            case 10058:
                                                data.enforceInterface("android.view.IWindowManager");
                                                unregisterOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener.Stub.asInterface(data.readStrongBinder()));
                                                reply.writeNoException();
                                                return true;
                                            case 10059:
                                                data.enforceInterface("android.view.IWindowManager");
                                                if (1 == data.readInt()) {
                                                    z2 = true;
                                                }
                                                setBootAnimationRotationLock(z2);
                                                reply.writeNoException();
                                                return true;
                                            default:
                                                return false;
                                        }
                                }
                        }
                }
            } else {
                data.enforceInterface("android.view.IWindowManager");
                setSplitTimeout(data.readInt());
                return true;
            }
        } else {
            data.enforceInterface("android.view.IWindowManager");
            String currentFocus = getCurrentFocus();
            reply.writeNoException();
            reply.writeString(currentFocus);
            return true;
        }
    }

    public boolean isLockWndShow() throws RemoteException {
        return false;
    }

    public void keyguardSetApkLockScreenShowing(boolean showing) throws RemoteException {
    }

    public IBinder getApkUnlockWindow() throws RemoteException {
        return null;
    }

    public void keyguardShowSecureApkLock(boolean show) throws RemoteException {
    }

    public boolean isLockOnShow() throws RemoteException {
        return false;
    }

    public boolean isSIMUnlockRunning() throws RemoteException {
        return false;
    }

    public boolean isInputShow() {
        if (this.mWms.getDefaultDisplayContentLocked().mInputMethodWindow != null) {
            return this.mWms.getDefaultDisplayContentLocked().mInputMethodWindow.mHasSurface;
        }
        return false;
    }

    public boolean isFullScreen() {
        String pids;
        WindowList list;
        if (!(this.mWms.getDefaultDisplayContentLocked().mCurrentFocus == null || (pids = getActiveAudioPids()) == null || !pids.contains(Integer.toString(this.mWms.getDefaultDisplayContentLocked().mCurrentFocus.mSession.mPid)) || (list = this.mWms.getDefaultDisplayContentLocked().mCurrentFocus.mChildren) == null)) {
            DisplayMetrics dm = this.mContext.getResources().getDisplayMetrics();
            int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    WindowState childWindow = (WindowState) list.get(i);
                    if (childWindow != null && childWindow.mHasSurface && childWindow.getContentFrameLw().width() == screenWidth && childWindow.getContentFrameLw().height() == screenHeight) {
                        return true;
                    }
                }
            } else if (this.mWms.getDefaultDisplayContentLocked().mCurrentFocus.mHasSurface && this.mWms.getDefaultDisplayContentLocked().mCurrentFocus.getContentFrameLw().width() == screenWidth && this.mWms.getDefaultDisplayContentLocked().mCurrentFocus.getContentFrameLw().height() == screenHeight) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private String getActiveAudioPids() {
        String pids = ((AudioManager) this.mContext.getSystemService("audio")).getParameters("get_pid");
        if (pids == null || pids.length() == 0) {
            return null;
        }
        return pids;
    }

    public boolean isStatusBarVisible() {
        return this.mWms.mPolicy.isStatusBarVisible();
    }

    public boolean isRotatingLw() throws RemoteException {
        return false;
    }

    public void setMagnification(Bundle bundle) throws RemoteException {
    }

    public void setMagnificationSpecEx(MagnificationSpec spec) {
        if (spec != null) {
            ((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class)).setMagnificationSpec(0, spec);
        }
    }

    public String getCurrentFocus() {
        WindowState focusWindow;
        OppoBaseWindowManagerService baseWms = typeCasting(this.mWms);
        if (baseWms == null || baseWms.mColorWmsInner == null || (focusWindow = baseWms.mColorWmsInner.getFocusedWindow()) == null) {
            return "";
        }
        return focusWindow.getOwningPackage();
    }

    public void requestKeyguard(String command) {
        if (this.mWms.mPolicy != null) {
            this.mWms.mPolicy.requestKeyguard(command);
        }
    }

    public boolean openKeyguardSession(IColorKeyguardSessionCallback callback, IBinder token, String module) {
        if (this.mWms.mPolicy != null) {
            return this.mWms.mPolicy.openKeyguardSession(callback, token, module);
        }
        return false;
    }

    public void removeWindowShownOnKeyguard() {
    }

    public void requestDismissKeyguard() {
        if (this.mWms.mPolicy != null) {
            this.mWms.mPolicy.requestDismissKeyguard();
        }
    }

    public Rect getFloatWindowRect(int displayId) {
        Rect r;
        WindowState win;
        synchronized (this.mWms.mGlobalLock) {
            DisplayContent dc = this.mWms.getDefaultDisplayContentLocked();
            r = new Rect();
            if (!(dc == null || (win = dc.getWindow($$Lambda$ColorWindowManagerServiceTransactionHelper$5v168abYjn6g7UVUzGdN6CmCs.INSTANCE)) == null)) {
                Slog.d(TAG, "get for tel win = " + win);
                r = win.getWindowFrames().mFrame;
            }
        }
        return r;
    }

    static /* synthetic */ boolean lambda$getFloatWindowRect$0(WindowState w) {
        return w.mHasSurface && getColorWindowStateInner(w).getAppOpVisibility() && (24 == w.mAppOp || 45 == w.mAppOp);
    }

    public boolean isWindowShownForUid(int uid) {
        boolean isShown = false;
        ArrayList<WindowState> windows = new ArrayList<>();
        synchronized (this.mWms.mGlobalLock) {
            this.mWms.mRoot.forAllWindows(new Consumer(uid, windows) {
                /* class com.android.server.wm.$$Lambda$ColorWindowManagerServiceTransactionHelper$h8VG94hBZd0qUy0BXgmR_RKn5gg */
                private final /* synthetic */ int f$0;
                private final /* synthetic */ ArrayList f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ColorWindowManagerServiceTransactionHelper.lambda$isWindowShownForUid$1(this.f$0, this.f$1, (WindowState) obj);
                }
            }, true);
            if (windows.size() > 0) {
                isShown = true;
            }
        }
        return isShown;
    }

    static /* synthetic */ void lambda$isWindowShownForUid$1(int uid, ArrayList windows, WindowState w) {
        if (w.mWinAnimator != null && w.mWinAnimator.getShown() && w.isVisible() && w.mOwnerUid == uid) {
            windows.add(w);
        }
    }

    public boolean checkIsFloatWindowForbidden(String packageName, int type) {
        return false;
    }

    public void setSplitTimeout(int timeout) {
        if (this.mWms.checkCallingPermission("android.permission.FREEZE_SCREEN", "setSplitTimeout()")) {
            this.mSplitTimeout = timeout;
            return;
        }
        throw new SecurityException("Requires FREEZE_SCREEN permission");
    }

    public void setGestureFollowAnimation(boolean animation) {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.i(TAG, "setGestureFollowAnimation  animation " + animation);
        }
        this.mGestureFollowAnimation = animation;
    }

    public boolean isGestureFollowAnimation() {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.i(TAG, "isGestureFollowAnimation  mGestureFollowAnimation " + this.mGestureFollowAnimation);
        }
        return this.mGestureFollowAnimation;
    }

    public void createMonitorInputConsumer(IBinder token, String name, InputChannel inputChannel) {
        this.mColorWmsEx.createMonitorInputConsumer(this.mWms, token, name, inputChannel);
    }

    public boolean destroyMonitorInputConsumer(String name) {
        return this.mColorWmsEx.destroyMonitorInputConsumer(name);
    }

    public void startColorDragWindow(String packageName, int resId, int mode, Bundle options) {
        if (this.mWms.checkCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "startColorDragWindow")) {
            OppoFeatureCache.get(IColorBreenoManager.DEFAULT).startColorDragWindow(packageName, resId, mode, options);
            return;
        }
        throw new SecurityException("startColorDragWindow Requires OPPO_COMPONENT_SAFE permission");
    }

    public void registerOppoWindowStateObserver(IOppoWindowStateObserver observer) {
        OppoBaseWindowManagerService baseWms = typeCasting(this.mWms);
        if (baseWms != null && baseWms.mColorWmsEx != null) {
            baseWms.mColorWmsEx.registerOppoWindowStateObserver(observer);
        }
    }

    public void unregisterOppoWindowStateObserver(IOppoWindowStateObserver observer) {
        OppoBaseWindowManagerService baseWms = typeCasting(this.mWms);
        if (baseWms != null && baseWms.mColorWmsEx != null) {
            baseWms.mColorWmsEx.unregisterOppoWindowStateObserver(observer);
        }
    }

    public boolean isInFreeformMode() {
        return false;
    }

    public void getFreeformStackBounds(Rect outBounds) {
        if (outBounds != null) {
            outBounds.setEmpty();
        }
    }

    public boolean isActivityNeedPalette(String pkg, String activityName) {
        if (this.mWms.mSystemReady) {
            return ColorNavigationBarUtil.getInstance().isActivityNeedPalette(pkg, activityName);
        }
        return false;
    }

    public int getNavBarColorFromAdaptation(String pkg, String activityName) {
        if (this.mWms.mSystemReady) {
            return ColorNavigationBarUtil.getInstance().getNavBarColorFromAdaptation(pkg, activityName);
        }
        return 0;
    }

    public int getStatusBarColorFromAdaptation(String pkg, String activityName) {
        if (this.mWms.mSystemReady) {
            return ColorNavigationBarUtil.getInstance().getStatusBarColorFromAdaptation(pkg, activityName);
        }
        return 0;
    }

    public int getImeBgColorFromAdaptation(String pkg) {
        if (this.mWms.mSystemReady) {
            return ColorNavigationBarUtil.getInstance().getImeBgColorFromAdaptation(pkg);
        }
        return 0;
    }

    public int getTypedWindowLayer(int type) {
        return 0;
    }

    public int getFocusedWindowIgnoreHomeMenuKey() {
        ColorBaseLayoutParams baseLayoutParams;
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            return getCustomFocusedWindowIgnoreHomeMenuKey();
        }
        WindowState currentFocus = this.mWms.getDefaultDisplayContentLocked().mCurrentFocus;
        if (!(currentFocus == null || (baseLayoutParams = typeCasting(currentFocus.getAttrs())) == null)) {
            if (baseLayoutParams.ignoreHomeMenuKey == 1 || (currentFocus.getAttrs().memoryType & 16777216) != 0) {
                return 1;
            }
            if (baseLayoutParams.ignoreHomeMenuKey == 2 || (currentFocus.getAttrs().memoryType & 33554432) != 0) {
                return 2;
            }
            if (baseLayoutParams.ignoreHomeMenuKey == 3 || (currentFocus.getAttrs().memoryType & ColorMultiAppManagerService.FLAG_MULTI_APP) != 0) {
                return 3;
            }
        }
        int colorKeyMode = ((WindowManagerService) this.mWms).mPolicy.getColorKeyMode();
        if (colorKeyMode != 2 && colorKeyMode != 3) {
            return 0;
        }
        Slog.d(TAG, "getFocusedWindowIgnoreHomeMenuKey colorKeyMode " + colorKeyMode);
        return 1;
    }

    private int getCustomFocusedWindowIgnoreHomeMenuKey() {
        int ignoreHomeTask;
        int flagIgnoreHomeTask = 0;
        WindowState currentFocus = this.mWms.getDefaultDisplayContentLocked().mCurrentFocus;
        if (currentFocus != null) {
            if (currentFocus.getAttrs().ignoreHomeMenuKey == 1 || (currentFocus.getAttrs().memoryType & 16777216) != 0) {
                return 1;
            }
            if (currentFocus.getAttrs().ignoreHomeMenuKey == 2 || (currentFocus.getAttrs().memoryType & 33554432) != 0) {
                flagIgnoreHomeTask = 0 | 1;
            } else if (currentFocus.getAttrs().ignoreHomeMenuKey == 3 || (currentFocus.getAttrs().memoryType & ColorMultiAppManagerService.FLAG_MULTI_APP) != 0) {
                flagIgnoreHomeTask = 0 | 2;
            }
        }
        int colorKeyMode = this.mWms.mPolicy.getColorKeyMode();
        if (colorKeyMode == 2 || colorKeyMode == 3) {
            Slog.d(TAG, "getFocusedWindowIgnoreHomeMenuKeyCustom colorKeyMode " + colorKeyMode);
            return 1;
        }
        int flagIgnoreHomeTask2 = flagIgnoreHomeTask | getCustomIngoreHomeTaskKey();
        if ((flagIgnoreHomeTask2 & 1) != 0 && (flagIgnoreHomeTask2 & 2) != 0) {
            ignoreHomeTask = 1;
        } else if ((flagIgnoreHomeTask2 & 1) != 0) {
            ignoreHomeTask = 2;
        } else if ((flagIgnoreHomeTask2 & 2) != 0) {
            ignoreHomeTask = 3;
        } else {
            ignoreHomeTask = 0;
        }
        Slog.d(TAG, "getFocusedWindowIgnoreHomeMenuKeyCustom: flagIgnoreHomeTask=" + flagIgnoreHomeTask2 + ", ignoreHomeTask=" + ignoreHomeTask);
        return ignoreHomeTask;
    }

    private int getCustomIngoreHomeTaskKey() {
        if (!this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            return 0;
        }
        int flagIgnoreHomeTask = 0;
        boolean isCustomHomeDisabled = SystemProperties.getBoolean("persist.sys.custom_home_disable", false);
        boolean isCustomTaskDisabled = SystemProperties.getBoolean("persist.sys.custom_task_disable", false);
        if (isCustomHomeDisabled) {
            flagIgnoreHomeTask = 0 | 1;
        } else if (isCustomTaskDisabled) {
            flagIgnoreHomeTask = 0 | 2;
        }
        Slog.d(TAG, "getCustomIngoreHomeTaskKey: flagIgnoreHomeTask=" + flagIgnoreHomeTask);
        return flagIgnoreHomeTask;
    }

    public void registerOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) {
        OppoBaseWindowManagerService baseWms = typeCasting(this.mWms);
        if (baseWms != null && baseWms.mColorWmsEx != null) {
            baseWms.mColorWmsEx.registerOnUiModeConfigurationChangeFinishListener(listener);
        }
    }

    public void unregisterOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) {
        OppoBaseWindowManagerService baseWms = typeCasting(this.mWms);
        if (baseWms != null && baseWms.mColorWmsEx != null) {
            baseWms.mColorWmsEx.unregisterOnUiModeConfigurationChangeFinishListener(listener);
        }
    }

    public void setBootAnimationRotationLock(boolean lockRotation) {
        if (this.mBaseWms != null) {
            this.mBaseWms.setRotationLockForBootAnimation(lockRotation);
        } else {
            Slog.e(TAG, "setRotationLockForBootAnimation failed.");
        }
    }

    @Override // com.android.server.wm.ColorWindowManagerServiceCommonHelper
    private OppoBaseWindowManagerService typeCasting(WindowManagerService wms) {
        if (wms != null) {
            return (OppoBaseWindowManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseWindowManagerService.class, wms);
        }
        return null;
    }

    private ColorBaseLayoutParams typeCasting(WindowManager.LayoutParams lp) {
        if (lp != null) {
            return (ColorBaseLayoutParams) ColorTypeCastingHelper.typeCasting(ColorBaseLayoutParams.class, lp);
        }
        return null;
    }

    private static IColorWindowStateInner getColorWindowStateInner(WindowState win) {
        OppoBaseWindowState baseWindowState = (OppoBaseWindowState) ColorTypeCastingHelper.typeCasting(OppoBaseWindowState.class, win);
        if (baseWindowState != null) {
            return baseWindowState.mColorWindowStateInner;
        }
        return IColorWindowStateInner.DEFAULT;
    }
}
