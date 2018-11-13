package com.android.server.wm;

import android.app.IColorKeyguardSessionCallback;
import android.app.IColorKeyguardSessionCallback.Stub;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.arp.OppoArpPeer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.MagnificationSpec;
import android.view.OppoWindowManagerPolicy;
import android.view.WindowManagerInternal;
import android.view.WindowManagerPolicy;
import com.android.server.LocalServices;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.input.InputManagerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import oppo.util.OppoStatistics;

public class OppoWindowManagerService extends WindowManagerService {
    private static final String QQ_PACKAGE_NAME = "com.tencent.mobileqq";
    private static final String TAG = "OppoWindowManagerService";
    private Context mContext;
    private final ColorLongshotWindowHelper mLongshotHelper;
    private OppoKeyguardListHelper mOppoKeyguardListHelper = OppoKeyguardListHelper.getInstance();

    OppoWindowManagerService(Context context, InputManagerService inputManager, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy) {
        super(context, inputManager, haveInputMethods, showBootMsgs, onlyCore, policy);
        this.mLongshotHelper = new ColorLongshotWindowHelper(context, this);
        this.mContext = context;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (this.mLongshotHelper.onTransact(code, data, reply, flags)) {
            return true;
        }
        boolean result;
        switch (code) {
            case 10010:
                data.enforceInterface("android.view.IWindowManager");
                result = isInputShow();
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 10011:
                data.enforceInterface("android.view.IWindowManager");
                result = isFullScreen();
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 10012:
                data.enforceInterface("android.view.IWindowManager");
                result = isStatusBarVisible();
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 10014:
                data.enforceInterface("android.view.IWindowManager");
                result = checkIsFloatWindowForbidden(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 10015:
                MagnificationSpec spec;
                data.enforceInterface("android.view.IWindowManager");
                if (data.readInt() != 0) {
                    spec = (MagnificationSpec) MagnificationSpec.CREATOR.createFromParcel(data);
                } else {
                    spec = null;
                }
                setMagnificationSpecEx(spec);
                reply.writeNoException();
                return true;
            case 10017:
                data.enforceInterface("android.view.IWindowManager");
                requestDismissKeyguard();
                reply.writeNoException();
                return true;
            case 10018:
                data.enforceInterface("android.view.IWindowManager");
                result = isWindowShownForUid(data.readInt());
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 10019:
                data.enforceInterface("android.view.IWindowManager");
                requestKeyguard(data.readString());
                reply.writeNoException();
                return true;
            case 10020:
                data.enforceInterface("android.view.IWindowManager");
                result = openKeyguardSession(Stub.asInterface(data.readStrongBinder()), data.readStrongBinder(), data.readString());
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 10022:
                data.enforceInterface("android.view.IWindowManager");
                removeWindowShownOnKeyguard();
                reply.writeNoException();
                return true;
            case 10025:
                data.enforceInterface("android.view.IWindowManager");
                String currentFocus = getCurrentFocus();
                reply.writeNoException();
                reply.writeString(currentFocus);
                return true;
            case 10030:
                data.enforceInterface("android.view.IWindowManager");
                Rect result2 = getFloatWindowRect(data.readInt());
                reply.writeNoException();
                if (result2 != null) {
                    reply.writeInt(1);
                    result2.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mLongshotHelper.setFileDescriptor(fd);
        super.dump(fd, pw, args);
        this.mLongshotHelper.setFileDescriptor(null);
    }

    boolean dumpWindows(PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        if (this.mLongshotHelper.dumpWindows(pw, name)) {
            return true;
        }
        return super.dumpWindows(pw, name, args, opti, dumpAll);
    }

    public boolean isInputShow() {
        if (this.mInputMethodWindow != null) {
            return this.mInputMethodWindow.mHasSurface;
        }
        return false;
    }

    public boolean isFullScreen() {
        if (this.mCurrentFocus != null) {
            String pids = getActiveAudioPids();
            if (pids == null || !pids.contains(Integer.toString(this.mCurrentFocus.mSession.mPid))) {
                return false;
            }
            WindowList list = this.mCurrentFocus.mChildren;
            if (list != null) {
                DisplayMetrics dm = this.mContext.getResources().getDisplayMetrics();
                int screenWidth = dm.widthPixels;
                int screenHeight = dm.heightPixels;
                if (list.size() <= 0) {
                    return this.mCurrentFocus.mHasSurface && this.mCurrentFocus.getContentFrameLw().width() == screenWidth && this.mCurrentFocus.getContentFrameLw().height() == screenHeight;
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        WindowState childWindow = (WindowState) list.get(i);
                        if (childWindow != null && childWindow.mHasSurface && childWindow.getContentFrameLw().width() == screenWidth && childWindow.getContentFrameLw().height() == screenHeight) {
                            return true;
                        }
                    }
                }
            }
        }
    }

    private String getActiveAudioPids() {
        String pids = ((AudioManager) this.mContext.getSystemService("audio")).getParameters("get_pid");
        if (pids == null || pids.length() == 0) {
            return null;
        }
        return pids;
    }

    public boolean checkIsFloatWindowForbidden(String packageName, int type) {
        return false;
    }

    public void setMagnificationSpecEx(MagnificationSpec spec) {
        if (spec != null) {
            ((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class)).setMagnificationSpec(spec);
        }
    }

    private static void convertCropForSurfaceFlinger(Rect crop, int rot, int dw, int dh) {
        int tmp;
        if (rot == 1) {
            tmp = crop.top;
            crop.top = dw - crop.right;
            crop.right = crop.bottom;
            crop.bottom = dw - crop.left;
            crop.left = tmp;
        } else if (rot == 2) {
            tmp = crop.top;
            crop.top = dh - crop.bottom;
            crop.bottom = dh - tmp;
            tmp = crop.right;
            crop.right = dw - crop.left;
            crop.left = dw - tmp;
        } else if (rot == 3) {
            tmp = crop.top;
            crop.top = crop.left;
            crop.left = dh - crop.bottom;
            crop.bottom = crop.right;
            crop.right = dh - tmp;
        }
    }

    public String getCurrentFocus() {
        return getFocusedWindowPkg();
    }

    public boolean isWindowShownForUid(int uid) {
        boolean isShown = false;
        ArrayList<WindowState> windows = new ArrayList();
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mRoot.forAllWindows((Consumer) new -$Lambda$_eaSO6hSZOKSnr04PN_UCoMXgDU((byte) 1, uid, windows), true);
                if (windows.size() > 0) {
                    isShown = true;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return isShown;
    }

    /* renamed from: lambda$-com_android_server_wm_OppoWindowManagerService_15013 */
    static /* synthetic */ void m206lambda$-com_android_server_wm_OppoWindowManagerService_15013(int uid, ArrayList windows, WindowState w) {
        if (w.mWinAnimator != null && w.mWinAnimator.getShown() && w.isVisible() && w.mOwnerUid == uid) {
            windows.add(w);
        }
    }

    public boolean isStatusBarVisible() {
        return ((OppoWindowManagerPolicy) this.mPolicy).isStatusBarVisible();
    }

    public void requestDismissKeyguard() {
        if (this.mPolicy != null) {
            ((OppoWindowManagerPolicy) this.mPolicy).requestDismissKeyguard();
        }
    }

    public boolean openKeyguardSession(IColorKeyguardSessionCallback callback, IBinder token, String module) {
        if (this.mPolicy != null) {
            return ((OppoWindowManagerPolicy) this.mPolicy).openKeyguardSession(callback, token, module);
        }
        return false;
    }

    public void requestKeyguard(String command) {
        if (this.mPolicy != null) {
            ((OppoWindowManagerPolicy) this.mPolicy).requestKeyguard(command);
        }
    }

    public void removeWindowShownOnKeyguard() {
        ArrayList<WindowState> mWaitingToRemove = new ArrayList();
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                int numDisplays = this.mRoot.getDisplayContents().size();
                for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    ((DisplayContent) this.mRoot.getDisplayContents().get(displayNdx)).forAllWindows((Consumer) new -$Lambda$8WJhgONAdZY2LTWXb_8Is2gNN3s((byte) 1, this, mWaitingToRemove), true);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (mWaitingToRemove.size() > 0) {
            for (WindowState w : mWaitingToRemove) {
                IBinder binder = w.mAppToken == null ? w.mToken == null ? null : w.mToken.token : w.mAppToken.token;
                if (binder != null) {
                    try {
                        boolean result = this.mActivityManager.finishActivity(binder, 0, null, 0);
                        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                            Slog.i(TAG, "removeWindowShownOnKeyguard finishActivity ok, result =" + result + ", w= " + w);
                        }
                    } catch (RemoteException e) {
                        Slog.w(TAG, "removeWindowShownOnKeyguard finishActivity fail, e=" + e.getMessage());
                    }
                }
            }
        }
    }

    /* renamed from: lambda$-com_android_server_wm_OppoWindowManagerService_17035 */
    /* synthetic */ void m208lambda$-com_android_server_wm_OppoWindowManagerService_17035(ArrayList mWaitingToRemove, WindowState w) {
        boolean appWindow = w.mAttrs.type >= 1 && w.mAttrs.type < OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT;
        if (appWindow && (w.isChildWindow() ^ 1) != 0 && (w.mAttrs.flags & DumpState.DUMP_FROZEN) != 0 && (isInKeyguardRemoveWhiteList(w.getOwningPackage()) ^ 1) != 0 && (isSystemApp(w.getOwningPackage()) ^ 1) != 0) {
            mWaitingToRemove.add(w);
            Map<String, String> staticEventMap = new HashMap();
            staticEventMap.put("pkgname", w.getOwningPackage());
            Slog.d(TAG, "remove window OwningPackage = " + w.getOwningPackage());
            OppoStatistics.onCommon(this.mContext, DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "keyguard_intercept_unlock_control", staticEventMap, false);
        }
    }

    public void disableKeyguard(IBinder token, String tag) {
        int i = 0;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        } else if (token == null) {
            throw new IllegalArgumentException("token == null");
        } else {
            if (!(this.mContext.getPackageManager().isFullFunctionMode() || (isSystemApp(Binder.getCallingUid()) ^ 1) == 0)) {
                if (tag != null) {
                    if (tag.equals("class com.zuimeia.suite.lockscreen.service.DaemonService")) {
                        i = 1;
                    } else {
                        i = tag.equals("IN");
                    }
                }
                if ((i ^ 1) != 0) {
                    if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                        Slog.d(TAG, "disableKeyguard return, tag =" + tag);
                    }
                    return;
                }
            }
            super.disableKeyguard(token, tag);
        }
    }

