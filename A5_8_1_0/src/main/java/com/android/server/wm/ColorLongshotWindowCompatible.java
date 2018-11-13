package com.android.server.wm;

import android.graphics.Rect;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.IWindow;
import android.view.WindowManager.LayoutParams;
import com.color.screenshot.ColorLongshotUtils;
import java.util.function.Consumer;

public class ColorLongshotWindowCompatible {
    private static final int SMALL_WINDOW_RATE = 5;
    private static final String TAG = "LongshotDump";
    private ColorLongshotMainWindow mMainWindow = null;
    private int mSurfaceLayer = 0;

    public interface WindowTraversalListener {
        void collectFloatWindows(IWindow iWindow, CharSequence charSequence, int i, LayoutParams layoutParams);

        void collectSystemWindows(IWindow iWindow, CharSequence charSequence, int i, LayoutParams layoutParams);

        void printWindow(String str, CharSequence charSequence);
    }

    public ColorLongshotMainWindow traversalWindows(DisplayContent displayContent, DisplayMetrics dispMetrics, DisplayMetrics realMetrics, int statusbarHeight, WindowTraversalListener listener) {
        this.mMainWindow = null;
        displayContent.forAllWindows((Consumer) new -$Lambda$bQ8IMZ_taBbscGhHwzSZOHc-GLw(statusbarHeight, this, listener, dispMetrics, realMetrics), true);
        return this.mMainWindow;
    }

    /* renamed from: lambda$-com_android_server_wm_ColorLongshotWindowCompatible_2048 */
    /* synthetic */ void m253lambda$-com_android_server_wm_ColorLongshotWindowCompatible_2048(WindowTraversalListener listener, DisplayMetrics dispMetrics, DisplayMetrics realMetrics, int statusbarHeight, WindowState w) {
        boolean isUnsupported = false;
        CharSequence winName = getWindowName(w);
        if (this.mMainWindow != null) {
            listener.printWindow("not MainDisplayWindow  ", winName);
        } else if (w.mClient == null) {
            listener.printWindow("null WindowClient      ", winName);
        } else if (!w.isReadyForDisplay()) {
            listener.printWindow("not ReadyForDisplay    ", winName);
        } else if (isTouchableWindow(w)) {
            CharSequence winTitle = getWindowTitle(w);
            if (ColorLongshotUtils.isInputMethodWindow(w.mAttrs.type, winTitle)) {
                listener.printWindow("is  InputMethodWindow  ", winName);
                return;
            }
            String owningPackage = getOwningPackage(w);
            if (ColorLongshotUtils.isScreenshotApp(owningPackage)) {
                listener.printWindow("is  ScreenshotApp      ", winName);
            } else if (ColorLongshotUtils.isExServiceUiApp(owningPackage)) {
                listener.printWindow("is  ExServiceUiApp     ", winName);
            } else {
                if (ColorLongshotUtils.isSystemUiApp(owningPackage)) {
                    if (ColorLongshotUtils.isStatusBar(w.mAttrs.type)) {
                        if (w.mAttrs.height != -1) {
                            listener.printWindow("is  SystemUi StatusBar ", winName);
                            listener.collectSystemWindows(w.mClient, winTitle, w.getSurfaceLayer(), w.mAttrs);
                            return;
                        } else if ((w.mAttrs.flags & 32) == 32) {
                            listener.printWindow("is  SystemUi StatusBar ", "HeadsUp");
                            listener.collectFloatWindows(w.mClient, winTitle, w.getSurfaceLayer(), w.mAttrs);
                            return;
                        } else {
                            listener.printWindow("is  SystemUi StatusBar ", winName);
                            isUnsupported = true;
                        }
                    } else if (ColorLongshotUtils.isSystemUiBar(w.mAttrs.type, winTitle)) {
                        listener.printWindow("is  SystemUi SystemWindow", winName);
                        listener.collectSystemWindows(w.mClient, winTitle, w.getSurfaceLayer(), w.mAttrs);
                        return;
                    } else if (!isFullScreenWindow(w, dispMetrics, realMetrics, statusbarHeight)) {
                        listener.printWindow("is  SystemUi FullWindow", winName);
                        listener.collectFloatWindows(w.mClient, winTitle, w.getSurfaceLayer(), w.mAttrs);
                        return;
                    }
                }
                if (isSmallFloatWindow(w, dispMetrics)) {
                    if (ColorLongshotUtils.isFloatAssistant(owningPackage)) {
                        listener.printWindow("is  SmallFloatAssistant", winName);
                    } else {
                        listener.printWindow("is  SmallFloatWindow   ", winName);
                        listener.collectFloatWindows(w.mClient, winTitle, w.getSurfaceLayer(), w.mAttrs);
                    }
                    return;
                }
                listener.printWindow("is  MainDisplayWindow  ", winName);
                this.mMainWindow = new ColorLongshotMainWindow(w, isUnsupported);
            }
        } else {
            listener.printWindow("not TouchableWindow    ", winName);
        }
    }

    public int getSurfaceLayerLocked(DisplayContent displayContent, int type) {
        this.mSurfaceLayer = 0;
        displayContent.forAllWindows((Consumer) new -$Lambda$_eaSO6hSZOKSnr04PN_UCoMXgDU((byte) 0, type, this), true);
        return this.mSurfaceLayer;
    }

    /* renamed from: lambda$-com_android_server_wm_ColorLongshotWindowCompatible_5973 */
    /* synthetic */ void m254lambda$-com_android_server_wm_ColorLongshotWindowCompatible_5973(int type, WindowState w) {
        if (w.mAttrs.type == type) {
            this.mSurfaceLayer = w.getSurfaceLayer();
        }
    }

    private String getOwningPackage(WindowState w) {
        String owningPackage = w.getOwningPackage();
        if (owningPackage != null) {
            return owningPackage;
        }
        WindowState parentWindow = w.getParentWindow();
        if (parentWindow != null) {
            return parentWindow.getOwningPackage();
        }
        return owningPackage;
    }

    private CharSequence getWindowTitle(WindowState w) {
        return w.mAttrs.getTitle();
    }

    private CharSequence getWindowName(WindowState w) {
        CharSequence title = getWindowTitle(w);
        if (TextUtils.isEmpty(title)) {
            return getOwningPackage(w);
        }
        return title;
    }

    private boolean isSmallSize(int value, int total) {
        return value < total / 5;
    }

    private boolean isSmallFloatWindow(WindowState w, DisplayMetrics dipMetrics) {
        Rect contentFrame = w.getContentFrameLw();
        int width = contentFrame.width();
        if (isSmallSize(contentFrame.width(), dipMetrics.widthPixels) || isSmallSize(contentFrame.height(), dipMetrics.heightPixels)) {
            return true;
        }
        return false;
    }

    private boolean isTouchableWindow(WindowState w) {
        return (w.mAttrs.flags & 16) == 0;
    }

    private boolean isFullScreenWindow(WindowState w, DisplayMetrics dispMetrics, DisplayMetrics realMetrics, int statusBarHeight) {
        Rect contentFrame = w.getContentFrameLw();
        int width = contentFrame.width();
        if (width != dispMetrics.widthPixels && width != realMetrics.widthPixels) {
            return false;
        }
        if (contentFrame.bottom != dispMetrics.heightPixels && contentFrame.bottom != realMetrics.heightPixels) {
            return false;
        }
        if (contentFrame.top == 0 || contentFrame.top == statusBarHeight) {
            return true;
        }
        return false;
    }
}
