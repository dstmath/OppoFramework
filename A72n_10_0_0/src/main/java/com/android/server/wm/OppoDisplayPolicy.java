package com.android.server.wm;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.ColorBaseLayoutParams;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import com.android.internal.util.ScreenshotHelper;
import com.android.server.am.ColorMultiAppManagerService;
import com.android.server.am.ColorResourcePreloadDatabaseHelper;
import com.android.server.policy.ColorLongshotPolicy;
import com.android.server.policy.OppoPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.color.screenshot.IColorScreenshotHelper;
import com.color.util.ColorDisplayCompatUtils;
import com.color.util.ColorTypeCastingHelper;
import com.color.view.ColorWindowManager;
import java.util.List;

public class OppoDisplayPolicy extends DisplayPolicy {
    private static final int CUTOUT_MODE_DEFAULT = 0;
    private static final int CUTOUT_MODE_HIDE = 2;
    private static final int CUTOUT_MODE_SHOW = 1;
    private static final String TAG = "OppoDisplayPolicy";
    private static ColorDisplayCompatUtils mDisplayCompatUtils = null;
    private OppoBaseDisplayPolicy mBase = typeCasting((DisplayPolicy) this);
    WindowState mColorFullScreenDisplay = null;
    WindowState mColorFullScreenDisplayLand = null;
    private View mColorFullScreenWindow = null;
    private View mColorFullScreenWindowLand = null;
    private View mColorSagAreaView = null;
    private Context mContext = null;
    private int mFloatAssistantDirection = 5;
    private String mFoucsPackage = null;
    private AlertDialog mFullscreenDialog;
    private AlertDialog mFullscreenDialogLand;
    private BroadcastReceiver mFullscreenDialogReceiver = new BroadcastReceiver() {
        /* class com.android.server.wm.OppoDisplayPolicy.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                if (OppoDisplayPolicy.this.mFullscreenDialogLand != null) {
                    OppoDisplayPolicy.this.mFullscreenDialogLand.dismiss();
                    OppoDisplayPolicy.this.mFullscreenDialogLand = null;
                }
                if (OppoDisplayPolicy.this.mFullscreenDialog != null) {
                    OppoDisplayPolicy.this.mFullscreenDialog.dismiss();
                    OppoDisplayPolicy.this.mFullscreenDialog = null;
                }
            }
        }
    };
    private int mHHHRotation = 0;
    private View mHchildLeft = null;
    private View mHchildRight = null;
    private boolean mIsVisibility = false;
    ColorInputMethodKeyboardPositionManager mKeyboardPositionManager = null;
    private LinearLayout.LayoutParams mLayoutParams = null;
    ColorLongshotPolicy mLongshotPolicy = new ColorLongshotPolicy();
    private PowerManager mPm = null;
    private int mRotation = 0;
    private String mScreenshotIntentDirectionExtra = "direction";
    private WindowManagerService mService = null;
    private int mThreeFingersPressDirection = 6;
    private int mUid = ColorFreeformManagerService.FREEFORM_CALLER_UID;
    private View mVchild = null;

    public OppoDisplayPolicy(WindowManagerService service, DisplayContent displayContent) {
        super(service, displayContent);
        this.mService = service;
        OppoBaseDisplayPolicy oppoBaseDisplayPolicy = this.mBase;
        if (oppoBaseDisplayPolicy != null) {
            this.mContext = oppoBaseDisplayPolicy.mColorDpInner.getContext();
            this.mLongshotPolicy.init(this.mContext);
            mDisplayCompatUtils = ColorDisplayCompatUtils.getInstance();
            this.mKeyboardPositionManager = ColorInputMethodKeyboardPositionManager.getInstance(this.mContext);
            this.mKeyboardPositionManager.addWindowManagerService(service);
        }
    }

    public boolean isGlobalActionVisible() {
        WindowManagerService windowManagerService = this.mService;
        WindowManagerPolicy policy = windowManagerService != null ? windowManagerService.mPolicy : null;
        if (policy == null || !(policy instanceof OppoPhoneWindowManager)) {
            return false;
        }
        return ((OppoPhoneWindowManager) policy).isGlobalActionVisible();
    }

    public boolean isNavigationBarVisible() {
        OppoBaseDisplayPolicy oppoBaseDisplayPolicy = this.mBase;
        if (oppoBaseDisplayPolicy == null || oppoBaseDisplayPolicy.mColorDpInner == null || this.mBase.mColorDpInner.getNavigationBar() == null) {
            return false;
        }
        return this.mBase.mColorDpInner.getNavigationBar().isVisibleLw();
    }

    public boolean isShortcutsPanelShow() {
        return this.mLongshotPolicy.isShortcutsPanelShow();
    }

    public boolean isVolumeShow() {
        return this.mLongshotPolicy.isVolumeShow();
    }

    public boolean isFloatAssistExpand() {
        return this.mLongshotPolicy.isFloatAssistExpand();
    }

    public boolean isEdgePanelExpand() {
        return this.mLongshotPolicy.isEdgePanelExpand();
    }

    public boolean isSystemUiVisibility() {
        OppoBaseDisplayPolicy oppoBaseDisplayPolicy = this.mBase;
        if (oppoBaseDisplayPolicy == null || oppoBaseDisplayPolicy.mColorDpInner == null || this.mBase.mColorDpInner.getStatusBar() == null) {
            return false;
        }
        return this.mBase.mColorDpInner.getStatusBar().isVisibleLw();
    }

    public void updateKeyboardPosition() {
        ColorInputMethodKeyboardPositionManager colorInputMethodKeyboardPositionManager = this.mKeyboardPositionManager;
        if (colorInputMethodKeyboardPositionManager != null) {
            colorInputMethodKeyboardPositionManager.updateKeyboardPosition();
        }
    }

    /* access modifiers changed from: package-private */
    public Handler createPolicyHandler(Looper looper) {
        return ((OppoBaseDisplayPolicy) ColorTypeCastingHelper.typeCasting(OppoBaseDisplayPolicy.class, this)).createPolicyHandlerWrapper(looper);
    }

    /* access modifiers changed from: package-private */
    public void adjustOppoWindowFrame(Rect pf, Rect df, Rect of, Rect cf, Rect dcf, Rect vf, WindowManager.LayoutParams attrs, DisplayFrames displayFrames) {
        ColorBaseLayoutParams baseLp = typeCasting(attrs);
        if (this.mKeyboardPositionManager != null && attrs.type == 2011) {
            this.mKeyboardPositionManager.updateInputMethodPaddingBottom(cf, vf, displayFrames);
        }
        if (ColorWindowManager.LayoutParams.isForceFullScreen(attrs.type)) {
            dcf.set(displayFrames.mOverscan);
            pf.set(displayFrames.mOverscan);
            df.set(displayFrames.mOverscan);
            of.set(displayFrames.mOverscan);
            cf.set(displayFrames.mOverscan);
        } else if (attrs.type >= 300) {
            boolean hasNavigationBar = false;
            boolean hasStatusBar = baseLp != null ? baseLp.mColorLayoutParams.hasStatusBar() : false;
            if (baseLp != null) {
                hasNavigationBar = baseLp.mColorLayoutParams.hasNavigationBar();
            }
            if (hasStatusBar || hasNavigationBar) {
                dcf.set(displayFrames.mOverscan);
                pf.set(displayFrames.mOverscan);
                df.set(displayFrames.mOverscan);
                of.set(displayFrames.mOverscan);
                cf.set(displayFrames.mContent);
                if (!hasStatusBar) {
                    cf.top = pf.top;
                }
                if (!hasNavigationBar) {
                    cf.bottom = pf.bottom;
                }
            }
        }
    }

    public void takeScreenshot(Intent intent) {
        OppoBaseDisplayPolicy oppoBaseDisplayPolicy = this.mBase;
        if (oppoBaseDisplayPolicy != null && oppoBaseDisplayPolicy.mColorDpInner != null) {
            ScreenshotHelper helper = this.mBase.mColorDpInner.getScreenshotHelper();
            WindowState statusBar = this.mBase.mColorDpInner.getStatusBar();
            WindowState navgationBar = this.mBase.mColorDpInner.getNavigationBar();
            if (helper != null && intent != null) {
                boolean z = false;
                if (intent.getIntExtra(this.mScreenshotIntentDirectionExtra, 1) < this.mFloatAssistantDirection) {
                    boolean z2 = statusBar != null && statusBar.isVisibleLw();
                    if (navgationBar != null && navgationBar.isVisibleLw()) {
                        z = true;
                    }
                    helper.takeScreenshot(1, z2, z, new ColorThreeFingerHandler(this));
                } else if (intent.getIntExtra(this.mScreenshotIntentDirectionExtra, 1) == this.mFloatAssistantDirection) {
                    boolean z3 = statusBar != null && statusBar.isVisibleLw();
                    if (navgationBar != null && navgationBar.isVisibleLw()) {
                        z = true;
                    }
                    helper.takeScreenshot(1, z3, z, new ColorFloatAssistantHandler());
                } else if (intent.getIntExtra(this.mScreenshotIntentDirectionExtra, 1) == this.mThreeFingersPressDirection) {
                    boolean z4 = statusBar != null && statusBar.isVisibleLw();
                    if (navgationBar != null && navgationBar.isVisibleLw()) {
                        z = true;
                    }
                    helper.takeScreenshot(1, z4, z, new ColorThreeFingerHandler(true));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class ColorThreeFingerHandler extends Handler implements IColorScreenshotHelper {
        private final boolean mIsPress;

        ColorThreeFingerHandler(OppoDisplayPolicy oppoDisplayPolicy) {
            this(false);
        }

        ColorThreeFingerHandler(boolean isPress) {
            this.mIsPress = isPress;
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }

        public String getSource() {
            return this.mIsPress ? "ThreeFingersPress" : "ThreeFingers";
        }

        public boolean isGlobalAction() {
            return OppoDisplayPolicy.this.isGlobalActionVisible();
        }
    }

    /* access modifiers changed from: private */
    public class ColorFloatAssistantHandler extends Handler implements IColorScreenshotHelper {
        private ColorFloatAssistantHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }

        public String getSource() {
            return "AssistantBall";
        }

        public boolean isGlobalAction() {
            return OppoDisplayPolicy.this.isGlobalActionVisible();
        }
    }

    public int prepareAddWindowLw(WindowState win, WindowManager.LayoutParams attrs) {
        int result = this.mLongshotPolicy.prepareAddWindowLw(win, attrs);
        if (result != 0) {
            return result;
        }
        if (win.toString().contains("VColorFullScreenDisplay")) {
            this.mColorFullScreenDisplay = win;
        } else if (win.toString().contains("HColorFullScreenDisplay")) {
            this.mColorFullScreenDisplayLand = win;
        }
        return OppoDisplayPolicy.super.prepareAddWindowLw(win, attrs);
    }

    public void removeWindowLw(WindowState win) {
        this.mLongshotPolicy.removeWindowLw(win);
        OppoDisplayPolicy.super.removeWindowLw(win);
    }

    public void adjustWindowParamsLw(WindowState win, WindowManager.LayoutParams attrs, int callingPid, int callingUid) {
        OppoDisplayPolicy.super.adjustWindowParamsLw(win, attrs, callingPid, callingUid);
        String name = attrs.packageName;
        if (!TextUtils.isEmpty(name) && !name.startsWith("android.server.cts") && !name.startsWith("android.server.am")) {
            int settingMode = mDisplayCompatUtils.getAppCutoutMode(name);
            if (mDisplayCompatUtils.inInstalledThirdPartyAppList(name)) {
                if (settingMode == 0) {
                    Log.d(TAG, name + ", no change cutoutMode: " + attrs.layoutInDisplayCutoutMode);
                }
                if (settingMode == 1 && attrs.layoutInDisplayCutoutMode != 3) {
                    attrs.layoutInDisplayCutoutMode = 3;
                    Log.d(TAG, name + ", setting cutoutMode: color always");
                }
                if (settingMode == 2 && attrs.layoutInDisplayCutoutMode != 0) {
                    attrs.layoutInDisplayCutoutMode = 0;
                    Log.d(TAG, name + ", setting cutoutMode: default");
                }
            } else if (settingMode == 1 && attrs.layoutInDisplayCutoutMode == 0) {
                attrs.layoutInDisplayCutoutMode = 3;
                Log.d(TAG, name + ", change system app cutoutMode: color always");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void luncherFullScreenAPPForLand(String packageName, int uid) {
        this.mContext.getPackageManager();
        Intent intent = new Intent("com.coloros.performance.RotateActivity");
        intent.putExtra(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_NAME, packageName);
        intent.putExtra(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, uid);
        intent.addFlags(268435456);
        try {
            this.mContext.startActivityAsUser(intent, UserHandle.getUserHandleForUid(uid));
        } catch (ActivityNotFoundException e) {
            Slog.e(TAG, "catch ActivityNotFoundException land" + e);
            luncherFullScreenApp(packageName, uid);
        }
    }

    private void luncherFullScreenApp(String packageName) {
        luncherFullScreenApp(packageName, ColorFreeformManagerService.FREEFORM_CALLER_UID);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void luncherFullScreenApp(String packageName, int uid) {
        PackageManager pm = this.mContext.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
        intent.setPackage(packageName);
        intent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> apps = pm.queryIntentActivitiesAsUser(intent, 0, UserHandle.getUserHandleForUid(uid));
        if (apps == null || apps.size() <= 0) {
            Slog.i(TAG, "launchActivity cannot found activity to start for package=" + packageName);
            return;
        }
        try {
            Intent it = new Intent("android.intent.action.MAIN");
            it.addCategory("android.intent.category.LAUNCHER");
            it.setComponent(new ComponentName(apps.get(0).activityInfo.packageName, apps.get(0).activityInfo.name));
            it.addFlags(268435456);
            this.mContext.startActivityAsUser(it, UserHandle.getUserHandleForUid(uid));
        } catch (ActivityNotFoundException e) {
            Slog.e(TAG, "catch ActivityNotFoundException " + e);
        } catch (Exception e2) {
            Slog.e(TAG, "catch Exception" + e2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void killFoucsPackage() {
        ((ActivityManager) this.mContext.getSystemService("activity")).forceStopPackageAsUser(this.mFoucsPackage, UserHandle.getUserId(this.mUid));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AlertDialog createDialog(final boolean isLandscape) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, 201523207);
        builder.setTitle(201590167).setView(201917592).setPositiveButton(201590173, new DialogInterface.OnClickListener() {
            /* class com.android.server.wm.OppoDisplayPolicy.AnonymousClass2 */

            public void onClick(DialogInterface dialog, int whichButton) {
                if (OppoDisplayPolicy.mDisplayCompatUtils != null) {
                    OppoDisplayPolicy.mDisplayCompatUtils.updateLocalAppsListForPkg(OppoDisplayPolicy.this.mFoucsPackage);
                }
                OppoDisplayPolicy.this.killFoucsPackage();
                if (isLandscape) {
                    if (OppoDisplayPolicy.this.mColorFullScreenWindowLand != null) {
                        OppoDisplayPolicy.this.mColorFullScreenWindowLand.setVisibility(8);
                    }
                    OppoDisplayPolicy oppoDisplayPolicy = OppoDisplayPolicy.this;
                    oppoDisplayPolicy.luncherFullScreenAPPForLand(oppoDisplayPolicy.mFoucsPackage, OppoDisplayPolicy.this.mUid);
                } else {
                    if (OppoDisplayPolicy.this.mColorFullScreenWindow != null) {
                        OppoDisplayPolicy.this.mColorFullScreenWindow.setVisibility(8);
                    }
                    OppoDisplayPolicy oppoDisplayPolicy2 = OppoDisplayPolicy.this;
                    oppoDisplayPolicy2.luncherFullScreenApp(oppoDisplayPolicy2.mFoucsPackage, OppoDisplayPolicy.this.mUid);
                }
                if (OppoDisplayPolicy.mDisplayCompatUtils != null) {
                    OppoDisplayPolicy.mDisplayCompatUtils.updateLocalShowDialogListForPkg(OppoDisplayPolicy.this.mFoucsPackage);
                }
            }
        }).setNegativeButton(201590174, (DialogInterface.OnClickListener) null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        WindowManager.LayoutParams l = dialog.getWindow().getAttributes();
        l.type = 2003;
        l.privateFlags |= 16;
        dialog.show();
        return dialog;
    }

    public void addDisplayFullScreenWindow() {
        this.mPm = (PowerManager) this.mContext.getSystemService("power");
        try {
            Context context = this.mContext;
            if (this.mColorFullScreenWindow == null) {
                this.mColorFullScreenWindow = new LinearLayout(context);
            }
            if (this.mColorFullScreenWindowLand == null) {
                this.mColorFullScreenWindowLand = new LinearLayout(context);
            }
            View view = this.mColorFullScreenWindow;
            this.mVchild = View.inflate(context, 201917590, null);
            this.mVchild.setOnClickListener(new View.OnClickListener() {
                /* class com.android.server.wm.OppoDisplayPolicy.AnonymousClass3 */

                public void onClick(View v) {
                    boolean shouldShow = false;
                    if (OppoDisplayPolicy.mDisplayCompatUtils != null) {
                        shouldShow = OppoDisplayPolicy.mDisplayCompatUtils.shouldShowFullscreenDialogForPkg(OppoDisplayPolicy.this.mFoucsPackage);
                    }
                    if (shouldShow) {
                        if (OppoDisplayPolicy.this.mFullscreenDialog != null) {
                            OppoDisplayPolicy.this.mFullscreenDialog.dismiss();
                            OppoDisplayPolicy.this.mFullscreenDialog = null;
                        }
                        OppoDisplayPolicy oppoDisplayPolicy = OppoDisplayPolicy.this;
                        oppoDisplayPolicy.mFullscreenDialog = oppoDisplayPolicy.createDialog(false);
                        return;
                    }
                    if (OppoDisplayPolicy.mDisplayCompatUtils != null) {
                        OppoDisplayPolicy.mDisplayCompatUtils.updateLocalAppsListForPkg(OppoDisplayPolicy.this.mFoucsPackage);
                    }
                    OppoDisplayPolicy.this.killFoucsPackage();
                    OppoDisplayPolicy.this.mColorFullScreenWindow.setVisibility(8);
                    OppoDisplayPolicy oppoDisplayPolicy2 = OppoDisplayPolicy.this;
                    oppoDisplayPolicy2.luncherFullScreenApp(oppoDisplayPolicy2.mFoucsPackage, OppoDisplayPolicy.this.mUid);
                }
            });
            View view2 = this.mColorFullScreenWindow;
            this.mHchildLeft = View.inflate(context, 201917591, null);
            this.mHchildLeft.setOnClickListener(new View.OnClickListener() {
                /* class com.android.server.wm.OppoDisplayPolicy.AnonymousClass4 */

                public void onClick(View v) {
                    if (OppoDisplayPolicy.mDisplayCompatUtils == null || !OppoDisplayPolicy.mDisplayCompatUtils.shouldShowFullscreenDialogForPkg(OppoDisplayPolicy.this.mFoucsPackage)) {
                        if (OppoDisplayPolicy.mDisplayCompatUtils != null) {
                            OppoDisplayPolicy.mDisplayCompatUtils.updateLocalAppsListForPkg(OppoDisplayPolicy.this.mFoucsPackage);
                        }
                        OppoDisplayPolicy.this.killFoucsPackage();
                        OppoDisplayPolicy.this.mColorFullScreenWindow.setVisibility(8);
                        OppoDisplayPolicy oppoDisplayPolicy = OppoDisplayPolicy.this;
                        oppoDisplayPolicy.luncherFullScreenAPPForLand(oppoDisplayPolicy.mFoucsPackage, OppoDisplayPolicy.this.mUid);
                        return;
                    }
                    if (OppoDisplayPolicy.this.mFullscreenDialogLand != null) {
                        OppoDisplayPolicy.this.mFullscreenDialogLand.dismiss();
                        OppoDisplayPolicy.this.mFullscreenDialogLand = null;
                    }
                    OppoDisplayPolicy oppoDisplayPolicy2 = OppoDisplayPolicy.this;
                    oppoDisplayPolicy2.mFullscreenDialogLand = oppoDisplayPolicy2.createDialog(true);
                }
            });
            View view3 = this.mColorFullScreenWindow;
            this.mHchildRight = View.inflate(context, 201917593, null);
            this.mHchildRight.setOnClickListener(new View.OnClickListener() {
                /* class com.android.server.wm.OppoDisplayPolicy.AnonymousClass5 */

                public void onClick(View v) {
                    if (OppoDisplayPolicy.mDisplayCompatUtils == null || !OppoDisplayPolicy.mDisplayCompatUtils.shouldShowFullscreenDialogForPkg(OppoDisplayPolicy.this.mFoucsPackage)) {
                        if (OppoDisplayPolicy.mDisplayCompatUtils != null) {
                            OppoDisplayPolicy.mDisplayCompatUtils.updateLocalAppsListForPkg(OppoDisplayPolicy.this.mFoucsPackage);
                        }
                        OppoDisplayPolicy.this.killFoucsPackage();
                        OppoDisplayPolicy.this.mColorFullScreenWindowLand.setVisibility(8);
                        OppoDisplayPolicy oppoDisplayPolicy = OppoDisplayPolicy.this;
                        oppoDisplayPolicy.luncherFullScreenAPPForLand(oppoDisplayPolicy.mFoucsPackage, OppoDisplayPolicy.this.mUid);
                        return;
                    }
                    if (OppoDisplayPolicy.this.mFullscreenDialogLand != null) {
                        OppoDisplayPolicy.this.mFullscreenDialogLand.dismiss();
                        OppoDisplayPolicy.this.mFullscreenDialogLand = null;
                    }
                    OppoDisplayPolicy oppoDisplayPolicy2 = OppoDisplayPolicy.this;
                    oppoDisplayPolicy2.mFullscreenDialogLand = oppoDisplayPolicy2.createDialog(true);
                }
            });
            this.mLayoutParams = new LinearLayout.LayoutParams(-2, -2, 0.0f);
            ((LinearLayout) this.mColorFullScreenWindow).setGravity(17);
            ((LinearLayout) this.mColorFullScreenWindowLand).setGravity(17);
            int[] height = null;
            if (!(this.mBase == null || this.mBase.mColorDpInner == null)) {
                height = this.mBase.mColorDpInner.getNavigationBarHeightForRotationDefault();
            }
            int navigationHeigt = height != null ? height[0] : 0;
            this.mVchild.setPadding(0, 0, 0, navigationHeigt);
            this.mHchildRight.setPadding(navigationHeigt, 0, 0, 0);
            this.mHchildLeft.setPadding(0, 0, navigationHeigt, 0);
            ((LinearLayout) this.mColorFullScreenWindow).addView(this.mVchild, this.mLayoutParams);
            ((LinearLayout) this.mColorFullScreenWindowLand).addView(this.mHchildLeft, this.mLayoutParams);
            this.mColorFullScreenWindow.setBackgroundColor(-16777216);
            this.mColorFullScreenWindow.setVisibility(4);
            this.mColorFullScreenWindowLand.setBackgroundColor(-16777216);
            this.mColorFullScreenWindowLand.setVisibility(4);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.setTitle("VColorFullScreenDisplay");
            params.type = 2100;
            params.format = 1;
            params.flags = 8;
            params.flags |= ColorMultiAppManagerService.FLAG_MULTI_APP;
            params.privateFlags |= 16;
            params.width = -1;
            params.height = -1;
            params.gravity = 80;
            params.x = 0;
            params.y = 0;
            WindowManager wm = (WindowManager) context.getSystemService("window");
            wm.addView(this.mColorFullScreenWindow, params);
            params.setTitle("HColorFullScreenDisplay");
            wm.addView(this.mColorFullScreenWindowLand, params);
            this.mIsVisibility = false;
        } catch (WindowManager.BadTokenException e) {
            Slog.e("DisplayCompat", "BadTokenException " + e);
        } catch (RuntimeException e2) {
            Slog.e("DisplayCompat", "RuntimeException " + e2);
        }
    }

    public void configChangeDisplayFullScreen(int rotation) {
        View view = this.mColorSagAreaView;
        if (view != null) {
            view.requestLayout();
        }
    }

    public void resetDisplayFullScreenWindow() {
        View view = this.mColorFullScreenWindow;
        if (view != null && this.mColorFullScreenWindowLand != null) {
            view.setVisibility(8);
            this.mColorFullScreenWindowLand.setVisibility(8);
            this.mColorFullScreenWindow.invalidate();
            this.mColorFullScreenWindowLand.invalidate();
            this.mIsVisibility = false;
            this.mRotation = 0;
            this.mFoucsPackage = null;
            this.mUid = ColorFreeformManagerService.FREEFORM_CALLER_UID;
            this.mHHHRotation = 0;
        }
    }

    public void reLayoutDisplayFullScreenWindow(boolean visibility, String packageName, int rotation, boolean needHide, int uid) {
        View view;
        PowerManager powerManager = this.mPm;
        if ((powerManager == null || powerManager.isScreenOn()) && (view = this.mColorFullScreenWindow) != null && this.mColorFullScreenWindowLand != null) {
            boolean z = this.mIsVisibility;
            if (z != visibility) {
                this.mIsVisibility = visibility;
                this.mIsVisibility = visibility;
                if (this.mIsVisibility) {
                    if (rotation == 0) {
                        view.setVisibility(0);
                        this.mColorFullScreenWindowLand.setVisibility(8);
                    } else if (rotation == 1) {
                        view.setVisibility(8);
                        this.mColorFullScreenWindowLand.setVisibility(0);
                    } else if (rotation == 2) {
                        view.setVisibility(0);
                        this.mColorFullScreenWindowLand.setVisibility(8);
                    } else if (rotation == 3) {
                        view.setVisibility(8);
                        this.mColorFullScreenWindowLand.setVisibility(0);
                    }
                    this.mFoucsPackage = packageName;
                    this.mUid = uid;
                } else {
                    view.setVisibility(8);
                    this.mColorFullScreenWindowLand.setVisibility(8);
                    this.mFoucsPackage = null;
                }
                updateChildVisibility((ViewGroup) this.mColorFullScreenWindow, needHide);
                updateChildVisibility((ViewGroup) this.mColorFullScreenWindowLand, needHide);
                this.mColorFullScreenWindow.invalidate();
                this.mColorFullScreenWindowLand.invalidate();
            } else if (z) {
                if (this.mHHHRotation != rotation) {
                    this.mHHHRotation = rotation;
                    if (rotation == 0) {
                        view.setVisibility(0);
                        this.mColorFullScreenWindowLand.setVisibility(8);
                    } else if (rotation == 1) {
                        view.setVisibility(8);
                        this.mColorFullScreenWindowLand.setVisibility(0);
                    } else if (rotation == 2) {
                        view.setVisibility(0);
                        this.mColorFullScreenWindowLand.setVisibility(8);
                    } else if (rotation == 3) {
                        view.setVisibility(8);
                        this.mColorFullScreenWindowLand.setVisibility(0);
                    }
                    updateChildVisibility((ViewGroup) this.mColorFullScreenWindow, needHide);
                    updateChildVisibility((ViewGroup) this.mColorFullScreenWindowLand, needHide);
                }
                this.mUid = uid;
            }
        }
    }

    public void updateDisplayFullScreenContent(int displayRotation) {
        if (this.mRotation != displayRotation) {
            this.mRotation = displayRotation;
            View view = this.mColorFullScreenWindow;
            if (view != null) {
                if (displayRotation != 0) {
                    if (displayRotation != 1) {
                        if (displayRotation != 2) {
                            if (!(displayRotation != 3 || this.mHchildRight == null || this.mLayoutParams == null)) {
                                ((ViewGroup) this.mColorFullScreenWindowLand).removeAllViews();
                                ((LinearLayout) this.mColorFullScreenWindowLand).addView(this.mHchildRight, this.mLayoutParams);
                            }
                        } else if (!(this.mVchild == null || this.mLayoutParams == null)) {
                            ((ViewGroup) view).removeAllViews();
                            ((LinearLayout) this.mColorFullScreenWindow).addView(this.mVchild, this.mLayoutParams);
                        }
                    } else if (!(this.mHchildLeft == null || this.mLayoutParams == null)) {
                        ((ViewGroup) this.mColorFullScreenWindowLand).removeAllViews();
                        ((LinearLayout) this.mColorFullScreenWindowLand).addView(this.mHchildLeft, this.mLayoutParams);
                    }
                } else if (!(this.mVchild == null || this.mLayoutParams == null)) {
                    ((ViewGroup) view).removeAllViews();
                    ((LinearLayout) this.mColorFullScreenWindow).addView(this.mVchild, this.mLayoutParams);
                }
                this.mColorFullScreenWindow.invalidate();
                this.mColorFullScreenWindowLand.invalidate();
            }
        }
    }

    private void updateChildVisibility(ViewGroup viewParent, boolean needHide) {
        if (viewParent != null && viewParent.getVisibility() == 0) {
            int childCount = viewParent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = viewParent.getChildAt(i);
                if (child != null) {
                    child.setVisibility(needHide ? 8 : 0);
                }
            }
        }
    }

    public void layoutDisplayFullScreenWindow(DisplayFrames displayFrames, int uiMode) {
        if (displayFrames.mDisplayId == 0) {
            if (this.mColorFullScreenDisplay != null) {
                layoutLwForColorFullScreen(displayFrames);
            }
            if (this.mColorFullScreenDisplayLand != null) {
                layoutLwForColorFullScreeLand(displayFrames);
            }
        }
    }

    public void layoutLwForColorFullScreen(DisplayFrames displayFrames) {
        int height;
        if (displayFrames != null && this.mColorFullScreenDisplay != null) {
            Rect df = new Rect();
            int height2 = (displayFrames.mDisplayWidth * 16) / 9;
            if (!displayFrames.mDisplayCutout.getDisplayCutout().isEmpty()) {
                height = height2 + displayFrames.mDisplayCutout.getDisplayCutout().getSafeInsetTop();
            } else {
                height = height2;
            }
            df.set(0, height, displayFrames.mDisplayWidth, displayFrames.mDisplayHeight);
            this.mColorFullScreenDisplay.getWindowFrames().setFrames(df, df, df, df, df, df, df, df);
            this.mColorFullScreenDisplay.getWindowFrames().setDisplayCutout(displayFrames.mDisplayCutout);
            this.mColorFullScreenDisplay.getWindowFrames().setParentFrameWasClippedByDisplayCutout(false);
            this.mColorFullScreenDisplay.computeFrameLw();
        }
    }

    public void layoutLwForColorFullScreeLand(DisplayFrames displayFrames) {
        if (this.mColorFullScreenDisplayLand != null && displayFrames != null) {
            Rect df = new Rect();
            int width = (displayFrames.mDisplayHeight * 16) / 9;
            int i = displayFrames.mRotation;
            if (i == 1) {
                df.set(displayFrames.mDisplayCutout.getDisplayCutout().getSafeInsetLeft() + width, 0, displayFrames.mDisplayWidth, displayFrames.mDisplayHeight);
            } else if (i != 3) {
                df.set(0, 0, 280, 1080);
            } else {
                df.set(0, 0, displayFrames.mDisplayWidth - (displayFrames.mDisplayCutout.getDisplayCutout().getSafeInsetRight() + width), displayFrames.mDisplayHeight);
            }
            this.mColorFullScreenDisplayLand.getWindowFrames().setFrames(df, df, df, df, df, df, df, df);
            this.mColorFullScreenDisplayLand.getWindowFrames().setDisplayCutout(displayFrames.mDisplayCutout);
            this.mColorFullScreenDisplayLand.getWindowFrames().setParentFrameWasClippedByDisplayCutout(false);
            this.mColorFullScreenDisplayLand.computeFrameLw();
        }
    }

    public void layoutWindowLw(WindowState win, WindowState attached, DisplayFrames displayFrames) {
        if (win != this.mColorFullScreenDisplay && win != this.mColorFullScreenDisplayLand) {
            OppoDisplayPolicy.super.layoutWindowLw(win, attached, displayFrames);
        }
    }

    public WindowState getTopFullscreenOpaqueWindowState() {
        OppoBaseDisplayPolicy oppoBaseDisplayPolicy = this.mBase;
        if (oppoBaseDisplayPolicy == null || oppoBaseDisplayPolicy.mColorDpInner == null) {
            return null;
        }
        return this.mBase.mColorDpInner.getTopFullscreenOpaqueWindowState();
    }

    public WindowState getFullScreenDisplayWindow() {
        return this.mColorFullScreenDisplay;
    }

    public WindowState getFullScreenDisplayWindowLand() {
        return this.mColorFullScreenDisplayLand;
    }

    private static OppoBaseDisplayPolicy typeCasting(DisplayPolicy dp) {
        if (dp != null) {
            return (OppoBaseDisplayPolicy) ColorTypeCastingHelper.typeCasting(OppoBaseDisplayPolicy.class, dp);
        }
        return null;
    }

    private static ColorBaseLayoutParams typeCasting(WindowManager.LayoutParams lp) {
        if (lp != null) {
            return (ColorBaseLayoutParams) ColorTypeCastingHelper.typeCasting(ColorBaseLayoutParams.class, lp);
        }
        return null;
    }
}