    private boolean isSystemApp(int uid) {
        if (uid < 10000) {
            if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                Slog.d(TAG, "isSystemApp system app, uid =" + uid);
            }
            return true;
        }
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
        if (packages == null) {
            if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                Slog.d(TAG, "isSystemApp getPackagesForUid = null, uid =" + uid);
            }
            return true;
        }
        for (int i = 0; i < packages.length; i++) {
            String packageName = packages[i];
            if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                Slog.d(TAG, "isSystemApp - i =" + i + ",  packages[i] = " + packageName);
            }
            if (isSystemApp(packageName)) {
                return true;
            }
        }
        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
            Slog.d(TAG, "isSystemApp end, return false, uid =" + uid);
        }
        return false;
    }

    private boolean isSystemApp(String packageName) {
        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
            Slog.d(TAG, "isSystemApp start, packageName =" + packageName);
        }
        if (packageName != null) {
            try {
                ApplicationInfo info = this.mContext.getPackageManager().getPackageInfo(packageName, 0).applicationInfo;
                if (!(info == null || (info.flags & 1) == 0)) {
                    if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                        Slog.d(TAG, "isSystemApp system app");
                    }
                    return true;
                }
            } catch (NameNotFoundException e) {
                if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                    Slog.d(TAG, "isSystemApp NameNotFoundException,  return false");
                }
                return false;
            }
        }
        return false;
    }

    private boolean isInKeyguardRemoveWhiteList(String packageName) {
        List<String> nameList = this.mOppoKeyguardListHelper.getSkipNameList();
        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
            Slog.d(TAG, "getSkipNameList =" + nameList);
        }
        if (packageName != null) {
            for (String pkg : nameList) {
                if (packageName.equalsIgnoreCase(pkg)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Rect getFloatWindowRect(int displayId) {
        Rect r;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                DisplayContent dc = getDefaultDisplayContentLocked();
                r = new Rect();
                if (dc != null) {
                    WindowState win = dc.getWindow(-$Lambda$6vWlz1P88lMkMG4g2BvX9ZYCNN0.$INST$3);
                    if (win != null) {
                        r = win.mVisibleFrame;
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return r;
    }

    /* renamed from: lambda$-com_android_server_wm_OppoWindowManagerService_22792 */
    static /* synthetic */ boolean m207lambda$-com_android_server_wm_OppoWindowManagerService_22792(WindowState w) {
        if (w.mHasSurface && w.mAppOpVisibility) {
            return 24 == w.mAppOp || 45 == w.mAppOp;
        } else {
            return false;
        }
    }
}
