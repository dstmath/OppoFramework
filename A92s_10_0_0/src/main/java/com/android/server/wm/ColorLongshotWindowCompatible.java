package com.android.server.wm;

import android.content.Context;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.color.screenshot.ColorLongshotDump;
import com.color.util.ColorLog;
import com.color.view.ColorWindowUtils;
import java.util.function.Consumer;

public class ColorLongshotWindowCompatible {
    private static final boolean DBG = ColorLongshotDump.DBG;
    private static final String[] SCREENSHOT_APP_EXCLUDED_WINDOWS = {"com.coloros.screenshot/com.coloros.screenshot.setting.ScreenshotSettingActivity"};
    private static final String[] SYSTEM_UI_WHITE_LIST_WINDOWS = {"com.android.systemui/com.coloros.systemui.navbar.settings.NavigationBarSettingsActivity"};
    private static final String TAG = "LongshotDump";
    private IBinder mLayerHandleToken = null;
    private ColorLongshotMainWindow mMainWindow = null;
    private int mSurfaceLayer = 0;
    private final ColorWindowDumpUtils mUtils;
    private int mWindowFlags = 0;

    public ColorLongshotWindowCompatible(Context context) {
        this.mUtils = new ColorWindowDumpUtils(context);
    }

    public DisplayMetrics getDispMetrics(DisplayContent displayContent) {
        return this.mUtils.getDispMetrics(displayContent);
    }

    public DisplayMetrics getRealMetrics(DisplayContent displayContent) {
        return this.mUtils.getRealMetrics(displayContent);
    }

    public int getStatusBarHeight() {
        return this.mUtils.getStatusBarHeight();
    }

