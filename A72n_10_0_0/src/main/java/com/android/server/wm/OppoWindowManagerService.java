package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import android.view.DisplayCutout;
import android.view.IWindow;
import android.view.InputChannel;
import android.view.InsetsState;
import android.view.WindowManager;
import com.android.internal.statusbar.IStatusBarService;
import com.android.server.ColorServiceFactory;
import com.android.server.input.InputManagerService;
import com.android.server.policy.WindowManagerPolicy;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.color.util.ColorTypeCastingHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class OppoWindowManagerService extends WindowManagerService {
    private static final String QQ_PACKAGE_NAME = "com.tencent.mobileqq";
    private static final String TAG = "OppoWindowManagerService";
    private OppoBaseWindowManagerService mBase = ((OppoBaseWindowManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseWindowManagerService.class, this));
    private Context mContext;
    private boolean mGestureFollowAnimation = false;
    private IStatusBarService mStatusBarService;
    private IBinder mToken = new Binder();

    public OppoWindowManagerService(Context context, InputManagerService im, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy, ActivityTaskManagerService atm, OppoTransactionFactory transactionFactory) {
        super(context, im, showBootMsgs, onlyCore, policy, atm, transactionFactory.mTransactinFactory);
        this.mContext = context;
    }

    @Override // com.android.server.wm.WindowManagerService
    public void systemReady() {
        super.systemReady();
        OppoBaseWindowManagerService oppoBaseWindowManagerService = this.mBase;
        if (oppoBaseWindowManagerService != null) {
            if (oppoBaseWindowManagerService.mColorWmsEx != null) {
                this.mBase.mColorWmsEx.systemReady();
            }
            if (this.mBase.mPswWmsEx != null) {
                this.mBase.mPswWmsEx.systemReady();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseWindowManagerService
    public void handleOppoMessage(Message msg, int whichHandler) {
        OppoBaseWindowManagerService oppoBaseWindowManagerService = this.mBase;
        if (oppoBaseWindowManagerService != null) {
            if (oppoBaseWindowManagerService.mColorWmsEx != null) {
                this.mBase.mColorWmsEx.handleMessage(msg, whichHandler);
            }
            if (this.mBase.mPswWmsEx != null) {
                this.mBase.mPswWmsEx.handleMessage(msg, whichHandler);
            }
        }
    }

    @Override // com.android.server.wm.WindowManagerService
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (super.onTransact(code, data, reply, flags)) {
            return true;
        }
        OppoBaseWindowManagerService oppoBaseWindowManagerService = this.mBase;
        if (oppoBaseWindowManagerService == null) {
            return false;
        }
        if (oppoBaseWindowManagerService.mColorWmsEx != null && this.mBase.mColorWmsEx.onTransact(code, data, reply, flags)) {
            return true;
        }
        if (this.mBase.mPswWmsEx == null || !this.mBase.mPswWmsEx.onTransact(code, data, reply, flags)) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.wm.WindowManagerService
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        OppoBaseWindowManagerService oppoBaseWindowManagerService = this.mBase;
        if (oppoBaseWindowManagerService != null) {
            oppoBaseWindowManagerService.mColorWmsEx.setFileDescriptorForScreenShot(fd);
        }
        super.dump(fd, pw, args);
        OppoBaseWindowManagerService oppoBaseWindowManagerService2 = this.mBase;
        if (oppoBaseWindowManagerService2 != null) {
            oppoBaseWindowManagerService2.mColorWmsEx.setFileDescriptorForScreenShot(null);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowManagerService
    public boolean dumpWindows(PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        OppoBaseWindowManagerService oppoBaseWindowManagerService = this.mBase;
        if (oppoBaseWindowManagerService == null || !oppoBaseWindowManagerService.mColorWmsEx.dumpWindowsForScreenShot(pw, name, args)) {
            return super.dumpWindows(pw, name, args, opti, dumpAll);
        }
        return true;
    }

    public void disableKeyguard(IBinder token, String tag) {
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
        if (!WindowManagerDebugConfig.DEBUG_KEYGUARD) {
            return false;
        }
        Slog.d(TAG, "isSystemApp end, return false, uid =" + uid);
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
            } catch (PackageManager.NameNotFoundException e) {
                if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                    Slog.d(TAG, "isSystemApp NameNotFoundException,  return false");
                }
                return false;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseWindowManagerService
    public TaskPositioner createTaskPositioner(WindowManagerService service) {
        return new OppoTaskPositioner(service);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseWindowManagerService
    public WindowState createWindowState(WindowManagerService service, Session s, IWindow c, WindowToken token, WindowState parentWindow, int appOp, int seq, WindowManager.LayoutParams a, int viewVisibility, int ownerId, boolean ownerCanAddInternalSystemWindow) {
        return new OppoWindowState(service, s, c, token, parentWindow, appOp, seq, a, viewVisibility, ownerId, ownerCanAddInternalSystemWindow);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseWindowManagerService
    public TaskStack createTaskStack(WindowManagerService service, int stackId, ActivityStack stack) {
        return new OppoTaskStack(service, stackId, stack);
    }

    @Override // com.android.server.wm.WindowManagerService
    public void setAnimationScale(int which, float scale) {
        OppoBaseWindowManagerService oppoBaseWindowManagerService;
        if (!(!ColorServiceFactory.getInstance().getColorEyeProtectManager().needResetAnimationScaleSetting(this.mContext, -2) || (oppoBaseWindowManagerService = this.mBase) == null || oppoBaseWindowManagerService.mColorWmsInner == null)) {
            this.mBase.mColorWmsInner.resetAnimationSetting();
        }
        super.setAnimationScale(which, scale);
    }

    @Override // com.android.server.wm.WindowManagerService
    public int addWindow(Session session, IWindow client, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int displayId, Rect outFrame, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, DisplayCutout.ParcelableWrapper outDisplayCutout, InputChannel outInputChannel, InsetsState outInsetsState) {
        if (attrs != null) {
            OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction_addWindow(82, Binder.getCallingUid(), attrs.type);
        }
        return super.addWindow(session, client, seq, attrs, viewVisibility, displayId, outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout, outInputChannel, outInsetsState);
    }

    @Override // com.android.server.wm.WindowManagerService
    public void disableKeyguard(IBinder token, String tag, int userId) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(71, Binder.getCallingUid());
        super.disableKeyguard(token, tag, userId);
    }

    @Override // com.android.server.wm.WindowManagerService
    public void reenableKeyguard(IBinder token, int userId) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(72, Binder.getCallingUid());
        super.reenableKeyguard(token, userId);
    }

    @Override // com.android.server.wm.OppoBaseWindowManagerService
    public void handleUiModeChanged() {
        if (OppoBaseActivityTaskManagerService.mUiModeChanged) {
            OppoBaseWindowManagerService oppoBaseWindowManagerService = this.mBase;
            if (!(oppoBaseWindowManagerService == null || oppoBaseWindowManagerService.mColorWmsEx == null)) {
                this.mBase.mColorWmsEx.notifyDarkModeListener();
            }
            OppoBaseActivityTaskManagerService.mUiModeChanged = false;
        }
    }

    private synchronized IStatusBarService getStatusBarService() {
        if (this.mStatusBarService == null) {
            this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
            if (this.mStatusBarService == null) {
                Slog.w(TAG, "mStatusBarService is null");
            }
        }
        return this.mStatusBarService;
    }

    @Override // com.android.server.wm.WindowManagerService
    public void disableStatusBar(boolean disable) {
        int state = 0;
        if (disable) {
            state = 0 | 65536;
        }
        Slog.d(TAG, "disableStatusBar state: " + state + " , disable: " + disable);
        disableStatusBarLocked(state);
    }

    private void disableStatusBarLocked(int state) {
        try {
            IStatusBarService service = getStatusBarService();
            if (service != null) {
                service.disable(state, this.mToken, this.mContext.getPackageName());
            }
        } catch (Exception e) {
            Slog.d(TAG, "disableStatusBar error");
        }
    }

    @Override // com.android.server.wm.OppoBaseWindowManagerService
    public void startFreezingScreenForUserSwitch(int exitAnim, int enterAnim, int freezeMillis) {
        if (checkCallingPermission("android.permission.FREEZE_SCREEN", "startFreezingScreen()")) {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (!this.mClientFreezingScreen) {
                        this.mClientFreezingScreen = true;
                        long origId = Binder.clearCallingIdentity();
                        try {
                            startFreezingDisplayLocked(exitAnim, enterAnim);
                            this.mH.removeMessages(30);
                            this.mH.sendEmptyMessageDelayed(30, (long) freezeMillis);
                        } finally {
                            Binder.restoreCallingIdentity(origId);
                        }
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Requires FREEZE_SCREEN permission");
    }
}
