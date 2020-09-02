package com.android.server.wm;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorDisplayCompatUtils;
import com.color.util.ColorTypeCastingHelper;

public class ColorFullScreenDisplayManager implements IColorFullScreenDisplayManager {
    private static final String TAG = "ColorFullScreenDisplayManager";
    private static final Object mLock = new Object();
    private static ColorFullScreenDisplayManager sInstance = null;
    private boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private WindowManagerService mWms = null;

    public static ColorFullScreenDisplayManager getInstance() {
        ColorFullScreenDisplayManager colorFullScreenDisplayManager;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new ColorFullScreenDisplayManager();
            }
            colorFullScreenDisplayManager = sInstance;
        }
        return colorFullScreenDisplayManager;
    }

    private ColorFullScreenDisplayManager() {
    }

    public void init(IColorWindowManagerServiceEx wmsEx) {
        if (wmsEx != null) {
            this.mWms = wmsEx.getWindowManagerService();
        }
    }

    public DisplayPolicy createDisplayPolicy(WindowManagerService service, DisplayContent displayContent) {
        return new OppoDisplayPolicy(service, displayContent);
    }

    public void sendMessageToWmService(int messageWhat, int messageArg1) {
        if (messageArg1 == -1) {
            this.mWms.mH.sendEmptyMessage(messageWhat);
            return;
        }
        Message msg = Message.obtain();
        msg.what = messageWhat;
        msg.arg1 = messageArg1;
        this.mWms.mH.sendMessageDelayed(msg, 0);
    }

    public void hanleFullScreenDisplayMessage(Message msg) {
        DisplayContent displaycontent;
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null && (displaycontent = windowManagerService.getDefaultDisplayContentLocked()) != null && displaycontent.getDisplayPolicy() != null && (displaycontent.getDisplayPolicy() instanceof OppoDisplayPolicy)) {
            OppoDisplayPolicy mPolicy = displaycontent.getDisplayPolicy();
            switch (msg.what) {
                case 1302:
                    Bundle bundle = msg.getData();
                    if (bundle != null) {
                        mPolicy.reLayoutDisplayFullScreenWindow(true, bundle.getString("packageName"), bundle.getInt("rotation"), bundle.getBoolean("needHide"), bundle.getInt("uid"));
                        return;
                    }
                    return;
                case 1303:
                    mPolicy.reLayoutDisplayFullScreenWindow(false, null, msg.arg1, false, ColorFreeformManagerService.FREEFORM_CALLER_UID);
                    return;
                case 1304:
                    mPolicy.addDisplayFullScreenWindow();
                    return;
                case 1305:
                    mPolicy.updateDisplayFullScreenContent(msg.arg1);
                    return;
                case 1306:
                    mPolicy.configChangeDisplayFullScreen(msg.arg1);
                    return;
                case 1307:
                    mPolicy.resetDisplayFullScreenWindow();
                    return;
                default:
                    return;
            }
        }
    }

    public void initColorDisplayCompat(String packageName, WindowState windowState) {
        OppoBaseWindowState baseWindowState = typeCasting(windowState);
        if (baseWindowState != null) {
            baseWindowState.setDisplayCompat(ColorDisplayCompatUtils.getInstance().shouldCompatAdjustForPkg(packageName));
            baseWindowState.setmDisplayHideFullscreenButton(ColorDisplayCompatUtils.getInstance().shouldHideFullscreenButtonForPkg(packageName));
        }
    }

    public void injectorGetSurfaceTouchableRegion(WindowState windowState, Region region) {
        if (windowState != null && region != null) {
            OppoBaseWindowState baseWindowState = typeCasting(windowState);
            String winStr = windowState.toString();
            if (baseWindowState != null && winStr != null && winStr.contains("ColorFullScreenDisplay") && baseWindowState.isSystemWindowStatus()) {
                region.set(0, 0, 0, 0);
            }
        }
    }

    public void injectorDisplayContentConsumer(DisplayContent displayContent, WindowState w, WindowState mCurrentFocus) {
        if (displayContent != null && w != null) {
            int i = displayContent.getDisplayInfo().logicalWidth;
            int i2 = displayContent.getDisplayInfo().logicalHeight;
            if (w.mAttrs == null || w.mAttrs.getTitle() == null || !w.mAttrs.getTitle().toString().contains("ColorFullScreenDisplay")) {
                boolean add = false;
                boolean remove = false;
                if (displayContent.getDisplayPolicy() != null && (displayContent.getDisplayPolicy() instanceof OppoDisplayPolicy) && w.getAttrs() != null) {
                    OppoDisplayPolicy oppoDisplayPolicy = displayContent.getDisplayPolicy();
                    OppoBaseWindowState baseWindowState = typeCasting(oppoDisplayPolicy.getFullScreenDisplayWindow());
                    OppoBaseWindowState baseWindowStateLand = typeCasting(oppoDisplayPolicy.getFullScreenDisplayWindowLand());
                    OppoBaseWindowState baseW = typeCasting(w);
                    if (w.toString().contains("InputMethod") || w.toString().contains("com.android.packageinstaller.permission.ui.GrantPermissionsActivity")) {
                        if (baseWindowState != null) {
                            baseWindowState.setSystemWindowStatus(true);
                        }
                        if (baseWindowStateLand != null) {
                            baseWindowStateLand.setSystemWindowStatus(true);
                        }
                        remove = true;
                    }
                    int fl = PolicyControl.getWindowFlags(w, w.getAttrs());
                    if (baseW == null || !baseW.isDisplayCompat() || (fl & 524288) != 0 || w.inMultiWindowMode()) {
                        if (oppoDisplayPolicy.getTopFullscreenOpaqueWindowState() == w) {
                            remove = true;
                        } else if (w.toString().contains("com.tencent.mm.plugin.voip.ui.VideoActivity")) {
                            remove = true;
                        } else if (w.toString().contains("Splash Screen com.tencent.mm")) {
                            remove = true;
                        }
                        if ((displayContent.getRotation() == 2 || displayContent.getRotation() == 0) && mCurrentFocus != null && (mCurrentFocus.toString().contains("ChooserActivity") || mCurrentFocus.toString().contains("ResolverActivity"))) {
                            remove = true;
                        }
                    } else if (oppoDisplayPolicy.getTopFullscreenOpaqueWindowState() == w && (mCurrentFocus == null || (!mCurrentFocus.toString().contains("ChooserActivity") && !mCurrentFocus.toString().contains("GestureTransitionView") && !mCurrentFocus.toString().contains("ResolverActivity")))) {
                        boolean isSystemWindow = false;
                        if (baseWindowState != null) {
                            isSystemWindow = baseWindowState.isSystemWindowStatus();
                        }
                        if (!isSystemWindow && baseWindowStateLand != null) {
                            isSystemWindow = baseWindowStateLand.isSystemWindowStatus();
                        }
                        if (!isSystemWindow && !remove) {
                            add = true;
                        }
                    }
                    if (add) {
                        Message msg = Message.obtain();
                        msg.what = 1302;
                        Bundle bundle = new Bundle();
                        bundle.putString("packageName", w.getAttrs().packageName);
                        bundle.putInt("rotation", displayContent.getRotation());
                        bundle.putBoolean("needHide", typeCasting(w) != null ? typeCasting(w).isDisplayHideFullscreenButtonNeeded() : false);
                        bundle.putInt("uid", w.getOwningUid());
                        msg.setData(bundle);
                        this.mWms.mH.sendMessageDelayed(msg, 0);
                    } else if (remove) {
                        this.mWms.mH.sendEmptyMessage(1303);
                    }
                }
            } else {
                Message msg2 = Message.obtain();
                msg2.what = 1305;
                msg2.arg1 = displayContent.getRotation();
                this.mWms.mH.sendMessageDelayed(msg2, 0);
            }
        }
    }

    public boolean performLayoutNoTrace(DisplayPolicy displayPolicy, DisplayFrames mDisplayFrames, int uiMode) {
        if (!(displayPolicy instanceof OppoDisplayPolicy)) {
            return false;
        }
        OppoDisplayPolicy oppoDisplayPolicy = (OppoDisplayPolicy) displayPolicy;
        oppoDisplayPolicy.layoutDisplayFullScreenWindow(mDisplayFrames, uiMode);
        oppoDisplayPolicy.beginLayoutLw(mDisplayFrames, uiMode);
        OppoBaseWindowState baseWindowState = typeCasting(oppoDisplayPolicy.getFullScreenDisplayWindow());
        OppoBaseWindowState baseWindowStateLand = typeCasting(oppoDisplayPolicy.getFullScreenDisplayWindowLand());
        if (baseWindowState != null) {
            baseWindowState.setSystemWindowStatus(false);
        }
        if (baseWindowStateLand == null) {
            return true;
        }
        baseWindowStateLand.setSystemWindowStatus(false);
        return true;
    }

    public int calculateCompatBoundsTransformation(int offsetX, Rect viewportBounds, float viewportW, float contentW, float mSizeCompatScale) {
        if (viewportBounds.left != 0) {
            return ((int) ((viewportW - (contentW * mSizeCompatScale)) + 1.0f)) + viewportBounds.left;
        }
        return 0;
    }

    public int calculateCompatBoundsOffsetX(int offsetX, Configuration config, ComponentName componentName, Rect viewportBounds, Rect contentBounds, float sizeCompatScale) {
        int value = offsetX;
        String packageName = componentName.getPackageName();
        if (ColorDisplayCompatUtils.getInstance().shouldCompatAdjustForPkg(packageName)) {
            float contentW = (float) contentBounds.width();
            float viewportW = (float) viewportBounds.width();
            int rotation = config.windowConfiguration.getRotation();
            if (rotation == 1) {
                value = viewportBounds.left;
            } else if (rotation == 3) {
                value = ((int) ((viewportW - (contentW * sizeCompatScale)) + 1.0f)) + viewportBounds.left;
            }
            if (this.DEBUG) {
                Log.d(TAG, "rotation: " + rotation + ", contentW: " + contentW + ", viewportW: " + viewportW);
                Log.d(TAG, "calculate compatBounds, " + packageName + ", offsetX: " + offsetX + " >>> " + value);
            }
        }
        return value;
    }

    public float getmaxAspectRatio(ActivityInfo info) {
        return ColorDisplayCompatUtils.getInstance().getmaxAspectRatio(info);
    }

    public boolean injectLayoutWindowLwDisplayPolicy(int cutoutMode) {
        return cutoutMode != 3;
    }

    private static OppoBaseWindowState typeCasting(WindowState win) {
        if (win != null) {
            return (OppoBaseWindowState) ColorTypeCastingHelper.typeCasting(OppoBaseWindowState.class, win);
        }
        return null;
    }
}