    public ColorLongshotMainWindow traversalWindows(DisplayContent displayContent, DisplayMetrics dispMetrics, DisplayMetrics realMetrics, int statusbarHeight, ColorWindowTraversalListener listener, String[] args) {
        this.mMainWindow = null;
        this.mWindowFlags = 0;
        displayContent.forAllWindows(new Consumer(listener, args, dispMetrics, realMetrics, statusbarHeight) {
            /* class com.android.server.wm.$$Lambda$ColorLongshotWindowCompatible$1fhhQ4Bmxe2FBq_1czo6jZLSJc */
            private final /* synthetic */ ColorWindowTraversalListener f$1;
            private final /* synthetic */ String[] f$2;
            private final /* synthetic */ DisplayMetrics f$3;
            private final /* synthetic */ DisplayMetrics f$4;
            private final /* synthetic */ int f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ColorLongshotWindowCompatible.this.lambda$traversalWindows$0$ColorLongshotWindowCompatible(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, (WindowState) obj);
            }
        }, true);
        updateWindowFlags();
        return this.mMainWindow;
    }

    public /* synthetic */ void lambda$traversalWindows$0$ColorLongshotWindowCompatible(ColorWindowTraversalListener listener, String[] args, DisplayMetrics dispMetrics, DisplayMetrics realMetrics, int statusbarHeight, WindowState w) {
        CharSequence winName = this.mUtils.getWindowName(w);
        if (w.mAppToken != null && w.mAppToken.mActivityRecord != null && !w.mAppToken.mActivityRecord.mayFreezeScreenLocked(w.mAppToken.mActivityRecord.app)) {
            listener.printWindow("no response window      ", winName);
        } else if (w.mClient == null) {
            listener.printWindow("null WindowClient      ", winName);
        } else if (!w.isReadyForDisplay()) {
            listener.printWindow("not ReadyForDisplay    ", winName);
        } else {
            if (this.mUtils.isSecureWindow(w)) {
                addSecureFlag(listener, winName);
            }
            if (this.mMainWindow != null) {
                listener.printWindow("not MainDisplayWindow  ", winName);
            } else if (!this.mUtils.isTouchableWindow(w)) {
                listener.printWindow("not TouchableWindow    ", winName);
            } else {
                CharSequence winTitle = this.mUtils.getWindowTitle(w);
                if (ColorWindowUtils.isInputMethodWindow(w.mAttrs.type, winTitle)) {
                    listener.printWindow("is  InputMethodWindow  ", winName);
                    return;
                }
                String owningPackage = this.mUtils.getOwningPackage(w);
                if (ColorWindowUtils.isScreenshotApp(owningPackage) && !isExcludedScreenshotWindows(winName)) {
                    listener.printWindow("is  ScreenshotApp      ", winName);
                } else if (ColorWindowUtils.isExServiceUiApp(owningPackage)) {
                    listener.printWindow("is  ExServiceUiApp     ", winName);
                } else {
                    if (ColorWindowUtils.isSystemUiApp(owningPackage)) {
                        if (ColorWindowUtils.isStatusBar(w.mAttrs.type)) {
                            if (!ColorWindowUtils.isExpand(w.mAttrs)) {
                                listener.printWindow("is  SysUi StatusBar    ", winName);
                                listener.collectSystemWindows(w.mClient, winTitle, getSurfaceLayer(w), w.mAttrs);
                                return;
                            } else if (ColorWindowUtils.isSystemHeadsUp(w.mAttrs)) {
                                listener.printWindow("is  SysUi StatusNotify ", "HeadsUp");
                                listener.collectFloatWindows(w.mClient, winTitle, getSurfaceLayer(w), w.mAttrs);
                                return;
                            } else {
                                listener.printWindow("is  SysUi StatusExpand ", winName);
                                addUnsupportFlag(listener, winName);
                            }
                        } else if (isNotWhiteListSystemUiWindows(winName)) {
                            if (ColorWindowUtils.isVolumeDialog(winTitle)) {
                                listener.printWindow("is  SysUi VolumeDialog   ", winName);
                                addVolumeFlag(listener, winName);
                                return;
                            } else if (ColorWindowUtils.isSystemFloatBar(w.mAttrs.type, winTitle)) {
                                if (ColorWindowUtils.isExpand(w.mAttrs)) {
                                    listener.printWindow("is  SysUi FullFloat    ", winName);
                                    if (!isTriggerFromSystemFloatBar(args)) {
                                        addUnsupportFlag(listener, winName);
                                        return;
                                    }
                                    return;
                                }
                                listener.printWindow("is  SysUi SmallFloat   ", winName);
                                return;
                            } else if (ColorWindowUtils.isSystemUiBar(w.mAttrs.type, winTitle)) {
                                listener.printWindow("is  SysUi SystemWindow   ", winName);
                                listener.collectSystemWindows(w.mClient, winTitle, getSurfaceLayer(w), w.mAttrs);
                                return;
                            } else if (!isFullScreenWindow(w, dispMetrics, realMetrics, statusbarHeight)) {
                                if (listener.hasSystemDocorView(w.mClient)) {
                                    listener.printWindow("is  SysUi SystemDialog   ", winName);
                                    addUnsupportFlag(listener, winName);
                                    return;
                                }
                                listener.printWindow("is  SysUi NoFullWindow   ", winName);
                                listener.collectFloatWindows(w.mClient, winTitle, getSurfaceLayer(w), w.mAttrs);
                                return;
                            }
                        }
                    }
                    if (ColorWindowUtils.isFloatAssistant(owningPackage) && ColorWindowUtils.isSystemWindow(w.mAttrs)) {
                        if (this.mUtils.isSmallFloatWindow(w, dispMetrics)) {
                            listener.printWindow("is  FloatAssistBar     ", winName);
                            return;
                        }
                        listener.printWindow("is  FloatAssistPanel   ", winName);
                        if (winName != null) {
                            if (ColorWindowUtils.isFloatAssistant(winName.toString()) && !isTriggerFromFloatBall(args)) {
                                addUnsupportFlag(listener, winName);
                            }
                            if (ColorWindowUtils.isEdgePanelTitle(winName.toString()) && !isTriggerFromEdgePanel(args)) {
                                addUnsupportFlag(listener, winName);
                            }
                        }
                        if (isDumpCmdModified(args)) {
                            return;
                        }
                    }
                    if (this.mUtils.isSmallFloatWindow(w, realMetrics)) {
                        listener.printWindow("is  SmallFloatWindow   ", winName);
                        listener.collectFloatWindows(w.mClient, winTitle, getSurfaceLayer(w), w.mAttrs);
                        return;
                    }
                    boolean reject = true;
                    if (ColorWindowUtils.isTalkBack(owningPackage)) {
                        boolean reject2 = !w.getAttrs().isFullscreen();
                        listener.printWindow("is  TalkBackWindow reject: " + reject2, winName);
                        if (!reject2) {
                            addUnsupportFlag(listener, winName);
                        } else {
                            return;
                        }
                    }
                    if (ColorWindowUtils.isAssistantScreen(owningPackage)) {
                        WindowManager.LayoutParams attrs = w.getAttrs();
                        if (attrs.x >= 0) {
                            reject = DBG;
                        }
                        listener.printWindow("AssistantScreen attrs x: " + attrs.x, winName);
                        listener.printWindow("AssistantScreen reject: " + reject, winName);
                        if (reject) {
                            return;
                        }
                    }
                    listener.printWindow("is  MainDisplayWindow  ", winName);
                    this.mMainWindow = new ColorLongshotMainWindow(w);
                }
            }
        }
    }

    public int getSurfaceLayerLocked(DisplayContent displayContent, int type) {
        this.mSurfaceLayer = 0;
        displayContent.forAllWindows(new Consumer(type) {
            /* class com.android.server.wm.$$Lambda$ColorLongshotWindowCompatible$JUArbasGYhuM9c_U4dWUzA_lg7c */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ColorLongshotWindowCompatible.this.lambda$getSurfaceLayerLocked$1$ColorLongshotWindowCompatible(this.f$1, (WindowState) obj);
            }
        }, true);
        return this.mSurfaceLayer;
    }

    public /* synthetic */ void lambda$getSurfaceLayerLocked$1$ColorLongshotWindowCompatible(int type, WindowState w) {
        if (w.mAttrs.type == type) {
            this.mSurfaceLayer = getSurfaceLayer(w);
        }
    }

    public IBinder getWindowByTypeLocked(DisplayContent displayContent, int type, AppWindowToken focusApp) {
        if (type < 0) {
            Task task = null;
            if (focusApp != null) {
                task = focusApp.getTask();
            }
            if (task != null) {
                boolean z = DBG;
                ColorLog.d(z, TAG, "getLongshotWindowByTypeLocked : focusApp=" + focusApp.toString());
                return task.getSurfaceControl().getHandle();
            }
        }
        this.mLayerHandleToken = null;
        displayContent.forAllWindows(new Consumer(type) {
            /* class com.android.server.wm.$$Lambda$ColorLongshotWindowCompatible$YAneD1kozewlIlaC6gHDoLS2aVY */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ColorLongshotWindowCompatible.this.lambda$getWindowByTypeLocked$2$ColorLongshotWindowCompatible(this.f$1, (WindowState) obj);
            }
        }, true);
        return this.mLayerHandleToken;
    }

    public /* synthetic */ void lambda$getWindowByTypeLocked$2$ColorLongshotWindowCompatible(int type, WindowState w) {
        if (w.mAttrs.type == type) {
            WindowToken token = null;
            if (w.mAppToken != null) {
                token = w.mAppToken;
            } else if (w.mToken != null) {
                token = w.mToken;
            }
            if (token != null) {
                CharSequence winTitle = this.mUtils.getWindowTitle(w);
                String owningPackage = this.mUtils.getOwningPackage(w);
                boolean z = DBG;
                ColorLog.d(z, TAG, "getWindowByTypeLocked : token=" + token.toString() + ", title=" + ((Object) winTitle) + ", pkg=" + owningPackage);
                this.mLayerHandleToken = token.getSurfaceControl().getHandle();
            }
        }
    }

    private int getSurfaceLayer(WindowState win) {
        return win.mBaseLayer;
    }

    private void addSecureFlag(ColorWindowTraversalListener listener, CharSequence winName) {
        this.mWindowFlags |= 1;
        listener.printDetect("Secure       ", winName);
    }

    private void addVolumeFlag(ColorWindowTraversalListener listener, CharSequence winName) {
        this.mWindowFlags |= 2;
        listener.printDetect("Volume       ", winName);
    }

    private void addUnsupportFlag(ColorWindowTraversalListener listener, CharSequence winName) {
        this.mWindowFlags |= 4;
        listener.printDetect("Unsupport    ", winName);
    }

    private void updateWindowFlags() {
        ColorLongshotMainWindow colorLongshotMainWindow = this.mMainWindow;
        if (colorLongshotMainWindow != null) {
            colorLongshotMainWindow.setFlags(this.mWindowFlags);
        }
    }

    private boolean isFullScreenWindow(WindowState w, DisplayMetrics dispMetrics, DisplayMetrics realMetrics, int statusBarHeight) {
        Rect contentFrame = w.getContentFrameLw();
        int width = contentFrame.width();
        boolean z = DBG;
        ColorLog.d(z, TAG, "isFullScreenWindow: width=" + width + "-->dispMetrics.widthPixels=" + dispMetrics.widthPixels + "-->realMetrics.widthPixels=" + realMetrics.widthPixels + "-->contentFrame.bottom=" + contentFrame.bottom + "-->dispMetrics.heightPixels=" + dispMetrics.heightPixels + "-->realMetrics.heightPixels=" + realMetrics.heightPixels + "-->contentFrame.top=" + contentFrame.top + "-->statusBarHeight=" + statusBarHeight);
        if (width != dispMetrics.widthPixels && width != realMetrics.widthPixels) {
            return DBG;
        }
        if (dispMetrics.heightPixels == realMetrics.heightPixels) {
            if ((contentFrame.top == 0 || contentFrame.top == statusBarHeight) && contentFrame.bottom == dispMetrics.heightPixels) {
                return true;
            }
        } else if (contentFrame.height() == dispMetrics.heightPixels || contentFrame.height() == realMetrics.heightPixels - statusBarHeight) {
            return true;
        } else {
            return DBG;
        }
        return DBG;
    }

    private boolean isTriggerFromFloatBall(String[] args) {
        if (args == null || args.length <= 0) {
            return DBG;
        }
        for (String source : args) {
            if ("AssistantBall".equals(source)) {
                return true;
            }
        }
        return DBG;
    }

    private boolean isTriggerFromSystemFloatBar(String[] args) {
        if (args == null || args.length <= 0) {
            return DBG;
        }
        for (String source : args) {
            if ("ScreenAssistant".equals(source)) {
                return true;
            }
        }
        return DBG;
    }

    private boolean isTriggerFromEdgePanel(String[] args) {
        if (args == null || args.length <= 0) {
            return DBG;
        }
        for (String source : args) {
            if ("EdgePanel".equals(source)) {
                return true;
            }
        }
        return DBG;
    }

    private boolean isDumpCmdModified(String[] args) {
        if (args == null || args.length < 2) {
            return DBG;
        }
        return true;
    }

    private static boolean isExcludedScreenshotWindows(CharSequence winName) {
        for (String excludedWin : SCREENSHOT_APP_EXCLUDED_WINDOWS) {
            if (excludedWin.equals(winName.toString())) {
                return true;
            }
        }
        return DBG;
    }

    private static boolean isNotWhiteListSystemUiWindows(CharSequence winName) {
        for (String excludedWin : SYSTEM_UI_WHITE_LIST_WINDOWS) {
            if (excludedWin.equals(winName.toString())) {
                return DBG;
            }
        }
        return true;
    }
}
